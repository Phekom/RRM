package za.co.xisystems.itis_rrm.ui.mainview.approvemeasure.approveMeasure_Item

import android.view.View
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.single_job_listing.*
import kotlinx.android.synthetic.main.single_listview_item.appListID
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemMeasureDTO
import za.co.xisystems.itis_rrm.ui.mainview.approvemeasure.ApproveMeasureViewModel
import za.co.xisystems.itis_rrm.utils.Coroutines

/**
 * Created by Francis Mahlava on 2019/12/22.
 */

class ApproveMeasureItem(
    private val jobItemMeasureDTO: JobItemMeasureDTO,
    private val approveViewModel: ApproveMeasureViewModel
) : Item() {
    val jobId = jobItemMeasureDTO.jobId
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.apply {
            appListID.text = getItemId(position + 1).toString()
            title.text = itemView.context.getString(
                R.string.pair,
                "JI:",
                jobItemMeasureDTO.jimNo
            )
            Coroutines.main {
                val sectionId =
                    approveViewModel.getProjectSectionIdForJobId(jobItemMeasureDTO.jobId!!)
                val route = approveViewModel.getRouteForProjectSectionId(sectionId)
                val section = approveViewModel.getSectionForProjectSectionId(sectionId)
                val routeSection = itemView.context.getString(R.string.route_section_badge, route, section)
                val description = approveViewModel.getItemDesc(jobItemMeasureDTO.jobId!!)
                subtitle.run {
                    text = context.getString(R.string.pair, routeSection, description)
                }
                icon.visibility = View.GONE
            }
        }
    }

    override fun getLayout() = R.layout.item_header
}

private fun getItemId(position: Int): Long {
    return position.toLong()
}
