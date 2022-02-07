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
import androidx.navigation.Navigation
import com.skydoves.progressview.ProgressView
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.android.x.closestDI
import org.kodein.di.instance
import timber.log.Timber
import za.co.xisystems.itis_rrm.BuildConfig
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.base.BaseFragment
import za.co.xisystems.itis_rrm.constants.Constants.THIRTY_SECONDS
import za.co.xisystems.itis_rrm.constants.Constants.TWO_SECONDS
import za.co.xisystems.itis_rrm.custom.errors.XIErrorHandler
import za.co.xisystems.itis_rrm.custom.notifications.ToastDuration.LONG
import za.co.xisystems.itis_rrm.custom.notifications.ToastGravity.BOTTOM
import za.co.xisystems.itis_rrm.custom.notifications.ToastStyle
import za.co.xisystems.itis_rrm.custom.results.XIResult
import za.co.xisystems.itis_rrm.custom.results.getPercentageComplete
import za.co.xisystems.itis_rrm.custom.views.IndefiniteSnackbar
import za.co.xisystems.itis_rrm.data._commons.views.ToastUtils
import za.co.xisystems.itis_rrm.data.localDB.entities.UserDTO
import za.co.xisystems.itis_rrm.databinding.FragmentHomeBinding
import za.co.xisystems.itis_rrm.extensions.isConnected
import za.co.xisystems.itis_rrm.ui.extensions.crashGuard
import za.co.xisystems.itis_rrm.ui.extensions.extensionToast
import za.co.xisystems.itis_rrm.utils.Coroutines
import kotlin.coroutines.cancellation.CancellationException

class HomeFragment : BaseFragment() {

    override val di by closestDI()
    private lateinit var homeViewModel: HomeViewModel
    private val factory: HomeViewModelFactory by instance()
    private var gpsEnabled: Boolean = false
    private var networkEnabled: Boolean = false
    private var userDTO: UserDTO? = null
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

    private val pvComplete = HashMap<String, Runnable>()

    companion object {
        val TAG: String = HomeFragment::class.java.simpleName
    }

    private fun homeDiagnostic() {
        if (requireContext().isConnected) {
            uiScope.launch(uiScope.coroutineContext) {
                try {
                    _ui?.group2Loading?.visibility = View.VISIBLE
                    acquireUser()
                    getOfflineSectionItems()
                } catch (t: Throwable) {
                    Timber.e(t, "Failed to fetch Section Items.")
                    val xiErr = XIResult.Error(t, t.message ?: XIErrorHandler.UNKNOWN_ERROR)
                    crashGuard(
                        throwable = xiErr,
                        refreshAction = { this@HomeFragment.retrySections() }
                    )
                } finally {
                    _ui?.group2Loading?.visibility = View.GONE
                }
            }
        } else {
            noConnectionWarning()
            showProgress()
        }
    }

    private fun retrySections() {
        IndefiniteSnackbar.hide()
        uiScope.launch {
            acquireUser()
            getOfflineSectionItems()
        }
    }

    private suspend fun getOfflineSectionItems() = withContext(uiScope.coroutineContext) {
        val sectionQuery = homeViewModel.offlineSectionItems.await()
        sectionQuery.observe(viewLifecycleOwner, { mSectionItem ->
            val allData = mSectionItem.count()
            if (mSectionItem.size == allData) {
                _ui?.group2Loading?.visibility = View.GONE
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
                        _ui?.welcome?.text = getString(R.string.welcome_greeting, it.userName)
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
                refreshAction = { this.retryAcquireUser() }
            )
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
        homeViewModel =
            ViewModelProvider(this.requireActivity(), factory)[HomeViewModel::class.java]
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

        isAppDbSynched()

        _ui?.serverTextView?.setOnClickListener {
            ToastUtils().toastServerAddress(requireContext())
        }

        _ui?.imageView7?.setOnClickListener {
            ToastUtils().toastVersion(requireContext())
        }

        _ui?.unallocatedPhotoAdd?.setOnClickListener {
            val directions = HomeFragmentDirections.actionNavHomeToNavUnallocated()
            Navigation.findNavController(it)
                .navigate(directions)
        }
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
        homeViewModel.databaseState.removeObservers(viewLifecycleOwner)
    }

    private fun initProgressViews() {
        _ui?.pvContracts?.setOnProgressChangeListener {
            progressListener(it, _ui?.pvContracts!!, "projects")
        }
        _ui?.pvTasks?.setOnProgressChangeListener {
            progressListener(it, _ui?.pvTasks!!, "tasks")
        }
        _ui?.pvSections?.setOnProgressChangeListener {
            progressListener(it, _ui?.pvSections!!, "sections")
        }
    }

    private fun progressListener(it: Float, progressView: ProgressView, label: String) {
        pvComplete[label] = Runnable { maxOutPv(progressView, "$label synched") }
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed(
            {
                pvComplete[label]?.run()
            },
            THIRTY_SECONDS
        )
        when {
            it > 0f && it <= progressView.max -> {
                progressView.labelText = "$label ${it.toInt()}%"
                handler.removeCallbacks(pvComplete[label]!!)
            }
        }
    }

    private fun maxOutPv(progressView: ProgressView, completionMessage: String): Job {
        return uiScope.launch(uiScope.coroutineContext) {
            if (progressView.progress >= 0) {
                progressView.progress = progressView.max
                progressView.labelText = completionMessage
                Handler(Looper.getMainLooper()).postDelayed({
                    progressView.visibility = View.GONE
                }, TWO_SECONDS)
            }
        }
    }

    private fun resetProgressViews() {
        _ui?.pvContracts?.progress = 0.0f
        _ui?.pvTasks?.progress = 0.0f
        _ui?.pvSections?.progress = 0.0f
    }

    private fun isAppDbSynched() {
        uiScope.launch(uiScope.coroutineContext) {
            homeViewModel.bigSyncDone.observe(viewLifecycleOwner, {
                Timber.d("Synced: $it")
                if (!it) {
                    promptUserToSync()
                }
            })
            homeViewModel.bigSyncCheck()
        }
    }

    private fun initSwipeToRefresh() {
        _ui?.itemsSwipeToRefresh?.setProgressBackgroundColorSchemeColor(
            ContextCompat.getColor(
                requireContext().applicationContext,
                R.color.colorPrimary
            )
        )

        _ui?.itemsSwipeToRefresh?.setColorSchemeColors(Color.WHITE)

        _ui?.itemsSwipeToRefresh?.setOnRefreshListener {
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
            _ui?.dataEnabled?.setText(R.string.mobile_data_not_connected)
            _ui?.ivConnection?.setImageDrawable(
                ContextCompat.getDrawable(
                    this.requireContext(),
                    R.drawable.ic_baseline_signal_cellular_connected_no_internet_32
                ).also { drawable ->
                    drawable?.setTint(colorNotConnected)
                }
            )
            result = false
        } else {
            _ui?.dataEnabled?.setText(R.string.mobile_data_connected)
            _ui?.ivConnection?.setImageDrawable(
                ContextCompat.getDrawable(
                    this.requireContext(),
                    R.drawable.ic_baseline_signal_cellular_connected_32
                ).also { drawable ->
                    drawable?.setTint(colorConnected)
                }
            )
        }

        // Check if GPS connected
        if (!gpsEnabled) {
            _ui?.locationEnabled?.text = requireActivity().getString(R.string.gps_not_connected)
            _ui?.ivLocation?.setImageDrawable(
                ContextCompat.getDrawable(
                    this.requireContext(),
                    R.drawable.ic_baseline_location_off_24
                ).also { drawable ->
                    drawable?.setTint(colorNotConnected)
                }
            )
        } else {
            _ui?.locationEnabled?.text = requireActivity().getString(R.string.gps_connected)
            _ui?.ivLocation?.setImageDrawable(
                ContextCompat.getDrawable(
                    this.requireContext(),
                    R.drawable.ic_baseline_location_on_24
                ).also { drawable ->
                    drawable?.setTint(colorConnected)
                }
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
                        style = ToastStyle.SUCCESS,
                        position = BOTTOM,
                        duration = LONG
                    )
                }
                is XIResult.Status -> {
                    extensionToast(message = result.message, style = ToastStyle.INFO, position = BOTTOM)
                }
                is XIResult.Error -> {
                    synchJob.cancel(CancellationException(result.message))
                    showProgress()

                    extensionToast(
                        title = "Sync Failed",
                        message = result.message,
                        style = ToastStyle.ERROR
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
        _ui?.itemsSwipeToRefresh?.isRefreshing = isLoading
        when (isLoading) {
            true -> {
                _ui?.bigSyncProgressLayout?.visibility = View.VISIBLE
            }
            else -> {
                _ui?.bigSyncProgressLayout?.visibility = View.GONE
            }
        }
    }

    private fun handleProgressUpdate(update: XIResult.ProgressUpdate) {
        when (update.key) {
            "projects" -> {
                updateProgress(update, _ui?.pvContracts!!)
            }
            "tasks" -> {
                updateProgress(update, _ui?.pvTasks!!)
            }
            "sections" -> {
                updateProgress(update, _ui?.pvSections!!)
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
                _ui?.unallocatedPhotoAdd?.visibility = View.GONE
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
            } finally {
                homeViewModel.resetSyncStatus()
                // Gallery is deferred until the new year
                _ui?.unallocatedPhotoAdd?.visibility = View.VISIBLE
            }
        } else {
            noInternetWarning()
            showProgress()
        }
    }

    private fun promptUserToSync() = Coroutines.ui {
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
            homeViewModel.healthState.observe(viewLifecycleOwner, { outcome ->
                outcome.getContentIfNotHandled()?.let { result ->
                    processHealthCheck(result)
                }
            })
            homeViewModel.healthCheck(userDTO!!.userId)
        }
    }

    private fun processHealthCheck(result: XIResult<Boolean>) {
        when (result) {
            is XIResult.Success<Boolean> -> {
                updateServiceHealth(result.data)
            }
            is XIResult.Error -> {
                updateServiceHealth(false)
                crashGuard(
                    throwable = result,
                    refreshAction = { this@HomeFragment.retryHealthCheck() }
                )
            }
            else -> {
                Timber.d("$result")
            }
        }
    }

    private fun retryHealthCheck() {
        IndefiniteSnackbar.hide()
        servicesHealthCheck()
    }

    private fun updateServiceHealth(serviceHealthy: Boolean) {
        when (serviceHealthy) {
            true -> {
                _ui?.connectedTo?.text = getString(R.string.services_up, BuildConfig.VERSION_NAME)
                _ui?.ivCloud?.setImageDrawable(
                    ContextCompat.getDrawable(
                        this@HomeFragment.requireContext(),
                        R.drawable.ic_baseline_services_up
                    ).also { drawable ->
                        drawable?.setTint(colorConnected)
                    }
                )
            }
            else -> {
                _ui?.connectedTo?.text = getString(R.string.services_down, BuildConfig.VERSION_NAME)
                _ui?.ivCloud?.setImageDrawable(
                    ContextCompat.getDrawable(
                        this@HomeFragment.requireContext(),
                        R.drawable.ic_baseline_services_down
                    ).also { drawable ->
                        drawable?.setTint(colorNotConnected)
                    }
                )
            }
        }
    }
}
