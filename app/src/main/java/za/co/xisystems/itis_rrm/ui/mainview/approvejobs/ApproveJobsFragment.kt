package za.co.xisystems.itis_rrm.ui.mainview.approvejobs

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
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
import za.co.xisystems.itis_rrm.data._commons.views.ToastUtils
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.ui.mainview._fragments.BaseFragment
import za.co.xisystems.itis_rrm.ui.mainview.approvejobs.approve_job_item.ApproveJob_Item
import za.co.xisystems.itis_rrm.utils.*

/**
 * Created by Francis Mahlava on 03,October,2019
 */

class ApproveJobsFragment : BaseFragment(), KodeinAware {

    override val kodein by kodein()
    private lateinit var approveViewModel: ApproveJobsViewModel
    private val factory: ApproveJobsViewModelFactory by instance()


    companion object {
        val TAG: String = ApproveJobsFragment::class.java.simpleName
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_approvejob, container, false)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // TODO: fix deprecated ViewModelProviders
        approveViewModel = activity?.run {
            ViewModelProviders.of(this, factory).get(ApproveJobsViewModel::class.java)
        } ?: throw Exception("Invalid Activity")
        Coroutines.main {
            val dialog = setDataProgressDialog(activity!!, getString(R.string.data_loading_please_wait))
            val jobs = approveViewModel.getJobsForActivityId(ActivityIdConstants.JOB_APPROVE)
            jobs.observe(viewLifecycleOwner, Observer { job_s ->
                val j_items = job_s.distinctBy{
                    it.JobId
                }
                noData.visibility = GONE
                toast(job_s.size.toString())
                initRecyclerView(j_items.toApproveListItems())
                group3_loading.visibility = GONE
            })

            jobs_swipe_to_refresh.setProgressBackgroundColorSchemeColor(
                ContextCompat.getColor(
                    context!!.applicationContext,
                    R.color.colorPrimary
                )
            )
            jobs_swipe_to_refresh.setColorSchemeColors(Color.WHITE)

            jobs_swipe_to_refresh.setOnRefreshListener {
                dialog.show()
                Coroutines.main {
                    try {
                        val freshJobs = approveViewModel.offlinedatas.await()
                        freshJobs.observe(viewLifecycleOwner, Observer {
                            jobs_swipe_to_refresh.isRefreshing = false
                            dialog.dismiss()
                        })
                    } catch (e: ApiException) {
                        ToastUtils().toastLong(activity, e.message)
                        dialog.dismiss()
                        jobs_swipe_to_refresh.isRefreshing  = false
                        Log.e(TAG, "API Exception", e)
                    } catch (e: NoInternetException) {
                        ToastUtils().toastLong(activity, e.message)
                        dialog.dismiss()
                        jobs_swipe_to_refresh.isRefreshing  = false
                        Log.e(TAG, "No Internet Connection", e)
                    } catch (e: NoConnectivityException) {
                        ToastUtils().toastLong(activity, e.message )
                                dialog.dismiss()
                        jobs_swipe_to_refresh.isRefreshing = false
                        Log.e(TAG, "Service Host Unreachable", e)
                    }
                }
            }
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
                    sendJobToApprove((it), view)
                }
            }
        }
    }

    private fun sendJobToApprove(
        job: ApproveJob_Item?,
        view: View
    ) {
        Coroutines.main {
            approveViewModel.jobapproval_Item6.value = job
        }

        Navigation.findNavController(view)
            .navigate(R.id.action_nav_approveJbs_to_jobInfoFragment)
    }

    private fun List<JobDTO>.toApproveListItems(): List<ApproveJob_Item> {
        return this.map { approveJobItems ->
            ApproveJob_Item(approveJobItems, approveViewModel)
        }
    }

}



