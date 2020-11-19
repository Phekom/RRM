package za.co.xisystems.itis_rrm.data.repositories

// import sun.security.krb5.Confounder.bytes

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import za.co.xisystems.itis_rrm.custom.errors.LocalDataException
import za.co.xisystems.itis_rrm.custom.errors.ServiceException
import za.co.xisystems.itis_rrm.custom.errors.XIErrorHandler
import za.co.xisystems.itis_rrm.custom.events.XIEvent
import za.co.xisystems.itis_rrm.custom.results.XIError
import za.co.xisystems.itis_rrm.custom.results.XIResult
import za.co.xisystems.itis_rrm.custom.results.XISuccess
import za.co.xisystems.itis_rrm.data.localDB.AppDatabase
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobEstimateWorksDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimateDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.UserDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.WorkflowJobDTO
import za.co.xisystems.itis_rrm.data.network.BaseConnectionApi
import za.co.xisystems.itis_rrm.data.network.SafeApiRequest
import za.co.xisystems.itis_rrm.utils.DataConversion
import za.co.xisystems.itis_rrm.utils.enums.WorkflowDirection

/**
 * Created by Francis Mahlava on 2019/11/28.
 */

class JobApprovalDataRepository(
    private val api: BaseConnectionApi,
    private val appDb: AppDatabase
) : SafeApiRequest() {
    companion object {
        val TAG: String = JobApprovalDataRepository::class.java.simpleName
    }

    private val qtyUpDate = MutableLiveData<String>()

    var workflowStatus: MutableLiveData<XIEvent<XIResult<String>>> = MutableLiveData()

    suspend fun getUser(): LiveData<UserDTO> {
        return withContext(Dispatchers.IO) {
            appDb.getUserDao().getUser()
        }
    }

    suspend fun getSectionForProjectSectionId(sectionId: String?): String {
        return withContext(Dispatchers.IO) {
            appDb.getProjectSectionDao().getSectionForProjectSectionId(sectionId!!)
        }
    }

    suspend fun getTenderRateForProjectItemId(projectItemId: String): Double {
        return withContext(Dispatchers.IO) {
            appDb.getProjectItemDao().getTenderRateForProjectItemId(projectItemId)
        }
    }

    suspend fun getUOMForProjectItemId(projectItemId: String): String {
        return withContext(Dispatchers.IO) {
            appDb.getProjectItemDao().getUOMForProjectItemId(projectItemId)
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

    suspend fun getProjectDescription(projectId: String): String {
        return withContext(Dispatchers.IO) {
            appDb.getProjectDao().getProjectDescription(projectId)
        }
    }

    suspend fun getJobsForActivityId(activityId: Int): LiveData<List<JobDTO>> {
        return withContext(Dispatchers.IO) {
            appDb.getJobDao().getJobsForActivityId(activityId)
        }
    }

    suspend fun upDateEstimate(newQuantity: String, newTotal: String, estimateId: String): String {
        val new_estimateId = DataConversion.toLittleEndian(estimateId)

        val quantityUpdateResponse = apiRequest {
            api.updateEstimateQty(
                new_estimateId,
                newQuantity.toDouble(),
                newTotal.toDouble()
            )
        }
        qtyUpDate.postValue(
            quantityUpdateResponse.errorMessage,
            estimateId,
            newQuantity.toDouble(),
            newTotal.toDouble()
        )
        val messages = quantityUpdateResponse.errorMessage ?: ""
        return withContext(Dispatchers.IO) {
            messages
        }
    }

    private fun <T> MutableLiveData<T>.postValue(
        errorMessage: String?,
        newEstimateId: String?,
        new_Quantity: Double,
        new_Total: Double
    ) {
        if (errorMessage == null) {
            appDb.getJobItemEstimateDao().upDateLineRate(newEstimateId!!, new_Quantity, new_Total)
        } else {
            Timber.e("newQty is null")
        }
    }

    suspend fun processWorkflowMove(
        userId: String,
        jobId: String,
        trackRouteId: String,
        description: String?,
        direction: Int
    ) {
        withContext(Dispatchers.IO) {
            val workflowMoveResponse =
                apiRequest { api.getWorkflowMove(userId, trackRouteId, description, direction) }
            val messages: String = workflowMoveResponse.errorMessage ?: ""
            if (workflowMoveResponse.workflowJob != null) {

                if (messages.isBlank()) {
                    workflowMoveResponse.workflowJob?.let { saveWorkflowJob(it) }
                } else {
                    postServiceException(messages)
                }
            } else {
                when (messages == "No Job found." && direction == WorkflowDirection.FAIL.value) {
                    true -> {
                        appDb.getJobDao().softDeleteJobForJobId(jobId)

                        workflowStatus.postValue(XIEvent(XISuccess("DECLINED")))
                    }
                    else -> {
                        postServiceException(messages)
                    }
                }
            }
        }
    }

    fun postServiceException(message: String) {
        val info = "Job Workflow Service Exception: $message"
        workflowStatus.postValue(XIEvent(XIError(ServiceException(info), info)))
    }

    suspend fun getQuantityForEstimationItemId(estimateId: String): LiveData<Double> {
        return withContext(Dispatchers.IO) {
            appDb.getJobItemEstimateDao().getQuantityForEstimationItemId(estimateId)
        }
    }

    suspend fun getLineRateForEstimationItemId(estimateId: String): LiveData<Double> {
        return withContext(Dispatchers.IO) {
            appDb.getJobItemEstimateDao().getLineRateForEstimationItemId(estimateId)
        }
    }

    suspend fun getJobEstimationItemsForJobId(jobID: String?): LiveData<List<JobItemEstimateDTO>> {
        return withContext(Dispatchers.IO) {
            appDb.getJobItemEstimateDao().getJobEstimationItemsForJobId2(jobID!!)
        }
    }

    suspend fun getProjectItemDescription(projectItemId: String): String {
        return withContext(Dispatchers.IO) {
            appDb.getProjectItemDao().getProjectItemDescription(projectItemId)
        }
    }

    suspend fun getJobEstimationItemsPhotoStartPath(estimateId: String): String {
        return withContext(Dispatchers.IO) {
            appDb.getJobItemEstimatePhotoDao().getJobEstimationItemsPhotoStartPath(estimateId)
        }
    }

    suspend fun getJobEstimationItemsPhotoEndPath(estimateId: String): String {
        return withContext(Dispatchers.IO) {
            appDb.getJobItemEstimatePhotoDao().getJobEstimationItemsPhotoEndPath(estimateId)
        }
    }

    private suspend fun saveWorkflowJob(workflowJob: WorkflowJobDTO) {
        withContext(Dispatchers.IO) {
            val job = setWorkflowJobBigEndianGuids(workflowJob)
            job?.let {
                updateWorkflowJobValuesAndInsertWhenNeeded(job)
            }
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
                    ) {
                        // Create Bare Bones
                        val estimateWorks = JobEstimateWorksDTO(
                            worksId = jobEstimateWorks.worksId,
                            estimateId = jobEstimateWorks.estimateId,
                            recordVersion = jobEstimateWorks.recordVersion,
                            recordSynchStateId = jobEstimateWorks.recordSynchStateId,
                            actId = jobEstimateWorks.actId,
                            trackRouteId = jobEstimateWorks.trackRouteId,
                            jobEstimateWorksPhotos = ArrayList()
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
                val jobSuccess = XISuccess(data = it)
                workflowStatus.postValue(XIEvent(jobSuccess))
            }

        } catch (t: Throwable) {
            val message = "Could not save updated workflow: ${t.message ?: XIErrorHandler.UNKNOWN_ERROR}"
            Timber.e(t, message)
            workflowStatus.postValue(XIEvent(XIError(LocalDataException(message), message)))
        }
    }

    private fun setWorkflowJobBigEndianGuids(job: WorkflowJobDTO): WorkflowJobDTO? {
        try {
            job.jobId = DataConversion.toBigEndian(job.jobId)
            job.trackRouteId = DataConversion.toBigEndian(job.trackRouteId)

            job.workflowItemEstimates?.forEach { jie ->
                jie.estimateId = DataConversion.toBigEndian(jie.estimateId)!!
                jie.trackRouteId = DataConversion.toBigEndian(jie.trackRouteId)!!
                //  Let's go through the WorkFlowEstimateWorks
                jie.workflowEstimateWorks.forEach { wfe ->
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
        } catch (t: Throwable) {
            val message = "Unable to set BigEndian GUIDS: ${t.message ?: XIErrorHandler.UNKNOWN_ERROR}"
            workflowStatus.postValue(XIEvent(XIError(LocalDataException(message), message)))
            return null
        }
    }
}
