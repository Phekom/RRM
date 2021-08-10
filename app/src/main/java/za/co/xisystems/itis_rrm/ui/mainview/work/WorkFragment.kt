/*
 * Updated by Shaun McDonald on 2021/02/15
 * Last modified on 2021/02/15 12:45 AM
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 */

@file:Suppress("KDocUnresolvedReference")

package za.co.xisystems.itis_rrm.ui.mainview.work

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.doOnNextLayout
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenStarted
import androidx.recyclerview.widget.LinearLayoutManager
import com.xwray.groupie.ExpandableGroup
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.fragment_work.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.DIAware
import org.kodein.di.android.x.closestDI
import org.kodein.di.instance
import timber.log.Timber
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.base.BaseFragment
import za.co.xisystems.itis_rrm.custom.errors.XIErrorHandler
import za.co.xisystems.itis_rrm.custom.results.XIError
import za.co.xisystems.itis_rrm.custom.views.IndefiniteSnackbar
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.databinding.FragmentWorkBinding
import za.co.xisystems.itis_rrm.extensions.observeOnce
import za.co.xisystems.itis_rrm.ui.mainview.work.estimate_work_item.CardItem
import za.co.xisystems.itis_rrm.ui.mainview.work.estimate_work_item.ExpandableHeaderWorkItem
import za.co.xisystems.itis_rrm.ui.scopes.UiLifecycleScope
import za.co.xisystems.itis_rrm.utils.ActivityIdConstants
import za.co.xisystems.itis_rrm.utils.Coroutines

const val INSET_TYPE_KEY = "inset_type"
const val INSET = "inset"

class WorkFragment : BaseFragment(), DIAware {

    override val di by closestDI()
    private lateinit var workViewModel: WorkViewModel
    private lateinit var expandableGroups: MutableList<ExpandableGroup>
    private val factory: WorkViewModelFactory by instance()
    private var uiScope = UiLifecycleScope()
    private var groupAdapter: GroupAdapter<GroupieViewHolder>? = GroupAdapter<GroupieViewHolder>()
    private var veiled: Boolean = false
    private var _ui: FragmentWorkBinding? = null
    private val ui get() = _ui!!

    init {

        lifecycleScope.launch {

            whenStarted {

                uiScope.onCreate()

                viewLifecycleOwner.lifecycle.addObserver(uiScope)
            }
        }
    }

    private suspend fun refreshEstimateJobsFromLocal() {
        ui.veiledWorkListView.veil()
        withContext(uiScope.coroutineContext) {
            val localJobs = workViewModel.getJobsForActivityId(
                ActivityIdConstants.JOB_APPROVED,
                ActivityIdConstants.ESTIMATE_INCOMPLETE
            )

            localJobs.observeOnce(viewLifecycleOwner, { jobsList ->
                ui.group7Loading.visibility = View.GONE
                if (jobsList.isNullOrEmpty()) {
                    ui.veiledWorkListView.visibility = View.GONE
                    ui.noData.visibility = View.VISIBLE
                } else {
                    Coroutines.main {
                        ui.veiledWorkListView.visibility = View.VISIBLE
                        ui.noData.visibility = View.GONE
                        val headerItems = jobsList.distinctBy {
                            it.jobId
                        }

                        this@WorkFragment.initRecyclerView(headerItems.toWorkListItems())
                    }
                }
            })
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _ui = FragmentWorkBinding.inflate(inflater, container, false)
        return ui.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {

        super.onActivityCreated(savedInstanceState)
        workViewModel = activity?.run {
            ViewModelProvider(this, factory).get(WorkViewModel::class.java)
        } ?: throw Exception("Invalid Activity")

        initSwipeToRefresh()
        initVeiledRecycler()
        uiScope.launch(uiScope.coroutineContext) {
            try {
                refreshEstimateJobsFromLocal()
            } catch (t: Throwable) {
                Timber.e(t, "Failed to fetch local jobs")
                val xiFail = XIError(t, t.message ?: XIErrorHandler.UNKNOWN_ERROR)
                crashGuard(
                    view = this@WorkFragment.requireView(),
                    throwable = xiFail,
                    refreshAction = { retryFetchingJobs() })
            }
        }
    }

    private fun initVeiledRecycler() {
        ui.veiledWorkListView.run {
            setVeilLayout(R.layout.item_velied_slug) { Toast.makeText(this@WorkFragment.requireContext(), "Loading ...", Toast.LENGTH_SHORT).show() }
            setAdapter(groupAdapter)
            setLayoutManager(LinearLayoutManager(this.context))
            addVeiledItems(15)
        }
    }

    private fun initSwipeToRefresh() {

        ui.worksSwipeToRefresh.setProgressBackgroundColorSchemeColor(
            ContextCompat.getColor(
                requireContext().applicationContext,
                R.color.colorPrimary
            )
        )

        ui.worksSwipeToRefresh.setColorSchemeColors(Color.WHITE)

        ui.worksSwipeToRefresh.setOnRefreshListener {
            fetchJobsFromService()
        }
    }

    private fun fetchJobsFromService() = uiScope.launch(uiScope.coroutineContext) {
        try {
            ui.veiledWorkListView.veil()
            veiled = true
            withContext(uiScope.coroutineContext) {
                refreshUserTaskListFromApi()
                refreshEstimateJobsFromLocal()
            }
        } catch (t: Throwable) {
            Timber.e(t, t.localizedMessage ?: XIErrorHandler.UNKNOWN_ERROR)
            val jobErr = XIError(t, "Failed to fetch jobs from service")
            crashGuard(
                view = this@WorkFragment.requireView(),
                throwable = jobErr,
                refreshAction = { retryFetchingJobs() }
            )
        } finally {
            ui.worksSwipeToRefresh.isRefreshing = false
        }
    }

    private fun retryFetchingJobs() {
        IndefiniteSnackbar.hide()
        fetchJobsFromService()
    }

    private suspend fun refreshUserTaskListFromApi() {

        withContext(Dispatchers.Main) {
            try {
                val jobs = workViewModel.offlineUserTaskList.await()
                jobs.observeOnce(viewLifecycleOwner, { works ->
                    Timber.d("${works.size} / ${works.count()} loaded.")
                })
            } catch (t: Throwable) {
                val message = "Failed to fetch jobs from service: ${t.message ?: XIErrorHandler.UNKNOWN_ERROR}"
                Timber.e(t, message)
                throw t
            }
        }
    }

    private fun initRecyclerView(
        workListItems: List<ExpandableGroup>
    ) {
        val groupAdapter = GroupAdapter<GroupieViewHolder>().apply {
            clear()
            addAll(workListItems)
            notifyItemRangeChanged(0, workListItems.size)
        }
        val layoutManager = LinearLayoutManager(this.requireContext())
        ui.veiledWorkListView.setLayoutManager(layoutManager)
        ui.veiledWorkListView.setAdapter(groupAdapter)
        ui.veiledWorkListView.doOnNextLayout { ui.veiledWorkListView.unVeil() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        val item = menu.findItem(R.id.action_settings)
        val item1 = menu.findItem(R.id.action_logout)

        if (item != null) item.isVisible = false
        if (item1 != null) item1.isVisible = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        uiScope.destroy()
        ui.veiledWorkListView.getRecyclerView().adapter = null
        groupAdapter = null
        _ui = null
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // no options menu
        return false
    }

    private suspend fun List<JobDTO>.toWorkListItems(): List<ExpandableGroup> {
        // Initialize Expandable group with expandable item and specify whether it should be expanded by default or not
        expandableGroups = mutableListOf()
        return this.map { jobDTO ->

            val expandableHeaderItem =
                ExpandableHeaderWorkItem(activity, jobDTO, workViewModel)

            // When expanding a work item, collapse the others

            expandableHeaderItem.onExpandListener = { toggledGroup ->

                expandableGroups.forEach {
                    if (it != toggledGroup && it.isExpanded) {
                        it.onToggleExpanded()
                    }
                }
                // scrollToPositionWithOffset it to the top
                scrollToTop(expandableGroups, toggledGroup)
            }

            ExpandableGroup(expandableHeaderItem, false).apply {
                val estimates = workViewModel.getJobEstimationItemsForJobId(
                    jobDTO.jobId,
                    ActivityIdConstants.ESTIMATE_INCOMPLETE
                )
                estimates.observeOnce(viewLifecycleOwner, { estimateItems ->
                    estimateItems.forEach { item ->

                        uiScope.launch(uiScope.coroutineContext) {
                            try {
                                val desc =
                                    workViewModel.getDescForProjectItemId(item.projectItemId!!)
                                val qty = item.qty.toString()
                                val rate = item.lineRate.toString()
                                val estimateId = item.estimateId

                                add(
                                    CardItem(
                                        activity = activity,
                                        text = desc,
                                        qty = qty,
                                        rate = rate,
                                        estimateId = estimateId,
                                        workViewModel = workViewModel,
                                        jobItemEstimate = item,
                                        job = jobDTO
                                    )
                                )
                            } catch (t: Throwable) {
                                Timber.e(t, "Failed to create work-item")
                                val workError = XIError(t, t.message ?: XIErrorHandler.UNKNOWN_ERROR)
                                XIErrorHandler.handleError(
                                    view = this@WorkFragment.requireView(),
                                    throwable = workError,
                                    shouldToast = true
                                )
                            }
                        }
                    }
                })
                expandableGroups.add(this)
            }
        }
    }

    private fun scrollToTop(expandableGroups: MutableList<ExpandableGroup>, toggledGroup: ExpandableGroup) {
        val groupPosition = expandableGroups.indexOf(toggledGroup)
        (ui.veiledWorkListView.getRecyclerView().layoutManager as LinearLayoutManager)
            .scrollToPositionWithOffset(groupPosition, -20)
    }
}
