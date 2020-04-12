package za.co.xisystems.itis_rrm.ui.mainview.approvejobs.view_job_info

import android.app.Dialog
import android.net.Uri
import androidx.fragment.app.FragmentActivity
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.estimates_item.*
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimateDTO
import za.co.xisystems.itis_rrm.ui.mainview.approvejobs.ApproveJobsViewModel
import za.co.xisystems.itis_rrm.utils.Coroutines
import za.co.xisystems.itis_rrm.utils.GlideApp
import za.co.xisystems.itis_rrm.utils.zoomage.ZoomageView
import java.io.File
import java.lang.NullPointerException


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
            //            appListID1.text = getItemId(position + 1).toString()
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
            photoPreviewEnd.setOnClickListener {
                Coroutines.main {
                    val endPhoto =
                        approveViewModel.getJobEstimationItemsPhotoEndPath(jobItemEstimateDTO.estimateId)
                    showZoomedImage(endPhoto)
                }
            }
            updateStartImage()
            updateEndImage()

        }

    }

    private fun showZoomedImage(imageUrl: String?) {
        val dialog = Dialog(this.activity, R.style.dialog_full_screen)
        dialog.setContentView(R.layout.new_job_photo)
        val zoomageView =
            dialog.findViewById<ZoomageView>(R.id.zoomedImage)
        GlideApp.with(this.activity!!)
            .load(imageUrl)
            .into(zoomageView)
        dialog.show()
    }

    override fun getLayout() = R.layout.estimates_item

    private fun GroupieViewHolder.updateStartImage() {
        Coroutines.main {

            val startPhoto =
                approveViewModel.getJobEstimationItemsPhotoStartPath(jobItemEstimateDTO.estimateId)

            if (null != startPhoto) {
                GlideApp.with(this.containerView)
                    .load(Uri.fromFile(File(startPhoto)))
                    .placeholder(R.drawable.logo_new_medium)
                    .into(photoPreviewStart)
            } else {
                GlideApp.with(this.containerView)
                    .load(R.drawable.no_image)
                    .placeholder(R.drawable.logo_new_medium)
                    .into(photoPreviewStart)
                photoPreviewStart.setOnClickListener(null)
            }


        }
    }


    private fun GroupieViewHolder.updateEndImage() {
        Coroutines.main {
            val endPhoto =
                approveViewModel.getJobEstimationItemsPhotoEndPath(jobItemEstimateDTO.estimateId)
            if (null != endPhoto) {
                GlideApp.with(this.containerView)
                    .load(Uri.fromFile(File(endPhoto)))
                    .placeholder(R.drawable.logo_new_medium)
                    .into(photoPreviewEnd)
            } else {
                GlideApp.with(this.containerView)
                    .load(R.drawable.no_image)
                    .placeholder(R.drawable.logo_new_medium)
                    .into(photoPreviewEnd)
                photoPreviewEnd.setOnClickListener(null)
            }
        }
    }

    private fun getItemId(position: Int): Long {
        return position.toLong()
    }
}
