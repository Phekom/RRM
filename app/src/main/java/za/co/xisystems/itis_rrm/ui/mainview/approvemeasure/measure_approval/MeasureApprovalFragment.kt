@file:Suppress("KDocUnresolvedReference")

package za.co.xisystems.itis_rrm.ui.mainview.approvemeasure.measure_approval

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import kotlinx.android.synthetic.main.fragment_measure_approval.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import timber.log.Timber
import www.sanju.motiontoast.MotionToast
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
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemMeasureDTO
import za.co.xisystems.itis_rrm.ui.extensions.doneProgress
import za.co.xisystems.itis_rrm.ui.extensions.failProgress
import za.co.xisystems.itis_rrm.ui.extensions.initProgress
import za.co.xisystems.itis_rrm.ui.extensions.motionToast
import za.co.xisystems.itis_rrm.ui.extensions.startProgress
import za.co.xisystems.itis_rrm.ui.mainview.approvemeasure.ApproveMeasureViewModel
import za.co.xisystems.itis_rrm.ui.mainview.approvemeasure.ApproveMeasureViewModelFactory
import za.co.xisystems.itis_rrm.ui.mainview.approvemeasure.approveMeasure_Item.ApproveMeasureItem
import za.co.xisystems.itis_rrm.ui.scopes.UiLifecycleScope
import za.co.xisystems.itis_rrm.utils.ActivityIdConstants
import za.co.xisystems.itis_rrm.utils.Coroutines
import za.co.xisystems.itis_rrm.utils.ServiceUtil
import za.co.xisystems.itis_rrm.utils.enums.WorkflowDirection
import za.co.xisystems.itis_rrm.utils.enums.WorkflowDirection.NEXT

@Suppress("KDocUnresolvedReference")
class MeasureApprovalFragment : BaseFragment(R.layout.fragment_measure_approval), KodeinAware {
    override val kodein by kodein()
    private lateinit var approveViewModel: ApproveMeasureViewModel
    private val factory: ApproveMeasureViewModelFactory by instance()
    private lateinit var measurementsToApprove: ArrayList<JobItemMeasureDTO>
    lateinit var dialog: Dialog
    private lateinit var progressButton: Button
    private var workObserver = Observer<XIResult<String>?> { handleWorkSubmission(it) }
    private var flowDirection: Int = 0
    private var measuresProcessed: Int = 0
    private var uiScope = UiLifecycleScope()
    private var measureJob: Job = Job()

    private fun handleWorkSubmission(outcome: XIResult<String>?) {
        outcome?.let { result ->
            when (result) {
                is XISuccess -> {
                    if (result.data == "WORK_COMPLETE") {
                        measurementsToApprove.clear()
                        initRecyclerView(measurementsToApprove.toMeasureItems())
                        progressButton.doneProgress(progressButton.text.toString())
                        popViewOnJobSubmit(flowDirection)
                    }
                }
                is XIError -> {
                    progressButton.failProgress("Failed")
                    XIErrorHandler.crashGuard(
                        fragment = this,
                        view = this.requireView(),
                        throwable = result,
                        refreshAction = { retryMeasurements() }
                    )
                }
                is XIStatus -> {
                    this.motionToast(result.message, MotionToast.TOAST_INFO)
                }
                is XIProgress -> {
                    when (result.isLoading) {
                        true -> progressButton.startProgress("Submitting ...")
                        else -> progressButton.doneProgress(progressButton.text.toString())
                    }
                }
            }
        }
    }

    private fun retryMeasurements() {
        IndefiniteSnackbar.hide()
        Coroutines.main {
            moveJobToNextWorkflow(NEXT)
            approveViewModel.workflowState.observe(viewLifecycleOwner, workObserver)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (activity as MainActivity).supportActionBar?.title =
            getString(string.measure_approval_title)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
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

            approveViewModel.measureApprovalItem.observe(viewLifecycleOwner, { job ->
                getMeasureItems(job)
            })

            approve_measure_button.setOnClickListener {
                val approveBuilder = AlertDialog.Builder(
                    requireActivity()
                )
                approveBuilder.setTitle(string.confirm)
                approveBuilder.setIcon(R.drawable.ic_approve)
                approveBuilder.setMessage(string.are_you_sure_you_want_to_approve2)
                progressButton = approve_measure_button
                progressButton.initProgress(viewLifecycleOwner)
                // Yes button
                approveBuilder.setPositiveButton(
                    string.yes
                ) { _, _ ->
                    processMeasurementWorkflow(NEXT)
                }

                // No button
                approveBuilder.setNegativeButton(
                    string.no
                ) { dialog, _ ->
                    // Do nothing but close dialog
                    dialog.dismiss()
                }
                val approveAlert = approveBuilder.create()
                approveAlert.show()
            }
        }
    }

    private fun processMeasurementWorkflow(workflowDirection: WorkflowDirection) {
        if (ServiceUtil.isNetworkAvailable(this.requireContext().applicationContext)) {
            Coroutines.main {
                approveViewModel.workflowState.observe(viewLifecycleOwner, workObserver)
                moveJobToNextWorkflow(workflowDirection)

            }
        } else {
            this.requireActivity().motionToast(
                getString(string.no_connection_detected),
                MotionToast.TOAST_ERROR
            )
            progressButton.failProgress("No internet")
        }
    }

    private suspend fun moveJobToNextWorkflow(workflowDirection: WorkflowDirection) {
        val approvalJob = uiScope.launch(uiScope.coroutineContext) {
            progressButton.startProgress("Submitting ...")

            val user = approveViewModel.user.await()
            user.observe(viewLifecycleOwner, { userDTO ->

                measuresProcessed = 0
                flowDirection = workflowDirection.value

                if (userDTO.userId.isBlank()) {
                    this@MeasureApprovalFragment.motionToast(
                        "User lacks permissions",
                        MotionToast.TOAST_ERROR,
                        MotionToast.GRAVITY_BOTTOM
                    )
                    progressButton.failProgress("Invalid User")
                } else {
                    Coroutines.main {
                        withContext(uiScope.coroutineContext) {
                            try {
                                approveViewModel.approveMeasurements(
                                    userDTO.userId,
                                    workflowDirection,
                                    measurementsToApprove
                                )
                            } catch (t: Throwable) {
                                val message = "Measurement Approval Exception: ${t.message ?: XIErrorHandler.UNKNOWN_ERROR}"
                                Timber.e(t, message)
                                val measureErr = XIError(t, message)
                                handleWorkSubmission(measureErr)
                            }
                        }
                    }
                }
            })
        }
        approvalJob.join()

    }

    private fun popViewOnJobSubmit(direction: Int) {
        if (direction == NEXT.value) {
            this.motionToast(string.measurement_approved, MotionToast.TOAST_SUCCESS)
        } else if (direction == WorkflowDirection.FAIL.value) {
            this.motionToast(string.measurement_declined, MotionToast.TOAST_INFO)
        }

        Intent(context?.applicationContext, MainActivity::class.java).also { home ->
            startActivity(home)
        }
    }

    private fun getMeasureItems(job: ApproveMeasureItem) {
        Coroutines.main {
            val measurements = approveViewModel.getJobMeasureItemsForJobId(
                job.jobId,
                ActivityIdConstants.MEASURE_COMPLETE
            )
            measurements.observe(viewLifecycleOwner, { jobItemMeasures ->
                jobItemMeasures?.let {
                    measurementsToApprove = ArrayList()
                    measurementsToApprove.addAll(it)
                    initRecyclerView(it.toMeasureItems())
                }
            })
        }
    }

    private fun initRecyclerView(measureListItems: List<MeasurementsItem>) {
        val groupAdapter = GroupAdapter<GroupieViewHolder>().apply {
            update(measureListItems)
        }
        view_measured_items.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = groupAdapter
        }
    }

    override fun onDestroyView() {
        // approveViewModel.workflowState.removeObservers(viewLifecycleOwner)
        view_measured_items.adapter = null
        super.onDestroyView()
    }

    private fun List<JobItemMeasureDTO>.toMeasureItems(): List<MeasurementsItem> {
        return this.map {
            MeasurementsItem(
                it,
                approveViewModel,
                activity,
                viewLifecycleOwner
            )
        }
    }
}
