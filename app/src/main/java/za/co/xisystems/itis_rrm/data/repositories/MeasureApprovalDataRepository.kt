package za.co.xisystems.itis_rrm.data.repositories

//import sun.security.krb5.Confounder.bytes

import android.os.Build
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.data.localDB.AppDatabase
import za.co.xisystems.itis_rrm.data.localDB.entities.*
import za.co.xisystems.itis_rrm.data.network.BaseConnectionApi
import za.co.xisystems.itis_rrm.data.network.SafeApiRequest
import za.co.xisystems.itis_rrm.data.preferences.PreferenceProvider
import za.co.xisystems.itis_rrm.utils.*
import za.co.xisystems.itis_rrm.utils.PhotoUtil.getPhotoPathFromExternalDirectory
import za.co.xisystems.itis_rrm.utils.enums.PhotoQuality
import za.co.xisystems.itis_rrm.utils.enums.WorkflowDirection
import java.util.*
import kotlin.collections.ArrayList


/**
 * Created by Francis Mahlava on 2019/11/28.
 */

class MeasureApprovalDataRepository(private val api: BaseConnectionApi, private val Db: AppDatabase, private val prefs: PreferenceProvider) : SafeApiRequest() {
    companion object {
        val TAG: String = MeasureApprovalDataRepository::class.java.simpleName
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

//    suspend fun getJobMeasureForActivityId(activityId: Int): LiveData<List<JobItemEstimateDTO>> {
////        return withContext(Dispatchers.IO) {
//////            Db.getJobItemEstimateDao().getJobsEstimateForActivityId(activityId, activityId2)
////        }
//    }



    suspend fun saveMeasurementItems(
        userId: String,
        jobId: String,
        jimNo: String?,
        contractVoId: String?,
        mSures: ArrayList<JobItemMeasureDTO>,
        activity: FragmentActivity?,
        itemMeasureJob: JobDTO
    ) : String {
        val measuredata = JsonObject()
        val jmadata = JsonObject()
        val jdata = JsonObject()
        val array = JsonArray()
        val array1 = JsonArray()
        val array2 = JsonArray()
        val array3 = JsonArray()
        measuredata.addProperty("ContractId", contractVoId)
        measuredata.addProperty("JiNo", jimNo)
        measuredata.addProperty("JobId", jobId)
        measuredata.add("MeasurementItems", array )
//       array.add(jdata)
        array.add(jdata)
        for (i in  mSures.indices){
            jdata.addProperty("ActId", mSures.get(i).actId )
            jdata.addProperty("ApprovalDate", mSures.get(i).approvalDate)
            jdata.addProperty("Cpa", mSures.get(i).cpa)
            jdata.addProperty("EndKm", mSures.get(i).endKm)
            jdata.addProperty("EstimateId", DataConversion.toLittleEndian(mSures.get(i).estimateId))
            jdata.addProperty("ItemMeasureId", DataConversion.toLittleEndian(mSures.get(i).itemMeasureId))
            jdata.addProperty("JimNo", mSures.get(i).jimNo)
            jdata.addProperty("PrjJobDto", mSures.get(i).job.toString())
//       jdata.add("PrjJobDto", array1 )
//       array1.add(jmadata)
            jdata.addProperty("JobDirectionId", mSures.get(i).jobDirectionId)
            jdata.addProperty("JobId", DataConversion.toLittleEndian(mSures.get(i).jobId))
            jdata.addProperty("PrjJobItemEstimateDto", mSures.get(i).jobItemEstimate.toString())
//       jdata.add("PrjJobItemEstimateDto", array2 )
//       array2.add(jmadata)
            jdata.addProperty("LineAmount", mSures.get(i).lineAmount)
            jdata.addProperty("LineRate", mSures.get(i).lineRate)
            jdata.addProperty("MeasureDate", mSures.get(i).measureDate)
            jdata.addProperty("MeasureGroupId", mSures.get(i).measureGroupId)
            jdata.add("PrjItemMeasurePhotoDtos", array3)
            array3.add(jmadata)
            jmadata.addProperty("Descr",mSures.get(i).jobItemMeasurePhotos.get(i).descr)
            jmadata.addProperty("Filename",mSures.get(i).jobItemMeasurePhotos.get(i).filename)
            jmadata.addProperty("ItemMeasureId",DataConversion.toLittleEndian(mSures.get(i).jobItemMeasurePhotos.get(i).itemMeasureId))
            jmadata.addProperty("PrjJobItemMeasureDto",mSures.get(i).jobItemMeasurePhotos.get(i).jobItemMeasure.toString())
            jmadata.addProperty("PhotoDate",mSures.get(i).jobItemMeasurePhotos.get(i).photoDate)
            jmadata.addProperty("PhotoId", DataConversion.toLittleEndian(mSures.get(i).jobItemMeasurePhotos.get(i).photoId))
            jmadata.addProperty("PhotoLatitude",mSures.get(i).jobItemMeasurePhotos.get(i).photoLatitude)
            jmadata.addProperty("PhotoLongitude",mSures.get(i).jobItemMeasurePhotos.get(i).photoLongitude)
            jmadata.addProperty("PhotoPath",mSures.get(i).jobItemMeasurePhotos.get(i).photoPath)
            jmadata.addProperty("RecordSynchStateId",mSures.get(i).jobItemMeasurePhotos.get(i).recordSynchStateId)
            jmadata.addProperty("RecordVersion",mSures.get(i).jobItemMeasurePhotos.get(i).recordVersion)

            jdata.addProperty("ProjectItemId", DataConversion.toLittleEndian(mSures.get(i).projectItemId))
            jdata.addProperty("ProjectVoId", mSures.get(i).projectVoId)
            jdata.addProperty("Qty", mSures.get(i).qty)
            jdata.addProperty("RecordSynchStateId", mSures.get(i).recordSynchStateId)
            jdata.addProperty("RecordVersion", mSures.get(i).recordVersion)
            jdata.addProperty("StartKm", mSures.get(i).startKm)
            jdata.addProperty("TrackRouteId", DataConversion.toLittleEndian(mSures.get(i).trackRouteId))

        }


        //TODO(finish building the MeasureItems and Location)
//       measuredata.add("MeasurementItems", mSures)
        measuredata.addProperty("UserId", userId)


        Log.e("JsonObject", "Json string $measuredata")
        val measurementItemResponse = apiRequest { api.saveMeasurementItems(measuredata) }
        workflowJ.postValue(measurementItemResponse.workflowJob,mSures, activity, itemMeasureJob)

        val messages = measurementItemResponse.errorMessage //activity?.getResources()?.getString(R.string.please_wait)
        return withContext(Dispatchers.IO) {
            messages
        }
    }


    private fun <T> MutableLiveData<T>.postValue(workflowjb : WorkflowJobDTO, jobItemMeasure : ArrayList<JobItemMeasureDTO>, activity: FragmentActivity?, itemMeasureJob: JobDTO) {
        if (workflowjb != null) {
            val job = setWorkflowJobBigEndianGuids(workflowjb)
            insertOrUpdateWorkflowJobInSQLite(job)
            Coroutines.io {
                uploadmeasueImages(jobItemMeasure, activity, itemMeasureJob)
            }
//            saveUserTaskList(toDoList)
//            val packageItemMeasure = workflowj
//            moveJobItemMeasurementsToNextWorkflow(jobItemMeasures)
        }
    }

    suspend fun getUpdatedJob(jobId: String): JobDTO {
        return withContext(Dispatchers.IO) {
            Db.getJobDao().getJobForJobId(jobId)
        }
    }

    private fun uploadmeasueImages(
        jobItemMeasures: ArrayList<JobItemMeasureDTO>,
        activity: FragmentActivity?,
        itemMeasureJob: JobDTO
    ) {
        var imageCounter = 1
        var totalImages = 0
        if (jobItemMeasures != null) {
            for (jobItemMeasure in jobItemMeasures.iterator()) {
                if (jobItemMeasure.jobItemMeasurePhotos != null) {
//                    var filename = jobItemMeasure.jobItemMeasurePhotos.
                    for (photo in jobItemMeasure.jobItemMeasurePhotos) {
                        if (PhotoUtil.photoExist(photo.filename!!)) {
                            val data: ByteArray = getData(photo.filename, PhotoQuality.HIGH, activity!!)
                            uploadmeasueImage(photo.filename, activity.getString(R.string.jpg),data,imageCounter, totalImages, itemMeasureJob,activity )
//                                uploadmeasueImage(jobItemEstimatePhoto.filename, PhotoQuality.HIGH, imageCounter, totalImages, packagejob,activity)
                            totalImages++
                        }
                    }


                }
            }
        }

    }

    private fun getData(filename: String, photoQuality: PhotoQuality, activity: FragmentActivity
    ): ByteArray {
        val uri = getPhotoPathFromExternalDirectory(activity.applicationContext, filename)
        val bitmap = PhotoUtil.getPhotoBitmapFromFile(activity.applicationContext, uri, photoQuality)
        val photo = PhotoUtil.getCompressedPhotoWithExifInfo(activity.applicationContext, bitmap!!, filename)
        return photo
    }


    private fun uploadmeasueImage(
        filename: String,
        extension: String,
        photo: ByteArray,
        imageCounter: Int,
        totalImages: Int,
        itemMeasureJob: JobDTO,
        activity: FragmentActivity?
    ) {

//        val bitmap = PhotoUtil.getPhotoBitmapFromFile(activity!!.applicationContext, PhotoUtil.getPhotoPathFromExternalDirectory( activity!!.applicationContext,filename), PhotoQuality.HIGH)
//        val photo = PhotoUtil.getCompressedPhotoWithExifInfo(activity!!.applicationContext,bitmap!!, filename) //

        processImageUpload(filename, extension,photo, totalImages, imageCounter,itemMeasureJob,activity!!)

//        Coroutines.main {
//            measureViewModel.processImageUpload(filename,extension ,photo)
//            activity?.hideKeyboard()
//            popViewOnJobSubmit()
//        }
    }



    private fun processImageUpload(
        filename: String,
        extension: String,
        photo: ByteArray,
        totalImages: Int,
        imageCounter: Int,
        itemMeasureJob: JobDTO,
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
            if (totalImages <= imageCounter)
                Coroutines.io {
                    val myjob  = getUpdatedJob(itemMeasureJob.JobId)
                    moveJobToNextWorkflow(myjob, activity)
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
            val direction: Int = WorkflowDirection.NEXT.getValue()
            val trackRouteId: String =  DataConversion.toLittleEndian(job.TrackRouteId)!!
            val description: String = activity.getResources().getString(R.string.submit_for_approval)

            Coroutines.io {
                val workflowMoveResponse = apiRequest { api.getWorkflowMove(job.UserId.toString(), trackRouteId, description, direction) }
                workflowJ.postValue(workflowMoveResponse.workflowJob)
//                workflows.postValue(workflowMoveResponse.toDoListGroups)
//                workflows.postValue(workflowMoveResponse.toDoListGroups)
            }

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

    private fun JobDTO.setTrackRouteId(toLittleEndian: String?) {
        this.TrackRouteId =  toLittleEndian
    }

    suspend fun getJobItemsToMeasureForJobId(jobID: String?): LiveData<List<JobItemEstimateDTO>> {
        return withContext(Dispatchers.IO) {
            Db.getJobItemEstimateDao().getJobItemsToMeasureForJobId(jobID!!)
        }
    }



    suspend fun getSingleJobFromJobId(jobId: String?): LiveData<JobDTO> {
        return withContext(Dispatchers.IO) {
            Db.getJobDao().getJobFromJobId(jobId!!)
        }
    }

    suspend fun getJobItemMeasuresForJobIdAndEstimateId2(
        jobId: String?,
        estimateId: String,
        jobItemMeasureArrayList: ArrayList<JobItemMeasureDTO>
    ): LiveData<List<JobItemMeasureDTO>>  {
        val jobItemMeasures = Db.getJobItemMeasureDao().getJobItemMeasuresForJobIdAndEstimateId(jobId, estimateId)
        if (jobItemMeasures != null) {
            for (jobItemMeasure in jobItemMeasureArrayList) {
                if (jobItemMeasure != null) {
                    if (Db.getJobItemMeasurePhotoDao().checkIfJobItemMeasurePhotoExistsForMeasureId(
                            jobItemMeasure.itemMeasureId
                        )
                    )

                        for (itemMeasurePhoto in jobItemMeasure.jobItemMeasurePhotos) {
                            Db.getJobItemMeasurePhotoDao().getJobItemMeasurePhotosForItemMeasureID(jobItemMeasure.itemMeasureId)

                            if (Db.getProjectItemDao().checkItemExistsItemId(jobItemMeasure.projectItemId!!)) {
                                val selectedItem: LiveData<ProjectItemDTO> = Db.getProjectItemDao().getItemForItemId(jobItemMeasure.projectItemId!!)
                                jobItemMeasure.selectedItemUom = selectedItem.value?.uom
                            }
                        }


                }
            }
        }

        return withContext(Dispatchers.IO) {
            jobItemMeasures!!
        }
    }



    suspend fun getItemForItemId(projectItemId: String?): LiveData<ProjectItemDTO> {
        return withContext(Dispatchers.IO) {
            Db.getProjectItemDao().getItemForItemId(projectItemId!!)
        }

    }


    suspend fun saveJobItemMeasureItems(jobItemMeasures: ArrayList<JobItemMeasureDTO>) {
        Coroutines.io {
            for (jobItemMeasure in jobItemMeasures!!.iterator()){
                if (!Db.getJobItemMeasureDao().checkIfJobItemMeasureExists(jobItemMeasure.itemMeasureId!!)){
                    Db.getJobItemMeasureDao().insertJobItemMeasure(jobItemMeasure!!)
                }

            }

        }
    }


    suspend fun getJobMeasureItemsPhotoPath(itemMeasureId: String): String {
        return withContext(Dispatchers.IO) {
            Db.getJobItemMeasurePhotoDao().getJobMeasureItemsPhotoPath(itemMeasureId)
        }
    }

    suspend fun getJobMeasureItemsPhotoPath2(itemMeasureId: String): String {
        return withContext(Dispatchers.IO) {
            Db.getJobItemMeasurePhotoDao().getJobMeasureItemsPhotoPath(itemMeasureId)
        }
    }


    suspend fun deleteItemMeasurefromList(itemMeasureId: String) {
        Coroutines.io {
            Db.getJobItemMeasureDao().deleteItemMeasurefromList(itemMeasureId)
        }
    }

    suspend fun deleteItemMeasurephotofromList(itemMeasureId: String) {
        Coroutines.io {
            Db.getJobItemMeasurePhotoDao().deleteItemMeasurephotofromList(itemMeasureId)
        }
    }


    suspend fun setJobItemMeasureImages(
        jobItemMeasurePhotoList: ArrayList<JobItemMeasurePhotoDTO>,
        estimateId: String?,
        selectedJobItemMeasure: JobItemMeasureDTO
    ){
        Coroutines.io {
            for (jobItemMeasurePhoto in jobItemMeasurePhotoList.iterator()){
                Db.getJobItemMeasureDao().insertJobItemMeasure(selectedJobItemMeasure)
                if (!Db.getJobItemMeasurePhotoDao().checkIfJobItemMeasurePhotoExists(jobItemMeasurePhoto.filename!!)){

                    Db.getJobItemMeasurePhotoDao().insertJobItemMeasurePhoto(jobItemMeasurePhoto!!)
                    jobItemMeasurePhoto.setEstimateId(estimateId)
                    Db.getJobItemMeasureDao().upDatePhotList(jobItemMeasurePhotoList,selectedJobItemMeasure.itemMeasureId!!)
                }

            }

        }
    }

    suspend fun getJobItemMeasuresForJobIdAndEstimateId(
        jobId: String?,
        estimateId: String
    ): LiveData<List<JobItemMeasureDTO>>  {
        val jobItemMeasures = Db.getJobItemMeasureDao().getJobItemMeasuresForJobIdAndEstimateId(jobId,estimateId)
        return withContext(Dispatchers.IO) {
            jobItemMeasures!!
        }
    }



    suspend fun getJobItemMeasurePhotosForItemEstimateID(estimateId: String): LiveData<List<JobItemMeasurePhotoDTO>>  {
        return withContext(Dispatchers.IO) {
            Db.getJobItemMeasurePhotoDao().getJobItemMeasurePhotosForItemEstimateID(estimateId)
        }
    }

    private fun JobItemMeasurePhotoDTO.setEstimateId(estimateId: String?) {
        this.estimateId = estimateId
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

    suspend fun getProjectItemDescription(projectItemId: String): String {
        return withContext(Dispatchers.IO) {
            Db.getProjectItemDao().getProjectItemDescription(projectItemId)
        }
    }













//    suspend fun getJobItemMeasurePhotosForItemMeasureID(itemMeasureId: String): LiveData<List<JobItemMeasurePhotoDTO>>  {
//        return withContext(Dispatchers.IO) {
//            Db.getJobItemMeasurePhotoDao().getJobItemMeasurePhotosForItemMeasureID(itemMeasureId)
//        }
//    }
//    suspend fun getJobsForActivityIds1(activityId1: Int, activityId2: Int): LiveData<List<JobDTO>> {
//        return withContext(Dispatchers.IO) {
//            Db.getJobDao().getJobsForActivityIds1(activityId1, activityId2)
//        }
//    }
//
//
//    suspend fun getItemStartKm(jobId: String): Double {
//        return withContext(Dispatchers.IO) {
//            Db.getJobDao().getItemStartKm(jobId)
//        }
//    }
//
//    suspend fun getItemEndKm(jobId: String): Double {
//        return withContext(Dispatchers.IO) {
//            Db.getJobDao().getItemEndKm(jobId)
//        }
//    }
//
//    suspend fun getItemTrackRouteId(jobId: String): String {
//        return withContext(Dispatchers.IO) {
//            Db.getJobDao().getItemTrackRouteId(jobId)
//        }
//    }






//
//    suspend fun getUOMForProjectItemId(projectItemId: String): String {
//        return withContext(Dispatchers.IO) {
//            Db.getProjectItemDao().getUOMForProjectItemId(projectItemId)
//        }
//    }


//    suspend fun getJobEstimationItemsForJobId(jobID: String?): LiveData<List<JobItemEstimateDTO>> {
//        return withContext(Dispatchers.IO) {
//            Db.getJobItemEstimateDao().getJobEstimationItemsForJobId(jobID!!)
//        }
//    }


//    suspend fun processWorkflowMove(
//        userId: String,
//        trackRouteId: String,
//        description: String?,
//        direction: Int
//    ) {
//        val workflowMoveResponse =
//            apiRequest { api.getWorkflowMove(userId, trackRouteId, description, direction) }
//        workflowJ.postValue(workflowMoveResponse.workflowJob)
////        workflows.postValue(workflowMoveResponse.toDoListGroups)
//
//    }





































































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












































