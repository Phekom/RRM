package za.co.xisystems.itis_rrm.ui.mainview.create.add_project_item

import android.app.AlertDialog.Builder
import android.view.View
import androidx.navigation.Navigation
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.new_job_item.*
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.R.drawable
import za.co.xisystems.itis_rrm.R.string
import za.co.xisystems.itis_rrm.data.localDB.entities.ItemDTOTemp
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimateDTO
import za.co.xisystems.itis_rrm.ui.mainview.create.CreateViewModel
import za.co.xisystems.itis_rrm.ui.mainview.work.INSET
import za.co.xisystems.itis_rrm.ui.mainview.work.INSET_TYPE_KEY
import za.co.xisystems.itis_rrm.utils.Coroutines
import za.co.xisystems.itis_rrm.utils.JobUtils
import za.co.xisystems.itis_rrm.utils.enums.ToastDuration.LONG
import za.co.xisystems.itis_rrm.utils.enums.ToastGravity.CENTER
import za.co.xisystems.itis_rrm.utils.enums.ToastStyle.DELETE

/**
 * Created by Francis Mahlava on 2019/12/29.
 */

// open class Project_Item(private val itemDesc: SectionProj_Item) : Item() {
open class ProjectItem(
    private val fragment: AddProjectFragment,
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
                // TODO: Build Delete Confirmation Dialog
                buildDeleteDialog(it)
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

    private fun buildDeleteDialog(view: View) {
        val itemDeleteBuilder =
            Builder(
                view.context // , android.R.style
                // .Theme_DeviceDefault_Dialog
            )
        itemDeleteBuilder.setTitle(string.confirm)
        itemDeleteBuilder.setIcon(drawable.ic_warning)
        itemDeleteBuilder.setMessage("Delete this project item?")
        // Yes button
        itemDeleteBuilder.setPositiveButton(
            string.yes
        ) { _, _ ->
            fragment.sharpToast("Deleting ...", "${this.itemDesc} removed.", DELETE, CENTER, LONG)
            Coroutines.main {
                // Delete the line item
                createViewModel.deleteItemFromList(itemDesc.itemId, itemDesc.jobId)

                // Set updated job and recalculate costs if applicable
                createViewModel.setJobToEdit(itemDesc.jobId)
                fragment.calculateTotalCost()
            }
        }
        // No button
        itemDeleteBuilder.setNegativeButton(
            string.no
        ) { dialog, _ ->
            // Do nothing but close dialog
            dialog.dismiss()
        }
        val deleteAlert = itemDeleteBuilder.create()
        deleteAlert.show()
    }
}
