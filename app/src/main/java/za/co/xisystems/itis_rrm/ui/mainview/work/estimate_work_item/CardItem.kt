package za.co.xisystems.itis_rrm.ui.mainview.work.estimate_work_item

import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.work_list_item.*
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.ui.mainview.work.INSET
import za.co.xisystems.itis_rrm.ui.mainview.work.INSET_TYPE_KEY

open class CardItem (val text: String,val qty: String,val rate: String) : Item() {

    init {
        extras[INSET_TYPE_KEY] = INSET
    }

    override fun getLayout() = R.layout.work_list_item

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.apply {
            expandable_child_textView.text = text
            qty_textView.text = qty
            line_amount_textView.text = rate
        }

    }
}
