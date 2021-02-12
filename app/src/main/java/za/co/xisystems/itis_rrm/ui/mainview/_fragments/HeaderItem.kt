package za.co.xisystems.itis_rrm.ui.mainview._fragments

import android.view.View
import androidx.annotation.DrawableRes
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.item_header.*
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.ui.mainview.work.WorkViewModel
import za.co.xisystems.itis_rrm.utils.Coroutines

open class HeaderItem(
    @DrawableRes private val iconResId: Int? = null,
    workItems: JobDTO,
    var workViewModel: WorkViewModel,
    private val onIconClickListener: View.OnClickListener? = null
) : Item() {

    var jobId = workItems.jobId
    var sectionId: String? = null

    override fun getLayout(): Int {
        return R.layout.item_header
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.apply {
            Coroutines.main {
                val jobNumber = workViewModel.getItemJobNo(jobId)
                title.apply {
                    text = context.applicationContext.getString(R.string.pair, "JI:", jobNumber)
                    subtitle.apply {
                        val sectionId = workViewModel.getProjectSectionIdForJobId(jobId)
                        val route = workViewModel.getRouteForProjectSectionId(sectionId)
                        val section = workViewModel.getSectionForProjectSectionId(sectionId)
                        val subtitleResId = workViewModel.getItemDescription(jobId)
                        val sectionRoute = " ( $route ${"/0$section"} )"
                        visibility = View.GONE
                        subtitleResId.let {
                            visibility = View.VISIBLE
                            text = context.applicationContext.getString(R.string.pair, it, sectionRoute)
                        }
                    }
                }
            }
            viewHolder.icon.apply {
                visibility = View.GONE
                iconResId?.let {
                    visibility = View.VISIBLE
                    setImageResource(it)
                    setOnClickListener(onIconClickListener)
                }
            }
        }
    }
}
