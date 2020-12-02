@file:Suppress("KDocUnresolvedReference")

package za.co.xisystems.itis_rrm.ui.mainview.work

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenStarted
import androidx.recyclerview.widget.LinearLayoutManager
import com.xwray.groupie.ExpandableGroup
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.fragment_work.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
import za.co.xisystems.itis_rrm.ui.mainview.work.estimate_work_item.CardItem
import za.co.xisystems.itis_rrm.ui.mainview.work.estimate_work_item.ExpandableHeaderWorkItem
import za.co.xisystems.itis_rrm.ui.scopes.UiLifecycleScope
import za.co.xisystems.itis_rrm.utils.ActivityIdConstants

const val INSET_TYPE_KEY = "inset_type"
const val INSET = "inset"

class WorkFragment : BaseFragment(R.layout.fragment_work), KodeinAware {

    override val kodein by kodein()
    private lateinit var workViewModel: WorkViewModel
    private lateinit var expandableGroups: MutableList<ExpandableGroup>
    private val factory: WorkViewModelFactory by instance()
    private var uiScope = UiLifecycleScope()
    private lateinit var layoutManager: LinearLayoutManager

    init {

        lifecycleScope.launch {

            whenStarted {

                uiScope.onCreate()
                viewLifecycleOwner.lifecycle.addObserver(uiScope)

                uiScope.launch(uiScope.coroutineContext) {
                    try {
                        refreshEstimateJobsFromLocal()
                    } catch (t: Throwable) {
                        Timber.e(t, "Failed to fetch local jobs")
                        val xiFail = XIError(t, t.localizedMessage ?: XIErrorHandler.UNKNOWN_ERROR)
                        crashGuard(
                            view = this@WorkFragment.requireView(),
                            throwable = xiFail,
                            refreshAction = { retryFetchingJobs() })
                    }
                }
            }
        }
    }

    private suspend fun refreshEstimateJobsFromLocal() {

        withContext(uiScope.coroutineContext) {
            group7_loading.visibility = View.VISIBLE
            workViewModel.getJobsForActivityId(
                ActivityIdConstants.JOB_APPROVED,
                ActivityIdConstants.ESTIMATE_INCOMPLETE
            ).observeOnce(viewLifecycleOwner, { jobsList ->
                group7_loading.visibility = View.GONE
                if (jobsList.isNullOrEmpty()) {
                    work_listView.visibility = View.GONE
                    noData.visibility = View.VISIBLE
                } else {
                    work_listView.visibility = View.VISIBLE
                    noData.visibility = View.GONE

                    val headerItems = jobsList.distinctBy {
                        it.JobId
                    }

                    uiScope.launch(uiScope.coroutineContext) {
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
    ): View? {
        return inflater.inflate(R.layout.fragment_work, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {

        super.onActivityCreated(savedInstanceState)

        workViewModel = activity?.run {
            ViewModelProvider(this, factory).get(WorkViewModel::class.java)
        } ?: throw Exception("Invalid Activity")

        initSwipeToRefresh()
    }

    private fun initSwipeToRefresh() {

        works_swipe_to_refresh.setProgressBackgroundColorSchemeColor(
            ContextCompat.getColor(
                requireContext().applicationContext,
                R.color.colorPrimary
            )
        )

        works_swipe_to_refresh.setColorSchemeColors(Color.WHITE)

        works_swipe_to_refresh.setOnRefreshListener {
            fetchJobsFromService()
        }
    }

    private fun fetchJobsFromService() {
        uiScope.launch(uiScope.coroutineContext) {
            try {
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
                works_swipe_to_refresh.isRefreshing = false
            }
        }
    }

    private fun retryFetchingJobs() {
        IndefiniteSnackbar.hide()
        fetchJobsFromService()
    }

    private suspend fun refreshUserTaskListFromApi() = uiScope.launch(uiScope.coroutineContext) {
        // This definitely needs to be a one-shot operation
        withContext(uiScope.coroutineContext) {
            val jobs = workViewModel.offlineUserTaskList.await()
            jobs.observeOnce(viewLifecycleOwner, { works ->
                Timber.d("${works.size} / ${works.count()} loaded.")
            })
        }
    }

    private fun initRecyclerView(
        workListItems: List<ExpandableGroup>
    ) {
        val groupAdapter = GroupAdapter<GroupieViewHolder>().apply {
            clear()
            update(workListItems)
        }

        work_listView.apply {
            layoutManager = LinearLayoutManager(this.context)
            adapter = groupAdapter
            work_listView.itemAnimator = null
        }
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
        uiScope.destroy()
        work_listView?.adapter = null
        super.onDestroyView()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    private suspend fun List<JobDTO>.toWorkListItems(): List<ExpandableGroup> {
        // Initialize Expandable group with expandable item and specify whether it should be expanded by default or not
        expandableGroups = mutableListOf()
        return this.map { jobDTO ->

            val expandableHeaderItem =
                ExpandableHeaderWorkItem(activity, jobDTO, workViewModel)
            ExpandableGroup(expandableHeaderItem, false).apply {
                expandableGroups.add(this)
                // When expanding a work item, collapse the others
                // and scrollToPositionWithOffset it to the top
                expandableHeaderItem.onExpandListener = { toggledGroup ->
                    expandableGroups.forEach {
                        if (it != toggledGroup && it.isExpanded) {
                            it.onToggleExpanded()
                        }
                    }
                    layoutManager.scrollToPositionWithOffset(2, 20)
                }

                val estimates = workViewModel.getJobEstimationItemsForJobId(
                    jobDTO.JobId,
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
                                val workError = XIError(t, t.localizedMessage ?: XIErrorHandler.UNKNOWN_ERROR)
                                XIErrorHandler.handleError(
                                    fragment = this@WorkFragment,
                                    view = this@WorkFragment.requireView(),
                                    throwable = workError,
                                    shouldToast = true
                                )
                            }
                        }
                    }
                })
            }
        }
    }
}
