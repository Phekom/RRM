/*
 * Updated by Shaun McDonald on 2021/02/04
 * Last modified on 2021/02/04 12:13 PM
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 */

package za.co.xisystems.itis_rrm.ui.mainview.activities.jobmain.ui.add_items

import android.app.AlertDialog.Builder
import android.view.View
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import com.xwray.groupie.viewbinding.BindableItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.custom.events.XIEvent
import za.co.xisystems.itis_rrm.custom.notifications.ToastDuration.LONG
import za.co.xisystems.itis_rrm.custom.notifications.ToastGravity.BOTTOM
import za.co.xisystems.itis_rrm.custom.notifications.ToastStyle
import za.co.xisystems.itis_rrm.data.localDB.entities.ItemDTOTemp
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimateDTO
import za.co.xisystems.itis_rrm.databinding.NewJobItemBinding
import za.co.xisystems.itis_rrm.ui.extensions.extensionToast
import za.co.xisystems.itis_rrm.ui.mainview.work.INSET
import za.co.xisystems.itis_rrm.ui.mainview.work.INSET_TYPE_KEY
import za.co.xisystems.itis_rrm.utils.Coroutines
import za.co.xisystems.itis_rrm.utils.JobUtils

/**
 * Created by Francis Mahlava on 2019/12/29.
 */

open class ProjectItem(
    private val fragment: AddProjectItemsFragment,
    private val tempItem : ItemDTOTemp,
    private val addViewModel: AddItemsViewModel,
    private val contractID: String?,
    private var job: JobDTO?
) : BindableItem<NewJobItemBinding>() {

    init {
        extras[INSET_TYPE_KEY] = INSET
    }

    private var clickListener: ((ProjectItem) -> Unit)? = null
    override fun getLayout() = R.layout.new_job_item
    private var theJobItemEstimate: JobItemEstimateDTO? = null
    override fun bind(viewBinding: NewJobItemBinding, position: Int) {
        viewBinding.apply {
            textViewItem.text = (tempItem.itemCode + "  " + tempItem.descr)
            Coroutines.main {
                getJobItemEstimate(tempItem.itemId, job!!.jobId).also { jobItemEstimate ->
                    processEstimate(jobItemEstimate, viewBinding)
                }
                root.setOnClickListener {
                    Coroutines.main {
                    addViewModel.backupJob(job!!)
                    addViewModel.backupProjectItem(tempItem)
                    addViewModel.tempProjectItem.value = XIEvent(tempItem)

                    sendSelectedItem(tempItem, contractID, job, theJobItemEstimate, it)
                    clickListener?.invoke(this@ProjectItem)
                    }
                }

                textViewItem.setOnClickListener {
                    Coroutines.main {
                        addViewModel.backupJob(job!!)
                        addViewModel.backupProjectItem(tempItem)
                        addViewModel.tempProjectItem.value = XIEvent(tempItem)

                        sendSelectedItem(tempItem, contractID, job, theJobItemEstimate, it)
                        clickListener?.invoke(this@ProjectItem)
                    }
                }

                root.setOnLongClickListener {
                    Coroutines.main {
                        buildDeleteDialog(it)
                    }
                    it.isLongClickable
                }
            }
        }
    }

    private suspend fun NewJobItemBinding.processEstimate(
        jobItemEstimate: JobItemEstimateDTO?,
        viewBinding: NewJobItemBinding
    ) {
        if (jobItemEstimate != null) {
            addViewModel.estimateComplete(jobItemEstimate).also { complete ->
                if (complete) {
                    val lineRate: Double = jobItemEstimate.lineRate
                    val tenderRate: Double = tempItem.tenderRate
                    val qty: Double = jobItemEstimate.qty
                    costTextView.text =
                        ("$qty  *   R $lineRate = ${JobUtils.formatCost(qty * tenderRate)}")
                    subTextView.visibility = View.GONE
                } else {
                    costTextView.text = viewBinding.root.context.getString(R.string.incomplete_estimate)
                    subTextView.visibility = View.VISIBLE
                }

                val notLocated = ContextCompat.getDrawable(
                    root.context,
                    R.drawable.ic_baseline_wrong_location_24
                )
                if (!jobItemEstimate.geoCoded && jobItemEstimate.size() == 2) {
                    textViewItem.setCompoundDrawablesWithIntrinsicBounds(
                        notLocated, null, null, null
                    )
                } else {
                    textViewItem.setCompoundDrawablesWithIntrinsicBounds(
                        null, null, null, null
                    )
                }
            }
            theJobItemEstimate = jobItemEstimate
//            bindItem(viewBinding)
        }
    }

    private fun sendSelectedItem(
        item: ItemDTOTemp,
        contractID: String?,
        newJob: JobDTO?,
        jobItemEstimate: JobItemEstimateDTO?,
        view: View
    ) {
        Coroutines.io {
            withContext(Dispatchers.Main.immediate) {
                val navDirections = AddProjectItemsFragmentDirections
                        .actionNavigationAddItemsToEstimatePhotoFragment(newJob?.jobId, item.itemId, jobItemEstimate?.estimateId,newJob?.contractVoId,null)
                Navigation.findNavController(view)
                    .navigate(navDirections)
            }
        }


    }

    private suspend fun getJobItemEstimate(itemId: String, jobId: String):
        JobItemEstimateDTO? = withContext(Dispatchers.Main) {
        val updatedEstimate = addViewModel.getJobEstimateIndexByItemAndJobId(itemId, jobId)
        return@withContext if (updatedEstimate != null) {
            job?.insertOrUpdateJobItemEstimate(updatedEstimate)
            job?.getJobEstimateByItemId(itemId)
        } else {
            null
        }
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
                message = "${this.tempItem.descr} removed.",
                style = ToastStyle.DELETE,
                position = BOTTOM,
                duration = LONG
            )

            Coroutines.main {
                // Get the JobEstimate
                val jobItemEstimate = getJobItemEstimate(tempItem.itemId, job!!.jobId)

                // Delete the project item.
                addViewModel.deleteItemFromList(tempItem.itemId, estimateId = jobItemEstimate?.estimateId)
//                addViewModel.deleteItemTempFromList()
                // Set updated job and recalculate costs if applicable
                job?.let {
                    it.removeJobEstimateByItemId(tempItem.itemId)
                    addViewModel.backupJob(it)
                    addViewModel.setJobToEdit(tempItem.jobId)
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
