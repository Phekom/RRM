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
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AlertDialog.Builder
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.activity_login.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance
import timber.log.Timber
import za.co.xisystems.itis_rrm.MainActivity
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.constants.Constants.TWO_SECONDS
import za.co.xisystems.itis_rrm.custom.errors.XIErrorHandler
import za.co.xisystems.itis_rrm.custom.results.XIError
import za.co.xisystems.itis_rrm.custom.results.isRecoverableException
import za.co.xisystems.itis_rrm.custom.views.IndefiniteSnackbar
import za.co.xisystems.itis_rrm.data._commons.views.ToastUtils
import za.co.xisystems.itis_rrm.data.localDB.entities.UserDTO
import za.co.xisystems.itis_rrm.databinding.ActivityLoginBinding
import za.co.xisystems.itis_rrm.ui.auth.model.AuthViewModel
import za.co.xisystems.itis_rrm.ui.auth.model.AuthViewModelFactory
import za.co.xisystems.itis_rrm.utils.Coroutines
import za.co.xisystems.itis_rrm.utils.ServiceUtil
import za.co.xisystems.itis_rrm.utils.hide
import za.co.xisystems.itis_rrm.utils.hideKeyboard
import za.co.xisystems.itis_rrm.utils.snackbar
import za.co.xisystems.itis_rrm.utils.toast
import java.util.concurrent.TimeUnit
import javax.xml.datatype.DatatypeConstants.SECONDS

class LoginActivity : AppCompatActivity(), View.OnClickListener, AuthListener, KodeinAware {
    private var binding: ActivityLoginBinding? = null

    private var pinInput = ""
    private var index = 0

    override val kodein by kodein()
    private val factory: AuthViewModelFactory by instance()
    private lateinit var viewModel: AuthViewModel
    private var doubleBackToExitPressed = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =
            DataBindingUtil.setContentView(this, R.layout.activity_login)
        viewModel = ViewModelProvider(this, factory).get(AuthViewModel::class.java)
        viewModel.setupAuthListener(this)
        Coroutines.main {
            val loggedInUser = viewModel.user.await()
            loggedInUser.observe(this, { user ->
                // Register the user
                if (user != null) {
                    if (user.pinHash != null) {
                        when (user.authd) {
                            true -> {
                                gotoMainActivity()
                            }
                            else -> {
                                usernameTextView.text = user.userName
                                initListener()
                            }
                        }
                    } else {
                        registerUserPin()
                    }
                } else {
                    registerUser()
                }
            })

            serverTextView.setOnClickListener {
                ToastUtils().toastServerAddress(this.applicationContext)
            }

            buildFlavorTextView.setOnClickListener {
                ToastUtils().toastVersion(this.applicationContext)
            }
        }
    }

    private fun registerUser() {
        Intent(this, RegisterActivity::class.java).also { registerUser ->
            registerUser.flags =
                Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(registerUser)
        }
    }

    private fun checkPinlights() {
        checkDisabledPinlights()
        checkPinColor()
    }

    private fun checkPinColor() {
        when (index) {
            1 -> binding!!.pin1.setImageResource(R.drawable.oval_pin_green)
            2 -> binding!!.pin2.setImageResource(R.drawable.oval_pin_green)
            3 -> binding!!.pin3.setImageResource(R.drawable.oval_pin_green)
            4 -> binding!!.pin4.setImageResource(R.drawable.oval_pin_green)
        }
    }

    private fun initListener() {
        binding!!.btn0.setOnClickListener(this)
        binding!!.btn1.setOnClickListener(this)
        binding!!.btn2.setOnClickListener(this)
        binding!!.btn3.setOnClickListener(this)
        binding!!.btn4.setOnClickListener(this)
        binding!!.btn5.setOnClickListener(this)
        binding!!.btn6.setOnClickListener(this)
        binding!!.btn7.setOnClickListener(this)
        binding!!.btn8.setOnClickListener(this)
        binding!!.btn9.setOnClickListener(this)
        binding!!.btnCancel.setOnClickListener(this)
        binding!!.btnDelete.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            binding!!.btnCancel.id -> {
                reset()
            }
            binding!!.btnDelete.id -> {
                if (index > 0) {
                    pinInput = pinInput.substring(0, pinInput.length - 1)
                    index--
                }
            }
            else -> {
                if (v is Button) {
                    val pinValue = v.text.toString().toIntOrNull()
                    pinValue?.let {
                        pinInput = pinInput.plus(it.toString())
                        index++
                    }
                }
            }
        }
        Timber.d("<TEST> -> Masuk$index")
        checkPinlights()
        if (index == 4) {
            Coroutines.main {
                viewModel.validatePin(pinInput)
            }
        }
    }

    private fun checkDisabledPinlights() {
        when (index) {
            1 -> binding!!.pin1.setImageResource(R.drawable.oval_pin_grey)
            2 -> binding!!.pin2.setImageResource(R.drawable.oval_pin_grey)
            3 -> binding!!.pin3.setImageResource(R.drawable.oval_pin_grey)
            4 -> binding!!.pin4.setImageResource(R.drawable.oval_pin_grey)
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
                Intent(this, RegisterPinActivity::class.java).also { pin ->
                    pin.flags =
                        Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(pin)
                }
            } else {
                toast("No internet connection detected")
            }
        }
        val setPinDialog = builder.create()
        setPinDialog.show()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (doubleBackToExitPressed == 2) {
            finishAffinity()
        } else {
            doubleBackToExitPressed++
            toast("Please press Back again to exit")
        }

        Handler(mainLooper).postDelayed({
            doubleBackToExitPressed = 1
        }, TWO_SECONDS)
    }

    override fun onResume() {
        super.onResume()
        reset()
    }

    private fun gotoMainActivity() {
        try {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            reset()
        } catch (t: Throwable) {
            val xiErr = XIError(t, "Failed to login")
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
        index = 0
        pinInput = ""
        resetAllPinColor()
    }

    private fun resetAllPinColor() {
        binding!!.pin1.setImageResource(R.drawable.oval_pin_grey)
        binding!!.pin2.setImageResource(R.drawable.oval_pin_grey)
        binding!!.pin3.setImageResource(R.drawable.oval_pin_grey)
        binding!!.pin4.setImageResource(R.drawable.oval_pin_grey)
    }

    override fun onStarted() {
        Timber.d("AuthInit")
    }

    override fun onSuccess(userDTO: UserDTO) {
        toast("You are Logged in as ${userDTO.userName}")
        gotoMainActivity()
    }

    override fun onSignOut(userDTO: UserDTO) {
        finishAffinity()
    }

    override fun onWarn(message: String) {
        onFailure(message)
    }

    override fun onFailure(message: String) {
        loading.hide()
        hideKeyboard()
        reset()
        reg_container.snackbar(message)
    }
}
