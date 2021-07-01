/**
 * Updated by Shaun McDonald on 2021/05/19
 * Last modified on 2021/05/18, 20:24
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 **/

@file:Suppress("Annotator")

package za.co.xisystems.itis_rrm.ui.mainview.estmeasure.submit_measure

import android.Manifest.permission
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
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
import za.co.xisystems.itis_rrm.databinding.FragmentCaptureItemMeasurePhotoBinding
import za.co.xisystems.itis_rrm.extensions.observeOnce
import za.co.xisystems.itis_rrm.services.LocationModel
import za.co.xisystems.itis_rrm.ui.custom.MeasureGalleryUIState
import za.co.xisystems.itis_rrm.ui.extensions.addZoomedImages
import za.co.xisystems.itis_rrm.ui.extensions.scaleForSize
import za.co.xisystems.itis_rrm.ui.extensions.showZoomedImage
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
    LocationFragment(),
    KodeinAware {

    override val kodein by kodein()
    private lateinit var measureViewModel: MeasureViewModel
    private val factory: MeasureViewModelFactory by instance()
    private val galleryObserver = Observer<XIResult<MeasureGalleryUIState>> { handleResponse(it) }
    private lateinit var jobItemMeasurePhotoArrayList: ArrayList<JobItemMeasurePhotoDTO>
    private lateinit var selectedJobItemMeasure: JobItemMeasureDTO
    private lateinit var imageUri: Uri
    private var filenamePath = HashMap<String, String>()
    private var viewPhotosOnly = false
    private var uiScope = UiLifecycleScope()
    private var _ui: FragmentCaptureItemMeasurePhotoBinding? = null
    private val ui get() = _ui!!
    private lateinit var photoUtil: PhotoUtil

    companion object {
        val TAG: String = CaptureItemMeasurePhotoFragment::class.java.simpleName
        private const val REQUEST_STORAGE_PERMISSION = 1
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (activity as MainActivity).supportActionBar?.title = getString(R.string.captured_photos)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // no options menu
        return false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        uiScope.onCreate()
        lifecycle.addObserver(uiScope)
        photoUtil = PhotoUtil.getInstance(this.requireContext().applicationContext)
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
        ui.estimateImageCollectionView.clearImages()
        measureViewModel = activity?.run {
            ViewModelProvider(this, factory).get(MeasureViewModel::class.java)
        } ?: throw Exception("Invalid Activity")

        jobItemMeasurePhotoArrayList = ArrayList<JobItemMeasurePhotoDTO>()
        ui.galleryLayout.visibility = View.INVISIBLE
        ui.photoButtons.visibility = View.GONE

        uiScope.launch(uiScope.coroutineContext) {
            measureViewModel.jobItemMeasure.observe(
                viewLifecycleOwner,
                { selectedJobItemM ->
                    selectedJobItemM?.let { it ->
                        ui.estimateImageCollectionView.clearImages()
                        viewPhotosOnly = false

                        if (measureViewModel.measuredJiNo != it.jimNo) {
                            this@CaptureItemMeasurePhotoFragment.sharpToast(
                                message = "Measuring job: ${it.jimNo}",
                                style = INFO,
                                position = ToastGravity.BOTTOM
                            )
                            measureViewModel.measuredJiNo = it.jimNo!!
                        }

                        selectedJobItemMeasure = it
                        checkForPhotos(selectedJobItemMeasure)
                    }
                })

            measureViewModel.measureGalleryUIState.observe(viewLifecycleOwner, galleryObserver)
        }

        ui.captureAnotherPhotoButton.setOnClickListener {
            launchCamera()
        }

        ui.doneImageButton.setOnClickListener { save ->
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

        ui.estimateImageCollectionView.clearImages()

        Navigation.findNavController(view)
            .navigate(R.id.action_captureItemMeasurePhotoFragment_to_submitMeasureFragment)
    }

    private fun checkForPhotos(selectedJobItemMeasure: JobItemMeasureDTO) {
        uiScope.launch(uiScope.coroutineContext) {
            ui.estimateImageCollectionView.clearImages()
            val photoFetch =
                measureViewModel.getMeasureItemPhotos(selectedJobItemMeasure.itemMeasureId)
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
        ui.galleryLayout.visibility = View.VISIBLE
        ui.doneImageButton.visibility = View.VISIBLE
        when (viewPhotosOnly) {
            true -> {
                ui.photoButtons.visibility = View.VISIBLE
                ui.captureAnotherPhotoButton.visibility = View.GONE
            }
            else -> {
                ui.photoButtons.visibility = View.VISIBLE
                ui.captureAnotherPhotoButton.visibility = View.VISIBLE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        uiScope.destroy()
        _ui = null
    }

    // TODO: Have location validated here
    // TODO: Check earlier and get user to switch Location on.
    private fun saveImage(): JobItemMeasurePhotoDTO? {
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

            return JobItemMeasurePhotoDTO(
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

    private fun launchCamera() {
        this.takingPhotos()
        imageUri = photoUtil.getUri()!!
        takePicture.launch(imageUri)
    }

    /**
     * ActivityResultContract for taking a photograph
     */
    private val takePicture = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isSaved ->
        if (isSaved) {
            val photo = saveImage()
            photo?.let { it ->
                jobItemMeasurePhotoArrayList.add(it)
                processAndSetImage()
            }
        } else {
            photoUtil.deleteImageFile(filenamePath.toString())
        }
    }

    private fun processAndSetImage() {

        ui.estimateImageCollectionView.scaleForSize(
            jobItemMeasurePhotoArrayList.size
        )

        photoUtil.getPhotoBitmapFromFile(
            imageUri,
            PhotoQuality.HIGH
        ).also { bmp ->
            bmp?.run {
                ui.estimateImageCollectionView.addImage(this,
                    object : ImageCollectionView.OnImageClickListener {
                        override fun onClick(bitmap: Bitmap, imageView: ImageView) {
                            showZoomedImage(
                                imageUri,
                                this@CaptureItemMeasurePhotoFragment.requireActivity()
                            )
                        }
                    })
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
            is XISuccess -> {
                toggleLongRunning(false)
                val uiState = response.data
                ui.estimateImageCollectionView.clearImages()

                if (uiState.photoPairs.isNotEmpty()) {

                    ui.estimateImageCollectionView.scaleForSize(
                        uiState.photoPairs.size
                    )
                    ui.estimateImageCollectionView.addZoomedImages(
                        uiState.photoPairs,
                        this@CaptureItemMeasurePhotoFragment.requireActivity()
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
            else -> {
                Timber.d("Unexpected result: $response")
            }
        }
    }

    private fun retryGallery() {
        IndefiniteSnackbar.hide()
        measureViewModel.galleryBackup.observeOnce(viewLifecycleOwner, {
            it?.let { measureViewModel.generateGalleryUI(it) }
        })
    }
}
