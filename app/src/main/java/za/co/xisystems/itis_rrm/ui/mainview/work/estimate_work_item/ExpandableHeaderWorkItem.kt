package za.co.xisystems.itis_rrm.ui.mainview.work.estimate_work_item

import android.graphics.drawable.Animatable
import android.view.View
import androidx.fragment.app.FragmentActivity
import com.xwray.groupie.ExpandableGroup
import com.xwray.groupie.ExpandableItem
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import kotlinx.android.synthetic.main.item_header.*
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.data._commons.views.ToastUtils
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.ui.mainview._fragments.HeaderItem
import za.co.xisystems.itis_rrm.ui.mainview.work.WorkViewModel
import za.co.xisystems.itis_rrm.utils.Coroutines

class ExpandableHeaderWorkItem(
    activity: FragmentActivity?,
    workItems: JobDTO,
//    workItems: JobItemEstimateDTO,
    workViewModel: WorkViewModel
) : HeaderItem(null, workItems, workViewModel), ExpandableItem {

    private var clickListener: ((ExpandableHeaderWorkItem) -> Unit)? = null

    private lateinit var expandableGroup: ExpandableGroup
    private var activity = activity
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        super.bind(viewHolder, position)

        viewHolder.apply {
            appListID.text = getItemId(position + 1).toString()
            icon.apply {
                visibility = View.VISIBLE
                setImageResource(if (expandableGroup.isExpanded) R.drawable.collapse else R.drawable.expand)
                setOnClickListener {
                    expandableGroup.onToggleExpanded()
                    bindIcon(viewHolder)
                }
            }
            bindItem(viewHolder)
        }

        viewHolder.itemView.setOnClickListener {
            clickListener?.invoke(this)
        }
    }

    private fun bindItem(viewHolder: GroupieViewHolder) {
        Coroutines.main {
            val startKm = workViewModel.getItemStartKm(jobId)
            val endKm = workViewModel.getItemEndKm(jobId)
            val trackRouteId = workViewModel.getItemTrackRouteId(jobId)

            viewHolder.headerLin2.setOnClickListener {
                if (startKm <= endKm) {
                    ToastUtils().toastShort(
                        activity,
                        "Job Info: Start Km: $startKm - End Km: $endKm"
                    )
                } else if (trackRouteId.isBlank()) {
                    ToastUtils().toastLong(
                        activity,
                        "Job not found please click on item to download job."
                    )
                }
            }
        }
    }

    private fun bindIcon(viewHolder: GroupieViewHolder) {
        viewHolder.icon.apply {
            visibility = View.VISIBLE
            setImageResource(if (expandableGroup.isExpanded) R.drawable.collapse_animated else R.drawable.expand_animated)
            (drawable as Animatable).start()
        }
    }

    override fun setExpandableGroup(onToggleListener: ExpandableGroup) {
        this.expandableGroup = onToggleListener
    }
}

private fun getItemId(position: Int): Long {
    return position.toLong()
}
