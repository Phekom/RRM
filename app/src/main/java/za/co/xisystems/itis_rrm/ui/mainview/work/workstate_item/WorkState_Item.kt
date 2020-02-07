package za.co.xisystems.itis_rrm.ui.mainview.work.workstate_item

import android.graphics.Color
import androidx.fragment.app.FragmentActivity
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.list_selector.*
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.data.localDB.entities.JobEstimateWorksDTO
import za.co.xisystems.itis_rrm.ui.mainview.work.WorkViewModel
import za.co.xisystems.itis_rrm.utils.toast

/**
 * Created by Francis Mahlava on 2019/12/22.
 */


class WorkState_Item(
    val jobItemWorks: JobEstimateWorksDTO,
    private val approveViewModel: WorkViewModel,
    private var activity: FragmentActivity?,
    private var groupAdapter: GroupAdapter<GroupieViewHolder>
) : Item() {
    private var selection = 0
    //private var lastSelectedRow: GroupieViewHolder? = null

    companion object {
        var selected_position = -1
    }

    var clickListener: ((WorkState_Item) -> Unit)? = null
//    val  mWorkFlowRoute : ArrayList<Long>?

    val workState = arrayOf("TA", "START", "MIDDLE", "END", "RTA")


    override fun bind(viewHolder: GroupieViewHolder, position: Int) {

       viewHolder.apply {
           state.text = workState[position]
           setNewState(position, viewHolder)
           if (selected_position ==  adapterPosition){
               viewHolder.stateBack.setBackgroundColor(activity!!.resources.getColor(R.color.sanral_burnt_orange))
               viewHolder.state.setTextColor(Color.WHITE)
           } else {
               viewHolder.stateBack.setBackgroundColor(Color.TRANSPARENT)
               viewHolder.state.setTextColor(Color.BLACK)
           }
           viewHolder.stateBack.setOnClickListener {
               activity?.toast(position.toString())
               selected_position = position
                groupAdapter.notifyDataSetChanged()

           }

//           Coroutines.main {
//               val sectionId  =  approveViewModel?.getProjectSectionIdForJobId(jobItemMeasureDTO.jobId!!)
//               val route  =  approveViewModel?.getRouteForProjectSectionId(sectionId!!)
//               val section  =  approveViewModel?.getSectionForProjectSectionId(sectionId!!)
//               apv_section.text =  "( ${route} ${"/0$section"} )"
//               val description  =  approveViewModel?.getItemDesc(jobItemMeasureDTO.jobId!!)
//               apv_description.text = description //list_selector
//           }
       }
        viewHolder.itemView.setOnClickListener {
            clickListener?.invoke(this)
        }
    }

    private fun setNewState(
        position: Int,
        viewHolder: GroupieViewHolder
    ) {
        if (jobItemWorks.actId == 15){
            selected_position = 0
        }else if (jobItemWorks.actId == 16){
            selected_position = 1
        }else  if (jobItemWorks.actId == 17){
            selected_position = 2
        }else if (jobItemWorks.actId == 18){
            selected_position = 3
        }else if (jobItemWorks.actId == 19){
            selected_position = 4
        }
        if (selected_position ==  position){
            viewHolder.stateBack.setBackgroundColor(activity!!.resources.getColor(R.color.sanral_burnt_orange))
            viewHolder.state.setTextColor(Color.WHITE)
        } else {
            viewHolder.stateBack.setBackgroundColor(Color.TRANSPARENT)
            viewHolder.state.setTextColor(Color.BLACK)
        }
    }



    override fun getLayout() = R.layout.list_selector

}



private fun GroupieViewHolder.getCount(position: Int): Long {
    return position.toLong()
}


private fun GroupieViewHolder.getItemId(position: Int): Long {
    return position.toLong()
}
