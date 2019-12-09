package za.co.xisystems.itis_rrm.ui.auth

import android.content.Context
import android.os.Build
import android.view.View
import androidx.lifecycle.ViewModel
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.data.network.responses.AuthResponse
import za.co.xisystems.itis_rrm.data.repositories.OfflineDataRepository
import za.co.xisystems.itis_rrm.data.repositories.UserRepository
import za.co.xisystems.itis_rrm.utils.ApiException
import za.co.xisystems.itis_rrm.utils.Coroutines
import za.co.xisystems.itis_rrm.utils.NoInternetException
import za.co.xisystems.itis_rrm.utils.lazyDeferred

/**
 * Created by Francis Mahlava on 2019/10/23.
 */


class AuthViewModel(
    private val repository: UserRepository,
    offlineDataRepository: OfflineDataRepository
    ) : ViewModel() {
    private val context : Context? = null

    var username: String? = null
    var password: String? = null
    var enterPin: String? = null
    var confirmPin: String? = null

    var authListener: AuthListener? = null
    var authResponse : AuthResponse? = null
//    fun getLoggedInUser() = repository.getUser()
//    fun getUserRole() = repository.getUser()


//    private fun deviceDetails(){
//
//        val telephonyManager = context?.applicationContext?.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager?
//        if (ActivityCompat.checkSelfPermission(
//                this.context!!,
//                Manifest.permission.READ_SMS
//            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
//                this.context!!,
//                Manifest.permission.READ_PHONE_NUMBERS
//            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
//                this.context!!,
//                Manifest.permission.READ_PHONE_STATE
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return
//        }
//        val phoneNumber = telephonyManager?.line1Number
//        val IMEI = telephonyManager?.imei
//        val androidDevice = " "+ R.string.android_sdk + Build.VERSION.SDK_INT + R.string.space + Build.BRAND + R.string.space + Build.MODEL + R.string.space + Build.DEVICE+""
//
//
//
//    }



    fun onRegButtonClick(view: View) {
        authListener?.onStarted()

        if (username.isNullOrEmpty()) {
            authListener?.onFailure("UserName is required")
            return
        }

        if (password.isNullOrEmpty()) {
            authListener?.onFailure("Password is required")
            return
        }

//        if (enterPin.isNullOrEmpty()) {
//            authListener?.onFailure("Please Enter pin")
//            return
//        }
//
//        if (confirmPin.isNullOrEmpty()) {
//            authListener?.onFailure("Please Confirm pin")
//            return
//        }
//
//        if (enterPin != confirmPin) {
//            authListener?.onFailure("Pin did not match")
//            return
//        }





            Coroutines.main {
                try {
//                    val telephonyManager = context?.applicationContext?.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager?
//                    if (ActivityCompat.checkSelfPermission(
//                            this.context!!,
//                            Manifest.permission.READ_SMS
//                        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
//                            this.context!!,
//                            Manifest.permission.READ_PHONE_NUMBERS
//                        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
//                            this.context!!,
//                            Manifest.permission.READ_PHONE_STATE
//                        ) != PackageManager.PERMISSION_GRANTED
//                    ) {
//
//                    }
                    val phoneNumber ="12345457"//telephonyManager?.line1Number
                    val IMEI = "45678"//telephonyManager?.imei
                    val androidDevice = " "+ R.string.android_sdk + Build.VERSION.SDK_INT + R.string.space + Build.BRAND + R.string.space + Build.MODEL + R.string.space + Build.DEVICE+""

                    repository.userRegister(username!!, password!!,phoneNumber!!,IMEI!!,androidDevice)
                    authListener?.onFailure("User Details are wrong please Try again")
                }catch(e: ApiException){
                    authListener?.onFailure(e.message!!)
                }catch (e: NoInternetException){
                    authListener?.onFailure(e.message!!)
                }
            }



    }

    val user by lazyDeferred {
        repository.getUser()
    }

    val offlinedata by lazyDeferred {
        offlineDataRepository.getSectionItems()
//        offlineDataRepository.getVoItems()
//        offlineDataRepository.getProjects()
//        offlineDataRepository.getWorkFlows()=
        offlineDataRepository.getContracts()

    }


}