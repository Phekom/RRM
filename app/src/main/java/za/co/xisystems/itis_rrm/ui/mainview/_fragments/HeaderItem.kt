package za.co.xisystems.itis_rrm.ui.mainview._fragments

import android.view.View
import androidx.annotation.DrawableRes
import com.xwray.groupie.viewbinding.BindableItem
import timber.log.Timber
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.databinding.ItemHeaderBinding
import za.co.xisystems.itis_rrm.ui.mainview.work.WorkViewModel
import za.co.xisystems.itis_rrm.utils.Coroutines

open class HeaderItem(
    @DrawableRes private val iconResId: Int? = null,
    workItems: JobDTO,
    var workViewModel: WorkViewModel,
    private val onIconClickListener: View.OnClickListener? = null
) : BindableItem<ItemHeaderBinding>() {

    var jobId = workItems.jobId
    var sectionId: String? = null

    override fun getLayout(): Int {
        return R.layout.item_header
    }

    /**
     * Perform any actions required to set up the view for display.
     *
     * @param viewBinding The ViewBinding to bind
     * @param position The adapter position
     */
    override fun bind(viewBinding: ItemHeaderBinding, position: Int) {
        viewBinding.apply {
            Coroutines.main {
                val jobNumber = workViewModel.getItemJobNo(jobId)
                title.apply {
                    text = root.context.getString(R.string.pair, "JI:", jobNumber)
                    subtitle.apply {
                        try {
                            val sectionId = workViewModel.getProjectSectionIdForJobId(jobId)
                            val route = workViewModel.getRouteForProjectSectionId(sectionId)
                            val section = workViewModel.getSectionForProjectSectionId(sectionId)
                            val subtitleResId = workViewModel.getItemDescription(jobId)
                            val sectionRoute = " ( $route ${"/0$section"} )"
                            visibility = View.GONE
                            subtitleResId.let {
                                visibility = View.VISIBLE
                                text = root.context.getString(R.string.pair, it, sectionRoute)
                            }
                        } catch (ex: Exception) {
                            Timber.e(ex, "Could not render job item")
                        }
                    }
                }
            }
            viewBinding.icon.apply {
                visibility = View.GONE
                iconResId?.let {
                    visibility = View.VISIBLE
                    setImageResource(it)
                    setOnClickListener(onIconClickListener)
                }
            }
        }
    }

    override fun initializeViewBinding(view: View): ItemHeaderBinding {
        return ItemHeaderBinding.bind(view)
    }
}
