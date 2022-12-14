/**
 * Updated by Shaun McDonald on 2021/05/19
 * Last modified on 2021/05/18, 19:02
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 **/

package za.co.xisystems.itis_rrm.ui.mainview.estmeasure.submit_measure

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenStarted
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.xwray.groupie.ExpandableGroup
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.viewbinding.GroupieViewHolder
import kotlinx.coroutines.*
import org.kodein.di.instance
import timber.log.Timber
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.base.BaseFragment
import za.co.xisystems.itis_rrm.custom.notifications.ToastDuration
import za.co.xisystems.itis_rrm.custom.notifications.ToastGravity
import za.co.xisystems.itis_rrm.custom.notifications.ToastGravity.BOTTOM
import za.co.xisystems.itis_rrm.custom.notifications.ToastStyle
import za.co.xisystems.itis_rrm.custom.results.XIResult
import za.co.xisystems.itis_rrm.custom.views.IndefiniteSnackbar
import za.co.xisystems.itis_rrm.data.localDB.entities.*
import za.co.xisystems.itis_rrm.databinding.FragmentSubmitMeasureBinding
import za.co.xisystems.itis_rrm.databinding.ItemMeasureHeaderBinding
import za.co.xisystems.itis_rrm.extensions.isConnected
import za.co.xisystems.itis_rrm.extensions.observeOnce
import za.co.xisystems.itis_rrm.ui.extensions.*
import za.co.xisystems.itis_rrm.ui.mainview.activities.main.MainActivity
import za.co.xisystems.itis_rrm.ui.mainview.estmeasure.MeasureViewModel
import za.co.xisystems.itis_rrm.ui.mainview.estmeasure.MeasureViewModelFactory
import za.co.xisystems.itis_rrm.utils.Coroutines
import za.co.xisystems.itis_rrm.utils.DataConversion
import java.util.*

class SubmitMeasureFragment : BaseFragment() {

    private lateinit var measureViewModel: MeasureViewModel
    private val factory: MeasureViewModelFactory by instance()
    private lateinit var jobItemMeasurePhotoDTO: ArrayList<JobItemMeasurePhotoDTO>
    private lateinit var jobItemMeasureArrayList: ArrayList<JobItemMeasureDTO>
    private lateinit var jobItemEstimatesForJob: ArrayList<JobItemEstimateDTO>
    private lateinit var jobItemMeasuresForJobItemEstimates: HashMap<JobItemEstimateDTO, List<JobItemMeasureDTO>>
    private lateinit var jobForItemEstimate: JobDTO
    private lateinit var jobItemMeasureList: ArrayList<JobItemMeasureDTO>
    private lateinit var jobItemEstimate: JobItemEstimateDTO
    private lateinit var expandableGroups: MutableList<ExpandableGroup>
    private lateinit var progressButton: Button
    private lateinit var originalCaption: String
    private var measurementObserver = Observer<XIResult<String>?> { handleMeasureSubmission(it) }
    private var measureJob: Job? = null
    private var _ui: FragmentSubmitMeasureBinding? = null
    private val ui get() = _ui!!

    init {
        lifecycleScope.launch {
            whenStarted {

                uiScope.launch(uiScope.coroutineContext) {

                    val estimateData = measureViewModel.estimateMeasureItem.distinctUntilChanged()
                    estimateData.observeOnce(viewLifecycleOwner, { estimateMeasureItem ->
                        jobItemEstimate = estimateMeasureItem.jobItemEstimateDTO
                        getWorkItems(jobItemEstimate.jobId)
                    })
                }
            }
        }
    }

    private fun handleMeasureSubmission(event: XIResult<String>?) {
        event?.let { outcome ->

            when (outcome) {
                is XIResult.Success -> {

                    extensionToast(
                        message = "Measurements submitted for Job ${outcome.data}",
                        style = ToastStyle.SUCCESS
                    )
                    progressButton.doneProgress(originalCaption)
                    toggleLongRunning(false)
                    jobItemMeasureList.clear()
                    popViewOnJobSubmit()
                }
                is XIResult.Error -> {
                    toggleLongRunning(false)

                    progressButton.failProgress("Workflow failed ...")
                    measureJob?.cancel(CancellationException(outcome.message))

                    extensionToast(
                        message = "Submission failed: ${outcome.message}",
                        style = ToastStyle.ERROR
                    )

                    crashGuard(
                        throwable = outcome,
                        refreshAction = { this@SubmitMeasureFragment.retryMeasurements() }
                    )
                }
                is XIResult.Status -> {
                    extensionToast(
                        message = outcome.message,
                        duration = ToastDuration.SHORT,
                        position = BOTTOM
                    )
                }

                is XIResult.Progress -> {
                    handleMeasurementProgress(outcome)
                }
                else -> Timber.d("$event")
            }
        }
    }

    private fun handleMeasurementProgress(outcome: XIResult.Progress) {
        toggleLongRunning(outcome.isLoading)
        when (outcome.isLoading) {
            true -> {
                progressButton.initProgress(viewLifecycleOwner)
                progressButton.startProgress("Submitting ...")
            }
            else -> {
                progressButton.doneProgress(originalCaption)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        jobItemMeasurePhotoDTO = ArrayList()
        jobItemMeasureArrayList = ArrayList()
        jobItemEstimatesForJob = ArrayList()
        jobItemMeasureList = ArrayList()

        jobItemMeasuresForJobItemEstimates =
            HashMap()

        measureViewModel =
            ViewModelProvider(this@SubmitMeasureFragment.requireActivity(), factory)[MeasureViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _ui = FragmentSubmitMeasureBinding.inflate(inflater, container, false)
        _ui?.toolbar?.apply {
            setTitle(getString(R.string.submit_measure_title))
            setOnBackClickListener(backClickListener)
        }

        return ui.root
    }

    private val backClickListener = View.OnClickListener {
        setBackPressed(it)
    }

    private fun setBackPressed(view: View) {
        Coroutines.io {
            withContext(Dispatchers.Main.immediate) {
                val navDirection = SubmitMeasureFragmentDirections
                    .actionSubmitMeasureFragmentToNavigationMeasureEst()
                Navigation.findNavController(view).navigate(navDirection)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Coroutines.main {

            ui.submitMeasurementsButton.setOnClickListener {
                progressButton = ui.submitMeasurementsButton
                originalCaption = ui.submitMeasurementsButton.text.toString()
                progressButton.initProgress(viewLifecycleOwner)
                measurementPrompt(jobForItemEstimate.jobId)
            }

            ui.itemsSwipeToRefresh.setProgressBackgroundColorSchemeColor(
                ContextCompat.getColor(
                    requireContext().applicationContext,
                    R.color.colorPrimary
                )
            )
            ui.itemsSwipeToRefresh.setColorSchemeColors(Color.WHITE)

            ui.itemsSwipeToRefresh.setOnRefreshListener {
                Coroutines.main {
                    measureViewModel.estimateMeasureItem.observeOnce(
                        viewLifecycleOwner,
                        { measureItem ->
                            getWorkItems(measureItem.jobItemEstimateDTO.jobId)
                            ui.itemsSwipeToRefresh.isRefreshing = false
                        }
                    )
                }
            }
        }
    }

    private fun measurementPrompt(jobId: String) {
        val workflowDialog = AlertDialog.Builder(
            requireActivity() // , android.R.style.Theme_DeviceDefault_Dialog
        )
        workflowDialog.run {
            setTitle(R.string.confirm)
            setIcon(R.drawable.ic_approve)
            setMessage(R.string.are_you_sure_you_want_to_submit_measurements)
            // Yes button
            setPositiveButton(R.string.yes) { _, _ ->
                if (requireActivity().isConnected) {
                    toggleLongRunning(true)
                    submitMeasurements(jobId)
                } else {
                    extensionToast(
                        message = getString(R.string.no_connection_detected),
                        style = ToastStyle.NO_INTERNET,
                        position = ToastGravity.CENTER
                    )
                    progressButton.failProgress(originalCaption)
                }
            }
            // No button
            setNegativeButton(
                R.string.no
            ) { dialog, _ ->
                // Do nothing but close dialog
                dialog.dismiss()
                progressButton.doneProgress(originalCaption)
            }
            create()
            show()
        }
    }

    private fun submitMeasurements(jobId: String?) {
        Coroutines.main {
            progressButton.startProgress("Submitting ...")
            measureViewModel.setBackupJobId(jobId!!)
            val jobItemMeasure =
                measureViewModel.getJobItemMeasuresForJobIdAndEstimateId(jobId) // estimateId
            jobItemMeasure.observeOnce(viewLifecycleOwner, { measureList ->
                val validMeasures = measureList.filter { msure ->
                    msure.qty > 0 && !msure.jobItemMeasurePhotos.isNullOrEmpty()
                }
                if (validMeasures.isNullOrEmpty()) {
                    extensionToast(
                        message = getString(R.string.please_make_sure_you_have_captured_photos),
                        style = ToastStyle.WARNING
                    )
                    progressButton.failProgress(originalCaption)
                } else {
                    extensionToast(
                        message = "You have Done " + validMeasures.size.toString() + " Measurements on this Estimate",
                        style = ToastStyle.INFO
                    )

                    val itemMeasures = validMeasures as ArrayList
                    itemMeasures.forEach { jim ->
                        val newJim = setJobMeasureLittleEndianGuids(jim)
                        jobItemMeasureList.add(newJim)
                    }
                    submitMeasures(jobForItemEstimate, jobItemMeasureList)
                }
            })
        }
    }

    private fun retryMeasurements() {
        IndefiniteSnackbar.hide()
        val backupJob = measureViewModel.backupJobId
        backupJob.observeOnce(viewLifecycleOwner, { response ->
            response?.let { jobId ->
                submitMeasurements(jobId)
            }
        })
    }

    private fun submitMeasures(
        itemMeasureJob: JobDTO,
        mSures: ArrayList<JobItemMeasureDTO>
    ) {
//         if(itemMeasureJob.contractVoId.isNullOrEmpty()){
//            extensionToast(
//                title = "Estimate Measurements",
//                message = "Contract ID Missing Please Contact ITIS Support",
//                style = ToastStyle.ERROR,
//                position = ToastGravity.CENTER
//            )
//             toggleLongRunning(false)
//             progressButton.failProgress(originalCaption)
//             popViewBck()
//        }else{
              Coroutines.main {
            val estimateIds = itemMeasureJob.jobItemEstimates.map { it.estimateId }
            val measureEstimateIds = mSures.map { it.estimateId }.distinct()
            val missingEstimateIds = estimateIds.asSequence().minus(measureEstimateIds).map { it }.toList()

            val user = measureViewModel.user.await()

            user.observe(viewLifecycleOwner, { userDTO ->
                when {
                    userDTO.userId.isBlank() -> {
                        showSubmissionError("Current user lacks permissions")
                    }
                    itemMeasureJob.jobId.isBlank() -> {
                        showSubmissionError("Selected job is invalid")
                    }
                    missingEstimateIds.isNotEmpty() -> {
                        showSubmissionError("Please include measurements for all estimates")
                    }
                    else -> {
                        prepareMeasurementWorkflow(itemMeasureJob, userDTO, mSures)
                    }
                }
            })
//        }
        }

    }

    private fun popViewBck() {
        val navDirection = SubmitMeasureFragmentDirections
            .actionSubmitMeasureFragmentToNavigationMeasureEst()
        Navigation.findNavController(this@SubmitMeasureFragment.requireView()).navigate(navDirection)
    }

    private fun prepareMeasurementWorkflow(
        itemMeasureJob: JobDTO,
        userDTO: UserDTO,
        mSures: ArrayList<JobItemMeasureDTO>
    ) {
        // littleEndian conversion for transport to backend
        Coroutines.main {
            val contractId = measureViewModel.getContractIdForProjectId(itemMeasureJob.projectId)
            val contractVoId: String =
                DataConversion.toLittleEndian(contractId)!!
            val jobId: String = DataConversion.toLittleEndian(itemMeasureJob.jobId)!!

                activity?.let {
                    processMeasurementWorkflow(
                        userDTO,
                        jobId,
                        itemMeasureJob,
                        contractVoId,
                        mSures,
                        it
                    )
                }
        }

    }

    private fun showSubmissionError(errorMessage: String) {
        extensionToast(
            title = "Estimate Measurements",
            message = errorMessage,
            style = ToastStyle.ERROR,
            position = ToastGravity.CENTER
        )
        toggleLongRunning(false)
        progressButton.failProgress(originalCaption)
    }

    private fun processMeasurementWorkflow(
        userDTO: UserDTO,
        jobId: String,
        itemMeasureJob: JobDTO,
        contractVoId: String,
        mSures: ArrayList<JobItemMeasureDTO>,
        it: FragmentActivity
    ) {
        uiScope.launch(uiScope.coroutineContext) {

            measureViewModel.workflowState.observe(
                viewLifecycleOwner,
                measurementObserver
            )

            measureJob = measureViewModel.processWorkflowMove(
                userDTO.userId,
                jobId,
                itemMeasureJob.jiNo,
                contractVoId,
                mSures,
                it,
                itemMeasureJob
            )
        }
    }

    private fun setJobMeasureLittleEndianGuids(jobMeasure: JobItemMeasureDTO): JobItemMeasureDTO {
        jobMeasure.setEstimateId(DataConversion.toLittleEndian(jobMeasure.estimateId))
        jobMeasure.setJobId(DataConversion.toLittleEndian(jobMeasure.jobId))
        jobMeasure.setProjectItemId(DataConversion.toLittleEndian(jobMeasure.projectItemId))
        jobMeasure.setItemMeasureId(DataConversion.toLittleEndian(jobMeasure.itemMeasureId))

        if (jobMeasure.trackRouteId != null) {
            jobMeasure.setTrackRouteId(DataConversion.toLittleEndian(jobMeasure.trackRouteId))
        } else {
            jobMeasure.trackRouteId = null
        }

        if (jobMeasure.measureGroupId != null) {
            jobMeasure.setMeasureGroupId(DataConversion.toLittleEndian(jobMeasure.measureGroupId))
        }

        jobMeasure.jobItemMeasurePhotos.forEach { jmep ->
            jmep.setPhotoId(DataConversion.toLittleEndian(jmep.photoId))
            jmep.setEstimateId(jobMeasure.estimateId)
            jmep.setItemMeasureId(jobMeasure.itemMeasureId)
        }
        return jobMeasure
    }

    private fun popViewOnJobSubmit() {
        // Delete data from database after successful upload
        Intent(requireContext(), MainActivity::class.java).also { home ->
            home.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(home)
        }
    }



    override fun onResume() {
        super.onResume()

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val navDirection = SubmitMeasureFragmentDirections
                    .actionSubmitMeasureFragmentToNavigationMeasureEst()
                Navigation.findNavController(this@SubmitMeasureFragment.requireView()).navigate(navDirection)
            }
        }
        requireActivity().onBackPressedDispatcher
            .addCallback(this, callback)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        val item = menu.findItem(R.id.action_settings)
        val item1 = menu.findItem(R.id.action_logout)
        val item2 = menu.findItem(R.id.action_search)
        if (item != null) item.isVisible = false
        if (item1 != null) item1.isVisible = false
        if (item2 != null) item2.isVisible = false
    }

    private fun getWorkItems(jobID: String?) {
        Coroutines.main {
            val measurements =
                measureViewModel.getJobItemsToMeasureForJobId(jobID).distinctUntilChanged()
            measurements.observeOnce(viewLifecycleOwner, { estimateList ->
                initRecyclerView(estimateList.toMeasureItems())
            })
        }
    }

    private fun initRecyclerView(measureItems: List<ExpandableGroup>) {
        val groupAdapter = GroupAdapter<GroupieViewHolder<ItemMeasureHeaderBinding>>().apply {
            addAll(measureItems)
            notifyItemRangeChanged(0, measureItems.size)
        }

        ui.measureListView.apply {
            layoutManager = LinearLayoutManager(this@SubmitMeasureFragment.requireContext())
            adapter = groupAdapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // measureViewModel.workflowState.removeObservers(viewLifecycleOwner)
        ui.measureListView.adapter = null
        _ui = null
    }

    private fun List<JobItemEstimateDTO>.toMeasureItems(): List<ExpandableGroup> {
        expandableGroups = mutableListOf()
        return this.map { jobItemEstimateDTO ->
            val expandableHeaderItem = ExpandableHeaderMeasureItem(
                this@SubmitMeasureFragment,
                jobItemEstimateDTO,
                measureViewModel,
                jobItemMeasurePhotoDTO,
                jobItemMeasureArrayList
            )
            expandableHeaderItem.onExpandListener = { toggledGroup ->
                expandableGroups.forEach {
                    if (it != toggledGroup && it.isExpanded) {
                        it.onToggleExpanded()
                    }
                }
            }

            ExpandableGroup(expandableHeaderItem, true).apply {
                expandableGroups.add(this)
                Coroutines.main {
                    val jobForJobItemEstimate = measureViewModel.getJobFromJobId(jobItemEstimateDTO.jobId)
                    jobForJobItemEstimate.observeOnce(
                        viewLifecycleOwner,
                        { job ->
                            //                        for (measure_i in jobItemMeasureArrayList) {
                            jobForItemEstimate = job
                            Coroutines.main {
                                val jobItemMeasure =
                                    measureViewModel.getJobItemMeasuresForJobIdAndEstimateId2(
                                        job.jobId,
                                        jobItemEstimateDTO.estimateId
                                    )
                                jobItemMeasure.observeOnce(viewLifecycleOwner, { measureList ->
                                    Coroutines.main {
                                        measureList?.forEach { jobItemMeasure ->
                                            Coroutines.main {
                                                val itemMeasureId = jobItemMeasure.itemMeasureId
                                                val qty = jobItemMeasure.qty.toString()
                                                val rate = jobItemMeasure.lineRate.toString()
                                                val jNo = jobItemMeasure.jimNo.toString()
                                                add(
                                                    CardMeasureItem(
                                                        activity = activity,
                                                        itemMeasureId = itemMeasureId,
                                                        qty = "$qty", // x $rate",
                                                        rate = "${qty.toBigDecimal() * rate.toBigDecimal()}",
                                                        text = jNo,
                                                        measureViewModel = measureViewModel,
                                                        uiScope = uiScope
                                                    )
                                                )
                                            }
                                        }
                                    }
                                })
                            }
                        }
                    )
                }
            }
        }
    }

    private fun JobItemMeasureDTO.setProjectItemId(toLittleEndian: String?) {
        this.projectItemId = toLittleEndian
    }

    private fun JobItemMeasureDTO.setMeasureGroupId(toLittleEndian: String?) {
        this.measureGroupId = toLittleEndian
    }

    private fun JobItemMeasureDTO.setEstimateId(toLittleEndian: String?) {
        this.estimateId = toLittleEndian
    }

    private fun JobItemMeasureDTO.setTrackRouteId(toLittleEndian: String?) {
        this.trackRouteId = toLittleEndian
    }

    private fun JobItemMeasurePhotoDTO.setItemMeasureId(toLittleEndian: String?) {
        this.itemMeasureId = toLittleEndian!!
    }

    private fun JobItemMeasureDTO.setJobId(toLittleEndian: String?) {
        this.jobId = toLittleEndian
    }

    private fun JobItemMeasurePhotoDTO.setPhotoId(toLittleEndian: String?) {
        this.photoId = toLittleEndian!!
    }

    private fun JobItemMeasurePhotoDTO.setEstimateId(toLittleEndian: String?) {
        this.estimateId = toLittleEndian!!
    }

    private fun JobItemMeasureDTO.setItemMeasureId(toLittleEndian: String?) {
        this.itemMeasureId = toLittleEndian!!
    }
}
