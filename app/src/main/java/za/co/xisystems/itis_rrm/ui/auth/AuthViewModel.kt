package za.co.xisystems.itis_rrm.ui.auth

import android.os.Build
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.toxicbakery.bcrypt.Bcrypt
import timber.log.Timber
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.constants.Constants.SALT_ROUNDS
import za.co.xisystems.itis_rrm.custom.errors.XIErrorHandler
import za.co.xisystems.itis_rrm.data.repositories.UserRepository
import za.co.xisystems.itis_rrm.utils.Coroutines
import za.co.xisystems.itis_rrm.utils.lazyDeferred

/**
 * Created by Francis Mahlava on 2019/10/23.
 * Updated by Shaun McDonald 2020/04/15
 */

class AuthViewModel(
    private val repository: UserRepository
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

    val newPinRegistered: MutableLiveData<Boolean> = MutableLiveData()

    fun onResetPinButtonClick(view: View) {

        when {
            enterOldPin.isNullOrBlank() -> {
                authListener?.onFailure("Please enter old PIN")
            }
            enterNewPin.isNullOrEmpty() -> {
                authListener?.onFailure("Please enter new PIN")
            }
            confirmNewPin.isNullOrEmpty() -> {
                authListener?.onFailure("Please confirm new PIN")
            }
            enterNewPin != confirmNewPin -> {
                authListener?.onFailure("New PINs do not match")
            }
            enterOldPin == enterNewPin -> {
                authListener?.onFailure("New PIN cannot be the same as the old PIN. Please enter a new PIN")
            }
            else -> Coroutines.main {

                try {
                    val pinHash = repository.getHash()
                    if (Bcrypt.verify(enterOldPin!!, pinHash!!)) {
                        val newHash = Bcrypt.hash(confirmNewPin!!, SALT_ROUNDS)
                        repository.updateHash(newHash, pinHash)
                        newPinRegistered.value = true
                    } else {
                        authListener?.onFailure("Old PIN is incorrect, pLease enter your current PIN")
                    }
                } catch (t: Throwable) {
                    showCauseAndEffect(t, "PIN update failed.")
                }
            }
        }
    }

    fun onRegPinButtonClick(view: View) {

        when {
            enterPin.isNullOrEmpty() -> {
                authListener?.onFailure("Please Enter PIN")
                return
            }
            confirmPin.isNullOrEmpty() -> {
                authListener?.onFailure("Please confirm PIN")
                return
            }
            enterPin != confirmPin -> {
                authListener?.onFailure("PIN did not match")
                return
            }

            // Length restrictions
            !enterPin.isNullOrBlank() -> {
                val pin = enterPin
                if (pin!!.length < 4 || pin.length > 4) {
                    authListener?.onFailure("PIN needs to be four digits long.")
                    return
                }
            }
        }

        Coroutines.main {
            try {
                val phoneNumber = "12345457"
                val imei = "45678"
                val androidDevice =
                    " " + R.string.android_sdk + Build.VERSION.SDK_INT + R.string.space +
                        Build.BRAND + R.string.space + Build.MODEL + R.string.space +
                        Build.DEVICE + ""
                val pinHash = Bcrypt.hash(confirmPin!!, SALT_ROUNDS)
                repository.upDateUser(
                    phoneNumber,
                    imei,
                    androidDevice,
                    pinHash
                )
            } catch (t: Throwable) {
                showCauseAndEffect(t, "PIN update failed.")
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
                val phoneNumber = "12345457"
                val imei = "45678"
                val androidDevice =
                    " " + R.string.android_sdk + Build.VERSION.SDK_INT + R.string.space +
                        Build.BRAND + R.string.space + Build.MODEL + R.string.space +
                        Build.DEVICE + ""
                repository.userRegister(
                    username!!,
                    password!!,
                    phoneNumber,
                    imei,
                    androidDevice
                )
            } catch (t: Throwable) {
                showCauseAndEffect(t, "User Registration failed.")
            }
        }
    }

    private fun showCauseAndEffect(t: Throwable, mEffect: String) {
        val effect = mEffect
        val cause = t.message ?: XIErrorHandler.UNKNOWN_ERROR
        val message = "$effect - $cause"
        Timber.e(t, message)
        authListener?.onFailure(t.message ?: XIErrorHandler.UNKNOWN_ERROR)
    }
}
