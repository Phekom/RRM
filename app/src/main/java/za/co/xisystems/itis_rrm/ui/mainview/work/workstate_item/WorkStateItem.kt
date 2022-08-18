/*
 * Updated by Shaun McDonald on 2021/01/25
 * Last modified on 2021/01/25 6:30 PM
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 */

package za.co.xisystems.itis_rrm.ui.mainview.work.workstate_item

import android.graphics.Color
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.viewbinding.BindableItem
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.data.localDB.entities.JobEstimateWorksDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.WfWorkStepDTO
import za.co.xisystems.itis_rrm.databinding.ListSelectorBinding
import za.co.xisystems.itis_rrm.ui.mainview.work.WorkViewModel
import za.co.xisystems.itis_rrm.utils.toast

/**
 * Created by Francis Mahlava on 2019/12/22.
 */

class WorkStateItem(
    private val jobItemWorks: JobEstimateWorksDTO?,
    private var activity: FragmentActivity?,
    private var groupAdapter: GroupAdapter<com.xwray.groupie.viewbinding.GroupieViewHolder<ListSelectorBinding>>,
    private val jobWorkStep: ArrayList<WfWorkStepDTO>,
    private val viewModel: WorkViewModel
) : BindableItem<ListSelectorBinding>() {

    companion object {
        var selected_position = -1
    }

    private var clickListener: ((WorkStateItem) -> Unit)? = null

    private fun setNewState(
        position: Int,
        viewBinding: ListSelectorBinding
    ) {
        // TO_DO (Replace this when Dynamic workflow is Functional)
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
            viewBinding.stateBack.setBackgroundColor(
                ContextCompat.getColor(
                    activity!!,
                    R.color.sanral_burnt_orange
                )
            )
            viewBinding.state.setTextColor(Color.WHITE)
        } else {
            viewBinding.stateBack.setBackgroundColor(Color.TRANSPARENT)
            viewBinding.state.setTextColor(Color.BLACK)
        }
    }

    override fun getLayout() = R.layout.list_selector

    /**
     * Perform any actions required to set up the view for display.
     *
     * @param viewBinding The ViewBinding to bind
     * @param position The adapter position
     */
    override fun bind(viewBinding: ListSelectorBinding, position: Int) {
        val step = jobWorkStep
        val workState = arrayOf(
            step[0].stepCode!!,
            step[1].stepCode!!,
            step[2].stepCode!!,
            step[3].stepCode!!,
            step[4].stepCode!!
        )

        viewBinding.apply {
            state.text = workState[position]

            setNewState(position, viewBinding)
            if (selected_position == position) {
                viewBinding.stateBack.setBackgroundColor(
                    ContextCompat.getColor(
                        activity!!,
                        R.color.sanral_burnt_orange
                    )
                )
                viewBinding.state.setTextColor(Color.WHITE)
            } else {
                viewBinding.stateBack.setBackgroundColor(Color.TRANSPARENT)
                viewBinding.state.setTextColor(Color.BLACK)
            }
            viewBinding.stateBack.setOnClickListener {
                activity?.toast(position.toString())

                // Coroutines.main {
                //     jobItemWorks?.let {
                //         // We can only review captured work until the estimates are posted
                //         if(position + 15 < it.actId) {
                //             jobItemWorks.estimateId?.let { estimateId ->
                //                 viewModel.populateWorkTab(estimateId, position + 15)
                //             }
                //
                //             groupAdapter.notifyItemChanged(position)
                //         } else {
                //             Timber.d("Only past work can be reviewed")
                //         }
                //     }
                //
                // }
            }
        }
        viewBinding.root.setOnClickListener {
            clickListener?.invoke(this)
        }
    }

    override fun initializeViewBinding(view: View): ListSelectorBinding {
        return ListSelectorBinding.bind(view)
    }
}
