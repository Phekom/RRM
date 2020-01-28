package za.co.xisystems.itis_rrm.ui.mainview.unsubmitted.unsubmited_item

import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.unsubmtd_job_list_item.*
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTOTemp
import za.co.xisystems.itis_rrm.ui.mainview.unsubmitted.UnSubmittedViewModel
import za.co.xisystems.itis_rrm.utils.Coroutines

/**
 * Created by Francis Mahlava on 2019/12/22.
 */


class UnSubmitedJob_Item(
    val jobDTO: JobDTOTemp,
    private val viewModel: UnSubmittedViewModel
) : Item(){

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
       viewHolder.apply {
           iTemID.text = getItemId(position + 1).toString()
           unsubmitted_project_textView.text = "JI:${jobDTO.JiNo} - "
           Coroutines.main {
               val sectionId  =  viewModel?.getProjectSectionIdForJobId(jobDTO.JobId)
//               val route  =  viewModel?.getRouteForProjectSectionId(sectionId!!)
//               val section  =  viewModel?.getSectionForProjectSectionId(sectionId!!)
//               unsubmitted_section_textView.text =  "( ${route} ${"/0$section"} )"
           }
           unsubmitted_description_textView.text = jobDTO.Descr

           deleteButton.setOnClickListener {
               Coroutines.main {
                   viewModel?.deleJobfromList(jobDTO.JobId)
               }
           }

           updateItem()
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
