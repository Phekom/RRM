@file:Suppress("Annotator")

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
import androidx.lifecycle.coroutineScope
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.fragment_capture_item_measure_photo.*
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import pereira.agnaldo.previewimgcol.ImageCollectionView
import timber.log.Timber
import za.co.xisystems.itis_rrm.MainActivity
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.base.LocationFragment
import za.co.xisystems.itis_rrm.custom.results.XIError
import za.co.xisystems.itis_rrm.custom.results.XIProgress
import za.co.xisystems.itis_rrm.custom.results.XIResult
import za.co.xisystems.itis_rrm.custom.results.XIStatus
import za.co.xisystems.itis_rrm.custom.results.XISuccess
import za.co.xisystems.itis_rrm.custom.views.IndefiniteSnackbar
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemMeasureDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemMeasurePhotoDTO
import za.co.xisystems.itis_rrm.extensions.observeOnce
import za.co.xisystems.itis_rrm.services.LocationModel
import za.co.xisystems.itis_rrm.ui.custom.MeasureGalleryUIState
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
import za.co.xisystems.itis_rrm.utils.enums.ToastGravity
import za.co.xisystems.itis_rrm.utils.enums.ToastStyle.INFO
import java.util.ArrayList
import java.util.Date
import java.util.HashMap

//
class CaptureItemMeasurePhotoFragment :
    LocationFragment(R.layout.fragment_capture_item_measure_photo),
    KodeinAware {

    override val kodein by kodein()
    private lateinit var measureViewModel: MeasureViewModel
    private val factory: MeasureViewModelFactory by instance()
    private val galleryObserver = Observer<XIResult<MeasureGalleryUIState>> { handleResponse(it) }
    private lateinit var jobItemMeasurePhotoArrayList: ArrayList<JobItemMeasurePhotoDTO>
    private var mTempPhotoPath: String? = null
    private lateinit var selectedJobItemMeasure: JobItemMeasureDTO
    private var jobItemMeasure: JobItemMeasureDTO? = null
    private lateinit var imageUri: Uri
    private var filenamePath = HashMap<String, String>()
    private var viewPhotosOnly = false
    private var uiScope = UiLifecycleScope()
    private var measureIdent: Boolean = false
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
        estimate_image_collection_view.clearImages()
        measureViewModel = activity?.run {
            ViewModelProvider(this, factory).get(MeasureViewModel::class.java)
        } ?: throw Exception("Invalid Activity")

        jobItemMeasurePhotoArrayList = ArrayList<JobItemMeasurePhotoDTO>()
        gallery_layout.visibility = View.INVISIBLE
        photoButtons.visibility = View.GONE

        uiScope.launch(uiScope.coroutineContext) {
            measureViewModel.jobItemMeasure.observe(
                viewLifecycleOwner,
                { selectedJobItemM ->
                    selectedJobItemM?.let { it ->
                        estimate_image_collection_view.clearImages()
                        viewPhotosOnly = false

                        if (!measureIdent) {
                            this@CaptureItemMeasurePhotoFragment.sharpToast(
                                message = "Measuring job: ${it.jimNo}",
                                style = INFO,
                                position = ToastGravity.BOTTOM
                            )
                            measureIdent = true
                        }

                        selectedJobItemMeasure = it
                        checkForPhotos(selectedJobItemMeasure)
                    }
                })

            measureViewModel.measureGalleryUIState.observe(viewLifecycleOwner, galleryObserver)
        }

        capture_another_photo_button.setOnClickListener {
            launchCamera()
        }

        done_image_button.setOnClickListener { save ->
            if (!viewPhotosOnly) {

                setJobItemMeasureImage(
                    jobItemMeasurePhotoArrayList,
                    measureViewModel,
                    selectedJobItemMeasure.estimateId,
                    selectedJobItemMeasure
                )

                uiScope.launch(uiScope.coroutineContext) {
                    measureViewModel.setMeasureItemPhotos(jobItemMeasurePhotoArrayList as List<JobItemMeasurePhotoDTO>)
                }
            }

            estimate_image_collection_view.clearImages()

            Navigation.findNavController(save)
                .navigate(R.id.action_captureItemMeasurePhotoFragment_to_submitMeasureFragment)
        }
    }

    private fun checkForPhotos(selectedJobItemMeasure: JobItemMeasureDTO) {
        uiScope.launch(uiScope.coroutineContext) {
            estimate_image_collection_view.clearImages()
            val photoFetch =
                measureViewModel.getMeasureItemPhotos(selectedJobItemMeasure.itemMeasureId!!)
            photoFetch.observe(viewLifecycleOwner, {
                it?.let {
                    if (it.isEmpty()) {
                        takeMeasurePhoto()
                        viewPhotosOnly = false
                        setupControls()
                    } else {
                        jobItemMeasurePhotoArrayList = it as ArrayList<JobItemMeasurePhotoDTO>
                    }
                }
            })
        }
    }

    private fun setupControls() {
        gallery_layout.visibility = View.VISIBLE
        when (viewPhotosOnly) {
            true -> {
                photoButtons.visibility = View.VISIBLE
                capture_another_photo_button.visibility = View.GONE
                done_image_button.visibility = View.VISIBLE
            }
            else -> {
                photoButtons.visibility = View.VISIBLE
                capture_another_photo_button.visibility = View.VISIBLE
                done_image_button.visibility = View.VISIBLE
            }
        }
    }

    override fun onDestroyView() {
        uiScope.destroy()
        viewLifecycleOwner.lifecycle.coroutineScope.cancel()
        super.onDestroyView()
    }

    // TODO: Have location validated here
    // TODO: Check earlier and get user to switch Location on.
    private fun saveImage(): JobItemMeasurePhotoDTO? {
        //  Location of picture
        val measurementLocation = getCurrentLocation()
        if (measurementLocation != null) {
            //  Save Image to Internal Storage
            filenamePath =
                PhotoUtil.saveImageToInternalStorage(
                    requireContext(),
                    imageUri
                ) as HashMap<String, String>

            Timber.d("location: ${measurementLocation.longitude}, ${measurementLocation.latitude}")
            Timber.d("accuracy: ${measurementLocation.accuracy}")

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

        val builder =
            AlertDialog.Builder(requireActivity(), android.R.style.Theme_DeviceDefault_Dialog)
        builder.setTitle(R.string.capture_measure_photo)
        builder.setIcon(R.drawable.ic_menu_camera)
        builder.setMessage(R.string.start_taking_photo)
        builder.setCancelable(false)
        // Yes button
        builder.setPositiveButton(R.string.ok) { _, _ ->
            if (ContextCompat.checkSelfPermission(
                    requireActivity(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
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
        val declineAlert = builder.create()
        declineAlert.show()
    }

    private fun launchCamera() {

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            val photo = saveImage()
            photo?.let {
                jobItemMeasurePhotoArrayList.add(it)
                processAndSetImage()
            }
        } else { // Otherwise, delete the temporary image file
            PhotoUtil.deleteImageFile(mTempPhotoPath)
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
            jobItemMeasurePhotoArrayList.size
        )
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

    private fun getCurrentLocation(): LocationModel? {
        return super.getLocation()
    }

    private fun handleResponse(response: XIResult<MeasureGalleryUIState>) {
        when (response) {
            is XISuccess -> {
                toggleLongRunning(false)
                val uiState = response.data
                estimate_image_collection_view.clearImages()

                if (uiState.photoPairs.isNotEmpty()) {
                    estimate_image_collection_view.addZoomedImages(
                        uiState.photoPairs,
                        this@CaptureItemMeasurePhotoFragment.requireActivity()
                    )

                    estimate_image_collection_view.scaleForSize(
                        uiState.photoPairs.size
                    )

                    viewPhotosOnly = true
                    setupControls()
                }
            }

            is XIError -> {
                crashGuard(
                    view = this@CaptureItemMeasurePhotoFragment.requireView(),
                    throwable = response,
                    refreshAction = { retryGallery() }
                )
            }
            is XIProgress -> {
                toggleLongRunning(response.isLoading)
            }
            is XIStatus -> {
                sharpToast(
                    message = response.message
                )
            }
        }
    }

    private fun retryGallery() {
        IndefiniteSnackbar.hide()
        measureViewModel.galleryBackup.observeOnce(viewLifecycleOwner, {
            it?.let { measureViewModel.generateGalleryUI(it) }
        })
    }

    companion object {
        val TAG: String = CaptureItemMeasurePhotoFragment::class.java.simpleName
        private const val REQUEST_IMAGE_CAPTURE = 1
        private const val REQUEST_STORAGE_PERMISSION = 1
    }
}
