package za.co.xisystems.itis_rrm.ui.mainview.estmeasure

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
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
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimateDTO
import za.co.xisystems.itis_rrm.ui.mainview._fragments.BaseFragment
import za.co.xisystems.itis_rrm.ui.mainview.estmeasure.estimate_measure_item.EstimateMeasure_Item
import za.co.xisystems.itis_rrm.utils.ActivityIdConstants
import za.co.xisystems.itis_rrm.utils.Coroutines

class MeasureFragment : BaseFragment(), KodeinAware {

    override val kodein by kodein()
    private lateinit var measureViewModel: MeasureViewModel
    private val factory: MeasureViewModelFactory by instance()

companion object{
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
            ViewModelProviders.of(this, factory).get(MeasureViewModel::class.java)
        } ?: throw Exception("Invalid Activity") as Throwable
        Coroutines.main {
            //            mydata_loading.show()
//            val measurements = measureViewModel.getJobMeasureForActivityId(ActivityIdConstants.ESTIMATE_MEASURE)MEASURE_PART_COMPLETE
     val measurements = measureViewModel.getJobMeasureForActivityId(ActivityIdConstants.ESTIMATE_MEASURE,ActivityIdConstants.MEASURE_PART_COMPLETE)
//            val measurements = approveViewModel.offlinedata.await()
            measurements.observe(viewLifecycleOwner, Observer { job_s ->
                if (job_s.isEmpty()){
                    Coroutines.main {
                        val measurements = measureViewModel.getJobMeasureForActivityId(ActivityIdConstants.ESTIMATE_MEASURE,ActivityIdConstants.JOB_ESTIMATE)
                        measurements.observe(viewLifecycleOwner, Observer { jos ->
                            noData.visibility = View.GONE
                            initRecyclerView(jos.toMeasureListItems())
                            toast(job_s.size.toString())
                            group5_loading.visibility = View.GONE
                        })
                    }
                }else{
                    noData.visibility = View.GONE
                    initRecyclerView(job_s.toMeasureListItems())
                    toast(job_s.size.toString())
                    group5_loading.visibility = View.GONE
                }

            })

            estimations_swipe_to_refresh.setProgressBackgroundColorSchemeColor(
                ContextCompat.getColor(
                    context!!.applicationContext,
                    R.color.colorPrimary
                )
            )
            estimations_swipe_to_refresh.setColorSchemeColors(Color.WHITE)

            estimations_swipe_to_refresh.setOnRefreshListener {
                Coroutines.main {
                    val jobs = measureViewModel.offlinedatas.await()
                    jobs.observe(viewLifecycleOwner, Observer { works ->
                        estimations_swipe_to_refresh.isRefreshing = false
                    })

                }
            }
        }
    }


    private fun initRecyclerView(measureListItems: List<EstimateMeasure_Item>) {
        val groupAdapter = GroupAdapter<GroupieViewHolder>().apply {
            addAll(measureListItems)
        }
        estimations_to_be_measured_listView.apply {
            layoutManager = LinearLayoutManager(this.context)
            adapter = groupAdapter

        }

        groupAdapter.setOnItemClickListener { item, view ->
            Coroutines.main {
                (item as? EstimateMeasure_Item)?.let {
                    //                    val descri = approveViewModel.getDescForProjectId(it.jobDTO.projectId!!)
//                    val sectionId  =  approveViewModel.getProjectSectionIdForJobId(it.jobDTO.jobId)
//                    val route  =  approveViewModel.getRouteForProjectSectionId(sectionId)
//                    val section  =  approveViewModel.getSectionForProjectSectionId(sectionId)
//                    sendJobtoAprove((it.jobItemEstimateDTO.jobId), view)
                    sendJobtoAprove((it), view)
                }

            }
        }
    }

    private fun sendJobtoAprove(
        job: EstimateMeasure_Item?,
        view: View
    ) {
        val jobId = job

        Coroutines.main {
            measureViewModel.measure_Item.value = jobId
        }

        Navigation.findNavController(view)
            .navigate(R.id.action_nav_estMeasure_to_submitMeasureFragment)
    }

    private fun List<JobItemEstimateDTO>.toMeasureListItems(): List<EstimateMeasure_Item> {
        return this.map { measure_items ->
            EstimateMeasure_Item(measure_items,measureViewModel)
        }
    }
}