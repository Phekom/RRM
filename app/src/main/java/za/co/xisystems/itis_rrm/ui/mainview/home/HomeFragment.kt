package za.co.xisystems.itis_rrm.ui.mainview.home


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
import za.co.xisystems.itis_rrm.data._commons.views.ToastUtils
import za.co.xisystems.itis_rrm.data.localDB.entities.UserDTO
import za.co.xisystems.itis_rrm.data.network.responses.HealthCheckResponse
import za.co.xisystems.itis_rrm.ui.custom.IndefiniteSnackbar
import za.co.xisystems.itis_rrm.ui.mainview._fragments.BaseFragment
import za.co.xisystems.itis_rrm.ui.mainview.activities.SharedViewModel
import za.co.xisystems.itis_rrm.ui.mainview.activities.SharedViewModelFactory
import za.co.xisystems.itis_rrm.ui.scopes.UiLifecycleScope
import za.co.xisystems.itis_rrm.utils.*
import za.co.xisystems.itis_rrm.utils.errors.ErrorHandler.handleError
import za.co.xisystems.itis_rrm.utils.results.Failure
import za.co.xisystems.itis_rrm.utils.results.Progress
import za.co.xisystems.itis_rrm.utils.results.ResultSet
import za.co.xisystems.itis_rrm.utils.results.Success


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
    private val observer = Observer<ResultSet<Boolean>> { handleResponse(it) }

    private fun handleResponse(it: ResultSet<Boolean>) {
        sharedViewModel.toggleLongRunning(false)
        items_swipe_to_refresh.isRefreshing = false
        when (it) {
            is Success -> {
                sharedViewModel.setMessage("Data Retrieved")

            }
            is Failure -> {
                sharedViewModel.setMessage("Data Retrieval Failed")
                handleError(
                    view = this@HomeFragment.requireView(),
                    throwable = it,
                    shouldToast = false,
                    shouldShowSnackBar = true,
                    refreshAction = { retryFetchAllData() }
                )
            }
            is Progress -> {
                sharedViewModel.toggleLongRunning(it.isLoading)
            }
        }

    }

    companion object {
        val TAG: String = HomeFragment::class.java.simpleName
    }

    init {

        lifecycleScope.launch {
            whenStarted {


                uiScope.launch(uiScope.coroutineContext) {
                    try {

                        data2_loading.show()

                        val user = homeViewModel.user.await()
                        user.observe(viewLifecycleOwner, Observer { user_ ->
                            userDTO = user_
                            username?.text = user_.userName
                        })
                        val entities = homeViewModel.offlineEnitities.await()
                        val contracts = homeViewModel.offlineSectionItems.await()
                        contracts.observe(viewLifecycleOwner, Observer { mSectionItem ->
                            val allData = mSectionItem.count()
                            if (mSectionItem.size == allData)
                                group2_loading.visibility = View.GONE

                        })

                    } catch (e: ApiException) {
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
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        activity?.hideKeyboard()
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        loadViewModels()

        initSwipeToRefresh()

        checkNetworkLocationStatus()

        connectedTo.text = "Version " + BuildConfig.VERSION_NAME


        serverTextView.setOnClickListener {
            ToastUtils().toastServerAddress(context)
        }

        imageView7.setOnClickListener {
            ToastUtils().toastVersion(context)
        }
    }

    private fun loadViewModels() {
        homeViewModel = activity?.run {
            ViewModelProvider(this, factory).get(HomeViewModel::class.java)
        } ?: throw Exception("Invalid Activity")

        sharedViewModel = activity?.run {
            ViewModelProvider(this, shareFactory).get(SharedViewModel::class.java)
        } ?: throw Exception("Invalid Activity")
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
            fetchAllData()
        }
    }

    private fun fetchAllData() {
        uiScope.launch(uiScope.coroutineContext) {
            sharedViewModel.setMessage("Data Loading")
            sharedViewModel.toggleLongRunning(true)
            withContext(uiScope.coroutineContext) {
                homeViewModel.fetchAllData(userDTO.userId)
                homeViewModel.fetchResult.observe(viewLifecycleOwner, observer)
            }
        }
    }

    private fun retryFetchAllData() {
        IndefiniteSnackbar.hide()
        fetchAllData()
    }

    private fun checkNetworkLocationStatus() {
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

    private val health: HealthCheckResponse? = null
    private fun ping() {
        Coroutines.main {
        }
    }


    private val colorConnected: Int
        get() = Color.parseColor("#55A359")

    private val colorNotConnected: Int
        get() = Color.RED


    override fun onResume() {
        super.onResume()
        ping()
    }

    override fun onDetach() {
        super.onDetach()
        ping()
    }
}
