package za.co.xisystems.itis_rrm.ui.mainview.estmeasure

import android.graphics.Color
import android.os.Bundle
import android.util.Log
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
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.base.BaseFragment
import za.co.xisystems.itis_rrm.custom.errors.ApiException
import za.co.xisystems.itis_rrm.custom.errors.NoConnectivityException
import za.co.xisystems.itis_rrm.custom.errors.NoInternetException
import za.co.xisystems.itis_rrm.data._commons.views.ToastUtils
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimateDTO
import za.co.xisystems.itis_rrm.ui.mainview.estmeasure.estimate_measure_item.EstimateMeasureItem
import za.co.xisystems.itis_rrm.ui.models.MeasureViewModel
import za.co.xisystems.itis_rrm.ui.models.MeasureViewModelFactory
import za.co.xisystems.itis_rrm.utils.ActivityIdConstants
import za.co.xisystems.itis_rrm.utils.Coroutines

class MeasureFragment : BaseFragment(R.layout.fragment_estmeasure), KodeinAware {


    override val kodein by kodein()
    private lateinit var measureViewModel: MeasureViewModel
    private val factory: MeasureViewModelFactory by instance()
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

            val itemEstimateData = measureViewModel.getJobMeasureForActivityId(
                ActivityIdConstants.ESTIMATE_MEASURE,
                ActivityIdConstants.MEASURE_PART_COMPLETE
            )

            itemEstimateData.observe(viewLifecycleOwner, Observer { itemEstimateList ->
                val jobHeaders = itemEstimateList.distinctBy {
                    it.jobId
                }
                if (itemEstimateList.isEmpty()) {
                    Coroutines.main {
                        val jobEstimateData = measureViewModel.getJobMeasureForActivityId(
                            ActivityIdConstants.ESTIMATE_MEASURE,
                            ActivityIdConstants.JOB_ESTIMATE
                        )
                        jobEstimateData.observe(viewLifecycleOwner, Observer { jos ->
                            val measure_items = jos.distinctBy {
                                it.jobId
                            }
                            noData.visibility = View.GONE
                            initRecyclerView(measure_items.toMeasureListItems())
                            toast(itemEstimateList.size.toString())
                            group5_loading.visibility = View.GONE
                        })
                    }
                } else {
                    noData.visibility = View.GONE
                    initRecyclerView(jobHeaders.toMeasureListItems())
                    toast(itemEstimateList.size.toString())
                    group5_loading.visibility = View.GONE
                }
            })

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
                        val jobs = measureViewModel.offlineUserTaskList.await()
                        jobs.observe(viewLifecycleOwner, Observer { works ->
                            if (works.isEmpty()) {
                                noData.visibility = View.VISIBLE
                            }
                            estimations_swipe_to_refresh.isRefreshing = false
                            dialog.dismiss()
                        })
                    } catch (e: ApiException) {
                        ToastUtils().toastLong(activity, e.message)
                        estimations_swipe_to_refresh.isRefreshing = false
                        dialog.dismiss()
                        Log.e("Service-Host", "API Exception", e)
                    } catch (e: NoInternetException) {
                        ToastUtils().toastLong(activity, e.message)
                        // snackError(this.coordinator, e.message)
                        dialog.dismiss()
                        estimations_swipe_to_refresh.isRefreshing = false
                        Log.e("Network-Connection", "No Internet Connection", e)
                    } catch (e: NoConnectivityException) {
                        ToastUtils().toastLong(activity, e.message)
                        dialog.dismiss()
                        estimations_swipe_to_refresh.isRefreshing = false
                        Log.e("Network-Error", "Service Host Unreachable", e)
                    }

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