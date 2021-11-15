package za.co.xisystems.itis_rrm.ui.mainview.approvemeasure.measure_approval

import android.net.Uri
import android.text.Editable
import android.text.InputType
import android.text.method.DigitsKeyListener
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.Navigation
import com.xwray.groupie.viewbinding.BindableItem
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.custom.notifications.ToastStyle
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemMeasureDTO
import za.co.xisystems.itis_rrm.databinding.MeasurementsItemBinding
import za.co.xisystems.itis_rrm.extensions.uomForUI
import za.co.xisystems.itis_rrm.ui.extensions.DecimalSignedDigitsKeyListener
import za.co.xisystems.itis_rrm.ui.extensions.extensionToast
import za.co.xisystems.itis_rrm.ui.mainview.approvemeasure.ApproveMeasureViewModel
import za.co.xisystems.itis_rrm.utils.Coroutines
import za.co.xisystems.itis_rrm.utils.GlideApp
import za.co.xisystems.itis_rrm.utils.ServiceUtil
import java.io.File
import java.lang.ref.WeakReference

/**
 * Created by Francis Mahlava on 2020/01/02.
 */
class MeasurementsItem(
    private val jobItemMeasureDTO: JobItemMeasureDTO,
    private val approveViewModel: ApproveMeasureViewModel,
    private val fragmentReference: WeakReference<MeasureApprovalFragment>,
    private val viewLifecycleOwner: LifecycleOwner
) : BindableItem<MeasurementsItemBinding>() {

    val activity = fragmentReference.get()?.requireActivity()
    private fun sendItemType(
        jobItemMeasureDTO: JobItemMeasureDTO
    ) {
        Coroutines.main {
            alertdialog(jobItemMeasureDTO)
        }
    }

    private fun alertdialog(jobItemMeasureDTO: JobItemMeasureDTO) {

        val textEntryView: View = activity?.layoutInflater?.inflate(R.layout.measure_dialog, null)!!
        val editQuantity = textEntryView.findViewById<View>(R.id.new_qty) as EditText
        editQuantity.inputType = InputType.TYPE_NUMBER_VARIATION_NORMAL and InputType.TYPE_NUMBER_FLAG_DECIMAL
        val digitsKeyListener = DigitsKeyListener.getInstance("-1234567890.")
        editQuantity.keyListener = DecimalSignedDigitsKeyListener(digitsKeyListener)

        val alert = AlertDialog.Builder(activity) // ,android.R.style.Theme_DeviceDefault_Dialog
        alert.setView(textEntryView)
        alert.setTitle(R.string.correct_measure)
        alert.setIcon(R.drawable.ic_edit)
        alert.setMessage(R.string.are_you_sure_you_want_to_correct)

        editQuantity.text = Editable.Factory.getInstance().newEditable(jobItemMeasureDTO.qty.toString())
        editQuantity.setSelectAllOnFocus(true)
        editQuantity.requestFocus()
        // Yes button
        alert.setPositiveButton(
            R.string.save
        ) { _, _ ->
            pushEdit(activity, editQuantity.text.toString(), jobItemMeasureDTO.itemMeasureId)
        }
        // No button
        alert.setNegativeButton(
            R.string.cancel
        ) { dialog, _ ->
            // Do nothing but close dialog
            dialog.dismiss()
        }
        val editAlert = alert.create()
        editAlert.show()
    }

    @Suppress("MagicNumber")
    private fun pushEdit(
        activity: FragmentActivity,
        editQuantity: String,
        itemMeasureId: String
    ) {
        if (ServiceUtil.isNetworkAvailable(activity.applicationContext)) {
            Coroutines.main {
                when {
                    editQuantity == "" || nanCheck(editQuantity) -> {
                        activity.extensionToast(
                            message = "Please Enter a valid Quantity",
                            style = ToastStyle.WARNING
                        )
                    }
                    editQuantity.length > 9 -> {
                        activity.extensionToast(
                            message = "You have exceeded the quantity allowed",
                            style = ToastStyle.WARNING
                        )
                    }
                    else -> {
                        correctMeasurement(editQuantity, itemMeasureId, activity)
                    }
                }
            }
        } else {
            activity.extensionToast(
                message = "No connection detected.",
                style = ToastStyle.NO_INTERNET
            )
        }
    }

    private suspend fun correctMeasurement(
        editQuantity: String,
        itemMeasureId: String,
        activity: FragmentActivity
    ) {
        fragmentReference.get()?.toggleLongRunning(true)
        val updated = approveViewModel.upDateMeasure(
            editQuantity,
            itemMeasureId
        )
        if (updated.isBlank()) {
            activity.extensionToast(
                message = "Data Updated",
                style = ToastStyle.SUCCESS
            )
        } else {
            activity.extensionToast(
                message = "Error on update: $updated.",
                style = ToastStyle.ERROR
            )
        }
        fragmentReference.get()?.toggleLongRunning(false)
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

    private suspend fun MeasurementsItemBinding.updateMeasureImage() {
        Coroutines.main {

            val photoPaths = approveViewModel.getJobMeasureItemsPhotoPath(jobItemMeasureDTO.itemMeasureId)
            if (photoPaths.isNotEmpty()) {
                val measurePhoto = photoPaths.first()
                if (measurePhoto.isNotBlank()) {
                    GlideApp.with(this.root.context)
                        .load(Uri.fromFile(File(measurePhoto)))
                        .placeholder(R.drawable.logo_new_medium)
                        .into(viewCapturedItemPhoto)
                }
            } else {
                GlideApp.with(this.root.context)
                    .load(R.drawable.no_image)
                    .placeholder(R.drawable.logo_new_medium)
                    .into(viewCapturedItemPhoto)
            }
        }
    }

    /**
     * Perform any actions required to set up the view for display.
     *
     * @param viewBinding The ViewBinding to bind
     * @param position The adapter position
     */
    override fun bind(viewBinding: MeasurementsItemBinding, position: Int) {
        viewBinding.apply {

            Coroutines.main {

                val quantity = approveViewModel.getQuantityForMeasureItemId(jobItemMeasureDTO.itemMeasureId)
                quantity.observe(viewLifecycleOwner, {
                    measureItemQuantityTextView.text = activity?.getString(R.string.pair, "Qty:", it.toString())
                })

                val descri = approveViewModel.getDescForProjectId(jobItemMeasureDTO.projectItemId!!)
                val uom =
                    approveViewModel.getUOMForProjectItemId(jobItemMeasureDTO.projectItemId!!)
                measureItemDescriptionTextView.text = activity?.getString(R.string.pair, "Estimate -", descri)

                val uomString = if (uom.isBlank() || uom.lowercase() == "none") {
                    ""
                } else {
                    activity?.uomForUI(uom)
                }

                val lineRate = approveViewModel.getLineRateForMeasureItemId(jobItemMeasureDTO.itemMeasureId)
                lineRate.observe(viewLifecycleOwner, {
                    val itemPrice = activity?.getString(R.string.pair, "R", it.toString())
                    measureItemUomTextView.text = activity?.getString(R.string.pair, itemPrice, uomString)
                })
                measureItemPriceTextView.visibility = View.GONE
            }
            correctButton.setOnClickListener {
                sendItemType(jobItemMeasureDTO)
            }
            viewCapturedItemPhoto.setOnClickListener {
                Coroutines.main {
                    approveViewModel.generateGalleryUI(jobItemMeasureDTO.itemMeasureId)
                    Navigation.findNavController(it)
                        .navigate(R.id.action_measureApprovalFragment_to_measureGalleryFragment)
                }
            }
            Coroutines.main {
                updateMeasureImage()
            }
        }
    }

    override fun initializeViewBinding(view: View): MeasurementsItemBinding {
        return MeasurementsItemBinding.bind(view)
    }
}
