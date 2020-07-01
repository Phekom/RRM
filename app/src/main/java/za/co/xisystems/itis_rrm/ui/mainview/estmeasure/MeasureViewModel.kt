package za.co.xisystems.itis_rrm.ui.mainview.estmeasure

import android.app.Application
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import timber.log.Timber
import za.co.xisystems.itis_rrm.data.localDB.entities.*
import za.co.xisystems.itis_rrm.data.repositories.MeasureCreationDataRepository
import za.co.xisystems.itis_rrm.data.repositories.OfflineDataRepository
import za.co.xisystems.itis_rrm.ui.custom.GalleryUIState
import za.co.xisystems.itis_rrm.ui.mainview.estmeasure.estimate_measure_item.EstimateMeasureItem
import za.co.xisystems.itis_rrm.utils.PhotoUtil
import za.co.xisystems.itis_rrm.utils.enums.PhotoQuality
import za.co.xisystems.itis_rrm.utils.lazyDeferred
import za.co.xisystems.itis_rrm.utils.results.XIError
import za.co.xisystems.itis_rrm.utils.results.XIResult
import za.co.xisystems.itis_rrm.utils.results.XISuccess
import za.co.xisystems.itis_rrm.utils.uncaughtExceptionHandler

class MeasureViewModel(
    application: Application,
    private val measureCreationDataRepository: MeasureCreationDataRepository,
    private val offlineDataRepository: OfflineDataRepository

) : AndroidViewModel(application) {

    var galleryMeasure: MutableLiveData<JobItemMeasureDTO> = MutableLiveData()
    var galleryUIState: MutableLiveData<XIResult<GalleryUIState>> = MutableLiveData()
    private var job: Job = SupervisorJob()
    private var viewModelContext = job + Dispatchers.Main + uncaughtExceptionHandler

    init {
        viewModelScope.launch(viewModelContext) {

            val galleryJob = launch {
                galleryMeasure.observeForever {
                    viewModelScope.launch(job + Dispatchers.Main + uncaughtExceptionHandler) {
                        generateGallery(it)
                    }
                }
            }
        }
    }

    val galleryBackup: MutableLiveData<String> = MutableLiveData()

    suspend fun generateGallery(measureItem: JobItemMeasureDTO) {
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
                        PhotoUtil.getPhotoPathFromExternalDirectory(fileName)
                    }
                    val bitmap = uri?.let { uri ->
                        PhotoUtil.getPhotoBitMapFromFile(
                            this@MeasureViewModel.getApplication(),
                            uri,
                            photoQuality
                        )
                    }
                    Pair(uri!!, bitmap!!)
                } catch (ex: Exception) {
                    Timber.e(ex, "Failed to load ${photo.filename}")
                    null
                }
            }


            val uiState = GalleryUIState(
                description = measureDescription,
                qty = measureItem.qty,
                lineRate = measureItem.lineRate,
                photoPairs = bitmaps
            )

            uiState.lineAmount = uiState.qty * uiState.lineRate

            galleryUIState.postValue(XISuccess(uiState))
            // setJobItemMeasure(measureItem)

        } catch (e: Exception) {
            Timber.e(e, "Failed to retrieve itemMeasure for Gallery")
            val galleryFail = XIError(e, "Failed to retrieve itemMeasure for Gallery")
            galleryUIState.postValue(galleryFail)
        }
    }

    suspend fun getDescForProjectId(projectItemId: String): String {
        return withContext(Dispatchers.IO) {
            measureCreationDataRepository.getProjectItemDescription(projectItemId)
        }
    }

    val offlineUserTaskList: Deferred<LiveData<List<ToDoListEntityDTO>>> by lazyDeferred {
        offlineDataRepository.getUserTaskList()
    }

    val user: Deferred<LiveData<UserDTO>> by lazyDeferred {
        measureCreationDataRepository.getUser()
    }

    val measureJob: MutableLiveData<JobDTO> = MutableLiveData<JobDTO>()
    fun setMeasureJob(value: JobDTO) {
        measureJob.postValue(value)
    }

    val jobItemMeasure = MutableLiveData<JobItemMeasureDTO>()
    fun setJobItemMeasure(measurea1: JobItemMeasureDTO) {
        jobItemMeasure.postValue(measurea1)
    }

    val measureItemPhotos: MutableLiveData<List<JobItemMeasurePhotoDTO>> =
        MutableLiveData<List<JobItemMeasurePhotoDTO>>()

    fun setMeasureItemPhotos(measurePhotoList: List<JobItemMeasurePhotoDTO>) {
        measureItemPhotos.postValue(measurePhotoList)
    }

    suspend fun getMeasureItemPhotos(itemMeasureId: String): LiveData<List<JobItemMeasurePhotoDTO>> {
        return withContext(Dispatchers.IO) {
            measureCreationDataRepository.getJobItemMeasurePhotosForItemMeasureID(itemMeasureId)
        }
    }

    val estimateMeasureItem: MutableLiveData<EstimateMeasureItem> =
        MutableLiveData<EstimateMeasureItem>()

    fun setMeasureItem(measurea: EstimateMeasureItem) {
        estimateMeasureItem.postValue(measurea)
    }

    suspend fun getJobMeasureForActivityId(
        activityId: Int,
        activityId2: Int
    ): LiveData<List<JobItemEstimateDTO>> {
        return withContext(Dispatchers.IO + uncaughtExceptionHandler) {
            measureCreationDataRepository.getJobMeasureForActivityId(activityId, activityId2)
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

    suspend fun getItemDescription(jobId: String): String {
        return withContext(Dispatchers.IO) {
            measureCreationDataRepository.getItemDescription(jobId)
        }
    }

    suspend fun getDescForProjectItemId(projectItemId: String): String {
        return withContext(Dispatchers.IO) {
            measureCreationDataRepository.getProjectItemDescription(projectItemId)
        }
    }

    suspend fun getItemJobNo(jobId: String): String {
        return withContext(Dispatchers.IO) {
            measureCreationDataRepository.getItemJobNo(jobId)
        }
    }

    suspend fun getJobMeasureItemsPhotoPath(itemMeasureId: String): List<String> {
        return withContext(Dispatchers.IO) {
            measureCreationDataRepository.getJobMeasureItemsPhotoPath(itemMeasureId)
        }
    }

    fun deleteItemMeasurefromList(itemMeasureId: String) {
        measureCreationDataRepository.deleteItemMeasurefromList(itemMeasureId)
    }

    fun deleteItemMeasurephotofromList(itemMeasureId: String) {
        measureCreationDataRepository.deleteItemMeasurephotofromList(itemMeasureId)
    }

    val workflowMoveResponse: MutableLiveData<XIResult<String>> =
        measureCreationDataRepository.workflowStatus

    suspend fun processWorkflowMove(
        userId: String,
        jobId: String,
        jimNo: String?,
        contractVoId: String?,
        mSures: ArrayList<JobItemMeasureDTO>,
        activity: FragmentActivity,
        itemMeasureJob: JobDTO
    ): Job = viewModelScope.launch(viewModelContext) {

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
            workflowMoveResponse.postValue(XIError(e, e.message!!))
        }
    }

    suspend fun getJobItemMeasuresForJobIdAndEstimateId(
        jobId: String?
//        ,estimateId: String
    ): LiveData<List<JobItemMeasureDTO>> {
        return withContext(Dispatchers.IO) {
            measureCreationDataRepository.getJobItemMeasuresForJobIdAndEstimateId(jobId)
        }
    }

    suspend fun getJobItemMeasuresForJobIdAndEstimateId2(
        jobId: String?,
        estimateId: String
        //   ,jobItemMeasureArrayList: ArrayList<JobItemMeasureDTO>
    ): LiveData<List<JobItemMeasureDTO>> {
        return withContext(Dispatchers.IO + uncaughtExceptionHandler) {
            measureCreationDataRepository.getJobItemMeasuresForJobIdAndEstimateId2(
                jobId,
                estimateId
            ) // ,jobItemMeasureArrayList
        }
    }

    suspend fun getJobItemsToMeasureForJobId(jobID: String?): LiveData<List<JobItemEstimateDTO>> {
        return withContext(Dispatchers.IO) {
            measureCreationDataRepository.getJobItemsToMeasureForJobId(jobID)
        }
    }

    suspend fun getItemForItemId(projectItemId: String?): LiveData<ProjectItemDTO> {
        return withContext(Dispatchers.IO) {
            measureCreationDataRepository.getItemForItemId(projectItemId)
        }
    }

    suspend fun getJobFromJobId(jobId: String?): LiveData<JobDTO> {
        return withContext(Dispatchers.IO) {
            measureCreationDataRepository.getSingleJobFromJobId(jobId)
        }
    }

    suspend fun errorMsg(): String {
        return withContext(Dispatchers.IO) {
            measureCreationDataRepository.errorMsg()
        }
    }

    suspend fun errorState(): Boolean {
        return withContext(Dispatchers.IO) {
            measureCreationDataRepository.errorState()
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

    fun saveJobItemMeasureItems(jobItemMeasureDTO: ArrayList<JobItemMeasureDTO>) {
        measureCreationDataRepository.saveJobItemMeasureItems(jobItemMeasureDTO)
    }

    suspend fun getJobItemMeasurePhotosForItemEstimateID(estimateId: String): LiveData<List<JobItemMeasurePhotoDTO>> {
        return withContext(Dispatchers.IO) {
            measureCreationDataRepository.getJobItemMeasurePhotosForItemEstimateID(estimateId)
        }
    }

    val backupJobId: MutableLiveData<String> = MutableLiveData()

    fun setBackupJobId(jobId: String) {
        backupJobId.postValue(jobId)
    }

    fun generateGalleryUI(itemMeasureId: String): Job =
        viewModelScope.launch(viewModelContext) {
            try {
                galleryBackup.postValue(itemMeasureId)
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

    private suspend fun getJobItemMeasureByItemMeasureId(itemMeasureId: String): LiveData<JobItemMeasureDTO> {
        return withContext(Dispatchers.IO) {
            measureCreationDataRepository.getJobItemMeasureByItemMeasureId(itemMeasureId)
        }
    }
}
