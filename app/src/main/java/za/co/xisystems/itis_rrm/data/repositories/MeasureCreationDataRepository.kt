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
import za.co.xisystems.itis_rrm.custom.errors.NoDataException
import za.co.xisystems.itis_rrm.custom.errors.ServiceException
import za.co.xisystems.itis_rrm.custom.results.XIError
import za.co.xisystems.itis_rrm.custom.results.XIResult
import za.co.xisystems.itis_rrm.custom.results.XISuccess
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
import za.co.xisystems.itis_rrm.utils.Coroutines
import za.co.xisystems.itis_rrm.utils.DataConversion
import za.co.xisystems.itis_rrm.utils.PhotoUtil
import za.co.xisystems.itis_rrm.utils.PhotoUtil.getPhotoPathFromExternalDirectory
import za.co.xisystems.itis_rrm.utils.enums.PhotoQuality
import za.co.xisystems.itis_rrm.utils.enums.WorkflowDirection

/**
 * Created by Francis Mahlava on 2019/11/28.
 */

class MeasureCreationDataRepository(
    private val api: BaseConnectionApi,
    private val appDb: AppDatabase
) : SafeApiRequest() {
    companion object {
        val TAG: String = MeasureCreationDataRepository::class.java.simpleName
    }

    private val workflowJ = MutableLiveData<WorkflowJobDTO>()
    private val photoUpload = MutableLiveData<String>()

    /**
     * This is used to gather the status of workflow operations.
     */
    val workflowStatus: MutableLiveData<XIResult<String>> = MutableLiveData()

    init {

        workflowJ.observeForever {
            try {
                saveWorkflowJob(it)
            } catch (e: Exception) {
                workflowStatus.postValue(XIError(e, e.message!!))
            }
        }
    }

    suspend fun getUser(): LiveData<UserDTO> {
        return withContext(Dispatchers.IO) {
            appDb.getUserDao().getUser()
        }
    }

    suspend fun getJobMeasureForActivityId(
        activityId: Int,
        activityId2: Int
    ): LiveData<List<JobItemEstimateDTO>> {
        return withContext(Dispatchers.IO) {
            appDb.getJobItemEstimateDao().getJobMeasureForActivityId(activityId, activityId2)
        }
    }

    suspend fun saveMeasurementItems(
        userId: String,
        jobId: String,
        jimNo: String?,
        contractVoId: String?,
        mSures: ArrayList<JobItemMeasureDTO>,
        activity: FragmentActivity,
        itemMeasureJob: JobDTO
    ) {

        return withContext(Dispatchers.IO) {

            val measureData = JsonObject()
            val gson = Gson()
            val newMeasure = gson.toJson(mSures)
            val jsonElement: JsonElement = JsonParser.parseString(newMeasure)
            measureData.addProperty("ContractId", contractVoId)
            measureData.addProperty("JiNo", jimNo)
            measureData.addProperty("JobId", jobId)
            measureData.add("MeasurementItems", jsonElement)
            measureData.addProperty("UserId", userId)
            Timber.d("$measureData")

            val measurementItemResponse = apiRequest { api.saveMeasurementItems(measureData) }

            val messages = measurementItemResponse.errorMessage ?: ""

            // You're only okay to perform the next step if this succeeded.
            if (messages.isBlank()) {
                postValue(
                    measurementItemResponse.workflowJob,
                    mSures,
                    activity,
                    itemMeasureJob
                )
            } else {
                workflowStatus.postValue(
                    XIError(
                        ServiceException(messages),
                        "Failed to save measurements: $messages"
                    )
                )
            }
        }
    }

    private fun postValue(
        workflowJobDTO: WorkflowJobDTO,
        jobItemMeasure: ArrayList<JobItemMeasureDTO>,
        activity: FragmentActivity,
        itemMeasureJob: JobDTO
    ) {
        Coroutines.io {
            try {

                itemMeasureJob.JobItemMeasures = jobItemMeasure
                val measureJob = setWorkflowJobBigEndianGuids(workflowJobDTO)
                insertOrUpdateWorkflowJobInSQLite(measureJob)
                uploadMeasurementImages(jobItemMeasure, activity)
                val myJob = getUpdatedJob(itemMeasureJob.JobId)
                moveJobToNextWorkflow(activity, measureJob, myJob)
            } catch (e: Exception) {
                Timber.e(e, "Failed to process workflow move: ${e.message}")
                workflowStatus.postValue(XIError(e, e.message!!))
            }
        }
    }

    private suspend fun getUpdatedJob(jobId: String): JobDTO {
        return withContext(Dispatchers.IO) {
            appDb.getJobDao().getJobForJobId(jobId)
        }
    }

    private fun uploadMeasurementImages(
        jobItemMeasures: ArrayList<JobItemMeasureDTO>,
        activity: FragmentActivity?
    ) {
        var imageCounter = 1
        var totalImages = 0
        if (jobItemMeasures.isNotEmpty()) {
            for (jobItemMeasure in jobItemMeasures.iterator()) {
                if (jobItemMeasure.jobItemMeasurePhotos.isNotEmpty()) {
                    totalImages += jobItemMeasure.jobItemMeasurePhotos.size
                    for (photo in jobItemMeasure.jobItemMeasurePhotos) {
                        if (PhotoUtil.photoExist(photo.filename!!)) {
                            val data: ByteArray =
                                getData(photo.filename, PhotoQuality.HIGH, activity!!)
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

    private fun getData(
        filename: String,
        photoQuality: PhotoQuality,
        activity: FragmentActivity
    ): ByteArray {
        val uri = getPhotoPathFromExternalDirectory(filename)
        val bitmap =
            PhotoUtil.getPhotoBitmapFromFile(activity.applicationContext, uri, photoQuality)
        return PhotoUtil.getCompressedPhotoWithExifInfo(
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
            imageData.addProperty("ImageByteArray", PhotoUtil.encode64Pic(photo))
            imageData.addProperty("ImageFileExtension", extension)
            Timber.d("imageData: $imageData")

            val uploadImageResponse = apiRequest { api.uploadRrmImage(imageData) }
            photoUpload.postValue(uploadImageResponse.errorMessage)
            if (imageCounter <= totalImages) {
                Timber.d("Processing $imageCounter of $totalImages images")
            }
        }
    }

    suspend fun errorMsg(): String {
        val messages = getErrorMsg()
        return withContext(Dispatchers.IO) {
            messages
        }
    }

    suspend fun errorState(): Boolean {
        val state = getErrorState()
        return withContext(Dispatchers.IO) {
            state
        }
    }

    private suspend fun moveJobToNextWorkflow(
        activity: FragmentActivity,
        myJob: WorkflowJobDTO?,
        job: JobDTO
    ) {

        if (myJob?.workflowItemMeasures == null) {
            getErrorMsg()
        } else {
            try {
                loop@ for (jobItemMeasure in myJob.workflowItemMeasures.iterator()) {
                    val direction: Int = WorkflowDirection.NEXT.value
                    val trackRouteId: String =
                        DataConversion.toLittleEndian(jobItemMeasure.trackRouteId)!!
                    val description: String =
                        activity.resources.getString(R.string.submit_for_approval)

                    val workflowMoveResponse = apiRequest {
                        api.getWorkflowMove(
                            job.UserId.toString(),
                            trackRouteId,
                            description,
                            direction
                        )
                    }
                    when {
                        workflowMoveResponse.errorMessage != null -> {
                            Timber.e(workflowMoveResponse.errorMessage)
                            workflowStatus.postValue(
                                XIError(
                                    ServiceException(workflowMoveResponse.errorMessage),
                                    workflowMoveResponse.errorMessage
                                )
                            )
                            break@loop
                        }

                        workflowMoveResponse.workflowJob == null -> {
                            Timber.d("WorkflowJob is null for JiNo: ${job.JiNo}")
                            val ndEx = NoDataException("WorkflowJob is null for JiNo: ${job.JiNo}")
                            workflowStatus.postValue(XIError(ndEx, ndEx.message!!))
                            break@loop
                        }

                        else -> {
                            Timber.d("${workflowMoveResponse.workflowJob}")
                            workflowJ.postValue(workflowMoveResponse.workflowJob)
                        }
                    }
                }
                workflowStatus.postValue(XISuccess("Workflow Complete!"))
            } catch (e: Exception) {
                Timber.e(e)
                workflowStatus.postValue(XIError(e, e.message!!))
            }
        }
    }

    private fun getErrorMsg():
        String {
        getErrorState()
        return "Error: WorkFlow Job is null"
    }

    private fun getErrorState():
        Boolean {
        return true
    }

    private fun saveWorkflowJob(
        workflowJob: WorkflowJobDTO?
    ) {
        try {
            if (workflowJob != null) {
                val job = setWorkflowJobBigEndianGuids(workflowJob)
                insertOrUpdateWorkflowJobInSQLite(job)
            } else {
                Timber.e("Error -> WorkFlow Job is null")
            }
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun getJobItemsToMeasureForJobId(
        jobID: String?
    ):
        LiveData<List<JobItemEstimateDTO>> {
        return withContext(Dispatchers.IO) {
            appDb.getJobItemEstimateDao().getJobItemsToMeasureForJobId(jobID!!)
        }
    }

    suspend fun getSingleJobFromJobId(
        jobId: String?
    ):
        LiveData<JobDTO> {
        return withContext(Dispatchers.IO) {
            appDb.getJobDao().getJobFromJobId(jobId!!)
        }
    }

    suspend fun getJobItemMeasuresForJobIdAndEstimateId2(
        jobId: String?,
        estimateId: String
    ):
        LiveData<List<JobItemMeasureDTO>> {
        return withContext(Dispatchers.IO) {
            appDb.getJobItemMeasureDao()
                .getJobItemMeasuresForJobIdAndEstimateId2(jobId, estimateId)
        }
    }

    suspend fun getItemForItemId(
        projectItemId: String?
    ):
        LiveData<ProjectItemDTO> {
        return withContext(Dispatchers.IO) {
            appDb.getProjectItemDao().getItemForItemId(projectItemId!!)
        }
    }

    fun saveJobItemMeasureItems(jobItemMeasures: ArrayList<JobItemMeasureDTO>) {
        Coroutines.io {
            for (jobItemMeasure in jobItemMeasures.iterator()) {
                if (!appDb.getJobItemMeasureDao()
                        .checkIfJobItemMeasureExists(jobItemMeasure.itemMeasureId!!)
                ) {
                    appDb.getJobItemMeasureDao().insertJobItemMeasure(jobItemMeasure)
                }
            }
        }
    }

    suspend fun getJobMeasureItemsPhotoPath(itemMeasureId: String): List<String> {
        return withContext(Dispatchers.IO) {
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
            for (jobItemMeasurePhoto in jobItemMeasurePhotoList.iterator()) {
                if (!appDb.getJobItemMeasureDao()
                        .checkIfJobItemMeasureExists(selectedJobItemMeasure.itemMeasureId!!)
                )
                    appDb.getJobItemMeasureDao()
                        .insertJobItemMeasure(selectedJobItemMeasure)

                appDb.getJobItemEstimateDao()
                    .setMeasureActId(selectedJobItemMeasure.actId, estimateId!!)

                if (!appDb.getJobItemMeasurePhotoDao()
                        .checkIfJobItemMeasurePhotoExists(jobItemMeasurePhoto.filename!!)
                ) {

                    appDb.getJobItemMeasurePhotoDao()
                        .insertJobItemMeasurePhoto(jobItemMeasurePhoto)
                    jobItemMeasurePhoto.setEstimateId(estimateId)
                    appDb.getJobItemMeasureDao().upDatePhotList(
                        jobItemMeasurePhotoList,
                        selectedJobItemMeasure.itemMeasureId!!
                    )
                }
            }
        }
    }

    suspend fun getJobItemMeasuresForJobIdAndEstimateId(
        jobId: String?
    ): LiveData<List<JobItemMeasureDTO>> {
        return withContext(Dispatchers.IO) {
            appDb.getJobItemMeasureDao().getJobItemMeasuresForJobIdAndEstimateId(jobId)
        }
    }

    suspend fun getJobItemMeasurePhotosForItemEstimateID(estimateId: String): LiveData<List<JobItemMeasurePhotoDTO>> {
        return withContext(Dispatchers.IO) {
            appDb.getJobItemMeasurePhotoDao()
                .getJobItemMeasurePhotosForItemEstimateID(estimateId)
        }
    }

    private fun JobItemMeasurePhotoDTO.setEstimateId(estimateId: String?) {
        this.estimateId = estimateId
    }

    suspend fun getItemDescription(jobId: String): String {
        return withContext(Dispatchers.IO) {
            appDb.getJobDao().getItemDescription(jobId)
        }
    }

    suspend fun getItemJobNo(jobId: String): String = appDb.getJobDao().getItemJobNo(jobId)

    private fun insertOrUpdateWorkflowJobInSQLite(job: WorkflowJobDTO?) {
        job?.let {
            updateWorkflowJobValuesAndInsertWhenNeeded(it)
        }
    }

    private fun updateWorkflowJobValuesAndInsertWhenNeeded(
        job: WorkflowJobDTO
    ) {
        Coroutines.io {
            try {
                appDb.getJobDao()
                    .updateJob(job.trackRouteId, job.actId, job.jiNo, job.jobId)

                job.workflowItemEstimates?.forEach { jobItemEstimate ->
                    appDb.getJobItemEstimateDao().updateExistingJobItemEstimateWorkflow(
                        jobItemEstimate.trackRouteId,
                        jobItemEstimate.actId,
                        jobItemEstimate.estimateId,
                    )

                    appDb.getJobItemEstimateDao().setMeasureActId(
                        jobItemEstimate.actId,
                        jobItemEstimate.estimateId,
                    )

                    jobItemEstimate.workflowEstimateWorks.forEach { jobEstimateWorks ->
                        if (!appDb.getEstimateWorkDao()
                                .checkIfJobEstimateWorksExist(jobEstimateWorks.worksId)
                        )

                            appDb.getEstimateWorkDao().insertJobEstimateWorks(
                                // jobEstimateWorks as JobEstimateWorksDTO
                                TODO("This should never happen!")
                            )
                        else
                            appDb.getEstimateWorkDao().updateJobEstimateWorksWorkflow(
                                jobEstimateWorks.worksId,
                                jobEstimateWorks.estimateId,
                                jobEstimateWorks.recordVersion,
                                jobEstimateWorks.recordSynchStateId,
                                jobEstimateWorks.actId,
                                jobEstimateWorks.trackRouteId
                            )
                    }


                    job.workflowItemMeasures?.forEach { jobItemMeasure ->
                        appDb.getJobItemMeasureDao().updateWorkflowJobItemMeasure(
                            jobItemMeasure.itemMeasureId,
                            jobItemMeasure.trackRouteId,
                            jobItemMeasure.actId,
                            jobItemMeasure.measureGroupId
                        )
                    }

                }

                //  Place the Job Section, UPDATE OR CREATE

                job.workflowJobSections?.forEach { jobSection ->
                    if (!appDb.getJobSectionDao()
                            .checkIfJobSectionExist(jobSection.jobSectionId)
                    )
                        appDb.getJobSectionDao().insertJobSection(jobSection)
                    else
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
            } catch (e: Exception) {
                val saveFail = XIError(e, "Failed to save updates: ${e.message}")
                workflowStatus.postValue(saveFail)
                Timber.e(e, "Failed to save updates: ${e.message}")
            }
        }
    }

    private fun setWorkflowJobBigEndianGuids(job: WorkflowJobDTO): WorkflowJobDTO? {

        job.jobId = DataConversion.toBigEndian(job.jobId)
        job.trackRouteId = DataConversion.toBigEndian(job.trackRouteId)

        if (job.workflowItemEstimates != null) {
            for (jie in job.workflowItemEstimates) {

                jie.estimateId = DataConversion.toBigEndian(jie.estimateId)!!
                jie.trackRouteId = DataConversion.toBigEndian(jie.trackRouteId)!!
                //  Let's go through the WorkFlowEstimateWorks
                for (wfe in jie.workflowEstimateWorks) {
                    wfe.trackRouteId =
                        DataConversion.toBigEndian(wfe.trackRouteId)!!
                    wfe.worksId = DataConversion.toBigEndian(wfe.worksId)!!

                    wfe.estimateId = DataConversion.toBigEndian(wfe.estimateId)!!
                }
            }
        }
        if (job.workflowItemMeasures != null) {
            for (jim in job.workflowItemMeasures) {
                jim.itemMeasureId = DataConversion.toBigEndian(jim.itemMeasureId)!!
                jim.measureGroupId =
                    DataConversion.toBigEndian(jim.measureGroupId)!!
                jim.trackRouteId = DataConversion.toBigEndian(jim.trackRouteId)!!
            }
        }
        if (job.workflowJobSections != null) {
            for (js in job.workflowJobSections) {
                js.jobSectionId = DataConversion.toBigEndian(js.jobSectionId)!!
                js.projectSectionId =
                    DataConversion.toBigEndian(js.projectSectionId)!!
                js.jobId = DataConversion.toBigEndian(js.jobId)
            }
        }
        return job
    }

    suspend fun getSectionForProjectSectionId(sectionId: String?): String {
        return withContext(Dispatchers.IO) {
            appDb.getProjectSectionDao().getSectionForProjectSectionId(sectionId!!)
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

    suspend fun getProjectItemDescription(projectItemId: String): String {
        return withContext(Dispatchers.IO) {
            appDb.getProjectItemDao().getProjectItemDescription(projectItemId)
        }
    }

    suspend fun getJobItemMeasureByItemMeasureId(itemMeasureId: String): LiveData<JobItemMeasureDTO> {
        return withContext(Dispatchers.IO) {
            appDb.getJobItemMeasureDao().getJobItemMeasureByItemMeasureId(itemMeasureId)
        }
    }

    suspend fun getJobItemMeasurePhotosForItemMeasureID(itemMeasureId: String): LiveData<List<JobItemMeasurePhotoDTO>> {
        return withContext(Dispatchers.IO) {
            appDb.getJobItemMeasurePhotoDao().getJobItemMeasurePhotosForItemMeasureID(itemMeasureId)
        }
    }
}
