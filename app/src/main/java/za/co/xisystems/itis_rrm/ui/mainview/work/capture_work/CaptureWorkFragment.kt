package za.co.xisystems.itis_rrm.ui.mainview.work.capture_work

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
import za.co.xisystems.itis_rrm.ui.mainview.work.WorkViewModel
import za.co.xisystems.itis_rrm.ui.mainview.work.WorkViewModelFactory
import za.co.xisystems.itis_rrm.utils.Coroutines


class CaptureWorkFragment : BaseFragment(), KodeinAware {

    override val kodein by kodein()
    private lateinit var workViewModel: WorkViewModel
    private val factory: WorkViewModelFactory by instance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity as MainActivity).supportActionBar?.title = getString(R.string.capture_work_title)




    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_capture_work, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        workViewModel = activity?.run {
            ViewModelProviders.of(this, factory).get(WorkViewModel::class.java)
        } ?: throw Exception("Invalid Activity") as Throwable

        Coroutines.main {

            workViewModel.work_Item.observe(viewLifecycleOwner, Observer { estimateId ->
                getWorkItems(estimateId)
            })

        }


    }

    private fun getWorkItems(estimateId: String?) {
        Coroutines.main {
            val estimate = workViewModel.getJobEstiItemForEstimateId(estimateId)
            estimate.observe(viewLifecycleOwner, Observer { work_s ->
                toast(work_s.size.toString())

        })

        }
//            val works = workViewModel.getJobsForActivityId(
//                ActivityIdConstants.JOB_APPROVED
//                , ActivityIdConstants.ESTIMATE_INCOMPLETE
//            )
//
////            val jobs = approveViewModel.offlinedata.await()
//            works.observe(viewLifecycleOwner, Observer { work_s ->
//                noData.visibility = View.GONE
//                toast(work_s.size.toString())
//                initRecyclerView(work_s.toWorkListItems())
//                group7_loading.visibility = View.GONE
//
    }












































    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (activity as MainActivity).supportActionBar?.title = getString(R.string.capture_work_title)
            }

    override fun onDetach() {
        super.onDetach()

    }




}
