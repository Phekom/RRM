package za.co.xisystems.itis_rrm.ui.mainview.estmeasure.submit_measure

import android.app.AlertDialog
import android.content.DialogInterface
import android.graphics.drawable.Animatable
import android.text.InputType
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.WindowManager
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.xwray.groupie.ExpandableGroup
import com.xwray.groupie.ExpandableItem
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import kotlinx.android.synthetic.main.item_header.appListID
import kotlinx.android.synthetic.main.item_header.icon
import kotlinx.android.synthetic.main.item_measure_header.headerLin
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimateDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemMeasureDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemMeasurePhotoDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.ProjectItemDTO
import za.co.xisystems.itis_rrm.extensions.observeOnce
import za.co.xisystems.itis_rrm.ui.mainview.estmeasure.MeasureViewModel
import za.co.xisystems.itis_rrm.ui.mainview.estmeasure.estimate_measure_item.MeasureHeaderItem
import za.co.xisystems.itis_rrm.utils.Coroutines
import za.co.xisystems.itis_rrm.utils.DataConversion
import za.co.xisystems.itis_rrm.utils.SqlLitUtils
import java.util.ArrayList
import java.util.Date
import java.util.HashMap

class ExpandableHeaderMeasureItem(
    activity: FragmentActivity?,
    measureItem: JobItemEstimateDTO,
    measureViewModel: MeasureViewModel,
    private val jobItemMeasurePhotoDTOArrayList: ArrayList<JobItemMeasurePhotoDTO>,
    private val jobItemMeasureArrayList: ArrayList<JobItemMeasureDTO>,
    private var jobItemEstimatesForJob: ArrayList<JobItemEstimateDTO>,
    private var listDataChild: HashMap<JobItemEstimateDTO, List<JobItemMeasureDTO>>

) : MeasureHeaderItem(null, measureItem, measureViewModel), ExpandableItem {

    var clickListener: ((ExpandableHeaderMeasureItem) -> Unit)? = null
    var navController: ((NavController) -> Unit)? = null
    val measureItem: JobItemEstimateDTO = measureItem
    var projIDz: String? = measureItem.projectItemId

    private var jobItemEst: JobDTO? = null
    private var itemEs: JobItemEstimateDTO? = null

    companion object {
        const val JOB_IMEASURE: String = "JobItemMeasure"
        const val PHOTO_RESULT = 9000
    }

    var onExpandListener: ((ExpandableGroup) -> Unit)? = null
    private lateinit var expandableGroup: ExpandableGroup
    private var activity = activity
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        super.bind(viewHolder, position)

        viewHolder.apply {
            appListID.text = getItemId(position + 1).toString()
            icon.apply {
                visibility = View.VISIBLE
                setImageResource(if (expandableGroup.isExpanded) R.drawable.collapse else R.drawable.expand)
                setOnClickListener {
                    expandableGroup.onToggleExpanded()
                    onExpandListener?.invoke(expandableGroup)
                    bindIcon(viewHolder)
                }
            }
            headerLin.apply {
                setOnClickListener { view ->
                    measureJobItemEstimate(view)
                    onExpandListener?.invoke(expandableGroup)
                    navController?.invoke(NavController(activity!!))
                }
            }
        }

        viewHolder.itemView.setOnClickListener { view ->
            clickListener?.invoke(this)
        }
    }

    private fun measureJobItemEstimate(
        view: View
    ) {
        Coroutines.main {
            val jobForJobItemEstimate = measureViewModel.getJobFromJobId(measureItem.jobId)
            jobForJobItemEstimate.observeOnce(activity!!, androidx.lifecycle.Observer { job ->
                if (measureItem != null && jobForJobItemEstimate != null)
                    showAddMeasurementQuantityDialog(
                        measureItem,
                        job,
                        jobItemMeasurePhotoDTOArrayList,
                        view
                    )
            })
        }
    }

    private fun showAddMeasurementQuantityDialog(
        measureItem: JobItemEstimateDTO,
        jobForJobItemEstimate: JobDTO,
        jobItemMeasurePhotoDTO: ArrayList<JobItemMeasurePhotoDTO>,
        view: View
    ) {

        Coroutines.main {
            val quantityInputEditText = EditText(activity)
            quantityInputEditText.inputType =
                InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL

            quantityInputEditText.setSingleLine()

            val selectedItemMeasure = measureViewModel.getItemForItemId(measureItem.projectItemId)
            selectedItemMeasure.observeOnce(activity!!, androidx.lifecycle.Observer { selected ->

                if (selected != null) {
                    var message: String = activity!!.getString(R.string.enter_quantity_measured)
                    quantityInputEditText.textAlignment = View.TEXT_ALIGNMENT_CENTER
                    if (!selected.uom.equals(activity?.getString(R.string.none))) {
                        quantityInputEditText.hint = selected.uom
                        message += activity?.getString(R.string.parenthesis_open) + selected.uom + activity?.getString(
                            R.string.parenthesis_close
                        )
                    }
                    Coroutines.main {
                        val desc = measureViewModel.getDescForProjectItemId(projIDz!!)

                        val enterQuantityDialog: AlertDialog =
                            AlertDialog.Builder(activity) // android.R.style.Theme_DeviceDefault_Dialog
                                .setTitle(
                                    "Estimate - " + desc + System.lineSeparator() + "Quantity : " + measureItem.qty.toString()
                                        .dropLast(
                                            2
                                        )
                                )
                                .setMessage(message)
                                .setCancelable(false)
                                .setIcon(R.drawable.ic_border_)
                                .setView(quantityInputEditText)
                                .setPositiveButton(R.string.ok,
                                    DialogInterface.OnClickListener { dialog, whichButton ->
                                        updateMeasureQuantity(
                                            quantityInputEditText,
                                            selected,
                                            jobForJobItemEstimate,
                                            measureItem,
                                            jobItemMeasurePhotoDTO,
                                            view
                                        )
                                    })

                                .setNegativeButton(R.string.cancel,
                                    DialogInterface.OnClickListener { dialog, which -> }).show()
                        quantityInputEditText.onFocusChangeListener =
                            OnFocusChangeListener { v, hasFocus ->
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
        quantityInputEditText: EditText,
        selected: ProjectItemDTO?,
        jobForJobItemEstimate: JobDTO,
        measureItem: JobItemEstimateDTO,
        jobItemMeasurePhotoDTO: ArrayList<JobItemMeasurePhotoDTO>,
        view: View
    ) {
        if (quantityInputEditText.text.toString() == "") {
            Toast.makeText(
                activity?.applicationContext,
                activity?.getString(R.string.place_quantity),
                Toast.LENGTH_LONG
            ).show()
        } else {
            if (quantityInputEditText.text.toString()
                    .isNotEmpty()
            ) {
                Coroutines.main {
                    val jobItemMeasure = setJobItemMeasure(
                        selected,
                        quantityInputEditText.text.toString()
                            .toDouble(),
                        jobForJobItemEstimate,
                        measureItem,
                        jobItemMeasurePhotoDTO
                    )
                    jobItemMeasureArrayList.add(jobItemMeasure)
                    captureItemMeasureImages(jobItemMeasure, view)
                }
            }
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
        val itemMeasure = JobItemMeasureDTO(
            ID = 0,
            actId = 0,
            approvalDate = null,
            cpa = (jobForJobItemEstimate.Cpa).toString().toInt(),
            endKm = (jobForJobItemEstimate.EndKm).toString().toDouble(),
            estimateId = (selectedJobItemEstimate.estimateId),
            itemMeasureId = (DataConversion.toBigEndian(newItemMeasureId)).toString(),
            jimNo = jobForJobItemEstimate.JiNo,
            jobDirectionId = (jobForJobItemEstimate.JobDirectionId).toString().toInt(),
            jobId = (jobForJobItemEstimate.JobId),
            lineAmount = (selectedJobItemEstimate.lineRate * quantity).toString().toDouble(),
            lineRate = (selectedJobItemEstimate.lineRate).toString().toDouble(),
            measureDate = (Date()).toString(),
            measureGroupId = null,
            jobItemMeasurePhotos = jobItemMeasurePhotoDTO,
            job = jobItemEst,
            jobItemEstimate = itemEs,
            projectItemId = (selectedJobItemEstimate.projectItemId).toString(),
            projectVoId = jobForJobItemEstimate.VoId,
            qty = (quantity).toString().toDouble(),
            recordSynchStateId = 0,
            recordVersion = 0,
            startKm = (jobForJobItemEstimate.StartKm).toString().toDouble(),
            trackRouteId = null,
            deleted = 0,
            entityDescription = null,
            selectedItemUom = (selectedItemToMeasure?.uom).toString()
        )

        return itemMeasure
    }

    private fun captureItemMeasureImages(
        jobItemMeasure: JobItemMeasureDTO?,
        view: View
    ) {
        when {
            jobItemMeasure != null -> {
                measureViewModel.setJobItemMeasure(jobItemMeasure)
                Navigation.findNavController(view)
                    .navigate(R.id.action_submitMeasureFragment_to_captureItemMeasurePhotoFragment)
            }
        }
    }

    private fun bindIcon(viewHolder: GroupieViewHolder) {
        viewHolder.icon.apply {
            visibility = View.VISIBLE
            setImageResource(if (expandableGroup.isExpanded) R.drawable.collapse_animated else R.drawable.expand_animated)
            (drawable as Animatable).start()
        }
    }

    override fun setExpandableGroup(onToggleListener: ExpandableGroup) {
        this.expandableGroup = onToggleListener
    }
}

private fun setJobItemMeasures(
    jobItemMeasures: ArrayList<JobItemMeasureDTO>,
    measureViewModel: MeasureViewModel
) {
    Coroutines.main {
        measureViewModel.saveJobItemMeasureItems(jobItemMeasures)
    }
}

private fun GroupieViewHolder.getItemId(position: Int): Long {
    return position.toLong()
}
