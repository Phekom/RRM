/**
 * Updated by Shaun McDonald on 2021/05/17
 * Last modified on 2021/05/17, 15:59
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 **/

@file:Suppress("RemoveExplicitTypeArguments")

package za.co.xisystems.itis_rrm.ui.mainview.approvejobs

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.view.doOnNextLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenStarted
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.coroutines.launch
import org.kodein.di.DIAware
import org.kodein.di.android.x.closestDI
import org.kodein.di.instance
import timber.log.Timber
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.base.BaseFragment
import za.co.xisystems.itis_rrm.custom.errors.XIErrorAction
import za.co.xisystems.itis_rrm.custom.errors.XIErrorHandler
import za.co.xisystems.itis_rrm.custom.results.XIResult
import za.co.xisystems.itis_rrm.custom.views.IndefiniteSnackbar
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.databinding.FragmentApprovejobBinding
import za.co.xisystems.itis_rrm.extensions.observeOnce
import za.co.xisystems.itis_rrm.ui.extensions.crashGuard
import za.co.xisystems.itis_rrm.ui.mainview.approvejobs.approve_job_item.ApproveJobItem
import za.co.xisystems.itis_rrm.ui.scopes.UiLifecycleScope
import za.co.xisystems.itis_rrm.utils.ActivityIdConstants
import za.co.xisystems.itis_rrm.utils.Coroutines
import java.lang.ref.WeakReference

/**
 * Created by Francis Mahlava on 03,October,2019
 */

class ApproveJobsFragment : BaseFragment(), DIAware {

    override val di by closestDI()
    private lateinit var approveViewModel: ApproveJobsViewModel
    private val factory: ApproveJobsViewModelFactory by instance<ApproveJobsViewModelFactory>()
    lateinit var dialog: Dialog
    private var uiScope = UiLifecycleScope()
    private var _ui: FragmentApprovejobBinding? = null
    private val ui get() = _ui!!
    private var groupAdapter = GroupAdapter<GroupieViewHolder>()
    private var queryObserver = Observer<XIResult<String>?> { handleQueryErrors(it) }

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

    private fun handleQueryErrors(outcome: XIResult<String>?) {
        outcome?.let { result ->
            when (result) {
                is XIResult.Error -> {
                    toggleLongRunning(false)
                    ui.approveJobVeiledRecycler.unVeil()
                    crashGuard(
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initVeiledRecyclerView()

        Coroutines.main {
            swipeToRefreshInit()
            fetchJobsFromServices()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        approveViewModel = activity?.run {
            ViewModelProvider(this, factory).get(ApproveJobsViewModel::class.java)
        } ?: throw Exception("Invalid Activity")
    }

    private fun initVeiledRecyclerView() {
        ui.approveJobVeiledRecycler.run {
            setVeilLayout(R.layout.item_velied_slug) {
                Toast.makeText(
                    this@ApproveJobsFragment.requireContext(),
                    "Loading ...", Toast.LENGTH_SHORT
                ).show()
            }
            setAdapter(groupAdapter)
            setLayoutManager(LinearLayoutManager(this.context))
            addVeiledItems(15)
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
                toggleLongRunning(true)
            }
            approveViewModel.workflowState.observe(viewLifecycleOwner, queryObserver)
            fetchQuery()
        } catch (t: Throwable) {
            val xiFail = XIResult.Error(t, t.message ?: XIErrorHandler.UNKNOWN_ERROR)
            crashGuard(
                throwable = xiFail,
                refreshAction = { uiScope.launch { retryAction() } }
            )
        } finally {
            approveViewModel.workflowState.removeObserver(queryObserver)
            toggleLongRunning(false)
            ui.approveJobVeiledRecycler.unVeil()
        }
    }

    private suspend fun fetchLocalJobs() {

        val jobs = approveViewModel.getJobsForActivityId(ActivityIdConstants.JOB_APPROVE)
        jobs.observe(viewLifecycleOwner, { jobList ->

            if (jobList.isNullOrEmpty()) {
                ui.approveJobVeiledRecycler.unVeil()
                ui.approveJobVeiledRecycler.visibility = View.GONE
                ui.noData.visibility = View.VISIBLE
            } else {
                ui.noData.visibility = View.GONE
                ui.approveJobVeiledRecycler.visibility = View.VISIBLE
                val jItems = jobList.distinctBy {
                    it.jobId
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
                protectedFetch(
                    veiled = true, { fetchJobsFromServices() },
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
            val xiFail = XIResult.Error(throwable, message)
            toggleLongRunning(false)
            ui.approveJobVeiledRecycler.unVeil()
            val xiAction = XIErrorAction(
                fragmentReference = WeakReference(this),
                view = this.requireView(),
                throwable = xiFail,
                shouldToast = false,
                shouldShowSnackBar = true,
                refreshAction = { retryFetchRemoteJobs() }
            )
            XIErrorHandler.handleError(xiAction)
        }
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
            notifyItemRangeChanged(0, approveJobListItems.size)
        }

        ui.approveJobVeiledRecycler.setLayoutManager(LinearLayoutManager(this.context))
        ui.approveJobVeiledRecycler.setAdapter(groupAdapter)
        ui.approveJobVeiledRecycler.doOnNextLayout {
            ui.approveJobVeiledRecycler.unVeil()
            toggleLongRunning(false)
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

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                this@ApproveJobsFragment.findNavController().popBackStack(R.id.nav_home, false)
            }
        }
        requireActivity().onBackPressedDispatcher
            .addCallback(this, callback)
    }
}
