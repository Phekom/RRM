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
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.geojson.Point
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import za.co.xisystems.itis_rrm.custom.events.XIEvent
import za.co.xisystems.itis_rrm.custom.results.XIResult
import za.co.xisystems.itis_rrm.custom.results.XIResult.Status
import za.co.xisystems.itis_rrm.custom.results.XIResult.Success
import za.co.xisystems.itis_rrm.data.localDB.entities.*
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

    var workItem = MutableLiveData<XIEvent<JobItemEstimateDTO>>()
    var workItemJob = MutableLiveData<XIEvent<JobDTO>>()
    var worksEstimate = MutableLiveData<XIEvent<JobEstimateWorksDTO?>>()
    var workSubmissionJob = MutableLiveData<JobDTO>()
    val backupWorkSubmission: MutableLiveData<JobEstimateWorksDTO> = MutableLiveData()
    val selectedJobId: MutableLiveData<String> = MutableLiveData()

    private var workflowStatus: LiveData<XIEvent<XIResult<String>>> = MutableLiveData()
    var workflowState: MutableLiveData<XIResult<String>?> = MutableLiveData()
    private val superJob = SupervisorJob()
    private var ioContext = Job(superJob) + dispatchers.io()
    private var mainContext = Job(superJob) + dispatchers.main()
    val backupCompletedEstimates: MutableLiveData<List<JobItemEstimateDTO>> = MutableLiveData()
    val historicalWorks: MutableLiveData<XIResult<JobEstimateWorksDTO>> = MutableLiveData()

    init {
        resetWorkState()
        initWorkflowChannels()
    }

    private fun initWorkflowChannels() {
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

    fun setWorkSubmissionJob(jobId: String) = viewModelScope.launch(ioContext) {
        val data = offlineDataRepository.getUpdatedJob(jobId)
        withContext(mainContext) {
            workSubmissionJob.value = data
        }
    }

    fun setWorkItem(estimateId: String) = viewModelScope.launch(ioContext) {
        val data = workDataRepository.getJobItemEstimateForEstimateId(estimateId)
        withContext(mainContext) {
            workItem.value = XIEvent(data)
        }
    }

    fun setWorkItemJob(jobId: String) = viewModelScope.launch(ioContext) {
        val data = offlineDataRepository.getUpdatedJob(jobId)
        withContext(mainContext) {
            workItemJob.value = XIEvent(data)
        }
    }

    suspend fun getJobsForActivityId(activityId1: Int, activityId2: Int): LiveData<List<JobDTO>> {
        return withContext(ioContext) {
            workDataRepository.getJobsForActivityIds(activityId1, activityId2).distinctUntilChanged()
        }
    }

    suspend fun getJobEstimationItemsForJobId(jobID: String?, actID: Int): List<JobItemEstimateDTO> = withContext(ioContext) {

        return@withContext workDataRepository.getJobEstimationItemsForJobId(jobID, actID)
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

    fun createSaveWorksPhotos(
        estimateWorksPhoto: ArrayList<JobEstimateWorksPhotoDTO>,
        itemEstiWorks: JobEstimateWorksDTO
    ) = viewModelScope.launch(ioContext, CoroutineStart.DEFAULT) {
        workDataRepository.createEstimateWorksPhoto(estimateWorksPhoto, itemEstiWorks)
        setWorkItem(itemEstiWorks.estimateId!!)
    }

    fun submitWorks(
        itemEstiWorks: JobEstimateWorksDTO,
        activity: FragmentActivity,
        itemEstiJob: JobDTO

    ) = viewModelScope.launch(ioContext, CoroutineStart.DEFAULT) {
        withContext(mainContext) {
            resetWorkState()
            initWorkflowChannels()
            itemEstiJob.setWorkStartDate()
        }
        val updateJob = workDataRepository.backupJobInProgress(itemEstiJob)
        workDataRepository.submitWorks(itemEstiWorks, activity, updateJob)
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
    ) = viewModelScope.launch(ioContext) {
        workDataRepository.processWorkflowMove(userId, trackRouteId, description, direction)
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
        resetWorkState()
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
            workItemJob.value = XIEvent(updatedJob)
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
