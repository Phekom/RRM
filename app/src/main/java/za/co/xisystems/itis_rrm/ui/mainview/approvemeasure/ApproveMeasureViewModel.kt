package za.co.xisystems.itis_rrm.ui.mainview.approvemeasure

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemMeasureDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.ToDoListEntityDTO
import za.co.xisystems.itis_rrm.data.repositories.MeasureApprovalDataRepository
import za.co.xisystems.itis_rrm.data.repositories.OfflineDataRepository
import za.co.xisystems.itis_rrm.ui.custom.GalleryUIState
import za.co.xisystems.itis_rrm.ui.mainview.approvemeasure.approveMeasure_Item.ApproveMeasureItem
import za.co.xisystems.itis_rrm.utils.PhotoUtil
import za.co.xisystems.itis_rrm.utils.enums.PhotoQuality
import za.co.xisystems.itis_rrm.utils.lazyDeferred
import za.co.xisystems.itis_rrm.utils.results.XIError
import za.co.xisystems.itis_rrm.utils.results.XIResult
import za.co.xisystems.itis_rrm.utils.results.XISuccess
import za.co.xisystems.itis_rrm.utils.uncaughtExceptionHandler

/**
 * Created by Francis Mahlava on 03,October,2019
 */
class ApproveMeasureViewModel(
    application: Application,
    private val measureApprovalDataRepository: MeasureApprovalDataRepository,
    private val offlineDataRepository: OfflineDataRepository
) : AndroidViewModel(application) {

    //    suspend fun getJobApproveMeasureForActivityId(activityId: Int): LiveData<List<JobItemMeasureDTO>> {
//        return withContext(Dispatchers.IO) {
//            offlineDataRepository.getJobApproveMeasureForActivityId(activityId)
//        }
//    }

    //    suspend fun getJobFromJobId(jobId: String): JobDTO {
//        return withContext(Dispatchers.IO) {
//            offlineDataRepository.getUpdatedJob(jobId)
//        }
//    }

    val offlineUserTaskList by lazyDeferred {
        offlineDataRepository.getUserTaskList()
    }
    val user by lazyDeferred {
        measureApprovalDataRepository.getUser()
    }
    val measureapproval_Item = MutableLiveData<ApproveMeasureItem>()
    var galleryUIState: MutableLiveData<XIResult<GalleryUIState>> = MutableLiveData()
    val job = SupervisorJob()

    var galleryMeasure: MutableLiveData<JobItemMeasureDTO> = MutableLiveData()

    init {
        galleryMeasure.observeForever {
            viewModelScope.launch(job + Dispatchers.Main + uncaughtExceptionHandler) {
                generateGallery(it)
            }
        }
    }

    fun setApproveMeasureItem(measureapproval: ApproveMeasureItem) {
        measureapproval_Item.postValue(measureapproval)
    }

    suspend fun getJobApproveMeasureForActivityId(activityId: Int): LiveData<List<JobItemMeasureDTO>> {
        return withContext(Dispatchers.IO) {
            measureApprovalDataRepository.getJobApproveMeasureForActivityId(activityId)
        }
    }

    suspend fun getJobsMeasureForActivityId(
        estimateComplete: Int,
        measureComplete: Int,
        estWorksComplete: Int,
        jobApproved: Int
    ): LiveData<List<JobDTO>> {
        return withContext(Dispatchers.IO) {
            measureApprovalDataRepository.getJobsMeasureForActivityId(
                estimateComplete,
                measureComplete,
                estWorksComplete,
                jobApproved
            )
        }
    }

    suspend fun getEntitiesListForActivityId(activityId: Int): LiveData<List<ToDoListEntityDTO>> {
        return withContext(Dispatchers.IO) {
            measureApprovalDataRepository.getEntitiesListForActivityId(activityId)
        }
    }

    suspend fun getProjectSectionIdForJobId(jobId: String): String {
        return withContext(Dispatchers.IO) {
            measureApprovalDataRepository.getProjectSectionIdForJobId(jobId)
        }
    }

    suspend fun getRouteForProjectSectionId(sectionId: String): String {
        return withContext(Dispatchers.IO) {
            measureApprovalDataRepository.getRouteForProjectSectionId(sectionId)
        }
    }

    suspend fun getSectionForProjectSectionId(sectionId: String): String {
        return withContext(Dispatchers.IO) {
            measureApprovalDataRepository.getSectionForProjectSectionId(sectionId)
        }
    }

    suspend fun getItemDesc(jobId: String): String {
        return withContext(Dispatchers.IO) {
            measureApprovalDataRepository.getItemDescription(jobId)
        }
    }

    suspend fun getDescForProjectId(projectItemId: String): String {
        return withContext(Dispatchers.IO) {
            measureApprovalDataRepository.getProjectItemDescription(projectItemId)
        }
    }

    suspend fun getJobMeasureItemsForJobId(
        jobID: String?,
        actId: Int
    ): LiveData<List<JobItemMeasureDTO>> {
        return withContext(Dispatchers.IO) {
            measureApprovalDataRepository.getJobMeasureItemsForJobId(jobID, actId)
        }
    }

    suspend fun getUOMForProjectItemId(projectItemId: String): String {
        return withContext(Dispatchers.IO) {
            measureApprovalDataRepository.getUOMForProjectItemId(projectItemId)
        }
    }

    suspend fun getJobMeasureItemsPhotoPath(itemMeasureId: String): List<String> {
        return withContext(Dispatchers.IO) {
            measureApprovalDataRepository.getJobMeasureItemPhotoPaths(itemMeasureId)
        }
    }

    suspend fun processWorkflowMove(
        userId: String,
        trackRouteId: String,
        description: String?,
        direction: Int
    ): String {
        return withContext(Dispatchers.IO) {
            measureApprovalDataRepository.processWorkflowMove(
                userId,
                trackRouteId,
                description,
                direction
            )
        }
    }

    suspend fun upDateMeasure(
        new_quantity: String,
        itemMeasureId: String?
    ): String {
        return withContext(Dispatchers.IO) {
            measureApprovalDataRepository.upDateMeasure(new_quantity, itemMeasureId!!)
        }
    }

    suspend fun getQuantityForMeasureItemId(itemMeasureId: String): LiveData<Double> {
        return withContext(Dispatchers.IO) {
            measureApprovalDataRepository.getQuantityForMeasureItemId(itemMeasureId)
        }
    }

    suspend fun generateGalleryUI(itemMeasureId: String) =
        viewModelScope.launch(job + Dispatchers.Main + uncaughtExceptionHandler) {
            try {
                val measureQuery = getJobItemMeasureByItemMeasureId(itemMeasureId).observeForever {
                    it?.let {
                        galleryMeasure.postValue(it)
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to retrieve itemMeasure for Gallery")
                val galleryFail = XIError(e, "Failed to retrieve itemMeasure for Gallery")
                galleryUIState.postValue(galleryFail)
            }
        }

    suspend fun getLineRateForMeasureItemId(itemMeasureId: String): LiveData<Double> {
        return withContext(Dispatchers.IO) {
            measureApprovalDataRepository.getLineRateForMeasureItemId(itemMeasureId)
        }
    }

    suspend fun generateGallery(measureItem: JobItemMeasureDTO) {
        try {

            val measureDescription =
                measureItem.projectItemId?.let {
                    getDescForProjectId(it)
                }

            val photoQuality = when (measureItem.jobItemMeasurePhotos.size) {
                in 1..4 -> PhotoQuality.HIGH
                in 5..10 -> PhotoQuality.MEDIUM
                else -> PhotoQuality.THUMB
            }

            val bitmaps = measureItem.jobItemMeasurePhotos.map { photo ->

                val uri = photo.filename?.let { fileName ->
                    PhotoUtil.getPhotoPathFromExternalDirectory(fileName)
                }
                val bmap = uri?.let { uri ->
                    PhotoUtil.getPhotoBitMapFromFile(
                        this@ApproveMeasureViewModel.getApplication(),
                        uri,
                        photoQuality
                    )
                }
                Pair(uri!!, bmap!!)
            }

            val uiState = GalleryUIState(
                description = measureDescription,
                qty = measureItem.qty,
                lineRate = measureItem.lineRate,
                photoPairs = bitmaps
            )

            uiState.lineAmount = uiState.qty * uiState.lineRate

            galleryUIState.postValue(XISuccess(uiState))
        } catch (e: Exception) {
            Timber.e(e, "Failed to retrieve itemMeasure for Gallery")
            val galleryFail = XIError(e, "Failed to retrieve itemMeasure for Gallery")
            galleryUIState.postValue(galleryFail)
        }
    }

    suspend fun getJobItemMeasureByItemMeasureId(itemMeasureId: String): LiveData<JobItemMeasureDTO> {
        return withContext(Dispatchers.IO) {
            measureApprovalDataRepository.getJobItemMeasureByItemMeasureId(itemMeasureId)
        }
    }
}
