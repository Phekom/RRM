package za.co.xisystems.itis_rrm.ui.mainview.home


import android.content.Context
import android.graphics.Color
import android.location.LocationManager
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.fragment_home.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import za.co.xisystems.itis_rrm.BuildConfig
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.data._commons.views.ToastUtils
import za.co.xisystems.itis_rrm.data.network.responses.HealthCheckResponse
import za.co.xisystems.itis_rrm.ui.mainview._fragments.BaseFragment
import za.co.xisystems.itis_rrm.utils.*


class HomeFragment : BaseFragment(), KodeinAware {

    override val kodein by kodein()
    private lateinit var homeViewModel: HomeViewModel
    private val factory: HomeViewModelFactory by instance()

    var gpsEnabled: Boolean = false
    var networkEnabled: Boolean = false
    var appContext: Context? = null

    companion object {
        val TAG: String = HomeFragment::class.java.simpleName
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
        activity?.hideKeyboard()
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val dialog = setDataProgressDialog(activity!!, getString(R.string.data_loading_please_wait))

        homeViewModel = activity?.run {
            ViewModelProvider(this, factory).get(HomeViewModel::class.java)
        } ?: throw Exception("Invalid Activity")

        Coroutines.main {
            try {
                data2_loading.show()
                val user = homeViewModel.user.await()
                user.observe(viewLifecycleOwner, Observer { user_ ->
                    username?.text = user_.userName
                })

                val contracts = homeViewModel.offlineData.await()
                contracts.observe(viewLifecycleOwner, Observer { mContracts ->
                    val allData = mContracts.count()
                    if (mContracts.size == allData)
                        group2_loading.visibility = View.GONE
                })
            } catch (e: ApiException) {
                ToastUtils().toastLong(activity, e.message)
                Log.e(TAG, "API Exception", e)
            } catch (e: NoInternetException) {
                ToastUtils().toastLong(activity, e.message)
                Log.e(TAG, "No Internet Connection", e)
            } catch (e: NoConnectivityException) {
                ToastUtils().toastLong(activity, e.message)
                dialog.dismiss()
                items_swipe_to_refresh.isRefreshing = false
                Log.e(TAG, "Service Host Unreachable", e)
            } finally {
                group2_loading.visibility = View.GONE
            }
        }

        items_swipe_to_refresh.setProgressBackgroundColorSchemeColor(
            ContextCompat.getColor(
                context!!.applicationContext,
                R.color.colorPrimary
            )
        )

        items_swipe_to_refresh.setColorSchemeColors(Color.WHITE)

        items_swipe_to_refresh.setOnRefreshListener {
            dialog.show()
            Coroutines.main {
                try {
                    val works = homeViewModel.offlineWorkFlows.await()
                    works.observe(viewLifecycleOwner, Observer { work ->
                        val allData = work.count()
                        if (work.size == allData)
                            dialog.dismiss()
                        items_swipe_to_refresh.isRefreshing = false
                    })
                } catch (e: ApiException) {
                    ToastUtils().toastLong(activity, e.message)
                    Log.e(TAG, "API Exception", e)
                } catch (e: NoInternetException) {
                    ToastUtils().toastLong(activity, e.message)
                    Log.e(TAG, "No Internet Connection", e)
                } catch (e: NoConnectivityException) {
                    ToastUtils().toastLong(activity, e.message)
                    Log.e(TAG, "Service Host Unreachable", e)
                } finally {
                    items_swipe_to_refresh.isRefreshing = false
                    dialog.dismiss()
                }
            }
        }

        Coroutines.main {

            val lm = activity!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val cm =
                activity!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
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
                locationEnabled.text = activity!!.getString(R.string.gps_not_connected)
                locationEnabled.setTextColor(colorNotConnected)
            } else {
                locationEnabled.text = activity!!.getString(R.string.gps_connected)
                locationEnabled.setTextColor(colorConnected)
            }
            connectedTo.text = "Version " + BuildConfig.VERSION_NAME
        }


        serverTextView.setOnClickListener {
            ToastUtils().toastServerAddress(context)
        }




        imageView7.setOnClickListener {
            ToastUtils().toastVersion(context)
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
//    override fun onDestroyView() {
//        super.onDestroyView()
//        if (view != null) {
//            val parent = view!!.parent as ViewGroup
//            parent?.removeAllViews()
//        }
//    }

}

//  Check every 2 secs if Mobile data or Location is off/on
//        val t = object : CountDownTimer(java.lang.Long.MAX_VALUE, 2000) {
//            override fun onFinish() {
//                start()
//            }
//
//            override fun onTick(millisUntilFinished: Long) {
//
//
//
//            }
//
//        }.start()
