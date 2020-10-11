package za.co.xisystems.itis_rrm.ui.auth

/**
 * Updated by Shaun McDonald - 2020/04/15
 */
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
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GooglePlayServicesUtil
import kotlinx.android.synthetic.main.activity_register.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance
import za.co.xisystems.itis_rrm.MainActivity
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.data._commons.views.ToastUtils
import za.co.xisystems.itis_rrm.data.localDB.entities.UserDTO
import za.co.xisystems.itis_rrm.data.network.PermissionController
import za.co.xisystems.itis_rrm.databinding.ActivityRegisterBinding
import za.co.xisystems.itis_rrm.utils.Coroutines
import za.co.xisystems.itis_rrm.utils.ServiceUtil
import za.co.xisystems.itis_rrm.utils.hide
import za.co.xisystems.itis_rrm.utils.hideKeyboard
import za.co.xisystems.itis_rrm.utils.show
import za.co.xisystems.itis_rrm.utils.snackbar
import za.co.xisystems.itis_rrm.utils.toast

private const val PERMISSION_REQUEST = 10

class RegisterActivity : AppCompatActivity(), AuthListener, KodeinAware, Runnable {

    override val kodein by kodein()
    private val factory: AuthViewModelFactory by instance<AuthViewModelFactory>()
    private lateinit var viewModel: AuthViewModel
    private lateinit var appContext: Context
    private var permissions = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.CAMERA,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.READ_SMS

    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appContext = this
        if (startPermissionRequest(permissions)) {
            toast("Permissions already provided.")
        } else {
            // The only fallback from Marshmallow is to grant all permissions
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(permissions, PERMISSION_REQUEST)
            }
        }

        val binding: ActivityRegisterBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_register)
        viewModel = ViewModelProvider(this, factory).get(AuthViewModel::class.java)
        binding.viewmodel = viewModel
        viewModel.authListener = this

        Coroutines.main {
            val loggedInUser = viewModel.user.await()
            loggedInUser.observe(this, Observer { user ->
                // Register the user
                if (user != null) {

                    if (user.PIN.isNullOrEmpty()) {
                        registerPinOrNot()
                    } else
                        Intent(this, MainActivity::class.java).also { home ->
                            home.flags =
                                Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(home)
                        }
                }
            })
            serverTextView.setOnClickListener {
                ToastUtils().toastServerAddress(appContext)
            }

            buildFlavorTextView.setOnClickListener {
                ToastUtils().toastVersion(appContext)
            }
        }
        isOnline()
    }

    private fun registerPinOrNot() {
        val builder =
            AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog)
        builder.setTitle(R.string.set_pin)
        builder.setIcon(R.drawable.ic_baseline_lock_24px)
        builder.setMessage(R.string.set_pin_msg)
        builder.setCancelable(false)
        // Yes button
        builder.setPositiveButton(R.string.ok) { dialog, which ->
            if (ServiceUtil.isInternetAvailable(this.applicationContext)) {
                Intent(this, RegisterPinActivity::class.java).also { pin ->
                    pin.flags =
                        Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(pin)
                }
            } else {
                toast(R.string.no_connection_detected.toString())
            }
        }
        val pinAlert = builder.create()
        pinAlert.show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST) {
            val allAllowed = requestAgain(permissions, grantResults)
            if (allAllowed)
                toast("Permissions Granted")
        }
    }

    private fun requestAgain(
        permissions: Array<out String>,
        grantResults: IntArray
    ): Boolean {
        var allAllowed = true
        for (i in permissions.indices) {
            if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                allAllowed = false
                val requestAgain = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    shouldShowRequestPermissionRationale(permissions[i])
                } else {
                    // Fallback granting all permissions
                    false
                }
                if (requestAgain) {
                    toast("Permission Denied")
                } else {
                    toast("Please Enable Permissions from your Device Settings")
                }
            }
        }
        return allAllowed
    }

    private fun startPermissionRequest(permissions: Array<String>): Boolean {
        var allAccess = true
        for (i in permissions.indices) {
            if (checkCallingOrSelfPermission(permissions[i]) == PackageManager.PERMISSION_DENIED) {
                allAccess = false
            }
        }
        return allAccess
    }

    override fun onStart() {
        super.onStart()
        val resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this)
        if (resultCode != ConnectionResult.SUCCESS) { // This dialog will help the user update to the latest GooglePlayServices
            val dialog =
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0)
            dialog?.show()
        }
        if (PermissionController.checkPermissionsEnabled(applicationContext)) {
//            googleApiClient!!.connect()
        } else {
            PermissionController.startPermissionRequests(this)
        }
    }

    override fun onStarted() {
        loading.show()
        hideKeyboard()
    }

    override fun onSuccess(userDTO: UserDTO) {
        loading.hide()
        toast("You are logged in as ${userDTO.userName}")
        this.run()
    }

    override fun onFailure(message: String) {
        loading.hide()
        hideKeyboard()
        reg_container.snackbar(message)
    }

    private fun isOnline(): Boolean {
        return ServiceUtil.isInternetAvailable(this.applicationContext)
    }

    override fun onSignOut(userDTO: UserDTO) {
        finishAffinity()
    }

    override fun run() {
        Coroutines.main {
            val contractList = viewModel.offlineData.await()
            contractList.observe(this, Observer { contractItems ->
                toast("Loading contract: ${contractItems.size} / ${contractItems.count()}")
            })
        }
    }

    companion object {
        val TAG: String = RegisterActivity::class.java.simpleName
    }
}
