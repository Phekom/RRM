package za.co.xisystems.itis_rrm.ui.mainview.unsubmitted.unsubmited_item

import androidx.fragment.app.FragmentActivity
import androidx.navigation.Navigation
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.unsubmtd_job_list_item.*
import kotlinx.coroutines.Job
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTOTemp
import za.co.xisystems.itis_rrm.data.network.PermissionController
import za.co.xisystems.itis_rrm.ui.mainview.unsubmitted.UnSubmittedViewModel
import za.co.xisystems.itis_rrm.utils.Coroutines
import za.co.xisystems.itis_rrm.utils.toast

/**
 * Created by Francis Mahlava on 2019/12/22.
 */


class UnSubmitedJob_Item(
    val jobDTO: JobDTOTemp,
    private val viewModel: UnSubmittedViewModel,
    private var  activity: FragmentActivity?
) : Item(){

    var clickListener: ((UnSubmitedJob_Item) -> Unit)? = null


    override fun bind(viewHolder: GroupieViewHolder, position: Int) {


       viewHolder.apply {
           iTemID.text = getItemId(position + 1).toString()
           unsubmitted_project_textView.text = "JI:${jobDTO.JiNo} - "
           Coroutines.main {
               val descri = viewModel.getDescForProjectId(jobDTO.ProjectId!!)
//               val section  =  viewModel.getProjectSection(jobDTO.SectionId)
//                section.observe(activity!!, Observer {
//                    unsubmitted_section_textView.text =  " ${it.route} " + " SEC " + " ${it.section} " + "(${it.route} / ${it.startKm} / ${it.endKm} )"
//                })
               unsubmitted_project_textView.text = descri
           }
           unsubmitted_description_textView.text = jobDTO.Descr

           deleteButton.setOnClickListener {
               Coroutines.main {
                   viewModel.deleJobfromList(jobDTO.JobId)
               }
           }

           updateItem()
       }

        viewHolder.itemView.setOnClickListener {view ->
            clickListener?.invoke(this)
            activity!!.toast("I have to go back and finish my Job")
            if (PermissionController.checkPermissionsEnabled(activity!!.getApplicationContext())) {
//                if (uncompletedJobs != null) {
//                    val job: Job = uncompletedJobs.get(position)
//                    // FlowCache.getInstance().setNewJob(job);
//// startActivity(new Intent(getActivity().getApplicationContext(), NewJobActivity.class));
//                    if (job != null) newJobIntent.startActivity(job.getJobId())
//                }


                Navigation.findNavController(view)
                    .navigate(R.id.action_nav_unSubmitted_to_addProjectFragment)
            } else {
                PermissionController.startPermissionRequests(
                    activity,
                    activity!!.getApplicationContext()
                )
            }



        }
    }

    override fun getLayout() = R.layout.unsubmtd_job_list_item

    private fun GroupieViewHolder.updateItem(){

    }
    private fun GroupieViewHolder.updatePojectItem(){

    }


}

private fun GroupieViewHolder.getItemId(position: Int): Long {
    return position.toLong()
}
