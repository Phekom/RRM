package za.co.xisystems.itis_rrm.ui.mainview.create.edit_estimates

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import icepick.State
import kotlinx.android.synthetic.main.fragment_photo_estimate.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import za.co.xisystems.itis_rrm.BuildConfig
import za.co.xisystems.itis_rrm.MainActivity
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.data._commons.AbstractTextWatcher
import za.co.xisystems.itis_rrm.data.localDB.entities.*
import za.co.xisystems.itis_rrm.ui.mainview._fragments.BaseFragment
import za.co.xisystems.itis_rrm.ui.mainview.create.CreateViewModel
import za.co.xisystems.itis_rrm.ui.mainview.create.CreateViewModelFactory
import za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils.LocationHelper
import za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils.MyState
import za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils.intents.AbstractIntent
import za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils.models.PhotoType
import za.co.xisystems.itis_rrm.utils.*
import za.co.xisystems.itis_rrm.utils.zoomage.ZoomageView
import java.text.DecimalFormat
import java.util.*
import kotlin.collections.HashMap


/**
 * Created by Francis Mahlava on 2019/12/29.
 */

class EstimatePhotoFragment : BaseFragment(), KodeinAware {


    override val kodein by kodein()
    private lateinit var createViewModel: CreateViewModel
    private val factory: CreateViewModelFactory by instance()

    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_STORAGE_PERMISSION = 1
    private val FILE_PROVIDER_AUTHORITY =  BuildConfig.APPLICATION_ID + ".provider"
    private var mAppExcutor: AppExecutor? = null
    lateinit var locationHelper: LocationHelper
//    lateinit var newJobEditEstimateHelper: NewJobEditEstimateHelper
    internal var myjob: JobDTO? = null

    var contractID : String? = null
    var projectID : String? = null

    @State
    var photoType: PhotoType = PhotoType.start
    @State
    var itemId_photoType = HashMap<String, String>()

//    @State
//    var itemId_photoType_tester = HashMap<String, String>()

    @State
    var filename_path = HashMap<String, String>()
    @State
    private var item: ItemDTOTemp? = null
    @MyState
    internal var job: JobDTO? = null

    @State
    var quantity = 1.0

    private var currentLocation: Location? = null

    private lateinit var jobArrayList: ArrayList<JobDTO>

    private lateinit var newJobItemEstimatesPhotosList: ArrayList<JobItemEstimatesPhotoDTO>
    private lateinit var newJobItemEstimatesWorksList: ArrayList<JobEstimateWorksDTO>
    private lateinit var newJobItemEstimatesList: ArrayList<JobItemEstimateDTO>
    private lateinit var jobItemMeasureArrayList: ArrayList<JobItemMeasureDTO>
    private lateinit var jobItemSectionArrayList: ArrayList<JobSectionDTO>
    private lateinit var  itemSections : ArrayList<ItemSectionDTO>
    private  var jobItemPhoto : JobItemEstimatesPhotoDTO? = null

    private lateinit var newJobItemEstimatesPhotosList2: ArrayList<JobItemEstimatesPhotoDTO>
    private lateinit var newJobItemEstimatesWorksList2: ArrayList<JobEstimateWorksDTO>
    private lateinit var newJobItemEstimatesList2: ArrayList<JobItemEstimateDTO>
    private lateinit var jobItemMeasureArrayList2: ArrayList<JobItemMeasureDTO>
    private lateinit var jobItemSectionArrayList2: ArrayList<JobSectionDTO>
    private lateinit var  itemSections2 : ArrayList<ItemSectionDTO>

    internal var useR: Int? = null
    private var startimageUri: Uri? = null
    private  var endimageUri: Uri? = null
    private  var imageUri: Uri? = null

     override fun onStart() {
        super.onStart()
        locationHelper.onStart()
    }

     override fun onPause() {
        super.onPause()
        locationHelper.onPause()
    }

     override fun onStop() {
        super.onStop()
        locationHelper.onStop()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_photo_estimate, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (activity as MainActivity).supportActionBar?.title = getString(R.string.edit_estimate)
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity as MainActivity).supportActionBar?.title = getString(R.string.edit_estimate)
//        updatePhotoUI(false)
//        updateSectionUI(false)
//        updatePhotoUI()
//        estimateCalculator =  EstimateCalculator(this)
        locationHelper = LocationHelper(this)
        locationHelper.onCreate()
        itemSections = ArrayList<ItemSectionDTO>()
        jobArrayList = ArrayList<JobDTO>()
//        jobItemPhoto = JobItemEstimatesPhotoDTO
        jobItemSectionArrayList = ArrayList<JobSectionDTO>()
        jobItemMeasureArrayList = ArrayList<JobItemMeasureDTO>()
        newJobItemEstimatesList = ArrayList<JobItemEstimateDTO>()
        newJobItemEstimatesPhotosList = ArrayList<JobItemEstimatesPhotoDTO>()
        newJobItemEstimatesWorksList = ArrayList<JobEstimateWorksDTO>()
//        newJobItemEstimatesList2 = ArrayList<JobItemEstimateDTO>()


        jobItemSectionArrayList2 = ArrayList<JobSectionDTO>()
        jobItemMeasureArrayList2 = ArrayList<JobItemMeasureDTO>()
        newJobItemEstimatesList2 = ArrayList<JobItemEstimateDTO>()
        newJobItemEstimatesPhotosList2 = ArrayList<JobItemEstimatesPhotoDTO>()
        newJobItemEstimatesWorksList2 = ArrayList<JobEstimateWorksDTO>()

    }



    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        createViewModel = activity?.run {
            ViewModelProviders.of(this, factory).get(CreateViewModel::class.java)
        } ?: throw Exception("Invalid Activity")

        mAppExcutor = AppExecutor()

        createViewModel.loggedUser.observe(viewLifecycleOwner, Observer { user ->
            useR = user
//            selectedContractTextView.text = user
        })
        createViewModel.job_Item.observe(viewLifecycleOwner, Observer { job_Item ->
            job = job_Item
        })
        createViewModel.contract_ID.observe(viewLifecycleOwner, Observer { contrct_id ->
            toast(contrct_id)
            contractID = contrct_id
        })
        createViewModel.project_ID.observe(viewLifecycleOwner, Observer { pro_id ->
            toast(pro_id)
            projectID = pro_id
        })
        createViewModel.project_Item.observe(viewLifecycleOwner, Observer { pro_Item ->
            item = pro_Item
            if (item != null) titleTextView.setText(item!!.itemCode + " " + item!!.descr) else toast(
                "item is null in " + javaClass.simpleName
            )

            setButtonClicks()

        })

        startImageView.setOnClickListener {
            showZoomedImage(startimageUri)
        }
        endImageView.setOnClickListener {
            showZoomedImage(endimageUri)
        }








        setValueEditText(getStoredValue())
        valueEditText!!.addTextChangedListener(object : AbstractTextWatcher() {
            override fun onTextChanged(text: String) {
                setCost()
            }
        })

    }

    private fun setButtonClicks() {

        val myClickListener = View.OnClickListener { view ->
            when (view?.id) {

                R.id.startPhotoButton -> {
                    takePhotoStart()
                }
                R.id.endPhotoButton -> {
                    takePhotoEnd()
                }

                R.id.cancelButton -> {

                }

                R.id.updateButton -> {
                 if( costTextView.text.isNullOrEmpty()){
                     labelTextView.startAnimation(anims!!.shake_long)
                 }else{
                     toast("things will work now")
                 }
                }

            }
        }

        startPhotoButton.setOnClickListener(myClickListener)
        endPhotoButton.setOnClickListener(myClickListener)
        cancelButton.setOnClickListener(myClickListener)
        updateButton.setOnClickListener(myClickListener)
//        updateButton.setOnClickListener(myClickListener)

    }

    private fun takePhotoStart() {
        photoType = PhotoType.start
        // Check for the external storage permission
        if (ContextCompat.checkSelfPermission(
                activity!!.applicationContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            !== PackageManager.PERMISSION_GRANTED
        ) { // If you do not have permission, request it
            ActivityCompat.requestPermissions(
                Activity(), arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_STORAGE_PERMISSION
            )
        } else { // Launch the camera if the permission exists
            launchCamera()
        }
    }

    private fun takePhotoEnd() {
        photoType = PhotoType.end
        // Check for the external storage permission
        if (ContextCompat.checkSelfPermission(
                activity!!.applicationContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            !== PackageManager.PERMISSION_GRANTED
        ) { // If you do not have permission, request it
            ActivityCompat.requestPermissions(
                Activity(), arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_STORAGE_PERMISSION
            )
        } else { // Launch the camera if the permission exists
            launchCamera()
        }

    }

    private fun launchCamera() {
        // type is "start" or "end"
        if (item != null) {
            itemId_photoType["itemId"] = item!!.itemId
            itemId_photoType["type"] = photoType.name
        }



        imageUri = PhotoUtil.getUri(this)
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(activity?.packageManager) != null) {
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
            takePictureIntent.putExtra(
                MediaStore.EXTRA_SCREEN_ORIENTATION,
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            )
            startActivityForResult(takePictureIntent, AbstractIntent.REQUEST_TAKE_PHOTO)
        }
    }


    override fun onSaveInstanceState(outState: Bundle) {
//        outState.putSerializable("itemId_photoType_tester", itemId_photoType_tester)
        outState.putSerializable("filename_path", filename_path)
        outState.putSerializable("photoType", photoType)
        outState.putSerializable("item", item)
//        outState.putSerializable("job", job)
//        outState.putDouble("quantity", quantity)
//        outState.putParcelable("currentLocation", currentLocation)
        super.onSaveInstanceState(outState)
    }

    fun onRestoreInstanceState(inState: Bundle) {
//        super.onRestoreInstanceState(inState)
//        itemId_photoType_tester =
//            inState.getSerializable("itemId_photoType_tester") as java.util.HashMap<String?, String?>
        filename_path =
            inState.getSerializable("filename_path") as HashMap<String, String>
        photoType = inState.getSerializable("photoType") as PhotoType
//        item = inState.getSerializable("item") as ItemDTO
//        job = inState.getSerializable("job") as JobDTO
//        quantity = inState.getDouble("quantity")
//        currentLocation =
//            inState.getParcelable<Parcelable>("currentLocation") as Location
        loadPhotos()
//        updatePhotoUI(true)
//        updateSectionUI(true)
//        if (currentLocation != null) {
//            Log.d("x-long", "" + currentLocation.getLongitude())
//            Log.d("x-lat", "" + currentLocation.getLatitude())
//        } else {
//            Log.d("x-", "[ currentLocation is null ]")
//        }
    }

    private fun loadPhotos() {
//        val jobItemEstimate: JobItemEstimate = getJob().getJobEstimateByItemId(item.getItemId())
//        if (jobItemEstimate == null) {
//            Log.d(
//                "x-",
//                "Error: JobItemEstimate not find in " + javaClass.simpleName
//            )
//        } else if (startimageUri == null || endimageUri == null) {
//            startimageUri = extractImageUri(jobItemEstimate.getJobItemEstimatePhotoStart())
//            endimageUri = extractImageUri(jobItemEstimate.getJobItemEstimatePhotoEnd())
//        }
    }


    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) { // If the image capture activity was called and was successful
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) { // Process the image and set it to the TextView
            processAndSetImage()
        } else { // Otherwise, delete the temporary image file
            BitmapUtils.deleteImageFile(context!!, filename_path.toString())
        }
    }

    @SuppressLint("RestrictedApi")
    private fun processAndSetImage() {

                    when (photoType) {
                        PhotoType.start -> updatePhotos(startImageView, imageUri.also { startimageUri = it }, true)
                        PhotoType.end -> updatePhotos(endImageView!!, imageUri.also { endimageUri = it }, true)

                    }
                    try { //  Location of picture
                        val currentLocation: Location = locationHelper?.getCurrentLocation()!!
                        if (currentLocation == null) toast("Error: Current location is null!")
                        //  Save Image to Internal Storage

                        filename_path = PhotoUtil.saveImageToInternalStorage(activity!!, imageUri!!) as HashMap<String, String>



//  Check to see if Tester
//                        if (itemId_photoType != null && itemId_photoType["tester"] == "true") {
//                            newJobEditEstimateHelper!!.isTester = true
//                        } else

//                        if (itemId_photoType != null )
//                        newJobEditEstimateHelper?.isTester = false

                        processPhotoEstimate(currentLocation, filename_path, itemId_photoType)

//                            newJobEditEstimateHelper?.processPhotoEstimate(
//                                currentLocation,
//                                filename_path,
//                                itemId_photoType_tester
//                            )
                    } catch (e: Exception) {
                        toast(R.string.error_getting_image)
                        e.printStackTrace()
                    }



        }

    private fun processPhotoEstimate(
        currentLocation: Location,
        filename_path: Map<String, String>,
        itemId_photoType: Map<String, String>
    ) {
        var isEstimateDone : Boolean = false
        val isPhotoStart = itemId_photoType["type"] == "start"
        val itemId = itemId_photoType["itemId"]
        val photoPath =  filename_path

        val newjob = createNewJob(itemId, jobArrayList, useR, newJobItemEstimatesList, jobItemMeasureArrayList, jobItemSectionArrayList)
        // create job estimate
        val jobItemEstimate = newjob.getJobEstimateByItemId(itemId)

        if (jobItemEstimate == null) {

            val itemEstimate = createItemEstimate(itemId, newjob, newJobItemEstimatesPhotosList, jobItemMeasureArrayList, newJobItemEstimatesWorksList,newJobItemEstimatesList)
            val jobItemEstimatePhoto = if (isPhotoStart) itemEstimate.getJobItemEstimatePhotoStart() else itemEstimate.getJobItemEstimatePhotoEnd()
            if (jobItemEstimatePhoto == null) {

//                val photoId: String = SqlLitUtils.generateUuid()
//                val isPhotoStart = itemId_photoType.get("type") == "start"
                if (currentLocation == null) {
                    activity!!.toast("Please make sure that you have activated the location on your device.")
                } else {

                    val photo =      createItemEstimatePhoto(itemEstimate, photoPath, currentLocation,newJobItemEstimatesPhotosList,itemId_photoType)


                    getRouteSectionPoint( currentLocation, itemEstimate,  photo, itemId_photoType,  newjob  )
                }


            }


        }else{
                if (currentLocation == null) {
                    activity!!.toast("Please make sure that you have activated the location on your device.")
                } else {
                    isEstimateDone = true
               val photo =   createItemEstimatePhoto(
                        jobItemEstimate,
                        photoPath,
                        currentLocation,
                        newJobItemEstimatesPhotosList,
                        itemId_photoType
                    )

//                    getRouteSectionPoint( currentLocation, itemEstimate,  photo, itemId_photoType,  newjob  )
                }

        }

        Coroutines.main{
            if (isEstimateDone){
                val newJobId: String = SqlLitUtils.generateUuid()
              val thisis =  String.format(Locale.US, "[Section] projectId=%s, sectionId=%s, route=%s, section=%s, direction=%s", projectID, contractID, "", "", "")


            val newjob2 =    JobDTOTemp(

                0, newJobId, contractID,  projectID, null,0.0,0.0,null,null,useR!!,null,null,
                0,0,0,0,null,null,null,null,newJobItemEstimatesList2,
                jobItemMeasureArrayList,jobItemSectionArrayList,null,0,null,null,null,
                    0,0,0,0,0,0,0,null,0,
                0,null,null,null

                )
            createViewModel.saveNewJob(newjob2)}
        }
}

    private fun getRouteSectionPoint(currentLocation: Location, itemEstimate: JobItemEstimateDTO, jobItemPhoto: JobItemEstimatesPhotoDTO, itemidPhototype: Map<String, String>, job: JobDTO) {
        Coroutines.main {
                            createViewModel.getRouteSectionPoint(currentLocation.latitude, currentLocation.longitude, useR.toString(),job.ProjectId )
//            createRouteSection(
//                response,
//                longitude,
//                longitude,
//                jobItemEstimate,
//                jobItemEstimatePhoto,
//                itemId_photoType_tester,
//                type
//            )
        }
    }


    private fun createNewJob(
        itemId: String?,
        jobArrayList: ArrayList<JobDTO>,
        useR: Int?,
        newJobItemEstimatesList: ArrayList<JobItemEstimateDTO>,
        jobItemMeasureArrayList: ArrayList<JobItemMeasureDTO>,
        jobItemSectionArrayList: ArrayList<JobSectionDTO>
    ): JobDTO {

        val newJobId: String = SqlLitUtils.generateUuid()
//            job.JobId = newJobId
//        val estimateId: String = SqlLitUtils.generateUuid()

        val newJob = JobDTO(
            newJobId, itemId, null,useR!!,0,null,null,
            null,null,null,0.0,0.0,null,null,
            0,0,null,null,null,newJobItemEstimatesList,
            jobItemMeasureArrayList,jobItemSectionArrayList,null,0,null,null,
            null,null,0,0,0,0,0,
            0,0,0,0,null,0,
            0,null,null,null,null,0,null
        )

        jobArrayList.add(newJob)
        return newJob!!
    }

    private fun createItemEstimatePhoto(
        itemEst: JobItemEstimateDTO,
        filename_path: Map<String, String>,
        currentLocation: Location?,
        newJobItemEstimatesPhotosList: ArrayList<JobItemEstimatesPhotoDTO>,
        itemidPhototype: Map<String, String>
    ): JobItemEstimatesPhotoDTO {

//        val photoId: String = SqlLitUtils.generateUuid()
//        val isPhotoStart = itemId_photoType.get("type") == "start"
// itemEstimate.setJobItemEstimatePhotoStart(jobItemEstimatePhoto!!)


        val isPhotoStart = itemidPhototype.get("type") == "start"
        val photoId: String = SqlLitUtils.generateUuid()

        val newEstimatePhoto = JobItemEstimatesPhotoDTO(
            "", itemEst.estimateId, filename_path["filename"]!!,
            DateUtil.DateToString(Date())!!, photoId, null, null,
             0.0, 0.0,currentLocation!!.latitude,currentLocation.longitude,currentLocation.latitude,currentLocation.longitude,filename_path["path"]!!,itemEst,
            0,0, isPhotoStart, null
        )



        newJobItemEstimatesPhotosList.add(newEstimatePhoto)
        return newEstimatePhoto

    }


    private fun createItemEstimate(
        itemId: String?,
        job1: JobDTO,
        newJobItemPhotosList: ArrayList<JobItemEstimatesPhotoDTO>,
        jobItemMeasureArrayList: ArrayList<JobItemMeasureDTO>,
        jobItemWorksList: ArrayList<JobEstimateWorksDTO>,
        newJobItemEstimatesList: ArrayList<JobItemEstimateDTO>

//                                   jobItemPhoto: JobItemEstimatesPhotoDTO
    ): JobItemEstimateDTO {
        val estimateId: String = SqlLitUtils.generateUuid()

        val newEstimate = JobItemEstimateDTO(
            0, estimateId, job1.JobId,0.0, jobItemWorksList, newJobItemPhotosList, jobItemMeasureArrayList,
            job, itemId,null,0.0,0,0,"", 0, null

        )
        newJobItemEstimatesList.add(newEstimate)
        return newEstimate
    }


    private fun createRouteSection(
        itemEstimate: JobItemEstimateDTO,
        filenamePath: Map<String, String>,
        currentLocation: Location?,
        jobItem: JobDTO
    ): JobSectionDTO {
        val newEstimateSection = JobSectionDTO("",null,jobItem.JobId,0.0,0.0,jobItem,0, 0)
//        newJobItemEstimatesPhotosList.add(newEstimatePhoto)
        return newEstimateSection
    }
















    private fun showZoomedImage(imageUrl: Uri?) {
        val dialog = Dialog(context!!, R.style.dialog_full_screen)
        dialog.setContentView(R.layout.new_job_photo)
        val zoomageView =
            dialog.findViewById<ZoomageView>(R.id.zoomedImage)
        GlideApp.with(this.activity!!)
            .load(imageUrl)
            .into(zoomageView)
        dialog.show()
    }


    override fun onResume() {
        super.onResume()
        setCost()
    }

    private fun updatePhotos(
    imageView: ImageView, imageUri: Uri?, animate: Boolean
) {
//        imageView: ImageView, imageUri: Uri?, animate: Boolean) {
        if (imageUri != null) {
            GlideApp.with(this)
                .load(imageUri)
                .into(imageView)
            if (animate) imageView.startAnimation(bounce_1000)
        }
    }



    fun setValueEditText(qty: Double) {
        when (item?.uom) {
            "m²", "m³", "m" -> this.valueEditText!!.setText("" + qty)
            else -> this.valueEditText!!.setText("" + qty.toInt())
        }
    }


    private fun setCost() {
        if (isEstimateComplete()) {
            calculateCost()
            valueEditText!!.visibility = View.VISIBLE
            costTextView!!.visibility = View.VISIBLE
            costTextView.startAnimation(anims!!.bounce_soft)
        } else {
            labelTextView!!.text = "Incomplete estimate..."
            labelTextView.startAnimation(anims!!.shake_long)
            valueEditText!!.visibility = View.GONE
            costTextView!!.visibility = View.GONE
        }
    }
//    fun setQuantity(quantity: Double) {
//        this.quantity = quantity
//    }

    private fun calculateCost() {
        val item: ItemDTOTemp? = item
        val currentStartKm = getStartKm()
        val currentEndKm = getEndKm()

        val value: String = valueEditText!!.text.toString()
        //  Lose focus on fields
        valueEditText.clearFocus()
        var lineRate = 0.0
        var qty = quantity
        try {
            qty = value.toDouble()
        } catch (e: NumberFormatException) {
            e.printStackTrace()
        }




        when (item!!.uom) {
            "No" -> {
                labelTextView!!.text = "Quantity: "
                try { //  make the change in the array and update view
                    lineRate = value.toDouble() * item!!.tenderRate

                } catch (e: NumberFormatException) {
                    e.printStackTrace()
                    toast("Please place the Quantity.")

                }
                labelTextView.setText("Area(m²): ")
                try { //  Set the Area to the QTY
                    lineRate = value.toDouble() * item!!.tenderRate

                } catch (e: NumberFormatException) {
                    e.printStackTrace()
                    toast("Please place the Area.")

                }
                labelTextView.setText("Volume(m³): ")
                try { //  Set the Area to the QTY
                    lineRate = value.toDouble() * item!!.tenderRate

                } catch (e: NumberFormatException) {
                    e.printStackTrace()
                    toast("Please place the Volume.")

                }
                labelTextView.setText("Amount: ")
                try { //  Set the Area to the QTY
                    lineRate = value.toDouble() * item!!.tenderRate
                } catch (e: NumberFormatException) {
                    e.printStackTrace()
                    toast("Please place the Prov Sum.")
                }
            }
            "m²" -> {
                labelTextView!!.text = "Area(m²): "
                try {
                    lineRate = value.toDouble() * item!!.tenderRate

                } catch (e: NumberFormatException) {
                    e.printStackTrace()
                    toast("Please place the Area.")

                }
                labelTextView.text = "Volume(m³): "
                try {
                    lineRate = value.toDouble() * item!!.tenderRate

                } catch (e: NumberFormatException) {
                    e.printStackTrace()
                    toast("Please place the Volume.")

                }
                labelTextView.text = "Amount: "
                try {
                    lineRate = value.toDouble() * item!!.tenderRate
                } catch (e: NumberFormatException) {
                    e.printStackTrace()
                    toast("Please place the Prov Sum.")
                }
            }
            "m³" -> {
                labelTextView!!.text = "Volume(m³): "
                try {
                    lineRate = value.toDouble() * item!!.tenderRate

                } catch (e: NumberFormatException) {
                    e.printStackTrace()
                    toast("Please place the Volume.")

                }
                labelTextView!!.text = "Amount: "
                try {
                    lineRate = value.toDouble() * item!!.tenderRate
                } catch (e: NumberFormatException) {
                    e.printStackTrace()
                    toast("Please place the Prov Sum.")
                }
            }
            "Prov Sum" -> {
                labelTextView!!.text = "Amount: "
                try {
                    lineRate = value.toDouble() * item!!.tenderRate
                } catch (e: NumberFormatException) {
                    e.printStackTrace()
                    toast("Please place the Prov Sum.")
                }
            }
            "m" -> {
                labelTextView!!.text = "Length(m): "
                try { //  Set the Area to the QTY
                    val length = currentEndKm - currentStartKm
                    lineRate = length * item!!.tenderRate
                } catch (e: NumberFormatException) {
                    e.printStackTrace()
                    toast("Please place the m.")
                }
            }
            else -> {
                labelTextView!!.text = "Quantity: "
                try { //  Default Calculation
                    lineRate = value.toDouble() * item!!.tenderRate

                } catch (e: NumberFormatException) {
                    e.printStackTrace()
                    toast("Please place the Quantity.")

                }
            }
        }

    costTextView!!.text = "  *   R " + item!!.tenderRate.toString() + " =  R " + DecimalFormat("##.##").format(lineRate)
        val jobItemEstimate: JobItemEstimateDTO? = getJobItemEstimate()
        if (jobItemEstimate != null) {
            jobItemEstimate.qty
            jobItemEstimate.lineRate
        }
    }


    private fun getStoredValue(): Double {
        val jobItemEstimate: JobItemEstimateDTO? = getJobItemEstimate()
        return jobItemEstimate?.qty ?: quantity
    }

    fun getJobItemEstimate(): JobItemEstimateDTO? {
        return job?.getJobEstimateByItemId(item!!.itemId)
    }

    private fun isEstimateComplete(): Boolean {
        return getJobItemEstimate() != null && getJobItemEstimate()!!.isEstimateComplete()
    }

    fun getStartKm(): Double {
        val jobItemEstimate: JobItemEstimateDTO? = getJobItemEstimate()
        return if (jobItemEstimate?.getJobItemEstimatePhotoStart() != null) jobItemEstimate.getJobItemEstimatePhotoStart()!!
            .endKm else 0.0
    }

    fun getEndKm(): Double {
        val jobItemEstimate: JobItemEstimateDTO? = getJobItemEstimate()
        return if (jobItemEstimate?.getJobItemEstimatePhotoEnd() != null) jobItemEstimate.getJobItemEstimatePhotoEnd()!!
            .endKm else 0.0
    }


    //    private void updateSectionUI(Bitmap bitmap, TextView textView, boolean animate) {
//        if (bitmap != null) {
//            boolean isStart = bitmap.equals(startBitmap);
//            String sectionText = getSectionText(isStart);
//            if (sectionText != null) {
//                textView.setText(sectionText);
//                if (animate) textView.startAnimation(getAnims().bounce_long);
//            }
//        }
//    }


//    private fun updateSectionUI(
//        imageUri: Uri?,
//        textView: TextView,
//        animate: Boolean
//    ) {
//        if (imageUri != null) {
//            val isStart = imageUri == startimageUri
//            val sectionText: String = getSectionText(isStart)
//            if (sectionText != null) {
//                textView.text = sectionText
//                if (animate) textView.startAnimation(anims!!.bounce_long)
//            }
//        }
//    }


    fun getCurrentLocation(): Location? {
        return currentLocation
    }

    fun setCurrentLocation(currentLocation: Location?) {
        this.currentLocation = currentLocation
    }

}

































//
//
//internal class EstimateCalculator(activity: EstimatePhotoFragment) :
//    IEstimateCalculator {
//    private val activity = activity
//    val labelTextView1: TextView? = null
//    private val valueEditText1: EditText? = null
//    private val costTextView1: TextView?  = null
//
//    fun getQuantity(): Double {
//        return activity.quantity
//    }

//
//
//    fun getItem(): ItemDTO? {
//        return activity.item
//    }
//
//    fun getJob(): JobDTO? {
//        return this.getJob()
//    }
//
//    private fun toast(string: String) {
//        activity.toast(string)
//    }
//
//
//
//    fun anims(): Animations? {
//        return activity.anims
//    }
//
//
//
//
//
//
//    fun getJobItemEstimate(): JobItemEstimateDTO? {
//        return getJob()!!.getJobEstimateByItemId(getItem()!!.itemId)
//    }
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//



//    private fun setCost() {
//        if (isEstimateComplete) {
//            calculateCost()
//            valueEditText1!!.visibility = View.VISIBLE
//            costTextView1!!.visibility = View.VISIBLE
//            costTextView1!!.startAnimation(anims()!!.bounce_soft)
//        } else {
//            labelTextView1!!.text = "Incomplete estimate..."
//            labelTextView1!!.startAnimation(anims()!!.shake_long)
//            valueEditText1!!.visibility = View.GONE
//            costTextView1!!.visibility = View.GONE
//        }
//    }

//    fun setValueEditText(qty: Double) {
//        when (uom) {
//            "m²", "m³", "m" -> this.valueEditText1!!.setText("" + qty)
//            else -> this.valueEditText1!!.setText("" + qty.toInt())
//        }
//    }
//    fun getJobItemEstimate(): JobItemEstimateDTO? {
//        return getJob()!!.getJobEstimateByItemId(getItem()!!.itemId)
//    }
//
//    private fun isEstimateComplete(): Boolean {
//        return getJobItemEstimate() != null && getJobItemEstimate()!!.isEstimateComplete()
//    }
//








//    private val isEstimateComplete: Boolean
//        private get() = jobItemEstimate != null && jobItemEstimate!!.isEstimateComplete()
//
//    val startKm: Double
//        get() {
//            val jobItemEstimate: JobItemEstimateDTO? = jobItemEstimate
//            return if (jobItemEstimate?.getJobItemEstimatePhotoStart() != null) jobItemEstimate.getJobItemEstimatePhotoStart()!!
//                .endKm else 0.0
//        }
//
//    val endKm: Double
//        get() {
//            val jobItemEstimate: JobItemEstimateDTO? = jobItemEstimate
//            return if (jobItemEstimate?.getJobItemEstimatePhotoEnd() != null) jobItemEstimate.getJobItemEstimatePhotoEnd()!!
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
//        val value = valueEditText1!!.text.toString()
//        //  Lose focus on fields
//        valueEditText1!!.clearFocus()
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
//                labelTextView1!!.text = "Quantity: "
//                try { //  make the change in the array and update view
//                    lineRate = value.toDouble() * item.tenderRate
//
//                } catch (e: NumberFormatException) {
//                    e.printStackTrace()
//                    activity.toast("Please place the Quantity.")
//
//                }
//                labelTextView1.text = "Area(m²): "
//                try { //  Set the Area to the QTY
//                    lineRate = value.toDouble() * item.tenderRate
//
//                } catch (e: NumberFormatException) {
//                    e.printStackTrace()
////                    toast("Please place the Area.")
//
//                }
//                labelTextView1.text = "Volume(m³): "
//                try { //  Set the Area to the QTY
//                    lineRate = value.toDouble() * item.tenderRate
//
//                } catch (e: NumberFormatException) {
//                    e.printStackTrace()
////                    toast("Please place the Volume.")
//                }
//                labelTextView1.text = "Amount: "
//                try { //  Set the Area to the QTY
//                    lineRate = value.toDouble() * item.tenderRate
//                } catch (e: NumberFormatException) {
//                    e.printStackTrace()
////                    toast("Please place the Prov Sum.")
//                }
//            }
//            "m²" -> {
//                labelTextView1!!.text = "Area(m²): "
//                try {
//                    lineRate = value.toDouble() * item.tenderRate
//
//                } catch (e: NumberFormatException) {
//                    e.printStackTrace()
////                    toast("Please place the Area.")
//
//                }
//                labelTextView1.text = "Volume(m³): "
//                try {
//                    lineRate = value.toDouble() * item.tenderRate
//
//                } catch (e: NumberFormatException) {
//                    e.printStackTrace()
////                    toast("Please place the Volume.")
//
//                                    }
//                labelTextView1.text = "Amount: "
//                try {
//                    lineRate = value.toDouble() * item.tenderRate
//                } catch (e: NumberFormatException) {
//                    e.printStackTrace()
////                    toast("Please place the Prov Sum.")
//                }
//            }
//            "m³" -> {
//                labelTextView1!!.text = "Volume(m³): "
//                try {
//                    lineRate = value.toDouble() * item.tenderRate
//
//                } catch (e: NumberFormatException) {
//                    e.printStackTrace()
////                    toast("Please place the Volume.")
//
//                }
//                labelTextView1.text = "Amount: "
//                try {
//                    lineRate = value.toDouble() * item.tenderRate
//                } catch (e: NumberFormatException) {
//                    e.printStackTrace()
////                    toast("Please place the Prov Sum.")
//                }
//            }
//            "Prov Sum" -> {
//                labelTextView1!!.text = "Amount: "
//                try {
//                    lineRate = value.toDouble() * item.tenderRate
//                } catch (e: NumberFormatException) {
//                    e.printStackTrace()
////                    toast("Please place the Prov Sum.")
//                }
//            }
//            "m" -> {
//                labelTextView1!!.text = "Length(m): "
//                try { //  Set the Area to the QTY
//                    val length = currentEndKm - currentStartKm
//                    lineRate = length * item.tenderRate
//                } catch (e: NumberFormatException) {
//                    e.printStackTrace()
////                    toast("Please place the m.")
//                }
//            }
//            else -> {
//                labelTextView1!!.text = "Quantity: "
//                try { //  Default Calculation
//                    lineRate = value.toDouble() * item.tenderRate
//
//                } catch (e: NumberFormatException) {
//                    e.printStackTrace()
////                    toast("Please place the Quantity.")
//                }
//            }
//        }
//        costTextView1!!.text = "  *   R " + item.tenderRate.toString() + " =  R " + DecimalFormat(
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
////        this.activity = activity
////        this.labelTextView = activity.labelTextView
////        this.valueEditText = activity.valueEditText
////        this.costTextView = activity.costTextView
//
//    }
//}

