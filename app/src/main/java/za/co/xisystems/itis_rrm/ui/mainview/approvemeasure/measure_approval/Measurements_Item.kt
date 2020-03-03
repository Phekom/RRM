package za.co.xisystems.itis_rrm.ui.mainview.approvemeasure.measure_approval

import android.app.Dialog
import android.net.Uri
import androidx.fragment.app.FragmentActivity
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.estimates_item.measure_item_description_textView
import kotlinx.android.synthetic.main.measurements_item.*
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.data._commons.views.ToastUtils
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemMeasureDTO
import za.co.xisystems.itis_rrm.ui.mainview.approvemeasure.ApproveMeasureViewModel
import za.co.xisystems.itis_rrm.utils.Coroutines
import za.co.xisystems.itis_rrm.utils.GlideApp
import za.co.xisystems.itis_rrm.utils.zoomage.ZoomageView
import java.io.File


/**
 * Created by Francis Mahlava on 2020/01/02.
 */
class Measurements_Item(
    val jobItemMeasureDTO: JobItemMeasureDTO,
    private val approveViewModel: ApproveMeasureViewModel,
    private val activity: FragmentActivity?
) : Item() {

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.apply {
            // appListID1.text = getItemId(position + 1).toString()
            measure_item_quantity_textView.text = "Quantity : " + jobItemMeasureDTO.qty.toString()
            measure_item_price_textView.text = "R " + jobItemMeasureDTO.lineRate.toString()
            Coroutines.main {
                val descri = approveViewModel.getDescForProjectId(jobItemMeasureDTO.projectItemId!!)
                val uom =
                    approveViewModel?.getUOMForProjectItemId(jobItemMeasureDTO.projectItemId!!)
                measure_item_description_textView.text = "Estimate - " + descri
                measure_item_uom_textView.text = "Unit of Measure: $uom"
                if (uom.equals("NONE")) {
                    measure_item_uom_textView.text = ""
                } else {
                    measure_item_uom_textView.text = "Unit of Measure: $uom"
                }

            }
            view_captured_item_photo.setOnClickListener {
                Coroutines.main {
                    val measurePhoto =
                        approveViewModel?.getJobMeasureItemsPhotoPath(jobItemMeasureDTO.itemMeasureId!!)
                showZoomedImage(measurePhoto)
                }
            }
            updateMeasureImage()

        }

    }

    private fun showZoomedImage(imageUrl: String?) {
        val dialog = Dialog(this!!.activity, R.style.dialog_full_screen)
        dialog.setContentView(R.layout.new_job_photo)
        val zoomageView =
            dialog.findViewById<ZoomageView>(R.id.zoomedImage)
        GlideApp.with(this.activity!!)
            .load(imageUrl)
            .into(zoomageView)
        dialog.show()
    }

    override fun getLayout() = R.layout.measurements_item

    private fun GroupieViewHolder.updateMeasureImage() {
        Coroutines.main {
            val measurePhoto =
                approveViewModel?.getJobMeasureItemsPhotoPath(jobItemMeasureDTO.itemMeasureId!!)
//            ToastUtils().toastLong(activity,measurePhoto)

            if (measurePhoto != null){
            GlideApp.with(this.containerView)
                .load(Uri.fromFile(File(measurePhoto)))
                .placeholder(R.drawable.logo_new_medium)
                .into(view_captured_item_photo)
        }else{
                GlideApp.with(this.containerView)
                    .load(R.drawable.no_image)
                    .placeholder(R.drawable.logo_new_medium)
                    .into(view_captured_item_photo)
            }

        }
    }




}

private fun GroupieViewHolder.getItemId(position: Int): Long {
    return position.toLong()
}

