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
import za.co.xisystems.itis_rrm.data.localDB.entities.*
import za.co.xisystems.itis_rrm.ui.mainview._fragments.BaseFragment
import za.co.xisystems.itis_rrm.ui.mainview.estmeasure.MeasureViewModel
import za.co.xisystems.itis_rrm.ui.mainview.estmeasure.MeasureViewModelFactory
import za.co.xisystems.itis_rrm.utils.Coroutines
import za.co.xisystems.itis_rrm.utils.DataConversion
import za.co.xisystems.itis_rrm.utils.ServiceUtil
import java.util.*


class SubmitMeasureFragment : BaseFragment(R.layout.fragment_submit_measure), KodeinAware {
    override val kodein by kodein()
    private lateinit var measureViewModel: MeasureViewModel
    private val factory: MeasureViewModelFactory by instance<MeasureViewModelFactory>()
    private lateinit var jobItemMeasurePhotoDTO: ArrayList<JobItemMeasurePhotoDTO>
    private lateinit var jobItemMeasureArrayList: ArrayList<JobItemMeasureDTO>
    private lateinit var jobItemEstimatesForJob: ArrayList<JobItemEstimateDTO>
    private lateinit var jobItemMeasuresForJobItemEstimates: HashMap<JobItemEstimateDTO, List<JobItemMeasureDTO>>
    private lateinit var jobForItemEstimate: JobDTO
    private lateinit var jobItemMeasureList: ArrayList<JobItemMeasureDTO>
    private lateinit var jobItemEstimate: JobItemEstimateDTO

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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        measureViewModel = activity?.run {
            ViewModelProvider(this, factory).get(MeasureViewModel::class.java)
        } ?: throw Exception("Invalid Activity")

        Coroutines.main {
            jobItemMeasurePhotoDTO = ArrayList<JobItemMeasurePhotoDTO>()
            jobItemMeasureArrayList = ArrayList<JobItemMeasureDTO>()
            jobItemEstimatesForJob = ArrayList<JobItemEstimateDTO>()
            jobItemMeasureList = ArrayList<JobItemMeasureDTO>()

            jobItemMeasuresForJobItemEstimates =
                HashMap<JobItemEstimateDTO, List<JobItemMeasureDTO>>()

            measureViewModel.measure_Item.observe(viewLifecycleOwner, Observer { jobID ->
                jobItemEstimate = jobID.jobItemEstimateDTO
                getWorkItems(jobItemEstimate.jobId)
            })


            submit_measurements_button.setOnClickListener {
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
                    measureViewModel.measure_Item.observe(
                        viewLifecycleOwner,
                        Observer { measureItem ->
                            getWorkItems(measureItem.jobItemEstimateDTO.jobId)
                            items_swipe_to_refresh.isRefreshing = false

                        })

                }
            }

        }

    }

    private fun submitMeasurements(jobId: String?) {
        Coroutines.main {
            val jobItemMeasure =
                measureViewModel.getJobItemMeasuresForJobIdAndEstimateId(jobId) //estimateId
            jobItemMeasure.observe(requireActivity(), Observer { m_sures ->
                if (m_sures.isNullOrEmpty() || m_sures.isEmpty()) {
                    toast(R.string.please_make_sure_you_have_captured_photos)
                } else {
                    toast("You have Done " + m_sures.size.toString() + " Measurements on this Estimate")

                    val itemMeasures = m_sures as ArrayList
                    for (jim in itemMeasures) {
                        val newJim = setJobMeasureLittleEndianGuids(jim)!!
                        jobItemMeasureList.add(newJim)
                    }
                    submitJobToMeasurements(jobForItemEstimate, jobItemMeasureList)
                }
            })
        }
    }

    private fun submitJobToMeasurements(
        itemMeasureJob: JobDTO,
        mSures: ArrayList<JobItemMeasureDTO>

    ) {
        val logoutBuilder = AlertDialog.Builder(
            requireActivity() //, android.R.style.Theme_DeviceDefault_Dialog
        )
        logoutBuilder.run {
            setTitle(R.string.confirm)
            setIcon(R.drawable.ic_approve)
            setMessage(R.string.are_you_sure_you_want_to_submit_measurements)
            // Yes button
            setPositiveButton(R.string.yes) { dialog, which ->
                if (ServiceUtil.isNetworkConnected(context?.applicationContext)) {
                    submitMeasures(itemMeasureJob, mSures)
                } else {
                    toast(R.string.no_connection_detected)
                }
            }
            // No button
            setNegativeButton(
                R.string.no
            ) { dialog, which ->
                // Do nothing but close dialog
                dialog.dismiss()
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
            user.observe(viewLifecycleOwner, Observer { user_ ->
                if (user_.userId == null) {
                    toast("Error: userId is null")
                } else if (itemMeasureJob.JobId == null) {
                    toast("Error: selectedJob is null")
                } else {
                    // beware littleEndian conversion for transport to Ser
                    val ContractVoId: String =
                        DataConversion.toLittleEndian(itemMeasureJob.ContractVoId)!!
                    val JobId: String = DataConversion.toLittleEndian(itemMeasureJob.JobId)!!

                    Coroutines.main {
                        val prog =
                            setDataProgressDialog(
                                requireActivity(),
                                getString(R.string.loading_job_wait)
                            )
                        prog.show()
                        val submit = activity?.let {
                            measureViewModel.processWorkflowMove(
                                user_.userId, JobId, itemMeasureJob.JiNo, ContractVoId, mSures,
                                it, itemMeasureJob
                            )

                        }
                        prog.dismiss()
                        if (submit != null) {
                            toast(submit)
                        } else {
                            toast(R.string.measure_submitted)
//                            sendJobToApprove((itemMeasureJob), view)
                            popViewOnJobSubmit()
                        }


                    }
                }

            })
        }
    }

//    private fun sendJobToApprove(
//        itemMeasureJob: JobDTO,
//        view: View?
//    ) {
//        Coroutines.main {
//            approveViewModel.measurea1_Job.value = itemMeasureJob
//        }
//
//    }

    fun setJobMeasureLittleEndianGuids(jim: JobItemMeasureDTO?): JobItemMeasureDTO? {
        if (jim != null) {
            jim.setEstimateId(DataConversion.toLittleEndian(jim.estimateId))
            jim.setJobId(DataConversion.toLittleEndian(jim.jobId))
            jim.setProjectItemId(DataConversion.toLittleEndian(jim.projectItemId))
            jim.setItemMeasureId(DataConversion.toLittleEndian(jim.itemMeasureId))

            if (jim.trackRouteId != null)
                jim.setTrackRouteId(DataConversion.toLittleEndian(jim.trackRouteId))
            else jim.trackRouteId = null

            if (jim.measureGroupId != null)
                jim.setMeasureGroupId(DataConversion.toLittleEndian(jim.measureGroupId))


            if (jim.jobItemMeasurePhotos != null) {
                for (jmep in jim.jobItemMeasurePhotos) {
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


    private fun getWorkItems(jobID: String?) {
        Coroutines.main {
            val measurements = measureViewModel.getJobItemsToMeasureForJobId(jobID)
            measurements.observe(viewLifecycleOwner, Observer { job_s ->
                initRecyclerView(job_s.toMeasure_Item())
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

        measure_listView.adapter = null
        super.onDestroyView()
    }

    private fun List<JobItemEstimateDTO>.toMeasure_Item(): List<ExpandableGroup> {
        return this.map { measure_item ->
            val expandableHeaderItem = ExpandableHeaderMeasureItem(
                activity,
                measure_item,
                measureViewModel,
                jobItemMeasurePhotoDTO,
                jobItemMeasureArrayList,
                jobItemEstimatesForJob,
                jobItemMeasuresForJobItemEstimates
            )
            ExpandableGroup(expandableHeaderItem, true).apply {

                Coroutines.main {
                    val jobForJobItemEstimate = measureViewModel.getJobFromJobId(measure_item.jobId)
                    jobForJobItemEstimate.observe(
                        viewLifecycleOwner,
                        androidx.lifecycle.Observer { job ->
                            //                        for (measure_i in jobItemMeasureArrayList) {
                            jobForItemEstimate = job
                            Coroutines.main {
                                val jobItemMeasure =
                                    measureViewModel.getJobItemMeasuresForJobIdAndEstimateId2(
                                        job.JobId,
                                        measure_item.estimateId
                                    )
                                jobItemMeasure.observe(requireActivity(), Observer { m_sures ->
                                    Coroutines.main {
                                        for (jobItemM in m_sures) {
                                            Coroutines.main {
                                                val itemMeasureId = jobItemM.itemMeasureId!!
                                                val qty = jobItemM.qty.toString()
                                                val rate = jobItemM.lineRate.toString()
                                                val jNo = jobItemM.jimNo.toString()
                                                add(
                                                    CardMeasureItem(
                                                        activity,
                                                        itemMeasureId,
                                                        qty,
                                                        rate,
                                                        jNo,
                                                        measureViewModel
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

    private fun JobItemMeasureDTO.setProjectVoId(toLittleEndian: String?) {
        this.projectVoId = toLittleEndian
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

    private fun JobItemEstimatesPhotoDTO.setPhotoId(toLittleEndian: String?) {
        this.photoId = toLittleEndian!!
    }

    private fun JobEstimateWorksPhotoDTO.setWorksId(toLittleEndian: String?) {
        this.worksId = toLittleEndian!!
    }

    private fun JobItemMeasurePhotoDTO.setEstimateId(toLittleEndian: String?) {
        this.estimateId = toLittleEndian
    }

    private fun JobItemMeasureDTO.setItemMeasureId(toLittleEndian: String?) {
        this.itemMeasureId = toLittleEndian
    }


}



