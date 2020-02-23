package za.co.xisystems.itis_rrm.data.repositories

//import sun.security.krb5.Confounder.bytes

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import za.co.xisystems.itis_rrm.data.localDB.AppDatabase
import za.co.xisystems.itis_rrm.data.localDB.entities.*
import za.co.xisystems.itis_rrm.data.network.BaseConnectionApi
import za.co.xisystems.itis_rrm.data.network.SafeApiRequest
import za.co.xisystems.itis_rrm.data.preferences.PreferenceProvider
import za.co.xisystems.itis_rrm.utils.*


/**
 * Created by Francis Mahlava on 2019/11/28.
 */

class JobApprovalDataRepository(private val api: BaseConnectionApi, private val Db: AppDatabase, private val prefs: PreferenceProvider) : SafeApiRequest() {
    companion object {
        val TAG: String = JobApprovalDataRepository::class.java.simpleName
    }


    private val workflowJ = MutableLiveData<WorkflowJobDTO>()


    init {

        workflowJ.observeForever {
            saveWorkflowJob(it)

        }


    }


    suspend fun getUser(): LiveData<UserDTO> {
        return withContext(Dispatchers.IO) {
            Db.getUserDao().getuser()
        }
    }


    suspend fun getSectionForProjectSectionId(sectionId: String?): String {
        return withContext(Dispatchers.IO) {
            Db.getProjectSectionDao().getSectionForProjectSectionId(sectionId!!)
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

    suspend fun processWorkflowMove(
        userId: String,
        trackRouteId: String,
        description: String?,
        direction: Int
    ) {
        val workflowMoveResponse =
            apiRequest { api.getWorkflowMove(userId, trackRouteId, description, direction) }
        workflowJ.postValue(workflowMoveResponse.workflowJob)
//        workflows.postValue(workflowMoveResponse.toDoListGroups)

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

    private fun saveWorkflowJob(workflowj : WorkflowJobDTO?) {
        if (workflowj != null) {
            val job = setWorkflowJobBigEndianGuids(workflowj)
            insertOrUpdateWorkflowJobInSQLite(job)
        }else {
//            Looper.prepare() // to be able to make toast
//        Toast.makeText(activity, "Error: WorkFlow Job is null", Toast.LENGTH_LONG).show()
            Log.e("Error:", " WorkFlow Job is null")
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

            if (job.workflowItemEstimates != null && job.workflowItemEstimates.size !== 0) {
                for (jobItemEstimate in job.workflowItemEstimates) {
                    Db.getJobItemEstimateDao().updateExistingJobItemEstimateWorkflow(
                        jobItemEstimate.trackRouteId,
                        jobItemEstimate.actId,
                        jobItemEstimate.estimateId
                    )

                    if (jobItemEstimate.workflowEstimateWorks != null) {
                        for (jobEstimateWorks in jobItemEstimate.workflowEstimateWorks) {
                            if (!Db.getEstimateWorkDao().checkIfJobEstimateWorksExist(
                                    jobEstimateWorks.worksId
                                )
                            )
                                Db.getEstimateWorkDao().insertJobEstimateWorks(
                                    jobEstimateWorks as JobEstimateWorksDTO
                                ) else Db.getEstimateWorkDao().updateJobEstimateWorksWorkflow(
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

                if (job.workflowItemMeasures != null && job.workflowItemMeasures.size !== 0) {
                    for (jobItemMeasure in job.workflowItemMeasures) {
                        Db?.getJobItemMeasureDao()!!.updateWorkflowJobItemMeasure(
                            jobItemMeasure.itemMeasureId,
                            jobItemMeasure.trackRouteId,
                            jobItemMeasure.actId,
                            jobItemMeasure.measureGroupId
                        )
                    }
                }

            }

            //  Place the Job Section, UPDATE OR CREATE
            if (job.workflowJobSections != null && job.workflowJobSections.size !== 0) {
                for (jobSection in job.workflowJobSections) {
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
    }


    private fun setWorkflowJobBigEndianGuids(job: WorkflowJobDTO): WorkflowJobDTO? {
        job.actId = job.actId
        job.jobId = DataConversion.toBigEndian(job.jobId)
        job.trackRouteId = DataConversion.toBigEndian(job.trackRouteId)
        job.jiNo = job.jiNo
        if (job.workflowItemEstimates != null) {
            for (jie in job.workflowItemEstimates) {
                jie.actId = jie.actId
                jie.estimateId = DataConversion.toBigEndian(jie.estimateId)!!
                jie.trackRouteId = DataConversion.toBigEndian(jie.trackRouteId)!!
                //  Lets go through the WorkFlowEstimateWorks
                for (wfe in jie.workflowEstimateWorks) {
                    wfe.trackRouteId = DataConversion.toBigEndian(wfe.trackRouteId)!!
                    wfe.worksId = DataConversion.toBigEndian(wfe.worksId)!!
                    wfe.actId = wfe.actId
                    wfe.estimateId = DataConversion.toBigEndian(wfe.estimateId)!!
                    wfe.recordVersion = wfe.recordVersion
                    wfe.recordSynchStateId = wfe.recordSynchStateId
                }
            }
        }
        if (job.workflowItemMeasures != null) {
            for (jim in job.workflowItemMeasures) {
                jim.actId = jim.actId
                jim.itemMeasureId = DataConversion.toBigEndian(jim.itemMeasureId)!!
                jim.measureGroupId = DataConversion.toBigEndian(jim.measureGroupId)!!
                jim.trackRouteId = DataConversion.toBigEndian(jim.trackRouteId)!!
            }
        }
        if (job.workflowJobSections != null) {
            for (js in job.workflowJobSections) {
                js.jobSectionId = DataConversion.toBigEndian(js.jobSectionId)!!
                js.projectSectionId = DataConversion.toBigEndian(js.projectSectionId)!!
                js.jobId = DataConversion.toBigEndian(js.jobId)
            }
        }
        return job
    }


    private operator fun <T> LiveData<T>.not(): Boolean {
        return true
    }



}












































