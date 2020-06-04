package za.co.xisystems.itis_rrm.data.repositories

//import sun.security.krb5.Confounder.bytes

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import za.co.xisystems.itis_rrm.data.localDB.AppDatabase
import za.co.xisystems.itis_rrm.data.localDB.entities.*
import za.co.xisystems.itis_rrm.data.network.BaseConnectionApi
import za.co.xisystems.itis_rrm.data.network.SafeApiRequest
import za.co.xisystems.itis_rrm.utils.Coroutines
import za.co.xisystems.itis_rrm.utils.DataConversion


/**
 * Created by Francis Mahlava on 2019/11/28.
 */

class JobApprovalDataRepository(
    private val api: BaseConnectionApi,
    private val Db: AppDatabase
) : SafeApiRequest() {
    companion object {
        val TAG: String = JobApprovalDataRepository::class.java.simpleName
    }


    private val workflowJ = MutableLiveData<WorkflowJobDTO>()
    private val qtyUpDate = MutableLiveData<String>()

    init {

        workflowJ.observeForever {
            saveWorkflowJob(it)

        }
        qtyUpDate.observeForever {
            saveqtyUpDate(it)

        }

    }

    private fun saveqtyUpDate( newQty : String?) {

    }



    suspend fun getUser(): LiveData<UserDTO> {
        return withContext(Dispatchers.IO) {
            Db.getUserDao().getUser()
        }
    }


    suspend fun getSectionForProjectSectionId(sectionId: String?): String {
        return withContext(Dispatchers.IO) {
            Db.getProjectSectionDao().getSectionForProjectSectionId(sectionId!!)
        }
    }

    suspend fun getTenderRateForProjectItemId(projectItemId: String): Double {
        return withContext(Dispatchers.IO) {
            Db.getProjectItemDao().getTenderRateForProjectItemId(projectItemId)
        }
    }


    suspend fun getUOMForProjectItemId(projectItemId: String): String {
        return withContext(Dispatchers.IO) {
            Db.getProjectItemDao().getUOMForProjectItemId(projectItemId)
        }
    }

    suspend fun getRouteForProjectSectionId(sectionId: String?): String {
        return withContext(Dispatchers.IO) {
            Db.getProjectSectionDao().getRouteForProjectSectionId(sectionId!!)
        }
    }

    suspend fun getProjectSectionIdForJobId(jobId: String?): String {
        return withContext(Dispatchers.IO) {
            Db.getJobSectionDao().getProjectSectionId(jobId!!)
        }
    }


    suspend fun getProjectDescription(projectId: String): String {
        return withContext(Dispatchers.IO) {
            Db.getProjectDao().getProjectDescription(projectId)
        }
    }

    suspend fun getJobsForActivityId(activityId: Int): LiveData<List<JobDTO>> {
        return withContext(Dispatchers.IO) {
            Db.getJobDao().getJobsForActivityId(activityId)
        }
    }

    suspend fun upDateEstimate(newQuantity: String, newTotal: String, estimateId: String): String {
        val new_estimateId = DataConversion.toLittleEndian(estimateId)
       val new_Quantity = newQuantity + ".0"
       val new_Total = newTotal + ".0"

        val quantityUpdateResponse = apiRequest { api.updateEstimateQty(new_estimateId,new_Quantity.toDouble(), new_Total.toDouble() ) }
        qtyUpDate.postValue(quantityUpdateResponse.errorMessage , estimateId,new_Quantity.toDouble(), new_Total.toDouble())
        val messages = quantityUpdateResponse.errorMessage ?: ""
        return withContext(Dispatchers.IO) {
            messages
        }
    }

    private fun <T> MutableLiveData<T>.postValue(
        errorMessage: String?,
        newEstimateid: String?,
        new_Quantity: Double,
        new_Total: Double
    ) {
        if (errorMessage == null) {
                    Db.getJobItemEstimateDao().upDateLineRate(newEstimateid!!, new_Quantity, new_Total)
        } else {
            Timber.e("newQty is null")
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
        workflowJ.postValue(workflowMoveResponse.workflowJob)

        val messages = workflowMoveResponse.errorMessage ?: ""

        return withContext(Dispatchers.IO) {
            messages
        }
    }


    suspend fun getQuantityForEstimationItemId(estimateId: String): LiveData<Double> {
        return withContext(Dispatchers.IO) {
            Db.getJobItemEstimateDao().getQuantityForEstimationItemId(estimateId)
        }
    }


    suspend fun getLineRateForEstimationItemId(estimateId: String): LiveData<Double> {
        return withContext(Dispatchers.IO) {
            Db.getJobItemEstimateDao().getLineRateForEstimationItemId(estimateId)
        }
    }

    suspend fun getJobEstimationItemsForJobId(jobID: String?): LiveData<List<JobItemEstimateDTO>> {
        return withContext(Dispatchers.IO) {
            Db.getJobItemEstimateDao().getJobEstimationItemsForJobId2(jobID!!)
        }
    }

    suspend fun getProjectItemDescription(projectItemId: String): String {
        return withContext(Dispatchers.IO) {
            Db.getProjectItemDao().getProjectItemDescription(projectItemId)
        }
    }


    suspend fun getJobEstimationItemsPhotoStartPath(estimateId: String): String {
        return withContext(Dispatchers.IO) {
            Db.getJobItemEstimatePhotoDao().getJobEstimationItemsPhotoStartPath(estimateId)
        }
    }


    suspend fun getJobEstimationItemsPhotoEndPath(estimateId: String): String {
        return withContext(Dispatchers.IO) {
            Db.getJobItemEstimatePhotoDao().getJobEstimationItemsPhotoEndPath(estimateId)
        }
    }

    private fun saveWorkflowJob( workflowJob: WorkflowJobDTO? ) {
        try {
            val job = setWorkflowJobBigEndianGuids(workflowJob!!)
            insertOrUpdateWorkflowJobInSQLite(job!!)
        } catch (ex: NullPointerException) {
            Timber.e(ex, "Error: WorkFlow Job is null")
        }
    }


    private fun insertOrUpdateWorkflowJobInSQLite(job: WorkflowJobDTO?) {
        job?.let {
            updateWorkflowJobValuesAndInsertWhenNeeded(it)
        }
    }

    private fun updateWorkflowJobValuesAndInsertWhenNeeded(job: WorkflowJobDTO) {
        Coroutines.io {
            Db.getJobDao().updateJob(job.trackRouteId, job.actId, job.jiNo, job.jobId)

            job.workflowItemEstimates?.forEach { jobItemEstimate ->
                Db.getJobItemEstimateDao().updateExistingJobItemEstimateWorkflow(
                    jobItemEstimate.trackRouteId,
                    jobItemEstimate.actId,
                    jobItemEstimate.estimateId
                )


                jobItemEstimate.workflowEstimateWorks.forEach { jobEstimateWorks ->
                    if (!Db.getEstimateWorkDao()
                            .checkIfJobEstimateWorksExist(jobEstimateWorks.worksId)
                    )
                        Db.getEstimateWorkDao().insertJobEstimateWorks(
                            // TODO: b0rk3d! This broken cast needs fixing.
                            jobEstimateWorks as JobEstimateWorksDTO
                        ) else
                        Db.getEstimateWorkDao().updateJobEstimateWorksWorkflow(
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
                Db.getJobItemMeasureDao().updateWorkflowJobItemMeasure(
                    jobItemMeasure.itemMeasureId,
                    jobItemMeasure.trackRouteId,
                    jobItemMeasure.actId,
                    jobItemMeasure.measureGroupId
                )
            }


            //  Place the Job Section, UPDATE OR CREATE
            job.workflowJobSections?.forEach { jobSection ->
                if (!Db.getJobSectionDao().checkIfJobSectionExist(jobSection.jobSectionId))
                    Db.getJobSectionDao().insertJobSection(jobSection) else
                    Db.getJobSectionDao().updateExistingJobSectionWorkflow(
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
    }


    private fun setWorkflowJobBigEndianGuids(job: WorkflowJobDTO): WorkflowJobDTO? {
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
    }


    private operator fun <T> LiveData<T>.not(): Boolean {
        return true
    }




}














































