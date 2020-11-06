package za.co.xisystems.itis_rrm.ui.auth

import android.os.Build
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import www.sanju.motiontoast.MotionToast
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.custom.errors.AuthException
import za.co.xisystems.itis_rrm.custom.errors.NoConnectivityException
import za.co.xisystems.itis_rrm.custom.errors.NoInternetException
import za.co.xisystems.itis_rrm.custom.errors.ServiceException
import za.co.xisystems.itis_rrm.custom.errors.XIErrorHandler
import za.co.xisystems.itis_rrm.data.repositories.OfflineDataRepository
import za.co.xisystems.itis_rrm.data.repositories.UserRepository
import za.co.xisystems.itis_rrm.utils.Coroutines
import za.co.xisystems.itis_rrm.utils.lazyDeferred

/**
 * Created by Francis Mahlava on 2019/10/23.
 * Updated by Shaun McDonald 2020/04/15
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
    val user by lazyDeferred {
        repository.getUser()
    }
    val offlineData by lazyDeferred {
        offlineDataRepository.getSectionItems()
        offlineDataRepository.getContracts()
    }

    suspend fun getPin(): String {
        return withContext(Dispatchers.IO) {
            repository.getPin()
        }
    }

    val newPinRegistered: MutableLiveData<Boolean> = MutableLiveData()


    fun onResetPinButtonClick(view: View) {

        if (enterOldPin.isNullOrBlank()) {
            authListener?.onFailure("Please enter old PIN")
            return
        }





        if (enterNewPin.isNullOrEmpty()) {
            authListener?.onFailure("Please enter new PIN")
            return
        }

        if (confirmNewPin.isNullOrEmpty()) {
            authListener?.onFailure("Please confirm new Pin")
            return
        }

        if (enterNewPin != confirmNewPin) {
            authListener?.onFailure("PINs did not match")
            return
        }
        Coroutines.main {
            try {
                if (enterOldPin == repository.getPin()) {
                    if (enterOldPin.equals(enterNewPin)) {
                        authListener?.onFailure("New PIN cannot be the same as the old PIN. Please enter a new PIN")
                    } else {
                        repository.upDateUserPin(confirmNewPin!!, enterOldPin!!)
                        newPinRegistered.value = true
                    }

                } else {
                    authListener?.onFailure("Old PIN is incorrect, pLease enter your current PIN")
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
            authListener?.onFailure("Please Enter PIN")
            return
        }

        if (confirmPin.isNullOrEmpty()) {
            authListener?.onFailure("Please confirm PIN")
            return
        }

        if (enterPin != confirmPin) {
            authListener?.onFailure("PIN did not match")
            return
        }

        // Length restrictions
        if (!enterPin.isNullOrBlank()) {
            val pin = enterPin
            if (pin!!.length < 4 || pin.length > 4) {
                authListener?.onFailure("PIN needs to be four digits long.")
                return
            }
        }

        Coroutines.main {
            try {
                // TODO: Get these metrics for the device
                val phoneNumber = "12345457"
                val imei = "45678"
                val androidDevice =
                    " " + R.string.android_sdk + Build.VERSION.SDK_INT + R.string.space + Build.BRAND + R.string.space + Build.MODEL + R.string.space + Build.DEVICE + ""
                repository.upDateUser(
//                    userId ,
                    phoneNumber,
                    imei,
                    androidDevice,
                    confirmPin!!
                )
            } catch (e: AuthException) {
                authListener?.onFailure(e.message!!)
            } catch (e: ServiceException) {
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
            authListener?.onFailure("User Name required")
            return
        }

        if (password.isNullOrEmpty()) {
            authListener?.onFailure("Password required")
            return
        }

        Coroutines.main {
            try {
                // TODO: Read these metrics from the device.
                val phoneNumber = "12345457"
                val imei = "45678"
                val androidDevice =
                    " " + R.string.android_sdk + Build.VERSION.SDK_INT + R.string.space + Build.BRAND + R.string.space + Build.MODEL + R.string.space + Build.DEVICE + ""
                repository.userRegister(
                    username!!,
                    password!!,
                    phoneNumber,
                    imei,
                    androidDevice
                )
            } catch (t: Throwable) {
                authListener?.onFailure(t.localizedMessage ?: XIErrorHandler.UNKNOWN_ERROR)
            }
        }
    }
}
