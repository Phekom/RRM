package za.co.xisystems.itis_rrm.ui.auth

/**
 * Updated by Shaun McDonald 2020/04/15
 */
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.activity_login.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance
import timber.log.Timber
import www.sanju.motiontoast.MotionToast
import za.co.xisystems.itis_rrm.MainActivity
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.custom.errors.XIErrorHandler
import za.co.xisystems.itis_rrm.custom.results.XIError
import za.co.xisystems.itis_rrm.custom.results.isConnectivityException
import za.co.xisystems.itis_rrm.custom.views.IndefiniteSnackbar
import za.co.xisystems.itis_rrm.data._commons.views.ToastUtils
import za.co.xisystems.itis_rrm.data.localDB.entities.UserDTO
import za.co.xisystems.itis_rrm.databinding.ActivityLoginBinding
import za.co.xisystems.itis_rrm.ui.extensions.motionToast
import za.co.xisystems.itis_rrm.utils.Coroutines
import za.co.xisystems.itis_rrm.utils.PhotoUtil
import za.co.xisystems.itis_rrm.utils.ServiceUtil

class LoginActivity : AppCompatActivity(), View.OnClickListener, AuthListener, KodeinAware {
    private var activityPinLockBinding: ActivityLoginBinding? = null

    private var pin = String()
    private var pinInput = ""
    private var index = 0

    override val kodein by kodein()
    private val factory: AuthViewModelFactory by instance()
    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityPinLockBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_login)
        viewModel = ViewModelProvider(this, factory).get(AuthViewModel::class.java)

        Coroutines.main {
            val loggedInUser = viewModel.user.await()
            loggedInUser.observe(this, { user ->
                // Register the user
                if (user != null) {
                    usernameTextView.text = user.userName
                    initPin()
                    initListener()
                } else {

                    Intent(this, RegisterActivity::class.java).also { home ->
                        home.flags =
                            Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(home)
                    }
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

    private fun initPin() {
        Coroutines.main {
            val loggedInUser = viewModel.user.await()
            loggedInUser.observe(this, { user ->
                if (user.PIN != null) {
                    Coroutines.main {
                        pin = viewModel.getPin()
                    }
                }
            })
        }
    }

    private fun checkPinColor() {
        when (index) {
            1 -> {
                activityPinLockBinding!!.pin1.setImageResource(R.drawable.oval_pin_green)
            }
            2 -> {
                activityPinLockBinding!!.pin2.setImageResource(R.drawable.oval_pin_green)
            }
            3 -> {
                activityPinLockBinding!!.pin3.setImageResource(R.drawable.oval_pin_green)
            }
            4 -> {
                activityPinLockBinding!!.pin4.setImageResource(R.drawable.oval_pin_green)
            }
        }
    }

    private fun initListener() {
        activityPinLockBinding!!.btn0.setOnClickListener(this)
        activityPinLockBinding!!.btn1.setOnClickListener(this)
        activityPinLockBinding!!.btn2.setOnClickListener(this)
        activityPinLockBinding!!.btn3.setOnClickListener(this)
        activityPinLockBinding!!.btn4.setOnClickListener(this)
        activityPinLockBinding!!.btn5.setOnClickListener(this)
        activityPinLockBinding!!.btn6.setOnClickListener(this)
        activityPinLockBinding!!.btn7.setOnClickListener(this)
        activityPinLockBinding!!.btn8.setOnClickListener(this)
        activityPinLockBinding!!.btn9.setOnClickListener(this)
        activityPinLockBinding!!.btnCancel.setOnClickListener(this)
        activityPinLockBinding!!.btnDelete.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v) {
            activityPinLockBinding!!.btnCancel -> {
                reset()
            }
            activityPinLockBinding!!.btnDelete -> {
                when (index) {
                    1 -> {
                        activityPinLockBinding!!.pin1.setImageResource(R.drawable.oval_pin_grey)
                    }
                    2 -> {
                        activityPinLockBinding!!.pin2.setImageResource(R.drawable.oval_pin_grey)
                    }
                    3 -> {
                        activityPinLockBinding!!.pin3.setImageResource(R.drawable.oval_pin_grey)
                    }
                    4 -> {
                        activityPinLockBinding!!.pin4.setImageResource(R.drawable.oval_pin_grey)
                    }
                }
                if (index > 0) {
                    pinInput = pinInput.substring(0, pinInput.length - 1)
                    index--
                }
            }
            else -> {
                if (v is Button) {
                    val pinValue = v.text.toString().toIntOrNull()
                    pinValue?.let {
                        pinInput += it
                        index++
                    }
                }
            }
        }
        Timber.d("<TEST> -> Masuk$index")
        checkPin()
        checkPinColor()
    }

    private fun checkPin() {
        Coroutines.main {
            val loggedInUser = viewModel.user.await()
            loggedInUser.observe(this, { user ->
                if (user.PIN.isNullOrEmpty()) {

                    val builder =
                        AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog)
                    builder.setTitle(R.string.set_pin)
                    builder.setIcon(R.drawable.ic_baseline_lock_24px)
                    builder.setMessage(R.string.set_pin_msg)
                    builder.setCancelable(false)
                    // Yes button
                    builder.setPositiveButton(R.string.ok) { dialog, which ->
                        if (ServiceUtil.isNetworkAvailable(this.applicationContext)) {
                            Intent(this, RegisterPinActivity::class.java).also { pin ->
                                pin.flags =
                                    Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                startActivity(pin)
                            }
                        } else {
                            this.motionToast(getString(R.string.no_connection_detected), MotionToast.TOAST_NO_INTERNET)
                        }
                    }
                    val declineAlert = builder.create()
                    declineAlert.show()
                } else {

                    if (index == 4) {
                        validatePin()
                    }
                }
            })
        }
    }

    override fun onResume() {
        super.onResume()
        reset()
    }

    private fun gotoMainActivity() {
        try {
            finish()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            reset()
        } catch (t: Throwable) {
            val xiErr = XIError(t, "Failed to login")
            if (xiErr.isConnectivityException()) {
                XIErrorHandler.handleError(
                    this.findViewById(R.id.reg_container),
                    xiErr,
                    shouldShowSnackBar = true,
                    refreshAction = { retryGotoMain() }
                )
            } else {
                XIErrorHandler.handleError(
                    this.findViewById(R.id.reg_container),
                    xiErr,
                    shouldToast = true
                )
            }
        }
    }

    private fun retryGotoMain() {
        IndefiniteSnackbar.hide()
        gotoMainActivity()
    }

    private fun validatePin() {
        if (pin == pinInput) {
            Coroutines.io {
                PhotoUtil.cleanupDevice()
            }
            gotoMainActivity()
        } else {
            reset()
            showMessage()
        }
    }

    private fun reset() {
        index = 0
        pinInput = ""
        resetAllPinColor()
    }

    private fun resetAllPinColor() {
        activityPinLockBinding!!.pin1.setImageResource(R.drawable.oval_pin_grey)
        activityPinLockBinding!!.pin2.setImageResource(R.drawable.oval_pin_grey)
        activityPinLockBinding!!.pin3.setImageResource(R.drawable.oval_pin_grey)
        activityPinLockBinding!!.pin4.setImageResource(R.drawable.oval_pin_grey)
    }

    private fun showMessage() {

        this.motionToast("Pin is incorrect", MotionToast.TOAST_ERROR)
        resetAllPinColor()
        pinInput = ""
    }

    override fun onStarted() {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override fun onSuccess(userDTO: UserDTO) {
        this.motionToast("You are Logged in as ${userDTO.userName}", MotionToast.TOAST_INFO)
    }

    override fun onSignOut(userDTO: UserDTO) {
        finishAffinity()
    }

    override fun onFailure(message: String) {
        // To change body of created functions use File | Settings | File Templates.
    }
}
