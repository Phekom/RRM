//package za.co.xisystems.itis_rrm.ui.mainview.create.edit_estimates
//
//import android.graphics.Bitmap
//import android.location.Location
//import android.os.Bundle
//import android.view.View
//import android.widget.EditText
//import android.widget.TextView
//import androidx.appcompat.app.AppCompatActivity
//import icepick.State
//import za.co.xisystems.itis_rrm.R
//import za.co.xisystems.itis_rrm.data._commons.AbstractTextWatcher
//import za.co.xisystems.itis_rrm.data._commons.Animations
//import za.co.xisystems.itis_rrm.data.localDB.entities.ItemDTO
//import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
//import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimateDTO
//import za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils.contracts.IEstimateCalculator
//import za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils.models.PhotoType
//import za.co.xisystems.itis_rrm.utils.toast
//import java.text.DecimalFormat
//import java.util.*
//
//class EstimatePhotoActivity : AppCompatActivity() {
//
//    var estimateCalculator: IEstimateCalculator? = null
//
//    @State
//    var itemId_photoType_tester =
//        HashMap<String, String>()
//    @State
//    var filename_path =
//        HashMap<String, String>()
//    @State
//    var photoType: PhotoType = PhotoType.start
//    @State
//    var item: ItemDTO? = null
//    var startBitmap: Bitmap? = null
//    var endBitmap:Bitmap? = null
//    @State
//    var job: JobDTO? = null
//    @State
//    var quantity = 1.0
//    @State
//    var currentLocation: Location? = null
//
//
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_estimate_photo)
//    }
//
//    estimateCalculator = EstimateCalculator(this)
//
//
//
//
//
//}
//
//
//internal class EstimateCalculator(activity: EstimatePhotoActivity) :
//    IEstimateCalculator {
//    private val activity: EstimatePhotoActivity
//    private val labelTextView: TextView
//    private val valueEditText: EditText
//    private val costTextView: TextView
//    var quantity: Double
//        get() = activity.quantity
//        set(quantity) {
//            activity.quantity = quantity
//        }
//
//    val item: ItemDTO
//        get() = activity.item!!
//
//    val job: JobDTO
//        get() = activity.getJob()
//
//    private fun toast(string: String) {
//        activity.toast(string)
//    }
//
//    fun onResume() {
//        setCost()
//    }
//
//    fun anims(): Animations {
//        return this.anims()
//    }
//
//    private fun setCost() {
//        if (isEstimateComplete) {
//            calculateCost()
//            valueEditText.visibility = View.VISIBLE
//            costTextView.visibility = View.VISIBLE
//            costTextView.startAnimation(anims().bounce_soft)
//        } else {
//            labelTextView.text = "Incomplete estimate..."
//            labelTextView.startAnimation(anims().shake_long)
//            valueEditText.visibility = View.GONE
//            costTextView.visibility = View.GONE
//        }
//    }
//
//    fun setValueEditText(qty: Double) {
//        when (uom) {
//            "m²", "m³", "m" -> valueEditText.setText("" + qty)
//            else -> valueEditText.setText("" + qty.toInt())
//        }
//    }
//
//    val jobItemEstimate: JobItemEstimateDTO?
//        get() = job.getJobEstimateByItemId(item.itemId)
//
//    private val isEstimateComplete: Boolean
//        private get() = jobItemEstimate != null && jobItemEstimate!!.isEstimateComplete()
//
//    val startKm: Double
//        get() {
//            val jobItemEstimate: JobItemEstimateDTO? = jobItemEstimate
//            return if (jobItemEstimate != null && jobItemEstimate.getJobItemEstimatePhotoStart() != null) jobItemEstimate.getJobItemEstimatePhotoStart()!!
//                .endKm else 0.0
//        }
//
//    val endKm: Double
//        get() {
//            val jobItemEstimate: JobItemEstimateDTO? = jobItemEstimate
//            return if (jobItemEstimate != null && jobItemEstimate.getJobItemEstimatePhotoEnd() != null) jobItemEstimate.getJobItemEstimatePhotoEnd()!!
//                .endKm else 0.0
//        }
//
//    private val storedValue: Double
//        private get() {
//            val jobItemEstimate: JobItemEstimateDTO? = jobItemEstimate
//            return if (jobItemEstimate == null) quantity else jobItemEstimate.qty
//        }
//
//    val uom: String
//        get() = item.uom!!
//
//    private fun calculateCost() {
//        val item: ItemDTO = item
//        val currentStartKm = startKm
//        val currentEndKm = endKm
//        val value = valueEditText.text.toString()
//        //  Lose focus on fields
//        valueEditText.clearFocus()
//        var lineRate = 0.0
//        var qty = 1.0
//        try {
//            qty = value.toDouble()
//        } catch (e: NumberFormatException) {
//            e.printStackTrace()
//        }
//        quantity = qty
//        when (uom) {
//            "No" -> {
//                labelTextView.text = "Quantity: "
//                try { //  make the change in the array and update view
//                    lineRate = value.toDouble() * item.tenderRate
//
//                } catch (e: NumberFormatException) {
//                    e.printStackTrace()
//                    toast("Please place the Quantity.")
//
//                }
//                labelTextView.text = "Area(m²): "
//                try { //  Set the Area to the QTY
//                    lineRate = value.toDouble() * item.tenderRate
//
//                } catch (e: NumberFormatException) {
//                    e.printStackTrace()
//                    toast("Please place the Area.")
//
//                }
//                labelTextView.text = "Volume(m³): "
//                try { //  Set the Area to the QTY
//                    lineRate = value.toDouble() * item.tenderRate
//
//                } catch (e: NumberFormatException) {
//                    e.printStackTrace()
//                    toast("Please place the Volume.")
//
//                }
//                labelTextView.text = "Amount: "
//                try { //  Set the Area to the QTY
//                    lineRate = value.toDouble() * item.tenderRate
//                } catch (e: NumberFormatException) {
//                    e.printStackTrace()
//                    toast("Please place the Prov Sum.")
//                }
//            }
//            "m²" -> {
//                labelTextView.text = "Area(m²): "
//                try {
//                    lineRate = value.toDouble() * item.tenderRate
//
//                } catch (e: NumberFormatException) {
//                    e.printStackTrace()
//                    toast("Please place the Area.")
//
//                }
//                labelTextView.text = "Volume(m³): "
//                try {
//                    lineRate = value.toDouble() * item.tenderRate
//
//                } catch (e: NumberFormatException) {
//                    e.printStackTrace()
//                    toast("Please place the Volume.")
//
//                }
//                labelTextView.text = "Amount: "
//                try {
//                    lineRate = value.toDouble() * item.tenderRate
//                } catch (e: NumberFormatException) {
//                    e.printStackTrace()
//                    toast("Please place the Prov Sum.")
//                }
//            }
//            "m³" -> {
//                labelTextView.text = "Volume(m³): "
//                try {
//                    lineRate = value.toDouble() * item.tenderRate
//
//                } catch (e: NumberFormatException) {
//                    e.printStackTrace()
//                    toast("Please place the Volume.")
//
//                }
//                labelTextView.text = "Amount: "
//                try {
//                    lineRate = value.toDouble() * item.tenderRate
//                } catch (e: NumberFormatException) {
//                    e.printStackTrace()
//                    toast("Please place the Prov Sum.")
//                }
//            }
//            "Prov Sum" -> {
//                labelTextView.text = "Amount: "
//                try {
//                    lineRate = value.toDouble() * item.tenderRate
//                } catch (e: NumberFormatException) {
//                    e.printStackTrace()
//                    toast("Please place the Prov Sum.")
//                }
//            }
//            "m" -> {
//                labelTextView.text = "Length(m): "
//                try { //  Set the Area to the QTY
//                    val length = currentEndKm - currentStartKm
//                    lineRate = length * item.tenderRate
//                } catch (e: NumberFormatException) {
//                    e.printStackTrace()
//                    toast("Please place the m.")
//                }
//            }
//            else -> {
//                labelTextView.text = "Quantity: "
//                try { //  Default Calculation
//                    lineRate = value.toDouble() * item.tenderRate
//
//                } catch (e: NumberFormatException) {
//                    e.printStackTrace()
//                    toast("Please place the Quantity.")
//
//                }
//            }
//        }
//        costTextView.text = "  *   R " + item.tenderRate.toString() + " =  R " + DecimalFormat(
//            "##.##"
//        ).format(lineRate)
//        val jobItemEstimate: JobItemEstimateDTO? = jobItemEstimate
//        if (jobItemEstimate != null) {
//            jobItemEstimate.qty
//            jobItemEstimate.lineRate
//        }
//    }
//
//    init {
//        this.activity = activity
//        labelTextView = activity.findViewById(R.id.labelTextView)
//        valueEditText = activity.findViewById(R.id.valueEditText)
//        costTextView = activity.findViewById(R.id.costTextView)
//        setValueEditText(storedValue)
//        valueEditText.addTextChangedListener(object : AbstractTextWatcher() {
//             override fun onTextChanged(text: String) {
//                setCost()
//            }
//        })
//    }
//}
//
