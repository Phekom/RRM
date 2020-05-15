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
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import timber.log.Timber
import za.co.xisystems.itis_rrm.BuildConfig
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.base.BaseFragment
import za.co.xisystems.itis_rrm.base.Error
import za.co.xisystems.itis_rrm.custom.errors.ApiException
import za.co.xisystems.itis_rrm.custom.errors.ErrorHandler
import za.co.xisystems.itis_rrm.custom.errors.NoConnectivityException
import za.co.xisystems.itis_rrm.custom.errors.NoInternetException
import za.co.xisystems.itis_rrm.data._commons.views.ToastUtils
import za.co.xisystems.itis_rrm.data.network.responses.HealthCheckResponse
import za.co.xisystems.itis_rrm.ui.mainview.activities.SharedViewModel
import za.co.xisystems.itis_rrm.ui.mainview.activities.SharedViewModelFactory
import za.co.xisystems.itis_rrm.ui.models.HomeViewModel
import za.co.xisystems.itis_rrm.ui.models.HomeViewModelFactory
import za.co.xisystems.itis_rrm.utils.Coroutines
import za.co.xisystems.itis_rrm.utils.show
import kotlin.system.measureTimeMillis


class HomeFragment : BaseFragment(R.layout.fragment_home), KodeinAware {

    override val kodein by kodein()
    private lateinit var homeViewModel: HomeViewModel
    private val factory: HomeViewModelFactory by instance()
    private lateinit var sharedViewModel: SharedViewModel
    private val shareFactory: SharedViewModelFactory by instance()
    private var userId: String? = null

    private var gpsEnabled: Boolean = false
    private var networkEnabled: Boolean = false

    companion object {
        val TAG: String = HomeFragment::class.java.simpleName
    }

    init {

        lifecycleScope.launch {
            whenStarted {

                homeViewModel = activity?.run {
                    ViewModelProvider(this, factory).get(HomeViewModel::class.java)
                } ?: throw Exception("Invalid Activity")

                sharedViewModel = activity?.run {
                    ViewModelProvider(this, shareFactory).get(SharedViewModel::class.java)
                } ?: throw Exception("Invalid Activity")


                val time = measureTimeMillis {
                    try {
                        data2_loading.show()
                        val user = homeViewModel.user.await()
                        user.observe(viewLifecycleOwner, Observer { user_ ->
                            username?.text = user_.userName
                            userId = user_.userId
                        })


                        val contracts = homeViewModel.offlineData.await()
                        contracts.observe(viewLifecycleOwner, Observer { mContracts ->

                            val allData = mContracts.count()
                            Timber.d("Fetched ${mContracts.size} contracts")

                        })
                    } catch (e: Exception) {
                        val ex = Error(
                            message = "Error while retrieving contracts.",
                            exception = e
                        )
                        ErrorHandler.handleError(
                            view = this@HomeFragment.requireView(),
                            throwable = ex,
                            shouldToast = true,
                            shouldShowSnackBar = false
                        )
                    } finally {
                        group2_loading.visibility = View.GONE
                    }
                }
                Timber.d("Fetch contracts complete in $time seconds.")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity?.hideKeyboard()
        swipeToRefreshInit()



        checkGPSConnectivity()

        connectedTo.text = "Version " + BuildConfig.VERSION_NAME


        serverTextView.setOnClickListener {
            ToastUtils().toastServerAddress(context)
        }

        imageView7.setOnClickListener {
            ToastUtils().toastVersion(context)
        }
    }

    private fun checkGPSConnectivity() {
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
            //            locationEnabled.text = "GPS NOT CONNECTED"
            locationEnabled.text = requireActivity().getString(R.string.gps_not_connected)
            locationEnabled.setTextColor(colorNotConnected)
        } else {
            locationEnabled.text = requireActivity().getString(R.string.gps_connected)
            locationEnabled.setTextColor(colorConnected)
        }
    }

    private fun swipeToRefreshInit() {

        items_swipe_to_refresh.setProgressBackgroundColorSchemeColor(
            ContextCompat.getColor(
                requireContext().applicationContext,
                R.color.colorPrimary
            )
        )

        items_swipe_to_refresh.setColorSchemeColors(Color.WHITE)

        items_swipe_to_refresh.setOnRefreshListener {
            Coroutines.main {
                try {
                    // dialog.show()
                    sharedViewModel.setMessage("Data Loading")
                    sharedViewModel.toggleLongRunning(true)
                    val works = homeViewModel.offlineWorkFlows.await()
                    works.observe(viewLifecycleOwner, Observer { work ->
                        val allData = work.count()
                        if (work.size == allData)
                            sharedViewModel.toggleLongRunning(false)
                        sharedViewModel.setMessage("Data Retrieved")
                    })
                } catch (e: ApiException) {
                    //ToastUtils().toastLong(activity, e.message)
                    sharedViewModel.setMessage(e.message)
                    Timber.e(e, "API Exception")
                } catch (e: NoInternetException) {
                    //ToastUtils().toastLong(activity, e.message)
                    sharedViewModel.setMessage(e.message)
                    Timber.e(e, "No Internet Connection")
                } catch (e: NoConnectivityException) {
                    sharedViewModel.setMessage(e.message)
                    Timber.e(e, "Service Host Unreachable")
                } finally {
                    items_swipe_to_refresh.isRefreshing = false
                    sharedViewModel.toggleLongRunning(false)
                }
            }
        }
    }

    private val health: HealthCheckResponse? = null
    private fun ping() {


        Coroutines.main {
            //            val user_nm = homeViewModel.user_n.await()
//            user_nm.observe(viewLifecycleOwner, Observer {
//
//                var responseListener: OfflineListener? = null
//                try {
//                    responseListener?.onSuccess(health!!.isAlive)
//                    connectedTo.setTextColor(colorConnected)
//
//
//                    responseListener?.onFailure(health!!.errorMessage)
//                    connectedTo.setTextColor(colorNotConnected)
//                }catch(e: ApiException){
//                    responseListener?.onFailure(e.message!!)
//                }catch (e: NoInternetException){
//                    responseListener?.onFailure(e.message!!)
//                }
//
//            })

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
