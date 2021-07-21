/**
 * Updated by Shaun McDonald on 2021/05/15
 * Last modified on 2021/05/14, 20:32
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 **/

/**
 * Updated by Shaun McDonald on 2021/05/14
 * Last modified on 2021/05/14, 19:43
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 **/

package za.co.xisystems.itis_rrm.ui.mainview.work.estimate_work_item

import android.view.View
import androidx.fragment.app.FragmentActivity
import androidx.navigation.Navigation
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.work_list_item.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimateDTO
import za.co.xisystems.itis_rrm.ui.mainview.work.INSET
import za.co.xisystems.itis_rrm.ui.mainview.work.INSET_TYPE_KEY
import za.co.xisystems.itis_rrm.ui.mainview.work.WorkFragmentDirections
import za.co.xisystems.itis_rrm.ui.mainview.work.WorkViewModel
import za.co.xisystems.itis_rrm.utils.Coroutines

open class CardItem(
    val activity: FragmentActivity?,
    val text: String,
    val qty: String,
    val rate: String,
    val estimateId: String,
    private val workViewModel: WorkViewModel,
    private val jobItemEstimate: JobItemEstimateDTO,
    private val job: JobDTO
) : Item() {

    init {
        extras[INSET_TYPE_KEY] = INSET
    }

    override fun getLayout() = R.layout.work_list_item

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.apply {
            Coroutines.main {
                expandable_child_textView.text = text
                qty_textView.text = qty
                line_amount_textView.text = rate
            }

            startWork_Btn.setOnClickListener {
                sendJobToWork(workViewModel, jobItemEstimate, it, job)
            }
        }
    }

    private fun sendJobToWork(
        workViewModel: WorkViewModel,
        estimate: JobItemEstimateDTO,
        view: View?,
        job: JobDTO
    ) {
        Coroutines.io {
            workViewModel.setWorkItemJob(job.jobId)
            workViewModel.setWorkItem(estimate.estimateId)
            withContext(Dispatchers.Main.immediate) {
                val navDirection = WorkFragmentDirections.actionNavWorkToCaptureWorkFragment(
                    jobId = job.jobId,
                    estimateId = estimate.estimateId
                )
                Navigation.findNavController(view!!).navigate(navDirection)
            }
        }
    }
}
