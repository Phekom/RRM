package za.co.xisystems.itis_rrm.ui.mainview.estmeasure.submit_measure


import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.drawable.Animatable
import android.text.InputType
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.WindowManager
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import com.xwray.groupie.ExpandableGroup
import com.xwray.groupie.ExpandableItem
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import kotlinx.android.synthetic.main.item_header.appListID
import kotlinx.android.synthetic.main.item_header.icon
import kotlinx.android.synthetic.main.item_measure_header.*
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.data.localDB.entities.*
import za.co.xisystems.itis_rrm.ui.mainview.estmeasure.MeasureViewModel
import za.co.xisystems.itis_rrm.ui.mainview.estmeasure.estimate_measure_item.Measure_HeaderItem
import za.co.xisystems.itis_rrm.utils.Coroutines
import za.co.xisystems.itis_rrm.utils.DataConversion
import za.co.xisystems.itis_rrm.utils.SqlLitUtils
import java.util.*


class ExpandableHeaderMeasureItem(
    activity: FragmentActivity?,
    measureItem: JobItemEstimateDTO,
    measureViewModel: MeasureViewModel,
    private val jobItemMeasurePhotoDTOArrayList: ArrayList<JobItemMeasurePhotoDTOTemp>,
    private val jobItemMeasureArrayList: ArrayList<JobItemMeasureDTOTemp>,
    private var jobItemEstimatesForJob: ArrayList<JobItemEstimateDTO>,
    private var listDataChild: HashMap<JobItemEstimateDTO, List<JobItemMeasureDTO>>

    ) : Measure_HeaderItem(null, measureItem, measureViewModel), ExpandableItem {


    var clickListener: ((ExpandableHeaderMeasureItem) -> Unit)? = null
    var navController: ((NavController) -> Unit)? = null
    val measureItem = measureItem
    var projIDz = measureItem.projectItemId

        companion object {
        const val JOB_IMEASURE = "JobItemMeasure"
        const val PHOTO_RESULT = 9000

    }



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
                    bindIcon(viewHolder)
                }
            }
            headerLin.apply {
                setOnClickListener {view ->
                measureJobItemEstimate(view)
//                   setupWithNavController(bnv, navController);


                    //                clickListener?.invoke(LinearLayout.this)
                navController?.invoke(NavController(activity!!))

            }
            }

        }


        viewHolder.itemView.setOnClickListener {view ->
            clickListener?.invoke(this)

        }




    }


    private fun measureJobItemEstimate(
        view: View
    ) {
        Coroutines.main {
            val jobForJobItemEstimate = measureViewModel.getJobFromJobId(measureItem.jobId)
            jobForJobItemEstimate.observe(activity!!, androidx.lifecycle.Observer { job->
                if (measureItem != null && jobForJobItemEstimate != null)
                    showAddMeasurementQuantityDialog(measureItem, job,jobItemMeasurePhotoDTOArrayList,jobItemMeasureArrayList, view)
            })
        }
    }

    private fun showAddMeasurementQuantityDialog(
        measureItem: JobItemEstimateDTO,
        jobForJobItemEstimate: JobDTO,
        jobItemMeasurePhotoDTO: ArrayList<JobItemMeasurePhotoDTOTemp>,
        jobItemMeasureArrayList: ArrayList<JobItemMeasureDTOTemp>,
        view: View
    ) {

        Coroutines.main {
            val quantityInputEditText = EditText(activity)
            quantityInputEditText.inputType =
                InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
//            quantityInputEditText.setTextColor(
//                ColorUtil.getTextColor(activity?.applicationContext,
//                    R.color.itis_gray
//                )
//            )
            quantityInputEditText.setSingleLine()

            val selectedItemMeasure = measureViewModel.getItemForItemId(measureItem.projectItemId)
            selectedItemMeasure.observe(activity!!, androidx.lifecycle.Observer { selected ->

                if (selected != null) {
                    var message: String = activity!!.getString(R.string.enter_quantity_measured)
                    quantityInputEditText.textAlignment = View.TEXT_ALIGNMENT_CENTER
                    if (!selected.uom.equals(activity?.getString(R.string.none))) {
                        quantityInputEditText.setHint(selected.uom)
                        message += activity?.getString(R.string.parenthesis_open) + selected.uom + activity?.getString(
                            R.string.parenthesis_close
                        )
                    }
                    Coroutines.main {
                        val desc = measureViewModel.getDescForProjectItemId(projIDz!!)

                        val enterQuantityDialog: AlertDialog =
                            AlertDialog.Builder(activity)//android.R.style.Theme_DeviceDefault_Dialog
                                .setTitle(
                                    "Estimate - " + desc + System.lineSeparator() + "Quantity : " + measureItem.qty.toString().dropLast(
                                        2
                                    )
                                )
                                .setMessage(message)
                                .setCancelable(false)
                                .setIcon(R.drawable.ic_border_)
                                .setView(quantityInputEditText)
                                .setPositiveButton(R.string.ok,
                                    DialogInterface.OnClickListener { dialog, whichButton ->
                                        if (quantityInputEditText.text.toString() == "") { Toast.makeText( activity?.getApplicationContext(),
                                                activity?.getString(R.string.place_quantity), Toast.LENGTH_LONG ).show()
                                        } else {
                                            if (!quantityInputEditText.text.toString().isEmpty()) {
                                                Coroutines.main {
                                                val jobItemMeasure = setJobItemMeasure(selected, quantityInputEditText.text.toString().toDouble(), jobForJobItemEstimate, measureItem, jobItemMeasurePhotoDTO,jobItemMeasureArrayList)
//                                                    setJobItemMeasures(jobItemMeasureArrayList,measureViewModel)
//                                                   measureViewModel.createJobItemMeasureItem(selected, quantityInputEditText.text.toString().toDouble(), jobForJobItemEstimate, measureItem, jobItemMeasurePhotoDTO)
//                                                    measureViewModel.createJobItemMeasureItem(jobItemMeasure)
                                                    captureItemMeasureImages(jobItemMeasure)

                                                }
                                            }
                                        }
                                    })

                                .setNegativeButton(R.string.cancel,
                                    DialogInterface.OnClickListener { dialog, which -> }).show()
                        quantityInputEditText.onFocusChangeListener =
                            OnFocusChangeListener { v, hasFocus ->
                                if (hasFocus) enterQuantityDialog.window.setSoftInputMode(
                                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE
                                )
                            }
                        quantityInputEditText.requestFocus()
                    }
                }

            })

        }

    }

    private fun setJobItemMeasure(
        selectedItemToMeasure: ProjectItemDTO?,
        quantity: Double,
        jobForJobItemEstimate: JobDTO,
        selectedJobItemEstimate: JobItemEstimateDTO,
        jobItemMeasurePhotoDTO: ArrayList<JobItemMeasurePhotoDTOTemp>,
        jobItemMeasureDTO: ArrayList<JobItemMeasureDTOTemp>
    ): JobItemMeasureDTOTemp {
        val newItemMeasureId: String = SqlLitUtils.generateUuid()
        val itemMeasure = JobItemMeasureDTOTemp(
             0,
            0,
            null,
            (jobForJobItemEstimate.Cpa).toString().toInt(),
            (jobForJobItemEstimate?.EndKm!!).toString().toDouble(),
            (selectedJobItemEstimate.estimateId),
            (DataConversion.toBigEndian(newItemMeasureId)).toString(),
            jobForJobItemEstimate.JiNo,
            (jobForJobItemEstimate.JobDirectionId).toString().toInt(),
            (jobForJobItemEstimate?.JobId),
            (selectedJobItemEstimate.lineRate * quantity).toString().toDouble(),
            (selectedJobItemEstimate.lineRate).toString().toDouble(),
            (Date()).toString(),
            null,
            jobItemMeasurePhotoDTO,
            jobForJobItemEstimate,
            selectedJobItemEstimate,
            (selectedJobItemEstimate.projectItemId).toString(),
            jobForJobItemEstimate.VoId.toString(),
            (quantity).toString().toDouble(),
            0,
            0,
            (jobForJobItemEstimate.StartKm).toString().toDouble(),
            null,
            null,
            (selectedItemToMeasure?.uom).toString() )

        this.jobItemMeasureArrayList.add(itemMeasure)
//        setJobItemMeasures( jobItemMeasureArrayList,measureViewModel )

        return itemMeasure!!
    }

    private fun captureItemMeasureImages(jobItemMeasu: JobItemMeasureDTOTemp) {
        Coroutines.main {
//            val jobItemMeasure = measureViewModel.getJobItemMeasureForJobId(measureItem.jobId)
//            jobItemMeasure.observe(activity!!, androidx.lifecycle.Observer { jItemMeasure ->
//                Toast.makeText(activity,jItemMeasure.itemMeasureId.toString(),Toast.LENGTH_SHORT).show()
                captureItemMeasurePhoto(jobItemMeasu)
//                Navigation.findNavController(view).navigate(R.id.action_submitMeasureFragment_to_captureItemMeasurePhotoFragment)

//            })

        }
    }

    private fun captureItemMeasurePhoto(
        jobItemMeasure: JobItemMeasureDTOTemp?
    ) {
        val jobItemMeasure = jobItemMeasure
        measureViewModel.measurea1_Item1.value = jobItemMeasure
//        Navigation.findNavController(view).navigate(R.id.action_submitMeasureFragment_to_captureItemMeasurePhotoFragment)

        val intent = Intent()
        intent.setClass(activity, CaptureItemMeasurePhotoActivity::class.java)
        intent.putExtra(JOB_IMEASURE, jobItemMeasure)
        activity!!.startActivity(intent)

//        val intent = Intent()
//        intent.setClass(activity, CaptureItemMeasurePhotoActivity::class.java)
////        val bundle = Bundle()
////        bundle.putSerializable("JobItemMeasure", jobItemMeasure)
////        intent.putExtras(bundle)
//        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
//        activity!!.startActivity(intent)



    }



    private fun bindIcon(viewHolder: GroupieViewHolder) {
        viewHolder.icon.apply {
            visibility = View.VISIBLE
            setImageResource(if (expandableGroup!!.isExpanded) R.drawable.collapse_animated else R.drawable.expand_animated)
            (drawable as Animatable).start()
        }
    }

    override fun setExpandableGroup(onToggleListener: ExpandableGroup) {
        this.expandableGroup = onToggleListener
    }
}



private fun setJobItemMeasures(
    jobItemMeasures: ArrayList<JobItemMeasureDTOTemp>,
    measureViewModel: MeasureViewModel
) {
    Coroutines.main {
        measureViewModel.saveJobItemMeasureItems(jobItemMeasures)
    }

//    this.JobItemMeasures = jobItemMeasures
}

private fun GroupieViewHolder.getItemId(position: Int): Long {
    return position.toLong()
}
