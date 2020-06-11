package za.co.xisystems.itis_rrm.ui.mainview.estmeasure.submit_measure

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import icepick.State
import kotlinx.android.synthetic.main.activity_capture_item_measure_photo.done_image_button
import kotlinx.android.synthetic.main.activity_capture_item_measure_photo.photoButtons
import kotlinx.android.synthetic.main.fragment_capture_item_measure_photo.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import pereira.agnaldo.previewimgcol.ImageCollectionView
import timber.log.Timber
import za.co.xisystems.itis_rrm.BuildConfig
import za.co.xisystems.itis_rrm.MainActivity
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.base.LocationFragment
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemMeasureDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemMeasurePhotoDTO
import za.co.xisystems.itis_rrm.services.LocationModel
import za.co.xisystems.itis_rrm.ui.extensions.scaleForSize
import za.co.xisystems.itis_rrm.ui.extensions.showZoomedImage
import za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils.intents.AbstractIntent
import za.co.xisystems.itis_rrm.ui.mainview.estmeasure.MeasureViewModel
import za.co.xisystems.itis_rrm.ui.mainview.estmeasure.MeasureViewModelFactory
import za.co.xisystems.itis_rrm.utils.Coroutines
import za.co.xisystems.itis_rrm.utils.DateUtil
import za.co.xisystems.itis_rrm.utils.PhotoUtil
import za.co.xisystems.itis_rrm.utils.SqlLitUtils
import za.co.xisystems.itis_rrm.utils.enums.PhotoQuality
import java.util.*

//
class CaptureItemMeasurePhotoFragment :
    LocationFragment(R.layout.fragment_capture_item_measure_photo),
    KodeinAware {

    override val kodein by kodein()
    private lateinit var measureViewModel: MeasureViewModel
    private val factory: MeasureViewModelFactory by instance<MeasureViewModelFactory>()

    companion object {
        val TAG: String = CaptureItemMeasurePhotoFragment::class.java.simpleName
        const val JOB_ITEM_MEASURE_PHOTO_ARRAY_LIST = "JobItemMeasurePhotoArrayList"
        const val PHOTO_RESULT = 9000
        private const val REQUEST_IMAGE_CAPTURE = 1
        private const val REQUEST_STORAGE_PERMISSION = 1
        private val FILE_PROVIDER_AUTHORITY = BuildConfig.APPLICATION_ID + ".provider"

        const val URI_LIST_DATA = "URI_LIST_DATA"
        const val IMAGE_FULL_SCREEN_CURRENT_POS = "IMAGE_FULL_SCREEN_CURRENT_POS"

        protected const val LOCATION_KEY = "location-key"

        // region (Public Static Final Fields)
        const val UPDATE_INTERVAL_IN_MILLISECONDS: Long = 10000
        const val FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2


    }

    // lateinit var locationHelper: LocationHelperFragment
    private var measureLocation: LocationModel? = null

    private lateinit var jobItemMeasurePhotoArrayList: ArrayList<JobItemMeasurePhotoDTO>
    private var mTempPhotoPath: String? = null
    private lateinit var selectedJobItemMeasure: JobItemMeasureDTO
    private var jobItemMeasure: JobItemMeasureDTO? = null
    private lateinit var imageUri: Uri

    @State
    var filenamePath = HashMap<String, String>()

    @State
    var location = HashMap<String, String>()

    var viewPhotosOnly = false


    override fun onAttach(context: Context) {
        super.onAttach(context)
        (activity as MainActivity).supportActionBar?.title = getString(R.string.captured_photos)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity as MainActivity).supportActionBar?.title = getString(R.string.captured_photos)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_capture_item_measure_photo, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        measureViewModel = activity?.run {
            ViewModelProvider(this, factory).get(MeasureViewModel::class.java)
        } ?: throw Exception("Invalid Activity")

        Coroutines.main {
            jobItemMeasurePhotoArrayList = ArrayList<JobItemMeasurePhotoDTO>()
            photoButtons.visibility = View.GONE
            measureViewModel.measurea1_Item1.observe(
                viewLifecycleOwner,
                Observer { selectedJobItemM ->
                    toast(selectedJobItemM.jimNo)
                    selectedJobItemMeasure = selectedJobItemM
                    takeMeasurePhoto()

                })

            photoButtons.visibility = View.VISIBLE

        }

        capture_another_photo_button.setOnClickListener { capture ->
            launchCamera()
        }


        done_image_button.setOnClickListener { save ->
            setJobItemMeasureImage(
                jobItemMeasurePhotoArrayList,
                measureViewModel,
                selectedJobItemMeasure.estimateId,
                selectedJobItemMeasure
            )
            Coroutines.main {
//                measureViewModel.measure_Item.value = selectedJobItemMeasure
            }
            Navigation.findNavController(save)
                .navigate(R.id.action_captureItemMeasurePhotoFragment_to_submitMeasureFragment)
        }
//        Save.visibility = View.GONE
//        Save.setOnClickListener { save ->
//            saveImage()
//            setJobItemMeasureImage(
//                jobItemMeasurePhotoArrayList,
//                measureViewModel,
//                selectedJobItemMeasure.estimateId,
//                selectedJobItemMeasure
//            )
//        }
    }

    // TODO: Have location validated here
    // TODO: Check earlier and get user to switch Location on.
    private fun saveImage(): JobItemMeasurePhotoDTO? {
        //  Location of picture
        val measurementLocation: LocationModel? = getCurrentLocation()
        if (measurementLocation != null) {
            //  Save Image to Internal Storage
            val photoId = SqlLitUtils.generateUuid()
            filenamePath =
                PhotoUtil.saveImageToInternalStorage(
                    requireContext(),
                    imageUri
                ) as HashMap<String, String>

            Timber.d("location: ${measurementLocation.longitude}, ${measurementLocation.latitude}")
            Timber.d("accuracy: ${measurementLocation.accuracy}")

            val jobItemMeasurePhoto = JobItemMeasurePhotoDTO(
                0,
                null,
                filenamePath["filename"],
                selectedJobItemMeasure.estimateId,
                selectedJobItemMeasure.itemMeasureId,
                DateUtil.DateToString(Date()),
                photoId, measurementLocation.latitude, measurementLocation.longitude,
                filenamePath["path"], jobItemMeasure, 0, 0
            )


//        jobForJobItemEstimate.setJobItemMeasures(jobItemMeasurePhotoArrayList)
            return jobItemMeasurePhoto

        } else {
            toast("Error: Current location is null!")
            return null
        }
    }


    private fun takeMeasurePhoto() {

        val logoutBuilder =
            AlertDialog.Builder(requireActivity(), android.R.style.Theme_DeviceDefault_Dialog)
        logoutBuilder.setTitle(R.string.capture_measure_photo)
        logoutBuilder.setIcon(R.drawable.ic_menu_camera)
        logoutBuilder.setMessage(R.string.start_taking_photo)
        logoutBuilder.setCancelable(false)
        // Yes button
        logoutBuilder.setPositiveButton(R.string.ok) { dialog, which ->
            if (ContextCompat.checkSelfPermission(
                    requireActivity(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) !== PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    REQUEST_STORAGE_PERMISSION
                )
            } else {
                launchCamera()
            }
        }
        val declineAlert = logoutBuilder.create()
        declineAlert.show()

    }

    private fun launchCamera() {

        Coroutines.main {
            imageUri = PhotoUtil.getUri3(requireActivity().applicationContext)!!
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (takePictureIntent.resolveActivity(requireActivity().packageManager) != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                takePictureIntent.putExtra(
                    MediaStore.EXTRA_SCREEN_ORIENTATION,
                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                )
                startActivityForResult(takePictureIntent, AbstractIntent.REQUEST_TAKE_PHOTO)
            }

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            val photo = saveImage()
            photo?.let {
                jobItemMeasurePhotoArrayList.add(it)
                processAndSetImage()
            }
        } else { // Otherwise, delete the temporary image file
            PhotoUtil.deleteImageFile(requireContext().applicationContext, mTempPhotoPath)
        }
    }

    private fun processAndSetImage() {

        val bitmap = PhotoUtil.getPhotoBitmapFromFile(
            requireActivity(),
            imageUri,
            PhotoQuality.HIGH
        )

        estimate_image_collection_view.addImage(bitmap!!,
            object : ImageCollectionView.OnImageClickListener {
                override fun onClick(bitmap: Bitmap, imageView: ImageView) {
                    showZoomedImage(
                        imageUri,
                        this@CaptureItemMeasurePhotoFragment.requireActivity()
                    )
                }
            })

        estimate_image_collection_view.scaleForSize(
            this@CaptureItemMeasurePhotoFragment.requireContext(),
            jobItemMeasurePhotoArrayList.size
        )
    }

    private fun setJobItemMeasureImage(
        jobItemMeasurePhotoList: ArrayList<JobItemMeasurePhotoDTO>,
        measureViewModel: MeasureViewModel,
        estimateId: String?,
        selectedJobItemMeasure: JobItemMeasureDTO
    ) {
        Coroutines.main {
            measureViewModel.setJobItemMeasureImages(
                jobItemMeasurePhotoList,
                estimateId,
                selectedJobItemMeasure
            )
        }
    }


    fun getCurrentLocation(): LocationModel? {
        return super.getLocation()
    }

}
