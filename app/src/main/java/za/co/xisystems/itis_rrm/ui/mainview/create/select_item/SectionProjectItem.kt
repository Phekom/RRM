package za.co.xisystems.itis_rrm.ui.mainview.create.select_item

import android.view.View
import com.xwray.groupie.viewbinding.BindableItem
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.data.localDB.entities.ProjectItemDTO
import za.co.xisystems.itis_rrm.databinding.ProjectItemBinding
import za.co.xisystems.itis_rrm.extensions.uomForUI

/**
 * Created by Francis Mahlava on 2019/12/22.
 */

class SectionProjectItem(
    val itemDTO: ProjectItemDTO
) : BindableItem<ProjectItemBinding>() {
    override fun bind(viewBinding: ProjectItemBinding, position: Int) {
        viewBinding.apply {
            itemCode.text = itemDTO.itemCode
            projectDescrTextView.text = itemDTO.descr
            unitofMTextView.text = ""
            itemDTO.uom?.let {
                unitofMTextView.text = root.context.uomForUI(itemDTO.uom)
            }
        }
    }

    override fun getLayout() = R.layout.project_item

    override fun initializeViewBinding(view: View): ProjectItemBinding {
        return ProjectItemBinding.bind(view)
    }
}
