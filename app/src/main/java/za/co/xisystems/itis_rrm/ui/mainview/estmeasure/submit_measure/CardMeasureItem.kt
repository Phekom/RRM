package za.co.xisystems.itis_rrm.ui.mainview.estmeasure.submit_measure

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.net.Uri
import androidx.fragment.app.FragmentActivity
import com.bumptech.glide.Glide
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.measure_estimate_list_item.*
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.ui.mainview.estmeasure.MeasureViewModel
import za.co.xisystems.itis_rrm.ui.mainview.work.INSET
import za.co.xisystems.itis_rrm.ui.mainview.work.INSET_TYPE_KEY
import za.co.xisystems.itis_rrm.utils.Coroutines
import za.co.xisystems.itis_rrm.utils.zoomage.ZoomageView
import java.io.File

open class CardMeasureItem(
    val activity: FragmentActivity?,
    val photo: String,

    val qty: String,
    val rate: String,
    val text: String,
    val measureViewModel: MeasureViewModel
) : Item() {
    var appContext: Context? = null
    init {
        extras[INSET_TYPE_KEY] = INSET
        if (activity != null) {
            appContext = activity.applicationContext
        }
    }
    // var clickListener: ((CardMeasureItem) -> Unit)? = null

    override fun getLayout() = R.layout.measure_estimate_list_item


    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.apply {
            jobNo.text = text
            measure_estimation_qty_textView.text = qty

            @SuppressLint("SetTextI18n")
            measure_estimation_line_amount.text = "R  $rate"

            measurements_photo_image.setOnClickListener {
                Coroutines.main {
                    val measurePhoto =
                        measureViewModel.getJobMeasureItemsPhotoPath(photo)
                    showZoomedImage(measurePhoto)
                }
            }

            updateMeasureImage()
        }

        viewHolder.itemView.setOnLongClickListener {
            Coroutines.main {
                measureViewModel.deleteItemMeasureFromList(photo)
                measureViewModel.deleteItemMeasurePhotoFromList(photo)
            }
            it.isLongClickable
        }

    }

    private fun GroupieViewHolder.updateMeasureImage() {
        Coroutines.main {

            val measurePhoto =
                measureViewModel.getJobMeasureItemsPhotoPath(photo)

            Glide.with(this.containerView)
                .load(Uri.fromFile(File(measurePhoto)))
                .placeholder(R.drawable.logo_new_medium)
                .into(measurements_photo_image)

        }
    }

    private fun showZoomedImage(imageUrl: String?) {
        if (null != appContext) {
            val dialog = Dialog(appContext, R.style.dialog_full_screen)
            dialog.setContentView(R.layout.new_job_photo)
            val zoomageView =
                dialog.findViewById<ZoomageView>(R.id.zoomedImage)
            Glide.with(dialog.context)
                .load(imageUrl)
                .into(zoomageView)
            dialog.show()
        }
    }

}
