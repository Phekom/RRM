/**
 * Updated by Shaun McDonald on 2021/05/19
 * Updated by Francis Mahlava on 2022/03/10
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 **/

package za.co.xisystems.itis_rrm.data.repositories

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.withContext
import timber.log.Timber
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.custom.errors.LocalDataException
import za.co.xisystems.itis_rrm.custom.errors.NoDataException
import za.co.xisystems.itis_rrm.custom.errors.RecoverableException
import za.co.xisystems.itis_rrm.custom.errors.ServiceException
import za.co.xisystems.itis_rrm.custom.errors.TransmissionException
import za.co.xisystems.itis_rrm.custom.errors.XIErrorHandler
import za.co.xisystems.itis_rrm.custom.events.XIEvent
import za.co.xisystems.itis_rrm.custom.results.XIResult
import za.co.xisystems.itis_rrm.data.localDB.AppDatabase
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimateDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemMeasureDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemMeasurePhotoDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.ProjectItemDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.UserDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.WorkflowJobDTO
import za.co.xisystems.itis_rrm.data.network.BaseConnectionApi
import za.co.xisystems.itis_rrm.data.network.SafeApiRequest
import za.co.xisystems.itis_rrm.forge.DefaultDispatcherProvider
import za.co.xisystems.itis_rrm.forge.DispatcherProvider
import za.co.xisystems.itis_rrm.utils.ActivityIdConstants
import za.co.xisystems.itis_rrm.utils.Coroutines
import za.co.xisystems.itis_rrm.utils.DataConversion
import za.co.xisystems.itis_rrm.utils.PhotoUtil
import za.co.xisystems.itis_rrm.utils.enums.PhotoQuality
import za.co.xisystems.itis_rrm.utils.enums.WorkflowDirection

/**
 * Created by Francis Mahlava on 2019/11/28.
 */

class MeasureCreationDataRepository(
    private val api: BaseConnectionApi,
    private val appDb: AppDatabase,
    private val photoUtil: PhotoUtil,
    private val dispatchers: DispatcherProvider = DefaultDispatcherProvider()
) : SafeApiRequest() {
    companion object {
        val TAG: String = MeasureCreationDataRepository::class.java.simpleName
    }

    private val photoUpload = MutableLiveData<String>()

    /**
     * This is used to gather the status of workflow operations.
     */
    val workflowStatus: MutableLiveData<XIEvent<XIResult<String>>> = MutableLiveData()

    suspend fun getUser(): LiveData<UserDTO> {
        return withContext(dispatchers.io()) {
            appDb.getUserDao().getUser()
        }
    }

    suspend fun getJobMeasureForActivityId(
        activityId: Int,
        activityId2: Int,
        activityId3: Int
    ): LiveData<List<JobItemEstimateDTO>> {
        return withContext(dispatchers.io()) {
            appDb.getJobItemEstimateDao().getJobMeasureForActivityId(activityId, activityId2, activityId3)
        }
    }

    suspend fun saveMeasurementItems(
        userId: String,
        jobId: String,
        jimNo: String?,
        contractId: String?,
        mSures: ArrayList<JobItemMeasureDTO>,
        activity: FragmentActivity,
        itemMeasureJob: JobDTO
    ) : String {
        val topic = "Saving Measurements"
        postWorkflowStatus(XIResult.Progress(true))
        val measureData = JsonObject()
        val gson = Gson()
        val newMeasure = gson.toJson(mSures)
        val jsonElement: JsonElement = JsonParser.parseString(newMeasure)
        measureData.addProperty("ContractId", contractId)
        measureData.addProperty("JiNo", jimNo)
        measureData.addProperty("JobId", jobId)
        measureData.add("MeasurementItems", jsonElement)
        measureData.addProperty("UserId", userId)
        Timber.d("$measureData")

        val measurementItemResponse = apiRequest { api.saveMeasurementItems(measureData) }
        val messages = measurementItemResponse.errorMessage
        // You're only okay to perform the next step if this succeeded.
        if (measurementItemResponse.errorMessage.isNullOrEmpty()) {
            persistMeasurementWorkflow(
                measurementItemResponse.workflowJob,
                mSures,
                activity,
                itemMeasureJob
            )
        } else {
            postWorkflowStatus(
                XIResult.Error(
                    topic = topic,
                    exception = ServiceException(messages!!),
                    message = messages
                )
            )
        }

        return withContext(dispatchers.io()) {
            messages!!
        }

    }

    private suspend fun persistMeasurementWorkflow(
        workflowJobDTO: WorkflowJobDTO,
        jobItemMeasure: ArrayList<JobItemMeasureDTO>,
        activity: FragmentActivity,
        itemMeasureJob: JobDTO
    ) = withContext(dispatchers.io()) {
        val topic = "Measurement Workflow"
        try {
            // itemMeasureJob.JobItemMeasures = jobItemMeasure
            val measureJob = setWorkflowJobBigEndianGuids(workflowJobDTO)
            insertOrUpdateWorkflowJobInSQLite(measureJob, estimatesPush = false)
            uploadMeasurementImages(jobItemMeasure, activity)
            val myJob = getUpdatedJob(itemMeasureJob.jobId)
            moveJobToNextWorkflow(activity, measureJob, myJob)
        } catch (t: Throwable) {
            val message = t.message ?: XIErrorHandler.UNKNOWN_ERROR
            Timber.e(t)
            workflowStatus.postValue(XIEvent(XIResult.Error(topic = topic, exception = t, message = message)))
        }
    }

    private suspend fun getUpdatedJob(jobId: String): JobDTO {
        return withContext(dispatchers.io()) {
            appDb.getJobDao().getJobForJobId(jobId)
        }
    }

    private suspend fun uploadMeasurementImages(
        jobItemMeasures: ArrayList<JobItemMeasureDTO>,
        activity: FragmentActivity
    ) = withContext(dispatchers.io()) {
        var imageCounter = 1
        var totalImages = 0
        if (jobItemMeasures.isNotEmpty()) {
            for (jobItemMeasure in jobItemMeasures.iterator()) {
                val photos = jobItemMeasure.jobItemMeasurePhotos
                totalImages += photos.size
                for (photo in photos) {
                    when {
                        photoUtil.photoExist(photo.filename!!) -> {
                            val data: ByteArray =
                                getData(photo.filename, PhotoQuality.HIGH)
                            processImageUpload(
                                photo.filename,
                                activity.getString(R.string.jpg),
                                data,
                                imageCounter,
                                totalImages
                            )
                            imageCounter++
                            Timber.d("$imageCounter / $totalImages processed")
                        }
                    }
                }
            }
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

    private fun processImageUpload(
        filename: String,
        extension: String,
        photo: ByteArray,
        totalImages: Int,
        imageCounter: Int
    ) {

        Coroutines.io {
            val imageData = JsonObject()
            imageData.addProperty("Filename", filename)
            imageData.addProperty("ImageByteArray", photoUtil.encode64Pic(photo))
            imageData.addProperty("ImageFileExtension", extension)
            Timber.d("imageData: $imageData")

            val uploadImageResponse = apiRequest { api.uploadRrmImage(imageData) }
            photoUpload.postValue(uploadImageResponse.errorMessage)
            if (imageCounter <= totalImages) {
                Timber.d("Processing $imageCounter of $totalImages images")
            }
        }
    }

    private var workComplete: Boolean = false

    private suspend fun moveJobToNextWorkflow(
        activity: FragmentActivity,
        myJob: WorkflowJobDTO?,
        job: JobDTO
    ) {
        val topic = "Measurements Workflow Updates"
        if (myJob?.workflowItemMeasures == null) {
            val errorMessage = "No measurements to process. Please send them when you have some."
            postWorkflowStatus(
                XIResult.Error(
                    topic = topic,
                    exception = RecoverableException(errorMessage),
                    message = errorMessage
                )
            )
        } else {
            val description = activity.resources.getString(R.string.submit_for_approval)

            try {
                val measurementTracks = myJob.workflowItemMeasures.mapNotNull { item ->
                    if (item.actId < ActivityIdConstants.MEASURE_COMPLETE) {
                        item.toMeasurementTrack(
                            userId = job.userId.toString(),
                            description = description,
                            direction = WorkflowDirection.NEXT.value
                        )
                    } else {
                        // remove invalid items from list
                        null
                    }
                }

                measurementTracks.forEachIndexed { index, measurementTrack ->
                    postWorkflowStatus(
                        XIResult.Status("Processing ${index + 1} of ${measurementTracks.size} measurements")
                    )
                    val workflowMoveResponse = apiRequest {
                        api.getWorkflowMove(
                            measurementTrack.userId,
                            measurementTrack.trackRouteId,
                            measurementTrack.description,
                            measurementTrack.direction
                        )
                    }
                    when {
                        workflowMoveResponse.errorMessage != null -> {
                            Timber.e(workflowMoveResponse.errorMessage)
                            throw ServiceException(workflowMoveResponse.errorMessage)
                        }

                        workflowMoveResponse.workflowJob == null -> {
                            Timber.d("WorkflowJob is null for JiNo: ${job.jiNo}")
                            throw NoDataException("WorkflowJob is null for JiNo: ${job.jiNo}")
                        }

                        else -> {
                            Timber.d("${workflowMoveResponse.workflowJob}")
                            val workflowJob = workflowMoveResponse.workflowJob
                            workflowJob?.let {
                                saveWorkflowJob(it, true)
                            }
                        }
                    }
                }

                postWorkflowStatus(XIResult.Success(job.jiNo!!))
                workComplete = true
            } catch (t: Throwable) {
                val errorMessage = t.message ?: XIErrorHandler.UNKNOWN_ERROR
                Timber.e(t)
                postWorkflowStatus(XIResult.Error(topic = topic, exception = t, message = errorMessage))
            }
        }
    }

    private fun postWorkflowStatus(result: XIResult<String>) {
        workflowStatus.postValue(XIEvent(result))
    }

    private suspend fun saveWorkflowJob(
        workflowJob: WorkflowJobDTO,
        pushMeasures: Boolean
    ) {
        val localWorkflow = setWorkflowJobBigEndianGuids(workflowJob)
        if (localWorkflow != null) {
            persistWorkflowJob(localWorkflow, pushMeasures)
        }
    }

    suspend fun getJobItemsToMeasureForJobId(
        jobID: String?
    ):
        LiveData<List<JobItemEstimateDTO>> {
        return withContext(dispatchers.io()) {
            appDb.getJobItemEstimateDao().getJobItemsToMeasureForJobId(jobID!!)
        }
    }

    suspend fun getContractIdForProjectId(
        projectId: String?
    ): String {
        return withContext(dispatchers.io()) {
            appDb.getProjectDao().getContractIdForProjectId(projectId!!)
        }
    }


    suspend fun getSingleJobFromJobId(
        jobId: String?
    ): LiveData<JobDTO> {
        return withContext(dispatchers.io()) {
            appDb.getJobDao().getJobFromJobId(jobId!!)
        }
    }


    suspend fun getJobItemMeasuresForJobIdAndEstimateId2(
        jobId: String?,
        estimateId: String
    ):
        LiveData<List<JobItemMeasureDTO>> {
        return withContext(dispatchers.io()) {
            appDb.getJobItemMeasureDao()
                .getJobItemMeasuresForJobIdAndEstimateId2(jobId, estimateId)
        }
    }

    suspend fun getItemForItemId(
        projectItemId: String?
    ):
        LiveData<ProjectItemDTO> {
        return withContext(dispatchers.io()) {
            appDb.getProjectItemDao().getItemForItemId(projectItemId!!)
        }
    }


    fun saveJobItemMeasureItems(jobItemMeasures: ArrayList<JobItemMeasureDTO>) {
        Coroutines.io {
            for (jobItemMeasure in jobItemMeasures.iterator()) {
                if (!appDb.getJobItemMeasureDao()
                    .checkIfJobItemMeasureExists(jobItemMeasure.itemMeasureId)
                ) {
                    appDb.getJobItemMeasureDao().insertJobItemMeasure(jobItemMeasure)
                }
            }
        }
    }

    suspend fun getJobMeasureItemsPhotoPath(itemMeasureId: String): List<String> {
        return withContext(dispatchers.io()) {
            appDb.getJobItemMeasurePhotoDao().getJobMeasureItemPhotoPaths(itemMeasureId)
        }
    }

    fun deleteItemMeasurefromList(itemMeasureId: String) {
        Coroutines.io {
            appDb.getJobItemMeasureDao().deleteItemMeasurefromList(itemMeasureId)
        }
    }

    fun deleteItemMeasurephotofromList(itemMeasureId: String) {
        Coroutines.io {
            appDb.getJobItemMeasurePhotoDao()
                .deleteItemMeasurephotofromList(itemMeasureId)
        }
    }

    suspend fun setJobItemMeasureImages(
        jobItemMeasurePhotoList: ArrayList<JobItemMeasurePhotoDTO>,
        estimateId: String?,
        selectedJobItemMeasure: JobItemMeasureDTO
    ) {
        Coroutines.io {
            if (!appDb.getJobItemMeasureDao()
                .checkIfJobItemMeasureExists(selectedJobItemMeasure.itemMeasureId)
            ) {
                appDb.getJobItemMeasureDao().insertJobItemMeasure(selectedJobItemMeasure)
            }

            appDb.getJobItemEstimateDao()
                .setMeasureActId(selectedJobItemMeasure.actId, estimateId!!)

            appDb.getJobItemMeasureDao().upDatePhotList(
                jobItemMeasurePhotoList,
                selectedJobItemMeasure.itemMeasureId
            )

            jobItemMeasurePhotoList.forEach { jobItemMeasurePhoto ->
                jobItemMeasurePhoto.setEstimateId(estimateId)
                appDb.getJobItemMeasurePhotoDao().insertJobItemMeasurePhoto(jobItemMeasurePhoto)
            }
        }
    }

    suspend fun getJobItemMeasuresForJobIdAndEstimateId(
        jobId: String?
    ): LiveData<List<JobItemMeasureDTO>> {
        return withContext(dispatchers.io()) {
            appDb.getJobItemMeasureDao().getJobItemMeasuresForJobIdAndEstimateId(jobId)
        }
    }

    private fun JobItemMeasurePhotoDTO.setEstimateId(estimateId: String?) {
        this.estimateId = estimateId!!
    }

    suspend fun getItemDescription(jobId: String): String {
        return withContext(dispatchers.io()) {
            appDb.getJobDao().getItemDescription(jobId)
        }
    }

    suspend fun getItemJobNo(jobId: String): String = withContext(dispatchers.io()) {
        return@withContext appDb.getJobDao().getItemJobNo(jobId)
    }

    private fun insertOrUpdateWorkflowJobInSQLite(job: WorkflowJobDTO?, estimatesPush: Boolean = false) {
        job?.let {
            Coroutines.io {
                persistWorkflowJob(it, estimatesPush)
            }
        }
    }

    private suspend fun persistWorkflowJob(
        job: WorkflowJobDTO,
        pushMeasures: Boolean = false
    ) {
        val topic = "Workflow updates"
        try {
            var measuresFlag = pushMeasures
            appDb.getJobDao()
                .updateJob(job.trackRouteId, job.actId, job.jiNo, job.jobId)

            job.workflowItemEstimates.forEach { jobItemEstimate ->
                appDb.getJobItemEstimateDao().updateExistingJobItemEstimateWorkflow(
                    jobItemEstimate.trackRouteId,
                    jobItemEstimate.actId,
                    jobItemEstimate.estimateId
                )

                if (pushMeasures) {

                    appDb.getJobItemEstimateDao().setMeasureActId(
                        actId = jobItemEstimate.actId,
                        estimateId = jobItemEstimate.estimateId
                    )
                }

                jobItemEstimate.workflowEstimateWorks.forEach { jobEstimateWorks ->
                    if (appDb.getEstimateWorkDao()
                        .checkIfJobEstimateWorksExist(jobEstimateWorks.worksId)
                    ) {
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

                job.workflowItemMeasures.forEach { jobItemMeasure ->
                    appDb.getJobItemMeasureDao().updateWorkflowJobItemMeasure(
                        jobItemMeasure.itemMeasureId,
                        jobItemMeasure.trackRouteId,
                        jobItemMeasure.actId,
                        jobItemMeasure.measureGroupId
                    )

                    /**
                     * If measurements are being pushed for confirmation,
                     * ensure that their attached estimates reflect this.
                     * Solves #MOBILEXI-1229 - Estimates are no longer stuck
                     */

                    if (measuresFlag) {
                        job.workflowItemEstimates.forEach {
                            appDb.getJobItemEstimateDao().updateActIdForJobItemEstimate(
                                jobItemMeasure.actId,
                                it.estimateId
                            )
                        }
                        measuresFlag = false
                    }
                }
            }

            //  Place the Job Section, UPDATE OR CREATE

            job.workflowJobSections.forEach { jobSection ->
                if (!appDb.getJobSectionDao()
                    .checkIfJobSectionExist(jobSection.jobSectionId)
                ) {
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
        } catch (e: Exception) {
            val saveFail = XIResult.Error(
                topic = topic,
                exception = e,
                message = e.message ?: XIErrorHandler.UNKNOWN_ERROR
            )

            postWorkflowStatus(saveFail)
            Timber.e(e, "Failed to save updates: ${e.message}")
        }
    }

    private fun setWorkflowJobBigEndianGuids(job: WorkflowJobDTO): WorkflowJobDTO? {
        val topic = "Measurement Workflow Conversions"
        try {
            job.jobId = DataConversion.toBigEndian(job.jobId)
            job.trackRouteId = DataConversion.toBigEndian(job.trackRouteId)
            job.workflowItemEstimates.forEach { jie ->
                jie.estimateId = DataConversion.toBigEndian(jie.estimateId)!!
                jie.trackRouteId = DataConversion.toBigEndian(jie.trackRouteId)!!
                //  Let's go through the WorkFlowEstimateWorks
                jie.workflowEstimateWorks.forEach { wfe ->
                    wfe.trackRouteId =
                        DataConversion.toBigEndian(wfe.trackRouteId)!!
                    wfe.worksId = DataConversion.toBigEndian(wfe.worksId)!!

                    wfe.estimateId = DataConversion.toBigEndian(wfe.estimateId)!!
                }
            }

            if (job.workflowItemMeasures.isNotEmpty()) {
                for (jim in job.workflowItemMeasures) {
                    jim.itemMeasureId = DataConversion.toBigEndian(jim.itemMeasureId)!!
                    jim.measureGroupId =
                        DataConversion.toBigEndian(jim.measureGroupId)!!
                    jim.trackRouteId = DataConversion.toBigEndian(jim.trackRouteId)!!
                }
            }
            if (job.workflowJobSections.isNotEmpty()) {
                for (js in job.workflowJobSections) {
                    js.jobSectionId = DataConversion.toBigEndian(js.jobSectionId)!!
                    js.projectSectionId =
                        DataConversion.toBigEndian(js.projectSectionId)!!
                    js.jobId = DataConversion.toBigEndian(js.jobId)
                }
            }
            return job
        } catch (t: Throwable) {
            val message = "Failed to convert to BigEndian: ${t.message ?: XIErrorHandler.UNKNOWN_ERROR}"
            postWorkflowStatus(
                XIResult.Error(
                    topic = topic,
                    exception = LocalDataException(message),
                    message = message
                )
            )
            postWorkflowStatus(XIResult.Progress(false))
            return null
        }
    }

    suspend fun getSectionForProjectSectionId(sectionId: String?): String {
        return withContext(dispatchers.io()) {
            appDb.getProjectSectionDao().getSectionForProjectSectionId(sectionId)
        }
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

    suspend fun getProjectItemDescription(projectItemId: String): String {
        return withContext(dispatchers.io()) {
            appDb.getProjectItemDao().getProjectItemDescription(projectItemId)
        }
    }

    suspend fun getJobItemMeasureByItemMeasureId(itemMeasureId: String): LiveData<JobItemMeasureDTO> {
        return withContext(dispatchers.io()) {
            appDb.getJobItemMeasureDao().getJobItemMeasureByItemMeasureId(itemMeasureId)
        }
    }

    suspend fun getJobItemMeasurePhotosForItemMeasureID(itemMeasureId: String): LiveData<List<JobItemMeasurePhotoDTO>> {
        return withContext(dispatchers.io()) {
            appDb.getJobItemMeasurePhotoDao().getJobItemMeasurePhotosForItemMeasureID(itemMeasureId)
        }
    }

    suspend fun getJobMeasureItemsByJobIdAndActId(
        jobID: String?,
        actId: Int
    ): LiveData<List<JobItemMeasureDTO>> {
        return withContext(dispatchers.io()) {
            appDb.getJobItemMeasureDao().getJobItemMeasuresByJobIdAndActId(jobID!!, actId)
        }
    }

    @Suppress("TooGenericExceptionCaught")
    suspend fun updateMeasureCreatedInfo(userId: String, jobId: String): Boolean {
        val requestData = JsonObject()
        requestData.addProperty("UserId", userId)
        requestData.addProperty("JobId", jobId)
        try {
            val updateResponse =
                apiRequest { api.updateMeasureCreatedInfo(requestData) }
            if (!updateResponse.isSuccess) {
                throw ServiceException(updateResponse.errorMessage!!)
            }
            return true
        } catch (ex: Exception) {
            val message = "Failed to update work start / end times."
            Timber.e(ex, message)
            throw TransmissionException(message, ex)
        }
    }
}
