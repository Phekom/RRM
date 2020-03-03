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
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import kotlinx.android.synthetic.main.fragment_approvejob.*
import kotlinx.android.synthetic.main.fragment_approvemeasure.*
import kotlinx.android.synthetic.main.fragment_approvemeasure.noData
import kotlinx.android.synthetic.main.fragment_work.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.data._commons.views.ToastUtils
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemMeasureDTO
import za.co.xisystems.itis_rrm.ui.mainview._fragments.BaseFragment
import za.co.xisystems.itis_rrm.ui.mainview.approvejobs.ApproveJobsFragment
import za.co.xisystems.itis_rrm.ui.mainview.approvemeasure.approveMeasure_Item.ApproveMeasure_Item
import za.co.xisystems.itis_rrm.utils.*

/**
 * Created by Francis Mahlava on 03,October,2019
 */


class ApproveMeasureFragment : BaseFragment(), KodeinAware {

    override val kodein by kodein()
    private lateinit var approveViewModel: ApproveMeasureViewModel
    private val factory: ApproveMeasureViewModelFactory by instance()

companion object{
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
            ViewModelProviders.of(this, factory).get(ApproveMeasureViewModel::class.java)
        } ?: throw Exception("Invalid Activity") as Throwable
        Coroutines.main {
            val dialog = setDataProgressDialog(activity!!, getString(R.string.data_loading_please_wait))

            val measurements = approveViewModel.getJobApproveMeasureForActivityId(ActivityIdConstants.MEASURE_COMPLETE)
//            val measurements  = approveViewModel.getJobsMeasureForActivityId(ActivityIdConstants.ESTIMATE_MEASURE,ActivityIdConstants.MEASURE_COMPLETE,ActivityIdConstants.EST_WORKS_COMPLETE,ActivityIdConstants.JOB_APPROVED)
//            val measurements = approveViewModel.getEntitiesListForActivityId(ActivityIdConstants.MEASURE_COMPLETE)
//            val measurements = approveViewModel.offlinedata.await()
            measurements.observe(viewLifecycleOwner, Observer { job_s ->
                noData.visibility = GONE
                val measure_items = job_s.distinctBy{
                    it.jobId
                }
                initRecyclerView(measure_items.toApproveListItems())
                toast(measure_items.size.toString())
                group4_loading.visibility = GONE
            })
            approvem_swipe_to_refresh.setProgressBackgroundColorSchemeColor(
                ContextCompat.getColor(
                    context!!.applicationContext,
                    R.color.colorPrimary
                )
            )
            approvem_swipe_to_refresh.setColorSchemeColors(Color.WHITE)

            approvem_swipe_to_refresh.setOnRefreshListener {
                dialog.show()
                Coroutines.main {
                    try {
                        val freshJobs = approveViewModel.offlinedatas.await()
                        freshJobs.observe(viewLifecycleOwner, Observer {
                            approvem_swipe_to_refresh.isRefreshing = false
                            dialog.dismiss()
                        })
                    } catch (e: ApiException) {
                        ToastUtils().toastLong(activity, e.message)
                        approvem_swipe_to_refresh.isRefreshing  = false
                        dialog.dismiss()
                        Log.e(ApproveJobsFragment.TAG, "API Exception", e)
                    } catch (e: NoInternetException) {
                        ToastUtils().toastLong(activity, e.message)
                        dialog.dismiss()
                        approvem_swipe_to_refresh.isRefreshing  = false
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
    }

    private fun initRecyclerView(approveMeasureListItems: List<ApproveMeasure_Item>) {
        val groupAdapter = GroupAdapter<GroupieViewHolder>().apply {
            addAll(approveMeasureListItems)

        }
        approve_measurements_list.apply {
            layoutManager = LinearLayoutManager(this.context)
            adapter = groupAdapter

        }

        groupAdapter.setOnItemClickListener { item, view ->
            Coroutines.main {
                (item as? ApproveMeasure_Item)?.let {
                    sendJobtoAprove((it), view)
                }

            }
        }
    }

    private fun sendJobtoAprove(
        job: ApproveMeasure_Item?,
        view: View
    ) {
        val jobId = job
        Coroutines.main {
            approveViewModel.measureapproval_Item.value = jobId
        }

        Navigation.findNavController(view)
            .navigate(R.id.action_nav_approvMeasure_to_measureApprovalFragment)
    }
    private fun List<JobItemMeasureDTO>.toApproveListItems(): List<ApproveMeasure_Item> {
//    private fun List<JobDTO>.toApproveListItems(): List<ApproveMeasure_Item> {
        return this.map { approvej_items ->
            ApproveMeasure_Item(approvej_items,approveViewModel)
        }
    }


}

//(item as? ApproveMeasure_Item)?.let {
////                    val descri = approveViewModel.getDescForProjectId(it.jobItemMeasureDTO.projectItemId!!)
////                    val sectionId  =  approveViewModel.getProjectSectionIdForJobId(it.jobDTO.jobId)
////                    val route  =  approveViewModel.getRouteForProjectSectionId(sectionId)
////                    val section  =  approveViewModel.getSectionForProjectSectionId(sectionId)
////                    sendJobtoAprove((it.jobItemMeasureDTO.jobId), view)
//    sendJobtoAprove((it), view)
//}


