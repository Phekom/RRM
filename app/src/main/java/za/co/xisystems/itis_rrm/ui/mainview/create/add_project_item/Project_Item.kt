package za.co.xisystems.itis_rrm.ui.mainview.create.add_project_item

import android.view.View
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.new_job_item.*
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimateDTO
import za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils.MyState
import za.co.xisystems.itis_rrm.ui.mainview.create.select_item.SectionProj_Item
import za.co.xisystems.itis_rrm.ui.mainview.work.INSET
import za.co.xisystems.itis_rrm.ui.mainview.work.INSET_TYPE_KEY
import za.co.xisystems.itis_rrm.utils.JobUtils

/**
 * Created by Francis Mahlava on 2019/12/29.
 */


open class Project_Item(private val itemDesc: SectionProj_Item) : Item() {
    @MyState
    private var job: JobDTO? = null
    init {
        extras[INSET_TYPE_KEY] = INSET
    }

    override fun getLayout() = R.layout.new_job_item

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
                viewHolder.apply {
            textViewItem.text = (itemDesc.itemDTO.itemCode +"  "+ itemDesc.itemDTO.descr)
                    val jobItemEstimate: JobItemEstimateDTO? = getJobItemEstimate(itemDesc.itemDTO.itemId)
                    if (jobItemEstimate != null && jobItemEstimate.isEstimateComplete()) {
                        val lineRate: Double = jobItemEstimate.lineRate
                        val tenderRate: Double = itemDesc.itemDTO.tenderRate
                        val qty: Double = jobItemEstimate.qty
                costTextView.text = ( "$qty  *   R $tenderRate = " + JobUtils.formatCost(lineRate))
                        subTextView.visibility = View.GONE
                    } else {
                        costTextView.text = "Incomplete Estimate..."
                        subTextView.visibility = View.VISIBLE
                    }


        }
    }

    private fun getJobItemEstimate(itemId: String): JobItemEstimateDTO? {
        return getJob()?.getJobEstimateByItemId(itemId)
    }

    private fun getJob(): JobDTO? {
        return this.job
    }


}
