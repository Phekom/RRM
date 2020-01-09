package za.co.xisystems.itis_rrm.ui.mainview.work

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.xwray.groupie.ExpandableGroup
import com.xwray.groupie.Group
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
import za.co.xisystems.itis_rrm.ui.mainview.work.estimate_work_item.ExpandableHeaderItem
import za.co.xisystems.itis_rrm.utils.ActivityIdConstants
import za.co.xisystems.itis_rrm.utils.Coroutines


const val INSET_TYPE_KEY = "inset_type"
const val INSET = "inset"

class WorkFragment : BaseFragment(), KodeinAware {

    override val kodein by kodein()
    private lateinit var workViewModel: WorkViewModel
    private val factory: WorkViewModelFactory by instance()
    private val rainbow200: IntArray by lazy { resources.getIntArray(R.array.rainbow_200) }
    val allGroups = mutableListOf<Group>()


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
            val works = workViewModel.getJobsForActivityId(
                ActivityIdConstants.JOB_APPROVED,
                ActivityIdConstants.ESTIMATE_INCOMPLETE
            )
//            val jobs = approveViewModel.offlinedata.await()
            works.observe(viewLifecycleOwner, Observer { work_s ->
                noData.visibility = View.GONE
                toast(work_s.size.toString())
                initRecyclerView(work_s.toWorkListItems())
                group7_loading.visibility = View.GONE
            })
        }


    }


    private fun initRecyclerView(approveMeasureListItems: List<ExpandableGroup>) {
        val groupAdapter = GroupAdapter<GroupieViewHolder>().apply {
            addAll(approveMeasureListItems)
        }
        work_listView.apply {
            layoutManager = LinearLayoutManager(this.context)
            adapter = groupAdapter

        }

//        groupAdapter.setOnItemClickListener { item, view ->
//            Coroutines.main {
//                                (item as? EstimateWork_Item)?.let {
//                //                    val descri = approveViewModel.getDescForProjectId(it.jobItemMeasureDTO.projectItemId!!)
////                    val sectionId  =  approveViewModel.getProjectSectionIdForJobId(it.jobDTO.jobId)
////                    val route  =  approveViewModel.getRouteForProjectSectionId(sectionId)
////                    val section  =  approveViewModel.getSectionForProjectSectionId(sectionId)
////                    sendJobtoAprove((it.jobItemMeasureDTO.jobId), view)
//                }
//
//            }
//        }

    }


    private fun sendJobtoAprove(
        jobId: String?,
        view: View
    ) {
        val jobId = jobId.toString()
        Coroutines.main {
            //            workViewModel.measure_Item.value = jobId
        }

        Navigation.findNavController(view)
            .navigate(R.id.action_nav_approvMeasure_to_measureApprovalFragment)
    }


    private fun List<JobDTO>.toWorkListItems(): List<ExpandableGroup> {
        //Initialize Expandable group with expandable item and specify whether it should be expanded by default or not

        return this.map { work_items ->
                        Coroutines.main {
                val sectionId  =  workViewModel?.getProjectSectionIdForJobId(work_items.JobId)
                val route  =  workViewModel?.getRouteForProjectSectionId(sectionId!!)
                val section  =  workViewModel?.getSectionForProjectSectionId(sectionId!!)

                val sectionRoute  =  " ( ${route} ${"/0$section"} )"

                        }

            val sectionRoute = "       GEt Route Here"
            val expandableHeaderItem =
                ExpandableHeaderItem("JI:${work_items.JiNo} ", work_items.Descr!! + sectionRoute)
            ExpandableGroup(expandableHeaderItem, false).apply {
                Coroutines.main {
                    val estimates = workViewModel.getJobEstimationItemsForJobId(work_items.JobId)

                    estimates.observe(viewLifecycleOwner, Observer { i_tems ->

                     val Desc = arrayOfNulls<String>(i_tems.size)
                     val qty = arrayOfNulls<String>(i_tems.size)
                     val rate = arrayOfNulls<String>(i_tems.size)
                        for (i in i_tems.indices) {
                            Coroutines.main {
                                Desc[i] =
                                    workViewModel?.getDescForProjectItemId(i_tems[i].projectItemId!!)
                                qty[i] = i_tems[i].qty.toString()
                                rate[i] = i_tems[i].lineRate.toString()
                                add(CardItem(Desc[i]!!, qty[i]!!, rate[i]!!))
                            }
                        }

                    })

                }
            }
        }

    }


}