package za.co.xisystems.itis_rrm.ui.mainview.approvejobs.view_job_info

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import kotlinx.android.synthetic.main.fragment_job_info.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import za.co.xisystems.itis_rrm.MainActivity
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimateDTO
import za.co.xisystems.itis_rrm.ui.mainview._fragments.BaseFragment
import za.co.xisystems.itis_rrm.ui.mainview.approvejobs.ApproveJobsViewModel
import za.co.xisystems.itis_rrm.ui.mainview.approvejobs.ApproveJobsViewModelFactory
import za.co.xisystems.itis_rrm.utils.*
import za.co.xisystems.itis_rrm.utils.enums.WorkflowDirection


class JobInfoFragment : BaseFragment(), KodeinAware {
    override val kodein by kodein()
    private lateinit var approveViewModel: ApproveJobsViewModel
    private val factory: ApproveJobsViewModelFactory by instance()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (activity as MainActivity).supportActionBar?.title = getString(R.string.jobinfo_item_title)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity as MainActivity).supportActionBar?.title = getString(R.string.jobinfo_item_title)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_job_info, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        approveViewModel = activity?.run {
            ViewModelProviders.of(this, factory).get(ApproveJobsViewModel::class.java)
        } ?: throw Exception("Invalid Activity") as Throwable
        Coroutines.main {
            mydata_loading.show()

            approveViewModel.jobapproval_Item6.observe(viewLifecycleOwner, Observer { job ->
                Coroutines.main {
                    getEstimateItems(job.jobDTO.JobId)
                    val descri = approveViewModel.getDescForProjectId(job.jobDTO.ProjectId!!)
                    val sectionId = approveViewModel.getProjectSectionIdForJobId(job.jobDTO.JobId)
                    val route = approveViewModel.getRouteForProjectSectionId(sectionId)
                    val section = approveViewModel.getSectionForProjectSectionId(sectionId)

                    project_description_textView.text = descri
                    section_description_textView.text = ("$route/ $section")
                    start_km_description_textView.text = (job.jobDTO.StartKm.toString())
                    end_km_description_textView.text = (job.jobDTO.EndKm.toString())
                }
            })
        }

        approve_job_button.setOnClickListener {
            val logoutBuilder = AlertDialog.Builder(
                activity //,android.R.style.Theme_DeviceDefault_Dialog
            )
            logoutBuilder.setTitle(R.string.confirm)
            logoutBuilder.setIcon(R.drawable.ic_approve)
            logoutBuilder.setMessage(R.string.are_you_sure_you_want_to_approve)

            // Yes button
            logoutBuilder.setPositiveButton(
                R.string.yes
            ) { dialog, which ->
                if (ServiceUtil.isNetworkConnected(context?.applicationContext)) {
                    moveJobToNextWorkflow(WorkflowDirection.NEXT)
                } else {
                   toast( R.string.no_connection_detected)
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

        decline_job_button.setOnClickListener {
            val logoutBuilder =
                AlertDialog.Builder(
                    activity //, android.R.style.Theme_DeviceDefault_Dialog
                )
            logoutBuilder.setTitle(R.string.confirm)
            logoutBuilder.setIcon(R.drawable.ic_warning)
            logoutBuilder.setMessage(R.string.are_you_sure_you_want_to_decline)
            // Yes button
            logoutBuilder.setPositiveButton(
                R.string.yes
            ) { dialog, which ->
                if (ServiceUtil.isNetworkConnected(context?.applicationContext)) {
                    moveJobToNextWorkflow(WorkflowDirection.FAIL)
                } else {
                    toast( R.string.no_connection_detected)
                }
            }
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

    }

    private fun moveJobToNextWorkflow(workflowDirection : WorkflowDirection) {
        Coroutines.main {
            val messages = arrayOf(
                getString(R.string.moving_to_next_step_in_workflow),
                getString(R.string.please_wait)
            )

            val user = approveViewModel.user.await()
            user.observe(viewLifecycleOwner, Observer { user_ ->
                approveViewModel.jobapproval_Item6.observe(viewLifecycleOwner, Observer { job ->

                    if (user_.userId == null) {
                        toast("Error: userId is null")
                    } else if ( getEstimateItems(job.jobDTO.JobId) == null) {
                        toast("Error: selectedJob is null")
                    } else {
                      toast(job.jobDTO.JobId)
                        // TODO beware littlEndian conversion
                        val trackRounteId: String = DataConversion.toLittleEndian(job.jobDTO.TrackRouteId)!!
                        val direction: Int = workflowDirection.getValue()

                        var description : String? = ""
                        if (workflow_comments_editText.text != null)
                            description = workflow_comments_editText.text.toString()
                       processWorkFlow(user_.userId, trackRounteId, direction, description)
                    }

                })
            })
        }

    }

    private fun processWorkFlow(
        userId: String,
        trackRounteId: String,
        direction: Int,
        description: String?
    ) {
        Coroutines.main {
        approveViewModel.processWorkflowMove(userId, trackRounteId,description,direction)
            activity?.hideKeyboard()
            popViewOnJobSubmit(direction)
    }

    }

    private fun popViewOnJobSubmit(direction: Int) {
        if (direction.equals(WorkflowDirection.NEXT)) {
            toast(R.string.job_approved)
        } else if (direction.equals(WorkflowDirection.FAIL)) {
            toast(R.string.job_declined)
        }


        Intent(context, MainActivity::class.java).also { home ->
            home.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(home)
        }
        // Navigation.findNavController(view!!)
//                .navigate(R.id.action_jobInfoFragment_to_nav_home)
    }

    private fun getEstimateItems(jobID: String?) {
        Coroutines.main {
        val estimates = approveViewModel.getJobEstimationItemsForJobId(jobID)
            estimates.observe(viewLifecycleOwner, Observer { job_s ->
                mydata_loading.hide()
                initRecyclerView(job_s.toEstimates_Item())

            })

        }
    }

    private fun initRecyclerView(estimatesListItems: List<Estimates_Item>) {
        val groupAdapter = GroupAdapter<GroupieViewHolder>().apply {
            addAll(estimatesListItems)
        }
        view_estimation_items_listView.apply {
            layoutManager = LinearLayoutManager(this.context) as RecyclerView.LayoutManager?
            adapter = groupAdapter

        }

    }

    private fun List<JobItemEstimateDTO>.toEstimates_Item(): List<Estimates_Item> {
        return this.map { approvej_items ->
//            getEstimateItemsPhoto(approvej_items.estimateId)
            Estimates_Item(approvej_items,approveViewModel, activity)
        }
    }

//    private fun getEstimateItemsPhoto(estimateId: String) {
//        Coroutines.main {
//            val estimatesphoto = approveViewModel.getJobEstimationItemsPhoto(estimateId)
//        }
//    }
}
