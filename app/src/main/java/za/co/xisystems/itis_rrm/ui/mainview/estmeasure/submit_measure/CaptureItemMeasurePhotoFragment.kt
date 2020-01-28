//package za.co.xisystems.itis_rrm.ui.mainview.estmeasure.submit_measure
//
//import android.Manifest
//import android.app.Activity.RESULT_OK
//import android.content.Context
//import android.content.Intent
//import android.content.pm.ActivityInfo
//import android.content.pm.PackageManager
//import android.graphics.Bitmap
//import android.location.Location
//import android.net.Uri
//import android.os.Bundle
//import android.provider.MediaStore
//import android.util.Log
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.Button
//import androidx.appcompat.app.AlertDialog
//import androidx.core.app.ActivityCompat
//import androidx.core.content.ContextCompat
//import androidx.lifecycle.Observer
//import androidx.lifecycle.ViewModelProviders
//import com.google.android.gms.common.ConnectionResult
//import com.google.android.gms.common.api.GoogleApiClient
//import com.google.android.gms.location.LocationListener
//import com.google.android.gms.location.LocationRequest
//import com.google.android.gms.location.LocationServices
//import icepick.State
//import kotlinx.android.synthetic.main.fragment_capture_item_measure_photo.*
//import org.kodein.di.KodeinAware
//import org.kodein.di.android.x.kodein
//import org.kodein.di.generic.instance
//import za.co.xisystems.itis_rrm.BuildConfig
//import za.co.xisystems.itis_rrm.MainActivity
//import za.co.xisystems.itis_rrm.R
//import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemMeasureDTOTemp
//import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemMeasurePhotoDTOTemp
//import za.co.xisystems.itis_rrm.data.network.PermissionController
//import za.co.xisystems.itis_rrm.ui.mainview._fragments.BaseFragment
//import za.co.xisystems.itis_rrm.ui.mainview.create.edit_estimates.AppExecutor
//import za.co.xisystems.itis_rrm.ui.mainview.create.edit_estimates.BitmapUtils
//import za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils.LocationHelper
//import za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils.intents.AbstractIntent
//import za.co.xisystems.itis_rrm.ui.mainview.estmeasure.MeasureViewModel
//import za.co.xisystems.itis_rrm.ui.mainview.estmeasure.MeasureViewModelFactory
//import za.co.xisystems.itis_rrm.utils.*
//import java.util.*
//
//class CaptureItemMeasurePhotoFragment : BaseFragment(), KodeinAware,
//    GoogleApiClient.ConnectionCallbacks,
//    GoogleApiClient.OnConnectionFailedListener, LocationListener {
//    override val kodein by kodein()
//    private lateinit var measureViewModel: MeasureViewModel
//    private val factory: MeasureViewModelFactory by instance()
//
//    companion object {
//        val TAG: String = CaptureItemMeasurePhotoFragment::class.java.simpleName
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
//    private lateinit var jobItemMeasurePhotoArrayList: ArrayList<JobItemMeasurePhotoDTOTemp>
//    private val mAppExcutor: AppExecutor? = null
//    private var mTempPhotoPath: String? = null
//    private var mResultsBitmap: Bitmap? = null
//    private lateinit var selectedJobItemMeasure: JobItemMeasureDTOTemp
//
//    // endregion (Public Fields)
//    protected var googleApiClient: GoogleApiClient? = null
//    protected var locationRequest: LocationRequest? = null
//
//    var viewPhotosOnly = false
//    private lateinit var imageUri: Uri
//    var locationHelper: LocationHelper? = null
//    private val captureAnotherPhotoButton: Button? = null
//    private lateinit var photoName: String
//    @State
//    var filename_path = HashMap<String, String>()
//    private var currentLocation: Location? = null
//
//    @Synchronized
//    protected fun buildGoogleApiClient() {
//        Log.i(TAG, getString(R.string.building_googleapiclient))
//        googleApiClient = GoogleApiClient.Builder(activity!!.applicationContext)
//            .addConnectionCallbacks(this)
//            .addOnConnectionFailedListener(this)
//            .addApi(LocationServices.API)
//            .build()
//        createLocationRequest()
//    }
//
//    override fun onAttach(context: Context) {
//        super.onAttach(context)
//        (activity as MainActivity).supportActionBar?.title = getString(R.string.captured_photos)
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        (activity as MainActivity).supportActionBar?.title = getString(R.string.captured_photos)
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        return inflater.inflate(R.layout.fragment_capture_item_measure_photo, container, false)
//    }
//
//    override fun onActivityCreated(savedInstanceState: Bundle?) {
//        super.onActivityCreated(savedInstanceState)
//        measureViewModel = activity?.run {
//            ViewModelProviders.of(this, factory).get(MeasureViewModel::class.java)
//        } ?: throw Exception("Invalid Activity") as Throwable
//        Coroutines.main {
//            photoButtons.visibility = View.GONE
//            measureViewModel.measurea1_Item1.observe(
//                viewLifecycleOwner,
//                Observer { selectedJobItemM ->
//                    toast(selectedJobItemM.jimNo)
//                    selectedJobItemMeasure = selectedJobItemM
//                    takeMeasurePhoto()
//                //                   getPhotosForSelectedJobItemMeasure(selectedJobItemM)
//
//                })
//            jobItemMeasurePhotoArrayList = ArrayList<JobItemMeasurePhotoDTOTemp>()
//
//        }
//
////        var imagePaths: ArrayList<String> = getIntent().getStringArrayListExtra(URI_LIST_DATA)
////        var currentPos: Int = getIntent().getIntExtra(IMAGE_FULL_SCREEN_CURRENT_POS, 0)
////
////        val manager: FragmentManager = getSupportFragmentManager()
////        val adapter = PagerAdapter(manager, imagePaths)
////        viewPager.setAdapter(adapter)
////        viewPager.setCurrentItem(currentPos)
//
//
////        capture_another_photo_button.setOnClickListener {
////
////        }
//
//        done_image_button.setOnClickListener { save ->
//            saveImgae()
////            updateJobItemMeasures(jobItemMeasureArrayList,measureViewModel)
//            setJobItemMeasureImage(jobItemMeasurePhotoArrayList, measureViewModel)
//        }
//        Save.visibility = View.GONE
//        Save.setOnClickListener { save ->
//            saveImgae()
////            updateJobItemMeasures(jobItemMeasureArrayList,measureViewModel)
//            setJobItemMeasureImage(jobItemMeasurePhotoArrayList, measureViewModel)
////            Navigation.findNavController(save).navigate(R.id.action_captureItemMeasurePhotoFragment_to_nav_estMeasure)
//        }
//
//    }
//
//
//
//    private fun setJobItemMeasureImage(
//        jobItemMeasurePhotoList: ArrayList<JobItemMeasurePhotoDTOTemp>,
//        measureViewModel: MeasureViewModel
//    ) {
//        Coroutines.main {
//            measureViewModel.setJobItemMeasureImages(jobItemMeasurePhotoList)
//        }
//    }
//
//    private fun saveImgae(): JobItemMeasurePhotoDTOTemp {
//
//        try { //  Location of picture
////            val currentLocation: Location = getCurrentLocation()!!
////            if (currentLocation == null) toast("Error: Current location is null!")
////                currentLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient)
//            //  Save Image to Internal Storage
//            filename_path =
//                PhotoUtil.saveImageToInternalStorage(context!!, imageUri) as HashMap<String, String>
//
//        } catch (e: java.lang.Exception) {
//            toast(R.string.error_getting_image)
//            Log.e("x-takePhoto", e.message)
//            e.printStackTrace()
//        }
//        val photoId = SqlLitUtils.generateUuid()
////            val PhotoLatitude = currentLocation.latitude
////            val PhotoLongitude = currentLocation.longitude
//
//        val jobItemMeasurePhoto = JobItemMeasurePhotoDTOTemp(
//            0,
//            null,
//            filename_path.get("filename"),
//            selectedJobItemMeasure.itemMeasureId,
//            DateUtil.DateToString(Date()),
//            photoId.toString(),
////            PhotoLatitude,
////            PhotoLongitude,
//            0.0,
//            0.0,
//
//            filename_path.get("path"),
//            selectedJobItemMeasure,
//            0,
//            0
//        )
//
//        jobItemMeasurePhotoArrayList!!.add(jobItemMeasurePhoto)
//
//        return jobItemMeasurePhoto!!
//    }
//
////    fun getCurrentLocation(): Location? {
////        return currentLocation
////    }
//
//    private fun takeMeasurePhoto() {
//
//        val logoutBuilder =
//            AlertDialog.Builder(activity!!, android.R.style.Theme_DeviceDefault_Dialog)
//        logoutBuilder.setTitle(R.string.capture_measure_photo)
//        logoutBuilder.setIcon(R.drawable.ic_menu_camera)
//        logoutBuilder.setMessage(R.string.start_taking_photo)
//        logoutBuilder.setCancelable(false)
//        // Yes button
//        logoutBuilder.setPositiveButton(R.string.ok) { dialog, which ->
//            if (ContextCompat.checkSelfPermission(
//                    activity!!,
//                    Manifest.permission.WRITE_EXTERNAL_STORAGE
//                ) !== PackageManager.PERMISSION_GRANTED
//            ) {
//                ActivityCompat.requestPermissions(
//                    activity!!,
//                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
//                    REQUEST_STORAGE_PERMISSION
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
////    private fun getPhotosForSelectedJobItemMeasure(selectedJobItemMeasure: JobItemMeasureDTOTemp) {
////        Coroutines.main {
////
////            for (jobItemMeasurePhoto in jobItemMeasurePhotoArrayList.iterator()) {
////                if (!PhotoUtil.photoExist(jobItemMeasurePhoto.filename!!)) {
////
////
//////                    measureViewModel.getPhotoForJobItemMeasure(jobItemMeasurePhoto.filename)
////                    val jobItemMeasurePhotos =  measureViewModel.getJobItemMeasurePhotosForItemMeasureID(selectedJobItemMeasure.itemMeasureId!!)
////                    jobItemMeasurePhotos.observe(activity!!, Observer { m_photos->
////                        if (m_photos != null) {
////                           toast(m_photos.size.toString())
////                            if (m_photos.size > 0) jobItemMeasurePhotoArrayList = m_photos as ArrayList<JobItemMeasurePhotoDTOTemp>
////                        } else {
////
////                        }
//////
//////
////                    })
////
////
////                } else {
//////
////                }
////            }
////
////
////
////
////
////
//////        for (jobItemMeasurePhoto in jobItemMeasurePhotoArrayList!!.iterator()) {
////
//////        }
////
////        }
////
////    }
////
//
//    private fun launchCamera() {
////        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
////        if (takePictureIntent.resolveActivity(activity!!.applicationContext.getPackageManager()) != null) { // Create the temporary File where the photo should go
////            var photoFile: File? = null
////            try { photoFile = BitmapUtils.createTempImageFile(activity!!)
////            } catch (ex: IOException) { // Error occurred while creating the File
////                ex.printStackTrace()
////            }
////            // Continue only if the File was successfully created
////            if (photoFile != null) { // Get the path of the temporary file
////                mTempPhotoPath = photoFile.absolutePath
////                // Get the content URI for the image file
////                val photoURI: Uri = FileProvider.getUriForFile(activity!!.applicationContext, FILE_PROVIDER_AUTHORITY, photoFile)
////                // Add the URI so the camera can store the image
////                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
////                takePictureIntent.putExtra(MediaStore.EXTRA_SCREEN_ORIENTATION, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
////                // Launch the camera activity
////                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
////            }
////        }
////        Coroutines.main {
////
////            imageUri = PhotoUtil.getUri2(this)!!
////            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
////            if (takePictureIntent.resolveActivity(activity!!.packageManager) != null) {
////                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
////                takePictureIntent.putExtra(
////                    MediaStore.EXTRA_SCREEN_ORIENTATION,
////                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
////                )
////                startActivityForResult(takePictureIntent, AbstractIntent.REQUEST_TAKE_PHOTO)
////            }
////
////        }
//    }
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) { // Process the image and set it to the TextView
//            processAndSetImage()
//        } else { // Otherwise, delete the temporary image file
//            BitmapUtils.deleteImageFile(context!!.applicationContext, mTempPhotoPath)
//        }
//    }
//
//    private fun processAndSetImage() {
//        GlideApp.with(this.activity!!)
//            .load(imageUri)
//            .into(m_imageView)
//        photoButtons.visibility = View.VISIBLE
//    }
//
////    override fun onPause() {
////        super.onPause()
////        if (googleApiClient!!.isConnected()) {
////            stopLocationUpdates()
////        }
////    }
//
//    private fun updateValuesFromBundle(savedInstanceState: Bundle?) {
//        Log.i(
//            TAG,
//            getString(R.string.updating_location_values_from_bundle)
//        )
//        if (savedInstanceState != null) {
//            if (savedInstanceState.keySet().contains(LOCATION_KEY))
//                currentLocation = savedInstanceState.getParcelable<Location>(LOCATION_KEY)
//        }
//    }
//
//    protected fun createLocationRequest() {
//        locationRequest = LocationRequest()
//        locationRequest!!.interval = UPDATE_INTERVAL_IN_MILLISECONDS
//        locationRequest!!.fastestInterval = FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS
//        locationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
//    }
//
//
//    protected fun startLocationUpdates() {
//        if (PermissionController.checkPermissionsEnabled(activity!!.applicationContext)) {
//            if (LocationServices.FusedLocationApi != null) {
//                LocationServices.FusedLocationApi.requestLocationUpdates(
//                    googleApiClient,
//                    locationRequest,
//                    this
//                )
//            }
//        } else {
//            PermissionController.startPermissionRequests(
//                activity,
//                activity!!.getApplicationContext()
//            )
//        }
//    }
//
//    protected fun stopLocationUpdates() {
//        if (PermissionController.checkPermissionsEnabled(activity!!.getApplicationContext())) {
//            if (LocationServices.FusedLocationApi != null) {
//                LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this)
//            }
//        } else {
//            PermissionController.startPermissionRequests(
//                activity,
//                activity!!.getApplicationContext()
//            )
//        }
//    }
//
//
//    override fun onConnected(p0: Bundle?) {
//        Log.i(TAG, getString(R.string.connected_to_googleapiclient))
//
//        if (null == currentLocation) currentLocation =
//            LocationServices.FusedLocationApi.getLastLocation(googleApiClient)
//        startLocationUpdates()
//    }
//
//
//    override fun onConnectionSuspended(cause: Int) {
//        Log.i(TAG, getString(R.string.connection_suspended))
//        googleApiClient!!.connect()
//    }
//
//    override fun onConnectionFailed(result: ConnectionResult) {
//        Log.i(TAG, getString(R.string.connection_failed_get_error_code) + result.errorCode)
//    }
//
//    override fun onLocationChanged(location: Location) {
//        currentLocation = location
//    }
//
//
//}
//
//
////// endregion (Protected Synchronized Methods)
////// region (Public Methods)
////fun capturePhoto() {
////    if (PermissionController.checkPermissionsEnabled(getApplicationContext())) { //            ContentValues values = new ContentValues();
////        val uuid = UUID.randomUUID()
////        photoName = uuid.toString()
////        //            photoName = SqlLitUtils.INSTANCE.generateUuid();
//////            values.put(MediaStore.Images.Media.TITLE, photoName);
//////            values.put(MediaStore.Images.Media.DESCRIPTION, getString(R.string.from_your_camera));
////        val imagesFolder =
////            File(Environment.getExternalStorageDirectory(), PhotoUtil.FOLDER)
////        imagesFolder.mkdirs()
////        val image = File(imagesFolder, photoName + getString(R.string.jpg))
////        imageUri = Uri.fromFile(image)
////        val photoURI = FileProvider.getUriForFile(
////            this@BaseCapturePhotoActivity, BuildConfig.APPLICATION_ID + ".provider",
////            image
////        )
////        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
////        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
////        startActivityForResult(
////            intent,
////            za.co.xisystems.itismaintenance.newCode.activities.BaseCapturePhotoActivity.CAMERA_REQUEST
////        )
////    } else {
////        PermissionController.startPermissionRequests(this, getApplicationContext())
////    }
////}
//
//
////        private fun populateAppropriateViewForPhotos() {
////        setupViewAccordingToNumberOfPhotos()
////        if (jobItemMeasurePhotoArrayList!!.size > 1) {
////            setupGrid()
////            this.photoGridAdapter.notifyDataSetChanged()
////        } else if (jobItemMeasurePhotoArrayList!!.size == 1) {
////            val singlePhotoImageView =
////                findViewById(R.id.single_photo_image_view) as ImageView
////            singlePhotoImageView.scaleType = ImageView.ScaleType.FIT_CENTER
////            val jobItemMeasurePhoto: JobItemMeasurePhoto = jobItemMeasurePhotoArrayList!![0]
////            if (PhotoUtil.photoExist(jobItemMeasurePhoto.getFilename())) {
////                singlePhotoImageView.rotation = PhotoUtil.getRotationForImage(
////                    PhotoUtil.getPhotoPathFromExternalDirectory(
////                        getApplicationContext(),
////                        jobItemMeasurePhoto.getFilename()
////                    )
////                )
////                singlePhotoImageView.setImageBitmap(
////                    PhotoUtil.getPhotoBitmapFromFile(
////                        getApplicationContext(),
////                        PhotoUtil.getPhotoPathFromExternalDirectory(
////                            getApplicationContext(),
////                            jobItemMeasurePhoto.getFilename()
////                        ), PhotoQuality.THUMB
////                    )
////                )
////            }
////        }
////        populateBackResult()
////        toast(R.string.photo_downloaded_successfully)
////        dismissProgressDialog()
////    }
//
////    private fun setupViewAccordingToNumberOfPhotos() {
////        val optionId: Int =
////            if (jobItemMeasurePhotoArrayList!!.size == 1) R.layout.single_photo else R.layout.grid_photos
////        val frame = findViewById(R.id.photo_placeholder_frame) as FrameLayout
////        frame.removeAllViews()
////        val photoView = layoutInflater.inflate(optionId, frame, false)
////        frame.addView(photoView)
////    }
//
//
////override fun onDetach() {
////    super.onDetach()
//////        listener = null
////}