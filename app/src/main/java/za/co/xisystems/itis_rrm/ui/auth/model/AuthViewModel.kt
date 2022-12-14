package za.co.xisystems.itis_rrm.ui.auth.model

import android.app.Application
import android.os.Build
import android.os.Build.VERSION
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.viewModelScope
import com.github.ajalt.timberkt.Timber
import com.google.android.material.textfield.TextInputEditText
import com.password4j.SecureString
import kotlinx.coroutines.*
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.custom.errors.*
import za.co.xisystems.itis_rrm.custom.events.XIEvent
import za.co.xisystems.itis_rrm.custom.results.XIResult
import za.co.xisystems.itis_rrm.data.localDB.entities.UserDTO
import za.co.xisystems.itis_rrm.data.network.responses.HealthCheckResponse
import za.co.xisystems.itis_rrm.data.preferences.MyAppPrefsManager
import za.co.xisystems.itis_rrm.data.repositories.UserRepository
import za.co.xisystems.itis_rrm.forge.DefaultDispatcherProvider
import za.co.xisystems.itis_rrm.forge.DispatcherProvider
import za.co.xisystems.itis_rrm.forge.XIArmoury
import za.co.xisystems.itis_rrm.ui.auth.AuthListener
import za.co.xisystems.itis_rrm.utils.PhotoUtil
import za.co.xisystems.itis_rrm.utils.lazyDeferred

/**
 * Created by Francis Mahlava on 2019/10/23.
 * Updated by Shaun McDonald 2020/04/15
 */

class AuthViewModel(
    private val repository: UserRepository,
    private val armoury: XIArmoury,
    private val photoUtil: PhotoUtil,
    application: Application,
    private val dispatchers: DispatcherProvider = DefaultDispatcherProvider()
) : AndroidViewModel(application) {
    private val supervisorJob = SupervisorJob()
    private val superJob = SupervisorJob()
    private var databaseStatus: MutableLiveData<XIEvent<XIResult<Boolean>>> = MutableLiveData()
    private var ioContext = dispatchers.io() + Job(superJob)
    private var mainContext = dispatchers.main() + Job(superJob)
    var healthState: MutableLiveData<XIEvent<XIResult<String>>> = MutableLiveData()
    var databaseState: MutableLiveData<XIResult<Boolean>?> = MutableLiveData()
    var versionCheckResults : MutableLiveData<XIEvent<XIResult<String>>> = MutableLiveData()

    var authListener: AuthListener? = null

    val user by lazyDeferred {
        repository.getUser().distinctUntilChanged()
    }

    val validPin: MutableLiveData<Boolean> = MutableLiveData()
    init {
        versionCheckResults = MutableLiveData()
//        healthState = MutableLiveData()
//        versionCheckResults = MutableLiveData()
    }
    companion object {
        const val PIN_SIZE = 4
    }

    fun onResetPinButtonClick(view: View, oldPin: String?, newPin: String?, confirmNewPin: String?) {
        updateUserPin(view, oldPin, newPin, confirmNewPin)
    }

    private fun updateUserPin(
        view: View,
        enterOldPin: String?,
        enterNewPin: String?,
        confirmNewPin: String?
    ) = viewModelScope.launch(ioContext) {
        listenerNotify {
            view.isClickable = false
        }
        try {
            when {
                enterOldPin.isNullOrEmpty() -> {
                    listenerNotify {
                        authListener?.onFailure("Please enter current PIN")
                    }
                }
                enterNewPin.isNullOrEmpty() -> {
                    listenerNotify {
                        authListener?.onFailure("Please enter new PIN")
                    }
                }
                confirmNewPin.isNullOrEmpty() -> {
                    listenerNotify {
                        authListener?.onFailure("Please confirm new PIN")
                    }
                }
                enterOldPin == enterNewPin -> {
                    listenerNotify {
                        authListener?.onFailure("New PIN cannot be the same as original Pin")
                    }
                }
                enterNewPin != confirmNewPin -> {
                    listenerNotify {
                        authListener?.onFailure("New PINs do not match")
                    }
                }
                confirmNewPin.length != PIN_SIZE -> {
                    listenerNotify {
                        authListener?.onFailure("PIN should be 4 (four) digits long")
                    }
                }

                else -> {
                    validateAndUpdatePin(enterOldPin, confirmNewPin)
                }
            }
        } catch (t: Throwable) {
            listenerNotify {
                postError(t)
            }
        } finally {
            listenerNotify {
                view.isClickable = true
            }
        }
    }

    fun failVersionValidation() = viewModelScope.launch(mainContext) {
        versionCheckResults.value =
            XIEvent(
                XIResult.Error(
                    TransmissionException(
                        "Server Communication Error", ConnectException("You Have No Active Data")
                    ),
                    "You May have Ran out of Data Please Check and Retry.","You Have No Active Data Connection"
                )
            )
    }



     suspend fun healthCheck() : HealthCheckResponse  {
        return withContext(mainContext) {
            repository.getHealthCheck()
        }
    }



    private suspend fun listenerNotify(notification: () -> Unit) {
        withContext(dispatchers.ui()) {
            notification()
        }
    }

    private suspend fun validateAndUpdatePin(enterOldPin: String, confirmNewPin: String) {

        val currentUser = user.await().value

        if (currentUser != null) {
            withContext(ioContext) {
                try {
                    val oldTokenGood = validateUserPin(currentUser, enterOldPin)
                    Timber.d(message = { "^*^ Old Token Valid: $oldTokenGood ^*^" })

                    if (oldTokenGood) {
                        registerUserPin1(currentUser, confirmNewPin)
                        repository.authenticatePin()
                        listenerNotify {
                            validPin.value = true
                        }
                    } else {
                        listenerNotify {
                            authListener?.onFailure("Old Pin is incorrect. Please enter the correct Pin")
                        }
                    }
                } catch (t: Throwable) {
                    listenerNotify { postError(t) }
                }
            }
        } else {
            listenerNotify { authListener?.onFailure("No logged in user.") }
        }
    }

    private suspend fun registerUserPin1(currentUser: UserDTO, newPin: String) {
        val newTokenString = SecureString(
            currentUser.userName
                .plus(currentUser.device)
                .plus(newPin).toCharArray(),
            false
        )

        val newToken = armoury.generateFutureToken(newTokenString)
        repository.updateHash(
            newToken,
            currentUser.pinHash!!
        )
        val updatedUser = user.await().value
        updatedUser?.let {
            listenerNotify { authListener?.onSuccess(it) }
        }
    }

    private suspend fun validateUserPin(currentUser: UserDTO, userPin: String): Boolean {
        val oldTokenString = SecureString(
            currentUser.userName
                .plus(currentUser.device)
                .plus(userPin).toCharArray(),
            false
        )
        return armoury.validateToken(oldTokenString, repository.getHash().toString())
    }

    fun onRegPinButtonClick(view: View, enterPin: TextInputEditText, confirmPin: TextInputEditText) {
        viewModelScope.launch(ioContext) {

            listenerNotify {
                view.isClickable = false
            }

            try {

                when {
                    enterPin.text.isNullOrEmpty() -> {
                        listenerNotify {
                            authListener?.onWarn("Please Enter PIN")
                        }
                    }
                    confirmPin.text.isNullOrEmpty() -> {
                        listenerNotify {
                            authListener?.onWarn("Please Confirm PIN")
                        }
                    }
                    enterPin.text.toString() != confirmPin.text.toString() -> {
                        listenerNotify {
                            authListener?.onWarn("PINs do not match")
                        }
                    }
                    confirmPin.text.toString().length != PIN_SIZE -> {
                        listenerNotify {
                            authListener?.onWarn("PIN should be 4 (four) digits long")
                        }
                    }
                    else -> {
                        registerUserPin(view, confirmPin.text.toString())
                    }
                }
            } catch (t: Throwable) {
                listenerNotify {
                    postError(t)
                }
            } finally {
                listenerNotify {
                    view.isClickable = true
                }
            }
        }
    }

    private suspend fun registerUserPin(view: View, confirmPin: String) = viewModelScope.launch(ioContext) {
        try {
            val loggedInUser = user.await().value
            loggedInUser?.let { it ->
                val imie = "45678"
                val androidDevice =
                    "${R.string.android_sdk} ${VERSION.SDK_INT} " +
                        "${Build.BRAND} ${Build.MODEL} ${Build.DEVICE}"

                val hashInput = it.userName.plus(androidDevice).plus(confirmPin)
                repository.updateUser(
                    it.phoneNumber.toString(),
                    imie,
                    androidDevice,
                    armoury.generateFutureToken(
                        SecureString(hashInput.toCharArray(), true)
                    )
                )
                repository.authenticatePin()
                val updatedUser = user.await().value
                listenerNotify {
                    authListener?.onSuccess(updatedUser!!)
                }
            }
        } catch (e: ServiceException) {
            listenerNotify {
                postError(e)
            }
        } catch (e: NoInternetException) {
            listenerNotify {
                postError(e)
            }
        } catch (e: NoConnectivityException) {
            listenerNotify {
                postError(e)
            }
        } finally {
            listenerNotify {
                view.isClickable = true
            }
        }
    }



    fun onRegButtonClick(
        view: View,
        username: TextInputEditText,
        password: TextInputEditText,
        myAppPrefsManager: MyAppPrefsManager?
    ) = viewModelScope.launch(ioContext) {
        try {
            listenerNotify {
                authListener?.onStarted()
                view.isClickable = false
            }
            when {
                username.text.isNullOrEmpty() -> {
                    listenerNotify {
                        authListener?.onWarn("UserName is required")
                    }
                }

                password.text.isNullOrEmpty() -> {
                    listenerNotify {
                        authListener?.onWarn("Password is required")
                    }
                }
                else -> {
                    registerNewUser(username.text.toString(), password.text.toString(), myAppPrefsManager)
                }
            }
        } catch (t: Throwable) {
            listenerNotify {
                postError(t)
            }
        } finally {
            listenerNotify {
                view.isClickable = true
            }
        }
    }

    private suspend fun registerNewUser(userName: String?, password: String?, myAppPrefsManager: MyAppPrefsManager?) {
        try {

            val phoneNumber = "12345457"
            val imie = "45678"
            val androidDevice =
                "${R.string.android_sdk} ${VERSION.SDK_INT} " +
                    "${Build.BRAND} ${Build.MODEL} ${Build.DEVICE}"
            val hashInput = userName.plus(androidDevice).plus(password)
            myAppPrefsManager?.setAccessToken(
                armoury.generateFutureToken(
                SecureString(hashInput.toCharArray(), true)
            ))
            myAppPrefsManager?.setSigned(password)
            repository.userRegister(
                userName!!.trim(),
                password!!,
                phoneNumber,
                imie,
                androidDevice
            )
        } catch (t: Throwable) {
            listenerNotify {
                postError(t)
            }
        }
    }

    private fun postError(t: Throwable) {
        val xiError = XIResult.Error(t.cause ?: t, t.message ?: XIErrorHandler.UNKNOWN_ERROR)
        val readableMessage = XIErrorHandler.humanReadable(xiError)
        authListener?.onFailure(readableMessage)
    }

    fun expirePin() = viewModelScope.launch(ioContext) {
        repository.expirePin()
    }

    fun validatePin(pin: String) = viewModelScope.launch(ioContext) {
        listenerNotify {
            authListener!!.onStarted()
        }
        val loggedInUser = user.await().value
        val inputHash = SecureString(
            loggedInUser!!.userName
                .plus(loggedInUser.device).plus(pin).toCharArray(),
            false
        )
        val result = armoury.validateToken(
            inputHash, loggedInUser.pinHash!!
        )
        if (!result) {
            repository.expirePin()
            listenerNotify { authListener?.onFailure("Invalid PIN entered") }
        } else {
            repository.authenticatePin()
            viewModelScope.launch(ioContext) {
                photoUtil.cleanupDevice()
            }
            listenerNotify { authListener?.onSuccess(loggedInUser) }
        }
        withContext(dispatchers.main()) {
            validPin.value = result
        }
    }

    fun setupAuthListener(mAuthListener: AuthListener) {
        authListener = mAuthListener
    }

    private fun teardownAuthListener() {
        authListener = null
    }

    override fun onCleared() {
        super.onCleared()
        teardownAuthListener()
        supervisorJob.cancelChildren()
    }
}
