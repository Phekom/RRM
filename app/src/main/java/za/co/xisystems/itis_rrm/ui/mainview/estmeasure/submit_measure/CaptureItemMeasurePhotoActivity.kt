package za.co.xisystems.itis_rrm.ui.mainview.estmeasure.submit_measure

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import icepick.State
import kotlinx.android.synthetic.main.fragment_capture_item_measure_photo.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance
import za.co.xisystems.itis_rrm.BuildConfig
import za.co.xisystems.itis_rrm.MainActivity
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemMeasureDTOTemp
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemMeasurePhotoDTOTemp
import za.co.xisystems.itis_rrm.ui.mainview.create.edit_estimates.AppExecutor
import za.co.xisystems.itis_rrm.ui.mainview.create.edit_estimates.BitmapUtils
import za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils.LocationHelper
import za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils.intents.AbstractIntent
import za.co.xisystems.itis_rrm.ui.mainview.estmeasure.MeasureViewModel
import za.co.xisystems.itis_rrm.ui.mainview.estmeasure.MeasureViewModelFactory
import za.co.xisystems.itis_rrm.ui.mainview.estmeasure.submit_measure.ExpandableHeaderMeasureItem.Companion.JOB_IMEASURE
import za.co.xisystems.itis_rrm.utils.*
import java.util.*

class CaptureItemMeasurePhotoActivity : AppCompatActivity() , KodeinAware,
    GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener,
    LocationListener
{
    override val kodein by kodein()
    private lateinit var measureViewModel: MeasureViewModel
    private val factory: MeasureViewModelFactory by instance()

    companion object {
        val TAG: String = CaptureItemMeasurePhotoActivity::class.java.simpleName
        const val JOB_ITEM_MEASURE_PHOTO_ARRAY_LIST = "JobItemMeasurePhotoArrayList"
        const val PHOTO_RESULT = 9000
        private const val REQUEST_IMAGE_CAPTURE = 1
        private const val REQUEST_STORAGE_PERMISSION = 1
        private val FILE_PROVIDER_AUTHORITY = BuildConfig.APPLICATION_ID + ".provider"

        const val URI_LIST_DATA = "URI_LIST_DATA"
        const val IMAGE_FULL_SCREEN_CURRENT_POS = "IMAGE_FULL_SCREEN_CURRENT_POS"

        protected const val LOCATION_KEY = "location-key"
        // region (Public Static Final Fields)
        const val UPDATE_INTERVAL_IN_MILLISECONDS: Long = 10000
        const val FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2


    }
    private val isLocationEnabled: Boolean
        get() {
            locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            return locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        }
    private lateinit var jobItemMeasurePhotoArrayList: ArrayList<JobItemMeasurePhotoDTOTemp>
    private val mAppExcutor: AppExecutor? = null
    private var mTempPhotoPath: String? = null
    private var mResultsBitmap: Bitmap? = null
    private lateinit var selectedJobItemMeasure: JobItemMeasureDTOTemp

    // endregion (Public Fields)
//    private lateinit var fusedLocationClient: FusedLocationProviderClient
//    private lateinit var locationRequest: LocationRequest
//    private lateinit var locationCallback: LocationCallback

    private var mGoogleApiClient: GoogleApiClient? = null
    private var mLocation: Location? = null
    private var mLocationManager: LocationManager? = null

    private var mLocationRequest: LocationRequest? = null
    private val listener: com.google.android.gms.location.LocationListener? = null
    private val UPDATE_INTERVAL = (2 * 1000).toLong()  /* 10 secs */
    private val FASTEST_INTERVAL: Long = 2000 /* 2 sec */

    private var locationManager: LocationManager? = null
    private var currentLocation: Location? = null


    var viewPhotosOnly = false
    private lateinit var imageUri: Uri
    var locationHelper: LocationHelper? = null
    private val captureAnotherPhotoButton: Button? = null
    private lateinit var photoName: String
    @State
    var filename_path = HashMap<String, String>()
    @State
    var location = HashMap<String, String>()
//    private lateinit var currentLocation : Location

    @Synchronized
    protected fun buildGoogleApiClient() {
        Log.i(TAG, getString(R.string.building_googleapiclient))

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_capture_item_measure_photo)
        (this).supportActionBar?.title = getString(R.string.captured_photos)

        measureViewModel = this?.run {
            ViewModelProviders.of(this, factory).get(MeasureViewModel::class.java)
        } ?: throw Exception("Invalid Activity") as Throwable

        mGoogleApiClient = GoogleApiClient.Builder(this)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(LocationServices.API)
            .build()

        mLocationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        Log.d("gggg","uooo");
        checkLocation()

        Coroutines.main {
            photoButtons.visibility = View.GONE
//            getIntent().getSerializableExtra(JOB_IMEASURE)

            if (intent.hasExtra(ExpandableHeaderMeasureItem.JOB_IMEASURE)) {
                selectedJobItemMeasure = intent.extras[JOB_IMEASURE] as JobItemMeasureDTOTemp
                takeMeasurePhoto()
                toast(selectedJobItemMeasure.jimNo.toString())
            }



//            measureViewModel.measurea1_Item1.observe(
//                this, Observer { selectedJobItemM ->
//
//                    selectedJobItemMeasure = selectedJobItemM
//                    takeMeasurePhoto()
//                    //     getPhotosForSelectedJobItemMeasure(selectedJobItemM)
//
//                })
            jobItemMeasurePhotoArrayList = ArrayList<JobItemMeasurePhotoDTOTemp>()

        }
        capture_another_photo_button.setOnClickListener {
            launchCamera()
        }

        done_image_button.setOnClickListener { save ->
            saveImgae()
            setJobItemMeasureImage(jobItemMeasurePhotoArrayList, measureViewModel,selectedJobItemMeasure.estimateId)

            Intent(this, MainActivity::class.java).also { home ->
                home.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(home)
            }


//            Navigation.findNavController(save).navigate(R.id.action_captureItemMeasurePhotoFragment_to_nav_estMeasure)
        }





        Save.visibility = View.GONE
//        Save.setOnClickListener { save ->
//            saveImgae()
////            updateJobItemMeasures(jobItemMeasureArrayList,measureViewModel)
//            setJobItemMeasureImage(jobItemMeasurePhotoArrayList, measureViewModel)
//
//        }


    }

//    private fun checkLocation() {
//        val manager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
//        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
//            showAlertLocation()
//        }
//        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
//        getLocationUpdates()
//    }
//
//    private fun getLocationUpdates() {
//        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
//        locationRequest = LocationRequest()
//        locationRequest.interval = 50000
//        locationRequest.fastestInterval = 50000
//        locationRequest.smallestDisplacement = 170f //170 m = 0.1 mile
//        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY //according to your app
//        locationCallback = object : LocationCallback() {
//            override fun onLocationResult(locationResult: LocationResult?) {
//                locationResult ?: return
//                if (locationResult.locations.isNotEmpty()) {
//                    /*val location = locationResult.lastLocation
//                    Log.e("location", location.toString())*/
//                    val addresses: List<Address>?
//                    val geoCoder = Geocoder(applicationContext, Locale.getDefault())
//                    addresses = geoCoder.getFromLocation(
//                        locationResult.lastLocation.latitude,
//                        locationResult.lastLocation.longitude,
//                        1
//                    )
//                    if (addresses != null && addresses.isNotEmpty()) {
//                        val address: String = addresses[0].getAddressLine(0)
//                        val city: String = addresses[0].locality
//                        val state: String = addresses[0].adminArea
//                        val country: String = addresses[0].countryName
//                        val postalCode: String = addresses[0].postalCode
//                        val knownName: String = addresses[0].featureName
//                        val lat : Double = addresses[0].latitude
//                        val long :Double =  addresses[0].longitude
//                        Log.e("location", "$address $city $state $postalCode $country $knownName")
//                        Log.e("location", "$lat $long ")
//                    }
//                }
//            }
//        }
//    }
//
//
//    // Start location updates
//    private fun startLocationUpdates() {
//        fusedLocationClient.requestLocationUpdates(
//            locationRequest,
//            locationCallback,
//            null /* Looper */
//        )
//    }
//    // Stop location updates
//    private fun stopLocationUpdates() {
//        fusedLocationClient.removeLocationUpdates(locationCallback)
//    }
//
//    // Stop receiving location update when activity not visible/foreground
//    override fun onPause() {
//        super.onPause()
//        stopLocationUpdates()
//    }
//
//    // Start receiving location update when activity  visible/foreground
//    override fun onResume() {
//        super.onResume()
//        startLocationUpdates()
//    }

    private fun showAlertLocation() {
        val dialog = AlertDialog.Builder(this)
        dialog.setMessage("Your location settings is set to Off, Please enable location to use this application")
        dialog.setPositiveButton("Settings") { _, _ ->
            val myIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(myIntent)
        }
        dialog.setNegativeButton("Cancel") { _, _ ->
            finish()
        }
        dialog.setCancelable(false)
        dialog.show()
    }

    private fun setJobItemMeasureImage(
        jobItemMeasurePhotoList: ArrayList<JobItemMeasurePhotoDTOTemp>,
        measureViewModel: MeasureViewModel,
        estimateId: String?
    ) {
        Coroutines.main {
            measureViewModel.setJobItemMeasureImages(jobItemMeasurePhotoList,estimateId)
        }
    }

    private fun saveImgae(): JobItemMeasurePhotoDTOTemp {

       val photoId = SqlLitUtils.generateUuid()
        //  Save Image to Internal Storage
        filename_path = PhotoUtil.saveImageToInternalStorage(this, imageUri) as HashMap<String, String>
//        location =
//        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
//        locationRequest = LocationRequest()
//        locationRequest.interval = 50000
//        locationRequest.fastestInterval = 50000
//        locationRequest.smallestDisplacement = 170f //170 m = 0.1 mile
//        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY //according to RRM
//        locationCallback = object : LocationCallback(){
//
//        }
        val photoLat  = currentLocation?.latitude?.toDouble()
        var latitude = 0.0
        var longitude = 0.0
        Log.e("location", " ${currentLocation?.longitude} ")

        val jobItemMeasurePhoto = JobItemMeasurePhotoDTOTemp(
            0,
            null,
            filename_path.get("filename"),
            selectedJobItemMeasure.itemMeasureId,
            selectedJobItemMeasure.estimateId,
            DateUtil.DateToString(Date()),
            photoId.toString(),null,null,0.0,0.0,
           latitude,
           longitude,
//            0.0,
//            0.0,

            filename_path.get("path"),
            selectedJobItemMeasure,
            0,
            0
        )

        jobItemMeasurePhotoArrayList!!.add(jobItemMeasurePhoto)
//        jobForJobItemEstimate.setJobItemMeasures(jobItemMeasurePhotoArrayList)
        return jobItemMeasurePhoto!!
    }

    private fun takeMeasurePhoto() {

        val logoutBuilder =
            AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog)
        logoutBuilder.setTitle(R.string.capture_measure_photo)
        logoutBuilder.setIcon(R.drawable.ic_menu_camera)
        logoutBuilder.setMessage(R.string.start_taking_photo)
        logoutBuilder.setCancelable(false)
        // Yes button
        logoutBuilder.setPositiveButton(R.string.ok) { dialog, which ->
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !== PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this!!,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    CaptureItemMeasurePhotoActivity.REQUEST_STORAGE_PERMISSION
                )
            } else {
                launchCamera()
            }
        }
        val declineAlert = logoutBuilder.create()
        declineAlert.show()

    }


    private fun launchCamera() {

        Coroutines.main {
            imageUri = PhotoUtil.getUri2(this)!!
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (takePictureIntent.resolveActivity(this!!.packageManager) != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                takePictureIntent.putExtra(
                    MediaStore.EXTRA_SCREEN_ORIENTATION,
                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                )
                startActivityForResult(takePictureIntent, AbstractIntent.REQUEST_TAKE_PHOTO)
            }

        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CaptureItemMeasurePhotoActivity.REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) { // Process the image and set it to the TextView
            processAndSetImage()
        } else { // Otherwise, delete the temporary image file
            BitmapUtils.deleteImageFile(this.applicationContext, mTempPhotoPath)
        }
    }

    private fun processAndSetImage() {
        GlideApp.with(this)
            .load(imageUri)
            .into(m_imageView)
        photoButtons.visibility = View.VISIBLE
    }


    private fun updateValuesFromBundle(savedInstanceState: Bundle?) {
        Log.i(
            CaptureItemMeasurePhotoActivity.TAG,
            getString(R.string.updating_location_values_from_bundle)
        )
        if (savedInstanceState != null) {
//            @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
//            if (savedInstanceState.keySet().contains(CaptureItemMeasurePhotoActivity.LOCATION_KEY))
//                currentLocation = savedInstanceState.getParcelable<Location>(
//                    CaptureItemMeasurePhotoActivity.LOCATION_KEY
//                )
        }
    }

//    protected fun createLocationRequest() {
//        locationRequest = LocationRequest()
//        locationRequest!!.interval = CaptureItemMeasurePhotoActivity.UPDATE_INTERVAL_IN_MILLISECONDS
//        locationRequest!!.fastestInterval =
//            CaptureItemMeasurePhotoActivity.FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS
//        locationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
//    }


//    @SuppressLint("MissingPermission")
//    override fun onConnected(p0: Bundle?) {
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return
//        }
//
//        startLocationUpdates()
//
//        mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient)
//
//        if (mLocation == null) {
//            startLocationUpdates()
//        }
//        if (mLocation != null) {
//
//            // mLatitudeTextView.setText(String.valueOf(mLocation.getLatitude()));
//            //mLongitudeTextView.setText(String.valueOf(mLocation.getLongitude()));
//        } else {
//            Toast.makeText(this, "Location not Detected", Toast.LENGTH_SHORT).show()
//        }
//    }

//    override fun onConnectionSuspended(i: Int) {
//        Log.i(TAG, "Connection Suspended")
//        mGoogleApiClient!!.connect()
//    }

//    override fun onConnectionFailed(connectionResult: ConnectionResult) {
//        Log.i(TAG, "Connection failed. Error: " + connectionResult.getErrorCode())
//    }

//    override fun onStart() {
//        super.onStart()
//        if (mGoogleApiClient != null) {
//            mGoogleApiClient!!.connect()
//        }
//    }
//
//    override fun onStop() {
//        super.onStop()
//        if (mGoogleApiClient!!.isConnected()) {
//            mGoogleApiClient!!.disconnect()
//        }
//    }
    override fun onLocationChanged(location: Location) {

//        val msg = "Updated Location: " +
//                java.lang.Double.toString(location.latitude) + "," +
//                java.lang.Double.toString(location.longitude)
//        this.photoLatitude = location.latitude.toString()
//        this.photoLongitude = location.longitude.toString()
          currentLocation = location

////        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
//        // You can now create a LatLng Object for use with maps
        val latLng = LatLng(location.latitude, location.longitude)

    }



    @SuppressLint("MissingPermission")
    override fun onConnected(p0: Bundle?) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }

        startLocationUpdates()

        mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient)

        if (mLocation == null) {
            startLocationUpdates()
        }
        if (mLocation != null) {

            // mLatitudeTextView.setText(String.valueOf(mLocation.getLatitude()));
            //mLongitudeTextView.setText(String.valueOf(mLocation.getLongitude()));
        } else {
            Toast.makeText(this, "Location not Detected", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onConnectionSuspended(i: Int) {
        Log.i(TAG, "Connection Suspended")
        mGoogleApiClient!!.connect()
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        Log.i(TAG, "Connection failed. Error: " + connectionResult.getErrorCode())
    }

    override fun onStart() {
        super.onStart()
        if (mGoogleApiClient != null) {
            mGoogleApiClient!!.connect()
        }
    }

    override fun onStop() {
        super.onStop()
        if (mGoogleApiClient!!.isConnected()) {
            mGoogleApiClient!!.disconnect()
        }
    }

    @SuppressLint("MissingPermission")
    protected fun startLocationUpdates() {
        // Create the location request
        mLocationRequest = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(UPDATE_INTERVAL)
            .setFastestInterval(FASTEST_INTERVAL)
        // Request location updates
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
            mLocationRequest, this)
        Log.d("reque", "--->>>>")
    }



    private fun checkLocation(): Boolean {
        if (!isLocationEnabled)
            showAlert()
        return isLocationEnabled
    }

    private fun showAlert() {
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("Enable Location")
            .setMessage("Your Locations Settings is set to 'Off'.\nPlease Enable Location to " + "use this app")
            .setPositiveButton("Location Settings") { paramDialogInterface, paramInt ->
                val myIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(myIntent)
            }
            .setNegativeButton("Cancel") { paramDialogInterface, paramInt -> }
        dialog.show()
    }



}



