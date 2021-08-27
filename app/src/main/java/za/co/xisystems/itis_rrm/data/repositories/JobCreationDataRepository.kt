package za.co.xisystems.itis_rrm.data.repositories

import android.os.Looper
import android.widget.Toast
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
import za.co.xisystems.itis_rrm.data.localDB.AppDatabase
import za.co.xisystems.itis_rrm.data.localDB.JobDataController
import za.co.xisystems.itis_rrm.data.localDB.entities.*
import za.co.xisystems.itis_rrm.data.localDB.views.SectionMarker
import za.co.xisystems.itis_rrm.data.network.BaseConnectionApi
import za.co.xisystems.itis_rrm.data.network.SafeApiRequest
import za.co.xisystems.itis_rrm.data.network.responses.UploadImageResponse
import za.co.xisystems.itis_rrm.domain.ContractSelector
import za.co.xisystems.itis_rrm.domain.ProjectSelector
import za.co.xisystems.itis_rrm.utils.Coroutines
import za.co.xisystems.itis_rrm.utils.DataConversion
import za.co.xisystems.itis_rrm.utils.PhotoUtil
import za.co.xisystems.itis_rrm.utils.enums.PhotoQuality
import za.co.xisystems.itis_rrm.utils.enums.WorkflowDirection
import java.io.IOException
import java.util.*

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
    private val photoUtil: PhotoUtil
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
        return withContext(Dispatchers.IO) {
            appDb.getUserDao().getUser()
        }
    }

    private fun sendMsg(uploadResponse: String?) {
        val response: UploadImageResponse? = null
        if (uploadResponse.isNullOrEmpty()) {
            jobDataController?.setMsg(response!!.errorMessage)
        }
    }

    private fun saveWorkflowJob(workflowJob: WorkflowJobDTO?) {
        if (workflowJob != null) {
            val job = setWorkflowJobBigEndianGuids(workflowJob)
            insertOrUpdateWorkflowJobInSQLite(job)
        } else {
            val message = "Workflow job is null."
            Timber.e(
                java.lang.NullPointerException(message),
                message
            )
        }
    }

    fun saveNewJob(newJob: JobDTO?) {
        Coroutines.io {
            if (newJob != null && !appDb.getJobDao().checkIfJobExist(newJob.jobId)) {
                appDb.getJobDao().insertOrUpdateJob(newJob)
            }
        }
    }

    suspend fun getSectionItems(): LiveData<List<SectionItemDTO>> {
        return withContext(Dispatchers.Default) { appDb.getSectionItemDao().getSectionItems() }
    }

    suspend fun getContracts(): LiveData<List<ContractDTO>> {
        return withContext(Dispatchers.Default) {
            appDb.getContractDao().getAllContracts()
        }
    }

    suspend fun getContractProjects(contractId: String): LiveData<List<ProjectDTO>> {
        return withContext(Dispatchers.Default) {
            appDb.getProjectDao().getAllProjectsByContract(contractId)
        }
    }

    suspend fun getAllSectionItemsForProject(projectId: String): LiveData<List<SectionItemDTO>> {
        return withContext(Dispatchers.IO) {
            appDb.getSectionItemDao().getFilteredSectionItems(projectId)
        }
    }

    suspend fun getAllItemsForSectionItemByProject(
        sectionItemId: String,
        projectId: String
    ): LiveData<List<ProjectItemDTO>> {
        return withContext(Dispatchers.IO) {
            appDb.getProjectItemDao().getAllItemsForSectionItemByProject(sectionItemId, projectId)
        }
    }

    suspend fun saveNewItem(newJobItem: ItemDTOTemp?) {
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

    fun updateNewJob(
        newJobId: String,
        startKM: Double,
        endKM: Double,
        sectionId: String,
        newJobItemEstimatesList: ArrayList<JobItemEstimateDTO>,
        jobItemSectionArrayList: ArrayList<JobSectionDTO>
    ) {
        Coroutines.io {
            if (appDb.getJobDao().checkIfJobExist(newJobId)) {
                appDb.getJobDao().updateJoSecId(
                    newJobId,
                    startKM,
                    endKM,
                    sectionId,
                    newJobItemEstimatesList,
                    jobItemSectionArrayList
                )
            }
        }
    }

    suspend fun getPointSectionData(projectId: String): SectionPointDTO {
        return withContext(Dispatchers.IO) {
            appDb.getSectionPointDao().getPointSectionData(projectId)
        }
    }

    suspend fun getSectionByRouteSectionProject(
        sectionId: String,
        linearId: String?,
        projectId: String?,
        pointLocation: Double
    ): String? {
        return withContext(Dispatchers.IO) {
            appDb.getProjectSectionDao()
                .getSectionByRouteSectionProject(sectionId, linearId!!, projectId!!, pointLocation)
        }
    }

    suspend fun getSection(sectionId: String): LiveData<ProjectSectionDTO> {
        return withContext(Dispatchers.IO) {
            appDb.getProjectSectionDao().getSection(sectionId)
        }
    }

    @Suppress("MagicNumber")
    suspend fun getRouteSectionPoint(
        latitude: Double,
        longitude: Double,
        useR: String,
        projectId: String?,
        jobId: String
    ): String? {

        val distance = 0.5
        val inBuffer = 1.0
        val routeSectionPointResponse =
            apiRequest { api.getRouteSectionPoint(distance, inBuffer, latitude, longitude, useR) }

        with(routeSectionPointResponse) {
            Timber.d("$routeSectionPointResponse")

            if (!errorMessage.isNullOrBlank()) {
                Timber.e("Could not validate photo location: $errorMessage")
            }

            return if (linearId.contains("xxx" as CharSequence, ignoreCase = true) ||
                bufferLocation.contains("xxx" as CharSequence, ignoreCase = true) ||
                !errorMessage.isNullOrBlank()
            ) {
                "xxxxxx"
            } else {
                postRouteSection(
                    direction = direction,
                    linearId = linearId,
                    pointLocation = pointLocation,
                    sectionId = sectionId,
                    projectId = projectId,
                    jobId = jobId
                )
            }
        }
    }

    suspend fun getAllProjectItems(projectId: String, jobId: String): LiveData<List<ItemDTOTemp>> {
        return withContext(Dispatchers.IO) {
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
        return withContext(Dispatchers.IO) {
            val itemsDeleted = appDb.getItemDaoTemp().deleteItemFromList(itemId)
            val estimatesDeleted =
                estimateId?.let {
                    appDb.getJobItemEstimateDao()
                        .deleteJobItemEstimateByEstimateId(it)
                } ?: 0
            itemsDeleted + estimatesDeleted
        }
    }

    suspend fun submitJob(userId: Int, job: JobDTO, activity: FragmentActivity): String {

        val jobData = JsonObject()
        val gson = Gson()
        val newJob = gson.toJson(job)
        val jsonElement: JsonElement = JsonParser.parseString(newJob)
        jobData.add("Job", jsonElement)
        jobData.addProperty("UserId", userId)
        Timber.i("Json Job: $jobData")

        val jobResponse = apiRequest { api.sendJobsForApproval(jobData) }
        postWorkflowJob(jobResponse.workflowJob, job, activity)

        val messages = jobResponse.errorMessage

        return withContext(Dispatchers.IO) {
            messages ?: ""
        }
    }

    private fun postWorkflowJob(
        workflowJob: WorkflowJobDTO?,
        job: JobDTO,
        activity: FragmentActivity
    ) {
        Coroutines.io {
            if (workflowJob != null) {
                val createJob = setWorkflowJobBigEndianGuids(workflowJob)
                insertOrUpdateWorkflowJobInSQLite(createJob)
                uploadCreateJobImages(
                    packageJob = job,
                    activity = activity
                )
                val myJob = getUpdatedJob(DataConversion.toBigEndian(job.jobId)!!)
                moveJobToNextWorkflow(myJob, activity)
            }
        }
    }

    suspend fun getUpdatedJob(jobId: String): JobDTO {
        return withContext(Dispatchers.IO) {
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

    suspend fun uploadCreateJobImages(packageJob: JobDTO, activity: FragmentActivity) = withContext(Dispatchers.IO) {

        var jobCounter = 1
        val totalJobs = packageJob.jobItemEstimates.size
        packageJob.jobItemEstimates.map { jobItemEstimate ->
            val totalImages = jobItemEstimate.jobItemEstimatePhotos.size
            var imageCounter = 1
            jobItemEstimate.jobItemEstimatePhotos.map { estimatePhoto ->
                if (photoUtil.photoExist(estimatePhoto.filename)) {

                    uploadRrmImage(
                        activity = activity,
                        filename = estimatePhoto.filename,
                        photoQuality = PhotoQuality.HIGH,
                        imageCounter = imageCounter,
                        totalImages = totalImages
                    )

                    Timber.d("Job $jobCounter of $totalJobs - $imageCounter of $totalImages images uploaded")
                    imageCounter++
                } else {
                    val message = "${estimatePhoto.filename} could not be loaded"
                    Timber.e(IOException(message), message)
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

    private fun processImageUpload(
        filename: String,
        extension: String,
        photo: ByteArray,
        totalImages: Int,
        imageCounter: Int
    ) {
        Coroutines.api {
            val imageData = JsonObject()
            imageData.addProperty("Filename", filename)
            imageData.addProperty("ImageByteArray", photoUtil.encode64Pic(photo))
            imageData.addProperty("ImageFileExtension", extension)
            Timber.d("Json Image: $imageData")

            val uploadImageResponse = apiRequest { api.uploadRrmImage(imageData) }
            photoUpload.postValue(uploadImageResponse.errorMessage)
            if (totalImages <= imageCounter) {
                Timber.d("Processed $imageCounter of $totalImages images.")
            }
        }
    }

    private fun moveJobToNextWorkflow(
        job: JobDTO,
        activity: FragmentActivity
    ) {

        if (job.trackRouteId == null) {
            Looper.prepare() // to be able to make toast
            Toast.makeText(activity, "Error: trackRouteId is null", Toast.LENGTH_LONG).show()
        } else {
            job.trackRouteId = DataConversion.toLittleEndian(job.trackRouteId)
            val direction: Int = WorkflowDirection.NEXT.value
            val trackRouteId: String = job.trackRouteId!!
            val description: String =
                activity.resources.getString(R.string.submit_for_approval)

            Coroutines.io {
                val workflowMoveResponse = apiRequest {
                    api.getWorkflowMove(
                        job.userId.toString(),
                        trackRouteId,
                        description,
                        direction
                    )
                }
                workflowMoveResponse.workflowJob?.let { workflowJob ->
                    workflowJobs.postValue(workflowJob)
                    appDb.getItemDaoTemp().deleteItemList(job.jobId)
                }
            }
        }
    }

    private fun postRouteSection(
        direction: String,
        linearId: String?,
        pointLocation: Double,
        sectionId: Int,
        projectId: String?,
        jobId: String?
    ): String? {
        return saveRouteSectionPoint(
            direction,
            linearId,
            pointLocation,
            sectionId,
            projectId,
            jobId
        )
    }

    private fun saveRouteSectionPoint(
        direction: String,
        linearId: String?,
        pointLocation: Double,
        sectionId: Int,
        projectId: String?,
        jobId: String?
    ): String? {
        val name = object {}.javaClass.enclosingMethod?.name
        Timber.d("x -> $name")
        if (linearId != null) {
            if (!appDb.getSectionPointDao().checkSectionExists(sectionId, projectId, jobId, pointLocation)) {
                appDb.getSectionPointDao()
                    .insertSection(direction, linearId, pointLocation, sectionId, projectId, jobId)
            }
            appDb.getProjectSectionDao().updateSectionDirection(direction, projectId)
        }

        var projectSectionId = appDb.getProjectSectionDao()
            .getSectionByRouteSectionProject(sectionId.toString(), linearId, projectId, pointLocation)

        // Deal with SectionDirection combinations.
        // S.McDonald 2021/05/14
        if (projectSectionId.isNullOrBlank()) {
            projectSectionId = appDb.getProjectSectionDao().getSectionByRouteSectionProject(
                sectionId.toString().plus(direction),
                linearId,
                projectId,
                pointLocation
            )
        }
        Timber.d("ProjectSectionId: $projectSectionId")
        return projectSectionId
    }

    suspend fun getContractNoForId(contractVoId: String?): String {
        return withContext(Dispatchers.IO) {
            appDb.getContractDao().getContractNoForId(contractVoId)
        }
    }

    suspend fun findRealSectionStartKm(
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
        return withContext(Dispatchers.Default) {
            appDb.getProjectSectionDao().findRealSectionEndKm(
                projectSectionDTO.route,
                pointLocation
            )
        }
    }

    suspend fun getProjectCodeForId(projectId: String?): String {
        return withContext(Dispatchers.IO) {
            appDb.getProjectDao().getProjectCodeForId(projectId)
        }
    }

    suspend fun backupJob(job: JobDTO) {
        return withContext(Dispatchers.IO) {
            appDb.getJobDao().insertOrUpdateJob(job)
        }
    }

    suspend fun checkIfJobSectionExistForJobAndProjectSection(jobId: String?, projectSectionId: String?): Boolean {
        return appDb.getJobSectionDao().checkIfJobSectionExistForJob(jobId, projectSectionId)
    }

    fun getContractSelectors(): List<ContractSelector> {
        return appDb.getContractDao().getContractSelectors()
    }

    fun getProjectSelectors(contractId: String): List<ProjectSelector> {
        return appDb.getProjectDao().getProjectSelectorsForContractId(contractId)
    }

    fun getValidEstimatesForJobId(jobId: String, actId: Int): List<JobItemEstimateDTO> {
        return appDb.getJobItemEstimateDao().getJobEstimationItemsForJobId(jobId, actId).value.orEmpty()
    }

    suspend fun getProjectItemById(itemId: String?): ItemDTOTemp? {
        return appDb.getItemDaoTemp().getProjectItemById(itemId!!)
    }

    suspend fun getEstimateById(estimateId: String): JobItemEstimateDTO = withContext(Dispatchers.IO) {
        return@withContext appDb.getJobItemEstimateDao().getJobItemEstimateForEstimateId(estimateId)
    }

    @Transaction
    suspend fun backupEstimate(estimate: JobItemEstimateDTO) = withContext(Dispatchers.IO) {
        appDb.getJobItemEstimateDao().insertJobItemEstimate(estimate)
        return@withContext getEstimateById(estimate.estimateId)
    }

    suspend fun backupProjectItem(item: ItemDTOTemp): Long = withContext(Dispatchers.IO) {
        return@withContext appDb.getItemDaoTemp().insertItems(item)
    }

    @Transaction
    suspend fun backupEstimatePhoto(photoDTO: JobItemEstimatesPhotoDTO):
            LiveData<List<JobItemEstimatesPhotoDTO>> = withContext(Dispatchers.IO) {
        appDb.getJobItemEstimatePhotoDao().insertJobItemEstimatePhoto(photoDTO)
        return@withContext appDb.getJobItemEstimatePhotoDao().getJobEstimationItemsPhoto(photoDTO.estimateId)
    }

    suspend fun saveJobSection(jobSection: JobSectionDTO) = withContext(Dispatchers.IO) {
        return@withContext appDb.getJobSectionDao().insertJobSection(jobSection)
    }
}
