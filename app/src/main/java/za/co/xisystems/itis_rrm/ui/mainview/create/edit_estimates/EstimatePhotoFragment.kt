package za.co.xisystems.itis_rrm.ui.mainview.create.edit_estimates

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenResumed
import androidx.lifecycle.whenStarted
import androidx.navigation.Navigation
import com.airbnb.lottie.LottieAnimationView
import icepick.Icepick
import icepick.State
import kotlinx.android.synthetic.main.fragment_photo_estimate.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import timber.log.Timber
import www.sanju.motiontoast.MotionToast
import za.co.xisystems.itis_rrm.BuildConfig
import za.co.xisystems.itis_rrm.MainActivity
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.R.string
import za.co.xisystems.itis_rrm.base.LocationFragment
import za.co.xisystems.itis_rrm.custom.errors.XIErrorHandler
import za.co.xisystems.itis_rrm.custom.results.XIError
import za.co.xisystems.itis_rrm.custom.views.IndefiniteSnackbar
import za.co.xisystems.itis_rrm.data._commons.AbstractTextWatcher
import za.co.xisystems.itis_rrm.data.localDB.entities.ItemDTOTemp
import za.co.xisystems.itis_rrm.data.localDB.entities.ItemSectionDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobEstimateWorksDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimateDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimatesPhotoDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemMeasureDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobSectionDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.ProjectSectionDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.SectionPointDTO
import za.co.xisystems.itis_rrm.extensions.observeOnce
import za.co.xisystems.itis_rrm.services.LocationModel
import za.co.xisystems.itis_rrm.ui.mainview.activities.SharedViewModel
import za.co.xisystems.itis_rrm.ui.mainview.activities.SharedViewModelFactory
import za.co.xisystems.itis_rrm.ui.mainview.create.CreateViewModel
import za.co.xisystems.itis_rrm.ui.mainview.create.CreateViewModelFactory
import za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils.intents.AbstractIntent
import za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils.models.PhotoType
import za.co.xisystems.itis_rrm.ui.scopes.UiLifecycleScope
import za.co.xisystems.itis_rrm.utils.Coroutines
import za.co.xisystems.itis_rrm.utils.DateUtil
import za.co.xisystems.itis_rrm.utils.GlideApp
import za.co.xisystems.itis_rrm.utils.PhotoUtil
import za.co.xisystems.itis_rrm.utils.ServiceUtil
import za.co.xisystems.itis_rrm.utils.SqlLitUtils
import za.co.xisystems.itis_rrm.utils.zoomage.ZoomageView
import java.io.File
import java.text.DecimalFormat
import java.util.Date
import kotlin.collections.set

/**
 * Created by Francis Mahlava on 2019/12/29.
 */

class EstimatePhotoFragment : LocationFragment(R.layout.fragment_photo_estimate),
    KodeinAware {

    private var sectionId: String? = null
    override val kodein by kodein()
    private lateinit var createViewModel: CreateViewModel
    private val factory: CreateViewModelFactory by instance()
    private var mAppExecutor: AppExecutor? = null
    private lateinit var lm: LocationManager
    private var gpsEnabled = false
    private var networkEnabled = false
    private var isEstimateDone: Boolean = false
    private var isRouteSectionPoint: Boolean = false
    private var startKm: Double? = null
    private var endKm: Double? = null
    private var disableGlide: Boolean = false
    private var locationWarning: Boolean = false

    @State
    var photoType: PhotoType = PhotoType.START

    @State
    var itemIdPhotoType: HashMap<String, String> = HashMap()
    internal var job: JobDTO? = null

    @State
    var filenamePath: HashMap<String, String> = HashMap()

    @State
    private var item: ItemDTOTemp? = null

    @State
    internal var newJob: JobDTO? = null

    @State
    internal var estimate: JobItemEstimateDTO? = null
    var direction: String? = null
    private var newJobItemEstimate: JobItemEstimateDTO? = null

    @State
    var quantity: Double = 1.0
    private var estimateId: String? = null
    private lateinit var jobArrayList: ArrayList<JobDTO>
    private lateinit var newJobItemEstimatesPhotosList: ArrayList<JobItemEstimatesPhotoDTO>
    private lateinit var newJobItemEstimatesWorksList: ArrayList<JobEstimateWorksDTO>
    private lateinit var newJobItemEstimatesList: ArrayList<JobItemEstimateDTO>
    private lateinit var jobItemMeasureArrayList: ArrayList<JobItemMeasureDTO>
    private lateinit var jobItemSectionArrayList: ArrayList<JobSectionDTO>
    private lateinit var itemSections: ArrayList<ItemSectionDTO>
    private lateinit var newJobItemEstimatesPhotosList2: ArrayList<JobItemEstimatesPhotoDTO>
    private lateinit var newJobItemEstimatesWorksList2: ArrayList<JobEstimateWorksDTO>
    private lateinit var jobItemMeasureArrayList2: ArrayList<JobItemMeasureDTO>
    private lateinit var jobItemSectionArrayList2: ArrayList<JobSectionDTO>
    internal var description: String? = null
    private var currentUser: Int = -1
    private var startImageUri: Uri? = null
    private var endImageUri: Uri? = null
    private var imageUri: Uri? = null
    private lateinit var sharedViewModel: SharedViewModel
    private val shareFactory: SharedViewModelFactory by instance()
    private val uiScope = UiLifecycleScope()

    init {
        System.setProperty("kotlinx.coroutines.debug", if (BuildConfig.DEBUG) "on" else "off")
        lifecycleScope.launch {

            whenStarted {

                createViewModel = activity?.run {
                    ViewModelProvider(this, factory).get(CreateViewModel::class.java)
                } ?: throw Exception("Invalid Activity")

                sharedViewModel = activity?.run {
                    ViewModelProvider(this, shareFactory).get(SharedViewModel::class.java)
                } ?: throw Exception("Invalid Activity")

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
                        createViewModel.jobItem.observeOnce(
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
                        createViewModel.projectItemTemp.observeOnce(
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
                // uiScope.job.cancel(cause = CancellationException("onResume"))
            }
        }
    }

    private fun onUserFound(foundUserId: Int?): Int? {
        if (foundUserId != null) {
            currentUser = foundUserId
        }
        return currentUser
    }

    private fun onJobFound(foundJobDTO: JobDTO?): JobDTO? {
        newJob = foundJobDTO!!
        if (estimateId != null) {
            val estimateDTO = newJob?.JobItemEstimates?.find { itemEstimate ->
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

        if (item != null) titleTextView.text =
            getString(string.pair, item!!.itemCode, item!!.descr)
        else
            toast(
                "item is null in " + javaClass.simpleName
            )

        if (newJob != null) {
            if (newJobItemEstimate == null && item != null)
                newJobItemEstimate = newJob?.getJobEstimateByItemId(item?.itemId)

            restoreEstimateViewState()
        }
        return item
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_photo_estimate, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (activity as MainActivity).supportActionBar?.title = getString(string.edit_estimate)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
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
        if (savedInstanceState != null) {
            onRestoreInstanceState(savedInstanceState)
        }
        lifecycle.addObserver(uiScope)
        setHasOptionsMenu(true)
        (activity as MainActivity).supportActionBar?.title = getString(string.edit_estimate)

        itemSections = ArrayList()
        jobArrayList = ArrayList()

        jobItemSectionArrayList = ArrayList()
        jobItemMeasureArrayList = ArrayList()
        newJobItemEstimatesList = ArrayList()
        newJobItemEstimatesPhotosList = ArrayList()
        newJobItemEstimatesWorksList = ArrayList()

        jobItemSectionArrayList2 = ArrayList()
        jobItemMeasureArrayList2 = ArrayList()

        newJobItemEstimatesPhotosList2 = ArrayList()
        newJobItemEstimatesWorksList2 = ArrayList()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Icepick.restoreInstanceState(this, savedInstanceState)
        viewLifecycleOwner.lifecycle.addObserver(uiScope)

        group13_loading.visibility = View.GONE
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

        valueEditText!!.addTextChangedListener(object : AbstractTextWatcher() {
            override fun onTextChanged(text: String) {
                setCost()
            }
        })

        setValueEditText(getStoredValue())
    }

    private fun setButtonClicks() {

        val myClickListener = View.OnClickListener { view ->
            when (view?.id) {

                R.id.startPhotoButton -> {
                    locationWarning = false
                    startImageView.visibility = View.GONE
                    startAnimationView.visibility = View.VISIBLE
                    takePhoto(PhotoType.START)
                }
                R.id.endPhotoButton -> {
                    locationWarning = false
                    endImageView.visibility = View.GONE
                    endAnimationView.visibility = View.VISIBLE
                    takePhoto(PhotoType.END)
                }

                R.id.cancelButton -> {
                    Coroutines.main {
                        createViewModel.deleteJobFromList(newJob!!.JobId)
                        createViewModel.deleteItemList(newJob!!.JobId)
                        createViewModel.setJobToEditItem(null)
                        createViewModel.jobItem.value = null
                        createViewModel.newJob.value = null
                        fragmentManager?.beginTransaction()?.remove(this)?.commit()
                        fragmentManager?.beginTransaction()?.detach(this)?.commit()

                        Navigation.findNavController(view)
                            .navigate(R.id.action_estimatePhotoFragment_to_nav_create)
                    }
                    // TODO(clear temp database Tables for Job And Items)
                }

                R.id.updateButton -> {
                    if (costTextView.text.isNullOrEmpty()) {
                        toast("Please Make Sure you have Captured Both Images To Continue")
                        labelTextView.startAnimation(animations!!.shake_long)
                    } else {
                        saveValidEstimate(view)
                    }
                }
            }
        }

        startPhotoButton.setOnClickListener(myClickListener)
        endPhotoButton.setOnClickListener(myClickListener)
        cancelButton.setOnClickListener(myClickListener)
        updateButton.setOnClickListener(myClickListener)

        // If the user hits the enter key on the costing field,
        // hide the keypad.

        valueEditText.setOnEditorActionListener { _, _, event ->
            if (event.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER) {
                requireActivity().hideKeyboard()
                true
            } else {
                false
            }
        }
    }

    private fun saveValidEstimate(view: View) {
        viewLifecycleOwner.lifecycle.coroutineScope.launch {

            newJobItemEstimate?.qty = valueEditText.text.toString().toDouble()
            val qty = newJobItemEstimate?.qty
            if (qty != null && item?.tenderRate != null) {
                createViewModel.setEstimateQuantity(qty)
                newJobItemEstimate?.lineRate = (qty * item!!.tenderRate)
            }

            createViewModel.saveNewJob(newJob!!)

            createViewModel.updateNewJob(
                newJob!!.JobId,
                startKm!!,
                endKm!!,
                newJob?.SectionId!!,
                newJob?.JobItemEstimates!!,
                newJob?.JobSections!!
            )

            updateData(view)
        }
    }

    private fun updateData(view: View) {
        uiScope.cancel(CancellationException("updating estimates..."))
        viewLifecycleOwner.lifecycle.coroutineScope.coroutineContext.cancel(
            CancellationException("updating estimates ...")
        )
        Navigation.findNavController(view)
            .navigate(R.id.action_estimatePhotoFragment_to_addProjectFragment)
    }

    private fun takePhoto(picType: PhotoType) {

        photoType = picType
        if (ContextCompat.checkSelfPermission(
                requireActivity().applicationContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_STORAGE_PERMISSION
            )
        } else {
            launchCamera()
        }
    }

    private fun displayPromptForEnablingGPS(
        activity: Activity
    ) {

        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
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

        val targetUri = when (photoType) {
            PhotoType.END -> endImageUri
            PhotoType.START -> startImageUri
        }

        imageUri = PhotoUtil.getUri(this)
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (this.activity?.packageManager?.let { takePictureIntent.resolveActivity(it) } != null) {
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
            takePictureIntent.putExtra(
                MediaStore.EXTRA_SCREEN_ORIENTATION,
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            )
            takePictureIntent.putExtra("photoType", itemIdPhotoType["type"])
            takePictureIntent.putExtra("itemId", itemIdPhotoType["itemId"])
            takePictureIntent.putExtra("targetUri", targetUri.toString())
            startActivityForResult(takePictureIntent, AbstractIntent.REQUEST_TAKE_PHOTO)
        }
    }

    private fun restoreEstimatePhoto(
        // jobItemEstimate: JobItemEstimateDTO,
        isStart: Boolean
    ) {

        val targetIndex = when (isStart) {
            true -> 0
            else -> 1
        }

        val targetImageView = when (isStart) {
            true -> startImageView
            else -> endImageView
        }

        val targetTextView = when (isStart) {
            true -> startSectionTextView
            else -> endSectionTextView
        }

        val targetUri =
            null
                ?: extractImageUri(
                    this.newJobItemEstimate?.jobItemEstimatePhotos?.get(
                        targetIndex
                    )
                )

        when (isStart) {
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
                isStart = isStart
            )
            loadEstimateItemPhoto(targetUri, targetImageView, false)
        }
    }

    private fun extractImageUri(jobItemEstimatePhoto: JobItemEstimatesPhotoDTO?): Uri? {
        if (jobItemEstimatePhoto != null) {
            val path: String = jobItemEstimatePhoto.photoPath
            Timber.d("x -> photo $path")
            if (path.isNotBlank()) {
                val file = File(path)
                return Uri.fromFile(file)
            }
        }
        return null
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) { // image capture activity successful
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // Process the image and set it to the TextView

            processAndSetImage()
        } else { // Otherwise, delete the temporary image file
            PhotoUtil.deleteImageFile(filenamePath.toString())
            haltAnimation()
            startImageView.visibility = View.VISIBLE
            endImageView.visibility = View.VISIBLE
        }
    }

    @SuppressLint("RestrictedApi")
    private fun processAndSetImage(
        // item: ItemDTOTemp?,
        // newJobDTO: JobDTO?,
        // estimate: JobItemEstimateDTO?
    ) {

        try { //  Location of picture
            val estimateLocation: LocationModel? = this.getCurrentLocation()
            Timber.d("$estimateLocation")
            if (estimateLocation != null) {

                //  Save Image to Internal Storage
                filenamePath = PhotoUtil.saveImageToInternalStorage(
                    requireActivity(),
                    imageUri!!
                ) as HashMap<String, String>

                processPhotoEstimate(
                    estimateLocation = estimateLocation,
                    filePath = filenamePath,
                    itemidPhototype = itemIdPhotoType
                )

                when (photoType) {
                    PhotoType.START -> updatePhotos(
                        imageUri = imageUri.also { startImageUri = it },
                        animate = true,
                        textView = startSectionTextView,
                        isStart = true
                    )

                    PhotoType.END -> updatePhotos(
                        imageUri = imageUri.also { endImageUri = it },
                        animate = true,
                        textView = endSectionTextView,
                        isStart = false
                    )
                }
            } else {
                toast("Error: Current location is null!")
            }
        } catch (e: Exception) {
            toast(string.error_getting_image)
            Timber.e(e)
            throw e
        }
    }

    private fun processPhotoEstimate(
        estimateLocation: LocationModel,
        filePath: Map<String, String>,
        itemidPhototype: Map<String, String>
        // item: ItemDTOTemp?,
        // newJobDTO: JobDTO?
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

            if (newJob?.JobItemEstimates == null) {
                newJob?.JobItemEstimates = ArrayList()
            }

            newJob?.JobItemEstimates!!.add(newJobItemEstimate!!)
        }

        if (ServiceUtil.isNetworkAvailable(requireActivity().applicationContext)) {

            uiScope.launch(context = uiScope.coroutineContext) {

                processPhotoLocation(estimateLocation, filePath, itemidPhototype)
            }
        } else {
            val networkToast = Toast.makeText(
                activity?.applicationContext,
                string.no_connection_detected,
                Toast.LENGTH_LONG
            )
            networkToast.setGravity(Gravity.CENTER_VERTICAL, 0, 0)
            networkToast.show()
        }
    }

    private suspend fun processPhotoLocation(
        estimateLocation: LocationModel,
        filePath: Map<String, String>,
        itemidPhototype: Map<String, String>
    ) {
        withContext(uiScope.coroutineContext) {
            val result = getRouteSectionPoint(
                estimateLocation
            )
            if (result.isNullOrBlank() || result.contains(other = "xxx" as CharSequence, ignoreCase = true)) {
                this@EstimatePhotoFragment.disableGlide = true
                showLocationWarning()
                resetPhotos()
            }
        }
        withContext(uiScope.coroutineContext) {
            if (!this@EstimatePhotoFragment.disableGlide) {
                validateRouteSection(newJob?.ProjectId!!)
            } else {
                resetPhotos()
            }
        }

        withContext(uiScope.coroutineContext) {
            if (!this@EstimatePhotoFragment.disableGlide) {
                placeEstimatePhotoInRouteSection(
                    filePath,
                    estimateLocation,
                    itemidPhototype
                )
            }

            resetPhotos()
        }
    }

    private fun resetPhotos() {
        haltAnimation()
        startImageView.visibility = View.VISIBLE
        endImageView.visibility = View.VISIBLE
        disableGlide = false
    }

    private suspend fun validateRouteSection(projectId: String) {
        val sectionPoint = createViewModel.getPointSectionData(projectId)
        onSectionPointFound(sectionPoint)
    }

    private suspend fun onSectionPointFound(sectionPoint: SectionPointDTO?) {
        Timber.d("SectionPoint: $sectionPoint")
        if (sectionPoint == null) {
            showLocationWarning()
        } else {
            validateRouteSectionByProject(
                sectionPoint = sectionPoint
            )
        }
    }

    private suspend fun validateRouteSectionByProject(
        sectionPoint: SectionPointDTO
    ) {

        val projectSectionId = createViewModel.getSectionByRouteSectionProject(
            sectionPoint.sectionId,
            sectionPoint.linearId,
            newJob?.ProjectId
        )

        onProjectSectionIdFound(projectSectionId)
    }

    private suspend fun onProjectSectionIdFound(projectSectionId: String?) {
        Timber.d("ProjectSectionId: $projectSectionId")
        if (projectSectionId == null) {
            showLocationWarning()
            this.disableGlide = true
            hideCostCard()
        } else {
            this.disableGlide = false
            costCard.visibility = View.VISIBLE

            createViewModel.sectionId.value = projectSectionId
            val projectSection = createViewModel.getSection(projectSectionId)
            projectSection.observe(viewLifecycleOwner, {
                it?.let {
                    val localProjectSection = it
                    startKm = localProjectSection.startKm
                    endKm = localProjectSection.endKm
                    Timber.d("ProjectSection: $it")
                    createRouteSection(
                        secId = projectSectionId,
                        jobId = newJob!!.JobId,
                        startKm = startKm!!,
                        endKm = endKm!!
                    ).apply {

                        newJob!!.JobSections?.add(this)
                        newJob!!.SectionId = jobSectionId
                        newJob!!.StartKm = this.startKm
                        newJob!!.EndKm = this.endKm
                        isRouteSectionPoint = true
                    }
                }
            })
        }
    }

    private fun showLocationWarning() {
        if (!locationWarning) {
            this.motionToast(
                getString(string.no_section_for_project),
                MotionToast.TOAST_ERROR,
                MotionToast.GRAVITY_BOTTOM,
                MotionToast.LONG_DURATION
            )
            locationWarning = true
        }
    }

    private suspend fun placeEstimatePhotoInRouteSection(
        filePath: Map<String, String>,
        currentLocation: LocationModel,
        itemidPhototype: Map<String, String>
    ) {
        val photo: JobItemEstimatesPhotoDTO?
        val sectionPointData = createViewModel.getPointSectionData(newJob?.ProjectId!!)

        if (sectionPointData != null && sectionPointData.sectionId.toString().isNotBlank()) {
            Timber.d("SectionPointDto: $sectionPointData")
            photo = createItemEstimatePhoto(
                itemEst = newJobItemEstimate!!,
                filePath = filePath,
                currentLocation = currentLocation,
                itemIdPhotoType = itemidPhototype,
                pointLocation = sectionPointData.pointLocation
            )
            this@EstimatePhotoFragment.newJobItemEstimate!!.setJobItemEstimatePhoto(
                photo
            )

            this@EstimatePhotoFragment.disableGlide = false

            val targetUri: Uri? = extractImageUri(photo)
            val targetView = when (photo.is_PhotoStart) {
                true -> startImageView
                else -> endImageView
            }

            val targetAnimation: LottieAnimationView = when (photo.is_PhotoStart) {
                true -> startAnimationView
                else -> endAnimationView
            }

            targetAnimation.visibility = View.GONE

            loadEstimateItemPhoto(
                targetUri,
                targetView,
                true
            )
        } else {
            showLocationWarning()
        }
    }

    private fun showSectionOutOfBoundError(sectionPoint: SectionPointDTO?) {
        toast(
            "You are not between the start: " + sectionPoint?.pointLocation.toString() +
                " and end: " + sectionPoint?.pointLocation.toString() + " co-ordinates for the project."
        )
        hideCostCard()
    }

    private suspend fun getRouteSectionPoint(
        currentLocation: LocationModel
    ): String? =
        createViewModel.getRouteSectionPoint(
            currentLocation.latitude,
            currentLocation.longitude,
            newJob!!.UserId.toString(),
            newJob!!.ProjectId,
            newJob!!.JobId
        )

    private fun createItemEstimatePhoto(
        itemEst: JobItemEstimateDTO,
        filePath: Map<String, String>,
        currentLocation: LocationModel?,
        itemIdPhotoType: Map<String, String>,
        pointLocation: Double
    ): JobItemEstimatesPhotoDTO {

        val isPhotoStart = itemIdPhotoType["type"] == PhotoType.START.name
        val photoId: String = SqlLitUtils.generateUuid()

        // newJobItemEstimatesPhotosList.add(newEstimatePhoto)
        return JobItemEstimatesPhotoDTO(
            descr = "",
            estimateId = itemEst.estimateId,
            filename = filePath["filename"] ?: error(""),
            photoDate = DateUtil.DateToString(Date())!!,
            photoId = photoId,
            photoStart = null,
            photoEnd = null,
            startKm = pointLocation,
            endKm = pointLocation,
            photoLatitude = currentLocation!!.latitude,
            photoLongitude = currentLocation.longitude,
            photoLatitudeEnd = currentLocation.latitude,
            photoLongitudeEnd = currentLocation.longitude,
            photoPath = filePath["path"] ?: error(""),
            jobItemEstimate = null,
            recordSynchStateId = 0,
            recordVersion = 0,
            is_PhotoStart = isPhotoStart,
            image = null
        )
    }

    private fun createItemEstimate(
        itemId: String?,
        newJob: JobDTO?,
        item: ItemDTOTemp?

    ): JobItemEstimateDTO {
        val estimateId = SqlLitUtils.generateUuid()

        // newJobItemEstimatesList.add(newEstimate)
        return JobItemEstimateDTO(
            actId = 0,
            estimateId = estimateId,
            jobId = newJob?.JobId,
            lineRate = item!!.tenderRate * item.quantity,
            jobEstimateWorks = null,
            jobItemEstimatePhotos = null, // newJobItemPhotosList,
            jobItemMeasure = null,
            // job = null,
            projectItemId = itemId,
            projectVoId = newJob?.ProjectVoId,
            qty = quantity,
            recordSynchStateId = 0,
            recordVersion = 0,
            trackRouteId = null,
            jobItemEstimatePhotoStart = null,
            jobItemEstimatePhotoEnd = null,
            estimateComplete = null,
            MEASURE_ACT_ID = 0,
            SelectedItemUOM = item.uom
        )
    }

    private fun createRouteSection(
        secId: String,
        jobId: String,
        startKm: Double,
        endKm: Double

    ): JobSectionDTO {
        val newJobSectionId: String = SqlLitUtils.generateUuid()
        return JobSectionDTO(
            jobSectionId = newJobSectionId,
            projectSectionId = secId,
            jobId = jobId,
            startKm = startKm,
            endKm = endKm,
            job = null,
            recordSynchStateId = 0,
            recordVersion = 0
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
            .error(R.drawable.no_image)
            .into(imageView)
        if (animate) imageView.startAnimation(bounce_1000)

        imageView.setOnClickListener {
            showZoomedImage(imageUri)
        }
    } catch (e: Exception) {
        Timber.e(e)
    } finally {

        this.isEstimateDone = newJobItemEstimate?.isEstimateComplete() ?: false

        if (isEstimateDone) {
            costCard.visibility = View.VISIBLE
            updateButton.visibility = View.VISIBLE
            setCost()
        } else {
            sharedViewModel.setMessage("Estimate is incomplete ...")
            hideCostCard()
        }
    }

    private fun establishRouteSectionData(
        isStart: Boolean,
        textView: TextView,
        animate: Boolean
    ) {
        Coroutines.main {
            try {
                createViewModel.sectionId.observe(viewLifecycleOwner, { sectId ->
                    Coroutines.main {
                        val section = createViewModel.getSection(sectId)
                        section.observe(viewLifecycleOwner, { projectSectionDTO ->
                            if (projectSectionDTO != null) {

                                captionEstimateItemPhoto(
                                    projectSectionDTO,
                                    isStart,
                                    textView,
                                    animate
                                )
                            }
                        })
                    }
                })
            } catch (t: Throwable) {
                val secErr = XIError(t, "Failed to caption photo: ${t.message ?: XIErrorHandler.UNKNOWN_ERROR}")
                Timber.e(t, secErr.message)
                XIErrorHandler.crashGuard(this, this.requireView(), secErr, refreshAction = { retryRouteSectionData(isStart, textView, animate) })
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
        isStart: Boolean,
        textView: TextView,
        animate: Boolean
    ) {

        val direction = section.direction
        if (direction != null) {

            val sectionText =
                section.route + " " + section.section + " " + section.direction + " " +
                    if (isStart) section.startKm else section.endKm

            textView.text = sectionText
            if (animate) textView.startAnimation(animations?.bounce_long)
        }
    }

    private fun setValueEditText(qty: Double) {
        when (item?.uom) {
            "m²", "m³", "m" -> {
                val decQty = "" + qty
                valueEditText!!.setText(decQty)
            }
            else -> {
                val intQty: String = "" + qty.toInt()
                valueEditText!!.setText(intQty)
            }
        }
    }

    private fun setCost() {
        if (isEstimateDone) {
            calculateCost()
            valueEditText!!.visibility = View.VISIBLE
            costTextView!!.visibility = View.VISIBLE
            costTextView.startAnimation(animations!!.bounce_soft)
        } else {
            labelTextView!!.text = getString(string.warning_estimate_incomplete)
            labelTextView.startAnimation(animations!!.shake_long)
            valueEditText!!.visibility = View.GONE
            costTextView!!.visibility = View.GONE
        }
    }

    private fun haltAnimation() {
        startAnimationView.visibility = View.GONE
        endAnimationView.visibility = View.GONE
    }

    private fun hideCostCard() {
        costCard.visibility = View.GONE
        updateButton.visibility = View.GONE
    }

    private fun calculateCost() {
        val item: ItemDTOTemp? = item
        val currentStartKm = getStartKm()
        val currentEndKm = getEndKm()

        val value = valueEditText!!.text.toString()
        //  Lose focus on fields
        //  valueEditText.clearFocus()

        var lineRate: Double? = null
        val tenderRate = item?.tenderRate

        var qty = 0.0

        try {
            qty = value.toDouble()
        } catch (e: NumberFormatException) {
            e.printStackTrace()
        }

        when (item!!.uom) {
            "No" -> {
                lineRate = validateNumberCosting(lineRate, qty, tenderRate)
            }
            "m²" -> {
                lineRate = validateAreaCosting(lineRate, qty, tenderRate)
            }
            "m³" -> {
                lineRate = validateVolumeCosting(qty, tenderRate)
            }
            "Prov Sum" -> {
                lineRate = validateProvSumCosting(lineRate, qty, tenderRate)
            }
            "m" -> {
                lineRate =
                    validateLengthCosting(currentEndKm, currentStartKm, lineRate, tenderRate)
            }
            else -> {
                labelTextView!!.text = getString(string.label_quantity)
                try { //  Default Calculation
                    lineRate = qty * tenderRate!!
                } catch (e: NumberFormatException) {
                    requireActivity().hideKeyboard()
                    e.printStackTrace()
                    toast("Please enter the Quantity.")
                }
            }
        }

        costTextView!!.text =
            ("  *   R " + tenderRate.toString() + " =  R " + DecimalFormat("##.##").format(
                lineRate
            ))

        newJobItemEstimate?.qty = qty
        createViewModel.setEstimateQuantity(qty)
        newJobItemEstimate?.lineRate = lineRate!!
    }

    private fun validateLengthCosting(
        currentEndKm: Double,
        currentStartKm: Double,
        lineRate: Double?,
        tenderRate: Double?
    ): Double? {
        var inlineRate = lineRate
        when (labelTextView!!.text) {
            getString(string.label_length_m) ->
                try { //  Set the Area to the QTY
                    val length = (currentEndKm - currentStartKm) * 1000
                    inlineRate = length * tenderRate!!
                } catch (e: NumberFormatException) {
                    requireActivity().hideKeyboard()
                    e.printStackTrace()
                    toast("Please enter the m.")
                }
        }
        return inlineRate
    }

    private fun validateProvSumCosting(
        lineRate: Double?,
        qty: Double,
        tenderRate: Double?
    ): Double? {
        var inlineRate = lineRate
        labelTextView!!.text = getString(string.label_amount)
        try {
            inlineRate = qty * tenderRate!!
        } catch (e: NumberFormatException) {
            requireActivity().hideKeyboard()
            e.printStackTrace()
            toast(getString(string.warning_estimate_enter_prov_sum))
        }
        return inlineRate
    }

    private fun validateVolumeCosting(
        qty: Double,
        tenderRate: Double?
    ): Double? {
        var inlineRate: Double?
        labelTextView!!.text = getString(string.label_volume_m3)
        try {
            inlineRate = qty * tenderRate!!
        } catch (e: NumberFormatException) {
            requireActivity().hideKeyboard()
            e.printStackTrace()
            inlineRate = null
            toast(getString(string.warning_estimate_enter_volume))
        }
        return inlineRate
    }

    private fun validateAreaCosting(
        lineRate: Double?,
        qty: Double,
        tenderRate: Double?
    ): Double? {
        var inlineRate = lineRate
        labelTextView!!.text = getString(string.label_area_m2)
        try {
            inlineRate = qty * tenderRate!!
        } catch (e: NumberFormatException) {
            requireActivity().hideKeyboard()
            e.printStackTrace()
            toast("Please place the Area.")
        }
        return inlineRate
    }

    private fun validateNumberCosting(
        lineRate: Double?,
        qty: Double,
        tenderRate: Double?
    ): Double? {
        var inlineRate = lineRate
        labelTextView!!.text = getString(string.label_quantity)
        try { //  make the change in the array and update view
            inlineRate = qty * tenderRate!!
        } catch (e: NumberFormatException) {
            requireActivity().hideKeyboard()
            e.printStackTrace()
            toast("Please place the Quantity.")
        }
        return inlineRate
    }

    private fun getStoredValue(): Double {
        val jobItemEstimate: JobItemEstimateDTO? = getJobItemEstimate()
        return jobItemEstimate?.qty ?: quantity
    }

    private fun getJobItemEstimate(): JobItemEstimateDTO? {
        return newJobItemEstimate
    }

    private fun getStartKm(): Double {
        val jobItemEstimate: JobItemEstimateDTO? = getJobItemEstimate()
        return if (jobItemEstimate != null && jobItemEstimate.size() > 0) {
            jobItemEstimate.getPhoto(0)?.startKm!!
        } else {
            0.0
        }
    }

    private fun getEndKm(): Double {
        val jobItemEstimate: JobItemEstimateDTO? = getJobItemEstimate()
        return if (jobItemEstimate != null && jobItemEstimate.size() > 1) {
            jobItemEstimate.getPhoto(1)?.endKm!!
        } else {
            0.0
        }
    }

    private fun getCurrentLocation(): LocationModel? {
        return super.getLocation()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.run {
            outState.putString("jobId", newJob?.JobId)
            newJobItemEstimate?.estimateId?.let {
                outState.putString("estimateId", it)
            }
        }
        super.onSaveInstanceState(outState)
        Timber.i("$outState")
    }

    private fun onRestoreInstanceState(inState: Bundle?) {

        val jobId = inState?.getString("jobId")
        estimateId = inState?.getString("estimateId")

        Coroutines.main {
            if (jobId != null) {
                createViewModel.getJob(jobId)
            }
        }
    }

    private fun restoreEstimateViewState() {

        sectionId = newJob?.SectionId
        if (sectionId != null) {
            createViewModel.setSectionId(sectionId!!)
        }
        // Load Photographs
        if (newJobItemEstimate != null) {

            uiScope.launch(uiScope.coroutineContext) {
                try {
                    quantity = newJobItemEstimate!!.qty
                    isEstimateDone = newJobItemEstimate!!.isEstimateComplete()
                    newJobItemEstimate?.jobItemEstimatePhotos?.forEach { photo ->
                        restoreEstimatePhoto(
                            photo.is_PhotoStart
                        )

                        val targetTextView = when (photo.is_PhotoStart) {
                            true -> startSectionTextView
                            else -> endSectionTextView
                        }
                        establishRouteSectionData(photo.is_PhotoStart, targetTextView, false)
                    }

                    if (isEstimateDone) {
                        costCard.visibility = View.VISIBLE
                        updateButton.visibility = View.VISIBLE
                        startKm = getStartKm()
                        endKm = getEndKm()
                        // newJobItemEstimate!!.qty = createViewModel.estimateQty.value!!
                        setValueEditText(quantity)
                    }
                } catch (t: Throwable) {
                    Timber.e(t, "Failed to restore estimate view-state.")
                    val estError = XIError(t, t.message ?: XIErrorHandler.UNKNOWN_ERROR)
                    XIErrorHandler.crashGuard(
                        this@EstimatePhotoFragment,
                        this@EstimatePhotoFragment.requireView(),
                        estError,
                        refreshAction = { restoreEstimateViewState() })
                }
            }
        }
    }

    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 1
        private const val REQUEST_STORAGE_PERMISSION = 1
    }
}
