package za.co.xisystems.itis_rrm.ui.mainview.estmeasure.estimate_measure_item

import android.view.View
import androidx.annotation.DrawableRes
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.item_header.*
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimateDTO
import za.co.xisystems.itis_rrm.ui.mainview.estmeasure.MeasureViewModel
import za.co.xisystems.itis_rrm.ui.mainview.work.WorkViewModel
import za.co.xisystems.itis_rrm.utils.Coroutines

open class Measure_HeaderItem(
    @DrawableRes private val iconResId: Int? = null,
    measures: JobItemEstimateDTO,
    measureViewModel: MeasureViewModel,
    private val onIconClickListener: View.OnClickListener? = null) : Item() {

    var projID = measures.projectItemId
    var qty = measures.qty
    var measureViewModel = measureViewModel

    override fun getLayout(): Int {
        return R.layout.item_measure_header
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.apply {
            Coroutines.main {
            subtitle.apply {
                    val subtitleResId = qty
                    visibility = View.GONE
                    subtitleResId?.let {
                        visibility = View.VISIBLE
                        text = "Quantity : ${it.toString().dropLast(2)}"
                    }
                }
                val desc  = measureViewModel?.getDescForProjectItemId(projID!!)
                title.text = "Estimate - ${desc} "

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