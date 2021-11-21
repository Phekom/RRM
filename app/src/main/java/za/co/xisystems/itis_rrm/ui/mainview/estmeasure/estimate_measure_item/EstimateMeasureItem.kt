/*
 * Updated by Shaun McDonald on 2021/02/08
 * Last modified on 2021/02/08 3:35 PM
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 */

package za.co.xisystems.itis_rrm.ui.mainview.estmeasure.estimate_measure_item

import android.annotation.SuppressLint
import android.view.View
import com.xwray.groupie.viewbinding.BindableItem
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimateDTO
import za.co.xisystems.itis_rrm.databinding.ItemHeaderBinding
import za.co.xisystems.itis_rrm.ui.mainview.estmeasure.MeasureViewModel
import za.co.xisystems.itis_rrm.utils.Coroutines

/**
 * Created by Francis Mahlava on 2019/12/22.
 */

class EstimateMeasureItem(
    val jobItemEstimateDTO: JobItemEstimateDTO,
    private val measureViewModel: MeasureViewModel
): BindableItem<ItemHeaderBinding>() {

    var sectionId: String? = null

    @SuppressLint("SetTextI18n")
    override fun bind(viewBinding: ItemHeaderBinding, position: Int) {
        viewBinding.apply {
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

    private fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun initializeViewBinding(view: View): ItemHeaderBinding {
        return ItemHeaderBinding.bind(view)
    }
}
