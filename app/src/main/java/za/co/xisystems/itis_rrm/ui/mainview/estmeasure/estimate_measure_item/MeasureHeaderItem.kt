package za.co.xisystems.itis_rrm.ui.mainview.estmeasure.estimate_measure_item

import android.view.View
import androidx.annotation.DrawableRes
import com.xwray.groupie.viewbinding.BindableItem
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimateDTO
import za.co.xisystems.itis_rrm.databinding.ItemMeasureHeaderBinding
import za.co.xisystems.itis_rrm.ui.mainview.estmeasure.MeasureViewModel
import za.co.xisystems.itis_rrm.utils.Coroutines

open class MeasureHeaderItem(
    @DrawableRes private val iconResId: Int? = null,
    measures: JobItemEstimateDTO,
    var measureViewModel: MeasureViewModel,
    private val onIconClickListener: View.OnClickListener? = null
) : BindableItem<ItemMeasureHeaderBinding>() {

    private var projectId = measures.projectItemId
    var qty = measures.qty

    override fun getLayout(): Int {
        return R.layout.item_measure_header
    }

    override fun bind(viewBinding: ItemMeasureHeaderBinding, position: Int) {
        viewBinding.apply {
            Coroutines.main {
                subtitle.apply {
                    val subtitleResId = qty
                    visibility = View.GONE
                    subtitleResId.let {
                        visibility = View.VISIBLE
                        text = root.rootView.context.getString(R.string.pair, "Quantity : ", it.toString().dropLast(2))
                    }
                }
                val desc = measureViewModel.getDescForProjectItemId(projectId!!)
                title.text = root.rootView.context.getString(R.string.pair, "Estimate - ", desc)
            }
        }

        viewBinding.icon.apply {
            visibility = View.GONE
            iconResId?.let {
                visibility = View.VISIBLE
                setImageResource(it)
                setOnClickListener(onIconClickListener)
            }
        }
    }

    override fun initializeViewBinding(view: View): ItemMeasureHeaderBinding {
        return ItemMeasureHeaderBinding.bind(view)
    }
}
