package za.co.xisystems.itis_rrm.ui.auth

/**
 * Updated by Shaun McDonald - 2020/04/15
 */
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import org.kodein.di.DIAware
import org.kodein.di.android.closestDI
import org.kodein.di.instance
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.data._commons.views.ToastUtils
import za.co.xisystems.itis_rrm.data.localDB.entities.UserDTO
import za.co.xisystems.itis_rrm.data.network.PermissionController
import za.co.xisystems.itis_rrm.databinding.ActivityRegisterBinding
import za.co.xisystems.itis_rrm.extensions.isConnected
import za.co.xisystems.itis_rrm.ui.auth.model.AuthViewModel
import za.co.xisystems.itis_rrm.ui.auth.model.AuthViewModelFactory
import za.co.xisystems.itis_rrm.utils.Coroutines
import za.co.xisystems.itis_rrm.utils.ServiceUtil
import za.co.xisystems.itis_rrm.utils.hide
import za.co.xisystems.itis_rrm.utils.hideKeyboard
import za.co.xisystems.itis_rrm.utils.show
import za.co.xisystems.itis_rrm.utils.snackbar
import za.co.xisystems.itis_rrm.utils.toast

private const val PERMISSION_REQUEST = 10

class RegisterActivity : AppCompatActivity(), AuthListener, DIAware {

    override val di by closestDI()
    private val factory: AuthViewModelFactory by instance()
    private lateinit var viewModel: AuthViewModel
    private var permissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_NETWORK_STATE,
        Manifest.permission.ACCESS_WIFI_STATE,
    )

    companion object {
        val TAG: String = RegisterActivity::class.java.simpleName
    }

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (startPermissionRequest(permissions)) {
            toast("Permissions already provided.")
        } else {
            requestPermissions(permissions, PERMISSION_REQUEST)
        }

        viewModel = ViewModelProvider(this, factory).get(AuthViewModel::class.java)
        viewModel.setupAuthListener(this)

        Coroutines.main {
            binding.registerbutton.setOnClickListener {
                val username = binding.registerusernameeditText
                val password = binding.registerpasswordeditText
                viewModel.onRegButtonClick(it, username, password)
                // ToastUtils().toastServerAddress(this.applicationContext)
            }

            when (this@RegisterActivity.isConnected) {
                true -> {
                    val loggedInUser = viewModel.user.await()
                    loggedInUser.observe(this, { user ->
                        // Register the user
                        if (user != null) {
                            when {
                                user.userStatus != "Y" -> {
                                    onFailure(getString(R.string.user_blocked, user.userName))
                                }
                                user.pinHash == null -> {
                                    Coroutines.main {
                                        registerPinOrNot()
                                    }
                                }
                                else -> {
                                    authorizeUser()
                                }
                            }
                        }
                    })
                }
                else -> {
                    onFailure("This step requires an active internet connection.")
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

    private fun authorizeUser() {
        Intent(this, LoginActivity::class.java).also { login ->
            login.flags =
                Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(login)
        }
    }

    private fun registerPinOrNot() {
        val builder =
            AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog)
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
            if (allAllowed) toast("Permissions Granted")
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
                val requestAgain = shouldShowRequestPermissionRationale(permissions[i])
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
        googlePlayServicesCheck(this)
    }

    private fun googlePlayServicesCheck(activity: Activity) {
        val resultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(activity.applicationContext)
        if (resultCode != ConnectionResult.SUCCESS) {
            // This dialog will help the user update to the latest GooglePlayServices
            val dialog =
                GoogleApiAvailability.getInstance().getErrorDialog(activity, resultCode, 0)
            dialog?.show()
        }
        if (PermissionController.checkPermissionsEnabled(applicationContext)) {
            // googleApiClient!!.connect()
        } else {
            PermissionController.startPermissionRequests(activity)
        }
    }

    override fun onStarted() {
        binding.loading.show()
        hideKeyboard()
    }

    override fun onSuccess(userDTO: UserDTO) {
        binding.loading.hide()
        toast("You are registered as ${userDTO.userName}")
    }

    override fun onFailure(message: String) {
        binding.loading.hide()
        hideKeyboard()
        binding.regContainer.snackbar(message)
    }

    override fun onSignOut(userDTO: UserDTO) {
        userDTO.authd = false
    }

    override fun onWarn(message: String) {
        onFailure(message)
    }
}
