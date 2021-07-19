/**
 * Updated by Shaun McDonald on 2021/05/15
 * Last modified on 2021/05/14, 20:32
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 **/

/**
 * Updated by Shaun McDonald on 2021/05/14
 * Last modified on 2021/05/14, 19:43
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 **/

package za.co.xisystems.itis_rrm.ui.mainview.approvejobs.view_job_info

import android.app.Dialog
import android.net.Uri
import android.text.Editable
import android.text.InputType
import android.text.method.DigitsKeyListener
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import java.io.File
import kotlinx.android.synthetic.main.estimates_item.*
import timber.log.Timber
import www.sanju.motiontoast.MotionToast
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.custom.results.XIResult
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimateDTO
import za.co.xisystems.itis_rrm.extensions.isConnected
import za.co.xisystems.itis_rrm.ui.extensions.DecimalSignedDigitsKeyListener
import za.co.xisystems.itis_rrm.ui.extensions.extensionToast
import za.co.xisystems.itis_rrm.ui.mainview.approvejobs.ApproveJobsViewModel
import za.co.xisystems.itis_rrm.utils.Coroutines
import za.co.xisystems.itis_rrm.utils.GlideApp
import za.co.xisystems.itis_rrm.utils.Utils.nanCheck
import za.co.xisystems.itis_rrm.utils.Utils.round
import za.co.xisystems.itis_rrm.utils.zoomage.ZoomageView

/**
 * Created by Francis Mahlava on 2020/01/02.
 */
class EstimatesItem(
    private val jobItemEstimateDTO: JobItemEstimateDTO,
    private val approveViewModel: ApproveJobsViewModel,
    private val activity: FragmentActivity?,
    private val viewLifecycleOwner: LifecycleOwner,
    private val updateObserver: Observer<XIResult<String>?>
) : Item() {

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.apply {
            Coroutines.main {
                val quantity =
                    approveViewModel.getQuantityForEstimationItemId(jobItemEstimateDTO.estimateId)
                quantity.observe(
                    viewLifecycleOwner,
                    { qty ->

                        estimation_item_quantity_textView.text = activity?.getString(R.string.pair, "Qty:", qty.toString())
                    }
                )
                val lineRate =
                    approveViewModel.getLineRateForEstimationItemId(jobItemEstimateDTO.estimateId)
                lineRate.observe(
                    viewLifecycleOwner,
                    {
                        estimation_item_price_textView.text = activity?.getString(R.string.pair, "R", it.toString())
                        // dialog.dismiss()
                    }
                )
//                estimation_item_quantity_textView.text = "Qty: " + quantity //jobItemEstimateDTO.qty.toString()
//                 estimation_item_price_textView.text = "R " +  lineRate //jobItemEstimateDTO.lineRate.toString()

                val descr =
                    approveViewModel.getDescForProjectItemId(jobItemEstimateDTO.projectItemId!!)
                val uom =
                    approveViewModel.getUOMForProjectItemId(jobItemEstimateDTO.projectItemId!!)
                measure_item_description_textView.text = descr

                if (uom == "NONE" || uom == "") {
                    estimation_item_uom_textView.text = ""
                } else {
                    estimation_item_uom_textView.text = activity?.getString(R.string.pair, "Unit of Measure:", uom)
                }
                correctButton.setOnClickListener {
                    sendItemType(jobItemEstimateDTO)
                }
            }
            updateStartImage()
            updateEndImage()
        }
    }

    private fun sendItemType(
        jobItemEstimateDTO: JobItemEstimateDTO
    ) {
        Coroutines.main {
            alertdialog(jobItemEstimateDTO)
        }
    }

    private fun alertdialog(
        jobItemEstimateDTO: JobItemEstimateDTO
    ) {

        val textEntryView: View =
            activity!!.layoutInflater.inflate(R.layout.estimate_dialog, null)
        val quantityEntry = textEntryView.findViewById<View>(R.id.new_qty) as EditText
        val totalEntry = textEntryView.findViewById<View>(R.id.new_total) as TextView
        val rate = textEntryView.findViewById<View>(R.id.current_rate) as TextView
        var updated = false
        val alert = AlertDialog.Builder(activity) // ,android.R.style.Theme_DeviceDefault_Dialog
        alert.setView(textEntryView)
        alert.setTitle(R.string.correct_estimate)
        alert.setIcon(R.drawable.ic_edit)
        alert.setMessage(R.string.are_you_sure_you_want_to_correct_esti)

        Coroutines.main {
            val tenderRate =
                approveViewModel.getTenderRateForProjectItemId(jobItemEstimateDTO.projectItemId!!)
            totalEntry.text = activity.getString(R.string.pair, "R", jobItemEstimateDTO.lineRate.toString())

            rate.text = activity.getString(R.string.pair, "R", tenderRate.toString())
            var cost: Double
            val newQuantity = jobItemEstimateDTO.qty
            val defaultQty = 0.0

            quantityEntry.inputType = InputType.TYPE_NUMBER_FLAG_DECIMAL and InputType.TYPE_NUMBER_VARIATION_NORMAL
            quantityEntry.text = Editable.Factory.getInstance().newEditable("$newQuantity")
            val digitsKeyListener = DigitsKeyListener.getInstance("-1234567890.")
            quantityEntry.keyListener = DecimalSignedDigitsKeyListener(digitsKeyListener)

            quantityEntry.doOnTextChanged { text, _, _, _ ->
                updated = true
                val input = text.toString()
                when {
                    nanCheck(input) || input.toDouble() == 0.0 -> {
                        cost = 0.0
                        totalEntry.text = activity.getString(R.string.pair, "R", cost.toString())
                    }
                    input.length > 9 -> {
                        quantityEntry.text =
                            Editable.Factory.getInstance().newEditable("$defaultQty")
                        activity.extensionToast(
                            message = "You Have exceeded the amount of Quantity allowed",
                            motionType = MotionToast.TOAST_WARNING
                        )
                    }
                    else -> {
                        val qty = input.toDouble()
                        cost = (tenderRate * qty).round(2)
                        totalEntry.text = activity.getString(R.string.pair, "R", cost.toString())
                    }
                }
            }
        }

        // Yes button
        alert.setPositiveButton(
            R.string.save
        ) { dialog, _ ->
            if (updated) {
                validateUpdateQty(activity, quantityEntry, totalEntry, jobItemEstimateDTO)
            } else {
                dialog.dismiss()
            }
        }
        // No button
        alert.setNegativeButton(
            R.string.cancel
        ) { dialog, _ ->
            // Do nothing but close dialog
            dialog.dismiss()
        }
        val declineAlert = alert.create()
        declineAlert.show()
    }

    private fun validateUpdateQty(
        activity: FragmentActivity,
        quantityEntry: EditText,
        totalEntry: TextView,
        jobItemEstimateDTO: JobItemEstimateDTO
    ) {
        if (activity.isConnected) {
            Coroutines.main {
                when {
                    quantityEntry.text.toString() == "" ||
                        nanCheck(quantityEntry.text.toString()) ||
                        quantityEntry.text.toString().toDouble() < 0.0 -> {
                        activity.extensionToast("Please Enter a valid Quantity", MotionToast.TOAST_WARNING)
                    }
                    else -> {
                        approveViewModel.updateState.observe(viewLifecycleOwner, updateObserver)
                        approveViewModel.upDateEstimate(
                            quantityEntry.text.toString(),
                            totalEntry.text.split(" ", ignoreCase = true)[1],
                            jobItemEstimateDTO.estimateId
                        )
                    }
                }
            }
        } else {
            activity.extensionToast("No connection detected.", MotionToast.TOAST_NO_INTERNET)
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

    override fun getLayout(): Int = R.layout.estimates_item

    private fun GroupieViewHolder.updateStartImage() {
        Coroutines.main {

            try {

                val startPhoto =
                    approveViewModel.getJobEstimationItemsPhotoStartPath(jobItemEstimateDTO.estimateId)

                GlideApp.with(this.containerView)
                    .load(Uri.fromFile(File(startPhoto)))
                    .centerCrop()
                    .error(R.drawable.no_image)
                    .placeholder(R.drawable.logo_new_medium)
                    .into(photoPreviewStart)

                photoPreviewStart.setOnClickListener {
                    showZoomedImage(startPhoto)
                }
            } catch (e: NullPointerException) {
                photoPreviewStart.setOnClickListener(null)
                Timber.d(e, "Start photo missing from job!")
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
                    .centerCrop()
                    .error(R.drawable.no_image)
                    .placeholder(R.drawable.logo_new_medium)
                    .into(photoPreviewEnd)

                photoPreviewEnd.setOnClickListener {
                    showZoomedImage(endPhoto)
                }
            } catch (e: NullPointerException) {
                photoPreviewEnd.setOnClickListener(null)
                Timber.d(e, "End photo missing from job!")
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }
}
