package za.co.xisystems.itis_rrm.ui.mainview.capture

import android.Manifest
import android.content.Context
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.android.x.closestDI
import org.kodein.di.instance
import za.co.xisystems.itis_rrm.MainActivity
import za.co.xisystems.itis_rrm.MobileNavigationDirections
import za.co.xisystems.itis_rrm.base.LocationFragment
import za.co.xisystems.itis_rrm.data.localDB.entities.UnallocatedPhotoDTO
import za.co.xisystems.itis_rrm.databinding.FragmentCaptureGalleryBinding
import za.co.xisystems.itis_rrm.services.LocationModel
import za.co.xisystems.itis_rrm.ui.extensions.addZoomedImages
import za.co.xisystems.itis_rrm.ui.extensions.scaleForSize
import za.co.xisystems.itis_rrm.ui.scopes.UiLifecycleScope
import za.co.xisystems.itis_rrm.utils.Coroutines
import za.co.xisystems.itis_rrm.utils.PhotoUtil
import java.io.File
import java.util.HashMap

class CaptureGalleryFragment : LocationFragment() {

    companion object {
        fun newInstance() = CaptureGalleryFragment()
        const val REQUEST_CAMERA_PERMISSIONS = 5050
    }

    private lateinit var captureViewModel: CaptureViewModel
    override val di by closestDI()
    private var _ui: FragmentCaptureGalleryBinding? = null
    private val ui get() = _ui!!
    private val photoUtil: PhotoUtil by instance()
    private val galleryItems = ArrayList<UnallocatedPhotoDTO>()
    private val captureFactory: CaptureViewModelFactory by instance()
    private var imageUri: Uri? = null
    private var filenamePath = HashMap<String, String>()
    private var uiScope = UiLifecycleScope()

    /**
     * ActivityResultContract for taking a photograph
     */
    private val takePicture = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isSaved ->
        if (isSaved) {
            imageUri?.let { realUri ->
                processAndSetImage(realUri)
            }
        } else {
            imageUri?.let { failedUri ->
                uiScope.launch(dispatchers.io()) {
                    val filenamePath = File(failedUri.path!!)
                    photoUtil.deleteImageFile(filenamePath.toString())
                }
            }
        }
        this.photosDone()
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
        ui.addPhoto.setOnClickListener {
            initLaunchCamera()
        }
        ui.doneImageButton.setOnClickListener {
            val direction = MobileNavigationDirections.actionGlobalNavHome()
            Navigation.findNavController(this@CaptureGalleryFragment.requireView()).navigate(direction)
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
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                REQUEST_CAMERA_PERMISSIONS
            )
        } else {
            launchCamera()
        }
    }

    private fun setupObservers() {
        val pictureQuery = captureViewModel.getUnallocatedPhotos()
        pictureQuery?.observe(viewLifecycleOwner, { unallocatedPics ->
            if (!unallocatedPics.isNullOrEmpty()) {
                galleryItems.clear()
                galleryItems.addAll(unallocatedPics as ArrayList<UnallocatedPhotoDTO>)
                loadPictures(galleryItems)
            } else {
                if (galleryItems.size < 1) {
                    initLaunchCamera()
                }
            }
        })
    }

    private fun launchCamera() {
        this.takingPhotos()
        Coroutines.io {
            imageUri = photoUtil.getUri()!!
            withContext(Dispatchers.Main.immediate) {
                takePicture.launch(imageUri)
            }
        }
    }

    private fun loadPictures(galleryItems: ArrayList<UnallocatedPhotoDTO>) = Coroutines.main {
        val filenames = galleryItems.map { photo -> photo.photoPath }
        val photoPairs = photoUtil.prepareGalleryPairs(filenames)
        if (photoPairs.isNotEmpty()) {
            ui.estimateImageCollectionView.clearImages()
            ui.estimateImageCollectionView.scaleForSize(photoPairs.size)
            ui.estimateImageCollectionView.addZoomedImages(photoPairs, requireActivity())
            ui.estimateImageCollectionView.visibility = View.VISIBLE
        }
    }

    private fun processAndSetImage(realUri: Uri) {
        val photoLocation = this.getLocation()
        if (photoLocation != null) {
            persistLocatedPhoto(photoLocation, realUri)
        }
    }

    private fun persistLocatedPhoto(
        currentLocation: LocationModel,
        imageUri: Uri
    ) {
        Coroutines.io {
            filenamePath = photoUtil.saveImageToInternalStorage(
                imageUri
            ) as HashMap<String, String>

            captureViewModel.createUnallocatedPhoto(
                filenamePath = filenamePath,
                currentLocation = currentLocation,
                pointLocation = -1.0
            )
            val lastPhoto = captureViewModel.lastPhoto
            lastPhoto.observe(viewLifecycleOwner, { photoEvent ->
                val newPhoto = photoEvent.getContentIfNotHandled()
                if (newPhoto != null) {
                    Coroutines.ui {
                        galleryItems.add(newPhoto)
                        loadPictures(galleryItems)
                    }
                }
            })
        }
    }
}
