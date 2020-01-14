package za.co.xisystems.itis_rrm.ui.mainview.create.add_project_item

import androidx.recyclerview.widget.ItemTouchHelper
import za.co.xisystems.itis_rrm.ui.mainview.create.select_item.SectionProj_Item

//class SwipeToDeleteItem(itemDesc: String, cost: String) : Project_Item(itemDesc, cost) {
    class SwipeToDeleteItem(itemDesc: SectionProj_Item) : Project_Item(itemDesc) {
    override fun getSwipeDirs(): Int {
        return ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
    }
}
