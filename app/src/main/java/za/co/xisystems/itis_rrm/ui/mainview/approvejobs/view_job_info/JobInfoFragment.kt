package za.co.xisystems.itis_rrm.ui.mainview.approvejobs.view_job_info

import android.app.AlertDialog.Builder
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.doOnNextLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.distinctUntilChanged
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.viewbinding.GroupieViewHolder
import org.kodein.di.DIAware
import org.kodein.di.android.x.closestDI
import org.kodein.di.instance
import timber.log.Timber
import za.co.xisystems.itis_rrm.MainActivity
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.base.BaseFragment
import za.co.xisystems.itis_rrm.constants.Constants
import za.co.xisystems.itis_rrm.custom.errors.XIErrorHandler
import za.co.xisystems.itis_rrm.custom.notifications.ToastDuration.LONG
import za.co.xisystems.itis_rrm.custom.notifications.ToastGravity.BOTTOM
import za.co.xisystems.itis_rrm.custom.notifications.ToastGravity.CENTER
import za.co.xisystems.itis_rrm.custom.notifications.ToastStyle
import za.co.xisystems.itis_rrm.custom.notifications.ToastStyle.NO_INTERNET
import za.co.xisystems.itis_rrm.custom.notifications.ToastStyle.SUCCESS
import za.co.xisystems.itis_rrm.custom.results.XIResult
import za.co.xisystems.itis_rrm.custom.views.IndefiniteSnackbar
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimateDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.UserDTO
import za.co.xisystems.itis_rrm.databinding.EstimatesItemBinding
import za.co.xisystems.itis_rrm.databinding.FragmentJobInfoBinding
import za.co.xisystems.itis_rrm.extensions.isConnected
import za.co.xisystems.itis_rrm.ui.extensions.ShimmerUtils
import za.co.xisystems.itis_rrm.ui.extensions.crashGuard
import za.co.xisystems.itis_rrm.ui.extensions.doneProgress
import za.co.xisystems.itis_rrm.ui.extensions.extensionToast
import za.co.xisystems.itis_rrm.ui.extensions.failProgress
import za.co.xisystems.itis_rrm.ui.extensions.initProgress
import za.co.xisystems.itis_rrm.ui.extensions.startProgress
import za.co.xisystems.itis_rrm.ui.mainview.approvejobs.ApproveJobsViewModel
import za.co.xisystems.itis_rrm.ui.mainview.approvejobs.ApproveJobsViewModelFactory
import za.co.xisystems.itis_rrm.ui.mainview.approvejobs.approve_job_item.ApproveJobItem
import za.co.xisystems.itis_rrm.utils.Coroutines
import za.co.xisystems.itis_rrm.utils.DataConversion
import za.co.xisystems.itis_rrm.utils.DateUtil
import za.co.xisystems.itis_rrm.utils.ServiceUtil
import za.co.xisystems.itis_rrm.utils.enums.WorkflowDirection
import za.co.xisystems.itis_rrm.utils.enums.WorkflowDirection.FAIL
import za.co.xisystems.itis_rrm.utils.enums.WorkflowDirection.NEXT
import java.util.Date

class JobInfoFragment : BaseFragment(), DIAware {
    override val di by closestDI()
    private lateinit var approveViewModel: ApproveJobsViewModel
    private val factory: ApproveJobsViewModelFactory by instance()
    private var workObserver = Observer<XIResult<String>?> { handleWorkSubmission(it) }
    private var updateObserver = Observer<XIResult<String>?> { handleUpdateProcess(it) }
    private lateinit var progressButton: Button
    private lateinit var flowDirection: WorkflowDirection
    private var _ui: FragmentJobInfoBinding? = null
    private val ui get() = _ui!!
    private var groupAdapter = GroupAdapter<GroupieViewHolder<EstimatesItemBinding>>()

    private fun handleUpdateProcess(outcome: XIResult<String>?) {
        outcome?.let { result ->
            when (result) {
                is XIResult.Success -> {
                    extensionToast(
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
                is XIResult.Success<String> -> {
                    val jiNo = result.data
                    progressButton.doneProgress()
                    toggleLongRunning(false)
                    popViewOnJobSubmit(flowDirection.value, jiNo)
                }
                else -> {
                    handleOthers(result, retryAction = { this@JobInfoFragment.retryWork() })
                }
            }
        }
    }

    private fun handleOthers(result: XIResult<String>, retryAction: () -> Unit = {}) {
        when (result) {
            is XIResult.Error -> {
                toggleLongRunning(false)
                progressButton.failProgress("Failed")
                crashGuard(
                    throwable = result,
                    refreshAction = { retryAction() }
                )
            }
            is XIResult.Status -> {
                extensionToast(message = result.message)
            }
            is XIResult.Progress -> {
                showWorkflowProgress(result)
            }
            else -> {
                Timber.d("$result")
            }
        }
    }

    private fun showWorkflowProgress(result: XIResult.Progress) {
        toggleLongRunning(result.isLoading)
        when (result.isLoading) {
            true -> {
                activity?.hideKeyboard()
                progressButton.initProgress(viewLifecycleOwner)
                progressButton.startProgress()
            }
            else -> progressButton.doneProgress()
        }
        Timber.d("Loading ${result.isLoading}")
    }

    private fun retryWork() {
        IndefiniteSnackbar.hide()
        moveJobToNextWorkflow(flowDirection)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (activity as MainActivity).supportActionBar?.title = getString(R.string.jobinfo_item_title)
        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val directions =
                    JobInfoFragmentDirections.actionJobInfoFragmentToNavApproveJbs()
                Navigation.findNavController(this@JobInfoFragment.requireView())
                    .navigate(directions)
            }
        }
        requireActivity().onBackPressedDispatcher
            .addCallback(this, callback)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // no options menu
        return false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity as MainActivity).supportActionBar?.title = getString(R.string.jobinfo_item_title)

        approveViewModel =
            ViewModelProvider(this.requireActivity(), factory)
                .get(ApproveJobsViewModel::class.java)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _ui = FragmentJobInfoBinding.inflate(inflater, container, false)
        return ui.root
    }

    override fun onStart() {
        super.onStart()
        Coroutines.main {

            initVeiledRecyclerView()

            initApprovalHeader()
        }

        buildApprovalDialog()

        buildDeclineDialog()
    }

    private fun buildDeclineDialog() {
        ui.declineJobButton.setOnClickListener {
            val declineBuilder =
                Builder(
                    activity // , android.R.style.Theme_DeviceDefault_Dialog
                )
            declineBuilder.setTitle(R.string.confirm)
            declineBuilder.setIcon(R.drawable.ic_warning)
            declineBuilder.setMessage(R.string.are_you_sure_you_want_to_decline)
            // Yes button
            declineBuilder.setPositiveButton(
                R.string.yes
            ) { _, _ ->
                if (this.requireActivity().isConnected) {
                    progressButton = ui.declineJobButton
                    moveJobToNextWorkflow(FAIL)
                } else {
                    extensionToast(
                        message = getString(R.string.no_connection_detected),
                        style = NO_INTERNET
                    )
                }
            }
            // No button
            declineBuilder.setNegativeButton(
                R.string.no
            ) { dialog, _ ->
                // Do nothing but close dialog
                dialog.dismiss()
            }
            val declineAlert = declineBuilder.create()
            declineAlert.show()
        }
    }

    private fun buildApprovalDialog() {
        ui.approveJobButton.setOnClickListener {
            val approvalBuilder = Builder(
                activity // ,android.R.style.Theme_DeviceDefault_Dialog
            )
            approvalBuilder.setTitle(R.string.confirm)
            approvalBuilder.setIcon(R.drawable.ic_approve)
            approvalBuilder.setMessage(R.string.are_you_sure_you_want_to_approve)

            // Yes button
            approvalBuilder.setPositiveButton(
                R.string.yes
            ) { _, _ ->
                if (ServiceUtil.isNetworkAvailable(requireContext().applicationContext)) {
                    progressButton = ui.approveJobButton
                    progressButton.initProgress(viewLifecycleOwner)
                    moveJobToNextWorkflow(NEXT)
                } else {
                    extensionToast(
                        message = getString(R.string.no_connection_detected),
                        style = NO_INTERNET
                    )
                }
            }

            // No button
            approvalBuilder.setNegativeButton(
                R.string.no
            ) { dialog, _ ->
                // Do nothing but close dialog
                dialog.dismiss()
            }
            val approvalDialog = approvalBuilder.create()
            approvalDialog.show()
        }
    }

    private fun initApprovalHeader() {
        approveViewModel.jobApprovalItem.distinctUntilChanged().observe(viewLifecycleOwner, { job ->
            Coroutines.main {
                getEstimateItems(job.jobDTO.jobId)
                val description = approveViewModel.getDescForProjectId(job.jobDTO.projectId!!)
                val sectionId = approveViewModel.getProjectSectionIdForJobId(job.jobDTO.jobId)
                val route = approveViewModel.getRouteForProjectSectionId(sectionId)
                val section = approveViewModel.getSectionForProjectSectionId(sectionId)

                ui.projectDescriptionTextView.text = description
                ui.sectionDescriptionTextView.text = ("$route/ $section")
                ui.startKmDescriptionTextView.text = (job.jobDTO.startKm.toString())
                ui.endKmDescriptionTextView.text = (job.jobDTO.endKm.toString())
            }
        })
    }

    private fun initVeiledRecyclerView() {
        ui.viewEstimationItemsListView.run {
            setVeilLayout(R.layout.estimates_item) {
                Toast.makeText(
                    this@JobInfoFragment.requireContext(),
                    "Loading ...",
                    Toast.LENGTH_SHORT
                ).show()
            }
            setAdapter(groupAdapter)
            setLayoutManager(LinearLayoutManager(this.context))
            addVeiledItems(3)
            shimmer = ShimmerUtils.getGrayShimmer(this@JobInfoFragment.requireContext())
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
                    progressButton.text = getString(R.string.approve_job_in_progress)
                    ui.declineJobButton.visibility = View.GONE
                }
                else -> {
                    progressButton.text = getString(R.string.decline_job_in_progress)
                    ui.approveJobButton.visibility = View.GONE
                }
            }
            progressButton.initProgress(viewLifecycleOwner)
            progressButton.startProgress()
            val user = approveViewModel.user.await()
            user.observe(viewLifecycleOwner, { userDTO ->
                approveViewModel.jobApprovalItem.distinctUntilChanged().observe(viewLifecycleOwner, { approveJobItem ->

                    when {
                        approveJobItem == null -> {
                            Timber.d("ApproveItem was null")
                        }
                        userDTO.userId.isBlank() -> {
                            extensionToast(
                                message = "The user lacks permissions.",
                                style = ToastStyle.ERROR,
                                position = CENTER
                            )
                            progressButton.failProgress("Invalid User")
                        }
                        approveJobItem.jobDTO.jobId.isBlank() -> {
                            extensionToast(
                                message = "The selected job is invalid.",
                                style = ToastStyle.ERROR,
                                position = CENTER
                            )
                            progressButton.failProgress("Invalid Job")
                        }
                        workflowDirection == FAIL &&
                            ui.workflowCommentsEditText.text.trim().isBlank() -> {
                            extensionToast(
                                message = "Please provide a comment / reason for declining this job",
                                style = ToastStyle.WARNING,
                                position = CENTER
                            )
                            resetButtons()
                            progressButton.failProgress(getString(R.string.decline_job))
                        }
                        else -> {
                            approveJobItem.jobDTO.remarks = ui.workflowCommentsEditText.text.trim().toString()
                            approveJobItem.jobDTO.approvalDate = DateUtil.dateToString(Date())
                            approveViewModel.backupJobInProgress(approveJobItem.jobDTO)
                            initJobWorkflow(approveJobItem, workflowDirection, userDTO)
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
        val jobId: String =
            DataConversion.toLittleEndian(job.jobDTO.jobId)!!
        val trackRouteId: String =
            DataConversion.toLittleEndian(job.jobDTO.trackRouteId)!!
        val direction: Int = workflowDirection.value

        var description: String? = ""
        if (ui.workflowCommentsEditText.text != null) {
            description = ui.workflowCommentsEditText.text.toString()
        }
        Coroutines.main {
            try {
                approveViewModel.workflowState.observe(viewLifecycleOwner, workObserver)
                val task = approveViewModel.processWorkflowMove(
                    userDTO.userId, trackRouteId, description, direction, jobId
                )
                task.join()
            } catch (t: Throwable) {
                val message = t.message ?: XIErrorHandler.UNKNOWN_ERROR
                val xiErr = XIResult.Error(t, "Failed to complete workflow: $message")
                handleWorkSubmission(xiErr)
            }
        }
    }

    private fun popViewOnJobSubmit(direction: Int, jiNo: String?) {
        when (direction) {
            NEXT.value -> {
                progressButton.text = getString(R.string.approve_job)
                extensionToast(
                    title = "Workflow Update",
                    message = getString(R.string.job_no_approved, jiNo!!),
                    style = SUCCESS
                )
            }
            FAIL.value -> {
                progressButton.text = getString(R.string.decline_job)
                extensionToast(
                    title = "Workflow Update",
                    message = getString(R.string.job_declined),
                    style = ToastStyle.DELETE
                )
            }
        }

        Handler(Looper.getMainLooper()).postDelayed(
            {
                Intent(context?.applicationContext, MainActivity::class.java).also { home ->
                    home.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(home)
                }
            },
            Constants.TWO_SECONDS
        )
    }

    private fun getEstimateItems(jobID: String?) {
        Coroutines.main {
            val estimates = approveViewModel.getJobEstimationItemsForJobId(jobID)
            estimates.observe(viewLifecycleOwner, { estimateList ->
                initRecyclerView(estimateList.toEstimatesListItem())
            })
        }
    }

    private fun initRecyclerView(estimatesListItems: List<EstimatesItem>) {
        groupAdapter = GroupAdapter<GroupieViewHolder<EstimatesItemBinding>>().apply {
            clear()
            addAll(estimatesListItems)
            notifyItemRangeChanged(0, estimatesListItems.size)
        }
        ui.viewEstimationItemsListView.run {
            setLayoutManager(LinearLayoutManager(this.context))
            setAdapter(groupAdapter)
            doOnNextLayout { unVeil() }
        }
    }

    private fun List<JobItemEstimateDTO>.toEstimatesListItem(): List<EstimatesItem> {
        return this.map { approvedJobItems ->
            EstimatesItem(approvedJobItems, approveViewModel, activity, viewLifecycleOwner, updateObserver)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        ui.viewEstimationItemsListView.setAdapter(null)
        approveViewModel.workflowState.removeObservers(viewLifecycleOwner)
        approveViewModel.updateState.removeObservers(viewLifecycleOwner)
        _ui = null
    }
}
