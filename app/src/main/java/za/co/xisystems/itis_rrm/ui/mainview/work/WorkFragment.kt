/*
 * Updated by Shaun McDonald on 2021/02/15
 * Last modified on 2021/02/15 12:45 AM
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 */

@file:Suppress("KDocUnresolvedReference")

package za.co.xisystems.itis_rrm.ui.mainview.work

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.view.doOnNextLayout
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenCreated
import androidx.lifecycle.whenResumed
import androidx.recyclerview.widget.LinearLayoutManager
import com.xwray.groupie.ExpandableGroup
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.viewbinding.GroupieViewHolder
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.instance
import timber.log.Timber
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.base.LocationFragment
import za.co.xisystems.itis_rrm.custom.errors.XIErrorHandler
import za.co.xisystems.itis_rrm.custom.results.XIResult
import za.co.xisystems.itis_rrm.custom.views.IndefiniteSnackbar
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimateDTO
import za.co.xisystems.itis_rrm.databinding.FragmentWorkBinding
import za.co.xisystems.itis_rrm.databinding.ItemExpandableHeaderBinding
import za.co.xisystems.itis_rrm.extensions.observeOnce
import za.co.xisystems.itis_rrm.extensions.uomForUI
import za.co.xisystems.itis_rrm.ui.extensions.crashGuard
import za.co.xisystems.itis_rrm.ui.mainview.activities.main.MainActivity
import za.co.xisystems.itis_rrm.ui.mainview.work.estimate_work_item.CardItem
import za.co.xisystems.itis_rrm.ui.mainview.work.estimate_work_item.ExpandableHeaderWorkItem
import za.co.xisystems.itis_rrm.ui.mainview.work.goto_work_location.GoToViewModel
import za.co.xisystems.itis_rrm.ui.mainview.work.goto_work_location.GoToViewModelFactory
import za.co.xisystems.itis_rrm.utils.ActivityIdConstants
import za.co.xisystems.itis_rrm.utils.Coroutines
import java.text.DecimalFormat

const val INSET_TYPE_KEY = "inset_type"
const val INSET = "inset"
const val JOB_ID = "jobId"

class WorkFragment : LocationFragment() {

    private lateinit var workViewModel: WorkViewModel
    private lateinit var expandableGroups: MutableList<ExpandableGroup>
    private val factory: WorkViewModelFactory by instance()
    private val mapFactory: GoToViewModelFactory by instance()
    private lateinit var goToViewModel: GoToViewModel
    private var groupAdapter: GroupAdapter<GroupieViewHolder<ItemExpandableHeaderBinding>>? =
        GroupAdapter<GroupieViewHolder<ItemExpandableHeaderBinding>>()
    private var _ui: FragmentWorkBinding? = null
    private val ui get() = _ui!!
    private lateinit var currentJobGroup: ExpandableGroup
    private var stateRestored = false
    private var jobId: String? = null

    init {

        lifecycleScope.launch {
            whenCreated {
                stateRestored = false
            }

            whenResumed {
                if (this@WorkFragment::currentJobGroup.isInitialized && !currentJobGroup.isExpanded) {
                    currentJobGroup.onToggleExpanded()
                }
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            /**
             * Callback for handling the [OnBackPressedDispatcher.onBackPressed] event.
             */
            override fun handleOnBackPressed() {
                Intent(requireContext(), MainActivity::class.java).also { home ->
                    home.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(home)
                }
//                val directions = WorkFragmentDirections.actionGlobalNavHome()
//                Navigation.findNavController(this@WorkFragment.requireView()).navigate(directions)
            }
        }
        requireActivity().onBackPressedDispatcher
            .addCallback(this, callback)
    }

    private fun refreshEstimateJobsFromLocal() = uiScope.launch(dispatchers.ui()) {
        val localJobs = workViewModel.getAllWork()
        localJobs?.observe(viewLifecycleOwner, { jobsList ->
            _ui?.group7Loading?.visibility = View.GONE
            if (jobsList.isNullOrEmpty()) {
                _ui?.veiledWorkListView?.visibility = View.GONE
                _ui?.noData?.visibility = View.VISIBLE
            } else {
                _ui?.veiledWorkListView?.visibility = View.VISIBLE
                _ui?.noData?.visibility = View.GONE
                val headerItems = jobsList.distinctBy {
                    it.jobId
                }
                Coroutines.ui {
                    this@WorkFragment.initRecyclerView(headerItems.toWorkListItems())
                }
            }
        })
    }

    private val backClickListener = View.OnClickListener {
        setBackPressed()
    }

    private fun setBackPressed() {
        Intent(requireContext(), MainActivity::class.java).also { home ->
            home.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(home)
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _ui = FragmentWorkBinding.inflate(inflater, container, false)
        _ui?.toolbar?.apply {
            setOnBackClickListener(backClickListener)
        }
        return ui.root
    }

    private fun initVeiledRecycler() {
        _ui?.veiledWorkListView?.veil()
        _ui?.veiledWorkListView?.run {
            setVeilLayout(R.layout.item_velied_slug) {
                Toast.makeText(
                    this@WorkFragment.requireContext(),
                    "Loading ...", Toast.LENGTH_SHORT
                ).show()
            }
            setAdapter(groupAdapter)
            setLayoutManager(LinearLayoutManager(this.context))
            addVeiledItems(15)
        }
    }

    private fun initSwipeToRefresh() {

        _ui?.worksSwipeToRefresh?.setProgressBackgroundColorSchemeColor(
            ContextCompat.getColor(
                requireContext().applicationContext,
                R.color.colorPrimary
            )
        )

        _ui?.worksSwipeToRefresh?.setColorSchemeColors(Color.WHITE)

        _ui?.worksSwipeToRefresh?.setOnRefreshListener {
            fetchJobsFromService()
        }
    }

    private fun fetchJobsFromService() = uiScope.launch(uiScope.coroutineContext) {
        try {
            toggleLongRunning(true)
            _ui?.veiledWorkListView?.veil()
            refreshUserTaskListFromApi()
            refreshEstimateJobsFromLocal()
        } catch (t: Throwable) {
            Timber.e(t, t.localizedMessage ?: XIErrorHandler.UNKNOWN_ERROR)
            val jobErr = XIResult.Error(t, "Failed to fetch jobs from service")
            crashGuard(
                throwable = jobErr,
                refreshAction = { this@WorkFragment.retryFetchingJobs() }
            )
        } finally {
            _ui?.worksSwipeToRefresh?.isRefreshing = false
            toggleLongRunning(false)
        }
    }

    private fun retryFetchingJobs() {
        IndefiniteSnackbar.hide()
        fetchJobsFromService()
    }

    private suspend fun refreshUserTaskListFromApi() {
        withContext(uiScope.coroutineContext) {
            try {
                val jobs = workViewModel.offlineUserTaskList.await()
                jobs.observeOnce(viewLifecycleOwner) { works ->
                    Timber.d("${works.size} / ${works.count()} loaded.")
                }
            } catch (t: Throwable) {
                val message = "Failed to fetch jobs from service: ${t.message ?: XIErrorHandler.UNKNOWN_ERROR}"
                Timber.e(t, message)
                throw t
            }
        }
    }

    private fun initRecyclerView(workListItems: List<ExpandableGroup>) {
        val groupAdapter = GroupAdapter<GroupieViewHolder<ItemExpandableHeaderBinding>>().apply {
            clear()
            addAll(workListItems)
            toggleLongRunning(false)
        }

        val layoutManager = LinearLayoutManager(this.requireContext())
        _ui?.veiledWorkListView?.setLayoutManager(layoutManager)
        _ui?.veiledWorkListView?.setAdapter(groupAdapter)
        _ui?.veiledWorkListView?.doOnNextLayout { _ui?.veiledWorkListView?.unVeil() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        workViewModel = ViewModelProvider(this.requireActivity(), factory)[WorkViewModel::class.java]
        goToViewModel = ViewModelProvider(this.requireActivity(), mapFactory)[GoToViewModel::class.java]
        setHasOptionsMenu(true)
    }

    private fun searchLocalJobs(query: String) = uiScope.launch(uiScope.coroutineContext) {
        initVeiledRecycler()
        val searchQuery = workViewModel.getSearchResults()
        searchQuery.observe(viewLifecycleOwner) { searchResults ->
            if (searchResults.isNullOrEmpty()) {
                _ui?.veiledWorkListView?.visibility = View.GONE
                _ui?.noData?.visibility = View.VISIBLE
            } else {
                Coroutines.main {
                    _ui?.veiledWorkListView?.visibility = View.VISIBLE
                    _ui?.noData?.visibility = View.GONE
                    val headerItems = searchResults.distinctBy {
                        it.jobId
                    }
                    this@WorkFragment.initRecyclerView(headerItems.toWorkListItems())
                }
            }
        }
        workViewModel.searchJobs(query)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _ui?.toolbar?.setTitle("Select Estimate Start To Work")
        if (savedInstanceState != null && !stateRestored) {
            onRestoreInstanceState(savedInstanceState)
            stateRestored = true
        }

        if (!stateRestored) {
            Timber.d("No Job Selected!")
        }
    }

    /**
     * Called when the Fragment is visible to the user.  This is generally
     * tied to [Activity.onStart] of the containing
     * Activity's lifecycle.
     */
    override fun onStart() {
        super.onStart()
        initSwipeToRefresh()
        initVeiledRecycler()
    }

    override fun onResume() {
        super.onResume()
        uiScope.launch(uiScope.coroutineContext) {
            try {
                toggleLongRunning(true)
                refreshEstimateJobsFromLocal()
            } catch (t: Throwable) {
                Timber.e(t, "Failed to fetch local jobs")
                val xiFail = XIResult.Error(t, t.message ?: XIErrorHandler.UNKNOWN_ERROR)
                crashGuard(
                    throwable = xiFail,
                    refreshAction = { retryFetchingJobs() }
                )
            } finally {
                toggleLongRunning(false)
            }
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        val item = menu.findItem(R.id.action_settings)
        val item1 = menu.findItem(R.id.action_logout)
        val item2 = menu.findItem(R.id.action_search)
        if (item != null) item.isVisible = false
        if (item1 != null) item1.isVisible = false
        if (item2 != null) {
            item2.isVisible = true
            val searchView = item2.actionView as SearchView
            searchView.queryHint = getString(R.string.jino_or_desc_search_hint)
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                // Search is triggered when the user clicks on the search button
                override fun onQueryTextSubmit(query: String?): Boolean {
                    when (query.isNullOrBlank()) {
                        true -> refreshEstimateJobsFromLocal()
                        else -> searchLocalJobs(query)
                    }
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    when (newText.isNullOrBlank()) {
                        true -> {
                            refreshEstimateJobsFromLocal()
                        }
                        else -> {
                            // No-op
                        }
                    }
                    return false
                }
            })
            searchView.setOnCloseListener {
                refreshEstimateJobsFromLocal()
                false
            }
        }
    }

    override fun onDestroyView() {
        _ui?.veiledWorkListView?.getRecyclerView()?.adapter = null
        groupAdapter = null
        _ui = null
        super.onDestroyView()
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
                estimates.forEach { item ->
                    createCardItem(item, jobDTO)
                }
                jobId?.let { selectedJobId ->
                    if (selectedJobId == jobDTO.jobId) {
                        currentJobGroup = this
                    }
                }
                expandableGroups.add(this)
            }
        }
    }

    private fun ExpandableGroup.createCardItem(
        item: JobItemEstimateDTO,
        jobDTO: JobDTO
    ) {
        val receiver = this
        uiScope.launch(uiScope.coroutineContext) {
            try {
//              val desc = workViewModel.getDescForProjectItemId(item.projectItemId!!)
                val projectItem = workViewModel.getProjectItemForProjectItemId(item.projectItemId!!)

//                ToastUtils().toastShort(requireContext(), sectionItem.description!!)

                val uom = workViewModel.getUOMForProjectItemId(item.projectItemId!!)
                val qty = item.qty.toString()
                val rate = item.lineRate
                val estimateId = item.estimateId
                val friendlyUOM = if (uom.isNullOrEmpty()) { "each" } else {
                    this@WorkFragment.requireContext().uomForUI(uom)
                }

                val cardItem = CardItem(
                    activity = activity,
                    projectItem = projectItem,
                    qty = "$qty $friendlyUOM",// @ ${DecimalFormat("#0.00").format(rate)} $friendlyUOM",
                    rate = DecimalFormat("#0.00").format(rate * qty.toDouble()),
                    estimateId = estimateId,
                    workViewModel = workViewModel,
                    jobItemEstimate = item,
                    job = jobDTO,
                    myLocation = currentLocation
                )
                receiver.add(
                    cardItem
                )
            } catch (t: Throwable) {
                val message = "Could not load JI ${jobDTO.jiNo}\nPlease report this to tech support."
                Timber.e(t, message)
                val workError = XIResult.Error(t, message)
                XIErrorHandler.handleError(
                    view = this@WorkFragment.requireView(),
                    throwable = workError,
                    shouldToast = true
                )
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.run {
            if (!this@WorkFragment.jobId.isNullOrBlank()) {
                putString(JOB_ID, this@WorkFragment.jobId)
            }
        }
        super.onSaveInstanceState(outState)
    }

    private fun onRestoreInstanceState(inState: Bundle) {
        jobId = inState.getString(JOB_ID, null)
    }

    private fun scrollToTop(expandableGroups: MutableList<ExpandableGroup>, toggledGroup: ExpandableGroup) {
        val groupPosition = expandableGroups.indexOf(toggledGroup)
        (_ui?.veiledWorkListView?.getRecyclerView()?.layoutManager as LinearLayoutManager)
            .scrollToPositionWithOffset(groupPosition, -20)
    }
}
