/*
 * Updated by Shaun McDonald on 2021/02/04
 * Last modified on 2021/02/04 10:39 AM
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 */

package za.co.xisystems.itis_rrm.ui.mainview.unsubmitted.unsubmited_item

import android.view.View
import androidx.navigation.Navigation
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.unsubmtd_job_list_item.*
import timber.log.Timber
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.custom.errors.XIErrorHandler
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.ui.mainview.create.CreateViewModel
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
    private val groupAdapter: GroupAdapter<GroupieViewHolder>
) : Item() {

    private var clickListener: ((UnSubmittedJobItem) -> Unit)? = null

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {

        viewHolder.apply {

            iTemID.text = getItemId(position + 1).toString()
            unsubmitted_project_textView.text = itemView
                .context.getString(R.string.pair, "JI:", jobDTO.jiNo)
            Coroutines.main {
                val descri = viewModel.getDescForProjectId(jobDTO.projectId!!)
                unsubmitted_project_textView.text = descri
            }
            unsubmitted_description_textView.text = jobDTO.descr

            deleteButton.setOnClickListener {
                Coroutines.main {
                    try {
                        viewModel.deleJobfromList(jobDTO.jobId)
                        viewModel.deleteItemList(jobDTO.jobId)
                        groupAdapter.clear()
                        groupAdapter.notifyDataSetChanged()
                        notifyChanged()
                    } catch (t: Throwable) {
                        Timber.e("Failed to delete unsubmitted job: ${t.message ?: XIErrorHandler.UNKNOWN_ERROR}")
                    }
                }
            }

            updateItem()
        }

        viewHolder.itemView.setOnClickListener { view ->
            clickListener?.invoke(this)
            sendJobToEdit((jobDTO), view)
        }
    }

    private fun sendJobToEdit(jobDTO: JobDTO, view: View) {

        val job = jobDTO
        Coroutines.main {
            createModel.setJobToEdit(job.jobId)
        }
        val navDirection =
            UnSubmittedFragmentDirections.actionNavUnSubmittedToAddProjectFragment(job.projectId, job.jobId)

        Navigation.findNavController(view)
            .navigate(navDirection)
    }

    override fun getLayout() = R.layout.unsubmtd_job_list_item

    private fun updateItem() {
        // Not interested in this
    }
}

private fun getItemId(position: Int): Long {
    return position.toLong()
}
