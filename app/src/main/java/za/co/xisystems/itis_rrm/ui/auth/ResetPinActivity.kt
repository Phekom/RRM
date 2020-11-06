package za.co.xisystems.itis_rrm.ui.auth

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GooglePlayServicesUtil
import kotlinx.android.synthetic.main.activity_register.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance
import www.sanju.motiontoast.MotionToast
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.data._commons.views.ToastUtils
import za.co.xisystems.itis_rrm.data.localDB.entities.UserDTO
import za.co.xisystems.itis_rrm.databinding.ActivityResetPinBinding
import za.co.xisystems.itis_rrm.extensions.observeOnce
import za.co.xisystems.itis_rrm.utils.Coroutines
import za.co.xisystems.itis_rrm.utils.hide
import za.co.xisystems.itis_rrm.utils.hideKeyboard
import za.co.xisystems.itis_rrm.utils.show
import za.co.xisystems.itis_rrm.utils.toast

class ResetPinActivity : AppCompatActivity(), AuthListener, KodeinAware, Runnable {
    companion object {
        val TAG: String = ResetPinActivity::class.java.simpleName
        private const val PERMISSION_REQUEST = 10
    }

    override val kodein by kodein()
    private val factory: AuthViewModelFactory by instance<AuthViewModelFactory>()
    private lateinit var viewModel: AuthViewModel
    private lateinit var appContext: Context
    private var permissions = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.CAMERA,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appContext = this

        if (startPermissionRequest(permissions)) {
            toast("Permissions Are already provided ")
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(permissions, PERMISSION_REQUEST)
            }
        }

        val binding: ActivityResetPinBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_reset_pin)
        viewModel = ViewModelProvider(this, factory).get(AuthViewModel::class.java)
        binding.viewmodel = viewModel
        viewModel.authListener = this

        Coroutines.main {
            val loggedInUser = viewModel.user.await()
            loggedInUser.observe(this, { user ->
                // Register the user
                if (user != null) {
                    Coroutines.main {

                        if (viewModel.enterOldPin != viewModel.confirmNewPin) {
                            getToLogin()
                        }
                    }

                    viewModel.newPinRegistered.observeOnce(this, {
                        it?.let {
                            when (it) {
                                true -> {
                                    MotionToast.createColorToast(
                                        this@ResetPinActivity,
                                        "PIN updated successfully",
                                        MotionToast.TOAST_SUCCESS,
                                        MotionToast.GRAVITY_BOTTOM,
                                        MotionToast.LONG_DURATION,
                                        ResourcesCompat.getFont(applicationContext, R.font.helvetica_regular)
                                    )
                                    viewModel.newPinRegistered.value = false
                                    getToLogin()
                                }
                                else -> {
                                    getToLogin()
                                }
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
            })
        }
    }

    private fun getToLogin() {
        Intent(this, LoginActivity::class.java).also { home ->
            home.flags =
                Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(home)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST) {
            var allAllowed = true
            for (i in permissions.indices) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    allAllowed = false
                    val requestAgain = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        shouldShowRequestPermissionRationale(permissions[i])
                    } else {
                        false
                    }
                    if (requestAgain) {
                        toast("Permission Denied")
                    } else {
                        toast("Please enable permissions from your Device Settings")
                    }
                }
            }
            if (allAllowed)
                toast("Permissions Granted")
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
        val resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this)
        if (resultCode != ConnectionResult.SUCCESS) { // This dialog will help the user update to the latest GooglePlayServices
            val dialog =
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0)
            dialog?.show()
        }
    }

    override fun onStarted() {
        loading.show()
        hideKeyboard()
    }

    override fun onSuccess(userDTO: UserDTO) {
        loading.hide()
        toast("You are logged in as ${userDTO.userName}")
    }

    override fun onFailure(message: String) {
        loading.hide()
        hideKeyboard()
        MotionToast.createColorToast(
            this,
            message,
            MotionToast.TOAST_WARNING,
            MotionToast.GRAVITY_BOTTOM,
            MotionToast.LONG_DURATION,
            ResourcesCompat.getFont(applicationContext, R.font.helvetica_regular)
        )
        // reg_container.snackbar(message)
    }

    override fun onSignOut(userDTO: UserDTO) {
    }

    override fun run() {
    }
}
