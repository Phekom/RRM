package za.co.xisystems.itis_rrm.ui.mainview._fragments

import android.view.View
import androidx.annotation.DrawableRes
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.item_header.*
import za.co.xisystems.itis_rrm.R

open class HeaderItem(
    private val titleStringResId: String,
    private val subtitleResId: String? = null,
    @DrawableRes private val iconResId: Int? = null,
    private val onIconClickListener: View.OnClickListener? = null) : Item() {

    override fun getLayout(): Int {
        return R.layout.item_header
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.title.setText(titleStringResId)

        viewHolder.subtitle.apply {
            visibility = View.GONE
            subtitleResId?.let {
                visibility = View.VISIBLE
                setText(it)
            }
        }

        viewHolder.icon.apply {
            visibility = View.GONE
            iconResId?.let {
                visibility = View.VISIBLE
                setImageResource(it)
                setOnClickListener(onIconClickListener)
            }
        }
    }
}