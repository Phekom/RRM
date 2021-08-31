/*
 * Updated by Shaun McDonald on 2021/01/25
 * Last modified on 2021/01/25 6:30 PM
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 */

package za.co.xisystems.itis_rrm.data.repositories

// import sun.security.krb5.Confounder.bytes

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.custom.errors.LocalDataException
import za.co.xisystems.itis_rrm.custom.errors.NoDataException
import za.co.xisystems.itis_rrm.custom.errors.ServiceException
import za.co.xisystems.itis_rrm.custom.errors.XIErrorHandler
import za.co.xisystems.itis_rrm.custom.events.XIEvent
import za.co.xisystems.itis_rrm.custom.results.XIResult
import za.co.xisystems.itis_rrm.custom.results.XIResult.*
import za.co.xisystems.itis_rrm.data.localDB.AppDatabase
import za.co.xisystems.itis_rrm.data.localDB.entities.*
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
    private val appDb: AppDatabase,
    private val photoUtil: PhotoUtil
) : SafeApiRequest() {

    companion object {
        val TAG: String = WorkDataRepository::class.java.simpleName
    }

    val workStatus: MutableLiveData<XIEvent<XIResult<String>>> = MutableLiveData()

    private suspend fun postWorkStatus(result: XIResult<String>) = withContext(Dispatchers.Main) {
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
        postWorkStatus(Progress(true))
        val currentWorksPhotos =
            estimateWorksItem.jobEstimateWorksPhotos.filter { photo ->
                photo.photoActivityId == estimateWorksItem.actId
            } as ArrayList<JobEstimateWorksPhotoDTO>

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
                    currentWorksPhotos,
                    estimateWorksItem,
                    activity,
                    estimateJob.userId
                )
            } else {
                throw ServiceException(messages)
            }
        } catch (t: Throwable) {
            val message = "Failed to upload job ${estimateJob.jiNo}: ${t.message ?: XIErrorHandler.UNKNOWN_ERROR}"
            Timber.e(t, message)
            val uploadException = Error(t, message)
            postWorkStatus(uploadException)
        }
    }

    private suspend fun postEstimateWorks(
        photos: ArrayList<JobEstimateWorksPhotoDTO>?,
        jobEstimateWorks: JobEstimateWorksDTO,
        activity: FragmentActivity,
        useR: Int
    ) {
        withContext(Dispatchers.IO) {
            try {
                uploadWorksImages(jobEstimateWorks, photos, activity)
                moveJobToNextWorkflowStep(jobEstimateWorks, useR)
            } catch (ex: Exception) {
                val message = "Failed to upload works estimate: ${ex.message ?: XIErrorHandler.UNKNOWN_ERROR}"
                Timber.e(ex, message)
                postWorkStatus(Error(ex, message))
            }
        }
    }

    private suspend fun uploadWorksImages(
        workEstimate: JobEstimateWorksDTO,
        worksPhotos: ArrayList<JobEstimateWorksPhotoDTO>?,
        activity: FragmentActivity
    ) = withContext(Dispatchers.IO) {
        var imageCounter = 1

        try {
            if (worksPhotos.isNullOrEmpty()) {
                throw java.lang.NullPointerException("WorkEstimate ${workEstimate.estimateId} has no photos.")
            } else {
                val totalImages = worksPhotos.size
                worksPhotos.forEach { jobItemPhoto ->
                    when {
                        photoUtil.photoExist(jobItemPhoto.filename) -> {
                            Timber.d("x -> UploadRrImage $imageCounter")
                            uploadRrmImage(
                                jobItemPhoto.filename,
                                PhotoQuality.HIGH,
                                imageCounter,
                                totalImages,
                                activity
                            )
                            imageCounter++
                        }
                        else -> {
                            throw NoDataException("Photo ${jobItemPhoto.filename} could not be loaded.")
                        }
                    }
                }
            }
        } catch (throwable: Throwable) {
            val errMessage = "Failed to stage image: ${throwable.message ?: XIErrorHandler.UNKNOWN_ERROR}"
            Timber.e(throwable, errMessage)
            postWorkStatus(Error(throwable, errMessage))
        }
    }

    suspend fun uploadRrmImage(
        filename: String,
        photoQuality: PhotoQuality,
        imageCounter: Int,
        totalImages: Int,
        activity: FragmentActivity
    ) = Coroutines.io {
        val data: ByteArray = getData(filename, photoQuality)
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
    ) = Coroutines.io {
        try {
            val imagedata = JsonObject()
            imagedata.addProperty("Filename", filename)
            imagedata.addProperty("ImageByteArray", photoUtil.encode64Pic(photo))
            imagedata.addProperty("ImageFileExtension", extension)
            Timber.d("ImageData: $imagedata")

            val uploadImageResponse = apiRequest { api.uploadRrmImage(imagedata) }
            val apiMessage = uploadImageResponse.errorMessage ?: ""

            if (apiMessage.trim().isNotBlank()) {
                throw ServiceException(apiMessage)
            }

            if (totalImages <= imageCounter) {
                Timber.d("Total Images: $totalImages")
            }
        } catch (throwable: Throwable) {
            val errMessage = "Failed to upload image: ${throwable.message ?: XIErrorHandler.UNKNOWN_ERROR}"
            Timber.e(throwable, errMessage)
            postWorkStatus(Error(throwable, errMessage))
        }
    }

    private suspend fun getData(
        filename: String,
        photoQuality: PhotoQuality
    ): ByteArray = withContext(Dispatchers.IO) {
        val uri = photoUtil.getPhotoPathFromExternalDirectory(filename)
        val bitmap =
            photoUtil.getPhotoBitmapFromFile(uri, photoQuality)
        return@withContext photoUtil.getCompressedPhotoWithExifInfo(
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

                postWorkStatus(Success(jobEstimateWorks.worksId))
            } catch (t: Throwable) {
                val message = "Failed to update workflow: ${t.message ?: XIErrorHandler.UNKNOWN_ERROR}"
                Timber.e(t, message)
                val workflowFail = Error(t, message)
                postWorkStatus(workflowFail)
            }
        }
    }

    suspend fun getWorkFlowCodes(eId: Int): LiveData<List<WfWorkStepDTO>> {
        return withContext(Dispatchers.IO) {
            appDb.getWorkStepDao().getWorkflowSteps(eId)
        }
    }

    suspend fun createEstimateWorksPhoto(
        estimateWorksPhotos: ArrayList<JobEstimateWorksPhotoDTO>,
        estimateWorksItem: JobEstimateWorksDTO
    ) {
        Coroutines.io {
            estimateWorksPhotos.forEach { estimateWorksPhoto ->
                if (!appDb.getEstimateWorkPhotoDao()
                        .checkIfEstimateWorksPhotoExist(estimateWorksPhoto.filename)
                ) {
                    appDb.getEstimateWorkPhotoDao().insertEstimateWorksPhoto(estimateWorksPhoto)
                } else {
                    Timber.d("${estimateWorksPhoto.filename} was already in the database")
                }
            }
            val allEstimateWorksPhotos =
                appDb.getEstimateWorkPhotoDao()
                    .getEstimateWorksPhotoForWorksId(estimateWorksItem.worksId) as ArrayList<JobEstimateWorksPhotoDTO>
            appDb.getEstimateWorkDao().updateJobEstimateWorkForEstimateID(
                allEstimateWorksPhotos,
                estimateWorksItem.estimateId
            )
        }
    }

    suspend fun getJobItemsEstimatesDoneForJobId(
        jobId: String?,
        estimateWorkPartComplete: Int,
        estWorksComplete: Int
    ): Int = withContext(Dispatchers.IO) {
        return@withContext appDb.getJobItemEstimateDao()
            .getJobItemsEstimatesDoneForJobId(jobId, estimateWorkPartComplete, estWorksComplete)
    }

    suspend fun getLiveJobEstimateWorksByEstimateId(estimateId: String?): LiveData<JobEstimateWorksDTO> {
        return withContext(Dispatchers.IO) {
            appDb.getEstimateWorkDao().getLiveJobEstimateWorksForEstimateId(estimateId)
        }
    }

    suspend fun getJobItemEstimateForEstimateId(estimateId: String): JobItemEstimateDTO {
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
            val saveFail = Error(ex, message)
            postWorkStatus(saveFail)
        }
    }

    private suspend fun updateWorkflowJobValuesAndInsertWhenNeeded(job: WorkflowJobDTO, inWorkflow: Boolean = false) {
        try {
            appDb.getJobDao().updateJob(job.trackRouteId, job.actId, job.jiNo, job.jobId)

            job.workflowItemEstimates.forEach { jobItemEstimate ->
                appDb.getJobItemEstimateDao().updateExistingJobItemEstimateWorkflow(
                    jobItemEstimate.trackRouteId,
                    jobItemEstimate.actId,
                    jobItemEstimate.estimateId
                )

                jobItemEstimate.workflowEstimateWorks.forEach { jobEstimateWorks ->
                    appDb.getEstimateWorkDao().updateJobEstimateWorksWorkflow(
                        jobEstimateWorks.worksId,
                        jobItemEstimate.estimateId,
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
            if (!inWorkflow) {
                postWorkStatus(Success(job.jiNo!!))
            }
        } catch (t: Throwable) {
            val message = "Unable to update workflow job: ${t.message ?: XIErrorHandler.UNKNOWN_ERROR}"
            Timber.e(t, message)
            postWorkStatus(Error(LocalDataException(message), message))
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
            val message = "Failed to set BigEndian Guids: ${t.message ?: XIErrorHandler.UNKNOWN_ERROR}"
            Coroutines.main { postWorkStatus(Error(t, message)) }
            return null
        }
    }

    suspend fun getSectionForProjectSectionId(sectionId: String?): String {
        return withContext(Dispatchers.IO) {
            appDb.getProjectSectionDao().getSectionForProjectSectionId(sectionId!!)
        }
    }

    suspend fun getUOMForProjectItemId(projectItemId: String): String? = withContext(Dispatchers.IO) {
        return@withContext appDb.getProjectItemDao().getUOMForProjectItemId(projectItemId)
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
            val uploadFail = Error(e, message)
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

    suspend fun getProjectItemById(projectItemId: String): ItemDTOTemp = withContext(Dispatchers.IO) {
        return@withContext appDb.getItemDaoTemp().getProjectItemById(projectItemId)
    }
}
