package za.co.xisystems.itis_rrm.data.localDB

import za.co.xisystems.itis_rrm.data.localDB.models.WorkflowJob
import za.co.xisystems.itis_rrm.data.repositories.OfflineDataRepository
import za.co.xisystems.itis_rrm.utils.DataConversion

/**
 * Created by Francis Mahlava on 2020/01/02.
 */
object JobDataController{

//    fun setWorkflowJobBigEndianGuids(workflowJob: WorkflowJob?): WorkflowJob {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//    }

    fun setWorkflowJobBigEndianGuids(job: WorkflowJob): WorkflowJob? {
        job.actId = job.actId
        job.jobId = DataConversion.toBigEndian(job.jobId)
        job.trackRouteId = DataConversion.toBigEndian(job.trackRouteId)
        job.jiNo = job.jiNo
        if (job.workflowItemEstimates != null) {
            for (jie in job.workflowItemEstimates) {
                jie.actId = jie.actId
                jie.estimateId = DataConversion.toBigEndian(jie.estimateId)
                jie.trackRouteId = DataConversion.toBigEndian(jie.trackRouteId)
                //  Lets go through the WorkFlowEstimateWorks
                for (wfe in jie.workflowEstimateWorks) {
                    wfe.trackRouteId = DataConversion.toBigEndian(wfe.trackRouteId)
                    wfe.worksId = DataConversion.toBigEndian(wfe.worksId)
                    wfe.actId = wfe.actId
                    wfe.estimateId = DataConversion.toBigEndian(wfe.estimateId)
                    wfe.recordVersion = wfe.recordVersion
                    wfe.recordSynchStateId = wfe.recordSynchStateId
                }
            }
        }
        if (job.workflowItemMeasures != null) {
            for (jim in job.workflowItemMeasures) {
                jim.actId = jim.actId
                jim.itemMeasureId = DataConversion.toBigEndian(jim.itemMeasureId)
                jim.measureGroupId = DataConversion.toBigEndian(jim.measureGroupId)
                jim.trackRouteId = DataConversion.toBigEndian(jim.trackRouteId)
            }
        }
        if (job.workflowJobSections != null) {
            for (js in job.workflowJobSections) {
                js.jobSectionId = DataConversion.toBigEndian(js.jobSectionId)
                js.projectSectionId = DataConversion.toBigEndian(js.projectSectionId)
                js.jobId = DataConversion.toBigEndian(js.jobId)
            }
        }
        return job
    }
    fun setMsg( uplodmsg : String?): String? {
      var uplodmsg = uplodmsg
        return uplodmsg
    }
    private val offlineDataRepository: OfflineDataRepository? = null

//    suspend fun getRoute(jobId: String?) : String?{
//        if (null == jobId || jobId.length == 0) return null
//        val sectionId = offlineDataRepository?.getProjectSectionIdForJobId(jobId)
//        val route = offlineDataRepository?.getRouteForProjectSectionId(sectionId!!)
//        return getRoute(route)
//    }


//    fun toLittleEndian(bigEndian: String?): String? {
//        if (null == bigEndian || bigEndian.length % 2 != 0) return null
//        val b = bigEndianHexStringToByteArray(bigEndian)
//        return toLittleEndian(b)
//    }

}