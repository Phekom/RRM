package za.co.xisystems.itis_rrm.ui.mainview.approvejobs.view_job_info

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import kotlinx.android.synthetic.main.fragment_approvejob.mydata_loading
import kotlinx.android.synthetic.main.fragment_job_info.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import www.sanju.motiontoast.MotionToast
import za.co.xisystems.itis_rrm.MainActivity
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.base.BaseFragment
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimateDTO
import za.co.xisystems.itis_rrm.ui.extensions.doneProgress
import za.co.xisystems.itis_rrm.ui.extensions.failProgress
import za.co.xisystems.itis_rrm.ui.extensions.initProgress
import za.co.xisystems.itis_rrm.ui.extensions.motionToast
import za.co.xisystems.itis_rrm.ui.extensions.startProgress
import za.co.xisystems.itis_rrm.ui.mainview.approvejobs.ApproveJobsViewModel
import za.co.xisystems.itis_rrm.ui.mainview.approvejobs.ApproveJobsViewModelFactory
import za.co.xisystems.itis_rrm.utils.Coroutines
import za.co.xisystems.itis_rrm.utils.DataConversion
import za.co.xisystems.itis_rrm.utils.ServiceUtil
import za.co.xisystems.itis_rrm.utils.enums.WorkflowDirection
import za.co.xisystems.itis_rrm.utils.hide
import za.co.xisystems.itis_rrm.utils.show

class JobInfoFragment : BaseFragment(R.layout.fragment_job_info), KodeinAware {
    override val kodein by kodein()
    private lateinit var approveViewModel: ApproveJobsViewModel
    private val factory: ApproveJobsViewModelFactory by instance()
    private lateinit var progressButton: Button

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (activity as MainActivity).supportActionBar?.title = getString(R.string.jobinfo_item_title)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity as MainActivity).supportActionBar?.title = getString(R.string.jobinfo_item_title)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_job_info, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        approveViewModel = activity?.run {
            ViewModelProvider(this, factory).get(ApproveJobsViewModel::class.java)
        } ?: throw Exception("Invalid Activity")

        // dialog =
        //     setDataProgressDialog(requireActivity(), getString(R.string.data_loading_please_wait))
        Coroutines.main {
            mydata_loading.show()

            approveViewModel.jobApprovalItem.observe(viewLifecycleOwner, { job ->
                Coroutines.main {
                    getEstimateItems(job.jobDTO.JobId)
                    val description = approveViewModel.getDescForProjectId(job.jobDTO.ProjectId!!)
                    val sectionId = approveViewModel.getProjectSectionIdForJobId(job.jobDTO.JobId)
                    val route = approveViewModel.getRouteForProjectSectionId(sectionId)
                    val section = approveViewModel.getSectionForProjectSectionId(sectionId)

                    project_description_textView.text = description
                    section_description_textView.text = ("$route/ $section")
                    start_km_description_textView.text = (job.jobDTO.StartKm.toString())
                    end_km_description_textView.text = (job.jobDTO.EndKm.toString())
                }
            })
        }

        approve_job_button.setOnClickListener {
            val approvalBuilder = AlertDialog.Builder(
                activity // ,android.R.style.Theme_DeviceDefault_Dialog
            )
            approvalBuilder.setTitle(R.string.confirm)
            approvalBuilder.setIcon(R.drawable.ic_approve)
            approvalBuilder.setMessage(R.string.are_you_sure_you_want_to_approve)

            // Yes button
            approvalBuilder.setPositiveButton(
                R.string.yes
            ) { dialog, which ->
                if (ServiceUtil.isNetworkAvailable(requireContext().applicationContext)) {
                    progressButton = approve_job_button
                    progressButton.initProgress(viewLifecycleOwner)
                    moveJobToNextWorkflow(WorkflowDirection.NEXT)
                } else {
                    this.requireActivity().motionToast(
                        message = getString(R.string.no_connection_detected),
                        motionType = MotionToast.TOAST_ERROR
                    )
                }
            }

            // No button
            approvalBuilder.setNegativeButton(
                R.string.no
            ) { dialog, which ->
                // Do nothing but close dialog
                dialog.dismiss()
            }
            val approvalDialog = approvalBuilder.create()
            approvalDialog.show()
        }

        decline_job_button.setOnClickListener {
            val declineBuilder =
                AlertDialog.Builder(
                    activity // , android.R.style.Theme_DeviceDefault_Dialog
                )
            declineBuilder.setTitle(R.string.confirm)
            declineBuilder.setIcon(R.drawable.ic_warning)
            declineBuilder.setMessage(R.string.are_you_sure_you_want_to_decline)
            // Yes button
            declineBuilder.setPositiveButton(
                R.string.yes
            ) { dialog, which ->
                if (ServiceUtil.isNetworkAvailable(requireContext().applicationContext)) {
                    progressButton = decline_job_button
                    progressButton.initProgress(viewLifecycleOwner)
                    moveJobToNextWorkflow(WorkflowDirection.FAIL)
                } else {
                    this.requireActivity().motionToast(
                        message = getString(R.string.no_connection_detected),
                        motionType = MotionToast.TOAST_ERROR
                    )
                }
            }
            // No button
            declineBuilder.setNegativeButton(
                R.string.no
            ) { dialog, which ->
                // Do nothing but close dialog
                dialog.dismiss()
            }
            val declineAlert = declineBuilder.create()
            declineAlert.show()
        }
    }

    private fun moveJobToNextWorkflow(workflowDirection: WorkflowDirection) {
        Coroutines.main {
            val caption = when (progressButton == approve_job_button) {
                true -> "Approving Job ..."
                else -> "Declining Job ..."
            }
            progressButton.startProgress(caption)
            val user = approveViewModel.user.await()
            user.observe(viewLifecycleOwner, { userDTO ->
                approveViewModel.jobApprovalItem.observe(viewLifecycleOwner, { job ->

                    when {
                        userDTO.userId.isBlank() -> {
                            this@JobInfoFragment.motionToast("The user lacks permissions.", MotionToast.TOAST_ERROR)
                            progressButton.failProgress("Invalid User")
                        }
                        job.jobDTO.JobId.isBlank() -> {
                            this@JobInfoFragment.motionToast("The selected job is invalid.", MotionToast.TOAST_ERROR)
                            progressButton.failProgress("Invalid Job")
                        }
                        else -> {
                            val trackRouteId: String =
                                DataConversion.toLittleEndian(job.jobDTO.TrackRouteId)!!
                            val direction: Int = workflowDirection.value

                            var description: String? = ""
                            if (workflow_comments_editText.text != null)
                                description = workflow_comments_editText.text.toString()
                            processWorkFlow(userDTO.userId, trackRouteId, direction, description)
                        }
                    }
                })
            })
        }
    }

    private fun processWorkFlow(
        userId: String,
        trackRouteId: String,
        direction: Int,
        description: String?
    ) {
        Coroutines.main {
            progressButton.startProgress("Submitting Data ...")

            val response =
                approveViewModel.processWorkflowMove(userId, trackRouteId, description, direction)
            if (response.isNotBlank()) {
                this@JobInfoFragment.motionToast(response, MotionToast.TOAST_ERROR)
                progressButton.failProgress("Workflow Failed")
            } else {
                progressButton.doneProgress("Workflow Complete")
                popViewOnJobSubmit(direction)
            }
        }
    }

    private fun popViewOnJobSubmit(direction: Int) {
        if (direction == WorkflowDirection.NEXT.value) {
            this.motionToast(getString(R.string.job_approved), MotionToast.TOAST_SUCCESS)
        } else if (direction == WorkflowDirection.FAIL.value) {
            this.motionToast(getString(R.string.job_declined), MotionToast.TOAST_INFO)
        }

        Intent(context?.applicationContext, MainActivity::class.java).also { home ->
            home.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(home)
        }
    }

    private fun getEstimateItems(jobID: String?) {
        Coroutines.main {
            val estimates = approveViewModel.getJobEstimationItemsForJobId(jobID)
            estimates.observe(viewLifecycleOwner, { estimateList ->
                mydata_loading.hide()
                initRecyclerView(estimateList.toEstimatesListItem())
            })
        }
    }

    private fun initRecyclerView(estimatesListItems: List<EstimatesItem>) {
        val groupAdapter = GroupAdapter<GroupieViewHolder>().apply {
            addAll(estimatesListItems)
        }
        view_estimation_items_listView.apply {
            layoutManager = LinearLayoutManager(this.context)
            adapter = groupAdapter
        }
    }

    private fun List<JobItemEstimateDTO>.toEstimatesListItem(): List<EstimatesItem> {
        return this.map { approvedJobItems ->

            EstimatesItem(approvedJobItems, approveViewModel, activity, viewLifecycleOwner)
        }
    }

    override fun onDestroyView() {
        view_estimation_items_listView.adapter = null
        super.onDestroyView()
    }
}
