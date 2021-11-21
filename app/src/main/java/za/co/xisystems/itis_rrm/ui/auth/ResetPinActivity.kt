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
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import org.kodein.di.DIAware
import org.kodein.di.android.closestDI
import org.kodein.di.instance
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle
import za.co.xisystems.itis_rrm.R.font
import za.co.xisystems.itis_rrm.data._commons.views.ToastUtils
import za.co.xisystems.itis_rrm.data.localDB.entities.UserDTO
import za.co.xisystems.itis_rrm.databinding.ActivityResetPinBinding
import za.co.xisystems.itis_rrm.delegates.viewBinding
import za.co.xisystems.itis_rrm.ui.auth.model.AuthViewModel
import za.co.xisystems.itis_rrm.ui.auth.model.AuthViewModelFactory
import za.co.xisystems.itis_rrm.utils.Coroutines
import za.co.xisystems.itis_rrm.utils.hide
import za.co.xisystems.itis_rrm.utils.hideKeyboard
import za.co.xisystems.itis_rrm.utils.show
import za.co.xisystems.itis_rrm.utils.toast

class ResetPinActivity: AppCompatActivity(), AuthListener, DIAware {
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
    private val binding: ActivityResetPinBinding by viewBinding(ActivityResetPinBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        appContext = this

        if (startPermissionRequest(permissions)) {
            toast("Permissions are already provided.")
        } else {
            requestPermissions(permissions, PERMISSION_REQUEST)
        }

        viewModel = ViewModelProvider(this, factory).get(AuthViewModel::class.java)
        viewModel.setupAuthListener(this)

        Coroutines.main {
            val loggedInUser = viewModel.user.await()
            loggedInUser.observe(this, { user ->
                // Register the user
                if (user != null) {
                    binding.resetPinbutton.setOnClickListener { view ->
                        viewModel.onResetPinButtonClick(
                            view = view,
                            oldPin = binding.enterPinEditText.text?.toString(),
                            newPin = binding.enterNewPinEditText.text?.toString(),
                            confirmNewPin = binding.confirmPinEditText.text?.toString()
                        )
                    }
                    binding.serverTextView.setOnClickListener {
                        ToastUtils().toastServerAddress(appContext)
                    }

                    binding.buildFlavorTextView.setOnClickListener {
                        ToastUtils().toastVersion(appContext)
                    }
                    scanForPinUpdate()
                }
            })
        }
    }

    private fun scanForPinUpdate() {
        viewModel.validPin.observe(this) {
            it?.let {
                when (it) {
                    true -> {
                        MotionToast.createColorToast(
                            context = this@ResetPinActivity,
                            message = "PIN updated successfully",
                            style = MotionToastStyle.SUCCESS,
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
        binding.loading.show()
        hideKeyboard()
    }

    override fun onSuccess(userDTO: UserDTO) {
        binding.loading.hide()
        hideKeyboard()
        toast("You are logged in as ${userDTO.userName}")
    }

    override fun onWarn(message: String) {
        onFailure(message)
    }

    override fun onFailure(message: String) {
        binding.loading.hide()
        hideKeyboard()
        MotionToast.createColorToast(
            this,
            message = message,
            style = MotionToastStyle.ERROR,
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
