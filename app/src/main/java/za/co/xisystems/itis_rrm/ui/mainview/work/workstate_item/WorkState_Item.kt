package za.co.xisystems.itis_rrm.ui.mainview.work.workstate_item

import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.list_selector.*
import kotlinx.android.synthetic.main.single_listview_item.*
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.data.localDB.entities.JobEstimateWorksDTO
import za.co.xisystems.itis_rrm.ui.mainview.work.WorkViewModel
import za.co.xisystems.itis_rrm.utils.Coroutines

/**
 * Created by Francis Mahlava on 2019/12/22.
 */


class WorkState_Item(
    val jobItemWorks : JobEstimateWorksDTO,
    private val approveViewModel: WorkViewModel
) : Item(){

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
       viewHolder.apply {
//           state.text = getItemId(position + 1).toString()
           if (jobItemWorks.actId == 15)
           state.text = "TA" else state.text = jobItemWorks.actId.toString()
//           Coroutines.main {
//               val sectionId  =  approveViewModel?.getProjectSectionIdForJobId(jobItemMeasureDTO.jobId!!)
//               val route  =  approveViewModel?.getRouteForProjectSectionId(sectionId!!)
//               val section  =  approveViewModel?.getSectionForProjectSectionId(sectionId!!)
//               apv_section.text =  "( ${route} ${"/0$section"} )"
//               val description  =  approveViewModel?.getItemDesc(jobItemMeasureDTO.jobId!!)
//               apv_description.text = description //list_selector
//           }
       }
    }

    override fun getLayout() = R.layout.list_selector

}

private fun GroupieViewHolder.getItemId(position: Int): Long {
    return position.toLong()
}
