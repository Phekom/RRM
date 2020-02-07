package za.co.xisystems.itis_rrm.ui.mainview.work.capture_work

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import kotlinx.android.synthetic.main.fragment_capture_work.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import za.co.xisystems.itis_rrm.MainActivity
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.data.localDB.entities.JobEstimateWorksDTO
import za.co.xisystems.itis_rrm.ui.mainview._fragments.BaseFragment
import za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils.intents.AbstractIntent
import za.co.xisystems.itis_rrm.ui.mainview.work.WorkViewModel
import za.co.xisystems.itis_rrm.ui.mainview.work.WorkViewModelFactory
import za.co.xisystems.itis_rrm.ui.mainview.work.workstate_item.WorkState_Item
import za.co.xisystems.itis_rrm.utils.Coroutines
import za.co.xisystems.itis_rrm.utils.GlideApp
import za.co.xisystems.itis_rrm.utils.PhotoUtil
import za.co.xisystems.itis_rrm.utils.ServiceUtil
import za.co.xisystems.itis_rrm.utils.zoomage.ZoomageView
import java.util.*


class CaptureWorkFragment : BaseFragment(), KodeinAware {

    override val kodein by kodein()
    private lateinit var workViewModel: WorkViewModel
    private val factory: WorkViewModelFactory by instance()

    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 1
        private const val REQUEST_STORAGE_PERMISSION = 1
    }
    private lateinit var imageUri: Uri
    private var mTempPhotoPath: String? = null


    private lateinit var workFlowRoute: ArrayList<Long>
    private lateinit var workFlowMenuTitles: ArrayList<String>
    private lateinit var groupAdapter : GroupAdapter<GroupieViewHolder>







    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity as MainActivity).supportActionBar?.title = getString(R.string.capture_work_title)
//        estimateWorksPhotoArrayList = ArrayList<EstimateWorksPhoto>()
        workFlowMenuTitles = ArrayList()
        groupAdapter = GroupAdapter<GroupieViewHolder>()
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
            ViewModelProviders.of(this, factory).get(WorkViewModel::class.java)
        } ?: throw Exception("Invalid Activity") as Throwable

        Coroutines.main {

            workViewModel.work_Item.observe(viewLifecycleOwner, Observer { estimateId ->
                getWorkItems(estimateId)
            })

        }

        work_imageView.visibility = View.GONE
        take_photo_button.setOnClickListener {
            launchCamera()
        }
        move_workflow_button.setOnClickListener {
            toast("Ready to Submit the Images")
            createUpdateDatabase()
            if (ServiceUtil.isNetworkConnected(activity!!.applicationContext)) { //  Lets Send to Service
//  sendJobToService(fetchLatestJobInfoFromSQLite());
                sendJobToService()
            } else {

            }
        }

        work_imageView.setOnClickListener {
            showZoomedImage(imageUri)
        }
    }

    private fun createUpdateDatabase() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun sendJobToService() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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
        } else { // Otherwise, delete the temporary image file
//            BitmapUtils.deleteImageFile(activity!!.applicationContext, mTempPhotoPath)
        }
    }

    private fun processAndSetImage() {
        GlideApp.with(this)
            .load(imageUri)
            .into(work_imageView)
//        photoButtons.visibility = View.VISIBLE
    }

    private fun showZoomedImage(imageUrl: Uri) {
        val dialog = Dialog(this!!.activity, R.style.dialog_full_screen)
        dialog.setContentView(R.layout.new_job_photo)
        val zoomageView =
            dialog.findViewById<ZoomageView>(R.id.zoomedImage)
        GlideApp.with(this.activity!!)
            .load(imageUrl)
            .into(zoomageView)
        dialog.show()
    }

    private fun getWorkItems(estimateId: String?) {
        Coroutines.main {
            val estimate = workViewModel.getJobEstiItemForEstimateId(estimateId)
            estimate.observe(viewLifecycleOwner, Observer { work_s ->
                setButtonStates(work_s[0].actId, work_s)
                initRecyclerView(work_s.toWorkStateItems())
            })

        }
//            val works = workViewModel.getJobsForActivityId(
//                ActivityIdConstants.JOB_APPROVED
//                , ActivityIdConstants.ESTIMATE_INCOMPLETE
//            )
//
////            val jobs = approveViewModel.offlinedata.await()
//            works.observe(viewLifecycleOwner, Observer { work_s ->
//                noData.visibility = View.GONE
//                toast(work_s.size.toString())
//                initRecyclerView(work_s.toWorkListItems())
//                group7_loading.visibility = View.GONE
//
    }

    private fun initRecyclerView(stateItems : List<WorkState_Item>) {
        groupAdapter.apply {
            for (i in 0..4) {//stateItems.indices
                add(stateItems[0])
            }
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
            if (jobEstimateWorks.actId !== actId) {
                comments_editText.setVisibility(View.GONE)
                headimg.visibility = View.GONE
                take_photo_button.setVisibility(View.GONE)
                move_workflow_button.setVisibility(View.GONE)
                comments_editText.setEnabled(false)
                take_photo_button.setEnabled(false)
                move_workflow_button.setEnabled(false)
                toast("You Have a Previous Step that Needs to be Done")
            } else {
                comments_editText.setVisibility(View.VISIBLE)
                headimg.visibility = View.VISIBLE
                take_photo_button.setVisibility(View.VISIBLE)
                move_workflow_button.setVisibility(View.VISIBLE)
                comments_editText.setEnabled(true)
                take_photo_button.setEnabled(true)
                move_workflow_button.setEnabled(true)
            }
        }

    }


    private fun List<JobEstimateWorksDTO>.toWorkStateItems(): List<WorkState_Item> {
        return this.map { approvej_items ->
            WorkState_Item(approvej_items, workViewModel, activity, groupAdapter)
        }
    }


    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (activity as MainActivity).supportActionBar?.title = getString(R.string.capture_work_title)
    }

    override fun onDetach() {
        super.onDetach()

    }


}
