/**
 * Updated by Shaun McDonald on 2021/05/17
 * Last modified on 2021/05/17, 15:59
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 **/

@file:Suppress(
    "RemoveExplicitTypeArguments"
)

package za.co.xisystems.itis_rrm.ui.mainview.approvemeasure

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.view.doOnNextLayout
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.distinctUntilChanged
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.viewbinding.GroupieViewHolder
import org.kodein.di.DIAware
import org.kodein.di.android.x.closestDI
import org.kodein.di.instance
import timber.log.Timber
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.base.BaseFragment
import za.co.xisystems.itis_rrm.custom.errors.XIErrorHandler
import za.co.xisystems.itis_rrm.custom.results.XIResult
import za.co.xisystems.itis_rrm.custom.views.IndefiniteSnackbar
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemMeasureDTO
import za.co.xisystems.itis_rrm.databinding.FragmentApprovemeasureBinding
import za.co.xisystems.itis_rrm.databinding.ItemHeaderBinding
import za.co.xisystems.itis_rrm.extensions.observeOnce
import za.co.xisystems.itis_rrm.ui.extensions.crashGuard
import za.co.xisystems.itis_rrm.ui.mainview.approvemeasure.approveMeasure_Item.ApproveMeasureItem
import za.co.xisystems.itis_rrm.utils.ActivityIdConstants
import za.co.xisystems.itis_rrm.utils.Coroutines

/**
 * Created by Francis Mahlava on 03,October,2019
 */

@Suppress("KDocUnresolvedReference")
class ApproveMeasureFragment : BaseFragment(), DIAware {

    private lateinit var approveViewModel: ApproveMeasureViewModel
    private val factory: ApproveMeasureViewModelFactory by instance<ApproveMeasureViewModelFactory>()

    private var _binding: FragmentApprovemeasureBinding? = null
    private val binding get() = _binding!!
    private var groupAdapter = GroupAdapter<GroupieViewHolder<ItemHeaderBinding>>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentApprovemeasureBinding.inflate(inflater, container, false)
        return binding.root
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        approveViewModel =
            ViewModelProvider(this.requireActivity(), factory)[ApproveMeasureViewModel::class.java]
    }

    override fun onStart() {
        super.onStart()
        swipeToRefreshInit()
        initVeiledRecyclerView()
    }

    override fun onResume() {
        super.onResume()
        fetchJobsFromService()
    }

    private fun initVeiledRecyclerView() {
        _binding?.approveMeasurementsList?.run {
            setVeilLayout(R.layout.item_velied_slug) { toast("Loading ...") }
            setAdapter(groupAdapter)
            setLayoutManager(LinearLayoutManager(this.context))
            addVeiledItems(15)
        }
    }

    private fun retryFetchMeasurements() {
        IndefiniteSnackbar.hide()
        _binding?.approveMeasurementsList?.veil()
        fetchJobsFromService()
    }

    private fun loadJobHeaders() {

        Coroutines.main {
            try {
                _binding?.approveMeasurementsList?.visibility = View.VISIBLE
                _binding?.approveMeasurementsList?.veil()
                val measurementsSubscription =
                    approveViewModel.getJobApproveMeasureForActivityId(ActivityIdConstants.MEASURE_COMPLETE)

                measurementsSubscription.distinctUntilChanged().observeOnce(viewLifecycleOwner) { measurementData ->

                    if (measurementData.isNullOrEmpty()) {
                        _binding?.noData?.visibility = View.VISIBLE
                        _binding?.approveMeasurementsList?.visibility = View.GONE
                        _binding?.approveMeasurementsList?.unVeil()
                    } else {
                        _binding?.noData?.visibility = View.GONE
                        _binding?.approveMeasurementsList?.visibility = View.VISIBLE

                        val jobHeaders = measurementData.distinctBy {
                            it.jobId
                        }
                        initRecyclerView(jobHeaders.toApproveListItems())
                    }
                }
            } catch (t: Throwable) {
                Timber.e(t, "Unable to fetch Measurements")
                val measureErr = XIResult.Error(t, t.message ?: XIErrorHandler.UNKNOWN_ERROR)
                toggleLongRunning(false)
                _binding?.approveMeasurementsList?.unVeil()
                crashGuard(
                    throwable = measureErr,
                    refreshAction = { this.retryFetchMeasurements() }
                )
            }
        }
    }

    private fun swipeToRefreshInit() {
        _binding?.approvemSwipeToRefresh?.setProgressBackgroundColorSchemeColor(
            ContextCompat.getColor(
                requireContext().applicationContext,
                R.color.colorPrimary
            )
        )

        _binding?.approvemSwipeToRefresh?.setColorSchemeColors(Color.WHITE)

        _binding?.approvemSwipeToRefresh?.setOnRefreshListener {
            _binding?.approveMeasurementsList?.veil()
            fetchJobsFromService()
        }
    }

    private fun fetchJobsFromService() {
        Coroutines.main {
            try {
                val freshJobs = approveViewModel.offlineUserTaskList.await()
                freshJobs.distinctUntilChanged().observeOnce(viewLifecycleOwner) {
                    if (it.isNullOrEmpty()) {

                        _binding?.noData?.visibility = View.VISIBLE
                        _binding?.approveMeasurementsList?.visibility = View.GONE
                    } else {
                        loadJobHeaders()
                    }
                }
            } catch (t: Throwable) {
                Timber.e(t, "Unable to fetch remote jobs")
                val measureErr = XIResult.Error(t, t.message ?: XIErrorHandler.UNKNOWN_ERROR)
                crashGuard(
                    throwable = measureErr,
                    refreshAction = { this.retryFetchMeasurements() }
                )
            } finally {
                _binding?.approvemSwipeToRefresh?.isRefreshing = false
            }
        }
    }

    private fun initRecyclerView(approveMeasureListItems: List<ApproveMeasureItem>) {

        val groupAdapter = GroupAdapter<GroupieViewHolder<ItemHeaderBinding>>().apply {
            clear()
            addAll(approveMeasureListItems)
            notifyItemRangeChanged(0, approveMeasureListItems.size)
        }

        _binding?.approveMeasurementsList?.run {
            setLayoutManager(LinearLayoutManager(this.context))
            setAdapter(adapter = groupAdapter)
            doOnNextLayout { _binding?.approveMeasurementsList?.unVeil() }
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
        _binding?.approveMeasurementsList?.setAdapter(null)
        _binding = null
    }

    private fun sendJobToApprove(
        job: ApproveMeasureItem?,
        view: View
    ) {
        Coroutines.main {
            job?.jobId?.let {
                approveViewModel.setJobIdForApproval(it)
                val navDirection = ApproveMeasureFragmentDirections
                    .actionNavApprovMeasureToMeasureApprovalFragment(it)
                Navigation.findNavController(view).navigate(navDirection)
            }
        }
    }

    private fun List<JobItemMeasureDTO>.toApproveListItems(): List<ApproveMeasureItem> {
        return this.map { approvejItems ->
            ApproveMeasureItem(approvejItems, approveViewModel)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            /**
             * Callback for handling the [OnBackPressedDispatcher.onBackPressed] event.
             */
            override fun handleOnBackPressed() {
                val direction = ApproveMeasureFragmentDirections.actionGlobalNavHome()
                Navigation.findNavController(this@ApproveMeasureFragment.requireView())
                    .navigate(direction)
            }
        }
        requireActivity().onBackPressedDispatcher
            .addCallback(this, callback)
    }
}
