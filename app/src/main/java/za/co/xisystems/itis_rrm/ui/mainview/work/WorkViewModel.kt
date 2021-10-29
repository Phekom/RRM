/**
 * Updated by Shaun McDonald on 2021/05/15
 * Last modified on 2021/05/14, 20:32
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 **/

/**
 * Updated by Shaun McDonald on 2021/05/14
 * Last modified on 2021/05/14, 19:43
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 **/

package za.co.xisystems.itis_rrm.ui.mainview.work

import android.app.Application
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.viewModelScope
import com.mapbox.geojson.Point
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import za.co.xisystems.itis_rrm.custom.errors.XIErrorHandler
import za.co.xisystems.itis_rrm.custom.events.XIEvent
import za.co.xisystems.itis_rrm.custom.results.XIResult
import za.co.xisystems.itis_rrm.custom.results.XIResult.Status
import za.co.xisystems.itis_rrm.custom.results.XIResult.Success
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobEstimateWorksDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobEstimateWorksPhotoDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimateDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimatesPhotoDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.WfWorkStepDTO
import za.co.xisystems.itis_rrm.data.repositories.OfflineDataRepository
import za.co.xisystems.itis_rrm.data.repositories.WorkDataRepository
import za.co.xisystems.itis_rrm.forge.DefaultDispatcherProvider
import za.co.xisystems.itis_rrm.forge.DispatcherProvider
import za.co.xisystems.itis_rrm.utils.DataConversion
import za.co.xisystems.itis_rrm.utils.lazyDeferred

class WorkViewModel(
    application: Application,
    private val workDataRepository: WorkDataRepository,
    private val offlineDataRepository: OfflineDataRepository,
    dispatchers: DispatcherProvider = DefaultDispatcherProvider()
) : AndroidViewModel(application) {

    val user by lazyDeferred {
        workDataRepository.getUser()
    }
    val offlineUserTaskList by lazyDeferred {
        offlineDataRepository.getUserTaskList()
    }
    var workItem = MutableLiveData<JobItemEstimateDTO>()
    var workItemJob = MutableLiveData<JobDTO>()
    val backupWorkSubmission: MutableLiveData<JobEstimateWorksDTO> = MutableLiveData()

    private var workflowStatus: LiveData<XIEvent<XIResult<String>>> = MutableLiveData()
    var workflowState: MutableLiveData<XIResult<String>?> = MutableLiveData()
    private val superJob = SupervisorJob()
    private var ioContext = Job(superJob) + dispatchers.io()
    private var mainContext = Job(superJob) + dispatchers.main()
    val backupCompletedEstimates: MutableLiveData<List<JobItemEstimateDTO>> = MutableLiveData()
    val historicalWorks: MutableLiveData<XIResult<JobEstimateWorksDTO>> = MutableLiveData()
    private val allWork: LiveData<List<JobDTO>>?
    private val searchResults: MutableLiveData<List<JobDTO>>
    init {
        viewModelScope.launch(mainContext) {
            // Set up the feed from the repository
            workflowStatus = workDataRepository.workStatus

            // This works so much better than observeForever, in that it doesn't
            // eat the device's memory alive and clears when the viewModel is cleared.
            workflowState = Transformations.map(workflowStatus) {
                it.getContentIfNotHandled()
            } as MutableLiveData<XIResult<String>?>
        }

        allWork = workDataRepository.allWork
        searchResults = workDataRepository.searchResults
    }

    fun searchJobs(criteria: String) {
        workDataRepository.jobSearch(criteria)
    }

    fun getSearchResults(): MutableLiveData<List<JobDTO>> {
        return searchResults
    }

    fun getAllWork(): LiveData<List<JobDTO>>? {
        return allWork
    }

    suspend fun setWorkItem(estimateId: String) = viewModelScope.launch(ioContext) {
        val data = workDataRepository.getJobItemEstimateForEstimateId(estimateId)
        withContext(mainContext) {
            workItem.value = data
        }
    }

    suspend fun setWorkItemJob(jobId: String) = viewModelScope.launch(ioContext) {
        val data = offlineDataRepository.getUpdatedJob(jobId)
        withContext(mainContext) {
            workItemJob.value = data
        }
    }

    suspend fun getJobsForActivityId(activityId1: Int, activityId2: Int): LiveData<List<JobDTO>> {
        return withContext(ioContext) {
            workDataRepository.getJobsForActivityIds(activityId1, activityId2)
                .distinctUntilChanged()
        }
    }

    suspend fun getJobEstimationItemsForJobId(jobID: String?, actID: Int):
        List<JobItemEstimateDTO> = withContext(ioContext) {

        val data = workDataRepository
            .getJobEstimationItemsForJobId(jobID, actID)

        return@withContext data
    }

    suspend fun getDescForProjectItemId(projectItemId: String): String {
        return withContext(ioContext) {
            workDataRepository.getProjectItemDescription(projectItemId)
        }
    }

    suspend fun getItemDescription(jobId: String): String {
        return withContext(ioContext) {
            workDataRepository.getItemDescription(jobId)
        }
    }

    suspend fun getItemJobNo(jobId: String): String {
        return withContext(ioContext) {
            workDataRepository.getItemJobNo(jobId)
        }
    }

    suspend fun getItemStartKm(jobId: String): Double {
        return withContext(ioContext) {
            workDataRepository.getItemStartKm(jobId)
        }
    }

    suspend fun getItemEndKm(jobId: String): Double {
        return withContext(ioContext) {
            workDataRepository.getItemEndKm(jobId)
        }
    }

    suspend fun getItemTrackRouteId(jobId: String): String {
        return withContext(ioContext) {
            workDataRepository.getItemTrackRouteId(jobId)
        }
    }

    suspend fun getProjectSectionIdForJobId(jobId: String): String {
        return withContext(ioContext) {
            workDataRepository.getProjectSectionIdForJobId(jobId)
        }
    }

    suspend fun getRouteForProjectSectionId(sectionId: String): String {
        return withContext(ioContext) {
            workDataRepository.getRouteForProjectSectionId(sectionId)
        }
    }

    suspend fun getSectionForProjectSectionId(sectionId: String): String {
        return withContext(ioContext) {
            workDataRepository.getSectionForProjectSectionId(sectionId)
        }
    }

    suspend fun getLiveJobEstimateWorksByEstimateId(estimateId: String?): LiveData<JobEstimateWorksDTO> =
        withContext(ioContext) {
            return@withContext workDataRepository.getLiveJobEstimateWorksByEstimateId(estimateId)
        }

    suspend fun getWorkFlowCodes(eId: Int): LiveData<List<WfWorkStepDTO>> {
        return withContext(ioContext) {
            workDataRepository.getWorkFlowCodes(eId)
        }
    }

    suspend fun createSaveWorksPhotos(
        estimateWorksPhoto: ArrayList<JobEstimateWorksPhotoDTO>,
        itemEstiWorks: JobEstimateWorksDTO
    ) {
        return withContext(ioContext) {
            workDataRepository.createEstimateWorksPhoto(estimateWorksPhoto, itemEstiWorks)
        }
    }

    @Suppress("TooGenericExceptionCaught")
    fun submitWorks(
        itemEstiWorks: JobEstimateWorksDTO,
        comments: String,
        activity: FragmentActivity,
        itemEstiJob: JobDTO

    ) = viewModelScope.launch(ioContext) {
        try {
            val systemJobId = DataConversion.toLittleEndian(itemEstiJob.jobId)!!
            val currentUser = user.await().value
            // If the job has no work start - now is the time!
            if (itemEstiJob.workStartDate.isNullOrBlank()) {
                itemEstiJob.setWorkStartDate()
                backupJobInProgress(itemEstiJob)
                // Let the backend know
                workDataRepository.updateWorkTimes(
                    currentUser!!.userId,
                    systemJobId,
                    true
                )
            }

            // Here's the update for the work stage
            workDataRepository.updateWorkStateInfo(
                jobId = systemJobId,
                userId = currentUser!!.userId.toInt(),
                activityId = itemEstiWorks.actId,
                remarks = comments
            )

            // Upload the work
            workDataRepository.submitWorks(itemEstiWorks, activity, itemEstiJob)

        } catch (t: Throwable) {
            val message = "Work upload failed - "
            val workFailReason = XIResult.Error(
                t.cause ?: t,
                "$message: ${t.message ?: XIErrorHandler.UNKNOWN_ERROR}"
            )
            viewModelScope.launch(mainContext) {
                workflowState.postValue(workFailReason)
            }
        }
    }

    suspend fun getJobItemEstimateForEstimateId(estimateId: String): JobItemEstimateDTO {
        return withContext(ioContext) {
            workDataRepository.getJobItemEstimateForEstimateId(estimateId)
        }
    }

    @Suppress("TooGenericExceptionCaught")
    fun processWorkflowMove(
        userId: String,
        jobId: String,
        trackRouteId: String,
        description: String?,
        direction: Int
    ) = viewModelScope.launch(ioContext) {

        try {
            val updatedJob = offlineDataRepository.getUpdatedJob(jobId)
            val systemJobId = DataConversion.toLittleEndian(updatedJob.jobId)!!

            // If the job has no work end - now is the time!
            if (updatedJob.workCompleteDate.isNullOrBlank()) {
                updatedJob.setWorkCompleteDate()
                backupJobInProgress(updatedJob)
                // Let the backend know
                workDataRepository.updateWorkTimes(
                    userId,
                    systemJobId,
                    false
                )
            }

            workDataRepository.processWorkflowMove(userId, trackRouteId, description, direction)
        } catch (t: Throwable) {
            val message = "Job submission failed -"
            val workFailReason = XIResult.Error(t.cause ?: t,
                "$message: ${t.message ?: XIErrorHandler.UNKNOWN_ERROR}")
            viewModelScope.launch(mainContext) {
                workflowState.postValue(workFailReason)
            }
        }
    }

    suspend fun getJobItemsEstimatesDoneForJobId(
        jobId: String?,
        estimateWorkPartComplete: Int,
        estWorksComplete: Int
    ): Int = withContext(ioContext) {
        return@withContext workDataRepository.getJobItemsEstimatesDoneForJobId(
            jobId,
            estimateWorkPartComplete,
            estWorksComplete
        )
    }

    suspend fun populateWorkTab(estimateId: String, actId: Int) {
        val newEstimateId = DataConversion.toBigEndian(estimateId)
        val worksDTO: JobEstimateWorksDTO = workDataRepository.getWorkItemsForEstimateIDAndActID(newEstimateId!!, actId)
        if (worksDTO.estimateId.isNullOrBlank()) {
            val worksPhotos = workDataRepository.getEstimateWorksPhotosForWorksId(worksDTO.worksId)
            if (!worksPhotos.isNullOrEmpty()) {
                worksDTO.jobEstimateWorksPhotos = worksPhotos as java.util.ArrayList<JobEstimateWorksPhotoDTO>
                historicalWorks.postValue(Success(worksDTO))
            } else {
                historicalWorks.postValue(Status("Photos failed to load"))
            }
        } else {
            historicalWorks.postValue(Status("Works failed to load"))
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
        super.onCleared()
        superJob.cancelChildren()
        workflowState = MutableLiveData()
        workflowStatus = MutableLiveData()
    }

    suspend fun getUOMForProjectItemId(projectItemId: String): String? = withContext(ioContext) {
        return@withContext workDataRepository.getUOMForProjectItemId(projectItemId)
    }

    fun resetWorkState() {
        workflowStatus = MutableLiveData()
        workflowState = MutableLiveData()
    }

    fun backupJobInProgress(job: JobDTO) = viewModelScope.launch(ioContext) {
        val updatedJob = workDataRepository.backupJobInProgress(job)
        withContext(mainContext) {
            workItemJob.value = updatedJob
        }
    }

    suspend fun getEstimateStartPhotoForId(estimateId: String): JobItemEstimatesPhotoDTO {
        return withContext(ioContext) {
            workDataRepository.getEstimateStartPhotoForId(estimateId)
        }
    }

    val myWorkItem = MutableLiveData<Point>()
    fun goToWorkLocation(selectedLocationPoint: Point) {
        myWorkItem.postValue(selectedLocationPoint)
    }
}
