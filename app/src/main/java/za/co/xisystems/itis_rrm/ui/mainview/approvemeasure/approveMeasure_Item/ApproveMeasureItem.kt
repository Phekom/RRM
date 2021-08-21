package za.co.xisystems.itis_rrm.ui.mainview.approvemeasure.approveMeasure_Item

import android.view.View
import com.xwray.groupie.viewbinding.BindableItem
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemMeasureDTO
import za.co.xisystems.itis_rrm.databinding.ItemHeaderBinding
import za.co.xisystems.itis_rrm.ui.mainview.approvemeasure.ApproveMeasureViewModel
import za.co.xisystems.itis_rrm.utils.Coroutines

/**
 * Created by Francis Mahlava on 2019/12/22.
 */

class ApproveMeasureItem(
    private val jobItemMeasureDTO: JobItemMeasureDTO,
    private val approveViewModel: ApproveMeasureViewModel
) : BindableItem<ItemHeaderBinding>() {
    val jobId = jobItemMeasureDTO.jobId

    override fun getLayout() = R.layout.item_header

    /**
     * Perform any actions required to set up the view for display.
     *
     * @param viewBinding The ViewBinding to bind
     * @param position The adapter position
     */
    override fun bind(viewBinding: ItemHeaderBinding, position: Int) {
        viewBinding.apply {
            appListID.text = getItemId(position + 1).toString()
            title.text = root.context.getString(
                R.string.pair,
                "JI:",
                jobItemMeasureDTO.jimNo
            )
            Coroutines.main {
                val sectionId =
                    approveViewModel.getProjectSectionIdForJobId(jobItemMeasureDTO.jobId!!)
                val route = approveViewModel.getRouteForProjectSectionId(sectionId)
                val section = approveViewModel.getSectionForProjectSectionId(sectionId)
                val routeSection = root.context.getString(R.string.route_section_badge, route, section)
                val description = approveViewModel.getItemDesc(jobItemMeasureDTO.jobId!!)
                subtitle.run {
                    text = context.getString(R.string.pair, routeSection, description)
                }
                icon.visibility = View.GONE
            }
        }
    }

    override fun initializeViewBinding(view: View): ItemHeaderBinding {
        return ItemHeaderBinding.bind(view)
    }
    private fun getItemId(position: Int): Long {
        return position.toLong()
    }
}
