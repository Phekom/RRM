package za.co.xisystems.itis_rrm.utils.image_capture

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModelProvider
import org.kodein.di.instance
import timber.log.Timber
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.databinding.ImagepickerActivityImagepickerBinding
import za.co.xisystems.itis_rrm.ui.base.BaseActivity
import za.co.xisystems.itis_rrm.utils.Coroutines
import za.co.xisystems.itis_rrm.utils.PhotoUtil
import za.co.xisystems.itis_rrm.utils.image_capture.camera.CameraModule
import za.co.xisystems.itis_rrm.utils.image_capture.camera.OnImageReadyListener
import za.co.xisystems.itis_rrm.utils.image_capture.helper.*
import za.co.xisystems.itis_rrm.utils.image_capture.listener.OnImageSelectListener
import za.co.xisystems.itis_rrm.utils.image_capture.model.Image
import za.co.xisystems.itis_rrm.utils.image_capture.model.ImagePickerConfig

/**
 * Created by Francis Mahlava on 2021/11/23.
 */

class ImagePickerActivity : BaseActivity(), OnImageSelectListener {

    private lateinit var binding: ImagepickerActivityImagepickerBinding
      private lateinit var viewModel: ImagePickerViewModel
    private val captureFactory: ImagePickerViewModelFactory by instance()
    internal val photoUtil: PhotoUtil by instance()
    private val cameraModule = CameraModule()

    private lateinit var config: ImagePickerConfig


    private val backClickListener = View.OnClickListener { onBackPressed() }
    private val cameraClickListener = View.OnClickListener { captureImageWithPermission() }
    private val doneClickListener = View.OnClickListener { onDone() }

    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                cameraModule.saveImage(
                    this@ImagePickerActivity,
                    config,
                    object : OnImageReadyListener {
                        override fun onImageReady(images: ArrayList<Image>) {
                            fetchDataWithPermission()
                        }
                        override fun onImageNotReady() {
                            fetchDataWithPermission()
                        }
                    })
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent == null) {
            finish()
            return
        }

        config = intent.getParcelableExtra(Constants.EXTRA_CONFIG)!!
        config.initDefaultValues(this@ImagePickerActivity)
        promptUserImagesWillGo()
        // Setup status bar theme
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            window.statusBarColor = Color.parseColor(config.statusBarColor)
            WindowCompat.setDecorFitsSystemWindows(window, false)
            WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars =
                config.isLightStatusBar
        }

        binding = ImagepickerActivityImagepickerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this, captureFactory).get(
            ImagePickerViewModel::class.java
        )
        viewModel.setConfig(config)
        viewModel.selectedImages.observe(this, {
            binding.toolbar.showDoneButton(config.isAlwaysShowDoneButton || it.isNotEmpty())
        })

        setupViews()
    }


    override fun onResume() {
        super.onResume()
        fetchDataWithPermission()
    }


    private fun promptUserImagesWillGo() = Coroutines.ui {
        val warningDialog: AlertDialog.Builder =
            AlertDialog.Builder(this@ImagePickerActivity) // android.R.style.Theme_DeviceDefault_Dialog
                .setTitle(
                    "Warning!! Images Will Auto Delete"
                )
                .setMessage(getString(R.string.auto_delete))
                .setCancelable(true)
                .setIcon(R.drawable.ic_warning)
                .setPositiveButton(R.string.ok) { warningDialog, _ ->
                    warningDialog.dismiss()
                }

        warningDialog.show()
    }

    private fun setupViews() {
        binding.toolbar.apply {
            config(config)
            setOnBackClickListener(backClickListener)
            setOnCameraClickListener(cameraClickListener)
            setOnDoneClickListener(doneClickListener)
        }

        val initialFragment = ImageFragment.newInstance(config.imageGridCount)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, initialFragment)
            .commit()
    }


    private fun fetchDataWithPermission() {
        val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        PermissionHelper.checkPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            object : PermissionHelper.PermissionAskListener {
                override fun onNeedPermission() {
                    PermissionHelper.requestAllPermissions(
                        this@ImagePickerActivity,
                        permissions,
                        Constants.RC_READ_EXTERNAL_STORAGE_PERMISSION
                    )
                }

                override fun onPermissionPreviouslyDenied() {
                    PermissionHelper.requestAllPermissions(
                        this@ImagePickerActivity,
                        permissions,
                        Constants.RC_READ_EXTERNAL_STORAGE_PERMISSION
                    )
                }

                override fun onPermissionDisabled() {
                    binding.snackbar.show(
                        R.string.msg_no_external_storage_permission
                    ) {
                        PermissionHelper.openAppSettings(this@ImagePickerActivity)
                    }
                }

                override fun onPermissionGranted() {
                    fetchData()
                }
            })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            Constants.RC_READ_EXTERNAL_STORAGE_PERMISSION -> {
                if (PermissionHelper.hasGranted(grantResults)) {
                    fetchData()
                } else {
                    finish()
                }
            }
            Constants.RC_WRITE_EXTERNAL_STORAGE_PERMISSION -> {
                if (PermissionHelper.hasGranted(grantResults)) {
                    captureImage()
                }
            }
            else -> {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }

    private fun fetchData() {
        viewModel.fetchImages()
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    private fun onDone() {
        val selectedImages = viewModel.selectedImages.value
        finishPickImages(selectedImages ?: arrayListOf())
    }


    private fun captureImageWithPermission() {
        if (DeviceHelper.isMinSdk29) {
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
                        this@ImagePickerActivity,
                        permissions,
                        Constants.RC_WRITE_EXTERNAL_STORAGE_PERMISSION
                    )
                }

                override fun onPermissionPreviouslyDenied() {
                    PermissionHelper.requestAllPermissions(
                        this@ImagePickerActivity,
                        permissions,
                        Constants.RC_WRITE_EXTERNAL_STORAGE_PERMISSION
                    )
                }

                override fun onPermissionDisabled() {
                    binding.snackbar.show(
                        R.string.msg_no_external_storage_permission
                    ) {
                        PermissionHelper.openAppSettings(this@ImagePickerActivity)
                    }
                }

                override fun onPermissionGranted() {
                    captureImage()
                }
            })
    }


    fun captureImage() {
        if (!DeviceHelper.checkCameraAvailability(this)) {
            return
        }

        val intent = cameraModule.getCameraIntent(this@ImagePickerActivity, config)
        if (intent == null) {
            ToastHelper.show(this, getString(R.string.error_open_camera))
            return
        }

        resultLauncher.launch(intent)
    }

    private fun finishPickImages(images: ArrayList<Image>) {
        val data = Intent()
        data.putParcelableArrayListExtra(Constants.EXTRA_IMAGES, images)
        setResult(Activity.RESULT_OK, data)
        finish()
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

    override fun onSelectedImagesChanged(selectedImages: ArrayList<Image>) {
        viewModel.selectedImages.value = selectedImages
    }

    override fun onSingleModeImageSelected(image: Image) {
        finishPickImages(ImageHelper.singleListFromImage(image))
    }



}