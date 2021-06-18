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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
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
import za.co.xisystems.itis_rrm.data.localDB.entities.WfWorkStepDTO
import za.co.xisystems.itis_rrm.data.repositories.OfflineDataRepository
import za.co.xisystems.itis_rrm.data.repositories.WorkDataRepository
import za.co.xisystems.itis_rrm.utils.DataConversion
import za.co.xisystems.itis_rrm.utils.lazyDeferred

class WorkViewModel(
    application: Application,
    private val workDataRepository: WorkDataRepository,
    private val offlineDataRepository: OfflineDataRepository,
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
    private var ioContext = Job(superJob) + Dispatchers.IO
    private var mainContext = Job(superJob) + Dispatchers.Main
    val backupCompletedEstimates: MutableLiveData<List<JobItemEstimateDTO>> = MutableLiveData()
    val historicalWorks: MutableLiveData<XIResult<JobEstimateWorksDTO>> = MutableLiveData()

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
            workDataRepository.getJobsForActivityIds(activityId1, activityId2).distinctUntilChanged()
        }
    }

    suspend fun getJobEstimationItemsForJobId(jobID: String?, actID: Int): LiveData<List<JobItemEstimateDTO>> {
        return withContext(ioContext) {
            workDataRepository.getJobEstimationItemsForJobId(jobID, actID).distinctUntilChanged()
        }
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

    suspend fun getJobEstiItemForEstimateId(estimateId: String?): LiveData<List<JobEstimateWorksDTO>> {
        return withContext(ioContext) {
            workDataRepository.getJobEstiItemForEstimateId(estimateId)
        }
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

    fun submitWorks(
        itemEstiWorks: JobEstimateWorksDTO,
        activity: FragmentActivity,
        itemEstiJob: JobDTO

    ) {
        viewModelScope.launch(ioContext) {
            workDataRepository.submitWorks(itemEstiWorks, activity, itemEstiJob)
        }
    }

    suspend fun getJobItemEstimateForEstimateId(estimateId: String): JobItemEstimateDTO {
        return withContext(ioContext) {
            workDataRepository.getJobItemEstimateForEstimateId(estimateId)
        }
    }

    fun processWorkflowMove(
        userId: String,
        trackRouteId: String,
        description: String?,
        direction: Int
    ) {
        viewModelScope.launch(ioContext) {
            workDataRepository.processWorkflowMove(userId, trackRouteId, description, direction)
        }
    }

    suspend fun getJobItemsEstimatesDoneForJobId(
        jobId: String?,
        estimateWorkPartComplete: Int,
        estWorksComplete: Int
    ): Int {
        return withContext(ioContext) {
            workDataRepository.getJobItemsEstimatesDoneForJobId(
                jobId,
                estimateWorkPartComplete,
                estWorksComplete
            )
        }
    }

    suspend fun populateWorkTab(estimateId: String, actId: Int) {
        val newEstimateId = DataConversion.toBigEndian(estimateId)
        val worksDTO: JobEstimateWorksDTO = workDataRepository.getWorkItemsForEstimateIDAndActID(newEstimateId!!, actId)
        if (worksDTO.estimateId.isNullOrBlank()) {
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
        super.onCleared()
        superJob.cancelChildren()
        workflowState = MutableLiveData()
        workflowStatus = MutableLiveData()
    }
}
