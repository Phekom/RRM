package za.co.xisystems.itis_rrm.ui.mainview.approvemeasure

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
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import kotlinx.android.synthetic.main.fragment_approvemeasure.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.base.BaseFragment
import za.co.xisystems.itis_rrm.custom.errors.ApiException
import za.co.xisystems.itis_rrm.custom.errors.NoConnectivityException
import za.co.xisystems.itis_rrm.custom.errors.NoInternetException
import za.co.xisystems.itis_rrm.data._commons.views.ToastUtils
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemMeasureDTO
import za.co.xisystems.itis_rrm.ui.mainview.approvejobs.ApproveJobsFragment
import za.co.xisystems.itis_rrm.ui.mainview.approvemeasure.approveMeasure_Item.ApproveMeasureItem
import za.co.xisystems.itis_rrm.utils.ActivityIdConstants
import za.co.xisystems.itis_rrm.utils.Coroutines

/**
 * Created by Francis Mahlava on 03,October,2019
 */


class ApproveMeasureFragment : BaseFragment(R.layout.fragment_approvemeasure), KodeinAware {

    override val kodein by kodein()
    private lateinit var approveViewModel: ApproveMeasureViewModel
    private val factory: ApproveMeasureViewModelFactory by instance()

    companion object {
        const val JOB_ID_FOR_MEASUREMENT_APPROVAL = "JOB_ID_FOR_MEASUREMENT_APPROVAL"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_approvemeasure, container, false)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        approveViewModel = activity?.run {
            ViewModelProvider(this, factory).get(ApproveMeasureViewModel::class.java)
        } ?: throw Exception("Invalid Activity")
        val dialog =
            setDataProgressDialog(activity!!, getString(R.string.data_loading_please_wait))
        Coroutines.main {

            try {
                dialog.show()
                val measurements =
                    approveViewModel.getJobApproveMeasureForActivityId(ActivityIdConstants.MEASURE_COMPLETE)
//            val measurements  = approveViewModel.getJobsMeasureForActivityId(ActivityIdConstants.ESTIMATE_MEASURE,ActivityIdConstants.MEASURE_COMPLETE,ActivityIdConstants.EST_WORKS_COMPLETE,ActivityIdConstants.JOB_APPROVED)
//            val measurements = approveViewModel.getEntitiesListForActivityId(ActivityIdConstants.MEASURE_COMPLETE)
//            val measurements = approveViewModel.offlinedata.await()
                measurements.observe(viewLifecycleOwner, Observer { job_s ->
                    noData.visibility = GONE
                    if (job_s.isEmpty()) {
                        noData.visibility = View.VISIBLE
                    }
                    val measure_items = job_s.distinctBy {
                        it.jobId
                    }
                    initRecyclerView(measure_items.toApproveListItems())
                    toast(measure_items.size.toString())
                    group4_loading.visibility = GONE
                })
                dialog.dismiss()
            } catch (e: ApiException) {
                ToastUtils().toastLong(activity, e.message)
                dialog.dismiss()
                Log.e(ApproveJobsFragment.TAG, "API Exception", e)
            } catch (e: NoInternetException) {
                ToastUtils().toastLong(activity, e.message)
                dialog.dismiss()
                Log.e(ApproveJobsFragment.TAG, "No Internet Connection", e)
            } catch (e: NoConnectivityException) {
                ToastUtils().toastLong(activity, e.message)
                dialog.dismiss()
                Log.e(ApproveJobsFragment.TAG, "Service Host Unreachable", e)
            }
        }

        Coroutines.main {
            approvem_swipe_to_refresh.setProgressBackgroundColorSchemeColor(
                ContextCompat.getColor(
                    context!!.applicationContext,
                    R.color.colorPrimary
                )
            )
            approvem_swipe_to_refresh.setColorSchemeColors(Color.WHITE)
        }

        approvem_swipe_to_refresh.setOnRefreshListener {
            dialog.show()
            Coroutines.main {
                try {
                    val freshJobs = approveViewModel.offlineUserTaskList.await()
                    freshJobs.observe(viewLifecycleOwner, Observer {
                        approvem_swipe_to_refresh.isRefreshing = false
                        if (it.isEmpty()) {
                            noData.visibility = View.VISIBLE
                        }
                        dialog.dismiss()
                    })
                } catch (e: ApiException) {
                    ToastUtils().toastLong(activity, e.message)
                    approvem_swipe_to_refresh.isRefreshing = false
                    dialog.dismiss()
                    Log.e(ApproveJobsFragment.TAG, "API Exception", e)
                } catch (e: NoInternetException) {
                    ToastUtils().toastLong(activity, e.message)
                    dialog.dismiss()
                    approvem_swipe_to_refresh.isRefreshing = false
                    Log.e(ApproveJobsFragment.TAG, "No Internet Connection", e)
                } catch (e: NoConnectivityException) {
                    ToastUtils().toastLong(activity, e.message)
                    dialog.dismiss()
                    approvem_swipe_to_refresh.isRefreshing = false
                    Log.e(ApproveJobsFragment.TAG, "Service Host Unreachable", e)
                }
            }
        }
    }


    private fun initRecyclerView(approveMeasureListItems: List<ApproveMeasureItem>) {
        val groupAdapter = GroupAdapter<GroupieViewHolder>().apply {
            addAll(approveMeasureListItems)

        }

        approve_measurements_list.apply {
            layoutManager = LinearLayoutManager(this.context)
            adapter = groupAdapter

        }

        groupAdapter.setOnItemClickListener { item, view ->
            Coroutines.main {
                (item as? ApproveMeasureItem)?.let {
                    sendJobToApprove((it), view)
                }

            }
        }
    }

    override fun onDestroyView() {
        approve_measurements_list.adapter = null
        super.onDestroyView()
    }

    private fun sendJobToApprove(
        job: ApproveMeasureItem?,
        view: View
    ) {
        Coroutines.main {
            approveViewModel.measureapproval_Item.value = job
        }

        Navigation.findNavController(view)
            .navigate(R.id.action_nav_approvMeasure_to_measureApprovalFragment)
    }

    private fun List<JobItemMeasureDTO>.toApproveListItems(): List<ApproveMeasureItem> {
//    private fun List<JobDTO>.toApproveListItems(): List<ApproveMeasure_Item> {
        return this.map { approvej_items ->
            ApproveMeasureItem(approvej_items, approveViewModel)
        }
    }


}



