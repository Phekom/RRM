package za.co.xisystems.itis_rrm.ui.mainview.approvemeasure.approveMeasure_Item

import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.single_listview_item.*
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemMeasureDTO
import za.co.xisystems.itis_rrm.ui.mainview.approvemeasure.ApproveMeasureViewModel
import za.co.xisystems.itis_rrm.utils.Coroutines

/**
 * Created by Francis Mahlava on 2019/12/22.
 */

class ApproveMeasureItem(
    val jobItemMeasureDTO: JobItemMeasureDTO,
//    val jobDTO: JobDTO,
    private val approveViewModel: ApproveMeasureViewModel
) : Item() {

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.apply {
            appListID.text = getItemId(position + 1).toString()
            listview_item_textView.text = "JI:${jobItemMeasureDTO.jimNo} - "
            Coroutines.main {
                val sectionId =
                    approveViewModel.getProjectSectionIdForJobId(jobItemMeasureDTO.jobId!!)
                val route = approveViewModel.getRouteForProjectSectionId(sectionId)
                val section = approveViewModel.getSectionForProjectSectionId(sectionId)
                apv_section.text = "( $route ${"/0$section"} )"
                val description = approveViewModel.getItemDesc(jobItemMeasureDTO.jobId!!)
                apv_description.text = description
            }
        }
    }

    override fun getLayout() = R.layout.single_listview_item
}

private fun getItemId(position: Int): Long {
    return position.toLong()
}
