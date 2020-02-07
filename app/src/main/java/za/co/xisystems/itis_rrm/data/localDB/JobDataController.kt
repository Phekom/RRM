package za.co.xisystems.itis_rrm.data.localDB

import org.apache.commons.lang3.SerializationUtils
import za.co.xisystems.itis_rrm.data.localDB.entities.*
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

    fun setWorkflowJobBigEndianGuids(job: WorkflowJobDTO): WorkflowJobDTO {
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
                js.jobSectionId = DataConversion.toBigEndian(js.jobSectionId)!!
                js.projectSectionId = DataConversion.toBigEndian(js.projectSectionId)
                js.jobId = DataConversion.toBigEndian(js.jobId)
            }
        }
        return job
    }


    fun setJobLittleEndianGuids(_job: JobDTOTemp?): JobDTOTemp? {
        val job: JobDTOTemp = SerializationUtils.clone(_job)!!
        job.setJobId(DataConversion.toLittleEndian(job.JobId))
        job.setProjectId(DataConversion.toLittleEndian(job.ProjectId))
        job.setPerfitemGroupId(DataConversion.toLittleEndian(job.PerfitemGroupId))
        job.setProjectVoId(DataConversion.toLittleEndian(job.ProjectVoId))
        job.setTrackRouteId(DataConversion.toLittleEndian(job.TrackRouteId))
        job.setContractVoId(DataConversion.toLittleEndian(job.ContractVoId))
        job.setVoId(DataConversion.toLittleEndian(job.VoId))
        if (job.JobSections != null) {
            for (jobSection in job.JobSections!!) {
                if (jobSection != null) {
                    jobSection.setJobSectionId(DataConversion.toLittleEndian(jobSection.jobSectionId))
                    jobSection.setProjectSectionId(DataConversion.toLittleEndian(jobSection.projectSectionId))
                    jobSection.setJobId(DataConversion.toLittleEndian(jobSection.jobId))
                }
            }
        }
        if (job.JobItemEstimates != null) {
            for (jie in job.JobItemEstimates!!) {
                jie.setEstimateId(DataConversion.toLittleEndian(jie.estimateId))
                jie.setJobId(DataConversion.toLittleEndian(jie.jobId))
                jie.setProjectItemId(DataConversion.toLittleEndian(jie.projectItemId))
                jie.setTrackRouteId(DataConversion.toLittleEndian(jie.trackRouteId))
                jie.setProjectVoId(DataConversion.toLittleEndian(jie.projectVoId))
                if (jie.jobItemEstimatePhotos != null) {
                    for (jiep in jie.jobItemEstimatePhotos!!) {
                        jiep.setPhotoId(DataConversion.toLittleEndian(jiep.photoId))
                        jiep.setEstimateId(DataConversion.toLittleEndian(jiep.estimateId))
                    }
                }
                if (jie.jobEstimateWorks != null) {
                    for (jew in jie.jobEstimateWorks!!) {
                        jew.setWorksId(DataConversion.toLittleEndian(jew.worksId))
                        jew.setEstimateId(DataConversion.toLittleEndian(jew.estimateId))
                        jew.setTrackRouteId(DataConversion.toLittleEndian(jew.trackRouteId))
                        if (jew.jobEstimateWorksPhotos != null) {
                            for (ewp in jew.jobEstimateWorksPhotos!!) {
                                ewp.setWorksId(DataConversion.toLittleEndian(ewp.worksId))
                                ewp.setPhotoId(DataConversion.toLittleEndian(ewp.photoId))
                            }
                        }
                    }
                }
            }
        }
        if (job.JobItemMeasures != null) {
            for (jim in job.JobItemMeasures!!) {
                jim.setItemMeasureId(DataConversion.toLittleEndian(jim.itemMeasureId))
                jim.setJobId(DataConversion.toLittleEndian(jim.jobId))
                jim.setProjectItemId(DataConversion.toLittleEndian(jim.projectItemId))
                jim.setMeasureGroupId(DataConversion.toLittleEndian(jim.measureGroupId))
                jim.setEstimateId(DataConversion.toLittleEndian(jim.estimateId))
                jim.setProjectVoId(DataConversion.toLittleEndian(jim.projectVoId))
                jim.setTrackRouteId(DataConversion.toLittleEndian(jim.trackRouteId))
                if (jim.jobItemMeasurePhotos != null) {
                    for (jimp in jim.jobItemMeasurePhotos) {
                        jimp.setPhotoId(DataConversion.toLittleEndian(jimp.photoId))
                        jimp.setItemMeasureId(DataConversion.toLittleEndian(jimp.itemMeasureId))
                    }
                }
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

private fun JobItemEstimateDTO.setProjectVoId(toLittleEndian: String?) {
    this.projectVoId = toLittleEndian
}

private fun JobItemEstimateDTO.setTrackRouteId(toLittleEndian: String?) {
    this.trackRouteId = toLittleEndian!!
}

private fun JobItemEstimateDTO.setProjectItemId(toLittleEndian: String?) {
    this.projectItemId = toLittleEndian
}

private fun JobItemEstimateDTO.setJobId(toLittleEndian: String?) {
    this.jobId = toLittleEndian
}

private fun JobEstimateWorksDTO.setWorksId(toLittleEndian: String?) {
    this.worksId = toLittleEndian!!
}

private fun JobEstimateWorksDTO.setEstimateId(toLittleEndian: String?) {
    this.estimateId = toLittleEndian
}

private fun JobEstimateWorksDTO.setTrackRouteId(toLittleEndian: String?) {
    this.trackRouteId = toLittleEndian!!
}

private fun JobItemEstimatesPhotoDTO.setEstimateId(toLittleEndian: String?) {
    this.estimateId = toLittleEndian!!
}

private fun JobItemEstimateDTO.setEstimateId(toLittleEndian: String?) {
    this.estimateId =  toLittleEndian!!
}

private fun JobItemMeasureDTO.setProjectItemId(toLittleEndian: String?) {
    this.projectItemId = toLittleEndian
}

private fun JobItemMeasureDTO.setMeasureGroupId(toLittleEndian: String?) {
    this.measureGroupId = toLittleEndian
}

private fun JobItemMeasureDTO.setEstimateId(toLittleEndian: String?) {
this.estimateId = toLittleEndian
}

private fun JobItemMeasureDTO.setProjectVoId(toLittleEndian: String?) {
    this.projectVoId = toLittleEndian
}

private fun JobItemMeasureDTO.setTrackRouteId(toLittleEndian: String?) {
    this.trackRouteId = toLittleEndian
}

private fun JobItemMeasureDTO.setJobId(toLittleEndian: String?) {
    this.jobId = toLittleEndian
}

private fun JobItemEstimatesPhotoDTO.setPhotoId(toLittleEndian: String?) {
    this.photoId = toLittleEndian!!
}

private fun JobEstimateWorksPhotoDTO.setWorksId(toLittleEndian: String?) {
    this.worksId = toLittleEndian!!
}

private fun JobItemMeasureDTO.setItemMeasureId(toLittleEndian: String?) {
    this.itemMeasureId = toLittleEndian
}

private fun JobEstimateWorksPhotoDTO.setPhotoId(toLittleEndian: String?) {
    this.photoId = toLittleEndian!!
}

private fun JobItemMeasurePhotoDTO.setPhotoId(toLittleEndian: String?) {
    this.photoId = toLittleEndian!!
}

private fun JobItemMeasurePhotoDTO.setItemMeasureId(toLittleEndian: String?) {
    this.itemMeasureId =  toLittleEndian
}

private fun JobSectionDTO.setJobId(toLittleEndian: String?) {
    this.jobId = toLittleEndian
}

private fun JobSectionDTO.setProjectSectionId(toLittleEndian: String?) {
    this.projectSectionId = toLittleEndian
}

private fun JobSectionDTO.setJobSectionId(toLittleEndian: String?) {
    this.jobSectionId = toLittleEndian!!
}

private fun JobDTOTemp.setVoId(toLittleEndian: String?) {
    this.VoId = toLittleEndian
}

private fun JobDTOTemp.setContractVoId(toLittleEndian: String?) {
    this.ContractVoId = toLittleEndian
}

private fun JobDTOTemp.setTrackRouteId(toLittleEndian: String?) {
    this.TrackRouteId =  toLittleEndian
}

private fun JobDTOTemp.setProjectVoId(toLittleEndian: String?) {
    this.ProjectVoId = toLittleEndian
}

private fun JobDTOTemp.setPerfitemGroupId(toLittleEndian: String?) {
    this.PerfitemGroupId = toLittleEndian
}

private fun JobDTOTemp.setProjectId(toLittleEndian: String?) {
    this.ProjectId = toLittleEndian
}

private fun JobDTOTemp.setJobId(toLittleEndian: String?) {
    this.JobId = toLittleEndian!!
}
