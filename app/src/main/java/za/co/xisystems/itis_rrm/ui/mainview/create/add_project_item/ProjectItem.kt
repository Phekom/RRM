/*
 * Updated by Shaun McDonald on 2021/02/04
 * Last modified on 2021/02/04 12:13 PM
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 */

package za.co.xisystems.itis_rrm.ui.mainview.create.add_project_item

import android.app.AlertDialog.Builder
import android.view.View
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import com.xwray.groupie.viewbinding.BindableItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.custom.notifications.ToastDuration.LONG
import za.co.xisystems.itis_rrm.custom.notifications.ToastGravity.BOTTOM
import za.co.xisystems.itis_rrm.custom.notifications.ToastStyle.DELETE
import za.co.xisystems.itis_rrm.data.localDB.entities.ItemDTOTemp
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimateDTO
import za.co.xisystems.itis_rrm.databinding.NewJobItemBinding
import za.co.xisystems.itis_rrm.ui.extensions.extensionToast
import za.co.xisystems.itis_rrm.ui.mainview.create.CreateViewModel
import za.co.xisystems.itis_rrm.ui.mainview.work.INSET
import za.co.xisystems.itis_rrm.ui.mainview.work.INSET_TYPE_KEY
import za.co.xisystems.itis_rrm.utils.Coroutines
import za.co.xisystems.itis_rrm.utils.JobUtils

/**
 * Created by Francis Mahlava on 2019/12/29.
 */

open class ProjectItem(
    private val fragment: AddProjectFragment,
    private val itemDesc: ItemDTOTemp,
    private val createViewModel: CreateViewModel,
    private val contractID: String?,
    private var job: JobDTO?
) : BindableItem<NewJobItemBinding>() {

    init {
        extras[INSET_TYPE_KEY] = INSET
    }

    var clickListener: ((ProjectItem) -> Unit)? = null
    override fun getLayout() = R.layout.new_job_item

    override fun bind(viewBinding: NewJobItemBinding, position: Int) {
        viewBinding.apply {
            Coroutines.main {
                textViewItem.text = (itemDesc.itemCode + "  " + itemDesc.descr)
                val jobItemEstimate: JobItemEstimateDTO? = getJobItemEstimate(itemDesc.itemId)
                if (jobItemEstimate != null && createViewModel.estimateComplete(jobItemEstimate)) {
                    val lineRate: Double = jobItemEstimate.lineRate
                    val tenderRate: Double = itemDesc.tenderRate
                    val qty: Double = jobItemEstimate.qty
                    costTextView.text =
                        ("$qty  *   R $lineRate = ${JobUtils.formatCost(qty * tenderRate)}")
                    subTextView.visibility = View.GONE
                } else {
                    costTextView.text = viewBinding.root.context.getString(R.string.incomplete_estimate)
                    subTextView.visibility = View.VISIBLE
                }

                val located = ContextCompat.getDrawable(root.context, R.drawable.ic_baseline_location_on_24)
                val notLocated = ContextCompat.getDrawable(root.context, R.drawable.ic_baseline_location_off_24)
                located?.setTint(fragment.requireContext().resources.getColor(R.color.sanral_dark_green, null))
                if (jobItemEstimate?.geoCoded == true) {
                    textViewItem.setCompoundDrawablesWithIntrinsicBounds(located, null, null, null)
                } else {
                    textViewItem.setCompoundDrawablesWithIntrinsicBounds(notLocated, null, null, null)
                }

                bindItem(viewBinding)
            }
        }
        viewBinding.root.setOnClickListener {
            sendSelectedItem(itemDesc, it, contractID, job)
            clickListener?.invoke(this)
        }

        viewBinding.root.setOnLongClickListener {
            Coroutines.main {
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
        Coroutines.io {
            createViewModel.backupJob(newJob!!)
            createViewModel.backupProjectItem(item)
            withContext(Dispatchers.Main) {
                createViewModel.setJobToEdit(newJob.jobId)
                createViewModel.contractId.value = contractId
                createViewModel.tempProjectItem.value = item
                createViewModel.setCurrentProjectItem(item.itemId)
            }
        }
        val navDirections =
            AddProjectFragmentDirections
                .actionAddProjectFragmentToEstimatePhotoFragment(
                    jobId = newJob?.jobId,
                    itemId = item.itemId,
                    estimateId = getJobItemEstimate(itemId = item.itemId)?.estimateId,
                    locationErrorMessage = null
                )

        Navigation.findNavController(view)
            .navigate(navDirections)
    }

    private fun bindItem(viewBinding: NewJobItemBinding) {
        viewBinding.textViewItem.setOnClickListener {
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
        itemDeleteBuilder.setTitle(R.string.confirm)
        itemDeleteBuilder.setIcon(R.drawable.ic_warning)
        itemDeleteBuilder.setMessage("Delete this project item?")
        // Yes button
        itemDeleteBuilder.setPositiveButton(
            R.string.yes
        ) { _, _ ->
            fragment.extensionToast(
                title = "Deleting ...",
                message = "${this.itemDesc.descr} removed.",
                style = DELETE,
                position = BOTTOM,
                duration = LONG
            )

            Coroutines.main {
                // Get the JobEstimate
                val jobItemEstimate = getJobItemEstimate(itemDesc.itemId)

                // Delete the project item.
                createViewModel.deleteItemFromList(itemDesc.itemId, estimateId = jobItemEstimate?.estimateId)

                // Set updated job and recalculate costs if applicable
                job?.let {
                    it.removeJobEstimateByItemId(itemDesc.itemId)
                    createViewModel.backupJob(it)
                    createViewModel.setJobToEdit(itemDesc.jobId)
                    fragment.uiUpdate()
                }
            }
        }
        // No button
        itemDeleteBuilder.setNegativeButton(
            R.string.no
        ) { dialog, _ ->
            // Do nothing but close dialog
            dialog.dismiss()
        }
        val deleteAlert = itemDeleteBuilder.create()
        deleteAlert.show()
    }

    override fun initializeViewBinding(view: View): NewJobItemBinding {
        return NewJobItemBinding.bind(view)
    }
}
