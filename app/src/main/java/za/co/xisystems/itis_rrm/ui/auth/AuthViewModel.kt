package za.co.xisystems.itis_rrm.ui.auth

import android.os.Build
import android.view.View
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.data.repositories.OfflineDataRepository
import za.co.xisystems.itis_rrm.data.repositories.UserRepository
import za.co.xisystems.itis_rrm.utils.*

/**
 * Created by Francis Mahlava on 2019/10/23.
 */


class AuthViewModel(
    private val repository: UserRepository,
    private val offlineDataRepository: OfflineDataRepository
) : ViewModel() {

    var username: String? = null
    var password: String? = null
    var enterPin: String? = null
    var confirmPin: String? = null

    var enterOldPin: String? = null
    var enterNewPin: String? = null
    var confirmNewPin: String? = null




    var authListener: AuthListener? = null


    suspend  fun getPin(): String {
        return withContext(Dispatchers.IO) {
            repository.getPin()
        }
    }



    fun onResetPinButtonClick(view: View) {

    if (enterOldPin.isNullOrEmpty()) {
        authListener?.onFailure("Please  Enter Old pin")
        return
    }

    if (enterNewPin.isNullOrEmpty()) {
        authListener?.onFailure("Please Enter New pin")
        return
    }

    if (confirmNewPin.isNullOrEmpty()) {
        authListener?.onFailure("Please Confirm New pin")
        return
    }

    if (enterNewPin != confirmNewPin) {
        authListener?.onFailure("Pin did not match")
        return
    }
    Coroutines.main {
        try {
            if (enterOldPin == repository.getPin()) {
                repository.upDateUserPin(confirmNewPin!!, enterOldPin!!)
            } else {
                authListener?.onFailure("Old Pin Is incorrect, pLease enter your current Pin")
            }

        } catch (e: AuthException) {
            authListener?.onFailure(e.message!!)
        } catch (e: NoInternetException) {
            authListener?.onFailure(e.message!!)
        } catch (e: NoConnectivityException) {
            authListener?.onFailure(e.message!!)
        }
    }

}

    fun onRegPinButtonClick(view: View) {

        if (enterPin.isNullOrEmpty()) {
            authListener?.onFailure("Please Enter pin")
            return
        }

        if (confirmPin.isNullOrEmpty()) {
            authListener?.onFailure("Please Confirm pin")
            return
        }

        if (enterPin != confirmPin) {
            authListener?.onFailure("Pin did not match")
            return
        }
        Coroutines.main {
            try {
                // TODO: Get these metrics for the device
                val phoneNumber = "12345457"
                val IMEI = "45678"
                val androidDevice =
                    " " + R.string.android_sdk + Build.VERSION.SDK_INT + R.string.space + Build.BRAND + R.string.space + Build.MODEL + R.string.space + Build.DEVICE + ""
                repository.upDateUser(
//                    userId ,
                    phoneNumber,
                    IMEI,
                    androidDevice,
                    confirmPin!!
                )
            } catch (e: AuthException) {
                authListener?.onFailure(e.message!!)
            } catch (e: ApiException) {
                authListener?.onFailure(e.message!!)
            } catch (e: NoInternetException) {
                authListener?.onFailure(e.message!!)
            } catch (e: NoConnectivityException) {
                authListener?.onFailure(e.message!!)
            }
        }

    }



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

        Coroutines.main {
            try {
                // TODO: Read these metrics from the device.
                val phoneNumber = "12345457"
                val IMEI = "45678"
                val androidDevice =
                    " " + R.string.android_sdk + Build.VERSION.SDK_INT + R.string.space + Build.BRAND + R.string.space + Build.MODEL + R.string.space + Build.DEVICE + ""
                repository.userRegister(
                    username!!,
                    password!!,
                    phoneNumber,
                    IMEI,
                    androidDevice
                )
            } catch (e: AuthException) {
                authListener?.onFailure(e.message!!)
            } catch (e: ApiException) {
                authListener?.onFailure(e.message!!)
            } catch (e: NoInternetException) {
                authListener?.onFailure(e.message!!)
            } catch (e: NoConnectivityException) {
                authListener?.onFailure(e.message!!)
            }
        }


    }




    val user by lazyDeferred {
        repository.getUser()
    }
    val offlineData by lazyDeferred {
        offlineDataRepository.getSectionItems()
        offlineDataRepository.getContracts()
    }

}
