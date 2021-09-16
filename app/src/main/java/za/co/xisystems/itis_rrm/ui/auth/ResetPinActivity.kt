/*
 * Updated by Shaun McDonald on 2021/02/08
 * Last modified on 2021/02/07 6:42 PM
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 */

package za.co.xisystems.itis_rrm.ui.auth

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import kotlinx.android.synthetic.main.activity_register.*
import org.kodein.di.DIAware
import org.kodein.di.android.closestDI
import org.kodein.di.instance
import www.sanju.motiontoast.MotionToast
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.R.font
import za.co.xisystems.itis_rrm.data._commons.views.ToastUtils
import za.co.xisystems.itis_rrm.data.localDB.entities.UserDTO
import za.co.xisystems.itis_rrm.databinding.ActivityResetPinBinding
import za.co.xisystems.itis_rrm.extensions.observeOnce
import za.co.xisystems.itis_rrm.ui.auth.model.AuthViewModel
import za.co.xisystems.itis_rrm.ui.auth.model.AuthViewModelFactory
import za.co.xisystems.itis_rrm.utils.*

class ResetPinActivity : AppCompatActivity(), AuthListener, DIAware {
    companion object {
        val TAG: String = ResetPinActivity::class.java.simpleName
        private const val PERMISSION_REQUEST = 10
    }

    override val di by closestDI()
    private val factory: AuthViewModelFactory by instance()
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
            toast("Permissions are already provided.")
        } else {
            requestPermissions(permissions, PERMISSION_REQUEST)
        }

        val binding: ActivityResetPinBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_reset_pin)
        viewModel = ViewModelProvider(this, factory).get(AuthViewModel::class.java)
        binding.viewmodel = viewModel
        viewModel.setupAuthListener(this)

        Coroutines.main {
            val loggedInUser = viewModel.user.await()
            loggedInUser.observe(this, { user ->
                // Register the user
                if (user != null) {
                    scanForPinUpdate()
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

    private fun scanForPinUpdate() {
        viewModel.validPin.observeOnce(this) {
            it?.let {
                when (it) {
                    true -> {
                        MotionToast.createColorToast(
                            context = this@ResetPinActivity,
                            message = "PIN updated successfully",
                            style = MotionToast.TOAST_SUCCESS,
                            position = MotionToast.GRAVITY_BOTTOM,
                            duration = MotionToast.LONG_DURATION,
                            font = ResourcesCompat.getFont(this@ResetPinActivity, font.helvetica_regular)
                        )
                        viewModel.validPin.value = false
                        getToLogin()
                    }
                    else -> {
                        getToLogin()
                    }
                }
            }
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
                    val requestAgain = shouldShowRequestPermissionRationale(permissions[i])
                    if (requestAgain) {
                        toast("Permission Denied")
                    } else {
                        toast("Please enable permissions from your Device Settings")
                    }
                }
            }
            if (allAllowed) {
                toast("Permissions Granted")
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
            val dialog =
                apiAvailability.getErrorDialog(
                    this,
                    resultCode,
                    RegisterPinActivity.GOOGLE_PLAY_SERVICES_RESOLUTION_REQUEST
                )
            dialog?.show()
        }
    }

    override fun onStarted() {
        loading.show()
        hideKeyboard()
    }

    override fun onSuccess(userDTO: UserDTO) {
        loading.hide()
        hideKeyboard()
        toast("You are logged in as ${userDTO.userName}")
    }

    override fun onWarn(message: String) {
        onFailure(message)
    }

    override fun onFailure(message: String) {
        loading.hide()
        hideKeyboard()
        MotionToast.createColorToast(
            this,
            message = message,
            style = MotionToast.TOAST_ERROR,
            position = MotionToast.GRAVITY_BOTTOM,
            duration = MotionToast.LONG_DURATION,
            font = ResourcesCompat.getFont(this, font.helvetica_regular)
        )
    }

    override fun onSignOut(userDTO: UserDTO) {
        Coroutines.io {
            viewModel.expirePin()
        }
    }
}
