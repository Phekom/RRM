package za.co.xisystems.itis_rrm.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.activity_login.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance
import za.co.xisystems.itis_rrm.MainActivity
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.data._commons.views.ToastUtils
import za.co.xisystems.itis_rrm.data.localDB.AppDatabase
import za.co.xisystems.itis_rrm.data.localDB.entities.UserDTO
import za.co.xisystems.itis_rrm.databinding.ActivityLoginBinding
import za.co.xisystems.itis_rrm.utils.Coroutines
import za.co.xisystems.itis_rrm.utils.ServiceUtil
import za.co.xisystems.itis_rrm.utils.toast


class LoginActivity : AppCompatActivity(), View.OnClickListener, AuthListener, KodeinAware {
    private var activityPinLockBinding: ActivityLoginBinding? = null
    //    private var pin = PinLock()
    private var pin = String()
    private var pinInput = ""
    private var index = 0
    private val Db: AppDatabase? = null
    //    private val loginViewModel: AuthViewModel? = null
//    private val repository: UserRepository? = null
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
            loggedInUser.observe(this, Observer { user ->
                // Register the user
                if (user != null) {
                    usernameTextView.text = user.userName
                    initPin()
                    initListener()
                } else {
//                    val intent = Intent(this, RegisterActivity::class.java)
//                    startActivity(intent)
//                    finish()



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
            loggedInUser.observe(this, Observer { user ->
                if (user.PIN != null) {
                    Coroutines.main {
                        pin = viewModel.getPin()
                    }


                }

            })
        }

    }

    private fun checkPinColor() {
        if (index == 1) {
            activityPinLockBinding!!.pin1.setImageResource(R.drawable.oval_pin_green)
        } else if (index == 2) {
            activityPinLockBinding!!.pin2.setImageResource(R.drawable.oval_pin_green)
        } else if (index == 3) {
            activityPinLockBinding!!.pin3.setImageResource(R.drawable.oval_pin_green)
        } else if (index == 4) {
            activityPinLockBinding!!.pin4.setImageResource(R.drawable.oval_pin_green)
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
        if (v === activityPinLockBinding!!.btn0) {
            pinInput += "0"
            index++
        } else if (v === activityPinLockBinding!!.btn1) {
            pinInput += "1"
            index++
        } else if (v === activityPinLockBinding!!.btn2) {
            pinInput += "2"
            index++
        } else if (v === activityPinLockBinding!!.btn3) {
            pinInput += "3"
            index++
        } else if (v === activityPinLockBinding!!.btn4) {
            pinInput += "4"
            index++
        } else if (v === activityPinLockBinding!!.btn5) {
            pinInput += "5"
            index++
        } else if (v === activityPinLockBinding!!.btn6) {
            pinInput += "6"
            index++
        } else if (v === activityPinLockBinding!!.btn7) {
            pinInput += "7"
            index++
        } else if (v === activityPinLockBinding!!.btn8) {
            pinInput += "8"
            index++
        } else if (v === activityPinLockBinding!!.btn9) {
            pinInput += "9"
            index++
        } else if (v === activityPinLockBinding!!.btnCancel) {
            reset()
        } else if (v === activityPinLockBinding!!.btnDelete) {
            if (index == 0) {
                // TODO: What are we planning to do here?
            } else {
                if (index == 1) {
                    activityPinLockBinding!!.pin1.setImageResource(R.drawable.oval_pin_grey)
                } else if (index == 2) {
                    activityPinLockBinding!!.pin2.setImageResource(R.drawable.oval_pin_grey)
                } else if (index == 3) {
                    activityPinLockBinding!!.pin3.setImageResource(R.drawable.oval_pin_grey)
                } else if (index == 4) {
                    activityPinLockBinding!!.pin4.setImageResource(R.drawable.oval_pin_grey)
                }
                pinInput = pinInput.substring(0, pinInput.length - 1)
                index--
            }
        }
        Log.d("<TEST>", "Masuk$index")
        checkPin()
        checkPinColor()
    }

    private fun checkPin() {
        Coroutines.main {
            val loggedInUser = viewModel.user.await()
            loggedInUser.observe(this, Observer { user ->
                if (user.PIN.isNullOrEmpty()){

                    val logoutBuilder = AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog)
                    logoutBuilder.setTitle(R.string.set_pin)
                    logoutBuilder.setIcon(R.drawable.ic_baseline_lock_24px)
                    logoutBuilder.setMessage(R.string.set_pin_msg)
                    logoutBuilder.setCancelable(false)
                    // Yes button
                    logoutBuilder.setPositiveButton(R.string.ok) { dialog, which ->
                        if (ServiceUtil.isNetworkConnected(this.applicationContext)) {
                            Intent(this, RegisterPinActivity::class.java).also {pin ->
                                pin.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                startActivity(pin)
                            }
                        } else {
                            toast(R.string.no_connection_detected.toString())
                        }
                    }
                    val declineAlert = logoutBuilder.create()
                    declineAlert.show()

                }

                else {

                    if (index == 4) {
//                    if (user.PIN!!.isNotEmpty()) {
////                        Toast.makeText(this, "Pin Successfully registered", Toast.LENGTH_SHORT).show()
//                        gotoMainActivity()
//                    } else {
                        validatePin()
//                    }
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
        finish()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        reset()
    }

    private fun validatePin() {
        if (pin == pinInput) {

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
//        if (valid) {
//            gotoMainActivity()
//        } else { // toast("Pin Invalid Try Again !!")
            Toast.makeText(this, "Pin is incorrect", Toast.LENGTH_SHORT).show()
            resetAllPinColor()
            pinInput = ""
//        }
    }

    override fun onStarted() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onSuccess(userDTO: UserDTO) {
        toast("You are Logged in as ${userDTO.userName}")
    }

    override fun onSignOut(userDTO: UserDTO) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onFailure(message: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}