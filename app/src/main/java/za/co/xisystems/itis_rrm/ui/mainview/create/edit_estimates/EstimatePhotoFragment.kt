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
    private var startKm: Double? = null
    private var endKm: Double? = null
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
    private lateinit var newJobItemEstimate: JobItemEstimateDTO

    @State
    var quantity = 1.0

    private var currentLocation: Location? = null

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
    internal var useR: Int? = null
    private var startImageUri: Uri? = null
    private var endImageUri: Uri? = null
    private var imageUri: Uri? = null

    private lateinit var sharedViewModel: SharedViewModel
    private val shareFactory: SharedViewModelFactory by instance()

    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 1
        private const val REQUEST_STORAGE_PERMISSION = 1
    }
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

        jobItemSectionArrayList = ArrayList<JobSectionDTO>()
        jobItemMeasureArrayList = ArrayList<JobItemMeasureDTO>()
        newJobItemEstimatesList = ArrayList<JobItemEstimateDTO>()
        newJobItemEstimatesPhotosList = ArrayList<JobItemEstimatesPhotoDTO>()
        newJobItemEstimatesWorksList = ArrayList<JobEstimateWorksDTO>()

        jobItemSectionArrayList2 = ArrayList<JobSectionDTO>()
        jobItemMeasureArrayList2 = ArrayList<JobItemMeasureDTO>()

        newJobItemEstimatesPhotosList2 = ArrayList<JobItemEstimatesPhotoDTO>()
        newJobItemEstimatesWorksList2 = ArrayList<JobEstimateWorksDTO>()

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

        createViewModel.job_Item.observe(viewLifecycleOwner, Observer { job_Item ->
            newJob = job_Item

        })

        createViewModel.project_Item.observe(viewLifecycleOwner, Observer { pro_Item ->
            item = pro_Item
            if (item != null) titleTextView.text = "${item!!.itemCode} ${item!!.descr}" else toast(
                "item is null in " + javaClass.simpleName
            )
            if (newJob != null && item != null) {
                val oldJob = newJob!!
                if (oldJob.SectionId != null) {
                    val jobSection = oldJob.SectionId!!
                    createViewModel.sectionId(jobSection)
                }

                setButtonClicks()
                loadPhotos()
            }
        })

        // We'll set the onclickListeners after the images load
        startImageView.setOnClickListener {
            // showZoomedImage(startImageUri)
            null
        }
        endImageView.setOnClickListener {
            // showZoomedImage(endImageUri)
            null
        }


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
                    takePhoto(PhotoType.START)
                }
                R.id.endPhotoButton -> {
                    takePhoto(PhotoType.END)
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
                            newJobItemEstimate.qty = valueEditText.text.toString().toDouble()
                            val qty = newJobItemEstimate.qty
                            newJobItemEstimate.lineRate = (qty * newJobItemEstimate.lineRate)

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

    private fun takePhoto(picType: PhotoType) {

        photoType = picType
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
        // type is "start" or "end"
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
            takePictureIntent.putExtra("photoType", itemIdPhotoType["type"])
            takePictureIntent.putExtra("itemId", itemIdPhotoType["itemId"])
            startActivityForResult(takePictureIntent, AbstractIntent.REQUEST_TAKE_PHOTO)
        }
    }

    private fun loadPhotos() {
        val oldJob = newJob
        Timber.e("$oldJob")
        if (oldJob?.SectionId != null) {

            // Set the RouteSection data
            val oldItemEstimate: JobItemEstimateDTO? = oldJob.getJobEstimateByItemId(item?.itemId)
            val oldSectionId = oldJob.SectionId!!
            createViewModel.sectionId(oldSectionId)

            // Restore the Photographs
            if (oldItemEstimate == null) {
                Timber.d("x -> Error: JobItemEstimate is null")
            } else if ((startImageUri == null || endImageUri == null) && oldItemEstimate.jobItemEstimatePhotos != null) {
                Coroutines.main {
                    restoreEstimatePhoto(oldItemEstimate, true)
                    restoreEstimatePhoto(oldItemEstimate, false)
                }
            }
            isEstimateDone = oldItemEstimate?.isEstimateComplete() ?: false
            if (isEstimateDone) {
                startKm = oldJob.StartKm
                endKm = oldJob.EndKm
                newJobItemEstimate = oldItemEstimate!!
                newJob = oldJob
                setCost()
            }

        }
    }

    private suspend fun restoreEstimatePhoto(
        jobItemEstimate: JobItemEstimateDTO,
        isStart: Boolean
    ) {
        val imageIndex = null ?: getExistingPhotoIndex(
            itemEstimate = jobItemEstimate,
            isPhotoStart = isStart
        )


        val targetImageView = when (isStart) {
            true -> startImageView
            else -> endImageView
        }

        val targetTextView = when (isStart) {
            true -> startSectionTextView
            else -> endSectionTextView
        }


        if (imageIndex != null) {
            val targetUri =
                extractImageUri(jobItemEstimate.jobItemEstimatePhotos?.get(imageIndex))
            updatePhotos(
                imageView = targetImageView,
                imageUri = targetUri,
                animate = true,
                textView = targetTextView,
                isStart = isStart
            )
        }
    }

    private fun extractImageUri(jobItemEstimatePhoto: JobItemEstimatesPhotoDTO?): Uri? {
        if (jobItemEstimatePhoto != null) {
            val path: String = jobItemEstimatePhoto.photoPath
            Timber.d("x -> photo $path")
            if (path != null) {
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
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) { // Process the image and set it to the TextView
            processAndSetImage(item, newJob)
        } else { // Otherwise, delete the temporary image file
            BitmapUtils.deleteImageFile(context!!, filenamePath.toString())
        }
    }

    @SuppressLint("RestrictedApi")
    private fun processAndSetImage(
        item: ItemDTOTemp?,
        newJobDTO: JobDTO?
        // photoType: PhotoType
    ) {


        try { //  Location of picture
            val currentLocation: Location = locationHelper.getCurrentLocation()!!
            if (currentLocation != null) {

                //  Save Image to Internal Storage
                filenamePath = PhotoUtil.saveImageToInternalStorage(
                    activity!!,
                    imageUri!!
                ) as HashMap<String, String>

                processPhotoEstimate(
                    currentLocation = currentLocation,
                    filename_path = filenamePath,
                    itemId_photoType = itemIdPhotoType,
                    item = item,
                    newJobDTO = newJobDTO
                )

                when (photoType) {
                    PhotoType.START -> updatePhotos(
                        imageView = startImageView,
                        imageUri = imageUri.also { startImageUri = it },
                        animate = true,
                        textView = startSectionTextView,
                        isStart = true
                    )
                    PhotoType.END -> updatePhotos(
                        imageView = endImageView,
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
            toast(R.string.error_getting_image)
            Timber.e(e)
            throw e
        }


    }

    private fun processPhotoEstimate(
        currentLocation: Location,
        filename_path: Map<String, String>,
        itemId_photoType: Map<String, String>,
        item: ItemDTOTemp?,
        newJobDTO: JobDTO?
    ) {

        val isPhotoStart = itemId_photoType["type"] == PhotoType.START.name
        val itemId = itemId_photoType["itemId"]


        // create job estimate
        var jobItemEstimate: JobItemEstimateDTO? = newJobDTO?.getJobEstimateByItemId(itemId)
        if (jobItemEstimate == null) {

            jobItemEstimate = createItemEstimate(
                itemId = itemId,
                newJob = newJobDTO,
                newJobItemPhotosList = newJobItemEstimatesPhotosList,
                item = item
            )

            if (newJobDTO?.JobItemEstimates == null) {
                newJobDTO?.JobItemEstimates = ArrayList<JobItemEstimateDTO>()
            }

            newJobDTO?.JobItemEstimates?.add(jobItemEstimate)

            if (jobItemEstimate.jobItemEstimatePhotos == null) {
                jobItemEstimate.jobItemEstimatePhotos = ArrayList<JobItemEstimatesPhotoDTO>()
            }
        }

        val jobPhotoIndex = getExistingPhotoIndex(jobItemEstimate, isPhotoStart)
        isEstimateDone = jobItemEstimate.isEstimateComplete()
        if (ServiceUtil.isNetworkConnected(activity!!.applicationContext)) {

            Coroutines.main {
                getRouteSectionPoint(
                    currentLocation, //       itemEstimate, photo, itemId_photoType,
                    newJobDTO,
                    item
                )

                placeEstimatePhotoInRouteSection(
                    newJobDTO,
                    jobItemEstimate,
                    filename_path,
                    currentLocation,
                    itemId_photoType,
                    jobPhotoIndex
                )
                isEstimateDone = jobItemEstimate.isEstimateComplete()
                if (isEstimateDone) {
                    costCard.visibility = View.VISIBLE
                    updateButton.visibility = View.VISIBLE
                }
            }

        } else {
            val networkToast = Toast.makeText(
                activity?.applicationContext,
                R.string.no_connection_detected,
                Toast.LENGTH_LONG
            )
            networkToast.setGravity(Gravity.CENTER_VERTICAL, 0, 0)
            networkToast.show()
            costCard.visibility = View.GONE
            updateButton.visibility = View.GONE
        }

        validateRouteSection(newJobDTO)

        isEstimateDone = jobItemEstimate.isEstimateComplete()

        if (isEstimateDone) {
            newJob = newJobDTO
            newJobItemEstimate = jobItemEstimate
            setCost()
        }
    }

    private fun validateRouteSection(newJobDTO: JobDTO?) {
        Coroutines.main {

            val section = createViewModel.getPointSectionData(newJobDTO?.ProjectId!!)
            section.observe(this, Observer { sectionPoint ->

                if (sectionPoint == null) {
                    showSectionOutOfBoundError(sectionPoint)
                    costCard.visibility = View.GONE
                    updateButton.visibility = View.GONE
                } else {
                    validateRouteSectionByProject(sectionPoint, newJobDTO)
                }

            })

        }
    }

    private fun validateRouteSectionByProject(
        sectionPoint: SectionPointDTO,
        newJobDTO: JobDTO?
    ) {
        Coroutines.main {
            val sectionID = createViewModel.getSectionByRouteSectionProject(
                sectionPoint.sectionId,
                sectionPoint.linearId,
                this.newJob?.ProjectId
            )
            sectionID.observe(this, Observer { sec_id ->
                Coroutines.main {
                    if (sec_id == null) {
                        toast(R.string.no_section_for_project)
                        costCard.visibility = View.GONE
                        updateButton.visibility = View.GONE
                        return@main
                    } else {
                        costCard.visibility = View.VISIBLE
                        createViewModel.sectionId.value = sec_id
                        val section = createViewModel.getSection(sec_id)
                        section.observe(
                            viewLifecycleOwner,
                            Observer { dbJobSection ->
                                Coroutines.main {
                                    startKm = dbJobSection.startKm
                                    endKm = dbJobSection.endKm

                                    val jobSection = createRouteSection(
                                        sec_id,
                                        newJobDTO!!.JobId,
                                        startKm!!,
                                        endKm!!
                                    )
                                    newJobDTO.JobSections?.add(jobSection)
                                    newJobDTO.SectionId = jobSection.jobSectionId
                                    newJobDTO.StartKm = jobSection.startKm
                                    newJobDTO.EndKm = jobSection.endKm

                                }
                            })
                    }
                }
            })
        }
    }

    private suspend fun placeEstimatePhotoInRouteSection(
        newJobDTO: JobDTO?,
        jobItemEstimate: JobItemEstimateDTO,
        filename_path: Map<String, String>,
        currentLocation: Location,
        itemId_photoType: Map<String, String>,
        jobPhotoIndex: Int?
    ) {
        val sectionPoint = createViewModel.getPointSectionData(newJobDTO?.ProjectId)
        sectionPoint.observe(viewLifecycleOwner, Observer {
            if (it?.direction != null && it.pointLocation != null) {

                val photo = createItemEstimatePhoto(
                    itemEst = jobItemEstimate,
                    filename_path = filename_path,
                    currentLocation = currentLocation,
                    itemIdPhotoType = itemId_photoType,
                    pointLocation = it.pointLocation
                )
                if (jobPhotoIndex != null) {
                    jobItemEstimate.jobItemEstimatePhotos!![jobPhotoIndex] = photo
                } else {
                    jobItemEstimate.jobItemEstimatePhotos!!.add(photo)
                }

            } else {
                this@EstimatePhotoFragment.disableGlide = true
                this@EstimatePhotoFragment.sharedViewModel.setMessage("GPS needed a moment to calibrate. Please retake photograph.")
            }


        })
    }

    private fun getExistingPhotoIndex(
        itemEstimate: JobItemEstimateDTO,
        isPhotoStart: Boolean
    ): Int? {
        val estimatePhotos = itemEstimate.jobItemEstimatePhotos!!.filter { photo ->
            photo.is_PhotoStart == isPhotoStart
        } as ArrayList

        val jobItemEstimatePhoto = if (estimatePhotos.size > 0) estimatePhotos[0] else null

        return if (jobItemEstimatePhoto != null)
            itemEstimate.jobItemEstimatePhotos!!.indexOf(jobItemEstimatePhoto)
        else
            null
    }


    private fun showSectionOutOfBoundError(sectionPoint: SectionPointDTO?) {
        toast(
            "You are not between the start: " + sectionPoint?.pointLocation.toString() +
                    " and end: " + sectionPoint?.pointLocation.toString() + " co-ordinates for the project."
        )
    }


    private suspend fun getRouteSectionPoint(
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
        itemEst: JobItemEstimateDTO,
        filename_path: Map<String, String>,
        currentLocation: Location?,
        itemIdPhotoType: Map<String, String>,
        pointLocation: Double
    ): JobItemEstimatesPhotoDTO {

        val isPhotoStart = itemIdPhotoType["type"] == PhotoType.START.name
        val photoId: String = SqlLitUtils.generateUuid()

        // newJobItemEstimatesPhotosList.add(newEstimatePhoto)
        return JobItemEstimatesPhotoDTO(
            descr = "",
            estimateId = itemEst.estimateId,
            filename = filename_path["filename"] ?: error(""),
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
            photoPath = filename_path["path"] ?: error(""),
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
        newJobItemPhotosList: ArrayList<JobItemEstimatesPhotoDTO>,
        item: ItemDTOTemp?

//                                   jobItemPhoto: JobItemEstimatesPhotoDTO
    ): JobItemEstimateDTO {
        val estimateId: String = SqlLitUtils.generateUuid()

        val newEstimate = JobItemEstimateDTO(
            actId = 0,
            estimateId = estimateId,
            jobId = newJob?.JobId,
            lineRate = item!!.tenderRate,
            jobEstimateWorks = null,
            jobItemEstimatePhotos = newJobItemPhotosList,
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

        newJobItemEstimatesList.add(newEstimate)
        return newEstimate
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
        textView: TextView,
        isStart: Boolean
        //,currentLocation: Location
    ) {
//        imageView: ImageView, imageUri: Uri?, animate: Boolean) {
        if (imageUri != null) {
            Coroutines.main {
                // group13_loading.visibility = View.VISIBLE
                // val works = createViewModel.offlineSectionItems.await()
                // works.observe(viewLifecycleOwner, Observer { works ->
                    group13_loading.visibility = View.GONE
                establishRouteSectionData(isStart, textView, animate)
                if (!disableGlide) {
                    loadEstimateItemPhoto(imageUri, imageView, animate)
                }
                disableGlide = false
                // })


            }

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

        imageView.setOnClickListener {
            null
        }

        Timber.e(e)
    }

    private fun establishRouteSectionData(
        isStart: Boolean,
        textView: TextView,
        animate: Boolean
    ) {
        createViewModel.sectionId.observe(viewLifecycleOwner, Observer { sectId ->
            Coroutines.main {
                val section = createViewModel.getSection(sectId)
                section.observe(viewLifecycleOwner, Observer { section ->
                    if (section != null) {
                        captionEstimateItemPhoto(section, isStart, textView, animate)
                    }

                })

            }

        })
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
            if (animate) textView.startAnimation(anims?.bounce_long)
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
                lineRate = validateNumberCosting(lineRate, qty, item)
            }
            "m²" -> {
                lineRate = validateAreaCosting(lineRate, qty, item)
            }
            "m³" -> {
                lineRate = validateVolumeCosting(lineRate, qty, item)
            }
            "Prov Sum" -> {
                lineRate = validateProvSumCosting(lineRate, qty, item)
            }
            "m" -> {
                lineRate = validateLengthCosting(currentEndKm, currentStartKm, lineRate, item)
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

    private fun validateLengthCosting(
        currentEndKm: Double,
        currentStartKm: Double,
        lineRate: Double?,
        item: ItemDTOTemp
    ): Double? {
        var lineRate1 = lineRate
        labelTextView!!.text = getString(R.string.label_length_m)
        try { //  Set the Area to the QTY
            val length = currentEndKm - currentStartKm
            lineRate1 = length * item.tenderRate
            activity!!.hideKeyboard()
        } catch (e: NumberFormatException) {
            e.printStackTrace()
            toast("Please place the m.")
        }
        return lineRate1
    }

    private fun validateProvSumCosting(
        lineRate: Double?,
        qty: Double,
        item: ItemDTOTemp
    ): Double? {
        var lineRate1 = lineRate
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

    private fun validateVolumeCosting(
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

    private fun validateAreaCosting(
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

    private fun validateNumberCosting(
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

    private fun getJobItemEstimate(): JobItemEstimateDTO? {
        return job?.getJobEstimateByItemId(item!!.itemId)
    }

    private fun getStartKm(): Double {
        val jobItemEstimate: JobItemEstimateDTO? = getJobItemEstimate()
        return if (jobItemEstimate?.jobItemEstimatePhotoStart != null && jobItemEstimate.jobItemEstimatePhotos!!.size > 0) {
            val startIndex: Int? = getExistingPhotoIndex(jobItemEstimate, true)
            if (startIndex != null) jobItemEstimate.jobItemEstimatePhotos!![startIndex].startKm else 0.0
        } else {
            0.0
        }

    }

    private fun getEndKm(): Double {
        val jobItemEstimate: JobItemEstimateDTO? = getJobItemEstimate()
        return if (jobItemEstimate?.jobItemEstimatePhotoStart != null && jobItemEstimate.jobItemEstimatePhotos!!.size > 0) {
            val endIndex: Int? = getExistingPhotoIndex(jobItemEstimate, false)
            if (endIndex != null) jobItemEstimate.jobItemEstimatePhotos!![endIndex].endKm else 0.0
        } else {
            0.0
        }
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
}



