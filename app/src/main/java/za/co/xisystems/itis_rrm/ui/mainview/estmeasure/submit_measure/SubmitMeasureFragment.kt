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
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.xwray.groupie.ExpandableGroup
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import kotlinx.android.synthetic.main.fragment_submit_measure.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import www.sanju.motiontoast.MotionToast
import za.co.xisystems.itis_rrm.MainActivity
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.base.BaseFragment
import za.co.xisystems.itis_rrm.custom.errors.XIErrorHandler
import za.co.xisystems.itis_rrm.custom.results.XIError
import za.co.xisystems.itis_rrm.custom.results.XISuccess
import za.co.xisystems.itis_rrm.custom.views.IndefiniteSnackbar
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimateDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemMeasureDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemMeasurePhotoDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.UserDTO
import za.co.xisystems.itis_rrm.extensions.observeOnce
import za.co.xisystems.itis_rrm.ui.extensions.doneProgress
import za.co.xisystems.itis_rrm.ui.extensions.failProgress
import za.co.xisystems.itis_rrm.ui.extensions.initProgress
import za.co.xisystems.itis_rrm.ui.extensions.startProgress
import za.co.xisystems.itis_rrm.ui.mainview.estmeasure.MeasureViewModel
import za.co.xisystems.itis_rrm.ui.mainview.estmeasure.MeasureViewModelFactory
import za.co.xisystems.itis_rrm.ui.scopes.UiLifecycleScope
import za.co.xisystems.itis_rrm.utils.Coroutines
import za.co.xisystems.itis_rrm.utils.DataConversion
import za.co.xisystems.itis_rrm.utils.ServiceUtil
import java.util.ArrayList
import java.util.HashMap

class SubmitMeasureFragment : BaseFragment(R.layout.fragment_submit_measure), KodeinAware {
    override val kodein by kodein()
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
    private var uiScope = UiLifecycleScope()
    private lateinit var progressButton: Button
    private lateinit var originalCaption: String

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        lifecycle.addObserver(uiScope)
        setHasOptionsMenu(true)
        (activity as MainActivity).supportActionBar?.title =
            getString(R.string.submit_measure_title)
        jobItemMeasurePhotoDTO = ArrayList()
        jobItemMeasureArrayList = ArrayList()
        jobItemEstimatesForJob = ArrayList()
        jobItemMeasureList = ArrayList()

        jobItemMeasuresForJobItemEstimates =
            HashMap()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        (activity as MainActivity).supportActionBar?.title =
            getString(R.string.submit_measure_title)

        return inflater.inflate(R.layout.fragment_submit_measure, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        measureViewModel = activity?.run {
            ViewModelProvider(this, factory).get(MeasureViewModel::class.java)
        } ?: throw Exception("Invalid Activity")

        Coroutines.main {

            measureViewModel.estimateMeasureItem.observe(viewLifecycleOwner, { jobID ->
                jobItemEstimate = jobID.jobItemEstimateDTO
                getWorkItems(jobItemEstimate.jobId)
            })

            submit_measurements_button.setOnClickListener {
                progressButton = submit_measurements_button
                originalCaption = submit_measurements_button.text.toString()
                progressButton.initProgress(viewLifecycleOwner)
                submitMeasurements(
                    jobItemEstimate.jobId
                )
            }

            items_swipe_to_refresh.setProgressBackgroundColorSchemeColor(
                ContextCompat.getColor(
                    requireContext().applicationContext,
                    R.color.colorPrimary
                )
            )

            items_swipe_to_refresh.setColorSchemeColors(Color.WHITE)

            items_swipe_to_refresh.setOnRefreshListener {
                Coroutines.main {
                    measureViewModel.estimateMeasureItem.observeOnce(
                        viewLifecycleOwner,
                        { measureItem ->
                            getWorkItems(measureItem.jobItemEstimateDTO.jobId)
                            items_swipe_to_refresh.isRefreshing = false
                        })
                }
            }
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
                    msure.qty > 0 && msure.jobItemMeasurePhotos.isNotEmpty()
                }
                if (validMeasures.isNullOrEmpty()) {
                    this.motionToast(R.string.please_make_sure_you_have_captured_photos, MotionToast.TOAST_WARNING)
                    progressButton.failProgress(originalCaption)
                } else {
                    this.motionToast(
                        "You have Done " + validMeasures.size.toString() + " Measurements on this Estimate",
                        MotionToast.TOAST_INFO
                    )

                    val itemMeasures = validMeasures as ArrayList
                    for (jim in itemMeasures) {
                        val newJim = setJobMeasureLittleEndianGuids(jim)!!
                        jobItemMeasureList.add(newJim)
                    }
                    submitJobToMeasurements(jobForItemEstimate, jobItemMeasureList)
                }
            })
            // jobItemMeasure.removeObservers(viewLifecycleOwner)
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

    private fun submitJobToMeasurements(
        itemMeasureJob: JobDTO,
        mSures: ArrayList<JobItemMeasureDTO>

    ) {

        val logoutBuilder = AlertDialog.Builder(
            requireActivity() // , android.R.style.Theme_DeviceDefault_Dialog
        )
        logoutBuilder.run {
            setTitle(R.string.confirm)
            setIcon(R.drawable.ic_approve)
            setMessage(R.string.are_you_sure_you_want_to_submit_measurements)
            // Yes button
            setPositiveButton(R.string.yes) { dialog, which ->
                if (ServiceUtil.isNetworkAvailable(requireContext().applicationContext)) {
                    submitMeasures(itemMeasureJob, mSures)
                } else {
                    this@SubmitMeasureFragment.motionToast(
                        getString(R.string.no_connection_detected),
                        MotionToast.TOAST_ERROR,
                        MotionToast.GRAVITY_BOTTOM
                    )
                    progressButton.failProgress(originalCaption)
                }
            }
            // No button
            setNegativeButton(
                R.string.no
            ) { dialog, which ->
                // Do nothing but close dialog
                dialog.dismiss()
                progressButton.doneProgress(originalCaption)
            }
            create()
            show()
        }
    }

    private fun submitMeasures(
        itemMeasureJob: JobDTO,
        mSures: ArrayList<JobItemMeasureDTO>
    ) {
        Coroutines.main {
            val user = measureViewModel.user.await()
            user.observe(viewLifecycleOwner, { userDTO ->
                when {
                    userDTO.userId.isBlank() -> {
                        this@SubmitMeasureFragment.motionToast(
                            "Error: current user lacks permissions",
                            MotionToast.TOAST_ERROR,
                            MotionToast.GRAVITY_BOTTOM
                        )
                        progressButton.failProgress(originalCaption)

                    }
                    itemMeasureJob.JobId.isBlank() -> {
                        this@SubmitMeasureFragment.motionToast(
                            "Error: selected job is invalid",
                            MotionToast.TOAST_ERROR,
                            MotionToast.GRAVITY_BOTTOM
                        )
                        progressButton.failProgress(originalCaption)
                    }
                    else -> {
                        // beware littleEndian conversion for transport to backend
                        val contractVoId: String =
                            DataConversion.toLittleEndian(itemMeasureJob.ContractVoId)!!
                        val jobId: String = DataConversion.toLittleEndian(itemMeasureJob.JobId)!!

                        Coroutines.main {

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
                }
            })
        }
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

            val workflowOutcome = measureViewModel.workflowState

            val result = measureViewModel.processWorkflowMove(
                userDTO.userId,
                jobId,
                itemMeasureJob.JiNo,
                contractVoId,
                mSures,
                it,
                itemMeasureJob
            )


            workflowOutcome.observe(
                viewLifecycleOwner,
                { response ->
                    response?.let { outcome ->
                        when (outcome) {
                            is XISuccess -> {
                                this@SubmitMeasureFragment.motionToast(
                                    "Measurements submitted for Job ${outcome.data}",
                                    MotionToast.TOAST_SUCCESS
                                )
                                progressButton.doneProgress(originalCaption)
                                jobItemMeasureList.clear()
                                popViewOnJobSubmit()
                            }
                            is XIError -> {
                                result.cancel(CancellationException(outcome.message))
                                this@SubmitMeasureFragment.motionToast(
                                    "Submission failed",
                                    MotionToast.TOAST_ERROR
                                )

                                XIErrorHandler.crashGuard(
                                    this@SubmitMeasureFragment,
                                    this@SubmitMeasureFragment.requireView(),
                                    outcome,
                                    refreshAction = { retryMeasurements() })
                            }
                        }
                    }
                })

        }
    }

    private fun setJobMeasureLittleEndianGuids(jim: JobItemMeasureDTO?): JobItemMeasureDTO? {
        jim?.let { jobMeasure ->
            jobMeasure.setEstimateId(DataConversion.toLittleEndian(jobMeasure.estimateId))
            jobMeasure.setJobId(DataConversion.toLittleEndian(jobMeasure.jobId))
            jobMeasure.setProjectItemId(DataConversion.toLittleEndian(jobMeasure.projectItemId))
            jobMeasure.setItemMeasureId(DataConversion.toLittleEndian(jobMeasure.itemMeasureId))

            if (jobMeasure.trackRouteId != null)
                jobMeasure.setTrackRouteId(DataConversion.toLittleEndian(jobMeasure.trackRouteId))
            else jobMeasure.trackRouteId = null

            if (jobMeasure.measureGroupId != null)
                jobMeasure.setMeasureGroupId(DataConversion.toLittleEndian(jobMeasure.measureGroupId))

            if (jobMeasure.jobItemMeasurePhotos.isNotEmpty()) {
                for (jmep in jobMeasure.jobItemMeasurePhotos) {
                    jmep.setPhotoId(DataConversion.toLittleEndian(jmep.photoId))
                    jmep.setEstimateId(DataConversion.toLittleEndian(jmep.estimateId))
                    jmep.setItemMeasureId(DataConversion.toLittleEndian(jmep.itemMeasureId))
                }
            }
        }

        return jim
    }

    private fun popViewOnJobSubmit() {
        // TODO: Delete data from database after successful upload

        Intent(context?.applicationContext, MainActivity::class.java).also { home ->
            startActivity(home)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).supportActionBar?.title =
            getString(R.string.submit_measure_title)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (activity as MainActivity).supportActionBar?.title =
            getString(R.string.submit_measure_title)
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
            val measurements = measureViewModel.getJobItemsToMeasureForJobId(jobID)
            measurements.observeOnce(viewLifecycleOwner, { estimateList ->
                initRecyclerView(estimateList.toMeasureItem())
            })
        }
    }

    private fun initRecyclerView(tomeasureItem: List<ExpandableGroup>) {
        val groupAdapter = GroupAdapter<GroupieViewHolder>().apply {
            addAll(tomeasureItem)
        }

        measure_listView.apply {
            layoutManager = LinearLayoutManager(this.context)
            adapter = groupAdapter
        }
    }

    override fun onDestroyView() {
        uiScope.destroy()
       // measureViewModel.workflowState.removeObservers(viewLifecycleOwner)
        measure_listView.adapter = null
        super.onDestroyView()
    }

    private fun List<JobItemEstimateDTO>.toMeasureItem(): List<ExpandableGroup> {
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
                        requireActivity(),
                        { job ->
                            //                        for (measure_i in jobItemMeasureArrayList) {
                            jobForItemEstimate = job
                            Coroutines.main {
                                val jobItemMeasure =
                                    measureViewModel.getJobItemMeasuresForJobIdAndEstimateId2(
                                        job.JobId,
                                        jobItemEstimateDTO.estimateId
                                    )
                                jobItemMeasure.observeOnce(requireActivity(), { measureList ->
                                    Coroutines.main {
                                        for (jobItemM in measureList) {
                                            Coroutines.main {
                                                val itemMeasureId = jobItemM.itemMeasureId!!
                                                val qty = jobItemM.qty.toString()
                                                val rate = jobItemM.lineRate.toString()
                                                val jNo = jobItemM.jimNo.toString()
                                                add(
                                                    CardMeasureItem(
                                                        activity = activity,
                                                        itemMeasureId = itemMeasureId,
                                                        qty = qty,
                                                        rate = rate,
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
        this.itemMeasureId = toLittleEndian
    }

    private fun JobItemMeasureDTO.setJobId(toLittleEndian: String?) {
        this.jobId = toLittleEndian
    }

    private fun JobItemMeasurePhotoDTO.setPhotoId(toLittleEndian: String?) {
        this.photoId = toLittleEndian!!
    }

    private fun JobItemMeasurePhotoDTO.setEstimateId(toLittleEndian: String?) {
        this.estimateId = toLittleEndian
    }

    private fun JobItemMeasureDTO.setItemMeasureId(toLittleEndian: String?) {
        this.itemMeasureId = toLittleEndian
    }
}
