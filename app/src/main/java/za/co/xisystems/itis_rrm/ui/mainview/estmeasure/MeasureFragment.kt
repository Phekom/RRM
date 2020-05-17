package za.co.xisystems.itis_rrm.ui.mainview.estmeasure

import android.app.ProgressDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import kotlinx.android.synthetic.main.fragment_approvemeasure.noData
import kotlinx.android.synthetic.main.fragment_estmeasure.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import timber.log.Timber
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.data._commons.views.ToastUtils
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimateDTO
import za.co.xisystems.itis_rrm.ui.mainview._fragments.BaseFragment
import za.co.xisystems.itis_rrm.ui.mainview.estmeasure.estimate_measure_item.EstimateMeasureItem
import za.co.xisystems.itis_rrm.utils.*

class MeasureFragment : BaseFragment(R.layout.fragment_estmeasure), KodeinAware {

    override val kodein by kodein()
    private lateinit var measureViewModel: MeasureViewModel
    private val factory: MeasureViewModelFactory by instance()

    companion object {
        const val JOB_MEASURE_EST_JOB_ID = "JOB_MEASURE_EST_JOB_ID"
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_estmeasure, container, false)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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

            val measurements = measureViewModel.getJobMeasureForActivityId(
                ActivityIdConstants.ESTIMATE_MEASURE,
                ActivityIdConstants.MEASURE_PART_COMPLETE
            )

            measurements.observe(viewLifecycleOwner, Observer { job_s ->
                val partCompleteHeaders = job_s.distinctBy {
                    it.jobId
                }
                if (job_s.isEmpty()) {
                    Coroutines.main {
                        val measurement = measureViewModel.getJobMeasureForActivityId(
                            ActivityIdConstants.ESTIMATE_MEASURE,
                            ActivityIdConstants.JOB_ESTIMATE
                        )
                        measurement.observe(viewLifecycleOwner, Observer { jos ->
                            val estimateHeaders = jos.distinctBy {
                                it.jobId
                            }
                            noData.visibility = View.GONE
                            initRecyclerView(estimateHeaders.toMeasureListItems())
                            toast(job_s.size.toString())
                            group5_loading.visibility = View.GONE
                        })
                    }
                } else {
                    noData.visibility = View.GONE
                    initRecyclerView(partCompleteHeaders.toMeasureListItems())
                    toast(job_s.size.toString())
                    group5_loading.visibility = View.GONE
                }

            })

            swipeToRefreshInit(dialog)
        }
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

            Coroutines.main {
                dialog.show()
                try {
                    val jobs = measureViewModel.offlineUserTaskList.await()
                    jobs.observe(viewLifecycleOwner, Observer { works ->
                        if (works.isEmpty()) {
                            noData.visibility = View.VISIBLE
                        }
                    })
                } catch (e: ApiException) {
                    ToastUtils().toastLong(activity, e.message)
                    Timber.e(e, e.message)
                } catch (e: NoInternetException) {
                    ToastUtils().toastLong(activity, e.message)
                    Timber.e(e, e.message)
                } catch (e: NoConnectivityException) {
                    ToastUtils().toastLong(activity, e.message)
                    Timber.e(e, e.message)
                } finally {
                    dialog.dismiss()
                    estimations_swipe_to_refresh.isRefreshing = false
                }

            }
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
            measureViewModel.measure_Item.value = measureItem
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