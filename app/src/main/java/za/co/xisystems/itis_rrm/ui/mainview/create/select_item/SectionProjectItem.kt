package za.co.xisystems.itis_rrm.ui.mainview.create.select_item

import android.view.View
import com.xwray.groupie.viewbinding.BindableItem
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.data.localDB.entities.ProjectItemDTO
import za.co.xisystems.itis_rrm.databinding.ProjectItemBinding
import za.co.xisystems.itis_rrm.extensions.uomForUI
import za.co.xisystems.itis_rrm.utils.JobUtils

/**
 * Created by Francis Mahlava on 2019/12/22.
 */

class SectionProjectItem(
    val itemDTO: ProjectItemDTO
) : BindableItem<ProjectItemBinding>() {

    override fun initializeViewBinding(view: View): ProjectItemBinding {
        return ProjectItemBinding.bind(view)
    }

    override fun bind(viewBinding: ProjectItemBinding, position: Int) {
        viewBinding.apply {
            itemCode.text = itemDTO.itemCode
            projectDescrTextView.text = itemDTO.descr
            unitofMTextView.text = ""
            when (itemDTO.uom.isNullOrBlank()) {
                true -> {
                    val defaultUOM = root.context.resources.getString(R.string.default_uom)
                    unitofMTextView.text = root.context.resources.getString(
                        R.string.pair,
                        JobUtils.formatCost(itemDTO.tenderRate),
                        defaultUOM
                    )
                }
                else -> {
                    unitofMTextView.text = root.context.getString(
                        R.string.pair,
                        JobUtils.formatCost(itemDTO.tenderRate),
                        root.context.uomForUI(itemDTO.uom)
                    )
                }
            }
        }
    }

    override fun getLayout(): Int {
        return R.layout.project_item
    }
}
