package za.co.xisystems.itis_rrm.ui.mainview.home

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import za.co.xisystems.itis_rrm.data.localDB.AppDatabase
import za.co.xisystems.itis_rrm.data.localDB.entities.SectionItemDTO
import za.co.xisystems.itis_rrm.data.repositories.OfflineDataRepository
import za.co.xisystems.itis_rrm.data.repositories.UserRepository
import za.co.xisystems.itis_rrm.utils.lazyDeferred

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