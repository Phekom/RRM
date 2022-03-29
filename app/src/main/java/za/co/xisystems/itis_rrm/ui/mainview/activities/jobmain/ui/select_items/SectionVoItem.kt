package za.co.xisystems.itis_rrm.ui.mainview.activities.jobmain.ui.select_items

import android.view.View
import androidx.navigation.Navigation
import com.xwray.groupie.viewbinding.BindableItem
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.data.localDB.entities.ItemDTOTemp
import za.co.xisystems.itis_rrm.data.localDB.entities.ItemSectionDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.VoItemDTO
import za.co.xisystems.itis_rrm.databinding.ProjectItemBinding
import za.co.xisystems.itis_rrm.extensions.uomForUI
import za.co.xisystems.itis_rrm.utils.Coroutines
import za.co.xisystems.itis_rrm.utils.JobUtils
import java.util.*

/**
 * Created by Francis Mahlava on 2019/12/22.
 */

class SectionVoItem(
    val projectItem: VoItemDTO,
    val itemSections: ArrayList<ItemSectionDTO>,
    val projectItemArgsData: SelectProjectItemsFragmentArgs,
    val contractVoId: String,
    val selectItemsViewModel: SelectItemsViewModel
) : BindableItem<ProjectItemBinding>() {
    override fun getLayout() = R.layout.project_item
    override fun initializeViewBinding(view: View): ProjectItemBinding {
        return ProjectItemBinding.bind(view)
    }
    private var clickListener: ((SectionVoItem) -> Unit)? = null

    override fun bind(viewBinding: ProjectItemBinding, position: Int) {
        viewBinding.apply {
            itemCode.text = projectItem.itemCode
            projectDescrTextView.text = projectItem.descr
            unitofMTextView.text = ""
            when (projectItem.uom.isNullOrBlank()) {
                true -> {
                    val defaultUOM = root.context.resources.getString(R.string.default_uom)
                    unitofMTextView.text = root.context.resources.getString(
                        R.string.pair,
                        JobUtils.formatCost(projectItem.tenderValue!!),
                        defaultUOM
                    )
                }
                else -> {
                    unitofMTextView.text = root.context.getString(
                        R.string.pair,
                        JobUtils.formatCost(projectItem.tenderValue!!),
                        root.context.uomForUI(projectItem.uom)
                    )
                }
            }
        }
        viewBinding.root.setOnClickListener { view ->
            clickListener?.invoke(this)
            val tempItem = createItemList(projectItem, itemSections)
            saveNewItem(tempItem)
            sendSelectedItem(tempItem, view)
        }
    }

    private fun createItemList(
        itemDTO: VoItemDTO,
        itemSections: ArrayList<ItemSectionDTO>
    ): ItemDTOTemp {
        val newItem = ItemDTOTemp(
            id = 0,
            itemId = itemDTO.projectVoItemId!!,
            descr = itemDTO.descr,
            itemCode = itemDTO.itemCode,
            itemSections = itemSections,
            tenderRate = itemDTO.tenderValue!!,
            uom = itemDTO.uom,
            workflowId = itemDTO.workflowId,
            sectionItemId = itemDTO.sectionItemId,
            quantity = itemDTO.quantity,
            estimateId = itemDTO.estimateId,
            projectId = itemDTO.projectId,
            jobId = projectItemArgsData.jobId!!,
            contractVoId = itemDTO.contractVoId!!,
            projectVoId = itemDTO.projectVoId,
        )
        return newItem
    }


    private fun saveNewItem(tempItem: ItemDTOTemp) {
        Coroutines.main {
            selectItemsViewModel.saveNewItem(tempItem)
        }
    }



    private fun sendSelectedItem(
        item: ItemDTOTemp,
        view: View
    ) {
//        Coroutines.io {
//            withContext(Dispatchers.Main.immediate) {
                val navDirection = SelectProjectItemsFragmentDirections
                    .actionNavigationSelectItemsToNavigationAddItems(item.projectId, item.jobId,item.contractVoId)
                Navigation.findNavController(view).navigate(navDirection)
//            }
//        }
    }




}
