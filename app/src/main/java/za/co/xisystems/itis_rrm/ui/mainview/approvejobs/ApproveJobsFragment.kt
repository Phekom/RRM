@file:Suppress("RemoveExplicitTypeArguments")

package za.co.xisystems.itis_rrm.ui.mainview.approvejobs

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.fragment_approvejob.*
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
import za.co.xisystems.itis_rrm.extensions.observeOnce
import za.co.xisystems.itis_rrm.ui.mainview.approvejobs.approve_job_item.ApproveJobItem
import za.co.xisystems.itis_rrm.utils.ActivityIdConstants
import za.co.xisystems.itis_rrm.utils.Coroutines

/**
 * Created by Francis Mahlava on 03,October,2019
 */

class ApproveJobsFragment : BaseFragment(R.layout.fragment_approvejob), KodeinAware {

    override val kodein by kodein()
    private lateinit var approveViewModel: ApproveJobsViewModel
    private val factory: ApproveJobsViewModelFactory by instance<ApproveJobsViewModelFactory>()
    lateinit var dialog: Dialog

    companion object {
        val TAG: String = ApproveJobsFragment::class.java.simpleName
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_approvejob, container, false)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        approveViewModel = activity?.run {
            ViewModelProvider(this, factory).get(ApproveJobsViewModel::class.java)
        } ?: throw Exception("Invalid Activity")

        Coroutines.main {
            dialog = setDataProgressDialog(
                requireActivity(),
                getString(R.string.data_loading_please_wait)
            )
            try {
                group3_loading.visibility = View.VISIBLE
                fetchLocalJobs()
            } catch (t: Throwable) {
                val xiFail = XIError(t, t.localizedMessage ?: XIErrorHandler.UNKNOWN_ERROR)
                XIErrorHandler.crashGuard(
                    view = this@ApproveJobsFragment.requireView(),
                    throwable = xiFail,
                    refreshAction = { retryFetchLocalJobs() }
                )
            } finally {
                group3_loading.visibility = View.GONE
            }

            swipeToRefreshInit()
        }
    }

    private fun retryFetchLocalJobs() {
        IndefiniteSnackbar.hide()
        Coroutines.main {
            fetchRemoteJobs()
        }
    }

    private suspend fun fetchLocalJobs() {

        val jobs = approveViewModel.getJobsForActivityId(ActivityIdConstants.JOB_APPROVE)
        jobs.observe(viewLifecycleOwner, { jobList ->

            if (jobList.isNullOrEmpty()) {
                job_data_layout.visibility = View.GONE
                no_data_layout.visibility = View.VISIBLE
            } else {
                val jItems = jobList.distinctBy {
                    it.JobId
                }
                no_data_layout.visibility = View.GONE
                job_data_layout.visibility = View.VISIBLE
                toast(jobList.size.toString())
                initRecyclerView(jItems.toApproveListItems())
            }
            group3_loading.visibility = GONE
        })
    }

    private fun swipeToRefreshInit() {
        jobs_swipe_to_refresh.setProgressBackgroundColorSchemeColor(
            ContextCompat.getColor(
                requireContext().applicationContext,
                R.color.colorPrimary
            )
        )

        jobs_swipe_to_refresh.setColorSchemeColors(Color.WHITE)

        jobs_swipe_to_refresh.setOnRefreshListener {
            fetchRemoteJobs()
        }
    }

    private fun fetchRemoteJobs() {
        Coroutines.main {
            try {
                group3_loading.visibility = View.VISIBLE
                val freshJobs = approveViewModel.offlineUserTaskList.await()
                freshJobs.observeOnce(viewLifecycleOwner, {
                    jobs_swipe_to_refresh.isRefreshing = false
                    if (it.isNullOrEmpty()) {
                        job_data_layout.visibility = View.GONE
                        no_data_layout.visibility = View.VISIBLE
                    }
                })
                fetchLocalJobs()
            } catch (t: Throwable) {
                val message = t.localizedMessage ?: XIErrorHandler.UNKNOWN_ERROR
                Timber.e(t)
                val xiFail = XIError(t, message)
                XIErrorHandler.crashGuard(
                    view = this@ApproveJobsFragment.requireView(),
                    throwable = xiFail,
                    refreshAction = { retryFetchRemoteJobs() }
                )
            } finally {
                jobs_swipe_to_refresh.isRefreshing = false
                group3_loading.visibility = View.GONE
            }
        }
    }

    private fun retryFetchRemoteJobs() {
        IndefiniteSnackbar.hide()
        fetchRemoteJobs()
    }

    private fun initRecyclerView(
        approveJobListItems: List<ApproveJobItem>
    ) {
        val groupAdapter = GroupAdapter<GroupieViewHolder>().apply {
            addAll(approveJobListItems)
        }
        approve_job_listView.apply {
            layoutManager = LinearLayoutManager(this.context)
            adapter = groupAdapter
        }

        groupAdapter.setOnItemClickListener { item, view ->
            Coroutines.main {
                (item as? ApproveJobItem)?.let {
                    sendJobToApprove((it), view)
                }
            }
        }
    }

    private fun sendJobToApprove(
        job: ApproveJobItem,
        view: View
    ) {
        Coroutines.main {
            approveViewModel.setJobForApproval(job)
        }

        Navigation.findNavController(view)
            .navigate(R.id.action_nav_approveJbs_to_jobInfoFragment)
    }

    private fun List<JobDTO>.toApproveListItems(): List<ApproveJobItem> {
        return this.map { approveJobItems ->
            ApproveJobItem(approveJobItems, approveViewModel, this@ApproveJobsFragment.requireContext())
        }
    }

    override fun onDestroyView() {
        approve_job_listView.adapter = null
        super.onDestroyView()
    }
}
