package za.co.xisystems.itis_rrm.ui.mainview.approvejobs

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import za.co.xisystems.itis_rrm.custom.errors.XIErrorHandler
import za.co.xisystems.itis_rrm.custom.events.XIEvent
import za.co.xisystems.itis_rrm.custom.results.XIError
import za.co.xisystems.itis_rrm.custom.results.XIProgress
import za.co.xisystems.itis_rrm.custom.results.XIResult
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimateDTO
import za.co.xisystems.itis_rrm.data.repositories.JobApprovalDataRepository
import za.co.xisystems.itis_rrm.data.repositories.OfflineDataRepository
import za.co.xisystems.itis_rrm.ui.mainview.approvejobs.approve_job_item.ApproveJobItem
import za.co.xisystems.itis_rrm.utils.lazyDeferred

/**
 * Created by Francis Mahlava on 03,October,2019
 */
class ApproveJobsViewModel(
    application: Application,
    private val jobApprovalDataRepository: JobApprovalDataRepository,
    private val offlineDataRepository: OfflineDataRepository
) : AndroidViewModel(application) {

    private val superJob = SupervisorJob()
    private val workflowStatus: LiveData<XIEvent<XIResult<String>>> =
        jobApprovalDataRepository.workflowStatus.distinctUntilChanged()

    val workflowState: MutableLiveData<XIResult<String>> = MutableLiveData()

    init {
        viewModelScope.launch {
            workflowStatus.observeForever {
                it?.let {
                    workflowState.postValue(it.getContentIfNotHandled())
                }
            }
        }
    }

    val user by lazyDeferred {
        jobApprovalDataRepository.getUser()
    }

    val offlineUserTaskList by lazyDeferred {
        offlineDataRepository.getUserTaskList()
    }

    suspend fun getUOMForProjectItemId(projectItemId: String): String {
        return withContext(Dispatchers.IO) {
            jobApprovalDataRepository.getUOMForProjectItemId(projectItemId)
        }
    }

    suspend fun getTenderRateForProjectItemId(projectItemId: String): Double {
        return withContext(Dispatchers.IO) {
            jobApprovalDataRepository.getTenderRateForProjectItemId(projectItemId)
        }
    }

    suspend fun getProjectSectionIdForJobId(jobId: String): String {
        return withContext(Dispatchers.IO) {
            jobApprovalDataRepository.getProjectSectionIdForJobId(jobId)
        }
    }

    suspend fun getRouteForProjectSectionId(sectionId: String): String {
        return withContext(Dispatchers.IO) {
            jobApprovalDataRepository.getRouteForProjectSectionId(sectionId)
        }
    }

    suspend fun getSectionForProjectSectionId(sectionId: String): String {
        return withContext(Dispatchers.IO) {
            jobApprovalDataRepository.getSectionForProjectSectionId(sectionId)
        }
    }

    val jobApprovalItem : MutableLiveData<ApproveJobItem> = MutableLiveData()
    fun setJobForApproval(jobapproval6: ApproveJobItem) {
        jobApprovalItem.value = jobapproval6
    }

    suspend fun processWorkflowMove(
        userId: String,
        trackRouteId: String,
        description: String?,
        direction: Int,
        jobId: String
    ) = viewModelScope.launch(viewModelScope.coroutineContext) {
        withContext(Dispatchers.IO) {
            try {
                workflowState.postValue(XIProgress(true))

                jobApprovalDataRepository.processWorkflowMove(
                    userId,
                    jobId,
                    trackRouteId,
                    description,
                    direction
                )
            } catch (t: Throwable) {
                val message = "Failed to process workflow: ${t.message ?: XIErrorHandler.UNKNOWN_ERROR}"
                workflowState.postValue(XIError(t, message))
            } finally {
                workflowState.postValue(XIProgress(false))
            }
        }
    }

    suspend fun getJobsForActivityId(activityId: Int): LiveData<List<JobDTO>> {
        return withContext(Dispatchers.IO) {
            jobApprovalDataRepository.getJobsForActivityId(
                activityId
//                , measureComplete,
//                estWorksComplete,
//                jobApproved
            ).distinctUntilChanged()
        }
    }

    suspend fun getDescForProjectId(projectId: String): String {
        return withContext(Dispatchers.IO) {
            jobApprovalDataRepository.getProjectDescription(projectId)
        }
    }

    suspend fun getJobEstimationItemsForJobId(jobID: String?): LiveData<List<JobItemEstimateDTO>> {
        return withContext(Dispatchers.IO) {
            jobApprovalDataRepository.getJobEstimationItemsForJobId(jobID)
        }
    }

    suspend fun getJobEstimationItemsPhotoStartPath(estimateId: String): String {
        return withContext(Dispatchers.IO) {
        jobApprovalDataRepository.getJobEstimationItemsPhotoStartPath(estimateId)
        }
    }

    suspend fun getJobEstimationItemsPhotoEndPath(estimateId: String): String {
        return withContext(Dispatchers.IO) {
        jobApprovalDataRepository.getJobEstimationItemsPhotoEndPath(estimateId)
        }
    }

    suspend fun getDescForProjectItemId(projectItemId: String): String {
        return withContext(Dispatchers.IO) {
        jobApprovalDataRepository.getProjectItemDescription(projectItemId)
        }
    }

    suspend fun upDateEstimate(
        new_quantity: String,
        new_total: String,
        estimateId: String
    ): String {
        return withContext(Dispatchers.IO){
            jobApprovalDataRepository.upDateEstimate(new_quantity, new_total, estimateId)
        }
    }

    suspend fun getQuantityForEstimationItemId(estimateId: String): LiveData<Double> {
        return withContext(Dispatchers.IO) {
            jobApprovalDataRepository.getQuantityForEstimationItemId(estimateId)
        }
    }

    suspend fun getLineRateForEstimationItemId(estimateId: String): LiveData<Double> {
        return withContext(Dispatchers.IO) {
            jobApprovalDataRepository.getLineRateForEstimationItemId(estimateId)
        }
    }
}
