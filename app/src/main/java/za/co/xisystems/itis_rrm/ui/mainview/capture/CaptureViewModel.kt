package za.co.xisystems.itis_rrm.ui.mainview.capture

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import za.co.xisystems.itis_rrm.data.localDB.entities.UnallocatedPhotoDTO
import za.co.xisystems.itis_rrm.data.repositories.CapturedPictureRepository
import za.co.xisystems.itis_rrm.data.repositories.UserRepository
import za.co.xisystems.itis_rrm.forge.DefaultDispatcherProvider
import za.co.xisystems.itis_rrm.forge.DispatcherProvider
import za.co.xisystems.itis_rrm.services.LocationModel
import za.co.xisystems.itis_rrm.utils.DateUtil
import za.co.xisystems.itis_rrm.utils.SqlLitUtils
import za.co.xisystems.itis_rrm.utils.lazyDeferred
import java.util.Date

class CaptureViewModel(
    private val unallocatedRepository: CapturedPictureRepository,
    private val userRepository: UserRepository,
    private val dispatchers: DispatcherProvider = DefaultDispatcherProvider(),
    application: Application

): AndroidViewModel(application) {
    private val unallocatedPhotos: LiveData<List<UnallocatedPhotoDTO>>? = unallocatedRepository.capturedPhotos
    private val searchResults: MutableLiveData<List<UnallocatedPhotoDTO>>
    private val superJob = SupervisorJob()
    private val ioContext = Job(superJob) + dispatchers.io()
    private val mainContext = Job(superJob) + dispatchers.main()

    init {
        searchResults = unallocatedRepository.searchResults
    }

    val currentUser by lazyDeferred {
        userRepository.getUser().distinctUntilChanged()
    }

    fun getUnallocatedPhotos(): LiveData<List<UnallocatedPhotoDTO>>? {
        return unallocatedPhotos
    }

    fun searchUnallocatedPhotos(criteria: String) = viewModelScope.launch(ioContext) {
        unallocatedRepository.searchUnallocatedPhotos(criteria)
    }

    fun getSearchResults(): LiveData<List<UnallocatedPhotoDTO>> {
        return searchResults
    }

    fun createUnallocatedPhoto(
        currentLocation: LocationModel,
        filenamePath: HashMap<String, String>
    ): UnallocatedPhotoDTO {
        val unallocatedPhotoId = SqlLitUtils.generateUuid()
        return UnallocatedPhotoDTO(
            id = 0,
            descr = "Watch this space for TFLite predictions",
            filename = filenamePath["filename"]!!,
            photoDate = DateUtil.dateToString(Date())!!,
            photoId = unallocatedPhotoId,
            photoLatitude = currentLocation.latitude,
            photoLongitude = currentLocation.longitude,
            kmMarker = 0.0,
            photoPath = filenamePath["path"]!!,
            recordSynchStateId = 0,
            recordVersion = 0,
            routeMarker = null,
            allocated = false
        )
    }

    fun saveUnallocatedPhoto(photo: UnallocatedPhotoDTO) = viewModelScope.launch(ioContext) {
        unallocatedRepository.insertUnallocatedPhoto(photo)
    }
}
