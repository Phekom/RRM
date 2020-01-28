package za.co.xisystems.itis_rrm.ui.auth

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GooglePlayServicesUtil
import kotlinx.android.synthetic.main.activity_register.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance
import za.co.xisystems.itis_rrm.MainActivity
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.data.localDB.entities.UserDTO
import za.co.xisystems.itis_rrm.databinding.ActivityRegisterBinding
import za.co.xisystems.itis_rrm.databinding.ActivityRegisterPinBinding
import za.co.xisystems.itis_rrm.utils.*

private const val PERMISSION_REQUEST = 10

class RegisterPinActivity : AppCompatActivity(), AuthListener  , KodeinAware ,Runnable{
    companion object{
        val TAG: String = RegisterPinActivity::class.java.simpleName
    }

    override val kodein by kodein()
    private val factory : AuthViewModelFactory by instance()
    lateinit var viewModel : AuthViewModel
    private lateinit var appContext: Context
    private var permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE ,Manifest.permission.ACCESS_FINE_LOCATION )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appContext = this

        if ( startPermissionRequest(appContext.applicationContext, permissions)){
            toast("Permissions Are already provided ")
        }else{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(permissions, PERMISSION_REQUEST)
            }
        }

        val binding :ActivityRegisterPinBinding = DataBindingUtil.setContentView(this, R.layout.activity_register_pin)
        viewModel = ViewModelProviders.of(this, factory).get(AuthViewModel::class.java)
        binding.viewmodel = viewModel
        viewModel.authListener = this

        Coroutines.main {
            val loggedInUser = viewModel.user.await()
            loggedInUser.observe(this, Observer {user ->
                // Register the user
                if (user != null) {
                    if (!user.PIN.equals(null)) {
                        Intent(this, MainActivity::class.java).also { home ->
                            home.flags =
                                Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(home)
                        }
                    }
                }
            })

        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode ==  PERMISSION_REQUEST){
            var allAllowed = true
            for (i in permissions.indices){
               if (grantResults[i] == PackageManager.PERMISSION_DENIED){
                   allAllowed = false
                   var requestAgain = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                       shouldShowRequestPermissionRationale(permissions[i])
                   } else {
                       TODO("VERSION.SDK_INT < M")
                   }
                   if (requestAgain){
                       toast("Permission Denied")
                   }else{
                       toast("Please Enable Pemmisions from your Device Settings")
                   }
               }
            }
            if (allAllowed)
                toast("Permissons Granted")
        }
    }

    fun  startPermissionRequest(context : Context, permissions: Array<String>) : Boolean {
        var allAccess = true
        for (i in permissions.indices){
            if(checkCallingOrSelfPermission(permissions[i]) == PackageManager.PERMISSION_DENIED){
                allAccess =  false

            }
        }
        return allAccess

    }


    override fun onStart() {
        super.onStart()
        val resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this)
        if (resultCode != ConnectionResult.SUCCESS) { //This dialog will help the user update to the latest GooglePlayServices
            val dialog =
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0)
            dialog?.show()
        }
    }

    override fun onStarted() {
        loading.show()
        hideKeyboard()
    }

    override fun onSuccess(user:UserDTO) {
            loading.hide()

        toast("You are Loggedin as ${user.userName}")
    }

    override fun onFailure(message: String) {
        loading.hide()
        hideKeyboard()
        reg_container.snackbar(message)
    }




    override fun onSignOut(user: UserDTO) {

    }

    override fun run() {
        Coroutines.main {
            val contracts = viewModel.offlinedata.await()
            contracts.observe(this, Observer { contrcts ->

            })
        }

    }


}


