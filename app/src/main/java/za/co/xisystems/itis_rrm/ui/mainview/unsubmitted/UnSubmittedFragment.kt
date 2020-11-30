package za.co.xisystems.itis_rrm.ui.mainview.unsubmitted

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import kotlinx.android.synthetic.main.fragment_unsubmittedjobs.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import timber.log.Timber
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.base.BaseFragment
import za.co.xisystems.itis_rrm.custom.errors.XIErrorHandler
import za.co.xisystems.itis_rrm.custom.results.XIError
import za.co.xisystems.itis_rrm.custom.views.IndefiniteSnackbar
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.ui.mainview.create.CreateViewModel
import za.co.xisystems.itis_rrm.ui.mainview.create.CreateViewModelFactory
import za.co.xisystems.itis_rrm.ui.mainview.unsubmitted.unsubmited_item.UnSubmittedJobItem
import za.co.xisystems.itis_rrm.utils.ActivityIdConstants
import za.co.xisystems.itis_rrm.utils.Coroutines

class UnSubmittedFragment : BaseFragment(R.layout.fragment_unsubmittedjobs), KodeinAware {

    override val kodein by kodein()
    private lateinit var createViewModel: CreateViewModel
    private val createFactory: CreateViewModelFactory by instance()
    private lateinit var groupAdapter: GroupAdapter<GroupieViewHolder>
    private lateinit var unSubmittedViewModel: UnSubmittedViewModel
    private val factory: UnSubmittedViewModelFactory by instance()

    companion object {
        val TAG: String = UnSubmittedFragment::class.java.simpleName
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_unsubmittedjobs, container, false)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        unSubmittedViewModel = activity?.run {
            ViewModelProvider(this, factory).get(UnSubmittedViewModel::class.java)
        } ?: throw Exception("Invalid Activity")

        createViewModel = activity?.run {
            ViewModelProvider(this, createFactory).get(CreateViewModel::class.java)
        } ?: throw Exception("Invalid Activity")

        fetchUnsubmitted()
    }

    private fun fetchUnsubmitted() {
        Coroutines.main {
            try {
                groupAdapter = GroupAdapter()
                group12_loading.visibility = View.VISIBLE
                val measurements =
                    unSubmittedViewModel.getJobsForActivityId(ActivityIdConstants.JOB_ESTIMATE)

                measurements.observe(viewLifecycleOwner, { jobList ->
                    if (jobList.isNullOrEmpty()) {
                        groupAdapter.clear()
                        noData.visibility = View.VISIBLE
                        incomplete_job_listView.visibility = View.GONE
                    } else {
                        noData.visibility = View.GONE
                        incomplete_job_listView.visibility = View.VISIBLE
                        initRecyclerView(jobList.toApproveListItems())
                    }
                })
            } catch (t: Throwable) {
                Timber.e(t, "Failed to fetch unsubmitted jobs!")
                val unsubError = XIError(t, t.message ?: XIErrorHandler.UNKNOWN_ERROR)

                crashGuard(
                    this@UnSubmittedFragment.requireView(),
                    unsubError,
                    refreshAction = { retryUnsubmitted() }
                )

            } finally {
                group12_loading.visibility = View.GONE
            }
        }
    }

    private fun retryUnsubmitted() {
        IndefiniteSnackbar.hide()
        fetchUnsubmitted()
    }

    private fun List<JobDTO>.toApproveListItems(): List<UnSubmittedJobItem> {
        return this.map { jobsToApprove ->
            UnSubmittedJobItem(jobsToApprove, unSubmittedViewModel, createViewModel, groupAdapter)
        }
    }

    private fun initRecyclerView(items: List<UnSubmittedJobItem>) {
        groupAdapter = GroupAdapter<GroupieViewHolder>().apply {
            addAll(items)
        }
        incomplete_job_listView.apply {
            layoutManager = LinearLayoutManager(this.context)
            adapter = groupAdapter
        }
    }

    override fun onDestroyView() {
        incomplete_job_listView.adapter = null
        super.onDestroyView()
    }
}
