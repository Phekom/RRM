package za.co.xisystems.itis_rrm.ui.mainview.capture

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.viewModelScope
import androidx.room.Transaction
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import za.co.xisystems.itis_rrm.custom.events.XIEvent
import za.co.xisystems.itis_rrm.data.localDB.entities.UnallocatedPhotoDTO
import za.co.xisystems.itis_rrm.data.repositories.CapturedPictureRepository
import za.co.xisystems.itis_rrm.data.repositories.UserRepository
import za.co.xisystems.itis_rrm.utils.DispatcherProvider
import za.co.xisystems.itis_rrm.services.LocationModel
import za.co.xisystems.itis_rrm.utils.DateUtil
import za.co.xisystems.itis_rrm.utils.DefaultDispatcherProvider
import za.co.xisystems.itis_rrm.utils.PhotoUtil
import za.co.xisystems.itis_rrm.utils.SqlLitUtils
import za.co.xisystems.itis_rrm.utils.enums.PhotoQuality
import za.co.xisystems.itis_rrm.utils.lazyDeferred
import java.io.File
import java.util.Date

class CaptureViewModel(
    private val capturedPicsRepository: CapturedPictureRepository,
    private val userRepository: UserRepository,
    dispatchers: DispatcherProvider = DefaultDispatcherProvider(),
    application: Application

) : AndroidViewModel(application) {
    val lastPhoto: MutableLiveData<XIEvent<UnallocatedPhotoDTO>> = MutableLiveData()
    private val unallocatedPhotos: LiveData<List<UnallocatedPhotoDTO>>? = capturedPicsRepository.capturedPhotos
    private val searchResults: MutableLiveData<List<UnallocatedPhotoDTO>> = capturedPicsRepository.searchResults
    private val superJob = SupervisorJob()
    private val ioContext = Job(superJob) + dispatchers.io()
    private val mainContext = Job(superJob) + dispatchers.main()
    private val photoUtil: PhotoUtil = PhotoUtil.getInstance(application.applicationContext)

    val currentUser by lazyDeferred {
        userRepository.getUser().distinctUntilChanged()
    }

    fun getUnallocatedPhotos(): LiveData<List<UnallocatedPhotoDTO>>? {
        return unallocatedPhotos
    }

    fun searchUnallocatedPhotos(criteria: String) = viewModelScope.launch(ioContext) {
        capturedPicsRepository.searchUnallocatedPhotos(criteria)
    }

    fun getSearchResults(): LiveData<List<UnallocatedPhotoDTO>> {
        return searchResults
    }

    fun createUnallocatedPhoto(
        filenamePath: Map<String, String>,
        currentLocation: LocationModel,
        pointLocation: Double
    ) = viewModelScope.launch(ioContext) {

        val photoId = SqlLitUtils.generateUuid()
        val bitmap = photoUtil.getPhotoBitmapFromFile(
            Uri.fromFile(File(filenamePath["path"]!!)),
            PhotoQuality.ORIGINAL
        )
        val newPhoto = UnallocatedPhotoDTO(
            id = 0,
            descr = "Watch this space for TFLite predictions",
            filename = filenamePath["filename"]!!,
            photoDate = DateUtil.dateToString(Date())!!,
            photoId = photoId,
            photoLatitude = currentLocation.latitude,
            photoLongitude = currentLocation.longitude,
            kmMarker = 0.0,
            photoPath = filenamePath["path"]!!,
            recordSynchStateId = 0,
            recordVersion = 0,
            routeMarker = currentLocation.toString(),
            allocated = false,
            pxHeight = bitmap?.height ?: -1,
            pxWidth = bitmap?.width ?: -1
        )

        capturedPicsRepository.insertUnallocatedPhoto(newPhoto)
        bitmap?.recycle()
        withContext(mainContext) {
            lastPhoto.value = XIEvent(newPhoto)
        }
    }

    @Transaction
    private fun eraseExistingPhoto(photoId: String, fileName: String, photoPath: String) =
        viewModelScope.launch(ioContext) {
            capturedPicsRepository.deleteUnallocatedPhotoById(photoId)
            if (photoUtil.photoExist(fileName)) {
                photoUtil.deleteImageFile(photoPath)
            }
        }

    fun saveUnallocatedPhoto(photo: UnallocatedPhotoDTO) = viewModelScope.launch(ioContext) {
        capturedPicsRepository.insertUnallocatedPhoto(photo)
    }
}
