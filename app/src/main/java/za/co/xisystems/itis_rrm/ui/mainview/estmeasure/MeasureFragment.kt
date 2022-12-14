/*
 * Updated by Shaun McDonald on 2021/01/30
 * Last modified on 2021/01/30 6:41 AM
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 */

package za.co.xisystems.itis_rrm.ui.mainview.estmeasure

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
import androidx.core.content.ContextCompat
import androidx.core.view.doOnNextLayout
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.distinctUntilChanged
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.viewbinding.GroupieViewHolder
import org.kodein.di.instance
import timber.log.Timber
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.base.BaseFragment
import za.co.xisystems.itis_rrm.custom.errors.XIErrorHandler
import za.co.xisystems.itis_rrm.custom.results.XIResult
import za.co.xisystems.itis_rrm.custom.views.IndefiniteSnackbar
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimateDTO
import za.co.xisystems.itis_rrm.databinding.FragmentEstmeasureBinding
import za.co.xisystems.itis_rrm.databinding.ItemHeaderBinding
import za.co.xisystems.itis_rrm.extensions.observeOnce
import za.co.xisystems.itis_rrm.ui.extensions.crashGuard
import za.co.xisystems.itis_rrm.ui.mainview.activities.main.MainActivity
import za.co.xisystems.itis_rrm.ui.mainview.estmeasure.estimate_measure_item.EstimateMeasureItem
import za.co.xisystems.itis_rrm.utils.ActivityIdConstants
import za.co.xisystems.itis_rrm.utils.Coroutines

@Suppress("KDocUnresolvedReference")
class MeasureFragment : BaseFragment() {

    private lateinit var measureViewModel: MeasureViewModel
    private val factory: MeasureViewModelFactory by instance()
    private var _ui: FragmentEstmeasureBinding? = null
    private val ui get() = _ui!!
    private var groupAdapter = GroupAdapter<GroupieViewHolder<ItemHeaderBinding>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        measureViewModel =
            ViewModelProvider(this.requireActivity(), factory)[MeasureViewModel::class.java]
        setHasOptionsMenu(true)
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
//                Navigation.findNavController(this@MeasureFragment.requireView())
//                    .navigate(R.id.action_global_nav_home)
            }
        }
        requireActivity().onBackPressedDispatcher
            .addCallback(this, callback)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        val item = menu.findItem(R.id.action_settings)
        val item1 = menu.findItem(R.id.action_logout)
        if (item != null) item.isVisible = false
        if (item1 != null) item1.isVisible = false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _ui = FragmentEstmeasureBinding.inflate(inflater, container, false)
        _ui?.toolbar?.apply {
            setTitle("Select Estimate To Measure")
            setOnBackClickListener(backClickListener)
        }
        return ui.root
    }


    override fun onStart() {
        super.onStart()
        Coroutines.main {
            initVeiledRecycler()
            fetchEstimateMeasures()
            swipeToRefreshInit()
        }
    }

    private suspend fun fetchEstimateMeasures() {
        val itemEstimateData = measureViewModel.getJobMeasureForActivityId(
            ActivityIdConstants.ESTIMATE_MEASURE,
            ActivityIdConstants.JOB_ESTIMATE,
            ActivityIdConstants.MEASURE_PART_COMPLETE
        )

        itemEstimateData.observeOnce(viewLifecycleOwner) { itemEstimateList ->

            if (itemEstimateList.isNullOrEmpty()) {
                _ui?.noData?.visibility = View.VISIBLE
                _ui?.estimationsToBeMeasuredListView?.visibility = View.GONE
                fetchJobMeasures()
            } else {
                val jobHeaders = itemEstimateList.distinctBy { item ->
                    item.jobId
                }
                Timber.d("Estimate measures detected: ${jobHeaders.size}")
                _ui?.estimationsToBeMeasuredListView?.visibility = View.VISIBLE
                _ui?.noData?.visibility = View.GONE
                initRecyclerView(jobHeaders.toMeasureListItems())
            }
        }
    }

    private fun swipeToRefreshInit() {
        _ui?.estimationsSwipeToRefresh?.setProgressBackgroundColorSchemeColor(
            ContextCompat.getColor(
                requireContext().applicationContext,
                R.color.colorPrimary
            )
        )
        _ui?.estimationsSwipeToRefresh?.setColorSchemeColors(Color.WHITE)

        _ui?.estimationsSwipeToRefresh?.setOnRefreshListener {
            _ui?.estimationsToBeMeasuredListView?.veil()
            fetchRemoteJobs()
        }
    }

    private fun fetchRemoteJobs() {
        try {
            Coroutines.main {
                val jobs = measureViewModel.offlineUserTaskList.await()
                jobs.distinctUntilChanged().observeOnce(viewLifecycleOwner) { works ->
                    if (works.isNullOrEmpty()) {
                        _ui?.noData?.visibility = View.VISIBLE
                        _ui?.estimationsToBeMeasuredListView?.visibility = View.GONE
                    } else {
                        Coroutines.main {
                            _ui?.noData?.visibility = View.GONE
                            _ui?.estimationsToBeMeasuredListView?.visibility = View.VISIBLE
                            fetchEstimateMeasures()
                        }
                    }
                }
            }
        } catch (t: Throwable) {
            val fetchError = XIResult.Error(t, t.message ?: XIErrorHandler.UNKNOWN_ERROR)
            crashGuard(
                throwable = fetchError,
                refreshAction = { this@MeasureFragment.retryFetchingJobs() }
            )
        } finally {
            _ui?.estimationsSwipeToRefresh?.isRefreshing = false
        }
    }


    fun retryFetchingJobs() {
        IndefiniteSnackbar.hide()
        Coroutines.main {
            fetchRemoteJobs()
        }
    }

    private fun fetchJobMeasures() {
        Coroutines.main {
            val jobEstimateData = measureViewModel.getJobMeasureForActivityId(
                ActivityIdConstants.ESTIMATE_MEASURE,
                ActivityIdConstants.JOB_ESTIMATE,
                ActivityIdConstants.MEASURE_PART_COMPLETE
            )

            jobEstimateData.distinctUntilChanged().observeOnce(viewLifecycleOwner) { jos ->
                if (jos.isNullOrEmpty()) {
                    _ui?.noData?.visibility = View.VISIBLE
                    _ui?.estimationsToBeMeasuredListView?.visibility = View.GONE
                } else {
                    _ui?.noData?.visibility = View.GONE
                    _ui?.estimationsToBeMeasuredListView?.visibility = View.VISIBLE
                    val measureItems = jos.distinctBy {
                        it.jobId
                    }
                    Timber.d("Job measures detected: ${measureItems.size}")
                    initRecyclerView(measureItems.toMeasureListItems())
                }
            }
        }
    }

    private fun initRecyclerView(measureList: List<EstimateMeasureItem>) {
        groupAdapter = GroupAdapter<GroupieViewHolder<ItemHeaderBinding>>().apply {
            clear()
            update(measureList)
            notifyItemRangeChanged(0, measureList.size)
        }
        _ui?.estimationsToBeMeasuredListView?.run {
            setAdapter(adapter = groupAdapter, layoutManager = LinearLayoutManager(this.context))
            doOnNextLayout { unVeil() }
        }

        groupAdapter.setOnItemClickListener { item, view ->
            Coroutines.main {
                (item as? EstimateMeasureItem)?.let {
                    sendForApproval((it), view)
                }
            }
        }
    }

    private fun sendForApproval(
        measureItem: EstimateMeasureItem,
        view: View
    ) {

        Coroutines.main {
            measureViewModel.setMeasureItem(measureItem)
        }

        Navigation.findNavController(view)
            .navigate(R.id.action_nav_estMeasure_to_submitMeasureFragment)
    }

    private fun List<JobItemEstimateDTO>.toMeasureListItems(): List<EstimateMeasureItem> {
        return this.map { estimateDTO ->
            EstimateMeasureItem(estimateDTO, measureViewModel)
        }
    }

    private fun initVeiledRecycler() {
        _ui?.estimationsToBeMeasuredListView?.run {
            setVeilLayout(R.layout.item_velied_slug) {
                Toast.makeText(
                    this@MeasureFragment.requireContext(),
                    "Loading ...",
                    Toast.LENGTH_SHORT
                ).show()
            }
            setAdapter(groupAdapter)
            setLayoutManager(LinearLayoutManager(this.context))
            addVeiledItems(15)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _ui?.estimationsToBeMeasuredListView?.setAdapter(null)
        _ui = null
    }
}
