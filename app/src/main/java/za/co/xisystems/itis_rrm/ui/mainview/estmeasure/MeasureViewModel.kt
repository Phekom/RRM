package za.co.xisystems.itis_rrm.ui.mainview.estmeasure

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import za.co.xisystems.itis_rrm.data.localDB.entities.*
import za.co.xisystems.itis_rrm.data.repositories.MeasureCreationDataRepository
import za.co.xisystems.itis_rrm.data.repositories.OfflineDataRepository
import za.co.xisystems.itis_rrm.ui.mainview.estmeasure.estimate_measure_item.EstimateMeasureItem
import za.co.xisystems.itis_rrm.utils.lazyDeferred
import za.co.xisystems.itis_rrm.utils.results.XIError
import za.co.xisystems.itis_rrm.utils.results.XIResult
import za.co.xisystems.itis_rrm.utils.uncaughtExceptionHandler

class MeasureViewModel (
    private val measureCreationDataRepository: MeasureCreationDataRepository,
    private val offlineDataRepository: OfflineDataRepository
) : ViewModel() {

    val offlineUserTaskList by lazyDeferred {
        offlineDataRepository.getUserTaskList()
    }

    val user by lazyDeferred {
        measureCreationDataRepository.getUser()
    }

    val measureJob = MutableLiveData<JobDTO>()
    fun Item1(value: JobDTO) {
        measureJob.value = value
    }

    val measurea1_Item1 = MutableLiveData<JobItemMeasureDTO>()
    fun Item1(measurea1: JobItemMeasureDTO) {
        measurea1_Item1.value = measurea1
    }

    val measurea1_Item2 = MutableLiveData<JobItemMeasureDTO>()
    fun Item2(measurea2: JobItemMeasureDTO) {
        measurea1_Item2.value = measurea2
    }


    val measurea1_Item3 = MutableLiveData<List<JobItemMeasurePhotoDTO>>()
    fun Item3(measurea3: List<JobItemMeasurePhotoDTO>) {
        measurea1_Item3.value = measurea3
    }


    val measure_Item = MutableLiveData<EstimateMeasureItem>()
    fun Item5(measurea: EstimateMeasureItem) {
        measure_Item.value = measurea
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


    suspend fun getJobMeasureItemsPhotoPath(itemMeasureId: String): String {
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
    ): Job = viewModelScope.launch(Dispatchers.IO + uncaughtExceptionHandler) {

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
            )//,jobItemMeasureArrayList
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
}
