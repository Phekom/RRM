/*
 * Updated by Shaun McDonald on 2021/01/25
 * Last modified on 2021/01/25 6:30 PM
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 */

package za.co.xisystems.itis_rrm.data.repositories

// import sun.security.krb5.Confounder.bytes

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.JsonObject
import kotlinx.coroutines.withContext
import timber.log.Timber
import za.co.xisystems.itis_rrm.custom.errors.LocalDataException
import za.co.xisystems.itis_rrm.custom.errors.ServiceException
import za.co.xisystems.itis_rrm.custom.errors.TransmissionException
import za.co.xisystems.itis_rrm.custom.errors.XIErrorHandler
import za.co.xisystems.itis_rrm.custom.events.XIEvent
import za.co.xisystems.itis_rrm.custom.results.XIResult
import za.co.xisystems.itis_rrm.data.localDB.AppDatabase
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobEstimateWorksDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimateDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.UserDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.WorkflowJobDTO
import za.co.xisystems.itis_rrm.data.network.BaseConnectionApi
import za.co.xisystems.itis_rrm.data.network.SafeApiRequest
import za.co.xisystems.itis_rrm.forge.DefaultDispatcherProvider
import za.co.xisystems.itis_rrm.forge.DispatcherProvider
import za.co.xisystems.itis_rrm.utils.DataConversion
import za.co.xisystems.itis_rrm.utils.enums.WorkflowDirection
import java.util.Locale

/**
 * Created by Francis Mahlava on 2019/11/28.
 */

class JobApprovalDataRepository(
    private val api: BaseConnectionApi,
    private val appDb: AppDatabase,
    private val dispatchers: DispatcherProvider = DefaultDispatcherProvider()
) : SafeApiRequest() {
    companion object {
        val TAG: String = JobApprovalDataRepository::class.java.simpleName
    }

    var workflowStatus: MutableLiveData<XIEvent<XIResult<String>>> = MutableLiveData()
    var updateStatus: MutableLiveData<XIEvent<XIResult<String>>> = MutableLiveData()

    suspend fun getUser(): LiveData<UserDTO> {
        return withContext(dispatchers.io()) {
            appDb.getUserDao().getUser()
        }
    }

    suspend fun getSectionForProjectSectionId(sectionId: String?): String {
        return withContext(dispatchers.io()) {
            appDb.getProjectSectionDao().getSectionForProjectSectionId(sectionId!!)
        }
    }

    suspend fun getTenderRateForProjectItemId(projectItemId: String): Double {
        return withContext(dispatchers.io()) {
            appDb.getProjectItemDao().getTenderRateForProjectItemId(projectItemId)
        }
    }

    suspend fun getUOMForProjectItemId(projectItemId: String): String? = withContext(dispatchers.io()) {
        return@withContext appDb.getProjectItemDao().getUOMForProjectItemId(projectItemId)
    }

    suspend fun getRouteForProjectSectionId(sectionId: String?): String {
        return withContext(dispatchers.io()) {
            appDb.getProjectSectionDao().getRouteForProjectSectionId(sectionId!!)
        }
    }

    suspend fun getProjectSectionIdForJobId(jobId: String?): String {
        return withContext(dispatchers.io()) {
            appDb.getJobSectionDao().getProjectSectionId(jobId!!)
        }
    }

    suspend fun getProjectDescription(projectId: String): String {
        return withContext(dispatchers.io()) {
            appDb.getProjectDao().getProjectDescription(projectId)
        }
    }

    suspend fun getJobsForActivityId(activityId: Int): LiveData<List<JobDTO>> {
        return withContext(dispatchers.io()) {
            appDb.getJobDao().getJobsForActivityId(activityId)
        }
    }

    suspend fun upDateEstimate(newQuantity: String, newRate: String, estimateId: String) {

        val topic = "Estimate Update"
        try {
            val newEstimateId = DataConversion.toLittleEndian(estimateId)

            val quantityUpdateResponse = apiRequest {
                api.updateEstimateQty(
                    newEstimateId,
                    newQuantity.toDouble(),
                    newRate.toDouble()
                )
            }

            val messages = quantityUpdateResponse.errorMessage ?: ""
            if (messages.isBlank()) {
                postNewQty(
                    estimateId,
                    newQuantity.toDouble(),
                    newRate.toDouble()
                )
            } else {
                throw ServiceException(messages)
            }
        } catch (throwable: Throwable) {
            val message = "Failed to update quantity: ${throwable.message ?: XIErrorHandler.UNKNOWN_ERROR}"
            Timber.e(throwable, message)
            postUpdateStatus(XIResult.Error(topic = topic, exception = throwable, message = message))
        }
    }

    private fun postUpdateStatus(result: XIResult<String>) {
        val newEvent = XIEvent(result)
        updateStatus.postValue(newEvent)
    }

    private fun postNewQty(
        newEstimateId: String?,
        newQuantity: Double,
        newRate: Double
    ) {
        val topic = "Updating Quantity"
        try {
            appDb.getJobItemEstimateDao().upDateLineRate(newEstimateId!!, newQuantity, newRate)
            postUpdateStatus(XIResult.Success("Quantity updated"))
        } catch (throwable: Throwable) {
            val message = throwable.message ?: XIErrorHandler.UNKNOWN_ERROR
            Timber.e(throwable)
            postUpdateStatus(
                XIResult.Error(
                    topic = topic,
                    exception = LocalDataException(message),
                    message = message
                )
            )
        }
    }

    suspend fun processWorkflowMove(
        userId: String,
        jobId: String,
        trackRouteId: String,
        description: String?,
        direction: Int
    ) {
        withContext(dispatchers.io()) {
            val topic = "Job Approval Workflow"
            try {
                val workflowMoveResponse =
                    apiRequest { api.getWorkflowMove(userId, trackRouteId, description, direction) }

                val errorMessage: String = workflowMoveResponse.errorMessage ?: ""
                when {
                    errorMessage.lowercase(Locale.ENGLISH).contains("no job found") &&
                        direction == WorkflowDirection.FAIL.value -> {
                        appDb.getJobDao().softDeleteJobForJobId(jobId)
                        postWorkflowStatus(XIResult.Success("DECLINED"))
                    }
                    errorMessage.isBlank() && workflowMoveResponse.workflowJob != null -> {
                        saveWorkflowJob(workflowMoveResponse.workflowJob!!)
                    }
                    else -> {
                        throw ServiceException(errorMessage)
                    }
                }
            } catch (t: Throwable) {
                val message = t.message ?: XIErrorHandler.UNKNOWN_ERROR
                Timber.e(t)
                postWorkflowStatus(XIResult.Error(topic = topic, exception = t, message = message))
            }
        }
    }

    suspend fun getQuantityForEstimationItemId(estimateId: String): LiveData<Double> {
        return withContext(dispatchers.io()) {
            appDb.getJobItemEstimateDao().getQuantityForEstimationItemId(estimateId)
        }
    }

    suspend fun getLineRateForEstimationItemId(estimateId: String): LiveData<Double> {
        return withContext(dispatchers.io()) {
            appDb.getJobItemEstimateDao().getLineRateForEstimationItemId(estimateId)
        }
    }

    suspend fun getJobEstimationItemsForJobId(jobID: String?): LiveData<List<JobItemEstimateDTO>> {
        return withContext(dispatchers.io()) {
            appDb.getJobItemEstimateDao().getJobEstimationItemsForJobId2(jobID!!)
        }
    }

    suspend fun getProjectItemDescription(projectItemId: String): String {
        return withContext(dispatchers.io()) {
            appDb.getProjectItemDao().getProjectItemDescription(projectItemId)
        }
    }

    suspend fun getJobEstimationItemsPhotoStartPath(estimateId: String): String {
        return withContext(dispatchers.io()) {
            appDb.getJobItemEstimatePhotoDao().getJobEstimationItemsPhotoStartPath(estimateId)
        }
    }

    suspend fun getJobEstimationItemsPhotoEndPath(estimateId: String): String {
        return withContext(dispatchers.io()) {
            appDb.getJobItemEstimatePhotoDao().getJobEstimationItemsPhotoEndPath(estimateId)
        }
    }

    private suspend fun saveWorkflowJob(workflowJob: WorkflowJobDTO) {
        withContext(dispatchers.io()) {
            val job = setWorkflowJobBigEndianGuids(workflowJob)
            job?.let {
                processWorkflowUpdates(job)
            }
        }
    }

    private fun postWorkflowStatus(result: XIResult<String>) {
        val newEvent = XIEvent(result)
        workflowStatus.postValue(newEvent)
    }

    private suspend fun processWorkflowUpdates(job: WorkflowJobDTO) {
        try {

            appDb.getJobDao().updateJob(job.trackRouteId, job.actId, job.jiNo, job.jobId)

            job.workflowItemEstimates.forEach { jobItemEstimate ->
                appDb.getJobItemEstimateDao().updateExistingJobItemEstimateWorkflow(
                    jobItemEstimate.trackRouteId,
                    jobItemEstimate.actId,
                    jobItemEstimate.estimateId
                )

                jobItemEstimate.workflowEstimateWorks.forEach { jobEstimateWorks ->
                    if (!appDb.getEstimateWorkDao()
                        .checkIfJobEstimateWorksExist(jobEstimateWorks.worksId)
                    ) {
                        // Create Bare Bones
                        val estimateWorks = JobEstimateWorksDTO(
                            actId = jobEstimateWorks.actId,
                            estimateId = jobEstimateWorks.estimateId,
                            jobEstimateWorksPhotos = ArrayList(),
                            recordSynchStateId = jobEstimateWorks.recordSynchStateId,
                            recordVersion = jobEstimateWorks.recordVersion,
                            trackRouteId = jobEstimateWorks.trackRouteId,
                            worksId = jobEstimateWorks.worksId
                        )

                        appDb.getEstimateWorkDao().insertJobEstimateWorks(
                            estimateWorks
                        )
                    } else {
                        appDb.getEstimateWorkDao().updateJobEstimateWorksWorkflow(
                            jobEstimateWorks.worksId,
                            jobEstimateWorks.estimateId,
                            jobEstimateWorks.recordVersion,
                            jobEstimateWorks.recordSynchStateId,
                            jobEstimateWorks.actId,
                            jobEstimateWorks.trackRouteId
                        )
                    }
                }
            }

            job.workflowItemMeasures.forEach { jobItemMeasure ->
                appDb.getJobItemMeasureDao().updateWorkflowJobItemMeasure(
                    jobItemMeasure.itemMeasureId,
                    jobItemMeasure.trackRouteId,
                    jobItemMeasure.actId,
                    jobItemMeasure.measureGroupId
                )
            }

            //  Place the Job Section, UPDATE OR CREATE
            job.workflowJobSections.forEach { jobSection ->
                if (!appDb.getJobSectionDao().checkIfJobSectionExist(jobSection.jobSectionId)) {
                    appDb.getJobSectionDao().insertJobSection(jobSection)
                } else {
                    appDb.getJobSectionDao().updateExistingJobSectionWorkflow(
                        jobSection.jobSectionId,
                        jobSection.projectSectionId,
                        jobSection.jobId,
                        jobSection.startKm,
                        jobSection.endKm,
                        jobSection.recordVersion,
                        jobSection.recordSynchStateId
                    )
                }
            }
            Timber.d("Updated Workflow: $job")
            job.jiNo?.let {
                val jobSuccess = XIResult.Success(data = it)
                postWorkflowStatus(jobSuccess)
            }
        } catch (t: Throwable) {
            val message = "Could not save updated workflow: ${t.message ?: XIErrorHandler.UNKNOWN_ERROR}"
            Timber.e(t, message)
            postWorkflowStatus(XIResult.Error(LocalDataException(message), message))
        }
    }

    private fun setWorkflowJobBigEndianGuids(job: WorkflowJobDTO): WorkflowJobDTO? {
        try {
            job.jobId = DataConversion.toBigEndian(job.jobId)
            job.trackRouteId = DataConversion.toBigEndian(job.trackRouteId)

            job.workflowItemEstimates.forEach { jie ->
                jie.estimateId = DataConversion.toBigEndian(jie.estimateId)!!
                jie.trackRouteId = DataConversion.toBigEndian(jie.trackRouteId)!!
                //  Let's go through the WorkFlowEstimateWorks
                jie.workflowEstimateWorks.forEach { wfe ->
                    wfe.trackRouteId = DataConversion.toBigEndian(wfe.trackRouteId)!!
                    wfe.worksId = DataConversion.toBigEndian(wfe.worksId)!!
                    wfe.estimateId = DataConversion.toBigEndian(wfe.estimateId)!!
                }
            }

            job.workflowItemMeasures.forEach { jim ->
                jim.itemMeasureId = DataConversion.toBigEndian(jim.itemMeasureId)!!
                jim.measureGroupId = DataConversion.toBigEndian(jim.measureGroupId)!!
                jim.trackRouteId = DataConversion.toBigEndian(jim.trackRouteId)!!
            }

            job.workflowJobSections.forEach { js ->
                js.jobSectionId = DataConversion.toBigEndian(js.jobSectionId)!!
                js.projectSectionId = DataConversion.toBigEndian(js.projectSectionId)!!
                js.jobId = DataConversion.toBigEndian(js.jobId)
            }
            return job
        } catch (t: Throwable) {
            val message = "Unable to set BigEndian GUIDS: ${t.message ?: XIErrorHandler.UNKNOWN_ERROR}"
            postWorkflowStatus(XIResult.Error(LocalDataException(message), message))
            return null
        }
    }

    suspend fun getJobEstimationItemByEstimateId(estimateId: String) = withContext(dispatchers.io()) {
        return@withContext appDb.getJobItemEstimateDao().getJobItemEstimateForEstimateId(estimateId)
    }

    suspend fun insertOrUpdateJob(job: JobDTO) = withContext(dispatchers.io()) {
        return@withContext appDb.getJobDao().updateJob(job)
    }

    @Suppress("TooGenericExceptionCaught")
    // We have a really smart error-handler
    suspend fun updateApprovalInfo(userId: String, jobId: String, remarks: String): Boolean {
        try {

            val requestData = JsonObject()
            requestData.addProperty("UserId", userId)
            requestData.addProperty("JobId", jobId)
            requestData.addProperty("Remarks", remarks)
            Timber.d("Json Job: $requestData")

            val approvalResponse = apiRequest {
                api.updateApprovalInfo(requestData)
            }
            if (!approvalResponse.isSuccess) {
                throw ServiceException(approvalResponse.errorMessage!!)
            }
            return true
        } catch (e: Exception) {
            val message = "Failed to update approval information"
            Timber.e(e, message)
            throw TransmissionException(message, e)
        }
    }
}
