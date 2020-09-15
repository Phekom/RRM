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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenResumed
import androidx.lifecycle.whenStarted
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import timber.log.Timber
import za.co.xisystems.itis_rrm.BuildConfig
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.base.BaseFragment
import za.co.xisystems.itis_rrm.custom.errors.ErrorHandler
import za.co.xisystems.itis_rrm.custom.errors.NoConnectivityException
import za.co.xisystems.itis_rrm.custom.errors.NoInternetException
import za.co.xisystems.itis_rrm.custom.errors.ServiceException
import za.co.xisystems.itis_rrm.custom.results.XIError
import za.co.xisystems.itis_rrm.custom.results.XIProgress
import za.co.xisystems.itis_rrm.custom.results.XIResult
import za.co.xisystems.itis_rrm.custom.results.XIStatus
import za.co.xisystems.itis_rrm.custom.results.XISuccess
import za.co.xisystems.itis_rrm.custom.results.isConnectivityException
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
    private val factory: HomeViewModelFactory by instance<HomeViewModelFactory>()
    private lateinit var sharedViewModel: SharedViewModel
    private val shareFactory: SharedViewModelFactory by instance<SharedViewModelFactory>()
    private var gpsEnabled: Boolean = false
    private var networkEnabled: Boolean = false
    private lateinit var userDTO: UserDTO
    private var uiScope = UiLifecycleScope()
    private val colorConnected: Int
        get() = Color.parseColor("#55A359")
    private val colorNotConnected: Int
        get() = Color.RED

    private val bigSyncObserver = Observer<XIResult<Boolean>> { handleBigSync(it) }

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
                    } catch (e: ServiceException) {
                        ToastUtils().toastLong(activity, e.message)
                        Timber.e(e, "API Exception")
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
        user.observe(this@HomeFragment, { userInstance ->
            userDTO = userInstance
            username?.text = userInstance.userName
            servicesHealthCheck()
            checkConnectivity()
        })
    }

    override fun onStop() {
        uiScope.destroy()
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

        checkConnectivity()

        // Check if database is synched and prompt user if necessary
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

    private suspend fun healthChecksOut(userId: String): Boolean {
        return homeViewModel.healthCheck(userId)
    }

    private fun retrySync() {
        IndefiniteSnackbar.hide()
        bigSync()
    }

    private fun ping() {
        Coroutines.main {
            acquireUser()
        }
    }

    override fun onResume() {
        super.onResume()
        ping()
    }

    override fun onDetach() {
        super.onDetach()
        ping()
    }

    private fun handleBigSync(result: XIResult<Boolean>) {
        when (result) {
            is XISuccess -> {
                sharedViewModel.setMessage("Data Retrieved")
            }
            is XIStatus -> {
                sharedViewModel.setMessage(result.message)
            }
            is XIError -> {
                sharedViewModel.setMessage("Sync Failed")
                sharedViewModel.toggleLongRunning(false)
                items_swipe_to_refresh.isRefreshing = false
                if (result.isConnectivityException()) {
                    ErrorHandler.handleError(
                        view = this@HomeFragment.requireView(),
                        shouldShowSnackBar = true,
                        throwable = result,
                        refreshAction = { retrySync() }
                    )
                } else {
                    ErrorHandler.handleError(
                        view = this@HomeFragment.requireView(),
                        shouldToast = true,
                        throwable = result
                    )
                }
            }
            is XIProgress -> {
                sharedViewModel.toggleLongRunning(result.isLoading)
                items_swipe_to_refresh.isRefreshing = result.isLoading
            }
        }
    }

    private fun bigSync() = uiScope.launch(uiScope.coroutineContext) {
        sharedViewModel.setMessage("Data Loading")
        sharedViewModel.toggleLongRunning(true)
        homeViewModel.databaseResult.observe(viewLifecycleOwner, bigSyncObserver)
        homeViewModel.fetchAllData(userDTO.userId)
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
                .setPositiveButton(
                    R.string.ok
                ) { dialog, whichButton ->
                    bigSync()
                }

        syncDialog.show()
    }

    companion object {
        val TAG: String = HomeFragment::class.java.simpleName
    }

    private fun servicesHealthCheck() = uiScope.launch(uiScope.coroutineContext) {
        if (healthChecksOut(userDTO.userId)) {
            connectedTo.text = getString(R.string.services_up, BuildConfig.VERSION_NAME)
            connectedTo.setTextColor(colorConnected)
        } else {
            connectedTo.text = getString(R.string.services_down, BuildConfig.VERSION_NAME)
            connectedTo.setTextColor(colorNotConnected)
        }
    }
}
