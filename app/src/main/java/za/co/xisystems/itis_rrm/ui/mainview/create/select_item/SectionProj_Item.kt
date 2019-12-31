package za.co.xisystems.itis_rrm.ui.mainview.create.select_item

import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.project_item.*
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.data.localDB.entities.ItemDTO

/**
 * Created by Francis Mahlava on 2019/12/22.
 */


class SectionProj_Item(
    val itemDTO: ItemDTO
) : Item(){
    override fun bind(viewHolder: ViewHolder, position: Int) {
       viewHolder.apply {
           itemCode.text = itemDTO.itemCode
           project_descr_textView.text = itemDTO.descr
           unitof_M_textView.text = "UOM = " + itemDTO.uom
           updateItem()
       }
    }

    override fun getLayout() = R.layout.project_item

    private fun ViewHolder.updateItem(){

    }
    private fun ViewHolder.updatePojectItem(){

    }


}