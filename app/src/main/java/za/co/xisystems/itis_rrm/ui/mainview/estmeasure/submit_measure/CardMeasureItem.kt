package za.co.xisystems.itis_rrm.ui.mainview.estmeasure.submit_measure

import android.app.Dialog
import android.net.Uri
import android.view.View
import androidx.fragment.app.FragmentActivity
import androidx.navigation.Navigation
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.measure_estimate_list_item.*
import kotlinx.android.synthetic.main.work_list_item.*
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.ui.mainview.estmeasure.MeasureViewModel
import za.co.xisystems.itis_rrm.ui.mainview.work.INSET
import za.co.xisystems.itis_rrm.ui.mainview.work.INSET_TYPE_KEY
import za.co.xisystems.itis_rrm.ui.mainview.work.WorkViewModel
import za.co.xisystems.itis_rrm.utils.Coroutines
import za.co.xisystems.itis_rrm.utils.GlideApp
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

    init {
        extras[INSET_TYPE_KEY] = INSET
    }

    override fun getLayout() = R.layout.measure_estimate_list_item

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.apply {
            jobNo.text = text
            measure_estimation_qty_textView.text = qty
            measure_estimation_line_amount.text = "R  $rate"

            measurements_photo_image.setOnClickListener(View.OnClickListener {
                Coroutines.main {
                    val measurePhoto =
                        measureViewModel?.getJobMeasureItemsPhotoPath(photo)
//                        approveViewModel?.getJobMeasureItemsPhotoPath(jobItemMeasureDTO.itemMeasureId!!)
                    showZoomedImage(measurePhoto)
                }
            })

            updateMeasureImage()
        }

    }
//    private fun sendJobtoWork(
//        workViewModel: WorkViewModel,
//        estimateId: String?,
//        view: View?
//    ) {
//        val estimateId = estimateId
//        Coroutines.main {
//            workViewModel.work_Item.value = estimateId
//        }
//        Navigation.findNavController(view!!)
//            .navigate(R.id.action_nav_work_to_captureWorkFragment)
//    }


    private fun GroupieViewHolder.updateMeasureImage() {
        Coroutines.main {

            val measurePhoto =
                measureViewModel?.getJobMeasureItemsPhotoPath(photo)
            if (measurePhoto != null){
                GlideApp.with(this.containerView)
                    .load(Uri.fromFile(File(measurePhoto)))
                    .placeholder(R.drawable.logo_new_medium)
                    .into(measurements_photo_image)

            }else{
                GlideApp.with(this.containerView)
                    .load(Uri.fromFile(File("")))
                    .placeholder(R.drawable.logo_new_medium)
                    .into(measurements_photo_image)
            }

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



}
