package za.co.xisystems.itis_rrm.ui.mainview.approvejobs.approve_job_item

import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.single_listview_item.*
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.ui.mainview.approvejobs.ApproveJobsViewModel
import za.co.xisystems.itis_rrm.utils.Coroutines

/**
 * Created by Francis Mahlava on 2019/12/22.
 */


class ApproveJob_Item(
    val jobDTO: JobDTO,
    private val approveViewModel: ApproveJobsViewModel
) : Item(){

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
       viewHolder.apply {
           appListID.text = getItemId(position + 1).toString()
           listview_item_textView.text = "JI:${jobDTO.JiNo} - "
           Coroutines.main {
               val sectionId  =  approveViewModel?.getProjectSectionIdForJobId(jobDTO.JobId)
               val route  =  approveViewModel?.getRouteForProjectSectionId(sectionId!!)
               val section  =  approveViewModel?.getSectionForProjectSectionId(sectionId!!)
               apv_section.text =  "( ${route} ${"/0$section"} )"
           }
           apv_description.text = jobDTO.Descr
           updateItem()
       }
    }

    override fun getLayout() = R.layout.single_listview_item

    private fun GroupieViewHolder.updateItem(){

    }
    private fun GroupieViewHolder.updatePojectItem(){

    }


}

private fun GroupieViewHolder.getItemId(position: Int): Long {
    return position.toLong()
}
