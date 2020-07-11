package za.co.xisystems.itis_rrm.ui.mainview.work

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobEstimateWorksDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobEstimateWorksPhotoDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimateDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.WF_WorkStepDTO
import za.co.xisystems.itis_rrm.data.repositories.OfflineDataRepository
import za.co.xisystems.itis_rrm.data.repositories.WorkDataRepository
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
    val workflowResponse = workDataRepository.workStatus
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

    ): String {
        return withContext(Dispatchers.IO) {
            workDataRepository.submitWorks(itemEstiWorks, activity, itemEstiJob)
        }
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
    ): String {
        return withContext(Dispatchers.IO) {
            workDataRepository.processWorkflowMove(userId, trackRouteId, description, direction)
        }
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
}
