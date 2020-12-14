package za.co.xisystems.itis_rrm.ui.mainview.estmeasure

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
import kotlinx.android.synthetic.main.fragment_work.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import timber.log.Timber
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.base.BaseFragment
import za.co.xisystems.itis_rrm.constants.Constants
import za.co.xisystems.itis_rrm.custom.errors.XIErrorHandler
import za.co.xisystems.itis_rrm.custom.results.XIError
import za.co.xisystems.itis_rrm.custom.views.IndefiniteSnackbar
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimateDTO
import za.co.xisystems.itis_rrm.databinding.FragmentEstmeasureBinding
import za.co.xisystems.itis_rrm.extensions.observeOnce
import za.co.xisystems.itis_rrm.ui.mainview.estmeasure.estimate_measure_item.EstimateMeasureItem
import za.co.xisystems.itis_rrm.utils.ActivityIdConstants
import za.co.xisystems.itis_rrm.utils.Coroutines

class MeasureFragment : BaseFragment(R.layout.fragment_estmeasure), KodeinAware {

    override val kodein by kodein()
    private lateinit var measureViewModel: MeasureViewModel
    private val factory: MeasureViewModelFactory by instance()
    private var _ui: FragmentEstmeasureBinding? = null
    private val ui get() = _ui!!
    private var groupAdapter = GroupAdapter<GroupieViewHolder>()

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _ui = FragmentEstmeasureBinding.inflate(inflater, container, false)
        return ui.root
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        measureViewModel = activity?.run {
            ViewModelProvider(this, factory).get(MeasureViewModel::class.java)
        } ?: throw Exception("Invalid Activity")

        Coroutines.main {
            initVeiledRecycler()
            fetchEstimateMeasures()
            swipeToRefreshInit()
        }
    }

    private fun delayedUnveil() {
        Handler(Looper.getMainLooper()).postDelayed(
            {
                if (!activity?.isFinishing!!) {
                    ui.estimationsToBeMeasuredListView.unVeil()
                }
            },
            Constants.ONE_SECOND
        )
    }

    private suspend fun fetchEstimateMeasures() {
        val itemEstimateData = measureViewModel.getJobMeasureForActivityId(
            ActivityIdConstants.ESTIMATE_MEASURE,
            ActivityIdConstants.MEASURE_PART_COMPLETE,
            ActivityIdConstants.JOB_ESTIMATE
        )

        itemEstimateData.observe(viewLifecycleOwner, { itemEstimateList ->

            if (itemEstimateList.isNullOrEmpty()) {
                ui.noData.visibility = View.VISIBLE
                ui.estimationsToBeMeasuredListView.visibility = View.GONE
                fetchJobMeasures()
            } else {
                val jobHeaders = itemEstimateList.distinctBy { item ->
                    item.jobId
                }
                Timber.d("Estimate measures detected: ${jobHeaders.size}")
                ui.estimationsToBeMeasuredListView.visibility = View.VISIBLE
                ui.noData.visibility = View.GONE
                initRecyclerView(jobHeaders.toMeasureListItems())
            }
        })
    }

    private fun swipeToRefreshInit() {
        ui.estimationsSwipeToRefresh.setProgressBackgroundColorSchemeColor(
            ContextCompat.getColor(
                requireContext().applicationContext,
                R.color.colorPrimary
            )
        )
        ui.estimationsSwipeToRefresh.setColorSchemeColors(Color.WHITE)

        ui.estimationsSwipeToRefresh.setOnRefreshListener {
            Coroutines.main {
                ui.estimationsToBeMeasuredListView.veil()
                fetchRemoteJobs()
            }
        }
    }

    private suspend fun fetchRemoteJobs() {
        Coroutines.main {
            try {
                withContext(Dispatchers.Main) {
                    val jobs = measureViewModel.offlineUserTaskList.await()
                    jobs.observeOnce(viewLifecycleOwner, { works ->
                        if (works.isNullOrEmpty()) {
                            ui.noData.visibility = View.VISIBLE
                            ui.estimationsToBeMeasuredListView.visibility = View.GONE
                        } else {
                            ui.noData.visibility = View.GONE
                            ui.estimationsToBeMeasuredListView.visibility = View.VISIBLE
                        }
                    })
                }
            } catch (t: Throwable) {
                val fetchError = XIError(t, t.message ?: XIErrorHandler.UNKNOWN_ERROR)
                crashGuard(
                    view = this@MeasureFragment.requireView(),
                    throwable = fetchError,
                    refreshAction = { retryFetchingJobs() }
                )
            } finally {
                ui.estimationsSwipeToRefresh.isRefreshing = false
                delayedUnveil()
            }
        }
    }

    private fun retryFetchingJobs() {
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

            jobEstimateData.observe(viewLifecycleOwner, { jos ->
                if (jos.isNullOrEmpty()) {
                    ui.noData.visibility = View.VISIBLE
                    ui.estimationsToBeMeasuredListView.visibility = View.GONE
                } else {
                    ui.noData.visibility = View.GONE
                    ui.estimationsToBeMeasuredListView.visibility = View.VISIBLE
                    val measureItems = jos.distinctBy {
                        it.jobId
                    }
                    Timber.d("Job measures detected: ${measureItems.size}")
                    initRecyclerView(measureItems.toMeasureListItems())
                }
            })
        }
    }

    private fun initRecyclerView(measureList: List<EstimateMeasureItem>) {
        groupAdapter = GroupAdapter<GroupieViewHolder>().apply {
            clear()
            update(measureList)
            notifyDataSetChanged()
        }
        ui.estimationsToBeMeasuredListView.run {
            setAdapter(adapter = groupAdapter, layoutManager = LinearLayoutManager(this.context))
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
        ui.estimationsToBeMeasuredListView.run {
            setVeilLayout(R.layout.single_listview_item, object : VeiledItemOnClickListener {
                /** will be invoked when the item on the [VeilRecyclerFrameView] clicked. */
                override fun onItemClicked(pos: Int) {
                    Toast.makeText(this@MeasureFragment.requireContext(), "Loading ...", Toast.LENGTH_SHORT).show()
                }
            })
            setAdapter(groupAdapter)
            setLayoutManager(LinearLayoutManager(this.context))
            addVeiledItems(10)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        ui.estimationsToBeMeasuredListView.setAdapter(null)
        _ui = null
    }
}
