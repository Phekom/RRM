package za.co.xisystems.itis_rrm.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.activity_register.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance
import za.co.xisystems.itis_rrm.MainActivity
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.data.localDB.entities.UserDTO
import za.co.xisystems.itis_rrm.databinding.ActivityRegisterBinding
import za.co.xisystems.itis_rrm.utils.*

class RegisterActivity : AppCompatActivity(), AuthListener  , KodeinAware {

    override val kodein by kodein()
    private val factory : AuthViewModelFactory by instance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding :ActivityRegisterBinding = DataBindingUtil.setContentView(this,R.layout.activity_register)
        val viewModel = ViewModelProviders.of(this, factory).get(AuthViewModel::class.java)
        binding.viewmodel = viewModel
        viewModel.authListener = this

         viewModel.getLoggedInUser().observe(this, Observer {user ->
             if (user != null){
                  Intent(this, MainActivity::class.java).also {
                      it.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                      startActivity(it)
                  }
             }
//             else {
//                 Intent(this, RegisterActivity::class.java).also {
//                     startActivity(it)
//
//                 }
//             }
         })

//        viewModel.getUserRole().observe(this, Observer {user ->
//            if (user != null){
//                Intent(this, MainActivity::class.java).also {
//                    it.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
//                    startActivity(it)
//                }
//            }
////             else {
////                 Intent(this, RegisterActivity::class.java).also {
////                     startActivity(it)
////
////                 }
////             }
//        })

    }

    override fun onStarted() {
        loading.show()
        hideKeyboard()
    }

    override fun onSuccess(userDTO:UserDTO) {
            loading.hide()
        toast("You are Loggedin as ${userDTO.userName}")
    }

    override fun onFailure(message: String) {
        loading.hide()
        reg_container.snackbar(message)
    }

    override fun onSignOut(userDTO: UserDTO) {

    }

}


