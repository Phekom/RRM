package za.co.xisystems.itis_rrm.ui.mainview.create.edit_estimates

import android.Manifest
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
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
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.ui.mainview._fragments.BaseFragment
import za.co.xisystems.itis_rrm.ui.mainview.create.CreateViewModel
import za.co.xisystems.itis_rrm.ui.mainview.create.CreateViewModelFactory
import za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils.intents.AbstractIntent
import za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils.models.PhotoType
import za.co.xisystems.itis_rrm.utils.PhotoUtil


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

    @State
    var photoType: PhotoType = PhotoType.start
    private val startImageView: ImageView? = null
    private  var endImageView: ImageView? = null
    private var startimageUri: Uri? = null
    private  var endimageUri: Uri? = null
    private  var imageUri:android.net.Uri? = null
    @State
    var filename_path = HashMap<String, String>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_photo_estimate, container, false)
    }






    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        createViewModel = activity?.run {
            ViewModelProviders.of(this, factory).get(CreateViewModel::class.java)
        } ?: throw Exception("Invalid Activity")

        mAppExcutor = AppExecutor()

        createViewModel.Sec_Item.observe(viewLifecycleOwner, Observer { pro_Item ->
            titleTextView.text = pro_Item.itemDTO.itemCode +" "+ pro_Item.itemDTO.descr
            setButtonClicks()

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
//                    val intent = Intent()
//                    intent.putExtra(AbstractIntent.JOB, getJob())
//                    setResult(Activity.RESULT_OK, intent)
//                    finish()
                }

//                R.id.startPhotoButton -> {
////                    infoTextView.visibility = View.VISIBLE
////                    selectProjectdrop.visibility = View.GONE
////                    photoLin.visibility = View.GONE
////                    mid_lin.visibility = View.VISIBLE
////                    last_lin.visibility = View.VISIBLE
//                }
//
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
//        outState.putSerializable("item", item)
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
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) { // If the image capture activity was called and was successful
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) { // Process the image and set it to the TextView
            processAndSetImage()
        } else { // Otherwise, delete the temporary image file
            BitmapUtils.deleteImageFile(context, filename_path.toString())
        }
    }

    private fun processAndSetImage() {

                    when (photoType) {
                        PhotoType.start -> updatePhotoUI(
                            startImageView!!, imageUri.also { startimageUri = it },
                            true
                        )
//                        PhotoType.end -> updatePhotoUI(
//                            endImageView!!,
//                            imageUri.also { endimageUri = it },
//                            true
//                        )
                    }
                    try { //  Location of picture
//                        val currentLocation: Location =
//                            locationHelper?.currentLocation!!
//                        if (currentLocation == null) toast("Error: Current location is null!")
                        //  Save Image to Internal Storage
                        filename_path = PhotoUtil.saveImageToInternalStorage(
                            context!!.applicationContext,
                            imageUri!!
                        ) as HashMap<String, String>

//                            newJobEditEstimateHelper.processPhotoEstimate(
//                                currentLocation,
//                                filename_path,
//                                itemId_photoType_tester
//                            )
                    } catch (e: java.lang.Exception) {
                        toast(R.string.error_getting_image)
                        Log.e("x-takePhoto", e.message)
                        e.printStackTrace()
                    }



        }
    }
//    private fun updatePhotoUI(animate: Boolean) {
//        updatePhotoUI(startImageView, startimageUri, animate)
//        updatePhotoUI(endImageView!!, endimageUri, animate)
//    }

    private fun updatePhotoUI(
        imageView: ImageView,
        imageUri: Uri?,
        animate: Boolean
    ) {
//        if (imageUri != null) {
//            imageView.setImageURI(imageUri)
////            if (animate) imageView.startAnimation(abounce_1000)
//        }
    }






