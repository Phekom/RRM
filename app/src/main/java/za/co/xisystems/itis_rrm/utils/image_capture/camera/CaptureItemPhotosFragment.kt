@file:Suppress("Annotator")

package za.co.xisystems.itis_rrm.utils.image_capture.camera

import android.Manifest.permission
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import com.mapbox.geojson.Point
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.instance
import timber.log.Timber
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.base.LocationFragment
import za.co.xisystems.itis_rrm.data._commons.utils.SqlLitUtils
import za.co.xisystems.itis_rrm.data._commons.views.ToastUtils
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimatesPhotoDTO
import za.co.xisystems.itis_rrm.databinding.FragmentImageCaptureBinding
import za.co.xisystems.itis_rrm.services.LocationModel
import za.co.xisystems.itis_rrm.ui.mainview.activities.jobmain.new_job_utils.models.PhotoType
import za.co.xisystems.itis_rrm.utils.Coroutines
import za.co.xisystems.itis_rrm.utils.GlideApp
import za.co.xisystems.itis_rrm.utils.PhotoUtil
import za.co.xisystems.itis_rrm.utils.ValidateInputs.isValidInput
import za.co.xisystems.itis_rrm.utils.image_capture.helper.Constants

import za.co.xisystems.itis_rrm.utils.image_capture.helper.DialogHelper
import za.co.xisystems.itis_rrm.utils.image_capture.helper.ImageProvider
import za.co.xisystems.itis_rrm.utils.image_capture.listener.DismissListener
import za.co.xisystems.itis_rrm.utils.image_capture.listener.ResultListener
import za.co.xisystems.itis_rrm.utils.image_capture.model.GridCount
import za.co.xisystems.itis_rrm.utils.image_capture.model.Image
import za.co.xisystems.itis_rrm.utils.image_capture.model.ImagePickerConfig
import za.co.xisystems.itis_rrm.utils.image_capture.model.RootDirectory
import za.co.xisystems.itis_rrm.utils.image_capture.registerImagePicker
import java.io.*
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*


class CaptureItemPhotosFragment : LocationFragment() {

    private lateinit var viewModel: CaptureImagesViewModel
    private val factory: CaptureImagesViewModelFactory by instance()
    private val photoUtil: PhotoUtil by instance()

    //    private val data: CaptureItemPhotosFragmentArgs by navArgs()
    private lateinit var mobileAppPhotoArrayList: ArrayList<JobItemEstimatesPhotoDTO>
    private var imageProvider = ImageProvider.CAMERA
    private var imageProviderInterceptor: ((ImageProvider) -> Unit)? = null
    private var dismissListener: DismissListener? = null
    internal var inventoryItem: String? = null
    private var filenamePath = HashMap<String, String>()
    private var viewPhotosOnly = false
    private var _ui: FragmentImageCaptureBinding? = null
    private val ui get() = _ui!!
    private var itemIdPhotoType = HashMap<String, String>()
    var photoType: PhotoType = PhotoType.START
    var imagepath: String? = null
    private var imageUri: Uri? = null
    private var cameraImageUri: Uri? = null


    private var permissions = arrayOf(
        permission.WRITE_EXTERNAL_STORAGE,
        permission.CAMERA,
        permission.READ_EXTERNAL_STORAGE,
        permission.ACCESS_FINE_LOCATION
    )
    private var images = ArrayList<Image>()
    private val launcher = registerImagePicker {
        uiScope.launch(dispatchers.io()) {
            images = it
            // your DCM card
            //  imagepath = ""//viewModel.photoUtil.getPath(images[0].uri)
            // the file to be moved or copied
            val sourceLocation = File(imagepath)
            // generate a new filename as per ITIS Records
            val newImageFileName = UUID.randomUUID()
            // make sure your target location folder exists!
            val targetLocation = File(PhotoUtil.instance.pictureFolder, "$newImageFileName.jpg")// "${images[0].name}"

            if (sourceLocation.exists()) {
                val inn: InputStream = FileInputStream(sourceLocation)
                val out: OutputStream = FileOutputStream(targetLocation)

                // Copy the bits from instream to outstream
                val buf = ByteArray(1024)
                var len: Int
                while (inn.read(buf).also { len = it } > 0) {
                    out.write(buf, 0, len)
                }
                inn.close()
                out.close()
                Timber.tag(TAG).d("Copy file successful.")
            } else {
                Timber.tag(TAG).d("Copy file failed. Source file missing.")
            }

            imageUri = photoUtil.getPhotoPathFromExternalDirectory("$newImageFileName.jpg")

            processAndSetImage(imageUri!!, "GALLERY", images[0].latitude, images[0].longitude)
        }
    }


    val config = ImagePickerConfig(
        isMultipleMode = false,
        isShowCamera = false,
        rootDirectory = RootDirectory.DCIM,
        subDirectory = "App Photos",
        selectedImages = images,
        statusBarColor = "#000000",
        isLightStatusBar = false,
        backgroundColor = "#FFFFFF",
        imageGridCount = GridCount(3, 5),
        limitMessage = "You could only select up to 10 photos",
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        uiScope.onCreate(this)
        lifecycle.addObserver(uiScope)
    }

    companion object {
        val TAG: String = CaptureItemPhotosFragment::class.java.simpleName
        private const val PERMISSION_REQUEST = 10
        private const val REQUEST_IMAGE_CAPTURE = 1
        const val MINIMUM_INTERVAL = 3
        private const val IMAGE_REQ_CODE = 101
        private const val GALLERY_INTENT_REQ_CODE = 4261
        private const val CAMERA_INTENT_REQ_CODE = 4281
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _ui = FragmentImageCaptureBinding.inflate(inflater, container, false)
        return ui.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = activity?.run {
            ViewModelProvider(this, factory)[CaptureImagesViewModel::class.java]
        } ?: throw Exception("Invalid Activity")
        Coroutines.main {
            requireActivity().hideKeyboard()
            mobileAppPhotoArrayList = ArrayList()

//            if (data.fromSource.isNullOrEmpty()) {
//                backpress()
//            } else {
//                when {
////                    data.fromSource.equals(getString(R.string.disaster_source)) -> {
////                        val disasterLog = viewModel.getDisasterLogForID(data.damageItemId!!)
////                        newDisasterLog = disasterLog
////                    }
////                    data.fromSource.equals("main inspection") -> {
////                        val disasterLog = viewModel.getDisasterLogForID(data.damageItemId!!)
////                        newDisasterLog = disasterLog
////                    }
////                    data.fromSource.equals(getString(R.string.new_structure_source)) -> {
//////                        val structure = viewModel.getStructureForID(data.structureID!!)
//////                        newStructure = structure
////                    }
////                    else -> {
////                        val inpsection = viewModel.getInspectionForID(data.inspectionId)
////                        newInspection = inpsection
////                    }
//                }

            ui.captureAnotherPhotoButton.setOnClickListener { save ->
                photoType = PhotoType.START
                launchCamera(CAMERA_INTENT_REQ_CODE, photoType)
            }

            ui.doneimagebutton.setOnClickListener { save ->
//                saveImagesAndExit(save)
            }
        }
    }

    private fun launchCamera(reqCode: Int, photoType: PhotoType) {
        Coroutines.main {

            toggleLongRunning(true)
            itemIdPhotoType["itemId"] = ""
            itemIdPhotoType["type"] = photoType.name

            Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                sharedViewModel.toggleLongRunning(true)
                val photoFile: File? = try {
                    viewModel.photoUtil.createImageFile()
                } catch (ex: IOException) {
                    ToastUtils().toastShort(requireContext(), ex.message)
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    cameraImageUri = viewModel.photoUtil.getUriFromPath(it.path)
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri)
                    takePictureIntent.putExtra(
                        MediaStore.EXTRA_SCREEN_ORIENTATION,
                        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri)
                    takePictureIntent.putExtra("photoType", itemIdPhotoType["type"])
                    takePictureIntent.putExtra("itemId", itemIdPhotoType["itemId"])
                    startActivityForResult(
                        takePictureIntent, reqCode
                    )
                }
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(
            requestCode,
            resultCode,
            data
        ) // If the image capture activity was called and was successful
        if (requestCode == CAMERA_INTENT_REQ_CODE && resultCode == Activity.RESULT_OK) {
            // Process the image and set it to the TextView
            if (cameraImageUri != null) {
                uiScope.launch(uiScope.coroutineContext) {
                    processAndSetImage(
                        cameraImageUri!!,
                        "CAMERA",
                        0.0,
                        0.0
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
                viewModel.photoUtil.deleteImageFile(filenamePath.toString())
            }
        }
        toggleLongRunning(false)
    }


    private fun processAndSetImage(
        imageUri: Uri,
        provider: String,
        latitude: Double,
        longitude: Double
    ) {

        if (provider == "CAMERA") {
            try {
                var myresult: String = imageUri.path ?: ""
                // get filename + ext of path
                val cut1 = myresult.lastIndexOf('/')
                if (cut1 != -1) myresult = myresult.substring(cut1 + 1)
                val fileName = myresult


                //  Location of picture
                filenamePath = viewModel.photoUtil.saveUnAllocatedImage(
                    requireContext(), config, fileName, imageUri, object : OnImageReadyListener {
                        override fun onImageReady(images: java.util.ArrayList<Image>) {
                            finishCaptureImage(images)
                        }

                        override fun onImageNotReady() {
                            finishCaptureImage(arrayListOf())
                        }
                    }) as HashMap<String, String>

                if (currentLocation == null) {
                    ToastUtils().toastLong(
                        requireContext(),
                        "Location is ether Turned Off or Not Ready please check connection and try again"
                    )
                } else {
                    processPhoto(currentLocation!!, filenamePath, itemIdPhotoType, imageUri)
                }
            } catch (e: Exception) {
                toast(R.string.error_getting_image)
                Timber.e(e)
                throw e
            }
        }


    }

    private fun finishCaptureImage(images: ArrayList<Image>) {
        val data = Intent()
        data.putParcelableArrayListExtra(Constants.EXTRA_IMAGES, images)
        requireActivity().setResult(Activity.RESULT_OK, data)
    }

    private fun processPhoto(
        photoLocation: LocationModel,
        filename_path: Map<String, String>,
        itemId_photoType: Map<String, String>,
        imageUri: Uri
    ) {
        Coroutines.main {
            val photo: JobItemEstimatesPhotoDTO?

            photo = viewModel.createUnallocatedPhoto(
                filenamePath = filename_path,
                currentLocation = photoLocation,
                pointLocation = -1.0,
                itemidPhototype = itemId_photoType
            )
            mobileAppPhotoArrayList.add(photo)

        }

        GlideApp.with(this)
            .load(imageUri)
            .centerCrop()
            .into(ui.itemImage)

    }


private fun startPermissionRequest(permissions: Array<String>): Boolean {
    var allAccess = true
    for (i in permissions.indices) {
//                if (checkCallingOrSelfPermission(permissions[i]) == PackageManager.PERMISSION_DENIED) {
//                    allAccess = false
//                }
    }
    return allAccess
}

//        private fun saveImagesAndExit(view: View) {
//            Coroutines.main {
//                when {
//                    data.fromSource.equals(getString(R.string.disaster_source)) -> {
//                        //  mobileAppPhotoArrayList[0].comment = ui.imageComment.text.toString()
//                        val saved = viewModel.savePhotos(
//                            mobileAppPhotoArrayList,
//                            getString(R.string.disaster_source)
//                        )
//                        if (saved.isEmpty()) {
//                            ToastUtils().toastShort(requireContext(), "Image Save Error Try Again")
//                        } else {
//                            saveAndExit(view)
//                        }
//                    }
//                    data.fromSource.equals("main inspection") -> {
//                        // mobileAppPhotoArrayList[0].comment = ui.imageComment.text.toString()
//                        val saved = viewModel.savePhotos(mobileAppPhotoArrayList, "main inspection")
//                        if (saved.isEmpty()) {
//                            ToastUtils().toastShort(requireContext(), "Image Save Error Try Again")
//                        } else {
//                            val defect =
//                                viewModel.getDefectInspectionForID(newDisasterLog?.damageItemId!!)
//                            defect.inspectionPhotos = mobileAppPhotoArrayList
//                            viewModel.getSaveDefect(defect)
//
//                            saveAndExit(view)
//                        }
//                    }
//                    else -> {
//                        // mobileAppPhotoArrayList[0].comment = ui.imageComment.text.toString()
//                        val saved = viewModel.savePhotos(mobileAppPhotoArrayList, "new Structure")
//                        if (saved.isEmpty()) {
//                            ToastUtils().toastShort(requireContext(), "Image Save Error Try Again")
//                        } else {
//                            saveAndExit(view)
//                        }
//                    }
//                }
//            }
//        }

//        private fun saveAndExit(view: View) {
//            when {
//                data.fromSource.equals(getString(R.string.disaster_source)) -> {
//                    Coroutines.io {
//                        withContext(Dispatchers.Main.immediate) {
//                            val navDirection =
//                                CaptureItemPhotosFragmentDirections.actionNavCaptureImageToNavDisasterHomeTwo(
//                                    data.damageItemId,
//                                    damageType = data.structureType
//                                )
//                            Navigation.findNavController(view).navigate(navDirection)
//                        }
//                    }
//                }
//                data.fromSource.equals("main inspection") -> {
//
//                    Coroutines.io {
//                        withContext(Dispatchers.Main.immediate) {
//                            val navDirection =
//                                CaptureItemPhotosFragmentDirections.actionNavCaptureImageToNavDisasterInspection(
//                                    defectItem = data.defectItem,
//                                    damageItemId = data.damageItemId,
//                                    structureType = data.structureType
//                                )
//                            Navigation.findNavController(view).navigate(navDirection)
//                        }
//                    }
//                }
//
//            }
//        }

//        private fun validateForm(): Boolean {
//            return if (data.fromSource.equals("getString(R.string.disaster_source)")) {
//                if (0 == mobileAppPhotoArrayList.size) {
//                    ToastUtils().toastLong(
//                        requireContext(),
//                        requireActivity().getString(R.string.photo_missing)
//                    )
//                    false
//                } else {
//                    true
//                }
//            } else if (data.fromSource.equals("main inspection")) {
//                if (0 == mobileAppPhotoArrayList.size) {
//                    ToastUtils().toastLong(
//                        requireContext(),
//                        requireActivity().getString(R.string.photo_missing)
//                    )
//                    false
//                } else {
//                    true
//                }
//            } else {
//                if (data.inventoryItem.equals("Yes")) {
//                    if (0 == mobileAppPhotoArrayList.size) {
//                        ToastUtils().toastLong(
//                            requireContext(),
//                            requireActivity().getString(R.string.photo_missing)
//                        )
//                        false
//                    } else {
//                        true
//                    }
//                } else {
//                    if (0 == mobileAppPhotoArrayList.size) {
//                        ToastUtils().toastLong(
//                            requireContext(),
//                            requireActivity().getString(R.string.photo_missing)
//                        )
//                        false
//                    } else if (!isValidInput(ui.imageComment.text.toString())) {
//                        ui.imageComment.error = getString(R.string.invalid_char)
//                        false
//                    } else {
//                        true
//                    }
//                }
//            }
//        }


//override fun onDestroyView() {
//    super.onDestroyView()
//    _ui = null
//}
}
