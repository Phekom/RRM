/**
 * Updated by Shaun McDonald on 2021/05/18
 * Last modified on 2021/05/18, 10:26
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 **/

package za.co.xisystems.itis_rrm.ui.mainview.create.edit_estimates

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog.Builder
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RadioButton
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenCreated
import androidx.lifecycle.whenResumed
import androidx.lifecycle.whenStarted
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.airbnb.lottie.LottieAnimationView
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.trip.session.LocationMatcherResult
import com.mapbox.navigation.core.trip.session.LocationObserver
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.android.x.closestDI
import org.kodein.di.instance
import timber.log.Timber
import za.co.xisystems.itis_rrm.BuildConfig
import za.co.xisystems.itis_rrm.MainActivity
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.base.LocationFragment
import za.co.xisystems.itis_rrm.custom.errors.XIErrorHandler
import za.co.xisystems.itis_rrm.custom.notifications.ColorToast
import za.co.xisystems.itis_rrm.custom.notifications.ToastDuration
import za.co.xisystems.itis_rrm.custom.notifications.ToastGravity
import za.co.xisystems.itis_rrm.custom.notifications.ToastStyle
import za.co.xisystems.itis_rrm.custom.results.XIResult
import za.co.xisystems.itis_rrm.custom.views.IndefiniteSnackbar
import za.co.xisystems.itis_rrm.data.localDB.entities.ItemDTOTemp
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimateDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimatesPhotoDTO
import za.co.xisystems.itis_rrm.databinding.FragmentPhotoEstimateBinding
import za.co.xisystems.itis_rrm.services.LocationModel
import za.co.xisystems.itis_rrm.ui.extensions.crashGuard
import za.co.xisystems.itis_rrm.ui.extensions.extensionToast
import za.co.xisystems.itis_rrm.ui.mainview.create.CreateViewModel
import za.co.xisystems.itis_rrm.ui.mainview.create.CreateViewModelFactory
import za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils.models.PhotoType
import za.co.xisystems.itis_rrm.ui.scopes.UiLifecycleScope
import za.co.xisystems.itis_rrm.utils.Coroutines
import za.co.xisystems.itis_rrm.utils.GlideApp
import za.co.xisystems.itis_rrm.utils.JobItemEstimateSize
import za.co.xisystems.itis_rrm.utils.PhotoUtil
import za.co.xisystems.itis_rrm.utils.zoomage.ZoomageView
import java.io.File
import java.text.DecimalFormat
import kotlin.collections.set

/**
 * Created by Francis Mahlava on 2019/12/29.
 * Updated by Francis Mahlava on 2021/11/23
 */

class EstimatePhotoFragment : LocationFragment() {

    private var selectedJobType: String? = null
    private var sectionId: String? = null
    override val di by closestDI()
    private lateinit var createViewModel: CreateViewModel
    private val factory: CreateViewModelFactory by instance()
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
    private val uiScope = UiLifecycleScope()
    private val photoUtil: PhotoUtil by instance()
    private var changesToPreserve: Boolean = false
    private var tenderRate: Double? = null
    private val navigationLocationProvider = NavigationLocationProvider()

    private val locationObserver = object : LocationObserver {
        override fun onNewRawLocation(rawLocation: Location) {
            // You're going to need this when you
            // aren't driving.
            navigationLocationProvider.changePosition(rawLocation)
        }

        override fun onNewLocationMatcherResult(locationMatcherResult: LocationMatcherResult) {
            val enhancedLocation = locationMatcherResult.enhancedLocation
            navigationLocationProvider.changePosition(
                enhancedLocation,
                locationMatcherResult.keyPoints,
            )

            updateCamera(enhancedLocation)
        }
    }

    private lateinit var mapboxMap: MapboxMap
    private lateinit var mapboxNavigation: MapboxNavigation
    private var _binding: FragmentPhotoEstimateBinding? = null
    private val binding get() = _binding!!

    /**
     * ActivityResultContract for taking a photograph
     */
    private val takePicture = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isSaved ->
        if (isSaved) {
            uiScope.launch(dispatchers.io()) {
                imageUri?.let { realUri ->
                    processAndSetImage(realUri)
                }
            }
        } else {
            imageUri?.let { failedUri ->
                uiScope.launch(dispatchers.io()) {
                    val filenamePath = File(failedUri.path!!)
                    photoUtil.deleteImageFile(filenamePath.toString())
                    withContext(Dispatchers.Main.immediate) {
                        haltAnimation()
                        binding.startImageView.visibility = View.VISIBLE
                        binding.endImageView.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    init {
        System.setProperty("kotlinx.coroutines.debug", if (BuildConfig.DEBUG) "on" else "off")
        lifecycleScope.launch {

            whenCreated {
                uiScope.onCreate()
                createViewModel =
                    ViewModelProvider(
                        this@EstimatePhotoFragment.requireActivity(),
                        factory
                    )[CreateViewModel::class.java]
            }

            whenStarted {
                viewLifecycleOwner.lifecycle.addObserver(uiScope)
            }

            whenResumed {
                if (newJob == null) readNavArgs()
            }
        }
    }

    companion object {
        private const val REQUEST_STORAGE_PERMISSION = 505
        private const val IMAGE_REQ_CODE = 101
        private const val GEOJSON_SOURCE_ID = "GEOJSON_SOURCE_ID"
        private const val ICON_LAYER_ID = "ICON_LAYER_ID"
        private const val ICON_SOURCE_ID = "ICON_SOURCE_ID"
        private const val ROUTE_LAYER_ID = "ROUTE_LAYER_ID"
        private const val RED_PIN_ICON_ID = "RED_PIN_ICON_ID"
        private const val DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L
        private const val DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPhotoEstimateBinding.inflate(inflater, container, false)

        return binding.root
    }

    private fun setJobType(jobType: String) {
        binding.apply {
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
                        binding.secondImage.visibility = View.GONE
                        binding.startPhotoButton.visibility = View.VISIBLE
                        binding.startPhotoButton.text = getString(R.string.capture_photo)
                    }
                    selectedJobType!!.contains("Line") -> {
                        binding.secondImage.visibility = View.VISIBLE
                        binding.startPhotoButton.visibility = View.VISIBLE
                        binding.startPhotoButton.text = getString(R.string.capture_start)
                    }
                    else -> {
                        binding.secondImage.visibility = View.GONE
                        binding.startPhotoButton.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun init() {
        initStyle()
        initNavigation()
    }

    private fun initStyle() {
        mapboxMap.loadStyleUri(Style.MAPBOX_STREETS)
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
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
            }
            startTripSession()
            registerLocationObserver(locationObserver)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (activity as MainActivity).supportActionBar?.title = getString(R.string.edit_estimate)
        locationWarning = false

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
        setHasOptionsMenu(true)

        (activity as MainActivity).supportActionBar?.title = getString(R.string.edit_estimate)
        newJobItemEstimatesList = ArrayList()
        newJobItemEstimatesPhotosList = ArrayList()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var stateRestored = false
        val args: EstimatePhotoFragmentArgs by navArgs()
        if (args.jobId?.isNotBlank() == true) {
            readNavArgs()
            stateRestored = true
        }

        if (savedInstanceState != null && !stateRestored) {
            onRestoreInstanceState(savedInstanceState)
        } else {
            setJobType("")
        }

        mapboxMap = binding.estimatemapview.getMapboxMap()
        binding.estimatemapview.location.apply {
            setLocationProvider(navigationLocationProvider)
            enabled = true
        }
        binding.secondImage.visibility = View.GONE
        binding.startPhotoButton.visibility = View.GONE

        init()

        pullData()
    }

    @Suppress("MagicNumber")
    private fun updateCamera(location: Location) {
        val mapAnimationOptions = MapAnimationOptions.Builder().duration(1500L).build()
        binding.estimatemapview.camera.easeTo(
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
    }

    private fun pullData() = uiScope.launch(uiScope.coroutineContext) {

        withContext(uiScope.coroutineContext) {
            var myUserId: Int?
            createViewModel.loggedUser.distinctUntilChanged().observe(
                viewLifecycleOwner,
                { user ->
                    user?.let {
                        myUserId = it
                        onUserFound(myUserId)
                    }
                }
            )
        }

        withContext(uiScope.coroutineContext) {
            var myJobDTO: JobDTO?
            createViewModel.itemJob.distinctUntilChanged().observe(
                viewLifecycleOwner,
                { jobEvent ->
                    jobEvent.getContentIfNotHandled()?.let { jobDTO ->
                        myJobDTO = jobDTO
                        onJobFound(myJobDTO!!)
                    }
                }
            )
        }

        withContext(uiScope.coroutineContext) {
            createViewModel.tempProjectItem.distinctUntilChanged().observe(
                viewLifecycleOwner,
                { itemEvent ->
                    itemEvent.getContentIfNotHandled()?.let { item ->
                        onItemFound(item)
                    }
                }
            )
        }

        withContext(uiScope.coroutineContext) {
            createViewModel.currentEstimate.distinctUntilChanged().observe(
                viewLifecycleOwner,
                { estimateEvent ->
                    estimateEvent.getContentIfNotHandled()?.let { estimateItem ->
                        onEstimateFound(estimateItem)
                    }
                }
            )
        }
    }

    private fun readNavArgs() = uiScope.launch(uiScope.coroutineContext) {
        val args: EstimatePhotoFragmentArgs? by navArgs()
        args?.let { navArgs ->

            if (!navArgs.jobId.isNullOrEmpty()) {
                createViewModel.setJobToEdit(navArgs.jobId.toString())
            }
            if (!navArgs.itemId.isNullOrEmpty()) {
                createViewModel.setCurrentProjectItem(navArgs.itemId.toString())
            }
            if (!navArgs.estimateId.isNullOrEmpty()) {
                estimateId = navArgs.estimateId
                estimateId?.let { realEstimateId ->
                    createViewModel.setEstimateToEdit(realEstimateId)
                }
            } else {
                createViewModel.currentEstimate = MutableLiveData()
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
            binding.titleTextView.text =
                getString(R.string.pair, item!!.itemCode, item!!.descr)
            tenderRate = item!!.tenderRate
        } else {
            toast(
                "item is null in " + javaClass.simpleName
            )
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // no options menu
        return false
        // To change body of created functions use File | Settings | File Templates.
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        val item = menu.findItem(R.id.action_settings)
        val item1 = menu.findItem(R.id.action_logout)
        val item2 = menu.findItem(R.id.action_search)
        if (item != null) item.isVisible = false
        if (item1 != null) item1.isVisible = false
        if (item2 != null) item2.isVisible = false
    }

    override fun onStart() {
        super.onStart()
        binding.group13Loading.visibility = View.GONE
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

        binding.valueEditText.doOnTextChanged { text, _, _, _ ->
            try {
                setEstimateQty(text)
            } catch (ex: java.lang.NumberFormatException) {
                Timber.e(" ")
            }
        }
    }

    private fun setEstimateQty(text: CharSequence?) {
        try {
            val quantity = text.toString().toDoubleOrNull() ?: 0.0
            newJobItemEstimate?.qty = quantity
        } catch (ex: java.lang.NumberFormatException) {
            Timber.e(" ")
            quantity = 0.0
        } finally {
            createViewModel.setEstimateQuantity(quantity)
            changesToPreserve = true
            setCost()
        }
    }

    @SuppressLint("TimberArgCount")
    private fun setButtonClicks() {

        val myClickListener = View.OnClickListener { view ->
            when (view?.id) {

//                 R.id.startPhotoButton -> {
//                     photoType = PhotoType.START
//                      if (item != null) {
//                          itemIdPhotoType["itemId"] = item!!.itemId
//                          itemIdPhotoType["type"] = photoType.name
//                      }
//
//                     ImagePicker.with(this)
//                         .saveDir(photoUtil.pictureFolder)
// //                        .cropSquare()
//                         .setImageProviderInterceptor { imageProvider -> // Intercept ImageProvider
//                             Timber.d("ImagePicker", "Selected ImageProvider: + ${ imageProvider.name} ")
//                         }
//                         .setDismissListener {
//                             Timber.d("Dialog Dismiss")
//                         }
//                         // Image resolution will be less than 512 x 512
//                         .maxResultSize(200, 200)
//                         .start(IMAGE_REQ_CODE)
//
//                     // binding.startPhotoButton.visibility = View.GONE
//                     // binding.originStart.visibility = View.VISIBLE
//                 }
//
//                 R.id.endPhotoButton -> {
//                     photoType = PhotoType.END
//                     if (item != null) {
//                         itemIdPhotoType["itemId"] = item!!.itemId
//                         itemIdPhotoType["type"] = photoType.name
//                     }
//
//                     ImagePicker.with(this)
//                         .saveDir(photoUtil.pictureFolder)
// //                        .cropSquare()
//                         .setImageProviderInterceptor { imageProvider -> // Intercept ImageProvider
//                             Timber.d("ImagePicker", "Selected ImageProvider: + ${ imageProvider.name} ")
//                         }
//                         .setDismissListener {
//                             Timber.d("Dialog Dismiss")
//                         }
//                         // Image resolution will be less than 512 x 512
//                         .maxResultSize(200, 200)
//                         .start(IMAGE_REQ_CODE)
//
//                     // binding.endPhotoButton.visibility = View.GONE
//                     //  binding.originEnd.visibility = View.VISIBLE
//                 }

                R.id.startPhotoButton -> {
                    locationWarning = false
                    binding.startImageView.visibility = View.GONE
                    binding.startAnimationView.visibility = View.VISIBLE
                    if (item != null) {
                        itemIdPhotoType["itemId"] = item!!.itemId
                        itemIdPhotoType["type"] = photoType.name
                    }
                    takePhoto(PhotoType.START)
                    this@EstimatePhotoFragment.takingPhotos()
                }

                R.id.endPhotoButton -> {
                    locationWarning = false
                    binding.endImageView.visibility = View.GONE
                    binding.endAnimationView.visibility = View.VISIBLE
                    if (item != null) {
                        itemIdPhotoType["itemId"] = item!!.itemId
                        itemIdPhotoType["type"] = photoType.name
                    }
                    takePhoto(PhotoType.END)
                    this@EstimatePhotoFragment.takingPhotos()
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

        binding.startPhotoButton.setOnClickListener(myClickListener)
        binding.endPhotoButton.setOnClickListener(myClickListener)
        binding.cancelButton.setOnClickListener(myClickListener)
        binding.updateButton.setOnClickListener(myClickListener)

        // If the user hits the enter key on the costing field,
        // hide the keypad.

        binding.valueEditText.setOnEditorActionListener { _, _, event ->
            if (event.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER) {
                requireActivity().hideKeyboard()
                true
            } else {
                false
            }
        }
    }

    // override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    //     super.onActivityResult(requestCode, resultCode, data)
    //     when (resultCode) {
    //         Activity.RESULT_OK -> {
    //
    //             // Uri object will not be null for RESULT_OK
    //             val uri: Uri = data?.data!!
    //             when (requestCode) {
    //                 IMAGE_REQ_CODE -> {
    //                     uiScope.launch(dispatchers.io()) {
    //                         processAndSetImage(uri)
    //                     }
    //                 }
    //             }
    //         }
    //         ImagePicker.RESULT_ERROR -> {
    //             ToastUtils().toastShort(requireContext(), ImagePicker.getError(data))
    //         }
    //         else -> {
    //             ToastUtils().toastShort(requireContext(), "Task Cancelled")
    //         }
    //     }
    // }

    private fun validateAndUpdateEstimate(view: View) {

        if (selectedJobType == JobItemEstimateSize.POINT.getValue()) {
            newJobItemEstimate?.jobItemEstimateSize = JobItemEstimateSize.POINT.getValue()

            if (binding.costTextView.text.isNullOrEmpty() || newJobItemEstimate!!.size() < 1) {
                extensionToast(
                    message = "Please capture or add at least one photo ...",
                    style = ToastStyle.INFO,
                    position = ToastGravity.BOTTOM
                )
                binding.labelTextView.startAnimation(animations!!.shake_long)
            } else {
                Coroutines.main {
                    createViewModel.estimateComplete(newJobItemEstimate!!).also { result ->
                        if (result) {
                            calculateCost()
                            this@EstimatePhotoFragment.toggleLongRunning(true)
                            saveValidEstimate(view)
                        }
                    }
                }
            }
        } else if (selectedJobType == JobItemEstimateSize.LINE.getValue()) {
            newJobItemEstimate?.jobItemEstimateSize = JobItemEstimateSize.LINE.getValue()
            if (binding.costTextView.text.isNullOrEmpty() || newJobItemEstimate!!.size() < 2) {
                extensionToast(
                    message = "Please capture or add the start and end photographs ...",
                    style = ToastStyle.INFO,
                    position = ToastGravity.BOTTOM
                )
                binding.labelTextView.startAnimation(animations!!.shake_long)
            } else {
                Coroutines.main {
                    createViewModel.estimateComplete(newJobItemEstimate!!).also { result ->
                        if (result) {
                            calculateCost()
                            this@EstimatePhotoFragment.toggleLongRunning(true)
                            saveValidEstimate(view)
                        }
                    }
                }
            }
        }
    }

    private fun navToAddProject(view: View) {
        val directions = EstimatePhotoFragmentDirections.actionEstimatePhotoFragmentToAddProjectFragment2(
            newJob?.projectId!!,
            newJob?.jobId!!
        )
        Navigation.findNavController(view).navigate(directions)
    }

    private suspend fun saveValidEstimate(view: View) = uiScope.launch(uiScope.coroutineContext) {
        setEstimateQty(binding.valueEditText.text as CharSequence)

        if (newJobItemEstimate!!.qty > 0 && newJobItemEstimate!!.lineRate > 0.0 && changesToPreserve) {

            val saveValidEstimate = newJobItemEstimate!!.copy(
                qty = newJobItemEstimate!!.qty,
                lineRate = newJobItemEstimate!!.lineRate
            )
            Coroutines.io {
                createViewModel.backupProjectItem(item!!)
                createViewModel.setTempProjectItem(item!!)
                createViewModel.backupEstimate(saveValidEstimate)
                newJob!!.insertOrUpdateJobItemEstimate(saveValidEstimate)
                createViewModel.saveNewJob(newJob!!)
                withContext(Dispatchers.Main.immediate) {
                    changesToPreserve = false
                    createViewModel.setJobToEdit(newJob!!.jobId)
                    createViewModel.setEstimateQuantity(saveValidEstimate.qty)
                    createViewModel.setEstimateLineRate(saveValidEstimate.lineRate)
                    createViewModel.setEstimateToEdit(saveValidEstimate.estimateId)
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

        var targetImageView = binding.startImageView
        var targetTextView = binding.startSectionTextView

        if (!photo.isPhotostart) {
            targetImageView = binding.endImageView
            targetTextView = binding.endSectionTextView
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
                targetTextView.text = mark
            }
            loadEstimateItemPhoto(targetUri, targetImageView, animate)
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
        imageUri: Uri
    ) = uiScope.launch(uiScope.coroutineContext) {
        try {
            // Location of picture
            val estimateLocation: LocationModel? = this@EstimatePhotoFragment.getCurrentLocation()
            Timber.d("x -> $estimateLocation")
            if (estimateLocation != null) {

                //  Save Image to Internal Storage
                withContext(dispatchers.io()) {
                    filenamePath = photoUtil.saveImageToInternalStorage(
                        imageUri
                    )!! // as HashMap<String, String>
                }

                processPhotoEstimate(
                    estimateLocation = estimateLocation,
                    filePath = filenamePath,
                    itemidPhototype = itemIdPhotoType
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
    }

    private suspend fun processPhotoEstimate(
        estimateLocation: LocationModel,
        filePath: Map<String, String>,
        itemidPhototype: Map<String, String>
    ) = withContext(uiScope.coroutineContext) {

        val itemId = item?.itemId ?: itemidPhototype["itemId"]

        if (newJobItemEstimate == null) {
            newJobItemEstimate = newJob?.getJobEstimateByItemId(itemId)
        }

        if (newJobItemEstimate == null) {
            newJobItemEstimate = createViewModel.createItemEstimate(
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
                processPhotoLocation(estimateLocation, filePath, itemidPhototype)
            }
        }
    }

    private fun processPhotoLocation(
        estimateLocation: LocationModel,
        filePath: Map<String, String>,
        itemidPhototype: Map<String, String>
    ) {
        try {
            toggleLongRunning(true)
            if (!this@EstimatePhotoFragment.disableGlide) {
                placeProvisionalPhoto(
                    filePath,
                    estimateLocation,
                    itemidPhototype
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
                        itemidPhototype
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
        itemidPhototype: Map<String, String>
    ) {
        IndefiniteSnackbar.hide()
        Coroutines.main {
            disableGlide = false
            processPhotoLocation(estimateLocation, filePath, itemidPhototype)
        }
    }

    private fun resetPhotos() {
        haltAnimation()
        binding.startImageView.visibility = View.VISIBLE
        binding.endImageView.visibility = View.VISIBLE
        disableGlide = false
    }

    private fun placeProvisionalPhoto(
        filePath: Map<String, String>,
        currentLocation: LocationModel,
        itemidPhototype: Map<String, String>
    ) = uiScope.launch(uiScope.coroutineContext) {
        val photo = createViewModel.createItemEstimatePhoto(
            itemEst = newJobItemEstimate!!,
            filePath = filePath,
            currentLocation = currentLocation,
            itemIdPhotoType = itemidPhototype,
            pointLocation = -1.0
        )

        val savedPhoto = createViewModel.backupEstimatePhoto(photo)

        this@EstimatePhotoFragment.newJobItemEstimate!!.setJobItemEstimatePhoto(
            savedPhoto
        )
        createViewModel.backupEstimate(newJobItemEstimate!!)
        newJobItemEstimate = createViewModel.updateEstimatePhotos(
            newJobItemEstimate!!.estimateId,
            newJobItemEstimate!!.jobItemEstimatePhotos
        )
        newJob?.insertOrUpdateJobItemEstimate(newJobItemEstimate!!)

        this@EstimatePhotoFragment.disableGlide = false

        val targetAnimation: LottieAnimationView = when (photo.isPhotostart) {
            true -> binding.startAnimationView
            else -> binding.endAnimationView
        }

        targetAnimation.cancelAnimation()
        targetAnimation.visibility = View.GONE
        restoreEstimatePhoto(savedPhoto, true)
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
                createViewModel.estimateComplete(newJobItemEstimate)
            withContext(dispatchers.ui()) {
                if (isEstimateDone) {
                    binding.costCard.visibility = View.VISIBLE
                    binding.updateButton.visibility = View.VISIBLE
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
                binding.valueEditText.setText(decQty)
            }
            else -> {
                val intQty: String = "" + qty.toInt()
                binding.valueEditText.setText(intQty)
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
        binding.labelTextView.text = getString(R.string.warning_estimate_incomplete)
        binding.labelTextView.startAnimation(animations!!.shake_long)
        binding.valueEditText.visibility = View.GONE
        binding.costTextView.visibility = View.GONE
    }

    private fun showCostCard() {
        binding.valueEditText.visibility = View.VISIBLE
        binding.costTextView.visibility = View.VISIBLE
        binding.costTextView.startAnimation(animations!!.bounce_soft)
        binding.labelTextView.text = getString(R.string.quantity)
    }

    private fun haltAnimation() {
        binding.startAnimationView.visibility = View.GONE
        binding.endAnimationView.visibility = View.GONE
    }

    private fun hideCostCard() {
        binding.costCard.visibility = View.GONE
        binding.updateButton.visibility = View.GONE
    }

    private fun calculateCost() {
        val item: ItemDTOTemp? = item

        val value = binding.valueEditText.text.toString()
        //  Lose focus on fields
        //  valueEditText.clearFocus()

        var lineAmount: Double?
        val tenderRate = newJobItemEstimate?.lineRate ?: item?.tenderRate ?: 0.0

        var qty = item?.quantity ?: value.toDoubleOrNull() ?: 0.0

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
                binding.labelTextView.text = getString(R.string.label_quantity)
                try { //  Default Calculation
                    lineAmount = qty * tenderRate
                } catch (e: NumberFormatException) {
                    lineAmount = null
                    requireActivity().hideKeyboard()
                    Timber.d(e)
                    toast("Please enter the Quantity.")
                }
            }
        }

        val displayAmount = lineAmount ?: tenderRate * qty

        when (displayAmount >= 0.0) {
            true -> {
                // Display pricing information
                binding.costTextView.text =
                    (" * R $tenderRate =  R ${DecimalFormat("#0.00").format(displayAmount)}")
                newJobItemEstimate?.qty = qty
                newJobItemEstimate?.lineRate = tenderRate
                createViewModel.setEstimateQuantity(qty)
                createViewModel.setEstimateLineRate(tenderRate)
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
        when (binding.labelTextView.text) {
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
        binding.labelTextView.text = getString(R.string.label_amount)
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
        binding.labelTextView.text = getString(R.string.label_volume_m3)
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
        binding.labelTextView.text = getString(R.string.label_area_m2)
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
        binding.labelTextView.text = getString(R.string.label_quantity)
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
                createViewModel.setJobToEdit(jobId)
            }
            if (itemId != null) {
                createViewModel.setCurrentProjectItem(itemId)
            }
            if (estimateId != null) {
                createViewModel.setEstimateToEdit(estimateId!!)
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
            createViewModel.setSectionId(sectionId!!)
        }
        // Load Photographs
        if (newJobItemEstimate != null) {
            uiScope.launch(uiScope.coroutineContext) {
                try {
                    setJobType(newJobItemEstimate?.jobItemEstimateSize!!)

                    quantity = newJobItemEstimate!!.qty
                    createViewModel.setEstimateQuantity(quantity)
                    newJobItemEstimate?.jobItemEstimatePhotos?.forEach { photo ->
                        restoreEstimatePhoto(
                            photo
                        )
                    }

                    isEstimateDone = createViewModel.estimateComplete(newJobItemEstimate)

                    if (isEstimateDone) {
                        binding.costCard.visibility = View.VISIBLE
                        binding.updateButton.visibility = View.VISIBLE
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
        uiScope.destroy()
        createViewModel.itemJob.removeObservers(viewLifecycleOwner)
        createViewModel.tempProjectItem.removeObservers(viewLifecycleOwner)
        createViewModel.currentEstimate.removeObservers(viewLifecycleOwner)
        createViewModel.loggedUser.removeObservers(viewLifecycleOwner)
        // make sure to stop the trip session. In this case it is being called inside `onDestroy`.
        mapboxNavigation.stopTripSession()
        // make sure to unregister the observer you have registered.
        mapboxNavigation.unregisterLocationObserver(locationObserver)
        mapboxNavigation.onDestroy()
        _binding = null
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
                        createViewModel.deleteItemFromList(it.itemId, it.estimateId)
                        newJob?.removeJobEstimateByItemId(it.itemId)
                        createViewModel.backupJob(newJob!!)
                        createViewModel.setJobToEdit(newJob?.jobId!!)
                        changesToPreserve = false
                        withContext(Dispatchers.Main.immediate) {
                            parentFragmentManager.beginTransaction().remove(this@EstimatePhotoFragment).commit()
                            parentFragmentManager.beginTransaction().detach(this@EstimatePhotoFragment).commit()
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
                    createViewModel.backupProjectItem(it)
                }
                newJobItemEstimate?.let {
                    createViewModel.backupEstimate(it)
                    createViewModel.updateEstimatePhotos(it.estimateId, it.jobItemEstimatePhotos)
                    newJob?.insertOrUpdateJobItemEstimate(it)
                    createViewModel.setEstimateToEdit(it.estimateId)
                }
                newJob?.let {
                    createViewModel.backupJob(it)
                    createViewModel.setJobToEdit(it.jobId)
                }

                changesToPreserve = false
            }
        }
    }
}
