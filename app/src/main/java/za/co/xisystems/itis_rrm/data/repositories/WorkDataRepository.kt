package za.co.xisystems.itis_rrm.data.repositories

//import sun.security.krb5.Confounder.bytes

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
import za.co.xisystems.itis_rrm.custom.errors.ApiException
import za.co.xisystems.itis_rrm.custom.errors.NoDataException
import za.co.xisystems.itis_rrm.data.localDB.AppDatabase
import za.co.xisystems.itis_rrm.data.localDB.entities.*
import za.co.xisystems.itis_rrm.data.network.BaseConnectionApi
import za.co.xisystems.itis_rrm.data.network.SafeApiRequest
import za.co.xisystems.itis_rrm.utils.Coroutines
import za.co.xisystems.itis_rrm.utils.DataConversion
import za.co.xisystems.itis_rrm.utils.PhotoUtil
import za.co.xisystems.itis_rrm.utils.enums.PhotoQuality
import za.co.xisystems.itis_rrm.utils.enums.WorkflowDirection
import java.util.*


/**
 * Created by Francis Mahlava on 2019/11/28.
 */

class WorkDataRepository(
    private val api: BaseConnectionApi,
    private val Db: AppDatabase
) : SafeApiRequest() {
    companion object {
        val TAG: String = WorkDataRepository::class.java.simpleName
    }


    val workflowJobs = MutableLiveData<WorkflowJobDTO>()
    val works = MutableLiveData<String>()
    val photoUpload = MutableLiveData<String>()


    init {

        workflowJobs.observeForever {
            saveWorkflowJob(it)

        }


    }


    suspend fun getUser(): LiveData<UserDTO> {
        return withContext(Dispatchers.IO) {
            Db.getUserDao().getUser()
        }
    }

    suspend fun getJobsForActivityIds(activityId1: Int, activityId2: Int): LiveData<List<JobDTO>> {
        return withContext(Dispatchers.IO) {
            Db.getJobDao().getJobsForActivityIds1(activityId1, activityId2)
        }
    }

    suspend fun getJobsForActivityId(activityId1: Int): LiveData<List<JobItemEstimateDTO>> {
        return withContext(Dispatchers.IO) {
            Db.getJobItemEstimateDao().getJobsForActivityId(activityId1)
        }
    }

    suspend fun getItemDescription(jobId: String): String {
        return withContext(Dispatchers.IO) {
            Db.getJobDao().getItemDescription(jobId)
        }
    }

    suspend fun getItemJobNo(jobId: String): String {
        return withContext(Dispatchers.IO) {
            Db.getJobDao().getItemJobNo(jobId)
        }
    }

    suspend fun getItemStartKm(jobId: String): Double {
        return withContext(Dispatchers.IO) {
            Db.getJobDao().getItemStartKm(jobId)
        }
    }

    suspend fun getItemEndKm(jobId: String): Double {
        return withContext(Dispatchers.IO) {
            Db.getJobDao().getItemEndKm(jobId)
        }
    }

    suspend fun getItemTrackRouteId(jobId: String): String {
        return withContext(Dispatchers.IO) {
            Db.getJobDao().getItemTrackRouteId(jobId)
        }
    }


    suspend fun submitWorks(
        estimateWorksItem: JobEstimateWorksDTO,
        activity: FragmentActivity,
        estimateJob: JobDTO
    ): String {

        val worksData = JsonObject()
        val gson = Gson()
        val newMeasure = gson.toJson(estimateWorksItem)
        val jsonElement: JsonElement = JsonParser.parseString(newMeasure)
        worksData.add("JobEstimateWorksItem", jsonElement)

        Timber.d("WorkEstimate $worksData")
        val uploadWorksItemResponse = apiRequest { api.uploadWorksItem(worksData) }
        postValue(
            uploadWorksItemResponse.errorMessage,
            estimateWorksItem,
            activity,
            estimateJob.UserId
        )

        val messages = uploadWorksItemResponse.errorMessage ?: ""
        return withContext(Dispatchers.IO) {
            messages
        }
    }


    private fun postValue(
        response: String?,
        jobEstimateWorks: JobEstimateWorksDTO,
        activity: FragmentActivity,
        useR: Int
    ) {
        if (response != null) {
            val apiException =
                ApiException(response)
            Timber.e(apiException)
            throw apiException
        } else {
            uploadWorksImages(jobEstimateWorks, activity)
            moveJobToNextWorkflowStep(jobEstimateWorks, activity, useR)
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
                Timber.e(noPhotosException)
                throw noPhotosException
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
                            NoDataException(
                                "Photo ${jobItemPhotos.filename} could not be loaded."
                            )
                        Timber.e(noDataException)
                        throw noDataException
                    }
                }
            }
        } else {
            val emptyPhotosException =
                NoDataException("WorkEstimate ${jobEstimateWorks.estimateId} photos are null.")
            Timber.e(emptyPhotosException)
            throw emptyPhotosException
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
        filename: String, photoQuality: PhotoQuality, activity: FragmentActivity
    ): ByteArray {
        val uri = PhotoUtil.getPhotoPathFromExternalDirectory(filename)
        val bitmap =
            PhotoUtil.getPhotoBitmapFromFile(activity.applicationContext, uri, photoQuality)
        return PhotoUtil.getCompressedPhotoWithExifInfo(
            bitmap!!,
            filename
        )
    }

    private fun moveJobToNextWorkflowStep(
        jobEstimateWorks: JobEstimateWorksDTO,
        activity: FragmentActivity,
        userId: Int
    ) {
        if (jobEstimateWorks.trackRouteId == null) {
            Looper.prepare() // to be able to make toast
            Toast.makeText(activity, "Error: trackRouteId is null", Toast.LENGTH_LONG).show()
        } else {
//            jobEstimateWorks.setTrackRouteId(jobEstimateWorks.trackRouteId)
            val direction: Int = WorkflowDirection.NEXT.value
            val trackRouteId: String = jobEstimateWorks.trackRouteId
            val description = "work step done"

            Coroutines.io {
                val workflowMoveResponse = apiRequest {
                    api.getWorkflowMove(
                        userId.toString(),
                        trackRouteId,
                        description,
                        direction
                    )
                }
                workflowJobs.postValue(workflowMoveResponse.workflowJob)
//                workflows.postValue(workflowMoveResponse.toDoListGroups)
            }


        }
    }

    private fun JobEstimateWorksDTO.setTrackRouteId(toBigEndian: String?) {
        this.trackRouteId = toBigEndian!!
    }


    suspend fun getWorkFlowCodes(eId: Int): LiveData<List<WF_WorkStepDTO>> {
        return withContext(Dispatchers.IO) {
            Db.getWorkStepDao().getWorkflowSteps(eId)
        }
    }


    suspend fun createEstimateWorksPhoto(
        estimateWorksPhotos: ArrayList<JobEstimateWorksPhotoDTO>,
        estimateWorksItem: JobEstimateWorksDTO
    ) {
        Coroutines.io {
            if (estimateWorksPhotos != null) {
                for (estimateWorksPhoto in estimateWorksPhotos) {
                    if (!Db.getEstimateWorkPhotoDao()
                            .checkIfEstimateWorksPhotoExist(estimateWorksPhoto.filename)
                    ) {
                        Db.getEstimateWorkPhotoDao().insertEstimateWorksPhoto(estimateWorksPhoto)
                    } else {
                        Timber.d("${estimateWorksPhoto.filename} was already in the database")
                    }

                }
                Db.getEstimateWorkDao().updateJobEstimateWorkForEstimateID(
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
            Db.getJobItemEstimateDao()
                .getJobItemsEstimatesDoneForJobId(jobId, estimateWorkPartComplete, estWorksComplete)
        }
    }


    suspend fun getJobEstiItemForEstimateId(estimateId: String?): LiveData<List<JobEstimateWorksDTO>> {
        return withContext(Dispatchers.IO) {
            Db.getEstimateWorkDao().getJobMeasureItemsForJobId(estimateId)
        }
    }


    suspend fun getJobItemEstimateForEstimateId(estimateId: String): LiveData<JobItemEstimateDTO> {
        return withContext(Dispatchers.IO) {
            Db.getJobItemEstimateDao().getJobItemEstimateForEstimateId(estimateId)
        }
    }

    private fun saveWorkflowJob(workflowj: WorkflowJobDTO?) {
        if (workflowj != null) {
            val job = setWorkflowJobBigEndianGuids(workflowj)
            insertOrUpdateWorkflowJobInSQLite(job)
        } else {
            val noDataException =
                NoDataException("Workflow Job is null")
            Timber.e(noDataException)
            throw noDataException
        }
    }


    private fun insertOrUpdateWorkflowJobInSQLite(job: WorkflowJobDTO?) {
        job?.let {
            updateWorkflowJobValuesAndInsertWhenNeeded(it)
        }
    }

    private fun updateWorkflowJobValuesAndInsertWhenNeeded(job: WorkflowJobDTO) {
        Coroutines.io {
            Db.getJobDao().updateJob(job.trackRouteId, job.actId, job.jiNo, job.jobId)


            job.workflowItemEstimates?.forEach { jobItemEstimate ->
                Db.getJobItemEstimateDao().updateExistingJobItemEstimateWorkflow(
                    jobItemEstimate.trackRouteId,
                    jobItemEstimate.actId,
                    jobItemEstimate.estimateId
                )

                jobItemEstimate.workflowEstimateWorks.forEach { jobEstimateWorks ->
                    if (!Db.getEstimateWorkDao()
                            .checkIfJobEstimateWorksExist(jobEstimateWorks.worksId)
                    )
                        Db.getEstimateWorkDao().insertJobEstimateWorks(
                            jobEstimateWorks as JobEstimateWorksDTO
                        )
                    else
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


            job.workflowItemMeasures?.forEach { jobItemMeasure ->
                Db.getJobItemMeasureDao().updateWorkflowJobItemMeasure(
                    jobItemMeasure.itemMeasureId,
                    jobItemMeasure.trackRouteId,
                    jobItemMeasure.actId,
                    jobItemMeasure.measureGroupId
                )
            }


            //  Place the Job Section, UPDATE OR CREATE
            job.workflowJobSections?.forEach { jobSection ->
                if (!Db.getJobSectionDao().checkIfJobSectionExist(jobSection.jobSectionId))
                    Db.getJobSectionDao().insertJobSection(jobSection)
                else
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


    private fun setWorkflowJobBigEndianGuids(job: WorkflowJobDTO): WorkflowJobDTO? {

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
    }


    private operator fun <T> LiveData<T>.not(): Boolean {
        return true
    }


    suspend fun getSectionForProjectSectionId(sectionId: String?): String {
        return withContext(Dispatchers.IO) {
            Db.getProjectSectionDao().getSectionForProjectSectionId(sectionId!!)
        }
    }


    suspend fun getUOMForProjectItemId(projectItemId: String): String {
        return withContext(Dispatchers.IO) {
            Db.getProjectItemDao().getUOMForProjectItemId(projectItemId)
        }
    }

    suspend fun getRouteForProjectSectionId(sectionId: String?): String {
        return withContext(Dispatchers.IO) {
            Db.getProjectSectionDao().getRouteForProjectSectionId(sectionId!!)
        }
    }

    suspend fun getProjectSectionIdForJobId(jobId: String?): String {
        return withContext(Dispatchers.IO) {
            Db.getJobSectionDao().getProjectSectionId(jobId!!)
        }
    }


    suspend fun getJobEstimationItemsForJobId(
        jobID: String?,
        actID: Int
    ): LiveData<List<JobItemEstimateDTO>> {
        return withContext(Dispatchers.IO) {
            Db.getJobItemEstimateDao().getJobEstimationItemsForJobId(jobID!!, actID)
        }
    }

    suspend fun getProjectItemDescription(projectItemId: String): String {
        return withContext(Dispatchers.IO) {
            Db.getProjectItemDao().getProjectItemDescription(projectItemId)
        }
    }

    suspend fun processWorkflowMove(
        userId: String,
        trackRouteId: String,
        description: String?,
        direction: Int
    ): String? {
        val workflowMoveResponse =
            apiRequest { api.getWorkflowMove(userId, trackRouteId, description, direction) }
        workflowJobs.postValue(workflowMoveResponse.workflowJob)
//        workflows.postValue(workflowMoveResponse.toDoListGroups)

        val messages = workflowMoveResponse.errorMessage
        // activity?.getResources()?.getString(R.string.please_wait)!!
        return withContext(Dispatchers.IO) {
            messages
        }

    }

    suspend fun getWorkItemsForActID(actId: Int): LiveData<List<JobEstimateWorksDTO>> {
        return withContext(Dispatchers.IO) {
            Db.getEstimateWorkDao().getWorkItemsForActID(actId)
        }
    }
}












































