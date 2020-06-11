package za.co.xisystems.itis_rrm.ui.mainview.approvemeasure.measure_approval

import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import kotlinx.android.synthetic.main.fragment_measure_approval.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import timber.log.Timber
import za.co.xisystems.itis_rrm.MainActivity
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemMeasureDTO
import za.co.xisystems.itis_rrm.ui.mainview._fragments.BaseFragment
import za.co.xisystems.itis_rrm.ui.mainview.approvemeasure.ApproveMeasureViewModel
import za.co.xisystems.itis_rrm.ui.mainview.approvemeasure.ApproveMeasureViewModelFactory
import za.co.xisystems.itis_rrm.ui.mainview.approvemeasure.approveMeasure_Item.ApproveMeasureItem
import za.co.xisystems.itis_rrm.utils.ActivityIdConstants
import za.co.xisystems.itis_rrm.utils.Coroutines
import za.co.xisystems.itis_rrm.utils.DataConversion
import za.co.xisystems.itis_rrm.utils.ServiceUtil
import za.co.xisystems.itis_rrm.utils.enums.WorkflowDirection


class MeasureApprovalFragment : BaseFragment(R.layout.fragment_measure_approval), KodeinAware {
    override val kodein by kodein()
    private lateinit var approveViewModel: ApproveMeasureViewModel
    private val factory: ApproveMeasureViewModelFactory by instance<ApproveMeasureViewModelFactory>()
    private lateinit var measurementsToApprove: ArrayList<JobItemMeasureDTO>
    lateinit var dialog : Dialog

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (activity as MainActivity).supportActionBar?.title =
            getString(R.string.measure_approval_title)

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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
            ViewModelProvider(this, factory).get(ApproveMeasureViewModel::class.java)
        } ?: throw Exception("Invalid Activity")
        dialog = setDataProgressDialog(requireActivity(), getString(R.string.data_loading_please_wait))
        Coroutines.main {

            approveViewModel.measureapproval_Item.observe(viewLifecycleOwner, Observer { job ->
                getMeasureItems(job)
            })

            approve_measure_button.setOnClickListener {
                val logoutBuilder = AlertDialog.Builder(
                    requireActivity() //, android.R.style.Theme_DeviceDefault_Dialog
                )
                logoutBuilder.setTitle(R.string.confirm)
                logoutBuilder.setIcon(R.drawable.ic_approve)
                logoutBuilder.setMessage(R.string.are_you_sure_you_want_to_approve2)

                // Yes button
                logoutBuilder.setPositiveButton(
                    R.string.yes
                ) { dialog, which ->
                    if (ServiceUtil.isNetworkConnected(context?.applicationContext)) {
                        moveJobToNextWorkflow(WorkflowDirection.NEXT)
                    } else {
                        toast(R.string.no_connection_detected)
                    }
                }
                // No button
                // No button
                logoutBuilder.setNegativeButton(
                    R.string.no
                ) { dialog, which ->
                    // Do nothing but close dialog
                    dialog.dismiss()
                }
                val declineAlert = logoutBuilder.create()
                declineAlert.show()
            }

//            decline_measure_button.setOnClickListener {
//                val logoutBuilder =
//                    AlertDialog.Builder(
//                        requireActivity() //, android.R.style.Theme_DeviceDefault_Dialog
//                    )
//                logoutBuilder.setTitle(R.string.confirm)
//                logoutBuilder.setIcon(R.drawable.ic_warning)
//                logoutBuilder.setMessage(R.string.are_you_sure_you_want_to_decline2)
//                // Yes button
//                logoutBuilder.setPositiveButton(
//                    R.string.yes
//                ) { dialog, which ->
//                    if (ServiceUtil.isNetworkConnected(context?.applicationContext)) {
////                        moveJobToNextWorkflow(WorkflowDirection.FAIL)
//                    } else {
//                        toast(R.string.no_connection_detected)
//                    }
//                }
//                // No button
//                logoutBuilder.setNegativeButton(
//                    R.string.no
//                ) { dialog, which ->
//                    // Do nothing but close dialog
//                    dialog.dismiss()
//                }
//                val declineAlert = logoutBuilder.create()
//                declineAlert.show()
//            }

        }
    }

    private fun moveJobToNextWorkflow(workflowDirection: WorkflowDirection) {
        Coroutines.main {
            arrayOf(
                getString(R.string.moving_to_next_step_in_workflow),
                getString(R.string.please_wait)
            )


            val user = approveViewModel.user.await()
            user.observe(viewLifecycleOwner, Observer { user_ ->
                try {
                    measurementsToApprove.forEach { measureItem ->
                        if (user_.userId.isBlank()) {
                            toast("Error: userId is null")
                        } else {
                            // littleEndian conversion for transport to the backend
                            val trackRouteId: String =
                                DataConversion.toLittleEndian(measureItem.trackRouteId)!!
                            val direction: Int = workflowDirection.value
                            val description = ""
                            processWorkFlow(user_.userId, trackRouteId, direction, description)
                        }
                    }
                    measurementsToApprove.clear()
                    toast(R.string.job_submitted)
                    popViewOnJobSubmit(workflowDirection.value)
                } catch (e: Exception) {
                    Timber.e(e, "Failed to Approve Measurments!")
                }
            })
        }
    }

    private fun processWorkFlow(
        userId: String, trackRouteId: String, direction: Int, description: String?
    ) {
        Coroutines.main {
            val prog = ProgressDialog(activity)
            prog.setTitle(getString(R.string.please_wait))
            prog.setMessage(getString(R.string.loading_job_wait))
            prog.setCancelable(false)
            prog.isIndeterminate = true
            prog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
            prog.show()
            val submit =
                approveViewModel.processWorkflowMove(userId, trackRouteId, description, direction)
            if (submit.isNotEmpty()) {
                prog.dismiss()
                toast(submit)
            } else {
                prog.dismiss()
            }
        }
    }

    private fun popViewOnJobSubmit(direction: Int) {
        if (direction == WorkflowDirection.NEXT.value) {
            toast(R.string.job_approved)
        } else if (direction == WorkflowDirection.FAIL.value) {
            toast(R.string.job_declined)
        }

        Intent(context?.applicationContext, MainActivity::class.java).also { home ->
            startActivity(home)
        }
    }

    private fun getMeasureItems(job: ApproveMeasureItem) {
        Coroutines.main {
            val measurements = approveViewModel.getJobMeasureItemsForJobId(
                job.jobItemMeasureDTO.jobId,
                ActivityIdConstants.MEASURE_COMPLETE
            )
            measurements.observe(viewLifecycleOwner, Observer { measureItems ->
                val allData = measureItems.count()

                if (allData == measureItems.size) {
                    measurementsToApprove = ArrayList()
                    measurementsToApprove.addAll(measureItems)
                    initRecyclerView(measureItems.toMeasureItem())
                }
            })
        }
    }

    private fun initRecyclerView(measureListItems: List<MeasurementsItem>) {
        val groupAdapter = GroupAdapter<GroupieViewHolder>().apply {
            addAll(measureListItems)
        }
        view_measured_items.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = groupAdapter

        }

    }

    override fun onDestroyView() {
        view_measured_items.adapter = null
        super.onDestroyView()
    }

    private fun List<JobItemMeasureDTO>.toMeasureItem(): List<MeasurementsItem> {
        return this.map { approvedJobItem ->
            MeasurementsItem(approvedJobItem, approveViewModel, activity, dialog, viewLifecycleOwner)
        }
    }

}
