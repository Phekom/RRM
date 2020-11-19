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
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import timber.log.Timber
import www.sanju.motiontoast.MotionToast
import za.co.xisystems.itis_rrm.BuildConfig
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.base.BaseFragment
import za.co.xisystems.itis_rrm.custom.errors.XIErrorHandler
import za.co.xisystems.itis_rrm.custom.results.XIError
import za.co.xisystems.itis_rrm.custom.results.XIProgress
import za.co.xisystems.itis_rrm.custom.results.XIResult
import za.co.xisystems.itis_rrm.custom.results.XIStatus
import za.co.xisystems.itis_rrm.custom.results.XISuccess
import za.co.xisystems.itis_rrm.custom.views.IndefiniteSnackbar
import za.co.xisystems.itis_rrm.data._commons.views.ToastUtils
import za.co.xisystems.itis_rrm.data.localDB.entities.UserDTO
import za.co.xisystems.itis_rrm.extensions.observeOnce
import za.co.xisystems.itis_rrm.ui.mainview.activities.SharedViewModel
import za.co.xisystems.itis_rrm.ui.mainview.activities.SharedViewModelFactory
import za.co.xisystems.itis_rrm.ui.scopes.UiLifecycleScope
import za.co.xisystems.itis_rrm.utils.Coroutines
import za.co.xisystems.itis_rrm.utils.ServiceUtil
import kotlin.coroutines.cancellation.CancellationException


class HomeFragment : BaseFragment(R.layout.fragment_home), KodeinAware {

    override val kodein by kodein()
    private lateinit var homeViewModel: HomeViewModel
    private val factory: HomeViewModelFactory by instance()
    private lateinit var sharedViewModel: SharedViewModel
    private val shareFactory: SharedViewModelFactory by instance()
    private var gpsEnabled: Boolean = false
    private var networkEnabled: Boolean = false
    private lateinit var userDTO: UserDTO
    private var uiScope = UiLifecycleScope()
    private lateinit var synchJob: Job
    private val colorConnected: Int
        get() = Color.parseColor("#55A359")
    private val colorNotConnected: Int
        get() = Color.RED

    private val bigSyncObserver = Observer<XIResult<Boolean>> {
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

                            group2_loading.visibility = View.VISIBLE
                            acquireUser()
                            getOfflineSectionItems()
                        } catch (t: Throwable) {
                            Timber.e(t, "Failed to fetch Section Items.")
                            val xiErr = XIError(t, t.message ?: XIErrorHandler.UNKNOWN_ERROR)
                            XIErrorHandler.crashGuard(this@HomeFragment, this@HomeFragment.requireView(), xiErr, refreshAction = { retrySections() })
                        } finally {
                            group2_loading.visibility = View.GONE
                        }
                    }
                } else {
                    motionToast(
                        getString(R.string.please_connect_to_internet_to_up_sync_offline_workflows),
                        MotionToast.TOAST_NO_INTERNET,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.LONG_DURATION
                    )
                }
            }
        }
    }

    private fun retrySections() {
        uiScope.launch(uiScope.coroutineContext) {
            IndefiniteSnackbar.hide()
            acquireUser()
            getOfflineSectionItems()
        }
    }

    private suspend fun getOfflineSectionItems() = withContext(uiScope.coroutineContext) {
        val sectionQuery = homeViewModel.offlineSectionItems.await()
        sectionQuery.observe(viewLifecycleOwner, { mSectionItem ->
            val allData = mSectionItem.count()
            if (mSectionItem.size == allData)
                group2_loading.visibility = View.GONE
        })
    }

    private suspend fun acquireUser() {
        try {
            val userJob = uiScope.launch(uiScope.coroutineContext) {

                val user = homeViewModel.user.await()
                user.observe(this@HomeFragment, { userInstance ->
                    userInstance?.let {
                        userDTO = it
                        username?.text = it.userName

                        checkConnectivity()
                        if (networkEnabled)
                            servicesHealthCheck()
                    }
                })
            }
            userJob.join()
        } catch (t: Throwable) {
            val connectErr = XIError(t, t.message ?: XIErrorHandler.UNKNOWN_ERROR)
            XIErrorHandler.crashGuard(this@HomeFragment, this@HomeFragment.requireView(), connectErr,
                refreshAction = { retryAcquireUser() })
        }
    }

    private fun retryAcquireUser() {
        IndefiniteSnackbar.hide()
        uiScope.launch(uiScope.coroutineContext) {
            checkConnectivity()
            if (networkEnabled)
                acquireUser()
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
    ): View? {
        activity?.hideKeyboard()
        return inflater.inflate(R.layout.fragment_home, container, false)
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
        // homeViewModel.databaseState.removeObservers(viewLifecycleOwner)
        uiScope.destroy()
        super.onDestroyView()
    }

    @ExperimentalStdlibApi
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        homeViewModel = activity?.run {
            ViewModelProvider(this, factory).get(HomeViewModel::class.java)
        } ?: throw Exception("Invalid Activity")

        sharedViewModel = activity?.run {
            ViewModelProvider(this, shareFactory).get(SharedViewModel::class.java)
        } ?: throw Exception("Invalid Activity")

        // Check if database is synched and prompt user if necessary
        isAppDbSynched()

        // Configure SwipeToRefresh
        initSwipeToRefresh()

        serverTextView.setOnClickListener {
            ToastUtils().toastServerAddress(requireContext())
        }

        imageView7.setOnClickListener {
            ToastUtils().toastVersion(requireContext())
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
        items_swipe_to_refresh.setProgressBackgroundColorSchemeColor(
            ContextCompat.getColor(
                requireContext().applicationContext,
                R.color.colorPrimary
            )
        )

        items_swipe_to_refresh.setColorSchemeColors(Color.WHITE)

        items_swipe_to_refresh.setOnRefreshListener {
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
            dataEnabled.setText(R.string.mobile_data_not_connected)
            dataEnabled.setTextColor(colorNotConnected)
            return false
        } else {
            dataEnabled.setText(R.string.mobile_data_connected)
            dataEnabled.setTextColor(colorConnected)
        }

        // Check if GPS connected
        return if (!gpsEnabled) {
            locationEnabled.text = requireActivity().getString(R.string.gps_not_connected)
            locationEnabled.setTextColor(colorNotConnected)
            false
        } else {
            locationEnabled.text = requireActivity().getString(R.string.gps_connected)
            locationEnabled.setTextColor(colorConnected)
            true
        }
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
                XIErrorHandler.crashGuard(
                    this@HomeFragment,
                    pingEx,
                    refreshAction = { retrySync() }
                )
            }
        }
    }

    private suspend fun handleBigSync(signal: XIResult<Boolean>?) {
        Timber.d("$signal")
        signal?.let{result ->
            when (result) {
                is XISuccess -> {
                    motionToast(
                        "Sync Complete",
                        MotionToast.TOAST_SUCCESS,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.LONG_DURATION
                    )
                    sharedViewModel.toggleLongRunning(false)
                    items_swipe_to_refresh.isRefreshing = false
                    synchJob.join()
                }
                is XIStatus -> {
                    sharedViewModel.setMessage(result.message)
                }
                is XIError -> {
                    sharedViewModel.setMessage("Sync Failed")
                    synchJob.cancel()
                    sharedViewModel.toggleLongRunning(false)
                    items_swipe_to_refresh.isRefreshing = false
                    XIErrorHandler.crashGuard(
                        view = this@HomeFragment.requireView(),
                        throwable = result,
                        refreshAction = { retrySync() }
                    )
                }
                is XIProgress -> {
                    sharedViewModel.toggleLongRunning(result.isLoading)
                    items_swipe_to_refresh.isRefreshing = result.isLoading
                }
            }
        }

    }

    private fun bigSync() = uiScope.launch(uiScope.coroutineContext) {
        try {
            sharedViewModel.setMessage("Data Loading")
            homeViewModel.databaseState?.observe(viewLifecycleOwner, bigSyncObserver)
            synchJob = homeViewModel.fetchAllData(userDTO.userId)
            synchJob.join()
            ping()
        } catch (t: Throwable) {
            XIErrorHandler.crashGuard(
                this@HomeFragment,
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
            connectedTo.text = getString(R.string.services_up, BuildConfig.VERSION_NAME)
            connectedTo.setTextColor(colorConnected)
        } else {
            connectedTo.text = getString(R.string.services_down, BuildConfig.VERSION_NAME)
            connectedTo.setTextColor(colorNotConnected)
        }
    }
}
