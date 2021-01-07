@file:Suppress(
    "RemoveExplicitTypeArguments"
)

package za.co.xisystems.itis_rrm.ui.mainview.approvemeasure

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.skydoves.androidveil.VeiledItemOnClickListener
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import timber.log.Timber
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.R.layout
import za.co.xisystems.itis_rrm.base.BaseFragment
import za.co.xisystems.itis_rrm.constants.Constants
import za.co.xisystems.itis_rrm.custom.errors.XIErrorHandler
import za.co.xisystems.itis_rrm.custom.results.XIError
import za.co.xisystems.itis_rrm.custom.views.IndefiniteSnackbar
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemMeasureDTO
import za.co.xisystems.itis_rrm.databinding.FragmentApprovemeasureBinding
import za.co.xisystems.itis_rrm.ui.mainview.approvemeasure.approveMeasure_Item.ApproveMeasureItem
import za.co.xisystems.itis_rrm.utils.ActivityIdConstants
import za.co.xisystems.itis_rrm.utils.Coroutines

/**
 * Created by Francis Mahlava on 03,October,2019
 */

class ApproveMeasureFragment : BaseFragment(), KodeinAware {

    override val kodein by kodein()
    private lateinit var approveViewModel: ApproveMeasureViewModel
    private val factory: ApproveMeasureViewModelFactory by instance<ApproveMeasureViewModelFactory>()

    private var _ui: FragmentApprovemeasureBinding? = null
    private val ui get() = _ui!!
    private var groupAdapter = GroupAdapter<GroupieViewHolder>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _ui = FragmentApprovemeasureBinding.inflate(inflater, container, false)
        return ui.root
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // no options menu
        return false
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        approveViewModel = activity?.run {
            ViewModelProvider(this, factory).get(ApproveMeasureViewModel::class.java)
        } ?: throw Exception("Invalid Activity")

        initVeiledRecyclerView()

        loadJobHeaders()

        swipeToRefreshInit()
    }

    private fun initVeiledRecyclerView() {
        ui.approveMeasurementsList.run {
            setVeilLayout(layout.item_velied_slug, object : VeiledItemOnClickListener {
                /** will be invoked when the item on the [VeilRecyclerFrameView] clicked. */
                override fun onItemClicked(pos: Int) {
                    Toast.makeText(this@ApproveMeasureFragment.requireContext(), "Loading ...", Toast.LENGTH_SHORT).show()
                }
            })
            setAdapter(groupAdapter)
            setLayoutManager(LinearLayoutManager(this.context))
            addVeiledItems(15)
        }
    }

    private fun retryFetchMeasurements() {
        IndefiniteSnackbar.hide()
        ui.approveMeasurementsList.veil()
        fetchJobsFromService()
        loadJobHeaders()
        delayedUnveil()
    }

    private fun loadJobHeaders() {

        Coroutines.main {
            try {

                val measurementsSubscription =
                    approveViewModel.getJobApproveMeasureForActivityId(ActivityIdConstants.MEASURE_COMPLETE)

                measurementsSubscription.observe(viewLifecycleOwner, { measurementData ->

                    if (measurementData.isNullOrEmpty()) {
                        ui.noData.visibility = View.VISIBLE
                        ui.approveMeasurementsList.visibility = View.GONE
                    } else {
                        ui.noData.visibility = View.GONE
                        ui.approveMeasurementsList.visibility = View.VISIBLE
                        val jobHeaders = measurementData.distinctBy {
                            it.jobId
                        }
                        initRecyclerView(jobHeaders.toApproveListItems())
                    }
                })
            } catch (t: Throwable) {
                Timber.e(t, "Unable to fetch Measurements")
                val measureErr = XIError(t, t.message ?: XIErrorHandler.UNKNOWN_ERROR)
                crashGuard(
                    view = this@ApproveMeasureFragment.requireView(),
                    throwable = measureErr,
                    refreshAction = { retryFetchMeasurements() }
                )
            }
        }
    }

    private fun swipeToRefreshInit() {
        ui.approvemSwipeToRefresh.setProgressBackgroundColorSchemeColor(
            ContextCompat.getColor(
                requireContext().applicationContext,
                R.color.colorPrimary
            )
        )

        ui.approvemSwipeToRefresh.setColorSchemeColors(Color.WHITE)

        ui.approvemSwipeToRefresh.setOnRefreshListener {
            ui.approveMeasurementsList.veil()
            fetchJobsFromService()
            loadJobHeaders()
            delayedUnveil()
        }
    }

    private fun delayedUnveil() {
        Handler(Looper.getMainLooper()).postDelayed(
            {
                if (!activity?.isFinishing!!) {
                    ui.approveMeasurementsList.unVeil()
                }
            },
            Constants.ONE_SECOND
        )
    }

    private fun fetchJobsFromService() {
        Coroutines.main {
            try {
                val freshJobs = approveViewModel.offlineUserTaskList.await()
                freshJobs.observe(viewLifecycleOwner, {
                    if (it.isNullOrEmpty()) {

                        ui.noData.visibility = View.VISIBLE
                        ui.approveMeasurementsList.visibility = View.GONE
                    } else {
                        ui.noData.visibility = View.GONE
                        ui.approveMeasurementsList.visibility = View.VISIBLE
                    }
                })
            } catch (t: Throwable) {
                Timber.e(t, "Unable to fetch remote jobs")
                val measureErr = XIError(t, t.message ?: XIErrorHandler.UNKNOWN_ERROR)
                crashGuard(
                    view = this@ApproveMeasureFragment.requireView(),
                    throwable = measureErr,
                    refreshAction = { retryFetchMeasurements() }
                )
            } finally {
                ui.approvemSwipeToRefresh.isRefreshing = false
            }
        }
    }

    private fun initRecyclerView(approveMeasureListItems: List<ApproveMeasureItem>) {

        val groupAdapter = GroupAdapter<GroupieViewHolder>().apply {
            clear()
            addAll(approveMeasureListItems)
            notifyDataSetChanged()
        }

        ui.approveMeasurementsList.run {
            setLayoutManager(LinearLayoutManager(this.context))
            setAdapter(adapter = groupAdapter)
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
        super.onDestroyView()
        ui.approveMeasurementsList.setAdapter(null)
        _ui = null
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
