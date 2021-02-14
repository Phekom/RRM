/*
 * Updated by Shaun McDonald on 2021/02/15
 * Last modified on 2021/02/15 12:10 AM
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 */

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
import android.os.Handler
import android.os.Looper
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
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenStarted
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import pereira.agnaldo.previewimgcol.ImageCollectionView
import timber.log.Timber
import za.co.xisystems.itis_rrm.MainActivity
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.base.LocationFragment
import za.co.xisystems.itis_rrm.constants.Constants
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
import za.co.xisystems.itis_rrm.data.localDB.entities.WfWorkStepDTO
import za.co.xisystems.itis_rrm.databinding.FragmentCaptureWorkBinding
import za.co.xisystems.itis_rrm.extensions.observeOnce
import za.co.xisystems.itis_rrm.services.LocationModel
import za.co.xisystems.itis_rrm.ui.extensions.addZoomedImages
import za.co.xisystems.itis_rrm.ui.extensions.doneProgress
import za.co.xisystems.itis_rrm.ui.extensions.failProgress
import za.co.xisystems.itis_rrm.ui.extensions.initProgress
import za.co.xisystems.itis_rrm.ui.extensions.scaleForSize
import za.co.xisystems.itis_rrm.ui.extensions.showZoomedImage
import za.co.xisystems.itis_rrm.ui.extensions.startProgress
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
import za.co.xisystems.itis_rrm.utils.enums.ToastDuration.SHORT
import za.co.xisystems.itis_rrm.utils.enums.ToastGravity.BOTTOM
import za.co.xisystems.itis_rrm.utils.enums.ToastGravity.CENTER
import za.co.xisystems.itis_rrm.utils.enums.ToastStyle
import za.co.xisystems.itis_rrm.utils.enums.ToastStyle.ERROR
import za.co.xisystems.itis_rrm.utils.enums.ToastStyle.INFO
import za.co.xisystems.itis_rrm.utils.enums.ToastStyle.NO_INTERNET
import za.co.xisystems.itis_rrm.utils.enums.ToastStyle.WARNING
import za.co.xisystems.itis_rrm.utils.enums.WorkflowDirection
import java.util.Date
import java.util.HashMap

class CaptureWorkFragment : LocationFragment(), KodeinAware {

    override val kodein by kodein()
    private lateinit var workViewModel: WorkViewModel
    private val factory: WorkViewModelFactory by instance()
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
    private lateinit var jobWorkStep: ArrayList<WfWorkStepDTO>
    private lateinit var keyListener: KeyListener
    private var uiScope = UiLifecycleScope()
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
    private var selectedJobId: String = ""

    init {
        lifecycleScope.launch {
            whenStarted {
                workViewModel = activity?.run {
                    ViewModelProvider(this, factory).get(WorkViewModel::class.java)
                } ?: throw Exception("Invalid Activity")
            }
            // CONTINUE HERE !!
            val args: CaptureWorkFragmentArgs by navArgs()
            args.jobId?.let {
                selectedJobId = it
                workViewModel.setWorkItemJob(it)
            }
            args.estimateId?.let {
                workViewModel.setWorkItem(it)
            }
        }
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
    ): View {
        // Inflate the layout for this fragment
        _ui = FragmentCaptureWorkBinding.inflate(inflater, container, false)
        return ui.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        uiScope.destroy()
        // Remember to flush the RecyclerView's adaptor
        workViewModel.workflowState.removeObservers(viewLifecycleOwner)
        ui.workActionsListView.adapter = null
        ui.imageCollectionView.clearImages()
        _ui = null
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        uiScope.launch(uiScope.coroutineContext) {

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
                    getWorkItems(itemEstimate, itemEstimateJob)
                }

            })
            workViewModel.historicalWorks.observe(viewLifecycleOwner, {
                it?.let { populateHistoricalWorkEstimate(it) }
            })
        }

        ui.imageCollectionView.visibility = View.GONE
        ui.imageCollectionView.clearImages()
        estimateWorksPhotoArrayList = ArrayList()

        ui.takePhotoButton.setOnClickListener {
            initCameraLaunch()
        }
        ui.moveWorkflowButton.setOnClickListener {
            validateUploadWorks()
        }
    }

    private fun populateHistoricalWorkEstimate(result: XIResult<JobEstimateWorksDTO>) {
        when (result) {
            is XISuccess -> {
                val worksData = result.data
                val filenames = worksData.jobEstimateWorksPhotos.map { photo ->
                    photo.filename
                }

                val photoPairs = filenames.let {
                    PhotoUtil.prepareGalleryPairs(it, requireActivity().applicationContext)
                }
                photoPairs.let {
                    ui.imageCollectionView.clearImages()
                    ui.imageCollectionView.scaleForSize(photoPairs.size)
                    ui.imageCollectionView.addZoomedImages(photoPairs, requireActivity())
                    keyListener = ui.commentsEditText.keyListener
                    ui.commentsEditText.keyListener = null
                    ui.commentsEditText.setText(getString(R.string.comment_placeholder), TextView.BufferType.NORMAL)
                    ui.takePhotoButton.isClickable = false
                    ui.takePhotoButton.background =
                        ContextCompat.getDrawable(requireContext(), R.drawable.round_corner_gray)
                    ui.moveWorkflowButton.isClickable = false
                    ui.moveWorkflowButton.background =
                        ContextCompat.getDrawable(requireContext(), R.drawable.round_corner_gray)
                }
            }
            is XIError -> {
                sharpToast(message = result.message, style = ERROR, position = BOTTOM, duration = LONG)
            }
            is XIStatus -> {
                ui.moveWorkflowButton.text = result.message
            }
            is XIProgress -> {
                when (result.isLoading) {
                    true -> ui.moveWorkflowButton.startProgress(ui.moveWorkflowButton.text.toString())
                    else -> ui.moveWorkflowButton.doneProgress(ui.moveWorkflowButton.text.toString())
                }
            }
            else -> Timber.d("$result")
        }
    }

    private fun validateUploadWorks() {

        when (estimateWorksPhotoArrayList.size) {
            0 -> {
                validationNotice(R.string.please_make_sure_workflow_items_contain_photos)
            }
            else -> when (ui.commentsEditText.text.trim().isEmpty()) {
                true -> {
                    validationNotice(R.string.please_provide_a_comment)
                }
                else -> {
                    ui.moveWorkflowButton.isClickable = false
                    uploadEstimateWorksItem(estimateWorksPhotoArrayList, jobitemEsti)
                    ui.moveWorkflowButton.isClickable = true
                }
            }
        }
    }

    private fun uploadEstimateWorksItem(estimatePhotos: ArrayList<JobEstimateWorksPhotoDTO>, estimateItem: JobItemEstimateDTO?) {
        if (ServiceUtil.isNetworkAvailable(requireActivity().applicationContext)) {
            //  Lets Send to Service
            itemEstiWorks.jobEstimateWorksPhotos = estimatePhotos
            itemEstiWorks.estimateId = estimateItem?.estimateId

            sendWorkToService(itemEstiWorks)
        } else {
            sharpToast(
                message = getString(R.string.no_connection_detected),
                style = NO_INTERNET,
                position = CENTER,
                duration = LONG
            )
            ui.moveWorkflowButton.failProgress("Network down ...")
        }
    }

    private fun validationNotice(stringId: Int) {
        sharpToast(
            message = getString(stringId),
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
        estimateWorksItem: JobEstimateWorksDTO
    ) {
        uiScope.launch(uiScope.coroutineContext) {
            workViewModel.workflowState.removeObserver(jobObserver)
            workViewModel.workflowState.observe(viewLifecycleOwner, workObserver)
            workViewModel.backupWorkSubmission.value = estimateWorksItem
            val newItemEstimateWorks = setJobWorksLittleEndianGuids(estimateWorksItem)
            if (newItemEstimateWorks.jobEstimateWorksPhotos.isNullOrEmpty()) {
                validationNotice(R.string.please_ensure_estimation_items_contain_photos)
            } else {
                workViewModel.submitWorks(newItemEstimateWorks, requireActivity(), itemEstimateJob)
            }
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
                    crashGuard(
                        view = this.requireView(),
                        throwable = result,
                        refreshAction = { retryJobSubmission() })
                }
                is XIStatus -> {
                    sharpToast(
                        message = result.message,
                        style = INFO,
                        position = BOTTOM,
                        duration = SHORT
                    )
                }
                is XIProgress -> {
                    when (result.isLoading) {
                        true -> ui.moveWorkflowButton.startProgress(ui.moveWorkflowButton.text.toString())
                        else -> ui.moveWorkflowButton.doneProgress(ui.moveWorkflowButton.text.toString())
                    }
                }
                else -> Timber.d("$result")
            }
        }
    }

    /**
     * Handler for submitting a completed work step
     */
    private fun handleWorkSubmission(outcome: XIResult<String>?) {
        outcome?.let { result ->
            when (result) {
                is XISuccess -> {
                    when (result.data == "WORK_COMPLETE") {
                        true -> {
                            popViewOnJobSubmit(WorkflowDirection.NEXT.value)
                        }
                        else -> {
                            sharpToast(
                                message = "Work captured",
                                style = ToastStyle.SUCCESS,
                                position = CENTER,
                                duration = SHORT
                            )
                            ui.moveWorkflowButton.doneProgress("Workflow complete")
                            refreshView()
                        }
                    }
                }
                is XIError -> {
                    crashGuard(
                        view = this@CaptureWorkFragment.requireView(),
                        throwable = result,
                        refreshAction = { this@CaptureWorkFragment.retryWorkSubmission() }
                    )
                }
                is XIStatus -> {
                    sharpToast(
                        message = result.message,
                        style = INFO,
                        position = CENTER,
                        duration = SHORT
                    )
                }
                is XIProgress -> {
                    when (result.isLoading) {

                        true -> {
                            ui.moveWorkflowButton.initProgress(viewLifecycleOwner)
                            ui.moveWorkflowButton.startProgress(ui.moveWorkflowButton.text.toString())
                        }
                        else -> ui.moveWorkflowButton.doneProgress(ui.moveWorkflowButton.text.toString())
                    }
                }

                else -> Timber.d("$result")
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
        if (!works.jobEstimateWorksPhotos.isNullOrEmpty()) {
            works.jobEstimateWorksPhotos.forEach { ewp ->
                ewp.setWorksId(DataConversion.toLittleEndian(ewp.worksId))
                ewp.setPhotoId(DataConversion.toLittleEndian(ewp.photoId))
            }
        }
        return works
    }

    private fun refreshView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

            parentFragmentManager.beginTransaction().detach(this).commitNow()
            parentFragmentManager.beginTransaction().attach(this).commitNow()
        } else {
            parentFragmentManager.beginTransaction().detach(this).attach(this).commit()
        }

        // Await the updated estimate record
        uiScope.launch(uiScope.coroutineContext) {
            workViewModel.workItem.observeOnce(viewLifecycleOwner, {
                Timber.d("$it")
                val id = STANDARD_WORKFLOW_STEPS
                // This part must be Deleted when the Dynamic workflow is complete.
                uiScope.launch(uiScope.coroutineContext) {
                    val workCodeData = workViewModel.getWorkFlowCodes(id)
                    workCodeData.observeOnce(viewLifecycleOwner, {
                        groupAdapter.notifyItemChanged(2)

                        ui.commentsEditText.setText("")
                        Timber.d("IsRefresh -> Yes")
                    })
                }
            })
        }
    }

    private fun launchCamera() {

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)
                if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
                    // Process the image and set it to the TextView
            processAndSetImage(itemEstiWorks)
                        ui.imageCollectionView.visibility = View.VISIBLE
                    }
    }

    private fun processAndSetImage(itemEstiWorks: JobEstimateWorksDTO) {
        try {
            //  Location of picture
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
                else -> sharpToast(
                    message = getString(R.string.please_enable_location_services),
                    style = ERROR, position = CENTER, duration = LONG
                )
            }
        } catch (e: Exception) {
            sharpToast(
                message = getString(R.string.error_getting_image),
                style = ERROR,
                position = CENTER,
                duration = LONG
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
            sharpToast(
                message = getString(R.string.please_enable_location_services),
                style = ERROR, position = CENTER, duration = LONG
            )
            // Launch Dialog
        } else {

            // unlock mutex
        uiScope.launch(uiScope.coroutineContext) {

            try {
                val photo = createItemWorksPhoto(
                    filenamePath,
                    currentLocation
                )

                    estimateWorksPhotoArrayList.add(photo)
                Timber.d("^*^ Photo Bug ^*^ Photos in array: ${estimateWorksPhotoArrayList.size}")

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

                // Prepare gallery for new size
                ui.imageCollectionView.scaleForSize(
                    estimateWorksPhotoArrayList.size
                )

                // Push photo into ImageCollectionView
                ui.imageCollectionView.addImage(
                        bitmap!!,
                    object : ImageCollectionView.OnImageClickListener {
                        override fun onClick(bitmap: Bitmap, imageView: ImageView) {
                                showZoomedImage(imageUrl, this@CaptureWorkFragment.requireActivity())
                        }
                    }
                )
                    ui.imageCollectionView.visibility = View.VISIBLE

                Timber.d("*^* PhotoBug *^* Photos in gallery: ${ui.imageCollectionView.childCount}")
            } catch (t: Throwable) {
                val message = "Gallery update failed: ${t.message ?: XIErrorHandler.UNKNOWN_ERROR}"
                Timber.e(t, message)
                crashGuard(this@CaptureWorkFragment.requireView(), XIError(t, message))
                }
            }
        }
    }

    private fun createItemWorksPhoto(
        filenamePath: HashMap<String, String>,
        currentLocation: LocationModel
    ): JobEstimateWorksPhotoDTO {
        val photoId = SqlLitUtils.generateUuid()

        return JobEstimateWorksPhotoDTO(
            id = 0,
            descr = "",
            filename = filenamePath["filename"]!!,
            photoActivityId = itemEstiWorks.actId,
            photoDate = DateUtil.dateToString(Date())!!,
            photoId = photoId,
            photoLongitude = currentLocation.latitude,
            photoLatitude = currentLocation.longitude,
            photoPath = filenamePath["path"]!!,
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

            if (workDone == estimateJob.jobItemEstimates.size) {
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
        if (estWorkDone == estimateJob.jobItemEstimates.size) {
            Coroutines.main {
                collectCompletedEstimates(estimateJob)
            }
        } else {
            popViewOnWorkSubmit(this.requireView())
        }
    }

    private suspend fun collectCompletedEstimates(estimateJob: JobDTO) {
        val iItems = workViewModel.getJobEstimationItemsForJobId(
            estimateJob.jobId,
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
                    jobWorkStep = workflowSteps as ArrayList<WfWorkStepDTO>

                    initRecyclerView(
                        estimateWorksList.toWorkStateItems(),
                        workflowSteps
                    )
                })
        }
    }

    private suspend fun getEstimatesCompleted(estimateJob: JobDTO): Int {
        return workViewModel.getJobItemsEstimatesDoneForJobId(
            estimateJob.jobId,
            ActivityIdConstants.ESTIMATE_WORK_PART_COMPLETE,
            ActivityIdConstants.EST_WORKS_COMPLETE
        )
    }

    private fun popViewOnWorkSubmit(view: View) {
        val navDirections = CaptureWorkFragmentDirections.actionCaptureWorkFragmentToNavWork(selectedJobId)
        Navigation.findNavController(view).navigate(navDirections)
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
            ) { _, _ ->

                Coroutines.main {
                    pushCompletedEstimates(estimates)
                }
            }

            dialogBuilder.show()
        }
    }

    private fun retryJobSubmission() {
        IndefiniteSnackbar.hide()
        val retryJobData = workViewModel.backupCompletedEstimates
        retryJobData.observeOnce(viewLifecycleOwner, {
            it?.let {
                Coroutines.main {
                    pushCompletedEstimates(it as ArrayList<JobItemEstimateDTO>)
                }
            }
        })
    }

    private suspend fun pushCompletedEstimates(estimates: ArrayList<JobItemEstimateDTO>) {
        estimateSize = estimates.size
        estimateCount = 0
        errorState = false

        workViewModel.backupCompletedEstimates.postValue(estimates as List<JobItemEstimateDTO>)
        jobSubmission = uiScope.launch(uiScope.coroutineContext) {
            workViewModel.workflowState.removeObserver(workObserver)
            workViewModel.workflowState.observe(viewLifecycleOwner, jobObserver)
            workViewModel.workflowState.postValue(XIProgress(true))
            withContext(uiScope.coroutineContext) {
                for (jobEstimate in estimates) {

                    Coroutines.main {
                        val jobItemEstimate = workViewModel.getJobItemEstimateForEstimateId(jobEstimate.estimateId)
                        if (jobItemEstimate.actId == ActivityIdConstants.ESTIMATE_WORK_PART_COMPLETE) {
                            withContext(uiScope.coroutineContext) {
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
        handleJobSubmission(XISuccess("WORK_COMPLETE"))
    }

    private suspend fun moveJobItemEstimateToNextWorkflow(
        workflowDirection: WorkflowDirection,
        jobItEstimate: JobItemEstimateDTO?
    ) {

        val user = workViewModel.user.await()
        user.observe(viewLifecycleOwner, { userDTO ->

            when {
                userDTO.userId.isBlank() -> {
                    sharpToast(
                        message = "Error: current user lacks permissions",
                        style = ERROR,
                        position = CENTER,
                        duration = LONG
                    )
                    ui.moveWorkflowButton.failProgress("Workflow failed ...")
                }
                jobItEstimate?.jobId == null -> {
                    sharpToast(
                        message = "Error: selected job is invalid",
                        style = ERROR,
                        position = CENTER,
                        duration = LONG
                    )
                    ui.moveWorkflowButton.failProgress("Workflow failed ...")
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
                                "Work complete.",
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
            sharpToast(
                resId = R.string.work_complete,
                style = ToastStyle.SUCCESS,
                position = CENTER,
                duration = LONG
            )
        } else if (direction == WorkflowDirection.FAIL.value) {
            sharpToast(
                message = getString(R.string.work_declined),
                style = INFO,
                position = CENTER,
                duration = LONG
            )
        }

        Handler(Looper.getMainLooper()).postDelayed(
            {
                Intent(activity, MainActivity::class.java).also { home ->
                    startActivity(home)
                }
            },
            Constants.TWO_SECONDS
        )
    }

    private fun initRecyclerView(
        stateItems: List<WorkStateItem>,
        workCodes: List<WfWorkStepDTO>
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
        (activity as MainActivity).supportActionBar?.title = getString(R.string.capture_work_title)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // no options menu
        return false
    }

    private fun getCurrentLocation(): LocationModel? {
        return super.getLocation()
    }

    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 1
        private const val REQUEST_STORAGE_PERMISSION = 1
        private const val STANDARD_WORKFLOW_STEPS = 3
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
