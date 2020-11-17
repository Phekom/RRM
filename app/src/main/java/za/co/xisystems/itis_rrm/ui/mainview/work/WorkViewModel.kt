package za.co.xisystems.itis_rrm.ui.mainview.work

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import za.co.xisystems.itis_rrm.custom.events.XIEvent
import za.co.xisystems.itis_rrm.custom.results.XIResult
import za.co.xisystems.itis_rrm.custom.results.XIStatus
import za.co.xisystems.itis_rrm.custom.results.XISuccess
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobEstimateWorksDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobEstimateWorksPhotoDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimateDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.WF_WorkStepDTO
import za.co.xisystems.itis_rrm.data.repositories.OfflineDataRepository
import za.co.xisystems.itis_rrm.data.repositories.WorkDataRepository
import za.co.xisystems.itis_rrm.utils.DataConversion
import za.co.xisystems.itis_rrm.utils.lazyDeferred
import za.co.xisystems.itis_rrm.utils.uncaughtExceptionHandler

class WorkViewModel(
    private val workDataRepository: WorkDataRepository,
    private val offlineDataRepository: OfflineDataRepository
) : ViewModel() {

    val user by lazyDeferred {
        workDataRepository.getUser()
    }
    val offlineUserTaskList by lazyDeferred {
        offlineDataRepository.getUserTaskList()
    }
    val workItem = MutableLiveData<JobItemEstimateDTO>()
    val workItemJob = MutableLiveData<JobDTO>()
    val backupWorkSubmission: MutableLiveData<JobEstimateWorksDTO> = MutableLiveData()
    private var workflowStatus: LiveData<XIEvent<XIResult<String>>> = MutableLiveData()
    var workflowState: MutableLiveData<XIResult<String>>? = MutableLiveData()
    val superJob = SupervisorJob()
    init {
        viewModelScope.launch(Job(superJob) + Dispatchers.Main + uncaughtExceptionHandler) {
            workflowStatus = workDataRepository.workStatus.distinctUntilChanged()

            workflowState = Transformations.map(workflowStatus) { it ->
                it?.getContentIfNotHandled()?.let {
                    it
                }
            } as? MutableLiveData<XIResult<String>>
        }
    }

    val backupCompletedEstimates: MutableLiveData<List<JobItemEstimateDTO>> = MutableLiveData()
    fun setWorkItem(work: JobItemEstimateDTO) {
        workItem.value = work
    }

    fun setWorkItemJob(workjob: JobDTO) {
        workItemJob.value = workjob
    }

    suspend fun getJobsForActivityId(activityId1: Int, activityId2: Int): LiveData<List<JobDTO>> {
        return withContext(Dispatchers.IO + uncaughtExceptionHandler) {
            workDataRepository.getJobsForActivityIds(activityId1, activityId2)
        }
    }

    suspend fun getJobsForActivityId1(activityId1: Int): LiveData<List<JobItemEstimateDTO>> {
        return withContext(Dispatchers.IO) {
            workDataRepository.getJobsForActivityId(activityId1)
        }
    }

    suspend fun getJobEstimationItemsForJobId(
        jobID: String?,
        actID: Int
    ): LiveData<List<JobItemEstimateDTO>> {
        return withContext(Dispatchers.IO + uncaughtExceptionHandler) {
            workDataRepository.getJobEstimationItemsForJobId(jobID, actID)
        }
    }

    suspend fun getDescForProjectItemId(projectItemId: String): String {
        return withContext(Dispatchers.IO + uncaughtExceptionHandler) {
            workDataRepository.getProjectItemDescription(projectItemId)
        }
    }

    suspend fun getItemDescription(jobId: String): String {
        return withContext(Dispatchers.IO + uncaughtExceptionHandler) {
            workDataRepository.getItemDescription(jobId)
        }
    }

    suspend fun getItemJobNo(jobId: String): String {
        return withContext(Dispatchers.IO + uncaughtExceptionHandler) {
            workDataRepository.getItemJobNo(jobId)
        }
    }

    suspend fun getItemStartKm(jobId: String): Double {
        return withContext(Dispatchers.IO + uncaughtExceptionHandler) {
            workDataRepository.getItemStartKm(jobId)
        }
    }

    suspend fun getItemEndKm(jobId: String): Double {
        return withContext(Dispatchers.IO + uncaughtExceptionHandler) {
            workDataRepository.getItemEndKm(jobId)
        }
    }

    suspend fun getItemTrackRouteId(jobId: String): String {
        return withContext(Dispatchers.IO) {
            workDataRepository.getItemTrackRouteId(jobId)
        }
    }

    suspend fun getProjectSectionIdForJobId(jobId: String): String {
        return withContext(Dispatchers.IO) {
            workDataRepository.getProjectSectionIdForJobId(jobId)
        }
    }

    suspend fun getRouteForProjectSectionId(sectionId: String): String {
        return withContext(Dispatchers.IO) {
            workDataRepository.getRouteForProjectSectionId(sectionId)
        }
    }

    suspend fun getSectionForProjectSectionId(sectionId: String): String {
        return withContext(Dispatchers.IO) {
            workDataRepository.getSectionForProjectSectionId(sectionId)
        }
    }

    suspend fun getJobEstiItemForEstimateId(estimateId: String?): LiveData<List<JobEstimateWorksDTO>> {
        return withContext(Dispatchers.IO) {
            workDataRepository.getJobEstiItemForEstimateId(estimateId)
        }
    }

    suspend fun getWorkFlowCodes(eId: Int): LiveData<List<WF_WorkStepDTO>> {
        return withContext(Dispatchers.IO) {
            workDataRepository.getWorkFlowCodes(eId)
        }
    }

    suspend fun createSaveWorksPhotos(
        estimateWorksPhoto: ArrayList<JobEstimateWorksPhotoDTO>,
        itemEstiWorks: JobEstimateWorksDTO
    ) {
        return withContext(Dispatchers.IO) {
            workDataRepository.createEstimateWorksPhoto(estimateWorksPhoto, itemEstiWorks)
        }
    }

    suspend fun submitWorks(
        itemEstiWorks: JobEstimateWorksDTO,
        activity: FragmentActivity,
        itemEstiJob: JobDTO

    ): Job = viewModelScope.launch( Job(superJob) + Dispatchers.Main + uncaughtExceptionHandler) {
        workDataRepository.submitWorks(itemEstiWorks, activity, itemEstiJob)
    }

    suspend fun getJobItemEstimateForEstimateId(estimateId: String): LiveData<JobItemEstimateDTO> {
        return withContext(Dispatchers.IO) {
            workDataRepository.getJobItemEstimateForEstimateId(estimateId)
        }
    }

    suspend fun processWorkflowMove(
        userId: String,
        trackRouteId: String,
        description: String?,
        direction: Int
    ) {

            workDataRepository.processWorkflowMove(userId, trackRouteId, description, direction)

    }

    suspend fun getJobItemsEstimatesDoneForJobId(
        jobId: String?,
        estimateWorkPartComplete: Int,
        estWorksComplete: Int
    ): Int {
        return withContext(Dispatchers.IO) {
            workDataRepository.getJobItemsEstimatesDoneForJobId(
                jobId,
                estimateWorkPartComplete,
                estWorksComplete
            )
        }
    }

    suspend fun getWorkItemsForActID(actId: Int): LiveData<List<JobEstimateWorksDTO>> {
        return withContext(Dispatchers.IO) {
            workDataRepository.getWorkItemsForActID(actId)
        }
    }

    val historicalWorks: MutableLiveData<XIResult<JobEstimateWorksDTO>> = MutableLiveData()

    suspend fun populateWorkTab(estimateId: String, actId: Int) {
        val newEstimateId = DataConversion.toBigEndian(estimateId)
        val worksDTO: JobEstimateWorksDTO? = workDataRepository.getWorkItemsForEstimateIDAndActID(newEstimateId!!, actId)
        if (worksDTO != null) {
            val worksPhotos = workDataRepository.getEstimateWorksPhotosForWorksId(worksDTO.worksId)
            if (!worksPhotos.isNullOrEmpty()) {
                worksDTO.jobEstimateWorksPhotos = worksPhotos as java.util.ArrayList<JobEstimateWorksPhotoDTO>
                historicalWorks.postValue(XISuccess(worksDTO))
            } else {
                historicalWorks.postValue(XIStatus("Photos failed to load"))
            }
        } else {
            historicalWorks.postValue(XIStatus("Works failed to load"))
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
        super.onCleared()
    }
}
