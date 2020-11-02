package za.co.xisystems.itis_rrm.data.localDB

import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobEstimateWorksDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobEstimateWorksPhotoDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimateDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimatesPhotoDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemMeasureDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemMeasurePhotoDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobSectionDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.WorkflowJobDTO
import za.co.xisystems.itis_rrm.data.repositories.OfflineDataRepository
import za.co.xisystems.itis_rrm.utils.DataConversion

/**
 * Created by Francis Mahlava on 2020/01/02.
 */
object JobDataController {

    fun setWorkflowJobBigEndianGuids(job: WorkflowJobDTO): WorkflowJobDTO {

        job.jobId = DataConversion.toBigEndian(job.jobId)
        job.trackRouteId = DataConversion.toBigEndian(job.trackRouteId)

        if (job.workflowItemEstimates != null) {
            for (jie in job.workflowItemEstimates) {

                jie.estimateId = DataConversion.toBigEndian(jie.estimateId)!!
                jie.trackRouteId = DataConversion.toBigEndian(jie.trackRouteId)!!
                //  Lets go through the WorkFlowEstimateWorks
                for (wfe in jie.workflowEstimateWorks) {
                    wfe.trackRouteId = DataConversion.toBigEndian(wfe.trackRouteId)!!
                    wfe.worksId = DataConversion.toBigEndian(wfe.worksId)!!

                    wfe.estimateId = DataConversion.toBigEndian(wfe.estimateId)!!
                }
            }
        }
        if (job.workflowItemMeasures != null) {
            for (jim in job.workflowItemMeasures) {

                jim.itemMeasureId = DataConversion.toBigEndian(jim.itemMeasureId)!!
                jim.measureGroupId = DataConversion.toBigEndian(jim.measureGroupId)!!
                jim.trackRouteId = DataConversion.toBigEndian(jim.trackRouteId)!!
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

    fun setJobLittleEndianGuids(job: JobDTO): JobDTO? {

        job.setJobId(DataConversion.toLittleEndian(job.JobId))
        job.setProjectId(DataConversion.toLittleEndian(job.ProjectId))
        job.setPerfitemGroupId(DataConversion.toLittleEndian(job.PerfitemGroupId))
        job.setProjectVoId(DataConversion.toLittleEndian(job.ProjectVoId))

        if (job.TrackRouteId != null)
            job.setTrackRouteId(DataConversion.toLittleEndian(job.TrackRouteId))
        else job.TrackRouteId = null
        job.setContractVoId(DataConversion.toLittleEndian(job.ContractVoId))
        job.setVoId(DataConversion.toLittleEndian(job.VoId))

        job.JobSections?.forEach { jobSection ->
            jobSection.setJobSectionId(DataConversion.toLittleEndian(jobSection.jobSectionId))
            jobSection.setProjectSectionId(DataConversion.toLittleEndian(jobSection.projectSectionId))
            jobSection.setJobId(DataConversion.toLittleEndian(jobSection.jobId))
        }

        job.JobItemEstimates?.forEach { jie ->
            jie.setEstimateId(DataConversion.toLittleEndian(jie.estimateId))
            jie.setJobId(DataConversion.toLittleEndian(jie.jobId))
            jie.setProjectItemId(DataConversion.toLittleEndian(jie.projectItemId))

            if (jie.trackRouteId != null)
                jie.setTrackRouteId(DataConversion.toLittleEndian(jie.trackRouteId))
            else jie.trackRouteId = null

            jie.setProjectVoId(DataConversion.toLittleEndian(jie.projectVoId))

            jie.jobItemEstimatePhotos?.forEach { jiep ->
                jiep.setPhotoId(DataConversion.toLittleEndian(jiep.photoId))
                jiep.setEstimateId(DataConversion.toLittleEndian(jiep.estimateId))
            }

            jie.jobEstimateWorks?.forEach { jew ->

                jew.setWorksId(DataConversion.toLittleEndian(jew.worksId))
                jew.setEstimateId(DataConversion.toLittleEndian(jew.estimateId))
                jew.setTrackRouteId(DataConversion.toLittleEndian(jew.trackRouteId))
                jew.jobEstimateWorksPhotos?.forEach { ewp ->
                    ewp.setWorksId(DataConversion.toLittleEndian(ewp.worksId))
                    ewp.setPhotoId(DataConversion.toLittleEndian(ewp.photoId))
                }
            }
        }

        job.JobItemMeasures?.forEach { jim ->

            jim.setItemMeasureId(DataConversion.toLittleEndian(jim.itemMeasureId))
            jim.setJobId(DataConversion.toLittleEndian(jim.jobId))
            jim.setProjectItemId(DataConversion.toLittleEndian(jim.projectItemId))
            jim.setMeasureGroupId(DataConversion.toLittleEndian(jim.measureGroupId))
            jim.setEstimateId(DataConversion.toLittleEndian(jim.estimateId))
            jim.setProjectVoId(DataConversion.toLittleEndian(jim.projectVoId))
            jim.setTrackRouteId(DataConversion.toLittleEndian(jim.trackRouteId))

            jim.jobItemMeasurePhotos.forEach { jimp ->

                jimp.setPhotoId(DataConversion.toLittleEndian(jimp.photoId))
                jimp.setItemMeasureId(DataConversion.toLittleEndian(jimp.itemMeasureId))
            }
        }

        return job
    }

    fun setJobMeasureLittleEndianGuids(jim: JobItemMeasureDTO?): JobItemMeasureDTO {
        if (jim != null) {
//            for (jim in jobItemMeasure) {
            jim.setEstimateId(DataConversion.toLittleEndian(jim.estimateId))
            jim.setJobId(DataConversion.toLittleEndian(jim.jobId))
            jim.setProjectItemId(DataConversion.toLittleEndian(jim.projectItemId))
            jim.setItemMeasureId(DataConversion.toLittleEndian(jim.itemMeasureId))

            for (jmep in jim.jobItemMeasurePhotos) {
                jmep.setPhotoId(DataConversion.toLittleEndian(jmep.photoId))
                jmep.setEstimateId(DataConversion.toLittleEndian(jmep.estimateId))
                jmep.setItemMeasureId(DataConversion.toLittleEndian(jmep.itemMeasureId))
            }
        }

        return jim!!
    }

    fun setMsg(uplodmsg: String?): String? {
        return uplodmsg
    }

    private val offlineDataRepository: OfflineDataRepository? = null
}

private fun JobItemMeasurePhotoDTO.setEstimateId(toLittleEndian: String?) {
    this.estimateId = toLittleEndian
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
    this.estimateId = toLittleEndian!!
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
    this.itemMeasureId = toLittleEndian
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

private fun JobDTO.setVoId(toLittleEndian: String?) {
    this.VoId = toLittleEndian
}

private fun JobDTO.setContractVoId(toLittleEndian: String?) {
    this.ContractVoId = toLittleEndian
}

private fun JobDTO.setTrackRouteId(toLittleEndian: String?) {
    this.TrackRouteId = toLittleEndian
}

private fun JobDTO.setProjectVoId(toLittleEndian: String?) {
    this.ProjectVoId = toLittleEndian
}

private fun JobDTO.setPerfitemGroupId(toLittleEndian: String?) {
    this.PerfitemGroupId = toLittleEndian
}

private fun JobDTO.setProjectId(toLittleEndian: String?) {
    this.ProjectId = toLittleEndian
}

private fun JobDTO.setJobId(toLittleEndian: String?) {
    this.JobId = toLittleEndian!!
}
