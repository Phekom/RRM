package za.co.xisystems.itis_rrm.ui.auth

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import org.kodein.di.instance
import za.co.xisystems.itis_rrm.ui.mainview.activities.main.MainActivity
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.custom.errors.XIErrorHandler
import za.co.xisystems.itis_rrm.custom.results.XIResult.Error
import za.co.xisystems.itis_rrm.custom.results.isRecoverableException
import za.co.xisystems.itis_rrm.custom.views.IndefiniteSnackbar
import za.co.xisystems.itis_rrm.data._commons.views.ToastUtils
import za.co.xisystems.itis_rrm.data.localDB.entities.UserDTO
import za.co.xisystems.itis_rrm.databinding.ActivityRegisterPinBinding
import za.co.xisystems.itis_rrm.ui.auth.model.AuthViewModel
import za.co.xisystems.itis_rrm.ui.auth.model.AuthViewModelFactory
import za.co.xisystems.itis_rrm.ui.base.BaseActivity
import za.co.xisystems.itis_rrm.utils.*


class RegisterPinActivity : BaseActivity(), AuthListener {
    companion object {
        val TAG: String = RegisterPinActivity::class.java.simpleName
        const val GOOGLE_PLAY_SERVICES_RESOLUTION_REQUEST = 1
        private const val PERMISSION_REQUEST = 10
    }

    private val factory: AuthViewModelFactory by instance()
    private lateinit var viewModel: AuthViewModel
    private lateinit var appContext: Context
    private var permissions = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.CAMERA,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    private lateinit var binding: ActivityRegisterPinBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterPinBinding.inflate(layoutInflater)
        setContentView(binding.root)

        appContext = this

        if (startPermissionRequest(permissions)) {
            toast("Permissions are already provided ")
        } else {
            requestPermissions(permissions, PERMISSION_REQUEST)
        }

        viewModel = ViewModelProvider(this, factory).get(AuthViewModel::class.java)
        viewModel.authListener = this

        Coroutines.main {
            binding.registerPinbutton.setOnClickListener {
                val enterPin = binding.enterPinEditText
                val confirmPin = binding.confirmPinEditText

                viewModel.onRegPinButtonClick(it, enterPin, confirmPin)
            }

            val loggedInUser = viewModel.user.await()
            loggedInUser.observe(this) { user ->
                // Register the user
                if (user?.pinHash != null) {
                    Intent(this, LoginActivity::class.java).also { login ->
                        login.flags =
                            Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(login)
                    }
                }
            }
            binding.serverTextView.setOnClickListener {
                ToastUtils().toastServerAddress(appContext)
            }

            binding.buildFlavorTextView.setOnClickListener {
                ToastUtils().toastVersion(appContext)
            }
        }
    }

    override fun startLongRunningTask() {

    }

    override fun endLongRunningTask() {

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST -> {
                var allAllowed = true
                for (i in permissions.indices) {
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        allAllowed = false
                        val requestAgain = shouldShowRequestPermissionRationale(permissions[i])
                        if (requestAgain) {
                            toast("Permission Denied")
                        } else {
                            toast("Please enable permissions from your Device Settings")
                        }
                    }
                }
                if (allAllowed) toast("Permissions Granted")
            }
        }
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
        val apiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = apiAvailability.isGooglePlayServicesAvailable(this)
        if (resultCode != ConnectionResult.SUCCESS) {
            // This dialog will help the user update to the latest GooglePlayServices
            val dialog = apiAvailability.getErrorDialog(this, resultCode, GOOGLE_PLAY_SERVICES_RESOLUTION_REQUEST)
            dialog?.show()
        }
    }

    private fun gotoMainActivity() {
        try {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
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

    override fun onStarted() {
        binding.loading.show()
        hideKeyboard()
    }

    override fun onSuccess(userDTO: UserDTO) {
        binding.loading.hide()
        toast("You are Logged in as ${userDTO.userName}")
        gotoMainActivity()
    }

    override fun onWarn(message: String) {
        onFailure(message)
    }

    override fun onFailure(message: String) {
        binding.loading.hide()
        hideKeyboard()
        binding.regContainer.snackbar(message)
    }

    override fun onSignOut(userDTO: UserDTO) {
        // Nothing to do here
    }
}
