@file:Suppress("RemoveExplicitTypeArguments")

package za.co.xisystems.itis_rrm.ui.mainview.approvejobs

import android.app.Dialog
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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenStarted
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.skydoves.androidveil.VeiledItemOnClickListener
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import timber.log.Timber
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.R.layout
import za.co.xisystems.itis_rrm.base.BaseFragment
import za.co.xisystems.itis_rrm.constants.Constants.ONE_SECOND
import za.co.xisystems.itis_rrm.custom.errors.XIErrorHandler
import za.co.xisystems.itis_rrm.custom.results.XIError
import za.co.xisystems.itis_rrm.custom.results.XIResult
import za.co.xisystems.itis_rrm.custom.views.IndefiniteSnackbar
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.databinding.FragmentApprovejobBinding
import za.co.xisystems.itis_rrm.extensions.observeOnce
import za.co.xisystems.itis_rrm.ui.mainview.approvejobs.approve_job_item.ApproveJobItem
import za.co.xisystems.itis_rrm.ui.scopes.UiLifecycleScope
import za.co.xisystems.itis_rrm.utils.ActivityIdConstants
import za.co.xisystems.itis_rrm.utils.Coroutines

/**
 * Created by Francis Mahlava on 03,October,2019
 */

class ApproveJobsFragment : BaseFragment(), KodeinAware {

    override val kodein by kodein()
    private lateinit var approveViewModel: ApproveJobsViewModel
    private val factory: ApproveJobsViewModelFactory by instance<ApproveJobsViewModelFactory>()
    lateinit var dialog: Dialog
    private var uiScope = UiLifecycleScope()
    private var _ui: FragmentApprovejobBinding? = null
    private val ui get() = _ui!!
    private var groupAdapter = GroupAdapter<GroupieViewHolder>()
    private var queryObserver = Observer<XIResult<String>?> { handleQueryErrors(it) }

    private fun handleQueryErrors(outcome: XIResult<String>?) {
        outcome?.let { result ->
            when (result) {
                is XIError -> {
                    crashGuard(
                        view = this.requireView(),
                        throwable = result,
                        refreshAction = { retryFetchRemoteJobs() }
                    )
                }
                else -> {
                    Timber.d("ignored: $result")
                }
            }
        }
    }

    companion object {
        val TAG: String = ApproveJobsFragment::class.java.simpleName
    }

    init {
        lifecycleScope.launch {
            whenStarted {
                uiScope.onCreate()
                viewLifecycleOwner.lifecycle.addObserver(uiScope)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _ui = FragmentApprovejobBinding.inflate(inflater, container, false)
        return ui.root
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // No options for this fragment
        return false
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        approveViewModel = activity?.run {
            ViewModelProvider(this, factory).get(ApproveJobsViewModel::class.java)
        } ?: throw Exception("Invalid Activity")

        initVeiledRecyclerView()

        Coroutines.main {
            swipeToRefreshInit()
            protectedFetch(veiled = false, { fetchLocalJobs() }, { retryFetchRemoteJobs() })
        }
    }

    private fun initVeiledRecyclerView() {
        ui.approveJobVeiledRecycler.run {
            setVeilLayout(layout.single_job_listing, object : VeiledItemOnClickListener {
                /** will be invoked when the item on the [VeilRecyclerFrameView] clicked. */
                override fun onItemClicked(pos: Int) {
                    Toast.makeText(this@ApproveJobsFragment.requireContext(), "Loading ...", Toast.LENGTH_SHORT).show()
                }
            })
            setAdapter(groupAdapter)
            setLayoutManager(LinearLayoutManager(this.context))
            addVeiledItems(10)
        }
    }

    private fun protectedFetch(
        veiled: Boolean = false,
        fetchQuery: suspend () -> Unit = {},
        retryAction: suspend () -> Unit = {}
    ) = uiScope.launch(uiScope.coroutineContext) {
        try {
            if (veiled) {
                ui.approveJobVeiledRecycler.veil()
            }
            approveViewModel.workflowState.observe(viewLifecycleOwner, queryObserver)
            fetchQuery()
        } catch (t: Throwable) {
            ui.approveJobVeiledRecycler.unVeil()
            val xiFail = XIError(t, t.message ?: XIErrorHandler.UNKNOWN_ERROR)
            crashGuard(
                view = this@ApproveJobsFragment.requireView(),
                throwable = xiFail,
                refreshAction = { uiScope.launch { retryAction() } }
            )
        } finally {
            if (veiled) delayedUnveil()
            approveViewModel.workflowState.removeObserver(queryObserver)
        }
    }

    private suspend fun fetchLocalJobs() {

        val jobs = approveViewModel.getJobsForActivityId(ActivityIdConstants.JOB_APPROVE)
        jobs.observe(viewLifecycleOwner, { jobList ->

            if (jobList.isNullOrEmpty()) {
                ui.approveJobVeiledRecycler.visibility = View.GONE
                ui.noData.visibility = View.VISIBLE
            } else {
                ui.noData.visibility = View.GONE
                ui.approveJobVeiledRecycler.visibility = View.VISIBLE
                val jItems = jobList.distinctBy {
                    it.JobId
                }
                initRecyclerView(jItems.toApproveListItems())
            }
        })
    }

    private fun swipeToRefreshInit() {
        ui.jobsSwipeToRefresh.setProgressBackgroundColorSchemeColor(
            ContextCompat.getColor(
                requireContext().applicationContext,
                R.color.colorPrimary
            )
        )

        ui.jobsSwipeToRefresh.setColorSchemeColors(Color.WHITE)

        ui.jobsSwipeToRefresh.setOnRefreshListener {
            uiScope.launch(uiScope.coroutineContext) {
                protectedFetch(veiled = true, { fetchJobsFromServices() },
                    { retryFetchRemoteJobs() }
                )
            }
        }
    }

    private suspend fun fetchJobsFromServices() {
        try {
            val freshJobs = approveViewModel.offlineUserTaskList.await()
            freshJobs.observeOnce(viewLifecycleOwner, {
                ui.jobsSwipeToRefresh.isRefreshing = false
                protectedFetch(veiled = true, { fetchLocalJobs() }, { retryFetchRemoteJobs() })
            })
        } catch (throwable: Throwable) {
            val message = "Failed to retrieve remote jobs: ${throwable.message ?: XIErrorHandler.UNKNOWN_ERROR}"
            Timber.e(throwable, message)
            throw throwable
        }
    }

    private fun delayedUnveil() {
        Handler(Looper.getMainLooper()).postDelayed(
            {
                if (!activity?.isFinishing!!) {
                    ui.approveJobVeiledRecycler.unVeil()
                }
            },
            ONE_SECOND
        )
    }

    private fun retryFetchRemoteJobs() = uiScope.launch {
        IndefiniteSnackbar.hide()
        protectedFetch(veiled = true, { fetchJobsFromServices() })
    }

    private fun initRecyclerView(
        approveJobListItems: List<ApproveJobItem>
    ) {
        groupAdapter = GroupAdapter<GroupieViewHolder>().apply {
            addAll(approveJobListItems)
            notifyDataSetChanged()
        }

        ui.approveJobVeiledRecycler.setLayoutManager(LinearLayoutManager(this.context))
        ui.approveJobVeiledRecycler.setAdapter(groupAdapter)

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
        super.onDestroyView()
        approveViewModel.workflowState.removeObservers(viewLifecycleOwner)
        ui.approveJobVeiledRecycler.setAdapter(null)
        uiScope.destroy()
        _ui = null
    }
}
