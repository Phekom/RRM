/*
 * Updated by Shaun McDonald on 2021/01/25
 * Last modified on 2021/01/25 6:30 PM
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 */

package za.co.xisystems.itis_rrm.data.repositories

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.Transaction
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.custom.errors.NoDataException
import za.co.xisystems.itis_rrm.custom.errors.ServiceException
import za.co.xisystems.itis_rrm.custom.errors.TransmissionException
import za.co.xisystems.itis_rrm.custom.errors.XIErrorHandler
import za.co.xisystems.itis_rrm.custom.events.XIEvent
import za.co.xisystems.itis_rrm.custom.results.XIResult
import za.co.xisystems.itis_rrm.data.localDB.AppDatabase
import za.co.xisystems.itis_rrm.data.localDB.dao.JobDao
import za.co.xisystems.itis_rrm.data.localDB.entities.ItemDTOTemp
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobEstimateWorksDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobEstimateWorksPhotoDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimateDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimatesPhotoDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.UserDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.WfWorkStepDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.WorkflowJobDTO
import za.co.xisystems.itis_rrm.data.network.BaseConnectionApi
import za.co.xisystems.itis_rrm.data.network.SafeApiRequest
import za.co.xisystems.itis_rrm.forge.DefaultDispatcherProvider
import za.co.xisystems.itis_rrm.forge.DispatcherProvider
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
    private val photoUtil: PhotoUtil,
    private val dispatchers: DispatcherProvider = DefaultDispatcherProvider()
) : SafeApiRequest() {

    val searchResults = MutableLiveData<List<JobDTO>>()
    private var jobDao: JobDao? = appDb.getJobDao()
    private val coroutineScope = CoroutineScope(dispatchers.main())
    var workStatus: MutableLiveData<XIEvent<XIResult<String>>> = MutableLiveData()
    val allWork: LiveData<List<JobDTO>>?

    init {
        allWork = jobDao?.getAllWork()
    }

    companion object {
        val TAG: String = WorkDataRepository::class.java.simpleName
    }

    fun jobSearch(criteria: String?) {
        coroutineScope.launch(dispatchers.main()) {
            searchResults.value = jobSearchAsync(criteria!!).await()
        }
    }

    private fun jobSearchAsync(criteria: String): Deferred<List<JobDTO>> =
        coroutineScope.async(dispatchers.io()) {
            return@async jobDao?.searchJobs(criteria.toRoomSearchString()) ?: listOf()
        }

    private fun postWorkStatus(result: XIResult<String>) = coroutineScope.launch(dispatchers.main()) {
        workStatus.postValue(XIEvent(result))
    }

    suspend fun getUser(): LiveData<UserDTO> {
        return withContext(dispatchers.io()) {
            appDb.getUserDao().getUser()
        }
    }

    suspend fun getJobsForActivityIds(activityId1: Int, activityId2: Int): LiveData<List<JobDTO>> {
        return withContext(dispatchers.io()) {
            appDb.getJobDao().getJobsForActivityIds1(activityId1, activityId2)
        }
    }

    suspend fun getJobsForActivityId(activityId1: Int): LiveData<List<JobItemEstimateDTO>> {
        return withContext(dispatchers.io()) {
            appDb.getJobItemEstimateDao().getJobsForActivityId(activityId1)
        }
    }

    suspend fun getItemDescription(jobId: String): String {
        return withContext(dispatchers.io()) {
            appDb.getJobDao().getItemDescription(jobId)
        }
    }

    suspend fun getItemJobNo(jobId: String): String {
        return withContext(dispatchers.io()) {
            appDb.getJobDao().getItemJobNo(jobId)
        }
    }

    suspend fun getItemStartKm(jobId: String): Double {
        return withContext(dispatchers.io()) {
            appDb.getJobDao().getItemStartKm(jobId)
        }
    }

    suspend fun getItemEndKm(jobId: String): Double {
        return withContext(dispatchers.io()) {
            appDb.getJobDao().getItemEndKm(jobId)
        }
    }

    suspend fun getItemTrackRouteId(jobId: String): String {
        return withContext(dispatchers.io()) {
            appDb.getJobDao().getItemTrackRouteId(jobId)
        }
    }

    suspend fun submitWorks(
        estimateWorksItem: JobEstimateWorksDTO,
        activity: FragmentActivity,
        estimateJob: JobDTO
    ) {
        postWorkStatus(XIResult.Progress(true))

        // post images for the current work stage only
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
            val uploadException = XIResult.Error(t, message)
            postWorkStatus(uploadException)
        }
    }

    private suspend fun postEstimateWorks(
        photos: ArrayList<JobEstimateWorksPhotoDTO>?,
        jobEstimateWorks: JobEstimateWorksDTO,
        activity: FragmentActivity,
        useR: Int
    ) = withContext(dispatchers.io()) {
        try {
            uploadWorksImages(jobEstimateWorks, photos, activity)
            moveJobToNextWorkflowStep(jobEstimateWorks, useR)
        } catch (ex: Exception) {
            val message = "Failed to upload works estimate: ${ex.message ?: XIErrorHandler.UNKNOWN_ERROR}"
            Timber.e(ex, message)
            postWorkStatus(XIResult.Error(ex, message))
        }
    }

    private suspend fun uploadWorksImages(
        workEstimate: JobEstimateWorksDTO,
        worksPhotos: ArrayList<JobEstimateWorksPhotoDTO>?,
        activity: FragmentActivity
    ) = withContext(dispatchers.io()) {
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
            postWorkStatus(XIResult.Error(throwable, errMessage))
        }
    }

    private suspend fun uploadRrmImage(
        filename: String,
        photoQuality: PhotoQuality,
        imageCounter: Int,
        totalImages: Int,
        activity: FragmentActivity
    ) = withContext(dispatchers.io()) {
        val data: ByteArray = getData(filename, photoQuality)
        processImageUpload(
            filename,
            activity.getString(R.string.jpg),
            data,
            totalImages,
            imageCounter
        )
    }

    private suspend fun processImageUpload(
        filename: String,
        extension: String,
        photo: ByteArray,
        totalImages: Int,
        imageCounter: Int
    ) = withContext(dispatchers.io()) {
        try {
            val imagedata = JsonObject()
            imagedata.addProperty("Filename", filename)
            imagedata.addProperty("ImageByteArray", photoUtil.encode64Pic(photo))
            imagedata.addProperty("ImageFileExtension", extension)
            // Timber.d("ImageData: $imagedata")

            val uploadImageResponse = apiRequest { api.uploadRrmImage(imagedata) }
            val apiMessage = uploadImageResponse.errorMessage ?: ""

            if (apiMessage.trim().isNotBlank()) {
                throw ServiceException(apiMessage)
            }

            if (totalImages <= imageCounter) {
                Timber.d("Total Images: $totalImages")
            } else {
                Timber.d("Upload Complete - uploaded: $imageCounter / $totalImages")
            }
        } catch (exception: Exception) {
            val errMessage = "Failed to upload image: ${exception.message ?: XIErrorHandler.UNKNOWN_ERROR}"
            Timber.e(exception, errMessage)
            // postWorkStatus(XIResult.Error(throwable, errMessage))
            throw TransmissionException(message = errMessage, cause = exception)
        }
    }

    private suspend fun getData(
        filename: String,
        photoQuality: PhotoQuality
    ): ByteArray = withContext(dispatchers.io()) {
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
    ) = withContext(dispatchers.io()) {
        try {
            if (jobEstimateWorks.trackRouteId.isEmpty()) {
                throw ServiceException("Error: trackRouteId is null")
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

            postWorkStatus(XIResult.Success(jobEstimateWorks.worksId))
        } catch (exception: Exception) {
            val message = "Failed to update workflow: ${exception.message ?: XIErrorHandler.UNKNOWN_ERROR}"
            Timber.e(exception, message)
            // val workflowFail = XIResult.Error(t, message)
            throw TransmissionException(message, exception)
        }
    }

    suspend fun getWorkFlowCodes(eId: Int): LiveData<List<WfWorkStepDTO>> {
        return withContext(dispatchers.io()) {
            appDb.getWorkStepDao().getWorkflowSteps(eId)
        }
    }

    suspend fun createEstimateWorksPhoto(
        estimateWorksPhotos: ArrayList<JobEstimateWorksPhotoDTO>,
        estimateWorksItem: JobEstimateWorksDTO
    ) = withContext(dispatchers.io()) {
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

    suspend fun getJobItemsEstimatesDoneForJobId(
        jobId: String?,
        estimateWorkPartComplete: Int,
        estWorksComplete: Int
    ): Int = withContext(dispatchers.io()) {
        return@withContext appDb.getJobItemEstimateDao()
            .getJobItemsEstimatesDoneForJobId(jobId, estimateWorkPartComplete, estWorksComplete)
    }

    suspend fun getLiveJobEstimateWorksByEstimateId(estimateId: String?): LiveData<JobEstimateWorksDTO> = withContext(dispatchers.io()) {
        return@withContext appDb.getEstimateWorkDao().getLiveJobEstimateWorksForEstimateId(estimateId)
    }

    suspend fun getJobItemEstimateForEstimateId(estimateId: String):
        JobItemEstimateDTO = withContext(dispatchers.io()) {
        return@withContext appDb.getJobItemEstimateDao().getJobItemEstimateForEstimateId(estimateId)
    }

    private fun saveWorkflowJob(workflowj: WorkflowJobDTO, inWorkflow: Boolean = false) = coroutineScope.launch(dispatchers.io()) {
        try {
            val job = setWorkflowJobBigEndianGuids(workflowj)
            if (job != null) {
                updateWorkflowJobValuesAndInsertWhenNeeded(job, inWorkflow)
            } else {
                throw NullPointerException("Workflow job is undefined!")
            }
        } catch (ex: Exception) {
            val message = "Failed to save updated job: ${ex.message ?: XIErrorHandler.UNKNOWN_ERROR}"
            Timber.e(ex, message)
            throw TransmissionException(message, ex)
        }
    }

    private fun updateWorkflowJobValuesAndInsertWhenNeeded(job: WorkflowJobDTO, inWorkflow: Boolean = false) {
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
                postWorkStatus(XIResult.Success("Job ${job.jiNo!!} completed"))
            }
        } catch (t: Throwable) {
            val message = "Unable to update workflow job: ${t.message ?: XIErrorHandler.UNKNOWN_ERROR}"
            Timber.e(t, message)
            throw TransmissionException(message, t)
        }
    }

    private fun setWorkflowJobBigEndianGuids(job: WorkflowJobDTO): WorkflowJobDTO {

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
            throw TransmissionException(message, t)
        }
    }

    suspend fun getSectionForProjectSectionId(sectionId: String?): String {
        return withContext(dispatchers.io()) {
            appDb.getProjectSectionDao().getSectionForProjectSectionId(sectionId!!)
        }
    }

    suspend fun getUOMForProjectItemId(projectItemId: String): String? = withContext(dispatchers.io()) {
        return@withContext appDb.getProjectItemDao().getUOMForProjectItemId(projectItemId)
    }

    suspend fun getRouteForProjectSectionId(sectionId: String?): String {
        return withContext(dispatchers.io()) {
            appDb.getProjectSectionDao().getRouteForProjectSectionId(sectionId)
        }
    }

    suspend fun getProjectSectionIdForJobId(jobId: String?): String {
        return withContext(dispatchers.io()) {
            appDb.getJobSectionDao().getProjectSectionId(jobId!!)
        }
    }

    suspend fun getJobEstimationItemsForJobId(
        jobID: String?,
        actID: Int
    ): List<JobItemEstimateDTO> {
        return withContext(dispatchers.io()) {
            appDb.getJobItemEstimateDao().getJobEstimationItemsForJobId(jobID!!, actID)
        }
    }

    suspend fun getProjectItemDescription(projectItemId: String): String {
        return withContext(dispatchers.io()) {
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
            throw TransmissionException(message, e)
        }
    }

    suspend fun getWorkItemsForActID(actId: Int): LiveData<List<JobEstimateWorksDTO>> {
        return withContext(dispatchers.io()) {
            appDb.getEstimateWorkDao().getWorkItemsForActID(actId)
        }
    }

    fun getWorkItemsForEstimateIDAndActID(estimateId: String, actId: Int) =
        appDb.getEstimateWorkDao().getWorkItemsForEstimateIDAndActID(estimateId, actId)

    fun getEstimateWorksPhotosForWorksId(worksId: String): List<JobEstimateWorksPhotoDTO> =
        appDb.getEstimateWorkPhotoDao().getEstimateWorksPhotoForWorksId(worksId)

    fun getEstimateWorksPhotoForWorksIdAndActID(worksId: String, actId: Int): List<JobEstimateWorksPhotoDTO> =
        appDb.getEstimateWorkPhotoDao().getEstimateWorksPhotoForWorksIdAndActID(worksId, actId)

    suspend fun getProjectItemById(projectItemId: String): ItemDTOTemp = withContext(dispatchers.io()) {
        return@withContext appDb.getItemDaoTemp().getProjectItemById(projectItemId)
    }

    @Transaction
    suspend fun backupJobInProgress(job: JobDTO) = withContext(dispatchers.io()) {
        appDb.getJobDao().insertOrUpdateJob(job)
        return@withContext appDb.getJobDao().getJobForJobId(job.jobId)
    }

    suspend fun getEstimateStartPhotoForId(estimateId: String): JobItemEstimatesPhotoDTO = withContext(dispatchers.io()) {
        return@withContext appDb.getJobItemEstimatePhotoDao().getEstimateStartPhotoForId(estimateId)
    }

    suspend fun updateWorkStateInfo(
        jobId: String,
        userId: Int,
        activityId: Int,
        remarks: String
    ): Boolean {
        try {
            val requestData = JsonObject()
            requestData.addProperty("UserId", userId)
            requestData.addProperty("JobId", jobId)
            requestData.addProperty("ActivityId", activityId)
            requestData.addProperty("Remarks", remarks)

            Timber.d("Json Job: $requestData")
            val updateResponse = apiRequest {
                api.updateWorkStateInfo(requestData)
            }

            if (!updateResponse.errorMessage.isNullOrBlank()) {
                throw ServiceException(updateResponse.errorMessage)
            }
            return true
        } catch (e: Exception) {
            val message = "Failed to update approval information"
            Timber.e(e, message)
            throw TransmissionException(message, e)
        }
    }

    suspend fun updateWorkTimes(userId: String, jobId: String, isStart: Boolean): Boolean {
        val requestData = JsonObject()
        requestData.addProperty("UserId", userId)
        requestData.addProperty("JobId", jobId)
        try {
            val updateResponse = when (isStart) {
                true -> {
                    apiRequest { api.updateWorkStartInfo(requestData) }
                }
                else -> {
                    apiRequest { api.updateWorkEndInfo(requestData) }
                }
            }
            if (!updateResponse.errorMessage.isNullOrBlank()) {
                throw ServiceException(updateResponse.errorMessage)
            }
            return true
        } catch (ex: Exception) {
            val message = "Failed to update work start / end times."
            Timber.e(ex, message)
            throw TransmissionException(message, ex)
        }
    }

    suspend fun clearErrors() = withContext(dispatchers.main()) {
        workStatus = MutableLiveData()
    }
}

fun String.toRoomSearchString(): String {
    return "%$this%"
}
