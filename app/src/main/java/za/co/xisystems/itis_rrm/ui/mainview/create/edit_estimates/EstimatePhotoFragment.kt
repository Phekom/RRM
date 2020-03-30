package za.co.xisystems.itis_rrm.ui.mainview.create.edit_estimates

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import icepick.Icepick
import icepick.State
import kotlinx.android.synthetic.main.fragment_photo_estimate.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import timber.log.Timber
import za.co.xisystems.itis_rrm.MainActivity
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.data._commons.AbstractTextWatcher
import za.co.xisystems.itis_rrm.data.localDB.entities.*
import za.co.xisystems.itis_rrm.ui.mainview._fragments.BaseFragment
import za.co.xisystems.itis_rrm.ui.mainview.activities.SharedViewModel
import za.co.xisystems.itis_rrm.ui.mainview.activities.SharedViewModelFactory
import za.co.xisystems.itis_rrm.ui.mainview.create.CreateViewModel
import za.co.xisystems.itis_rrm.ui.mainview.create.CreateViewModelFactory
import za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils.LocationHelper
import za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils.intents.AbstractIntent
import za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils.models.PhotoType
import za.co.xisystems.itis_rrm.utils.*
import za.co.xisystems.itis_rrm.utils.zoomage.ZoomageView
import java.io.File
import java.text.DecimalFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


/**
 * Created by Francis Mahlava on 2019/12/29.
 */

class EstimatePhotoFragment : BaseFragment(R.layout.fragment_photo_estimate), KodeinAware {

    private var sectionId: String? = null
    override val kodein by kodein()
    private lateinit var createViewModel: CreateViewModel
    private val factory: CreateViewModelFactory by instance()


    private var mAppExecutor: AppExecutor? = null
    private lateinit var locationHelper: LocationHelper
    private lateinit var lm: LocationManager
    private var gpsEnabled = false
    private var networkEnabled = false

    private var isEstimateDone: Boolean = false
    private var startKM: Double? = null
    private var endKM: Double? = null
    private var disableGlide: Boolean = false

    @State
    var photoType: PhotoType = PhotoType.START
    @State
    var itemIdPhotoType = HashMap<String, String>()

    internal var job: JobDTO? = null

    @State
    var filenamePath = HashMap<String, String>()
    @State
    private var item: ItemDTOTemp? = null
    @State
    internal var newJob: JobDTO? = null
    @State
    internal var estimate: JobItemEstimateDTO? = null
    var direction: String? = null
    private lateinit var jobItemEstimate: JobItemEstimateDTO

    @State
    var quantity = 1.0

    private var currentLocation: Location? = null

    private lateinit var jobArrayList: ArrayList<JobDTO>
    private lateinit var jobItemEstimatesPhotosList: ArrayList<JobItemEstimatesPhotoDTO>
    private lateinit var jobItemEstimatesWorksList: ArrayList<JobEstimateWorksDTO>
    private lateinit var jobItemEstimatesList: ArrayList<JobItemEstimateDTO>
    private lateinit var jobItemMeasureArrayList: ArrayList<JobItemMeasureDTO>
    private lateinit var jobItemSectionArrayList: ArrayList<JobSectionDTO>
    private lateinit var itemSections: ArrayList<ItemSectionDTO>

    internal var description: String? = null
    internal var useR: Int? = null
    private var startImageUri: Uri? = null
    private var endImageUri: Uri? = null
    private var imageUri: Uri? = null

    private lateinit var sharedViewModel: SharedViewModel
    private val shareFactory: SharedViewModelFactory by instance()


    // TODO: Move non-UI functions to the ViewModel

    override fun onStart() {
        super.onStart()
        locationHelper.onStart()
    }

    override fun onPause() {
        super.onPause()
        locationHelper.onPause()
    }

    override fun onStop() {
        super.onStop()
        locationHelper.onStop()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_photo_estimate, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (activity as MainActivity).supportActionBar?.title = getString(R.string.edit_estimate)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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
        locationHelper = LocationHelper(this)
        locationHelper.onCreate()
        itemSections = ArrayList<ItemSectionDTO>()
        jobArrayList = ArrayList<JobDTO>()
//        jobItemPhoto = JobItemEstimatesPhotoDTO
        jobItemSectionArrayList = ArrayList<JobSectionDTO>()
        jobItemMeasureArrayList = ArrayList<JobItemMeasureDTO>()
        jobItemEstimatesList = ArrayList<JobItemEstimateDTO>()
        jobItemEstimatesPhotosList = ArrayList<JobItemEstimatesPhotoDTO>()
        jobItemEstimatesWorksList = ArrayList<JobEstimateWorksDTO>()
//        newJobItemEstimatesList2 = ArrayList<JobItemEstimateDTO>()


//        jobItemSectionArrayList2 = ArrayList<JobSectionDTO>()
//        jobItemMeasureArrayList2 = ArrayList<JobItemMeasureDTO>()
//        newJobItemEstimatesList2 = ArrayList<JobItemEstimateDTO>()
//        newJobItemEstimatesPhotosList2 = ArrayList<JobItemEstimatesPhotoDTO>()
//        newJobItemEstimatesWorksList2 = ArrayList<JobEstimateWorksDTO>()

    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Icepick.restoreInstanceState(this, savedInstanceState)
        createViewModel = activity?.run {
            ViewModelProvider(this, factory).get(CreateViewModel::class.java)
        } ?: throw Exception("Invalid Activity")


        sharedViewModel = activity?.run {
            ViewModelProvider(this, shareFactory).get(SharedViewModel::class.java)
        } ?: throw Exception("Invalid Activity")

        group13_loading.visibility = View.GONE
        mAppExecutor = AppExecutor()
        lm = activity!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        createViewModel.loggedUser.observe(viewLifecycleOwner, Observer { user ->
            useR = user
        })

        createViewModel.job_Item.observe(viewLifecycleOwner, Observer { jobItem ->
            newJob = jobItem
        })

        createViewModel.project_Item.observe(viewLifecycleOwner, Observer { projectItem ->
            item = projectItem
            if (item != null) titleTextView.text = "${item!!.itemCode} ${item!!.descr}" else toast(
                "item is null in " + javaClass.simpleName
            )
            setButtonClicks()
        })

        startImageView.setOnClickListener {
            showZoomedImage(startImageUri)
        }
        endImageView.setOnClickListener {
            showZoomedImage(endImageUri)
        }

        loadPhotos()
        try {
            networkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (!gpsEnabled) { // notify user && !network_enabled
            displayPromptForEnablingGPS(activity!!)
        }
        setValueEditText(getStoredValue())
        valueEditText!!.addTextChangedListener(object : AbstractTextWatcher() {
            override fun onTextChanged(text: String) {
                setCost()
            }
        })

    }


    private fun setButtonClicks() {

        val myClickListener = View.OnClickListener { view ->
            when (view?.id) {

                R.id.startPhotoButton -> {
                    takePhotoStart()
                }
                R.id.endPhotoButton -> {
                    takePhotoEnd()
                }

                R.id.cancelButton -> {
                    Navigation.findNavController(view)
                        .navigate(R.id.action_estimatePhotoFragment_to_nav_create)
                    Coroutines.main {
                        createViewModel.deleJobfromList(newJob!!.JobId)
                        createViewModel.deleteItemList(newJob!!.JobId)
                        fragmentManager?.beginTransaction()?.remove(this)?.commit()
                        fragmentManager?.beginTransaction()?.detach(this)?.commit()
                    }
                    //TODO(clear temp database Tables for Job And Items)
                }

                R.id.updateButton -> {
                    if (costTextView.text.isNullOrEmpty()) {
                        toast("Please Make Sure you have Captured Both Images To Continue")
                        labelTextView.startAnimation(anims!!.shake_long)
                    } else {
                        Coroutines.main {
                            jobItemEstimate.qty = valueEditText.text.toString().toDouble()
                            val qty = jobItemEstimate.qty
                            jobItemEstimate.lineRate = (qty * jobItemEstimate.lineRate)

                            createViewModel.updateNewJob(
                                newJob!!.JobId,
                                startKM!!,
                                endKM!!,
                                sectionId!!,
                                newJob?.JobItemEstimates!!,
                                newJob?.JobSections!!
                            )
                            updateData(view)
                        }
                    }


                }

            }
        }

        startPhotoButton.setOnClickListener(myClickListener)
        endPhotoButton.setOnClickListener(myClickListener)
        cancelButton.setOnClickListener(myClickListener)
        updateButton.setOnClickListener(myClickListener)

    }

    private fun updateData(view: View) {
        Navigation.findNavController(view)
            .navigate(R.id.action_estimatePhotoFragment_to_addProjectFragment)
    }

    private fun takePhotoStart() {

        photoType = PhotoType.START
        if (ContextCompat.checkSelfPermission(
                activity!!.applicationContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity!!,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_STORAGE_PERMISSION
            )

        } else {
            launchCamera()
        }
    }


    private fun takePhotoEnd() {
        photoType = PhotoType.END
        // Check for the external storage permission
        if (ContextCompat.checkSelfPermission(
                activity!!.applicationContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            != PackageManager.PERMISSION_GRANTED
        ) { // If you do not have permission, request it
            ActivityCompat.requestPermissions(
                Activity(), arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_STORAGE_PERMISSION
            )
        } else { // Launch the camera if the permission exists
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
            .setPositiveButton("OK", DialogInterface.OnClickListener { d, id ->
                activity.startActivity(Intent(action))
                d.dismiss()
            })
        builder.create().show()

    }

    private fun launchCamera() {
        // type is START or END
        if (item != null) {
            itemIdPhotoType["itemId"] = item!!.itemId
            itemIdPhotoType["type"] = photoType.name
        }

        imageUri = PhotoUtil.getUri(this)
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(activity?.packageManager) != null) {
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
            takePictureIntent.putExtra(
                MediaStore.EXTRA_SCREEN_ORIENTATION,
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            )
            startActivityForResult(takePictureIntent, AbstractIntent.REQUEST_TAKE_PHOTO)
        }
    }

    private fun loadPhotos() {
        val existingJobItemEstimate = newJob?.getJobEstimateByItemId(item?.itemId)
        if (existingJobItemEstimate == null) {
            Timber.d(
                "Error: existingJobItemEstimate not find in %s", javaClass.simpleName
            )
        } else if (startImageUri == null || endImageUri == null) {
            startImageUri = extractImageUri(existingJobItemEstimate.jobItemEstimatePhotos?.get(0))
            endImageUri = extractImageUri(existingJobItemEstimate.jobItemEstimatePhotos?.get(1))

        }
        if (startImageUri != null) {
            displayEstimatePhoto(
                imageUri = startImageUri,
                imageView = startImageView,
                animate = false
            )
        }
        if (endImageUri != null) {
            displayEstimatePhoto(imageUri = endImageUri, imageView = endImageView, animate = false)
        }

    }


    //    Bitmap extractBitmap(JobItemEstimatePhoto jobItemEstimatePhoto) {
//        if (jobItemEstimatePhoto != null) {
//            String path = jobItemEstimatePhoto.getPhotoPath();
//            Log.d("x-", "photo " + path);
//            if (path != null) {
//                Bitmap bitmap = BitmapFactory.decodeFile(path);
//                // resize
//                // return bitmap == null ? null : ThumbnailUtils.extractThumbnail(bitmap, 256, 256);
//                return bitmap;
//            }
//        }
//        return null;
//    }
    fun extractImageUri(jobItemEstimatePhoto: JobItemEstimatesPhotoDTO?): Uri? {
        if (jobItemEstimatePhoto != null) {
            val path: String = jobItemEstimatePhoto.photoPath
            Timber.d("x- photo $path")
            if (path != null) {
                val file = File(path)
                // resize
// return bitmap == null ? null : ThumbnailUtils.extractThumbnail(bitmap, 256, 256);
                return Uri.fromFile(file)
            }
        }
        return null
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) { // If the image capture activity was called and was successful
        if (requestCode == Companion.REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) { // Process the image and set it to the TextView
            processAndSetImage(item, newJob)
        } else { // Otherwise, delete the temporary image file
            BitmapUtils.deleteImageFile(context!!, filenamePath.toString())
        }
    }

    @SuppressLint("RestrictedApi")
    private fun processAndSetImage(
        item: ItemDTOTemp?,
        newJobDTO: JobDTO?
    ) {

        when (photoType) {
            PhotoType.START -> updatePhotos(
                startImageView,
                imageUri.also { startImageUri = it },
                true, startSectionTextView, true
            )
            PhotoType.END -> updatePhotos(
                endImageView!!,
                imageUri.also { endImageUri = it },
                true,
                endSectionTextView,
                false
            )

        }
        try { //  Location of picture
            val currentLocation: Location? = locationHelper.getCurrentLocation()
            if (currentLocation == null) toast("Error: Current location is null!")
            //  Save Image to Internal Storage

            filenamePath = PhotoUtil.saveImageToInternalStorage(
                activity!!,
                imageUri!!
            ) as HashMap<String, String>

            processPhotoEstimate(currentLocation!!, filenamePath, itemIdPhotoType, item, newJobDTO)

        } catch (e: Exception) {
            toast(R.string.error_getting_image)
            Timber.e(e)
            e.printStackTrace()
        }


    }


    // TODO: Move non-UI portions of this code to ViewModel
    private fun processPhotoEstimate(
        currentLocation: Location,
        filePath: Map<String, String>,
        itemIdPhotoType: Map<String, String>,
        item: ItemDTOTemp?,
        newJobDTO: JobDTO?
    ) {
        isEstimateDone = false
        val isPhotoStart = itemIdPhotoType["type"] == PhotoType.START.name
        val itemId = itemIdPhotoType["itemId"]


        // create job estimate
        val existingJobItemEstimate = newJobDTO?.getJobEstimateByItemId(itemId)
        if (existingJobItemEstimate == null) {

            jobItemEstimate = createItemEstimate(
                itemId = itemId,
                newJob = newJobDTO,
                // newJobItemPhotosList = jobItemEstimatesPhotosList,
                // jobItemMeasureArrayList = newJobDTO?.JobItemMeasures!!,
                // jobItemWorksList = jobItemEstimatesWorksList,
                item = item
            )!!

            if (newJobDTO?.JobItemEstimates == null) {
                newJobDTO?.JobItemEstimates = ArrayList<JobItemEstimateDTO>()
            }


            newJobDTO?.JobItemEstimates?.add(jobItemEstimate)


            if (ServiceUtil.isNetworkConnected(activity!!.applicationContext)) {

                getRouteSectionPoint(
                    currentLocation = currentLocation,
                    job = newJobDTO,
                    item = item
                )
                getPointSectionData(
                    newJobDTO!!,
                    jobItemEstimate,
                    filePath,
                    currentLocation,
                    itemIdPhotoType,
                    isPhotoStart
                )

            } else {
                val networkToast = Toast.makeText(
                    activity?.applicationContext,
                    R.string.no_connection_detected,
                    Toast.LENGTH_LONG
                )
                networkToast.setGravity(Gravity.CENTER_VERTICAL, 0, 0)
                networkToast.show()

            }


        } else {
            jobItemEstimate = existingJobItemEstimate.copy()
            if (ServiceUtil.isNetworkConnected(activity!!.applicationContext)) {
                // Network connected
                isEstimateDone = jobItemEstimate.isEstimateComplete()

                getRouteSectionPoint(
                    currentLocation = currentLocation,
                    job = newJobDTO,
                    item = item
                )

                getPointSectionData(
                    newJobDTO = newJobDTO,
                    itemEstimate = jobItemEstimate,
                    filePath = filePath,
                    currentLocation = currentLocation,
                    itemIdPhotoType = itemIdPhotoType,
                    isPhotoStart = isPhotoStart
                )

                Coroutines.main {
                    setAndValidateEstimatePhoto(
                        newJobDTO,
                        filePath,
                        currentLocation,
                        itemIdPhotoType,
                        isPhotoStart
                    )

                }

                costCard.visibility = View.VISIBLE
                updateButton.visibility = View.VISIBLE
            } else {
                val networkToast = Toast.makeText(
                    activity?.applicationContext,
                    "Please ensure that you have activated the location on your device",
                    Toast.LENGTH_LONG
                )
                networkToast.setGravity(Gravity.CENTER_VERTICAL, 0, 0)
                networkToast.show()
                costCard.visibility = View.GONE
                updateButton.visibility = View.GONE
            }

        }


        Coroutines.main {
            if (isEstimateDone) {
                finalizeEstimate(newJobDTO)


            }

        }

    }

    private suspend fun finalizeEstimate(newJobDTO: JobDTO?) {
        val section = createViewModel.getPointSectionData(newJobDTO?.ProjectId)
        section.observe(this, Observer { sectionPoint ->
            if (sectionPoint == null) {
                showSectionOutOfBoundError(sectionPoint)
                costCard.visibility = View.GONE
                updateButton.visibility = View.GONE
            } else {
                Coroutines.main {
                    val sectionID = createViewModel.getSectionByRouteSectionProject(
                        sectionPoint.sectionId,
                        sectionPoint.linearId,
                        this.newJob?.ProjectId
                    )
                    sectionID.observe(this, Observer { projectSectionId ->
                        Coroutines.main {
                            if (projectSectionId == null) {
                                toast(R.string.no_section_for_project)
                                costCard.visibility = View.GONE
                                updateButton.visibility = View.GONE
                                return@main
                            } else {
                                costCard.visibility = View.VISIBLE
                                setJobProjectSection(projectSectionId)


                            }

                        }
                    })


                }
            }


        })
    }

    private suspend fun setAndValidateEstimatePhoto(
        newJobDTO: JobDTO?,
        filePath: Map<String, String>,
        currentLocation: Location,
        itemIdPhotoType: Map<String, String>,
        isPhotoStart: Boolean
    ) {
        val section = createViewModel.getPointSectionData(newJobDTO?.ProjectId)
        var direction: String? = null
        section.observe(this, Observer { sectionPoint ->
            direction = sectionPoint.direction ?: ""
            val photo = createItemEstimatePhoto(
                jobItemEstimate,
                filePath,
                currentLocation,
                itemIdPhotoType,
                pointLocation = sectionPoint.pointLocation
            )

            if (photo != null) {

                if (isPhotoStart) {
                    jobItemEstimate.jobItemEstimatePhotos!!.add(0, photo)
                    // jobItemEstimate.setJobItemEstimatePhotoStart(photo)
                } else {
                    jobItemEstimate.jobItemEstimatePhotos!!.add(1, photo)
                    // jobItemEstimate.setJobItemEstimatePhotoEnd(photo)
                }
                isEstimateDone = jobItemEstimate.isEstimateComplete()
            }

        })
    }

    private suspend fun setJobProjectSection(
        projectSectionId: String
        // itemId_photoType: Map<String, String>
    ) {
        createViewModel.sectionId.value = projectSectionId

        val section = createViewModel.getSection(projectSectionId)
        section.observe(viewLifecycleOwner, Observer { section ->

            startKM = section.startKm
            endKM = section.endKm
            sectionId = projectSectionId

            val routeSection = createRouteSection(
                projectSectionId = projectSectionId,
                jobId = this.newJob!!.JobId,
                startKm = startKM!!,
                endKm = endKM!!
            )

            newJob?.JobSections?.add(routeSection)
            newJob?.SectionId = routeSection.projectSectionId
            newJob?.StartKm = routeSection.startKm
            newJob?.EndKm = routeSection.endKm

        })
    }

    private fun getPointSectionData(
        newJobDTO: JobDTO,
        itemEstimate: JobItemEstimateDTO,
        filePath: Map<String, String>,
        currentLocation: Location,
        itemIdPhotoType: Map<String, String>,
        isPhotoStart: Boolean
    ) {
        Coroutines.main {
            createViewModel.getPointSectionData(newJobDTO.ProjectId).apply {

                if (this.value?.direction != null && this.value?.pointLocation != null) {

                    val photo = createItemEstimatePhoto(
                        itemEstimate = itemEstimate,
                        filePath = filePath,
                        currentLocation = currentLocation,
                        // jobItemEstimatesPhotosList,
                        itemIdPhotoType = itemIdPhotoType,
                        // direction = this.value!!.direction,
                        pointLocation = this.value!!.pointLocation
                    )

                    if (isPhotoStart) {
                        itemEstimate.jobItemEstimatePhotos!!.add(0, photo!!)
                        // itemEstimate.setJobItemEstimatePhotoStart(photo)
                    } else {
                        itemEstimate.jobItemEstimatePhotos!!.add(1, photo!!)
                        ///itemEstimate.setJobItemEstimatePhotoEnd(photo)
                    }
                } else {
                    this@EstimatePhotoFragment.disableGlide = true
                    this@EstimatePhotoFragment.sharedViewModel.setMessage("GPS needed a moment to calibrate. Please retake photograph.")
                }
            }
        }
    }

    private fun showSectionOutOfBoundError(sectionPoint: SectionPointDTO?) {
        toast(
            "You are not between the start: " + sectionPoint?.pointLocation.toString() +
                    " and end: " + sectionPoint?.pointLocation.toString() + " co-ordinates for the project."
        )
    }


    private fun getRouteSectionPoint(
        currentLocation: Location,
        job: JobDTO?,
        item: ItemDTOTemp?
    ) {
        Coroutines.main {

            createViewModel.getRouteSectionPoint(

                currentLocation.latitude,
                currentLocation.longitude,
                job!!.UserId.toString(),
                job.ProjectId,
                job.JobId,
                item
            )

        }
    }



    private fun createItemEstimatePhoto(
        itemEstimate: JobItemEstimateDTO,
        filePath: Map<String, String>,
        currentLocation: Location?,
        // jobItemEstimatesPhotosList: ArrayList<JobItemEstimatesPhotoDTO>,
        itemIdPhotoType: Map<String, String>,
        // direction: String?,
        pointLocation: Double
    ): JobItemEstimatesPhotoDTO? {


        val isPhotoStart = itemIdPhotoType["type"] == PhotoType.START.name
        val photoId: String = SqlLitUtils.generateUuid()

        val newEstimatePhoto = JobItemEstimatesPhotoDTO(
            descr = "",
            estimateId = itemEstimate.estimateId,
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
            jobItemEstimate = estimate,
            recordSynchStateId = 0,
            recordVersion = 0,
            is_PhotoStart = isPhotoStart,
            image = null
        )
        return try {
            assert(newEstimatePhoto.estimateId != itemEstimate.estimateId)
            newEstimatePhoto
        } catch (e: AssertionError) {
            Timber.e(
                e,
                "Expecting ${itemEstimate.estimateId}, but got ${newEstimatePhoto.estimateId}."
            )
            null
        }
    }

    // TODO: Move this code to the CreateViewModel

    private fun createItemEstimate(
        itemId: String?,
        newJob: JobDTO?,
        // newJobItemPhotosList: ArrayList<JobItemEstimatesPhotoDTO>,
        // jobItemMeasureArrayList: ArrayList<JobItemMeasureDTO>,
        // jobItemWorksList: ArrayList<JobEstimateWorksDTO>,
        item: ItemDTOTemp?
    ): JobItemEstimateDTO? {
        val estimateId: String = SqlLitUtils.generateUuid()

        val newEstimate = JobItemEstimateDTO(
            actId = 0,
            estimateId = estimateId,
            jobId = newJob?.JobId,
            lineRate = item!!.tenderRate,
            jobEstimateWorks = null,
            jobItemEstimatePhotos = ArrayList<JobItemEstimatesPhotoDTO>(),
            jobItemMeasure = null,
            job = null,
            projectItemId = itemId,
            projectVoId = null,
            qty = quantity,
            recordSynchStateId = 0,
            recordVersion = 0,
            trackRouteId = null,
            jobItemEstimatePhotoStart = null,
            jobItemEstimatePhotoEnd = null,
            estimateComplete = null,
            MEASURE_ACT_ID = 0,
            SelectedItemUOM = null

        )

        // id alignment validation
        return try {
            assert(newJob?.JobId == newEstimate.jobId)
            newEstimate
        } catch (e: AssertionError) {
            Timber.e(e, "Expected ${newJob?.JobId} got ${newEstimate.jobId}")
            null
        }
    }


    // TODO: Move this code to the CreateViewModel
    private fun createRouteSection(
        projectSectionId: String,
        jobId: String,
        startKm: Double,
        endKm: Double

    ): JobSectionDTO {
        val newJobSectionId: String = SqlLitUtils.generateUuid()
        val newJobSection = JobSectionDTO(
            jobSectionId = newJobSectionId,
            projectSectionId = projectSectionId,
            jobId = jobId,
            startKm = startKm,
            endKm = endKm,
            job = null,
            recordSynchStateId = 0,
            recordVersion = 0
        )
        return newJobSection
    }


    private fun showZoomedImage(imageUrl: Uri?) {
        val dialog = Dialog(context!!, R.style.dialog_full_screen)
        dialog.setContentView(R.layout.new_job_photo)
        val zoomageView =
            dialog.findViewById<ZoomageView>(R.id.zoomedImage)
        GlideApp.with(this.activity!!)
            .load(imageUrl)
            .into(zoomageView)
        dialog.show()
    }


    override fun onResume() {
        super.onResume()
        setCost()
    }

    private fun updatePhotos(
        imageView: ImageView,
        imageUri: Uri?,
        animate: Boolean,
        sectionTextView: TextView,
        isStart: Boolean
    ) {
        if (imageUri != null) {
            enumerateOfflineSections(isStart, sectionTextView, animate, imageUri, imageView)
        }
    }

    private fun enumerateOfflineSections(
        isStart: Boolean,
        sectionTextView: TextView,
        animate: Boolean,
        imageUri: Uri?,
        imageView: ImageView
    ) {
        Coroutines.main {
            group13_loading.visibility = View.VISIBLE
            val works = createViewModel.offlineSectionItems.await()
            works.observe(viewLifecycleOwner, Observer { sectionItems ->
                group13_loading.visibility = View.GONE
                createViewModel.sectionId.observe(viewLifecycleOwner, Observer { sectId ->
                    labelPhotoSection(sectId, isStart, sectionTextView, animate)
                })
                displayEstimatePhoto(imageUri, imageView, animate)
            })
        }
    }

    private fun labelPhotoSection(
        sectId: String,
        isStart: Boolean,
        sectionTextView: TextView,
        animate: Boolean
    ) {
        Coroutines.main {
            val section = createViewModel.getSection(sectId)
            section.observe(viewLifecycleOwner, Observer { section ->
                if (section != null) {
                    val direction = section.direction
                    if (direction != null) {

                        val sectionText =
                            section.route + " " + section.section + " " + section.direction + " " +
                                    if (isStart) section.startKm else section.endKm

                        sectionTextView.text = sectionText
                        if (animate) sectionTextView.startAnimation(anims?.bounce_long)
                    }
                }
            })
        }
    }

    private fun displayEstimatePhoto(
        imageUri: Uri?,
        imageView: ImageView,
        animate: Boolean
    ) {
        if (!disableGlide) {
            GlideApp.with(this)
                .load(imageUri)
                .error(R.drawable.no_image)
                .into(imageView)
            if (animate) imageView.startAnimation(bounce_1000)
        } else {
            disableGlide = false
        }
    }

    private fun setValueEditText(qty: Double) {
        when (item?.uom) {
            "m²", "m³", "m" -> valueEditText!!.setText("" + qty)
            else -> valueEditText!!.setText("" + qty.toInt())
        }
    }


    private fun setCost() {
        if (isEstimateDone) {
            calculateCost()
            valueEditText!!.visibility = View.VISIBLE
            costTextView!!.visibility = View.VISIBLE
            costTextView.startAnimation(anims!!.bounce_soft)
        } else {
            labelTextView!!.text = getString(R.string.warning_estimate_incomplete)
            labelTextView.startAnimation(anims!!.shake_long)
            valueEditText!!.visibility = View.GONE
            costTextView!!.visibility = View.GONE
        }
    }


    // TODO: Move Non-UI portions to CreateViewModel
    private fun calculateCost() {
        val item: ItemDTOTemp? = item
        val currentStartKm = getStartKm()
        val currentEndKm = getEndKm()

        val value = valueEditText!!.text.toString()
        //  Lose focus on fields
        valueEditText.clearFocus()
        var lineRate = item?.tenderRate
        var qty = quantity
        try {
            qty = value.toDouble()
        } catch (e: NumberFormatException) {
            e.printStackTrace()
        }

        when (item!!.uom) {
            "No" -> {
                lineRate = runNumberCosting(lineRate, qty, item)
            }
            "m²" -> {
                lineRate = runAreaCosting(lineRate, qty, item)
            }
            "m³" -> {
                lineRate = runVolumeCosting(lineRate, qty, item)
            }
            "Prov Sum" -> {
                labelTextView!!.text = getString(R.string.label_amount)
                try {
                    lineRate = qty * item.tenderRate
                    activity!!.hideKeyboard()
                } catch (e: NumberFormatException) {
                    e.printStackTrace()
                    toast(getString(R.string.warning_estimate_enter_prov_sum))
                }
            }
            "m" -> {
                labelTextView!!.text = getString(R.string.label_length_m)
                try { //  Set the Area to the QTY
                    val length = currentEndKm - currentStartKm
                    lineRate = length * item.tenderRate
                    activity!!.hideKeyboard()
                } catch (e: NumberFormatException) {
                    e.printStackTrace()
                    toast("Please place the m.")
                }
            }
            else -> {
                labelTextView!!.text = getString(R.string.label_quantity)
                try { //  Default Calculation
                    lineRate = qty * item.tenderRate
                    activity!!.hideKeyboard()

                } catch (e: NumberFormatException) {
                    e.printStackTrace()
                    toast("Please place the Quantity.")

                }
            }
        }

        costTextView!!.text =
            ("  *   R " + item.tenderRate.toString() + " =  R " + DecimalFormat("##.##").format(
                lineRate
            ))
        val jobItemEstimate: JobItemEstimateDTO? = getJobItemEstimate()
        if (jobItemEstimate != null) {
            jobItemEstimate.qty
            jobItemEstimate.lineRate
        }
    }

    private fun runVolumeCosting(
        lineRate: Double?,
        qty: Double,
        item: ItemDTOTemp
    ): Double? {
        var lineRate1 = lineRate
        labelTextView!!.text = getString(R.string.label_volume_m3)
        try {
            lineRate1 = qty * item.tenderRate
            activity!!.hideKeyboard()
        } catch (e: NumberFormatException) {
            e.printStackTrace()
            toast(getString(R.string.warning_estimate_enter_volume))

        }
        labelTextView!!.text = getString(R.string.label_amount)
        try {
            lineRate1 = qty * item.tenderRate
            activity!!.hideKeyboard()
        } catch (e: NumberFormatException) {
            e.printStackTrace()
            toast(getString(R.string.warning_estimate_enter_prov_sum))
        }
        return lineRate1
    }

    private fun runAreaCosting(
        lineRate: Double?,
        qty: Double,
        item: ItemDTOTemp
    ): Double? {
        var lineRate1 = lineRate
        labelTextView!!.text = getString(R.string.label_area_m2)
        try {
            lineRate1 = qty * item.tenderRate
            activity!!.hideKeyboard()
        } catch (e: NumberFormatException) {
            e.printStackTrace()
            toast("Please place the Area.")

        }
        labelTextView.text = getString(R.string.label_volume_m3)
        try {
            lineRate1 = qty * item.tenderRate
            activity!!.hideKeyboard()
        } catch (e: NumberFormatException) {
            e.printStackTrace()
            toast(getString(R.string.warning_estimate_enter_volume))

        }
        labelTextView.text = getString(R.string.label_quantity)
        try {
            lineRate1 = qty * item.tenderRate
            activity!!.hideKeyboard()
        } catch (e: NumberFormatException) {
            e.printStackTrace()
            toast(getString(R.string.warning_estimate_enter_prov_sum))
        }
        return lineRate1
    }

    private fun runNumberCosting(
        lineRate: Double?,
        qty: Double,
        item: ItemDTOTemp
    ): Double? {
        var lineRate1 = lineRate
        labelTextView!!.text = getString(R.string.label_quantity)
        try { //  make the change in the array and update view
            lineRate1 = qty * item.tenderRate

        } catch (e: NumberFormatException) {
            e.printStackTrace()
            toast("Please place the Quantity.")
            activity!!.hideKeyboard()
        }
        labelTextView.text = getString(R.string.label_area_m2)
        try { //  Set the Area to the QTY
            lineRate1 = qty * item.tenderRate
            activity!!.hideKeyboard()
        } catch (e: NumberFormatException) {
            e.printStackTrace()
            toast("Please place the Area.")
            activity!!.hideKeyboard()
        }
        labelTextView.text = getString(R.string.label_volume_m3)
        try { //  Set the Area to the QTY
            lineRate1 = qty * item.tenderRate
            activity!!.hideKeyboard()
        } catch (e: NumberFormatException) {
            e.printStackTrace()
            toast(getString(R.string.warning_estimate_enter_volume))
            activity!!.hideKeyboard()
        }
        labelTextView.text = getString(R.string.label_amount)
        try { //  Set the Area to the QTY
            lineRate1 = qty * item.tenderRate
        } catch (e: NumberFormatException) {
            e.printStackTrace()
            toast(getString(R.string.warning_estimate_enter_prov_sum))
            activity!!.hideKeyboard()
        }
        return lineRate1
    }


    private fun getStoredValue(): Double {
        val jobItemEstimate: JobItemEstimateDTO? = getJobItemEstimate()
        return jobItemEstimate?.qty ?: quantity
    }

    fun getJobItemEstimate(): JobItemEstimateDTO? {
        return job?.getJobEstimateByItemId(item!!.itemId)
    }

    fun getStartKm(): Double {
        jobItemEstimate = getJobItemEstimate()!!
        return if (jobItemEstimate.jobItemEstimatePhotos?.get(0) != null) jobItemEstimate.jobItemEstimatePhotos!![0].startKm else 0.0

    }

    fun getEndKm(): Double {
        jobItemEstimate = getJobItemEstimate()!!
        return if (jobItemEstimate.jobItemEstimatePhotos?.get(1) != null) jobItemEstimate.jobItemEstimatePhotos!![1].endKm else 0.0
    }

    fun getCurrentLocation(): Location? {
        return currentLocation
    }

    fun setCurrentLocation(currentLocation: Location?) {
        this.currentLocation = currentLocation
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Icepick.saveInstanceState(this, outState)
    }

    fun onRestoreInstanceState(inState: Bundle) {

        filenamePath =
            inState.getSerializable("filename_path") as HashMap<String, String>
        photoType = inState.getSerializable("photoType") as PhotoType

        loadPhotos()

    }

    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 1
        private const val REQUEST_STORAGE_PERMISSION = 1
    }

}



