package za.co.xisystems.itis_rrm.ui.mainview.work.capture_work

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import icepick.State
import kotlinx.android.synthetic.main.fragment_capture_work.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import za.co.xisystems.itis_rrm.MainActivity
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.data.localDB.entities.*
import za.co.xisystems.itis_rrm.ui.mainview._fragments.BaseFragment
import za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils.intents.AbstractIntent
import za.co.xisystems.itis_rrm.ui.mainview.work.WorkViewModel
import za.co.xisystems.itis_rrm.ui.mainview.work.WorkViewModelFactory
import za.co.xisystems.itis_rrm.ui.mainview.work.work_utils.LocationHelper
import za.co.xisystems.itis_rrm.ui.mainview.work.workstate_item.WorkStateItem
import za.co.xisystems.itis_rrm.utils.*
import za.co.xisystems.itis_rrm.utils.enums.WorkflowDirection
import za.co.xisystems.itis_rrm.utils.zoomage.ZoomageView
import java.util.*
import kotlin.collections.ArrayList


class CaptureWorkFragment : BaseFragment(), KodeinAware {

    override val kodein by kodein()
    private lateinit var workViewModel: WorkViewModel
    private val factory: WorkViewModelFactory by instance()

    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 1
        private const val REQUEST_STORAGE_PERMISSION = 1
    }

    private var imageUri: Uri? = null
    private var mTempPhotoPath: String? = null

    //    private var estimatId :String? = null
    private lateinit var workFlowRoute: ArrayList<Long>
    private lateinit var workFlowMenuTitles: ArrayList<String>
    private lateinit var groupAdapter: GroupAdapter<GroupieViewHolder>
    private lateinit var estimateWorksPhotoArrayList: ArrayList<JobEstimateWorksPhotoDTO>
    private lateinit var estimateWorksArrayList: ArrayList<JobEstimateWorksDTO>
    private lateinit var estimateWorksList: ArrayList<JobEstimateWorksDTO>
    private lateinit var itemEsti: JobItemEstimateDTO
    private lateinit var itemEstiJob: JobDTO
    private var jobitemEsti: JobItemEstimateDTO? = null
    private var jobitemEstiWorks: JobEstimateWorksDTO? = null
    private lateinit var itemEstiWorks: JobEstimateWorksDTO
    private lateinit var jobWorkStep: ArrayList<WF_WorkStepDTO>


    @State
    var filename_path = HashMap<String, String>()
    lateinit var locationHelper: LocationHelper
    private var currentLocation: Location? = null
    //    private var index = -1
    lateinit var useR: UserDTO

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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity as MainActivity).supportActionBar?.title = getString(R.string.capture_work_title)
//        estimateWorksPhotoArrayList = ArrayList<EstimateWorksPhoto>()
        workFlowMenuTitles = ArrayList()
        groupAdapter = GroupAdapter()
        locationHelper =
            LocationHelper(
                this
            )
        locationHelper.onCreate()
        estimateWorksPhotoArrayList = ArrayList<JobEstimateWorksPhotoDTO>()
        estimateWorksList = ArrayList<JobEstimateWorksDTO>()
        estimateWorksArrayList = ArrayList<JobEstimateWorksDTO>()
        jobWorkStep = ArrayList<WF_WorkStepDTO>()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_capture_work, container, false)
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
            workViewModel.work_ItemJob.observe(viewLifecycleOwner, Observer { estimateJob ->
                itemEstiJob = estimateJob

            })
            workViewModel.work_Item.observe(viewLifecycleOwner, Observer { estimate ->
                getWorkItems(estimate, itemEstiJob)
//                estimatId = estimate.estimateId
                itemEsti = estimate

            })


        }

        work_imageView.visibility = View.GONE
        take_photo_button.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    activity!!.applicationContext,
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
        move_workflow_button.setOnClickListener {
            if (estimateWorksPhotoArrayList.size > 0) {
                val prog =
                    setDataProgressDialog(activity!!, getString(R.string.data_loading_please_wait))
                if (ServiceUtil.isNetworkConnected(activity!!.applicationContext)) { //  Lets Send to Service

                    itemEstiWorks.jobEstimateWorksPhotos = estimateWorksPhotoArrayList
                    itemEstiWorks.jobItemEstimate = jobitemEsti

                    sendJobToService(itemEstiWorks, prog)
                } else {
                    val networkToast = Toast.makeText(
                        activity?.getApplicationContext(),
                        R.string.no_connection_detected,
                        Toast.LENGTH_LONG
                    )
                    networkToast.setGravity(Gravity.CENTER_VERTICAL, 0, 0)
                    networkToast.show()

                }

            } else {
                val validation = Toast.makeText(
                    activity?.applicationContext,
                    getString(R.string.please_make_sure_workflow_items_contain_photos),
                    Toast.LENGTH_LONG
                )
                validation.setGravity(Gravity.CENTER_VERTICAL, 0, 0)
                validation.show()


            }

        }

        work_imageView.setOnClickListener {
            showZoomedImage(imageUri!!)
        }
    }


    private fun sendJobToService(
        itemEstiWorks: JobEstimateWorksDTO,

        prog: ProgressDialog
    ) {
        Coroutines.main {

            val newitemEstiWorks = setJobWorksLittleEndianGuids(itemEstiWorks)
            val response = workViewModel.submitWorks(newitemEstiWorks, activity!!, itemEstiJob)
            if (response != null) {
                refreshView(prog)
            } else
                activity?.toast(response)

        }
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
            work_imageView.clearFocus()
            estimateWorksPhotoArrayList.clear()
            comments_editText.setText("")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

                fragmentManager?.beginTransaction()?.detach(this)?.commitNow()
                prog.dismiss()
                fragmentManager?.beginTransaction()?.attach(this)?.commitNow()

            } else {
                prog.dismiss()
                fragmentManager?.beginTransaction()?.detach(this)?.attach(this)?.commit()

//                                initRecyclerView(work_s.toWorkStateItems(),workCodes)
            }
//            val works = workViewModel.getJobsForActivityId(ActivityIdConstants.JOB_APPROVED, ActivityIdConstants.ESTIMATE_INCOMPLETE)
//            works.observe(viewLifecycleOwner, Observer { work_s ->

            workViewModel.work_Item.observe(viewLifecycleOwner, Observer { estimate ->
                //                getWorkItems(estimate, itemEstiJob)

                val id = 3 //TODO("THis part must be Deleted when the Dynamic workflow is Added")
                Coroutines.main {
                    val workcode = workViewModel.getWokrCodes(id)
                    workcode.observe(viewLifecycleOwner, Observer { workCodes ->
                        prog.dismiss()
                        groupAdapter.notifyItemChanged(2)
//                        getWorkItems(estimate.estimateId, estimate, itemEstiJob)
                        Log.e("IsRefresh", "Yes")
                    })
                }


            })

//            })
        }
    }

    private fun launchCamera() {

        Coroutines.main {
            imageUri = PhotoUtil.getUri3(activity!!.applicationContext)!!
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (takePictureIntent.resolveActivity(activity!!.packageManager) != null) {
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
            processAndSetImage()
            work_imageView.visibility = View.VISIBLE
        }
    }

    private fun processAndSetImage() {
        try { //  Location of picture
            val currentLocation: Location = locationHelper.getCurrentLocation()!!
            if (currentLocation == null) toast("Error: Current location is null!")
            //  Save Image to Internal Storage

            filename_path = PhotoUtil.saveImageToInternalStorage(
                activity!!,
                imageUri!!
            ) as HashMap<String, String>

            processPhotoWorks(currentLocation, filename_path, itemEsti)

        } catch (e: Exception) {
            toast(R.string.error_getting_image)
            e.printStackTrace()
        }
        GlideApp.with(this)
            .asBitmap()
            .load(imageUri)
            .into(work_imageView)

        groupAdapter.notifyItemChanged(0)
    }

    private fun processPhotoWorks(
        currentLocation: Location,
        filenamePath: HashMap<String, String>,
        itemEsti: JobItemEstimateDTO
    ) {

        if (currentLocation == null) {
            activity!!.toast("Please make sure that you have activated the location on your device.")
        } else {
            val photo = createItemWorksPhoto(
                itemEsti,
                filenamePath,
                currentLocation,
                estimateWorksPhotoArrayList
            )
            Coroutines.main {
                itemEstiWorks.jobEstimateWorksPhotos = estimateWorksPhotoArrayList
                workViewModel.createSaveWorksPhotos(
                    estimateWorksPhotoArrayList,
                    itemEsti,
                    itemEstiWorks
                )
            }
        }
    }

    private fun showZoomedImage(imageUrl: Uri) {
        val dialog = Dialog(activity?.applicationContext, R.style.dialog_full_screen)
        dialog.setContentView(R.layout.new_job_photo)
        val zoomageView =
            dialog.findViewById<ZoomageView>(R.id.zoomedImage)
        GlideApp.with(this.activity!!)
            .load(imageUrl)
            .into(zoomageView)
        dialog.show()
    }

    private fun createItemWorksPhoto(
        itemEsti: JobItemEstimateDTO,
        filenamePath: HashMap<String, String>,
        currentLocation: Location,
        estimateWorksPhotoArrayList: ArrayList<JobEstimateWorksPhotoDTO>
    ): JobEstimateWorksPhotoDTO {
        val photoId = SqlLitUtils.generateUuid()
        val jobItemWorksPhoto = JobEstimateWorksPhotoDTO(
            0,
            "",
            filenamePath["filename"]!!,
            itemEstiWorks.actId,
            DateUtil.DateToString(Date())!!
            ,
            photoId,
            currentLocation.latitude,
            currentLocation.longitude,
            filenamePath["path"]!!,
            estimateWorksList,
            0,
            0,
            itemEstiWorks.worksId

        )
        estimateWorksPhotoArrayList.add(jobItemWorksPhoto)
//        = estimateWorksPhotoArrayList
//        itemEstiWorks.jobEstimateWorksPhotos?.add(jobItemWorksPhoto)
        return jobItemWorksPhoto
    }


    private fun getWorkItems(
//        estimateId: String?,
        estimate: JobItemEstimateDTO,
        itemEstiJob: JobDTO
    ) {
        Coroutines.main {
            //            submitAllOutStandingEstimates(estimate)/
            val workDone: Int = workViewModel.getJobItemsEstimatesDoneForJobId(
                itemEstiJob.JobId,
                ActivityIdConstants.ESTIMATE_WORK_PART_COMPLETE,
                ActivityIdConstants.EST_WORKS_COMPLETE
            )
            if (workDone == itemEstiJob.JobItemEstimates?.size) {
                val iItems = itemEstiJob.JobItemEstimates
                estimate
                submitAllOutStandingEstimates(iItems)
            } else {

                val estimateWorks = workViewModel.getJobEstiItemForEstimateId(estimate.estimateId)
                estimateWorks.observe(viewLifecycleOwner, Observer { work_s ->
                    //                    setButtonStates(work_s.get(0).actId, work_s)
                    for (itemwork in work_s) {
                        if (itemwork?.actId == ActivityIdConstants.EST_WORKS_COMPLETE) {
                            Coroutines.main {
                                val workDone: Int = workViewModel.getJobItemsEstimatesDoneForJobId(
                                    itemEstiJob.JobId,
                                    ActivityIdConstants.ESTIMATE_WORK_PART_COMPLETE,
                                    ActivityIdConstants.EST_WORKS_COMPLETE
                                )
                                if (workDone == itemEstiJob.JobItemEstimates?.size) {
                                    val iItems = itemEstiJob.JobItemEstimates
                                    submitAllOutStandingEstimates(iItems)
                                } else {
                                    popViewOnWorkSubmit(view)
                                }
                            }
                        } else {
                            val id =
                                3 //TODO("THis part must be Deleted when the Dynamic workflow is Implemented ")
                            Coroutines.main {
                                val workcode = workViewModel.getWokrCodes(id)
                                workcode.observe(viewLifecycleOwner, Observer { workCodes ->
                                    jobWorkStep = workCodes as ArrayList<WF_WorkStepDTO>

                                    initRecyclerView(work_s.toWorkStateItems(), workCodes)
                                })

                            }
                        }
                        itemEstiWorks = itemwork
                        estimateWorksArrayList = work_s as ArrayList<JobEstimateWorksDTO>


                    }
                })

            }

        }
    }

    private fun popViewOnWorkSubmit(view: View?) {
        Navigation.findNavController(view!!).navigate(R.id.action_captureWorkFragment_to_nav_work)
    }

    private fun submitAllOutStandingEstimates(estimate: ArrayList<JobItemEstimateDTO>?) {
        // get Data from db Search for all estimates 8 and work 21 = result is int > 0  then button yes else fetch
        Coroutines.main {
            if (estimate?.size != 0) {
                val dialogBuilder: AlertDialog.Builder = AlertDialog.Builder(activity!!)
                dialogBuilder.setTitle(R.string.confirm)
                dialogBuilder.setIcon(R.drawable.ic_error)
                dialogBuilder.setMessage("All work is Done, Submit for Measurements")
                dialogBuilder.setCancelable(false)
                dialogBuilder.setPositiveButton(
                    R.string.yes
                ) { dialog, which ->

                    for (jobEstimate in estimate!!.iterator()) {
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
        val messages = arrayOf(
            getString(R.string.moving_to_next_step_in_workflow),
            getString(R.string.please_wait)
        )
        Coroutines.main {
            val user = workViewModel.user.await()
            user.observe(viewLifecycleOwner, Observer { user_ ->
                //                approveViewModel.jobapproval_Item6.observe(viewLifecycleOwner, Observer { job ->
                Coroutines.main {
                    when {
                        user_.userId == null -> {
                            toast("Error: userId is null")
                        }
                        jobItEstimate?.jobId == null -> {
                            toast("Error: selectedJob is null")
                        }
                        else -> {
                            toast(jobItEstimate?.jobId)
                            // TODO beware littlEndian conversion
                            val trackRounteId: String =
                                DataConversion.toLittleEndian(jobItEstimate?.trackRouteId)!!
                            val direction: Int = workflowDirection.getValue()
                            Coroutines.main {
                                val prog = setDataProgressDialog(
                                    activity!!,
                                    getString(R.string.data_loading_please_wait)
                                )
                                prog.show()
                                val submit = workViewModel.processWorkflowMove(
                                    user_.userId,
                                    trackRounteId,
                                    null,
                                    direction
                                )
                                if (submit.isNullOrEmpty()) {
                                    //                                toast(submit)
                                    //                                 prog.dismiss()
                                    popViewOnJobSubmit(direction, submit)
                                } else {
                                    //                                prog.dismiss()
                                    popViewOnJobSubmit(direction, submit)
                                }

                            }


                            //                        processWorkFlow(user_.userId, trackRounteId, direction, description)
                            //                        popViewOnJobSubmit(direction)
                        }
                    }
                }
            })

        }


    }

    private fun popViewOnJobSubmit(direction: Int, submit: String) {
        if (direction == WorkflowDirection.NEXT.value) {
            toast(R.string.job_approved)
        } else if (direction == WorkflowDirection.FAIL.value) {
            toast(R.string.job_declined)
        }
        Intent(activity, MainActivity::class.java).also { home ->
            //            home.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(home)
        }
        // Navigation.findNavController(view!!)
//                .navigate(R.id.action_jobInfoFragment_to_nav_home)
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

//                val workcode =  workViewModel.getWorkCodes.await()
//                workcode.observe(viewLifecycleOwner, Observer { workCodes ->
//
////
//                })


            }


            //val workState = arrayOf("TA", "START", "MIDDLE", "END", "RTA")
        }
        work_actions_listView.apply {
            layoutManager = LinearLayoutManager(this.context)
            adapter = groupAdapter

        }

//        groupAdapter.setOnItemClickListener { item, view ->
//
//        }

    }

    private fun setButtonStates(
        actId: Int,
        workS: List<JobEstimateWorksDTO>
    ) {
        for (jobEstimateWorks in workS) {
            if (jobEstimateWorks.actId != actId) {
                comments_editText.visibility = View.GONE
                headimg.visibility = View.GONE
                take_photo_button.visibility = View.GONE
                move_workflow_button.visibility = View.GONE
                comments_editText.isEnabled = false
                take_photo_button.isEnabled = false
                move_workflow_button.isEnabled = false
                toast("You Have a Previous Step that Needs to be Done")
            } else {
                comments_editText.visibility = View.VISIBLE
                headimg.visibility = View.VISIBLE
                take_photo_button.visibility = View.VISIBLE
                move_workflow_button.visibility = View.VISIBLE
                comments_editText.isEnabled = true
                take_photo_button.isEnabled = true
                move_workflow_button.isEnabled = true
            }
        }

    }

    private fun List<JobEstimateWorksDTO>.toWorkStateItems(): List<WorkStateItem> {
//    private fun List<WF_WorkStepDTO>.toWorkStateItems(): List<WorkState_Item> {

        return this.map { approveJobItems ->
            WorkStateItem(approveJobItems, activity, groupAdapter, jobWorkStep)
        }
    }


    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {

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

//val photos = intArrayOf(estimateWorksPhotoArrayList.size)
//
//            // create the ImageSwitcher
//            val imgSwitcher = ImageSwitcher(activity?.applicationContext)
//
//            imgSwitcher?.setFactory({
//                val imgView = ImageView(activity?.applicationContext)
//                imgView.scaleType = ImageView.ScaleType.FIT_CENTER
//                imgView.setPadding(20, 20, 20, 20)
//                imgView
//            })
//
//            val c_Layout = activity?.findViewById<ConstraintLayout>(R.id.thumb_photo_place_holder_frameLayout)
//            //add ImageSwitcher in constraint layout
//            c_Layout?.addView(imgSwitcher)
//
//            // set the method and pass array as a parameter
//            imgSwitcher?.setImageResource(photos[index])
//
//            val imgIn = AnimationUtils.loadAnimation(
//                activity, android.R.anim.slide_in_left)
//            imgSwitcher?.inAnimation = imgIn
//            val imgOut = AnimationUtils.loadAnimation(
//                activity, android.R.anim.slide_out_right)
//            imgSwitcher?.outAnimation = imgOut
//            // previous button functionality
//            val prev = activity?.findViewById<Button>(R.id.prev)
//            prev?.setOnClickListener {
//                index = if (index - 1 >= 0) index - 1 else 1
//                imgSwitcher?.setImageResource(photos[index])
//            }
//            // next button functionality
//            val next = activity?.findViewById<Button>(R.id.next)
//            next?.setOnClickListener {
//                index = if (index + 1 < photos.size) index +1 else 0
//                imgSwitcher?.setImageResource(photos[index])
//            }