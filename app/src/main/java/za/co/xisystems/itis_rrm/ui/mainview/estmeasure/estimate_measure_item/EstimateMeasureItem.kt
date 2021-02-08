/*
 * Updated by Shaun McDonald on 2021/02/08
 * Last modified on 2021/02/08 3:35 PM
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 */

package za.co.xisystems.itis_rrm.ui.mainview.estmeasure.estimate_measure_item

import android.annotation.SuppressLint
import android.view.View
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.item_header.*
import kotlinx.android.synthetic.main.single_listview_item.appListID
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
) : Item() {

    var sectionId: String? = null

    @SuppressLint("SetTextI18n")
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.apply {
            appListID.text = getItemId(position + 1).toString()

            Coroutines.main {
                val jiNo = measureViewModel.getItemJobNo(jobItemEstimateDTO.jobId!!)
                title.text = "JI:$jiNo"
                val sectionId =
                    measureViewModel.getProjectSectionIdForJobId(jobItemEstimateDTO.jobId!!)
                val route = measureViewModel.getRouteForProjectSectionId(sectionId)
                val section = measureViewModel.getSectionForProjectSectionId(sectionId)
                val description = measureViewModel.getItemDescription(jobItemEstimateDTO.jobId!!)
                subtitle.text = "( $route ${"/0$section"} ) - $description"
            }
            icon.visibility = View.GONE
        }
    }

    override fun getLayout() = R.layout.item_header
}

private fun GroupieViewHolder.getItemId(position: Int): Long {
    return position.toLong()
}
