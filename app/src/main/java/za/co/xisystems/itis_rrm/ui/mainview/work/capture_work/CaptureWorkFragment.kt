package za.co.xisystems.itis_rrm.ui.mainview.work.capture_work

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.method.KeyListener
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import kotlinx.android.synthetic.main.fragment_capture_work.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import pereira.agnaldo.previewimgcol.ImageCollectionView
import timber.log.Timber
import www.sanju.motiontoast.MotionToast
import za.co.xisystems.itis_rrm.MainActivity
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.base.LocationFragment
import za.co.xisystems.itis_rrm.custom.errors.XIErrorHandler
import za.co.xisystems.itis_rrm.custom.results.XIError
import za.co.xisystems.itis_rrm.custom.results.XIProgress
import za.co.xisystems.itis_rrm.custom.results.XIResult
import za.co.xisystems.itis_rrm.custom.results.XIStatus
import za.co.xisystems.itis_rrm.custom.results.XISuccess
import za.co.xisystems.itis_rrm.custom.views.IndefiniteSnackbar
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobEstimateWorksDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobEstimateWorksPhotoDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimateDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.UserDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.WF_WorkStepDTO
import za.co.xisystems.itis_rrm.extensions.observeOnce
import za.co.xisystems.itis_rrm.services.LocationModel
import za.co.xisystems.itis_rrm.ui.extensions.addZoomedImages
import za.co.xisystems.itis_rrm.ui.extensions.doneProgress
import za.co.xisystems.itis_rrm.ui.extensions.failProgress
import za.co.xisystems.itis_rrm.ui.extensions.initProgress
import za.co.xisystems.itis_rrm.ui.extensions.scaleForSize
import za.co.xisystems.itis_rrm.ui.extensions.showZoomedImage
import za.co.xisystems.itis_rrm.ui.extensions.startProgress
import za.co.xisystems.itis_rrm.ui.mainview.activities.SharedViewModel
import za.co.xisystems.itis_rrm.ui.mainview.activities.SharedViewModelFactory
import za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils.intents.AbstractIntent
import za.co.xisystems.itis_rrm.ui.mainview.work.WorkViewModel
import za.co.xisystems.itis_rrm.ui.mainview.work.WorkViewModelFactory
import za.co.xisystems.itis_rrm.ui.mainview.work.workstate_item.WorkStateItem
import za.co.xisystems.itis_rrm.ui.scopes.UiLifecycleScope
import za.co.xisystems.itis_rrm.utils.ActivityIdConstants
import za.co.xisystems.itis_rrm.utils.Coroutines
import za.co.xisystems.itis_rrm.utils.DataConversion
import za.co.xisystems.itis_rrm.utils.DateUtil
import za.co.xisystems.itis_rrm.utils.PhotoUtil
import za.co.xisystems.itis_rrm.utils.ServiceUtil
import za.co.xisystems.itis_rrm.utils.SqlLitUtils
import za.co.xisystems.itis_rrm.utils.enums.PhotoQuality
import za.co.xisystems.itis_rrm.utils.enums.ToastDuration.LONG
import za.co.xisystems.itis_rrm.utils.enums.ToastGravity.BOTTOM
import za.co.xisystems.itis_rrm.utils.enums.ToastGravity.CENTER
import za.co.xisystems.itis_rrm.utils.enums.ToastStyle.ERROR
import za.co.xisystems.itis_rrm.utils.enums.ToastStyle.NO_INTERNET
import za.co.xisystems.itis_rrm.utils.enums.ToastStyle.WARNING
import za.co.xisystems.itis_rrm.utils.enums.WorkflowDirection
import java.util.Date
import java.util.HashMap

class CaptureWorkFragment : LocationFragment(R.layout.fragment_capture_work), KodeinAware {

    override val kodein by kodein()
    private lateinit var workViewModel: WorkViewModel
    private val factory: WorkViewModelFactory by instance()
    private lateinit var sharedViewModel: SharedViewModel
    private val shareFactory: SharedViewModelFactory by instance()
    private var imageUri: Uri? = null
    private lateinit var workFlowMenuTitles: ArrayList<String>
    private lateinit var groupAdapter: GroupAdapter<GroupieViewHolder>
    private lateinit var estimateWorksPhotoArrayList: ArrayList<JobEstimateWorksPhotoDTO>
    private lateinit var estimateWorksArrayList: ArrayList<JobEstimateWorksDTO>
    private lateinit var estimateWorksList: ArrayList<JobEstimateWorksDTO>
    private lateinit var itemEstimate: JobItemEstimateDTO
    private lateinit var itemEstimateJob: JobDTO
    private var jobitemEsti: JobItemEstimateDTO? = null
    private lateinit var itemEstiWorks: JobEstimateWorksDTO
    private lateinit var jobWorkStep: ArrayList<WF_WorkStepDTO>
    private lateinit var keyListener: KeyListener
    private var uiScope = UiLifecycleScope()
    private var workObserver = Observer<XIResult<String>> { handleWorkSubmission(it) }
    private var jobObserver = Observer<XIResult<String>> { handleJobSubmission(it) }

    private var filenamePath = HashMap<String, String>()
    private var workLocation: LocationModel? = null
    private lateinit var useR: UserDTO
    private lateinit var workSubmission: Job
    private lateinit var jobSubmission: Job
    override fun onStop() {
        uiScope.destroy()
        super.onStop()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(uiScope)
        (activity as MainActivity).supportActionBar?.title = getString(R.string.capture_work_title)
        workFlowMenuTitles = ArrayList()
        groupAdapter = GroupAdapter()
        estimateWorksPhotoArrayList = ArrayList()
        estimateWorksList = ArrayList()
        estimateWorksArrayList = ArrayList()
        jobWorkStep = ArrayList()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_capture_work, container, false)
    }

    override fun onDestroyView() {
        // Remember to flush the RecyclerView's adaptor
        workViewModel.workflowState?.removeObservers(viewLifecycleOwner)
        work_actions_listView.adapter = null
        image_collection_view.clearImages()
        super.onDestroyView()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        workViewModel = activity?.run {
            ViewModelProvider(this, factory).get(WorkViewModel::class.java)
        } ?: throw Exception("Invalid Activity")

        sharedViewModel = activity?.run {
            ViewModelProvider(this, shareFactory).get(SharedViewModel::class.java)
        } ?: throw Exception("Invalid Activity")

        uiScope.launch(uiScope.coroutineContext) {

            val user = workViewModel.user.await()
            user.observe(viewLifecycleOwner, { userDTO ->
                useR = userDTO
            })

            workViewModel.workItemJob.observe(viewLifecycleOwner, { estimateJob ->
                itemEstimateJob = estimateJob
            })

            workViewModel.workItem.observe(viewLifecycleOwner, { estimate ->
                itemEstimate = estimate

                getWorkItems(itemEstimate, itemEstimateJob)
            })
            workViewModel.historicalWorks.observe(viewLifecycleOwner, {
                it?.let { populateHistoricalWorkEstimate(it) }
            })
        }

        image_collection_view.visibility = View.GONE
        take_photo_button.setOnClickListener {
            initCameraLaunch()
        }
        move_workflow_button.setOnClickListener {
            validateUploadWorks()
        }
    }

    private fun populateHistoricalWorkEstimate(result: XIResult<JobEstimateWorksDTO>) {
        when (result) {
            is XISuccess -> {
                val worksData = result.data
                val filenames = worksData.jobEstimateWorksPhotos?.map { photo ->
                    photo.filename
                }

                val photoPairs = filenames?.let {
                    PhotoUtil.prepareGalleryPairs(it, requireActivity().applicationContext)
                }
                photoPairs?.let {
                    image_collection_view.clearImages()
                    image_collection_view.scaleForSize(photoPairs.size)
                    image_collection_view.addZoomedImages(photoPairs, requireActivity())
                    keyListener = comments_editText.keyListener
                    comments_editText.keyListener = null
                    comments_editText.setText(getString(R.string.comment_placeholder), TextView.BufferType.NORMAL)
                    take_photo_button.isClickable = false
                    take_photo_button.background =
                        ContextCompat.getDrawable(requireContext(), R.drawable.round_corner_gray)
                    move_workflow_button.isClickable = false
                    move_workflow_button.background =
                        ContextCompat.getDrawable(requireContext(), R.drawable.round_corner_gray)
                }
            }
            is XIError -> {
                sharedViewModel.setColorMessage(result.message, ERROR, BOTTOM, LONG)
            }
            is XIStatus -> {
                move_workflow_button.text = result.message
            }
            is XIProgress -> {
                when (result.isLoading) {
                    true -> move_workflow_button.startProgress(move_workflow_button.text.toString())
                    else -> move_workflow_button.doneProgress(move_workflow_button.text.toString())
                }
            }
        }
    }

    private fun validateUploadWorks() {

        when (estimateWorksPhotoArrayList.size) {
            0 -> {
                validationNotice(R.string.please_make_sure_workflow_items_contain_photos)
            }
            else -> when (comments_editText.text.trim().isEmpty()) {
                true -> {
                    validationNotice(R.string.please_provide_a_comment)
                }
                else -> {
                    move_workflow_button.isClickable = false
                    uploadEstimateWorksItem()
                    move_workflow_button.isClickable = true
                }
            }
        }
    }

    private fun uploadEstimateWorksItem() {
        if (ServiceUtil.isNetworkAvailable(requireActivity().applicationContext)) { //  Lets Send to Service

            itemEstiWorks.jobEstimateWorksPhotos = estimateWorksPhotoArrayList
            itemEstiWorks.jobItemEstimate = jobitemEsti

            sendWorkToService(itemEstiWorks)
        } else {
            sharedViewModel.setColorMessage(
                msg = getString(R.string.no_connection_detected),
                style = NO_INTERNET,
                position = CENTER,
                duration = LONG
            )
            move_workflow_button.failProgress("Network down ...")
        }
    }

    private fun validationNotice(stringId: Int) {
        sharedViewModel.setColorMessage(
            msg = getString(stringId),
            style = WARNING,
            position = CENTER,
            duration = LONG
        )
    }

    private fun initCameraLaunch() {
        if (ContextCompat.checkSelfPermission(
                requireActivity().applicationContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                Activity(),
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_STORAGE_PERMISSION
            )
        } else {
            launchCamera()
        }
    }

    private fun sendWorkToService(
        itemEstiWorks: JobEstimateWorksDTO
    ) {
        uiScope.launch(uiScope.coroutineContext) {
            workViewModel.workflowState?.observe(viewLifecycleOwner, workObserver)
            workViewModel.backupWorkSubmission.postValue(itemEstiWorks)
            val newItemEstimateWorks = setJobWorksLittleEndianGuids(itemEstiWorks)
            workSubmission = workViewModel.submitWorks(newItemEstimateWorks, requireActivity(), itemEstimateJob)
            workSubmission.join()
        }
    }

    /**
     * Handler routine for submitting completed work for a job.
     */
    private fun handleJobSubmission(outcome: XIResult<String>?) {
        outcome?.let { result ->
            when (result) {
                is XISuccess<String> -> {
                    if (result.data == "WORK_COMPLETE") {
                        popViewOnJobSubmit(WorkflowDirection.NEXT.value)
                    }
                }
                is XIError -> {
                    XIErrorHandler.crashGuard(
                        fragment = this,
                        view = this.requireView(),
                        throwable = result,
                        refreshAction = { this.retryJobSubmission() })
                }
                is XIStatus -> {
                    sharpToast(
                        result.message,
                        MotionToast.TOAST_INFO,
                        position = MotionToast.GRAVITY_TOP,
                        duration = MotionToast.SHORT_DURATION
                    )
                }
                is XIProgress -> {
                    when (result.isLoading) {
                        true -> move_workflow_button.startProgress(move_workflow_button.text.toString())
                        else -> move_workflow_button.doneProgress(move_workflow_button.text.toString())
                    }
                }
            }
        }
    }

    private var estimateSize = 0
    private var estimateCount = 0

    /**
     * Handler for submitting a completed work step
     */
    private fun handleWorkSubmission(outcome: XIResult<String>?) {
        outcome?.let { result ->
            when (result) {
                is XISuccess -> {
                    sharpToast(
                        "Work captured",
                        motionType = MotionToast.TOAST_SUCCESS,
                        position = MotionToast.GRAVITY_BOTTOM,
                        duration = MotionToast.SHORT_DURATION
                    )
                    move_workflow_button.doneProgress("Workflow complete")
                    refreshView()
                }
                is XIError -> {
                    XIErrorHandler.crashGuard(
                        fragment = this@CaptureWorkFragment,
                        view = this@CaptureWorkFragment.requireView(),
                        throwable = result,
                        refreshAction = { this@CaptureWorkFragment.retryWorkSubmission() }
                    )
                }
                is XIStatus -> {
                    sharpToast(
                        result.message,
                        MotionToast.TOAST_INFO,
                        position = MotionToast.GRAVITY_TOP,
                        duration = MotionToast.SHORT_DURATION
                    )
                }
                is XIProgress -> {
                    when (result.isLoading) {

                        true -> {
                            move_workflow_button.initProgress(viewLifecycleOwner)
                            move_workflow_button.startProgress(move_workflow_button.text.toString())
                        }
                        else -> move_workflow_button.doneProgress(move_workflow_button.text.toString())
                    }
                }
            }
        }
    }

    private fun retryWorkSubmission() {
        IndefiniteSnackbar.hide()
        val backupWorkSubmission = workViewModel.backupWorkSubmission
        backupWorkSubmission.observeOnce(viewLifecycleOwner, {
            it?.let {
                itemEstiWorks = it
                sendWorkToService(itemEstiWorks)
            }
        })
    }

    /**
     * Prepare EstimateWorks entry for transport to the backend
     * @param works JobEstimateWorksDTO
     * @return JobEstimateWorksDTO
     */
    private fun setJobWorksLittleEndianGuids(works: JobEstimateWorksDTO): JobEstimateWorksDTO {

        works.setWorksId(DataConversion.toLittleEndian(works.worksId))
        works.setEstimateId(DataConversion.toLittleEndian(works.estimateId))
        works.setTrackRouteId(DataConversion.toLittleEndian(works.trackRouteId))
        if (works.jobEstimateWorksPhotos != null) {
            for (ewp in works.jobEstimateWorksPhotos!!) {
                ewp.setWorksId(DataConversion.toLittleEndian(ewp.worksId))
                ewp.setPhotoId(DataConversion.toLittleEndian(ewp.photoId))
            }
        }
        return works
    }

    private fun refreshView() {
        groupAdapter.clear()
        image_collection_view.clearImages()
        estimateWorksPhotoArrayList.clear()
        comments_editText.setText("")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

            fragmentManager?.beginTransaction()?.detach(this)?.commitNow()
            fragmentManager?.beginTransaction()?.attach(this)?.commitNow()
        } else {
            fragmentManager?.beginTransaction()?.detach(this)?.attach(this)?.commit()
        }

        // Await the updated estimate record
        uiScope.launch(uiScope.coroutineContext) {
            workViewModel.workItem.observe(viewLifecycleOwner, {
                Timber.d("$it")

                val id = 3
                // This part must be Deleted when the Dynamic workflow is complete.
                uiScope.launch(uiScope.coroutineContext) {
                    val workCodeData = workViewModel.getWorkFlowCodes(id)
                    workCodeData.observe(viewLifecycleOwner, {
                        groupAdapter.notifyItemChanged(2)
                        Timber.d("IsRefresh -> Yes")
                    })
                }
            })
        }
    }

    private fun launchCamera() {

        uiScope.launch(uiScope.coroutineContext) {
            imageUri = PhotoUtil.getUri3(requireActivity().applicationContext)!!
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (takePictureIntent.resolveActivity(requireActivity().packageManager) != null) {
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
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            // Process the image and set it to the TextView
            processAndSetImage(itemEstiWorks)
            image_collection_view.visibility = View.VISIBLE
        }
    }

    private fun processAndSetImage(itemEstiWorks: JobEstimateWorksDTO) {
        try { //  Location of picture
            val currentLocation: LocationModel? = this.getCurrentLocation()
            Timber.d("$currentLocation")
            when (currentLocation != null) {
                true -> {
                    filenamePath = PhotoUtil.saveImageToInternalStorage(
                        requireActivity(),
                        imageUri!!
                    ) as HashMap<String, String>

                    processPhotoWorks(currentLocation, filenamePath, itemEstiWorks)

                    groupAdapter.notifyItemChanged(0)
                }
                else -> this.sharpToast("Error: Current location is null!", MotionToast.TOAST_ERROR)
            }
        } catch (e: Exception) {
            sharedViewModel.setColorMessage(
                getString(R.string.error_getting_image),
                ERROR,
                CENTER,
                LONG
            )

            e.printStackTrace()
        }
    }

    private fun processPhotoWorks(
        currentLocation: LocationModel?,
        filenamePath: HashMap<String, String>,
        itemEstiWorks: JobEstimateWorksDTO
    ) {

        if (currentLocation == null) {
            // Check network availability / connectivity
            sharedViewModel.setColorMessage("Please enable location services.", ERROR, CENTER, LONG)
            // Launch Dialog
        } else {
            // requireMutex
            val photo = createItemWorksPhoto(
                filenamePath,
                currentLocation
            )
            estimateWorksPhotoArrayList.add(photo)
            // unlock mutex
            uiScope.launch(uiScope.coroutineContext) {
                itemEstiWorks.jobEstimateWorksPhotos = estimateWorksPhotoArrayList
                workViewModel.createSaveWorksPhotos(
                    estimateWorksPhotoArrayList,
                    itemEstiWorks
                )

                // Get imageUri from filename
                val imageUrl = PhotoUtil.getPhotoPathFromExternalDirectory(
                    photo.filename
                )

                // Generate Bitmap from file
                val bitmap =
                    PhotoUtil.getPhotoBitmapFromFile(
                        requireActivity(),
                        imageUrl,
                        PhotoQuality.HIGH
                    )

                // Push photo into ImageCollectionView
                image_collection_view.addImage(
                    bitmap!!,
                    object : ImageCollectionView.OnImageClickListener {
                        override fun onClick(bitmap: Bitmap, imageView: ImageView) {
                            showZoomedImage(imageUrl, this@CaptureWorkFragment.requireActivity())
                        }
                    })

                image_collection_view.scaleForSize(
                    estimateWorksPhotoArrayList.size
                )
            }
        }
    }

    private fun createItemWorksPhoto(
        filenamePath: HashMap<String, String>,
        currentLocation: LocationModel
    ): JobEstimateWorksPhotoDTO {
        val photoId = SqlLitUtils.generateUuid()

        return JobEstimateWorksPhotoDTO(
            Id = 0,
            descr = "",
            filename = filenamePath["filename"]!!,
            photoActivityId = itemEstiWorks.actId,
            photoDate = DateUtil.DateToString(Date())!!,
            photoId = photoId,
            photoLongitude = currentLocation.latitude,
            photoLatitude = currentLocation.longitude,
            photoPath = filenamePath["path"]!!,
            estimateWorks = estimateWorksList,
            recordVersion = 0,
            recordSynchStateId = 0,
            worksId = itemEstiWorks.worksId
        )
    }

    private fun getWorkItems(
        estimateItem: JobItemEstimateDTO,
        estimateJob: JobDTO
    ) {
        uiScope.launch(uiScope.coroutineContext) {

            val workDone: Int = getEstimatesCompleted(estimateJob)

            if (workDone == estimateJob.JobItemEstimates?.size) {
                collectCompletedEstimates(estimateJob)
            } else {

                val estimateWorksData =
                    workViewModel.getJobEstiItemForEstimateId(estimateItem.estimateId)
                estimateWorksData.observe(viewLifecycleOwner, { estimateWorksList ->

                    for (workItem in estimateWorksList) {
                        if (workItem.actId == ActivityIdConstants.EST_WORKS_COMPLETE) {
                            uiScope.launch(uiScope.coroutineContext) {
                                val estWorkDone: Int =
                                    getEstimatesCompleted(estimateJob)
                                submitEstimatesOrPop(estWorkDone, estimateJob)
                            }
                        } else {

                            generateWorkflowSteps(estimateWorksList)
                        }
                        itemEstiWorks = workItem
                    }
                    estimateWorksArrayList = estimateWorksList as ArrayList<JobEstimateWorksDTO>
                })
            }
        }
    }

    private fun submitEstimatesOrPop(
        estWorkDone: Int,
        estimateJob: JobDTO
    ) {
        if (estWorkDone == estimateJob.JobItemEstimates?.size) {
            Coroutines.main {
                collectCompletedEstimates(estimateJob)
            }
        } else {
            popViewOnWorkSubmit(this.requireView())
        }
    }

    private suspend fun collectCompletedEstimates(estimateJob: JobDTO) {
        val iItems = workViewModel.getJobEstimationItemsForJobId(
            estimateJob.JobId,
            ActivityIdConstants.ESTIMATE_WORK_PART_COMPLETE
        )
        iItems.observe(viewLifecycleOwner, {
            it?.let {
                submitAllOutStandingEstimates(it as ArrayList<JobItemEstimateDTO>)
            }
        })
    }

    private fun generateWorkflowSteps(estimateWorksList: List<JobEstimateWorksDTO>) {
        // Remove for Dynamic Workflow
        val id = ActivityIdConstants.JOB_APPROVED
        uiScope.launch(uiScope.coroutineContext) {
            val workflowStepData = workViewModel.getWorkFlowCodes(id)
            workflowStepData.observe(
                viewLifecycleOwner,
                { workflowSteps ->
                    jobWorkStep = workflowSteps as ArrayList<WF_WorkStepDTO>

                    initRecyclerView(
                        estimateWorksList.toWorkStateItems(),
                        workflowSteps
                    )
                })
        }
    }

    private suspend fun getEstimatesCompleted(estimateJob: JobDTO): Int {
        return workViewModel.getJobItemsEstimatesDoneForJobId(
            estimateJob.JobId,
            ActivityIdConstants.ESTIMATE_WORK_PART_COMPLETE,
            ActivityIdConstants.EST_WORKS_COMPLETE
        )
    }

    private fun popViewOnWorkSubmit(view: View) {
        Navigation.findNavController(view)
            .navigate(R.id.action_captureWorkFragment_to_nav_work)
    }

    private fun submitAllOutStandingEstimates(estimates: ArrayList<JobItemEstimateDTO>?) {
        // get Data from db Search for all estimates 8 and work 21 = result is int > 0  then button yes else fetch

        if (!estimates.isNullOrEmpty()) {
            val dialogBuilder: AlertDialog.Builder = AlertDialog.Builder(requireActivity())
            dialogBuilder.setTitle(R.string.confirm)
            dialogBuilder.setIcon(R.drawable.ic_error)
            dialogBuilder.setMessage("Work Complete - Submit for Measurements")
            dialogBuilder.setCancelable(false)
            dialogBuilder.setPositiveButton(
                R.string.yes
            ) { dialog, which ->

                pushCompletedEstimates(estimates)
            }

            dialogBuilder.show()
        }
    }

    private fun retryJobSubmission() {
        IndefiniteSnackbar.hide()
        val retryJobData = workViewModel.backupCompletedEstimates
        retryJobData.observeOnce(viewLifecycleOwner, {
            it?.let {
                pushCompletedEstimates(it as ArrayList<JobItemEstimateDTO>)
            }
        })
    }

    private fun pushCompletedEstimates(estimates: ArrayList<JobItemEstimateDTO>, workStep: Boolean = true) {
        estimateSize = estimates.size
        estimateCount = 0
        errorState = false

        workViewModel.backupCompletedEstimates.postValue(estimates as List<JobItemEstimateDTO>)
        Coroutines.main {
            workViewModel.workflowState?.observe(viewLifecycleOwner, jobObserver)
            workViewModel.workflowState?.postValue(XIProgress(true))
            jobSubmission = uiScope.launch(uiScope.coroutineContext) {
                withContext(uiScope.coroutineContext) {

                    for (jobEstimate in estimates) {
                        Timber.d("Id: ${jobEstimate.estimateId}")
                        val convertedId = DataConversion.toBigEndian(jobEstimate.estimateId)
                        Timber.d("Converted Id: $convertedId")
                        val jobItemEstimate = workViewModel.getJobItemEstimateForEstimateId(jobEstimate.estimateId)
                        jobItemEstimate.observe(viewLifecycleOwner, { jobItEstmt ->
                            jobItEstmt?.let {
                                Coroutines.main {
                                    withContext(uiScope.coroutineContext) {
                                        moveJobItemEstimateToNextWorkflow(
                                            WorkflowDirection.NEXT,
                                            it
                                        )
                                    }
                                }
                            }
                        })
                    }
                }
            }
            jobSubmission.join()
            handleJobSubmission(XISuccess("WORK_COMPLETE"))
        }
    }

    private var errorState = false
    private suspend fun moveJobItemEstimateToNextWorkflow(
        workflowDirection: WorkflowDirection,
        jobItEstimate: JobItemEstimateDTO?
    ) {

        val user = workViewModel.user.await()
        user.observe(viewLifecycleOwner, { userDTO ->

            when {
                userDTO.userId.isBlank() -> {
                    sharedViewModel.setColorMessage(
                        "Error: current user lacks permissions",
                        ERROR,
                        CENTER,
                        LONG
                    )
                    move_workflow_button.failProgress("Workflow failed ...")
                }
                jobItEstimate?.jobId == null -> {
                    sharedViewModel.setColorMessage(
                        "Error: selected job is invalid",
                        ERROR,
                        CENTER,
                        LONG
                    )
                    move_workflow_button.failProgress("Workflow failed ...")
                }
                else -> {
                    val trackRouteId: String =
                        DataConversion.toLittleEndian(jobItEstimate.trackRouteId)!!
                    val direction: Int = workflowDirection.value

                    Coroutines.main {
                        val estimateJob = uiScope.launch(uiScope.coroutineContext) {
                            workViewModel.processWorkflowMove(
                                userDTO.userId,
                                trackRouteId,
                                null,
                                direction
                            )
                        }
                        estimateJob.join()
                    }
                }
            }
        })
    }

    private fun popViewOnJobSubmit(direction: Int) {
        if (direction == WorkflowDirection.NEXT.value) {
            this.sharpToast(
                R.string.work_complete,
                MotionToast.TOAST_SUCCESS,
                MotionToast.GRAVITY_TOP,
                MotionToast.LONG_DURATION
            )
        } else if (direction == WorkflowDirection.FAIL.value) {
            this.sharpToast(
                getString(R.string.work_declined),
                MotionToast.TOAST_INFO,
                MotionToast.GRAVITY_TOP,
                MotionToast.LONG_DURATION
            )
        }
        Intent(activity, MainActivity::class.java).also { home ->
            startActivity(home)
        }
    }

    private fun initRecyclerView(
        stateItems: List<WorkStateItem>,
        workCodes: List<WF_WorkStepDTO>
    ) {
        groupAdapter.apply {
            Coroutines.main {
                groupAdapter.clear()
                for (i in workCodes.indices) { // stateItems.indices
                    add(stateItems[0])
                    groupAdapter.notifyDataSetChanged()
                }
            }
        }
        work_actions_listView.apply {
            layoutManager = LinearLayoutManager(this.context)
            adapter = groupAdapter
        }
    }

    private fun List<JobEstimateWorksDTO>.toWorkStateItems(): List<WorkStateItem> {

        return this.map { approveJobItems ->
            WorkStateItem(approveJobItems, activity, groupAdapter, jobWorkStep, workViewModel)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (activity as MainActivity).supportActionBar?.title = getString(R.string.capture_work_title)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    private fun getCurrentLocation(): LocationModel? {
        return super.getLocation()
    }

    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 1
        private const val REQUEST_STORAGE_PERMISSION = 1
    }
}

private fun JobEstimateWorksPhotoDTO.setWorksId(toLittleEndian: String?) {
    this.worksId = toLittleEndian!!
}

private fun JobEstimateWorksPhotoDTO.setPhotoId(toLittleEndian: String?) {
    this.photoId = toLittleEndian!!
}

private fun JobEstimateWorksDTO.setWorksId(toLittleEndian: String?) {
    this.worksId = toLittleEndian!!
}

private fun JobEstimateWorksDTO.setEstimateId(toLittleEndian: String?) {
    this.estimateId = toLittleEndian
}

private fun JobEstimateWorksDTO.setTrackRouteId(toLittleEndian: String?) {
    this.trackRouteId = toLittleEndian!!
}
