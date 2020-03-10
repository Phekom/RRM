//package za.co.xisystems.itis_rrm.ui.mainview.estmeasure.submit_measure
//
//import android.Manifest
//import android.app.Activity
//import android.content.Intent
//import android.content.pm.ActivityInfo
//import android.content.pm.PackageManager
//import android.graphics.Bitmap
//import android.location.Location
//import android.net.Uri
//import android.os.Bundle
//import android.provider.MediaStore
//import android.util.Log
//import android.view.View
//import android.widget.Button
//import androidx.appcompat.app.AlertDialog
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.app.ActivityCompat
//import androidx.core.content.ContextCompat
//import androidx.lifecycle.ViewModelProviders
//import icepick.State
//import kotlinx.android.synthetic.main.fragment_capture_item_measure_photo.*
//import org.kodein.di.KodeinAware
//import org.kodein.di.android.kodein
//import org.kodein.di.generic.instance
//import za.co.xisystems.itis_rrm.BuildConfig
//import za.co.xisystems.itis_rrm.MainActivity
//import za.co.xisystems.itis_rrm.R
//import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemMeasureDTO
//import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemMeasurePhotoDTO
//import za.co.xisystems.itis_rrm.ui.mainview.create.edit_estimates.AppExecutor
//import za.co.xisystems.itis_rrm.ui.mainview.create.edit_estimates.BitmapUtils
//import za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils.intents.AbstractIntent
//import za.co.xisystems.itis_rrm.ui.mainview.estmeasure.MeasureViewModel
//import za.co.xisystems.itis_rrm.ui.mainview.estmeasure.MeasureViewModelFactory
//import za.co.xisystems.itis_rrm.ui.mainview.estmeasure.submit_measure.ExpandableHeaderMeasureItem.Companion.JOB_IMEASURE
//import za.co.xisystems.itis_rrm.utils.*
//import java.util.*
//
//class CaptureItemMeasurePhotoActivity : AppCompatActivity() , KodeinAware {
//
//    override val kodein by kodein()
//    private lateinit var measureViewModel: MeasureViewModel
//    private val factory: MeasureViewModelFactory by instance()
//
//    lateinit var locationHelper: LocationHelper
//    private var currentLocation: Location? = null
//
//    private lateinit var jobItemMeasurePhotoArrayList: ArrayList<JobItemMeasurePhotoDTO>
//    private var mTempPhotoPath: String? = null
//    private lateinit var selectedJobItemMeasure: JobItemMeasureDTO
//    private var jobItemMeasure: JobItemMeasureDTO? = null
//    private lateinit var imageUri: Uri
//
//    @State
//    var filename_path = HashMap<String, String>()
//    @State
//    var location = HashMap<String, String>()
//
//
//    private val mAppExcutor: AppExecutor? = null
//
//    private var mResultsBitmap: Bitmap? = null
//
//    var viewPhotosOnly = false
//
//    private val captureAnotherPhotoButton: Button? = null
//    private lateinit var photoName: String
//    companion object {
//        val TAG: String = CaptureItemMeasurePhotoActivity::class.java.simpleName
//        const val JOB_ITEM_MEASURE_PHOTO_ARRAY_LIST = "JobItemMeasurePhotoArrayList"
//        const val PHOTO_RESULT = 9000
//        private const val REQUEST_IMAGE_CAPTURE = 1
//        private const val REQUEST_STORAGE_PERMISSION = 1
//        private val FILE_PROVIDER_AUTHORITY = BuildConfig.APPLICATION_ID + ".provider"
//
//        const val URI_LIST_DATA = "URI_LIST_DATA"
//        const val IMAGE_FULL_SCREEN_CURRENT_POS = "IMAGE_FULL_SCREEN_CURRENT_POS"
//
//        protected const val LOCATION_KEY = "location-key"
//        // region (Public Static Final Fields)
//        const val UPDATE_INTERVAL_IN_MILLISECONDS: Long = 10000
//        const val FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2
//
//
//    }
//
//
//    override fun onStart() {
//        super.onStart()
//        locationHelper.onStart()
//    }
//
//    override fun onPause() {
//        super.onPause()
//        locationHelper.onPause()
//    }
//
//    override fun onStop() {
//        super.onStop()
//        locationHelper.onStop()
//    }
//
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_capture_item_measure_photo)
//        (this).supportActionBar?.title = getString(R.string.captured_photos)
//
//        measureViewModel = this?.run {
//            ViewModelProviders.of(this, factory).get(MeasureViewModel::class.java)
//        } ?: throw Exception("Invalid Activity") as Throwable
//
//        locationHelper = LocationHelper(this)
//        locationHelper.onCreate()
//
//        Coroutines.main {
//            photoButtons.visibility = View.GONE
////            getIntent().getSerializableExtra(JOB_IMEASURE)
//
//            if (intent.hasExtra(ExpandableHeaderMeasureItem.JOB_IMEASURE)) {
//                selectedJobItemMeasure = intent.extras[JOB_IMEASURE] as JobItemMeasureDTO
//                takeMeasurePhoto()
//                toast(selectedJobItemMeasure.jimNo.toString())
//            }
//
//
////            measureViewModel.measurea1_Item1.observe(
////                this, Observer { selectedJobItemM ->
////
////                    selectedJobItemMeasure = selectedJobItemM
////                    takeMeasurePhoto()
////                    //     getPhotosForSelectedJobItemMeasure(selectedJobItemM)
////
////                })
//            jobItemMeasurePhotoArrayList = ArrayList<JobItemMeasurePhotoDTO>()
//
//        }
//
//        capture_another_photo_button.setOnClickListener {
//            launchCamera()
//        }
//
//        done_image_button.setOnClickListener { save ->
//            saveImgae()
//            setJobItemMeasureImage(
//                jobItemMeasurePhotoArrayList,
//                measureViewModel,
//                selectedJobItemMeasure.estimateId,
//                selectedJobItemMeasure
//            )
//
//            Intent(this, MainActivity::class.java).also { home ->
//                home.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
//                startActivity(home)
//            }
//
//
////            Navigation.findNavController(save).navigate(R.id.action_captureItemMeasurePhotoFragment_to_nav_estMeasure)
//        }
//
//
//
//
//
//        Save.visibility = View.GONE
////        Save.setOnClickListener { save ->
////            saveImgae()
//////            updateJobItemMeasures(jobItemMeasureArrayList,measureViewModel)
////            setJobItemMeasureImage(jobItemMeasurePhotoArrayList, measureViewModel)
////
////        }
//
//
//    }
//
//    private fun setJobItemMeasureImage(
//        jobItemMeasurePhotoList: ArrayList<JobItemMeasurePhotoDTO>,
//        measureViewModel: MeasureViewModel,
//        estimateId: String?,
//        selectedJobItemMeasure: JobItemMeasureDTO
//    ) {
//        Coroutines.main {
//            measureViewModel.setJobItemMeasureImages(jobItemMeasurePhotoList, estimateId, selectedJobItemMeasure)
//        }
//    }
//
//    private fun saveImgae(): JobItemMeasurePhotoDTO {
//        //  Location of picture
//        val currentLocation: Location = locationHelper?.getCurrentLocation()!!
//        if (currentLocation == null) toast("Error: Current location is null!")
//        //  Save Image to Internal Storage
//        val photoId = SqlLitUtils.generateUuid()
//        filename_path =
//            PhotoUtil.saveImageToInternalStorage(this, imageUri) as HashMap<String, String>
//
//        Log.e("location", " ${currentLocation.longitude} ")
//
//        val jobItemMeasurePhoto = JobItemMeasurePhotoDTO(
//            0,
//            null,
//            filename_path.get("filename"),
//            selectedJobItemMeasure.estimateId,
//            selectedJobItemMeasure.itemMeasureId,
//            DateUtil.DateToString(Date()),
//            photoId,   currentLocation.latitude,  currentLocation.longitude,
//            filename_path.get("path"), jobItemMeasure, 0,   0
//
//        )
//
//        jobItemMeasurePhotoArrayList!!.add(jobItemMeasurePhoto)
////        jobForJobItemEstimate.setJobItemMeasures(jobItemMeasurePhotoArrayList)
//        return jobItemMeasurePhoto!!
//    }
//
//    private fun takeMeasurePhoto() {
//
//        val logoutBuilder =
//            AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog)
//        logoutBuilder.setTitle(R.string.capture_measure_photo)
//        logoutBuilder.setIcon(R.drawable.ic_menu_camera)
//        logoutBuilder.setMessage(R.string.start_taking_photo)
//        logoutBuilder.setCancelable(false)
//        // Yes button
//        logoutBuilder.setPositiveButton(R.string.ok) { dialog, which ->
//            if (ContextCompat.checkSelfPermission(
//                    this,
//                    Manifest.permission.WRITE_EXTERNAL_STORAGE
//                ) !== PackageManager.PERMISSION_GRANTED
//            ) {
//                ActivityCompat.requestPermissions(
//                    this!!,
//                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
//                    CaptureItemMeasurePhotoActivity.REQUEST_STORAGE_PERMISSION
//                )
//            } else {
//                launchCamera()
//            }
//        }
//        val declineAlert = logoutBuilder.create()
//        declineAlert.show()
//
//    }
//
//
//    private fun launchCamera() {
//
//        Coroutines.main {
//            imageUri = PhotoUtil.getUri2(this)!!
//            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//            if (takePictureIntent.resolveActivity(this!!.packageManager) != null) {
//                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
//                takePictureIntent.putExtra(
//                    MediaStore.EXTRA_SCREEN_ORIENTATION,
//                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
//                )
//                startActivityForResult(takePictureIntent, AbstractIntent.REQUEST_TAKE_PHOTO)
//            }
//
//        }
//    }
//
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) { // Process the image and set it to the TextView
//            processAndSetImage()
//        } else { // Otherwise, delete the temporary image file
//            BitmapUtils.deleteImageFile(this.applicationContext, mTempPhotoPath)
//        }
//    }
//
//    private fun processAndSetImage() {
//        GlideApp.with(this)
//            .load(imageUri)
//            .into(m_imageView)
//        photoButtons.visibility = View.VISIBLE
//    }
//
//    private fun updateValuesFromBundle(savedInstanceState: Bundle?) {
//        Log.i(
//            CaptureItemMeasurePhotoActivity.TAG,
//            getString(R.string.updating_location_values_from_bundle)
//        )
//        if (savedInstanceState != null) {
////            @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
////            if (savedInstanceState.keySet().contains(CaptureItemMeasurePhotoActivity.LOCATION_KEY))
////                currentLocation = savedInstanceState.getParcelable<Location>(
////                    CaptureItemMeasurePhotoActivity.LOCATION_KEY
////                )
//        }
//    }
//
//
//
//
//
//
//
//
//
//
//
//
//
//    fun getCurrentLocation(): Location? {
//        return currentLocation
//    }
//
//    fun setCurrentLocation(currentLocation: Location?) {
//        this.currentLocation = currentLocation
//    }
//
//}
//
