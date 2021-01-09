@file:Suppress("KDocUnresolvedReference")

package za.co.xisystems.itis_rrm.ui.mainview.approvemeasure.measure_approval

import android.app.Dialog
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
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.skydoves.androidveil.VeiledItemOnClickListener
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import timber.log.Timber
import za.co.xisystems.itis_rrm.MainActivity
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.R.string
import za.co.xisystems.itis_rrm.base.BaseFragment
import za.co.xisystems.itis_rrm.constants.Constants
import za.co.xisystems.itis_rrm.custom.results.XIError
import za.co.xisystems.itis_rrm.custom.results.XIProgress
import za.co.xisystems.itis_rrm.custom.results.XIResult
import za.co.xisystems.itis_rrm.custom.results.XIStatus
import za.co.xisystems.itis_rrm.custom.results.XISuccess
import za.co.xisystems.itis_rrm.custom.views.IndefiniteSnackbar
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemMeasureDTO
import za.co.xisystems.itis_rrm.databinding.FragmentMeasureApprovalBinding
import za.co.xisystems.itis_rrm.ui.extensions.doneProgress
import za.co.xisystems.itis_rrm.ui.extensions.failProgress
import za.co.xisystems.itis_rrm.ui.extensions.initProgress
import za.co.xisystems.itis_rrm.ui.extensions.startProgress
import za.co.xisystems.itis_rrm.ui.mainview.approvemeasure.ApproveMeasureViewModel
import za.co.xisystems.itis_rrm.ui.mainview.approvemeasure.ApproveMeasureViewModelFactory
import za.co.xisystems.itis_rrm.ui.mainview.approvemeasure.approveMeasure_Item.ApproveMeasureItem
import za.co.xisystems.itis_rrm.ui.scopes.UiLifecycleScope
import za.co.xisystems.itis_rrm.utils.ActivityIdConstants
import za.co.xisystems.itis_rrm.utils.Coroutines
import za.co.xisystems.itis_rrm.utils.ServiceUtil
import za.co.xisystems.itis_rrm.utils.enums.ToastDuration.SHORT
import za.co.xisystems.itis_rrm.utils.enums.ToastGravity.BOTTOM
import za.co.xisystems.itis_rrm.utils.enums.ToastGravity.CENTER
import za.co.xisystems.itis_rrm.utils.enums.ToastStyle.ERROR
import za.co.xisystems.itis_rrm.utils.enums.ToastStyle.INFO
import za.co.xisystems.itis_rrm.utils.enums.ToastStyle.NO_INTERNET
import za.co.xisystems.itis_rrm.utils.enums.ToastStyle.SUCCESS
import za.co.xisystems.itis_rrm.utils.enums.WorkflowDirection
import za.co.xisystems.itis_rrm.utils.enums.WorkflowDirection.NEXT

class MeasureApprovalFragment : BaseFragment(), KodeinAware {
    override val kodein by kodein()
    private lateinit var approveViewModel: ApproveMeasureViewModel
    private val factory: ApproveMeasureViewModelFactory by instance()
    private lateinit var measurementsToApprove: ArrayList<JobItemMeasureDTO>
    lateinit var dialog: Dialog
    private lateinit var progressButton: Button
    private var workObserver = Observer<XIResult<String>?> { handleMeasureProcessing(it) }
    private var flowDirection: Int = 0
    private var measuresProcessed: Int = 0

    private var uiScope = UiLifecycleScope()
    private var _ui: FragmentMeasureApprovalBinding? = null
    private val ui get() = _ui!!
    private var groupAdapter = GroupAdapter<GroupieViewHolder>()

    private fun handleMeasureProcessing(outcome: XIResult<String>?) {
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
                    crashGuard(
                        view = this.requireView(),
                        throwable = result,
                        refreshAction = { this.retryMeasurements() }
                    )
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
                        true -> progressButton.startProgress("Submitting ...")
                        else -> progressButton.doneProgress(progressButton.text.toString())
                    }
                }
                else -> Timber.d("$result")
            }
        }
    }

    private fun retryMeasurements() {
        IndefiniteSnackbar.hide()
        Coroutines.main {
            approveViewModel.workflowState.observe(viewLifecycleOwner, workObserver)
            processMeasurementWorkflow(NEXT)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (activity as MainActivity).supportActionBar?.title =
            getString(string.measure_approval_title)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // no options menu
        return false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _ui = FragmentMeasureApprovalBinding.inflate(inflater, container, false)
        return ui.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        approveViewModel = activity?.run {
            ViewModelProvider(this, factory).get(ApproveMeasureViewModel::class.java)
        } ?: throw Exception("Invalid Activity")

        Coroutines.main {

            ui.viewMeasuredItems.run {
                setVeilLayout(R.layout.measurements_item, object : VeiledItemOnClickListener {
                    /** will be invoked when the item on the [VeilRecyclerFrameView] clicked. */
                    override fun onItemClicked(pos: Int) {
                        Toast.makeText(
                            this@MeasureApprovalFragment.requireContext(),
                            "Loading ...",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
                setAdapter(groupAdapter)
                setLayoutManager(LinearLayoutManager(this.context))
                addVeiledItems(10)
            }

            ui.viewMeasuredItems.veil()
            approveViewModel.measureApprovalItem.observe(viewLifecycleOwner, { job ->
                getMeasureItems(job)
            })

            ui.approveMeasureButton.setOnClickListener {
                val approveBuilder = AlertDialog.Builder(
                    requireActivity()
                )
                approveBuilder.setTitle(string.confirm)
                approveBuilder.setIcon(R.drawable.ic_approve)
                approveBuilder.setMessage(string.are_you_sure_you_want_to_approve2)
                // Yes button
                approveBuilder.setPositiveButton(
                    string.yes
                ) { _, _ ->
                    progressButton = ui.approveMeasureButton
                    progressButton.initProgress(viewLifecycleOwner)
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
                workflowMeasurements(workflowDirection)
            }
        } else {
            sharpToast(
                message = getString(string.no_connection_detected),
                style = NO_INTERNET
            )
            progressButton.failProgress("No internet")
        }
    }

    private suspend fun workflowMeasurements(workflowDirection: WorkflowDirection) {
        approveViewModel.workflowState.observe(viewLifecycleOwner, workObserver)

        val approvalJob = uiScope.launch(uiScope.coroutineContext) {
            progressButton.startProgress("Submitting ...")

            val user = approveViewModel.user.await()
            user.observe(viewLifecycleOwner, { userDTO ->

                measuresProcessed = 0
                flowDirection = workflowDirection.value

                if (userDTO.userId.isBlank()) {
                    showSubmissionError("User lacks permissions", "Invalid User")
                } else {
                    Coroutines.main {
                        withContext(uiScope.coroutineContext) {
                            approveViewModel.approveMeasurements(
                                userDTO.userId,
                                workflowDirection,
                                measurementsToApprove
                            )
                        }
                    }
                }
            })
        }
        approvalJob.join()
    }

    private fun showSubmissionError(errMessage: String, progFailCaption: String) {
        sharpToast(
            message = errMessage,
            style = ERROR,
            position = CENTER
        )
        progressButton.failProgress(progFailCaption)
    }

    private fun popViewOnJobSubmit(direction: Int) {
        if (direction == NEXT.value) {
            sharpToast(resId = string.measurement_approved, style = SUCCESS, duration = SHORT)
        } else if (direction == WorkflowDirection.FAIL.value) {
            sharpToast(resId = string.measurement_declined, style = INFO, duration = SHORT)
        }
        Handler(Looper.getMainLooper()).postDelayed({
            Intent(context?.applicationContext, MainActivity::class.java).also { home ->
                startActivity(home)
            }
        }, Constants.TWO_SECONDS)
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
            delayedUnveil()
        }
    }

    private fun delayedUnveil() {
        Handler(Looper.getMainLooper()).postDelayed(
            {
                if (!activity?.isFinishing!!) {
                    ui.viewMeasuredItems.unVeil()
                }
            },
            Constants.ONE_SECOND
        )
    }

    private fun initRecyclerView(measureListItems: List<MeasurementsItem>) {
        groupAdapter.apply {
            clear()
            update(measureListItems)
            notifyDataSetChanged()
        }
        ui.viewMeasuredItems.run {
            setAdapter(groupAdapter, layoutManager = LinearLayoutManager(context))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // clear out all the leakers
        uiScope.destroy()
        approveViewModel.workflowState.removeObservers(viewLifecycleOwner)
        ui.viewMeasuredItems.setAdapter(null)
        _ui = null
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
