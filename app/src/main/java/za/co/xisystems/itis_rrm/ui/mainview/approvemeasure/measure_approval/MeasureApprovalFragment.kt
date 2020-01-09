package za.co.xisystems.itis_rrm.ui.mainview.approvemeasure.measure_approval

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import kotlinx.android.synthetic.main.fragment_measure_approval.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import za.co.xisystems.itis_rrm.MainActivity
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemMeasureDTO
import za.co.xisystems.itis_rrm.ui.mainview._fragments.BaseFragment
import za.co.xisystems.itis_rrm.ui.mainview.approvemeasure.ApproveMeasureViewModel
import za.co.xisystems.itis_rrm.ui.mainview.approvemeasure.ApproveMeasureViewModelFactory
import za.co.xisystems.itis_rrm.utils.ActivityIdConstants
import za.co.xisystems.itis_rrm.utils.Coroutines


class MeasureApprovalFragment : BaseFragment(), KodeinAware {
    override val kodein by kodein()
    private lateinit var approveViewModel: ApproveMeasureViewModel
    private val factory: ApproveMeasureViewModelFactory by instance()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (activity as MainActivity).supportActionBar?.title = getString(R.string.measure_approval_title)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_measure_approval, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        approveViewModel = activity?.run {
            ViewModelProviders.of(this, factory).get(ApproveMeasureViewModel::class.java)
        } ?: throw Exception("Invalid Activity") as Throwable

        Coroutines.main {
//            mydata_loading.show()

            approveViewModel.measureapproval_Item.observe(viewLifecycleOwner, Observer { jobID ->
                getMeasureItems(jobID)
            })
        }
    }

    private fun getMeasureItems(jobID: String?) {
        Coroutines.main {
            val measurements = approveViewModel.getJobMeasureItemsForJobId(jobID, ActivityIdConstants.MEASURE_COMPLETE)
            measurements.observe(viewLifecycleOwner, Observer { job_s ->
//                mydata_loading.hide()
                toast(job_s.size.toString())
                initRecyclerView(job_s.toMeasure_Item())

            })

        }
    }

    private fun initRecyclerView(measureListItems: List<Measurements_Item>) {
        val groupAdapter = GroupAdapter<GroupieViewHolder>().apply {
            addAll(measureListItems)
        }
        view_measured_items.apply {
            layoutManager = LinearLayoutManager(this.context)
            adapter = groupAdapter

        }

    }

    private fun List<JobItemMeasureDTO>.toMeasure_Item(): List<Measurements_Item> {
        return this.map { approvej_items ->
            //            getEstimateItemsPhoto(approvej_items.estimateId)
            Measurements_Item(approvej_items,approveViewModel, activity)
        }
    }









    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {
//        listener?.onFragmentInteraction(uri)
    }



    override fun onDetach() {
        super.onDetach()

    }








}
