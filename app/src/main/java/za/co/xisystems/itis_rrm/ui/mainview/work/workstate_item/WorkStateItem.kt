package za.co.xisystems.itis_rrm.ui.mainview.work.workstate_item

import android.graphics.Color
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.list_selector.*
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.data.localDB.entities.JobEstimateWorksDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.WF_WorkStepDTO
import za.co.xisystems.itis_rrm.ui.mainview.work.WorkViewModel
import za.co.xisystems.itis_rrm.utils.Coroutines
import za.co.xisystems.itis_rrm.utils.toast

/**
 * Created by Francis Mahlava on 2019/12/22.
 */

class WorkStateItem(
    private val jobItemWorks: JobEstimateWorksDTO?,
    private var activity: FragmentActivity?,
    private var groupAdapter: GroupAdapter<GroupieViewHolder>,
    private val jobWorkStep: ArrayList<WF_WorkStepDTO>,
    private val viewModel: WorkViewModel
) : Item() {

    companion object {
        var selected_position = -1
    }

    private var clickListener: ((WorkStateItem) -> Unit)? = null

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {

        val step = jobWorkStep
        val workState = arrayOf(
            step[0].Step_Code!!,
            step[1].Step_Code!!,
            step[2].Step_Code!!,
            step[3].Step_Code!!,
            step[4].Step_Code!!
        )

        viewHolder.apply {
            state.text = workState[position]

            setNewState(position, viewHolder)
            if (selected_position == adapterPosition) {
                viewHolder.stateBack.setBackgroundColor(
                    ContextCompat.getColor(
                        activity!!,
                        R.color.sanral_burnt_orange
                    )
                )
                viewHolder.state.setTextColor(Color.WHITE)
            } else {
                viewHolder.stateBack.setBackgroundColor(Color.TRANSPARENT)
                viewHolder.state.setTextColor(Color.BLACK)
            }
            viewHolder.stateBack.setOnClickListener {
                activity?.toast(position.toString())
                selected_position = position
                Coroutines.main {
                    jobItemWorks?.estimateId?.let {
                        viewModel.populateWorkTab(it, position + 15)
                    }

                }
                groupAdapter.notifyDataSetChanged()
            }
        }
        viewHolder.itemView.setOnClickListener {
            clickListener?.invoke(this)
        }
    }

    private fun setNewState(
        position: Int,
        viewHolder: GroupieViewHolder
    ) {
        // TODO (Replace this when Dynamic workflow is Functional)
        when (jobItemWorks?.actId) {
            15 -> {
                selected_position = 0
            }
            16 -> {
                selected_position = 1
            }
            17 -> {
                selected_position = 2
            }
            18 -> {
                selected_position = 3
            }
            19 -> {
                selected_position = 4
            }
        }
        if (selected_position == position) {
            viewHolder.stateBack.setBackgroundColor(
                ContextCompat.getColor(
                    activity!!,
                    R.color.sanral_burnt_orange
                )
            )
            viewHolder.state.setTextColor(Color.WHITE)
        } else {
            viewHolder.stateBack.setBackgroundColor(Color.TRANSPARENT)
            viewHolder.state.setTextColor(Color.BLACK)
        }
    }

    override fun getLayout() = R.layout.list_selector
}
