package za.co.xisystems.itis_rrm.ui.mainview.home

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.location.LocationManager
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenResumed
import androidx.lifecycle.whenStarted
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import timber.log.Timber
import za.co.xisystems.itis_rrm.BuildConfig
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.base.BaseFragment
import za.co.xisystems.itis_rrm.custom.errors.NoConnectivityException
import za.co.xisystems.itis_rrm.custom.errors.NoInternetException
import za.co.xisystems.itis_rrm.custom.errors.XIErrorHandler
import za.co.xisystems.itis_rrm.custom.results.XIError
import za.co.xisystems.itis_rrm.custom.results.XIStatus
import za.co.xisystems.itis_rrm.custom.results.XISuccess
import za.co.xisystems.itis_rrm.custom.results.isConnectivityError
import za.co.xisystems.itis_rrm.data._commons.views.ToastUtils
import za.co.xisystems.itis_rrm.data.localDB.entities.UserDTO
import za.co.xisystems.itis_rrm.extensions.observeOnce
import za.co.xisystems.itis_rrm.ui.custom.IndefiniteSnackbar
import za.co.xisystems.itis_rrm.ui.mainview.activities.SharedViewModel
import za.co.xisystems.itis_rrm.ui.mainview.activities.SharedViewModelFactory
import za.co.xisystems.itis_rrm.ui.scopes.UiLifecycleScope
import za.co.xisystems.itis_rrm.utils.Coroutines

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
    private val colorConnected: Int
        get() = Color.parseColor("#55A359")
    private val colorNotConnected: Int
        get() = Color.RED

    init {

        lifecycleScope.launch {
            whenStarted {

                uiScope.launch(uiScope.coroutineContext) {
                    try {

                        group2_loading.visibility = View.VISIBLE

                        acquireUser()

                        val contracts = homeViewModel.offlineSectionItems.await()
                        contracts.observe(viewLifecycleOwner, { mSectionItem ->
                            val allData = mSectionItem.count()
                            if (mSectionItem.size == allData)
                                group2_loading.visibility = View.GONE
                        })
                    } catch (t: Throwable) {
                        Timber.e(t, t.message ?: XIErrorHandler.UNKNOWN_ERROR)
                        val xiErr = XIError(t, "Failed to load SectionItem")
                        handleBigSyncError(xiErr)
                    } catch (e: NoInternetException) {
                        ToastUtils().toastLong(activity, e.message)
                        Timber.e(e, "No Internet Connection")
                    } catch (e: NoConnectivityException) {
                        ToastUtils().toastLong(activity, e.message)
                        Timber.e(e, "Service Host Unreachable")
                    } finally {
                        group2_loading.visibility = View.GONE
                    }
                }
            }
            whenResumed {
                uiScope.launch(uiScope.coroutineContext) {
                    acquireUser()
                }
            }
        }
    }

    private suspend fun acquireUser() = withContext(uiScope.coroutineContext) {

        val user = homeViewModel.user.await()
        user.observeOnce(this@HomeFragment, { userInstance ->
            userDTO = userInstance
            username?.text = userInstance.userName
        })
    }

    override fun onStop() {
        uiScope.coroutineContext.cancelChildren()
        viewLifecycleOwner.lifecycleScope.coroutineContext.cancelChildren()
        super.onStop()
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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        homeViewModel = activity?.run {
            ViewModelProvider(this, factory).get(HomeViewModel::class.java)
        } ?: throw Exception("Invalid Activity")

        sharedViewModel = activity?.run {
            ViewModelProvider(this, shareFactory).get(SharedViewModel::class.java)
        } ?: throw Exception("Invalid Activity")

        ping()

        uiScope.launch(uiScope.coroutineContext) {
            homeViewModel.bigSyncCheck()
            homeViewModel.bigSyncDone.observeOnce(viewLifecycleOwner, {
                it?.let {
                    Timber.d("Synced: $it")
                    if (!it) {
                        promptUserToSync()
                    }
                }
            })
        }

        initSwipeToRefresh()

        serverTextView.setOnClickListener {
            ToastUtils().toastServerAddress(requireContext())
        }

        imageView7.setOnClickListener {
            ToastUtils().toastVersion(requireContext())
        }
    }

    private fun initSwipeToRefresh() {
        items_swipe_to_refresh.setProgressBackgroundColorSchemeColor(
            ContextCompat.getColor(
                requireContext().applicationContext,
                R.color.colorPrimary
            )
        )

        items_swipe_to_refresh.setColorSchemeColors(Color.WHITE)

        items_swipe_to_refresh.setOnRefreshListener {
            bigSync()
        }
    }

    private fun checkConnectivity() {
        val lm = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val cm =
            requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        networkEnabled = cm.isDefaultNetworkActive
        //  Check if Network Enabled
        if (!networkEnabled) {
            dataEnabled.setText(R.string.mobile_data_not_connected)
            dataEnabled.setTextColor(colorNotConnected)
        } else {
            dataEnabled.setText(R.string.mobile_data_connected)
            dataEnabled.setTextColor(colorConnected)
        }

        // Check if GPS connected
        if (!gpsEnabled) {
            locationEnabled.text = requireActivity().getString(R.string.gps_not_connected)
            locationEnabled.setTextColor(colorNotConnected)
        } else {
            locationEnabled.text = requireActivity().getString(R.string.gps_connected)
            locationEnabled.setTextColor(colorConnected)
        }
    }

    private fun retrySync() {
        IndefiniteSnackbar.hide()
        bigSync()
    }

    private fun ping() {
        Coroutines.main {
            checkConnectivity()
            servicesHealthCheck()
        }
    }

    override fun onResume() {
        super.onResume()
        ping()
    }

    private fun bigSync() = uiScope.launch(uiScope.coroutineContext) {
        try {
            if (!items_swipe_to_refresh.isRefreshing)
                items_swipe_to_refresh.isRefreshing = true

            sharedViewModel.setMessage("Data Loading")
            sharedViewModel.toggleLongRunning(true)

            homeViewModel.dataBaseStatus.observe(
                viewLifecycleOwner,
                { t ->
                    t?.let {
                        when (t) {
                            is XISuccess -> {
                                sharedViewModel.setMessage("Data Retrieved")
                                sharedViewModel.toggleLongRunning(false)
                                items_swipe_to_refresh.isRefreshing = false
                            }
                            is XIStatus -> {
                                sharedViewModel.setMessage(t.message)
                            }
                            is XIError -> {
                                sharedViewModel.setMessage("Sync Failed")
                                handleBigSyncError(t)
                            }
                        }
                    }
                })
            val fetched = homeViewModel.fetchAllData(userDTO.userId)
            Timber.d("$fetched")
        } catch (t: Throwable) {
            sharedViewModel.setMessage("Sync Failed")
            Timber.e(t, "Failed BigSync")
            val xiErr = XIError(t, t.message ?: XIErrorHandler.UNKNOWN_ERROR)
            handleBigSyncError(xiErr)
        } finally {
            items_swipe_to_refresh.isRefreshing = false
            sharedViewModel.toggleLongRunning(false)
        }
    }

    private fun handleBigSyncError(xiErr: XIError) {

        items_swipe_to_refresh.isRefreshing = false
        sharedViewModel.toggleLongRunning(false)
        group2_loading.visibility = View.GONE

        when {

            xiErr.isConnectivityError() -> {

                XIErrorHandler.handleError(
                    view = this@HomeFragment.requireView(),
                    throwable = xiErr,
                    shouldShowSnackBar = true,
                    refreshAction = { retrySync() }
                )
            }
            else -> {
                XIErrorHandler.handleError(
                    view = this@HomeFragment.requireView(),
                    throwable = xiErr,
                    shouldToast = true
                )
            }
        }
    }

    private fun promptUserToSync() {
        val syncDialog: AlertDialog.Builder =
            AlertDialog.Builder(activity) // android.R.style.Theme_DeviceDefault_Dialog
                .setTitle(
                    "Initial Synchronisation"
                )
                .setMessage("As a new user, please synchronise the local database.")
                .setCancelable(false)
                .setIcon(R.drawable.ic_baseline_cloud_download_24)
                .setPositiveButton(R.string.ok) { dialog, whichButton ->
                    bigSync()
                }

        syncDialog.show()
    }

    companion object {
        val TAG: String = HomeFragment::class.java.simpleName
    }

    private fun servicesHealthCheck() = uiScope.launch(uiScope.coroutineContext) {
        try {
            if (homeViewModel.healthCheck()) {
                connectedTo.text = getString(R.string.services_up, BuildConfig.VERSION_NAME)
                connectedTo.setTextColor(colorConnected)
            } else {
                connectedTo.text = getString(R.string.services_down, BuildConfig.VERSION_NAME)
                connectedTo.setTextColor(colorNotConnected)
            }
        } catch (t: Throwable) {
            val ziError = XIError(t, t.localizedMessage ?: XIErrorHandler.UNKNOWN_ERROR)
            XIErrorHandler.handleError(
                this@HomeFragment.requireView(),
                ziError,
                shouldToast = true
            )
        }
    }
}
