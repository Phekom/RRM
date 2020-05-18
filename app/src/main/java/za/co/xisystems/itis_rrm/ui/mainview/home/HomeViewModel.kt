package za.co.xisystems.itis_rrm.ui.mainview.home

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.*
import timber.log.Timber
import za.co.xisystems.itis_rrm.data.localDB.AppDatabase
import za.co.xisystems.itis_rrm.data.repositories.OfflineDataRepository
import za.co.xisystems.itis_rrm.data.repositories.UserRepository
import za.co.xisystems.itis_rrm.utils.lazyDeferred
import za.co.xisystems.itis_rrm.utils.uncaughtExceptionHandler
import kotlin.system.measureTimeMillis

class HomeViewModel(
    private val repository: UserRepository,
    private val offlineDataRepository: OfflineDataRepository,
    private val Db: AppDatabase,
    val context: Context
) : ViewModel() {

    val offlineData by lazyDeferred {
        offlineDataRepository.getContracts()
    }
    val user by lazyDeferred {
        repository.getUser()
    }

    val offlineWorkFlows by lazyDeferred {
        offlineDataRepository.getWorkFlows()
    }

    val offlineSectionItems by lazyDeferred {
        offlineDataRepository.getSectionItems()
    }

    val fetchResult = MutableLiveData<Boolean>()

    suspend fun fetchAllData() {

        val time = measureTimeMillis {
            val superViewScope = CoroutineScope(Dispatchers.IO + Job() + uncaughtExceptionHandler)
            val jobs = ArrayList<Deferred<*>>()
            superViewScope.launch {
                fetchResult.postValue(false)

                val userId = Db.getUserDao().getUserID()
                jobs.add(async {
                    offlineDataRepository.refreshLookups(userId)
                })

                jobs.add(async {
                    offlineDataRepository.refreshActivitySections(userId)
                })

                jobs.add(async {
                    offlineDataRepository.refreshWorkflows(userId)
                })

                jobs.add(async {
                    offlineDataRepository.fetchUserTaskList(userId)

                })

                jobs.add(async {
                    offlineDataRepository.refreshContractInfo(userId)
                })


                try {
                    val result = jobs.joinAll()
                    Timber.d("$result")
                    fetchResult.postValue(true)
                } catch (e: Exception) {
                    Timber.e(e, "Failed fetching allData: ${e.message}")
                    throw e
                }
            }


        }
        Timber.d("Time taken: $time")

    }

////    if (context?.applicationContext != null) {
//        //  Check every 2 secs if Mobile data or Location is off/on
//        val t = object : CountDownTimer(java.lang.Long.MAX_VALUE, 2000) {
//
//            // This is called every interval. (Every 2 seconds)
//            override fun onTick(millisUntilFinished: Long) {
//
//                val lm =
//                    context?.applicationContext!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager
//                val cm =
//                    context?.applicationContext!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//
//                var gps_enabled = false
//                var network_enabled = false
//
//                val gps = binding.root.findViewById<View>(R.id.locationEnabled) as TextView
//                val data =  binding.root.findViewById<View>(R.id.dataEnabled) as TextView
//
//                try {
//                    gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
//                } catch (ex: Exception) {
//                }
//
//                try {
//                    val cmClass = Class.forName(cm.javaClass.name)
//                    val method = cmClass.getDeclaredMethod("getMobileDataEnabled")
//                    // Make the method callable =====
//                    method.isAccessible = true
//                    // get the setting for "mobile data"=========
//                    network_enabled = method.invoke(cm) as Boolean
//
//                } catch (ex: Exception) {
//                }
//
////                      Check if GPS connected
//                if (!gps_enabled) {
//                    gps.setText(R.string.gps_not_connected)
//                    gps.setTextColor(colorNotConnected)
//                } else {
//                    gps.setText(R.string.gps_connected)
//                    gps.setTextColor(colorConnected)
//                }
//
//                //  Check if Network Enabled
//                if (!network_enabled) {
//                    data.setText(R.string.mobile_data_not_connected)
//                    data.setTextColor(colorNotConnected)
//                } else {
//                    data.setText(R.string.mobile_data_connected)
//                    data.setTextColor(colorConnected)
//                }
//            }
//
//            override fun onFinish() {
//                start()
//            }
//        }.start()
//    }

}