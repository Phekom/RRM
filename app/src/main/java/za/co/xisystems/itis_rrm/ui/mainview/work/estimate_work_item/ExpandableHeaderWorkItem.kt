package za.co.xisystems.itis_rrm.ui.mainview.work.estimate_work_item

import android.graphics.drawable.Animatable
import android.view.View
import androidx.fragment.app.FragmentActivity
import com.xwray.groupie.ExpandableGroup
import com.xwray.groupie.ExpandableItem
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.data._commons.views.ToastUtils
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.databinding.ItemHeaderBinding
import za.co.xisystems.itis_rrm.ui.mainview._fragments.HeaderItem
import za.co.xisystems.itis_rrm.ui.mainview.work.WorkViewModel
import za.co.xisystems.itis_rrm.utils.Coroutines

class ExpandableHeaderWorkItem(
    private var activity: FragmentActivity?,
    workItems: JobDTO,
    workViewModel: WorkViewModel
) : HeaderItem(null, workItems, workViewModel), ExpandableItem {

    private var clickListener: ((ExpandableHeaderWorkItem) -> Unit)? = null
    var onExpandListener: ((ExpandableGroup) -> Unit)? = null
    private lateinit var expandableGroup: ExpandableGroup

    override fun bind(viewBinding: ItemHeaderBinding, position: Int) {
        super.bind(viewBinding, position)

        viewBinding.apply {
            appListID.text = getItemId(position + 1).toString()
            icon.apply {
                visibility = View.VISIBLE
                if (expandableGroup.isExpanded) {
                    setImageResource(R.drawable.collapse)
                } else {
                    setImageResource(R.drawable.expand)
                }
                setOnClickListener {
                    bindIcon(viewBinding)
                    expandableGroup.onToggleExpanded()
                    onExpandListener?.invoke(expandableGroup)
                }
            }
            bindItem(viewBinding)
        }

        viewBinding.root.setOnClickListener {
            clickListener?.invoke(this)
            expandableGroup.onToggleExpanded()
            onExpandListener?.invoke(expandableGroup)
        }
    }

    private fun bindItem(viewBinding: ItemHeaderBinding) {
        Coroutines.main {
            val startKm = workViewModel.getItemStartKm(jobId)
            val endKm = workViewModel.getItemEndKm(jobId)
            val trackRouteId = workViewModel.getItemTrackRouteId(jobId)

            viewBinding.headerLin2.setOnClickListener {
                when {
                    startKm <= endKm -> {
                        ToastUtils().toastShort(
                            activity,
                            "Job Info: Start Km: $startKm - End Km: $endKm"
                        )
                    }
                    trackRouteId.isBlank() -> {
                        ToastUtils().toastLong(
                            activity,
                            "Job not found please click on item to download job."
                        )
                    }
                }
                expandableGroup.onToggleExpanded()
                onExpandListener?.invoke(expandableGroup)
            }
        }
    }

    private fun bindIcon(viewHolder: ItemHeaderBinding) {
        viewHolder.icon.apply {
            visibility = View.VISIBLE
            setImageResource(
                if (expandableGroup.isExpanded) {
                    R.drawable.collapse_animated
                } else {
                    R.drawable.expand_animated
                }
            )
            (drawable as Animatable).start()
        }
    }

    override fun setExpandableGroup(onToggleListener: ExpandableGroup) {
        this.expandableGroup = onToggleListener
    }

    private fun getItemId(position: Int): Long {
        return position.toLong()
    }
}
