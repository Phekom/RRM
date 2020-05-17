package za.co.xisystems.itis_rrm.ui.mainview.work.capture_work

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import icepick.State
import kotlinx.android.synthetic.main.fragment_capture_work.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.cancel
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import pereira.agnaldo.previewimgcol.ImageCollectionView
import timber.log.Timber
import za.co.xisystems.itis_rrm.MainActivity
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.data.localDB.entities.*
import za.co.xisystems.itis_rrm.ui.mainview._fragments.BaseFragment
import za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils.intents.AbstractIntent
import za.co.xisystems.itis_rrm.ui.mainview.work.WorkViewModel
import za.co.xisystems.itis_rrm.ui.mainview.work.WorkViewModelFactory
import za.co.xisystems.itis_rrm.ui.mainview.work.work_utils.LocationHelper
import za.co.xisystems.itis_rrm.ui.mainview.work.workstate_item.WorkStateItem
import za.co.xisystems.itis_rrm.ui.scopes.UiLifecycleScope
import za.co.xisystems.itis_rrm.utils.*
import za.co.xisystems.itis_rrm.utils.enums.PhotoQuality
import za.co.xisystems.itis_rrm.utils.enums.WorkflowDirection
import za.co.xisystems.itis_rrm.utils.zoomage.ZoomageView
import java.util.*
import kotlin.collections.ArrayList


class CaptureWorkFragment : BaseFragment(R.layout.fragment_capture_work), KodeinAware {


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
    private lateinit var jobWorkStep: ArrayList<WF_WorkStepDTO>
    private var uiScope = UiLifecycleScope()

    @State
    var filenamePath = HashMap<String, String>()
    private lateinit var locationHelper: LocationHelper
    private var currentLocation: Location? = null
    lateinit var useR: UserDTO
    override fun onStart() {
        super.onStart()
        locationHelper.onStart()
    }

    override fun onPause() {
        locationHelper.onPause()
        super.onPause()
    }

    override fun onStop() {
        locationHelper.onStop()
        super.onStop()

    }

    override fun onDestroy() {
        uiScope.cancel(CancellationException("onDestroy"))
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity as MainActivity).supportActionBar?.title = getString(R.string.capture_work_title)
        workFlowMenuTitles = ArrayList()
        groupAdapter = GroupAdapter()
        locationHelper =
            LocationHelper(
                this
            )
        locationHelper.onCreate()
        estimateWorksPhotoArrayList = ArrayList()
        estimateWorksList = ArrayList()
        estimateWorksArrayList = ArrayList()
        jobWorkStep = ArrayList()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_capture_work, container, false)
    }

    override fun onDestroyView() {
        // Remember to flush the RecyclerView's adaptor
        work_actions_listView.adapter = null
        super.onDestroyView()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        workViewModel = activity?.run {
            ViewModelProvider(this, factory).get(WorkViewModel::class.java)
        } ?: throw Exception("Invalid Activity")

        Coroutines.main {
            val user = workViewModel.user.await()
            user.observe(viewLifecycleOwner, Observer { user_ ->
                useR = user_
//                group10_loading.visibility = View.GONE
            })

            workViewModel.workItemJob.observe(viewLifecycleOwner, Observer { estimateJob ->
                itemEstimateJob = estimateJob
            })

            workViewModel.workItem.observe(viewLifecycleOwner, Observer { estimate ->
                itemEstimate = estimate

                getWorkItems(itemEstimate, itemEstimateJob)
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

    private fun validateUploadWorks() {

        when (estimateWorksPhotoArrayList.size) {
            0 -> {
                validationNotice(R.string.please_make_sure_workflow_items_contain_photos)
            }
            else -> when (comments_editText.text.isNullOrEmpty()) {
                true -> {
                    validationNotice(R.string.please_provide_a_comment)
                }
                else -> {
                    val prog =
                        setDataProgressDialog(
                            requireActivity(),
                            getString(R.string.data_loading_please_wait)
                        )
                    uploadEstimateWorksItem(prog)
                }
            }
        }
    }

    private fun uploadEstimateWorksItem(prog: ProgressDialog) {
        if (ServiceUtil.isNetworkConnected(requireActivity().applicationContext)) { //  Lets Send to Service

            itemEstiWorks.jobEstimateWorksPhotos = estimateWorksPhotoArrayList
            itemEstiWorks.jobItemEstimate = jobitemEsti

            sendJobToService(itemEstiWorks, prog)
        } else {
            val networkToast = Toast.makeText(
                activity?.applicationContext,
                R.string.no_connection_detected,
                Toast.LENGTH_LONG
            )
            networkToast.setGravity(Gravity.CENTER_VERTICAL, 0, 0)
            networkToast.show()

            persistJobToDevice(itemEstiWorks, prog)
        }
    }

    private fun validationNotice(stringId: Int) {
        val validation = Toast.makeText(
            activity?.applicationContext,
            getString(stringId),
            Toast.LENGTH_LONG
        )
        validation.setGravity(Gravity.CENTER_VERTICAL, 0, 0)
        validation.show()
    }

    private fun initCameraLaunch() {
        if (ContextCompat.checkSelfPermission(
                requireActivity().applicationContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) !== PackageManager.PERMISSION_GRANTED
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

    private fun sendJobToService(
        itemEstiWorks: JobEstimateWorksDTO,
        prog: ProgressDialog
    ) {
        Coroutines.main {

            val newitemEstiWorks = setJobWorksLittleEndianGuids(itemEstiWorks)
            val response =
                workViewModel.submitWorks(newitemEstiWorks, requireActivity(), itemEstimateJob)
            if (response.isBlank()) {
                refreshView(prog)
            } else
                activity?.toast(response)
        }
    }

    /**
     * If work estimates cannot be sent to the service, they are stored locally
     * @param itemEstiWorks JobEstimateWorksDTO
     * @param prog ProgressDialog
     */
    private fun persistJobToDevice(
        itemEstiWorks: JobEstimateWorksDTO,
        prog: ProgressDialog
    ) {
        // TODO: Persist valid estimateWorks data to device,
        // Trickle upload once connectivity restored.
    }

    private fun setJobWorksLittleEndianGuids(works: JobEstimateWorksDTO): JobEstimateWorksDTO {
        if (works != null) {
//            for (jew in works) {
            works.setWorksId(DataConversion.toLittleEndian(works.worksId))
            works.setEstimateId(DataConversion.toLittleEndian(works.estimateId))
            works.setTrackRouteId(DataConversion.toLittleEndian(works.trackRouteId))
            if (works.jobEstimateWorksPhotos != null) {
                for (ewp in works.jobEstimateWorksPhotos!!) {
                    ewp.setWorksId(DataConversion.toLittleEndian(ewp.worksId))
                    ewp.setPhotoId(DataConversion.toLittleEndian(ewp.photoId))
                }
            }
//            }
        }
        return works
    }

    private fun refreshView(prog: ProgressDialog) {
        Coroutines.main {
            groupAdapter.clear()
            image_collection_view.clearImages()
            estimateWorksPhotoArrayList.clear()
            comments_editText.setText("")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

                parentFragmentManager.beginTransaction().detach(this).commitNow()
                prog.dismiss()
                parentFragmentManager.beginTransaction().attach(this).commitNow()


            } else {
                prog.dismiss()
                parentFragmentManager.beginTransaction().detach(this)
                    .attach(this).commit()

            }

            // Await the updated estimate record

            workViewModel.workItem.observe(viewLifecycleOwner, Observer {
                Timber.d("$it")

                val id = ActivityIdConstants.JOB_APPROVED
                // This part must be Deleted when the Dynamic workflow is complete.
                Coroutines.main {
                    val workCodeData = workViewModel.getWorkFlowCodes(id)
                    workCodeData.observe(viewLifecycleOwner, Observer {

                        groupAdapter.notifyItemChanged(2)
                        Timber.d("IsRefresh -> Yes")
                    })
                }


            })
        }
    }

    private fun launchCamera() {

        Coroutines.main {
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
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) { // Process the image and set it to the TextView
            processAndSetImage(itemEstiWorks)
            image_collection_view.visibility = View.VISIBLE
        }
    }

    private fun processAndSetImage(itemEstiWorks: JobEstimateWorksDTO) {
        try { //  Location of picture
            val currentLocation: Location? = locationHelper.getCurrentLocation()
            when (currentLocation != null) {
                true -> {
                    filenamePath = PhotoUtil.saveImageToInternalStorage(
                        requireActivity(),
                        imageUri!!
                    ) as HashMap<String, String>

                    processPhotoWorks(currentLocation, filenamePath, itemEstiWorks)

                    groupAdapter.notifyItemChanged(0)
                }
                else -> toast("Error: Current location is null!")
            }

        } catch (e: Exception) {
            toast(R.string.error_getting_image)
            e.printStackTrace()
        }


    }

    private fun processPhotoWorks(
        currentLocation: Location?,
        filenamePath: HashMap<String, String>,
        itemEstiWorks: JobEstimateWorksDTO
    ) {

        if (currentLocation == null) {
            // Check network availability / connectivity
            requireActivity().toast("Please enable location services.")
            // Launch Dialog
        } else {
            // requireMutex
            val photo = createItemWorksPhoto(
                filenamePath,
                currentLocation
            )
            estimateWorksPhotoArrayList.add(photo)
            // unlock mutex
            Coroutines.main {
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
                        this.requireActivity(),
                        imageUrl,
                        PhotoQuality.HIGH
                    )

                // Push photo into ImageCollectionView
                image_collection_view.addImage(
                    bitmap!!,
                    object : ImageCollectionView.OnImageClickListener {
                        override fun onClick(bitmap: Bitmap, imageView: ImageView) {
                            showZoomedImage(imageUrl)
                        }
                    })
            }


        }
    }

    private fun showZoomedImage(imageUrl: Uri) {
        val dialog = Dialog(this.requireActivity(), R.style.dialog_full_screen)
        dialog.setContentView(R.layout.new_job_photo)
        val zoomageView =
            dialog.findViewById<ZoomageView>(R.id.zoomedImage)
        GlideApp.with(this.requireActivity())
            .load(imageUrl)
            .into(zoomageView!!)
        dialog.show()
    }

    private fun createItemWorksPhoto(
        filenamePath: HashMap<String, String>,
        currentLocation: Location
    ): JobEstimateWorksPhotoDTO {
        val photoId = SqlLitUtils.generateUuid()

//        = estimateWorksPhotoArrayList
//        itemEstiWorks.jobEstimateWorksPhotos?.add(jobItemWorksPhoto)
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
        Coroutines.main {

            val workDone: Int = workViewModel.getJobItemsEstimatesDoneForJobId(
                estimateJob.JobId,
                ActivityIdConstants.ESTIMATE_WORK_PART_COMPLETE,
                ActivityIdConstants.EST_WORKS_COMPLETE
            )

            if (workDone == estimateJob.JobItemEstimates?.size) {
                val iItems = estimateJob.JobItemEstimates
                // estimate
                submitAllOutStandingEstimates(iItems)
            } else {

                val estimateWorksData =
                    workViewModel.getJobEstiItemForEstimateId(estimateItem.estimateId)
                estimateWorksData.observe(viewLifecycleOwner, Observer { estimateWorksList ->

                    for (workItem in estimateWorksList) {
                        if (workItem.actId == ActivityIdConstants.EST_WORKS_COMPLETE) {
                            Coroutines.main {
                                val estWorkDone: Int =
                                    workViewModel.getJobItemsEstimatesDoneForJobId(
                                        estimateJob.JobId,
                                        ActivityIdConstants.ESTIMATE_WORK_PART_COMPLETE,
                                        ActivityIdConstants.EST_WORKS_COMPLETE
                                    )
                                if (estWorkDone == estimateJob.JobItemEstimates?.size) {
                                    val iItems = estimateJob.JobItemEstimates
                                    submitAllOutStandingEstimates(iItems)
                                } else {
                                    popViewOnWorkSubmit(requireView())
                                }
                            }
                        } else {
                            val id = ActivityIdConstants.JOB_APPROVED

                            // Remove for Dynamic Workflow

                            Coroutines.main {
                                val workflowStepData = workViewModel.getWorkFlowCodes(id)
                                workflowStepData.observe(
                                    viewLifecycleOwner,
                                    Observer { workflowSteps ->
                                        jobWorkStep = workflowSteps as ArrayList<WF_WorkStepDTO>

                                        initRecyclerView(
                                            estimateWorksList.toWorkStateItems(),
                                            workflowSteps
                                        )
                                    })

                            }
                        }
                        itemEstiWorks = workItem
                    }
                    estimateWorksArrayList = estimateWorksList as ArrayList<JobEstimateWorksDTO>
                })

            }

        }
    }

    private fun popViewOnWorkSubmit(view: View) {
        Navigation.findNavController(requireView())
            .navigate(R.id.action_captureWorkFragment_to_nav_work)
    }

    private fun submitAllOutStandingEstimates(estimates: ArrayList<JobItemEstimateDTO>?) {
        // get Data from db Search for all estimates 8 and work 21 = result is int > 0  then button yes else fetch
        Coroutines.main {
            if (estimates?.size != 0) {
                val dialogBuilder: AlertDialog.Builder = AlertDialog.Builder(requireActivity())
                dialogBuilder.setTitle(R.string.confirm)
                dialogBuilder.setIcon(R.drawable.ic_error)
                dialogBuilder.setMessage("Work Complete - Submit for Measurements")
                dialogBuilder.setCancelable(false)
                dialogBuilder.setPositiveButton(
                    R.string.yes
                ) { dialog, which ->

                    for (jobEstimate in estimates!!.iterator()) {
                        Coroutines.main {
                            //                                if(jobEstimate != null){
                            val jobItemEstimate = workViewModel.getJobItemEstimateForEstimateId(
                                DataConversion.toBigEndian(jobEstimate.estimateId)!!
                            )
                            jobItemEstimate.observe(viewLifecycleOwner, Observer { jobItEstmt ->
                                moveJobItemEstimateToNextWorkflow(
                                    WorkflowDirection.NEXT,
                                    jobItEstmt
                                )
                            })

                        }

                    }
                }

                dialogBuilder.show()

            }

        }
    }

    private fun moveJobItemEstimateToNextWorkflow(
        workflowDirection: WorkflowDirection,
        jobItEstimate: JobItemEstimateDTO?
    ) {

        Coroutines.main {
            val user = workViewModel.user.await()
            user.observe(viewLifecycleOwner, Observer { user_ ->

                Coroutines.main {
                    when {
                        user_.userId.isBlank() -> {
                            toast("Error: userId is null")
                        }
                        jobItEstimate?.jobId == null -> {
                            toast("Error: selectedJob is null")
                        }
                        else -> {
                            toast(jobItEstimate.jobId)
                            // TODO beware littleEndian conversion
                            val trackRouteId: String =
                                DataConversion.toLittleEndian(jobItEstimate.trackRouteId)!!
                            val direction: Int = workflowDirection.value
                            Coroutines.main {
                                val progressDialog = setDataProgressDialog(
                                    requireActivity(),
                                    getString(R.string.data_loading_please_wait)
                                )
                                progressDialog.show()
                                val submit = workViewModel.processWorkflowMove(
                                    user_.userId,
                                    trackRouteId,
                                    null,
                                    direction
                                )
                                progressDialog.dismiss()
                                if (submit.isNullOrEmpty()) {
                                    popViewOnJobSubmit(direction)
                                } else {
                                    toast("Problem with work submission: $submit")
                                }

                            }

                        }
                    }
                }
            })

        }
    }

    private fun popViewOnJobSubmit(direction: Int) {
        if (direction == WorkflowDirection.NEXT.value) {
            toast(R.string.job_approved)
        } else if (direction == WorkflowDirection.FAIL.value) {
            toast(R.string.job_declined)
        }
        Intent(activity, MainActivity::class.java).also { home ->
            startActivity(home)
        }
    }

    private fun initRecyclerView(
        stateItems: List<WorkStateItem>, workCodes: List<WF_WorkStepDTO>
    ) {
        groupAdapter.apply {
            Coroutines.main {
                groupAdapter.clear()
                for (i in workCodes.indices) {//stateItems.indices
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
//    private fun List<WF_WorkStepDTO>.toWorkStateItems(): List<WorkState_Item> {

        return this.map { approveJobItems ->
            WorkStateItem(approveJobItems, activity, groupAdapter, jobWorkStep)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (activity as MainActivity).supportActionBar?.title = getString(R.string.capture_work_title)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun getCurrentLocation(): Location? {
        return currentLocation
    }

    fun setCurrentLocation(currentLocation: Location?) {
        this.currentLocation = currentLocation
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
