package za.co.xisystems.itis_rrm.ui.mainview.home


import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.fragment_home.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.data.network.responses.HealthCheckResponse
import za.co.xisystems.itis_rrm.ui.mainview._fragments.BaseFragment
import za.co.xisystems.itis_rrm.utils.Coroutines
import za.co.xisystems.itis_rrm.utils.show


class HomeFragment : BaseFragment(), KodeinAware {

    override val kodein by kodein()
    private lateinit var homeViewModel: HomeViewModel
    private val factory: HomeViewModelFactory by instance()
//    private val factoryAuth: AuthViewModelFactory by instance()
    private lateinit var appContext: Context

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        activity?.hideKeyboard()
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        appContext = this.context!!
        homeViewModel = activity?.run {
            ViewModelProviders.of(this, factory).get(HomeViewModel::class.java)
        } ?: throw Exception("Invalid Activity") as Throwable
        Coroutines.main {
            data2_loading.show()
            val user = homeViewModel.user.await()
            user.observe(viewLifecycleOwner, Observer { user_ ->
                username?.setText(user_.userName)
            })

            val contracts = homeViewModel.offlinedata.await()
            contracts.observe(viewLifecycleOwner, Observer { contrcts ->
                group2_loading.visibility = View.GONE
            })

            items_swipe_to_refresh.setProgressBackgroundColorSchemeColor(
                ContextCompat.getColor(
                    context!!.applicationContext,
                    R.color.colorPrimary
                )
            )
            items_swipe_to_refresh.setColorSchemeColors(Color.WHITE)

            items_swipe_to_refresh.setOnRefreshListener {
                Coroutines.main {
                    val works = homeViewModel.offlinedatas.await()
                    works.observe(viewLifecycleOwner, Observer { works ->
                        items_swipe_to_refresh.isRefreshing = false
                    })

                }
            }

        }

//        activity!!.runOnUiThread {
//            //No.1
//            if (appContext?.applicationContext != null) {
//                //  Check every 2 secs if Mobile data or Location is off/on
//                val t = object : CountDownTimer(java.lang.Long.MAX_VALUE, 2000) {
//
//                    // This is called every interval. (Every 2 seconds)
//                    override fun onTick(millisUntilFinished: Long) {
//
//                        val lm  = appContext?.applicationContext!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager
//                        val cm =appContext?.applicationContext!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//
//                        var gps_enabled = false
//                        var network_enabled = false
//
//
//                        val gps  = locationEnabled
//                        val data = dataEnabled
//
//                        try {
//                            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
//                        } catch (ex: Exception) {
//                        }
//
//                        try {
//                            val cmClass = Class.forName(cm.javaClass.name)
//                            val method = cmClass.getDeclaredMethod("getMobileDataEnabled")
//                            // Make the method callable =====
//                            method.isAccessible = true
//                            // get the setting for "mobile data"=========
//                            network_enabled = method.invoke(cm) as Boolean
//
//                        } catch (ex: Exception) {
//                        }
//
////                      Check if GPS connected
//                        if (!gps_enabled) {
//                            gps.setText(R.string.gps_not_connected)
//                            gps.setTextColor(colorNotConnected)
//                        } else {
//                            gps.setText(R.string.gps_connected)
//                            gps.setTextColor(colorConnected)
//                        }
//
//                        //  Check if Network Enabled
//                        if (!network_enabled) {
//                            data.setText(R.string.mobile_data_not_connected)
//                            data.setTextColor(colorNotConnected)
//                        } else {
//                            data.setText(R.string.mobile_data_connected)
//                            data.setTextColor(colorConnected)
//                        }
//                    }
//
//                    override fun onFinish() {
//                        start()
//                    }
//                }.start()
//            }
//        } // end of No.1 UI new thread
//


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


