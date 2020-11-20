package za.co.xisystems.itis_rrm.ui.mainview.approvemeasure

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import za.co.xisystems.itis_rrm.custom.errors.XIErrorHandler.UNKNOWN_ERROR
import za.co.xisystems.itis_rrm.custom.events.XIEvent
import za.co.xisystems.itis_rrm.custom.results.XIError
import za.co.xisystems.itis_rrm.custom.results.XIProgress
import za.co.xisystems.itis_rrm.custom.results.XIResult
import za.co.xisystems.itis_rrm.custom.results.XISuccess
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemMeasureDTO
import za.co.xisystems.itis_rrm.data.repositories.MeasureApprovalDataRepository
import za.co.xisystems.itis_rrm.data.repositories.OfflineDataRepository
import za.co.xisystems.itis_rrm.extensions.getDistinct
import za.co.xisystems.itis_rrm.ui.custom.MeasureGalleryUIState
import za.co.xisystems.itis_rrm.ui.mainview.approvemeasure.approveMeasure_Item.ApproveMeasureItem
import za.co.xisystems.itis_rrm.utils.PhotoUtil
import za.co.xisystems.itis_rrm.utils.enums.PhotoQuality
import za.co.xisystems.itis_rrm.utils.enums.WorkflowDirection
import za.co.xisystems.itis_rrm.utils.lazyDeferred
import za.co.xisystems.itis_rrm.utils.uncaughtExceptionHandler

/**
 * Created by Francis Mahlava on 03,October,2019
 */
class ApproveMeasureViewModel(
    application: Application,
    private val measureApprovalDataRepository: MeasureApprovalDataRepository,
    private val offlineDataRepository: OfflineDataRepository
) : AndroidViewModel(application) {

    val offlineUserTaskList by lazyDeferred {
        offlineDataRepository.getUserTaskList()
    }

    val user by lazyDeferred {
        measureApprovalDataRepository.getUser()
    }

    val measureApprovalItem: MutableLiveData<ApproveMeasureItem> = MutableLiveData()

    var measureGalleryUIState: MutableLiveData<XIResult<MeasureGalleryUIState>> = MutableLiveData()

    private val superJob = SupervisorJob()

    private var galleryMeasure: MutableLiveData<JobItemMeasureDTO> = MutableLiveData()

    private lateinit var workflowStatus: LiveData<XIEvent<XIResult<String>>>

    var workflowState: MutableLiveData<XIResult<String>?> = MutableLiveData()

    init {
        viewModelScope.launch(Job(superJob) + Dispatchers.Main + uncaughtExceptionHandler) {
            workflowStatus = measureApprovalDataRepository.workflowStatus.distinctUntilChanged()
            workflowStatus.observeForever {
                it?.let {
                    workflowState.postValue(it.getContentIfNotHandled())
                }
            }
            galleryMeasure.observeForever {
                viewModelScope.launch(Job(superJob) + Dispatchers.Main + uncaughtExceptionHandler) {
                    generateGallery(it)
                }
            }
        }
    }

    fun setApproveMeasureItem(measureapproval: ApproveMeasureItem) {
        measureApprovalItem.postValue(measureapproval)
    }

    suspend fun getJobApproveMeasureForActivityId(activityId: Int): LiveData<List<JobItemMeasureDTO>> {
        return withContext(Dispatchers.IO) {
            measureApprovalDataRepository.getJobApproveMeasureForActivityId(activityId).getDistinct()
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
            measureApprovalDataRepository.getJobMeasureItemsForJobId(jobID, actId).getDistinct()
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

    suspend fun approveMeasurements(
        userId: String,
        workflowDirection: WorkflowDirection,
        measurements: List<JobItemMeasureDTO>
    ) = viewModelScope.launch {
        workflowState.postValue(XIProgress(true))


        withContext(Dispatchers.IO + uncaughtExceptionHandler) {
            try {
                measureApprovalDataRepository.processWorkflowMove(userId, measurements, workflowDirection.value)

                workflowState.postValue(XISuccess("WORK_COMPLETE"))

            } catch (t: Throwable) {
                workflowState.postValue(XIError(t, t.message ?: UNKNOWN_ERROR))
            }
        }
    }

    suspend fun upDateMeasure(
        editQuantity: String,
        itemMeasureId: String?
    ): String {
        return withContext(Dispatchers.IO) {
            measureApprovalDataRepository.upDateMeasure(editQuantity, itemMeasureId!!)
        }
    }

    suspend fun getQuantityForMeasureItemId(itemMeasureId: String): LiveData<Double> {
        return withContext(Dispatchers.IO) {
            measureApprovalDataRepository.getQuantityForMeasureItemId(itemMeasureId)
        }
    }

    suspend fun generateGalleryUI(itemMeasureId: String) =
        viewModelScope.launch(Job(superJob) + Dispatchers.Main + uncaughtExceptionHandler) {
            try {
                getJobItemMeasureByItemMeasureId(itemMeasureId).observeForever {
                    it?.let {
                        galleryMeasure.postValue(it)
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, galleryError)
                val galleryFail = XIError(e, galleryError)
                measureGalleryUIState.postValue(galleryFail)
            }
        }

    suspend fun getLineRateForMeasureItemId(itemMeasureId: String): LiveData<Double> {
        return withContext(Dispatchers.IO) {
            measureApprovalDataRepository.getLineRateForMeasureItemId(itemMeasureId)
        }
    }

    private suspend fun generateGallery(measureItem: JobItemMeasureDTO) =
        viewModelScope.launch(viewModelScope.coroutineContext) {
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
                    val bmap = uri?.let { mUri ->
                        PhotoUtil.getPhotoBitMapFromFile(
                            this@ApproveMeasureViewModel.getApplication(),
                            mUri,
                            photoQuality
                        )
                    }
                    Pair(uri!!, bmap!!)
                }

                val uiState = MeasureGalleryUIState(
                    description = measureDescription,
                    qty = measureItem.qty,
                    lineRate = measureItem.lineRate,
                    photoPairs = bitmaps,
                    lineAmount = measureItem.qty * measureItem.lineRate,
                    jobItemMeasureDTO = measureItem

                )

                measureGalleryUIState.postValue(XISuccess(uiState))
            } catch (t: Throwable) {
                val message = "$galleryError: ${t.message ?: UNKNOWN_ERROR}"
                Timber.e(t, message)
                val galleryFail = XIError(t, message)
                measureGalleryUIState.postValue(galleryFail)
            }
        }

    companion object {
        const val galleryError = "Failed to retrieve itemMeasure for Gallery"
    }

    private suspend fun getJobItemMeasureByItemMeasureId(itemMeasureId: String): LiveData<JobItemMeasureDTO> {
        return withContext(Dispatchers.IO) {
            measureApprovalDataRepository.getJobItemMeasureByItemMeasureId(itemMeasureId).getDistinct()
        }
    }

    /**
     * This method will be called when this ViewModel is no longer used and will be destroyed.
     *
     *
     * It is useful when ViewModel observes some data and you need to clear this subscription to
     * prevent a leak of this ViewModel.
     */
    override fun onCleared() {
        superJob.cancelChildren()
        workflowState = MutableLiveData()
        super.onCleared()
    }
}
