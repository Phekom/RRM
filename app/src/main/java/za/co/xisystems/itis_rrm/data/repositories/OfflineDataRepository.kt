package za.co.xisystems.itis_rrm.data.repositories

import android.content.Context
import android.os.Build
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.Navigation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import za.co.xisystems.itis_rrm.MainActivity
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.data._commons.views.ToastUtils
import za.co.xisystems.itis_rrm.data.localDB.AppDatabase
import za.co.xisystems.itis_rrm.data.localDB.JobDataController
import za.co.xisystems.itis_rrm.data.localDB.entities.*
import za.co.xisystems.itis_rrm.data.localDB.models.WorkflowJob
import za.co.xisystems.itis_rrm.data.network.BaseConnectionApi
import za.co.xisystems.itis_rrm.data.network.OfflineListener
import za.co.xisystems.itis_rrm.data.network.SafeApiRequest
import za.co.xisystems.itis_rrm.data.network.responses.WorkflowMoveResponse
import za.co.xisystems.itis_rrm.data.preferences.PreferenceProvider
import za.co.xisystems.itis_rrm.utils.*
import za.co.xisystems.itis_rrm.utils.enums.WorkflowDirection
import java.io.File
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.regex.Pattern

/**
 * Created by Francis Mahlava on 2019/11/28.
 */

const val MINIMUM_INTERVAL = 3
private val jobDataController : JobDataController? = null
private val activity: FragmentActivity? = null
private val Db: AppDatabase? = null
class OfflineDataRepository(
    private val api: BaseConnectionApi,
    private val Db: AppDatabase,
    private val prefs: PreferenceProvider
) : SafeApiRequest() {
    companion object {
        val TAG: String = OfflineDataRepository::class.java.simpleName
    }



    private val appContext: Context? = null
    private var rListener: OfflineListener? = null
    private val conTracts = MutableLiveData<List<ContractDTO>>()
    //    private val sectionItems = MutableLiveData<ArrayList<SectionItemDTO>>()
    private val sectionItems = MutableLiveData<ArrayList<String>>()
    private val projects = MutableLiveData<ArrayList<ProjectDTO>>()
    private val projectItems = MutableLiveData<ArrayList<ItemDTO>>()
    private val voItems = MutableLiveData<ArrayList<VoItemDTO>>()
    private val projectSections = MutableLiveData<ArrayList<ProjectSectionDTO>>()
    private val job = MutableLiveData<JobDTO>()
    private val estimatePhoto = MutableLiveData<String>()
            private val measurePhoto = MutableLiveData<String>()
    private val workFlow = MutableLiveData<WorkFlowsDTO>()
    private val lookups = MutableLiveData<ArrayList<LookupDTO>>()
    private val toDoListGroups = MutableLiveData<ArrayList<ToDoGroupsDTO>>()
    private val workflows = MutableLiveData<ArrayList<ToDoGroupsDTO>>()



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

//
//        sectionItems.observeForever {
//            //            saveSectionItems(it)
//            saveSectionsItems(it)//            saveProjectsItems(it)
//        }
    }



    suspend fun getContracts(): LiveData<List<ContractDTO>> {
        return withContext(Dispatchers.IO) {
            //            val userId = Db?.getUserDao()!!.getuserID()
//            fetchContracts(userId)
            Db?.getContractDao()!!.getAllContracts()
        }
    }

    suspend fun getContractProjects(contractId: String): LiveData<List<ProjectDTO>> {
        return withContext(Dispatchers.IO) {
            Db?.getProjectDao()!!.getAllProjectsByContract(contractId)
        }
    }

    suspend fun getProjects(): LiveData<List<ProjectDTO>> {
        return withContext(Dispatchers.IO) {
            Db?.getProjectDao()!!.getAllProjects()
        }
    }

    suspend fun getProjectItems(): LiveData<List<ItemDTO>> {
        return withContext(Dispatchers.IO) {
            Db?.getItemDao()!!.getAllItemsForAllProjects()
        }
    }

    suspend fun getItemForItemCode(sectionItemId: String): LiveData<List<ItemDTO>> {
        return withContext(Dispatchers.IO) {
            //            val projectId = DataConversion.toLittleEndian( Db?.getProjectDao()!!.getProjectId())
            Db?.getItemDao()!!.getItemForItemCode(sectionItemId)
        }
    }

    suspend fun getAllItemsForProjectId(projectId: String): LiveData<List<ItemDTO>> {
        return withContext(Dispatchers.IO) {
            //            val projectId = DataConversion.toLittleEndian( Db?.getProjectDao()!!.getProjectId())
            Db?.getItemDao()!!.getAllItemsForProjectId(projectId)
        }
    }

    suspend fun getAllItemsForSectionItem(
        sectionItemId: String,
        projectId: String
    ): LiveData<List<ItemDTO>> {
        return withContext(Dispatchers.IO) {
            //            val projectId = DataConversion.toLittleEndian( Db?.getProjectDao()!!.getProjectId())
            Db?.getItemDao()!!.getAllItemsForSectionItem(sectionItemId, projectId)
        }
    }


    suspend fun getWorkFlows(): LiveData<List<WorkFlowDTO>> {
        return withContext(Dispatchers.IO) {
            val userId = Db?.getUserDao()!!.getuserID()
            fetchAllData(userId)
            Db?.getWorkFlowDao()!!.getWorkflows()
        }
    }


    suspend fun getSectionItems(): LiveData<SectionItemDTO> {
        return withContext(Dispatchers.IO) {
            val userId = Db?.getUserDao()!!.getuserID()
            fetchContracts(userId)
            Db?.getSectionItemDao()!!.getSectionItems()
        }
    }

    suspend fun getAllSectionItem(): LiveData<List<SectionItemDTO>> {
        return withContext(Dispatchers.IO) {
            Db?.getSectionItemDao()!!.getAllSectionItems()
        }
    }

    suspend fun getUser(): LiveData<UserDTO> {
        return withContext(Dispatchers.IO) {
            Db?.getUserDao()!!.getuser()
        }
    }

    suspend fun getJobsForActivityId(activityId: Int): LiveData<List<JobDTO>> {
        return withContext(Dispatchers.IO) {
            Db?.getJobDao()!!.getJobsForActivityId(activityId)
        }
    }

    suspend fun getJobsForActivityIds1(activityId1: Int, activityId2: Int): LiveData<List<JobDTO>> {
        return withContext(Dispatchers.IO) {
            Db?.getJobDao()!!.getJobsForActivityIds1(activityId1, activityId2)
        }
    }
    suspend fun getJobsForActivityIds(activityId1: Int): LiveData<List<JobItemEstimateDTO>> {
        return withContext(Dispatchers.IO) {
            Db?.getJobItemEstimateDao()!!.getJobsForActivityId(activityId1)
        }
    }



    suspend fun getJobMeasureForActivityId(activityId: Int): LiveData<List<JobItemEstimateDTO>> {
        return withContext(Dispatchers.IO) {
            Db?.getJobItemEstimateDao()!!.getJobsForActivityId(activityId!!)
        }
    }

    suspend fun getJobApproveMeasureForActivityId(activityId: Int): LiveData<List<JobItemMeasureDTO>> {
        return withContext(Dispatchers.IO) {
            Db?.getJobItemMeasureDao()!!.getJobApproveMeasureForActivityId(activityId)
        }
    }

    suspend fun getJobEstimationItemsPhoto(estimateId: String): LiveData<List<JobItemEstimatesPhotoDTO>> {
        return withContext(Dispatchers.IO) {
//           val filename =  Db?.getJobItemEstimatePhotoDao()!!.getJobEstimationItemsPhotoFilename(estimateId)
//            getPhotoForJobItemEstimate(filename)
            Db?.getJobItemEstimatePhotoDao()!!.getJobEstimationItemsPhoto(estimateId)
        }
    }
    suspend fun getJobEstimationItemsPhotoStartPath(estimateId: String): String {
        return withContext(Dispatchers.IO) {
            Db?.getJobItemEstimatePhotoDao()!!.getJobEstimationItemsPhotoStartPath(estimateId)
        }
    }

    suspend fun getJobEstimationItemsPhotoEndPath(estimateId: String): String {
        return withContext(Dispatchers.IO) {
            Db?.getJobItemEstimatePhotoDao()!!.getJobEstimationItemsPhotoEndPath(estimateId)
        }
    }

    suspend fun getJobMeasureItemsPhotoPath(itemMeasureId: String): String {
        return withContext(Dispatchers.IO) {
            Db?.getJobItemMeasurePhotoDao()!!.getJobMeasureItemsPhotoPath(itemMeasureId)
        }
    }

    suspend fun getJobEstimationItemsForJobId(jobID: String?): LiveData<List<JobItemEstimateDTO>> {
        return withContext(Dispatchers.IO) {
            Db?.getJobItemEstimateDao()!!.getJobEstimationItemsForJobId(jobID!!)
        }
    }

    suspend fun getJobMeasureItemsForJobId(jobID: String?, actId: Int): LiveData<List<JobItemMeasureDTO>> {
        return withContext(Dispatchers.IO) {
            Db?.getJobItemMeasureDao()!!.getJobMeasureItemsForJobId(jobID!!, actId)
        }
    }

    suspend fun getJobEstiItemForEstimateId(estimateId: String?): LiveData<List<JobEstimateWorksDTO>> {
        return withContext(Dispatchers.IO) {
            Db?.getEstimateWorkDao()!!.getJobMeasureItemsForJobId(estimateId)
        }
    }

//    suspend fun getJobItemMeasureForJobId(jobID: String?): LiveData<List<JobItemMeasureDTO>> {
//        return withContext(Dispatchers.IO) {
//            Db?.getJobItemMeasureDao()!!.getJobItemMeasureForJobId(jobID!!)
//        }
//    }

    suspend fun getSingleJobFromJobId(jobId: String?): LiveData<JobDTO> {
        return withContext(Dispatchers.IO) {
            Db?.getJobDao()!!.getJobFromJobId(jobId!!)
        }
    }

    suspend fun jobExists(jobId: String?): Boolean {
        return withContext(Dispatchers.IO) {
            Db?.getJobDao()!!.checkIfJobExist(jobId!!)
        }
    }

    suspend fun getProjectSectionIdForJobId(jobId: String?): String {
        return withContext(Dispatchers.IO) {
            Db?.getJobSectionDao()!!.getProjectSectionId(jobId!!)
        }
    }

    suspend fun getProjectDescription(projectId: String): String {
        return withContext(Dispatchers.IO) {
            Db?.getProjectDao()!!.getProjectDescription(projectId)
        }
    }

    suspend fun getProjectItemDescription(projectItemId: String): String {
        return withContext(Dispatchers.IO) {
            Db?.getItemDao()!!.getProjectItemDescription(projectItemId)
        }
    }

    suspend fun getItemDescription(jobId: String): String {
        return withContext(Dispatchers.IO) {
            Db?.getJobDao()!!.getItemDescription(jobId)
        }
    }

    suspend fun getItemJobNo(jobId: String): String {
        return withContext(Dispatchers.IO) {
            Db?.getJobDao()!!.getItemJobNo(jobId)
        }
    }

    suspend fun getItemStartKm(jobId: String): Double {
        return withContext(Dispatchers.IO) {
            Db?.getJobDao()!!.getItemStartKm(jobId)
        }
    }
    suspend fun getItemEndKm(jobId: String): Double {
        return withContext(Dispatchers.IO) {
            Db?.getJobDao()!!.getItemEndKm(jobId)
        }
    }

    suspend fun getItemTrackRouteId(jobId: String): String {
        return withContext(Dispatchers.IO) {
            Db?.getJobDao()!!.getItemTrackRouteId(jobId)
        }
    }









    suspend fun getUOMForProjectItemId(projectItemId: String): String {
        return withContext(Dispatchers.IO) {
            Db?.getItemDao()!!.getUOMForProjectItemId(projectItemId)
        }
    }

    suspend fun getRouteForProjectSectionId(sectionId: String?): String {
        return withContext(Dispatchers.IO) {
            Db?.getProjectSectionDao()!!.getRouteForProjectSectionId(sectionId!!)
        }
    }

    suspend fun getSectionForProjectSectionId(sectionId: String?): String {
        return withContext(Dispatchers.IO) {
            Db?.getProjectSectionDao()!!.getSectionForProjectSectionId(sectionId!!)
        }
    }

    suspend fun getJobs(): LiveData<List<JobDTO>> {
        return withContext(Dispatchers.IO) {
            Db?.getJobDao()!!.getAllJobsForAllProjects()
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
                        if (!Db?.getSectionItemDao()!!.checkIfSectionitemsExist(itemCode))
                            Db?.getSectionItemDao()!!.insertSectionitem(activitySection!!, itemCode!!, sectionItemId!!)
//                            section.setSecctionItemId(sectionItemId)
//                            section.setItemCode(itemCode)
//                            section.setDescription(activitySection)
//                             Db?.getSectionItemDao()!!.insertSectionitem(section)



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
//            Db?.getContractDao()!!.saveAllContracts(contracts)
            if (contracts != null) {
                for (contract in contracts) {
                    if (!Db?.getContractDao()!!.checkIfContractExists(contract.contractId))
                        Db?.getContractDao()!!.insertContract(contract)
                    if (contract.projects != null) {
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                            prefs.savelastSavedAt(LocalDateTime.now().toString())
//                        }
                        for (project in contract.projects) {
                            if (!Db?.getProjectDao()!!.checkProjectExists(project.projectId)) {
                                Db?.getProjectDao()!!.insertProject(
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
                                    if (!Db?.getItemDao()!!.checkItemExistsItemId(item.itemId)) {
//                                        Db?.getItemDao()!!.insertItem(item)
                                        //  Lets get the ID from Sections Items
                                        val pattern = Pattern.compile("(.*?)\\.")
                                        val matcher = pattern.matcher(item.itemCode)
                                        if (matcher.find()) {
                                            val itemCode = matcher.group(1) + "0"
                                            //  Lets Get the ID Back on Match
                                            val sectionItemId =
                                                Db?.getSectionItemDao()!!.getSectionItemId(
                                                    itemCode.replace(
                                                        "\\s+".toRegex(), ""
                                                    )
                                                )
                                            Db?.getItemDao()!!.insertItem(
                                                item.itemId,
                                                item.itemCode,
                                                item.descr,
                                                item.itemSections,
                                                item.tenderRate,
                                                item.uom,
                                                item.workflowId,
                                                sectionItemId,
                                                item.quantity,
                                                item.estimateId,
                                                project.projectId
                                            )
                                        }
                                    }

                                }
                            }

                            if (project.projectSections != null) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    prefs.savelastSavedAt(LocalDateTime.now().toString())
                                }
                                for (section in project.projectSections) { //project.projectSections
                                    if (!Db?.getProjectSectionDao()!!.checkSectionExists(section.sectionId))
//                                        Db?.getProjectSectionDao()!!.insertSections(section)
                                        Db?.getProjectSectionDao()!!.insertSection(
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
                                    if (!Db?.getVoItemDao()!!.checkIfVoItemExist(voItem.projectVoId))
//                                        Db?.getVoItemDao()!!.insertVoItem(voItem)
                                        Db?.getVoItemDao()!!.insertVoItem(
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

    private fun saveWorkFlowsInfo(workFlows: WorkFlowsDTO) {
        Coroutines.io {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                prefs.savelastSavedAt(LocalDateTime.now().toString())
            }
            if (workFlows != null)
                Db?.getWorkflowsDao()!!.insertWorkFlows(workFlows)
            if (workFlows.workflows != null) {
                for (workFlow in workFlows.workflows) {
                    if (!Db?.getWorkFlowDao()!!.checkWorkFlowExistsWorkflowID(workFlow.workflowId))
                        Db?.getWorkFlowDao()!!.insertWorkFlow(workFlow)
//                    Db?.getWorkFlowDao()!!.insertWorkFlow(workFlow.dateCreated,workFlow.errorRouteId, workFlow.revNo, workFlow.startRouteId, workFlow.userId,
//                        workFlow.wfHeaderId, workFlow.workFlowRoute, workFlow.workflowId)

                    if (workFlow.workFlowRoute != null) {
                        for (workFlowRoute in workFlow.workFlowRoute!!) {  //ArrayList<WorkFlowRouteDTO>()
                            if (!Db?.getWorkFlowRouteDao()!!.checkWorkFlowRouteExists(workFlowRoute.routeId))
//                                Db?.getWorkFlowRouteDao()!!.insertWorkFlowRoutes(
//                                    workFlowRoute )
                                Db?.getWorkFlowRouteDao()!!.insertWorkFlowRoute(
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
                    Db?.getActivityDao()!!.insertActivitys(activity)
//                    Db?.getActivityDao()!!.insertActivity( activity.actId,  activity.actTypeId, activity.approvalId, activity.sContentId,  activity.actName, activity.descr )
                }
            }

            if (workFlows.infoClasses != null) {
                for (infoClass in workFlows.infoClasses) {
                    Db?.getInfoClassDao()!!.insertInfoClasses(infoClass)
//                    Db?.getInfoClassDao()!!.insertInfoClass(infoClass.sLinkId, infoClass.sInfoClassId,  infoClass.wfId)
                }
            }
        }
    }

    private fun saveJobs(job: JobDTO?) {
        Coroutines.io {
            if (job != null) {

                if (!Db?.getJobDao()!!.checkIfJobExist(job.JobId)) {
                    job.run {
                        setJobId(DataConversion.toBigEndian(JobId))
                        setProjectId(DataConversion.toBigEndian(ProjectId))
                        setContractVoId(DataConversion.toBigEndian(ContractVoId))
                        setTrackRouteId(DataConversion.toBigEndian(TrackRouteId))
//                        setRoute(route)
                    }
                    DataConversion.toBigEndian(job.PerfitemGroupId)
                    DataConversion.toBigEndian(job.ProjectVoId)
                    Db?.getJobDao()!!.insertOrUpdateJobs(job)
                }

                if (job.JobSections != null && job.JobSections.size != 0) {
                    for (jobSection in job.JobSections) {
                        if (!Db?.getJobSectionDao()!!.checkIfJobSectionExist(jobSection.jobSectionId))
                            jobSection.setJobSectionId(DataConversion.toBigEndian(jobSection.jobSectionId))
                        jobSection.setProjectSectionId(DataConversion.toBigEndian(jobSection.projectSectionId))
                        jobSection.setJobId(DataConversion.toBigEndian(jobSection.jobId))
                        Db?.getJobSectionDao()!!.insertJobSection(
                            jobSection
                        )

                    }

                }

                if (job.JobItemEstimates != null && job.JobItemEstimates!!.size != 0) {
                    for (jobItemEstimate in job.JobItemEstimates!!) {
                        if (!Db?.getJobItemEstimateDao()!!.checkIfJobItemEstimateExist(jobItemEstimate.estimateId)
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
                            Db?.getJobItemEstimateDao()!!.insertJobItemEstimate(jobItemEstimate)
                            Db?.getJobDao()!!.setEstimateActId(jobItemEstimate.actId, job.JobId)
//                            job.setEstimateActId(jobItemEstimate.actId)
                            if (jobItemEstimate.jobItemEstimatePhotos != null) {
                                for (jobItemEstimatePhoto in jobItemEstimate.jobItemEstimatePhotos) {
                                    if (!Db?.getJobItemEstimatePhotoDao()!!.checkIfJobItemEstimatePhotoExistsByPhotoId(
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
                                    Db?.getJobItemEstimatePhotoDao()!!.insertJobItemEstimatePhoto(
                                        jobItemEstimatePhoto
                                    )
                                    if (!PhotoUtil.photoExist(jobItemEstimatePhoto.filename)) {
                                        getPhotoForJobItemEstimate(jobItemEstimatePhoto.filename)
                                    }
                                }
                            }
                            if (jobItemEstimate.jobEstimateWorks != null) {
                                for (jobEstimateWorks in jobItemEstimate.jobEstimateWorks) {
                                    if (!Db?.getEstimateWorkDao()!!.checkIfJobEstimateWorksExist(
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
                                    Db?.getEstimateWorkDao()!!.insertJobEstimateWorks(
                                        jobEstimateWorks
                                    )
                                    Db?.getJobDao()!!.setEstimateWorksActId(jobEstimateWorks.actId, job.JobId)
//                                    job.setEstimateWorksActId(jobEstimateWorks.actId)
                                    if (jobEstimateWorks.jobEstimateWorksPhotos != null) {
                                        for (estimateWorksPhoto in jobEstimateWorks.jobEstimateWorksPhotos) {
                                            if (!Db?.getEstimateWorkPhotoDao()!!.checkIfEstimateWorksPhotoExist(
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
                                            Db?.getEstimateWorkPhotoDao()!!.insertEstimateWorksPhoto(
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
                        if (!Db?.getJobItemMeasureDao()!!.checkIfJobItemMeasureExists(jobItemMeasure.itemMeasureId!!)) {
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
                            Db?.getJobItemMeasureDao()!!.insertJobItemMeasure(jobItemMeasure)
                            Db?.getJobDao()!!.setMeasureActId(jobItemMeasure.actId, job.JobId)
//                            job.setMeasureActId(jobItemMeasure.actId)
                            if (jobItemMeasure.jobItemMeasurePhotos != null) {
                                for (jobItemMeasurePhoto in jobItemMeasure.jobItemMeasurePhotos) {
                                    if (!Db?.getJobItemMeasurePhotoDao()!!.checkIfJobItemMeasurePhotoExists(
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
                                    Db?.getJobItemMeasurePhotoDao()!!.insertJobItemMeasurePhoto(
                                        jobItemMeasurePhoto
                                    )
                                    if (!PhotoUtil.photoExist(jobItemMeasurePhoto.filename))
                                        getPhotoForJobItemMeasure( jobItemMeasurePhoto.filename)
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

    private suspend fun getPhotoForJobItemMeasure(filename: String) {
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

    private fun saveTaskList(toDoList: ArrayList<ToDoGroupsDTO>?) {
        val response : WorkflowMoveResponse? = null
        if (toDoList != null) {
            val job: WorkflowJob? =
                jobDataController?.setWorkflowJobBigEndianGuids(response!!.getWorkflowJob()!!)
            insertOrUpdateWorkflowJobInSQLite(job)
            saveUserTaskList(toDoList)

        }
    }



    private fun saveUserTaskList(toDoListGroups: ArrayList<ToDoGroupsDTO>?) {
        Coroutines.io {
            if (toDoListGroups != null) {
                for (toDoListGroup in toDoListGroups) {
                    if (!Db?.getToDoGroupsDao()!!.checkIfGroupCollectionExist(toDoListGroup.groupId)) {
                        Db?.getToDoGroupsDao()!!.insertToDoGroups(toDoListGroup)
//                   TODO(this is done with the line above)
//                        Db?.getToDoGroupsDao()!!.insertToDoGroups(
//                            toDoListGroup.getGroupId(), toDoListGroup.getGroupDescription(),
//                            toDoListGroup.getGroupName(), toDoListGroup.getSortOrder()
//                        )
                    }

                    val entitiesArrayList = toDoListGroup.toDoListEntities

                    for (toDoListEntity in entitiesArrayList) {
                        val jobId = getJobIdFromPrimaryKeyValues(toDoListEntity.primaryKeyValues)
                        insertEntity(toDoListEntity, jobId!!)
                        for (subEntity in toDoListEntity.entities) {
                            insertEntity(subEntity, jobId)
                            val job_Id = DataConversion.toLittleEndian(jobId!!)
                            fetchJobList(job_Id!!)
                        }

                    }

                }

            }
        }
    }

    private fun insertEntity(entity: ToDoListEntityDTO, jobId: String) {
        Coroutines.io {
            if (!Db?.getEntitiesDao()!!.checkIfEntitiesExist(DataConversion.bigEndianToString(entity.trackRouteId!!))) {
                Db?.getEntitiesDao()!!.insertEntitie(
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
                    Db?.getPrimaryKeyValueDao()!!.insertPrimaryKeyValue(
                        primaryKeyValue.primary_key,
                        DataConversion.bigEndianToString(primaryKeyValue.value!!),
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
                return DataConversion.bigEndianToString(primaryKeyValue.value!!)
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

    suspend fun processWorkflowMove(userId: String, trackRounteId: String, description: String?, direction: Int ) {
        val workflowMoveResponse = apiRequest { api.getWorkflowMove(userId ,trackRounteId, description, direction) }
        workflows.postValue(workflowMoveResponse.toDoListGroups)
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

                val contractsResponse = apiRequest { api.refreshContractInfo(userId) }
                conTracts.postValue(contractsResponse.contracts)

                val toDoListGroupsResponse = apiRequest { api.getUserTaskList(userId) }
                toDoListGroups.postValue(toDoListGroupsResponse.toDoListGroups)

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

                val contractsResponse = apiRequest { api.refreshContractInfo(userId) }
                conTracts.postValue(contractsResponse.contracts)

                val toDoListGroupsResponse = apiRequest { api.getUserTaskList(userId) }
                toDoListGroups.postValue(toDoListGroupsResponse.toDoListGroups)

        } catch (e: ApiException) {
            ToastUtils().toastLong(activity, e.message)
        } catch (e: NoInternetException) {
            ToastUtils().toastLong(activity, e.message)
            Log.e("Network-Connection", "No Internet Connection", e)
        }

    }


    private fun isFetchNeeded(savedAt: LocalDateTime): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ChronoUnit.HOURS.between(savedAt, LocalDateTime.now()) > MINIMUM_INTERVAL
        } else {
            true
        }
    }


    fun saveLookups(lookups: ArrayList<LookupDTO>?) {
        Coroutines.io {
            if (lookups != null) {
                for (lookup in lookups) {
                    if (!Db?.getLookupDao()!!.checkIfLookupExist(lookup.lookupName))
                        Db?.getLookupDao()!!.insertLookup(lookup)

                    if (lookup.lookupOptions != null) {
                        for (lookupOption in lookup.lookupOptions) {
                            if (!Db?.getLookupOptionDao()!!.checkLookupOptionExists(
                                    lookupOption.valueMember,
                                    lookup.lookupName
                                )
                            )
                                Db?.getLookupOptionDao()!!.insertLookupOption(
                                    lookupOption.valueMember, lookupOption.displayMember,
                                    lookupOption.contextMember, lookup.lookupName
                                )
                        }
                    }
                }
            }
        }
    }



}

private fun insertOrUpdateWorkflowJobInSQLite(job: WorkflowJob?) {
    job?.let {
        updateWorkflowJobValuesAndInsertWhenNeeded(it)
    }
}

private fun updateWorkflowJobValuesAndInsertWhenNeeded(job: WorkflowJob) {
    Coroutines.io {
        Db?.getJobDao()!!.updateJob(job.jobId, job.actId, job.trackRouteId, job.jiNo)

        if (job.workflowItemEstimates != null && job.workflowItemEstimates.size !== 0) {
            for (jobItemEstimate in job.workflowItemEstimates) {
                Db?.getJobItemEstimateDao()!!.updateExistingJobItemEstimateWorkflow(
                    jobItemEstimate.trackRouteId,
                    jobItemEstimate.actId,
                    jobItemEstimate.estimateId
                )

                if (jobItemEstimate.workflowEstimateWorks != null) {
                    for (jobEstimateWorks in jobItemEstimate.workflowEstimateWorks) {
                        if (!Db?.getEstimateWorkDao()!!.checkIfJobEstimateWorksExist(
                                jobEstimateWorks.worksId
                            )
                        )
                            Db?.getEstimateWorkDao()!!.insertJobEstimateWorks(
                                jobEstimateWorks as JobEstimateWorksDTO
                            ) else Db?.getEstimateWorkDao()!!.updateJobEstimateWorksWorkflow(
                            jobEstimateWorks.worksId, jobEstimateWorks.estimateId, jobEstimateWorks.recordVersion, jobEstimateWorks.recordSynchStateId,
                            jobEstimateWorks.actId, jobEstimateWorks.trackRouteId
                        )
                    }
                }
            }
        }

        if (job.workflowItemMeasures != null) {
            for (jobItemMeasure in job.workflowItemMeasures) {

                Db?.getJobItemMeasureDao()!!.updateWorkflowJobItemMeasure(jobItemMeasure.itemMeasureId,
                    jobItemMeasure.trackRouteId, jobItemMeasure.actId, jobItemMeasure.measureGroupId)
            }
        }

        //  Place the Job Section, UPDATE OR CREATE
        if (job.workflowJobSections != null && job.workflowJobSections.size !== 0) {
            for (jobSection in job.workflowJobSections) {
                if (!Db?.getJobSectionDao()!!.checkIfJobSectionExist(jobSection.jobSectionId))
                    Db?.getJobSectionDao()!!.insertJobSection(
                        jobSection as JobSectionDTO
                    ) else
                    Db?.getJobSectionDao()!!.updateExistingJobSectionWorkflow(jobSection.jobSectionId,
                        jobSection.projectSectionId, jobSection.jobId, jobSection.startKm,jobSection.endKm,
                        jobSection.recordVersion,jobSection.recordSynchStateId )
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
    saveEstimatePhoto(photo ,fileName)
}

fun saveEstimatePhoto(estimatePhoto: String?, fileName: String) {
    Coroutines.io {
        if (estimatePhoto != null) {
                PhotoUtil.createPhotofolder(estimatePhoto, fileName)
//            PhotoUtil.createPhotofolder(fileName)
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


