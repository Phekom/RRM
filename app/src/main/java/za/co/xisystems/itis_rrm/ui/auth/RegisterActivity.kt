package za.co.xisystems.itis_rrm.ui.auth

/**
 * Updated by Shaun McDonald - 2020/04/15
 */
import am.appwise.components.ni.NoInternetDialog
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import com.github.ajalt.timberkt.Timber
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import org.kodein.di.DIAware
import org.kodein.di.instance
import za.co.xisystems.itis_rrm.MainApp
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.data._commons.views.ToastUtils
import za.co.xisystems.itis_rrm.data.localDB.entities.UserDTO
import za.co.xisystems.itis_rrm.data.preferences.MyAppPrefsManager
import za.co.xisystems.itis_rrm.databinding.ActivityRegisterBinding
import za.co.xisystems.itis_rrm.extensions.isConnected
import za.co.xisystems.itis_rrm.ui.auth.model.AuthViewModel
import za.co.xisystems.itis_rrm.ui.auth.model.AuthViewModelFactory
import za.co.xisystems.itis_rrm.ui.base.BaseActivity
import za.co.xisystems.itis_rrm.ui.scopes.UiLifecycleScope
import za.co.xisystems.itis_rrm.utils.*


class RegisterActivity : BaseActivity(), AuthListener, DIAware {
    override val di by lazy { (applicationContext as MainApp).di }
    private val factory: AuthViewModelFactory by instance()
    private lateinit var viewModel: AuthViewModel
    private var uiScope = UiLifecycleScope()
    private var permissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_NETWORK_STATE,
        Manifest.permission.ACCESS_WIFI_STATE,
    )
    var myAppPrefsManager: MyAppPrefsManager? = null
    private var pinAlert: AlertDialog? = null
    private var builder: AlertDialog.Builder? = null
    override var gpsEnabled = false
    private var networkEnabled: Boolean = false

    companion object {
        val TAG: String = RegisterActivity::class.java.simpleName
        private const val PERMISSION_REQUEST = 10
    }

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]
        viewModel.setupAuthListener(this)
        val noInternetDialog: NoInternetDialog = NoInternetDialog.Builder(this@RegisterActivity).build()
        myAppPrefsManager = MyAppPrefsManager(this)
        if (startPermissionRequest(permissions)) {
            getInternetConnectionResult(noInternetDialog)
        } else {
            requestPermissions(permissions, PERMISSION_REQUEST)
        }


        binding.serverTextView.setOnClickListener {
            ToastUtils().toastServerAddress(this.applicationContext)
        }

        binding.buildFlavorTextView.setOnClickListener {
            ToastUtils().toastVersion(this.applicationContext)
        }


    }

    override fun startLongRunningTask() {
        Timber.e{"starting task..."}
        binding.loading.visibility = View.VISIBLE
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
        Timber.i { "stopping task..." }
        binding.loading.visibility = View.GONE
        // Re-enable UI touches
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun registerThisUser(view: View, username: TextInputEditText, password: TextInputEditText,
                                 noInternetDialog: NoInternetDialog) {
        uiScope.launch(uiScope.coroutineContext) {
            viewModel.onRegButtonClick(view, username, password, myAppPrefsManager)
            when (this@RegisterActivity.isConnected) {
                true -> {
                    networkEnabled = true
                    if (!networkEnabled) {
                        noInternetDialog.showDialog()
                    } else {
                        if (noInternetDialog.isShowing) {
                            ToastUtils().toastShort(this@RegisterActivity, "Check Internet Connection")
                        } else {
                            val loggedInUser = viewModel.user.await()
                            loggedInUser.observe(this@RegisterActivity) { user ->
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
                            }
                        }
                    }

                }
                else -> {
                    onFailure("This step requires an active internet connection.")
                }
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
        builder = AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog)
        builder?.setTitle(R.string.set_pin)
        builder?.setIcon(R.drawable.ic_baseline_lock_24px)
        builder?.setMessage(R.string.set_pin_msg)
        builder?.setCancelable(false)
        // Yes button
        builder?.setPositiveButton(R.string.ok) { _, _ ->
            if (ServiceUtil.isNetworkAvailable(this.applicationContext)) {
                Intent(this, RegisterPinActivity::class.java).also { pin ->
                    pin.flags =
                        Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    pinAlert?.dismiss()
                    startActivity(pin)
                }
            } else {
                toast(R.string.no_connection_detected.toString())
            }
        }

        pinAlert = builder?.create()
        pinAlert?.show()
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

    override fun onPause() {
        super.onPause()
        pinAlert?.dismiss()
    }

    override fun onStart() {
        super.onStart()
        googlePlayServicesCheck(this)
    }

    override fun onResume() {
        super.onResume()
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

    }


    private fun getInternetConnectionResult(noInternetDialog: NoInternetDialog) {

        binding.registerbutton.setOnClickListener {
            val username = binding.registerusernameeditText
            val password = binding.registerpasswordeditText
            registerThisUser(it, username, password, noInternetDialog)
            // ToastUtils().toastServerAddress(this.applicationContext)
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
