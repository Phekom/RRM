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
import za.co.xisystems.itis_rrm.utils.toast

open class CardItem(
    val activity: FragmentActivity?,
    val text: String,
    val qty: String,
    val rate: String,
    val estimateId: String,
    val workViewModel: WorkViewModel,
    val itemEsti: JobItemEstimateDTO,
    val jobworkItems: JobDTO
) : Item() {

    init {
        extras[INSET_TYPE_KEY] = INSET
    }

    override fun getLayout() = R.layout.work_list_item

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.apply {
            Coroutines.main {

                val workss =  workViewModel.getWorkItemsForActID(ActivityIdConstants.EST_WORKS_COMPLETE)
                workss.observe(activity!!, Observer {
//                    activity.toast(workss.size)
                    expandable_child_textView.text = text
                    qty_textView.text = qty
                    line_amount_textView.text = rate
                })

            }

            startWork_Btn.setOnClickListener(View.OnClickListener {
                sendJobtoWork( workViewModel ,itemEsti, it,jobworkItems)
            })
        }

    }
    private fun sendJobtoWork(
        workViewModel: WorkViewModel,
        estimate: JobItemEstimateDTO,
        view: View?,
        jobworkItems: JobDTO
    ) {
        val estimate = estimate
        val jobworkItems = jobworkItems
        Coroutines.main {

            workViewModel.work_ItemJob.value = jobworkItems
            workViewModel.work_Item.value = estimate
        }
        Navigation.findNavController(view!!)
            .navigate(R.id.action_nav_work_to_captureWorkFragment)
    }

}
