package za.co.xisystems.itis_rrm.ui.mainview.approvemeasure.measure_approval

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import kotlinx.android.synthetic.main.fragment_measure_approval.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import za.co.xisystems.itis_rrm.MainActivity
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemMeasureDTO
import za.co.xisystems.itis_rrm.ui.mainview._fragments.BaseFragment
import za.co.xisystems.itis_rrm.ui.mainview.approvemeasure.ApproveMeasureViewModel
import za.co.xisystems.itis_rrm.ui.mainview.approvemeasure.ApproveMeasureViewModelFactory
import za.co.xisystems.itis_rrm.ui.mainview.approvemeasure.approveMeasure_Item.ApproveMeasureItem
import za.co.xisystems.itis_rrm.utils.ActivityIdConstants
import za.co.xisystems.itis_rrm.utils.Coroutines
import za.co.xisystems.itis_rrm.utils.DataConversion
import za.co.xisystems.itis_rrm.utils.ServiceUtil
import za.co.xisystems.itis_rrm.utils.enums.WorkflowDirection


class MeasureApprovalFragment : BaseFragment(R.layout.fragment_measure_approval), KodeinAware {
    override val kodein by kodein()
    private lateinit var approveViewModel: ApproveMeasureViewModel
    private val factory: ApproveMeasureViewModelFactory by instance()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (activity as MainActivity).supportActionBar?.title = getString(R.string.measure_approval_title)

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_measure_approval, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        approveViewModel = activity?.run {
            ViewModelProvider(this, factory).get(ApproveMeasureViewModel::class.java)
        } ?: throw Exception("Invalid Activity")

        Coroutines.main {

            approveViewModel.measureapproval_Item.observe(viewLifecycleOwner, Observer { job ->
                getMeasureItems(job)
            })

           approve_measure_button.setOnClickListener {
                val logoutBuilder = AlertDialog.Builder(
                    activity!! //, android.R.style.Theme_DeviceDefault_Dialog
                )
                logoutBuilder.setTitle(R.string.confirm)
                logoutBuilder.setIcon(R.drawable.ic_approve)
                logoutBuilder.setMessage(R.string.are_you_sure_you_want_to_approve2)

                // Yes button
                logoutBuilder.setPositiveButton(
                    R.string.yes
                ) { dialog, which ->
                    if (ServiceUtil.isNetworkConnected(context?.applicationContext)) {
                        moveJobToNextWorkflow(WorkflowDirection.NEXT)
                    } else {
                        toast( R.string.no_connection_detected)
                    }
                }
                // No button
                // No button
                logoutBuilder.setNegativeButton(
                    R.string.no
                ) { dialog, which ->
                    // Do nothing but close dialog
                    dialog.dismiss()
                }
                val declineAlert = logoutBuilder.create()
                declineAlert.show()
            }

            decline_measure_button.setOnClickListener {
                val logoutBuilder =
                    AlertDialog.Builder(
                        activity!! //, android.R.style.Theme_DeviceDefault_Dialog
                    )
                logoutBuilder.setTitle(R.string.confirm)
                logoutBuilder.setIcon(R.drawable.ic_warning)
                logoutBuilder.setMessage(R.string.are_you_sure_you_want_to_decline2)
                // Yes button
                logoutBuilder.setPositiveButton(
                    R.string.yes
                ) { dialog, which ->
                    if (ServiceUtil.isNetworkConnected(context?.applicationContext)) {
//                        moveJobToNextWorkflow(WorkflowDirection.FAIL)
                    } else {
                        toast(R.string.no_connection_detected)
                    }
                }
                // No button
                logoutBuilder.setNegativeButton(
                    R.string.no
                ) { dialog, which ->
                    // Do nothing but close dialog
                    dialog.dismiss()
                }
                val declineAlert = logoutBuilder.create()
                declineAlert.show()
            }

        }
    }

//    private fun moveJobToNextWorkflow(workflowDirection: WorkflowDirection) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//    }

    private fun moveJobToNextWorkflow(workflowDirection : WorkflowDirection) {
        Coroutines.main {
            val messages = arrayOf(
                getString(R.string.moving_to_next_step_in_workflow),
                getString(R.string.please_wait)
            )

            val user = approveViewModel.user.await()
            user.observe(viewLifecycleOwner, Observer { user_ ->
                approveViewModel.measureapproval_Item.observe(viewLifecycleOwner, Observer { job ->

                    if (user_.userId == null) {
                        toast("Error: userId is null")
                        // } else if ( getEstimateItems(job.jobItemMeasureDTO.jobId) == null) {
                       // toast("Error: selectedJob is null")
                    } else {
                        // TODO beware littleEndian conversion
                        val trackRouteId: String =
                            DataConversion.toLittleEndian(job.jobItemMeasureDTO.trackRouteId)!!
                        val direction: Int = workflowDirection.value

                        val description = ""
//                        val messages = messages
//                        if (workflow_comments_editText.text != null){
//                            description = workflow_comments_editText.text.toString()
//                        }else{
//                            toast(context?.applicationContext!!.getString(R.string.enter_comment))
//                            return@Observer
//                        }


                        processWorkFlow(user_.userId, trackRouteId, direction, description)
                    }

                })
            })
        }

    }

    private fun processWorkFlow(
        userId: String, trackRouteId: String, direction: Int, description: String?
    ) {
        Coroutines.main {
            val prog = ProgressDialog(activity)
            prog.setTitle(getString(R.string.please_wait))
            prog.setMessage(getString(R.string.loading_job_wait))
            prog.setCancelable(false)
            prog.isIndeterminate = true
            prog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
            prog.show()
            val submit =
                approveViewModel.processWorkflowMove(userId, trackRouteId, description, direction)
//            activity?.hideKeyboard()
            if (submit != null){
                prog.dismiss()
                toast(submit) }else {
                prog.dismiss()
                toast(R.string.job_submitted)
                popViewOnJobSubmit(direction)}
//            popViewOnJobSubmit(direction)
        }
    }

    private fun popViewOnJobSubmit(direction: Int) {
        if (direction == WorkflowDirection.NEXT.value) {
            toast(R.string.job_approved)
        } else if (direction == WorkflowDirection.FAIL.value) {
            toast(R.string.job_declined)
        }

        Intent(context?.applicationContext , MainActivity::class.java).also { home ->
//            home.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(home)
        }
        // Navigation.findNavController(view!!)
//                .navigate(R.id.action_jobInfoFragment_to_nav_home)
    }


    private fun getMeasureItems(job: ApproveMeasureItem) {
        Coroutines.main {
            val measurements = approveViewModel.getJobMeasureItemsForJobId(job.jobItemMeasureDTO.jobId, ActivityIdConstants.MEASURE_COMPLETE)
            measurements.observe(viewLifecycleOwner, Observer { job_s ->
                val measureItems = job_s.distinctBy {
                    it.jobId
                }
//
                toast(job_s.size.toString())
                initRecyclerView(measureItems.toMeasureItem())

            })

        }
    }

    private fun initRecyclerView(measureListItems: List<MeasurementsItem>) {
        val groupAdapter = GroupAdapter<GroupieViewHolder>().apply {
            addAll(measureListItems)
        }
        view_measured_items.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = groupAdapter

        }

    }

    private fun List<JobItemMeasureDTO>.toMeasureItem(): List<MeasurementsItem> {
        return this.map { approvedJobItem ->
            //            getEstimateItemsPhoto(approvedJobItem.estimateId)
            MeasurementsItem(approvedJobItem, approveViewModel, activity)
        }
    }









    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {
//        listener?.onFragmentInteraction(uri)
    }


}
