package za.co.xisystems.itis_rrm.ui.mainview.work.capture_work

import android.Manifest.permission
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.method.KeyListener
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView.BufferType.NORMAL
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.viewbinding.GroupieViewHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.instance
import timber.log.Timber
import za.co.xisystems.itis_rrm.ui.mainview.activities.main.MainActivity
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.base.LocationFragment
import za.co.xisystems.itis_rrm.constants.Constants
import za.co.xisystems.itis_rrm.custom.errors.XIErrorHandler
import za.co.xisystems.itis_rrm.custom.notifications.ToastDuration
import za.co.xisystems.itis_rrm.custom.notifications.ToastGravity
import za.co.xisystems.itis_rrm.custom.notifications.ToastStyle
import za.co.xisystems.itis_rrm.custom.results.XIResult
import za.co.xisystems.itis_rrm.custom.views.IndefiniteSnackbar
import za.co.xisystems.itis_rrm.data._commons.utils.SqlLitUtils
import za.co.xisystems.itis_rrm.data.localDB.entities.*
import za.co.xisystems.itis_rrm.databinding.FragmentCaptureWorkBinding
import za.co.xisystems.itis_rrm.databinding.ListSelectorBinding
import za.co.xisystems.itis_rrm.extensions.checkLocationProviders
import za.co.xisystems.itis_rrm.extensions.isConnected
import za.co.xisystems.itis_rrm.extensions.observeOnce
import za.co.xisystems.itis_rrm.services.LocationModel
import za.co.xisystems.itis_rrm.ui.extensions.*
import za.co.xisystems.itis_rrm.ui.mainview.work.WorkViewModel
import za.co.xisystems.itis_rrm.ui.mainview.work.WorkViewModelFactory
import za.co.xisystems.itis_rrm.ui.mainview.work.workstate_item.WorkStateItem
import za.co.xisystems.itis_rrm.utils.*
import za.co.xisystems.itis_rrm.utils.enums.WorkflowDirection
import java.util.*

class CaptureWorkFragment : LocationFragment() {

    private lateinit var workViewModel: WorkViewModel
    private val factory: WorkViewModelFactory by instance()
    private val photoUtil: PhotoUtil by instance()
    private var imageUri: Uri? = null
    private lateinit var workFlowMenuTitles: ArrayList<String>
    private lateinit var groupAdapter: GroupAdapter<GroupieViewHolder<ListSelectorBinding>>
    private lateinit var estimateWorksPhotoArrayList: ArrayList<JobEstimateWorksPhotoDTO>
    private lateinit var estimateWorksArrayList: ArrayList<JobEstimateWorksDTO>
    private lateinit var estimateWorksList: ArrayList<JobEstimateWorksDTO>
    private lateinit var itemEstimate: JobItemEstimateDTO
    private lateinit var itemEstimateJob: JobDTO
    private lateinit var activeWorks: JobEstimateWorksDTO
    private lateinit var jobWorkStep: ArrayList<WfWorkStepDTO>
    private lateinit var keyListener: KeyListener
    private var workObserver = Observer<XIResult<String>?> { handleWorkSubmission(it) }
    private var jobObserver = Observer<XIResult<String>?> { handleJobSubmission(it) }
    private var filenamePath = HashMap<String, String>()
    private lateinit var useR: UserDTO
    private lateinit var jobSubmission: Job
    private var _ui: FragmentCaptureWorkBinding? = null
    private val ui get() = _ui!!
    private var estimateSize = 0
    private var estimateCount = 0
    private var errorState = false
    private var measuresShowing = false

    /**
     * ActivityResultContract for taking a photograph
     */

    private val takePicture = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isSaved ->
        if (isSaved) {
            if (this@CaptureWorkFragment::activeWorks.isInitialized) {
                imageUri?.let { realUri ->
                    processAndSetImage(activeWorks, realUri).also {
                        this@CaptureWorkFragment.photosDone()
                    }
                }
            } else {
                showWorkIncomplete()
            }
        } else {
            Coroutines.io {
                photoUtil.deleteImageFile(filenamePath.toString()).also {
                    this@CaptureWorkFragment.photosDone()
                }
            }
        }
    }

    var stateRestored: Boolean = false

    companion object {
        private const val REQUEST_STORAGE_PERMISSION = 1
        private const val STANDARD_WORKFLOW_STEPS = 3
        const val JOB_KEY = "jobId"
        const val ESTIMATE_KEY = "estimateId"
    }

    private val backClickListener = View.OnClickListener {
        setBackPressed(it)
    }

    private fun setBackPressed(view : View) {
        Coroutines.io {
            withContext(Dispatchers.Main.immediate) {
                val navDirection = CaptureWorkFragmentDirections.actionCaptureWorkFragmentToNavigationWork(itemEstimateJob.jobId)
                Navigation.findNavController(view).navigate(navDirection)
            }
        }
    }

    private fun showWorkIncomplete() = uiScope.launch(dispatchers.main()) {
        this@CaptureWorkFragment.extensionToast(
            title = "Work data is incomplete",
            message = "Please contact support about this job, and have them restore or remove it",
            style = ToastStyle.DELETE,
            duration = ToastDuration.LONG,
            position = ToastGravity.CENTER
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        workFlowMenuTitles = ArrayList()
        estimateWorksPhotoArrayList = ArrayList()
        estimateWorksList = ArrayList()
        estimateWorksArrayList = ArrayList()
        jobWorkStep = ArrayList()
        groupAdapter = GroupAdapter<GroupieViewHolder<ListSelectorBinding>>()
//        (activity as MainActivity).supportActionBar?.title = getString(R.string.capture_work_title) + " "
        workViewModel = ViewModelProvider(this.requireActivity(), factory)[WorkViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _ui = FragmentCaptureWorkBinding.inflate(inflater, container, false)

        _ui?.toolbar?.apply {
            setTitle( getString(R.string.capture_work_title))
            setOnBackClickListener(backClickListener)
        }
        return ui.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Remember to flush the RecyclerView's adaptor
        workViewModel.workflowState.removeObservers(viewLifecycleOwner)
        ui.workActionsListView.adapter = null
        ui.imageCollectionView.clearImages()
        _ui = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        stateRestored = false
        ui.commentsEditText.filters = arrayOf(ValidateInputs.EMOJI_FILTER)
        val args: CaptureWorkFragmentArgs by navArgs()

        args.jobId?.let {
            onRestoreInstanceState(args.toBundle())
        }

        if (!stateRestored && savedInstanceState != null) {
            onRestoreInstanceState(savedInstanceState)
        }
    }

    private fun bindUI() {
        ui.imageCollectionView.clearImages()
        estimateWorksPhotoArrayList = ArrayList()

        ui.takePhotoButton.setOnClickListener {
            initLaunchCamera()
        }
        ui.moveWorkflowButton.setOnClickListener {
            validateUploadWorks()
        }
    }

    override fun onStart() {
        super.onStart()
        bindUI()
        pullData()
        this.checkLocationProviders()
    }

    private fun pullData() = uiScope.launch(dispatchers.main()) {

        val user = workViewModel.user.await()
        user.observe(viewLifecycleOwner, { userDTO ->
            useR = userDTO
        })

        workViewModel.workItemJob.observe(viewLifecycleOwner, { estimateJob ->
            estimateJob?.let {
                itemEstimateJob = it
            }
        })

        workViewModel.workItem.observe(viewLifecycleOwner, { estimate ->
            estimate?.let {
                itemEstimate = it
                if (this@CaptureWorkFragment::itemEstimateJob.isInitialized) {
                    getWorkItems(itemEstimate, itemEstimateJob)
                }
            }
        })

        workViewModel.historicalWorks.observe(viewLifecycleOwner, {
            it?.let { populateHistoricalWorkEstimate(it) }
        })
    }

    private fun onRestoreInstanceState(inState: Bundle) {
        inState.run {
            val jobId = getString(JOB_KEY, "")
            val estimateId = getString(ESTIMATE_KEY, "")
            if (jobId.isNotBlank() && estimateId.isNotBlank()) {
                workViewModel.setWorkItemJob(jobId)
                workViewModel.setWorkItem(estimateId)
            }
            stateRestored = true
        }
    }

    private fun populateHistoricalWorkEstimate(result: XIResult<JobEstimateWorksDTO>) {
        when (result) {
            is XIResult.Success -> {
                handleLoadingSuccess(result)
            }
            is XIResult.Error -> {
                extensionToast(
                    message = result.message,
                    style = ToastStyle.ERROR,
                    position = ToastGravity.BOTTOM,
                    duration = ToastDuration.LONG
                )
            }
            is XIResult.Status -> {
                ui.moveWorkflowButton.text = result.message
            }
            is XIResult.Progress -> {
                handleProgress(result)
            }
            else -> Timber.d("$result")
        }
    }

    private fun handleLoadingSuccess(result: XIResult.Success<JobEstimateWorksDTO>) =
        uiScope.launch(dispatchers.main()) {
            val worksData = result.data
            val filenames = worksData.jobEstimateWorksPhotos.map { photo ->
                photo.filename
            }

            val photoPairs = filenames.let {
                photoUtil.prepareGalleryPairs(it)
            }

            renderHistoricalGallery(photoPairs)
        }

    private fun renderHistoricalGallery(photoPairs: List<Pair<Uri, Bitmap>>) {
        ui.imageCollectionView.clearImages()
        ui.imageCollectionView.scaleForSize(photoPairs.size)
        ui.imageCollectionView.addZoomedImages(photoPairs, requireActivity())
        keyListener = ui.commentsEditText.keyListener!!
        ui.commentsEditText.keyListener = null
        ui.commentsEditText.setText(getString(R.string.comment_placeholder), NORMAL)
        ui.takePhotoButton.isClickable = false
        ui.takePhotoButton.background =
            ContextCompat.getDrawable(requireContext(), R.drawable.round_corner_gray)
        ui.moveWorkflowButton.isClickable = false
        ui.moveWorkflowButton.background =
            ContextCompat.getDrawable(requireContext(), R.drawable.round_corner_gray)
    }

    private fun loadPictures(result: XIResult.Success<JobEstimateWorksDTO>) = uiScope.launch(dispatchers.main()) {
        val worksData = result.data
        val filenames = worksData.jobEstimateWorksPhotos.filter { photo ->
            photo.photoActivityId == worksData.actId
        }.map { photo ->
            photo.photoPath
        }

        val photoPairs = photoUtil.prepareGalleryPairs(filenames)

        if (photoPairs.isNotEmpty()) {
            ui.imageCollectionView.clearImages()
            ui.imageCollectionView.scaleForSize(photoPairs.size)
            ui.imageCollectionView.addZoomedImages(photoPairs, requireActivity())
            ui.imageCollectionView.visibility = View.VISIBLE
        }
    }

    private fun validateUploadWorks() {

        when {
            estimateWorksPhotoArrayList.none { photo ->
                photo.photoActivityId == activeWorks.actId
            } -> {
                validationNotice(R.string.please_make_sure_workflow_items_contain_photos)
            }
            ui.commentsEditText.text.trim().isEmpty() -> {
                validationNotice(R.string.please_provide_a_comment)
            }
            else -> {
                ui.moveWorkflowButton.isClickable = false
                uploadEstimateWorksItem(estimateWorksPhotoArrayList, itemEstimate)
                ui.moveWorkflowButton.isClickable = true
            }
        }
    }

    private fun uploadEstimateWorksItem(
        estimatePhotos: ArrayList<JobEstimateWorksPhotoDTO>,
        estimateItem: JobItemEstimateDTO?
    ) {
        if (requireActivity().isConnected) {
            //  Lets Send to Service

            activeWorks.jobEstimateWorksPhotos = estimatePhotos

            activeWorks.estimateId = estimateItem?.estimateId
            var comments = ""
            if (!ValidateInputs.isValidInput(ui.commentsEditText.text.toString())) {
                ui.commentsEditText.error = getString(R.string.invalid_char)+ " Or ( $&+~;=\\?@|/'<>^*()%!- )"
            } else {
                comments = ui.commentsEditText.text!!.toString().trim { it <= ' ' }
            }
            // val comments = ui.commentsEditText.text.toString().trim()

            sendWorkToService(activeWorks, comments)
        } else {
            extensionToast(
                message = getString(R.string.no_connection_detected),
                style = ToastStyle.NO_INTERNET,
                position = ToastGravity.CENTER,
                duration = ToastDuration.LONG
            )
            ui.moveWorkflowButton.failProgress("Network down ...")
        }
    }

    private fun validationNotice(stringId: Int) {
        extensionToast(
            message = getString(stringId),
            style = ToastStyle.WARNING,
            position = ToastGravity.CENTER,
            duration = ToastDuration.LONG
        )
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
                    permission.WRITE_EXTERNAL_STORAGE,
                    permission.READ_EXTERNAL_STORAGE
                ),
                REQUEST_STORAGE_PERMISSION
            )
        } else {
            launchCamera()
        }
    }

    private fun sendWorkToService(
        estimateWorksItem: JobEstimateWorksDTO,
        comments: String
    ) {
        this.toggleLongRunning(true)
        workViewModel.workflowState.removeObserver(jobObserver)
        workViewModel.workflowState.observe(viewLifecycleOwner, workObserver)
        workViewModel.backupWorkSubmission.value = estimateWorksItem
        if (estimateWorksItem.jobEstimateWorksPhotos.isNullOrEmpty()) {
            validationNotice(R.string.please_ensure_estimation_items_contain_photos)
        } else {
            workViewModel.submitWorks(estimateWorksItem, comments, requireActivity(), itemEstimateJob)
        }
    }

    /**
     * Handler routine for submitting completed work for a job.
     */
    private fun handleJobSubmission(outcome: XIResult<String>?) {
        outcome?.let { result ->
            when (result) {
                is XIResult.Success<String> -> {
                    toggleLongRunning(false)
                    if (result.data == "JOB_COMPLETE") {
                        popViewOnJobSubmit(WorkflowDirection.NEXT.value)
                    }
                }
                is XIResult.Error -> {
                    toggleLongRunning(false)
                    ui.moveWorkflowButton.failProgress("Job submission failed")
                    workViewModel.resetWorkState()
                    crashGuard(
                        throwable = result,
                        refreshAction = { this.retryJobSubmission() }
                    )
                }
                is XIResult.Status -> {
                    extensionToast(
                        message = result.message,
                        style = ToastStyle.INFO,
                        position = ToastGravity.BOTTOM,
                        duration = ToastDuration.SHORT
                    )
                }
                is XIResult.Progress -> {
                    handleProgress(result)
                }
                else -> Timber.d("$result")
            }
        }
    }

    private fun handleProgress(result: XIResult.Progress) {
        when (result.isLoading) {
            true -> {
                toggleLongRunning(true)
                ui.moveWorkflowButton.initProgress(viewLifecycleOwner)
                ui.moveWorkflowButton.startProgress(ui.moveWorkflowButton.text.toString())
            }
            else -> {
                toggleLongRunning(false)
                ui.moveWorkflowButton.doneProgress(ui.moveWorkflowButton.text.toString())
            }
        }
    }

    /**
     * Handler for submitting a completed work step
     */
    private fun handleWorkSubmission(outcome: XIResult<String>?) {
        outcome?.let { result ->
            when (result) {
                is XIResult.Success -> {
                    handleWorkSuccess(result)
                }
                is XIResult.Error -> {
                    this@CaptureWorkFragment.toggleLongRunning(false)
                    ui.moveWorkflowButton.failProgress("Work submission failed")
                    crashGuard(
                        throwable = result,
                        refreshAction = { this@CaptureWorkFragment.retryWorkSubmission() }
                    )
                }
                is XIResult.Status -> {
                    extensionToast(
                        message = result.message,
                        style = ToastStyle.INFO,
                        position = ToastGravity.CENTER,
                        duration = ToastDuration.SHORT
                    )
                }
                is XIResult.Progress -> {
                    handleProgress(result)
                }

                else -> Timber.d("$result")
            }
        }
    }

    private fun handleWorkSuccess(result: XIResult.Success<String>) = uiScope.launch(dispatchers.ui()) {
        when (result.data != "JOB_COMPLETE") {
            true -> {
                ui.imageCollectionView.clearImages()
                this@CaptureWorkFragment.extensionToast(
                    message = "Work captured",
                    style = ToastStyle.SUCCESS,
                    position = ToastGravity.CENTER,
                    duration = ToastDuration.SHORT
                )
                ui.moveWorkflowButton.doneProgress("Workflow complete")
                toggleLongRunning(false)
                refreshUI()
            }
            else -> {
                Timber.d("Unexpected Response: $result")
            }
        }
    }

    private fun refreshUI() {
        uiScope.launch(dispatchers.main()) {
            Timber.d("RefreshUI -> JobID ${itemEstimateJob.jobId}")
            Timber.d("RefreshUI -> EstimateID ${itemEstimate.estimateId}")
            val directions = CaptureWorkFragmentDirections.actionCaptureWorkFragmentSelf(
                 itemEstimateJob.jobId,
                 itemEstimate.estimateId
            )

            Navigation.findNavController(this@CaptureWorkFragment.requireView()).navigate(directions)
        }
    }

    private fun retryWorkSubmission() {
        IndefiniteSnackbar.hide()
        val backupWorkSubmission = workViewModel.backupWorkSubmission
        backupWorkSubmission.observeOnce(viewLifecycleOwner, {
            it?.let {
                activeWorks = it
                val comments = ui.commentsEditText.text.toString().trim()
                sendWorkToService(activeWorks, comments)
            }
        })
    }

    private fun launchCamera() {
        this@CaptureWorkFragment.takingPhotos()
        uiScope.launch(dispatchers.ui()) {
            imageUri = photoUtil.getUri()!!
            takePicture.launch(imageUri)
        }
    }

    private fun processAndSetImage(itemEstiWorks: JobEstimateWorksDTO, imageUri: Uri) {
        try {
            //  Location of picture
            val currentLocation: LocationModel? = this.getCurrentLocation()
            Timber.d("$currentLocation")
            when (currentLocation != null) {
                true -> {
                    persistLocatedPhoto(currentLocation, itemEstiWorks, imageUri)
                }
                else -> {
                    extensionToast(
                        message = getString(R.string.please_enable_location_services),
                        style = ToastStyle.ERROR, position = ToastGravity.CENTER, duration = ToastDuration.LONG
                    )
                    this.checkLocationProviders()
                }
            }
        } catch (e: Exception) {
            extensionToast(
                message = getString(R.string.error_getting_image),
                style = ToastStyle.ERROR,
                position = ToastGravity.CENTER,
                duration = ToastDuration.LONG
            )
            Timber.e(e, getString(R.string.error_getting_image))
        } finally {
            this.photosDone()
        }
    }

    private fun persistLocatedPhoto(
        currentLocation: LocationModel,
        itemEstiWorks: JobEstimateWorksDTO,
        imageUri: Uri
    ) {
        Coroutines.io {
            filenamePath = photoUtil.saveImageToInternalStorage(
                imageUri
            ) as HashMap<String, String>

            val photo = createItemWorksPhoto(
                itemEstiWorks,
                filenamePath,
                currentLocation
            )

            withContext(Dispatchers.Main.immediate) {
                estimateWorksPhotoArrayList.add(photo)
                withContext(dispatchers.io()) {
                    savePhotoMetadata(
                        estimateWorksPhotoArrayList,
                        itemEstiWorks
                    )
                }
                groupAdapter.notifyItemChanged(0)
            }
        }
    }

    private fun savePhotoMetadata(
        workPortfolio: ArrayList<JobEstimateWorksPhotoDTO>,
        activeWorks: JobEstimateWorksDTO
    ) {
        Coroutines.io {
            try {
                workViewModel.createSaveWorksPhotos(
                    workPortfolio,
                    activeWorks
                )
            } catch (t: Throwable) {
                withContext(Dispatchers.Main.immediate) {
                    val message = "Gallery update failed: ${t.message ?: XIErrorHandler.UNKNOWN_ERROR}"
                    Timber.e(t, message)
                    crashGuard(
                        throwable = XIResult.Error(t, message)
                    )
                }
            }
        }
    }

    private fun createItemWorksPhoto(
        worksEstimate: JobEstimateWorksDTO,
        filenamePath: HashMap<String, String>,
        currentLocation: LocationModel
    ): JobEstimateWorksPhotoDTO {
        val photoId = SqlLitUtils.generateUuid()

        return JobEstimateWorksPhotoDTO(
            id = 0,
            descr = "",
            filename = filenamePath["filename"]!!,
            photoActivityId = worksEstimate.actId,
            photoDate = DateUtil.dateToString(Date())!!,
            photoId = photoId,
            photoLongitude = currentLocation.latitude,
            photoLatitude = currentLocation.longitude,
            photoPath = filenamePath["path"]!!,
            recordVersion = 0,
            recordSynchStateId = 0,
            worksId = worksEstimate.worksId
        )
    }

    private fun getWorkItems(
        estimateItem: JobItemEstimateDTO,
        estimateJob: JobDTO
    ) {
        uiScope.launch(dispatchers.main()) {
            // Has work been completed on the current job?
            val workDone: Int = getEstimatesCompleted(estimateJob.jobId)

            if (workDone == estimateJob.jobItemEstimates.size) {
                collectCompletedEstimates(estimateJob.jobId)
            } else {
                loadWorkEstimate(estimateItem.estimateId, estimateJob.jobId)
            }
        }

    }

    private suspend fun loadWorkEstimate(
        estimateItemId: String,
        estimateJobId: String
    ) {

        val estimateWorksData =
            workViewModel.getLiveJobEstimateWorksByEstimateId(estimateItemId)
        estimateWorksData.observe(viewLifecycleOwner, { estimateWorks ->

            estimateWorks?.let { workItem ->
                // Is work completed on this estimate?
                if (workItem.actId == ActivityIdConstants.EST_WORKS_COMPLETE) {
                    uiScope.launch {
                        val estWorkDone: Int = getEstimatesCompleted(estimateJobId)
                        submitEstimatesOrPop(estWorkDone, estimateJobId)
                    }
                } else {
                    estimateWorksArrayList = arrayListOf(estimateWorks)
                    generateWorkflowSteps(estimateWorksArrayList)
                }

                activeWorks = workItem

                ui.imageCollectionView.clearImages()
                estimateWorksPhotoArrayList = activeWorks.jobEstimateWorksPhotos
                if (estimateWorksPhotoArrayList.size > 0) {
                    loadPictures(XIResult.Success(activeWorks))
                }
            }
        })
    }

    private fun submitEstimatesOrPop(
        estWorkDone: Int,
        estimateJobId: String
    ) {
        workViewModel.setWorkItemJob(estimateJobId)

        val estimateJobData = workViewModel.workItemJob
        estimateJobData.observe(viewLifecycleOwner, { estimateJob ->
            if (estWorkDone == estimateJob?.jobItemEstimates?.size) {
                collectCompletedEstimates(estimateJob.jobId)
            } else {
                popViewOnWorkSubmit()
            }
        })

    }

    private fun collectCompletedEstimates(estimateJobId: String) = uiScope.launch(dispatchers.main()) {
        this@CaptureWorkFragment.toggleLongRunning(true)
        val iItems = workViewModel.getJobEstimationItemsForJobId(
            estimateJobId,
            ActivityIdConstants.ESTIMATE_WORK_PART_COMPLETE
        )
        if (iItems.isNotEmpty()) {
            toggleLongRunning(false)
            submitAllOutStandingEstimates(iItems)
        } else {
            Timber.d("Nothing to send")
            toggleLongRunning(false)
        }
    }

    private fun generateWorkflowSteps(estimateWorksList: List<JobEstimateWorksDTO>) {
        // Remove for Dynamic Workflow
        val id = ActivityIdConstants.JOB_APPROVED
        uiScope.launch(dispatchers.main()) {
            val workflowStepData = workViewModel.getWorkFlowCodes(id)
            workflowStepData.observe(
                viewLifecycleOwner,
                { workflowSteps ->
                    jobWorkStep = workflowSteps as ArrayList<WfWorkStepDTO>

                    initRecyclerView(
                        estimateWorksList.toWorkStateItems(),
                        workflowSteps
                    )
                }
            )
        }
    }

    private suspend fun getEstimatesCompleted(estimateJobId: String): Int {
        return workViewModel.getJobItemsEstimatesDoneForJobId(
            estimateJobId,
            ActivityIdConstants.ESTIMATE_WORK_PART_COMPLETE,
            ActivityIdConstants.EST_WORKS_COMPLETE
        )
    }

    private fun popViewOnWorkSubmit() {
        uiScope.launch(dispatchers.main()) {
            val directions = CaptureWorkFragmentDirections.actionCaptureWorkFragmentToNavigationWork(itemEstimate.jobId)
            Navigation.findNavController(this@CaptureWorkFragment.requireView()).navigate(directions)
        }

    }

    @Synchronized
    private fun submitAllOutStandingEstimates(
        estimates: List<JobItemEstimateDTO>?
    ) {
        // get Data from db Search for all estimates 8 and work 21 = result is int > 0  then button yes else fetch

        val alertDialog = AlertDialog.Builder(requireActivity())
        alertDialog.setTitle(R.string.confirm)
        alertDialog.setIcon(R.drawable.ic_error)
        alertDialog.setMessage("Work Complete - Submit for Measurements")
        alertDialog.setCancelable(false)
        alertDialog.setPositiveButton(
            R.string.yes
        ) { dialog, _ ->
            dialog.dismiss()
            uiScope.launch {
                if (!estimates.isNullOrEmpty()) {
                    pushCompletedEstimates(estimates)
                    measuresShowing = false
                }
            }
        }
        if (estimates?.isNotEmpty() == true && !measuresShowing) {
            alertDialog.create()
            alertDialog.show()
        }

    }


    private fun retryJobSubmission() {
        IndefiniteSnackbar.hide()
        val retryJobData = workViewModel.backupCompletedEstimates
        retryJobData.observeOnce(viewLifecycleOwner, {
            it?.let {
                uiScope.launch(dispatchers.main()) {
                    pushCompletedEstimates(it as ArrayList<JobItemEstimateDTO>)
                }
            }
        })
    }

    private suspend fun pushCompletedEstimates(
        estimates: List<JobItemEstimateDTO>
    ) = withContext(dispatchers.main()) {
        estimateSize = estimates.size
        estimateCount = 0
        errorState = false

        workViewModel.backupCompletedEstimates.postValue(estimates)
        jobSubmission = uiScope.launch(dispatchers.main()) {
            workViewModel.workflowState.removeObserver(workObserver)
            workViewModel.workflowState.observe(viewLifecycleOwner, jobObserver)
            workViewModel.workflowState.postValue(XIResult.Progress(true))
            withContext(dispatchers.main()) {
                for (jobEstimate in estimates) {
                    uiScope.launch(dispatchers.main()) {
                        val jobItemEstimate = workViewModel.getJobItemEstimateForEstimateId(jobEstimate.estimateId)
                        if (jobItemEstimate.actId == ActivityIdConstants.ESTIMATE_WORK_PART_COMPLETE) {
                            withContext(dispatchers.main()) {
                                moveJobItemEstimateToNextWorkflow(
                                    WorkflowDirection.NEXT,
                                    jobEstimate
                                )
                            }
                        }
                    }
                }
            }
        }
        jobSubmission.join()

        handleJobSubmission(XIResult.Success("JOB_COMPLETE"))
    }

    private suspend fun moveJobItemEstimateToNextWorkflow(
        workflowDirection: WorkflowDirection,
        jobItEstimate: JobItemEstimateDTO?
    ) = withContext(dispatchers.ui()) {

        val user = workViewModel.user.await()
        user.observe(viewLifecycleOwner, { userDTO ->

            when {
                userDTO.userId.isBlank() -> {
                    this@CaptureWorkFragment.extensionToast(
                        message = "Error: current user lacks permissions",
                        style = ToastStyle.ERROR,
                        position = ToastGravity.CENTER,
                        duration = ToastDuration.LONG
                    )
                    ui.moveWorkflowButton.failProgress("Workflow failed ...")
                }
                jobItEstimate?.jobId == null -> {
                    this@CaptureWorkFragment.extensionToast(
                        message = "Error: selected job is invalid",
                        style = ToastStyle.ERROR,
                        position = ToastGravity.CENTER,
                        duration = ToastDuration.LONG
                    )
                    ui.moveWorkflowButton.failProgress("Workflow failed ...")
                }
                else -> {
                    prepareWorkflowMove(jobItEstimate, workflowDirection, userDTO)
                }
            }
        })
    }

    private fun prepareWorkflowMove(
        jobItEstimate: JobItemEstimateDTO,
        workflowDirection: WorkflowDirection,
        userDTO: UserDTO
    ) {
        val trackRouteId: String =
            DataConversion.toLittleEndian(jobItEstimate.trackRouteId)!!
        val direction: Int = workflowDirection.value

        uiScope.launch(dispatchers.main()) {
            workViewModel.processWorkflowMove(
                userDTO.userId,
                jobItEstimate.jobId!!,
                trackRouteId,
                "Work complete.",
                direction
            )
        }
    }

    private fun popViewOnJobSubmit(direction: Int) {
        when (direction) {
            WorkflowDirection.NEXT.value -> {
                extensionToast(
                    title = "Workflow update",
                    message = getString(R.string.work_complete),
                    style = ToastStyle.SUCCESS,
                    position = ToastGravity.CENTER,
                    duration = ToastDuration.LONG
                )
            }
            WorkflowDirection.FAIL.value -> {
                extensionToast(
                    title = "Workflow Update",
                    message = getString(R.string.work_declined),
                    style = ToastStyle.DELETE,
                    position = ToastGravity.CENTER,
                    duration = ToastDuration.LONG
                )
            }
        }

        Handler(Looper.getMainLooper()).postDelayed(
            {
                uiScope.launch {
//                    val directions = CaptureWorkFragmentDirections.actionGlobalNavHome()
//                    Navigation.findNavController(this@CaptureWorkFragment.requireView()).navigate(directions)
                    Intent(requireContext(), MainActivity::class.java).also { home ->
                        home.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(home)
                    }
                }
            },
            Constants.TWO_SECONDS
        )
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.run {
            putString(JOB_KEY, itemEstimateJob.jobId)
            putString(ESTIMATE_KEY, itemEstimate.estimateId)
        }

        super.onSaveInstanceState(outState)
    }

    private fun initRecyclerView(
        stateItems: List<WorkStateItem>,
        workCodes: List<WfWorkStepDTO>
    ) {
        groupAdapter.run {
            clear()
            for (i in workCodes.indices) {
                add(stateItems[0])
                notifyItemChanged(i)
            }
        }

        ui.workActionsListView.apply {
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
        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true) {

            override fun handleOnBackPressed() {
                uiScope.launch(dispatchers.main()) {
                    val directions = CaptureWorkFragmentDirections.actionCaptureWorkFragmentToNavigationWork(itemEstimateJob.jobId)
                    Navigation.findNavController(this@CaptureWorkFragment.requireView()).navigate(directions)
                }
            }
        }
        requireActivity().onBackPressedDispatcher
            .addCallback(this, callback)
    }


    private fun getCurrentLocation(): LocationModel? {
        return super.getLocation()
    }
}
