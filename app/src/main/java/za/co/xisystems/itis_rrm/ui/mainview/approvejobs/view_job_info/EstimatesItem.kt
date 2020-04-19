package za.co.xisystems.itis_rrm.ui.mainview.approvejobs.view_job_info

import android.app.Dialog
import android.net.Uri
import androidx.fragment.app.FragmentActivity
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.estimates_item.*
import timber.log.Timber
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimateDTO
import za.co.xisystems.itis_rrm.ui.mainview.approvejobs.ApproveJobsViewModel
import za.co.xisystems.itis_rrm.utils.Coroutines
import za.co.xisystems.itis_rrm.utils.GlideApp
import za.co.xisystems.itis_rrm.utils.zoomage.ZoomageView
import java.io.File


/**
 * Created by Francis Mahlava on 2020/01/02.
 */
class EstimatesItem(
    private val jobItemEstimateDTO: JobItemEstimateDTO,
    private val approveViewModel: ApproveJobsViewModel,
    private val activity: FragmentActivity?
) : Item() {

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.apply {
            estimation_item_quantity_textView.text = "Qty: " + jobItemEstimateDTO.qty.toString()
            estimation_item_price_textView.text = "R " + jobItemEstimateDTO.lineRate.toString()
            Coroutines.main {
                val descr =
                    approveViewModel.getDescForProjectItemId(jobItemEstimateDTO.projectItemId!!)
                val uom =
                    approveViewModel.getUOMForProjectItemId(jobItemEstimateDTO.projectItemId!!)
                measure_item_description_textView.text = descr
                // estimation_item_uom_textView.text = "Unit of Measure: $uom"
                if (uom == "NONE" || uom == "") {
                    estimation_item_uom_textView.text = ""
                } else {
                    estimation_item_uom_textView.text = "Unit of Measure: $uom"
                }

            }

            updateStartImage()
            updateEndImage()

        }

    }

    private fun showZoomedImage(imageUrl: String?) {
        val dialog = Dialog(activity!!, R.style.dialog_full_screen)
        dialog.setContentView(R.layout.new_job_photo)
        val zoomageView =
            dialog.findViewById<ZoomageView>(R.id.zoomedImage)
        GlideApp.with(activity)
            .load(imageUrl)
            .into(zoomageView)
        dialog.show()
    }

    override fun getLayout() = R.layout.estimates_item

    private fun GroupieViewHolder.updateStartImage() {
        Coroutines.main {

            try {

                val startPhoto =
                    approveViewModel.getJobEstimationItemsPhotoStartPath(jobItemEstimateDTO.estimateId)

                GlideApp.with(this.containerView)
                    .load(Uri.fromFile(File(startPhoto)))
                    .error(R.drawable.no_image)
                    .placeholder(R.drawable.logo_new_medium)
                    .into(photoPreviewStart)

                photoPreviewStart.setOnClickListener {
                    showZoomedImage(startPhoto)
                }
            } catch (e: NullPointerException) {
                photoPreviewStart.setOnClickListener(null)
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }


    private fun GroupieViewHolder.updateEndImage() {
        Coroutines.main {
            try {
                val endPhoto =
                    approveViewModel.getJobEstimationItemsPhotoEndPath(jobItemEstimateDTO.estimateId)

                GlideApp.with(this.containerView)
                    .load(Uri.fromFile(File(endPhoto)))
                    .error(R.drawable.no_image)
                    .placeholder(R.drawable.logo_new_medium)
                    .into(photoPreviewEnd)

                photoPreviewEnd.setOnClickListener {
                    showZoomedImage(endPhoto)
                }

            } catch (e: NullPointerException) {
                photoPreviewEnd.setOnClickListener(null)
            } catch (e: Exception) {
                Timber.e(e)

            }
        }
    }

}
