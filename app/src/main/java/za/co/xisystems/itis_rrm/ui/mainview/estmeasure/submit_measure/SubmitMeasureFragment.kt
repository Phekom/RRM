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
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.xwray.groupie.ExpandableGroup
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import kotlinx.android.synthetic.main.fragment_submit_measure.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import za.co.xisystems.itis_rrm.MainActivity
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimateDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemMeasureDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemMeasurePhotoDTO
import za.co.xisystems.itis_rrm.ui.mainview._fragments.BaseFragment
import za.co.xisystems.itis_rrm.ui.mainview.estmeasure.MeasureViewModel
import za.co.xisystems.itis_rrm.ui.mainview.estmeasure.MeasureViewModelFactory
import za.co.xisystems.itis_rrm.utils.ActivityIdConstants
import za.co.xisystems.itis_rrm.utils.Coroutines
import za.co.xisystems.itis_rrm.utils.DataConversion
import za.co.xisystems.itis_rrm.utils.ServiceUtil
import java.util.*


class SubmitMeasureFragment : BaseFragment(), KodeinAware {
    override val kodein by kodein()
    private lateinit var measureViewModel: MeasureViewModel
    private val factory: MeasureViewModelFactory by instance()
    private val jobItemMeasure: JobItemMeasureDTO? = null
    private lateinit var jobItemMeasurePhotoDTO: ArrayList<JobItemMeasurePhotoDTO>
    private lateinit var jobItemMeasureArrayList: ArrayList<JobItemMeasureDTO>
    private lateinit var jobItemEstimatesForJob: ArrayList<JobItemEstimateDTO>
    private lateinit var jobItemMeasuresForJobItemEstimates: HashMap<JobItemEstimateDTO, List<JobItemMeasureDTO>>
    private lateinit var jobForItemEstimate: JobDTO
    private lateinit var jobItemMeasureList: ArrayList<JobItemMeasureDTO>
    private lateinit var jobItemEstimate : JobItemEstimateDTO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        (activity as MainActivity).supportActionBar?.title =
            getString(R.string.submit_measure_title)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        (activity as MainActivity).supportActionBar?.title =
            getString(R.string.submit_measure_title)
        measureViewModel = activity?.run {
            ViewModelProvider(this, factory).get(MeasureViewModel::class.java)
        } ?: throw Exception("Invalid Activity")

        Coroutines.main {
            measureViewModel.measure_Item.observe(viewLifecycleOwner, Observer { jobID ->
                getWorkItems(jobID.jobItemEstimateDTO.jobId)
            })
        }
        return inflater.inflate(R.layout.fragment_submit_measure, container, false)
    }

//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        (activity as MainActivity).supportActionBar?.title =
//            getString(R.string.submit_measure_title)
//
//    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        measureViewModel = activity?.run {
            ViewModelProvider(this, factory).get(MeasureViewModel::class.java)
        } ?: throw Exception("Invalid Activity")

        Coroutines.main {
            jobItemMeasurePhotoDTO = ArrayList()
            jobItemMeasureArrayList = ArrayList()
            jobItemEstimatesForJob = ArrayList()
            jobItemMeasureList = ArrayList()

            jobItemMeasuresForJobItemEstimates = HashMap()

            measureViewModel.measure_Item.observe(viewLifecycleOwner, Observer { jobID ->
                jobItemEstimate = jobID.jobItemEstimateDTO
                getWorkItems(jobItemEstimate.jobId)
            })


            submit_measurements_button.setOnClickListener {
                submitMeasurements(
                    jobItemEstimate.jobId,
                    jobItemEstimate.estimateId
                )

            }

            items_swipe_to_refresh.setProgressBackgroundColorSchemeColor(
                ContextCompat.getColor(
                    context!!.applicationContext,
                    R.color.colorPrimary
                )
            )
            items_swipe_to_refresh.setColorSchemeColors(Color.WHITE)

            items_swipe_to_refresh.setOnRefreshListener {
                Coroutines.main {
                    measureViewModel.measure_Item.observe(viewLifecycleOwner, Observer { measureItem ->
                        getWorkItems(measureItem.jobItemEstimateDTO.jobId)
                        items_swipe_to_refresh.isRefreshing = false
                    })

                }
            }


        }


    }

    private fun submitMeasurements(jobId: String?, estimateId: String) {
        Coroutines.main {
            val jobItemMeasure = measureViewModel.getJobItemMeasuresForJobIdAndEstimateId(jobId, estimateId)
            jobItemMeasure.observe(activity!!, Observer { m_sures ->
                if (m_sures.isNullOrEmpty() || m_sures.isEmpty()) {
                    toast(R.string.please_make_sure_you_have_captured_photos)
                } else {
                    toast("You have Done " + m_sures.size.toString() + " Measurements on this Estimate")

                    val itemMeasures = m_sures as ArrayList
                    submitJobToMeasurements(jobForItemEstimate, itemMeasures)

                }
            })
        }
    }

    private fun submitJobToMeasurements(
        itemMeasureJob: JobDTO,
        mSures: ArrayList<JobItemMeasureDTO>
//        filename: String,
//        jobItemMeasures: JobItemMeasureDTO
    ) {
        val logoutBuilder = AlertDialog.Builder(
            activity //,android.R.style.Theme_DeviceDefault_Dialog
        )
        logoutBuilder.setTitle(R.string.confirm)
        logoutBuilder.setIcon(R.drawable.ic_approve)
        logoutBuilder.setMessage(R.string.are_you_sure_you_want_to_submit_measurements)

        // Yes button
        logoutBuilder.setPositiveButton( R.string.yes) { dialog, which ->
            if (ServiceUtil.isNetworkConnected(context?.applicationContext)) {
                submitMeasures(itemMeasureJob, mSures)
//                uploadRrmImage(filename,jobItemMeasures)
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

    private fun submitMeasures(
        itemMeasureJob: JobDTO,
        mSures: ArrayList<JobItemMeasureDTO>
    ) {
        Coroutines.main {
            val user = measureViewModel.user.await()
            user.observe(viewLifecycleOwner, Observer { user_ ->
                when {
                    user_.userId == null -> {
                        toast("Error: userId is null")
                    }
                    itemMeasureJob.JobId == null -> {
                        toast("Error: selectedJob is null")
                    }
                    else -> {
                        // TODO beware littleEndian conversion
                        if (mSures != null) {
                            for (jim in mSures) {
                                val newMeasures = setJobMeasureLittleEndianGuids(jim)
                                if (jim.actId == ActivityIdConstants.MEASURE_COMPLETE) {
                                    toast("Measurement ${jim.itemMeasureId} Already Submitted")
                                } else {
                                    jobItemMeasureList.add(newMeasures)
                                }

                            }
                        }
                        val contractVoId: String =
                            DataConversion.toLittleEndian(itemMeasureJob.ContractVoId)!!
                        val jobId: String = DataConversion.toLittleEndian(itemMeasureJob.JobId)!!

                        Coroutines.main {
                            val prog = setDataProgressDialog(
                                activity!!,
                                getString(R.string.loading_job_wait)
                            )
                            prog.show()
                            val submit = activity?.let {
                                measureViewModel.processWorkflowMove(
                                    user_.userId,
                                    jobId,
                                    itemMeasureJob.JiNo,
                                    contractVoId,
                                    jobItemMeasureList,
                                    it,
                                    itemMeasureJob
                                )
                            }
                            if (submit != null) {
                                prog.dismiss()
                                toast(submit)
                            } else {
                                prog.dismiss()
                                popViewOnJobSubmit()
                            }

                        }
                    }
                }
            })
        }
    }

    private fun setJobMeasureLittleEndianGuids(jim: JobItemMeasureDTO): JobItemMeasureDTO {
        if (jim != null) {
//            for (jim in jobItemMeasure) {
            jim.setEstimateId(DataConversion.toLittleEndian(jim.estimateId))
            jim.setJobId(DataConversion.toLittleEndian(jim.jobId))
            jim.setProjectItemId(DataConversion.toLittleEndian(jim.projectItemId))
            jim.setItemMeasureId(DataConversion.toLittleEndian(jim.itemMeasureId))

                if (jim.trackRouteId != null)
                    jim.setTrackRouteId(DataConversion.toLittleEndian(jim.trackRouteId))
                else jim.trackRouteId = null

            if (jim.measureGroupId != null)
            jim.setMeasureGroupId(DataConversion.toLittleEndian(jim.measureGroupId))

//                jim.setProjectVoId(DataConversion.toLittleEndian(jim.projectVoId))
            if (jim.jobItemMeasurePhotos != null) {
                for (jobItemPhoto in jim.jobItemMeasurePhotos) {
                    jobItemPhoto.setPhotoId(DataConversion.toLittleEndian(jobItemPhoto.photoId))
                    jobItemPhoto.setEstimateId(DataConversion.toLittleEndian(jobItemPhoto.estimateId))
                    jobItemPhoto.setItemMeasureId(DataConversion.toLittleEndian(jobItemPhoto.itemMeasureId))
                }
            }

//            }
        }

        return jim
    }



    private fun popViewOnJobSubmit() {
        // TODO("delete data from database after success upload")
        Intent(context?.applicationContext  , MainActivity::class.java).also { home ->
//            home.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(home)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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




//    private fun populateHashMap(jobItemEstimatesForJob: List<JobItemEstimateDTO>) {
//        Coroutines.main {
//            for (jobItemEstimate in jobItemEstimatesForJob) {
//                var jobItemMeasureArrayList: List<JobItemMeasureDTO> =
//                    ArrayList<JobItemMeasureDTO>()
//                if (jobItemEstimate.estimateId != null) {
//                    if (measureViewModel.checkIfJobItemMeasureExistsForJobIdAndEstimateId(
//                            jobItemEstimate.jobId,
//                            jobItemEstimate.estimateId
//                        )
//                    ) {
//                        jobItemMeasureArrayList =
//                            measureViewModel.getJobItemMeasuresForJobIdAndEstimateId(
//                                jobItemEstimate.jobId,
//                                jobItemEstimate.estimateId
//                            )
//                    }
//                }
//                jobItemMeasuresForJobItemEstimates[jobItemEstimate] = jobItemMeasureArrayList!!
//            }
//
//        }
//
//    }

    private fun getWorkItems(jobID: String?) {
        Coroutines.main {
            val measurements = measureViewModel.getJobItemsToMeasureForJobId(jobID)
            measurements.observe(viewLifecycleOwner, Observer { job_s ->
                initRecyclerView(job_s.toMeasureItem())
            })
        }
    }

    private fun initRecyclerView(toMeasureItem: List<ExpandableGroup>) {
        val groupAdapter = GroupAdapter<GroupieViewHolder>().apply {
            addAll(toMeasureItem)
        }
        measure_listView.apply {
            layoutManager = LinearLayoutManager(this.context)
            adapter = groupAdapter

        }


    }

    private fun List<JobItemEstimateDTO>.toMeasureItem(): List<ExpandableGroup> {
        return this.map { measure_item ->
            val expandableHeaderItem = ExpandableHeaderMeasureItem(
                activity,
                measure_item,
                measureViewModel,
                jobItemMeasurePhotoDTO,
                jobItemMeasureArrayList
            )
            ExpandableGroup(expandableHeaderItem, true).apply {

                Coroutines.main {
                    val jobForJobItemEstimate = measureViewModel.getJobFromJobId(measure_item.jobId)
                    jobForJobItemEstimate.observe(viewLifecycleOwner, androidx.lifecycle.Observer {job->
                        //                        for (measure_i in jobItemMeasureArrayList) {
                        jobForItemEstimate = job
                        Coroutines.main {
                            val jobItemMeasure =
                                measureViewModel.getJobItemMeasuresForJobIdAndEstimateId2(job.JobId, measure_item.estimateId)//, jobItemMeasureArrayList
                            jobItemMeasure.observe(activity!!, Observer { m_sures ->
                                Coroutines.main {
                                    for (jobItemM in m_sures) {
                                            Coroutines.main {
                                                val itemMeasureId = jobItemM.itemMeasureId!!
                                                val qty= jobItemM.qty.toString()
                                                val rate = jobItemM.lineRate.toString()
                                                val jNo = jobItemM.jimNo.toString()
                                                add(
                                                    CardMeasureItem(
                                                        activity,
                                                        itemMeasureId, qty, rate, jNo, measureViewModel
                                                    )
                                                )
                                            }

                                    }
                                }


                            })

                        }

                    })

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
        this.itemMeasureId =  toLittleEndian
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



