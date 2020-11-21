package za.co.xisystems.itis_rrm.data.repositories

// import sun.security.krb5.Confounder.bytes

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.util.ArrayList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.custom.errors.LocalDataException
import za.co.xisystems.itis_rrm.custom.errors.NoDataException
import za.co.xisystems.itis_rrm.custom.errors.ServiceException
import za.co.xisystems.itis_rrm.custom.errors.XIErrorHandler
import za.co.xisystems.itis_rrm.custom.events.XIEvent
import za.co.xisystems.itis_rrm.custom.results.XIError
import za.co.xisystems.itis_rrm.custom.results.XIProgress
import za.co.xisystems.itis_rrm.custom.results.XIResult
import za.co.xisystems.itis_rrm.custom.results.XISuccess
import za.co.xisystems.itis_rrm.data.localDB.AppDatabase
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobEstimateWorksDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobEstimateWorksPhotoDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimateDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.UserDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.WF_WorkStepDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.WorkflowJobDTO
import za.co.xisystems.itis_rrm.data.network.BaseConnectionApi
import za.co.xisystems.itis_rrm.data.network.SafeApiRequest
import za.co.xisystems.itis_rrm.utils.Coroutines
import za.co.xisystems.itis_rrm.utils.DataConversion
import za.co.xisystems.itis_rrm.utils.PhotoUtil
import za.co.xisystems.itis_rrm.utils.enums.PhotoQuality
import za.co.xisystems.itis_rrm.utils.enums.WorkflowDirection

/**
 * Created by Francis Mahlava on 2019/11/28.
 */

class WorkDataRepository(
    private val api: BaseConnectionApi,
    private val appDb: AppDatabase
) : SafeApiRequest() {
    companion object {
        val TAG: String = WorkDataRepository::class.java.simpleName
    }

    val photoUpload = MutableLiveData<String?>()

    val workStatus: MutableLiveData<XIEvent<XIResult<String>>> = MutableLiveData()

    private fun postWorkStatus(result: XIResult<String>) {
        workStatus.postValue(XIEvent(result))
    }

    suspend fun getUser(): LiveData<UserDTO> {
        return withContext(Dispatchers.IO) {
            appDb.getUserDao().getUser()
        }
    }

    suspend fun getJobsForActivityIds(activityId1: Int, activityId2: Int): LiveData<List<JobDTO>> {
        return withContext(Dispatchers.IO) {
            appDb.getJobDao().getJobsForActivityIds1(activityId1, activityId2)
        }
    }

    suspend fun getJobsForActivityId(activityId1: Int): LiveData<List<JobItemEstimateDTO>> {
        return withContext(Dispatchers.IO) {
            appDb.getJobItemEstimateDao().getJobsForActivityId(activityId1)
        }
    }

    suspend fun getItemDescription(jobId: String): String {
        return withContext(Dispatchers.IO) {
            appDb.getJobDao().getItemDescription(jobId)
        }
    }

    suspend fun getItemJobNo(jobId: String): String {
        return withContext(Dispatchers.IO) {
            appDb.getJobDao().getItemJobNo(jobId)
        }
    }

    suspend fun getItemStartKm(jobId: String): Double {
        return withContext(Dispatchers.IO) {
            appDb.getJobDao().getItemStartKm(jobId)
        }
    }

    suspend fun getItemEndKm(jobId: String): Double {
        return withContext(Dispatchers.IO) {
            appDb.getJobDao().getItemEndKm(jobId)
        }
    }

    suspend fun getItemTrackRouteId(jobId: String): String {
        return withContext(Dispatchers.IO) {
            appDb.getJobDao().getItemTrackRouteId(jobId)
        }
    }

    suspend fun submitWorks(
        estimateWorksItem: JobEstimateWorksDTO,
        activity: FragmentActivity,
        estimateJob: JobDTO
    ) {
        postWorkStatus(XIProgress(true))
        val worksData = JsonObject()
        val gson = Gson()
        val newMeasure = gson.toJson(estimateWorksItem)
        val jsonElement: JsonElement = JsonParser.parseString(newMeasure)
        worksData.add("JobEstimateWorksItem", jsonElement)

        Timber.d("WorkEstimate $worksData")

        try {
            val uploadWorksItemResponse = apiRequest { api.uploadWorksItem(worksData) }

            val messages = uploadWorksItemResponse.errorMessage ?: ""

            if (messages.isBlank()) {
                postEstimateWorks(
                    estimateWorksItem,
                    activity,
                    estimateJob.UserId
                )
            } else {
                val uploadException = ServiceException(messages)
                val uploadFail =
                    XIError(uploadException, "Failed to upload work for Job: ${estimateJob.JiNo}")
                postWorkStatus(uploadFail)
            }

            return withContext(Dispatchers.IO) {
                messages
            }
        } catch (e: Exception) {
            throw e
        }
    }

    private suspend fun postEstimateWorks(
        jobEstimateWorks: JobEstimateWorksDTO,
        activity: FragmentActivity,
        useR: Int
    ) {
        withContext(Dispatchers.IO) {
            uploadWorksImages(jobEstimateWorks, activity)
            moveJobToNextWorkflowStep(jobEstimateWorks, useR)
        }
    }

    private fun uploadWorksImages(
        jobEstimateWorks: JobEstimateWorksDTO,
        activity: FragmentActivity
    ) {
        var imageCounter = 1

        if (jobEstimateWorks.jobEstimateWorksPhotos != null) {
            if (jobEstimateWorks.jobEstimateWorksPhotos!!.isEmpty()) {
                val noPhotosException =
                    NoDataException("WorkEstimate ${jobEstimateWorks.estimateId} has no photos.")
                val uploadFail = XIError(noPhotosException, noPhotosException.message ?: XIErrorHandler.UNKNOWN_ERROR)
                postWorkStatus(uploadFail)
            } else {
                val totalImages = jobEstimateWorks.jobEstimateWorksPhotos!!.size
                for (jobItemPhotos in jobEstimateWorks.jobEstimateWorksPhotos!!) {
                    if (PhotoUtil.photoExist(jobItemPhotos.filename)) {
                        Timber.d("x -> UploadRrImage $imageCounter")
                        uploadRrmImage(
                            jobItemPhotos.filename,
                            PhotoQuality.HIGH,
                            imageCounter,
                            totalImages,
                            activity
                        )
                        imageCounter++
                    } else {
                        val noDataException =
                            NoDataException("Photo ${jobItemPhotos.filename} could not be loaded.")
                        Timber.e(noDataException)
                        val photoError = XIError(noDataException,
                            noDataException.message ?: XIErrorHandler.UNKNOWN_ERROR)
                        postWorkStatus(photoError)
                    }
                }
            }
        } else {
            val emptyPhotosException =
                NoDataException("WorkEstimate ${jobEstimateWorks.estimateId} photos are null.")
            Timber.e(emptyPhotosException)
            postWorkStatus(XIError(emptyPhotosException, emptyPhotosException.message ?: XIErrorHandler.UNKNOWN_ERROR))
        }
    }

    private fun uploadRrmImage(
        filename: String,
        photoQuality: PhotoQuality,
        imageCounter: Int,
        totalImages: Int,
        activity: FragmentActivity
    ) {
        val data: ByteArray = getData(filename, photoQuality, activity)
        processImageUpload(
            filename,
            activity.getString(R.string.jpg),
            data,
            totalImages,
            imageCounter
        )
    }

    private fun processImageUpload(
        filename: String,
        extension: String,
        photo: ByteArray,
        totalImages: Int,
        imageCounter: Int
    ) {
        Coroutines.io {
            val imagedata = JsonObject()
            imagedata.addProperty("Filename", filename)
            imagedata.addProperty("ImageByteArray", PhotoUtil.encode64Pic(photo))
            imagedata.addProperty("ImageFileExtension", extension)
            Timber.d("ImageData: $imagedata")

            val uploadImageResponse = apiRequest { api.uploadRrmImage(imagedata) }
            photoUpload.postValue(uploadImageResponse.errorMessage)
            if (totalImages <= imageCounter) {
                Timber.d("Total Images: $totalImages")
            }
        }
    }

    private fun getData(
        filename: String,
        photoQuality: PhotoQuality,
        activity: FragmentActivity
    ): ByteArray {
        val uri = PhotoUtil.getPhotoPathFromExternalDirectory(filename)
        val bitmap =
            PhotoUtil.getPhotoBitmapFromFile(activity.applicationContext, uri, photoQuality)
        return PhotoUtil.getCompressedPhotoWithExifInfo(
            bitmap!!,
            filename
        )
    }

    private suspend fun moveJobToNextWorkflowStep(
        jobEstimateWorks: JobEstimateWorksDTO,
        userId: Int
    ) {
        withContext(Dispatchers.IO) {
            try {
                if (jobEstimateWorks.trackRouteId.isEmpty()) {
                    val wfEx = ServiceException("Error: trackRouteId is null")
                    throw wfEx
                } else {
                    val direction: Int = WorkflowDirection.NEXT.value
                    val trackRouteId: String = jobEstimateWorks.trackRouteId
                    val description = "work step done"

                    val workflowMoveResponse = apiRequest {
                        api.getWorkflowMove(
                            userId.toString(),
                            trackRouteId,
                            description,
                            direction
                        )
                    }
                    if (workflowMoveResponse.errorMessage != null) {
                        throw ServiceException(workflowMoveResponse.errorMessage)
                    }
                    if (workflowMoveResponse.workflowJob != null) {
                        saveWorkflowJob(workflowMoveResponse.workflowJob!!, true)
                    } else {
                        throw ServiceException("Workflow Job is null.")
                    }
                }

                postWorkStatus(XIProgress(false))
                postWorkStatus(XISuccess(jobEstimateWorks.worksId))
            } catch (t: Throwable) {
                val message = "Failed to update workflow: ${t.message ?: XIErrorHandler.UNKNOWN_ERROR}"
                Timber.e(t, message)
                val workflowFail = XIError(t, message)
                postWorkStatus(workflowFail)
            }
        }
    }

    suspend fun getWorkFlowCodes(eId: Int): LiveData<List<WF_WorkStepDTO>> {
        return withContext(Dispatchers.IO) {
            appDb.getWorkStepDao().getWorkflowSteps(eId)
        }
    }

    suspend fun createEstimateWorksPhoto(
        estimateWorksPhotos: ArrayList<JobEstimateWorksPhotoDTO>,
        estimateWorksItem: JobEstimateWorksDTO
    ) {
        Coroutines.io {
            if (estimateWorksPhotos.isNotEmpty()) {
                for (estimateWorksPhoto in estimateWorksPhotos) {
                    if (!appDb.getEstimateWorkPhotoDao()
                            .checkIfEstimateWorksPhotoExist(estimateWorksPhoto.filename)
                    ) {
                        appDb.getEstimateWorkPhotoDao().insertEstimateWorksPhoto(estimateWorksPhoto)
                    } else {
                        Timber.d("${estimateWorksPhoto.filename} was already in the database")
                    }
                }
                appDb.getEstimateWorkDao().updateJobEstimateWorkForEstimateID(
                    estimateWorksItem.jobEstimateWorksPhotos!!,
                    estimateWorksItem.estimateId
                )
            }
        }
    }

    suspend fun getJobItemsEstimatesDoneForJobId(
        jobId: String?,
        estimateWorkPartComplete: Int,
        estWorksComplete: Int
    ): Int {
        return withContext(Dispatchers.IO) {
            appDb.getJobItemEstimateDao()
                .getJobItemsEstimatesDoneForJobId(jobId, estimateWorkPartComplete, estWorksComplete)
        }
    }

    suspend fun getJobEstiItemForEstimateId(estimateId: String?): LiveData<List<JobEstimateWorksDTO>> {
        return withContext(Dispatchers.IO) {
            appDb.getEstimateWorkDao().getJobMeasureItemsForJobId(estimateId)
        }
    }

    suspend fun getJobItemEstimateForEstimateId(estimateId: String): LiveData<JobItemEstimateDTO> {
        return withContext(Dispatchers.IO) {
            appDb.getJobItemEstimateDao().getJobItemEstimateForEstimateId(estimateId)
        }
    }

    private suspend fun saveWorkflowJob(workflowj: WorkflowJobDTO, inWorkflow: Boolean = false) {
        try {
            val job = setWorkflowJobBigEndianGuids(workflowj)
            if (job != null) {
                updateWorkflowJobValuesAndInsertWhenNeeded(job, inWorkflow)
            } else {
                throw NullPointerException("Workflow job is undefined!")
            }
        } catch (ex: Exception) {
            val message = "Failed to save updated job: ${ex.message ?: XIErrorHandler.UNKNOWN_ERROR}"
            Timber.e(message)
            val saveFail = XIError(ex, message)
            postWorkStatus(saveFail)
        }
    }

    private suspend fun updateWorkflowJobValuesAndInsertWhenNeeded(job: WorkflowJobDTO, inWorkflow: Boolean = false) {
        try {
            appDb.getJobDao().updateJob(job.trackRouteId, job.actId, job.jiNo, job.jobId)

            job.workflowItemEstimates?.forEach { jobItemEstimate ->
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
            if (!inWorkflow) {
                postWorkStatus(XISuccess(job.jiNo!!))
            }
        } catch (t: Throwable) {
            val message = "Unable to update workflow job: ${t.message ?: XIErrorHandler.UNKNOWN_ERROR}"
            Timber.e(t, message)
            postWorkStatus(XIError(LocalDataException(message), message))
        }
    }

    private fun setWorkflowJobBigEndianGuids(job: WorkflowJobDTO): WorkflowJobDTO? {

        try {
            job.jobId = DataConversion.toBigEndian(job.jobId)
            job.trackRouteId = DataConversion.toBigEndian(job.trackRouteId)

            job.workflowItemEstimates?.forEach { jie ->
                jie.estimateId = DataConversion.toBigEndian(jie.estimateId)!!
                jie.trackRouteId = DataConversion.toBigEndian(jie.trackRouteId)!!
                //  Lets go through the WorkFlowEstimateWorks
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
            val message = "Failed to set BigEndian Guids: ${t.message ?: XIErrorHandler.UNKNOWN_ERROR}"
            postWorkStatus(XIError(t, message))
            return null
        }
    }

    suspend fun getSectionForProjectSectionId(sectionId: String?): String {
        return withContext(Dispatchers.IO) {
            appDb.getProjectSectionDao().getSectionForProjectSectionId(sectionId!!)
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

    suspend fun getJobEstimationItemsForJobId(
        jobID: String?,
        actID: Int
    ): LiveData<List<JobItemEstimateDTO>> {
        return withContext(Dispatchers.IO) {
            appDb.getJobItemEstimateDao().getJobEstimationItemsForJobId(jobID!!, actID)
        }
    }

    suspend fun getProjectItemDescription(projectItemId: String): String {
        return withContext(Dispatchers.IO) {
            appDb.getProjectItemDao().getProjectItemDescription(projectItemId)
        }
    }

    suspend fun processWorkflowMove(
        userId: String,
        trackRouteId: String,
        description: String?,
        direction: Int
    ) {
        try {
            val workflowMoveResponse =
                apiRequest { api.getWorkflowMove(userId, trackRouteId, description, direction) }
            val messages = workflowMoveResponse.errorMessage ?: ""
            if (messages.isBlank() && workflowMoveResponse.workflowJob != null) {
                saveWorkflowJob(workflowMoveResponse.workflowJob!!)
            } else {
                throw ServiceException(messages)
            }
        } catch (e: Exception) {
            val prefix = "Failed to process workflow move"
            val message = "$prefix: ${e.message ?: XIErrorHandler.UNKNOWN_ERROR}"
            Timber.e(e, message)
            val uploadFail = XIError(e, message)
            postWorkStatus(uploadFail)
        }
    }

    suspend fun getWorkItemsForActID(actId: Int): LiveData<List<JobEstimateWorksDTO>> {
        return withContext(Dispatchers.IO) {
            appDb.getEstimateWorkDao().getWorkItemsForActID(actId)
        }
    }

    suspend fun getWorkItemsForEstimateIDAndActID(estimateId: String, actId: Int) =
        appDb.getEstimateWorkDao().getWorkItemsForEstimateIDAndActID(estimateId, actId)

    suspend fun getEstimateWorksPhotosForWorksId(worksId: String): List<JobEstimateWorksPhotoDTO> =
        appDb.getEstimateWorkPhotoDao().getEstimateWorksPhotoForWorksId(worksId)
}
