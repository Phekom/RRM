package za.co.xisystems.itis_rrm.ui.mainview.approvejobs

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.fragment_approvejob.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.ui.mainview._fragments.BaseFragment
import za.co.xisystems.itis_rrm.ui.mainview.approvejobs.approve_job_item.ApproveJob_Item
import za.co.xisystems.itis_rrm.utils.ActivityIdConstants
import za.co.xisystems.itis_rrm.utils.Coroutines

/**
 * Created by Francis Mahlava on 03,October,2019
 */

class ApproveJobsFragment : BaseFragment(), KodeinAware {

    override val kodein by kodein()
    private lateinit var approveViewModel: ApproveJobsViewModel
    private val factory: ApproveJobsViewModelFactory by instance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_approvejob, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        approveViewModel = activity?.run {
            ViewModelProviders.of(this, factory).get(ApproveJobsViewModel::class.java)
        } ?: throw Exception("Invalid Activity") as Throwable
        Coroutines.main {
            //            mydata_loading.show()
            val jobs = approveViewModel.getJobsForActivityId(ActivityIdConstants.JOB_APPROVE)
//            val jobs = approveViewModel.offlinedata.await()
            jobs.observe(viewLifecycleOwner, Observer { job_s ->
                noData.visibility = GONE
                initRecyclerView(job_s.toApproveListItems())
                group3_loading.visibility = View.GONE
            })
        }
    }

    private fun initRecyclerView(approveJobListItems: List<ApproveJob_Item>) {
        val groupAdapter = GroupAdapter<GroupieViewHolder>().apply {
            addAll(approveJobListItems)
        }
        approve_job_listView.apply {
            layoutManager = LinearLayoutManager(this.context)
            adapter = groupAdapter

        }

        groupAdapter.setOnItemClickListener { item, view ->
            Coroutines.main {
                (item as? ApproveJob_Item)?.let {
                    val descri = approveViewModel.getDescForProjectId(it.jobDTO.ProjectId!!)
                    val sectionId = approveViewModel.getProjectSectionIdForJobId(it.jobDTO.JobId)
                    val route = approveViewModel.getRouteForProjectSectionId(sectionId)
                    val section = approveViewModel.getSectionForProjectSectionId(sectionId)
//                sendJobtoAprove((it.jobDTO.JobId),(descri), ("$route/ $section"),(it.jobDTO.StartKm),(it.jobDTO.EndKm ), view
                    sendJobtoAprove((it), view)
                }
            }
        }
    }

    private fun sendJobtoAprove(
        job: ApproveJob_Item?,
//        jobId: String?,
//        descr: String?,
//        section: String?,
//        startKm: Double,
//        endKm: Double,
        view: View
    ) {
        val job = job
//        val jobId = jobId.toString()
//        val descr = descr.toString()
//        val section = section.toString()
//        val startKm = startKm.toString()
//        val endKm = endKm.toString()
        Coroutines.main {
            //            approveViewModel.jobapproval_Item1.value = descr
//            approveViewModel.jobapproval_Item2.value = section
//            approveViewModel.jobapproval_Item3.value = startKm
//            approveViewModel.jobapproval_Item4.value = endKm
//            approveViewModel.jobapproval_Item5.value = jobId
            approveViewModel.jobapproval_Item6.value = job
        }

        Navigation.findNavController(view)
            .navigate(R.id.action_nav_approveJbs_to_jobInfoFragment)
    }

    private fun List<JobDTO>.toApproveListItems(): List<ApproveJob_Item> {
        return this.map { approvej_items ->
            ApproveJob_Item(approvej_items, approveViewModel)
        }
    }

}



