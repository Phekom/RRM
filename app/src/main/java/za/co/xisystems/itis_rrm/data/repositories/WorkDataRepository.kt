package za.co.xisystems.itis_rrm.data.repositories

//import sun.security.krb5.Confounder.bytes

import android.os.Build
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.data.localDB.AppDatabase
import za.co.xisystems.itis_rrm.data.localDB.entities.*
import za.co.xisystems.itis_rrm.data.network.BaseConnectionApi
import za.co.xisystems.itis_rrm.data.network.SafeApiRequest
import za.co.xisystems.itis_rrm.data.preferences.PreferenceProvider
import za.co.xisystems.itis_rrm.utils.*
import za.co.xisystems.itis_rrm.utils.enums.PhotoQuality
import za.co.xisystems.itis_rrm.utils.enums.WorkflowDirection
import java.util.*
import kotlin.collections.ArrayList


/**
 * Created by Francis Mahlava on 2019/11/28.
 */

class WorkDataRepository(private val api: BaseConnectionApi, private val Db: AppDatabase, private val prefs: PreferenceProvider) : SafeApiRequest() {
    companion object {
        val TAG: String = WorkDataRepository::class.java.simpleName
    }


    private val workflowJ = MutableLiveData<WorkflowJobDTO>()
    private val works = MutableLiveData<String>()
    private val photoupload = MutableLiveData<String>()


    init {

        workflowJ.observeForever {
            saveWorkflowJob(it)

        }


    }


    suspend fun getUser(): LiveData<UserDTO> {
        return withContext(Dispatchers.IO) {
            Db.getUserDao().getuser()
        }
    }

    suspend fun getJobsForActivityIds1(activityId1: Int, activityId2: Int): LiveData<List<JobDTO>> {
        return withContext(Dispatchers.IO) {
            Db.getJobDao().getJobsForActivityIds1(activityId1, activityId2)
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
        itemEstiWorks: JobEstimateWorksDTO,
        activity: FragmentActivity,
        itemEstiJob: JobDTO
    ) : String
    {

        val worksdata = JsonObject()
        val gson = Gson()
        val newmeasure = gson.toJson(itemEstiWorks)
        val jsonElement: JsonElement = JsonParser().parse(newmeasure)
        worksdata.add("JobEstimateWorksItem", jsonElement )

        Log.e("JsonObject", "Json string $worksdata")
        val uploadWorksItemResponse = apiRequest { api.uploadWorksItem(worksdata) }
        works.postValue(uploadWorksItemResponse.errorMessage,itemEstiWorks ,activity,itemEstiJob.UserId)

        val messages = activity.getResources().getString(R.string.please_wait) //uploadWorksItemResponse.errorMessage
        return withContext(Dispatchers.IO) {
            messages
        }

//        DataConversion.toLittleEndian()
    }


    private fun <T> MutableLiveData<T>.postValue(response: String?, jobEstimateWorks : JobEstimateWorksDTO, activity: FragmentActivity,  useR: Int
    ) {
        if (response != null) {

        }else{
            uploadworksImages(jobEstimateWorks, activity)
            moveJobToNextWorkflowStep(jobEstimateWorks, activity, useR)
        }
    }

    private fun uploadworksImages(
        jobEstimateWorks: JobEstimateWorksDTO,
        activity: FragmentActivity
    ) {
        var imageCounter = 1
        var totalImages = 0
        if (jobEstimateWorks.jobEstimateWorksPhotos != null) {
            if (jobEstimateWorks.jobEstimateWorksPhotos !!.isEmpty()) {
//                progressView.toast("(job.getPrjJobItemEstimates() is empty")
//                progressView.dismissProgressDialog()
            } else {
                for (jobItemWphotos in jobEstimateWorks.jobEstimateWorksPhotos !!) {
//                    if (jobEstimateWorks.jobEstimateWorksPhotos!= null && jobEstimateWorks.jobEstimateWorksPhotos!!.size > 0) {
//                        val photos: Array<JobEstimateWorksPhotoDTO> =
//                            arrayOf<JobEstimateWorksPhotoDTO>(
//                                jobEstimateWorks.jobEstimateWorksPhotos!!.get(0),
//                                jobEstimateWorks.jobEstimateWorksPhotos!!.get(1)
//                            )
//                        for (jobItemEstimatePhoto in photos) {
//                        for (jobItemEstimatePhoto in jobItemEstimate.jobItemEstimatePhotos!!) {
                            if (PhotoUtil.photoExist(jobItemWphotos.filename)) {
                                Log.d("x-", "UploadRrImage $imageCounter")
                                uploadRrmImage(
                                    jobItemWphotos.filename,
                                    PhotoQuality.HIGH,
                                    imageCounter,
                                    totalImages,
                                    jobEstimateWorks,
                                    activity
                                )
                                imageCounter++
                            } else {
//                                setProgressBarMessage(text)
//                                progressView.toast("Error: photo filename is empty!")
                                Log.d("x-", "Error: photo filename is empty!")
                            }
//                        }
//                    } else {
//                        progressView.toast("Error: photos are empty!")
                        Log.d("x-", "Error: photos are empty!")
//                        progressView.dismissProgressDialog()
//                    }
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
        jobEstimateWorks: JobEstimateWorksDTO,
        activity: FragmentActivity
    ) {
        val data: ByteArray = getData(filename, photoQuality, activity)
        processImageUpload(
            filename,
            activity.getString(R.string.jpg),
            data,
            totalImages,
            imageCounter,
            jobEstimateWorks,
            activity
        )
    }

    private fun processImageUpload(
        filename: String,
        extension: String,
        photo: ByteArray,
        totalImages: Int,
        imageCounter: Int,
        jobEstimateWorks: JobEstimateWorksDTO,
        activity: FragmentActivity
    ) {
        Coroutines.io {
            val imagedata = JsonObject()
            imagedata.addProperty("Filename", filename)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                imagedata.addProperty("ImageByteArray", Base64.getEncoder().encodeToString(photo))
            }
            imagedata.addProperty("ImageFileExtension", extension)
            Log.e("JsonObject", "Json string $imagedata")

            val uploadImageResponse = apiRequest { api.uploadRrmImage(imagedata) }
            photoupload.postValue(uploadImageResponse.errorMessage)
            if (totalImages <= imageCounter){
                Log.e("Coroutines", "Json string $totalImages")
            }

        }
    }

    private fun getData(
        filename: String, photoQuality: PhotoQuality, activity: FragmentActivity
    ): ByteArray {
        val uri = PhotoUtil.getPhotoPathFromExternalDirectory(activity.applicationContext, filename)
        val bitmap =
            PhotoUtil.getPhotoBitmapFromFile(activity.applicationContext, uri, photoQuality)
        val photo = PhotoUtil.getCompressedPhotoWithExifInfo(
            activity.applicationContext,
            bitmap!!,
            filename
        )
        return photo
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
            val direction: Int = WorkflowDirection.NEXT.getValue()
            val trackRouteId: String = jobEstimateWorks.trackRouteId!!
            val description: String = "work step done"

            Coroutines.io {
                val workflowMoveResponse = apiRequest { api.getWorkflowMove(userId.toString(), trackRouteId, description, direction) }
                workflowJ.postValue(workflowMoveResponse.workflowJob)
//                workflows.postValue(workflowMoveResponse.toDoListGroups)
            }


        }
    }


    private fun JobEstimateWorksDTO.setTrackRouteId(toBigEndian: String?) {
        this.trackRouteId = toBigEndian!!
    }



    suspend fun getWokrCodes(eId: Int): LiveData<List<WF_WorkStepDTO>> {
        return withContext(Dispatchers.IO) {
            Db.getWorkStepDao().getWorkflowSteps(eId)
        }
    }


    suspend fun createEstimateWorksPhoto(
        estimateWorksPhotos: ArrayList<JobEstimateWorksPhotoDTO>,
        estimat: JobItemEstimateDTO,
        itemEstiWorks: JobEstimateWorksDTO
    ) {
        Coroutines.io {
            if (estimateWorksPhotos != null){
                for (estimateWorksPhoto in estimateWorksPhotos){
                    if (!Db.getEstimateWorkPhotoDao().checkIfEstimateWorksPhotoExist(estimateWorksPhoto.filename)) {
                        Db.getEstimateWorkPhotoDao().insertEstimateWorksPhoto(estimateWorksPhoto)
                    } else {
//                Db.getEstimateWorkPhotoDao().updateExistingEstimateWorksPhoto(estimateWorksPhoto, estimatId)
                    }

                }
                Db.getEstimateWorkDao().updateJobEstimateWorkForEstimateID(itemEstiWorks.jobEstimateWorksPhotos!!, itemEstiWorks.estimateId)
            }

        }

    }

    suspend fun getJobItemsEstimatesDoneForJobId(
        jobId: String?,
        estimateWorkPartComplete: Int,
        estWorksComplete: Int
    ): Int{
        return withContext(Dispatchers.IO) {
            Db.getJobItemEstimateDao().getJobItemsEstimatesDoneForJobId(jobId, estimateWorkPartComplete,estWorksComplete)
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

    private fun saveWorkflowJob(workflowj : WorkflowJobDTO?) {
        if (workflowj != null) {
            val job = setWorkflowJobBigEndianGuids(workflowj)
            insertOrUpdateWorkflowJobInSQLite(job)
        }else {
//            Looper.prepare() // to be able to make toast
//        Toast.makeText(activity, "Error: WorkFlow Job is null", Toast.LENGTH_LONG).show()
            Log.e("Error:", " WorkFlow Job is null")
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

            if (job.workflowItemEstimates != null && job.workflowItemEstimates.size !== 0) {
                for (jobItemEstimate in job.workflowItemEstimates) {
                    Db.getJobItemEstimateDao().updateExistingJobItemEstimateWorkflow(
                        jobItemEstimate.trackRouteId,
                        jobItemEstimate.actId,
                        jobItemEstimate.estimateId
                    )

                    if (jobItemEstimate.workflowEstimateWorks != null) {
                        for (jobEstimateWorks in jobItemEstimate.workflowEstimateWorks) {
                            if (!Db.getEstimateWorkDao().checkIfJobEstimateWorksExist(
                                    jobEstimateWorks.worksId
                                )
                            )
                                Db.getEstimateWorkDao().insertJobEstimateWorks(
                                    jobEstimateWorks as JobEstimateWorksDTO
                                ) else Db.getEstimateWorkDao().updateJobEstimateWorksWorkflow(
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

                if (job.workflowItemMeasures != null && job.workflowItemMeasures.size !== 0) {
                    for (jobItemMeasure in job.workflowItemMeasures) {
                        Db?.getJobItemMeasureDao()!!.updateWorkflowJobItemMeasure(
                            jobItemMeasure.itemMeasureId,
                            jobItemMeasure.trackRouteId,
                            jobItemMeasure.actId,
                            jobItemMeasure.measureGroupId
                        )
                    }
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


    private fun setWorkflowJobBigEndianGuids(job: WorkflowJobDTO): WorkflowJobDTO? {
        job.actId = job.actId
        job.jobId = DataConversion.toBigEndian(job.jobId)
        job.trackRouteId = DataConversion.toBigEndian(job.trackRouteId)
        job.jiNo = job.jiNo
        if (job.workflowItemEstimates != null) {
            for (jie in job.workflowItemEstimates) {
                jie.actId = jie.actId
                jie.estimateId = DataConversion.toBigEndian(jie.estimateId)!!
                jie.trackRouteId = DataConversion.toBigEndian(jie.trackRouteId)!!
                //  Lets go through the WorkFlowEstimateWorks
                for (wfe in jie.workflowEstimateWorks) {
                    wfe.trackRouteId = DataConversion.toBigEndian(wfe.trackRouteId)!!
                    wfe.worksId = DataConversion.toBigEndian(wfe.worksId)!!
                    wfe.actId = wfe.actId
                    wfe.estimateId = DataConversion.toBigEndian(wfe.estimateId)!!
                    wfe.recordVersion = wfe.recordVersion
                    wfe.recordSynchStateId = wfe.recordSynchStateId
                }
            }
        }
        if (job.workflowItemMeasures != null) {
            for (jim in job.workflowItemMeasures) {
                jim.actId = jim.actId
                jim.itemMeasureId = DataConversion.toBigEndian(jim.itemMeasureId)!!
                jim.measureGroupId = DataConversion.toBigEndian(jim.measureGroupId)!!
                jim.trackRouteId = DataConversion.toBigEndian(jim.trackRouteId)!!
            }
        }
        if (job.workflowJobSections != null) {
            for (js in job.workflowJobSections) {
                js.jobSectionId = DataConversion.toBigEndian(js.jobSectionId)!!
                js.projectSectionId = DataConversion.toBigEndian(js.projectSectionId)!!
                js.jobId = DataConversion.toBigEndian(js.jobId)
            }
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


    suspend fun getJobEstimationItemsForJobId(jobID: String?): LiveData<List<JobItemEstimateDTO>> {
        return withContext(Dispatchers.IO) {
            Db.getJobItemEstimateDao().getJobEstimationItemsForJobId(jobID!!)
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
    ) :String {
        val workflowMoveResponse =
            apiRequest { api.getWorkflowMove(userId, trackRouteId, description, direction) }
        workflowJ.postValue(workflowMoveResponse.workflowJob)
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


//    suspend fun getProjectDescription(projectId: String): String {
//        return withContext(Dispatchers.IO) {
//            Db.getProjectDao().getProjectDescription(projectId)
//        }
//    }
//
//    suspend fun getJobsForActivityId(activityId: Int): LiveData<List<JobDTO>> {
//        return withContext(Dispatchers.IO) {
//            Db.getJobDao().getJobsForActivityId(activityId)
//        }
//    }
//

//
//
//
//    suspend fun getJobEstimationItemsPhotoStartPath(estimateId: String): String {
//        return withContext(Dispatchers.IO) {
//            Db.getJobItemEstimatePhotoDao().getJobEstimationItemsPhotoStartPath(estimateId)
//        }
//    }
//
//
//    suspend fun getJobEstimationItemsPhotoEndPath(estimateId: String): String {
//        return withContext(Dispatchers.IO) {
//            Db.getJobItemEstimatePhotoDao().getJobEstimationItemsPhotoEndPath(estimateId)
//        }
//    }



}












































