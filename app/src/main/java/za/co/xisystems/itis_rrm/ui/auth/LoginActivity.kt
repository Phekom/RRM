package za.co.xisystems.itis_rrm.ui.auth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.activity_login.*
import org.kodein.di.KodeinAware
import za.co.xisystems.itis_rrm.MainActivity
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.data.localDB.AppDatabase
import za.co.xisystems.itis_rrm.data.repositories.UserRepository
import za.co.xisystems.itis_rrm.databinding.ActivityLoginBinding
import za.co.xisystems.itis_rrm.ui.auth.model.PinLock
import za.co.xisystems.itis_rrm.utils.Coroutines
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance
import za.co.xisystems.itis_rrm.data.localDB.entities.UserDTO
import za.co.xisystems.itis_rrm.utils.toast

class LoginActivity : AppCompatActivity(), View.OnClickListener, AuthListener  , KodeinAware {
    private var activityPinLockBinding: ActivityLoginBinding? = null
    private val pin = PinLock()
    private var pinInput = ""
    private var index = 0
    private val Db: AppDatabase? = null
    private val loginViewModel: AuthViewModel? = null
    private val repository: UserRepository? = null
    override val kodein by kodein()
    private val factory : AuthViewModelFactory by instance()
    lateinit var viewModel : AuthViewModel
    private lateinit var appContext: Context


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityPinLockBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_login)
        viewModel = ViewModelProviders.of(this, factory).get(AuthViewModel::class.java)

        Coroutines.main {
            val loggedInUser = viewModel.user.await()
            loggedInUser.observe(this, Observer {user ->
                // Register the user
                if (user != null) {
                    usernameTextView.text = user.userName
                    initPin()
                    initListener()
                }else{
//                    val intent = Intent(this, RegisterActivity::class.java)
//                    startActivity(intent)
//                    finish()
//
                    Intent(this, RegisterActivity::class.java).also {home ->
                        home.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(home)
                    }
                }
            })

        }

    }



    private fun initPin() { //        String newpin =  loginViewModel.getPin();
//        if(loginViewModel.getPin() != null) {
////            pin = usr.getPin();
//            Toast.makeText(this, newpin, Toast.LENGTH_SHORT).show();
//        }
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
        if (index == 4) {
            if (!pin.registered) { //                db.insertPin(pinInput);
                Toast.makeText(this, "Pin Successfully registered", Toast.LENGTH_SHORT).show()
                gotoMainActivity()
            } else {
                validatePin()
            }
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
        if (pin.lockNumber == pinInput) {
            pin.valid = true
        } else {
            pin.valid = false
        }
        reset()
        showMessage(pin.valid)
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

    private fun showMessage(valid: Boolean) {
        if (valid) {
            gotoMainActivity()
        } else {
            Toast.makeText(this, "Pin is incorrect", Toast.LENGTH_SHORT).show()
            resetAllPinColor()
            pinInput = ""
        }
    }

    override fun onStarted() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onSuccess(userDTO: UserDTO) {
        toast("You are Loggedin as ${userDTO.userName}")
    }

    override fun onSignOut(userDTO: UserDTO) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onFailure(message: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}