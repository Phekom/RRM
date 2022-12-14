/*
 * Updated by Shaun McDonald on 2021/01/25
 * Last modified on 2021/01/25 6:30 PM
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 */

package za.co.xisystems.itis_rrm.ui.mainview.estmeasure.submit_measure

import android.app.AlertDialog
import android.graphics.drawable.Animatable
import android.text.InputType
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.WindowManager
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.xwray.groupie.ExpandableGroup
import com.xwray.groupie.ExpandableItem
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.custom.notifications.ToastStyle
import za.co.xisystems.itis_rrm.data._commons.utils.SqlLitUtils
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimateDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemMeasureDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemMeasurePhotoDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.ProjectItemDTO
import za.co.xisystems.itis_rrm.databinding.ItemMeasureHeaderBinding
import za.co.xisystems.itis_rrm.extensions.observeOnce
import za.co.xisystems.itis_rrm.extensions.uomForUI
import za.co.xisystems.itis_rrm.ui.extensions.extensionToast
import za.co.xisystems.itis_rrm.ui.mainview.estmeasure.MeasureViewModel
import za.co.xisystems.itis_rrm.ui.mainview.estmeasure.estimate_measure_item.MeasureHeaderItem
import za.co.xisystems.itis_rrm.utils.Coroutines
import za.co.xisystems.itis_rrm.utils.DateUtil
import java.util.ArrayList
import java.util.Date

class ExpandableHeaderMeasureItem(
    private var fragment: Fragment,
    private val measureItem: JobItemEstimateDTO,
    measureViewModel: MeasureViewModel,
    private val jobItemMeasurePhotoDTOArrayList: ArrayList<JobItemMeasurePhotoDTO>,
    private val jobItemMeasureArrayList: ArrayList<JobItemMeasureDTO>

) : MeasureHeaderItem(null, measureItem, measureViewModel), ExpandableItem {

    private var clickListener: ((ExpandableHeaderMeasureItem) -> Unit)? = null
    private var navController: ((NavController) -> Unit)? = null
    private var projectItemIdz: String? = measureItem.projectItemId

    companion object {
        const val JOB_ITEM_MEASURE: String = "JobItemMeasure"
    }

    var onExpandListener: ((ExpandableGroup) -> Unit)? = null
    private lateinit var expandableGroup: ExpandableGroup

    override fun bind(viewBinding: ItemMeasureHeaderBinding, position: Int) {
        super.bind(viewBinding, position)

        viewBinding.apply {
            appListID.text = getItemId(position + 1).toString()
            icon.apply {
                visibility = View.VISIBLE
                setImageResource(if (expandableGroup.isExpanded) R.drawable.collapse else R.drawable.expand)
                setOnClickListener {
                    expandableGroup.onToggleExpanded()
                    onExpandListener?.invoke(expandableGroup)
                    bindIcon(viewBinding)
                }
            }
            headerLin.apply {
                setOnClickListener {
                    measureJobItemEstimate()
                    onExpandListener?.invoke(expandableGroup)
                    navController?.invoke(NavController(fragment.requireActivity()))
                }
            }
        }

        viewBinding.root.rootView.setOnClickListener {
            clickListener?.invoke(this)
        }
    }

    private fun measureJobItemEstimate() {
        Coroutines.main {
            val jobForMeasures = measureViewModel.getJobFromJobId(measureItem.jobId)
            jobForMeasures.observeOnce(fragment.viewLifecycleOwner, { job ->
                if (measureItem.jobId != null && job.jobId == measureItem.jobId!!) {
                    showAddMeasurementQuantityDialog(
                        measureItem,
                        job,
                        jobItemMeasurePhotoDTOArrayList
                    )
                }
            })
        }
    }

    private fun showAddMeasurementQuantityDialog(
        measureItem: JobItemEstimateDTO,
        jobForJobItemEstimate: JobDTO,
        jobItemMeasurePhotoDTO: ArrayList<JobItemMeasurePhotoDTO>
    ) {

        Coroutines.main {
            val quantityInputEditText = EditText(fragment.requireActivity())
            quantityInputEditText.inputType =
                InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL

            quantityInputEditText.setSingleLine()

            val selectedItemMeasure = measureViewModel.getItemForItemId(measureItem.projectItemId)
            selectedItemMeasure.observeOnce(fragment.requireActivity(), { selected ->

                if (selected != null) {
                    var message: String = fragment.requireActivity().getString(R.string.enter_quantity_measured)
                    quantityInputEditText.textAlignment = View.TEXT_ALIGNMENT_CENTER
                    if (!selected.uom.isNullOrBlank() &&
                        selected.uom != fragment.requireActivity().getString(R.string.none)
                    ) {
                        val friendlyUOM = fragment.requireActivity().uomForUI(selected.uom)
                        quantityInputEditText.hint = friendlyUOM

                        message += fragment.requireActivity().getString(R.string.badge, friendlyUOM)
                    }
                    Coroutines.main {
                        val desc = measureViewModel.getDescForProjectItemId(projectItemIdz!!)

                        val enterQuantityDialog: AlertDialog =
                            AlertDialog.Builder(fragment.requireActivity())
                                // android.R.style.Theme_DeviceDefault_Dialog
                                .setTitle(
                                    "Estimate - $desc${System.lineSeparator()}Quantity : " + measureItem.qty.toString()
                                        .dropLast(
                                            2
                                        )
                                )
                                .setMessage(message)
                                .setCancelable(false)
                                .setIcon(R.drawable.ic_border_)
                                .setView(quantityInputEditText)
                                .setPositiveButton(R.string.ok) { _, _ ->

                                    val measureQuantity =
                                        quantityInputEditText.text.toString().toDoubleOrNull() ?: 0.0
                                    when {
                                        measureQuantity <= 0.0 || measureQuantity.isNaN() -> {
                                            fragment.extensionToast(
                                                message = "Please Enter a valid Quantity",
                                                style = ToastStyle.WARNING
                                            )
                                        }
                                        quantityInputEditText.text.toString().length > 9 -> {
                                            fragment.extensionToast(
                                                message = "You have exceeded the quantity allowed",
                                                style = ToastStyle.WARNING
                                            )
                                        }
                                        else -> {
                                            updateMeasureQuantity(
                                                measureQuantity,
                                                selected,
                                                jobForJobItemEstimate,
                                                measureItem,
                                                jobItemMeasurePhotoDTO
                                            )
                                        }
                                    }
                                }
                                .setNegativeButton(
                                    R.string.cancel
                                ) { _, _ -> }.show()
                        quantityInputEditText.onFocusChangeListener =
                            OnFocusChangeListener { _, hasFocus ->
                                if (hasFocus) enterQuantityDialog.window?.setSoftInputMode(
                                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE
                                )
                            }
                        quantityInputEditText.requestFocus()
                    }
                }
            })
        }
    }

    private fun updateMeasureQuantity(
        measureQuantity: Double,
        selected: ProjectItemDTO?,
        jobForJobItemEstimate: JobDTO,
        measureItem: JobItemEstimateDTO,
        jobItemMeasurePhotoDTO: ArrayList<JobItemMeasurePhotoDTO>
    ) {
        createJobMeasureItem(selected, measureQuantity, jobForJobItemEstimate, measureItem, jobItemMeasurePhotoDTO)
    }

    private fun createJobMeasureItem(
        selected: ProjectItemDTO?,
        measureQuantity: Double,
        jobForJobItemEstimate: JobDTO,
        measureItem: JobItemEstimateDTO,
        jobItemMeasurePhotoDTO: ArrayList<JobItemMeasurePhotoDTO>
    ) {
        Coroutines.main {
            val jobItemMeasure = this@ExpandableHeaderMeasureItem.setJobItemMeasure(
                selected,
                measureQuantity,
                jobForJobItemEstimate,
                measureItem,
                jobItemMeasurePhotoDTO
            )
            jobItemMeasureArrayList.add(jobItemMeasure)
            captureItemMeasureImages(jobItemMeasure)
        }
    }

    private fun setJobItemMeasure(
        selectedItemToMeasure: ProjectItemDTO?,
        quantity: Double,
        jobForJobItemEstimate: JobDTO,
        selectedJobItemEstimate: JobItemEstimateDTO,
        jobItemMeasurePhotoDTO: ArrayList<JobItemMeasurePhotoDTO>
    ): JobItemMeasureDTO {
        val newItemMeasureId: String = SqlLitUtils.generateUuid()

        return JobItemMeasureDTO(
            id = 0,
            actId = 0,
            approvalDate = null,
            cpa = (jobForJobItemEstimate.cpa).toString().toInt(),
            endKm = (jobForJobItemEstimate.endKm).toString().toDouble(),
            estimateId = (selectedJobItemEstimate.estimateId),
            itemMeasureId = newItemMeasureId,
            jimNo = jobForJobItemEstimate.jiNo,
            jobDirectionId = (jobForJobItemEstimate.jobDirectionId).toString().toInt(),
            jobId = (jobForJobItemEstimate.jobId),
            lineAmount = (selectedJobItemEstimate.lineRate * quantity).toString().toDouble(),
            lineRate = (selectedJobItemEstimate.lineRate).toString().toDouble(),
            measureDate = DateUtil.dateToString(Date()).toString(),
            measureGroupId = null,
            jobItemMeasurePhotos = jobItemMeasurePhotoDTO,
            projectItemId = (selectedJobItemEstimate.projectItemId).toString(),
            projectVoId = jobForJobItemEstimate.projectVoId,
            qty = (quantity).toString().toDouble(),
            recordSynchStateId = 0,
            recordVersion = 0,
            startKm = (jobForJobItemEstimate.startKm).toString().toDouble(),
            trackRouteId = null,
            deleted = 0,
            entityDescription = null,
            selectedItemUom = (selectedItemToMeasure?.uom).toString()
        )
    }

    private fun captureItemMeasureImages(
        jobItemMeasure: JobItemMeasureDTO?
    ) {
        when {
            jobItemMeasure != null -> {
                measureViewModel.setJobItemMeasure(jobItemMeasure)
                fragment.findNavController()
                    .navigate(R.id.action_submitMeasureFragment_to_captureItemMeasurePhotoFragment)
            }
        }
    }

    private fun bindIcon(viewBinding: ItemMeasureHeaderBinding) {
        viewBinding.icon.apply {
            visibility = View.VISIBLE
            setImageResource(
                if (expandableGroup.isExpanded) {
                    R.drawable.collapse_animated
                } else {
                    R.drawable.expand_animated
                }
            )
            (drawable as Animatable).start()
        }
    }

    override fun setExpandableGroup(onToggleListener: ExpandableGroup) {
        this.expandableGroup = onToggleListener
    }

    private fun getItemId(position: Int): Long {
        return position.toLong()
    }
}
