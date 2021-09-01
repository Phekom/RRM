/*
 * Updated by Shaun McDonald on 2021/01/25
 * Last modified on 2021/01/25 6:30 PM
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 */

package za.co.xisystems.itis_rrm.ui.mainview.work.workstate_item

import android.graphics.Color
import androidx.core.content.ContextCompat
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.list_selector.*
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.data.localDB.entities.JobEstimateWorksDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.WfWorkStepDTO
import za.co.xisystems.itis_rrm.ui.mainview.work.WorkViewModel
import za.co.xisystems.itis_rrm.ui.mainview.work.capture_work.CaptureWorkFragment
import za.co.xisystems.itis_rrm.utils.Coroutines
import java.lang.ref.WeakReference

/**
 * Created by Francis Mahlava on 2019/12/22.
 */

class WorkStateItem(
    private val jobItemWorks: JobEstimateWorksDTO?,
    private var fragmentRef: WeakReference<CaptureWorkFragment>?,
    private var groupAdapter: GroupAdapter<GroupieViewHolder>,
    private val jobWorkStep: ArrayList<WfWorkStepDTO>,
    private val viewModel: WorkViewModel
) : Item() {

    companion object {
        var selected_position = -1
    }

    private var clickListener: ((WorkStateItem) -> Unit)? = null

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {

        val step = jobWorkStep
        val workState = arrayOf(
            step[0].stepCode!!,
            step[1].stepCode!!,
            step[2].stepCode!!,
            step[3].stepCode!!,
            step[4].stepCode!!
        )

        viewHolder.apply {
            state.text = workState[position]

            setNewState(position, viewHolder, jobItemWorks?.actId!!)
            if (selected_position == position) {
                viewHolder.stateBack.setBackgroundColor(
                    ContextCompat.getColor(
                        fragmentRef?.get()?.requireActivity()!!,
                        R.color.sanral_burnt_orange
                    )
                )
                viewHolder.state.setTextColor(Color.WHITE)
            } else {
                viewHolder.stateBack.setBackgroundColor(Color.TRANSPARENT)
                viewHolder.state.setTextColor(Color.BLACK)
            }
            viewHolder.stateBack.setOnClickListener {
                val selectedPosition = position
                Coroutines.main {
                    // We can only review captured work until the estimates are posted
                    val activityId = position + 15
                    if (activityId <= jobItemWorks.actId) {
                        setNewState(selectedPosition, viewHolder, activityId)
                        fragmentRef?.get()?.loadPictures(jobItemWorks, activityId)
                        groupAdapter.notifyItemChanged(position)
                    } else {
                        fragmentRef?.get()?.showFutureWarning()
                    }
                }
            }
        }
        viewHolder.itemView.setOnClickListener {
            clickListener?.invoke(this)
        }
    }

    private fun setNewState(
        position: Int,
        viewHolder: GroupieViewHolder,
        actId: Int
    ) {
        // TODO (Replace this when Dynamic workflow is Functional)
        when (actId) {
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
                    fragmentRef?.get()?.requireContext()!!,
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
