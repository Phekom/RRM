package za.co.xisystems.itis_rrm.ui.mainview.approvejobs.view_job_info

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import kotlinx.android.synthetic.main.fragment_job_info.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import za.co.xisystems.itis_rrm.MainActivity
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimateDTO
import za.co.xisystems.itis_rrm.ui.mainview._fragments.BaseFragment
import za.co.xisystems.itis_rrm.ui.mainview.approvejobs.ApproveJobsViewModel
import za.co.xisystems.itis_rrm.ui.mainview.approvejobs.ApproveJobsViewModelFactory
import za.co.xisystems.itis_rrm.utils.Coroutines
import za.co.xisystems.itis_rrm.utils.hide
import za.co.xisystems.itis_rrm.utils.show


class JobInfoFragment : BaseFragment(), KodeinAware {
    override val kodein by kodein()
    private lateinit var approveViewModel: ApproveJobsViewModel
    private val factory: ApproveJobsViewModelFactory by instance()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (activity as MainActivity).supportActionBar?.title = getString(R.string.jobinfo_item_title)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity as MainActivity).supportActionBar?.title = getString(R.string.jobinfo_item_title)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_job_info, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        approveViewModel = activity?.run {
            ViewModelProviders.of(this, factory).get(ApproveJobsViewModel::class.java)
        } ?: throw Exception("Invalid Activity") as Throwable
        Coroutines.main {
            mydata_loading.show()

            approveViewModel.jobapproval_Item5.observe(viewLifecycleOwner, Observer { jobID ->
                getEstimateItems(jobID)
            })
        }

        approveViewModel.jobapproval_Item1.observe(viewLifecycleOwner, Observer { pro_Item ->
            project_description_textView.text = pro_Item
        })
        approveViewModel.jobapproval_Item2.observe(viewLifecycleOwner, Observer { pro_Item ->
            section_description_textView.text = pro_Item
        })
        approveViewModel.jobapproval_Item3.observe(viewLifecycleOwner, Observer { pro_Item ->
            start_km_description_textView.text = pro_Item
        })
        approveViewModel.jobapproval_Item4.observe(viewLifecycleOwner, Observer { pro_Item ->
            end_km_description_textView.text = pro_Item
        })
    }

    private fun getEstimateItems(jobID: String?) {
        Coroutines.main {
        val estimates = approveViewModel.getJobEstimationItemsForJobId(jobID)
            estimates.observe(viewLifecycleOwner, Observer { job_s ->
                mydata_loading.hide()
                initRecyclerView(job_s.toEstimates_Item())

            })

        }
    }

    private fun initRecyclerView(estimatesListItems: List<Estimates_Item>) {
        val groupAdapter = GroupAdapter<GroupieViewHolder>().apply {
            addAll(estimatesListItems)
        }
        view_estimation_items_listView.apply {
            layoutManager = LinearLayoutManager(this.context)
            adapter = groupAdapter

        }

    }

    private fun List<JobItemEstimateDTO>.toEstimates_Item(): List<Estimates_Item> {
        return this.map { approvej_items ->
//            getEstimateItemsPhoto(approvej_items.estimateId)
            Estimates_Item(approvej_items,approveViewModel, activity)
        }
    }

//    private fun getEstimateItemsPhoto(estimateId: String) {
//        Coroutines.main {
//            val estimatesphoto = approveViewModel.getJobEstimationItemsPhoto(estimateId)
//        }
//    }
}
