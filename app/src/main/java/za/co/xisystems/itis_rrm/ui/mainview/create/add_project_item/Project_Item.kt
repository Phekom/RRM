package za.co.xisystems.itis_rrm.ui.mainview.create.add_project_item

import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.new_job_item.*
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.data.localDB.entities.ItemDTO

/**
 * Created by Francis Mahlava on 2019/12/29.
 */
class Project_Item(
    val itemDTO: ItemDTO
) : Item(){
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.apply {
            textViewItem.text = itemDTO.itemCode +"  "+ itemDTO.descr
//            project_descr_textView.text = itemDTO.descr
//            unitof_M_textView.text = "UOM = " + itemDTO.uom
            updateItem()
        }
    }

    override fun getLayout() = R.layout.new_job_item

    private fun ViewHolder.updateItem(){

    }


}