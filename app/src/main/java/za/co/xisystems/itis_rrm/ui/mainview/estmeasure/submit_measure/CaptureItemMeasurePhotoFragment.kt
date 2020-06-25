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
import kotlinx.android.synthetic.main.activity_capture_item_measure_photo.done_image_button
import kotlinx.android.synthetic.main.activity_capture_item_measure_photo.photoButtons
import kotlinx.android.synthetic.main.fragment_capture_item_measure_photo.*
import kotlinx.coroutines.launch
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
import za.co.xisystems.itis_rrm.extensions.observeOnce
import za.co.xisystems.itis_rrm.services.LocationModel
import za.co.xisystems.itis_rrm.ui.custom.GalleryUIState
import za.co.xisystems.itis_rrm.ui.extensions.addZoomedImages
import za.co.xisystems.itis_rrm.ui.extensions.scaleForSize
import za.co.xisystems.itis_rrm.ui.extensions.showZoomedImage
import za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils.intents.AbstractIntent
import za.co.xisystems.itis_rrm.ui.mainview.estmeasure.MeasureViewModel
import za.co.xisystems.itis_rrm.ui.mainview.estmeasure.MeasureViewModelFactory
import za.co.xisystems.itis_rrm.ui.scopes.UiLifecycleScope
import za.co.xisystems.itis_rrm.utils.DateUtil
import za.co.xisystems.itis_rrm.utils.PhotoUtil
import za.co.xisystems.itis_rrm.utils.SqlLitUtils
import za.co.xisystems.itis_rrm.utils.enums.PhotoQuality
import za.co.xisystems.itis_rrm.utils.errors.ErrorHandler
import za.co.xisystems.itis_rrm.utils.results.XIError
import za.co.xisystems.itis_rrm.utils.results.XIResult
import za.co.xisystems.itis_rrm.utils.results.XISuccess
import java.util.*

//
class CaptureItemMeasurePhotoFragment :
    LocationFragment(R.layout.fragment_capture_item_measure_photo),
    KodeinAware {

    override val kodein by kodein()
    private lateinit var measureViewModel: MeasureViewModel
    private val factory: MeasureViewModelFactory by instance<MeasureViewModelFactory>()
    private val galleryObserver = Observer<XIResult<GalleryUIState>> { handleResponse(it) }
    private var measurementLocation: LocationModel? = null
    private lateinit var jobItemMeasurePhotoArrayList: ArrayList<JobItemMeasurePhotoDTO>
    private var mTempPhotoPath: String? = null
    private lateinit var selectedJobItemMeasure: JobItemMeasureDTO
    private var jobItemMeasure: JobItemMeasureDTO? = null
    private lateinit var imageUri: Uri
    private var filenamePath = HashMap<String, String>()
    private var viewPhotosOnly = false
    private var uiScope = UiLifecycleScope()
    override fun onAttach(context: Context) {
        super.onAttach(context)
        (activity as MainActivity).supportActionBar?.title = getString(R.string.captured_photos)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycle.addObserver(uiScope)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        uiScope.onCreate()
        return inflater.inflate(R.layout.fragment_capture_item_measure_photo, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        measureViewModel = activity?.run {
            ViewModelProvider(this, factory).get(MeasureViewModel::class.java)
        } ?: throw Exception("Invalid Activity")

        jobItemMeasurePhotoArrayList = ArrayList<JobItemMeasurePhotoDTO>()

        photoButtons.visibility = View.GONE

        uiScope.launch(uiScope.coroutineContext) {
            measureViewModel.jobItemMeasure.observe(
                viewLifecycleOwner,
                Observer { selectedJobItemM ->
                    selectedJobItemM?.let { it ->
                        toast(it.jimNo)
                        selectedJobItemMeasure = it
                        checkForPhotos(selectedJobItemMeasure)
                    }
                })

            measureViewModel.galleryUIState.observeOnce(viewLifecycleOwner, galleryObserver)
        }

        photoButtons.visibility = View.VISIBLE

        capture_another_photo_button.setOnClickListener {
            launchCamera()
        }

        done_image_button.setOnClickListener { save ->
            setJobItemMeasureImage(
                jobItemMeasurePhotoArrayList,
                measureViewModel,
                selectedJobItemMeasure.estimateId,
                selectedJobItemMeasure
            )

            estimate_image_collection_view.clearImages()

            uiScope.launch(uiScope.coroutineContext) {
                measureViewModel.setMeasureItemPhotos(jobItemMeasurePhotoArrayList as List<JobItemMeasurePhotoDTO>)
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

    private fun checkForPhotos(selectedJobItemMeasure: JobItemMeasureDTO) {
        uiScope.launch(uiScope.coroutineContext) {
            val photoFetch =
                measureViewModel.getJobItemMeasurePhotosForItemEstimateID(selectedJobItemMeasure.itemMeasureId!!)
            photoFetch.observeOnce(viewLifecycleOwner, Observer {
                it?.let {
                    if (it.isEmpty()) {
                        takeMeasurePhoto()
                    } else {
                        jobItemMeasurePhotoArrayList = it as ArrayList<JobItemMeasurePhotoDTO>
                        photoButtons.visibility = View.VISIBLE
                    }
                }
            })
        }
    }

    // TODO: Have location validated here
// TODO: Check earlier and get user to switch Location on.
    private fun saveImage(): JobItemMeasurePhotoDTO? {
        //  Location of picture
        val measurementLocation = getCurrentLocation()
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

            //        jobForJobItemEstimate.setJobItemMeasures(jobItemMeasurePhotoArrayList)
            return JobItemMeasurePhotoDTO(
                ID = 0,
                descr = null,
                filename = filenamePath["filename"],
                estimateId = selectedJobItemMeasure.estimateId,
                itemMeasureId = selectedJobItemMeasure.itemMeasureId,
                photoDate = DateUtil.DateToString(Date()),
                photoId = SqlLitUtils.generateUuid(),
                photoLatitude = measurementLocation.latitude,
                photoLongitude = measurementLocation.longitude,
                photoPath = filenamePath["path"],
                jobItemMeasure = jobItemMeasure,
                recordSynchStateId = 0,
                recordVersion = 0
            )
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

        uiScope.launch(uiScope.coroutineContext) {
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

        uiScope.launch(uiScope.coroutineContext) {
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
                jobItemMeasurePhotoArrayList.size
            )
        }
    }

    private fun setJobItemMeasureImage(
        jobItemMeasurePhotoList: ArrayList<JobItemMeasurePhotoDTO>,
        measureViewModel: MeasureViewModel,
        estimateId: String?,
        selectedJobItemMeasure: JobItemMeasureDTO
    ) {
        uiScope.launch(uiScope.coroutineContext) {
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

    private fun handleResponse(response: XIResult<GalleryUIState>) {
        when (response) {
            is XISuccess -> {
                val uiState = response.data
//                measurement_description.text = uiState.description
//                val costing = "${uiState.qty} x R ${uiState.lineRate} = R ${uiState.lineAmount}"
//                measurement_costing.text = costing
                estimate_image_collection_view.clearImages()
                estimate_image_collection_view.addZoomedImages(
                    uiState.photoPairs,
                    this@CaptureItemMeasurePhotoFragment.requireActivity()
                )
                estimate_image_collection_view.scaleForSize(
                    uiState.photoPairs.size
                )
            }
            is XIError ->
                ErrorHandler.handleError(
                    view = this@CaptureItemMeasurePhotoFragment.requireView(),
                    throwable = response,
                    shouldToast = false,
                    shouldShowSnackBar = true,
                    refreshAction = { retryGallery() }
                )
        }
    }

    private fun retryGallery() {
        measureViewModel.galleryBackup.observeOnce(viewLifecycleOwner, Observer {
            it?.let { measureViewModel.generateGalleryUI(it) }
        })
    }

    companion object {
        val TAG: String = CaptureItemMeasurePhotoFragment::class.java.simpleName
        const val JOB_ITEM_MEASURE_PHOTO_ARRAY_LIST = "JobItemMeasurePhotoArrayList"
        const val PHOTO_RESULT = 9000
        private const val REQUEST_IMAGE_CAPTURE = 1
        private const val REQUEST_STORAGE_PERMISSION = 1
        private const val FILE_PROVIDER_AUTHORITY = BuildConfig.APPLICATION_ID + ".provider"

        const val URI_LIST_DATA = "URI_LIST_DATA"
        const val IMAGE_FULL_SCREEN_CURRENT_POS = "IMAGE_FULL_SCREEN_CURRENT_POS"

        private const val LOCATION_KEY = "location-key"

        // region (Public Static Final Fields)
        const val UPDATE_INTERVAL_IN_MILLISECONDS: Long = 10000
        const val FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2
    }
}
