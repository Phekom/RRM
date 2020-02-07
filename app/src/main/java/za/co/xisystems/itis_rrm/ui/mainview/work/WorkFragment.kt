package za.co.xisystems.itis_rrm.ui.mainview.work

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.xwray.groupie.ExpandableGroup
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.fragment_approvejob.noData
import kotlinx.android.synthetic.main.fragment_work.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.ui.mainview._fragments.BaseFragment
import za.co.xisystems.itis_rrm.ui.mainview.work.estimate_work_item.CardItem
import za.co.xisystems.itis_rrm.ui.mainview.work.estimate_work_item.ExpandableHeaderWorkItem
import za.co.xisystems.itis_rrm.utils.ActivityIdConstants
import za.co.xisystems.itis_rrm.utils.Coroutines


const val INSET_TYPE_KEY = "inset_type"
const val INSET = "inset"

class WorkFragment : BaseFragment(), KodeinAware {

    override val kodein by kodein()
    private lateinit var workViewModel: WorkViewModel
    private val factory: WorkViewModelFactory by instance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_work, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        workViewModel = activity?.run {
            ViewModelProviders.of(this, factory).get(WorkViewModel::class.java)
        } ?: throw Exception("Invalid Activity") as Throwable
        Coroutines.main {
            //            mydata_loading.show()
            val works = workViewModel.getJobsForActivityId(ActivityIdConstants.JOB_APPROVED, ActivityIdConstants.ESTIMATE_INCOMPLETE)
//            val works = workViewModel.getJobsForActivityIds(ActivityIdConstants.JOB_APPROVED, ActivityIdConstants.ESTIMATE_INCOMPLETE)
//            val works = workViewModel.getJobsForActivityIds(ActivityIdConstants.ESTIMATE_INCOMPLETE)
//            val jobs = approveViewModel.offlinedata.await()
            works.observe(viewLifecycleOwner, Observer { work_s ->
                noData.visibility = View.GONE
                toast(work_s.size.toString())
                initRecyclerView(work_s.toWorkListItems())
                group7_loading.visibility = View.GONE
            })
        }


    }


    private fun initRecyclerView(
        workListItems: List<ExpandableGroup>
    ) {
        val groupAdapter = GroupAdapter<GroupieViewHolder>().apply {
            addAll(workListItems)
        }
        work_listView.apply {
            layoutManager = LinearLayoutManager(this.context)
            adapter = groupAdapter

        }

        groupAdapter.setOnItemClickListener { item, view ->

//            ToastUtils().toastShort(activity,
//                "Job Info: Start Km: ")

//            Coroutines.main {
//                                (item as? ExpandableHeaderItem)?.let {
//                //                    val descri = approveViewModel.getDescForProjectId(it.jobItemMeasureDTO.projectItemId!!)
////                    val sectionId  =  approveViewModel.getProjectSectionIdForJobId(it.jobDTO.jobId)
////                    val route  =  approveViewModel.getRouteForProjectSectionId(sectionId)
////                    val section  =  approveViewModel.getSectionForProjectSectionId(sectionId)
////                    sendJobtoAprove((it.jobItemMeasureDTO.jobId), view)
//
//
//                }
//
//            }
        }

    }


//    private fun sendJobtoAprove(
//        jobId: String?,
//        view: View
//    ) {
//        val jobId = jobId.toString()
//        Coroutines.main {
//            //            workViewModel.measure_Item.value = jobId
//        }
//
//        Navigation.findNavController(view)
//            .navigate(R.id.action_nav_approvMeasure_to_measureApprovalFragment)
//    }

//    private fun List<JobItemEstimateDTO>.toWorkListItems(): List<ExpandableGroup> {
    private fun List<JobDTO>.toWorkListItems(): List<ExpandableGroup> {
        //Initialize Expandable group with expandable item and specify whether it should be expanded by default or not

        return this.map { work_items ->

//                                    if (work_items.StartKm <= work_items.EndKm) {
//                                        toast("Job Info: Start Km: " + work_items.StartKm.toString() + " - End Km: " + work_items.EndKm)
//                                    } else if (null == work_items.TrackRouteId) {
//                                       toast("Job not found please click on item to download job.")
//                                    }

            val expandableHeaderItem =
            ExpandableHeaderWorkItem( activity, work_items, workViewModel, work_items.JobId)
//            ExpandableHeaderItem("JI:${work_items.JiNo} ", work_items.Descr!! , activity, work_items, workViewModel)
            ExpandableGroup(expandableHeaderItem, false).apply {
                Coroutines.main {
                    val estimates = workViewModel.getJobEstimationItemsForJobId(work_items.JobId)
                    estimates.observe(viewLifecycleOwner, Observer { i_tems ->
                     val estimateId = arrayOfNulls<String>(i_tems.size)
                     val Desc = arrayOfNulls<String>(i_tems.size)
                     val qty = arrayOfNulls<String>(i_tems.size)
                     val rate = arrayOfNulls<String>(i_tems.size)
                        for (i in i_tems.indices) {
                            Coroutines.main {
                                Desc[i] =
                                    workViewModel?.getDescForProjectItemId(i_tems[i].projectItemId!!)
                                qty[i] = i_tems[i].qty.toString()
                                rate[i] = i_tems[i].lineRate.toString()
                                estimateId[i] = i_tems[i].estimateId
                                add(CardItem( activity,Desc[i]!!, qty[i]!!, rate[i]!!,estimateId[i]!!, workViewModel))
                            }
                        }

                    })

                }
            }
        }

    }


}