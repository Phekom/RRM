package za.co.xisystems.itis_rrm.utils.image_capture.camera

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import org.kodein.di.instance
import timber.log.Timber
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.databinding.ImagepickerActivityCameraBinding
import za.co.xisystems.itis_rrm.ui.base.BaseActivity
import za.co.xisystems.itis_rrm.utils.PhotoUtil

import za.co.xisystems.itis_rrm.utils.image_capture.ImagePickerViewModel
import za.co.xisystems.itis_rrm.utils.image_capture.ImagePickerViewModelFactory
import za.co.xisystems.itis_rrm.utils.image_capture.helper.DeviceHelper
import za.co.xisystems.itis_rrm.utils.image_capture.helper.ImageConstants
import za.co.xisystems.itis_rrm.utils.image_capture.helper.PermissionHelper
import za.co.xisystems.itis_rrm.utils.image_capture.helper.PermissionHelper.hasGranted
import za.co.xisystems.itis_rrm.utils.image_capture.helper.PermissionHelper.openAppSettings
import za.co.xisystems.itis_rrm.utils.image_capture.helper.ToastHelper
import za.co.xisystems.itis_rrm.utils.image_capture.model.Image
import za.co.xisystems.itis_rrm.utils.image_capture.model.ImagePickerConfig
import java.util.*

/**
 * Created by Francis Mahlava on 2021/11/23.
 */
class CameraActivity : BaseActivity() {

    private lateinit var binding: ImagepickerActivityCameraBinding
    private lateinit var config: ImagePickerConfig

    private val cameraModule = CameraModule()
    private var alertDialog: AlertDialog? = null
    private var isOpeningCamera = false
    val photoUtil: PhotoUtil by instance()

    private lateinit var viewModel: ImagePickerViewModel
    private val captureFactory: ImagePickerViewModelFactory by instance()

    private val permissions =
        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)

    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                cameraModule.saveImage(
                    this@CameraActivity,
                    config,
                    object : OnImageReadyListener {
                        override fun onImageReady(images: ArrayList<Image>) {
                            finishCaptureImage(images)
                        }

                        override fun onImageNotReady() {
                            finishCaptureImage(arrayListOf())
                        }
                    })
            } else {
                finishCaptureImage(arrayListOf())
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent == null) {
            finish()
            return
        }
        viewModel = ViewModelProvider(this, captureFactory).get(
            ImagePickerViewModel::class.java
        )
        config = intent.getParcelableExtra(ImageConstants.EXTRA_CONFIG)!!
        config.initDefaultValues(this@CameraActivity)

        binding = ImagepickerActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }


    override fun startLongRunningTask() {
        Timber.i("starting task...")
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
        Timber.i("stopping task...")
        // Re-enable UI touches
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onResume() {
        super.onResume()
        if (!isOpeningCamera && (alertDialog == null || !alertDialog!!.isShowing)) {
            captureImageWithPermission()
        }
    }

    private fun captureImageWithPermission() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            captureImage()
            return
        }

        val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        PermissionHelper.checkPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            object : PermissionHelper.PermissionAskListener {
                override fun onNeedPermission() {
                    PermissionHelper.requestAllPermissions(
                        this@CameraActivity,
                        permissions,
                        ImageConstants.RC_WRITE_EXTERNAL_STORAGE_PERMISSION
                    )
                }

                override fun onPermissionPreviouslyDenied() {
                    PermissionHelper.requestAllPermissions(
                        this@CameraActivity,
                        permissions,
                        ImageConstants.RC_WRITE_EXTERNAL_STORAGE_PERMISSION
                    )
                }

                override fun onPermissionDisabled() {
                    showOpenSettingDialog()
                }

                override fun onPermissionGranted() {
                    captureImage()
                }
            })
    }

    private fun showOpenSettingDialog() {
        val builder = AlertDialog.Builder(
            ContextThemeWrapper(
                this@CameraActivity,
                R.style.Theme_AppCompat_Light_Dialog
            )
        )
        with(builder) {
            setMessage(R.string.msg_no_external_storage_permission)
            setNegativeButton(R.string.action_cancel) { _, _ ->
                finish()
            }
            setPositiveButton(R.string.action_ok) { _, _ ->
                openAppSettings(this@CameraActivity)
                finish()
            }
        }

        alertDialog = builder.create()
        alertDialog!!.show()
    }

    private fun captureImage() {
        if (!DeviceHelper.checkCameraAvailability(this)) {
            finish()
            return
        }

        val intent = cameraModule.getCameraIntent(this@CameraActivity, config)
        if (intent == null) {
            ToastHelper.show(this, getString(R.string.error_open_camera))
            return
        }

        resultLauncher.launch(intent)
        isOpeningCamera = true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            ImageConstants.RC_WRITE_EXTERNAL_STORAGE_PERMISSION -> {
                if (hasGranted(grantResults)) {
                    captureImage()
                } else {
                    finish()
                }
            }
            else -> {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
                finish()
            }
        }
    }

    private fun finishCaptureImage(images: ArrayList<Image>) {
        val data = Intent()
        data.putParcelableArrayListExtra(ImageConstants.EXTRA_IMAGES, images)
        setResult(Activity.RESULT_OK, data)
        finish()
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, 0)
    }
}