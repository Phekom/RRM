package za.co.xisystems.itis_rrm.data.repositories

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.Transaction
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.custom.errors.NoDataException
import za.co.xisystems.itis_rrm.custom.errors.ServiceException
import za.co.xisystems.itis_rrm.custom.errors.XIErrorHandler
import za.co.xisystems.itis_rrm.custom.results.XIResult
import za.co.xisystems.itis_rrm.data.localDB.AppDatabase
import za.co.xisystems.itis_rrm.data.localDB.JobDataController
import za.co.xisystems.itis_rrm.data.localDB.entities.ItemDTOTemp
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimateDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimatesPhotoDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobSectionDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobTypeEntityDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.ProjectItemDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.ProjectSectionDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.SectionItemDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.SectionPointDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.UserDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.WorkflowJobDTO
import za.co.xisystems.itis_rrm.data.localDB.views.SectionMarker
import za.co.xisystems.itis_rrm.data.network.BaseConnectionApi
import za.co.xisystems.itis_rrm.data.network.SafeApiRequest
import za.co.xisystems.itis_rrm.data.network.responses.UploadImageResponse
import za.co.xisystems.itis_rrm.domain.ContractSelector
import za.co.xisystems.itis_rrm.domain.ProjectSelector
import za.co.xisystems.itis_rrm.forge.DefaultDispatcherProvider
import za.co.xisystems.itis_rrm.forge.DispatcherProvider
import za.co.xisystems.itis_rrm.utils.Coroutines
import za.co.xisystems.itis_rrm.utils.DataConversion
import za.co.xisystems.itis_rrm.utils.PhotoUtil
import za.co.xisystems.itis_rrm.utils.enums.PhotoQuality
import za.co.xisystems.itis_rrm.utils.enums.WorkflowDirection
import java.io.IOException

/**
 * Created by Francis Mahlava on 2019/11/28.
 */

/**
 * Updated by Shaun McDonald on 2021/05/19
 * Last modified on 2021/05/19, 19:05
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 **/

class JobCreationDataRepository(
    private val api: BaseConnectionApi,
    private val appDb: AppDatabase,
    private val photoUtil: PhotoUtil,
    private val dispatchers: DispatcherProvider = DefaultDispatcherProvider()
) : SafeApiRequest() {

    private val workflowJobs = MutableLiveData<WorkflowJobDTO>()
    private val jobDataController: JobDataController? = null
    private val photoUpload = MutableLiveData<String>()
    private val routeSectionPoint = MutableLiveData<String>()

    init {

        workflowJobs.observeForever {
            saveWorkflowJob(it)
        }

        photoUpload.observeForever {
            sendMsg(it)
        }

        routeSectionPoint.observeForever {
        }
    }

    suspend fun getUser(): LiveData<UserDTO> {
        return withContext(dispatchers.io()) {
            appDb.getUserDao().getUser()
        }
    }

    private fun sendMsg(uploadResponse: String?) {
        val response: UploadImageResponse? = null
        if (uploadResponse.isNullOrEmpty()) {
            jobDataController?.setMsg(response!!.errorMessage)
        }
    }

    fun saveWorkflowJob(workflowJob: WorkflowJobDTO?) {
        if (workflowJob != null) {
            val job = setWorkflowJobBigEndianGuids(workflowJob)
            insertOrUpdateWorkflowJobInSQLite(job)
        } else {
            val message = "Workflow job is null."
            Timber.e(java.lang.NullPointerException(message), message)
            throw NoDataException(message)
        }
    }

    fun saveNewJob(newJob: JobDTO?) {
        Coroutines.io {
            if (newJob != null && !appDb.getJobDao().checkIfJobExist(newJob.jobId)) {
                appDb.getJobDao().insertOrUpdateJob(newJob)
            }
        }
    }

    suspend fun getAllSectionItemsForProject(projectId: String): LiveData<List<SectionItemDTO>> {
        return withContext(dispatchers.io()) {
            appDb.getSectionItemDao().getFilteredSectionItems(projectId)
        }
    }

    suspend fun getAllItemsForSectionItemByProject(
        sectionItemId: String,
        projectId: String
    ): LiveData<List<ProjectItemDTO>> {
        return withContext(dispatchers.io()) {
            appDb.getProjectItemDao().getAllItemsForSectionItemByProject(sectionItemId, projectId)
        }
    }

    fun saveNewItem(newJobItem: ItemDTOTemp?) {
        Coroutines.io {
            if (newJobItem != null && !appDb.getItemDaoTemp()
                .checkItemExistsItemId(newJobItem.itemId)
            ) {

                appDb.getItemDaoTemp().insertItems(newJobItem)
            }
        }
    }

    fun delete(item: ItemDTOTemp) {
        Coroutines.io {
            appDb.getItemDaoTemp().deleteItem(item)
        }
    }

    fun deleteJobfromList(jobId: String) {
        Coroutines.io {
            appDb.getJobDao().deleteJobForJobId(jobId)
        }
    }

    @Suppress("TooGenericExceptionCaught")
    @Transaction
    suspend fun updateNewJob(
        newJobId: String,
        startKM: Double,
        endKM: Double,
        sectionId: String,
        newJobItemEstimatesList: ArrayList<JobItemEstimateDTO>,
        jobItemSectionArrayList: ArrayList<JobSectionDTO>
    ): XIResult<JobDTO> = withContext(dispatchers.io()) {
        try {

            if (appDb.getJobDao().checkIfJobExist(newJobId)) {
                appDb.getJobItemEstimateDao().updateJobItemEstimates(newJobItemEstimatesList)
                jobItemSectionArrayList.forEach { jobSectionDTO ->
                    appDb.getJobSectionDao().updateExistingJobSectionWorkflow(
                        jobSectionId = jobSectionDTO.jobSectionId,
                        projectSectionId = jobSectionDTO.projectSectionId,
                        jobId = newJobId,
                        startKm = jobSectionDTO.startKm,
                        endKm = jobSectionDTO.endKm,
                        recordVersion = jobSectionDTO.recordVersion,
                        recordSynchStateId = jobSectionDTO.recordSynchStateId
                    )
                }
                appDb.getJobDao().updateJoSecId(
                    newJobId = newJobId,
                    startKM = startKM,
                    endKM = endKM,
                    sectionId = sectionId,
                    newJobItemEstimatesList = newJobItemEstimatesList,
                    jobItemSectionArrayList = jobItemSectionArrayList
                )
                val updatedJob = appDb.getJobDao().getJobForJobId(newJobId)
                return@withContext XIResult.Success(updatedJob)
            } else {
                throw NoDataException("Job $newJobId does not exist...")
            }
        } catch (ex: Exception) {
            val message = "Failed to update job: ${ex.message ?: XIErrorHandler.UNKNOWN_ERROR}"
            Timber.e(ex, message)
            return@withContext XIResult.Error(ex, message)
        }
    }

    suspend fun getPointSectionData(projectId: String): SectionPointDTO {
        return withContext(dispatchers.io()) {
            appDb.getSectionPointDao().getPointSectionData(projectId)
        }
    }

    suspend fun getSectionByRouteSectionProject(
        sectionId: String,
        linearId: String?,
        projectId: String?,
        pointLocation: Double
    ): String? {
        return withContext(dispatchers.io()) {
            appDb.getProjectSectionDao()
                .getSectionByRouteSectionProject(sectionId, linearId!!, projectId!!, pointLocation)
        }
    }

    suspend fun getLiveSection(sectionId: String): LiveData<ProjectSectionDTO> {
        return withContext(dispatchers.io()) {
            appDb.getProjectSectionDao().getLiveSection(sectionId)
        }
    }

    suspend fun getSection(sectionId: String): ProjectSectionDTO = withContext(dispatchers.io()) {
        return@withContext appDb.getProjectSectionDao().getSection(sectionId)
    }

    suspend fun getAllProjectItems(projectId: String, jobId: String): LiveData<List<ItemDTOTemp>> {
        return withContext(dispatchers.io()) {
            appDb.getItemDaoTemp().getAllProjecItems(projectId, jobId)
        }
    }

    fun deleteItemList(jobId: String) {
        Coroutines.io {
            appDb.getItemDaoTemp().deleteItemList(jobId)
        }
    }

    @Transaction
    suspend fun deleteItemFromList(itemId: String, estimateId: String?): Int {
        return withContext(dispatchers.io()) {
            val itemsDeleted = appDb.getItemDaoTemp().deleteItemFromList(itemId)
            val estimatesDeleted =
                estimateId?.let {
                    appDb.getJobItemEstimateDao()
                        .deleteJobItemEstimateByEstimateId(it)
                } ?: 0

            val estimatesPhotosDeleted =
                estimateId?.let {
                    appDb.getJobItemEstimatePhotoDao()
                        .deleteJobItemEstimatePhotosByEstimateId(it)
                } ?: 0
            itemsDeleted + estimatesDeleted + estimatesPhotosDeleted
        }
    }

    @Suppress("TooGenericExceptionCaught")
    suspend fun submitJob(userId: Int, job: JobDTO): WorkflowJobDTO = withContext(dispatchers.io()) {
        val jobData = JsonObject()
        val gson = Gson()
        val newJob = gson.toJson(job)
        val jsonElement: JsonElement = JsonParser.parseString(newJob)
        jobData.add("Job", jsonElement)
        jobData.addProperty("UserId", userId)
        Timber.d("Json Job: $jobData")

        val jobResponse = apiRequest { api.sendJobsForApproval(jobData) }

        jobResponse.errorMessage?.let {
            throw ServiceException(jobResponse.errorMessage)
        }

        if (jobResponse.workflowJob != null) {
            return@withContext jobResponse.workflowJob!!
        } else {
            throw NoDataException("Server returned empty workflow response.")
        }
    }

    suspend fun postWorkflowJob(
        workflowJob: WorkflowJobDTO,
        job: JobDTO,
        activity: FragmentActivity
    ): JobDTO = withContext(dispatchers.io()) {

        val translatedJob = setWorkflowJobBigEndianGuids(workflowJob)
        insertOrUpdateWorkflowJobInSQLite(translatedJob)
        uploadCreateJobImages(
            packageJob = job,
            activity = activity
        )

        return@withContext getUpdatedJob(DataConversion.toBigEndian(job.jobId)!!)
    }

    suspend fun getUpdatedJob(jobId: String): JobDTO {
        return withContext(dispatchers.io()) {
            appDb.getJobDao().getJobForJobId(jobId)
        }
    }

    private fun setWorkflowJobBigEndianGuids(job: WorkflowJobDTO): WorkflowJobDTO {

        // Job + WorkflowItems + EstimateWorks
        job.jobId = DataConversion.toBigEndian(job.jobId)
        job.trackRouteId = DataConversion.toBigEndian(job.trackRouteId)
        job.workflowItemEstimates.forEach { jie ->
            jie.estimateId = DataConversion.toBigEndian(jie.estimateId)!!
            jie.trackRouteId = DataConversion.toBigEndian(jie.trackRouteId)!!
            jie.workflowEstimateWorks.forEach { wfe ->
                wfe.trackRouteId = DataConversion.toBigEndian(wfe.trackRouteId)!!
                wfe.worksId = DataConversion.toBigEndian(wfe.worksId)!!
                wfe.estimateId = DataConversion.toBigEndian(wfe.estimateId)!!
            }
        }

        // WorkflowItemMeasures
        job.workflowItemMeasures.forEach { jim ->
            jim.itemMeasureId = DataConversion.toBigEndian(jim.itemMeasureId)!!
            jim.measureGroupId = DataConversion.toBigEndian(jim.measureGroupId)!!
            jim.trackRouteId = DataConversion.toBigEndian(jim.trackRouteId)!!
        }

        // WorkflowJobSections
        job.workflowJobSections.forEach { js ->
            js.jobSectionId = DataConversion.toBigEndian(js.jobSectionId)!!
            js.projectSectionId = DataConversion.toBigEndian(js.projectSectionId)!!
            js.jobId = DataConversion.toBigEndian(js.jobId)
        }

        return job
    }

    private fun insertOrUpdateWorkflowJobInSQLite(job: WorkflowJobDTO?) {
        job?.let {
            updateWorkflowJobValuesAndInsertWhenNeeded(it)
        }
    }

    private fun updateWorkflowJobValuesAndInsertWhenNeeded(job: WorkflowJobDTO) {
        Coroutines.io {
            appDb.getJobDao().updateJob(
                trackRouteId = job.trackRouteId,
                actId = job.actId,
                jiNo = job.jiNo,
                jobId = job.jobId
            )

            job.workflowItemEstimates.forEach { workflowItemEstimate ->
                appDb.getJobItemEstimateDao().updateExistingJobItemEstimateWorkflow(
                    trackRouteId = workflowItemEstimate.trackRouteId,
                    actId = workflowItemEstimate.actId,
                    estimateId = workflowItemEstimate.estimateId
                )
                workflowItemEstimate.workflowEstimateWorks.forEach { workflowEstimateWork ->
                    appDb.getEstimateWorkDao().updateJobEstimateWorksWorkflow(
                        worksId = workflowEstimateWork.worksId,
                        estimateId = workflowEstimateWork.estimateId,
                        recordVersion = workflowEstimateWork.recordVersion,
                        recordSynchStateId = workflowEstimateWork.recordSynchStateId,
                        actId = workflowEstimateWork.actId,
                        trackRouteId = workflowEstimateWork.trackRouteId
                    )
                }
            }

            job.workflowItemMeasures.forEach { workflowItemMeasure ->
                appDb.getJobItemMeasureDao().updateWorkflowJobItemMeasure(
                    itemMeasureId = workflowItemMeasure.itemMeasureId,
                    trackRouteId = workflowItemMeasure.trackRouteId,
                    actId = workflowItemMeasure.actId,
                    measureGroupId = workflowItemMeasure.measureGroupId
                )

                job.workflowJobSections.forEach { jobSection ->
                    if (!appDb.getJobSectionDao().checkIfJobSectionExist(jobSection.jobSectionId)) {
                        appDb.getJobSectionDao().insertJobSection(jobSection)
                    } else {
                        appDb.getJobSectionDao().updateExistingJobSectionWorkflow(
                            jobSectionId = jobSection.jobSectionId,
                            projectSectionId = jobSection.projectSectionId,
                            jobId = jobSection.jobId,
                            startKm = jobSection.startKm,
                            endKm = jobSection.endKm,
                            recordVersion = jobSection.recordVersion,
                            recordSynchStateId = jobSection.recordSynchStateId
                        )
                    }
                }
            }
        }
    }

    private suspend fun uploadCreateJobImages(
        packageJob: JobDTO,
        activity: FragmentActivity
    ) {

        var jobCounter = 1
        val totalJobs = packageJob.jobItemEstimates.size
        packageJob.jobItemEstimates.map { jobItemEstimate ->
            val totalImages = jobItemEstimate.jobItemEstimatePhotos.size
            var imageCounter = 1
            jobItemEstimate.jobItemEstimatePhotos.map nextPhoto@{ estimatePhoto ->
                if (photoUtil.photoExist(estimatePhoto.filename)) {

                    uploadRrmImage(
                        activity = activity,
                        filename = estimatePhoto.filename,
                        photoQuality = PhotoQuality.HIGH,
                        imageCounter = imageCounter,
                        totalImages = totalImages
                    )

                    Timber.d("Estimate $jobCounter of $totalJobs - $imageCounter of $totalImages images uploaded")
                    imageCounter++
                } else {
                    val message = "${estimatePhoto.filename} could not be loaded"
                    Timber.e(IOException(message), message)
                    return@nextPhoto
                }
            }
            jobCounter++
        }

        if (totalJobs == 0) {
            Timber.d("No estimate jobs found.")
        }
    }

    private suspend fun uploadRrmImage(
        activity: FragmentActivity,
        filename: String,
        photoQuality: PhotoQuality,
        imageCounter: Int,
        totalImages: Int
    ) {

        val data: ByteArray = getData(filename, photoQuality)
        processImageUpload(
            filename,
            activity.getString(R.string.jpg),
            data,
            totalImages,
            imageCounter
        )
    }

    private suspend fun getData(
        filename: String,
        photoQuality: PhotoQuality
    ): ByteArray {
        val uri = photoUtil.getPhotoPathFromExternalDirectory(filename)
        val bitmap =
            photoUtil.getPhotoBitmapFromFile(uri, photoQuality)
        return photoUtil.getCompressedPhotoWithExifInfo(
            bitmap!!,
            filename
        )
    }

    private suspend fun processImageUpload(
        filename: String,
        extension: String,
        photo: ByteArray,
        totalImages: Int,
        imageCounter: Int
    ) {
        val imageData = JsonObject()
        imageData.addProperty("Filename", filename)
        imageData.addProperty("ImageByteArray", photoUtil.encode64Pic(photo))
        imageData.addProperty("ImageFileExtension", extension)
        Timber.d("Json Image: $imageData")

        val uploadImageResponse = apiRequest { api.uploadRrmImage(imageData) }
        uploadImageResponse.errorMessage?.let {
            val message = "Failed to upload image: $it"
            throw ServiceException(message)
        }
        if (totalImages <= imageCounter) {
            Timber.d("Processed $imageCounter of $totalImages images.")
        }
    }

    suspend fun moveJobToNextWorkflow(
        job: JobDTO,
        activity: FragmentActivity
    ) = withContext(dispatchers.io()) {

        if (job.trackRouteId == null) {
            throw IllegalStateException("Cannot workflow job ${job.jiNo}: TrackRouteId cannot be null")
        } else {
            job.trackRouteId = DataConversion.toLittleEndian(job.trackRouteId)
            val direction: Int = WorkflowDirection.NEXT.value
            val trackRouteId: String = job.trackRouteId!!
            val description: String =
                activity.resources.getString(R.string.submit_for_approval)

            val workflowMoveResponse = apiRequest {
                api.getWorkflowMove(
                    job.userId.toString(),
                    trackRouteId,
                    description,
                    direction
                )
            }

            workflowMoveResponse.errorMessage?.let {
                throw ServiceException("Local workflow failed : $it")
            }

            if (workflowMoveResponse.workflowJob == null) {
                throw NoDataException("Server returned empty workflow job.")
            }

            return@withContext workflowMoveResponse.workflowJob
        }
    }

    suspend fun getContractNoForId(contractVoId: String?): String {
        return withContext(dispatchers.io()) {
            appDb.getContractDao().getContractNoForId(contractVoId)
        }
    }

    fun findRealSectionStartKm(
        projectSectionDTO: ProjectSectionDTO,
        pointLocation: Double
    ): SectionMarker {
        val data = appDb.getProjectSectionDao().findRealSectionStartKm(
            projectSectionDTO.route,
            pointLocation
        )

        return data ?: SectionMarker("Start", 0.0)
    }

    suspend fun findRealSectionEndKm(
        projectSectionDTO: ProjectSectionDTO,
        pointLocation: Double
    ): SectionMarker {
        return withContext(dispatchers.default()) {
            appDb.getProjectSectionDao().findRealSectionEndKm(
                projectSectionDTO.route,
                pointLocation
            )
        }
    }

    suspend fun getProjectCodeForId(projectId: String?): String {
        return withContext(dispatchers.io()) {
            appDb.getProjectDao().getProjectCodeForId(projectId)
        }
    }

    @Transaction
    suspend fun backupJob(job: JobDTO) {
        return withContext(dispatchers.io()) {
            if (appDb.getJobDao().checkIfJobExist(job.jobId)) {
                appDb.getJobDao().updateJob(job)
            } else {
                appDb.getJobDao().insertOrUpdateJob(job)
            }
            appDb.getJobDao().getJobForJobId(job.jobId)
        }
    }

    fun checkIfJobSectionExistForJobAndProjectSection(jobId: String?, projectSectionId: String?): Boolean {
        return appDb.getJobSectionDao().checkIfJobSectionExistForJob(jobId, projectSectionId)
    }

    fun getContractSelectors(): List<ContractSelector> {
        return appDb.getContractDao().getContractSelectors()
    }

    fun getProjectSelectors(contractId: String): List<ProjectSelector> {
        return appDb.getProjectDao().getProjectSelectorsForContractId(contractId)
    }

    fun getValidEstimatesForJobId(jobId: String, actId: Int): List<JobItemEstimateDTO> {
        return appDb.getJobItemEstimateDao().getJobEstimationItemsForJobId(jobId, actId)
    }

    fun getProjectItemById(itemId: String?): ItemDTOTemp {
        return appDb.getItemDaoTemp().getProjectItemById(itemId!!)
    }

    suspend fun getEstimateById(estimateId: String): JobItemEstimateDTO = withContext(dispatchers.io()) {
        return@withContext appDb.getJobItemEstimateDao().getJobItemEstimateForEstimateId(estimateId)
    }

    @Transaction
    suspend fun backupEstimate(estimate: JobItemEstimateDTO): JobItemEstimateDTO = withContext(dispatchers.io()) {
        if (appDb.getJobItemEstimateDao().checkIfJobItemEstimateExist(estimate.estimateId)) {
            appDb.getJobItemEstimateDao().updateJobItemEstimate(estimate)
        } else {
            appDb.getJobItemEstimateDao().insertJobItemEstimate(estimate)
        }
        return@withContext appDb.getJobItemEstimateDao().getJobItemEstimateForEstimateId(estimate.estimateId)
    }

    suspend fun backupProjectItem(item: ItemDTOTemp): Long = withContext(dispatchers.io()) {
        return@withContext appDb.getItemDaoTemp().insertItems(item)
    }

    @Transaction
    suspend fun backupEstimatePhoto(photoDTO: JobItemEstimatesPhotoDTO):
        JobItemEstimatesPhotoDTO = withContext(dispatchers.io()) {
        if (appDb.getJobItemEstimatePhotoDao()
            .checkIfJobItemEstimatePhotoExistsByPhotoId(photoDTO.photoId)
        ) {
            appDb.getJobItemEstimatePhotoDao().updateJobItemEstimatePhoto(photoDTO)
        } else {
            appDb.getJobItemEstimatePhotoDao().insertJobItemEstimatePhoto(photoDTO)
        }
        return@withContext appDb.getJobItemEstimatePhotoDao().getJobItemEstimatePhoto(photoDTO.photoId)
    }

    suspend fun saveJobSection(jobSection: JobSectionDTO): JobSectionDTO? = withContext(dispatchers.io()) {
        appDb.getJobSectionDao().insertJobSection(jobSection)
        return@withContext appDb.getJobSectionDao().getJobSectionFromJobId(jobSection.jobId!!)
    }

    suspend fun getJobSectionByJobId(jobId: String): JobSectionDTO? = withContext(dispatchers.io()) {
        return@withContext appDb.getJobSectionDao().getJobSectionFromJobId(jobId)
    }

    suspend fun getJobEstimateIndexByItemAndJobId(
        itemId: String,
        jobId: String
    ): JobItemEstimateDTO? = withContext(dispatchers.io()) {
        return@withContext appDb.getJobItemEstimateDao().getJobEstimateIndexByItemAndJobId(itemId, jobId)
    }

    suspend fun eraseExistingPhoto(photoId: String) = withContext(dispatchers.io()) {
        appDb.getJobItemEstimatePhotoDao().deletePhotoById(photoId)
    }


    suspend fun getStructureTypes(): LiveData<List<JobTypeEntityDTO>> {
        return withContext(Dispatchers.IO) {
            appDb.getJobTypeDao().getAll()
        }
    }
}
