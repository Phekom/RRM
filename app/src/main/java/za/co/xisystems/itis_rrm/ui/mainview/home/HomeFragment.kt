/**
 * Updated by Shaun McDonald on 2021/05/15
 * Last modified on 2021/05/14, 20:32
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 **/

/**
 * Updated by Shaun McDonald on 2021/05/14
 * Last modified on 2021/05/14, 19:43
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 **/

@file:Suppress("KDocUnresolvedReference", "Annotator")

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
import com.skydoves.progressview.ProgressView
import kotlin.coroutines.cancellation.CancellationException
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
import za.co.xisystems.itis_rrm.utils.enums.ToastStyle.SUCCESS

class HomeFragment : BaseFragment(), KodeinAware {

    override val kodein by kodein()
    private lateinit var homeViewModel: HomeViewModel
    private val factory: HomeViewModelFactory by instance()
    private var gpsEnabled: Boolean = false
    private var networkEnabled: Boolean = false
    private var userDTO: UserDTO? = null
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
            }
        }
    }

    private fun homeDiagnostic() {
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
            noConnectionWarning()
            showProgress()
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

    /**
     * Called when the Fragment is visible to the user.  This is generally
     * tied to [Activity.onStart] of the containing
     * Activity's lifecycle.
     */
    override fun onStart() {
        super.onStart()
    }

    private suspend fun acquireUser() {
        try {
            val userJob = uiScope.launch(uiScope.coroutineContext) {

                val user = homeViewModel.user.await()
                user.observe(viewLifecycleOwner, { userInstance ->
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        homeViewModel = ViewModelProvider(this, factory).get(HomeViewModel::class.java)
        checkConnectivity()
        homeDiagnostic()
        // Configure progressView
        initProgressViews()

        // Configure SwipeToRefresh
        initSwipeToRefresh()

        ui.serverTextView.setOnClickListener {
            ToastUtils().toastServerAddress(requireContext())
        }

        ui.imageView7.setOnClickListener {
            ToastUtils().toastVersion(requireContext())
        }

        // Check if database is synched and prompt user if necessary
        isAppDbSynched()
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
        // prevent viewBinding from leaking
        _ui = null
        uiScope.destroy()
        homeViewModel.databaseState.removeObservers(viewLifecycleOwner)
    }

    @ExperimentalStdlibApi
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    private fun initProgressViews() {

        ui.pvContracts.setOnProgressChangeListener {
            ui.pvContracts.labelText = "projects ${it.toInt()}%"
        }
        ui.pvTasks.setOnProgressChangeListener {
            ui.pvTasks.labelText = "tasks ${it.toInt()}%"
        }
        ui.pvSections.setOnProgressChangeListener {
            ui.pvSections.labelText = "sections ${it.toInt()}%"
        }
    }

    private fun resetProgressViews() {
        ui.pvContracts.progress = 0.0f
        ui.pvTasks.progress = 0.0f
        ui.pvSections.progress = 0.0f
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
        var result = true
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
            result = false
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

        return result
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
                    refreshAction = { this@HomeFragment.retrySync() }
                )
            }
        }
    }

    private fun handleBigSync(signal: XIResult<Boolean>?) {
        Timber.d("$signal")
        signal?.let { result ->
            when (result) {
                is XISuccess -> {
                    showProgress()
                    sharpToast(
                        message = "Sync Complete",
                        style = SUCCESS,
                        position = BOTTOM,
                        duration = LONG
                    )
                }
                is XIStatus -> {
                    sharpToast(message = result.message, style = INFO, position = BOTTOM)
                }
                is XIError -> {
                    synchJob.cancel(CancellationException(result.message))
                    showProgress()

                    sharpToast(
                        title = "Sync Failed",
                        message = result.message,
                        style = ERROR
                    )

                    crashGuard(
                        view = this@HomeFragment.requireView(),
                        throwable = result,
                        refreshAction = { this@HomeFragment.retrySync() }
                    )
                }
                is XIProgress -> {
                    showProgress(result.isLoading)
                }
                is XIProgressUpdate -> {
                    handleProgressUpdate(result)
                }
            }
        }
    }

    private fun showProgress(
        isLoading: Boolean = false
    ) {
        toggleLongRunning(isLoading)
        ui.itemsSwipeToRefresh.isRefreshing = isLoading
        when (isLoading) {
            true -> {
                ui.bigSyncProgressLayout.visibility = View.VISIBLE
            }
            else -> {
                ui.bigSyncProgressLayout.visibility = View.GONE
            }
        }
    }

    private fun handleProgressUpdate(update: XIProgressUpdate) {
        when (update.key) {
            "projects" -> {
                updateProgress(update, ui.pvContracts)
            }
            "tasks" -> {
                updateProgress(update, ui.pvTasks)
            }
            "sections" -> {
                updateProgress(update, ui.pvSections)
            }
        }
    }

    private fun updateProgress(update: XIProgressUpdate, progressView: ProgressView) {
        if (progressView.visibility != View.VISIBLE) {
            progressView.visibility = View.VISIBLE
        }
        if (update.getPercentageComplete() > progressView.progress) {
            progressView.progress = update.getPercentageComplete()
        }
    }

    private fun bigSync() = uiScope.launch(uiScope.coroutineContext) {
        if (networkEnabled) {
            try {

                toast("Data Loading")
                resetProgressViews()
                showProgress(true)
                homeViewModel.databaseState.observe(viewLifecycleOwner, bigSyncObserver)
                synchJob = homeViewModel.fetchAllData(userDTO!!.userId)
                synchJob.join()
                ping()
            } catch (t: Throwable) {
                val errorMessage = "Failed to download remote data: ${t.message ?: XIErrorHandler.UNKNOWN_ERROR}"
                Timber.e(t, errorMessage)
                crashGuard(
                    this@HomeFragment.requireView(),
                    XIError(t, errorMessage),
                    refreshAction = { retrySync() }
                )
            }
        } else {
            noInternetWarning()
            showProgress()
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
                    uiScope.launch {
                        bigSync()
                    }
                }

        syncDialog.show()
    }

    private fun servicesHealthCheck() = uiScope.launch(uiScope.coroutineContext) {
        if (!activity?.isFinishing!!) {
            try {

                if (userDTO != null && networkEnabled && homeViewModel.healthCheck(userDTO!!.userId)) {
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
            } catch (t: Throwable) {
                val message = "Could not check service health: ${t.message ?: XIErrorHandler.UNKNOWN_ERROR}"
                Timber.e(t, message)
                this@HomeFragment.view?.let {
                    val fetchError = XIError(t, message)
                    crashGuard(
                        this@HomeFragment.requireView(),
                        fetchError,
                        refreshAction = { this@HomeFragment.retryHealthCheck() }
                    )
                }
            }
        }
    }

    private fun retryHealthCheck() {
        IndefiniteSnackbar.hide()
        servicesHealthCheck()
    }

    companion object {
        val TAG: String = HomeFragment::class.java.simpleName
    }
}
