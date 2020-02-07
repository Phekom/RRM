package za.co.xisystems.itis_rrm.data.repositories

import android.app.Activity
import android.os.Build
import android.os.Environment
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
//import sun.security.krb5.Confounder.bytes
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.data._commons.views.ToastUtils
import za.co.xisystems.itis_rrm.data.localDB.AppDatabase
import za.co.xisystems.itis_rrm.data.localDB.JobDataController
import za.co.xisystems.itis_rrm.data.localDB.entities.*
import za.co.xisystems.itis_rrm.data.network.BaseConnectionApi
import za.co.xisystems.itis_rrm.data.network.OfflineListener
import za.co.xisystems.itis_rrm.data.network.SafeApiRequest
import za.co.xisystems.itis_rrm.data.network.request.RrmJobRequest
import za.co.xisystems.itis_rrm.data.network.responses.JobResponse
import za.co.xisystems.itis_rrm.data.network.responses.SaveMeasurementResponse
import za.co.xisystems.itis_rrm.data.network.responses.UploadImageResponse
import za.co.xisystems.itis_rrm.data.network.responses.WorkflowMoveResponse
import za.co.xisystems.itis_rrm.data.preferences.PreferenceProvider
import za.co.xisystems.itis_rrm.utils.*
import za.co.xisystems.itis_rrm.utils.PhotoUtil.getPhotoPathFromExternalDirectory
import za.co.xisystems.itis_rrm.utils.enums.PhotoQuality
import za.co.xisystems.itis_rrm.utils.enums.WorkflowDirection
import java.io.File
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.Base64.getEncoder
import java.util.regex.Pattern


/**
 * Created by Francis Mahlava on 2019/11/28.
 */

const val MINIMUM_INTERVAL = 3
private val jobDataController : JobDataController? = null
private val Db: AppDatabase? = null
class OfflineDataRepository(
    private val api: BaseConnectionApi,
    private val Db: AppDatabase,
    private val prefs: PreferenceProvider
) : SafeApiRequest() {
    companion object {
        val TAG: String = OfflineDataRepository::class.java.simpleName
    }



    private val activity: Activity? = null
    private var rListener: OfflineListener? = null
    private val conTracts = MutableLiveData<List<ContractDTO>>()
    //    private val sectionItems = MutableLiveData<ArrayList<SectionItemDTO>>()
    private val sectionItems = MutableLiveData<ArrayList<String>>()
    private val projects = MutableLiveData<ArrayList<ProjectDTO>>()
    private val projectItems = MutableLiveData<ArrayList<ProjectItemDTO>>()
    private val voItems = MutableLiveData<ArrayList<VoItemDTO>>()
    private val projectSections = MutableLiveData<ArrayList<ProjectSectionDTO>>()
    private val job = MutableLiveData<JobDTO>()
    private val new_job = MutableLiveData<JobDTOTemp>()
    private val estimatePhoto = MutableLiveData<String>()
    private val measurePhoto = MutableLiveData<String>()
    private val workFlow = MutableLiveData<WorkFlowsDTO>()
    private val lookups = MutableLiveData<ArrayList<LookupDTO>>()
    private val toDoListGroups = MutableLiveData<ArrayList<ToDoGroupsDTO>>()
    private val workflows = MutableLiveData<ArrayList<ToDoGroupsDTO>>()
    private val workflowJ = MutableLiveData<WorkflowJobDTO>()
    private val workflowJ2 = MutableLiveData<WorkflowJobDTO>()
    private val photoupload = MutableLiveData<String>()
    private val routeSectionPoint = MutableLiveData<String>()


    init {
        conTracts.observeForever {
            saveContracts(it)
        }
        sectionItems.observeForever {
            saveSectionsItems(it)
        }

        workFlow.observeForever {
            saveWorkFlowsInfo(it)
        }

        lookups.observeForever {
            saveLookups(it)
        }
        projectItems.observeForever {
            //            saveProjectItems(it)
        }

        toDoListGroups.observeForever {
            saveUserTaskList(it)
        }

        workflows.observeForever {
            saveTaskList(it)
        }

        job.observeForever {
            saveJobs(it)

        }

        new_job.observeForever {
            processRrmJobResponse(it)

        }
        workflowJ.observeForever {
            //            saveSectionItems(it)
            saveWorkflowJob(it)//
//            saveWorkflowJob2(it)
        }
        workflowJ2.observeForever {
            saveWorkflowJob2(it)
        }

        photoupload.observeForever {
            sendMSg(it)
        }



    }

    private fun processRrmJobResponse(jobDTO: JobDTOTemp) {
        val imageCounter = 1
        val totalImages = 0










    }


    suspend fun getContracts(): LiveData<List<ContractDTO>> {
        return withContext(Dispatchers.IO) {
            //            val userId = Db.getUserDao().getuserID()
//            fetchContracts(userId)
            Db.getContractDao().getAllContracts()
        }
    }

    suspend fun getContractProjects(contractId: String): LiveData<List<ProjectDTO>> {
        return withContext(Dispatchers.IO) {
            Db.getProjectDao().getAllProjectsByContract(contractId)
        }
    }

    suspend fun getProjects(): LiveData<List<ProjectDTO>> {
        return withContext(Dispatchers.IO) {
            Db.getProjectDao().getAllProjects()
        }
    }

    suspend fun getProjectItems(): LiveData<List<ProjectItemDTO>> {
        return withContext(Dispatchers.IO) {
            Db.getProjectItemDao().getAllItemsForAllProjects()
        }
    }

    suspend fun getItemForItemCode(sectionItemId: String): LiveData<List<ProjectItemDTO>> {
        return withContext(Dispatchers.IO) {
            //            val projectId = DataConversion.toLittleEndian( Db.getProjectDao().getProjectId())
            Db.getProjectItemDao().getItemForItemCode(sectionItemId)
        }
    }

    suspend  fun getAllProjecItems(projectId: String): LiveData<List<ItemDTOTemp>> {
        return withContext(Dispatchers.IO) {
            Db.getItemDao_Temp().getAllProjecItems(projectId)
        }
    }

   suspend fun getSection(sectionId: String): LiveData<ProjectSectionDTO> {
        return withContext(Dispatchers.IO) {
            Db.getProjectSectionDao().getSection(sectionId)
        }
    }


    suspend fun getAllItemsForProjectId(projectId: String): LiveData<List<ProjectItemDTO>> {
        return withContext(Dispatchers.IO) {
            //            val projectId = DataConversion.toLittleEndian( Db.getProjectDao().getProjectId())
            Db.getProjectItemDao().getAllItemsForProjectId(projectId)
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

    suspend fun getJobforEstinmate(jobId: String): LiveData<JobDTOTemp> {
        return withContext(Dispatchers.IO) {
            Db.getJobDaoTemp().getJobFromJobId(jobId)
        }
    }

    suspend fun getWorkFlows(): LiveData<List<WorkFlowDTO>> {
        return withContext(Dispatchers.IO) {
            val userId = Db.getUserDao().getuserID()
            fetchAllData(userId)
            Db.getWorkFlowDao().getWorkflows()
        }
    }


    suspend fun getSectionItems(): LiveData<SectionItemDTO> {
        return withContext(Dispatchers.IO) {
            val userId = Db.getUserDao().getuserID()
            fetchContracts(userId)
            Db.getSectionItemDao().getSectionItems()
        }
    }
    suspend fun getJobItemEstimatePhotoForEstimateId(estimateId: String): LiveData<List<JobItemEstimatesPhotoDTO>>  {
        return withContext(Dispatchers.IO) {
            Db.getJobItemEstimatePhotoDao().getJobItemEstimatePhotoForEstimateId(estimateId)
        }
    }
    suspend fun getAllSectionItem(): LiveData<List<SectionItemDTO>> {
        return withContext(Dispatchers.IO) {
            Db.getSectionItemDao().getAllSectionItems()
        }
    }

    suspend fun getPointSectionData(projectId: String?): LiveData<SectionPointDTO> { //jobId,jobId: String,
        return withContext(Dispatchers.IO) {
//            Db.getSectionItemDao().getAllSectionItems()
            Db.getSectionPointDao().getPointSectionData(projectId)
        }
    }

    suspend fun getUser(): LiveData<UserDTO> {
        return withContext(Dispatchers.IO) {
            Db.getUserDao().getuser()
        }
    }

    suspend fun getJobsMeasureForActivityId(
        estimateComplete: Int,
        measureComplete: Int,
        estWorksComplete: Int,
        jobApproved: Int
    ): LiveData<List<JobDTO>> {
        return withContext(Dispatchers.IO) {
            Db.getJobDao().getJobsMeasureForActivityIds(estimateComplete,measureComplete,estWorksComplete,jobApproved)
        }
    }

    suspend fun getJobsForActivityId(activityId: Int): LiveData<List<JobDTO>> {
        return withContext(Dispatchers.IO) {
            Db.getJobDao().getJobsForActivityId(activityId)
        }
    }

    suspend fun getJobsForActId(activityId: Int): LiveData<List<JobDTOTemp>> {
        return withContext(Dispatchers.IO) {
            Db.getJobDaoTemp().getJobsForActivityId(activityId)
        }
    }

    suspend fun getJobsForActivityIds1(activityId1: Int, activityId2: Int): LiveData<List<JobDTO>> {
        return withContext(Dispatchers.IO) {
            Db.getJobDao().getJobsForActivityIds1(activityId1, activityId2)
        }
    }

    suspend fun getJobsForActivityIds(activityId1: Int): LiveData<List<JobItemEstimateDTO>> {
        return withContext(Dispatchers.IO) {
            Db.getJobItemEstimateDao().getJobsForActivityId(activityId1)
        }
    }

    suspend fun getJobMeasureForActivityId(activityId: Int): LiveData<List<JobItemEstimateDTO>> {
        return withContext(Dispatchers.IO) {
            Db.getJobItemEstimateDao().getJobsForActivityId(activityId!!)
        }
    }


    suspend fun getJobItemsToMeasureForJobId(jobID: String?): LiveData<List<JobItemEstimateDTO>> {
        return withContext(Dispatchers.IO) {
            Db.getJobItemEstimateDao().getJobItemsToMeasureForJobId(jobID!!)
        }
    }

    suspend fun getJobApproveMeasureForActivityId(activityId: Int): LiveData<List<JobItemMeasureDTO>> {
        return withContext(Dispatchers.IO) {
            Db.getJobItemMeasureDao().getJobApproveMeasureForActivityId(activityId)
        }
    }

    suspend fun getEntitiesListForActivityId(activityId: Int): LiveData<List<ToDoListEntityDTO>> {
        return withContext(Dispatchers.IO) {
            Db.getEntitiesDao().getEntitiesListForActivityId(activityId)
        }
    }

    suspend fun getJobEstimationItemsPhoto(estimateId: String): LiveData<List<JobItemEstimatesPhotoDTO>> {
        return withContext(Dispatchers.IO) {
            //           val filename =  Db.getJobItemEstimatePhotoDao().getJobEstimationItemsPhotoFilename(estimateId)
//            getPhotoForJobItemEstimate(filename)
            Db.getJobItemEstimatePhotoDao().getJobEstimationItemsPhoto(estimateId)
        }
    }

    suspend fun getJobItemMeasurePhotosForItemMeasureID(itemMeasureId: String): LiveData<List<JobItemMeasurePhotoDTOTemp>>  {
        return withContext(Dispatchers.IO) {
            Db.getJobItemMeasurePhotoDao_Temp().getJobItemMeasurePhotosForItemMeasureID(itemMeasureId)
        }
    }

    suspend fun getJobItemMeasurePhotosForItemEstimateID(estimateId: String): LiveData<List<JobItemMeasurePhotoDTOTemp>>  {
        return withContext(Dispatchers.IO) {
            Db.getJobItemMeasurePhotoDao_Temp().getJobItemMeasurePhotosForItemEstimateID(estimateId)
        }
    }

    suspend fun getJobEstimationItemsPhotoStartPath(estimateId: String): String {
        return withContext(Dispatchers.IO) {
            Db.getJobItemEstimatePhotoDao().getJobEstimationItemsPhotoStartPath(estimateId)
        }
    }

    suspend fun getJobEstimationItemsPhotoEndPath(estimateId: String): String {
        return withContext(Dispatchers.IO) {
            Db.getJobItemEstimatePhotoDao().getJobEstimationItemsPhotoEndPath(estimateId)
        }
    }

    suspend fun getJobMeasureItemsPhotoPath(itemMeasureId: String): String {
        return withContext(Dispatchers.IO) {
            Db.getJobItemMeasurePhotoDao().getJobMeasureItemsPhotoPath(itemMeasureId)
        }
    }

    suspend fun getJobMeasureItemsPhotoPath2(itemMeasureId: String): String {
        return withContext(Dispatchers.IO) {
            Db.getJobItemMeasurePhotoDao_Temp().getJobMeasureItemsPhotoPath(itemMeasureId)
        }
    }

    suspend fun getJobEstimationItemsForJobId(jobID: String?): LiveData<List<JobItemEstimateDTO>> {
        return withContext(Dispatchers.IO) {
            Db.getJobItemEstimateDao().getJobEstimationItemsForJobId(jobID!!)
        }
    }

    suspend fun getJobMeasureItemsForJobId(
        jobID: String?,
        actId: Int
    ): LiveData<List<JobItemMeasureDTO>> {
        return withContext(Dispatchers.IO) {
            Db.getJobItemMeasureDao().getJobMeasureItemsForJobId(jobID!!, actId)
        }
    }

    suspend fun getJobEstiItemForEstimateId(estimateId: String?): LiveData<List<JobEstimateWorksDTO>> {
        return withContext(Dispatchers.IO) {
            Db.getEstimateWorkDao().getJobMeasureItemsForJobId(estimateId)
        }
    }

    suspend fun getSectionByRouteSectionProject(
        sectionId: Int,
        linearId: String?,
        projectId: String?
    ): LiveData<String> {
        return withContext(Dispatchers.IO) {
            Db.getProjectSectionDao().getSectionByRouteSectionProject(sectionId,linearId!!,projectId)
        }

    }


    suspend fun getProjectSection(sectionId: String?): LiveData<ProjectSectionDTO> {
        return withContext(Dispatchers.IO) {
            Db.getProjectSectionDao().getSection(sectionId!!)
        }

    }


    suspend fun getJobItemMeasuresForJobIdAndEstimateId(
        jobId: String?,
        estimateId: String
    ): LiveData<List<JobItemMeasureDTOTemp>>  {
        val jobItemMeasures = Db.getJobItemMeasureDao_Temp().getJobItemMeasuresForJobIdAndEstimateId(jobId,estimateId)
//        if (jobItemMeasures != null) {
//            for (jobItemMeasure in jobItemMeasures) {
//                if (jobItemMeasure != null) {
//                    if (Db.getJobItemMeasurePhotoDao().checkIfJobItemMeasurePhotoExistsForMeasureId(
//                            jobItemMeasure.itemMeasureId
//                        )
//                    )
//
//                        for (itemMeasurePhoto in jobItemMeasure.jobItemMeasurePhotos) {
//                            Db.getJobItemMeasurePhotoDao()
//                                .getJobItemMeasurePhotosForItemMeasureID(jobItemMeasure.itemMeasureId)
//
//                            if (Db.getItemDao().checkItemExistsItemId(jobItemMeasure.projectItemId!!)) {
//                                val selectedItem: LiveData<ItemDTO> =
//                                    Db.getItemDao().getItemForItemId(jobItemMeasure.projectItemId!!)
//                                jobItemMeasure.selectedItemUom = selectedItem.value?.uom
//                            }
//                        }
//
//
//                }
//            }
//        }

        return withContext(Dispatchers.IO) {
            jobItemMeasures!!
        }
    }


    suspend fun getJobItemMeasuresForJobIdAndEstimateId2(
        jobId: String?,
        estimateId: String,
        jobItemMeasureArrayList: ArrayList<JobItemMeasureDTOTemp>
    ): LiveData<List<JobItemMeasureDTOTemp>>  {
        val jobItemMeasures = Db.getJobItemMeasureDao_Temp().getJobItemMeasuresForJobIdAndEstimateId(jobId, estimateId)
        if (jobItemMeasures != null) {
            for (jobItemMeasure in jobItemMeasureArrayList) {
                if (jobItemMeasure != null) {
                    if (Db.getJobItemMeasurePhotoDao_Temp().checkIfJobItemMeasurePhotoExistsForMeasureId(
                            jobItemMeasure.itemMeasureId
                        )
                    )

                        for (itemMeasurePhoto in jobItemMeasure.jobItemMeasurePhotos) {
                            Db.getJobItemMeasurePhotoDao_Temp().getJobItemMeasurePhotosForItemMeasureID(jobItemMeasure.itemMeasureId)

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

    suspend fun getJobItemMeasureForJobId(jobID: String?): LiveData<JobItemMeasureDTOTemp> {
        return withContext(Dispatchers.IO) {
            Db.getJobItemMeasureDao_Temp().getJobItemMeasureForJobId(jobID!!)
        }
    }


    suspend fun getItemForItemId(projectItemId: String?): LiveData<ProjectItemDTO> {
        return withContext(Dispatchers.IO) {
            Db.getProjectItemDao().getItemForItemId(projectItemId!!)
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
        if (!Db.getJobDaoTemp().checkIfJobExist(newjobId)) {
//
        }else{
            Db.getJobDaoTemp().updateJoSecId(
                newjobId,
                startKM,
                endKM,
                sectionId,
                newJobItemEstimatesList,
                jobItemSectionArrayList)
        }
    }
    }

    suspend fun getSingleJobFromJobId(jobId: String?): LiveData<JobDTO> {
        return withContext(Dispatchers.IO) {
            Db.getJobDao().getJobFromJobId(jobId!!)
        }
    }

//    fun getJobEstimateWorksWithPhotos(estimateId: String?): JobEstimateWorksDTO {
//        val jobEstimateWorks: JobEstimateWorksDTO =
//            Db.getEstimateWorkDao().getJobEstimateWorksForEstimateId(estimateId)
//        jobEstimateWorks.jobEstimateWorksPhotos =  Db.getEstimateWorkPhotoDao().getEstimateWorksPhotoForWorksId(jobEstimateWorks.worksId) //as ArrayList<JobEstimateWorksPhotoDTO>
//        return jobEstimateWorks
//    }


    suspend fun jobExists(jobId: String?): Boolean {
        return withContext(Dispatchers.IO) {
            Db.getJobDao().checkIfJobExist(jobId!!)
        }
    }

    suspend fun getProjectSectionIdForJobId(jobId: String?): String {
        return withContext(Dispatchers.IO) {
            Db.getJobSectionDao().getProjectSectionId(jobId!!)
        }
    }

    suspend fun getProjectDescription(projectId: String): String {
        return withContext(Dispatchers.IO) {
            Db.getProjectDao().getProjectDescription(projectId)
        }
    }

    suspend fun getProjectItemDescription(projectItemId: String): String {
        return withContext(Dispatchers.IO) {
            Db.getProjectItemDao().getProjectItemDescription(projectItemId)
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


    suspend fun checkIfJobItemMeasureExistsForJobIdAndEstimateId(
        jobId: String?,
        estimateId: String
    ): Boolean {
        return withContext(Dispatchers.IO) {
            Db.getJobItemMeasureDao()
                .checkIfJobItemMeasureExistsForJobIdAndEstimateId(jobId, estimateId)
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

    suspend fun getSectionForProjectSectionId(sectionId: String?): String {
        return withContext(Dispatchers.IO) {
            Db.getProjectSectionDao().getSectionForProjectSectionId(sectionId!!)
        }
    }

    suspend fun getJobs(): LiveData<List<JobDTO>> {
        return withContext(Dispatchers.IO) {
            Db.getJobDao().getAllJobsForAllProjects()
        }
    }

    suspend fun delete(item: ItemDTOTemp) {
        Coroutines.io {
        Db.getItemDao_Temp().deleteItem(item)
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

    suspend fun deleJobfromList(jobId: String) {
        Coroutines.io {
        Db.getJobDaoTemp().deleteJobForJobId(jobId)
      }
    }


    suspend fun saveNewJob(newjob: JobDTOTemp) {
        Coroutines.io {
            if (newjob != null) {

                if (!Db.getJobDaoTemp().checkIfJobExist(newjob.JobId)) {
//                    newjob.run {
//                        setJobId(DataConversion.toBigEndian(JobId))
//                        setProjectId(DataConversion.toBigEndian(ProjectId))
//                        if (ContractVoId != null){
//                            setContractVoId(DataConversion.toBigEndian(ContractVoId))
//                        }
//                        if (TrackRouteId != null)
//                        setTrackRouteId(DataConversion.toBigEndian(TrackRouteId))
////                        setRoute(route)
//                    }
//                    DataConversion.toBigEndian(newjob.PerfitemGroupId)
//                    DataConversion.toBigEndian(newjob.ProjectVoId)
                    Db.getJobDaoTemp().insertOrUpdateJobs(newjob)
                }

//                if (newjob.JobSections != null && newjob.JobSections!!.size != 0) {
//                    for (jobSection in newjob.JobSections!!) {
//                        if (!Db.getJobSectionDao().checkIfJobSectionExist(jobSection.jobSectionId))
//                            jobSection.setJobSectionId(DataConversion.toBigEndian(jobSection.jobSectionId))
//                        jobSection.setProjectSectionId(DataConversion.toBigEndian(jobSection.projectSectionId))
//                        jobSection.setJobId(DataConversion.toBigEndian(jobSection.jobId))
//                        Db.getJobSectionDao().insertJobSection(
//                            jobSection
//                        )
//
//                    }
//
//                }
//
//                if (newjob.JobItemEstimates != null && newjob.JobItemEstimates!!.size != 0) {
//                    for (jobItemEstimate in newjob.JobItemEstimates!!) {
//                        if (!Db.getJobItemEstimateDao().checkIfJobItemEstimateExist(jobItemEstimate.estimateId)
//                        ) {
//                            jobItemEstimate.setEstimateId(DataConversion.toBigEndian(jobItemEstimate.estimateId))
//                            jobItemEstimate.setJobId(DataConversion.toBigEndian(jobItemEstimate.jobId))
//                            jobItemEstimate.setProjectItemId(
//                                DataConversion.toBigEndian(
//                                    jobItemEstimate.projectItemId
//                                )
//                            )
//                            jobItemEstimate.setTrackRouteId(
//                                DataConversion.toBigEndian(
//                                    jobItemEstimate.trackRouteId
//                                )
//                            )
//                            jobItemEstimate.setProjectVoId(
//                                DataConversion.toBigEndian(
//                                    jobItemEstimate.projectVoId
//                                )
//                            )
//                            Db.getJobItemEstimateDao().insertJobItemEstimate(jobItemEstimate)
//                            Db.getJobDao().setEstimateActId(jobItemEstimate.actId, newjob.JobId)
////                            job.setEstimateActId(jobItemEstimate.actId)
//                            if (jobItemEstimate.jobItemEstimatePhotos != null) {
//                                for (jobItemEstimatePhoto in jobItemEstimate.jobItemEstimatePhotos) {
//                                    if (!Db.getJobItemEstimatePhotoDao().checkIfJobItemEstimatePhotoExistsByPhotoId(
//                                            jobItemEstimatePhoto.photoId
//                                        )
//                                    )
//                                        jobItemEstimatePhoto.setPhotoPath(
//                                            Environment.getExternalStorageDirectory().toString() + File.separator
//                                                    + PhotoUtil.FOLDER + File.separator + jobItemEstimatePhoto.filename
//                                        )
//                                    when (jobItemEstimatePhoto.descr) {
//                                        "photo_start" -> jobItemEstimatePhoto.setIsPhotoStart(true)
//                                        "photo_end" -> jobItemEstimatePhoto.setIsPhotoStart(false)
//                                    }
//                                    jobItemEstimatePhoto.setPhotoId(
//                                        DataConversion.toBigEndian(
//                                            jobItemEstimatePhoto.photoId
//                                        )
//                                    )
//                                    jobItemEstimatePhoto.setEstimateId(
//                                        DataConversion.toBigEndian(
//                                            jobItemEstimatePhoto.estimateId
//                                        )
//                                    )
//                                    Db.getJobItemEstimatePhotoDao().insertJobItemEstimatePhoto(
//                                        jobItemEstimatePhoto
//                                    )
//                                    if (!PhotoUtil.photoExist(jobItemEstimatePhoto.filename)) {
//                                        getPhotoForJobItemEstimate(jobItemEstimatePhoto.filename)
//                                    }
//                                }
//                            }
////                            if (jobItemEstimate.jobEstimateWorks != null) {
////                                for (jobEstimateWorks in jobItemEstimate.jobEstimateWorks) {
////                                    if (!Db.getEstimateWorkDao().checkIfJobEstimateWorksExist(
////                                            jobEstimateWorks.worksId
////                                        )
////                                    ) jobEstimateWorks.setWorksId(
////                                        DataConversion.toBigEndian(
////                                            jobEstimateWorks.worksId
////                                        )
////                                    )
////                                    jobEstimateWorks.setEstimateId(
////                                        DataConversion.toBigEndian(
////                                            jobEstimateWorks.estimateId
////                                        )
////                                    )
////                                    jobEstimateWorks.setTrackRouteId(
////                                        DataConversion.toBigEndian(
////                                            jobEstimateWorks.trackRouteId
////                                        )
////                                    )
////                                    Db.getEstimateWorkDao().insertJobEstimateWorks(
////                                        jobEstimateWorks
////                                    )
////                                    Db.getJobDao()
////                                        .setEstimateWorksActId(jobEstimateWorks.actId, newjob.JobId)
//////                                    job.setEstimateWorksActId(jobEstimateWorks.actId)
////                                    if (jobEstimateWorks.jobEstimateWorksPhotos != null) {
////                                        for (estimateWorksPhoto in jobEstimateWorks.jobEstimateWorksPhotos!!) {
////                                            if (!Db.getEstimateWorkPhotoDao().checkIfEstimateWorksPhotoExist(
////                                                    estimateWorksPhoto.filename
////                                                )
////                                            ) estimateWorksPhoto.setWorksId(
////                                                DataConversion.toBigEndian(
////                                                    estimateWorksPhoto.worksId
////                                                )
////                                            )
////                                            estimateWorksPhoto.setPhotoId(
////                                                DataConversion.toBigEndian(
////                                                    estimateWorksPhoto.photoId
////                                                )
////                                            )
////                                            Db.getEstimateWorkPhotoDao().insertEstimateWorksPhoto(
////                                                estimateWorksPhoto
////                                            )
////                                        }
////                                    }
////                                }
////                            }
//                        }
//                    }
//                }

//                if (newjob.JobItemMeasures != null) {
//                    for (jobItemMeasure in newjob.JobItemMeasures!!) {
//                        if (!Db.getJobItemMeasureDao().checkIfJobItemMeasureExists(jobItemMeasure.itemMeasureId!!)) {
//                            jobItemMeasure.setItemMeasureId(
//                                DataConversion.toBigEndian(
//                                    jobItemMeasure.itemMeasureId
//                                )
//                            )
//                            jobItemMeasure.setJobId(DataConversion.toBigEndian(jobItemMeasure.jobId))
//                            jobItemMeasure.setProjectItemId(
//                                DataConversion.toBigEndian(
//                                    jobItemMeasure.projectItemId
//                                )
//                            )
//                            jobItemMeasure.setMeasureGroupId(
//                                DataConversion.toBigEndian(
//                                    jobItemMeasure.measureGroupId
//                                )
//                            )
//                            jobItemMeasure.setEstimateId(DataConversion.toBigEndian(jobItemMeasure.estimateId))
//                            jobItemMeasure.setProjectVoId(DataConversion.toBigEndian(jobItemMeasure.projectVoId))
//                            jobItemMeasure.setTrackRouteId(DataConversion.toBigEndian(jobItemMeasure.trackRouteId))
//                            jobItemMeasure.setJobNo(newjob.JiNo)
//                            Db.getJobItemMeasureDao().insertJobItemMeasure(jobItemMeasure)
//                            Db.getJobDao().setMeasureActId(jobItemMeasure.actId, newjob.JobId)
////                            job.setMeasureActId(jobItemMeasure.actId)
//                            if (jobItemMeasure.jobItemMeasurePhotos != null) {
//                                for (jobItemMeasurePhoto in jobItemMeasure.jobItemMeasurePhotos) {
//                                    if (!Db.getJobItemMeasurePhotoDao().checkIfJobItemMeasurePhotoExists(
//                                            jobItemMeasurePhoto.filename!!
//                                        )
//                                    ) jobItemMeasurePhoto.setPhotoPath(
//                                        Environment.getExternalStorageDirectory().toString() + File.separator
//                                                + PhotoUtil.FOLDER + File.separator + jobItemMeasurePhoto.filename
//                                    )
//                                    jobItemMeasurePhoto.setPhotoId(
//                                        DataConversion.toBigEndian(
//                                            jobItemMeasurePhoto.photoId
//                                        )
//                                    )
//                                    jobItemMeasurePhoto.setItemMeasureId(
//                                        DataConversion.toBigEndian(
//                                            jobItemMeasurePhoto.itemMeasureId
//                                        )
//                                    )
//                                    Db.getJobItemMeasurePhotoDao().insertJobItemMeasurePhoto(
//                                        jobItemMeasurePhoto
//                                    )
//                                    if (!PhotoUtil.photoExist(jobItemMeasurePhoto.filename))
//                                        getPhotoForJobItemMeasure(jobItemMeasurePhoto.filename)
////                                    else {
////                                        populateAppropriateViewForPhotos()
////                                    }
////                                    if (!PhotoUtil.photoExist(jobItemEstimatePhoto.filename)) {
////                                        getPhotoForJobItemEstimate(jobItemEstimatePhoto.filename)
////                                    }
//                                }
//                            }
//                        }
//                    }
//                }

            }
        }
    }
    private fun saveSectionsItems(sections: ArrayList<String>?) {
        Coroutines.io {

            for (section in sections!!) {
                //  Lets get the String
                val pattern = Pattern.compile("(.*?):")
                val matcher = pattern.matcher(section)

                val sectionItemId = SqlLitUtils.generateUuid()
                val activitySection = section
                if (matcher.find()) {
                    if (section != null) {
                        val itemCode = matcher.group(1).replace("\\s+".toRegex(), "")
                        if (!Db.getSectionItemDao().checkIfSectionitemsExist(itemCode))
                            Db.getSectionItemDao().insertSectionitem(
                                activitySection!!,
                                itemCode!!,
                                sectionItemId!!
                            )
//                            section.setSecctionItemId(sectionItemId)
//                            section.setItemCode(itemCode)
//                            section.setDescription(activitySection)
//                             Db.getSectionItemDao().insertSectionitem(section)


                    }

                }

            }

        }
    }

    //    private fun saveEstimatePhoto(estimatePhoto : ByteArray?) {
//    private fun saveEstimatePhoto(estimatePhoto: String?) {
//        Coroutines.io {
//
//        }
//    }


    private fun saveContracts(contracts: List<ContractDTO>) {
        Coroutines.io {
            //            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                prefs.savelastSavedAt(LocalDateTime.now().toString())
//            }
//            Db.getContractDao().saveAllContracts(contracts)
            if (contracts != null) {
                for (contract in contracts) {
                    if (!Db.getContractDao().checkIfContractExists(contract.contractId))
                        Db.getContractDao().insertContract(contract)
                    if (contract.projects != null) {
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                            prefs.savelastSavedAt(LocalDateTime.now().toString())
//                        }
                        for (project in contract.projects) {
                            if (!Db.getProjectDao().checkProjectExists(project.projectId)) {
                                Db.getProjectDao().insertProject(
                                    project.projectId,
                                    project.descr,
                                    project.endDate,
                                    project.items,
                                    project.projectCode,
                                    project.projectMinus,
                                    project.projectPlus,
                                    project.projectSections,
                                    project.voItems,
                                    contract.contractId
                                )
                            }
                            if (project.items != null) {
//                                val projectId = DataConversion.toLittleEndian(project.projectId)
                                for (item in project.items) {
                                    if (!Db.getProjectItemDao().checkItemExistsItemId(item.itemId)) {
//                                        Db.getItemDao().insertItem(item)
                                        //  Lets get the ID from Sections Items
                                        val pattern = Pattern.compile("(.*?)\\.")
                                        val matcher = pattern.matcher(item.itemCode)
                                        if (matcher.find()) {
                                            val itemCode = matcher.group(1) + "0"
                                            //  Lets Get the ID Back on Match
                                            val sectionItemId = Db.getSectionItemDao().getSectionItemId(itemCode.replace("\\s+".toRegex(), ""))
//                                            val sectionItemId = Db.getSectionItemDao().getSectionItemId(item.itemCode!!)
                                            Db.getProjectItemDao().insertItem(item.itemId, item.itemCode, item.descr, item.itemSections, item.tenderRate, item.uom,
                                                item.workflowId, sectionItemId, item.quantity, item.estimateId, project.projectId)
                                        }
                                    }

                                }
                            }

                            if (project.projectSections != null) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    prefs.savelastSavedAt(LocalDateTime.now().toString())
                                }
                                for (section in project.projectSections) { //project.projectSections
                                    if (!Db.getProjectSectionDao().checkSectionExists(section.sectionId))
//                                        Db.getProjectSectionDao().insertSections(section)
                                        Db.getProjectSectionDao().insertSection(
                                            section.sectionId,
                                            section.route,
                                            section.section,
                                            section.startKm,
                                            section.endKm,
                                            section.direction,
                                            project.projectId
                                        )
                                }
                            }

                            if (project.voItems != null) {
                                for (voItem in project.voItems) { //project.voItems
                                    if (!Db.getVoItemDao().checkIfVoItemExist(voItem.projectVoId))
//                                        Db.getVoItemDao().insertVoItem(voItem)
                                        Db.getVoItemDao().insertVoItem(
                                            voItem.projectVoId,
                                            voItem.itemCode,
                                            voItem.voDescr,
                                            voItem.descr,
                                            voItem.uom,
                                            voItem.rate,
                                            voItem.projectItemId,
                                            voItem.contractVoId,
                                            voItem.contractVoItemId,
                                            project.projectId
                                        )
                                }
                            }


                        }
                    }
                }
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
            if (!Db.getSectionPointDao().checkSectionExists(sectionId,projectId,jobId)){
                Db.getSectionPointDao().insertSection(direction,linearId,pointLocation,sectionId,projectId,jobId)
            }



        }
    }








    private fun saveWorkFlowsInfo(workFlows: WorkFlowsDTO) {
        Coroutines.io {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                prefs.savelastSavedAt(LocalDateTime.now().toString())
            }
            if (workFlows != null)
                Db.getWorkflowsDao().insertWorkFlows(workFlows)
            if (workFlows.workflows != null) {
                for (workFlow in workFlows.workflows) {
                    if (!Db.getWorkFlowDao().checkWorkFlowExistsWorkflowID(workFlow.workflowId))
                        Db.getWorkFlowDao().insertWorkFlow(workFlow)
//                    Db.getWorkFlowDao().insertWorkFlow(workFlow.dateCreated,workFlow.errorRouteId, workFlow.revNo, workFlow.startRouteId, workFlow.userId,
//                        workFlow.wfHeaderId, workFlow.workFlowRoute, workFlow.workflowId)

                    if (workFlow.workFlowRoute != null) {
                        for (workFlowRoute in workFlow.workFlowRoute!!) {  //ArrayList<WorkFlowRouteDTO>()
                            if (!Db.getWorkFlowRouteDao().checkWorkFlowRouteExists(workFlowRoute.routeId))
//                                Db.getWorkFlowRouteDao().insertWorkFlowRoutes(
//                                    workFlowRoute )
                                Db.getWorkFlowRouteDao().insertWorkFlowRoute(
                                    workFlowRoute.routeId,
                                    workFlowRoute.actId,
                                    workFlowRoute.nextRouteId,
                                    workFlowRoute.failRouteId,
                                    workFlowRoute.errorRouteId,
                                    workFlowRoute.canStart,
                                    workFlow.workflowId
                                )
                        }
                    }
                }
            }

            if (workFlows.activities != null) {
                for (activity in workFlows.activities) {
                    Db.getActivityDao().insertActivitys(activity)
//                    Db.getActivityDao().insertActivity( activity.actId,  activity.actTypeId, activity.approvalId, activity.sContentId,  activity.actName, activity.descr )
                }
            }

            if (workFlows.infoClasses != null) {
                for (infoClass in workFlows.infoClasses) {
                    Db.getInfoClassDao().insertInfoClasses(infoClass)
//                    Db.getInfoClassDao().insertInfoClass(infoClass.sLinkId, infoClass.sInfoClassId,  infoClass.wfId)
                }
            }
        }
    }

    private fun saveJobs(job: JobDTO?) {
        Coroutines.io {
            if (job != null) {

                if (!Db.getJobDao().checkIfJobExist(job.JobId)) {
                    job.run {
                        setJobId(DataConversion.toBigEndian(JobId))
                        setProjectId(DataConversion.toBigEndian(ProjectId))
                        if (ContractVoId != null){
                            setContractVoId(DataConversion.toBigEndian(ContractVoId))
                        }
                        setTrackRouteId(DataConversion.toBigEndian(TrackRouteId))
//                        setRoute(route)
                    }
                    DataConversion.toBigEndian(job.PerfitemGroupId)
                    DataConversion.toBigEndian(job.ProjectVoId)
                    Db.getJobDao().insertOrUpdateJobs(job)
                }

                if (job.JobSections != null && job.JobSections!!.size != 0) {
                    for (jobSection in job.JobSections!!) {
                        if (!Db.getJobSectionDao().checkIfJobSectionExist(jobSection.jobSectionId))
                            jobSection.setJobSectionId(DataConversion.toBigEndian(jobSection.jobSectionId))
                        jobSection.setProjectSectionId(DataConversion.toBigEndian(jobSection.projectSectionId))
                        jobSection.setJobId(DataConversion.toBigEndian(jobSection.jobId))
                        Db.getJobSectionDao().insertJobSection(
                            jobSection
                        )

                    }

                }

                if (job.JobItemEstimates != null && job.JobItemEstimates!!.size != 0) {
                    for (jobItemEstimate in job.JobItemEstimates!!) {
                        if (!Db.getJobItemEstimateDao().checkIfJobItemEstimateExist(jobItemEstimate.estimateId)
                        ) {
                            jobItemEstimate.setEstimateId(DataConversion.toBigEndian(jobItemEstimate.estimateId))
                            jobItemEstimate.setJobId(DataConversion.toBigEndian(jobItemEstimate.jobId))
                            jobItemEstimate.setProjectItemId(
                                DataConversion.toBigEndian(
                                    jobItemEstimate.projectItemId
                                )
                            )
                            jobItemEstimate.setTrackRouteId(
                                DataConversion.toBigEndian(
                                    jobItemEstimate.trackRouteId
                                )
                            )
                            jobItemEstimate.setProjectVoId(
                                DataConversion.toBigEndian(
                                    jobItemEstimate.projectVoId
                                )
                            )
                            Db.getJobItemEstimateDao().insertJobItemEstimate(jobItemEstimate)
                            Db.getJobDao().setEstimateActId(jobItemEstimate.actId, job.JobId)
//                            job.setEstimateActId(jobItemEstimate.actId)
                            if (jobItemEstimate.jobItemEstimatePhotos != null) {
                                for (jobItemEstimatePhoto in jobItemEstimate.jobItemEstimatePhotos!!) {
                                    if (!Db.getJobItemEstimatePhotoDao().checkIfJobItemEstimatePhotoExistsByPhotoId(
                                            jobItemEstimatePhoto.photoId
                                        )
                                    )
                                        jobItemEstimatePhoto.setPhotoPath(
                                            Environment.getExternalStorageDirectory().toString() + File.separator
                                                    + PhotoUtil.FOLDER + File.separator + jobItemEstimatePhoto.filename
                                        )
                                    when (jobItemEstimatePhoto.descr) {
                                        "photo_start" -> jobItemEstimatePhoto.setIsPhotoStart(true)
                                        "photo_end" -> jobItemEstimatePhoto.setIsPhotoStart(false)
                                    }
                                    jobItemEstimatePhoto.setPhotoId(
                                        DataConversion.toBigEndian(
                                            jobItemEstimatePhoto.photoId
                                        )
                                    )
                                    jobItemEstimatePhoto.setEstimateId(
                                        DataConversion.toBigEndian(
                                            jobItemEstimatePhoto.estimateId
                                        )
                                    )
                                    Db.getJobItemEstimatePhotoDao().insertJobItemEstimatePhoto(
                                        jobItemEstimatePhoto
                                    )
                                    if (!PhotoUtil.photoExist(jobItemEstimatePhoto.filename)) {
                                        getPhotoForJobItemEstimate(jobItemEstimatePhoto.filename)
                                    }
                                }
                            }
                            if (jobItemEstimate.jobEstimateWorks != null) {
                                for (jobEstimateWorks in jobItemEstimate.jobEstimateWorks!!) {
                                    if (!Db.getEstimateWorkDao().checkIfJobEstimateWorksExist(
                                            jobEstimateWorks.worksId
                                        )
                                    ) jobEstimateWorks.setWorksId(
                                        DataConversion.toBigEndian(
                                            jobEstimateWorks.worksId
                                        )
                                    )
                                    jobEstimateWorks.setEstimateId(
                                        DataConversion.toBigEndian(
                                            jobEstimateWorks.estimateId
                                        )
                                    )
                                    jobEstimateWorks.setTrackRouteId(
                                        DataConversion.toBigEndian(
                                            jobEstimateWorks.trackRouteId
                                        )
                                    )
                                    Db.getEstimateWorkDao().insertJobEstimateWorks(
                                        jobEstimateWorks
                                    )
                                    Db.getJobDao()
                                        .setEstimateWorksActId(jobEstimateWorks.actId, job.JobId)
//                                    job.setEstimateWorksActId(jobEstimateWorks.actId)
                                    if (jobEstimateWorks.jobEstimateWorksPhotos != null) {
                                        for (estimateWorksPhoto in jobEstimateWorks.jobEstimateWorksPhotos!!) {
                                            if (!Db.getEstimateWorkPhotoDao().checkIfEstimateWorksPhotoExist(
                                                    estimateWorksPhoto.filename
                                                )
                                            ) estimateWorksPhoto.setWorksId(
                                                DataConversion.toBigEndian(
                                                    estimateWorksPhoto.worksId
                                                )
                                            )
                                            estimateWorksPhoto.setPhotoId(
                                                DataConversion.toBigEndian(
                                                    estimateWorksPhoto.photoId
                                                )
                                            )
                                            Db.getEstimateWorkPhotoDao().insertEstimateWorksPhoto(
                                                estimateWorksPhoto
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (job.JobItemMeasures != null) {
                    for (jobItemMeasure in job.JobItemMeasures!!) {
                        if (!Db.getJobItemMeasureDao().checkIfJobItemMeasureExists(jobItemMeasure.itemMeasureId!!)) {
                            jobItemMeasure.setItemMeasureId(
                                DataConversion.toBigEndian(
                                    jobItemMeasure.itemMeasureId
                                )
                            )
                            jobItemMeasure.setJobId(DataConversion.toBigEndian(jobItemMeasure.jobId))
                            jobItemMeasure.setProjectItemId(
                                DataConversion.toBigEndian(
                                    jobItemMeasure.projectItemId
                                )
                            )
                            jobItemMeasure.setMeasureGroupId(
                                DataConversion.toBigEndian(
                                    jobItemMeasure.measureGroupId
                                )
                            )
                            jobItemMeasure.setEstimateId(DataConversion.toBigEndian(jobItemMeasure.estimateId))
                            jobItemMeasure.setProjectVoId(DataConversion.toBigEndian(jobItemMeasure.projectVoId))
                            jobItemMeasure.setTrackRouteId(DataConversion.toBigEndian(jobItemMeasure.trackRouteId))
                            jobItemMeasure.setJobNo(job.JiNo)
                            Db.getJobItemMeasureDao().insertJobItemMeasure(jobItemMeasure)
                            Db.getJobDao().setMeasureActId(jobItemMeasure.actId, job.JobId)
//                            job.setMeasureActId(jobItemMeasure.actId)
                            if (jobItemMeasure.jobItemMeasurePhotos != null) {
                                for (jobItemMeasurePhoto in jobItemMeasure.jobItemMeasurePhotos) {
                                    if (!Db.getJobItemMeasurePhotoDao().checkIfJobItemMeasurePhotoExists(
                                            jobItemMeasurePhoto.filename!!
                                        )
                                    ) jobItemMeasurePhoto.setPhotoPath(
                                        Environment.getExternalStorageDirectory().toString() + File.separator
                                                + PhotoUtil.FOLDER + File.separator + jobItemMeasurePhoto.filename
                                    )
                                    jobItemMeasurePhoto.setPhotoId(
                                        DataConversion.toBigEndian(
                                            jobItemMeasurePhoto.photoId
                                        )
                                    )
                                    jobItemMeasurePhoto.setItemMeasureId(
                                        DataConversion.toBigEndian(
                                            jobItemMeasurePhoto.itemMeasureId
                                        )
                                    )
                                    Db.getJobItemMeasurePhotoDao().insertJobItemMeasurePhoto(
                                        jobItemMeasurePhoto
                                    )
                                    if (!PhotoUtil.photoExist(jobItemMeasurePhoto.filename))
                                        getPhotoForJobItemMeasure(jobItemMeasurePhoto.filename)
//                                    else {
//                                        populateAppropriateViewForPhotos()
//                                    }
//                                    if (!PhotoUtil.photoExist(jobItemEstimatePhoto.filename)) {
//                                        getPhotoForJobItemEstimate(jobItemEstimatePhoto.filename)
//                                    }
                                }
                            }
                        }
                    }
                }

            }
        }
    }

    suspend fun getPhotoForJobItemMeasure(filename: String) {
        try {
            val photoMeasure = apiRequest { api.getPhotoMeasure(filename) }
            measurePhoto.postValue(photoMeasure.photo, filename)
            ToastUtils().toastLong(activity, "You do not have an active data connection ")
        } catch (e: ApiException) {
            ToastUtils().toastLong(activity, e.message)
        } catch (e: NoInternetException) {
            ToastUtils().toastLong(activity, e.message)
            Log.e("NetworkConnection", "No Internet Connection", e)
        }
    }

    private suspend fun getPhotoForJobItemEstimate(filename: String) {
        try {
            val photoEstimate = apiRequest { api.getPhotoEstimate(filename) }
            estimatePhoto.postValue(photoEstimate.photo, filename)
            ToastUtils().toastLong(activity, "You do not have an active data connection ")
        } catch (e: ApiException) {
            ToastUtils().toastLong(activity, e.message)
        } catch (e: NoInternetException) {
            ToastUtils().toastLong(activity, e.message)
            Log.e("NetworkConnection", "No Internet Connection", e)
        }
    }

    private fun sendMSg(uploadResponse : String?) {
        val response: UploadImageResponse? = null
        if (uploadResponse.isNullOrEmpty())
        jobDataController?.setMsg(response!!.errorMessage)
    }

    private fun saveWorkflowJob(workflowj: WorkflowJobDTO?) {
        val response: SaveMeasurementResponse? = null

        if (workflowj != null) {
            val job: WorkflowJobDTO? =
                jobDataController?.setWorkflowJobBigEndianGuids(response!!.getWorkflowJob()!!)
            insertOrUpdateWorkflowJobInSQLite(workflowj)
//            saveUserTaskList(toDoList)
            val packageItemMeasure = workflowj
//            uploadImages(packageItemMeasure)
        }
    }

    private fun saveWorkflowJob2(workflowj: WorkflowJobDTO?) {
//        val response: JobResponse? = null
//
//        if (workflowj != null) {
//            val job: WorkflowJob? =
//                jobDataController?.setWorkflowJobBigEndianGuids(response!!.getWorkflowJob()!!)
//            insertOrUpdateWorkflowJobInSQLite(workflowj)
////            saveUserTaskList(toDoList)
//            val packagejob = workflowj
//
//        }
    }

//    private fun uploadImages(packageItemMeasure : ArrayList<JobItemMeasureDTO> ) {
//        var imageCounter = 0
//        var totalImages = 0
//
//        for (jim in jobForJobItemEstimate.getJobItemMeasures()) {
//            for (jimp in jim.getPrjItemMeasurePhotoDtos()) {
//                if (PhotoUtil.photoExist(jimp.getFilename())) //  Get Total Images
//                    totalImages++
//            }
//        }
//
//        for (jim in jobForJobItemEstimate.getJobItemMeasures()) {
//            for (jimp in jim.getPrjItemMeasurePhotoDtos()) {
//                if (PhotoUtil.photoExist(jimp.getFilename())) //  Add to Counter Specific Photo
//                    imageCounter++
//                uploadRrmImage(jimp.getFilename(), imageCounter, totalImages, packageItemMeasure)
//            }
//        }
//    }

//    private fun uploadRrmImage(filename: String, imageCounter: Int, totalImages: Int, packageItemMeasure: ArrayList<JobItemMeasureDTO>) {
//
//        val messages = arrayOf<String>(
//            activity!!.getString(R.string.uploading_photo) + imageCounter + activity!!.getString(R.string.new_line) +
//                    activity!!.getString(R.string.please_wait)
//        )
//        val bitmap = PhotoUtil.getPhotoBitmapFromFile(
//            activity!!.getApplicationContext(),
//            PhotoUtil.getPhotoPathFromExternalDirectory(activity!!.getApplicationContext(), filename),
//            PhotoQuality.HIGH
//        )
//        val data =
//            PhotoUtil.getCompressedPhotoWithExifInfo(activity!!.getApplicationContext(), bitmap!!, filename)
//        processData(UploadImageResponse response)
//    }

//    private fun processData(any: Any) {
//        try {
//            val photoMeasure = apiRequest { api.getPhotoMeasure(filename) }
//            measurePhoto.postValue(photoMeasure.photo, filename)
//            ToastUtils().toastLong(activity, "You do not have an active data connection ")
//        } catch (e: ApiException) {
//            ToastUtils().toastLong(activity, e.message)
//        } catch (e: NoInternetException) {
//            ToastUtils().toastLong(activity, e.message)
//            Log.e("NetworkConnection", "No Internet Connection", e)
//        }
//    }

    private fun saveTaskList(toDoListGroups: ArrayList<ToDoGroupsDTO>?) {
        val response: WorkflowMoveResponse? = null
        if (toDoListGroups != null) {
            val job: WorkflowJobDTO? =
                jobDataController?.setWorkflowJobBigEndianGuids(response!!.getWorkflowJob()!!)
            insertOrUpdateWorkflowJobInSQLite(job)
            saveUserTaskList(toDoListGroups)

        }
    }

    private fun saveUserTaskList(toDoListGroups: ArrayList<ToDoGroupsDTO>?) {
        Coroutines.io {
            if (toDoListGroups != null) {
                for (toDoListGroup in toDoListGroups) {
                    if (!Db.getToDoGroupsDao().checkIfGroupCollectionExist(toDoListGroup.groupId)) {
                        Db.getToDoGroupsDao().insertToDoGroups(toDoListGroup)
//                   TODO(this is done with the line above)
//                        Db.getToDoGroupsDao().insertToDoGroups(
//                            toDoListGroup.getGroupId(), toDoListGroup.getGroupDescription(),
//                            toDoListGroup.getGroupName(), toDoListGroup.getSortOrder()
//

//                        )
                    }

                    val entitiesArrayList = toDoListGroup.toDoListEntities
//                    for (entities in entitiesArrayList) {
//                        if (!Db.getPrimaryKeyValueDao().checkPrimaryKeyValuesExistTrackRouteId( Util.ByteArrayToStringUUID( entities.trackRouteId )!!)
//                        ) {
//                            val primaryKeyValue = Db.getPrimaryKeyValueDao().getPrimaryKeyValuesFromTrackRouteId(Util.ByteArrayToStringUUID(entities.trackRouteId)!!)
//
//                               if (primaryKeyValue != null) {
//                                   var job : JobDTO? = null
//                                   job?.setJobId(Util.ByteArrayToStringUUID(primaryKeyValue.p_value))
//                                   job?.setDescr(entities.description)
////                                   val job_Id = DataConversion.toLittleEndian(jobId!!)
//                                   fetchJobList(Util.ByteArrayToStringUUID(primaryKeyValue.p_value)!!)
////                                   if (!ApproveJobsFragment.jobArrayContains(
////                                           job.getJobId(),
////                                           jobList
////                                       )
////                                   ) jobList.add(job)
//                               }
//
//
//
//
//                        }
//                    }

                    for (toDoListEntity in entitiesArrayList) {
                        val jobId = getJobIdFromPrimaryKeyValues(toDoListEntity.primaryKeyValues)
                        insertEntity(toDoListEntity, jobId!!)
                        val job_Id = DataConversion.toLittleEndian(jobId)
                        fetchJobList(job_Id!!)
//                        for (subEntity in toDoListEntity.entities) {
//                            insertEntity(subEntity, jobId)
////                            val job_Id = DataConversion.toLittleEndian(jobId!!)
////                            fetchJobList(job_Id!!)
//                        }

                    }

                }

            }
        }
    }

    private fun insertEntity(entity: ToDoListEntityDTO, jobId: String) {
        Coroutines.io {




            if (!Db.getEntitiesDao().checkIfEntitiesExist(DataConversion.bigEndianToString(entity.trackRouteId!!))) {
                Db.getEntitiesDao().insertEntitie(
                    DataConversion.bigEndianToString(entity.trackRouteId!!)
                    ,
                    if (entity.actionable) 1 else 0,
                    entity.activityId,
                    entity.currentRouteId,
                    entity.data,
                    entity.description,
                    entity.entities,
                    entity.entityName,
                    entity.location,
                    entity.primaryKeyValues,
                    entity.recordVersion!!,
                    jobId
                )

                for (primaryKeyValue in entity.primaryKeyValues) {
                    Db.getPrimaryKeyValueDao().insertPrimaryKeyValue(
                        primaryKeyValue.primary_key,
                        DataConversion.bigEndianToString(primaryKeyValue.p_value!!),
                        DataConversion.bigEndianToString(entity.trackRouteId!!),
                        entity.activityId
                    )
                }

            }
        }
    }

    private fun getJobIdFromPrimaryKeyValues(primaryKeyValues: ArrayList<PrimaryKeyValueDTO>): String? {
        for (primaryKeyValue in primaryKeyValues) {
            if (primaryKeyValue.primary_key!!.contains("JobId")) {
                return DataConversion.bigEndianToString(primaryKeyValue.p_value!!)
            }
        }
        return null
    }

    private suspend fun fetchJobList(jobId: String) {
        try {
            val jobResponse = apiRequest { api.getJobsForApproval(jobId) }
            job.postValue(jobResponse.job)
            ToastUtils().toastLong(activity, "You do not have an active data connection ")
        } catch (e: ApiException) {
            ToastUtils().toastLong(activity, e.message)
        } catch (e: NoInternetException) {
            ToastUtils().toastLong(activity, e.message)
        }
    }

    suspend fun processWorkflowMove(
        userId: String,
        trackRouteId: String,
        description: String?,
        direction: Int
    ) {
        val workflowMoveResponse =
            apiRequest { api.getWorkflowMove(userId, trackRouteId, description, direction) }
        workflows.postValue(workflowMoveResponse.toDoListGroups)
    }

    suspend fun getRouteSectionPoint(
        latitude: Double,
        longitude: Double,
        useR: String,
        projectId: String?,
        jobId: String,
        itemCode: ItemDTOTemp?
    ) {
        val routeSectionPointResponse = apiRequest { api.getRouteSectionPoint(latitude,longitude,useR) }
        routeSectionPoint.postValue(routeSectionPointResponse.direction,routeSectionPointResponse.linearId,routeSectionPointResponse.pointLocation,routeSectionPointResponse.sectionId, projectId, jobId, itemCode)
    }

    suspend fun submitJob(userId: Int, job: JobDTOTemp, activity: FragmentActivity) : String {

        val jobhead = JsonObject()
        val jobdata = JsonObject()
        val jobestimatedata = JsonObject()
        val array = JsonArray()
        array.add(jobestimatedata)
        val jobestimateimagesdata1 = JsonObject()
        val jobestimateimagesdata2 = JsonObject()
        val array4 = JsonArray()
        array4.add(jobestimateimagesdata1)
        array4.add(jobestimateimagesdata2)
//        jobItemEstimatePhotoEnd
//        jobItemEstimatePhotoStart
        jobestimatedata.addProperty("selectedItemUOM", job.JobItemEstimates?.get(0)?.SelectedItemUOM )
        jobestimatedata.addProperty("estimateComplete",job.JobItemEstimates?.get(0)?.estimateComplete )
        jobestimatedata.addProperty("ActId", job.JobItemEstimates?.get(0)?.actId )
        jobestimatedata.addProperty("EstimateId",  DataConversion.toLittleEndian( job.JobItemEstimates?.get(0)?.estimateId ))
        jobestimatedata.addProperty("PrjJobDto", job.JobItemEstimates?.get(0)?.job.toString() )
        jobestimatedata.addProperty("JobId",  DataConversion.toLittleEndian( job.JobItemEstimates?.get(0)?.jobId ))
        jobestimatedata.add("MobileJobItemEstimatesPhotos", array4)
        jobestimatedata.addProperty("LineRate", job.JobItemEstimates?.get(0)?.lineRate )
        jobestimatedata.addProperty("MobileEstimateWorks",  job.JobItemEstimates?.get(0)?.jobEstimateWorks?.toString() )
        jobestimatedata.addProperty("MobileJobItemMeasures",  job.JobItemEstimates?.get(0)?.jobItemMeasure.toString() )
        jobestimatedata.addProperty("ProjectItemId",   DataConversion.toLittleEndian( job.JobItemEstimates?.get(0)?.projectItemId ))
        jobestimatedata.addProperty("ProjectVoId",  job.JobItemEstimates?.get(0)?.projectVoId )
        jobestimatedata.addProperty("Qty",  job.JobItemEstimates?.get(0)?.qty )
        jobestimatedata.addProperty("RecordSynchStateId",  job.JobItemEstimates?.get(0)?.recordSynchStateId )
        jobestimatedata.addProperty("RecordVersion",  job.JobItemEstimates?.get(0)?.recordVersion )
        jobestimatedata.addProperty("TrackRouteId",  job.JobItemEstimates?.get(0)?.trackRouteId )


        jobestimateimagesdata1.addProperty("Descr", job.JobItemEstimates?.get(0)?.jobItemEstimatePhotos?.get(0)?.descr )
        jobestimateimagesdata1.addProperty("Endkm",job.JobItemEstimates?.get(0)?.jobItemEstimatePhotos?.get(0)?.endKm )
        jobestimateimagesdata1.addProperty("EstimateId", DataConversion.toLittleEndian( job.JobItemEstimates?.get(0)?.jobItemEstimatePhotos?.get(0)?.estimateId ))
        jobestimateimagesdata1.addProperty("Filename", job.JobItemEstimates?.get(0)?.jobItemEstimatePhotos?.get(0)?.filename )
        jobestimateimagesdata1.addProperty("IsPhotoStart", job.JobItemEstimates?.get(0)?.jobItemEstimatePhotos?.get(0)?.is_PhotoStart )
        jobestimateimagesdata1.addProperty("PrjJobItemEstimateDto", job.JobItemEstimates?.get(0)?.jobItemEstimatePhotos?.get(0)?.jobItemEstimate.toString() )
        jobestimateimagesdata1.addProperty("PhotoDate", job.JobItemEstimates?.get(0)?.jobItemEstimatePhotos?.get(0)?.photoDate )
        jobestimateimagesdata1.addProperty("PhotoEnd",  job.JobItemEstimates?.get(0)?.jobItemEstimatePhotos?.get(0)?.photoEnd )
        jobestimateimagesdata1.addProperty("PhotoId",  DataConversion.toLittleEndian( job.JobItemEstimates?.get(0)?.jobItemEstimatePhotos?.get(0)?.photoId ))
        jobestimateimagesdata1.addProperty("PhotoLatitude",  job.JobItemEstimates?.get(0)?.jobItemEstimatePhotos?.get(0)?.photoLatitude )
        jobestimateimagesdata1.addProperty("PhotoLatitudeEnd",  job.JobItemEstimates?.get(0)?.jobItemEstimatePhotos?.get(0)?.photoLatitudeEnd )
        jobestimateimagesdata1.addProperty("PhotoLongitude",  job.JobItemEstimates?.get(0)?.jobItemEstimatePhotos?.get(0)?.photoLongitude )
        jobestimateimagesdata1.addProperty("PhotoLongitudeEnd",  job.JobItemEstimates?.get(0)?.jobItemEstimatePhotos?.get(0)?.photoLongitudeEnd )
        jobestimateimagesdata1.addProperty("PhotoPath",  job.JobItemEstimates?.get(0)?.jobItemEstimatePhotos?.get(0)?.photoPath )
        jobestimateimagesdata1.addProperty("PhotoStart",  job.JobItemEstimates?.get(0)?.jobItemEstimatePhotos?.get(0)?.photoStart )
        jobestimateimagesdata1.addProperty("RecordSynchStateId",  job.JobItemEstimates?.get(0)?.jobItemEstimatePhotos?.get(0)?.recordSynchStateId )
        jobestimateimagesdata1.addProperty("RecordVersion",  job.JobItemEstimates?.get(0)?.jobItemEstimatePhotos?.get(0)?.recordVersion )
        jobestimateimagesdata1.addProperty("Startkm",  job.JobItemEstimates?.get(0)?.jobItemEstimatePhotos?.get(0)?.startKm )

        jobestimateimagesdata2.addProperty("Descr", job.JobItemEstimates?.get(0)?.jobItemEstimatePhotos?.get(1)?.descr )
        jobestimateimagesdata2.addProperty("Endkm",job.JobItemEstimates?.get(0)?.jobItemEstimatePhotos?.get(1)?.endKm )
        jobestimateimagesdata2.addProperty("EstimateId", DataConversion.toLittleEndian( job.JobItemEstimates?.get(0)?.jobItemEstimatePhotos?.get(1)?.estimateId ))
        jobestimateimagesdata2.addProperty("Filename", job.JobItemEstimates?.get(0)?.jobItemEstimatePhotos?.get(1)?.filename )
        jobestimateimagesdata2.addProperty("IsPhotoStart", job.JobItemEstimates?.get(0)?.jobItemEstimatePhotos?.get(1)?.is_PhotoStart )
        jobestimateimagesdata2.addProperty("PrjJobItemEstimateDto", job.JobItemEstimates?.get(0)?.jobItemEstimatePhotos?.get(1)?.jobItemEstimate.toString() )
        jobestimateimagesdata2.addProperty("PhotoDate", job.JobItemEstimates?.get(0)?.jobItemEstimatePhotos?.get(1)?.photoDate )
        jobestimateimagesdata2.addProperty("PhotoEnd",  job.JobItemEstimates?.get(0)?.jobItemEstimatePhotos?.get(1)?.photoEnd )
        jobestimateimagesdata2.addProperty("PhotoId",  DataConversion.toLittleEndian( job.JobItemEstimates?.get(0)?.jobItemEstimatePhotos?.get(1)?.photoId ))
        jobestimateimagesdata2.addProperty("PhotoLatitude",  job.JobItemEstimates?.get(0)?.jobItemEstimatePhotos?.get(1)?.photoLatitude )
        jobestimateimagesdata2.addProperty("PhotoLatitudeEnd",  job.JobItemEstimates?.get(0)?.jobItemEstimatePhotos?.get(1)?.photoLatitudeEnd )
        jobestimateimagesdata2.addProperty("PhotoLongitude",  job.JobItemEstimates?.get(0)?.jobItemEstimatePhotos?.get(1)?.photoLongitude )
        jobestimateimagesdata2.addProperty("PhotoLongitudeEnd",  job.JobItemEstimates?.get(0)?.jobItemEstimatePhotos?.get(1)?.photoLongitudeEnd )
        jobestimateimagesdata2.addProperty("PhotoPath",  job.JobItemEstimates?.get(0)?.jobItemEstimatePhotos?.get(1)?.photoPath )
        jobestimateimagesdata2.addProperty("PhotoStart",  job.JobItemEstimates?.get(0)?.jobItemEstimatePhotos?.get(1)?.photoStart )
        jobestimateimagesdata2.addProperty("RecordSynchStateId",  job.JobItemEstimates?.get(0)?.jobItemEstimatePhotos?.get(1)?.recordSynchStateId )
        jobestimateimagesdata2.addProperty("RecordVersion",  job.JobItemEstimates?.get(0)?.jobItemEstimatePhotos?.get(1)?.recordVersion )
        jobestimateimagesdata2.addProperty("Startkm",  job.JobItemEstimates?.get(0)?.jobItemEstimatePhotos?.get(1)?.startKm )


        val jobmeasuredata = JsonObject()
        val array1 = JsonArray()
         //array1.add(jobmeasuredata)//job.JobItemEstimates.toString()
        val jobesectiondata = JsonObject()
        val array2 = JsonArray()
        array2.add(jobesectiondata)
        jobesectiondata.addProperty("EndKm", job.JobSections?.get(0)?.endKm )
        jobesectiondata.addProperty("PrjJobDto", job.JobSections?.get(0)?.job.toString() )
        jobesectiondata.addProperty("JobId", DataConversion.toLittleEndian( job.JobSections?.get(0)?.jobId))
        jobesectiondata.addProperty("JobSectionId", DataConversion.toLittleEndian( job.JobSections?.get(0)?.jobSectionId ))
        jobesectiondata.addProperty("ProjectSectionId", DataConversion.toLittleEndian( job.JobSections?.get(0)?.projectSectionId ))
        jobesectiondata.addProperty("RecordSynchStateId", job.JobSections?.get(0)?.recordSynchStateId )
        jobesectiondata.addProperty("RecordVersion", job.JobSections?.get(0)?.recordVersion )
        jobesectiondata.addProperty("StartKm", job.JobSections?.get(0)?.startKm )

//        array.add(job.JobItemEstimates.toString())
        jobdata.addProperty("ActId", job.ActId )
        jobdata.addProperty("IssueDate", job.IssueDate.toString() )
        jobdata.addProperty("DueDate", job.DueDate.toString() )
        jobdata.addProperty("StartDate", job.StartDate.toString() )
        jobdata.addProperty("ApprovalDate", job.ApprovalDate.toString() )
        jobdata.addProperty("ContractVoId", DataConversion.toLittleEndian( job.ContractVoId ))
        jobdata.addProperty("ContractorId", job.ContractorId )
        jobdata.addProperty("Cpa", job.Cpa )
        jobdata.addProperty("DayWork", job.DayWork )
        jobdata.addProperty("Descr", job.Descr )
        jobdata.addProperty("EndKm", job.EndKm )
        jobdata.addProperty("EngineerId", job.EngineerId )
        jobdata.addProperty("EntireRoute", job.EntireRoute )
        jobdata.addProperty("IsExtraWork", job.IsExtraWork )
        jobdata.addProperty("JiNo", job.JiNo )
        jobdata.addProperty("JobCategoryId", job.JobCategoryId )
        jobdata.addProperty("JobDirectionId", job.JobDirectionId )
        jobdata.addProperty("JobId", DataConversion.toLittleEndian( job.JobId  ))
        jobdata.add("MobileJobItemEstimates", array )
        jobdata.add("MobileJobItemMeasures", array1 )
        jobdata.addProperty("JobPositionId", job.JobPositionId )
        jobdata.add("MobileJobSections", array2 )
        jobdata.addProperty("JobStatusId", job.JobStatusId )
        jobdata.addProperty("M9100", job.M9100 )
        jobdata.addProperty("PerfitemGroupId", job.PerfitemGroupId )
        jobdata.addProperty("ProjectId", DataConversion.toLittleEndian( job.ProjectId ))
        jobdata.addProperty("ProjectVoId", job.ProjectVoId )
        jobdata.addProperty("QtyUpdateAllowed", job.QtyUpdateAllowed )
        jobdata.addProperty("RecordSynchStateId", job.RecordSynchStateId )
        jobdata.addProperty("RecordVersion", job.RecordVersion )
        jobdata.addProperty("Remarks", job.Remarks )
        jobdata.addProperty("Route", job.Route )
        jobdata.addProperty("RrmJiNo", job.RrmJiNo )
        jobdata.addProperty("Section", job.Section)
        jobdata.addProperty("SectionId", job.SectionId )
        jobdata.addProperty("StartKm", job.StartKm )
        jobdata.addProperty("TrackRouteId", job.TrackRouteId )
        jobdata.addProperty("UserId", job.UserId )
        jobdata.addProperty("VoId", job.VoId )
        jobdata.addProperty("WorkCompleteDate", job.WorkCompleteDate )
        jobdata.addProperty("WorkStartDate", job.WorkStartDate )
//
        jobhead.add("Job", jobdata)
        jobhead.addProperty("UserId", userId)
//        var jsonk = gson.toJson(json2)
        Log.e("JsonObject", "Json string $jobhead")
        val jobResponse = apiRequest { api.sendJobsForApproval(jobhead) }
        workflowJ2.postValue(jobResponse.workflowJob,job ,activity)

        val messages = activity.getResources().getString(R.string.please_wait)
        return withContext(Dispatchers.IO) {
            messages
        }
    }


   suspend fun saveMeasurementItems(userId: String, jobId: String, jimNo: String?, contractVoId: String?, mSures: ArrayList<JobItemMeasureDTOTemp>) {
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
       jdata.addProperty("ActId", mSures.get(0).actId )
       jdata.addProperty("ApprovalDate", mSures.get(0).approvalDate)
       jdata.addProperty("Cpa", mSures.get(0).cpa)
       jdata.addProperty("EndKm", mSures.get(0).endKm)
       jdata.addProperty("EstimateId", mSures.get(0).estimateId)
       jdata.addProperty("ItemMeasureId", mSures.get(0).itemMeasureId)
       jdata.addProperty("JimNo", mSures.get(0).jimNo)
       jdata.addProperty("PrjJobDto", mSures.get(0).job.toString())
//       jdata.add("PrjJobDto", array1 )
//       array1.add(jmadata)
       jdata.addProperty("JobDirectionId", mSures.get(0).jobDirectionId)
       jdata.addProperty("JobId", mSures.get(0).jobId)
       jdata.addProperty("PrjJobItemEstimateDto", mSures.get(0).jobItemEstimate.toString())
//       jdata.add("PrjJobItemEstimateDto", array2 )
//       array2.add(jmadata)
       jdata.addProperty("LineAmount", mSures.get(0).lineAmount)
       jdata.addProperty("LineRate", mSures.get(0).lineRate)
       jdata.addProperty("MeasureDate", mSures.get(0).measureDate)
       jdata.addProperty("MeasureGroupId", mSures.get(0).measureGroupId)
       jdata.add("PrjItemMeasurePhotoDtos", array3)
       array3.add(jmadata)
       jdata.addProperty("ProjectItemId", mSures.get(0).projectItemId)
       jdata.addProperty("ProjectVoId", mSures.get(0).projectVoId)
       jdata.addProperty("Qty", mSures.get(0).qty)
       jdata.addProperty("RecordSynchStateId", mSures.get(0).recordSynchStateId)
       jdata.addProperty("RecordVersion", mSures.get(0).recordVersion)
       jdata.addProperty("StartKm", mSures.get(0).startKm)
       jdata.addProperty("TrackRouteId", mSures.get(0).trackRouteId)



       //TODO(finish building the MeasureItems and Location)
//       measuredata.add("MeasurementItems", mSures)
       measuredata.addProperty("UserId", userId)


       Log.e("JsonObject", "Json string $measuredata")
       val measurementItemResponse =
            apiRequest { api.saveMeasurementItems(measuredata) }
        workflowJ.postValue(measurementItemResponse.workflowJob)
    }

    suspend fun imageUpload(filename: String, extension: String, photo: ByteArray) {
        val imagedata = JsonObject()
        imagedata.addProperty("Filename", filename)
        imagedata.addProperty("ImageByteArray", Base64.getEncoder().encodeToString(photo))
        imagedata.addProperty("ImageFileExtension", extension)

        val uploadImageResponse = apiRequest { api.uploadRrmImage(imagedata) }
        photoupload.postValue(uploadImageResponse.errorMessage)
    }

    private suspend fun fetchContracts(userId: String) {
        val lastSavedAt = prefs.getLastSavedAt()
        try {
            if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    lastSavedAt == null || isFetchNeeded(LocalDateTime.parse(lastSavedAt))
                } else {
                    true
                }
            ) {

                val activitySectionsResponse = apiRequest { api.activitySectionsRefresh(userId) }
                sectionItems.postValue(activitySectionsResponse.activitySections)

                val workFlowResponse = apiRequest { api.workflowsRefresh(userId) }
                workFlow.postValue(workFlowResponse.workFlows)

                val lookupResponse = apiRequest { api.lookupsRefresh(userId) }
                lookups.postValue(lookupResponse.mobileLookups)

                val toDoListGroupsResponse = apiRequest { api.getUserTaskList(userId) }
                toDoListGroups.postValue(toDoListGroupsResponse.toDoListGroups)

                val contractsResponse = apiRequest { api.refreshContractInfo(userId) }
                conTracts.postValue(contractsResponse.contracts)



            }
            ToastUtils().toastLong(activity, "You do not have an active data connection ")
        } catch (e: ApiException) {
            ToastUtils().toastLong(activity, e.message)
        } catch (e: NoInternetException) {
            ToastUtils().toastLong(activity, e.message)
            Log.e("Network-Connection", "No Internet Connection", e)
        }

    }

    private suspend fun fetchAllData(userId: String) {
        val lastSavedAt = prefs.getLastSavedAt()
        try {
            val activitySectionsResponse = apiRequest { api.activitySectionsRefresh(userId) }
            sectionItems.postValue(activitySectionsResponse.activitySections)

            val workFlowResponse = apiRequest { api.workflowsRefresh(userId) }
            workFlow.postValue(workFlowResponse.workFlows)

            val lookupResponse = apiRequest { api.lookupsRefresh(userId) }
            lookups.postValue(lookupResponse.mobileLookups)

            val toDoListGroupsResponse = apiRequest { api.getUserTaskList(userId) }
            toDoListGroups.postValue(toDoListGroupsResponse.toDoListGroups)

            val contractsResponse = apiRequest { api.refreshContractInfo(userId) }
            conTracts.postValue(contractsResponse.contracts)



        } catch (e: ApiException) {
            ToastUtils().toastLong(activity, e.message)
        } catch (e: NoInternetException) {
            ToastUtils().toastLong(activity, e.message)
            Log.e("Network-Connection", "No Internet Connection", e)
        }

    }


    private fun isFetchNeeded(savedAt: LocalDateTime): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ChronoUnit.DAYS.between(savedAt, LocalDateTime.now()) > MINIMUM_INTERVAL
        } else {
            true
        }
    }


    fun saveLookups(lookups: ArrayList<LookupDTO>?) {
        Coroutines.io {
            if (lookups != null) {
                for (lookup in lookups) {
                    if (!Db.getLookupDao().checkIfLookupExist(lookup.lookupName))
                        Db.getLookupDao().insertLookup(lookup)

                    if (lookup.lookupOptions != null) {
                        for (lookupOption in lookup.lookupOptions) {
                            if (!Db.getLookupOptionDao().checkLookupOptionExists(
                                    lookupOption.valueMember,
                                    lookup.lookupName
                                )
                            )
                                Db.getLookupOptionDao().insertLookupOption(
                                    lookupOption.valueMember, lookupOption.displayMember,
                                    lookupOption.contextMember, lookup.lookupName
                                )
                        }
                    }
                }
            }
        }
    }

    fun deleteAllData(vararg voids: Void?): Void? {
//        val databases =
//            File(appContext?.applicationInfo!!.dataDir + "myRRM_Database.db")
//        ToastUtils().toastLong(activity, databases.toString())

        if (Db != null) {
//            if (!Db.getJobDaoTemp().getAllJobsForAllProjects()){
                Db.getJobDao().deleteAll()
                Db.getJobSectionDao().deleteAll()
                Db.getJobItemEstimateDao().deleteAll()
                Db.getJobItemMeasureDao().deleteAll()
                Db.getJobItemEstimatePhotoDao().deleteAll()
                Db.getJobItemMeasurePhotoDao().deleteAll()
                Db.getContractDao().deleteAll()
                Db.getVoItemDao().deleteAll()
                Db.getProjectDao().deleteAll()
                Db.getPrimaryKeyValueDao().deleteAll()
                Db.getLookupOptionDao().deleteAll()
                Db.getLookupDao().deleteAll()
                Db.getEntitiesDao().deleteAll()
                Db.getProjectItemDao().deleteAll()
                Db.getItemSectionDao().deleteAll()
                Db.getProjectSectionDao().deleteAll()
                Db.getWorkFlowDao().deleteAll()
                Db.getWorkFlowRouteDao().deleteAll()
                Db.getWorkflowsDao().deleteAll()
                Db.getInfoClassDao().deleteAll()
                Db.getActivityDao().deleteAll()
                Db.getToDoGroupsDao().deleteAll()
                Db.getEstimateWorkDao().deleteAll()
                Db.getEstimateWorkPhotoDao().deleteAll()
                Db.getSectionItemDao().deleteAll()
                Db.getJobItemMeasureDao_Temp().deleteAll()
                Db.getJobItemMeasurePhotoDao_Temp().deleteAll()
                Db.getJobDaoTemp().deleteAll()
                Db.getItemDao_Temp().deleteAll()
                Db.getSectionPointDao().deleteAll()
                Db.getUserDao().deleteAll()
                Db.getUserRoleDao().deleteAll()
//            Db.getJobItemMeasurePhotoDao_Temp().deleteAll()

//            }else{

//            }





        }
        return null
    }

    suspend fun saveJobItemMeasureItems(jobItemMeasures: ArrayList<JobItemMeasureDTOTemp>) {
        Coroutines.io {
            for (jobItemMeasure in jobItemMeasures!!.iterator()){
                if (!Db.getJobItemMeasureDao_Temp().checkIfJobItemMeasureExists(jobItemMeasure.itemMeasureId!!)){
                    Db.getJobItemMeasureDao_Temp().insertJobItemMeasure(jobItemMeasure!!)
                }

            }

        }
    }
  
    suspend fun setJobItemMeasureImages(
        jobItemMeasurePhotoList: ArrayList<JobItemMeasurePhotoDTOTemp>,
        estimateId: String?
    ){
        Coroutines.io {
            for (jobItemMeasurePhoto in jobItemMeasurePhotoList!!.iterator()){
                Db.getJobItemMeasureDao_Temp().insertJobItemMeasure(jobItemMeasurePhoto.jobItemMeasureTemp)
                if (!Db.getJobItemMeasurePhotoDao_Temp().checkIfJobItemMeasurePhotoExists(jobItemMeasurePhoto.filename!!)){

                    Db.getJobItemMeasurePhotoDao_Temp().insertJobItemMeasurePhoto(jobItemMeasurePhoto!!)
                    jobItemMeasurePhoto.setEstimateId(estimateId)
                }

            }

        }
    }




    private fun insertOrUpdateWorkflowJobInSQLite(job: WorkflowJobDTO?) {
        job?.let {
            updateWorkflowJobValuesAndInsertWhenNeeded(it)
        }
    }

    private fun updateWorkflowJobValuesAndInsertWhenNeeded(job: WorkflowJobDTO) {
        Coroutines.io {
            Db?.getJobDaoTemp()!!.updateJob(job.jobId, job.actId, job.trackRouteId, job.jiNo)

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
            }

            if (job.workflowItemMeasures != null) {
                for (jobItemMeasure in job.workflowItemMeasures) {

                    Db?.getJobItemMeasureDao()!!.updateWorkflowJobItemMeasure(
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

    private fun JobDTO.setEstimateWorksActId(actId: Int) {
        this.WORKS_ACT_ID = actId
    }

    private fun JobDTO.setMeasureActId(actId: Int) {
        this.MEASURE_ACT_ID = actId
    }

    private fun JobDTO.setEstimateActId(actId: Int) {
        this.ESTIMATES_ACT_ID = actId
    }

    private fun SectionItemDTO.setDescription(activitySection: String) {
        this.description = activitySection
    }

    private fun SectionItemDTO.setItemCode(itemCode: String) {
        this.itemCode = itemCode
    }

    private fun SectionItemDTO.setSecctionItemId(sectionItemId: String) {
        this.sectionItemId = sectionItemId
    }

    private fun JobItemMeasureDTO.setJobNo(jiNo: String?) {
        this.jimNo = jiNo
    }

//private fun <T> LiveData<T>.postValue(photo: String?, fileName: String) {
//    saveEstimatePhoto(photo ,fileName)
//}

    private fun <T> MutableLiveData<T>.postValue(photo: String?, fileName: String) {
        saveEstimatePhoto(photo, fileName)
    }

    fun saveEstimatePhoto(estimatePhoto: String?, fileName: String) {
        Coroutines.io {
            if (estimatePhoto != null) {
                PhotoUtil.createPhotofolder(estimatePhoto, fileName)
//            PhotoUtil.createPhotofolder(fileName)
            }
        }

    }


    private fun <T> MutableLiveData<T>.postValue(workflowj : WorkflowJobDTO, job: JobDTOTemp,  activity: FragmentActivity) {
        if (workflowj != null) {
            val myJob  = setWorkflowJobBigEndianGuids(workflowj)
            insertOrUpdateWorkflowJobInSQLite(myJob)
            uploadImages(job, activity)
        }
    }

    suspend fun getUpdatedJob(jobId: String): JobDTOTemp {
        return withContext(Dispatchers.IO) {
            Db.getJobDaoTemp().getJobForJobId(jobId)
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
                jie.estimateId = DataConversion.toBigEndian(jie.estimateId)
                jie.trackRouteId = DataConversion.toBigEndian(jie.trackRouteId)
                //  Lets go through the WorkFlowEstimateWorks
                for (wfe in jie.workflowEstimateWorks) {
                    wfe.trackRouteId = DataConversion.toBigEndian(wfe.trackRouteId)
                    wfe.worksId = DataConversion.toBigEndian(wfe.worksId)
                    wfe.actId = wfe.actId
                    wfe.estimateId = DataConversion.toBigEndian(wfe.estimateId)
                    wfe.recordVersion = wfe.recordVersion
                    wfe.recordSynchStateId = wfe.recordSynchStateId
                }
            }
        }
        if (job.workflowItemMeasures != null) {
            for (jim in job.workflowItemMeasures) {
                jim.actId = jim.actId
                jim.itemMeasureId = DataConversion.toBigEndian(jim.itemMeasureId)
                jim.measureGroupId = DataConversion.toBigEndian(jim.measureGroupId)
                jim.trackRouteId = DataConversion.toBigEndian(jim.trackRouteId)
            }
        }
        if (job.workflowJobSections != null) {
            for (js in job.workflowJobSections) {
                js.jobSectionId = DataConversion.toBigEndian(js.jobSectionId)!!
                js.projectSectionId = DataConversion.toBigEndian(js.projectSectionId)
                js.jobId = DataConversion.toBigEndian(js.jobId)
            }
        }
        return job
    }


    private fun uploadImages(
        packagejob: JobDTOTemp,
        activity: FragmentActivity
    ) {
        var imageCounter = 1
        var totalImages = 0

        if (packagejob.JobItemEstimates != null) {
            for (jobItemEstimate in packagejob.JobItemEstimates!!) {
                if (jobItemEstimate.jobItemEstimatePhotos != null) {
                    val photos: Array<JobItemEstimatesPhotoDTO> = arrayOf<JobItemEstimatesPhotoDTO>(
                        jobItemEstimate.jobItemEstimatePhotos!!.get(0),
                        jobItemEstimate.jobItemEstimatePhotos!!.get(1)
                    )
                    for (jobItemEstimatePhoto in photos) {
                        if (PhotoUtil.photoExist(jobItemEstimatePhoto.filename)) {
                            totalImages++
                        }
                    }
                }
            }
        }

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
                        for (jobItemEstimatePhoto in jobItemEstimate.jobItemEstimatePhotos!!) {
                            if (PhotoUtil.photoExist(jobItemEstimatePhoto.filename)) {
                                Log.d("x-", "UploadRrImage $imageCounter")
                                uploadRrmImage(jobItemEstimatePhoto.filename, PhotoQuality.HIGH, imageCounter, totalImages, packagejob,activity)
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
        packagejob: JobDTOTemp,
        activity: FragmentActivity
    ) {

        val data: ByteArray = getData(filename, photoQuality, activity)
        processImageUpload(filename, activity.getString(R.string.jpg),data, totalImages, imageCounter,packagejob,activity)
    }



    private fun getData(filename: String, photoQuality: PhotoQuality, activity: FragmentActivity
    ): ByteArray {
        val uri = getPhotoPathFromExternalDirectory(activity.applicationContext, filename)
        val bitmap = PhotoUtil.getPhotoBitmapFromFile(activity.applicationContext, uri, PhotoQuality.HIGH)
        val photo = PhotoUtil.getCompressedPhotoWithExifInfo(activity.applicationContext, bitmap!!, filename)
        return photo
    }


    private fun processImageUpload(
        filename: String,
        extension: String,
        photo: ByteArray,
        totalImages: Int,
        imageCounter: Int,
        packagejob: JobDTOTemp,
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
                    val job  = getUpdatedJob(packagejob.JobId)
                    moveJobToNextWorkflow(job, activity)
                }
        }
    }

    private fun moveJobToNextWorkflow(
        job: JobDTOTemp,
        activity: FragmentActivity
    ) {

        if (job.TrackRouteId == null) {
           Looper.prepare() // to be able to make toast
            Toast.makeText(activity, "Error: trackRouteId is null", Toast.LENGTH_LONG).show()
        } else {
            job.setTrackRouteId(DataConversion.toLittleEndian(job.TrackRouteId))
            val direction: Int = WorkflowDirection.NEXT.getValue()
            val trackRouteId: String = job.TrackRouteId!!
            val description: String = activity.getResources().getString(R.string.submit_for_approval)

            Coroutines.io {
                val workflowMoveResponse = apiRequest { api.getWorkflowMove(job.UserId.toString(), trackRouteId, description, direction) }
                workflows.postValue(workflowMoveResponse.toDoListGroups)
            }

        }
    }


//private fun JobItemEstimatesPhotoDTO.setPhotoPath(photoPath: String) {
//    this.photoPath = photoPath
//}

//private fun SectionItemDTO.setSectionItemId(sectionItemId: String) {
//    var sectionItmId = SqlLitUtils.generateUuid()
//    sectionItmId = sectionItemId
//    this.sectionItemId = sectionItemId
//}
//
//private fun SectionItemDTO.setItemCode(itemCode: String) {
//    this.itemCode = itemCode
//}
//
//private fun SectionItemDTO.setActivitySection(activitySection: String) {
//    this.description = activitySection
//}


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


    suspend fun deleteItemfromList(itemId: String) {
        Coroutines.io {
            Db.getItemDao_Temp().deleteItemfromList(itemId)
        }
    }

}

private fun JobDTOTemp.setTrackRouteId(toLittleEndian: String?) {
    this.TrackRouteId =  toLittleEndian
}


private fun RrmJobRequest.setUserId(userId: Int) {
    this.userId = userId
}

private fun RrmJobRequest.setJob(job: JobDTOTemp) {
    this.job  = job
}


private fun JobItemMeasurePhotoDTOTemp.setEstimateId(estimateId: String?) {
    this.estimateId = estimateId
}

private fun SaveMeasurementResponse.getWorkflowJob(): WorkflowJobDTO {
    return workflowJob
}
private fun JobResponse.getWorkflowJob(): WorkflowJobDTO {
    return workflowJob
}
private fun JobItemMeasureDTO.setItemMeasurePhotoDTO(jobItemMeasurePhotoDTO: java.util.ArrayList<JobItemMeasurePhotoDTO>) {
    this.jobItemMeasurePhotos = jobItemMeasurePhotoDTO
}

private fun JobItemMeasureDTO.setJob(jobForItemEstimate: JobDTO) {
    this.job = jobForItemEstimate
}

private fun JobItemMeasureDTO.setJobItemEstimate(selectedJobItemEstimate: JobItemEstimateDTO) {
    this.jobItemEstimate = selectedJobItemEstimate
}

private fun JobItemMeasureDTO.setDesc(nothing: Nothing?) {
    this.entityDescription = nothing
}

private fun JobItemMeasureDTO.setApproveldate(nothing: Nothing?) {
    this.approvalDate = nothing
}

private fun JobItemMeasureDTO.setActId(actId: Int) {
this.actId = actId
}



