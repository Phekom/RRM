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
import com.xwray.groupie.viewbinding.BindableItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimateDTO
import za.co.xisystems.itis_rrm.databinding.WorkListItemBinding
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
) : BindableItem<WorkListItemBinding>() {

    init {
        extras[INSET_TYPE_KEY] = INSET
    }

    override fun getLayout() = R.layout.work_list_item

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

    /**
     * Perform any actions required to set up the view for display.
     *
     * @param viewBinding The ViewBinding to bind
     * @param position The adapter position
     */
    override fun bind(viewBinding: WorkListItemBinding, position: Int) {
        viewBinding.apply {
            Coroutines.main {
                expandableChildTextView.text = text
                qtyTextView.text = qty
                lineAmountTextView.text = rate
            }

            startWorkBtn.setOnClickListener {
                sendJobToWork(workViewModel, jobItemEstimate, root, job)
            }
        }
    }

    override fun initializeViewBinding(view: View): WorkListItemBinding {
        return WorkListItemBinding.bind(view)
    }
}
