/**
 * Updated by Shaun McDonald on 2021/05/15
 * Last modified on 2021/05/15, 00:49
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 **/

/**
 * Updated by Shaun McDonald on 2021/05/14
 * Last modified on 2021/05/14, 19:43
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 **/

@file:Suppress("KDocUnresolvedReference")

package za.co.xisystems.itis_rrm.ui.mainview.approvemeasure.measure_approval

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.view.doOnNextLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.viewbinding.GroupieViewHolder
import java.lang.ref.WeakReference
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.instance
import timber.log.Timber
import za.co.xisystems.itis_rrm.ui.mainview.activities.main.MainActivity
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.base.BaseFragment
import za.co.xisystems.itis_rrm.constants.Constants
import za.co.xisystems.itis_rrm.custom.notifications.ToastDuration
import za.co.xisystems.itis_rrm.custom.notifications.ToastGravity
import za.co.xisystems.itis_rrm.custom.notifications.ToastStyle
import za.co.xisystems.itis_rrm.custom.results.XIResult
import za.co.xisystems.itis_rrm.custom.views.IndefiniteSnackbar
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemMeasureDTO
import za.co.xisystems.itis_rrm.databinding.FragmentMeasureApprovalBinding
import za.co.xisystems.itis_rrm.databinding.MeasurementsItemBinding
import za.co.xisystems.itis_rrm.ui.extensions.crashGuard
import za.co.xisystems.itis_rrm.ui.extensions.doneProgress
import za.co.xisystems.itis_rrm.ui.extensions.extensionToast
import za.co.xisystems.itis_rrm.ui.extensions.failProgress
import za.co.xisystems.itis_rrm.ui.extensions.initProgress
import za.co.xisystems.itis_rrm.ui.extensions.startProgress
import za.co.xisystems.itis_rrm.ui.mainview.approvemeasure.ApproveMeasureViewModel
import za.co.xisystems.itis_rrm.ui.mainview.approvemeasure.ApproveMeasureViewModelFactory
import za.co.xisystems.itis_rrm.utils.ActivityIdConstants
import za.co.xisystems.itis_rrm.utils.Coroutines
import za.co.xisystems.itis_rrm.utils.ServiceUtil
import za.co.xisystems.itis_rrm.utils.enums.WorkflowDirection
import za.co.xisystems.itis_rrm.utils.enums.WorkflowDirection.NEXT

class MeasureApprovalFragment : BaseFragment() {

    private lateinit var approveViewModel: ApproveMeasureViewModel
    private val factory: ApproveMeasureViewModelFactory by instance()
    private lateinit var measurementsToApprove: ArrayList<JobItemMeasureDTO>
    lateinit var dialog: Dialog
    private lateinit var progressButton: Button
    private var workObserver = Observer<XIResult<String>?> { handleMeasureProcessing(it) }
    private var flowDirection: Int = 0
    private var measuresProcessed: Int = 0
    private var selectedJobId: String? = null
    private var _ui: FragmentMeasureApprovalBinding? = null
    private val ui get() = _ui!!
    private var groupAdapter = GroupAdapter<GroupieViewHolder<MeasurementsItemBinding>>()

    private fun handleMeasureProcessing(outcome: XIResult<String>?) = uiScope.launch(dispatchers.ui()) {
        outcome?.let { result ->
            when (result) {
                is XIResult.Success -> {
                    progressButton.doneProgress(progressButton.text.toString())
                    this@MeasureApprovalFragment.toggleLongRunning(false)
                    popViewOnJobSubmit(flowDirection)
                }
                is XIResult.Error -> {
                    progressButton.failProgress("Failed")
                    this@MeasureApprovalFragment.toggleLongRunning(false)
                    crashGuard(
                        throwable = result,
                        refreshAction = { this@MeasureApprovalFragment.retryMeasurements() }
                    )
                }
                is XIResult.Status -> {
                    extensionToast(
                        message = result.message,
                        style = ToastStyle.INFO,
                        position = ToastGravity.BOTTOM,
                        duration = ToastDuration.SHORT
                    )
                }
                is XIResult.Progress -> {
                    showWorkProgress(result)
                }
                else -> Timber.d("$result")
            }
        }
    }

    private fun showWorkProgress(result: XIResult.Progress) {
        toggleLongRunning(result.isLoading)
        when (result.isLoading) {
            true -> {
                progressButton.startProgress("Submitting ...")
                this@MeasureApprovalFragment.toggleLongRunning(true)
            }
            else -> progressButton.doneProgress(progressButton.text.toString())
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
            getString(R.string.measure_approval_title)

        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            /**
             * Callback for handling the [OnBackPressedDispatcher.onBackPressed] event.
             */
            override fun handleOnBackPressed() {
                Navigation.findNavController(this@MeasureApprovalFragment.requireView())
                    .navigate(R.id.nav_approveMeasure)
            }
        }
        requireActivity().onBackPressedDispatcher
            .addCallback(this, callback)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        approveViewModel =
            ViewModelProvider(this.requireActivity(), factory)[ApproveMeasureViewModel::class.java]
    }



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _ui = FragmentMeasureApprovalBinding.inflate(inflater, container, false)
        return ui?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Coroutines.main {

            initVeiledRecycler()

            _ui?.viewMeasuredItems?.veil()
            approveViewModel.jobIdForApproval.observe(viewLifecycleOwner, { jobId ->
                jobId?.let {
                    selectedJobId = it
                    getMeasureItems(it)
                }
            })

            _ui?.approveMeasureButton?.setOnClickListener {
                val approveBuilder = AlertDialog.Builder(
                    requireActivity()
                )
                approveBuilder.setTitle(R.string.confirm)
                approveBuilder.setIcon(R.drawable.ic_approve)
                approveBuilder.setMessage(R.string.are_you_sure_you_want_to_approve2)
                // Yes button
                approveBuilder.setPositiveButton(
                    R.string.yes
                ) { _, _ ->
                    progressButton = _ui?.approveMeasureButton!!
                    progressButton.initProgress(viewLifecycleOwner)
                    processMeasurementWorkflow(NEXT)
                }

                // No button
                approveBuilder.setNegativeButton(
                    R.string.no
                ) { dialog, _ ->
                    // Do nothing but close dialog
                    dialog.dismiss()
                }
                val approveAlert = approveBuilder.create()
                approveAlert.show()
            }
        }
    }

    private fun initVeiledRecycler() {
        _ui?.viewMeasuredItems?.run {
            setVeilLayout(R.layout.measurements_item) { toast("Loading ...") }
            setAdapter(groupAdapter)
            setLayoutManager(LinearLayoutManager(this.context))
            addVeiledItems(10)
        }
    }

    private fun processMeasurementWorkflow(workflowDirection: WorkflowDirection) {
        if (ServiceUtil.isNetworkAvailable(this.requireContext().applicationContext)) {
            Coroutines.main {
                workflowMeasurements(workflowDirection)
            }
        } else {
            extensionToast(
                message = getString(R.string.no_connection_detected),
                style = ToastStyle.NO_INTERNET
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
                                selectedJobId!!,
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
        uiScope.launch(dispatchers.ui()) {
            extensionToast(
                message = errMessage,
                style = ToastStyle.ERROR,
                position = ToastGravity.CENTER
            )
            progressButton.failProgress(progFailCaption)
        }
    }

    private fun popViewOnJobSubmit(direction: Int) {
        when (direction) {
            NEXT.value -> {
                extensionToast(
                    message = getString(R.string.measurement_approved),
                    style = ToastStyle.SUCCESS,
                    duration = ToastDuration.SHORT
                )
            }
            WorkflowDirection.FAIL.value -> {
                extensionToast(
                    message = getString(R.string.measurement_declined),
                    style = ToastStyle.INFO,
                    duration = ToastDuration.SHORT
                )
            }
        }
        Handler(Looper.getMainLooper()).postDelayed({
            Intent(this.requireActivity(), MainActivity::class.java).also { mainAct ->
                mainAct.flags =
                    Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK

                startActivity(mainAct)
            }
        }, Constants.TWO_SECONDS)
    }

    private fun getMeasureItems(jobId: String) {
        Coroutines.main {
            val measurements = approveViewModel.getJobMeasureItemsForJobId(
                jobId,
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
        groupAdapter.apply {
            clear()
            update(measureListItems)
            notifyItemRangeChanged(0, measureListItems.size)
        }
        _ui?.viewMeasuredItems?.getRecyclerView()?.run {
            adapter = groupAdapter
            layoutManager = LinearLayoutManager(
                this@MeasureApprovalFragment.requireContext()
            )
            doOnNextLayout { _ui?.viewMeasuredItems?.unVeil() }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // clear out all the leakers
        approveViewModel.workflowState.removeObservers(viewLifecycleOwner)
        _ui?.viewMeasuredItems?.setAdapter(null)
        _ui = null
    }

    private fun List<JobItemMeasureDTO>.toMeasureItems(): List<MeasurementsItem> {
        return this.map {
            MeasurementsItem(
                it,
                approveViewModel,
                WeakReference(this@MeasureApprovalFragment),
                viewLifecycleOwner
            )
        }
    }
}
