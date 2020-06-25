package za.co.xisystems.itis_rrm.ui.mainview.create.add_project_item

import android.view.View
import androidx.navigation.Navigation
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.new_job_item.*
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.data.localDB.entities.ItemDTOTemp
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimateDTO
import za.co.xisystems.itis_rrm.ui.mainview.work.INSET
import za.co.xisystems.itis_rrm.ui.mainview.work.INSET_TYPE_KEY
import za.co.xisystems.itis_rrm.ui.models.CreateViewModel
import za.co.xisystems.itis_rrm.utils.Coroutines
import za.co.xisystems.itis_rrm.utils.JobUtils

/**
 * Created by Francis Mahlava on 2019/12/29.
 */

// open class Project_Item(private val itemDesc: SectionProj_Item) : Item() {
open class ProjectItem(
    private val itemDesc: ItemDTOTemp,
    private val createViewModel: CreateViewModel,
    private val contractID: String?,
    private var job: JobDTO?
) : Item() {

    init {
        extras[INSET_TYPE_KEY] = INSET
    }

    var clickListener: ((ProjectItem) -> Unit)? = null
    override fun getLayout() = R.layout.new_job_item

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
                viewHolder.apply {
                    textViewItem.text = (itemDesc.itemCode + "  " + itemDesc.descr)
                    val jobItemEstimate: JobItemEstimateDTO? = getJobItemEstimate(itemDesc.itemId)
                    if (jobItemEstimate != null && jobItemEstimate.isEstimateComplete()) {
                        val lineRate: Double = jobItemEstimate.lineRate
                        val tenderRate: Double = itemDesc.tenderRate
                        val qty: Double = jobItemEstimate.qty
                        costTextView.text =
                            ("$qty  *   R $tenderRate = " + JobUtils.formatCost(lineRate))
                        subTextView.visibility = View.GONE
                    } else {
                        costTextView.text = "Incomplete Estimate..."
                        subTextView.visibility = View.VISIBLE
                    }

                    bindItem(viewHolder)
        }
        viewHolder.itemView.setOnClickListener {
            sendSelectedItem(itemDesc, it, contractID, job)
            clickListener?.invoke(this)
        }

        viewHolder.itemView.setOnLongClickListener {
            Coroutines.main {
                createViewModel.deleteItemfromList(itemDesc.itemId)
            }
           it.isLongClickable
        }
    }

    private fun sendSelectedItem(
        item: ItemDTOTemp,
        view: View,
        contractID: String?,
        job: JobDTO?
    ) {
        val contractId = contractID
        val newJob = job
        Coroutines.main {
            createViewModel.jobItem.value = newJob
            createViewModel.contractId.value = contractId
            createViewModel.projectItemTemp.value = item
        }

        Navigation.findNavController(view)
            .navigate(R.id.action_addProjectFragment_to_estimatePhotoFragment)
    }

    private fun bindItem(viewHolder: GroupieViewHolder) {
       viewHolder.textViewItem.setOnClickListener {
           sendSelectedItem(itemDesc, it, contractID, job)
       }
    }

    private fun getJobItemEstimate(itemId: String): JobItemEstimateDTO? {
        return job?.getJobEstimateByItemId(itemId)
    }
}
