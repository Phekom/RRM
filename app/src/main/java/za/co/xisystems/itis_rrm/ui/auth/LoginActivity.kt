/**
 * Updated by Shaun McDonald on 2021/05/15
 * Last modified on 2021/05/14, 20:32
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 **/

package za.co.xisystems.itis_rrm.ui.auth

/**
 * Updated by Shaun McDonald 2020/04/15
 */
import android.R.style
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog.Builder
import androidx.lifecycle.ViewModelProvider
import com.poovam.pinedittextfield.PinField
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.kodein.di.DIAware
import org.kodein.di.android.closestDI
import org.kodein.di.instance
import timber.log.Timber
import za.co.xisystems.itis_rrm.MainActivity
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.constants.Constants.TWO_SECONDS
import za.co.xisystems.itis_rrm.custom.errors.XIErrorHandler
import za.co.xisystems.itis_rrm.custom.results.XIResult.Error
import za.co.xisystems.itis_rrm.custom.results.isRecoverableException
import za.co.xisystems.itis_rrm.custom.views.IndefiniteSnackbar
import za.co.xisystems.itis_rrm.data._commons.views.ToastUtils
import za.co.xisystems.itis_rrm.data.localDB.entities.UserDTO
import za.co.xisystems.itis_rrm.databinding.ActivityLoginBinding
import za.co.xisystems.itis_rrm.extensions.exitApplication
import za.co.xisystems.itis_rrm.ui.auth.model.AuthViewModel
import za.co.xisystems.itis_rrm.ui.auth.model.AuthViewModelFactory
import za.co.xisystems.itis_rrm.ui.base.BaseActivity
import za.co.xisystems.itis_rrm.utils.Coroutines
import za.co.xisystems.itis_rrm.utils.ServiceUtil
import za.co.xisystems.itis_rrm.utils.hide
import za.co.xisystems.itis_rrm.utils.hideKeyboard
import za.co.xisystems.itis_rrm.utils.show
import za.co.xisystems.itis_rrm.utils.snackbar
import za.co.xisystems.itis_rrm.utils.toast
import za.co.xisystems.traffic_count.delegates.viewBinding

class LoginActivity : BaseActivity(), AuthListener, DIAware {

    override val di by closestDI()
    private val factory: AuthViewModelFactory by instance<AuthViewModelFactory>()
    private lateinit var authViewModel: AuthViewModel
    private val binding by viewBinding(ActivityLoginBinding::inflate)
    private lateinit var enteredPin: String
    private var doubleBackToExitPressed = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authViewModel = ViewModelProvider(this, factory).get(AuthViewModel::class.java)
        authViewModel.setupAuthListener(this)

        val pinListener = object : PinField.OnTextCompleteListener {
            override fun onTextComplete(enteredText: String): Boolean {
                enteredPin = enteredText
                validatePin(enteredPin)
                return true
            }
        }

        binding.pinField.onTextCompleteListener = pinListener

        Coroutines.main {
            val loggedInUser = authViewModel.user.await()
            loggedInUser.observe(this) { user ->
                // Register the user
                if (user != null) {
                    when {
                        user.userStatus != "Y" -> {
                            onFailure(getString(R.string.user_blocked, user.userName))
                            binding.pinField.isEnabled = false
                        }
                        user.pinHash != null -> {
                            authorizeUser(user)
                        }
                        else -> {
                            registerUserPin()
                        }
                    }
                } else {
                    registerUser()
                }
            }

            binding.serverTextView.setOnClickListener {
                ToastUtils().toastServerAddress(this.applicationContext)
            }

            binding.buildFlavorTextView.setOnClickListener {
                ToastUtils().toastVersion(this.applicationContext)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        reset()
    }

    private fun authorizeUser(user: UserDTO) {
        binding.usernameTextView.text = user.userName
    }

    private fun registerUser() {
        Intent(this, RegisterActivity::class.java).also { registerUser ->
            registerUser.flags =
                Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(registerUser)
        }
    }

    private fun validatePin(pin: String) {
        Coroutines.io {
            authViewModel.validatePin(pin)
        }
    }

    private fun registerUserPin() {
        val builder =
            Builder(this, style.Theme_DeviceDefault_Dialog)
        builder.setTitle(R.string.set_pin)
        builder.setIcon(R.drawable.ic_baseline_lock_24px)
        builder.setMessage(R.string.set_pin_msg)
        builder.setCancelable(false)
        // Yes button
        builder.setPositiveButton(R.string.ok) { _, _ ->
            if (ServiceUtil.isNetworkAvailable(this.applicationContext)) {
                Intent(this, RegisterPinActivity::class.java).also { registerPinAct ->
                    registerPinAct.flags =
                        Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(registerPinAct)
                }
            } else {
                toast("No internet connection detected")
            }
        }
        val setPinDialog = builder.create()
        setPinDialog.show()
    }

    // Double-back press to exit application
    override fun onBackPressed() {
        doubleBackToExitPressed++
        if (doubleBackToExitPressed == 2) {
            exitApplication()
        } else {
            toast("Please press Back again to exit")
            Handler(mainLooper).postDelayed({
                doubleBackToExitPressed = 0
            }, TWO_SECONDS)
        }
    }

    private fun gotoMainActivity() {
        try {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            reset()
        } catch (t: Throwable) {
            val xiErr = Error(t, "Failed to login")
            if (xiErr.isRecoverableException()) {
                XIErrorHandler.handleError(
                    view = findViewById(R.id.reg_container),
                    throwable = xiErr,
                    shouldShowSnackBar = true,
                    refreshAction = { this.retryGotoMain() }
                )
            } else {
                XIErrorHandler.handleError(
                    view = findViewById(R.id.reg_container),
                    throwable = xiErr,
                    shouldToast = true
                )
            }
        }
    }

    private fun retryGotoMain() {
        IndefiniteSnackbar.hide()
        gotoMainActivity()
    }

    private fun reset() {
        enteredPin = ""
        binding.pinField.text?.clear()
        binding.pinField.requestFocus()
    }

    override fun onStarted() {
        Timber.d("AuthInit")
        hideKeyboard()
        binding.loading.show()
    }

    override fun onSuccess(userDTO: UserDTO) {
        binding.loading.hide()
        hideKeyboard()
        toast("You are Logged in as ${userDTO.userName}")
        gotoMainActivity()
    }

    override fun onSignOut(userDTO: UserDTO) {
        binding.loading.hide()
        hideKeyboard()
        closeApp()
    }

    override fun startLongRunningTask() {
        Timber.i("starting task...")
        hideKeyboard()
        binding.loading.show()
        // Make UI untouchable for duration of task
        window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
        window.setFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )
    }

    override fun endLongRunningTask() {
        Timber.i("stopping task...")
        hideKeyboard()
        binding.loading.hide()

        // Re-enable UI touches
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun closeApp() {
        Coroutines.io {
            authViewModel.expirePin()
            withContext(Dispatchers.Main.immediate) {
                finish()
            }
        }
    }

    override fun onWarn(message: String) {
        binding.loading.hide()
        hideKeyboard()
        toast(message)
    }

    override fun onFailure(message: String) {
        binding.loading.hide()
        hideKeyboard()
        reset()
        binding.regContainer.snackbar(message)
    }
}
