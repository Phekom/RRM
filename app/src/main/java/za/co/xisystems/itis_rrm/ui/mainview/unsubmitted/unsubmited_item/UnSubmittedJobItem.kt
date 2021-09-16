/*
 * Updated by Shaun McDonald on 2021/02/04
 * Last modified on 2021/02/04 10:39 AM
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 */

package za.co.xisystems.itis_rrm.ui.mainview.unsubmitted.unsubmited_item

import android.app.AlertDialog.Builder
import android.view.View
import androidx.navigation.Navigation
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.viewbinding.BindableItem
import com.xwray.groupie.viewbinding.GroupieViewHolder
import timber.log.Timber
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.R.drawable
import za.co.xisystems.itis_rrm.R.string
import za.co.xisystems.itis_rrm.custom.errors.XIErrorHandler
import za.co.xisystems.itis_rrm.custom.notifications.ToastDuration.LONG
import za.co.xisystems.itis_rrm.custom.notifications.ToastGravity.BOTTOM
import za.co.xisystems.itis_rrm.custom.notifications.ToastStyle.DELETE
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.databinding.UnsubmtdJobListItemBinding
import za.co.xisystems.itis_rrm.ui.extensions.extensionToast
import za.co.xisystems.itis_rrm.ui.mainview.create.CreateViewModel
import za.co.xisystems.itis_rrm.ui.mainview.unsubmitted.UnSubmittedFragment
import za.co.xisystems.itis_rrm.ui.mainview.unsubmitted.UnSubmittedFragmentDirections
import za.co.xisystems.itis_rrm.ui.mainview.unsubmitted.UnSubmittedViewModel
import za.co.xisystems.itis_rrm.utils.Coroutines

/**
 * Created by Francis Mahlava on 2019/12/22.
 */

class UnSubmittedJobItem(
    private val jobDTO: JobDTO,
    private val viewModel: UnSubmittedViewModel,
    private val createModel: CreateViewModel,
    private val groupAdapter: GroupAdapter<GroupieViewHolder<UnsubmtdJobListItemBinding>>,
    private val fragment: UnSubmittedFragment
) : BindableItem<UnsubmtdJobListItemBinding>() {

    private var clickListener: ((UnSubmittedJobItem) -> Unit)? = null

    override fun bind(viewBinding: UnsubmtdJobListItemBinding, position: Int) {

        viewBinding.apply {

            iTemID.text = getItemId(position + 1).toString()
            Coroutines.main {
                val descri = viewModel.getDescForProjectId(jobDTO.projectId!!)
                unsubmittedProjectTextView.text = descri
            }
            unsubmittedDescriptionTextView.text = jobDTO.descr

            deleteButton.setOnClickListener {
                buildDeleteDialog(it, position)
            }

            updateItem(position)
        }

        viewBinding.root.setOnClickListener { view ->
            clickListener?.invoke(this)
            sendJobToEdit((jobDTO), view)
        }
    }

    private fun sendJobToEdit(jobDTO: JobDTO, view: View) {

        Coroutines.main {
            createModel.setJobToEdit(jobDTO.jobId)
        }
        val navDirection =
            UnSubmittedFragmentDirections.actionNavUnSubmittedToAddProjectFragment(
                jobDTO.projectId,
                jobDTO.jobId
            )

        Navigation.findNavController(view)
            .navigate(navDirection)
    }

    override fun getLayout() = R.layout.unsubmtd_job_list_item

    private fun updateItem(position: Int) {
        // Not interested in this
        groupAdapter.notifyItemChanged(position)
    }

    private fun buildDeleteDialog(view: View, position: Int) {
        val itemDeleteBuilder =
            Builder(
                view.context // , android.R.style
                // .Theme_DeviceDefault_Dialog
            )
        itemDeleteBuilder.setTitle(string.confirm)
        itemDeleteBuilder.setIcon(drawable.ic_warning)
        itemDeleteBuilder.setMessage("Delete this unsubmitted job?")
        // Yes button
        itemDeleteBuilder.setPositiveButton(
            string.yes
        ) { _, _ ->
            fragment.extensionToast(
                title = "Deleting ...",
                message = "${this.jobDTO.descr} removed.",
                style = DELETE,
                position = BOTTOM,
                duration = LONG
            )
            Coroutines.main {
                try {
                    viewModel.deleJobfromList(jobDTO.jobId)
                    viewModel.deleteItemList(jobDTO.jobId)
                    groupAdapter.notifyItemRemoved(position)
                    notifyChanged()
                } catch (t: Throwable) {
                    Timber.e("Failed to delete unsubmitted job: ${t.message ?: XIErrorHandler.UNKNOWN_ERROR}")
                }
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

    override fun initializeViewBinding(view: View): UnsubmtdJobListItemBinding {
        return UnsubmtdJobListItemBinding.bind(view)
    }

    private fun getItemId(position: Int): Long {
        return position.toLong()
    }
}


