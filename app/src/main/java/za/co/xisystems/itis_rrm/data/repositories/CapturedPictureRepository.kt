package za.co.xisystems.itis_rrm.data.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.Transaction
import kotlinx.coroutines.*
import za.co.xisystems.itis_rrm.data.localDB.AppDatabase
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimatesPhotoDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.UnallocatedPhotoDTO
import za.co.xisystems.itis_rrm.forge.DefaultDispatcherProvider
import za.co.xisystems.itis_rrm.forge.DispatcherProvider

class CapturedPictureRepository(
    private val appDb: AppDatabase,
    private val dispatchers: DispatcherProvider = DefaultDispatcherProvider()
) {
    val capturedPhotos: LiveData<List<UnallocatedPhotoDTO>>?

    val searchResults = MutableLiveData<List<UnallocatedPhotoDTO>>()

    // createUnallocatedPhoto
    init {
        capturedPhotos = appDb.getUnallocatedPhotoDao().getAllPhotos()
    }

    val coroutineScope = CoroutineScope(dispatchers.main())

    fun insertUnallocatedPhoto(unallocatedPhoto: UnallocatedPhotoDTO) {
        coroutineScope.launch(dispatchers.io()) {
            insertUnallocatedPhotoAsync(unallocatedPhoto)
        }
    }

    private suspend fun insertUnallocatedPhotoAsync(unallocatedPhoto: UnallocatedPhotoDTO) {
        appDb.getUnallocatedPhotoDao().insertUnallocatedPhoto(unallocatedPhoto)
    }

    // retakeUnallocatedPhoto
    fun retakeUnallocatedPhoto(retakenUnallocatedPhoto: UnallocatedPhotoDTO) {
        coroutineScope.launch(dispatchers.io()) {
            updateUnallocatedPhotoAsync(retakenUnallocatedPhoto)
        }
    }

    fun deleteUnallocatedPhoto(unallocatedPhoto: UnallocatedPhotoDTO) {
        coroutineScope.launch(dispatchers.io()) {
            deleteUnallocatedPhotoAsync(unallocatedPhoto)
        }
    }

    private fun deleteUnallocatedPhotoAsync(unallocatedPhoto: UnallocatedPhotoDTO) {
        appDb.getUnallocatedPhotoDao().deletePhoto(unallocatedPhoto)
    }

    private suspend fun updateUnallocatedPhotoAsync(retakenUnallocatedPhoto: UnallocatedPhotoDTO) {
        appDb.getUnallocatedPhotoDao().updateUnallocatedPhoto(retakenUnallocatedPhoto)
    }

    // searchUnallocatedPhotos
    fun searchUnallocatedPhotos(criteria: String) {
        coroutineScope.launch(dispatchers.main()) {
            searchResults.value = searchUnallocatedPhotoAsync(criteria.toRoomSearchString()).await()
        }
    }

    @Transaction
    suspend fun backupEstimatePhoto(photoDTO: JobItemEstimatesPhotoDTO):
            JobItemEstimatesPhotoDTO = withContext(dispatchers.io()) {
        if (appDb.getJobItemEstimatePhotoDao()
                .checkIfJobItemEstimatePhotoExistsByPhotoId(photoDTO.photoId)
        ) {
            appDb.getJobItemEstimatePhotoDao().updateJobItemEstimatePhoto(photoDTO)
        } else {
            appDb.getJobItemEstimatePhotoDao().insertJobItemEstimatePhoto(photoDTO)
        }
        return@withContext appDb.getJobItemEstimatePhotoDao().getJobItemEstimatePhoto(photoDTO.photoId)
    }

    private fun searchUnallocatedPhotoAsync(criteria: String): Deferred<List<UnallocatedPhotoDTO>?> =
        coroutineScope.async(dispatchers.io()) {
            return@async appDb.getUnallocatedPhotoDao().searchUnallocatedPhotos(criteria)
        }

    // deleteExpiredPhotos
    fun deleteExpiredPhotos() {
        coroutineScope.launch(dispatchers.io()) {
            deleteExpiredPhotosAsync()
        }
    }

    private fun deleteExpiredPhotosAsync() {
        appDb.getUnallocatedPhotoDao().deleteExpiredPhotos()
    }

    // deleteAllUnallocatedPhotos
}
