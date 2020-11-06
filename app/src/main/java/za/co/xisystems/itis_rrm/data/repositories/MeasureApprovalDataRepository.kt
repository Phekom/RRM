package za.co.xisystems.itis_rrm.data.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import za.co.xisystems.itis_rrm.custom.errors.NoResponseException
import za.co.xisystems.itis_rrm.custom.errors.ServiceException
import za.co.xisystems.itis_rrm.custom.errors.XIErrorHandler
import za.co.xisystems.itis_rrm.custom.results.XIError
import za.co.xisystems.itis_rrm.custom.results.XIResult
import za.co.xisystems.itis_rrm.custom.results.XISuccess
import za.co.xisystems.itis_rrm.data.localDB.AppDatabase
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemMeasureDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.ToDoListEntityDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.UserDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.WorkflowJobDTO
import za.co.xisystems.itis_rrm.data.network.BaseConnectionApi
import za.co.xisystems.itis_rrm.data.network.SafeApiRequest
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

    private val workflowJ = MutableLiveData<WorkflowJobDTO>()
    private val qtyUpDate = MutableLiveData<String>()

    init {

        workflowJ.observeForever {
            saveWorkflowJob(it)
        }
    }

    val workflowStatus: MutableLiveData<XIResult<WorkflowJobDTO>> = MutableLiveData()

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
            appDb.getJobItemMeasureDao().getJobMeasureItemsForJobId(jobID!!, actId)
        }
    }

    suspend fun getUOMForProjectItemId(projectItemId: String): String {
        return withContext(Dispatchers.IO) {
            appDb.getProjectItemDao().getUOMForProjectItemId(projectItemId)
        }
    }

    suspend fun processWorkflowMove(
        userId: String,
        trackRouteId: String,
        description: String?,
        direction: Int
    ): String {
        val workflowMoveResponse =
            apiRequest { api.getWorkflowMove(userId, trackRouteId, description, direction) }

        val messages: String = workflowMoveResponse.errorMessage ?: ""

        if (messages.isBlank())
            workflowJ.postValue(workflowMoveResponse.workflowJob)
        else {
            val message = "Could not process workflow: $messages"
            val wfThrowable = ServiceException(message)
            val wfError = XIError(wfThrowable, message)
            workflowStatus.postValue(wfError)
        }

        return withContext(Dispatchers.IO) {
            messages
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

    private fun saveWorkflowJob(workflowj: WorkflowJobDTO?) {
        if (workflowj != null) {
            val job = setWorkflowJobBigEndianGuids(workflowj)
            job?.let {
                insertOrUpdateWorkflowJobInSQLite(job)
            }
        } else {
            Timber.e("WorkFlow Job is null")
            val wfError = NoResponseException("Workflow Job is null")
            val xiErr = XIError(wfError, wfError.message ?: XIErrorHandler.UNKNOWN_ERROR)
            workflowStatus.postValue(xiErr)
        }
    }

    private fun insertOrUpdateWorkflowJobInSQLite(job: WorkflowJobDTO) {
        Coroutines.io {
            updateWorkflowJobValuesAndInsertWhenNeeded(job)
        }
    }

    private suspend fun updateWorkflowJobValuesAndInsertWhenNeeded(job: WorkflowJobDTO) {

        try {
            appDb.getJobDao().updateJob(job.trackRouteId, job.actId, job.jiNo, job.jobId)

            job.workflowItemEstimates?.forEach { jobItemEstimate ->
                appDb.getJobItemEstimateDao().updateExistingJobItemEstimateWorkflow(
                    jobItemEstimate.trackRouteId,
                    jobItemEstimate.actId,
                    jobItemEstimate.estimateId
                )

                jobItemEstimate.workflowEstimateWorks.forEach { jobEstimateWorks ->
                    if (!appDb.getEstimateWorkDao()
                            .checkIfJobEstimateWorksExist(jobEstimateWorks.worksId)
                    )
                        appDb.getEstimateWorkDao().insertJobEstimateWorks(
                            // TODO: b0rk3d - this broken cast needs fixing.
                            // jobEstimateWorks as JobEstimateWorksDTO
                            TODO("This should never happen!")
                        )
                    else
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

            job.workflowItemMeasures?.forEach { jobItemMeasure ->
                appDb.getJobItemMeasureDao().updateWorkflowJobItemMeasure(
                    jobItemMeasure.itemMeasureId,
                    jobItemMeasure.trackRouteId,
                    jobItemMeasure.actId,
                    jobItemMeasure.measureGroupId
                )
            }

            //  Place the Job Section, UPDATE OR CREATE

            job.workflowJobSections?.forEach { jobSection ->
                if (!appDb.getJobSectionDao().checkIfJobSectionExist(jobSection.jobSectionId))
                    appDb.getJobSectionDao().insertJobSection(jobSection)
                else
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
            val jobSuccess = XISuccess(data = job)
            workflowStatus.postValue(jobSuccess)
        } catch (t: Throwable) {
            val message = "Could not save updated workflow: ${ t.localizedMessage ?: XIErrorHandler.UNKNOWN_ERROR}"
            Timber.e(t, message)
            val dbError = XIError(t, message)
            workflowStatus.postValue(dbError)
        }
    }

    private fun setWorkflowJobBigEndianGuids(job: WorkflowJobDTO): WorkflowJobDTO? {
        job.jobId = DataConversion.toBigEndian(job.jobId)
        job.trackRouteId = DataConversion.toBigEndian(job.trackRouteId)
        job.workflowItemEstimates?.forEach { jie ->
            jie.estimateId = DataConversion.toBigEndian(jie.estimateId)!!
            jie.trackRouteId = DataConversion.toBigEndian(jie.trackRouteId)!!
            //  Lets go through the WorkFlowEstimateWorks
            for (wfe in jie.workflowEstimateWorks) {
                wfe.trackRouteId = DataConversion.toBigEndian(wfe.trackRouteId)!!
                wfe.worksId = DataConversion.toBigEndian(wfe.worksId)!!
                wfe.estimateId = DataConversion.toBigEndian(wfe.estimateId)!!
            }
        }

        job.workflowItemMeasures?.forEach { jim ->
            jim.itemMeasureId = DataConversion.toBigEndian(jim.itemMeasureId)!!
            jim.measureGroupId = DataConversion.toBigEndian(jim.measureGroupId)!!
            jim.trackRouteId = DataConversion.toBigEndian(jim.trackRouteId)!!
        }

        job.workflowJobSections?.forEach { js ->
            js.jobSectionId = DataConversion.toBigEndian(js.jobSectionId)!!
            js.projectSectionId = DataConversion.toBigEndian(js.projectSectionId)!!
            js.jobId = DataConversion.toBigEndian(js.jobId)
        }

        return job
    }

    suspend fun upDateMeasure(
        newQuantity: String,
        itemMeasureId: String
    ): String {
        val newMeasureId = DataConversion.toLittleEndian(itemMeasureId)

        val quantityUpdateResponse =
            apiRequest { api.upDateMeasureQty(newMeasureId, newQuantity.toDouble()) }
        qtyUpDate.postValue(
            quantityUpdateResponse.errorMessage,
            itemMeasureId,
            newQuantity.toDouble()
        )
        val messages = quantityUpdateResponse.errorMessage ?: ""
        return withContext(Dispatchers.IO) {
            messages
        }
    }

    private fun <T> MutableLiveData<T>.postValue(
        errorMessage: String?,
        itemMeasureId: String?,
        new_Quantity: Double
    ) {
        if (errorMessage.isNullOrBlank()) {
            appDb.getJobItemMeasureDao().upDateQty(itemMeasureId!!, new_Quantity)
        } else {
            Timber.e("newQty is null")
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
