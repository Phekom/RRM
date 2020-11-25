@file:Suppress(
    "RemoveExplicitTypeArguments"
)

package za.co.xisystems.itis_rrm.ui.mainview.approvemeasure

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import kotlinx.android.synthetic.main.fragment_approvemeasure.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import timber.log.Timber
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.base.BaseFragment
import za.co.xisystems.itis_rrm.custom.errors.XIErrorHandler
import za.co.xisystems.itis_rrm.custom.results.XIError
import za.co.xisystems.itis_rrm.custom.views.IndefiniteSnackbar
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemMeasureDTO
import za.co.xisystems.itis_rrm.ui.mainview.approvemeasure.approveMeasure_Item.ApproveMeasureItem
import za.co.xisystems.itis_rrm.utils.ActivityIdConstants
import za.co.xisystems.itis_rrm.utils.Coroutines

/**
 * Created by Francis Mahlava on 03,October,2019
 */

class ApproveMeasureFragment : BaseFragment(R.layout.fragment_approvemeasure), KodeinAware {

    override val kodein by kodein()
    private lateinit var approveViewModel: ApproveMeasureViewModel
    private val factory: ApproveMeasureViewModelFactory by instance<ApproveMeasureViewModelFactory>()

    companion object {
        const val JOB_ID_FOR_MEASUREMENT_APPROVAL = "JOB_ID_FOR_MEASUREMENT_APPROVAL"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_approvemeasure, container, false)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        approveViewModel = activity?.run {
            ViewModelProvider(this, factory).get(ApproveMeasureViewModel::class.java)
        } ?: throw Exception("Invalid Activity")

        loadJobHeaders()

        swipeToRefreshInit()
    }

    private fun retryFetchMeasurements() {
        IndefiniteSnackbar.hide()
        fetchJobsFromService()
        loadJobHeaders()
    }

    private fun loadJobHeaders() {

        Coroutines.main {
            try {
                group4_loading.visibility = View.VISIBLE
                val measurementsSubscription =
                    approveViewModel.getJobApproveMeasureForActivityId(ActivityIdConstants.MEASURE_COMPLETE)

                measurementsSubscription.observe(viewLifecycleOwner, { measurementData ->

                    if (measurementData.isNullOrEmpty()) {
                        noData.visibility = View.VISIBLE
                        approve_measurements_list.visibility = View.GONE
                    } else {
                        noData.visibility = View.GONE
                        approve_measurements_list.visibility = View.VISIBLE
                        val jobHeaders = measurementData.distinctBy {
                            it.jobId
                        }
                        initRecyclerView(jobHeaders.toApproveListItems())
                        group4_loading.visibility = View.GONE
                    }
                })
            } catch (t: Throwable) {
                Timber.e(t, "Unable to fetch Measurements")
                val measureErr = XIError(t, t.message ?: XIErrorHandler.UNKNOWN_ERROR)
                XIErrorHandler.crashGuard(
                    fragment = this@ApproveMeasureFragment,
                    view = this@ApproveMeasureFragment.requireView(),
                    throwable = measureErr,
                    refreshAction = { retryFetchMeasurements() }
                )
            } finally {
                group4_loading.visibility = View.GONE
            }
        }
    }

    private fun swipeToRefreshInit() {
        approvem_swipe_to_refresh.setProgressBackgroundColorSchemeColor(
            ContextCompat.getColor(
                requireContext().applicationContext,
                R.color.colorPrimary
            )
        )

        approvem_swipe_to_refresh.setColorSchemeColors(Color.WHITE)

        approvem_swipe_to_refresh.setOnRefreshListener {
            fetchJobsFromService()
            loadJobHeaders()
        }
    }

    private fun fetchJobsFromService() {
        Coroutines.main {
            try {
                group4_loading.visibility = View.VISIBLE
                val freshJobs = approveViewModel.offlineUserTaskList.await()
                freshJobs.observe(viewLifecycleOwner, {
                    if (it.isNullOrEmpty()) {
                        noData.visibility = View.VISIBLE
                        approve_measurements_list.visibility = View.GONE
                    } else {
                        noData.visibility = View.GONE
                        approve_measurements_list.visibility = View.VISIBLE
                    }
                })
            } catch (t: Throwable) {
                Timber.e(t, "Unable to fetch remote jobs")
                val measureErr = XIError(t, t.message ?: XIErrorHandler.UNKNOWN_ERROR)
                XIErrorHandler.crashGuard(
                    fragment = this@ApproveMeasureFragment,
                    view = this@ApproveMeasureFragment.requireView(),
                    throwable = measureErr,
                    refreshAction = { retryFetchMeasurements() }
                )
            } finally {
                group4_loading.visibility = View.GONE
                approvem_swipe_to_refresh.isRefreshing = false
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
            job?.let { approveViewModel.setApproveMeasureItem(job) }
        }

        Navigation.findNavController(view)
            .navigate(R.id.action_nav_approvMeasure_to_measureApprovalFragment)
    }

    private fun List<JobItemMeasureDTO>.toApproveListItems(): List<ApproveMeasureItem> {
        return this.map { approvejItems ->
            ApproveMeasureItem(approvejItems, approveViewModel)
        }
    }
}
