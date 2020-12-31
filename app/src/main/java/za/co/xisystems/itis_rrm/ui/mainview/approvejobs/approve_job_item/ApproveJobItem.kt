package za.co.xisystems.itis_rrm.ui.mainview.approvejobs.approve_job_item

import android.content.Context
import android.view.View
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.item_header.*
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.ui.mainview.approvejobs.ApproveJobsViewModel
import za.co.xisystems.itis_rrm.utils.Coroutines

/**
 * Created by Francis Mahlava on 2019/12/22.
 */

class ApproveJobItem(
    val jobDTO: JobDTO,
    private val approveViewModel: ApproveJobsViewModel,
    private val context: Context
) : Item() {
    var route: String? = null
    var section: String? = null
    var sectionId: String? = null
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.apply {
            appListID.text = getItemId(position + 1).toString()
            title.text = context.getString(R.string.pair, "JI: ", jobDTO.JiNo.toString())
            Coroutines.main {
                sectionId = approveViewModel.getProjectSectionIdForJobId(jobDTO.JobId)
                if (sectionId.isNullOrEmpty()) sectionId = ""
                route = approveViewModel.getRouteForProjectSectionId(sectionId!!)
                if (route.isNullOrEmpty()) route = ""
                section = approveViewModel.getSectionForProjectSectionId(sectionId!!)
                if (section.isNullOrEmpty()) section = ""
                subtitle.apply {
                    text = context.getString(
                        R.string.pair,
                        context.getString(R.string.route_section_badge, route, section),
                        jobDTO.Descr
                    )
                }
            }
            icon.visibility = View.GONE
        }
    }

    override fun getLayout() = R.layout.item_header
}

private fun getItemId(position: Int): Long {
    return position.toLong()
}
