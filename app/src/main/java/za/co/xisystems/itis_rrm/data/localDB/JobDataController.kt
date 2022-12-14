/*
 * Updated by Shaun McDonald on 2021/01/25
 * Last modified on 2021/01/25 6:30 PM
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 */

package za.co.xisystems.itis_rrm.data.localDB

import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobEstimateWorksDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobEstimateWorksPhotoDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimateDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimatesPhotoDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemMeasureDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemMeasurePhotoDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobSectionDTO
import za.co.xisystems.itis_rrm.utils.DataConversion

/**
 * Created by Francis Mahlava on 2020/01/02.
 */
object JobDataController {

    fun setJobLittleEndianGuids(job: JobDTO): JobDTO {

        job.setJobId(DataConversion.toLittleEndian(job.jobId))
        job.setProjectId(DataConversion.toLittleEndian(job.projectId))
        job.setPerfitemGroupId(DataConversion.toLittleEndian(job.perfitemGroupId))
        job.setProjectVoId(DataConversion.toLittleEndian(job.projectVoId))
        // job.sectionId = DataConversion.toLittleEndian(job.sectionId)

        if (job.trackRouteId != null) {
            job.setTrackRouteId(DataConversion.toLittleEndian(job.trackRouteId))
        } else {
            job.trackRouteId = null
        }
        job.setContractVoId(DataConversion.toLittleEndian(job.contractVoId))
        job.setContractId(DataConversion.toLittleEndian(job.contractId))

        job.jobSections.forEach { jobSection ->
            if (jobSection.recordVersion.equals(1)){

            }else{
                jobSection.setJobSectionId(DataConversion.toLittleEndian(jobSection.jobSectionId))
                jobSection.setProjectSectionId(DataConversion.toLittleEndian(jobSection.projectSectionId))
                jobSection.setJobId(DataConversion.toLittleEndian(jobSection.jobId))
            }


        }

        job.jobItemEstimates.forEach { jie ->
            jie.setEstimateId(DataConversion.toLittleEndian(jie.estimateId))
            jie.setJobId(DataConversion.toLittleEndian(jie.jobId))
            jie.setProjectItemId(DataConversion.toLittleEndian(jie.projectItemId))

            if (jie.trackRouteId != null) {
                jie.setTrackRouteId(DataConversion.toLittleEndian(jie.trackRouteId))
            } else {
                jie.trackRouteId = null
            }
            jie.setContractVoId(DataConversion.toLittleEndian(jie.contractVoId))
            jie.setProjectVoId(DataConversion.toLittleEndian(jie.projectVoId))

            jie.jobItemEstimatePhotos.forEach { jiep ->
                jiep.setPhotoId(DataConversion.toLittleEndian(jiep.photoId))
                jiep.setEstimateId(DataConversion.toLittleEndian(jiep.estimateId))
            }

            jie.jobEstimateWorks.forEach { jew ->

                jew.setWorksId(DataConversion.toLittleEndian(jew.worksId))
                jew.setEstimateId(DataConversion.toLittleEndian(jew.estimateId))
                jew.setTrackRouteId(DataConversion.toLittleEndian(jew.trackRouteId))
                jew.jobEstimateWorksPhotos.forEach { ewp ->
                    ewp.setWorksId(DataConversion.toLittleEndian(ewp.worksId))
                    ewp.setPhotoId(DataConversion.toLittleEndian(ewp.photoId))
                }
            }
        }

        job.jobItemMeasures.forEach { jim ->

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

    fun setMsg(uplodmsg: String?): String? {
        return uplodmsg
    }
}

private fun JobItemEstimateDTO.setContractVoId(toLittleEndian: String?) {
    this.contractVoId = toLittleEndian
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
    this.itemMeasureId = toLittleEndian!!
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

private fun JobDTO.setContractId(contractId: String?) {
    this.contractId = contractId
}

private fun JobDTO.setContractVoId(contractVoId: String?) {
    this.contractVoId = contractVoId
}

private fun JobDTO.setTrackRouteId(trackRouteId: String?) {
    this.trackRouteId = trackRouteId
}

private fun JobDTO.setProjectVoId(projectVoId: String?) {
    this.projectVoId = projectVoId
}

private fun JobDTO.setPerfitemGroupId(perfitemGroupId: String?) {
    this.perfitemGroupId = perfitemGroupId
}

private fun JobDTO.setProjectId(projectId: String?) {
    this.projectId = projectId
}

private fun JobDTO.setJobId(jobId: String?) {
    this.jobId = jobId!!
}
