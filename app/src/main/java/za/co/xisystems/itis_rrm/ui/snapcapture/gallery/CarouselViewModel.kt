package za.co.xisystems.itis_rrm.ui.snapcapture.gallery

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.viewModelScope
import androidx.room.Transaction
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import za.co.xisystems.itis_rrm.data.localDB.entities.UnallocatedPhotoDTO
import za.co.xisystems.itis_rrm.data.repositories.CapturedPictureRepository
import za.co.xisystems.itis_rrm.data.repositories.UserRepository
import za.co.xisystems.itis_rrm.utils.DispatcherProvider
import za.co.xisystems.itis_rrm.services.LocationModel
import za.co.xisystems.itis_rrm.utils.DateUtil
import za.co.xisystems.itis_rrm.utils.DefaultDispatcherProvider
import za.co.xisystems.itis_rrm.utils.PhotoUtil
import za.co.xisystems.itis_rrm.utils.SqlLitUtils
import za.co.xisystems.itis_rrm.utils.lazyDeferred
import java.util.Date

class CarouselViewModel(
    private val userRepository: UserRepository,
    private val capturedPictureRepository: CapturedPictureRepository,
    application: Application,
    dispatchers: DispatcherProvider = DefaultDispatcherProvider()
) : AndroidViewModel(application) {
    private val unallocatedPhotos: LiveData<List<UnallocatedPhotoDTO>>? = capturedPictureRepository.capturedPhotos
    private val searchResults: MutableLiveData<List<UnallocatedPhotoDTO>> = capturedPictureRepository.searchResults
    private val superJob = SupervisorJob()
    private val ioContext = Job(superJob) + dispatchers.io()
    private val mainContext = Job(superJob) + dispatchers.main()
    private val photoUtil: PhotoUtil? = null

    val currentUser by lazyDeferred {
        userRepository.getUser().distinctUntilChanged()
    }

    fun getUnallocatedPhotos(): LiveData<List<UnallocatedPhotoDTO>>? {
        return unallocatedPhotos
    }

    fun searchUnallocatedPhotos(criteria: String) = viewModelScope.launch(ioContext) {
        capturedPictureRepository.searchUnallocatedPhotos(criteria)
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

        val capturedPhoto = UnallocatedPhotoDTO(
            id = 0,
            descr = "",
            filename = filenamePath["filename"] ?: error(""),
            photoDate = DateUtil.dateToString(Date())!!,
            photoLatitude = currentLocation.latitude,
            photoLongitude = currentLocation.longitude,
            photoId = photoId,
            kmMarker = pointLocation,
            photoPath = filenamePath["path"] ?: error(""),
            recordSynchStateId = 0,
            recordVersion = 0,
            routeMarker = currentLocation.toString(),
            allocated = false,
            pxHeight = -1,
            pxWidth = -1
        )

        capturedPictureRepository.insertUnallocatedPhoto(capturedPhoto)
    }

    @Transaction
    private fun eraseExistingPhoto(fileName: String, photoPath: String) =
        viewModelScope.launch(ioContext) {
            if (photoUtil?.photoExist(fileName)!!) {
                photoUtil.deleteImageFile(photoPath)
            }
        }
}
