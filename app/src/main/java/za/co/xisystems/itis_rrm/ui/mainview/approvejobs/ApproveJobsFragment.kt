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
import androidx.lifecycle.Observer
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
import za.co.xisystems.itis_rrm.data._commons.views.ToastUtils
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.ui.mainview._fragments.BaseFragment
import za.co.xisystems.itis_rrm.ui.mainview.approvejobs.approve_job_item.ApproveJobItem
import za.co.xisystems.itis_rrm.utils.*

/**
 * Created by Francis Mahlava on 03,October,2019
 */

class ApproveJobsFragment : BaseFragment(R.layout.fragment_approvejob), KodeinAware {
//

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
            dialog = setDataProgressDialog(requireActivity(), getString(R.string.data_loading_please_wait))
            val jobs = approveViewModel.getJobsForActivityId(ActivityIdConstants.JOB_APPROVE)
            jobs.observe(viewLifecycleOwner, Observer { job_s ->
                val jItems = job_s.distinctBy {
                    it.JobId
                }
                noData.visibility = GONE
                if (job_s.isEmpty()) {
                    noData.visibility = View.VISIBLE
                }
                toast(job_s.size.toString())
                initRecyclerView(jItems.toApproveListItems())
                group3_loading.visibility = GONE
            })

            jobs_swipe_to_refresh.setProgressBackgroundColorSchemeColor(
                ContextCompat.getColor(
                    requireContext().applicationContext,
                    R.color.colorPrimary
                )
            )

            jobs_swipe_to_refresh.setColorSchemeColors(Color.WHITE)

            jobs_swipe_to_refresh.setOnRefreshListener {
                dialog.show()
                Coroutines.main {
                    try {
                        val freshJobs = approveViewModel.offlineUserTaskList.await()
                        freshJobs.observe(viewLifecycleOwner, Observer {
                            jobs_swipe_to_refresh.isRefreshing = false
                            if (it.isEmpty()) {
                                noData.visibility = View.VISIBLE
                            }
                            dialog.dismiss()
                        })
                    } catch (e: ApiException) {
                        ToastUtils().toastLong(activity, e.message)
                        Timber.e(e, "API Exception")
                    } catch (e: NoInternetException) {
                        ToastUtils().toastLong(activity, e.message)
                        Timber.e(e, "No Internet Connection")
                    } catch (e: NoConnectivityException) {
                        ToastUtils().toastLong(activity, e.message)
                        Timber.e(e, "Service Host Unreachable")
                    } finally {
                        dialog.dismiss()
                        jobs_swipe_to_refresh.isRefreshing = false
                    }
                }
            }
        }
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
            ApproveJobItem(approveJobItems, approveViewModel)
        }
    }

    override fun onDestroyView() {
        // jobs_swipe_to_refresh.setOnRefreshListener { null }
        approve_job_listView.adapter = null
        super.onDestroyView()
    }
}
