package za.co.xisystems.itis_rrm.data.repositories

//import sun.security.krb5.Confounder.bytes

import android.os.Build
import android.os.Looper
import android.util.Log
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
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.data._commons.views.ToastUtils
import za.co.xisystems.itis_rrm.data.localDB.AppDatabase
import za.co.xisystems.itis_rrm.data.localDB.JobDataController
import za.co.xisystems.itis_rrm.data.localDB.entities.*
import za.co.xisystems.itis_rrm.data.network.BaseConnectionApi
import za.co.xisystems.itis_rrm.data.network.SafeApiRequest
import za.co.xisystems.itis_rrm.data.network.responses.UploadImageResponse
import za.co.xisystems.itis_rrm.data.preferences.PreferenceProvider
import za.co.xisystems.itis_rrm.utils.*
import za.co.xisystems.itis_rrm.utils.PhotoUtil.getPhotoPathFromExternalDirectory
import za.co.xisystems.itis_rrm.utils.enums.PhotoQuality
import za.co.xisystems.itis_rrm.utils.enums.WorkflowDirection
import java.util.*


/**
 * Created by Francis Mahlava on 2019/11/28.
 */

const val MINIMUM_INTERVALY = 3
private val jobDataController : JobDataController? = null
class JobCreationDataRepository(private val api: BaseConnectionApi, private val Db: AppDatabase, private val prefs: PreferenceProvider) : SafeApiRequest() {
    companion object {
        val TAG: String = JobCreationDataRepository::class.java.simpleName
    }




    private val workflowJ = MutableLiveData<WorkflowJobDTO>()
    private val workflowJ2 = MutableLiveData<WorkflowJobDTO>()
    private val photoupload = MutableLiveData<String>()
    private val works = MutableLiveData<String>()
    private val routeSectionPoint = MutableLiveData<String>()


    init {



        workflowJ.observeForever {
            saveWorkflowJob(it)//

        }
//        workflowJ2.observeForever {
//            saveWorkflowJob2(it)
//        }

        photoupload.observeForever {
            sendMSg(it)
        }


    }


    suspend fun getUser(): LiveData<UserDTO> {
        return withContext(Dispatchers.IO) {
            Db.getUserDao().getuser()
        }
    }





    private fun sendMSg(uploadResponse : String?) {
        val response: UploadImageResponse? = null
        if (uploadResponse.isNullOrEmpty())
            jobDataController?.setMsg(response!!.errorMessage)
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


    suspend fun saveNewJob(newjob: JobDTO) {
        Coroutines.io {
            if (newjob != null) {
                if (!Db.getJobDao().checkIfJobExist(newjob.JobId)) {
                    Db.getJobDao().insertOrUpdateJobs(newjob)
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

    suspend fun saveNewItem(newjItem: ItemDTOTemp) {
        Coroutines.io {
            if (newjItem != null) {
                if (!Db.getItemDao_Temp().checkItemExistsItemId(newjItem.itemId)) {

                    Db.getItemDao_Temp().insertItems(newjItem)
                }
            }
        }
    }

    suspend fun delete(item: ItemDTOTemp) {
        Coroutines.io {
            Db.getItemDao_Temp().deleteItem(item)
        }
    }


    suspend fun deleJobfromList(jobId: String) {
        Coroutines.io {
            Db.getJobDao().deleteJobForJobId(jobId)
        }
    }

    suspend fun updateNewJob(
        newjobId: String,
        startKM: Double,
        endKM: Double,
        sectionId: String,
        newJobItemEstimatesList: ArrayList<JobItemEstimateDTO>,
        jobItemSectionArrayList: ArrayList<JobSectionDTO>
    ) {
        Coroutines.io {
            if (!Db.getJobDao().checkIfJobExist(newjobId)) {
//
            } else {
                Db.getJobDao().updateJoSecId(
                    newjobId,
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
            //            Db.getSectionItemDao().getAllSectionItems()
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
                apiRequest { api.getRouteSectionPoint(distance,buffer,latitude, longitude, useR) }
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
            ToastUtils().toastLong(prefs.appContext, e.message)
        } catch (e: NoInternetException) {
            ToastUtils().toastLong(prefs.appContext, e.message)
            Log.e("NetworkConnection", "No Internet Connection", e)
        } catch (e: NoConnectivityException) {
            ToastUtils().toastLong(prefs.appContext, e.message)
            Log.e("Network-Connection", "Backend Host Unreachable", e)
        }

    }

    suspend fun getAllProjecItems(projectId: String): LiveData<List<ItemDTOTemp>> {
        return withContext(Dispatchers.IO) {
            Db.getItemDao_Temp().getAllProjecItems(projectId)
        }
    }
    suspend fun deleteItemList(jobId: String) {
        Coroutines.io {
            Db.getItemDao_Temp().deleteItemList(jobId)
        }
    }

    suspend fun deleteItemfromList(itemId: String) {
        Coroutines.io {
            Db.getItemDao_Temp().deleteItemfromList(itemId)
        }
    }

    suspend fun submitJob(userId: Int, job: JobDTO, activity: FragmentActivity): String {

        val jobhead = JsonObject()
        val gson = Gson()
        val newjob = gson.toJson(job)
        val jsonElement: JsonElement = JsonParser().parse(newjob)
        jobhead.add("Job", jsonElement)
        jobhead.addProperty("UserId", userId)
        Log.e("JsonObject", "Json string $jobhead")

        val jobResponse = apiRequest { api.sendJobsForApproval(jobhead) }
        workflowJ2.postValue(jobResponse.workflowJob, job, activity)

        val messages = jobResponse.errorMessage
//          activity.getResources().getString(R.string.please_wait)
        return withContext(Dispatchers.IO) {
            messages
        }
    }

    private fun <T> MutableLiveData<T>.postValue(
        workflowj: WorkflowJobDTO,
        job: JobDTO,
        activity: FragmentActivity
    ) {
        Coroutines.io {
        if (workflowj != null) {
            val createJob = setWorkflowJobBigEndianGuids(workflowj)
            insertOrUpdateWorkflowJobInSQLite(createJob)
            uploadcreateJobImages(job, activity)
            val myjob  = getUpdatedJob(DataConversion.toBigEndian(job.JobId)!!)
            moveJobToNextWorkflow(myjob, activity)
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
//                                Db.getEstimateWorkDao().insertJobEstimateWorks(jobEstimateWorks as JobEstimateWorksDTO)
                            else Db.getEstimateWorkDao().updateJobEstimateWorksWorkflow(
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
                        Db.getJobItemMeasureDao().updateWorkflowJobItemMeasure(
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


    private fun uploadcreateJobImages(packagejob: JobDTO, activity: FragmentActivity) {
        var imageCounter = 1
        var totalImages = 0

        if (packagejob.JobItemEstimates != null) {
            if (packagejob.JobItemEstimates!!.isEmpty()) {
//                progressView.toast("(job.getPrjJobItemEstimates() is empty")
//                progressView.dismissProgressDialog()
            } else {
                for (jobItemEstimate in packagejob.JobItemEstimates!!) {
                    if (jobItemEstimate.jobItemEstimatePhotos != null && jobItemEstimate.jobItemEstimatePhotos!!.size > 0) {
                        val photos: Array<JobItemEstimatesPhotoDTO> =
                            arrayOf<JobItemEstimatesPhotoDTO>(
                                jobItemEstimate.jobItemEstimatePhotos!!.get(0),
                                jobItemEstimate.jobItemEstimatePhotos!!.get(1)
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
                                    packagejob,
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
        packagejob: JobDTO,
        activity: FragmentActivity
    ) {

        val data: ByteArray = getData(filename, photoQuality, activity)
        processImageUpload(
            filename,
            activity.getString(R.string.jpg),
            data,
            totalImages,
            imageCounter,
            packagejob,
            activity
        )
    }


    private fun getData(
        filename: String, photoQuality: PhotoQuality, activity: FragmentActivity
    ): ByteArray {
        val uri = getPhotoPathFromExternalDirectory(activity.applicationContext, filename)
        val bitmap =
            PhotoUtil.getPhotoBitmapFromFile(activity.applicationContext, uri, photoQuality)
        val photo = PhotoUtil.getCompressedPhotoWithExifInfo(
            activity.applicationContext,
            bitmap!!,
            filename
        )
        return photo
    }


    private fun processImageUpload(
        filename: String,
        extension: String,
        photo: ByteArray,
        totalImages: Int,
        imageCounter: Int,
        packagejob: JobDTO,
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
                workflowJ.postValue(workflowMoveResponse.workflowJob)
                Db.getItemDao_Temp().deleteItemList(job.JobId)

//                toDoListGroups.postValue(workflowMoveResponse.toDoListGroups)
//                workflows.postValue(workflowMoveResponse.toDoListGroups)
            }

        }
    }



    private  fun <T> MutableLiveData<T>.postValue(
        direction: String, linearId: String, pointLocation: Double, sectionId: Int, projectId: String?, jobId: String?, item: ItemDTOTemp?
    ) { saveRouteSectionPoint(direction,linearId, pointLocation,sectionId,projectId, jobId,  item) }

    private fun saveRouteSectionPoint(direction: String, linearId: String, pointLocation: Double, sectionId: Int, projectId: String?, jobId: String?, item: ItemDTOTemp?) {
        if (linearId != null) {
            //Db.getProjectSectionDao().getSectionByRouteSectionProject(linearId, sectionId, direction, projectId)
//           activity?.toast(direction + linearId +  "$pointLocation"  + sectionId.toString()+ projectId )
            if (!Db.getSectionPointDao().checkSectionExists(sectionId, projectId, jobId)) {
                Db.getSectionPointDao().insertSection(direction,linearId,pointLocation,sectionId,projectId,jobId)
            }



        }
    }




    private fun <T> MutableLiveData<T>.postValue(photo: String?, fileName: String) {
        saveEstimatePhoto(photo, fileName)
    }

    fun saveEstimatePhoto(estimatePhoto: String?, fileName: String) {
        Coroutines.io {
            if (estimatePhoto != null) {
                PhotoUtil.createPhotofolder(estimatePhoto, fileName)
//            PhotoUtil.createPhotofolder(fileName)
            }else{
                PhotoUtil.createPhotofolder()
            }

        }

    }

   suspend fun getContractNoForId(contractVoId: String?): String {
        return withContext(Dispatchers.IO) {
            Db.getContractDao().getContractNoForId(contractVoId)
        }
    }

    suspend fun getProjectCodeForId(projectId: String?): String {
        return withContext(Dispatchers.IO){
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












































