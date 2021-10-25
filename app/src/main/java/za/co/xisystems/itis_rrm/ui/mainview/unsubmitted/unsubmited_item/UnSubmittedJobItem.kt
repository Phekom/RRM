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
import www.sanju.motiontoast.MotionToastStyle
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.R.drawable
import za.co.xisystems.itis_rrm.R.string
import za.co.xisystems.itis_rrm.custom.errors.XIErrorHandler
import za.co.xisystems.itis_rrm.custom.notifications.ToastDuration.LONG
import za.co.xisystems.itis_rrm.custom.notifications.ToastGravity.BOTTOM
import za.co.xisystems.itis_rrm.custom.results.XIResult
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.databinding.UnsubmtdJobListItemBinding
import za.co.xisystems.itis_rrm.extensions.observeOnce
import za.co.xisystems.itis_rrm.ui.extensions.extensionToast
import za.co.xisystems.itis_rrm.ui.mainview.create.CreateViewModel
import za.co.xisystems.itis_rrm.ui.mainview.unsubmitted.UnSubmittedFragment
import za.co.xisystems.itis_rrm.ui.mainview.unsubmitted.UnSubmittedFragmentDirections
import za.co.xisystems.itis_rrm.ui.mainview.unsubmitted.UnSubmittedViewModel
import za.co.xisystems.itis_rrm.utils.ActivityIdConstants
import za.co.xisystems.itis_rrm.utils.Coroutines

/**
 * Created by Francis Mahlava on 2019/12/22.
 */

class UnSubmittedJobItem(
    private val jobDTO: JobDTO,
    private val viewModel: UnSubmittedViewModel,
    private val createModel: CreateViewModel,
    private val groupAdapter: GroupAdapter<GroupieViewHolder<UnsubmtdJobListItemBinding>>,
    private val fragment: UnSubmittedFragment,
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

            uploadButton.setOnClickListener {
                buildUploadDialog(it, position)
            }

            when (jobDTO.actId) {
                ActivityIdConstants.JOB_PENDING_UPLOAD -> {
                    uploadButton.visibility = View.VISIBLE
                    deleteButton.visibility = View.GONE
                }
                else -> {
                    uploadButton.visibility = View.GONE
                    deleteButton.visibility = View.VISIBLE
                }
            }

            updateItem(position)
        }

        viewBinding.root.setOnClickListener { view ->
            clickListener?.invoke(this)
            sendJobToEdit(jobDTO, view)
        }
    }

    private fun buildUploadDialog(view: View, position: Int) {
        val itemDeleteBuilder =
            Builder(
                view.context // , android.R.style
                // .Theme_DeviceDefault_Dialog
            )
        itemDeleteBuilder.setTitle(string.confirm)
        itemDeleteBuilder.setIcon(drawable.ic_baseline_file_upload_24)
        itemDeleteBuilder.setMessage("Upload this unsubmitted job?")
        // Yes button
        itemDeleteBuilder.setPositiveButton(
            string.yes
        ) { _, _ ->
            fragment.extensionToast(
                title = "Uploading ...",
                message = "${this.jobDTO.descr} in transit.",
                style = MotionToastStyle.INFO,
                position = BOTTOM,
                duration = LONG
            )
            Coroutines.main {
                try {
                    createModel.setJobForReUpload(jobDTO.jobId)
                    createModel.jobForReUpload.observeOnce(fragment.viewLifecycleOwner, { event ->
                        event.getContentIfNotHandled()?.let { incompleteJob ->
                            Coroutines.main {
                                fragment.toggleLongRunning(true)
                                val result = createModel.reUploadJob(incompleteJob, fragment.requireActivity())
                                handleUploadResult(result, position, jobDTO.jobId)
                            }
                        }
                    })
                } catch (t: Throwable) {
                    Timber.e("Failed to upload unsubmitted job: ${t.message ?: XIErrorHandler.UNKNOWN_ERROR}")
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

    private fun sendForReview(jobDTO: JobDTO, view: View?) {
        fragment.extensionToast(message = "Not yet implemented")
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

    private fun handleUploadResult(result: XIResult<Boolean>, position: Int, jobId: String) {
        fragment.toggleLongRunning(false)
        when (result) {
            is XIResult.Success -> {
                fragment.extensionToast(
                    message = "Job ${jobDTO.jiNo} uploaded successfully",
                    style = MotionToastStyle.SUCCESS
                )
                viewModel.deleteJobFromList(jobId)
                viewModel.deleteItemList(jobId)
                groupAdapter.notifyItemRemoved(position)
                notifyChanged()
            }
            is XIResult.Error -> {
                fragment.extensionToast(
                    title = "Upload failed.",
                    message = "${result.message} - hit upload arrow to retry.",
                    style = MotionToastStyle.ERROR
                )
                createModel.resetUploadState()
            }
            else -> {
                Timber.e("$result")
            }
        }
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
                style = MotionToastStyle.DELETE,
                position = BOTTOM,
                duration = LONG
            )
            Coroutines.main {
                try {
                    viewModel.deleteJobFromList(jobDTO.jobId)
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
