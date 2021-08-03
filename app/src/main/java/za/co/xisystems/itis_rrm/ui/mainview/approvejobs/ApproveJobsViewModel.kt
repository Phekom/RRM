package za.co.xisystems.itis_rrm.ui.mainview.approvejobs

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.*
import timber.log.Timber
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
    private val offlineDataRepository: OfflineDataRepository,
) : AndroidViewModel(application) {

    private val superJob = SupervisorJob()
    private lateinit var workflowStatus: LiveData<XIEvent<XIResult<String>>>
    private lateinit var updateStatus: LiveData<XIEvent<XIResult<String>>>

    private val workExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        val message = "Caught during workflow: ${throwable.message ?: XIErrorHandler.UNKNOWN_ERROR}"
        Timber.e(throwable)
        val caughtException = XIError(
            throwable, message
        )
        workflowState.postValue(caughtException)
    }

    private val mainContext = Job(superJob) + Dispatchers.Main + workExceptionHandler
    private val ioContext = Job(superJob) + Dispatchers.Main + workExceptionHandler
    var workflowState: MutableLiveData<XIResult<String>?> = MutableLiveData()
    var updateState: MutableLiveData<XIResult<String>?> = MutableLiveData()

    init {
        viewModelScope.launch(mainContext) {

            workflowStatus = jobApprovalDataRepository.workflowStatus

            workflowState = Transformations.map(workflowStatus) {
                it.getContentIfNotHandled()
            } as MutableLiveData<XIResult<String>?>

            updateStatus = jobApprovalDataRepository.updateStatus

            updateState = Transformations.map(updateStatus) {
                it.getContentIfNotHandled()
            } as MutableLiveData<XIResult<String>?>
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

    val jobApprovalItem: MutableLiveData<ApproveJobItem> = MutableLiveData()
    fun setJobForApproval(approveJobItem: ApproveJobItem) {
        jobApprovalItem.value = approveJobItem
    }

    suspend fun processWorkflowMove(
        userId: String,
        trackRouteId: String,
        description: String?,
        direction: Int,
        jobId: String
    ) = viewModelScope.launch(viewModelScope.coroutineContext) {
        withContext(ioContext) {
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
        return withContext(ioContext) {
            jobApprovalDataRepository.getJobsForActivityId(
                activityId
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
        updatedQty: String,
        updatedRate: String,
        estimateId: String
    ) {
        withContext(Dispatchers.IO) {
            jobApprovalDataRepository.upDateEstimate(updatedQty, updatedRate, estimateId)
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

    suspend fun getJobEstimationItemByEstimateId(estimateId: String) = liveData {
        val estimate = jobApprovalDataRepository.getJobEstimationItemByEstimateId(estimateId)
        emit(estimate)
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
        workflowStatus = MutableLiveData()
        updateState = MutableLiveData()
        updateStatus = MutableLiveData()
        super.onCleared()
    }
}
