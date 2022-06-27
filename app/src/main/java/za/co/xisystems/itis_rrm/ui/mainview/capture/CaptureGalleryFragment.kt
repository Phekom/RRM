package za.co.xisystems.itis_rrm.ui.mainview.capture

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.instance
import za.co.xisystems.itis_rrm.ui.mainview.activities.main.MainActivity
import za.co.xisystems.itis_rrm.MobileNavigationDirections
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.base.LocationFragment
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimatesPhotoDTO
import za.co.xisystems.itis_rrm.databinding.FragmentCaptureGalleryBinding
import za.co.xisystems.itis_rrm.services.LocationModel
import za.co.xisystems.itis_rrm.ui.extensions.addZoomedImages
import za.co.xisystems.itis_rrm.ui.extensions.scaleForSize
import za.co.xisystems.itis_rrm.ui.mainview.activities.jobmain.new_job_utils.models.PhotoType
import za.co.xisystems.itis_rrm.ui.scopes.UiLifecycleScope
import za.co.xisystems.itis_rrm.utils.Coroutines
import za.co.xisystems.itis_rrm.utils.PhotoUtil
import za.co.xisystems.itis_rrm.utils.image_capture.camera.OnImageReadyListener
import za.co.xisystems.itis_rrm.utils.image_capture.helper.Constants
import za.co.xisystems.itis_rrm.utils.image_capture.helper.PermissionHelper.openAppSettings
import za.co.xisystems.itis_rrm.utils.image_capture.model.GridCount
import za.co.xisystems.itis_rrm.utils.image_capture.model.Image
import za.co.xisystems.itis_rrm.utils.image_capture.model.ImagePickerConfig
import za.co.xisystems.itis_rrm.utils.image_capture.model.RootDirectory
import java.io.File
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.set

class CaptureGalleryFragment : LocationFragment() {

    companion object {
        fun newInstance() = CaptureGalleryFragment()
        const val REQUEST_CAMERA_PERMISSIONS = 5050
        private const val REQUEST_IMAGE_CAPTURE = 4281
    }

    private var alertDialog: AlertDialog? = null
    private var images = ArrayList<Image>()

    private var itemIdPhotoType = HashMap<String, String>()
    private lateinit var captureViewModel: CaptureViewModel

    private var _ui: FragmentCaptureGalleryBinding? = null
    private val ui get() = _ui!!
    private val photoUtil: PhotoUtil by instance()
    var photoType: PhotoType = PhotoType.START
    private val galleryItems = ArrayList<JobItemEstimatesPhotoDTO>()
    private val captureFactory: CaptureViewModelFactory by instance()
    private var imageUri: Uri? = null
    private var rrmFileUri: Uri? = null
    private var filenamePath: HashMap<String, String> = HashMap()

    /**
     * ActivityResultContract for taking a photograph
     */

    val config = ImagePickerConfig(
        isMultipleMode = true,
        isCameraOnly = true,
        isShowCamera = true,
        rootDirectory = RootDirectory.DCIM,
        subDirectory = "RRM App Photos",
        selectedImages = images,
        statusBarColor = "#000000",
        isLightStatusBar = false,
        backgroundColor = "#FFFFFF",
        folderGridCount = GridCount(2, 4),
        imageGridCount = GridCount(3, 5),
    )


//    private val takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) { isSaved ->
//        if (isSaved) {
//            imageUri?.let { realUri ->
//                var myresult: String = realUri.path ?: ""
//                // get filename + ext of path
//                val cut1 = myresult.lastIndexOf('/')
//                if (cut1 != -1) myresult = myresult.substring(cut1 + 1)
//                val fileName = myresult
//
//                rrmFileUri = realUri
//                photoUtil.saveUnAllocatedImage(
//                    requireContext(),
//                    config, fileName,realUri,
//                    object : OnImageReadyListener {
//                        override fun onImageReady(images: java.util.ArrayList<Image>) {
//                            finishCaptureImage(images)
//                        }
//
//                        override fun onImageNotReady() {
//                            finishCaptureImage(arrayListOf())
//                        }
//                    })
//                processAndSetImage(realUri)
//            }
//
//        } else {
//            imageUri?.let { failedUri ->
//                uiScope.launch(dispatchers.io()) {
//                    val filenamePath = File(failedUri.path!!)
//                    photoUtil.deleteImageFile(filenamePath.toString())
//                }
//            }
//        }
//        this.photosDone()
//    }

    private val takePicture = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isSaved ->
        if (isSaved) {
            uiScope.launch(dispatchers.io()) {
                imageUri?.let { realUri ->
                    processAndSetImage(realUri)
                }
            }
        } else {
            imageUri?.let { failedUri ->
                uiScope.launch(dispatchers.io()) {
                    val filenamePath = File(failedUri.path!!)
                    photoUtil.deleteImageFile(filenamePath.toString())
                }
            }
        }
    }

    private fun finishCaptureImage(images: java.util.ArrayList<Image>) {
        val data = Intent()
        data.putParcelableArrayListExtra(Constants.EXTRA_IMAGES, images)
        requireActivity().setResult(Activity.RESULT_OK, data)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (activity as MainActivity).supportActionBar?.title = "Capture Photographs"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        captureViewModel =
            ViewModelProvider(this.requireActivity(), captureFactory)[CaptureViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _ui = FragmentCaptureGalleryBinding.inflate(inflater, container, false)
        return ui.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        bindUI()
    }

    private fun bindUI() {
        _ui?.addPhoto?.setOnClickListener {
            initLaunchCamera()
        }
        _ui?.doneImageButton?.setOnClickListener {
            val direction = MobileNavigationDirections.actionGlobalNavHome()
            Navigation.findNavController(this@CaptureGalleryFragment.requireView()).navigate(direction)
        }
    }

    private fun setupObservers() {
        val pictureQuery = captureViewModel.getUnallocatedPhotos()
        pictureQuery?.observe(viewLifecycleOwner) { unallocatedPics ->
            if (!unallocatedPics.isNullOrEmpty()) {
                galleryItems.clear()
                galleryItems.addAll(unallocatedPics as ArrayList<JobItemEstimatesPhotoDTO>)
                loadPictures(galleryItems)
            } else {
                if (galleryItems.size < 1) {
                    initLaunchCamera()
                }
            }
        }
    }


    private fun initLaunchCamera() {
        if (ContextCompat.checkSelfPermission(
                requireActivity().applicationContext,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                ),
                REQUEST_CAMERA_PERMISSIONS
            )
        } else {
            photoType = PhotoType.START
            launchCamera(REQUEST_IMAGE_CAPTURE, photoType)
        }

    }

    private fun showOpenSettingDialog() {
        val builder = AlertDialog.Builder(this.requireContext(), R.style.Theme_AppCompat_Light_Dialog)
        with(builder) {
            setMessage(R.string.msg_no_external_storage_permission)
            setNegativeButton(R.string.action_cancel) { _, _ ->
                //requireActivity().finish()
            }
            setPositiveButton(R.string.action_ok) { _, _ ->
                openAppSettings(requireActivity())
//                requireActivity().finish()
            }
        }

        alertDialog = builder.create()
        alertDialog!!.show()
    }


    private fun launchCamera(REQUEST_IMAGE_CAPTURE: Int, photoType: PhotoType) {
        val imageFileName = UUID.randomUUID()
        this.takingPhotos()
        Coroutines.io {
            imageUri = photoUtil.createUnAllocatedUri(imageFileName)!!
            withContext(Dispatchers.Main.immediate) {
                takePicture.launch(imageUri!!)
            }
        }

    }

//    private fun launchCamera(reqCode: Int, photoType: PhotoType) {
//        Coroutines.main {
//
//            toggleLongRunning(true)
//            itemIdPhotoType["itemId"] = ""
//            itemIdPhotoType["type"] = photoType.name
//
//            Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
//                sharedViewModel.toggleLongRunning(true)
//                val photoFile: File? = try {
//                    photoUtil.createImageFile()
//                } catch (ex: IOException) {
//                    ToastUtils().toastShort(requireContext(), ex.message)
//                    null
//                }
//                // Continue only if the File was successfully created
//                photoFile?.also {
//                    imageUri = photoUtil.getUriFromPath(it.path)
//                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
//                    takePictureIntent.putExtra(
//                        MediaStore.EXTRA_SCREEN_ORIENTATION,
//                        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
//                    )
//                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
//                    takePictureIntent.putExtra("photoType", itemIdPhotoType["type"])
//                    takePictureIntent.putExtra("itemId", itemIdPhotoType["itemId"])
//                    startActivityForResult(
//                        takePictureIntent, reqCode
//                    )
//                }
//            }
//        }
//
//    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(
            requestCode,
            resultCode,
            data
        ) // If the image capture activity was called and was successful
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            // Process the image and set it to the TextView
            if (imageUri != null) {
                uiScope.launch(uiScope.coroutineContext) {
                    processAndSetImage(
                        imageUri!!
                    )
                }
            } else {
                Snackbar.make(
                    ui.root,
                    "Image Saving Error Please Re-Capture",
                    Snackbar.LENGTH_INDEFINITE
                ).setAction(getString(R.string.retry)) {
                    launchCamera(requestCode, photoType)
                }.show()
            }
        } else { // Otherwise, delete the temporary image file
            uiScope.launch(uiScope.coroutineContext) {
                photoUtil.deleteImageFile(filenamePath.toString())
            }
        }
        toggleLongRunning(false)
    }






    private fun loadPictures(galleryItems: ArrayList<JobItemEstimatesPhotoDTO>) = Coroutines.main {
        val filenames = galleryItems.map { photo -> photo.photoPath }
        val photoPairs = photoUtil.prepareGalleryPairs(filenames)
        if (photoPairs.isNotEmpty()) {
            _ui?.estimateImageCollectionView?.clearImages()
            _ui?.estimateImageCollectionView?.scaleForSize(photoPairs.size)
            _ui?.estimateImageCollectionView?.addZoomedImages(photoPairs, requireActivity())
            _ui?.estimateImageCollectionView?.visibility = View.VISIBLE
        }
    }

    private fun processAndSetImage(realUri: Uri) {
        val photoLocation = this.getLocation()
        if (photoLocation != null) {
            uiScope.launch(dispatchers.io()) {
            persistLocatedPhoto(photoLocation, realUri)
        }
        }
    }

    private suspend fun persistLocatedPhoto(
//        currentLocation: LocationModel,
//        imageUri: Uri
//    ) {
//        Coroutines.io {
//            private suspend fun processAndSetImage(
                currentLocation: LocationModel,
                imageUri: Uri

            ) = withContext(uiScope.coroutineContext) {


                itemIdPhotoType["itemId"] = ""
//            }
            itemIdPhotoType["type"] = "unallocated"

            filenamePath = photoUtil.saveImageToInternalStorage2(
                 requireContext(),imageUri
            )!! as HashMap<String, String>

            val photo = captureViewModel.createUnallocatedPhoto(
                filenamePath = filenamePath,
                currentLocation = currentLocation,
                pointLocation = -1.0,
                itemidPhototype = itemIdPhotoType
            )
            captureViewModel.saveUnallocatedPhoto(photo)
            withContext(Dispatchers.Main.immediate) {
                galleryItems.add(photo)
                loadPictures(galleryItems)
            }
        }
    }

