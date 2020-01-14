package za.co.xisystems.itis_rrm.ui.mainview.estmeasure.submit_measure

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import za.co.xisystems.itis_rrm.MainActivity
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.ui.mainview._fragments.BaseFragment
import za.co.xisystems.itis_rrm.ui.mainview.estmeasure.MeasureViewModel
import za.co.xisystems.itis_rrm.ui.mainview.estmeasure.MeasureViewModelFactory
import za.co.xisystems.itis_rrm.utils.Coroutines


class SubmitMeasureFragment : BaseFragment(), KodeinAware {
    override val kodein by kodein()
    private lateinit var measureViewModel: MeasureViewModel
    private val factory: MeasureViewModelFactory by instance()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (activity as MainActivity).supportActionBar?.title = getString(R.string.submit_measure_title)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity as MainActivity).supportActionBar?.title = getString(R.string.submit_measure_title)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_submit_measure, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        measureViewModel = activity?.run {
            ViewModelProviders.of(this, factory).get(MeasureViewModel::class.java)
        } ?: throw Exception("Invalid Activity") as Throwable

        Coroutines.main {
            //            mydata_loading.show()

            measureViewModel.measure_Item.observe(viewLifecycleOwner, Observer { jobID ->
                toast(jobID)
                getWorkItems(jobID)
            })
        }
    }

    private fun getWorkItems(jobID: String?) {
        Coroutines.main {
//            val measurements = measureViewModel.getJobEstimatesItemsForJobId(jobID, ActivityIdConstants.MEASURE_COMPLETE)
//            measurements.observe(viewLifecycleOwner, Observer { job_s ->
                //                mydata_loading.hide()
//                toast(job_s.size.toString())
//                initRecyclerView(job_s.toMeasure_Item())

//            })

        }
    }




































    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {
//        listener?.onFragmentInteraction(uri)
    }



    override fun onDetach() {
        super.onDetach()
//        listener = null
    }




}
