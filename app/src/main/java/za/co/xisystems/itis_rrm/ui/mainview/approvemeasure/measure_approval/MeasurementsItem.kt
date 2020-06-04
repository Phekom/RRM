package za.co.xisystems.itis_rrm.ui.mainview.approvemeasure.measure_approval

import android.app.Dialog
import android.net.Uri
import android.text.Editable
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.estimates_item.measure_item_description_textView
import kotlinx.android.synthetic.main.measurements_item.*
import kotlinx.android.synthetic.main.measurements_item.correctButton
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemMeasureDTO
import za.co.xisystems.itis_rrm.ui.mainview.approvemeasure.ApproveMeasureViewModel
import za.co.xisystems.itis_rrm.utils.Coroutines
import za.co.xisystems.itis_rrm.utils.GlideApp
import za.co.xisystems.itis_rrm.utils.ServiceUtil
import za.co.xisystems.itis_rrm.utils.toast
import za.co.xisystems.itis_rrm.utils.zoomage.ZoomageView
import java.io.File


/**
 * Created by Francis Mahlava on 2020/01/02.
 */
class MeasurementsItem(
    private val jobItemMeasureDTO: JobItemMeasureDTO,
    private val approveViewModel: ApproveMeasureViewModel,
    private val activity: FragmentActivity?,
    private val dialog: Dialog,
    private val viewLifecycleOwner: LifecycleOwner
) : Item() {

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.apply {
            //            appListID1.text = getItemId(position + 1).toString()
//            measure_item_quantity_textView.text = "Quantity : " + jobItemMeasureDTO.qty.toString()
//            measure_item_price_textView.text = "R " + jobItemMeasureDTO.lineRate.toString()
            Coroutines.main { dialog.show()
                val quantity = approveViewModel.getQuantityForMeasureItemId(jobItemMeasureDTO.itemMeasureId!!)
                quantity.observe(viewLifecycleOwner, Observer {
                    measure_item_quantity_textView.text = "Qty: $it"
                })
                val lineRate = approveViewModel.getLineRateForMeasureItemId(jobItemMeasureDTO.itemMeasureId!!)
                lineRate.observe(viewLifecycleOwner, Observer {
                    measure_item_price_textView.text = "R $it"
                    dialog.dismiss()
                })

                val descri = approveViewModel.getDescForProjectId(jobItemMeasureDTO.projectItemId!!)
                val uom =
                    approveViewModel.getUOMForProjectItemId(jobItemMeasureDTO.projectItemId!!)
                measure_item_description_textView.text = "Estimate - $descri"
                measure_item_uom_textView.text = "Unit of Measure: $uom"
                if (uom == "NONE") {
                    measure_item_uom_textView.text = ""
                } else {
                    measure_item_uom_textView.text = "Unit of Measure: $uom"
                }

            }
            correctButton.setOnClickListener {
                sendItemType((it), jobItemMeasureDTO,approveViewModel)
            }
            view_captured_item_photo.setOnClickListener {
                Coroutines.main {
                    val measurePhoto =
                        approveViewModel.getJobMeasureItemsPhotoPath(jobItemMeasureDTO.itemMeasureId!!)
                    showZoomedImage(measurePhoto)
                }
            }
            updateMeasureImage()

        }

    }

    private fun sendItemType(
        it: View?,
        jobItemMeasureDTO: JobItemMeasureDTO,
        approveViewModel: ApproveMeasureViewModel
    ) {
        Coroutines.main {
            alertdialog(jobItemMeasureDTO)
        }
    }

    private fun alertdialog(jobItemMeasureDTO: JobItemMeasureDTO) {
        val text = arrayOfNulls<String>(2)
        val textEntryView: View = activity!!.layoutInflater.inflate(R.layout.measure_dialog, null)
        val new_quantity = textEntryView.findViewById<View>(R.id.new_qty) as EditText
//        val structure_Type = textEntryView.findViewById<View>(R.id.structure_Type1) as TextView
//        val structure_InspTitle = textEntryView.findViewById<View>(R.id.structure_InspTitle) as TextView

        val alert = AlertDialog.Builder(activity)//,android.R.style.Theme_DeviceDefault_Dialog
        alert.setView(textEntryView)
        alert.setTitle(R.string.correct_measure)
        alert.setIcon(R.drawable.ic_edit)
        alert.setMessage(R.string.are_you_sure_you_want_to_correct)

        new_quantity.text = Editable.Factory.getInstance().newEditable(jobItemMeasureDTO.qty.toString())

        // Yes button
        alert.setPositiveButton(
            R.string.save
        ) { dialog, which ->
            if (ServiceUtil.isNetworkConnected(activity.applicationContext)) {
                Coroutines.main{
                    if(new_quantity.text.toString() == "" || Integer.parseInt(new_quantity.text.toString()) == 0 ){
                        activity.toast("Please Enter a valid Quantity")
                    }else{
                        val updated =  approveViewModel.upDateMeasure(new_quantity.text.toString(), jobItemMeasureDTO.itemMeasureId)
                        if (updated.isBlank()) {
                            activity.toast("Data Updated was Successful")
                        }else{
                            activity.toast("Data Updated Error!!  Server Not Reachable")
                        }
                    }

                }
            } else {
                activity.toast("No connection detected.")
            }
        }
        // No button
        alert.setNegativeButton(
            R.string.cancel
        ) { dialog, which ->
            // Do nothing but close dialog
            dialog.dismiss()
        }
        val declineAlert = alert.create()
        declineAlert.show()

    }

    private fun showZoomedImage(imageUrl: String?) {
        val dialog = this.activity?.let { Dialog(it, R.style.dialog_full_screen) }
        dialog?.setContentView(R.layout.new_job_photo)
        val zoomageView =
            dialog?.findViewById<ZoomageView>(R.id.zoomedImage)
        GlideApp.with(this.activity!!)
            .load(imageUrl)
            .into(zoomageView!!)
        dialog.show()
    }

    override fun getLayout() = R.layout.measurements_item

    private fun GroupieViewHolder.updateMeasureImage() {
        Coroutines.main {
            val measurePhoto =
                approveViewModel.getJobMeasureItemsPhotoPath(jobItemMeasureDTO.itemMeasureId!!)
//            ToastUtils().toastLong(activity,measurePhoto)

            if (measurePhoto != null) {
            GlideApp.with(this.containerView)
                    .load(Uri.fromFile(File(measurePhoto)))
                    .placeholder(R.drawable.logo_new_medium)
                    .into(view_captured_item_photo)
            } else {
                GlideApp.with(this.containerView)
                    .load(R.drawable.no_image)
                    .placeholder(R.drawable.logo_new_medium)
                    .into(view_captured_item_photo)
            }

        }
    }




}

private fun getItemId(position: Int): Long {
    return position.toLong()
}

