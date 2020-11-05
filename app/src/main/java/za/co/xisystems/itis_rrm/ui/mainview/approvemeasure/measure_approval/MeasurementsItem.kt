package za.co.xisystems.itis_rrm.ui.mainview.approvemeasure.measure_approval

import android.net.Uri
import android.text.Editable
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.Navigation
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import java.io.File
import kotlinx.android.synthetic.main.measurements_item.*
import www.sanju.motiontoast.MotionToast
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemMeasureDTO
import za.co.xisystems.itis_rrm.ui.extensions.motionToast
import za.co.xisystems.itis_rrm.ui.mainview.approvemeasure.ApproveMeasureViewModel
import za.co.xisystems.itis_rrm.utils.Coroutines
import za.co.xisystems.itis_rrm.utils.GlideApp
import za.co.xisystems.itis_rrm.utils.ServiceUtil

/**
 * Created by Francis Mahlava on 2020/01/02.
 */
class MeasurementsItem(
    private val jobItemMeasureDTO: JobItemMeasureDTO,
    private val approveViewModel: ApproveMeasureViewModel,
    private val activity: FragmentActivity?,
    private val viewLifecycleOwner: LifecycleOwner
) : Item() {

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.apply {

            Coroutines.main {

            val quantity = approveViewModel.getQuantityForMeasureItemId(jobItemMeasureDTO.itemMeasureId!!)
                quantity.observe(viewLifecycleOwner, {
                    measure_item_quantity_textView.text = activity?.getString(R.string.pair, "Qty:", it.toString())
                })

                val lineRate = approveViewModel.getLineRateForMeasureItemId(jobItemMeasureDTO.itemMeasureId!!)
                lineRate.observe(viewLifecycleOwner, {
                    measure_item_price_textView.text = activity?.getString(R.string.pair, "R", it.toString())
                })

                val descri = approveViewModel.getDescForProjectId(jobItemMeasureDTO.projectItemId!!)
                val uom =
                    approveViewModel.getUOMForProjectItemId(jobItemMeasureDTO.projectItemId!!)
                measure_item_description_textView.text = activity?.getString(R.string.pair, "Estimate -", descri)
                if (uom == "NONE") {
                    measure_item_uom_textView.text = ""
                } else {
                    measure_item_uom_textView.text = activity?.getString(R.string.pair, "Unit of Measure:", uom)
                }
            }
            correctButton.setOnClickListener {
                sendItemType(jobItemMeasureDTO)
            }
            view_captured_item_photo.setOnClickListener {
                Coroutines.main {
                    approveViewModel.generateGalleryUI(jobItemMeasureDTO.itemMeasureId!!)
                    Navigation.findNavController(it)
                        .navigate(R.id.action_measureApprovalFragment_to_measureGalleryFragment)
                }
            }
            Coroutines.main {
                updateMeasureImage()
            }
        }
    }

    private fun sendItemType(
        jobItemMeasureDTO: JobItemMeasureDTO
    ) {
        Coroutines.main {
            alertdialog(jobItemMeasureDTO)
        }
    }

    private fun alertdialog(jobItemMeasureDTO: JobItemMeasureDTO) {
        val textEntryView: View = activity!!.layoutInflater.inflate(R.layout.measure_dialog, null)
        val editQuantity = textEntryView.findViewById<View>(R.id.new_qty) as EditText

        val alert = AlertDialog.Builder(activity) // ,android.R.style.Theme_DeviceDefault_Dialog
        alert.setView(textEntryView)
        alert.setTitle(R.string.correct_measure)
        alert.setIcon(R.drawable.ic_edit)
        alert.setMessage(R.string.are_you_sure_you_want_to_correct)

        editQuantity.text = Editable.Factory.getInstance().newEditable(jobItemMeasureDTO.qty.toString())

        // Yes button
        alert.setPositiveButton(
            R.string.save
        ) { dialog, which ->
            if (ServiceUtil.isNetworkAvailable(activity.applicationContext)) {
                Coroutines.main {
                    if (editQuantity.text.toString() == "" || nanCheck(editQuantity.text.toString())) {
                        activity.motionToast("Please Enter a valid Quantity", MotionToast.TOAST_ERROR)
                    } else {
                        val updated = approveViewModel.upDateMeasure(
                            editQuantity.text.toString(),
                            jobItemMeasureDTO.itemMeasureId
                        )
                        if (updated.isBlank()) {
                            activity.motionToast("Data Updated was Successful", MotionToast.TOAST_SUCCESS)
                        } else {
                            activity.motionToast("Data Updated Error!!  Server Not Reachable", MotionToast.TOAST_ERROR)
                        }
                    }
                }
            } else {
                activity.motionToast("No connection detected.", MotionToast.TOAST_NO_INTERNET)
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

    private fun nanCheck(toString: String): Boolean {
        return try {
            val dbl = toString.toDouble()
            dbl.isNaN()
        } catch (e: Exception) {
            true
        }
    }

    override fun getLayout() = R.layout.measurements_item

    private suspend fun GroupieViewHolder.updateMeasureImage() {
        Coroutines.main {

            val photoPaths = approveViewModel.getJobMeasureItemsPhotoPath(jobItemMeasureDTO.itemMeasureId!!)
            if (photoPaths.isNotEmpty()) {
                val measurePhoto = photoPaths.first()
                if (!measurePhoto.isBlank()) {
                    GlideApp.with(this.containerView)
                        .load(Uri.fromFile(File(measurePhoto)))
                        .placeholder(R.drawable.logo_new_medium)
                        .into(view_captured_item_photo)
                }
            } else {
                GlideApp.with(this.containerView)
                    .load(R.drawable.no_image)
                    .placeholder(R.drawable.logo_new_medium)
                    .into(view_captured_item_photo)
            }
        }
    }
}
