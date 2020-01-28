package za.co.xisystems.itis_rrm.ui.mainview.estmeasure.submit_measure

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.xwray.groupie.ExpandableGroup
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.fragment_submit_measure.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import za.co.xisystems.itis_rrm.MainActivity
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.data.localDB.JobDataController
import za.co.xisystems.itis_rrm.data.localDB.entities.*
import za.co.xisystems.itis_rrm.ui.mainview._fragments.BaseFragment
import za.co.xisystems.itis_rrm.ui.mainview.estmeasure.MeasureViewModel
import za.co.xisystems.itis_rrm.ui.mainview.estmeasure.MeasureViewModelFactory
import za.co.xisystems.itis_rrm.utils.Coroutines
import za.co.xisystems.itis_rrm.utils.DataConversion
import za.co.xisystems.itis_rrm.utils.PhotoUtil
import za.co.xisystems.itis_rrm.utils.ServiceUtil
import za.co.xisystems.itis_rrm.utils.enums.PhotoQuality
import java.util.*


class SubmitMeasureFragment : BaseFragment(), KodeinAware {
    override val kodein by kodein()
    private lateinit var measureViewModel: MeasureViewModel
    private val factory: MeasureViewModelFactory by instance()
    private val jobItemMeasure: JobItemMeasureDTO? = null
    private val jobDataController: JobDataController? = null
    private lateinit var jobItemMeasurePhotoDTO: ArrayList<JobItemMeasurePhotoDTOTemp>
    private lateinit var jobItemMeasureArrayList: ArrayList<JobItemMeasureDTOTemp>
    private lateinit var jobItemEstimatesForJob: ArrayList<JobItemEstimateDTO>
    private lateinit var jobItemMeasuresForJobItemEstimates: HashMap<JobItemEstimateDTO, List<JobItemMeasureDTO>>
    private lateinit var jobForJobItemEstimate: JobDTO


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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
            ViewModelProviders.of(this, factory).get(MeasureViewModel::class.java)
        } ?: throw Exception("Invalid Activity") as Throwable

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
            ViewModelProviders.of(this, factory).get(MeasureViewModel::class.java)
        } ?: throw Exception("Invalid Activity") as Throwable

        Coroutines.main {
            jobItemMeasurePhotoDTO = ArrayList<JobItemMeasurePhotoDTOTemp>()
            jobItemMeasureArrayList = ArrayList<JobItemMeasureDTOTemp>()
            jobItemEstimatesForJob = ArrayList<JobItemEstimateDTO>()

            jobItemMeasuresForJobItemEstimates =
                HashMap<JobItemEstimateDTO, List<JobItemMeasureDTO>>()

            measureViewModel.measure_Item.observe(viewLifecycleOwner, Observer { jobID ->
                getWorkItems(jobID.jobItemEstimateDTO.jobId)
//                populateHashMap(jobItemEstimatesForJob)
                submit_measurements_button.setOnClickListener {
                    submitMeasurements(
                        jobID.jobItemEstimateDTO.jobId,
                        jobID.jobItemEstimateDTO.estimateId
                    )


                }
            })


            items_swipe_to_refresh.setProgressBackgroundColorSchemeColor(
                ContextCompat.getColor(
                    context!!.applicationContext,
                    R.color.colorPrimary
                )
            )
            items_swipe_to_refresh.setColorSchemeColors(Color.WHITE)

            items_swipe_to_refresh.setOnRefreshListener {
                Coroutines.main {
                    measureViewModel.measure_Item.observe(viewLifecycleOwner, Observer { jobID ->
                        getWorkItems(jobID.jobItemEstimateDTO.jobId)
                        items_swipe_to_refresh.isRefreshing = false


                    })
//                    val jobItemMeasure = measureViewModel.getJobItemMeasuresForJobIdAndEstimateId2(it.JobId, measure_item.estimateId,jobItemMeasureArrayList)
//                    jobItemMeasure.observe(activity!!, Observer { m_sures ->
//
//                    })
                }
            }


        }


    }

    private fun submitMeasurements(jobId: String?, estimateId: String) {
        Coroutines.main {
            var imageCounter = 0
            val totalImages = 0
            val jobItemMeasure =
                measureViewModel.getJobItemMeasuresForJobIdAndEstimateId(jobId, estimateId)
            jobItemMeasure.observe(activity!!, Observer { m_sures ->
                if (m_sures.isNullOrEmpty()) {
                    toast(R.string.please_make_sure_you_have_captured_photos)
                } else {
                    toast("You have Done " + m_sures.size.toString() + " Measurements on this Estimate")
//                    val measurePhotos = measureViewModel.getJobItemMeasurePhotosForItemMeasureID(itemMeasure.itemMeasureId!!)
                    for (jobItemMeasures in m_sures) { Coroutines.main {
                        val measurePhotos =
                            measureViewModel.getJobItemMeasurePhotosForItemEstimateID(jobItemMeasures.estimateId!!)
                        measurePhotos.observe(activity!!, Observer { photos ->
                            for (jobItemMeasurePhoto in photos) {
                                var itemMeasures = m_sures as ArrayList
                                if (PhotoUtil.photoExist(jobItemMeasurePhoto.filename!!))

                                //  Add to Counter Specific Photo
                                submitJobToMeasurements(jobItemMeasures.job!!, itemMeasures,jobItemMeasurePhoto.filename,jobItemMeasures)
//                                imageCounter++

                            }
                            toast("And " + photos.size.toString() + "  Images")


                        })
                    }
                }


                }

//                                if (itemMeasure.job != null && itemMeasure.jobItemMeasurePhotos.isNullOrEmpty()) {

//                                }


            })
        }
    }

    private fun uploadRrmImage(filename: String, jobItemMeasures: JobItemMeasureDTOTemp) {
//        val messages = arrayOf(getString(R.string.uploading_photo) + imageCounter + getString(R.string.new_line) + getString(R.string.please_wait))
        val bitmap = PhotoUtil.getPhotoBitmapFromFile(activity!!.applicationContext,
            PhotoUtil.getPhotoPathFromExternalDirectory(activity!!.applicationContext, filename),
            PhotoQuality.HIGH
        )
        val photo = PhotoUtil.getCompressedPhotoWithExifInfo(activity!!.applicationContext, bitmap!!, filename)
        processImageUpload(filename, activity!!.getString(R.string.jpg),photo)

    }

    private fun processImageUpload(
        filename: String,
        extension: String,
        photo: ByteArray
    ) {
        Coroutines.main {
            measureViewModel.processImageUpload(filename,extension ,photo)
            activity?.hideKeyboard()
            popViewOnJobSubmit()
        }
    }

    private fun submitJobToMeasurements(
        itemMeasureJob: JobDTO,
        mSures: ArrayList<JobItemMeasureDTOTemp>,
        filename: String,
        jobItemMeasures: JobItemMeasureDTOTemp
    ) {
        val logoutBuilder = AlertDialog.Builder(
            activity //,android.R.style.Theme_DeviceDefault_Dialog
        )
        logoutBuilder.setTitle(R.string.confirm)
        logoutBuilder.setIcon(R.drawable.ic_approve)
        logoutBuilder.setMessage(R.string.are_you_sure_you_want_to_submit_measurements)

        // Yes button
        logoutBuilder.setPositiveButton(
            R.string.yes
        ) { dialog, which ->
            if (ServiceUtil.isNetworkConnected(context?.applicationContext)) {
                submiteMeasures(itemMeasureJob,mSures)
                uploadRrmImage(filename,jobItemMeasures)
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

    private fun submiteMeasures(
        itemMeasureJob: JobDTO,
        mSures: ArrayList<JobItemMeasureDTOTemp>
    ) {
        Coroutines.main {
            val user = measureViewModel.user.await()
            user.observe(viewLifecycleOwner, Observer { user_ ->
                //            measureViewModel.jobapproval_Item6.observe(viewLifecycleOwner, Observer { job ->

                if (user_.userId == null) {
                    toast("Error: userId is null")
                } else if (itemMeasureJob.JobId == null) {
                    toast("Error: selectedJob is null")
                } else {
//                    // TODO beware littlEndian conversion
                    val ContractVoId: String =
                        DataConversion.toLittleEndian(itemMeasureJob.ContractVoId)!!
//                    processWorkFlow(
//                        user_.userId,
//                        itemMeasureJob.JobId!!, itemMeasureJob.JiNo, ContractVoId, mSures
//                    )


                    Coroutines.main {
                        measureViewModel.processWorkflowMove( user_.userId, itemMeasureJob.JobId!!, itemMeasureJob.JiNo, ContractVoId, mSures)
//            activity?.hideKeyboard()
//            popViewOnJobSubmit()
                    }
                }

//            })
            })
        }
    }


    private fun popViewOnJobSubmit() {
        // TODO("delete datat from database after sucess upload")
        Intent(context, MainActivity::class.java).also { home ->
            home.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(home)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).supportActionBar?.title =
            getString(R.string.submit_measure_title)

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
                //                mydata_loading.hide()
//                toast(job_s.size.toString())
                initRecyclerView(job_s.toMeasure_Item())
//
            })


        }
    }

    private fun initRecyclerView(tomeasureItem: List<ExpandableGroup>) {
        val groupAdapter = GroupAdapter<GroupieViewHolder>().apply {
            addAll(tomeasureItem)
        }
        estimates_to_be_approved_listView.apply {
            layoutManager = LinearLayoutManager(this.context) as RecyclerView.LayoutManager?
            adapter = groupAdapter

        }


    }
//   override fun onDestroyView() {
//        super.onDestroyView()
//        if (view != null) {
//            val parent = view!!.parent as ViewGroup
//            parent?.removeAllViews()
//        }
//    }
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
                    jobForJobItemEstimate.observe(activity!!, androidx.lifecycle.Observer {
                        //                        for (measure_i in jobItemMeasureArrayList) {
                        Coroutines.main {
                            val jobItemMeasure =
                                measureViewModel.getJobItemMeasuresForJobIdAndEstimateId2(
                                    it.JobId,
                                    measure_item.estimateId,
                                    jobItemMeasureArrayList
                                )
                            jobItemMeasure.observe(activity!!, Observer { m_sures ->
                                //                                    for (i in m_sures.indices) {
//                                        toast(m_sures.size.toString())
//                                        add(CardMeasureItem(activity,"","","", "", measureViewModel))
//                                    }
                                val jNo = arrayOfNulls<String>(m_sures.size)
                                val photo = arrayOfNulls<String>(m_sures.size)
                                val qty = arrayOfNulls<String>(m_sures.size)
                                val rate = arrayOfNulls<String>(m_sures.size)
                                for (i in m_sures.indices) {
                                    Coroutines.main {
                                        photo[i] = m_sures[i].itemMeasureId!!
                                        qty[i] = m_sures[i].qty.toString()
                                        rate[i] = m_sures[i].lineRate.toString()
                                        jNo[i] = m_sures[i].jimNo
                                        add(
                                            CardMeasureItem(
                                                activity,
                                                photo[i]!!,
                                                qty[i]!!,
                                                rate[i]!!,
                                                jNo[i]!!,
                                                measureViewModel
                                            )
                                        )
                                    }
                                }

                            })

                        }

//                        }


                    })

                }
//

            }
        }


    }


}


//        }
//    }
//    private fun List<JobItemEstimateDTO>.toMeasure_Item(): List<ExpandableGroup> {
//        return this.map { measure_items ->
//            EstimateMeasure_Item(measure_items,measureViewModel)
//        }
//    }


//}


//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        when (requestCode) {
//            PHOTO_RESULT -> {
//                if (resultCode == Activity.RESULT_OK) {
//                    if (data != null) {
//                        val jobItemMeasurePhotoArrayList: ArrayList<JobItemMeasurePhotoDTO>
//                        jobItemMeasurePhotoArrayList =
//                            data.extras[CaptureItemMeasurePhotoFragment.JOB_ITEM_MEASURE_PHOTO_ARRAY_LIST] as ArrayList<JobItemMeasurePhotoDTO>
//                        for (jobItemMeasure in jobItemMeasureArrayList) {
//                            if (jobItemMeasure.equals(this.jobItemMeasure)) this.jobItemMeasure.jobItemMeasurePhotos = jobItemMeasurePhotoArrayList
//                        }
//
//                        jobForJobItemEstimate.observe(this, Observer {
//                            it.JobItemMeasures = jobItemMeasureArrayList
//                        })
//                        setJobItemMeasureInHashMap(jobItemMeasureArrayList)
//                    }
//
//                }
//            }
//        }
//    }
//
//    private fun setJobItemMeasureInHashMap(jobItemMeasures: ArrayList<JobItemMeasureDTO>) {
//        if (jobItemMeasuresForJobItemEstimates != null) {
//            jobItemMeasuresForJobItemEstimates = HashMap<JobItemEstimateDTO, ArrayList<JobItemMeasureDTO>>()
//            jobItemMeasuresForJobItemEstimates[selectedJobItemEstimate] =
//                getJobItemMeasurementsForJobItemEstimate(
//                    selectedJobItemEstimate.getEstimateId(),
//                    jobItemMeasures
//                )
//            repopulateHashMap(selectedJobItemEstimate, jobItemEstimatesForJob)
//        }
//        estimationMeasurementExpandableListAdapter.swapItems(
//            jobItemEstimatesForJob,
//            jobItemMeasuresForJobItemEstimates
//        )
//    }
//


//


//                        val jobItemMeasurePhotos = measureViewModel.getJobItemMeasurePhotosForItemMeasureID(jobItemMeasurePhoto.itemMeasureId!!)
//                        jobItemMeasurePhotos.observe(activity!!, Observer { m_photos ->  })
//                            if (m_photos != null) { toast(m_photos.size.toString())
////                                    if (m_photos.size > 0) jobItemMeasurePhotoArrayList = m_photos as ArrayList<JobItemMeasurePhotoDTOTemp> } else {
////                                sendImagestoHeader(selectedJobItemMeasure,m_photos, view!!)
//
//                            }


//    }
//
//    // TODO: Rename method, update argument and hook method into UI event
//    fun onButtonPressed(uri: Uri) {
////        listener?.onFragmentInteraction(uri)
//    }
//
//
//
//    override fun onDetach() {
//        super.onDetach()
////        listener = null
//    }
//
//
//}



