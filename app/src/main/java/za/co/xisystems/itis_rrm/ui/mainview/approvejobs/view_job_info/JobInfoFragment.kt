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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import timber.log.Timber
import za.co.xisystems.itis_rrm.MainActivity
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.R.string
import za.co.xisystems.itis_rrm.base.BaseFragment
import za.co.xisystems.itis_rrm.custom.errors.XIErrorHandler
import za.co.xisystems.itis_rrm.custom.results.XIError
import za.co.xisystems.itis_rrm.custom.results.XIProgress
import za.co.xisystems.itis_rrm.custom.results.XIResult
import za.co.xisystems.itis_rrm.custom.results.XIStatus
import za.co.xisystems.itis_rrm.custom.results.XISuccess
import za.co.xisystems.itis_rrm.custom.views.IndefiniteSnackbar
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimateDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.UserDTO
import za.co.xisystems.itis_rrm.databinding.FragmentJobInfoBinding
import za.co.xisystems.itis_rrm.ui.extensions.doneProgress
import za.co.xisystems.itis_rrm.ui.extensions.failProgress
import za.co.xisystems.itis_rrm.ui.extensions.initProgress
import za.co.xisystems.itis_rrm.ui.extensions.startProgress
import za.co.xisystems.itis_rrm.ui.mainview.approvejobs.ApproveJobsViewModel
import za.co.xisystems.itis_rrm.ui.mainview.approvejobs.ApproveJobsViewModelFactory
import za.co.xisystems.itis_rrm.ui.mainview.approvejobs.approve_job_item.ApproveJobItem
import za.co.xisystems.itis_rrm.utils.Coroutines
import za.co.xisystems.itis_rrm.utils.DataConversion
import za.co.xisystems.itis_rrm.utils.ServiceUtil
import za.co.xisystems.itis_rrm.utils.enums.ToastDuration.LONG
import za.co.xisystems.itis_rrm.utils.enums.ToastGravity.BOTTOM
import za.co.xisystems.itis_rrm.utils.enums.ToastStyle
import za.co.xisystems.itis_rrm.utils.enums.ToastStyle.NO_INTERNET
import za.co.xisystems.itis_rrm.utils.enums.ToastStyle.SUCCESS
import za.co.xisystems.itis_rrm.utils.enums.WorkflowDirection
import za.co.xisystems.itis_rrm.utils.hide
import za.co.xisystems.itis_rrm.utils.show

class JobInfoFragment : BaseFragment(R.layout.fragment_job_info), KodeinAware {
    override val kodein by kodein()
    private lateinit var approveViewModel: ApproveJobsViewModel
    private val factory: ApproveJobsViewModelFactory by instance()
    private var workObserver = Observer<XIResult<String>?> { handleWorkSubmission(it) }
    private var updateObserver = Observer<XIResult<String>?> { handleUpdateProcess(it) }
    private lateinit var progressButton: Button
    private lateinit var flowDirection: WorkflowDirection
    private var _ui: FragmentJobInfoBinding? = null
    private val ui get() = _ui!!

    private fun handleUpdateProcess(outcome: XIResult<String>?) {
        outcome?.let { result ->
            when (result) {
                is XISuccess -> {
                    sharpToast(
                        message = "Estimate updated",
                        style = SUCCESS,
                        position = BOTTOM,
                        duration = LONG
                    )
                }
                else -> handleOthers(result)
            }
        }
    }

    private fun handleWorkSubmission(outcome: XIResult<String>?) {
        outcome?.let { result ->
            when (result) {
                is XISuccess<String> -> {
                    val jiNo = result.data
                    progressButton.doneProgress()
                    popViewOnJobSubmit(flowDirection.value, jiNo)
                }
                else -> {
                    handleOthers(result, retryAction = { retryWork() })
                }
            }
        }
    }

    private fun handleOthers(result: XIResult<String>, retryAction: () -> Unit = {}) {
        when (result) {
            is XIError -> {
                progressButton.failProgress("Failed")
                crashGuard(
                    view = this.requireView(),
                    throwable = result,
                    refreshAction = { retryAction }
                )
            }
            is XIStatus -> {
                sharpToast(result.message)
            }
            is XIProgress -> {
                when (result.isLoading) {
                    true -> progressButton.startProgress()
                    else -> progressButton.doneProgress()
                }
                Timber.d("Loading ${result.isLoading}")
            }
            else -> {
                Timber.d("$result")
            }
        }
    }

    private fun retryWork() {
        IndefiniteSnackbar.hide()
        moveJobToNextWorkflow(flowDirection)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (activity as MainActivity).supportActionBar?.title = getString(string.jobinfo_item_title)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity as MainActivity).supportActionBar?.title = getString(string.jobinfo_item_title)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
       _ui = FragmentJobInfoBinding.inflate(inflater, container, false)
        return ui.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        approveViewModel = activity?.run {
            ViewModelProvider(this, factory).get(ApproveJobsViewModel::class.java)
        } ?: throw Exception("Invalid Activity")

        Coroutines.main {
            ui.mydataLoading.show()

            approveViewModel.jobApprovalItem.observe(viewLifecycleOwner, { job ->
                Coroutines.main {
                    getEstimateItems(job.jobDTO.JobId)
                    val description = approveViewModel.getDescForProjectId(job.jobDTO.ProjectId!!)
                    val sectionId = approveViewModel.getProjectSectionIdForJobId(job.jobDTO.JobId)
                    val route = approveViewModel.getRouteForProjectSectionId(sectionId)
                    val section = approveViewModel.getSectionForProjectSectionId(sectionId)

                    ui.projectDescriptionTextView.text = description
                    ui.sectionDescriptionTextView.text = ("$route/ $section")
                    ui.startKmDescriptionTextView.text = (job.jobDTO.StartKm.toString())
                    ui.endKmDescriptionTextView.text = (job.jobDTO.EndKm.toString())
                }
            })
        }

        ui.approveJobButton.setOnClickListener {
            val approvalBuilder = AlertDialog.Builder(
                activity // ,android.R.style.Theme_DeviceDefault_Dialog
            )
            approvalBuilder.setTitle(string.confirm)
            approvalBuilder.setIcon(R.drawable.ic_approve)
            approvalBuilder.setMessage(string.are_you_sure_you_want_to_approve)

            // Yes button
            approvalBuilder.setPositiveButton(
                string.yes
            ) { _, _ ->
                if (ServiceUtil.isNetworkAvailable(requireContext().applicationContext)) {
                    progressButton = ui.approveJobButton
                    progressButton.initProgress(viewLifecycleOwner)
                    moveJobToNextWorkflow(WorkflowDirection.NEXT)
                } else {
                    sharpToast(
                        message = getString(string.no_connection_detected),
                        style = NO_INTERNET
                    )
                }
            }

            // No button
            approvalBuilder.setNegativeButton(
                string.no
            ) { dialog, _ ->
                // Do nothing but close dialog
                dialog.dismiss()
            }
            val approvalDialog = approvalBuilder.create()
            approvalDialog.show()
        }

        ui.declineJobButton.setOnClickListener {
            val declineBuilder =
                AlertDialog.Builder(
                    activity // , android.R.style.Theme_DeviceDefault_Dialog
                )
            declineBuilder.setTitle(string.confirm)
            declineBuilder.setIcon(R.drawable.ic_warning)
            declineBuilder.setMessage(string.are_you_sure_you_want_to_decline)
            // Yes button
            declineBuilder.setPositiveButton(
                string.yes
            ) { _, _ ->
                if (ServiceUtil.isNetworkAvailable(requireContext().applicationContext)) {
                    progressButton = ui.declineJobButton
                    moveJobToNextWorkflow(WorkflowDirection.FAIL)
                } else {
                    sharpToast(
                        message = getString(string.no_connection_detected),
                        style = NO_INTERNET
                    )
                }
            }
            // No button
            declineBuilder.setNegativeButton(
                string.no
            ) { dialog, _ ->
                // Do nothing but close dialog
                dialog.dismiss()
            }
            val declineAlert = declineBuilder.create()
            declineAlert.show()
        }
    }

    private fun resetButtons() {
        ui.approveJobButton.visibility = View.VISIBLE
        ui.declineJobButton.visibility = View.VISIBLE
    }

    private fun moveJobToNextWorkflow(workflowDirection: WorkflowDirection) {
        Coroutines.main {
            flowDirection = workflowDirection

            when (progressButton == ui.approveJobButton) {
                true -> {
                    progressButton.text = getString(string.approve_job_in_progress)
                    ui.declineJobButton.visibility = View.GONE
                }
                else -> {
                    progressButton.text = getString(string.decline_job_in_progress)
                    ui.approveJobButton.visibility = View.GONE
                }
            }
            progressButton.initProgress(viewLifecycleOwner)
            progressButton.startProgress()
            val user = approveViewModel.user.await()
            user.observe(viewLifecycleOwner, { userDTO ->
                approveViewModel.jobApprovalItem.observe(viewLifecycleOwner, { job ->

                    when {
                        job == null -> {
                            Timber.d("ApproveItem was null")
                        }
                        userDTO.userId.isBlank() -> {
                            sharpToast(message = "The user lacks permissions.", style = ToastStyle.ERROR)
                            progressButton.failProgress("Invalid User")
                        }
                        job.jobDTO.JobId.isBlank() -> {
                            sharpToast(message = "The selected job is invalid.", style = ToastStyle.ERROR)
                            progressButton.failProgress("Invalid Job")
                        }
                        workflowDirection == WorkflowDirection.FAIL &&
                            ui.workflowCommentsEditText.text.trim().isBlank() -> {
                            sharpToast(
                                message = "Please provide a comment / reason for declining this job",
                                style = ToastStyle.WARNING
                            )
                            resetButtons()
                            progressButton.failProgress(getString(string.decline_job))
                        }
                        else -> {
                            initJobWorkflow(job, workflowDirection, userDTO)
                        }
                    }
                })
            })
        }
    }

    private fun initJobWorkflow(
        job: ApproveJobItem,
        workflowDirection: WorkflowDirection,
        userDTO: UserDTO
    ) {
        val trackRouteId: String =
            DataConversion.toLittleEndian(job.jobDTO.TrackRouteId)!!
        val direction: Int = workflowDirection.value

        var description: String? = ""
        if (ui.workflowCommentsEditText.text != null) {
            description = ui.workflowCommentsEditText.text.toString()
        }
        Coroutines.main {
            try {
                approveViewModel.workflowState.observe(viewLifecycleOwner, workObserver)
                val task = approveViewModel.processWorkflowMove(
                    userDTO.userId, trackRouteId, description, direction, job.jobDTO.JobId
                )
                task.join()
            } catch (t: Throwable) {
                val message = t.message ?: XIErrorHandler.UNKNOWN_ERROR
                val xiErr = XIError(t, "Failed to complete workflow: $message")
                handleWorkSubmission(xiErr)
            }
        }
    }

    private fun popViewOnJobSubmit(direction: Int, jiNo: String?) {
        if (direction == WorkflowDirection.NEXT.value) {
            progressButton.text = getString(string.approve_job)
            sharpToast(
                message = getString(string.job_no_approved, jiNo!!),
                style = SUCCESS
            )
        } else if (direction == WorkflowDirection.FAIL.value) {
            progressButton.text = getString(string.decline_job)
            sharpToast(
                message = getString(string.job_declined),
                style = ToastStyle.INFO
            )
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
                ui.mydataLoading.hide()
                initRecyclerView(estimateList.toEstimatesListItem())
            })
        }
    }

    private fun initRecyclerView(estimatesListItems: List<EstimatesItem>) {
        val groupAdapter = GroupAdapter<GroupieViewHolder>().apply {
            addAll(estimatesListItems)
        }
        ui.viewEstimationItemsListView.apply {
            layoutManager = LinearLayoutManager(this.context)
            adapter = groupAdapter
        }
    }

    private fun List<JobItemEstimateDTO>.toEstimatesListItem(): List<EstimatesItem> {
        return this.map { approvedJobItems ->
            EstimatesItem(approvedJobItems, approveViewModel, activity, viewLifecycleOwner, updateObserver)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        ui.viewEstimationItemsListView.adapter = null
        approveViewModel.workflowState.removeObservers(viewLifecycleOwner)
        approveViewModel.updateState.removeObservers(viewLifecycleOwner)
        _ui = null
    }
}
