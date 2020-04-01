package za.co.xisystems.itis_rrm.data.repositories

//import sun.security.krb5.Confounder.bytes

import android.os.Build
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

// const val MINIMUM_INTERVALY = 3
class JobCreationDataRepository(
    private val api: BaseConnectionApi,
    private val Db: AppDatabase,
    private val prefs: PreferenceProvider
) : SafeApiRequest() {
    companion object {
        val TAG: String = JobCreationDataRepository::class.java.simpleName
    }


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
//        workflowJ2.observeForever {
//            saveWorkflowJob2(it)
//        }

        photoUpload.observeForever {
            sendMsg(it)
        }


    }


    suspend fun getUser(): LiveData<UserDTO> {
        return withContext(Dispatchers.IO) {
            Db.getUserDao().getuser()
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
//            Looper.prepare() // to be able to make toast
//        Toast.makeText(activity, "Error: WorkFlow Job is null", Toast.LENGTH_LONG).show()
            Log.e(TAG, " WorkFlow Job is null")
        }
    }


    suspend fun saveNewJob(newJob: JobDTO?) {
        Coroutines.io {
            if (newJob != null) {
                if (!Db.getJobDao().checkIfJobExist(newJob.JobId)) {
                    Db.getJobDao().insertOrUpdateJobs(newJob)
                }

            }
        }
    }

    suspend fun getSectionItems(): LiveData<SectionItemDTO> {
        return withContext(Dispatchers.IO) {
            Db.getSectionItemDao().getSectionItems()
        }
    }


    suspend fun getContracts(): LiveData<List<ContractDTO>> {
        return withContext(Dispatchers.IO) {
            Db.getContractDao().getAllContracts()
        }
    }

    suspend fun getContractProjects(contractId: String): LiveData<List<ProjectDTO>> {
        return withContext(Dispatchers.IO) {
            Db.getProjectDao().getAllProjectsByContract(contractId)
        }
    }

    suspend fun getAllSectionItem(): LiveData<List<SectionItemDTO>> {
        return withContext(Dispatchers.IO) {
            Db.getSectionItemDao().getAllSectionItems()
        }
    }

    suspend fun getAllItemsForSectionItem(
        sectionItemId: String,
        projectId: String
    ): LiveData<List<ProjectItemDTO>> {
        return withContext(Dispatchers.IO) {
            //            val projectId = DataConversion.toLittleEndian( Db.getProjectDao().getProjectId())
            Db.getProjectItemDao().getAllItemsForSectionItem(sectionItemId, projectId)
        }
    }

    suspend fun saveNewItem(newJobItem: ItemDTOTemp?) {
        Coroutines.io {
            if (newJobItem != null) {
                if (!Db.getItemDaoTemp().checkItemExistsItemId(newJobItem.itemId)) {

                    Db.getItemDaoTemp().insertItems(newJobItem)
                }
            }
        }
    }

    fun delete(item: ItemDTOTemp) {
        Coroutines.io {
            Db.getItemDaoTemp().deleteItem(item)
        }
    }


    fun deleteJobfromList(jobId: String) {
        Coroutines.io {
            Db.getJobDao().deleteJobForJobId(jobId)
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
            if (!Db.getJobDao().checkIfJobExist(newJobId)) {
//
            } else {
                Db.getJobDao().updateJoSecId(
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


    suspend fun getPointSectionData(projectId: String?): LiveData<SectionPointDTO> { //jobId,jobId: String,
        return withContext(Dispatchers.IO) {
            // Db.getSectionItemDao().getAllSectionItems()
            Db.getSectionPointDao().getPointSectionData(projectId)
        }
    }

    suspend fun getSectionByRouteSectionProject(
        sectionId: Int,
        linearId: String?,
        projectId: String?
    ): LiveData<String> {
        return withContext(Dispatchers.IO) {
            Db.getProjectSectionDao()
                .getSectionByRouteSectionProject(sectionId, linearId!!, projectId)
        }

    }

    suspend fun getSection(sectionId: String): LiveData<ProjectSectionDTO> {
        return withContext(Dispatchers.IO) {
            Db.getProjectSectionDao().getSection(sectionId)
        }
    }

    suspend fun getRouteSectionPoint(
        latitude: Double,
        longitude: Double,
        useR: String,
        projectId: String?,
        jobId: String,
        itemCode: ItemDTOTemp?
    ) {
        try {
            val distance = 0.50
            val buffer = 0
            val routeSectionPointResponse =
                apiRequest { api.getRouteSectionPoint(distance, buffer, latitude, longitude, useR) }

            if (routeSectionPointResponse == null) {
                Timber.e(NullPointerException("RouteSectionPoint is empty!"))
            }

            routeSectionPoint.postValue(
                routeSectionPointResponse.direction,
                routeSectionPointResponse.linearId,
                routeSectionPointResponse.pointLocation,
                routeSectionPointResponse.sectionId,
                projectId,
                jobId,
                itemCode
            )
        } catch (e: ApiException) {
            throw e
        } catch (e: NoInternetException) {
            Log.e(TAG, "No Internet Connection", e)
            throw e
        } catch (e: NoConnectivityException) {

            Log.e(TAG, "Service Host Unreachable", e)
            throw e
        }

    }

    suspend fun getAllProjectItems(projectId: String, jobId: String): LiveData<List<ItemDTOTemp>> {
        return withContext(Dispatchers.IO) {
            Db.getItemDaoTemp().getAllProjecItems(projectId, jobId)
        }
    }

    fun deleteItemList(jobId: String) {
        Coroutines.io {
            Db.getItemDaoTemp().deleteItemList(jobId)
        }
    }

    fun deleteItemFromList(itemId: String) {
        Coroutines.io {
            Db.getItemDaoTemp().deleteItemfromList(itemId)
        }
    }

    suspend fun submitJob(userId: Int, job: JobDTO, activity: FragmentActivity): String {

        val jobhead = JsonObject()
        val gson = Gson()
        val newjob = gson.toJson(job)
        val jsonElement: JsonElement = JsonParser().parse(newjob)
        jobhead.add("Job", jsonElement)
        jobhead.addProperty("UserId", userId)
        Timber.d("Json Job: $jobhead")

        val jobResponse = apiRequest { api.sendJobsForApproval(jobhead) }
        workflowJ2.postValue(jobResponse.workflowJob, job, activity)

        val messages = jobResponse.errorMessage
//          activity.getResources().getString(R.string.please_wait)
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
            Db.getJobDao().getJobForJobId(jobId)
        }
    }

    private fun setWorkflowJobBigEndianGuids(job: WorkflowJobDTO): WorkflowJobDTO? {
        job.actId = job.actId
        job.jobId = DataConversion.toBigEndian(job.jobId)
        job.trackRouteId = DataConversion.toBigEndian(job.trackRouteId)
        job.workflowItemEstimates?.map { jie ->
            jie.estimateId = DataConversion.toBigEndian(jie.estimateId)!!
            jie.trackRouteId = DataConversion.toBigEndian(jie.trackRouteId)!!
            jie.workflowEstimateWorks.map { wfe ->
                wfe.trackRouteId = DataConversion.toBigEndian(wfe.trackRouteId)!!
                wfe.worksId = DataConversion.toBigEndian(wfe.worksId)!!
                wfe.estimateId = DataConversion.toBigEndian(wfe.estimateId)!!
            }
        }

        // WorkflowItemMeasures
        job.workflowItemMeasures?.map { jim ->
            jim.itemMeasureId = DataConversion.toBigEndian(jim.itemMeasureId)!!
            jim.measureGroupId = DataConversion.toBigEndian(jim.measureGroupId)!!
            jim.trackRouteId = DataConversion.toBigEndian(jim.trackRouteId)!!
        }

        // WorkflowJobSections
        job.workflowJobSections?.map { js ->
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
            Db.getJobDao().updateJob(job.trackRouteId, job.actId, job.jiNo, job.jobId)

            if (job.workflowItemEstimates != null && job.workflowItemEstimates.size != 0) {
                for (jobItemEstimate in job.workflowItemEstimates) {
                    Db.getJobItemEstimateDao().updateExistingJobItemEstimateWorkflow(
                        jobItemEstimate.trackRouteId,
                        jobItemEstimate.actId,
                        jobItemEstimate.estimateId
                    )

                    if (jobItemEstimate.workflowEstimateWorks != null) {
                        for (jobEstimateWorks in jobItemEstimate.workflowEstimateWorks) {
                            if (Db.getEstimateWorkDao()
                                    .checkIfJobEstimateWorksExist(jobEstimateWorks.worksId)
                            ) {
                                // TODO: What were we planning to do here?
                                // Db.getEstimateWorkDao().insertJobEstimateWorks(jobEstimateWorks as JobEstimateWorksDTO)
                                Db.getEstimateWorkDao().updateJobEstimateWorksWorkflow(
                                    jobEstimateWorks.worksId,
                                    jobEstimateWorks.estimateId,
                                    jobEstimateWorks.recordVersion,
                                    jobEstimateWorks.recordSynchStateId,
                                    jobEstimateWorks.actId,
                                    jobEstimateWorks.trackRouteId
                                )
                            }
                        }


                    }
                }
            }

            if (job.workflowItemMeasures != null && job.workflowItemMeasures.size !== 0) {
                for (jobItemMeasure in job.workflowItemMeasures) {
                    Db.getJobItemMeasureDao().updateWorkflowJobItemMeasure(
                        jobItemMeasure.itemMeasureId,
                        jobItemMeasure.trackRouteId,
                        jobItemMeasure.actId,
                        jobItemMeasure.measureGroupId
                    )
                }
            }


            //  Place the Job Section, UPDATE OR CREATE
            if (job.workflowJobSections != null && job.workflowJobSections.size !== 0) {
                for (jobSection in job.workflowJobSections) {
                    if (!Db.getJobSectionDao().checkIfJobSectionExist(jobSection.jobSectionId))
                        Db.getJobSectionDao().insertJobSection(jobSection) else
                        Db.getJobSectionDao().updateExistingJobSectionWorkflow(
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
        }
    }


    private fun uploadCreateJobImages(packageJob: JobDTO, activity: FragmentActivity) {
        var imageCounter = 1
        val totalImages = 0

        if (packageJob.JobItemEstimates != null) {
            if (packageJob.JobItemEstimates!!.isEmpty()) {
//                progressView.toast("(job.getPrjJobItemEstimates() is empty")
//                progressView.dismissProgressDialog()
            } else {
                for (jobItemEstimate in packageJob.JobItemEstimates!!) {
                    if (jobItemEstimate.jobItemEstimatePhotos != null && jobItemEstimate.jobItemEstimatePhotos!!.size > 0) {
                        val photos: Array<JobItemEstimatesPhotoDTO> =
                            arrayOf(
                                jobItemEstimate.jobItemEstimatePhotos!![0],
                                jobItemEstimate.jobItemEstimatePhotos!![1]
                            )
                        for (jobItemEstimatePhoto in photos) {
//                        for (jobItemEstimatePhoto in jobItemEstimate.jobItemEstimatePhotos!!) {
                            if (PhotoUtil.photoExist(jobItemEstimatePhoto.filename)) {
                                Log.d("x-", "UploadRrImage $imageCounter")
                                uploadRrmImage(
                                    jobItemEstimatePhoto.filename,
                                    PhotoQuality.HIGH,
                                    imageCounter,
                                    totalImages,
                                    packageJob,
                                    activity
                                )
                                imageCounter++
                            } else {
//                                setProgressBarMessage(text)
//                                progressView.toast("Error: photo filename is empty!")
                                Log.d("x-", "Error: photo filename is empty!")
                            }
                        }
                    } else {
//                        progressView.toast("Error: photos are empty!")
                        Log.d("x-", "Error: photos are empty!")
//                        progressView.dismissProgressDialog()
                    }
                }
            }
        } else {
//            progressView.toast("Error: no job item estimates.")
            Log.d("x-", "Error: no job item estimates.")
//            progressView.dismissProgressDialog()
        }


    }

    private fun uploadRrmImage(
        filename: String,
        photoQuality: PhotoQuality,
        imageCounter: Int,
        totalImages: Int,
        packageJob: JobDTO,
        activity: FragmentActivity
    ) {

        val data: ByteArray = getData(filename, photoQuality, activity)
        processImageUpload(
            filename,
            activity.getString(R.string.jpg),
            data,
            totalImages,
            imageCounter,
            packageJob,
            activity
        )
    }


    private fun getData(
        filename: String, photoQuality: PhotoQuality, activity: FragmentActivity
    ): ByteArray {
        val uri = getPhotoPathFromExternalDirectory(activity.applicationContext, filename)
        val bitmap =
            PhotoUtil.getPhotoBitmapFromFile(activity.applicationContext, uri, photoQuality)
        return PhotoUtil.getCompressedPhotoWithExifInfo(
            activity.applicationContext,
            bitmap!!,
            filename
        )
    }


    private fun processImageUpload(
        filename: String,
        extension: String,
        photo: ByteArray,
        totalImages: Int,
        imageCounter: Int,
        packageJob: JobDTO,
        activity: FragmentActivity
    ) {

        Coroutines.io {
            val imageData = JsonObject()
            imageData.addProperty("Filename", filename)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                imageData.addProperty("ImageByteArray", Base64.getEncoder().encodeToString(photo))
            } else {
                // Generic Base64 utility
                imageData.addProperty(
                    "ImageByteArray",
                    android.util.Base64.encodeToString(photo, android.util.Base64.DEFAULT)
                )
            }
            imageData.addProperty("ImageFileExtension", extension)
            Timber.d("Json Image: $imageData")

            val uploadImageResponse = apiRequest { api.uploadRrmImage(imageData) }
            photoUpload.postValue(uploadImageResponse.errorMessage)
            if (totalImages <= imageCounter) {
                Log.e("Coroutines", "Json string $totalImages")
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
                Db.getItemDaoTemp().deleteItemList(job.JobId)

//                toDoListGroups.postValue(workflowMoveResponse.toDoListGroups)
//                workflows.postValue(workflowMoveResponse.toDoListGroups)
            }

        }
    }


    private fun <T> MutableLiveData<T>.postValue(
        direction: String,
        linearId: String,
        pointLocation: Double,
        sectionId: Int,
        projectId: String?,
        jobId: String?,
        item: ItemDTOTemp?
    ) {
        saveRouteSectionPoint(
            direction,
            linearId,
            pointLocation,
            sectionId,
            projectId,
            jobId,
            item
        )
    }

    private fun saveRouteSectionPoint(
        direction: String,
        linearId: String,
        pointLocation: Double,
        sectionId: Int,
        projectId: String?,
        jobId: String?,
        item: ItemDTOTemp?
    ) {
        if (linearId != null) {
            //Db.getProjectSectionDao().getSectionByRouteSectionProject(linearId, sectionId, direction, projectId)
//           activity?.toast(direction + linearId +  "$pointLocation"  + sectionId.toString()+ projectId )
            if (!Db.getSectionPointDao().checkSectionExists(sectionId, projectId, jobId)) {
                Db.getSectionPointDao()
                    .insertSection(direction, linearId, pointLocation, sectionId, projectId, jobId)
            }
            Db.getProjectSectionDao().updateSectionDirection(direction, projectId)
        }
    }


    private fun <T> MutableLiveData<T>.postValue(photo: String?, fileName: String) {
        return saveEstimatePhoto(photo, fileName)
    }

    fun saveEstimatePhoto(estimatePhoto: String?, fileName: String) {
        Coroutines.io {
            if (estimatePhoto != null) {
                PhotoUtil.createPhotoFolder(estimatePhoto, fileName)
//            PhotoUtil.createPhotofolder(fileName)
            } else {
                PhotoUtil.createPhotoFolder()
            }

        }

    }

    suspend fun getContractNoForId(contractVoId: String?): String {
        return withContext(Dispatchers.IO) {
            Db.getContractDao().getContractNoForId(contractVoId)
        }
    }

    suspend fun getProjectCodeForId(projectId: String?): String {
        return withContext(Dispatchers.IO) {
            Db.getProjectDao().getProjectCodeForId(projectId)
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


    private operator fun <T> LiveData<T>.not(): Boolean {
        return true
    }


}












































