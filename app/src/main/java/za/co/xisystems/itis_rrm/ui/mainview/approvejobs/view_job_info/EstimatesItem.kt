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
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.xwray.groupie.viewbinding.BindableItem
import timber.log.Timber
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.custom.notifications.ToastStyle
import za.co.xisystems.itis_rrm.custom.results.XIResult
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimateDTO
import za.co.xisystems.itis_rrm.databinding.EstimatesItemBinding
import za.co.xisystems.itis_rrm.extensions.uomForUI
import za.co.xisystems.itis_rrm.ui.extensions.extensionToast
import za.co.xisystems.itis_rrm.ui.mainview.approvejobs.ApproveJobsViewModel
import za.co.xisystems.itis_rrm.utils.Coroutines
import za.co.xisystems.itis_rrm.utils.GlideApp
import za.co.xisystems.itis_rrm.utils.ServiceUtil
import za.co.xisystems.itis_rrm.utils.Utils.nanCheck
import za.co.xisystems.itis_rrm.utils.Utils.round
import za.co.xisystems.itis_rrm.utils.zoomage.ZoomageView
import java.io.File

/**
 * Created by Francis Mahlava on 2020/01/02.
 */
class EstimatesItem(
    private val jobItemEstimateDTO: JobItemEstimateDTO,
    private val approveViewModel: ApproveJobsViewModel,
    private val activity: FragmentActivity?,
    private val viewLifecycleOwner: LifecycleOwner,
    private val updateObserver: Observer<XIResult<String>?>
) : BindableItem<EstimatesItemBinding>() {

    override fun initializeViewBinding(view: View): EstimatesItemBinding {
        return EstimatesItemBinding.bind(view)
    }

    /**
     * Perform any actions required to set up the view for display.
     *
     * @param viewBinding The ViewDataBinding to bind
     * @param position The adapter position
     */
    override fun bind(viewBinding: EstimatesItemBinding, position: Int) {
        viewBinding.apply {
            Coroutines.main {
                val estimate =
                    approveViewModel.getJobEstimationItemByEstimateId(jobItemEstimateDTO.estimateId)
                estimate.observe(viewLifecycleOwner, {
                    it?.let {
                        viewBinding.estimationItemPriceTextView.text =
                            activity?.getString(R.string.pair, "R", (it.qty * it.lineRate).round(2).toString())
                        viewBinding.estimationItemQuantityTextView.text =
                            activity?.getString(R.string.pair, "Qty:", it.qty.toString())
                    }
                })

                val descr =
                    approveViewModel.getDescForProjectItemId(jobItemEstimateDTO.projectItemId!!)
                val uom =
                    approveViewModel.getUOMForProjectItemId(jobItemEstimateDTO.projectItemId!!)
                viewBinding.measureItemDescriptionTextView.text = descr

                estimationItemUomTextView.text = if (uom == "NONE" || uom.isNullOrBlank()) {
                    activity?.getString(
                        R.string.pair,
                        jobItemEstimateDTO.lineRate.toString(),
                        "each"
                    )
                } else {
                    activity?.getString(
                        R.string.pair,
                        jobItemEstimateDTO.lineRate.toString(),
                        activity.uomForUI(uom)
                    )
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
        val rate = textEntryView.findViewById<View>(R.id.current_rate) as TextView
        val totalEntry = textEntryView.findViewById<View>(R.id.new_total) as TextView

        var updated = false
        val alert = AlertDialog.Builder(activity) // ,android.R.style.Theme_DeviceDefault_Dialog
        alert.setView(textEntryView)
        alert.setTitle(R.string.correct_estimate)
        alert.setIcon(R.drawable.ic_edit)
        alert.setMessage(R.string.are_you_sure_you_want_to_correct_esti)

        Coroutines.main {
            val tenderRate =
                approveViewModel.getTenderRateForProjectItemId(jobItemEstimateDTO.projectItemId!!)
            val total = (jobItemEstimateDTO.qty * jobItemEstimateDTO.lineRate)
            totalEntry.text = activity.getString(R.string.pair, "R", total.toString())

            rate.text = activity.getString(R.string.pair, "R", tenderRate.toString())
            var cost: Double
            val newQuantity = jobItemEstimateDTO.qty

            quantityEntry.text = Editable.Factory.getInstance().newEditable("$newQuantity")

            quantityEntry.doOnTextChanged { text, _, _, _ ->
                updated = true
                val input = text.toString()
                val newQty = validateDouble(
                    input,
                    editText = quantityEntry,
                    default = jobItemEstimateDTO.qty.toString(),
                    activity = activity
                )

                if (newQty != 0.0) {
                    cost = (tenderRate * newQty).round(2)
                    totalEntry.text = activity.getString(R.string.pair, "R", cost.toString())
                }
            }
        }

        // Yes button
        alert.setPositiveButton(
            R.string.save
        ) { dialog, _ ->
            if (updated) {
                Coroutines.io {
                    val tenderRate =
                        approveViewModel.getTenderRateForProjectItemId(jobItemEstimateDTO.projectItemId!!)

                    validateUpdateQty(activity, quantityEntry, tenderRate, jobItemEstimateDTO)
                }
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
        tenderRate: Double,
        jobItemEstimateDTO: JobItemEstimateDTO
    ) {
        if (ServiceUtil.isNetworkAvailable(activity.applicationContext)) {
            Coroutines.main {
                when {
                    quantityEntry.text.toString() == "" ||
                        nanCheck(quantityEntry.text.toString()) ||
                        quantityEntry.text.toString().toDouble() < 0.0 -> {
                        activity.extensionToast(
                            message = "Please Enter a valid Quantity",
                            style = ToastStyle.WARNING
                        )
                    }
                    else -> {
                        approveViewModel.updateState.observe(viewLifecycleOwner, updateObserver)
                        approveViewModel.upDateEstimate(
                            quantityEntry.text.toString(),
                            tenderRate.toString(),
                            jobItemEstimateDTO.estimateId
                        )
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

    private fun EstimatesItemBinding.updateStartImage() {
        Coroutines.main {

            try {

                val startPhoto =
                    approveViewModel.getJobEstimationItemsPhotoStartPath(jobItemEstimateDTO.estimateId)

                GlideApp.with(this.root)
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

    private fun EstimatesItemBinding.updateEndImage() {
        Coroutines.main {
            try {
                val endPhoto =
                    approveViewModel.getJobEstimationItemsPhotoEndPath(jobItemEstimateDTO.estimateId)

                GlideApp.with(this.root)
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

    private fun validateDouble(input: String, editText: EditText, default: String, activity: FragmentActivity): Double {
        return when {
            nanCheck(input) || input.toDouble() == 0.0 -> {
                0.0
            }
            input.length > 9 -> {
                editText.text =
                    Editable.Factory.getInstance().newEditable(default)
                activity.extensionToast(
                    message = "You Have exceeded the allowable maximum",
                    style = ToastStyle.WARNING
                )
                0.0
            }
            else -> {
                input.toDouble()
            }
        }
    }
}
