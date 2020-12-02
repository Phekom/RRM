@file:Suppress("KDocUnresolvedReference")

package za.co.xisystems.itis_rrm.ui.mainview.home

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenStarted
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import timber.log.Timber
import za.co.xisystems.itis_rrm.BuildConfig
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.base.BaseFragment
import za.co.xisystems.itis_rrm.custom.errors.XIErrorHandler
import za.co.xisystems.itis_rrm.custom.results.XIError
import za.co.xisystems.itis_rrm.custom.results.XIProgress
import za.co.xisystems.itis_rrm.custom.results.XIProgressUpdate
import za.co.xisystems.itis_rrm.custom.results.XIResult
import za.co.xisystems.itis_rrm.custom.results.XIStatus
import za.co.xisystems.itis_rrm.custom.results.XISuccess
import za.co.xisystems.itis_rrm.custom.results.getPercentageComplete
import za.co.xisystems.itis_rrm.custom.views.IndefiniteSnackbar
import za.co.xisystems.itis_rrm.data._commons.views.ToastUtils
import za.co.xisystems.itis_rrm.data.localDB.entities.UserDTO
import za.co.xisystems.itis_rrm.databinding.FragmentHomeBinding
import za.co.xisystems.itis_rrm.extensions.observeOnce
import za.co.xisystems.itis_rrm.ui.scopes.UiLifecycleScope
import za.co.xisystems.itis_rrm.utils.Coroutines
import za.co.xisystems.itis_rrm.utils.ServiceUtil
import za.co.xisystems.itis_rrm.utils.enums.ToastDuration.LONG
import za.co.xisystems.itis_rrm.utils.enums.ToastGravity.BOTTOM
import za.co.xisystems.itis_rrm.utils.enums.ToastStyle.ERROR
import za.co.xisystems.itis_rrm.utils.enums.ToastStyle.INFO
import za.co.xisystems.itis_rrm.utils.enums.ToastStyle.NO_INTERNET
import za.co.xisystems.itis_rrm.utils.enums.ToastStyle.SUCCESS
import kotlin.coroutines.cancellation.CancellationException

class HomeFragment : BaseFragment(R.layout.fragment_home), KodeinAware {

    override val kodein by kodein()
    private lateinit var homeViewModel: HomeViewModel
    private val factory: HomeViewModelFactory by instance()
    private var gpsEnabled: Boolean = false
    private var networkEnabled: Boolean = false
    private lateinit var userDTO: UserDTO
    private var uiScope = UiLifecycleScope()
    private lateinit var synchJob: Job
    private val colorConnected: Int
        get() = Color.parseColor("#55A359")
    private val colorNotConnected: Int
        get() = Color.RED

    // viewBinding implementation
    private var _ui: FragmentHomeBinding? = null
    private val ui get() = _ui!!

    // observer implementation
    private val bigSyncObserver = Observer<XIResult<Boolean>?> {
        Coroutines.main {
            handleBigSync(it)
        }
    }

    init {

        lifecycleScope.launch {
            whenStarted {
                lifecycle.addObserver(uiScope)
                checkConnectivity()

                if (networkEnabled) {
                    uiScope.launch(uiScope.coroutineContext) {
                        try {

                            ui.group2Loading.visibility = View.VISIBLE
                            acquireUser()
                            getOfflineSectionItems()
                        } catch (t: Throwable) {
                            Timber.e(t, "Failed to fetch Section Items.")
                            val xiErr = XIError(t, t.message ?: XIErrorHandler.UNKNOWN_ERROR)
                            crashGuard(this@HomeFragment.requireView(), xiErr, refreshAction = { retrySections() })
                        } finally {
                            ui.group2Loading.visibility = View.GONE
                        }
                    }
                } else {
                    sharpToast(
                        message = "Please connect to internet via mobile or wifi to download contract and project information",
                        style = NO_INTERNET,
                        position = BOTTOM,
                        duration = LONG
                    )
                }
            }
        }
    }

    private fun retrySections() {
        IndefiniteSnackbar.hide()
        uiScope.launch(uiScope.coroutineContext) {
            acquireUser()
            getOfflineSectionItems()
        }
    }

    private suspend fun getOfflineSectionItems() = withContext(uiScope.coroutineContext) {
        val sectionQuery = homeViewModel.offlineSectionItems.await()
        sectionQuery.observe(viewLifecycleOwner, { mSectionItem ->
            val allData = mSectionItem.count()
            if (mSectionItem.size == allData) {
                ui.group2Loading.visibility = View.GONE
            }
        })
    }

    private suspend fun acquireUser() {
        try {
            val userJob = uiScope.launch(uiScope.coroutineContext) {

                val user = homeViewModel.user.await()
                user.observe(this@HomeFragment, { userInstance ->
                    userInstance.let {
                        userDTO = it
                        ui.welcome.text = getString(R.string.welcome_greeting, it.userName)

                        checkConnectivity()
                        if (networkEnabled) {
                            servicesHealthCheck()
                        }
                    }
                })
            }
            userJob.join()
        } catch (t: Throwable) {
            val errorMessage = "Failed to load user: ${t.message ?: XIErrorHandler.UNKNOWN_ERROR}"
            Timber.e(t, errorMessage)
            val connectErr = XIError(t, errorMessage)
            crashGuard(
                view = this@HomeFragment.requireView(),
                throwable = connectErr,
                refreshAction = { retryAcquireUser() })
        }
    }

    private fun retryAcquireUser() {
        IndefiniteSnackbar.hide()
        uiScope.launch(uiScope.coroutineContext) {
            checkConnectivity()
            if (networkEnabled) {
                acquireUser()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        uiScope.onCreate()
        lifecycle.addObserver(uiScope)
        setHasOptionsMenu(true)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        val item = menu.findItem(R.id.action_search)
        if (item != null) item.isVisible = false
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        activity?.menuInflater?.inflate(R.menu.search, menu)
        return true
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        activity?.hideKeyboard()
        // Init viewBinding - note the use of inflater here
        _ui = FragmentHomeBinding.inflate(inflater, container, false)
        return ui.root
    }

    /**
     * Called when the view previously created by [.onCreateView] has
     * been detached from the fragment.  The next time the fragment needs
     * to be displayed, a new view will be created.  This is called
     * after [.onStop] and before [.onDestroy].  It is called
     * *regardless* of whether [.onCreateView] returned a
     * non-null view.  Internally it is called after the view's state has
     * been saved but before it has been removed from its parent.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        // homeViewModel.databaseState.removeObservers(viewLifecycleOwner)
        uiScope.destroy()
        // prevent viewBinding from leaking
        _ui = null
    }

    @ExperimentalStdlibApi
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        homeViewModel = activity?.run {
            ViewModelProvider(this, factory).get(HomeViewModel::class.java)
        } ?: throw Exception("Invalid Activity")

        // Check if database is synched and prompt user if necessary
        initProgressViews()
        isAppDbSynched()

        // Configure SwipeToRefresh
        initSwipeToRefresh()

        ui.serverTextView.setOnClickListener {
            ToastUtils().toastServerAddress(requireContext())
        }

        ui.imageView7.setOnClickListener {
            ToastUtils().toastVersion(requireContext())
        }
    }

    private fun initProgressViews() {
        ui.pvContracts.setOnProgressChangeListener {
            ui.pvContracts.labelText = "projects ${it.toInt()}%"
        }
        ui.pvTasks.setOnProgressChangeListener {
            ui.pvTasks.labelText = "tasks ${it.toInt()}%"
        }
    }

    private fun isAppDbSynched() {
        uiScope.launch(uiScope.coroutineContext) {
            homeViewModel.bigSyncCheck()
            homeViewModel.bigSyncDone.observeOnce(viewLifecycleOwner, {
                Timber.d("Synced: $it")
                if (!it) {
                    promptUserToSync()
                }
            })
        }
    }

    @ExperimentalStdlibApi
    private fun initSwipeToRefresh() {
        ui.itemsSwipeToRefresh.setProgressBackgroundColorSchemeColor(
            ContextCompat.getColor(
                requireContext().applicationContext,
                R.color.colorPrimary
            )
        )

        ui.itemsSwipeToRefresh.setColorSchemeColors(Color.WHITE)

        ui.itemsSwipeToRefresh.setOnRefreshListener {
            Coroutines.main {
                bigSync()
            }
        }
    }

    private fun checkConnectivity(): Boolean {
        val lm = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        networkEnabled = ServiceUtil.isNetworkAvailable(requireActivity().applicationContext)
        //  Check if Network Enabled
        if (!networkEnabled) {
            ui.dataEnabled.setText(R.string.mobile_data_not_connected)
            ui.dataEnabled.setTextColor(colorNotConnected)
            ui.ivConnection.setImageDrawable(
                ContextCompat.getDrawable(
                    this.requireContext(),
                    R.drawable.ic_baseline_signal_cellular_connected_no_internet_32
                )
            )
            return false
        } else {
            ui.dataEnabled.setText(R.string.mobile_data_connected)
            ui.dataEnabled.setTextColor(colorConnected)
            ui.ivConnection.setImageDrawable(
                ContextCompat.getDrawable(
                    this.requireContext(),
                    R.drawable.ic_baseline_signal_cellular_connected_32
                )
            )
        }

        // Check if GPS connected
        if (!gpsEnabled) {
            ui.locationEnabled.text = requireActivity().getString(R.string.gps_not_connected)
            ui.locationEnabled.setTextColor(colorNotConnected)
            ui.ivLocation.setImageDrawable(
                ContextCompat.getDrawable(
                    this.requireContext(),
                    R.drawable.ic_baseline_location_off_24
                )
            )
            return false
        } else {
            ui.locationEnabled.text = requireActivity().getString(R.string.gps_connected)
            ui.locationEnabled.setTextColor(colorConnected)
            ui.ivLocation.setImageDrawable(
                ContextCompat.getDrawable(
                    this.requireContext(),
                    R.drawable.ic_baseline_location_on_24
                )
            )
        }
        return true
    }

    private fun retrySync() {
        IndefiniteSnackbar.hide()
        bigSync()
    }

    private fun ping() {
        uiScope.launch(uiScope.coroutineContext) {
            try {
                if (networkEnabled) {
                    acquireUser()
                } else {
                    synchJob.cancel(CancellationException("Connectivity lost ... please try again later"))
                }
            } catch (t: Throwable) {
                val pingEx = XIError(t, t.message ?: XIErrorHandler.UNKNOWN_ERROR)
                crashGuard(
                    view = this@HomeFragment.requireView(),
                    throwable = pingEx,
                    refreshAction = { retrySync() }
                )
            }
        }
    }

    private suspend fun handleBigSync(signal: XIResult<Boolean>?) {
        Timber.d("$signal")
        signal?.let { result ->
            when (result) {
                is XISuccess -> {
                    sharpToast(
                        message = "Sync Complete",
                        style = SUCCESS,
                        position = BOTTOM,
                        duration = LONG
                    )
                    toggleLongRunning(false)
                    ui.itemsSwipeToRefresh.isRefreshing = false
                    ui.bigsyncProgressLayout.visibility = View.GONE
                    synchJob.join()
                }
                is XIStatus -> {
                    sharpToast(message = result.message, style = INFO, position = BOTTOM)
                }
                is XIError -> {
                    sharpToast(
                        title = "Sync Failed",
                        message = result.message,
                        style = ERROR
                    )
                    synchJob.cancel(CancellationException(result.message))
                    toggleLongRunning(false)
                    ui.itemsSwipeToRefresh.isRefreshing = false
                    crashGuard(
                        view = this@HomeFragment.requireView(),
                        throwable = result,
                        refreshAction = { retrySync() }
                    )
                }
                is XIProgress -> {
                    toggleLongRunning(result.isLoading)
                    ui.itemsSwipeToRefresh.isRefreshing = result.isLoading
                    when (result.isLoading) {
                        true -> {
                            ui.bigsyncProgressLayout.visibility = View.VISIBLE
                        }
                        else -> {
                            ui.bigsyncProgressLayout.visibility = View.GONE
                        }
                    }
                }
                is XIProgressUpdate -> {
                    handleProgressUpdate(result)
                }
            }
        }
    }

    private fun handleProgressUpdate(update: XIProgressUpdate) {
        when (update.key) {
            "contracts" -> {
                ui.pvContracts.progress = update.getPercentageComplete()
            }
            "tasks" -> {
                ui.pvTasks.progress = update.getPercentageComplete()
            }
        }
    }

    private fun bigSync() = uiScope.launch(uiScope.coroutineContext) {
        try {
            toast("Data Loading")
            ui.bigsyncProgressLayout.visibility = View.VISIBLE
            homeViewModel.databaseState.observe(viewLifecycleOwner, bigSyncObserver)
            synchJob = homeViewModel.fetchAllData(userDTO.userId)
            synchJob.join()
            ping()
        } catch (t: Throwable) {
            crashGuard(
                this@HomeFragment.requireView(),
                XIError(t, t.message ?: XIErrorHandler.UNKNOWN_ERROR),
                refreshAction = { retrySync() }
            )
        }
    }

    private fun promptUserToSync() {
        val syncDialog: AlertDialog.Builder =
            AlertDialog.Builder(activity) // android.R.style.Theme_DeviceDefault_Dialog
                .setTitle(
                    "Initial Synchronisation"
                )
                .setMessage(getString(R.string.needs_dbsynch))
                .setCancelable(false)
                .setIcon(R.drawable.ic_baseline_cloud_download_24)
                .setPositiveButton(R.string.ok) { _, _ ->
                    Coroutines.main {
                        bigSync()
                    }
                }

        syncDialog.show()
    }

    companion object {
        val TAG: String = HomeFragment::class.java.simpleName
    }

    private fun servicesHealthCheck() = uiScope.launch(uiScope.coroutineContext) {
        if (networkEnabled && homeViewModel.healthCheck(userDTO.userId)) {
            ui.connectedTo.text = getString(R.string.services_up, BuildConfig.VERSION_NAME)
            ui.connectedTo.setTextColor(colorConnected)
            ui.ivCloud.setImageDrawable(
                ContextCompat.getDrawable(
                    this@HomeFragment.requireContext(),
                    R.drawable.ic_baseline_services_up
                )
            )
        } else {
            ui.connectedTo.text = getString(R.string.services_down, BuildConfig.VERSION_NAME)
            ui.connectedTo.setTextColor(colorNotConnected)
            ui.ivCloud.setImageDrawable(
                ContextCompat.getDrawable(
                    this@HomeFragment.requireContext(),
                    R.drawable.ic_baseline_services_down
                )
            )
        }
    }
}
