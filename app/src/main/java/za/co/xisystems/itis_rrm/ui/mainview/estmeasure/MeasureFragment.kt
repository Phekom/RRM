@file:Suppress("RemoveExplicitTypeArguments")

package za.co.xisystems.itis_rrm.ui.mainview.estmeasure

import android.app.ProgressDialog
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
import kotlinx.android.synthetic.main.fragment_approvemeasure.noData
import kotlinx.android.synthetic.main.fragment_estmeasure.estimations_swipe_to_refresh
import kotlinx.android.synthetic.main.fragment_estmeasure.estimations_to_be_measured_listView
import kotlinx.android.synthetic.main.fragment_estmeasure.group5_loading
import kotlinx.android.synthetic.main.fragment_estmeasure.no_data_layout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import timber.log.Timber
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.base.BaseFragment
import za.co.xisystems.itis_rrm.custom.errors.ErrorHandler
import za.co.xisystems.itis_rrm.custom.results.XIError
import za.co.xisystems.itis_rrm.custom.results.isConnectivityException
import za.co.xisystems.itis_rrm.custom.views.IndefiniteSnackbar
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimateDTO
import za.co.xisystems.itis_rrm.extensions.observeOnce
import za.co.xisystems.itis_rrm.ui.mainview.estmeasure.estimate_measure_item.EstimateMeasureItem
import za.co.xisystems.itis_rrm.ui.scopes.UiLifecycleScope
import za.co.xisystems.itis_rrm.utils.ActivityIdConstants
import za.co.xisystems.itis_rrm.utils.Coroutines

class MeasureFragment : BaseFragment(R.layout.fragment_estmeasure), KodeinAware {

    override val kodein by kodein()
    private lateinit var measureViewModel: MeasureViewModel
    private val factory: MeasureViewModelFactory by instance<MeasureViewModelFactory>()
    private val uiScope = UiLifecycleScope()

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
    ): View? {
        return inflater.inflate(R.layout.fragment_estmeasure, container, false)
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
            val dialog =
                setDataProgressDialog(
                    requireActivity(),
                    getString(R.string.data_loading_please_wait)
                )

            fetchEstimateMeasures()

            swipeToRefreshInit(dialog)
        }
    }

    private fun retryToDoList() {
        IndefiniteSnackbar.hide()
    }

    private fun swipeToRefreshInit(dialog: ProgressDialog) {
        estimations_swipe_to_refresh.setProgressBackgroundColorSchemeColor(
            ContextCompat.getColor(
                requireContext().applicationContext,
                R.color.colorPrimary
            )
        )
        estimations_swipe_to_refresh.setColorSchemeColors(Color.WHITE)

        estimations_swipe_to_refresh.setOnRefreshListener {
            dialog.show()
            Coroutines.main {
                try {
                    withContext(Dispatchers.Main) {
                        val jobs = measureViewModel.offlineUserTaskList.await()
                        jobs.observe(viewLifecycleOwner, {
                            it?.let {
                                uiScope.launch { fetchEstimateMeasures() }
                            }
                        })
                    }
                } catch (exc: Exception) {
                    Timber.e(exc, "Failed to fetch user todo list.")
                    val ex = XIError(exc, exc.message ?: ErrorHandler.UNKNOWN_ERROR)
                    when (ex.isConnectivityException()) {
                        true -> {
                            ErrorHandler.handleError(
                                view = this@MeasureFragment.requireView(),
                                throwable = ex,
                                shouldShowSnackBar = true,
                                refreshAction = { retryToDoList() }
                            )
                        }
                        else -> {
                            ErrorHandler.handleError(
                                view = this@MeasureFragment.requireView(),
                                throwable = ex,
                                shouldToast = true
                            )
                        }
                    }
                } finally {
                    estimations_swipe_to_refresh.isRefreshing = false
                }
            }
        }
    }

    private suspend fun fetchEstimateMeasures() {
        val itemEstimateData = measureViewModel.getJobMeasureForActivityId(
            ActivityIdConstants.ESTIMATE_MEASURE,
            ActivityIdConstants.MEASURE_PART_COMPLETE
        )

        itemEstimateData.observeOnce(viewLifecycleOwner, { estimateList ->
            if (estimateList.isEmpty()) {
                fetchJobEstimates()
            } else {
                val jobHeaders = estimateList.distinctBy {
                    it.jobId
                }
                noData.visibility = View.GONE
                initRecyclerView(jobHeaders.toMeasureListItems())
                toast(estimateList.size.toString())
                group5_loading.visibility = View.GONE
            }
        })
    }

    private fun fetchJobEstimates() {
        Coroutines.main {
            val jobEstimateData = measureViewModel.getJobMeasureForActivityId(
                ActivityIdConstants.ESTIMATE_MEASURE,
                ActivityIdConstants.JOB_ESTIMATE
            )

            jobEstimateData.observeOnce(viewLifecycleOwner, { estimateList ->
                if (estimateList.isEmpty()) {
                    no_data_layout.visibility = View.VISIBLE
                } else {
                    val measureItems = estimateList.distinctBy { it.jobId }
                    if (measureItems.isNotEmpty()) {
                        no_data_layout.visibility = View.GONE
                        initRecyclerView(measureItems.toMeasureListItems())
                        group5_loading.visibility = View.GONE
                        toast("Found ${measureItems.size} jobs")
                    }
                }
            })
        }
    }

    private fun initRecyclerView(measureListItems: List<EstimateMeasureItem>) {
        val groupAdapter = GroupAdapter<GroupieViewHolder>().apply {
            addAll(measureListItems)
        }
        estimations_to_be_measured_listView.apply {
            layoutManager = LinearLayoutManager(this.context)
            adapter = groupAdapter
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
        return this.map { measure_items ->
            EstimateMeasureItem(measure_items, measureViewModel)
        }
    }

    override fun onDestroyView() {
        estimations_to_be_measured_listView.adapter = null
        super.onDestroyView()
    }
}
