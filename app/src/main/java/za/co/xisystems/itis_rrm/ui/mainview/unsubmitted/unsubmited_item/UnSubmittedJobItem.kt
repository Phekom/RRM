/*
 * Updated by Shaun McDonald on 2021/02/04
 * Last modified on 2021/02/04 10:39 AM
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 */

package za.co.xisystems.itis_rrm.ui.mainview.unsubmitted.unsubmited_item

import android.annotation.SuppressLint
import android.app.AlertDialog.Builder
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.navigation.Navigation
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.viewbinding.BindableItem
import com.xwray.groupie.viewbinding.GroupieViewHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.custom.errors.XIErrorHandler
import za.co.xisystems.itis_rrm.custom.notifications.ToastDuration.LONG
import za.co.xisystems.itis_rrm.custom.notifications.ToastGravity.BOTTOM
import za.co.xisystems.itis_rrm.custom.notifications.ToastStyle
import za.co.xisystems.itis_rrm.custom.results.XIResult
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.data.network.responses.PhotoPotholeResponse
import za.co.xisystems.itis_rrm.databinding.UnsubmtdJobListItemBinding
import za.co.xisystems.itis_rrm.ui.extensions.extensionToast
import za.co.xisystems.itis_rrm.ui.mainview.activities.jobmain.ui.add_items.AddItemsViewModel
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
    private val createModel: AddItemsViewModel,
    private val groupAdapter: GroupAdapter<GroupieViewHolder<UnsubmtdJobListItemBinding>>,
    private val fragment: UnSubmittedFragment,
) : BindableItem<UnsubmtdJobListItemBinding>() {
    private var clickListener: ((UnSubmittedJobItem) -> Unit)? = null
    var section = ""
    override fun bind(viewBinding: UnsubmtdJobListItemBinding, position: Int) {

        viewBinding.apply {

            iTemID.text = getItemId(position + 1).toString()
            Coroutines.main {
                val descri = viewModel.getDescForProjectId(jobDTO.projectId!!)
                unsubmittedProjectTextView.text = descri

                if (jobDTO.sectionId.isNullOrEmpty()) {
                    val projectSectionId = viewModel.getProjectSectionIdForJobId(jobDTO.jobId)
                    val prjSction = viewModel.getProjectSectionForId(projectSectionId)
                    section = prjSction.route + prjSction.section + prjSction.direction
                    unsubmittedSectionTextView.text = section
                } else {
                    val prjSction = viewModel.getProjectSectionForId(jobDTO.sectionId!!)
                    if (prjSction == null) {
                        unsubmittedSectionTextView.text = ""
                    } else {
                        section = (prjSction.route + prjSction.section + prjSction.direction) ?: ""
                        unsubmittedSectionTextView.text = section
                    }
                }
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
            Coroutines.main {
                if (jobDTO.jobType == "Pothole"){
                    decisionAlertdialog(view, jobDTO)
                }else{
                    sendJobToEdit(jobDTO, view)
                }

            }
        }
    }

    private fun buildUploadDialog(view: View, position: Int) {
        val itemDeleteBuilder =
            Builder(
                view.context // , android.R.style
                // .Theme_DeviceDefault_Dialog
            )
        itemDeleteBuilder.setTitle(R.string.confirm)
        itemDeleteBuilder.setIcon(R.drawable.ic_baseline_file_upload_24)
        itemDeleteBuilder.setMessage("Upload this un-submitted job?")
        // Yes button
        itemDeleteBuilder.setPositiveButton(
            R.string.yes
        ) { _, _ ->
            fragment.extensionToast(
                title = "Uploading ...",
                message = "${this.jobDTO.descr} in transit.",
                style = ToastStyle.INFO,
                position = BOTTOM,
                duration = LONG
            )
            Coroutines.main {
                try {
//                    createModel.setJobForReUpload(jobDTO.jobId)
//                    createModel.jobForReUpload.observeOnce(fragment.viewLifecycleOwner, { event ->
//                        event.getContentIfNotHandled()?.let { incompleteJob ->
//                            Coroutines.main {
//                                fragment.toggleLongRunning(true)
//                                val result = createModel.reUploadJob(incompleteJob, fragment.requireActivity())
//                                handleUploadResult(result, position, jobDTO.jobId)
//                            }
//                        }
//                    })
                } catch (t: Throwable) {
                    Timber.e("Failed to upload un-submitted job: ${t.message ?: XIErrorHandler.UNKNOWN_ERROR}")
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

    private fun sendJobToEdit(jobData: JobDTO, view: View) {
        Coroutines.io {
            withContext(Dispatchers.Main.immediate) {
                val navDirection = UnSubmittedFragmentDirections
                    .actionNavigationUnSubmittedToNavigationAddItems(jobData.projectId!!, jobData.jobId, jobData.contractVoId)
                Navigation.findNavController(fragment.requireView()).navigate(navDirection)
            }
        }


//        Coroutines.main {
//            createModel.setJobToEdit(jobDTO.jobId)
//        }
//        val navDirection =
//            UnSubmittedFragmentDirections.actionNavUnSubmittedToAddProjectFragment(
//                jobDTO.projectId,
//                jobDTO.jobId
//            )
//
//        Navigation.findNavController(view)
//            .navigate(navDirection)
    }

    private fun handleUploadResult(result: XIResult<Boolean>, position: Int, jobId: String) {
        fragment.toggleLongRunning(false)
        when (result) {
            is XIResult.Success -> {
                fragment.extensionToast(
                    message = "Job ${jobDTO.jiNo} uploaded successfully",
                    style = ToastStyle.SUCCESS
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
                    style = ToastStyle.ERROR
                )
//                createModel.resetUploadState()
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
        itemDeleteBuilder.setTitle(R.string.confirm)
        itemDeleteBuilder.setIcon(R.drawable.ic_warning)
        itemDeleteBuilder.setMessage("Delete this unsubmitted job?")
        // Yes button
        itemDeleteBuilder.setPositiveButton(
            R.string.yes
        ) { _, _ ->
            fragment.extensionToast(
                title = "Deleting ...",
                message = "${this.jobDTO.descr} removed.",
                style = ToastStyle.DELETE,
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
            R.string.no
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

    @SuppressLint("SetTextI18n")
    private fun decisionAlertdialog(view: View, jobDTO: JobDTO) {
        val textEntryView = fragment.layoutInflater.inflate(R.layout.unsubmitted_alert_dialog, null)
        val jiNo = textEntryView.findViewById<View>(R.id.ji_numb) as TextView
        val section = textEntryView.findViewById<View>(R.id.section_numb) as TextView
        val decline = textEntryView.findViewById<View>(R.id.decline_job_button) as Button
        val create = textEntryView.findViewById<View>(R.id.create_job_button) as Button
        val latitude = textEntryView.findViewById<View>(R.id.latitudeText) as TextView
        val longitude = textEntryView.findViewById<View>(R.id.longitudeText) as TextView


        val alert: androidx.appcompat.app.AlertDialog = androidx.appcompat.app.AlertDialog.Builder(fragment.requireContext())
            .setView(textEntryView)
            .setIcon(R.drawable.ic_ji_direction)
            .setTitle(R.string.ji_direction_choice)
            .setMessage("Please Note if you decline a job instruction you will be required to give a valid reason")
//            .setNegativeButton("decline", null)
//            .setPositiveButton("create", null)
            .create()

        alert.setOnShowListener { dialog ->
           Coroutines.ui{
              // val photoData = viewModel.getPotholePhoto(jobDTO.jobId)
               jiNo.text = "JI: ${jobDTO.jiNo}"
               section.text = jobDTO.pHRoute ?: ""
               latitude.text = jobDTO.pHLatitude.toString()?:""
               longitude.text = jobDTO.pHLongitude.toString()?:""
               decline.setOnClickListener {
                   sendJobToDecline(jobDTO, view)
                   dialog.dismiss()
               }
               create.setOnClickListener {
                   sendJobToEdit(jobDTO, view)
                   dialog.dismiss()
               }
           }
        }

        alert.show()
    }

    private fun sendJobToDecline(jobDTO: JobDTO, view: View) {
        Coroutines.io {
            withContext(Dispatchers.Main.immediate) {
                val navDirection = UnSubmittedFragmentDirections
                    .actionNavigationUnSubmittedToDeclineJobFragment(jobDTO.jobId)
                Navigation.findNavController(view).navigate(navDirection)
            }
        }
    }


}
