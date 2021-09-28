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
import android.os.Handler
import android.os.Looper
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.DIAware
import org.kodein.di.android.x.closestDI
import org.kodein.di.instance
import timber.log.Timber
import za.co.xisystems.itis_rrm.BuildConfig
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.base.BaseFragment
import za.co.xisystems.itis_rrm.constants.Constants.TWO_SECONDS
import za.co.xisystems.itis_rrm.custom.errors.XIErrorHandler
import za.co.xisystems.itis_rrm.custom.notifications.ToastDuration.LONG
import za.co.xisystems.itis_rrm.custom.notifications.ToastGravity.BOTTOM
import za.co.xisystems.itis_rrm.custom.notifications.ToastStyle.ERROR
import za.co.xisystems.itis_rrm.custom.notifications.ToastStyle.INFO
import za.co.xisystems.itis_rrm.custom.notifications.ToastStyle.SUCCESS
import za.co.xisystems.itis_rrm.custom.results.XIResult
import za.co.xisystems.itis_rrm.custom.results.getPercentageComplete
import za.co.xisystems.itis_rrm.custom.views.IndefiniteSnackbar
import za.co.xisystems.itis_rrm.data._commons.views.ToastUtils
import za.co.xisystems.itis_rrm.data.localDB.entities.UserDTO
import za.co.xisystems.itis_rrm.databinding.FragmentHomeBinding
import za.co.xisystems.itis_rrm.extensions.isConnected
import za.co.xisystems.itis_rrm.extensions.observeOnce
import za.co.xisystems.itis_rrm.ui.extensions.crashGuard
import za.co.xisystems.itis_rrm.ui.extensions.extensionToast
import za.co.xisystems.itis_rrm.ui.scopes.UiLifecycleScope
import za.co.xisystems.itis_rrm.utils.Coroutines
import kotlin.coroutines.cancellation.CancellationException

class HomeFragment : BaseFragment(), DIAware {

    override val di by closestDI()
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

    companion object {
        val TAG: String = HomeFragment::class.java.simpleName
        private const val progressComplete = -100.0f
    }

    init {
        lifecycleScope.launch {
            whenStarted {
                lifecycle.addObserver(uiScope)
            }
        }
    }

    private fun homeDiagnostic() {
        if (requireContext().isConnected) {
            uiScope.launch(uiScope.coroutineContext) {
                try {
                    ui.group2Loading.visibility = View.VISIBLE
                    acquireUser()
                    getOfflineSectionItems()
                } catch (t: Throwable) {
                    Timber.e(t, "Failed to fetch Section Items.")
                    val xiErr = XIResult.Error(t, t.message ?: XIErrorHandler.UNKNOWN_ERROR)
                    crashGuard(
                        throwable = xiErr,
                        refreshAction = { this@HomeFragment.retrySections() })
                } finally {
                    ui.group2Loading.visibility = View.GONE
                }
            }
        } else {
            noConnectionWarning()
            showProgress()
        }
    }

    fun retrySections() {
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
            val connectErr = XIResult.Error(t, errorMessage)
            crashGuard(
                throwable = connectErr,
                refreshAction = { this.retryAcquireUser() })
        }
    }

    fun retryAcquireUser() {
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
        homeViewModel = ViewModelProvider(this.requireActivity(), factory).get(HomeViewModel::class.java)
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

    override fun onStart() {

        super.onStart()
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        homeViewModel = ViewModelProvider(this.requireActivity(), factory).get(HomeViewModel::class.java)
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

    private fun initProgressViews() {

        ui.pvContracts.setOnProgressChangeListener {
            when {
                it >= 0f -> ui.pvContracts.labelText = "projects ${it.toInt()}%"
                it == progressComplete -> {
                    maxOutPv(ui.pvContracts, "projects synched")
                }
            }
            ui.pvSections.visibility = View.GONE
            ui.pvTasks.visibility = View.GONE
        }
        ui.pvTasks.setOnProgressChangeListener {
            when {
                it >= 0f -> ui.pvTasks.labelText = "tasks ${it.toInt()}%"
                it == progressComplete -> {
                    maxOutPv(ui.pvTasks, "tasks synched")
                }
            }
            ui.pvContracts.visibility = View.GONE
            ui.pvSections.visibility = View.GONE
        }
        ui.pvSections.setOnProgressChangeListener {
            when {
                it >= 0 -> ui.pvSections.labelText = "goods and services ${it.toInt()}%"
                it == progressComplete -> {
                    maxOutPv(ui.pvSections, "goods synched")
                }
            }
            ui.pvSections.visibility = View.GONE
            ui.pvTasks.visibility = View.GONE
        }
    }

    private fun maxOutPv(progressView: ProgressView, completionMessage: String) {
        if (progressView.progress >= 0) {
            progressView.progress = progressView.max
            progressView.labelText = completionMessage
            Handler(Looper.getMainLooper()).postDelayed({
                progressView.visibility = View.GONE
            }, TWO_SECONDS)
        }
    }

    private fun resetProgressViews() {
        ui.pvContracts.progress = 0.0f
        ui.pvTasks.progress = 0.0f
        ui.pvSections.progress = 0.0f
    }

    private fun isAppDbSynched() {
        uiScope.launch(uiScope.coroutineContext) {
            homeViewModel.bigSyncDone.observeOnce(viewLifecycleOwner, {
                Timber.d("Synced: $it")
                if (!it) {
                    promptUserToSync()
                }
            })
            homeViewModel.bigSyncCheck()
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
        networkEnabled = this.requireContext().isConnected
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

    fun retrySync() {
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
                val pingEx = XIResult.Error(t, t.message ?: XIErrorHandler.UNKNOWN_ERROR)
                crashGuard(
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
                is XIResult.Success -> {
                    showProgress()
                    extensionToast(
                        message = "Sync Complete",
                        style = SUCCESS,
                        position = BOTTOM,
                        duration = LONG
                    )
                }
                is XIResult.Status -> {
                    extensionToast(message = result.message, style = INFO, position = BOTTOM)
                }
                is XIResult.Error -> {
                    synchJob.cancel(CancellationException(result.message))
                    showProgress()

                    extensionToast(
                        title = "Sync Failed",
                        message = result.message,
                        style = ERROR
                    )

                    crashGuard(
                        throwable = result,
                        refreshAction = { this@HomeFragment.retrySync() }
                    )
                }
                is XIResult.Progress -> {
                    showProgress(result.isLoading)
                }
                is XIResult.ProgressUpdate -> {
                    handleProgressUpdate(result)
                }
                is XIResult.RestException -> {
                    Timber.e("$result")
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

    private fun handleProgressUpdate(update: XIResult.ProgressUpdate) {
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

    private fun updateProgress(update: XIResult.ProgressUpdate, progressView: ProgressView) {
        if (progressView.visibility != View.VISIBLE) {
            progressView.visibility = View.VISIBLE
        }
        progressView.progress = update.getPercentageComplete()
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
                    throwable = XIResult.Error(t, errorMessage),
                    refreshAction = { this@HomeFragment.retrySync() }
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
        if (!activity?.isFinishing!! && userDTO != null && networkEnabled) {
            homeViewModel.healthState.observe(viewLifecycleOwner, { result ->
                when (result) {
                    is XIResult.Success -> {
                        updateServiceHealth(result.data)
                    }
                    is XIResult.Error -> {
                        crashGuard(
                            throwable = result,
                            refreshAction = { this@HomeFragment.retryHealthCheck() }
                        )
                    }
                    else -> {
                        Timber.d("$result")
                    }
                }
            })
            homeViewModel.healthCheck(userDTO!!.userId)
        }
    }

    fun retryHealthCheck() {
        IndefiniteSnackbar.hide()
        servicesHealthCheck()
    }

    private fun updateServiceHealth(serviceHealthy: Boolean) {
        when (serviceHealthy) {
            true -> {
                ui.connectedTo.text = getString(R.string.services_up, BuildConfig.VERSION_NAME)
                ui.connectedTo.setTextColor(colorConnected)
                ui.ivCloud.setImageDrawable(
                    ContextCompat.getDrawable(
                        this@HomeFragment.requireContext(),
                        R.drawable.ic_baseline_services_up
                    )
                )
            }
            else -> {
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
}
