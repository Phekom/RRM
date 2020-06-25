package za.co.xisystems.itis_rrm.ui.mainview.unsubmitted.unsubmited_item

import android.view.View
import androidx.navigation.Navigation
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.unsubmtd_job_list_item.*
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.ui.mainview.unsubmitted.UnSubmittedViewModel
import za.co.xisystems.itis_rrm.utils.Coroutines

/**
 * Created by Francis Mahlava on 2019/12/22.
 */

class UnSubmittedJobItem(
    val jobDTO: JobDTO,
    private val viewModel: UnSubmittedViewModel,
    private val groupAdapter: GroupAdapter<GroupieViewHolder>
) : Item() {

    var clickListener: ((UnSubmittedJobItem) -> Unit)? = null

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {

        viewHolder.apply {
            iTemID.text = getItemId(position + 1).toString()
            unsubmitted_project_textView.text = "JI:${jobDTO.JiNo} - "
            Coroutines.main {
                val descri = viewModel.getDescForProjectId(jobDTO.ProjectId!!)
               unsubmitted_project_textView.text = descri
           }
           unsubmitted_description_textView.text = jobDTO.Descr

           deleteButton.setOnClickListener {
               Coroutines.main {
                   viewModel.deleJobfromList(jobDTO.JobId)
                   viewModel.deleteItemList(jobDTO.JobId)
                   groupAdapter.clear()
                   groupAdapter.notifyDataSetChanged()
                   notifyChanged()
               }
           }

           updateItem()
       }

        viewHolder.itemView.setOnClickListener { view ->
            clickListener?.invoke(this)
            sendJobtoEdit((jobDTO), view)
        }
    }

    private fun sendJobtoEdit(jobDTO: JobDTO, view: View) {

        val job = jobDTO
        Coroutines.main {
            viewModel.getJobToEdit(job.JobId)
        }

        Navigation.findNavController(view)
            .navigate(R.id.action_nav_unSubmitted_to_addProjectFragment)
    }

    override fun getLayout() = R.layout.unsubmtd_job_list_item

    private fun GroupieViewHolder.updateItem() {
    }

    private fun GroupieViewHolder.updatePojectItem() {
    }
}

private fun GroupieViewHolder.getItemId(position: Int): Long {
    return position.toLong()
}
