package za.co.xisystems.itis_rrm.ui.mainview.estmeasure.estimate_measure_item

import android.annotation.SuppressLint
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.single_listview_item.*
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimateDTO
import za.co.xisystems.itis_rrm.ui.mainview.estmeasure.MeasureViewModel
import za.co.xisystems.itis_rrm.utils.Coroutines

/**
 * Created by Francis Mahlava on 2019/12/22.
 */


class EstimateMeasureItem(
    val jobItemEstimateDTO: JobItemEstimateDTO,
    private val measureViewModel: MeasureViewModel
) : Item(){

    var sectionId: String? = null
    @SuppressLint("SetTextI18n")
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.apply {
            appListID.text = getItemId(position + 1).toString()

            Coroutines.main {
                val jiNo = measureViewModel.getItemJobNo(jobItemEstimateDTO.jobId!!)
                listview_item_textView.text = "JI:${jiNo} - "
                val sectionId =
                    measureViewModel.getProjectSectionIdForJobId(jobItemEstimateDTO.jobId!!)
                val route = measureViewModel.getRouteForProjectSectionId(sectionId)
                val section = measureViewModel.getSectionForProjectSectionId(sectionId)
                apv_section.text = "( $route ${"/0$section"} )"
                val description = measureViewModel.getItemDescription(jobItemEstimateDTO.jobId!!)
                apv_description.text = description
            }

        }
    }

    override fun getLayout() = R.layout.single_listview_item

}

private fun GroupieViewHolder.getItemId(position: Int): Long {
    return position.toLong()
}
