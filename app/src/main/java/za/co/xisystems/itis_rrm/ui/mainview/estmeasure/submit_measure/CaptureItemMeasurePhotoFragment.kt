/**
 * Updated by Shaun McDonald on 2021/05/19
 * Last modified on 2021/05/18, 20:24
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 **/

@file:Suppress("Annotator")

package za.co.xisystems.itis_rrm.ui.mainview.estmeasure.submit_measure

import android.Manifest.permission
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.instance
import pereira.agnaldo.previewimgcol.ImageCollectionView
import timber.log.Timber
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.base.LocationFragment
import za.co.xisystems.itis_rrm.custom.notifications.ToastGravity
import za.co.xisystems.itis_rrm.custom.notifications.ToastStyle
import za.co.xisystems.itis_rrm.custom.results.XIResult
import za.co.xisystems.itis_rrm.custom.views.IndefiniteSnackbar
import za.co.xisystems.itis_rrm.data._commons.utils.SqlLitUtils
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemMeasureDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemMeasurePhotoDTO
import za.co.xisystems.itis_rrm.databinding.FragmentCaptureItemMeasurePhotoBinding
import za.co.xisystems.itis_rrm.extensions.observeOnce
import za.co.xisystems.itis_rrm.services.LocationModel
import za.co.xisystems.itis_rrm.ui.custom.MeasureGalleryUIState
import za.co.xisystems.itis_rrm.ui.extensions.*
import za.co.xisystems.itis_rrm.ui.mainview.estmeasure.MeasureViewModel
import za.co.xisystems.itis_rrm.ui.mainview.estmeasure.MeasureViewModelFactory
import za.co.xisystems.itis_rrm.utils.Coroutines
import za.co.xisystems.itis_rrm.utils.DateUtil
import za.co.xisystems.itis_rrm.utils.PhotoUtil
import za.co.xisystems.itis_rrm.utils.enums.PhotoQuality
import java.util.*

//
class CaptureItemMeasurePhotoFragment :
    LocationFragment() {

    private lateinit var measureViewModel: MeasureViewModel
    private val factory: MeasureViewModelFactory by instance()
    private val photoUtil: PhotoUtil by instance()
    private val galleryObserver = Observer<XIResult<MeasureGalleryUIState>> { handleResponse(it) }
    private lateinit var jobItemMeasurePhotoArrayList: ArrayList<JobItemMeasurePhotoDTO>
    private lateinit var selectedJobItemMeasure: JobItemMeasureDTO
    private lateinit var imageUri: Uri
    private var filenamePath = HashMap<String, String>()
    private var viewPhotosOnly = false
    private var _ui: FragmentCaptureItemMeasurePhotoBinding? = null
    private val ui get() = _ui!!

    /**
     * ActivityResultContract for taking a photograph
     */
    private val takePicture = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isSaved ->
        if (isSaved) {
            Coroutines.io {
                val photo = saveImage()
                photo?.let { it ->
                    withContext(Dispatchers.Main.immediate) {
                        jobItemMeasurePhotoArrayList.add(it)
                        processAndSetImage()
                    }
                }
            }
        } else {
            Coroutines.io {
                photoUtil.deleteImageFile(filenamePath.toString())
            }
        }
    }

    companion object {
        val TAG: String = CaptureItemMeasurePhotoFragment::class.java.simpleName
        private const val REQUEST_STORAGE_PERMISSION = 1
    }




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(uiScope)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _ui = FragmentCaptureItemMeasurePhotoBinding.inflate(inflater, container, false)
        return ui.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _ui?.estimateImageCollectionView?.clearImages()
        measureViewModel =
            ViewModelProvider(this.requireActivity(), factory)[MeasureViewModel::class.java]

        jobItemMeasurePhotoArrayList = ArrayList()
        _ui?.galleryLayout?.visibility = View.INVISIBLE
        _ui?.photoButtons?.visibility = View.GONE

        uiScope.launch(uiScope.coroutineContext) {
            measureViewModel.jobItemMeasure.observe(
                viewLifecycleOwner
            ) { selectedJobItemM ->
                selectedJobItemM?.let { it ->
                    _ui?.estimateImageCollectionView?.clearImages()
                    viewPhotosOnly = false

                    if (measureViewModel.measuredJiNo != it.jimNo) {
                        this@CaptureItemMeasurePhotoFragment.extensionToast(
                            message = "Measuring job: ${it.jimNo}",
                            style = ToastStyle.INFO,
                            position = ToastGravity.BOTTOM
                        )
                        measureViewModel.measuredJiNo = it.jimNo!!
                    }

                    selectedJobItemMeasure = it
                    checkForPhotos(selectedJobItemMeasure)
                }
            }

            measureViewModel.measureGalleryUIState.observe(viewLifecycleOwner, galleryObserver)
        }

        _ui?.captureAnotherPhotoButton?.setOnClickListener {
            launchCamera()
        }

        _ui?.doneImageButton?.setOnClickListener { save ->
            saveImagesAndExit(save)
        }
    }

    private fun saveImagesAndExit(view: View) {
        if (!viewPhotosOnly) {

            setJobItemMeasureImage(
                jobItemMeasurePhotoArrayList,
                measureViewModel,
                selectedJobItemMeasure.estimateId!!,
                selectedJobItemMeasure
            )

            uiScope.launch(uiScope.coroutineContext) {
                measureViewModel.setMeasureItemPhotos(jobItemMeasurePhotoArrayList as List<JobItemMeasurePhotoDTO>)
            }
        }

        _ui?.estimateImageCollectionView?.clearImages()

        Navigation.findNavController(view)
            .navigate(R.id.action_captureItemMeasurePhotoFragment_to_submitMeasureFragment)
    }

    private fun checkForPhotos(selectedJobItemMeasure: JobItemMeasureDTO) {
        uiScope.launch(uiScope.coroutineContext) {
            _ui?.estimateImageCollectionView?.clearImages()
            val photoFetch =
                measureViewModel.getMeasureItemPhotos(selectedJobItemMeasure.itemMeasureId)
            photoFetch.observe(viewLifecycleOwner) {
                it?.let {
                    if (it.isEmpty()) {
                        takeMeasurePhoto()
                        viewPhotosOnly = false
                        setupControls()
                    } else {
                        jobItemMeasurePhotoArrayList = it as ArrayList<JobItemMeasurePhotoDTO>
                    }
                }
            }
        }
    }

    private fun setupControls() {
        _ui?.galleryLayout?.visibility = View.VISIBLE
        _ui?.doneImageButton?.visibility = View.VISIBLE
        when (viewPhotosOnly) {
            true -> {
                _ui?.photoButtons?.visibility = View.VISIBLE
                _ui?.captureAnotherPhotoButton?.visibility = View.GONE
            }
            else -> {
                _ui?.photoButtons?.visibility = View.VISIBLE
                _ui?.captureAnotherPhotoButton?.visibility = View.VISIBLE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _ui = null
    }

    // 2_DO: Have location validated here
    // 2_DO: Check earlier and get user to switch Location on.
    private suspend fun saveImage(): JobItemMeasurePhotoDTO? = withContext(dispatchers.io()) {
        //  Location of picture
        val measurementLocation = getCurrentLocation()
        if (measurementLocation != null) {
            //  Save Image to Internal Storage
            filenamePath =
                photoUtil.saveImageToInternalStorage(
                imageUri
            ) as HashMap<String, String>

            Timber.d("location: ${measurementLocation.longitude}, ${measurementLocation.latitude}")
            Timber.d("accuracy: ${measurementLocation.accuracy}")

            return@withContext JobItemMeasurePhotoDTO(
                id = 0,
                descr = null,
                filename = filenamePath["filename"],
                estimateId = selectedJobItemMeasure.estimateId!!,
                itemMeasureId = selectedJobItemMeasure.itemMeasureId,
                photoDate = DateUtil.dateToString(Date()),
                photoId = SqlLitUtils.generateUuid(),
                photoLatitude = measurementLocation.latitude,
                photoLongitude = measurementLocation.longitude,
                photoPath = filenamePath["path"],
                recordSynchStateId = 0,
                recordVersion = 0
            )
        } else {
            withContext(Dispatchers.Main.immediate) {
                toast("Error: Current location is null!")
            }
            return@withContext null
        }
    }

    private fun takeMeasurePhoto() {
       // val alertDialog = AlertDialog.Builder(requireActivity())
        val builder = AlertDialog.Builder(requireActivity())
        builder.setTitle(R.string.capture_measure_photo)
        builder.setIcon(R.drawable.ic_menu_camera)
        builder.setMessage(R.string.start_taking_photo)
        builder.setCancelable(false)
        // Yes button
        builder.setPositiveButton(R.string.ok) { _, _ ->
            initLaunchCamera()
        }
        val photoAlert = builder.create()
        photoAlert.show()
    }

    private fun initLaunchCamera() {
        if (ContextCompat.checkSelfPermission(
                requireActivity().applicationContext,
                permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(
                    permission.CAMERA,
                    permission.WRITE_EXTERNAL_STORAGE,
                    permission.READ_EXTERNAL_STORAGE
                ),
                REQUEST_STORAGE_PERMISSION
            )
        } else {
            launchCamera()
        }
    }

    private fun launchCamera() = Coroutines.main {
        this.takingPhotos()
        imageUri = photoUtil.getUri()!!
        takePicture.launch(imageUri)
    }

    private suspend fun processAndSetImage() = Coroutines.main {

        _ui?.estimateImageCollectionView?.scaleForSize(
            jobItemMeasurePhotoArrayList.size
        )

        photoUtil.getPhotoBitmapFromFile(
            imageUri,
            PhotoQuality.HIGH
        ).also { bmp ->
            bmp?.run {
                _ui?.estimateImageCollectionView?.addImage(
                    this,
                    object : ImageCollectionView.OnImageClickListener {
                        override fun onClick(bitmap: Bitmap, imageView: ImageView) {
                            showZoomedImage(
                                imageUri,
                                this@CaptureItemMeasurePhotoFragment.requireActivity()
                            )
                        }
                    }
                )
            }
            this@CaptureItemMeasurePhotoFragment.photosDone()
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

    private fun getCurrentLocation(): LocationModel? {
        return super.getLocation()
    }

    private fun handleResponse(response: XIResult<MeasureGalleryUIState>) {
        when (response) {
            is XIResult.Success -> {
                handleGallerySuccess(response)
            }

            is XIResult.Error -> {
                crashGuard(
                    throwable = response,
                    refreshAction = { this.retryGallery() }
                )
            }
            is XIResult.Progress -> {
                toggleLongRunning(response.isLoading)
            }
            is XIResult.Status -> {
                extensionToast(
                    message = response.message
                )
            }
            else -> {
                Timber.d("Unexpected result: $response")
            }
        }
    }

    private fun handleGallerySuccess(response: XIResult.Success<MeasureGalleryUIState>) {
        toggleLongRunning(false)
        val uiState = response.data
        _ui?.estimateImageCollectionView?.clearImages()

        if (uiState.photoPairs.isNotEmpty()) {

            _ui?.estimateImageCollectionView?.scaleForSize(
                uiState.photoPairs.size
            )
            _ui?.estimateImageCollectionView?.addZoomedImages(
                uiState.photoPairs,
                this@CaptureItemMeasurePhotoFragment.requireActivity()
            )

            viewPhotosOnly = true
            setupControls()
        }
    }

    private fun retryGallery() {
        IndefiniteSnackbar.hide()
        measureViewModel.galleryBackup.observeOnce(viewLifecycleOwner) {
            it?.let { measureViewModel.generateGalleryUI(it) }
        }
    }
}
