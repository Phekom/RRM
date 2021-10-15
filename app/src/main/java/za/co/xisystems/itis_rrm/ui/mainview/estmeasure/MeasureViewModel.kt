/*
 * Updated by Shaun McDonald on 2021/01/25
 * Last modified on 2021/01/25 6:30 PM
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 */

package za.co.xisystems.itis_rrm.ui.mainview.estmeasure

import android.app.Application
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.*
import kotlinx.coroutines.*
import timber.log.Timber
import za.co.xisystems.itis_rrm.custom.events.XIEvent
import za.co.xisystems.itis_rrm.custom.results.XIResult
import za.co.xisystems.itis_rrm.data.localDB.entities.*
import za.co.xisystems.itis_rrm.data.repositories.MeasureCreationDataRepository
import za.co.xisystems.itis_rrm.data.repositories.OfflineDataRepository
import za.co.xisystems.itis_rrm.ui.custom.MeasureGalleryUIState
import za.co.xisystems.itis_rrm.ui.mainview.estmeasure.estimate_measure_item.EstimateMeasureItem
import za.co.xisystems.itis_rrm.utils.PhotoUtil
import za.co.xisystems.itis_rrm.utils.enums.PhotoQuality
import za.co.xisystems.itis_rrm.utils.lazyDeferred
import za.co.xisystems.itis_rrm.utils.uncaughtExceptionHandler

class MeasureViewModel(
    application: Application,
    private val measureCreationDataRepository: MeasureCreationDataRepository,
    private val offlineDataRepository: OfflineDataRepository

) : AndroidViewModel(application) {

    private var galleryMeasure: MutableLiveData<JobItemMeasureDTO> = MutableLiveData()
    var measureGalleryUIState: MutableLiveData<XIResult<MeasureGalleryUIState>> = MutableLiveData()
    private var job: Job = SupervisorJob()
    private var viewModelContext = job + Dispatchers.Main + uncaughtExceptionHandler
    val galleryBackup: MutableLiveData<String> = MutableLiveData()
    val offlineUserTaskList: Deferred<LiveData<List<ToDoListEntityDTO>>> by lazyDeferred {
        offlineDataRepository.getUserTaskList()
    }
    val user: Deferred<LiveData<UserDTO>> by lazyDeferred {
        measureCreationDataRepository.getUser()
    }
    val jobItemMeasure: MutableLiveData<JobItemMeasureDTO> = MutableLiveData()
    private val measureItemPhotos: MutableLiveData<List<JobItemMeasurePhotoDTO>> = MutableLiveData()
    var measuredJiNo: String = ""
    val estimateMeasureItem: MutableLiveData<EstimateMeasureItem> = MutableLiveData()
    private lateinit var workflowStatus: MutableLiveData<XIEvent<XIResult<String>>>
    var workflowState: MutableLiveData<XIResult<String>?> = MutableLiveData()
    val backupJobId: MutableLiveData<String> = MutableLiveData()
    private val superJob = SupervisorJob()
    private val mainContext = (Job(superJob) + Dispatchers.Main + uncaughtExceptionHandler)
    private val ioContext = (Job(superJob) + Dispatchers.IO + uncaughtExceptionHandler)
    private val photoUtil = PhotoUtil.getInstance(getApplication())

    init {
        initWorkflowChannels()
    }

    private fun initWorkflowChannels() {
        viewModelScope.launch(viewModelContext) {
            workflowStatus = measureCreationDataRepository.workflowStatus
            launch(mainContext) {
                galleryMeasure.observeForever {
                    viewModelScope.launch(job + Dispatchers.Main + uncaughtExceptionHandler) {
                        generateGallery(it)
                    }
                }
                workflowState = Transformations.map(workflowStatus) {
                    it.getContentIfNotHandled()
                } as MutableLiveData<XIResult<String>?>
            }
        }
    }

    companion object {
        const val galleryError = "Failed to retrieve itemMeasure for Gallery"
    }

    private fun setGalleryMeasure(value: JobItemMeasureDTO) {
        galleryMeasure.postValue(value)
    }

    private fun generateGallery(measureItem: JobItemMeasureDTO) = viewModelScope.launch(ioContext) {
        try {
            galleryBackup.postValue(measureItem.itemMeasureId)
            val measureDescription =
                measureItem.projectItemId?.let {
                    getDescForProjectId(it)
                }

            val photoQuality = when (measureItem.jobItemMeasurePhotos.size) {
                in 1..4 -> PhotoQuality.HIGH
                in 5..16 -> PhotoQuality.MEDIUM
                else -> PhotoQuality.THUMB
            }

            val bitmaps = measureItem.jobItemMeasurePhotos.mapNotNull { photo ->

                try {
                    val uri = photo.filename?.let { fileName ->
                        photoUtil.getPhotoPathFromExternalDirectory(fileName)
                    }
                    val bitmap = uri?.let { qualifiedUri ->
                        photoUtil.getPhotoBitmapFromFile(
                            qualifiedUri,
                            photoQuality
                        )
                    }
                    Pair(uri!!, bitmap!!)
                } catch (ex: Exception) {
                    Timber.e(ex, "Failed to load ${photo.filename}")
                    null
                }
            }

            val uiState = MeasureGalleryUIState(
                description = measureDescription,
                qty = measureItem.qty,
                lineRate = measureItem.lineRate,
                photoPairs = bitmaps,
                lineAmount = measureItem.qty * measureItem.lineRate,
                jobItemMeasureDTO = measureItem
            )

            measureGalleryUIState.postValue(XIResult.Success(uiState))
        } catch (e: Exception) {
            Timber.e(e, galleryError)
            val galleryFail = XIResult.Error(e, galleryError)
            measureGalleryUIState.postValue(galleryFail)
        }
    }

    private suspend fun getDescForProjectId(projectItemId: String): String {
        return withContext(Dispatchers.IO) {
            measureCreationDataRepository.getProjectItemDescription(projectItemId)
        }
    }

    fun setJobItemMeasure(measurea1: JobItemMeasureDTO) {
        jobItemMeasure.value = measurea1
    }

    fun setMeasureItemPhotos(measurePhotoList: List<JobItemMeasurePhotoDTO>) {
        measureItemPhotos.postValue(measurePhotoList)
    }

    suspend fun getMeasureItemPhotos(itemMeasureId: String): LiveData<List<JobItemMeasurePhotoDTO>> {
        return withContext(Dispatchers.IO) {
            measureCreationDataRepository.getJobItemMeasurePhotosForItemMeasureID(itemMeasureId)
        }
    }

    fun setMeasureItem(measurea: EstimateMeasureItem) {
        estimateMeasureItem.postValue(measurea)
    }

    suspend fun getJobMeasureForActivityId(
        activityId: Int,
        activityId2: Int,
        activityId3: Int
    ): LiveData<List<JobItemEstimateDTO>> {
        return withContext(Dispatchers.IO + uncaughtExceptionHandler) {
            measureCreationDataRepository.getJobMeasureForActivityId(
                activityId,
                activityId2,
                activityId3
            ).distinctUntilChanged()
        }
    }

    suspend fun getProjectSectionIdForJobId(jobId: String): String {
        return withContext(Dispatchers.IO) {
            measureCreationDataRepository.getProjectSectionIdForJobId(jobId)
        }
    }

    suspend fun getRouteForProjectSectionId(sectionId: String): String {
        return withContext(Dispatchers.IO) {
            measureCreationDataRepository.getRouteForProjectSectionId(sectionId)
        }
    }

    suspend fun getSectionForProjectSectionId(sectionId: String): String {
        return withContext(Dispatchers.IO) {
            measureCreationDataRepository.getSectionForProjectSectionId(sectionId)
        }
    }

    suspend fun getItemDescription(jobId: String): String = measureCreationDataRepository.getItemDescription(jobId)
    suspend fun getDescForProjectItemId(projectItemId: String): String {
        return withContext(Dispatchers.IO) {
            measureCreationDataRepository.getProjectItemDescription(projectItemId)
        }
    }

    suspend fun getItemJobNo(jobId: String): String = measureCreationDataRepository.getItemJobNo(jobId)
    suspend fun getJobMeasureItemsPhotoPath(itemMeasureId: String): List<String> {
        return withContext(Dispatchers.IO) {
            measureCreationDataRepository.getJobMeasureItemsPhotoPath(itemMeasureId)
        }
    }

    fun deleteItemMeasureFromList(itemMeasureId: String) {
        measureCreationDataRepository.deleteItemMeasurefromList(itemMeasureId)
    }

    fun deleteItemMeasurePhotoFromList(itemMeasureId: String) {
        measureCreationDataRepository.deleteItemMeasurephotofromList(itemMeasureId)
    }

    suspend fun processWorkflowMove(
        userId: String,
        jobId: String,
        jimNo: String?,
        contractVoId: String?,
        mSures: ArrayList<JobItemMeasureDTO>,
        activity: FragmentActivity,
        itemMeasureJob: JobDTO
    ): Job = viewModelScope.launch(ioContext) {

        // Flush any previous results
        withContext(mainContext) {
            resetWorkState()
        }

        try {
            measureCreationDataRepository.saveMeasurementItems(
                userId,
                jobId,
                jimNo,
                contractVoId,
                mSures,
                activity,
                itemMeasureJob
            )
        } catch (e: Exception) {
            workflowState.postValue(XIResult.Error(e, e.message!!))
        }
    }

    suspend fun getJobItemMeasuresForJobIdAndEstimateId(
        jobId: String?
    ): LiveData<List<JobItemMeasureDTO>> {
        return withContext(Dispatchers.IO) {
            measureCreationDataRepository.getJobItemMeasuresForJobIdAndEstimateId(jobId)
        }
    }

    suspend fun getJobItemMeasuresForJobIdAndEstimateId2(
        jobId: String?,
        estimateId: String
    ): LiveData<List<JobItemMeasureDTO>> = withContext(Dispatchers.IO + uncaughtExceptionHandler) {
        return@withContext measureCreationDataRepository.getJobItemMeasuresForJobIdAndEstimateId2(
            jobId,
            estimateId
        )
    }

    suspend fun getJobItemsToMeasureForJobId(jobID: String?): LiveData<List<JobItemEstimateDTO>> = withContext(Dispatchers.IO) {
        return@withContext measureCreationDataRepository.getJobItemsToMeasureForJobId(jobID)
    }

    suspend fun getItemForItemId(projectItemId: String?): LiveData<ProjectItemDTO> = withContext(Dispatchers.IO) {
        return@withContext measureCreationDataRepository.getItemForItemId(projectItemId)
    }

    suspend fun getJobFromJobId(jobId: String?): LiveData<JobDTO> {
        return withContext(Dispatchers.IO) {
            measureCreationDataRepository.getSingleJobFromJobId(jobId)
        }
    }

    suspend fun setJobItemMeasureImages(
        jobItemMeasurePhotoList: ArrayList<JobItemMeasurePhotoDTO>,
        estimateId: String?,
        selectedJobItemMeasure: JobItemMeasureDTO
    ) {
        measureCreationDataRepository.setJobItemMeasureImages(
            jobItemMeasurePhotoList,
            estimateId,
            selectedJobItemMeasure
        )
    }

    fun setBackupJobId(jobId: String) {
        backupJobId.postValue(jobId)
    }

    fun generateGalleryUI(itemMeasureId: String): Job =
        viewModelScope.launch(viewModelContext) {
            try {
                galleryBackup.postValue(itemMeasureId)
                val itemMeasureDTO = getJobItemMeasureByItemMeasureId(itemMeasureId)
                itemMeasureDTO.observeForever {
                    it?.let {
                        setGalleryMeasure(it)
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, galleryError)
                val galleryFail = XIResult.Error(e, galleryError)
                measureGalleryUIState.postValue(galleryFail)
            }
        }

    private suspend fun getJobItemMeasureByItemMeasureId(itemMeasureId: String): LiveData<JobItemMeasureDTO> {
        return withContext(Dispatchers.IO) {
            measureCreationDataRepository.getJobItemMeasureByItemMeasureId(itemMeasureId)
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
        workflowState = MutableLiveData()
        workflowStatus = MutableLiveData()
        superJob.cancelChildren()
        super.onCleared()
    }

    private fun resetWorkState() {
        workflowState = MutableLiveData()
        workflowStatus = MutableLiveData()
        initWorkflowChannels()
    }
}
