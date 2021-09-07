/**
 * Updated by Shaun McDonald on 2021/05/18
 * Last modified on 2021/05/18, 10:26
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 **/

package za.co.xisystems.itis_rrm.ui.mainview.create.edit_estimates

import android.Manifest.permission
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog.Builder
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenCreated
import androidx.lifecycle.whenResumed
import androidx.lifecycle.whenStarted
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.airbnb.lottie.LottieAnimationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.DIAware
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
import za.co.xisystems.itis_rrm.data.localDB.entities.ProjectSectionDTO
import za.co.xisystems.itis_rrm.databinding.FragmentPhotoEstimateBinding
import za.co.xisystems.itis_rrm.extensions.checkLocationProviders
import za.co.xisystems.itis_rrm.extensions.observeOnce
import za.co.xisystems.itis_rrm.services.LocationModel
import za.co.xisystems.itis_rrm.ui.extensions.extensionToast
import za.co.xisystems.itis_rrm.ui.mainview.create.CreateViewModel
import za.co.xisystems.itis_rrm.ui.mainview.create.CreateViewModelFactory
import za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils.models.PhotoType
import za.co.xisystems.itis_rrm.ui.scopes.UiLifecycleScope
import za.co.xisystems.itis_rrm.utils.Coroutines
import za.co.xisystems.itis_rrm.utils.DateUtil
import za.co.xisystems.itis_rrm.utils.GlideApp
import za.co.xisystems.itis_rrm.utils.PhotoUtil
import za.co.xisystems.itis_rrm.utils.SqlLitUtils
import za.co.xisystems.itis_rrm.utils.zoomage.ZoomageView
import java.text.DecimalFormat
import java.util.Date
import kotlin.collections.set

/**
 * Created by Francis Mahlava on 2019/12/29.
 */

class EstimatePhotoFragment : LocationFragment(), DIAware {

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
    private var pointLocation: Double? = null

    private var _ui: FragmentPhotoEstimateBinding? = null
    private val ui get() = _ui!!

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
    private lateinit var photoUtil: PhotoUtil
    private var changesToPreserve: Boolean = false

    /**
     * ActivityResultContract for taking a photograph
     */
    private val takePicture = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isSaved ->
        if (isSaved) {
            processAndSetImage()
        } else {
            uiScope.launch(uiScope.coroutineContext) {
                withContext(Dispatchers.IO) {
                    photoUtil.deleteImageFile(filenamePath.toString())
                    withContext(Dispatchers.Main.immediate) {
                        haltAnimation()
                        ui.startImageView.visibility = View.VISIBLE
                        ui.endImageView.visibility = View.VISIBLE
                    }
                }
            }
        }
        this@EstimatePhotoFragment.photosDone()
    }

    init {
        System.setProperty("kotlinx.coroutines.debug", if (BuildConfig.DEBUG) "on" else "off")
        lifecycleScope.launch {

            whenCreated {
                uiScope.onCreate()
                createViewModel = activity?.run {
                    ViewModelProvider(this, factory).get(CreateViewModel::class.java)
                } ?: throw Exception("Invalid Activity")
            }

            whenStarted {
                viewLifecycleOwner.lifecycle.addObserver(uiScope)
                uiScope.launch(uiScope.coroutineContext) {

                    withContext(uiScope.coroutineContext) {
                        var myUserId: Int?
                        createViewModel.loggedUser.observe(
                            viewLifecycleOwner,
                            { user ->
                                user?.let {
                                    myUserId = it
                                    onUserFound(myUserId)
                                }
                            })
                    }

                    withContext(uiScope.coroutineContext) {
                        var myJobDTO: JobDTO?
                        createViewModel.currentJob.observeOnce(
                            viewLifecycleOwner,
                            { jobDto ->
                                jobDto?.let { jobDTO ->
                                    myJobDTO = jobDTO
                                    onJobFound(myJobDTO!!)
                                }
                            })
                    }

                    withContext(uiScope.coroutineContext) {
                        var itemDTO: ItemDTOTemp?
                        createViewModel.tempProjectItem.observe(
                            viewLifecycleOwner,
                            { itemDTOTemp ->

                                itemDTO = itemDTOTemp
                                itemDTO?.let {
                                    onItemFound(it)
                                }
                            })
                    }
                }
            }

            whenResumed {
                if (currentUser == -1) readNavArgs()
            }
        }
    }

    companion object {
        private const val REQUEST_STORAGE_PERMISSION = 505
    }

    private fun readNavArgs() = uiScope.launch(uiScope.coroutineContext) {
        val args: EstimatePhotoFragmentArgs? by navArgs()
        args?.let { navArgs ->
            if (!navArgs.estimateId.isNullOrEmpty()) {
                estimateId = navArgs.estimateId
                estimateId?.let { realEstimateId ->
                    createViewModel.setEstimateToEdit(realEstimateId)
                }
            }
            if (!navArgs.jobId.isNullOrEmpty()) {
                createViewModel.setJobToEdit(navArgs.jobId.toString())
            }
            if (!navArgs.itemId.isNullOrEmpty()) {
                createViewModel.setCurrentProjectItem(navArgs.itemId.toString())
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
        item = itemDTO

        if (item != null) {
            ui.titleTextView.text =
                getString(R.string.pair, item!!.itemCode, item!!.descr)
        } else {
            toast(
                "item is null in " + javaClass.simpleName
            )
        }

        if (newJob != null) {
            if (newJobItemEstimate == null && item != null) {
                newJobItemEstimate = newJob?.getJobEstimateByItemId(item?.itemId)
            }
            restoreEstimateViewState()
        }
        return item
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _ui = FragmentPhotoEstimateBinding.inflate(inflater, container, false)
        return ui.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (activity as MainActivity).supportActionBar?.title = getString(R.string.edit_estimate)
        locationWarning = false
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        (activity as MainActivity).supportActionBar?.title = getString(R.string.edit_estimate)
        newJobItemEstimatesList = ArrayList()
        newJobItemEstimatesPhotosList = ArrayList()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        photoUtil = PhotoUtil.getInstance(this.requireContext().applicationContext)

        var stateRestored = false
        val args: EstimatePhotoFragmentArgs by navArgs()
        if (args.jobId?.isNotBlank() == true) {
            readNavArgs()
            stateRestored = true
        }
        if (savedInstanceState != null && !stateRestored) {
            onRestoreInstanceState(savedInstanceState)
        }
    }

    override fun onStart() {
        super.onStart()
        ui.group13Loading.visibility = View.GONE
        mAppExecutor = AppExecutor()
        checkLocationProviders()
        setButtonClicks()
        setValueEditText(getStoredValue())

        ui.valueEditText.doOnTextChanged { _, _, _, _ ->
            changesToPreserve = true
            setCost()
        }
    }

    private fun setButtonClicks() {

        val myClickListener = View.OnClickListener { view ->
            when (view?.id) {

                R.id.startPhotoButton -> {
                    locationWarning = false
                    ui.startImageView.visibility = View.GONE
                    ui.startAnimationView.visibility = View.VISIBLE
                    takePhoto(PhotoType.START)
                    this@EstimatePhotoFragment.takingPhotos()
                }
                R.id.endPhotoButton -> {
                    locationWarning = false
                    ui.endImageView.visibility = View.GONE
                    ui.endAnimationView.visibility = View.VISIBLE
                    takePhoto(PhotoType.END)
                    this@EstimatePhotoFragment.takingPhotos()
                }

                R.id.cancelButton -> {
                    Coroutines.main {
                        buildDeleteDialog(this@EstimatePhotoFragment.requireView())
                    }
                }

                R.id.updateButton -> {
                    if (ui.costTextView.text.isNullOrEmpty()) {
                        toast("Please Make Sure you have Captured Both Images To Continue")
                        ui.labelTextView.startAnimation(animations!!.shake_long)
                    } else {
                        this@EstimatePhotoFragment.toggleLongRunning(true)
                        saveValidEstimate(view)
                    }
                }
            }
        }

        ui.startPhotoButton.setOnClickListener(myClickListener)
        ui.endPhotoButton.setOnClickListener(myClickListener)
        ui.cancelButton.setOnClickListener(myClickListener)
        ui.updateButton.setOnClickListener(myClickListener)

        // If the user hits the enter key on the costing field,
        // hide the keypad.

        ui.valueEditText.setOnEditorActionListener { _, _, event ->
            if (event.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER) {
                requireActivity().hideKeyboard()
                true
            } else {
                false
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

    private fun saveValidEstimate(view: View) {
        viewLifecycleOwner.lifecycle.coroutineScope.launch {

            newJobItemEstimate?.qty = ui.valueEditText.text.toString().toDouble()
            val qty = newJobItemEstimate?.qty
            if (qty != null && item?.tenderRate != null) {
                createViewModel.setEstimateQuantity(qty)
                newJobItemEstimate?.lineRate = (item!!.tenderRate)
            }

            createViewModel.saveNewJob(newJob!!)
            changesToPreserve = false
            updateData(view)
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
                permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(
                    permission.CAMERA,
                    permission.READ_EXTERNAL_STORAGE
                ),
                REQUEST_STORAGE_PERMISSION
            )
        } else {
            val currentLocation = getCurrentLocation()
            if (currentLocation != null) {
                launchCamera()
            } else {
                this.checkLocationProviders()
            }
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
        Coroutines.io {
            imageUri = photoUtil.getUri()
            withContext(Dispatchers.Main.immediate) {
                this@EstimatePhotoFragment.takingPhotos()
                takePicture.launch(imageUri)
            }
        }
    }

    private suspend fun restoreEstimatePhoto(
        // jobItemEstimate: JobItemEstimateDTO,
        photo: JobItemEstimatesPhotoDTO
    ) = Coroutines.main {

        var targetImageView = ui.startImageView
        var targetTextView = ui.startSectionTextView

        if (!photo.isPhotostart) {
            targetImageView = ui.endImageView
            targetTextView = ui.endSectionTextView
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
            updatePhotos(
                imageUri = targetUri,
                animate = false,
                textView = targetTextView,
                isStart = photo.isPhotostart
            )
            photo.sectionMarker?.let { mark ->
                targetTextView.text = mark
            }
            loadEstimateItemPhoto(targetUri, targetImageView, false)
        }
    }

    private suspend fun extractImageUri(
        jobItemEstimatePhoto: JobItemEstimatesPhotoDTO?
    ): Uri? = withContext(Dispatchers.IO) {
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
    private fun processAndSetImage(
        // item: ItemDTOTemp?,
        // newJobDTO: JobDTO?,
        // estimate: JobItemEstimateDTO?
    ) {
        Coroutines.main {
            try { //  Location of picture
                val estimateLocation: LocationModel? = this.getCurrentLocation()
                Timber.d("$estimateLocation")
                if (estimateLocation != null) {

                    //  Save Image to Internal Storage
                    withContext(Dispatchers.IO) {
                        filenamePath = photoUtil.saveImageToInternalStorage(
                            imageUri!!
                        ) as HashMap<String, String>
                    }

                    processPhotoEstimate(
                        estimateLocation = estimateLocation,
                        filePath = filenamePath,
                        itemidPhototype = itemIdPhotoType
                    )

                    when (photoType) {
                        PhotoType.START -> updatePhotos(
                            imageUri = imageUri.also { startImageUri = it },
                            animate = true,
                            textView = ui.startSectionTextView,
                            isStart = true
                        )

                        PhotoType.END -> updatePhotos(
                            imageUri = imageUri.also { endImageUri = it },
                            animate = true,
                            textView = ui.endSectionTextView,
                            isStart = false
                        )
                    }
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
    }

    private fun processPhotoEstimate(
        estimateLocation: LocationModel,
        filePath: Map<String, String>,
        itemidPhototype: Map<String, String>
    ) {

        val itemId = item?.itemId ?: itemidPhototype["itemId"]

        if (newJobItemEstimate == null) {
            newJobItemEstimate = newJob?.getJobEstimateByItemId(itemId)
        }

        if (newJobItemEstimate == null) {
            newJobItemEstimate = createItemEstimate(
                itemId = itemId,
                newJob = newJob,
                item = item
            )

            if (newJob?.jobItemEstimates == null) {
                newJob?.jobItemEstimates = ArrayList()
            }

            newJob?.jobItemEstimates!!.add(newJobItemEstimate!!)
        }

        uiScope.launch(context = uiScope.coroutineContext) {
            processPhotoLocation(estimateLocation, filePath, itemidPhototype)
        }
    }

    // TODO: polygon verification for offline photography
    private suspend fun processPhotoLocation(
        estimateLocation: LocationModel,
        filePath: Map<String, String>,
        itemidPhototype: Map<String, String>
    ) {
        try {
            toggleLongRunning(true)
            withContext(uiScope.coroutineContext) {
                if (!this@EstimatePhotoFragment.disableGlide) {
                    placeProvisionalPhoto(
                        filePath,
                        estimateLocation,
                        itemidPhototype
                    )
                }
            }
        } catch (t: Throwable) {
            disableGlide = true
            val message = "Failed to verify photo location: ${t.message ?: XIErrorHandler.UNKNOWN_ERROR}"
            Timber.e(t, message)
            val xiErr = XIResult.Error(t, message)
            crashGuard(
                view = this.requireView(),
                throwable = xiErr,
                refreshAction = {
                    retryProcessPhotoLocation(
                        estimateLocation,
                        filePath,
                        itemidPhototype
                    )
                })
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
        ui.startImageView.visibility = View.VISIBLE
        ui.endImageView.visibility = View.VISIBLE
        disableGlide = false
    }

    private suspend fun placeProvisionalPhoto(
        filePath: Map<String, String>,
        currentLocation: LocationModel,
        itemidPhototype: Map<String, String>
    ) = Coroutines.main {
        val photo = createItemEstimatePhoto(
            itemEst = newJobItemEstimate!!,
            filePath = filePath,
            currentLocation = currentLocation,
            itemIdPhotoType = itemidPhototype,
            pointLocation = -1.0
        )

        this@EstimatePhotoFragment.newJobItemEstimate!!.setJobItemEstimatePhoto(
            photo
        )

        this@EstimatePhotoFragment.disableGlide = false

        val targetUri: Uri? = extractImageUri(photo)
        val targetView = when (photo.isPhotostart) {
            true -> ui.startImageView
            else -> ui.endImageView
        }

        val targetAnimation: LottieAnimationView = when (photo.isPhotostart) {
            true -> ui.startAnimationView
            else -> ui.endAnimationView
        }

        val targetTextView = when (photo.isPhotostart) {
            true -> ui.startSectionTextView
            else -> ui.endSectionTextView
        }
        targetAnimation.visibility = View.GONE

        photo.sectionMarker?.let {
            targetTextView.text = it
        }

        loadEstimateItemPhoto(
            targetUri,
            targetView,
            true
        )
    }

    private fun createItemEstimatePhoto(
        itemEst: JobItemEstimateDTO,
        filePath: Map<String, String>,
        currentLocation: LocationModel,
        itemIdPhotoType: Map<String, String>,
        pointLocation: Double
    ): JobItemEstimatesPhotoDTO {

        changesToPreserve = true
        val isPhotoStart = itemIdPhotoType["type"] == PhotoType.START.name
        val photoId: String = SqlLitUtils.generateUuid()

        // newJobItemEstimatesPhotosList.add(newEstimatePhoto)
        return JobItemEstimatesPhotoDTO(
            descr = "",
            estimateId = itemEst.estimateId,
            filename = filePath["filename"] ?: error(""),
            photoDate = DateUtil.dateToString(Date())!!,
            photoId = photoId,
            photoStart = null,
            photoEnd = null,
            startKm = pointLocation,
            endKm = pointLocation,
            photoLatitude = currentLocation.latitude,
            photoLongitude = currentLocation.longitude,
            photoLatitudeEnd = currentLocation.latitude,
            photoLongitudeEnd = currentLocation.longitude,
            photoPath = filePath["path"] ?: error(""),
            recordSynchStateId = 0,
            recordVersion = 0,
            isPhotostart = isPhotoStart,
            sectionMarker = currentLocation.toString()
        )
    }

    private fun createItemEstimate(
        itemId: String?,
        newJob: JobDTO?,
        item: ItemDTOTemp?

    ): JobItemEstimateDTO {
        changesToPreserve = true
        val estimateId = SqlLitUtils.generateUuid()

        // newJobItemEstimatesList.add(newEstimate)
        return JobItemEstimateDTO(
            actId = 0,
            estimateId = estimateId,
            jobId = newJob?.jobId,
            lineRate = item!!.tenderRate,
            jobEstimateWorks = arrayListOf(),
            jobItemEstimatePhotos = arrayListOf(),
            jobItemMeasure = arrayListOf(),
            projectItemId = itemId,
            projectVoId = newJob?.projectVoId,
            qty = quantity,
            recordSynchStateId = 0,
            recordVersion = 0,
            trackRouteId = null,
            jobItemEstimatePhotoStart = null,
            jobItemEstimatePhotoEnd = null,
            estimateComplete = null,
            measureActId = 0,
            selectedItemUom = item.uom
        )
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
        if (newJob == null) readNavArgs()
        setCost()
    }

    private fun updatePhotos(
        imageUri: Uri?,
        animate: Boolean,
        textView: TextView,
        isStart: Boolean
    ) {

        if (imageUri != null) {
            // Coroutines.main {
            establishRouteSectionData(isStart, textView, animate)
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
            .placeholder(R.drawable.logo_new_medium)
            .error(R.drawable.no_image)
            .into(imageView)

        if (animate) imageView.startAnimation(bounce_1000)

        imageView.setOnClickListener {
            showZoomedImage(imageUri)
        }
    } catch (e: Exception) {
        Timber.e(e)
    } finally {
        Coroutines.main {
            this.isEstimateDone = createViewModel.estimateComplete(newJobItemEstimate)

            if (isEstimateDone) {
                ui.costCard.visibility = View.VISIBLE
                ui.updateButton.visibility = View.VISIBLE
                setCost()
            } else {
                extensionToast(
                    message = "Please take both photographs ...",
                    style = ToastStyle.INFO,
                    position = ToastGravity.BOTTOM
                )
                hideCostCard()
            }
        }
    }

    private fun establishRouteSectionData(
        isStart: Boolean,
        textView: TextView,
        animate: Boolean
    ) {
        Coroutines.main {
            try {
                withContext(Dispatchers.Main) {
                    createViewModel.sectionId.observe(viewLifecycleOwner, { sectId ->
                        Coroutines.main {
                            val section = createViewModel.getSection(sectId)

                            section.observe(viewLifecycleOwner, { projectSectionDTO ->
                                if (projectSectionDTO != null) {
                                    captionEstimateItemPhoto(
                                        projectSectionDTO,
                                        textView,
                                        animate
                                    )
                                }
                            })
                        }
                    })
                }
            } catch (t: Throwable) {
                val secErr = XIResult.Error(t, "Failed to caption photo: ${t.message ?: XIErrorHandler.UNKNOWN_ERROR}")
                Timber.e(t, secErr.message)
                crashGuard(
                    this.requireView(),
                    secErr,
                    refreshAction = { retryRouteSectionData(isStart, textView, animate) }
                )
            }
        }
    }

    private fun retryRouteSectionData(
        isStart: Boolean,
        textView: TextView,
        animate: Boolean
    ) {
        IndefiniteSnackbar.hide()
        establishRouteSectionData(isStart, textView, animate)
    }

    private fun captionEstimateItemPhoto(
        section: ProjectSectionDTO,
        textView: TextView,
        animate: Boolean,
    ) {
        val direction = section.direction
        if (direction != null) {

            Coroutines.main {
                withContext(Dispatchers.Main.immediate) {
                    textView.text = getRealSection(section, pointLocation!!)
                    if (animate) textView.startAnimation(animations?.bounce_long)
                }
            }
        }
    }

    private fun getRealSection(
        section: ProjectSectionDTO,
        pointLocation: Double
    ): String {
        return "${section.route} ${section.section} ${section.direction} at $pointLocation km"
    }

    private fun setValueEditText(qty: Double) {
        when (item?.uom) {
            "M2", "M3", "M" -> {
                val decQty = "" + qty
                ui.valueEditText.setText(decQty)
            }
            else -> {
                val intQty: String = "" + qty.toInt()
                ui.valueEditText.setText(intQty)
            }
        }
    }

    private fun setCost() {
        if (isEstimateDone) {
            calculateCost()
            ui.valueEditText.visibility = View.VISIBLE
            ui.costTextView.visibility = View.VISIBLE
            ui.costTextView.startAnimation(animations!!.bounce_soft)
            ui.labelTextView.text = "Quantity"
        } else {
            ui.labelTextView.text = getString(R.string.warning_estimate_incomplete)
            ui.labelTextView.startAnimation(animations!!.shake_long)
            ui.valueEditText.visibility = View.GONE
            ui.costTextView.visibility = View.GONE
        }
    }

    private fun haltAnimation() {
        ui.startAnimationView.visibility = View.GONE
        ui.endAnimationView.visibility = View.GONE
    }

    private fun hideCostCard() {
        ui.costCard.visibility = View.GONE
        ui.updateButton.visibility = View.GONE
    }

    private fun calculateCost() {
        val item: ItemDTOTemp? = item

        val value = ui.valueEditText.text.toString()
        //  Lose focus on fields
        //  valueEditText.clearFocus()

        var lineAmount: Double? = null
        val tenderRate = item?.tenderRate ?: 0.0

        var qty = value.toDoubleOrNull() ?: 1.0

        try {
            qty = value.toDouble()
        } catch (e: NumberFormatException) {
            Timber.d(e)
        }

        when (item?.uom) {
            in arrayOf("QTY", "NO", null) -> {
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
                ui.labelTextView.text = getString(R.string.label_quantity)
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

        ui.costTextView.text =
            (" * R $tenderRate =  R ${DecimalFormat("#0.00").format(lineAmount ?: qty * tenderRate)}")

        newJobItemEstimate?.qty = qty
        createViewModel.setEstimateQuantity(qty)
        newJobItemEstimate?.lineRate = tenderRate
    }

    private fun validateLengthCosting(
        qty: Double,
        tenderRate: Double?
    ): Double? {
        var inLineAmount: Double? = null
        when (ui.labelTextView.text) {
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
        var inlineAmount: Double? = null
        ui.labelTextView.text = getString(R.string.label_amount)
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
        var inlineAmount: Double? = null
        ui.labelTextView.text = getString(R.string.label_volume_m3)
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
        var inLineAmount: Double? = null
        ui.labelTextView.text = getString(R.string.label_area_m2)
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
        var inLineAmount: Double? = null
        ui.labelTextView.text = getString(R.string.label_quantity)
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
            newJobItemEstimate?.let {
                outState.putString("estimateId", newJobItemEstimate?.estimateId ?: "")
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
                    quantity = newJobItemEstimate!!.qty
                    createViewModel.setEstimateQuantity(quantity)
                    isEstimateDone = createViewModel.estimateComplete(newJobItemEstimate)
                    newJobItemEstimate?.jobItemEstimatePhotos?.forEach { photo ->
                        restoreEstimatePhoto(
                            photo
                        )
                    }

                    if (isEstimateDone) {
                        ui.costCard.visibility = View.VISIBLE
                        ui.updateButton.visibility = View.VISIBLE
                        setValueEditText(quantity)
                    }
                } catch (t: Throwable) {
                    Timber.e(t, "Failed to restore estimate view-state.")
                    val estError = XIResult.Error(t, t.message ?: XIErrorHandler.UNKNOWN_ERROR)
                    crashGuard(
                        view = this@EstimatePhotoFragment.requireView(),
                        throwable = estError,
                        refreshAction = { restoreEstimateViewState() })
                }
            }
        }
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
        createViewModel.currentJob.removeObservers(viewLifecycleOwner)
        createViewModel.tempProjectItem.removeObservers(viewLifecycleOwner)
        createViewModel.loggedUser.removeObservers(viewLifecycleOwner)
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
                    createViewModel.deleteItemFromList(it.itemId, it.estimateId)
                    newJob?.removeJobEstimateByItemId(it.itemId)
                    createViewModel.backupJob(newJob!!)
                    createViewModel.setJobToEdit(newJob?.jobId!!)
                    parentFragmentManager.beginTransaction().remove(this).commit()
                    parentFragmentManager.beginTransaction().detach(this).commit()
                    navToAddProject(view)
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
        Coroutines.main {
            if (changesToPreserve) {
                newJobItemEstimate?.let {
                    createViewModel.backupEstimate(it)
                }
                newJob?.let {
                    createViewModel.backupJob(it)
                }
                changesToPreserve = false
            }
        }
        super.onStop()
    }
}
