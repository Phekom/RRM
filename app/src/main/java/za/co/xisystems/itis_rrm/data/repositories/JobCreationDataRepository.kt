package za.co.xisystems.itis_rrm.data.repositories

// import sun.security.krb5.Confounder.bytes

import android.os.Looper
import android.widget.Toast
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
import za.co.xisystems.itis_rrm.data.localDB.AppDatabase
import za.co.xisystems.itis_rrm.data.localDB.JobDataController
import za.co.xisystems.itis_rrm.data.localDB.entities.*
import za.co.xisystems.itis_rrm.data.network.BaseConnectionApi
import za.co.xisystems.itis_rrm.data.network.SafeApiRequest
import za.co.xisystems.itis_rrm.data.network.responses.UploadImageResponse
import za.co.xisystems.itis_rrm.data.preferences.PreferenceProvider
import za.co.xisystems.itis_rrm.utils.Coroutines
import za.co.xisystems.itis_rrm.utils.DataConversion
import za.co.xisystems.itis_rrm.utils.PhotoUtil
import za.co.xisystems.itis_rrm.utils.PhotoUtil.getPhotoPathFromExternalDirectory
import za.co.xisystems.itis_rrm.utils.enums.PhotoQuality
import za.co.xisystems.itis_rrm.utils.enums.WorkflowDirection
import java.io.IOException
import java.util.*

/**
 * Created by Francis Mahlava on 2019/11/28.
 */

class JobCreationDataRepository(
    private val api: BaseConnectionApi,
    private val appDb: AppDatabase,
    private val prefs: PreferenceProvider
) : SafeApiRequest() {

    private val workflowJobs = MutableLiveData<WorkflowJobDTO>()
    private val jobDataController: JobDataController? = null
    private val workflowJ = MutableLiveData<WorkflowJobDTO>()
    private val workflowJ2 = MutableLiveData<WorkflowJobDTO>()
    private val photoUpload = MutableLiveData<String>()
    private val works = MutableLiveData<String>()
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

    suspend fun getJobSection(jobId: String): JobSectionDTO? {
        return withContext(Dispatchers.IO) {
            appDb.getJobSectionDao().getJobSectionFromJobId(jobId)
        }
    }

    suspend fun getUser(): LiveData<UserDTO> {
        return withContext(Dispatchers.IO) {
            appDb.getUserDao().getUser()
        }
    }

    private fun sendMsg(uploadResponse: String?) {
        val response: UploadImageResponse? = null
        if (uploadResponse.isNullOrEmpty())
            jobDataController?.setMsg(response!!.errorMessage)
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
            if (newJob != null && !appDb.getJobDao().checkIfJobExist(newJob.JobId)) {
                appDb.getJobDao().insertOrUpdateJobs(newJob)
            }
        }
    }

    suspend fun getSectionItems(): LiveData<List<SectionItemDTO>> {
        return withContext(Dispatchers.IO) {
            appDb.getSectionItemDao().getSectionItems()
        }
    }

    suspend fun getContracts(): LiveData<List<ContractDTO>> {
        return withContext(Dispatchers.IO) {
            appDb.getContractDao().getAllContracts()
        }
    }

    suspend fun getProjects(): LiveData<List<ProjectDTO>> {
        return withContext(Dispatchers.IO) {
            appDb.getProjectDao().getAllProjects()
        }
    }

    suspend fun getContractProjects(contractId: String): LiveData<List<ProjectDTO>> {
        return withContext(Dispatchers.IO) {
            appDb.getProjectDao().getAllProjectsByContract(contractId)
        }
    }

    suspend fun getAllSectionItems(): LiveData<List<SectionItemDTO>> {
        return withContext(Dispatchers.IO) {
            appDb.getSectionItemDao().getAllSectionItems()
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
            if (!appDb.getJobDao().checkIfJobExist(newJobId)) {
//
            } else {
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

    suspend fun getPointSectionData(projectId: String?): LiveData<SectionPointDTO> { // jobId,jobId: String,
        return withContext(Dispatchers.IO) {
            // Db.getSectionItemDao().getAllSectionItems()
            appDb.getSectionPointDao().getPointSectionData(projectId)
        }
    }

    suspend fun getSectionByRouteSectionProject(
        sectionId: Int,
        linearId: String?,
        projectId: String?
    ): LiveData<String?> {
        return withContext(Dispatchers.IO) {
            appDb.getProjectSectionDao()
                .getSectionByRouteSectionProject(sectionId.toString(), linearId!!, projectId!!)
        }
    }

    suspend fun getSection(sectionId: String): LiveData<ProjectSectionDTO> {
        return withContext(Dispatchers.IO) {
            appDb.getProjectSectionDao().getSection(sectionId)
        }
    }

    suspend fun getRouteSectionPoint(
        latitude: Double,
        longitude: Double,
        useR: String,
        projectId: String?,
        jobId: String
    ): LiveData<String?> {

        val distance = 1
        val buffer = 0
        val routeSectionPointResponse =
            apiRequest { api.getRouteSectionPoint(distance, buffer, latitude, longitude, useR) }

        Timber.d("$routeSectionPointResponse")

        routeSectionPoint.postValue(
            direction = routeSectionPointResponse.direction,
            linearId = routeSectionPointResponse.linearId,
            pointLocation = routeSectionPointResponse.pointLocation,
            sectionId = routeSectionPointResponse.sectionId,
            projectId = projectId,
            jobId = jobId
        )

        return withContext(Dispatchers.IO) {
            appDb.getProjectSectionDao().getSectionByRouteSectionProject(
                routeSectionPointResponse.sectionId.toString(),
                routeSectionPointResponse.linearId, projectId
            )
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

    fun deleteItemFromList(itemId: String) {
        Coroutines.io {
            appDb.getItemDaoTemp().deleteItemfromList(itemId)
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
        workflowJ2.postValue(jobResponse.workflowJob, job, activity)

        val messages = jobResponse.errorMessage

        return withContext(Dispatchers.IO) {
            messages
        }
    }

    private fun <T> MutableLiveData<T>.postValue(
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
                val myJob = getUpdatedJob(DataConversion.toBigEndian(job.JobId)!!)
                moveJobToNextWorkflow(myJob, activity)
            }
        }
    }

    suspend fun getUpdatedJob(jobId: String): JobDTO {
        return withContext(Dispatchers.IO) {
            appDb.getJobDao().getJobForJobId(jobId)
        }
    }

    private fun setWorkflowJobBigEndianGuids(job: WorkflowJobDTO): WorkflowJobDTO? {

        // Job + WorkflowItems + EstimateWorks
        job.jobId = DataConversion.toBigEndian(job.jobId)
        job.trackRouteId = DataConversion.toBigEndian(job.trackRouteId)
        job.workflowItemEstimates?.forEach { jie ->
            jie.estimateId = DataConversion.toBigEndian(jie.estimateId)!!
            jie.trackRouteId = DataConversion.toBigEndian(jie.trackRouteId)!!
            jie.workflowEstimateWorks.forEach { wfe ->
                wfe.trackRouteId = DataConversion.toBigEndian(wfe.trackRouteId)!!
                wfe.worksId = DataConversion.toBigEndian(wfe.worksId)!!
                wfe.estimateId = DataConversion.toBigEndian(wfe.estimateId)!!
            }
        }

        // WorkflowItemMeasures
        job.workflowItemMeasures?.forEach { jim ->
            jim.itemMeasureId = DataConversion.toBigEndian(jim.itemMeasureId)!!
            jim.measureGroupId = DataConversion.toBigEndian(jim.measureGroupId)!!
            jim.trackRouteId = DataConversion.toBigEndian(jim.trackRouteId)!!
        }

        // WorkflowJobSections
        job.workflowJobSections?.forEach { js ->
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

            job.workflowItemEstimates?.forEach { workflowItemEstimate ->
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

            job.workflowItemMeasures?.forEach { workflowItemMeasure ->
                appDb.getJobItemMeasureDao().updateWorkflowJobItemMeasure(
                    itemMeasureId = workflowItemMeasure.itemMeasureId,
                    trackRouteId = workflowItemMeasure.trackRouteId,
                    actId = workflowItemMeasure.actId,
                    measureGroupId = workflowItemMeasure.measureGroupId
                )
            }

            job.workflowJobSections?.forEach { jobSection ->
                if (!appDb.getJobSectionDao().checkIfJobSectionExist(jobSection.jobSectionId))
                    appDb.getJobSectionDao().insertJobSection(jobSection) else
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

    private fun uploadCreateJobImages(packageJob: JobDTO, activity: FragmentActivity) {

        var jobCounter = 1
        val totalJobs = packageJob.JobItemEstimates?.size ?: 0

        packageJob.JobItemEstimates?.map { jobItemEstimate ->
            val totalImages = jobItemEstimate.jobItemEstimatePhotos?.size ?: 0
            var imageCounter = 1
            jobItemEstimate.jobItemEstimatePhotos?.map { estimatePhoto ->
                if (PhotoUtil.photoExist(estimatePhoto.filename)) {

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

    private fun uploadRrmImage(
        activity: FragmentActivity,
        filename: String,
        photoQuality: PhotoQuality,
        imageCounter: Int,
        totalImages: Int
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
        Coroutines.api {
            val imageData = JsonObject()
            imageData.addProperty("Filename", filename)
            imageData.addProperty("ImageByteArray", PhotoUtil.encode64Pic(photo))
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

        if (job.TrackRouteId == null) {
            Looper.prepare() // to be able to make toast
            Toast.makeText(activity, "Error: trackRouteId is null", Toast.LENGTH_LONG).show()
        } else {
            job.TrackRouteId = DataConversion.toLittleEndian(job.TrackRouteId)
            val direction: Int = WorkflowDirection.NEXT.value
            val trackRouteId: String = job.TrackRouteId!!
            val description: String =
                activity.resources.getString(R.string.submit_for_approval)

            Coroutines.io {
                val workflowMoveResponse = apiRequest {
                    api.getWorkflowMove(
                        job.UserId.toString(),
                        trackRouteId,
                        description,
                        direction
                    )
                }
                workflowJobs.postValue(workflowMoveResponse.workflowJob)
                appDb.getItemDaoTemp().deleteItemList(job.JobId)
            }
        }
    }

    private fun <T> MutableLiveData<T>.postValue(
        direction: String,
        linearId: String?,
        pointLocation: Double,
        sectionId: Int,
        projectId: String?,
        jobId: String?
    ): LiveData<String?> {
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
    ): LiveData<String?> {
        val name = object {}.javaClass.enclosingMethod?.name
        Timber.d("x -> $name")
        if (linearId != null) {

            if (!appDb.getSectionPointDao().checkSectionExists(sectionId, projectId, jobId)) {
                appDb.getSectionPointDao()
                    .insertSection(direction, linearId, pointLocation, sectionId, projectId, jobId)
            }
            appDb.getProjectSectionDao().updateSectionDirection(direction, projectId)
        }

        return appDb.getProjectSectionDao()
            .getSectionByRouteSectionProject(sectionId.toString(), linearId, projectId)
    }

    private fun <T> MutableLiveData<T>.postValue(photo: String?, fileName: String) {
        return saveEstimatePhoto(photo, fileName)
    }

    fun saveEstimatePhoto(estimatePhoto: String?, fileName: String) {
        Coroutines.io {
            if (estimatePhoto != null) {
                PhotoUtil.createPhotoFolder(estimatePhoto, fileName)
            } else {
                PhotoUtil.createPhotoFolder()
            }
        }
    }

    suspend fun getContractNoForId(contractVoId: String?): String {
        return withContext(Dispatchers.IO) {
            appDb.getContractDao().getContractNoForId(contractVoId)
        }
    }

    suspend fun getProjectCodeForId(projectId: String?): String {
        return withContext(Dispatchers.IO) {
            appDb.getProjectDao().getProjectCodeForId(projectId)
        }
    }

    private fun JobItemMeasureDTO.setSelectedItemUom(selectedItemUom: String?) {
        this.selectedItemUom = selectedItemUom
    }

    private fun JobItemMeasureDTO.setMeasureDate(measureDate: Date) {
        this.measureDate = measureDate.toString()
    }

    private fun JobItemMeasureDTO.setLineAmount(lineAmount: Double) {
        this.lineAmount = lineAmount
    }

    private fun JobItemMeasureDTO.setCpa(cpa: Int) {
        this.cpa = cpa
    }

    private fun JobItemMeasureDTO.setRecordSynchStateId(recordSynchStateId: Int) {
        this.recordSynchStateId = recordSynchStateId
    }

    private fun JobItemMeasureDTO.setRecordVersion(recordVersion: Int) {
        this.recordVersion = recordVersion
    }

    private fun JobItemMeasureDTO.setJobDirectionId(jobDirectionId: Int) {
        this.jobDirectionId = jobDirectionId
    }

    private fun JobItemMeasureDTO.setEndKm(endKm: Double) {
        this.endKm = endKm
    }

    private fun JobItemMeasureDTO.setStartKm(startKm: Double) {
        this.startKm = startKm
    }

    private fun JobItemMeasureDTO.setLineRate(lineRate: Double) {
        this.lineRate = lineRate
    }

    private fun JobItemMeasureDTO.setQty(quantity: Double) {
        this.qty = quantity
    }

    private fun JobItemMeasurePhotoDTO.setPhotoPath(photoPath: String) {
        this.photoPath = photoPath
    }

    private fun JobItemEstimatesPhotoDTO.setIsPhotoStart(photoStart: Boolean) {
        this.is_PhotoStart = photoStart
    }

    private fun JobItemEstimatesPhotoDTO.setPhotoPath(photoPath: String) {
        this.photoPath = photoPath
    }

    private fun JobDTO.setRoute(route: String?) {
        this.Route = route!!
    }

    private fun JobDTO.setDescr(descr: String?) {
        this.Descr = descr
    }

    private fun JobDTO.setJobId(toBigEndian: String?) {
        this.JobId = toBigEndian!!
    }

    private fun JobDTO.setProjectId(toBigEndian: String?) {
        this.ProjectId = toBigEndian!!
    }

    private fun JobDTO.setContractVoId(toBigEndian: String?) {
        this.ContractVoId = toBigEndian!!
    }

    private fun JobDTO.setTrackRouteId(toBigEndian: String?) {
        this.TrackRouteId = toBigEndian!!
    }

    private fun JobSectionDTO.setJobSectionId(toBigEndian: String?) {
        this.jobSectionId = toBigEndian!!
    }

    private fun JobSectionDTO.setProjectSectionId(toBigEndian: String?) {
        this.projectSectionId = toBigEndian!!
    }

    private fun JobSectionDTO.setJobId(toBigEndian: String?) {
        this.jobId = toBigEndian!!
    }

    private fun JobItemMeasurePhotoDTO.setItemMeasureId(toBigEndian: String?) {
        this.itemMeasureId = toBigEndian
    }

    private fun JobItemMeasurePhotoDTO.setPhotoId(toBigEndian: String?) {
        this.photoId = toBigEndian!!
    }

    private fun JobEstimateWorksPhotoDTO.setPhotoId(toBigEndian: String?) {
        this.photoId = toBigEndian!!
    }

    private fun JobEstimateWorksPhotoDTO.setWorksId(toBigEndian: String?) {
        this.worksId = toBigEndian!!
    }

    private fun JobItemMeasureDTO.setItemMeasureId(toBigEndian: String?) {
        this.itemMeasureId = toBigEndian
    }

    private fun JobItemMeasureDTO.setJobId(toBigEndian: String?) {
        this.jobId = toBigEndian
    }

    private fun JobItemMeasureDTO.setProjectItemId(toBigEndian: String?) {
        this.projectItemId = toBigEndian
    }

    private fun JobItemMeasureDTO.setMeasureGroupId(toBigEndian: String?) {
        this.measureGroupId = toBigEndian
    }

    private fun JobItemMeasureDTO.setEstimateId(toBigEndian: String?) {
        this.estimateId = toBigEndian
    }

    private fun JobItemMeasureDTO.setProjectVoId(toBigEndian: String?) {
        this.projectVoId = toBigEndian
    }

    private fun JobItemMeasureDTO.setTrackRouteId(toBigEndian: String?) {
        this.trackRouteId = toBigEndian
    }

    private fun JobEstimateWorksDTO.setTrackRouteId(toBigEndian: String?) {
        this.trackRouteId = toBigEndian!!
    }

    private fun JobEstimateWorksDTO.setEstimateId(toBigEndian: String?) {
        this.estimateId = toBigEndian!!
    }

    private fun JobEstimateWorksDTO.setWorksId(toBigEndian: String?) {
        this.worksId = toBigEndian!!
    }

    private fun JobItemEstimatesPhotoDTO.setEstimateId(toBigEndian: String?) {
        this.estimateId = toBigEndian!!
    }

    private fun JobItemEstimatesPhotoDTO.setPhotoId(toBigEndian: String?) {
        this.photoId = toBigEndian!!
    }

    private fun JobItemEstimateDTO.setProjectVoId(toBigEndian: String?) {
        this.projectVoId = toBigEndian
    }

    private fun JobItemEstimateDTO.setTrackRouteId(toBigEndian: String?) {
        this.trackRouteId = toBigEndian!!
    }

    private fun JobItemEstimateDTO.setProjectItemId(toBigEndian: String?) {
        this.projectItemId = toBigEndian
    }

    private fun JobItemEstimateDTO.setJobId(toBigEndian: String?) {
        this.jobId = toBigEndian!!
    }

    private fun JobItemEstimateDTO.setEstimateId(toBigEndian: String?) {
        this.estimateId = toBigEndian!!
    }
}
