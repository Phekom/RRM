/*
 * Updated by Shaun McDonald on 2021/01/25
 * Last modified on 2021/01/25 6:30 PM
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 */

package za.co.xisystems.itis_rrm.data.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import za.co.xisystems.itis_rrm.custom.errors.LocalDataException
import za.co.xisystems.itis_rrm.custom.errors.ServiceException
import za.co.xisystems.itis_rrm.custom.errors.XIErrorHandler
import za.co.xisystems.itis_rrm.custom.events.XIEvent
import za.co.xisystems.itis_rrm.custom.results.XIResult.Error
import za.co.xisystems.itis_rrm.custom.results.XIResult
import za.co.xisystems.itis_rrm.custom.results.XIResult.Status
import za.co.xisystems.itis_rrm.custom.results.XIResult.Success
import za.co.xisystems.itis_rrm.data.localDB.AppDatabase
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemMeasureDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.ToDoListEntityDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.UserDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.WorkflowJobDTO
import za.co.xisystems.itis_rrm.data.network.BaseConnectionApi
import za.co.xisystems.itis_rrm.data.network.SafeApiRequest
import za.co.xisystems.itis_rrm.utils.ActivityIdConstants
import za.co.xisystems.itis_rrm.utils.Coroutines
import za.co.xisystems.itis_rrm.utils.DataConversion

/**
 * Created by Francis Mahlava on 2019/11/28.
 */

class MeasureApprovalDataRepository(
    private val api: BaseConnectionApi,
    private val appDb: AppDatabase
) : SafeApiRequest() {
    companion object {
        val TAG: String = MeasureApprovalDataRepository::class.java.simpleName
    }

    val workflowStatus: MutableLiveData<XIEvent<XIResult<String>>> = MutableLiveData()

    suspend fun getUser(): LiveData<UserDTO> {
        return withContext(Dispatchers.IO) {
            appDb.getUserDao().getUser()
        }
    }

    suspend fun getJobItemMeasureByItemMeasureId(itemMeasureId: String): LiveData<JobItemMeasureDTO> {
        return withContext(Dispatchers.IO) {
            appDb.getJobItemMeasureDao().getJobItemMeasureByItemMeasureId(itemMeasureId)
        }
    }

    suspend fun getJobApproveMeasureForActivityId(activityId: Int): LiveData<List<JobItemMeasureDTO>> {
        return withContext(Dispatchers.IO) {
            appDb.getJobItemMeasureDao().getJobApproveMeasureForActivityId(activityId)
        }
    }

    suspend fun getJobsMeasureForActivityId(
        estimateComplete: Int,
        measureComplete: Int,
        estWorksComplete: Int,
        jobApproved: Int
    ): LiveData<List<JobDTO>> {
        return withContext(Dispatchers.IO) {
            appDb.getJobDao().getJobsMeasureForActivityIds(
                estimateComplete,
                measureComplete,
                estWorksComplete,
                jobApproved
            )
        }
    }

    suspend fun getEntitiesListForActivityId(activityId: Int): LiveData<List<ToDoListEntityDTO>> {
        return withContext(Dispatchers.IO) {
            appDb.getEntitiesDao().getEntitiesListForActivityId(activityId)
        }
    }

    suspend fun getJobMeasureItemsForJobId(
        jobID: String?,
        actId: Int
    ): LiveData<List<JobItemMeasureDTO>> {
        return withContext(Dispatchers.IO) {
            appDb.getJobItemMeasureDao().getJobItemMeasuresByJobIdAndActId(jobID!!, actId)
        }
    }

    suspend fun getUOMForProjectItemId(projectItemId: String): String {
        return withContext(Dispatchers.IO) {
            appDb.getProjectItemDao().getUOMForProjectItemId(projectItemId)
        }
    }

    suspend fun processWorkflowMove(
        userId: String,
        measurements: List<JobItemMeasureDTO>,
        direction: Int
    ) {
        Coroutines.io {
            try {
                val description = ""

                var flowJob: WorkflowJobDTO? = null
                measurements.forEachIndexed { index, measure ->
                    val measureTrackId = DataConversion.toLittleEndian(measure.trackRouteId)
                    measureTrackId?.let {
                        withContext(Dispatchers.IO) {
                            postWorkflowStatus(Status("Processing ${index + 1} of ${measurements.size} measurements"))
                            val workflowMoveResponse =
                                apiRequest { api.getWorkflowMove(userId, measureTrackId, description, direction) }
                            workflowMoveResponse.workflowJob?.let { job ->
                                job.workflowItemMeasures.forEach { jobItemMeasure ->
                                    if (jobItemMeasure.actId == ActivityIdConstants.MEASURE_APPROVED) {
                                        val itemMeasureId = DataConversion.toBigEndian(jobItemMeasure.itemMeasureId)
                                        val trackRouteId = DataConversion.toBigEndian(jobItemMeasure.trackRouteId)
                                        val measureGroupId = DataConversion.toBigEndian(jobItemMeasure.measureGroupId)
                                        appDb.getJobItemMeasureDao().updateWorkflowJobItemMeasure(
                                            itemMeasureId,
                                            trackRouteId,
                                            jobItemMeasure.actId,
                                            measureGroupId
                                        )
                                    }
                                }
                                flowJob = job
                            }
                        }
                    }
                }
                saveWorkflowJob(flowJob!!)
                postWorkflowStatus(Success("WORK_COMPLETE"))
            } catch (t: Throwable) {
                val message = "Failed to Approve Measurement: ${t.message ?: XIErrorHandler.UNKNOWN_ERROR}"
                postWorkflowStatus(Error(t, message))
            }
        }
    }

    private suspend fun postWorkflowStatus(status: XIResult<String>) {
        withContext(Dispatchers.Main) {
            workflowStatus.postValue(XIEvent(status))
        }
    }

    suspend fun getSectionForProjectSectionId(sectionId: String?): String {
        return withContext(Dispatchers.IO) {
            appDb.getProjectSectionDao().getSectionForProjectSectionId(sectionId!!)
        }
    }

    suspend fun getItemDescription(jobId: String): String {
        return withContext(Dispatchers.IO) {
            appDb.getJobDao().getItemDescription(jobId)
        }
    }

    suspend fun getRouteForProjectSectionId(sectionId: String?): String {
        return withContext(Dispatchers.IO) {
            appDb.getProjectSectionDao().getRouteForProjectSectionId(sectionId!!)
        }
    }

    suspend fun getProjectSectionIdForJobId(jobId: String?): String {
        return withContext(Dispatchers.IO) {
            appDb.getJobSectionDao().getProjectSectionId(jobId!!)
        }
    }

    suspend fun getProjectItemDescription(projectItemId: String): String {
        return withContext(Dispatchers.IO) {
            appDb.getProjectItemDao().getProjectItemDescription(projectItemId)
        }
    }

    suspend fun getJobMeasureItemPhotoPaths(itemMeasureId: String): List<String> {
        return withContext(Dispatchers.IO) {
            appDb.getJobItemMeasurePhotoDao().getJobMeasureItemPhotoPaths(itemMeasureId)
        }
    }

    private suspend fun saveWorkflowJob(workflowJob: WorkflowJobDTO) {
        val job = setWorkflowJobBigEndianGuids(workflowJob)
        job?.let {
            updateWorkflowJobValuesAndInsertWhenNeeded(job)
        }
    }

    private suspend fun updateWorkflowJobValuesAndInsertWhenNeeded(job: WorkflowJobDTO) {
        Coroutines.io {
            try {

                job.workflowItemEstimates.forEach { jobItemEstimate ->
                    appDb.getJobItemEstimateDao().updateExistingJobItemEstimateWorkflow(
                        jobItemEstimate.trackRouteId,
                        jobItemEstimate.actId,
                        jobItemEstimate.estimateId
                    )

                    jobItemEstimate.workflowEstimateWorks.forEach { jobEstimateWorks ->
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

                appDb.getJobDao().updateJob(job.trackRouteId, job.actId, job.jiNo, job.jobId)

                Timber.d("Updated Workflow: $job")
                postWorkflowStatus(Success("WORK_COMPLETE"))
            } catch (t: Throwable) {
                val message = "Could not save updated workflow: ${t.message ?: XIErrorHandler.UNKNOWN_ERROR}"
                Timber.e(t, message)
                postWorkflowStatus(Error(LocalDataException(message), message))
            }
        }
    }

    private fun setWorkflowJobBigEndianGuids(job: WorkflowJobDTO): WorkflowJobDTO? {
        try {
            job.jobId = DataConversion.toBigEndian(job.jobId)
            job.trackRouteId = DataConversion.toBigEndian(job.trackRouteId)
            job.workflowItemEstimates.forEach { jie ->
                jie.estimateId = DataConversion.toBigEndian(jie.estimateId)!!
                jie.trackRouteId = DataConversion.toBigEndian(jie.trackRouteId)!!
                //  Lets go through the WorkFlowEstimateWorks
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
            Coroutines.io {
                postWorkflowStatus(
                     Error(
                        LocalDataException(t.message ?: XIErrorHandler.UNKNOWN_ERROR),
                        t.message ?: XIErrorHandler.UNKNOWN_ERROR
                    )
                )
            }
            return null
        }
    }

    suspend fun upDateMeasure(
        newQuantity: String,
        itemMeasureId: String
    ): String {
        val newMeasureId = DataConversion.toLittleEndian(itemMeasureId)

        val quantityUpdateResponse =
            apiRequest { api.upDateMeasureQty(newMeasureId, newQuantity.toDouble()) }
        postQuantity(
            quantityUpdateResponse.errorMessage,
            itemMeasureId,
            newQuantity.toDouble()
        )
        val messages = quantityUpdateResponse.errorMessage ?: ""
        return withContext(Dispatchers.IO) {
            messages
        }
    }

    private fun postQuantity(
        errorMessage: String?,
        itemMeasureId: String?,
        newQuantity: Double
    ) {
        if (errorMessage.isNullOrBlank()) {
            appDb.getJobItemMeasureDao().upDateQty(itemMeasureId!!, newQuantity)
        } else {
            val message = "Failed to update Quantity: $errorMessage"
            val serviceException = ServiceException(message)
            Timber.e(serviceException)
            Coroutines.main {
                postWorkflowStatus(Error(serviceException, message))
            }
        }
    }

    suspend fun getQuantityForMeasureItemId(itemMeasureId: String): LiveData<Double> {
        return withContext(Dispatchers.IO) {
            appDb.getJobItemMeasureDao().getQuantityForMeasureItemId(itemMeasureId)
        }
    }

    suspend fun getLineRateForMeasureItemId(itemMeasureId: String): LiveData<Double> {
        return withContext(Dispatchers.IO) {
            appDb.getJobItemMeasureDao().getLineRateForMeasureItemId(itemMeasureId)
        }
    }
}
