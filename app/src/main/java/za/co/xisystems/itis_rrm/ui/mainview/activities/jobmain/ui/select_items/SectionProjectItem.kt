package za.co.xisystems.itis_rrm.ui.mainview.activities.jobmain.ui.select_items

import android.view.View
import androidx.navigation.Navigation
import com.xwray.groupie.viewbinding.BindableItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.data.localDB.entities.ItemDTOTemp
import za.co.xisystems.itis_rrm.data.localDB.entities.ItemSectionDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.ProjectItemDTO
import za.co.xisystems.itis_rrm.databinding.ProjectItemBinding
import za.co.xisystems.itis_rrm.extensions.uomForUI
import za.co.xisystems.itis_rrm.utils.Coroutines
import za.co.xisystems.itis_rrm.utils.JobUtils
import java.util.ArrayList

/**
 * Created by Francis Mahlava on 2019/12/22.
 */

class SectionProjectItem(
    val itemDTO: ProjectItemDTO,
    val itemSections: ArrayList<ItemSectionDTO>,
    val jobId: String?,
    val projectId: String,
    val viewModel: SelectItemsViewModel
) : BindableItem<ProjectItemBinding>() {
    override fun getLayout() = R.layout.project_item
    override fun initializeViewBinding(view: View): ProjectItemBinding {
        return ProjectItemBinding.bind(view)
    }
    private var clickListener: ((SectionProjectItem) -> Unit)? = null

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
        viewBinding.root.setOnClickListener { view ->
            clickListener?.invoke(this)
            val tempItem = createItemList(itemDTO, itemSections)
            saveNewItem(tempItem)
            sendSelectedItem(tempItem, view)
        }
    }

    private fun createItemList(
        itemDTO: ProjectItemDTO,
        itemSections: ArrayList<ItemSectionDTO>
    ): ItemDTOTemp {
        val newItem = ItemDTOTemp(
            id = 0,
            itemId = itemDTO.itemId,
            descr = itemDTO.descr,
            itemCode = itemDTO.itemCode,
            itemSections = itemSections,
            tenderRate = itemDTO.tenderRate,
            uom = itemDTO.uom,
            workflowId = itemDTO.workflowId,
            sectionItemId = itemDTO.sectionItemId,
            quantity = itemDTO.quantity,
            estimateId = itemDTO.estimateId,
            projectId = itemDTO.projectId!!,
            jobId = jobId!!
        )
        return newItem
    }


    private fun saveNewItem(tempItem: ItemDTOTemp) {
        Coroutines.main {
            viewModel.saveNewItem(tempItem)
        }
    }



    private fun sendSelectedItem(
        item: ItemDTOTemp,
        view: View
    ) {
//        Coroutines.io {
//            withContext(Dispatchers.Main.immediate) {
                val navDirection = SelectProjectItemsFragmentDirections
                    .actionNavigationSelectItemsToNavigationAddItems(projectId, jobId)
                Navigation.findNavController(view).navigate(navDirection)
//            }
//        }
    }




}
