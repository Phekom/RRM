package za.co.xisystems.itis_rrm.utils.image_capture

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.app.ActivityCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.ajalt.timberkt.Timber
import com.google.android.material.snackbar.Snackbar
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import org.kodein.di.instance
import www.sanju.motiontoast.MotionToastStyle
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.custom.notifications.ToastStyle
import za.co.xisystems.itis_rrm.databinding.ImagepickerActivityImagepickerBinding
import za.co.xisystems.itis_rrm.ui.base.BaseActivity
import za.co.xisystems.itis_rrm.ui.extensions.extensionToast
import za.co.xisystems.itis_rrm.utils.Coroutines
import za.co.xisystems.itis_rrm.utils.GlideApp
import za.co.xisystems.itis_rrm.utils.PhotoUtil
import za.co.xisystems.itis_rrm.utils.image_capture.camera.CameraModule
import za.co.xisystems.itis_rrm.utils.image_capture.camera.OnImageReadyListener
import za.co.xisystems.itis_rrm.utils.image_capture.helper.ImageConstants
import za.co.xisystems.itis_rrm.utils.image_capture.helper.*
import za.co.xisystems.itis_rrm.utils.image_capture.listener.OnImageSelectListener
import za.co.xisystems.itis_rrm.utils.image_capture.model.Image
import za.co.xisystems.itis_rrm.utils.image_capture.model.ImagePickerConfig
import za.co.xisystems.itis_rrm.utils.image_capture.photoExifData.*
import za.co.xisystems.itis_rrm.utils.image_capture.photoExifData.Constants
import za.co.xisystems.itis_rrm.utils.image_capture.photoExifData.extension.convertDecimalToDegrees
import za.co.xisystems.itis_rrm.utils.image_capture.photoExifData.extension.getLatitudeRef
import za.co.xisystems.itis_rrm.utils.image_capture.photoExifData.extension.getLongitudeRef
import za.co.xisystems.itis_rrm.utils.image_capture.photoExifData.extension.getTags
import java.io.IOException


/**
 * Created by Francis Mahlava on 2021/11/23.
 */

class ImagePickerActivity : BaseActivity(), OnImageSelectListener {

    private lateinit var binding: ImagepickerActivityImagepickerBinding
    private lateinit var viewModel: ImagePickerViewModel
    private val captureFactory: ImagePickerViewModelFactory by instance()
    internal val photoUtil: PhotoUtil by instance()
    private val cameraModule = CameraModule()
    val groupAdapter = GroupAdapter<GroupieViewHolder>()
    private lateinit var config: ImagePickerConfig
    var latitude: Double? = null
    var longitude: Double? = null
    var noneTagLatitude: Double? = null
    var noneTagLongitude: Double? = null
    private lateinit var exifTagsContainerList: List<ExifTagsContainer>
    lateinit var exifInterface: ExifInterface

    private val backClickListener = View.OnClickListener { onBackPressed() }
    private val cameraClickListener = View.OnClickListener { captureImageWithPermission() }
    private val doneClickListener = View.OnClickListener { onDone() }

    companion object {
        val TAG: String = ImagePickerActivity::class.java.simpleName
        private const val PERMISSION_REQUEST = 42
    }

    private var permissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_NETWORK_STATE,
        Manifest.permission.ACCESS_WIFI_STATE,
    )

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
        if (!startPermissionRequest(permissions)) {
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST)
        }
        if (intent == null) {
            finish()
            return
        }

        config = intent.getParcelableExtra(ImageConstants.EXTRA_CONFIG)!!
        config.initDefaultValues(this@ImagePickerActivity)
//        promptUserImagesWillGo()
        // Setup status bar theme
        window.statusBarColor = Color.parseColor(config.statusBarColor)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars =
            config.isLightStatusBar

        binding = ImagepickerActivityImagepickerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this, captureFactory).get(
            ImagePickerViewModel::class.java
        )
        viewModel.setConfig(config)
        viewModel.selectedImages.observe(this) {
            binding.toolbar.showDoneButton(config.isAlwaysShowDoneButton || it.isNotEmpty())
        }

        setupViews()
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
                        ImageConstants.RC_READ_EXTERNAL_STORAGE_PERMISSION
                    )
                }

                override fun onPermissionPreviouslyDenied() {
                    PermissionHelper.requestAllPermissions(
                        this@ImagePickerActivity,
                        permissions,
                        ImageConstants.RC_READ_EXTERNAL_STORAGE_PERMISSION
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
            ImageConstants.RC_READ_EXTERNAL_STORAGE_PERMISSION -> {
                if (PermissionHelper.hasGranted(grantResults)) {
                    fetchData()
                } else {
                    finish()
                }
            }
            ImageConstants.RC_WRITE_EXTERNAL_STORAGE_PERMISSION -> {
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

//        exifDataAlertdialog(selectedImages, groupAdapter)


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
                        this@ImagePickerActivity,
                        permissions,
                        ImageConstants.RC_WRITE_EXTERNAL_STORAGE_PERMISSION
                    )
                }

                override fun onPermissionPreviouslyDenied() {
                    PermissionHelper.requestAllPermissions(
                        this@ImagePickerActivity,
                        permissions,
                        ImageConstants.RC_WRITE_EXTERNAL_STORAGE_PERMISSION
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
        data.putParcelableArrayListExtra(ImageConstants.EXTRA_IMAGES, images)
        setResult(Activity.RESULT_OK, data)
        finish()
    }

    override fun startLongRunningTask() {
        Timber.i(null) { "starting task..." }
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
        Timber.i(null) { "stopping task..." }
        // Re-enable UI touches
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onSelectedImagesChanged(selectedImages: ArrayList<Image>) {
        viewModel.selectedImages.value = selectedImages
    }

    override fun onSingleModeImageSelected(image: Image) {
//        exifDataAlertdialog(image, groupAdapter)
        finishPickImages(ImageHelper.singleListFromImage(image))
    }


//    private fun exifDataAlertdialog(
//        selectedImages: Image,
//        groupAdapter: GroupAdapter<GroupieViewHolder>
//    ) {
//        val textEntryView: View = layoutInflater.inflate(R.layout.exifdata_alert_dialog, null)
//        val selectedImage = textEntryView.findViewById<View>(R.id.image_photo) as ImageView
//        val imageData = textEntryView.findViewById<View>(R.id.imageDatarecyclerview) as RecyclerView
//
//        val alert: AlertDialog =
//            AlertDialog.Builder(this@ImagePickerActivity)
//                .setView(textEntryView)
//                .setCancelable(true)
//                .setIcon(R.drawable.ic_check)
//                .setTitle("Selected Image Data")
////                .setMessage("Make Sure You have Captured all Required Field")
//                .setPositiveButton("Continue", null)
////                .setNegativeButton("Save and Submit Now", null)
//                .create()
//
//
//        alert.setOnShowListener { dialog ->
//            val justSave = alert.getButton(AlertDialog.BUTTON_POSITIVE)
////            val saveAndSubmit = alert.getButton(AlertDialog.BUTTON_NEGATIVE)
//            val photo = selectedImages.uri
//            val imagepath = viewModel.photoUtil.getPath(selectedImages.uri)
//            computeTags(imagepath!!)
//
//            GlideApp.with(this)
//                .load(photo)
//                .centerCrop()
//                .into(selectedImage)
//
//            initRecyclerViewImageExif(
//                exifTagsContainerList.toActivityItems(imagepath, imageData),
//                imageData,
//                groupAdapter
//            )
//
//
//
//            justSave.setOnClickListener {
//                Coroutines.main {
//                    if (latitude == null && longitude == null) {
//
//                        if(noneTagLatitude == null && noneTagLongitude == null){
//                            extensionToast(
//                                title = "GPS Location Needed",
//                                message = "Please Manually Add GPS Tags",
//                                style = ToastStyle.ERROR
//                            )
//                            exifDataAlertdialog(selectedImages, groupAdapter)
//                        }else{
//                            selectedImages.latitude = noneTagLatitude!!
//                            selectedImages.longitude = noneTagLongitude!!
//                            finishPickImages(ImageHelper.singleListFromImage(selectedImages))
//                        }
//
//                    }else{
//                        selectedImages.latitude = latitude!!
//                        selectedImages.longitude = longitude!!
//                        finishPickImages(ImageHelper.singleListFromImage(selectedImages))
//                    }
//
//                    dialog.dismiss()
//                }
//            }
//
//        }
//        alert.show()
//    }

    private fun transformList(map: MutableMap<String, String>): List<ExifTagsContainer> {
        val locationsList = arrayListOf<ExifField>()
        val gpsList = arrayListOf<ExifField>()
        val datesList = arrayListOf<ExifField>()
        val cameraPropertiesList = arrayListOf<ExifField>()
//        val dimensionsList = arrayListOf<ExifField>()
//        val othersList = arrayListOf<ExifField>()
        map.forEach {
            when (it.key) {
                Constants.EXIF_LATITUDE, Constants.EXIF_LONGITUDE -> {
                    locationsList.add(ExifField(it.key, it.value))
                }

                ExifInterface.TAG_DATETIME, ExifInterface.TAG_DATETIME_DIGITIZED -> {
                    datesList.add(ExifField(it.key, it.value))
                }

                ExifInterface.TAG_MAKE, ExifInterface.TAG_MODEL -> {
                    cameraPropertiesList.add(ExifField(it.key, it.value))
                }

//               ExifInterface.TAG_IMAGE_LENGTH, ExifInterface.TAG_IMAGE_WIDTH -> {
//                    dimensionsList.add(ExifField(it.key, it.value))
//                }

                else -> {
                    if (it.key.contains("GPS")) gpsList.add(ExifField(it.key, it.value))
                    //else othersList.add(ExifField(it.key, it.value))
                }
            }
        }
        locationsList.addAll(gpsList)
        return arrayListOf(
            ExifTagsContainer(locationsList, Type.GPS),
            ExifTagsContainer(datesList, Type.DATE),
            ExifTagsContainer(cameraPropertiesList, Type.CAMERA_PROPERTIES)
        )
//            ExifTagsContainer(dimensionsList, Type.DIMENSION),
//            ExifTagsContainer(othersList, Type.OTHER))
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initRecyclerViewImageExif(
        items: List<DamageImagePickerItem>,
        imageData: RecyclerView,
        groupAdapter: GroupAdapter<GroupieViewHolder>
    ) {
        groupAdapter.apply {
            clear()
            addAll(items)
            notifyDataSetChanged()
        }
        imageData.apply {
            layoutManager = LinearLayoutManager(this.context)
            adapter = groupAdapter
        }
    }

    private fun List<ExifTagsContainer>.toActivityItems(
        imagepath: String,
        imageData: RecyclerView
    ): List<DamageImagePickerItem> {
        return this.map { imageItems ->
            DamageImagePickerItem(imageItems, this@ImagePickerActivity, imagepath, imageData)
        }
    }

    fun computeTags(
        imagepath: String
    ) {
        exifInterface = ExifInterface(imagepath)
        val map = exifInterface.getTags()
        exifTagsContainerList = transformList(map)

        latitude = map[Constants.EXIF_LATITUDE]?.toDouble()
        longitude = map[Constants.EXIF_LONGITUDE]?.toDouble()
    }


    fun changeExifDataList(
        exifTagsContainerList: List<ExifTagsContainer>,
        groupAdapter: GroupAdapter<GroupieViewHolder>,
        imageData: RecyclerView,
        imagepath: String
    ) {
        initRecyclerViewImageExif(
            exifTagsContainerList.toActivityItems(imagepath, imageData),
            imageData,
            groupAdapter
        )
    }


//    fun showAlertDialog(
//        imageItem: ExifTagsContainer,
//        activity: ImagePickerActivity,
//        imagepath: String,
//        imageData: RecyclerView
//    ) {
//        val alertDialogBuilder = AlertDialog.Builder(activity)
//
//        val optionsList = mutableListOf<String>()
//        //Add menu options to the alert dialog, the first one is in every item.
//        if (imageItem.type == Type.GPS) {
//            optionsList.add(activity.getString(R.string.alert_item_open_map))
//            optionsList.add(activity.getString(R.string.alert_item_edit_add))
////            optionsList.add(activity.getString(R.string.alert_item_remove_gps_tags))
//        }
////        else if (imageItem.type == Type.DATE) {
////            optionsList.add(activity.getString(R.string.alert_item_edit))
////        }
//        alertDialogBuilder.setTitle(activity.getString(R.string.alert_select_an_action))
//        alertDialogBuilder.setItems(optionsList.toTypedArray()) { _, which ->
//            if (imageItem.type == Type.GPS)
//                activity.openDialogMap(imageItem, imagepath, imageData)
//
////            if (which == 0) {
////                activity.copyDataToClipboard(imageItem)
////            } else if (which == 1) {
////                if (imageItem.type == Type.GPS)
////                    activity.openDialogMap(imageItem)
//////                else if (imageItem.type == Type.DATE)
//////                    activity.editDate(imageItem)
////            }
//
//        }
//        val dialog = alertDialogBuilder.create()
//        dialog.show()
//
//    }

    private fun onCompleteLocationChanged() {
//        Snackbar.make(
//            this@ImagePickerActivity,
//            binding.root, getString(R.string.location_saved),
//            Snackbar.LENGTH_INDEFINITE
//        ).setAction(getString(R.string.ok), View.OnClickListener
//        // Handle the Retry Button Click
//        {}).show()
    }

    fun openDialogMap(imageItem: ExifTagsContainer, imagepath: String, imageData: RecyclerView) {
        val latitude =
            imageItem.list.find { it.tag == Constants.EXIF_LATITUDE }?.attribute?.toDouble()
        val longitude =
            imageItem.list.find { it.tag == Constants.EXIF_LONGITUDE }?.attribute?.toDouble()
       // openLocationEdit(latitude, longitude, imagepath, imageData)
    }

    fun changeExifLocation(imagepath: String, imageData: RecyclerView, location: Location) {
        try {
            exifInterface.apply {
                setAttribute(
                    ExifInterface.TAG_GPS_LATITUDE_REF,
                    exifInterface.getLatitudeRef(location.latitude)
                )
                setAttribute(
                    ExifInterface.TAG_GPS_LATITUDE,
                    exifInterface.convertDecimalToDegrees(location.latitude)
                )
                setAttribute(
                    ExifInterface.TAG_GPS_LONGITUDE_REF,
                    exifInterface.getLongitudeRef(location.longitude)
                )
                setAttribute(
                    ExifInterface.TAG_GPS_LONGITUDE,
                    exifInterface.convertDecimalToDegrees(location.longitude)
                )
            }
            exifInterface.saveAttributes()
            computeTags(imagepath)
            changeExifDataList(exifTagsContainerList, groupAdapter, imageData, imagepath)
//                    getAddressByTriggerRequest()
            onCompleteLocationChanged()
        } catch (e: IOException) {
            noneTagLatitude = location.latitude.toDouble()
            noneTagLongitude = location.longitude.toDouble()
            extensionToast(
                title = "Location Edit Failed",
                message = "Failed to Save Location Tag",
                style = ToastStyle.ERROR
            )
        }
    }

//    private fun openLocationEdit(
//        latitude: Double?,
//        longitude: Double?,
//        imagepath: String,
//        imageData: RecyclerView
//    ) {
//        val textEntryView: View = layoutInflater.inflate(R.layout.location_edit_alert_dialog, null)
//        val latitudeText = textEntryView.findViewById<View>(R.id.latiText) as AppCompatEditText
//        val longitudeText = textEntryView.findViewById<View>(R.id.logiText) as AppCompatEditText
//
//        val alert: AlertDialog =
//            AlertDialog.Builder(this@ImagePickerActivity)
//                .setView(textEntryView)
//                .setCancelable(true)
//                .setIcon(R.drawable.ic_pin_drop)
//                .setTitle("Add Location To Image Data")
//                .setMessage("Enter Coordinates where this image was Captured")
//                .setPositiveButton("Save", null)
//                .setNegativeButton("Cancel", null)
//                .create()
//
//
//        alert.setOnShowListener { dialog ->
//            val justSave = alert.getButton(AlertDialog.BUTTON_POSITIVE)
//            val cancel = alert.getButton(AlertDialog.BUTTON_NEGATIVE)
//            when {
//                latitude == null -> {
//                    latitudeText.setText("0.0")
//                }
//                true -> {
//                    latitudeText.setText(latitude.toString())
//                }
//
//            }
//            when {
//                longitude == null -> {
//                    longitudeText.setText("0.0")
//                }
//                true -> {
//                    longitudeText.setText(longitude.toString())
//                }
//            }
//
//            justSave.setOnClickListener {
//                if (latitudeText.text.isNullOrEmpty()) {
//                    latitudeText.error = getString(R.string.latitude_required)
//                } else if (longitudeText.text.isNullOrEmpty()) {
//                    longitudeText.error = getString(R.string.longitude_required)
//                } else {
////                    val lat ="-25.456123"
////                    val long = "28.123456"
////                    changeExifLocation(imagepath, imageData ,Location(lat.toDouble(), long.toDouble()))
//
//                    changeExifLocation(
//                        imagepath,
//                        imageData,
//                        Location(
//                            latitudeText.text.toString().toDouble(),
//                            longitudeText.text.toString().toDouble()
//                        )
//                    )
//                }
//                //finishPickImages(selectedImages ?: arrayListOf())
//                dialog.dismiss()
//
//            }
//            cancel.setOnClickListener {
//                dialog.dismiss()
//            }
//        }
//        alert.show()
//
//    }


}