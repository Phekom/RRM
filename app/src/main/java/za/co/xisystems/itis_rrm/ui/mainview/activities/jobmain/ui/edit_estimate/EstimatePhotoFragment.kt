package za.co.xisystems.itis_rrm.ui.mainview.activities.jobmain.ui.edit_estimate

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog.Builder
import android.app.Dialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RadioButton
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.*
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.airbnb.lottie.LottieAnimationView
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.Style
import com.mapbox.maps.extension.observable.eventdata.MapLoadingErrorEventData
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.delegates.listeners.OnMapLoadErrorListener
import com.mapbox.maps.plugin.gestures.OnMapClickListener
import com.mapbox.maps.plugin.gestures.removeOnMapClickListener
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.trip.session.LocationMatcherResult
import com.mapbox.navigation.core.trip.session.LocationObserver
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.instance
import timber.log.Timber
import za.co.xisystems.itis_rrm.BuildConfig
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.base.LocationFragment
import za.co.xisystems.itis_rrm.custom.errors.XIErrorHandler
import za.co.xisystems.itis_rrm.custom.notifications.ColorToast
import za.co.xisystems.itis_rrm.custom.notifications.ToastDuration
import za.co.xisystems.itis_rrm.custom.notifications.ToastGravity
import za.co.xisystems.itis_rrm.custom.notifications.ToastStyle
import za.co.xisystems.itis_rrm.custom.results.XIResult
import za.co.xisystems.itis_rrm.custom.views.IndefiniteSnackbar
import za.co.xisystems.itis_rrm.data._commons.views.ToastUtils
import za.co.xisystems.itis_rrm.data.localDB.entities.ItemDTOTemp
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimateDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimatesPhotoDTO
import za.co.xisystems.itis_rrm.databinding.FragmentPhotoEstimateBinding
import za.co.xisystems.itis_rrm.services.LocationModel
import za.co.xisystems.itis_rrm.ui.extensions.crashGuard
import za.co.xisystems.itis_rrm.ui.extensions.extensionToast
import za.co.xisystems.itis_rrm.ui.mainview.activities.jobmain.JobCreationActivity
import za.co.xisystems.itis_rrm.ui.mainview.activities.jobmain.new_job_utils.models.PhotoType
import za.co.xisystems.itis_rrm.utils.Coroutines
import za.co.xisystems.itis_rrm.utils.GlideApp
import za.co.xisystems.itis_rrm.utils.JobItemEstimateSize
import za.co.xisystems.itis_rrm.utils.PhotoUtil
import za.co.xisystems.itis_rrm.utils.image_capture.helper.DialogHelper
import za.co.xisystems.itis_rrm.utils.image_capture.helper.ImageProvider
import za.co.xisystems.itis_rrm.utils.image_capture.listener.DismissListener
import za.co.xisystems.itis_rrm.utils.image_capture.listener.ResultListener
import za.co.xisystems.itis_rrm.utils.image_capture.model.GridCount
import za.co.xisystems.itis_rrm.utils.image_capture.model.Image
import za.co.xisystems.itis_rrm.utils.image_capture.model.ImagePickerConfig
import za.co.xisystems.itis_rrm.utils.image_capture.model.RootDirectory
import za.co.xisystems.itis_rrm.utils.image_capture.registerImagePicker
import za.co.xisystems.itis_rrm.utils.zoomage.ZoomageView
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.DecimalFormat
import kotlin.collections.set


/**
 * Created by Francis Mahlava on 2019/12/29.
 * Updated by Shaun McDonald on 2021/05/18
 * Last modified on 2021/05/18, 10:26
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 * * Updated by Francis Mahlava on 2022/01/23
 * Updated by Francis Mahlava on 2022/03/04
 */


class EstimatePhotoFragment : LocationFragment() {

    private var selectedJobType: String? = null
    private var sectionId: String? = null
    private lateinit var estimatePhotoViewModel: EstimatePhotoViewModel
    private val factory: EstimatePhotoViewModelFactory by instance()
    private var mAppExecutor: AppExecutor? = null
    private lateinit var lm: LocationManager
    private var gpsEnabled = false
    private var networkEnabled = false
    private var isEstimateDone: Boolean = false
    private var disableGlide: Boolean = false
    private var locationWarning: Boolean = false
    private var photoType: PhotoType = PhotoType.START
    private var itemIdPhotoType: HashMap<String, String> = HashMap()
    private var filenamePath: HashMap<String, String> = HashMap()
    private var item: ItemDTOTemp? = null
    private var newJob: JobDTO? = null
    internal var estimate: JobItemEstimateDTO? = null
    var direction: String? = null
    private var newJobItemEstimate: JobItemEstimateDTO? = null
    var quantity: Double = 1.0
    private var estimateId: String? = null
    private lateinit var newJobItemEstimatesPhotosList: ArrayList<JobItemEstimatesPhotoDTO>
    private lateinit var newJobItemEstimatesList: ArrayList<JobItemEstimateDTO>
    internal var description: String? = null
    private var currentUser: Int = -1
    private var startImageUri: Uri? = null
    private var endImageUri: Uri? = null
    private var imageUri: Uri? = null
    private val photoUtil: PhotoUtil by instance()
    private var changesToPreserve: Boolean = false
    private var tenderRate: Double? = null
    private val navigationLocationProvider = NavigationLocationProvider()
    val editEstimateData : EstimatePhotoFragmentArgs by navArgs()

    private val locationObserver = object : LocationObserver {
        override fun onNewRawLocation(rawLocation: Location) {
            // You're going to need this when you
            // aren't driving.
            navigationLocationProvider.changePosition(rawLocation)
            updateCamera(rawLocation)
        }

        override fun onNewLocationMatcherResult(locationMatcherResult: LocationMatcherResult) {
            val enhancedLocation = locationMatcherResult.enhancedLocation
            navigationLocationProvider.changePosition(
                enhancedLocation,
                locationMatcherResult.keyPoints,
            )
//            updateCamera(enhancedLocation)
        }
    }

    private lateinit var mapboxMap: MapboxMap
    private lateinit var mapboxNavigation: MapboxNavigation
    private var _ui : FragmentPhotoEstimateBinding? = null
    private val ui get() = _ui!!

    private var images = ArrayList<Image>()
    private var imageProvider = ImageProvider.CAMERA
    private var imageProviderInterceptor: ((ImageProvider) -> Unit)? = null
    private var dismissListener: DismissListener? = null
    private val launcher = registerImagePicker {
        uiScope.launch(dispatchers.io()) {
            images = it
            if (images.isEmpty()) {
                Looper.prepare()
                extensionToast(
                    title = "Error Missing Image",
                    message = "No Image Seleted.",
                    style = ToastStyle.DELETE,
                    position = ToastGravity.BOTTOM,
                    duration = ToastDuration.LONG
                )
            }else{
                val uri = photoUtil.getPhotoPathFromExternalDirectory(images[0].name)
                processAndSetImage(uri, "GALLERY")
            }

        }
    }

    /**
     * ActivityResultContract for taking a photograph
     */
    private val takePicture = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isSaved ->
        if (isSaved) {
            uiScope.launch(dispatchers.io()) {
                imageUri?.let { realUri ->

                    // implemented below
                    val bitmap = getScreenShotFromView(ui.estimatemapview)

                    // if bitmap is not null then save it to gallery
                    if (bitmap != null) {
                        saveMediaToStorage(bitmap)
                    }

                    processAndSetImage(realUri, "CAMERA")
                }
            }
        } else {
            imageUri?.let { failedUri ->
                uiScope.launch(dispatchers.io()) {
                    val filenamePath = File(failedUri.path!!)
                    photoUtil.deleteImageFile(filenamePath.toString())
                    withContext(Dispatchers.Main.immediate) {
                        haltAnimation()
                        _ui?.startImageView?.visibility = View.VISIBLE
                        _ui?.endImageView?.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    //this method saves the image to gallery
    private fun saveMediaToStorage(bitmap: Bitmap) {
        //Generating a file name
        val filename = "${System.currentTimeMillis()}.jpg"

        //Output stream
        var fos: OutputStream? = null

        //For devices running android >= Q
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            //getting the contentResolver
            requireActivity().contentResolver?.also { resolver ->

                //Content resolver will process the contentvalues
                val contentValues = ContentValues().apply {

                    //putting file information in content values
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }

                //Inserting the contentValues to contentResolver and getting the Uri
                val imageUri: Uri? =
                    resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

                //Opening an outputstream with the Uri that we got
                fos = imageUri?.let { resolver.openOutputStream(it) }
            }
        } else {
            //These for devices running on android < Q
            val imagesDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val image = File(imagesDir, filename)
            fos = FileOutputStream(image)
        }

        fos?.use {
            //Finally writing the bitmap to the output stream that we opened
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
          //  Toast.makeText(requireContext() , "Captured View and saved to Gallery" , Toast.LENGTH_SHORT).show()

        }
    }

    init {
        System.setProperty("kotlinx.coroutines.debug", if (BuildConfig.DEBUG) "on" else "off")
        lifecycleScope.launch {

            whenCreated {
                estimatePhotoViewModel =
                    ViewModelProvider(
                        this@EstimatePhotoFragment.requireActivity(),
                        factory
                    )[EstimatePhotoViewModel::class.java]
            }

            whenResumed {
                if (newJob == null) readNavArgs()
            }
        }
    }

    companion object {
        private const val REQUEST_STORAGE_PERMISSION = 505
        private const val IMAGE_REQ_CODE = 101
        private const val GALLERY_INTENT_REQ_CODE = 4261
        private const val CAMERA_INTENT_REQ_CODE = 4281

        private const val GEOJSON_SOURCE_ID = "GEOJSON_SOURCE_ID"
        private const val ICON_LAYER_ID = "ICON_LAYER_ID"
        private const val ICON_SOURCE_ID = "ICON_SOURCE_ID"
        private const val ROUTE_LAYER_ID = "ROUTE_LAYER_ID"
        private const val RED_PIN_ICON_ID = "RED_PIN_ICON_ID"
        private const val DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L
        private const val DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5
    }

    val config = ImagePickerConfig(
        isMultipleMode = true,
        // isCameraOnly = false,
        isShowCamera = false,
        rootDirectory = RootDirectory.DCIM,
        subDirectory = "RRM App Photos",
        selectedImages = images,
        statusBarColor = "#000000",
        isLightStatusBar = false,
        backgroundColor = "#FFFFFF",
        imageGridCount = GridCount(3, 5),
        limitMessage = "You could only select up to 10 photos",
    )

    private val backClickListener = View.OnClickListener {
        setBackPressed(it)
    }

    private fun setBackPressed(view: View) {
        Coroutines.io {
            withContext(Dispatchers.Main.immediate) {
                val navDirection = EstimatePhotoFragmentDirections
                    .actionEstimatePhotoFragmentToNavigationAddItems(editEstimateData.itemId!!, editEstimateData.jobId,"")
                Navigation.findNavController(view).navigate(navDirection)
            }
        }
    }

    private fun getScreenShotFromView(v: View): Bitmap? {
        // create a bitmap object
        var screenshot: Bitmap? = null
        try {
            screenshot = Bitmap.createBitmap(v.measuredWidth, v.measuredHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(screenshot)
            v.draw(canvas)
        } catch (e: Exception) {
            Timber.e("Failed to capture screenshot because:%s", e.message)
        }
        return screenshot
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _ui = FragmentPhotoEstimateBinding.inflate(inflater, container, false)
        _ui?.toolbar?.apply {
            setTitle( getString(R.string.edit_estimate))
            setOnBackClickListener(backClickListener)
        }
        return ui.root
    }

    private fun setJobType(jobType: String) {
        _ui?.apply {
            when (jobType) {
                "Point" -> {
                    point.isChecked = true
                }
                "Line" -> {
                    line.isChecked = true
                }
                else -> {
                    line.isChecked = false
                    point.isChecked = false
                }
            }

            jobTypeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
                val radio: RadioButton = root.findViewById(checkedId)
                selectedJobType = "${radio.text}"
                when {
                    selectedJobType!!.contains("Point") -> {
                        _ui?.secondImage?.visibility = View.GONE
                        _ui?.startPhotoButton?.visibility = View.VISIBLE
                        _ui?.startPhotoButton?.text = getString(R.string.capture_photo)
                    }
                    selectedJobType!!.contains("Line") -> {
                        _ui?.secondImage?.visibility = View.VISIBLE
                        _ui?.startPhotoButton?.visibility = View.VISIBLE
                        _ui?.startPhotoButton?.text = getString(R.string.capture_start)
                    }
                    else -> {
                        _ui?.secondImage?.visibility = View.GONE
                        _ui?.startPhotoButton?.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun initMap() {
        initStyle()
        initNavigation()
    }

    private fun initStyle() {
        mapboxMap.loadStyleUri(
            Style.MAPBOX_STREETS, {},
            object : OnMapLoadErrorListener {
                override fun onMapLoadError(eventData: MapLoadingErrorEventData) {
                    Timber.e("${eventData.type.name} - ${eventData.message}")
                }
            }
        )
    }

    @SuppressLint("MissingPermission")
    private fun initNavigation() {
        mapboxNavigation = MapboxNavigation(
            NavigationOptions.Builder(requireContext())
                .accessToken(getString(R.string.mapbox_access_token))
                .build()
        ).apply {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                this@EstimatePhotoFragment.extensionToast(
                    message = "Please enable location services in order to proceed",
                    style = ToastStyle.WARNING,
                    position = ToastGravity.CENTER,
                    duration = ToastDuration.LONG
                )
            }
            startTripSession()
            registerLocationObserver(locationObserver)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
         locationWarning = false
        var jOB_ACTIVITY: JobCreationActivity = context as JobCreationActivity
        jOB_ACTIVITY.navigationView?.visibility = View.GONE
        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navToAddProject(this@EstimatePhotoFragment.requireView())
            }
        }
        requireActivity().onBackPressedDispatcher
            .addCallback(this@EstimatePhotoFragment, callback)
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        newJobItemEstimatesList = ArrayList()
        newJobItemEstimatesPhotosList = ArrayList()

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var stateRestored = false
        if (editEstimateData.jobId?.isNotBlank() == true) {
            readNavArgs()
            stateRestored = true
        }

        if (savedInstanceState != null && !stateRestored) {
            onRestoreInstanceState(savedInstanceState)
        } else {
            setJobType("")
        }

        mapboxMap = _ui?.estimatemapview?.getMapboxMap()!!
        _ui?.estimatemapview?.location?.apply {
            setLocationProvider(navigationLocationProvider)
            enabled = true
        }
        mapboxMap.removeOnMapClickListener( OnMapClickListener {
            false
        })
        _ui?.secondImage?.visibility = View.GONE
        _ui?.startPhotoButton?.visibility = View.GONE

        initMap()

        pullData()
    }



    @Suppress("MagicNumber")
    private fun updateCamera(location: Location) {
        val mapAnimationOptions = MapAnimationOptions.Builder().duration(1500L).build()
        _ui?.estimatemapview?.camera?.easeTo(
            CameraOptions.Builder()
                // Centers the camera to the lng/lat specified.
                .center(Point.fromLngLat(location.longitude, location.latitude))
                // specifies the zoom value. Increase or decrease to zoom in or zoom out
                .zoom(17.0)
                // specify frame of reference from the center.
                .padding(EdgeInsets(100.0, 0.0, 0.0, 0.0))
                .build(),
            mapAnimationOptions
        )

        location.apply {
            when {
                hasAccuracy() -> {
//                    if (accuracy < 20.0F ){
                        _ui?.group13loading?.visibility = View.GONE
                        _ui?.linearlayouthorizon?.visibility = View.VISIBLE
                        _ui?.lowBtns?.visibility = View.VISIBLE
                        _ui?.photoLin?.visibility = View.VISIBLE
                    // ToastUtils().toastShort(requireContext(), location.accuracy.toString())
//                    }else{
//                        _ui?.linearlayouthorizon?.visibility = View.INVISIBLE
//                        _ui?.lowBtns?.visibility = View.INVISIBLE
//                        _ui?.group13loading?.visibility = View.VISIBLE
//                        _ui?.textViewloading?.setText(R.string.updating_accuracy)
//                        extensionToast(
//                            title = "GPS Location Accuracy is $accuracy OR Lower",
//                            message = "And Not Good For Validation!\nPlease Stand In One Position",
//                            style = ToastStyle.WARNING,
//                            position = ToastGravity.BOTTOM
//                        )
//                    }
                }
                else ->
                    ToastUtils().toastShort(requireContext(), "GPS Location Accuracy Not Available At this place")
            }
        }
    }

    private fun pullData() = uiScope.launch(uiScope.coroutineContext) {

        withContext(uiScope.coroutineContext) {
            var myUserId: Int?
            estimatePhotoViewModel.loggedUser.distinctUntilChanged().observe(
                viewLifecycleOwner
            ) { user ->
                0
                user?.let {
                    myUserId = it
                    onUserFound(myUserId)
                }
            }
        }

        withContext(uiScope.coroutineContext) {
            val  myJobDTO = estimatePhotoViewModel.getJobForId(editEstimateData.jobId!!)
            onJobFound(myJobDTO)
        }

        withContext(uiScope.coroutineContext) {
            val item = estimatePhotoViewModel.getItemTempForID(editEstimateData.itemId!!)
            onItemFound(item)
        }

        if(editEstimateData.estimateId != null){
            withContext(uiScope.coroutineContext) {
                val estimateItem =  estimatePhotoViewModel.getEstimateForId(editEstimateData.estimateId!!)
                onEstimateFound(estimateItem)
            }
        }

    }

    private fun readNavArgs() = uiScope.launch(uiScope.coroutineContext) {

        editEstimateData?.let { navArgs ->

            if (!navArgs.jobId.isNullOrEmpty()) {
                estimatePhotoViewModel.setJobToEdit(navArgs.jobId.toString())
            }
            if (!navArgs.itemId.isNullOrEmpty()) {
                estimatePhotoViewModel.setCurrentProjectItem(navArgs.itemId.toString())
            }
            if (!navArgs.estimateId.isNullOrEmpty()) {
                estimateId = navArgs.estimateId
                estimateId?.let { realEstimateId ->
                    estimatePhotoViewModel.setEstimateToEdit(realEstimateId)
                }
            } else {
                estimatePhotoViewModel.currentEstimate = MutableLiveData()
            }
        }
    }

    private fun onUserFound(foundUserId: Int?): Int {
        if (foundUserId != null) {
            currentUser = foundUserId
        }
        return currentUser
    }

    private fun onJobFound(foundJobDTO: JobDTO?): JobDTO {
        newJob = foundJobDTO!!
        if (estimateId != null) {
            val estimateDTO = newJob?.jobItemEstimates?.find { itemEstimate ->
                itemEstimate.estimateId == estimateId
            }
            if (estimateDTO != null) {
                newJobItemEstimate = estimateDTO
            }
        }
        return newJob as JobDTO
    }

    private fun onItemFound(itemDTO: ItemDTOTemp?): ItemDTOTemp? {
        if (itemDTO != null) {
            item = itemDTO
            _ui?.titleTextView?.text = getString(R.string.pair, item!!.itemCode, item!!.descr)
            tenderRate = item!!.tenderRate
        } else {
            toast("item is null in " + javaClass.simpleName )
            return item
        }

        if (newJob != null) {
            if (newJobItemEstimate == null && item != null) {
                newJobItemEstimate = newJob?.getJobEstimateByItemId(item?.itemId)
                if (newJobItemEstimate != null) {
                    newJobItemEstimate?.lineRate = item?.tenderRate!!
                }
            }
            restoreEstimateViewState()
        }
        return item
    }

    private fun onEstimateFound(estimateDTO: JobItemEstimateDTO) {
        newJobItemEstimate = estimateDTO
        quantity = newJobItemEstimate?.qty ?: 1.0
        if (newJob != null) {
            restoreEstimateViewState()
        }
    }


    override fun onStart() {
        super.onStart()
        _ui?.group13loading?.visibility = View.GONE
        mAppExecutor = AppExecutor()
        lm = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager

        setButtonClicks()

        try {
            networkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (e: Exception) {
            Timber.e(e)
        }

        if (!gpsEnabled) { // notify user && !network_enabled
            displayPromptForEnablingGPS(requireActivity())
        }

        setValueEditText(getStoredValue())

        _ui?.valueEditText?.doOnTextChanged { text, _, _, _ ->
            try {
                setEstimateQty(text)
            } catch (ex: java.lang.NumberFormatException) {
                Timber.e(" ")
            }
        }
    }

    private fun setEstimateQty(text: CharSequence?) {
        try {
            val quantity = text.toString().toIntOrNull() ?: 0.0
            newJobItemEstimate?.qty = quantity.toDouble()
        } catch (ex: java.lang.NumberFormatException) {
            Timber.e(" ")
            quantity = 0.0
        } finally {
            estimatePhotoViewModel.setEstimateQuantity(quantity)
            changesToPreserve = true
            setCost()
        }
    }

    @SuppressLint("TimberArgCount")
    private fun setButtonClicks() {

        val myClickListener = View.OnClickListener { view ->
            when (view?.id) {
                R.id.startPhotoButton -> {
                    photoType = PhotoType.START
                    if (item != null) {
                        itemIdPhotoType["itemId"] = item!!.itemId
                        itemIdPhotoType["type"] = photoType.name
                    }
                    showImageProviderDialog(IMAGE_REQ_CODE, photoType)

                }

                R.id.endPhotoButton -> {
                    photoType = PhotoType.END
                    if (item != null) {
                        itemIdPhotoType["itemId"] = item!!.itemId
                        itemIdPhotoType["type"] = photoType.name
                    }
                    showImageProviderDialog(IMAGE_REQ_CODE, photoType)
                }


                R.id.cancelButton -> {
                    Coroutines.main {
                        buildDeleteDialog(this@EstimatePhotoFragment.requireView())
                    }
                }

                R.id.updateButton -> {
                    validateAndUpdateEstimate(view)
                }
            }
        }

        _ui?.startPhotoButton?.setOnClickListener(myClickListener)
        _ui?.endPhotoButton?.setOnClickListener(myClickListener)
        _ui?.cancelButton?.setOnClickListener(myClickListener)
        _ui?.updateButton?.setOnClickListener(myClickListener)

        // If the user hits the enter key on the costing field,
        // hide the keypad.

        _ui?.valueEditText?.setOnEditorActionListener { _, _, event ->
            if (event.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER) {
                requireActivity().hideKeyboard()
                true
            } else {
                false
            }
        }
    }

    private fun showImageProviderDialog(reqCode: Int, photoType: PhotoType) {
        DialogHelper.showChooseAppDialog(
            requireActivity(),
            object : ResultListener<ImageProvider> {
                override fun onResult(t: ImageProvider?) {
                    t?.let {
                        imageProvider = it
                        imageProviderInterceptor?.invoke(imageProvider)
                        if (it.equals(ImageProvider.CAMERA)) {
                            startMyActivity(CAMERA_INTENT_REQ_CODE, photoType)
                        } else {
                            startMyActivity(GALLERY_INTENT_REQ_CODE, photoType)
                        }

                    }
                }
            },
            dismissListener
        )
    }


    private fun startMyActivity(reqCode: Int, photoType: PhotoType) {

        when (imageProvider) {
            ImageProvider.GALLERY -> {
                launcher.launch(config)
            }

            ImageProvider.CAMERA -> {
                takePhoto(photoType)
                this@EstimatePhotoFragment.takingPhotos()
            }
            else -> {
                // Something went Wrong! This case should never happen
                Timber.e("Image provider can not be null")
                //setError(getString(R.string.error_task_cancelled))
            }
        }

    }


    private fun validateAndUpdateEstimate(view: View) {

        if (selectedJobType == JobItemEstimateSize.POINT.getValue()) {
            validateEstimateBySize(view, JobItemEstimateSize.POINT.getValue(), 1)
        } else if (selectedJobType == JobItemEstimateSize.LINE.getValue()) {
            validateEstimateBySize(view, JobItemEstimateSize.LINE.getValue(), 2)
        }
    }

    private fun validateEstimateBySize(view: View, jobSize: String, minimumPhotoCount: Int) {
        newJobItemEstimate?.jobItemEstimateSize = jobSize
        if (_ui?.costTextView?.text.isNullOrEmpty() || newJobItemEstimate!!.size() < minimumPhotoCount) {
            extensionToast(
                message = "Please capture at least $minimumPhotoCount photograph(s)...",
                style = ToastStyle.INFO,
                position = ToastGravity.BOTTOM
            )
            _ui?.labelTextView?.startAnimation(animations!!.shake_long)
        } else {
            saveCheckedEstimate(view)
        }
    }

    private fun saveCheckedEstimate(view: View) {
        Coroutines.main {
            estimatePhotoViewModel.estimateComplete(newJobItemEstimate!!).also { result ->
                if (result) {
                    calculateCost()
                    this@EstimatePhotoFragment.toggleLongRunning(true)
                    saveValidEstimate(view)
                }
            }
        }
    }

    private fun navToAddProject(view: View) {
        val directions = EstimatePhotoFragmentDirections.actionEstimatePhotoFragmentToNavigationAddItems(
            editEstimateData.itemId!!,
            editEstimateData.jobId!!,
            editEstimateData.contractVoId?:"")
        Navigation.findNavController(view).navigate(directions)
    }

    private suspend fun saveValidEstimate(view: View) = uiScope.launch(uiScope.coroutineContext) {
        setEstimateQty(_ui?.valueEditText?.text as CharSequence)

        if (newJobItemEstimate!!.qty > 0 && newJobItemEstimate!!.lineRate > 0.0 && changesToPreserve) {

            val saveValidEstimate = newJobItemEstimate!!.copy(
                qty = newJobItemEstimate!!.qty,
                lineRate = newJobItemEstimate!!.lineRate
            )
            Coroutines.io {
                estimatePhotoViewModel.backupProjectItem(item!!)
                estimatePhotoViewModel.setTempProjectItem(item!!)
                estimatePhotoViewModel.backupEstimate(saveValidEstimate)
                newJob!!.insertOrUpdateJobItemEstimate(saveValidEstimate)
                estimatePhotoViewModel.saveNewJob(newJob!!)
                withContext(Dispatchers.Main.immediate) {
                    changesToPreserve = false
                    estimatePhotoViewModel.setJobToEdit(newJob!!.jobId)
                    estimatePhotoViewModel.setEstimateQuantity(saveValidEstimate.qty)
                    estimatePhotoViewModel.setEstimateLineRate(saveValidEstimate.lineRate)
                    estimatePhotoViewModel.setEstimateToEdit(saveValidEstimate.estimateId)
                    updateData(view)
                }
            }
        }
    }

    private fun updateData(view: View) {
        this.toggleLongRunning(false)
        newJob?.let {
            navToAddProject(view)
        }
    }

    private fun takePhoto(picType: PhotoType) {
        photoType = picType
        initLaunchCamera()
    }

    private fun initLaunchCamera() {
        if (ContextCompat.checkSelfPermission(
                requireActivity().applicationContext,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                REQUEST_STORAGE_PERMISSION
            )
        } else {
            launchCamera()
        }
    }

    private fun displayPromptForEnablingGPS(
        activity: Activity
    ) {

        val builder = Builder(activity)
        builder.setCancelable(false)
        val action = Settings.ACTION_LOCATION_SOURCE_SETTINGS
        val message = ("Your GPS seems to be disabled, Please enable it to continue")
        builder.setMessage(message)
            .setPositiveButton("OK") { d, _ ->
                activity.startActivity(Intent(action))
                d.dismiss()
            }
        builder.create().show()
    }

    private fun launchCamera() {
        // type is "start" or "end"
        if (item != null) {
            itemIdPhotoType["itemId"] = item!!.itemId
            itemIdPhotoType["type"] = photoType.name
        }
        Coroutines.main {
            imageUri = photoUtil.getUri()
            withContext(Dispatchers.Main.immediate) {
                this@EstimatePhotoFragment.takingPhotos()
                takePicture.launch(imageUri)
            }
        }
    }

    private suspend fun restoreEstimatePhoto(
        // jobItemEstimate: JobItemEstimateDTO,
        photo: JobItemEstimatesPhotoDTO,
        animate: Boolean = false
    ) = Coroutines.main {

        var targetImageView = _ui?.startImageView
        var targetTextView = _ui?.startSectionTextView

        if (!photo.isPhotostart) {
            targetImageView = _ui?.endImageView
            targetTextView = _ui?.endSectionTextView
        }

        val targetUri = null ?: extractImageUri(photo)

        when (photo.isPhotostart) {
            true -> {
                startImageUri = targetUri
                photoType = PhotoType.START
            }
            else -> {
                endImageUri = targetUri
                photoType = PhotoType.END
            }
        }
        if (targetUri != null) {
            photo.sectionMarker?.let { mark ->
                targetTextView?.text = mark
            }
            loadEstimateItemPhoto(targetUri, targetImageView!!, animate)
        }
    }

    private suspend fun extractImageUri(
        jobItemEstimatePhoto: JobItemEstimatesPhotoDTO?
    ): Uri? = withContext(dispatchers.io()) {
        if (jobItemEstimatePhoto != null) {
            val path: String = jobItemEstimatePhoto.photoPath
            Timber.d("x -> photo $path")

            if (path.isNotBlank()) {
                return@withContext photoUtil.getUri(path)
            } else {
                return@withContext null
            }
        }
        return@withContext null
    }


    @SuppressLint("RestrictedApi")
    private suspend fun processAndSetImage(
        imageUri: Uri, provider: String

    ) = withContext(uiScope.coroutineContext) {

        if (provider == "CAMERA") {
            try { //  Location of picture
                val estimateLocation: LocationModel? = this@EstimatePhotoFragment.getCurrentLocation()
                Timber.d("x -> $estimateLocation")
                if (estimateLocation != null) {
                    val imageFileName = ""
                    //  Save Image to Internal Storage
                    withContext(dispatchers.io()) {
                        filenamePath = photoUtil.saveImageToInternalStorage(
                            imageUri
                        )!! // as HashMap<String, String>
                    }

                    processPhotoEstimate(
                        estimateLocation = estimateLocation,
                        filePath = filenamePath,
                        itemidPhototype = itemIdPhotoType,
                        imageFileName
                    )
                } else {
                    resetPhotos()
                    displayPromptForEnablingGPS(this@EstimatePhotoFragment.requireActivity())
                }
            } catch (e: Exception) {
                toast(R.string.error_getting_image)
                Timber.e(e)
                throw e
            }
        } else {
        /* *
         *  provider == "GALLERY"
         *  Location of picture
         */
       try {
           

                val estimatephotodata = estimatePhotoViewModel.getEstimatePhotoByName(images[0].name)
                if (estimatephotodata != null) {

                    val estimateLocation = LocationModel(
                        latitude = estimatephotodata.photoLatitude!!,
                        longitude = estimatephotodata.photoLongitude!!,
                        accuracy = 0F,
                        bearing = 0F
                    )
                    val imageFileName = estimatephotodata.filename!!
                    val path = estimatephotodata.photoPath
                    val map: HashMap<String, String> =
                        HashMap()
                    map["filename"] = imageFileName
                    map["path"] = path
                    filenamePath = map

                    //ToastUtils().toastShort(requireContext(), "$estimatephotodata")
                    processPhotoEstimate(
                        estimateLocation = estimateLocation,
                        filePath = filenamePath,
                        itemidPhototype = itemIdPhotoType,
                        imageFileName
                    )

                }else{
                    resetPhotos()
                    ToastUtils().toastShort(requireContext(), "Image Selected Has Already Been Used OR It Was Not Captured Using the RRM App")
                }

            } catch (e: Exception) {
                toast(R.string.error_getting_image)
                Timber.e(e)
                throw e
            }
        }

    }

    private suspend fun processPhotoEstimate(
        estimateLocation: LocationModel,
        filePath: Map<String, String>,
        itemidPhototype: Map<String, String>,
        imageFileName : String ) = withContext(uiScope.coroutineContext) {

        val itemId = item?.itemId ?: itemidPhototype["itemId"]

        if (newJobItemEstimate == null) {
            newJobItemEstimate = newJob?.getJobEstimateByItemId(itemId)
        }

        if (newJobItemEstimate == null) {
            newJobItemEstimate = estimatePhotoViewModel.createItemEstimate(
                itemId = itemId,
                newJob = newJob,
                item = item,
                estimateSize = selectedJobType
            )

            item = item!!.copy(estimateId = newJobItemEstimate!!.estimateId)

            if (newJob?.jobItemEstimates == null) {
                newJob?.jobItemEstimates = ArrayList()
            }

            newJob?.insertOrUpdateJobItemEstimate(newJobItemEstimate!!)

        }

        newJobItemEstimate?.let {
            uiScope.launch(context = uiScope.coroutineContext) {
                processPhotoLocation(estimateLocation, filePath, itemidPhototype, imageFileName)
            }
        }
    }

    private fun processPhotoLocation(
        estimateLocation: LocationModel,
        filePath: Map<String, String>,
        itemidPhototype: Map<String, String>,
        imageFileName: String
    ) {
        try {
            toggleLongRunning(true)
            if (!this@EstimatePhotoFragment.disableGlide) {
                placeProvisionalPhoto(
                    filePath,
                    estimateLocation,
                    itemidPhototype,imageFileName
                )
            }
        } catch (t: Throwable) {
            disableGlide = true
            val message = "Failed to capture photo: ${t.message ?: XIErrorHandler.UNKNOWN_ERROR}"
            Timber.e(t, message)
            val xiErr = XIResult.Error(t, message)
            crashGuard(
                throwable = xiErr,
                refreshAction = {
                    this.retryProcessPhotoLocation(
                        estimateLocation,
                        filePath,
                        itemidPhototype,imageFileName
                    )
                }
            )
        } finally {
            toggleLongRunning(false)
            resetPhotos()
        }
    }


    private fun retryProcessPhotoLocation(
        estimateLocation: LocationModel,
        filePath: Map<String, String>,
        itemidPhototype: Map<String, String>,
        imageFileName: String
    ) {
        IndefiniteSnackbar.hide()
        Coroutines.main {
            disableGlide = false
            processPhotoLocation(estimateLocation, filePath, itemidPhototype, imageFileName)
        }
    }


    private fun placeProvisionalPhoto(
        filePath: Map<String, String>,
        currentLocation: LocationModel,
        itemidPhototype: Map<String, String>,
        imageFileName: String
    ) = uiScope.launch(uiScope.coroutineContext) {


        val photo = checkIfPhotoExists(filePath, currentLocation, itemidPhototype,imageFileName)

        val savedPhoto = estimatePhotoViewModel.backupEstimatePhoto(photo)

        this@EstimatePhotoFragment.newJobItemEstimate!!.setJobItemEstimatePhoto(
            savedPhoto
        )
        estimatePhotoViewModel.backupEstimate(newJobItemEstimate!!)
        newJobItemEstimate = estimatePhotoViewModel.updateEstimatePhotos(
            newJobItemEstimate!!.estimateId,
            newJobItemEstimate!!.jobItemEstimatePhotos
        )
        newJob?.insertOrUpdateJobItemEstimate(newJobItemEstimate!!)

        this@EstimatePhotoFragment.disableGlide = false

        val targetAnimation: LottieAnimationView = when (photo.isPhotostart) {
            true -> _ui?.startAnimationView!!
            else -> _ui?.endAnimationView!!
        }

        targetAnimation.cancelAnimation()
        targetAnimation.visibility = View.GONE
        restoreEstimatePhoto(savedPhoto, true)
    }

    private suspend fun checkIfPhotoExists(
        filePath: Map<String, String>, currentLocation: LocationModel,
        itemidPhototype: Map<String, String>,
        imageFileName: String
    ) : JobItemEstimatesPhotoDTO {
        var photo : JobItemEstimatesPhotoDTO? = null
        val isPhotoStart = itemIdPhotoType["type"] == PhotoType.START.name
        val newpic = estimatePhotoViewModel.checkIfPhotoExists(imageFileName)
        if (newpic){
            if (PhotoType.START.name == "END"){
                photo = estimatePhotoViewModel.createItemEstimatePhoto2(
                    itemEst = newJobItemEstimate!!,
                    filePath = filePath,
                    currentLocation = currentLocation,
                    itemIdPhotoType = itemidPhototype,
                    pointLocation = -1.0
                )
            }else {
                val newpic2 = estimatePhotoViewModel.checkIfPhotoExistsByNameAndEstimateId(imageFileName,newJobItemEstimate?.estimateId!! )
                if (newpic2){
                    photo = estimatePhotoViewModel.createItemEstimatePhoto2(
                        itemEst = newJobItemEstimate!!,
                        filePath = filePath,
                        currentLocation = currentLocation,
                        itemIdPhotoType = itemidPhototype,
                        pointLocation = -1.0
                    )
                }else{
                    photo = estimatePhotoViewModel.getEstimatePhotoByName(imageFileName)
                    photo?.descr =  "" //" Used as ${ itemIdPhotoType["type"]} photo"
                    photo?.estimateId = newJobItemEstimate?.estimateId!!
                    photo?.isPhotostart = isPhotoStart

                    estimatePhotoViewModel.backupEstimatePhoto(photo!!)
                }

            }

        }else {
            photo = estimatePhotoViewModel.createItemEstimatePhoto(
                itemEst = newJobItemEstimate!!,
                filePath = filePath,
                currentLocation = currentLocation,
                itemIdPhotoType = itemidPhototype,
                pointLocation = -1.0
            )
        }
        return photo
    }


    private fun resetPhotos() {
        haltAnimation()
        _ui?.startImageView?.visibility = View.VISIBLE
        _ui?.endImageView?.visibility = View.VISIBLE
        disableGlide = false
    }


    private fun showZoomedImage(imageUrl: Uri?) {
        val dialog = Dialog(requireContext(), R.style.dialog_full_screen)
        dialog.setContentView(R.layout.new_job_photo)
        val zoomageView =
            dialog.findViewById<ZoomageView>(R.id.zoomedImage)
        GlideApp.with(this.requireActivity())
            .load(imageUrl)
            .into(zoomageView)
        dialog.show()
    }

    override fun onResume() {
        super.onResume()
        if (newJob == null) {
            readNavArgs()
        } else {
            setCost()
        }
    }

    private fun loadEstimateItemPhoto(
        imageUri: Uri?,
        imageView: ImageView,
        animate: Boolean
    ) = try {
        GlideApp.with(this)
            .load(imageUri)
            .centerCrop()
            .error(R.drawable.no_image)
            .into(imageView)

        if (animate) imageView.startAnimation(bounce_1000)

        imageView.setOnClickListener {
            showZoomedImage(imageUri)
        }
    } catch (e: Exception) {
        Timber.e(e)
    } finally {
        uiScope.launch(dispatchers.io()) {
            this@EstimatePhotoFragment.isEstimateDone =
                estimatePhotoViewModel.estimateComplete(newJobItemEstimate)
            withContext(dispatchers.ui()) {
                if (isEstimateDone) {
                    _ui?.costCard?.visibility = View.VISIBLE
                    _ui?.updateButton?.visibility = View.VISIBLE
                    setCost()
                } else {
                    hideCostCard()
                }
            }
        }
    }

    private fun setValueEditText(qty: Double) {
        when (item?.uom) {
            "M2", "M3", "M" -> {
                val decQty = "" + qty
                _ui?.valueEditText?.setText(decQty)
            }
            else -> {
                val intQty: String = "" + qty.toInt()
                _ui?.valueEditText?.setText(intQty)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setCost() {
        if (selectedJobType.equals("Point")) {
            if (newJobItemEstimate?.size() ?: 0 >= 1) {
                showCostCard()
                calculateCost()
            } else {
                incompleteEstimateNotice()
            }
        } else {
            if (newJobItemEstimate?.size() ?: 0 >= 2) {
                showCostCard()
                calculateCost()
            } else {
                incompleteEstimateNotice()
            }
        }
    }

    private fun incompleteEstimateNotice() {
        _ui?.labelTextView?.text = getString(R.string.warning_estimate_incomplete)
        _ui?.labelTextView?.startAnimation(animations!!.shake_long)
        _ui?.valueEditText?.visibility = View.GONE
        _ui?.costTextView?.visibility = View.GONE
    }

    private fun showCostCard() {
        _ui?.valueEditText?.visibility = View.VISIBLE
        _ui?.costTextView?.visibility = View.VISIBLE
        _ui?.costTextView?.startAnimation(animations!!.bounce_soft)
        _ui?.labelTextView?.text = getString(R.string.quantity)
    }

    private fun haltAnimation() {
        _ui?.startAnimationView?.visibility = View.GONE
        _ui?.endAnimationView?.visibility = View.GONE
    }

    private fun hideCostCard() {
        _ui?.costCard?.visibility = View.GONE
        _ui?.updateButton?.visibility = View.GONE
    }

    @SuppressLint("SetTextI18n")
    private fun calculateCost() {
        val item: ItemDTOTemp? = item

        val value = _ui?.valueEditText?.text.toString()
        //  Lose focus on fields
        //  valueEditText.clearFocus()

        var lineAmount: Double?
        val tenderRate = item?.tenderRate ?: newJobItemEstimate?.lineRate

        val qty = value.toDoubleOrNull() ?: 0.0

        when (item?.uom) {
            "NO", "QTY", null -> {
                lineAmount = validateNumberCosting(qty, tenderRate)
            }
            "M2" -> {
                lineAmount = validateAreaCosting(qty, tenderRate)
            }
            "M3" -> {
                lineAmount = validateVolumeCosting(qty, tenderRate)
            }
            "PROV SUM" -> {
                lineAmount = validateProvSumCosting(qty, tenderRate)
            }
            "M" -> {
                lineAmount = validateLengthCosting(qty = qty, tenderRate = tenderRate)
            }
            else -> {
                _ui?.labelTextView?.text = getString(R.string.label_quantity)
                try { //  Default Calculation
                    lineAmount = qty * tenderRate!!
                } catch (e: NumberFormatException) {
                    lineAmount = null
                    requireActivity().hideKeyboard()
                    Timber.d(e)
                    toast("Please enter the Quantity.")
                }
            }
        }

        val displayAmount = lineAmount ?: tenderRate!! * qty

        when (displayAmount >= 0.0) {
            true -> {
                // Display pricing information
                _ui?.costTextView?.text =
                    (" * R $tenderRate =  R ${DecimalFormat("#0.00").format(displayAmount)}")
                newJobItemEstimate?.qty = qty
                newJobItemEstimate?.lineRate = tenderRate!!
                estimatePhotoViewModel.setEstimateQuantity(qty)
                estimatePhotoViewModel.setEstimateLineRate(tenderRate)
                changesToPreserve = true
            }
            else -> {
                incompleteEstimateNotice()
            }
        }
    }

    private fun validateLengthCosting(
        qty: Double,
        tenderRate: Double?
    ): Double? {
        var inLineAmount: Double? = null
        when (_ui?.labelTextView?.text) {
            getString(R.string.label_length_m) ->
                try { //  Set the Area to the QTY
                    inLineAmount = qty * tenderRate!!
                } catch (e: NumberFormatException) {
                    requireActivity().hideKeyboard()
                    Timber.d(e)
                    toast("Please enter the m.")
                }
        }
        return inLineAmount
    }

    private fun validateProvSumCosting(
        qty: Double,
        tenderRate: Double?
    ): Double? {
        var inlineAmount: Double?
        _ui?.labelTextView?.text = getString(R.string.label_amount)
        try {
            inlineAmount = qty * tenderRate!!
        } catch (e: NumberFormatException) {
            inlineAmount = null
            requireActivity().hideKeyboard()
            toast(getString(R.string.warning_estimate_enter_prov_sum))
        }
        return inlineAmount
    }

    private fun validateVolumeCosting(
        qty: Double,
        tenderRate: Double?
    ): Double? {
        var inlineAmount: Double?
        _ui?.labelTextView?.text = getString(R.string.label_volume_m3)
        try {
            inlineAmount = qty * tenderRate!!
        } catch (e: NumberFormatException) {
            requireActivity().hideKeyboard()
            inlineAmount = null
            toast(getString(R.string.warning_estimate_enter_volume))
        }
        return inlineAmount
    }

    private fun validateAreaCosting(
        qty: Double,
        tenderRate: Double?
    ): Double? {
        var inLineAmount: Double?
        _ui?.labelTextView?.text = getString(R.string.label_area_m2)
        try {
            inLineAmount = qty * tenderRate!!
        } catch (e: NumberFormatException) {
            inLineAmount = null
            requireActivity().hideKeyboard()
            toast("Please place the Area.")
        }
        return inLineAmount
    }

    private fun validateNumberCosting(
        qty: Double,
        tenderRate: Double?
    ): Double? {
        var inLineAmount: Double?
        _ui?.labelTextView?.text = getString(R.string.label_quantity)
        try { //  make the change in the array and update view
            inLineAmount = qty * tenderRate!!
        } catch (e: NumberFormatException) {
            inLineAmount = null
            requireActivity().hideKeyboard()
            toast("Please place the Quantity.")
        }
        return inLineAmount
    }

    private fun getStoredValue(): Double {
        val jobItemEstimate: JobItemEstimateDTO? = getJobItemEstimate()
        return jobItemEstimate?.qty ?: quantity
    }

    private fun getJobItemEstimate(): JobItemEstimateDTO? {
        return newJobItemEstimate
    }

    private fun getCurrentLocation(): LocationModel? {
        return super.getLocation()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.run {
            outState.putString("jobId", newJob?.jobId)
            outState.putString("itemId", item?.itemId)
            newJobItemEstimate?.let { realEstimate ->
                outState.putString("estimateId", realEstimate.estimateId)
            }
        }
        super.onSaveInstanceState(outState)
        Timber.i("$outState")
    }

    private fun onRestoreInstanceState(inState: Bundle?) {

        val jobId = inState?.getString("jobId")
        estimateId = inState?.getString("estimateId")
        val itemId = inState?.getString("itemId")
        val locationErrorMessage = inState?.getString("locationErrorMessage")
        Coroutines.io {
            if (jobId != null) {
                estimatePhotoViewModel.setJobToEdit(jobId)
            }
            if (itemId != null) {
                estimatePhotoViewModel.setCurrentProjectItem(itemId)
            }
            if (estimateId != null) {
                estimatePhotoViewModel.setEstimateToEdit(estimateId!!)
            }
        }

        if (!locationErrorMessage.isNullOrBlank()) {
            val locationErrorToast = ColorToast(
                title = "Location Error",
                message = locationErrorMessage,
                style = ToastStyle.ERROR,
                gravity = ToastGravity.CENTER,
                duration = ToastDuration.LONG
            )
            this@EstimatePhotoFragment.extensionToast(locationErrorToast)
        }
    }

    private fun restoreEstimateViewState() {

        sectionId = newJob?.sectionId
        if (sectionId != null) {
            estimatePhotoViewModel.setSectionId(sectionId!!)
        }
        // Load Photographs
        if (newJobItemEstimate != null) {
            uiScope.launch(uiScope.coroutineContext) {
                try {
                    setJobType(newJobItemEstimate?.jobItemEstimateSize!!)

                    quantity = newJobItemEstimate!!.qty
                    estimatePhotoViewModel.setEstimateQuantity(quantity)
                    newJobItemEstimate?.jobItemEstimatePhotos?.forEach { photo ->
                        restoreEstimatePhoto(
                            photo
                        )
                    }

                    isEstimateDone = estimatePhotoViewModel.estimateComplete(newJobItemEstimate)

                    if (isEstimateDone) {
                        _ui?.costCard?.visibility = View.VISIBLE
                        _ui?.updateButton?.visibility = View.VISIBLE
                        setValueEditText(quantity)
                        setCost()
                    }
                } catch (t: Throwable) {
                    Timber.e(t, "Failed to restore estimate view-state.")
                    val estError = XIResult.Error(t, t.message ?: XIErrorHandler.UNKNOWN_ERROR)
                    crashGuard(
                        throwable = estError,
                        refreshAction = { this@EstimatePhotoFragment.retryEstimateViewState() }
                    )
                }
            }
        }
    }

    private fun retryEstimateViewState() {
        IndefiniteSnackbar.hide()
        restoreEstimateViewState()
    }

    /**
     * Called when the view previously created by [.onCreateView] has
     * been detached from the fragment.  The next time the fragment needs
     * to be displayed, a new view will be created.  This is called
     * after [.onStop] and before [.onDestroy].  It is called
     * *regardless* of whether [.onCreateView] returned a
     * non-null view.  Internally it is called after the view's state has
     * been saved but before it has been removed from its parent.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        estimatePhotoViewModel.itemJob.removeObservers(viewLifecycleOwner)
        estimatePhotoViewModel.tempProjectItem.removeObservers(viewLifecycleOwner)
        estimatePhotoViewModel.currentEstimate.removeObservers(viewLifecycleOwner)
        estimatePhotoViewModel.loggedUser.removeObservers(viewLifecycleOwner)
        // make sure to stop the trip session. In this case it is being called inside `onDestroy`.
        mapboxNavigation.stopTripSession()
        // make sure to unregister the observer you have registered.
        mapboxNavigation.unregisterLocationObserver(locationObserver)
        mapboxNavigation.onDestroy()
        _ui = null
    }

    private suspend fun buildDeleteDialog(view: View) {
        val itemDeleteBuilder =
            Builder(
                view.context // , android.R.style
                // .Theme_DeviceDefault_Dialog
            )
        itemDeleteBuilder.setTitle(R.string.confirm)
        itemDeleteBuilder.setIcon(R.drawable.ic_warning)
        itemDeleteBuilder.setMessage("Delete this project item?")
        // Yes button
        itemDeleteBuilder.setPositiveButton(
            R.string.yes
        ) { _, _ ->
            Coroutines.main {
                item?.let {
                    extensionToast(
                        title = "Deleting ...",
                        message = "${it.descr} removed.",
                        style = ToastStyle.DELETE,
                        position = ToastGravity.BOTTOM,
                        duration = ToastDuration.LONG
                    )

                    Coroutines.io {
                        estimatePhotoViewModel.deleteItemFromList(it.itemId, it.estimateId)
                        newJob?.removeJobEstimateByItemId(it.itemId)
                        estimatePhotoViewModel.backupJob(newJob!!)
                        estimatePhotoViewModel.setJobToEdit(newJob?.jobId!!)
                        changesToPreserve = false
                        withContext(Dispatchers.Main.immediate) {
                            navToAddProject(view)
                        }
                    }
                }
            }
        }
        // No button
        itemDeleteBuilder.setNegativeButton(
            R.string.no
        ) { dialog, _ ->
            // Do nothing but close dialog
            dialog.dismiss()
        }
        val deleteAlert = itemDeleteBuilder.create()
        deleteAlert.show()
    }

    override fun onStop() {
        super.onStop()

        Coroutines.main {
            if (changesToPreserve) {
                item?.let {
                    estimatePhotoViewModel.backupProjectItem(it)
                }
                newJobItemEstimate?.let {
                    estimatePhotoViewModel.backupEstimate(it)
                    estimatePhotoViewModel.updateEstimatePhotos(it.estimateId, it.jobItemEstimatePhotos)
                    newJob?.insertOrUpdateJobItemEstimate(it)
                    estimatePhotoViewModel.setEstimateToEdit(it.estimateId)
                }
                newJob?.let {
                    estimatePhotoViewModel.backupJob(it)
                    estimatePhotoViewModel.setJobToEdit(it.jobId)
                }

                changesToPreserve = false
            }
        }
    }
}
