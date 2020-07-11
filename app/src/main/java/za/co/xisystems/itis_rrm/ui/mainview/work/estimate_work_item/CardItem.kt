package za.co.xisystems.itis_rrm.ui.mainview.work.estimate_work_item

import android.view.View
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.work_list_item.*
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimateDTO
import za.co.xisystems.itis_rrm.ui.mainview.work.INSET
import za.co.xisystems.itis_rrm.ui.mainview.work.INSET_TYPE_KEY
import za.co.xisystems.itis_rrm.ui.mainview.work.WorkViewModel
import za.co.xisystems.itis_rrm.utils.ActivityIdConstants
import za.co.xisystems.itis_rrm.utils.Coroutines

open class CardItem(
    val activity: FragmentActivity?,
    val text: String,
    val qty: String,
    val rate: String,
    val estimateId: String,
    private val workViewModel: WorkViewModel,
    private val itemEsti: JobItemEstimateDTO,
    private val jobworkItems: JobDTO
) : Item() {

    init {
        extras[INSET_TYPE_KEY] = INSET
    }

    override fun getLayout() = R.layout.work_list_item

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.apply {
            Coroutines.main {

                val works =
                    workViewModel.getWorkItemsForActID(ActivityIdConstants.EST_WORKS_COMPLETE)
                works.observe(activity!!, Observer {
//                  activity.toast(works.size)
                    expandable_child_textView.text = text
                    qty_textView.text = qty
                    line_amount_textView.text = rate
                })
            }

            startWork_Btn.setOnClickListener {
                sendJobToWork(workViewModel, itemEsti, it, jobworkItems)
            }
        }
    }

    private fun sendJobToWork(
        workViewModel: WorkViewModel,
        estimate: JobItemEstimateDTO,
        view: View?,
        jobworkItems: JobDTO
    ) {
        val estimate = estimate
        val jobworkItems = jobworkItems
        Coroutines.main {

            workViewModel.workItemJob.value = jobworkItems
            workViewModel.workItem.value = estimate
        }
        Navigation.findNavController(view!!)
            .navigate(R.id.action_nav_work_to_captureWorkFragment)
    }
}
