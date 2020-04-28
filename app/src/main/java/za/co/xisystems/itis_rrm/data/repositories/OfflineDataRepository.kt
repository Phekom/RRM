package za.co.xisystems.itis_rrm.data.repositories

//import sun.security.krb5.Confounder.bytes

// import android.app.Activity
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.data.localDB.AppDatabase
import za.co.xisystems.itis_rrm.data.localDB.JobDataController
import za.co.xisystems.itis_rrm.data.localDB.entities.*
import za.co.xisystems.itis_rrm.data.network.BaseConnectionApi
import za.co.xisystems.itis_rrm.data.network.SafeApiRequest
import za.co.xisystems.itis_rrm.data.network.responses.JobResponse
import za.co.xisystems.itis_rrm.data.network.responses.SaveMeasurementResponse
import za.co.xisystems.itis_rrm.data.network.responses.UploadImageResponse
import za.co.xisystems.itis_rrm.data.preferences.PreferenceProvider
import za.co.xisystems.itis_rrm.utils.Coroutines
import za.co.xisystems.itis_rrm.utils.DataConversion
import za.co.xisystems.itis_rrm.utils.PhotoUtil
import za.co.xisystems.itis_rrm.utils.PhotoUtil.getPhotoPathFromExternalDirectory
import za.co.xisystems.itis_rrm.utils.SqlLitUtils
import za.co.xisystems.itis_rrm.utils.enums.PhotoQuality
import za.co.xisystems.itis_rrm.utils.enums.WorkflowDirection
import java.io.File
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.regex.Pattern


/**
 * Created by Francis Mahlava on 2019/11/28.
 */

const val MINIMUM_INTERVAL = 3
private val jobDataController: JobDataController? = null

class OfflineDataRepository(
    private val api: BaseConnectionApi,
    private val Db: AppDatabase,
    private val prefs: PreferenceProvider
) : SafeApiRequest() {


    private val conTracts = MutableLiveData<List<ContractDTO>>()
    private val sectionItems = MutableLiveData<ArrayList<String>>()
    private val projects = MutableLiveData<ArrayList<ProjectDTO>>()
    private val projectItems = MutableLiveData<ArrayList<ProjectItemDTO>>()
    private val voItems = MutableLiveData<ArrayList<VoItemDTO>>()
    private val projectSections = MutableLiveData<ArrayList<ProjectSectionDTO>>()
    private val job = MutableLiveData<JobDTO>()
    private val newJob = MutableLiveData<JobDTOTemp>()
    private val estimatePhoto = MutableLiveData<String>()
    private val measurePhoto = MutableLiveData<String>()
    private val workFlow = MutableLiveData<WorkFlowsDTO>()
    private val lookups = MutableLiveData<ArrayList<LookupDTO>>()
    private val toDoListGroups = MutableLiveData<ArrayList<ToDoGroupsDTO>>()
    private val workflows = MutableLiveData<ArrayList<ToDoGroupsDTO>>()
    private val workflowJ = MutableLiveData<WorkflowJobDTO>()
    private val workflowJ2 = MutableLiveData<WorkflowJobDTO>()
    private val photoupload = MutableLiveData<String>()
    private val works = MutableLiveData<String>()
    private val routeSectionPoint = MutableLiveData<String>()

    init {
        conTracts.observeForever {
            Coroutines.io {
                saveContracts(it)
            }
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

        toDoListGroups.observeForever {
            saveUserTaskList(it)
        }

        workflows.observeForever {
            saveTaskList(it)
        }

        job.observeForever {
            saveJobs(it)

        }


        workflowJ.observeForever {
            saveWorkflowJob(it)
        }
        workflowJ2.observeForever {
            saveWorkflowJob2(it)
        }

        photoupload.observeForever {
            sendMSg(it)
        }


    }

    suspend fun getContracts(): LiveData<List<ContractDTO>> {
        return withContext(Dispatchers.IO) {
            Db.getContractDao().getAllContracts()
        }
    }

    suspend fun getRoles(): LiveData<List<UserRoleDTO>> {
        return withContext(Dispatchers.IO) {
            Db.getUserRoleDao().getRoles()
        }
    }

    suspend fun getWorkFlows(): LiveData<List<WorkFlowDTO>> {

        return withContext(Dispatchers.IO) {
            val userId = Db.getUserDao().getUserID()
            fetchAllData(userId)
            Db.getWorkFlowDao().getWorkflows()
        }
    }

    suspend fun getSectionItems(): LiveData<List<SectionItemDTO>> {
        return withContext(Dispatchers.IO) {
            val userId = Db.getUserDao().getUserID()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                fetchContracts(userId)
            }
            Db.getSectionItemDao().getSectionItems()
        }
    }

    suspend fun getUser(): LiveData<UserDTO> {
        return withContext(Dispatchers.IO) {
            Db.getUserDao().getUser()
        }
    }

    suspend fun getJobsForActivityId(activityId: Int): LiveData<List<JobDTO>> {
        return withContext(Dispatchers.IO) {
            Db.getJobDao().getJobsForActivityId(activityId)
        }
    }

    suspend fun getJobsForActId(activityId: Int): LiveData<List<JobDTO>> {
        return withContext(Dispatchers.IO) {
            Db.getJobDao().getJobsForActivityId(activityId)
        }
    }

    suspend fun getJobsForActivityIds1(activityId1: Int, activityId2: Int): LiveData<List<JobDTO>> {
        return withContext(Dispatchers.IO) {
            Db.getJobDao().getJobsForActivityIds1(activityId1, activityId2)
        }
    }

    suspend fun getJobMeasureForActivityId(
        activityId: Int,
        activityId2: Int
    ): LiveData<List<JobItemEstimateDTO>> {
        return withContext(Dispatchers.IO) {
            Db.getJobItemEstimateDao().getJobMeasureForActivityId(activityId, activityId2)
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
            Db.getProjectItemDao().getItemForItemCode(sectionItemId)
        }
    }

    suspend fun getSection(sectionId: String): LiveData<ProjectSectionDTO> {
        return withContext(Dispatchers.IO) {
            Db.getProjectSectionDao().getSection(sectionId)
        }
    }

    suspend fun getAllItemsForProjectId(projectId: String): LiveData<List<ProjectItemDTO>> {
        return withContext(Dispatchers.IO) {
            Db.getProjectItemDao().getAllItemsForProjectId(projectId)
        }
    }

    suspend fun getAllItemsForSectionItem(
        sectionItemId: String,
        projectId: String
    ): LiveData<List<ProjectItemDTO>> {
        return withContext(Dispatchers.IO) {
            Db.getProjectItemDao().getAllItemsForSectionItem(sectionItemId, projectId)
        }
    }

    suspend fun getJobForJobId(jobId: String): LiveData<JobDTO> {
        return withContext(Dispatchers.IO) {
            Db.getJobDao().getJobFromJobId(jobId)
        }
    }

    suspend fun getJobItemEstimatePhotoForEstimateId(estimateId: String): LiveData<List<JobItemEstimatesPhotoDTO>> {
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

    suspend fun getJobItemsToMeasureForJobId(jobID: String?): LiveData<List<JobItemEstimateDTO>> {
        return withContext(Dispatchers.IO) {
            Db.getJobItemEstimateDao().getJobItemsToMeasureForJobId(jobID!!)
        }
    }

    suspend fun getJobEstimationItemsPhoto(estimateId: String): LiveData<List<JobItemEstimatesPhotoDTO>> {
        return withContext(Dispatchers.IO) {
            Db.getJobItemEstimatePhotoDao().getJobEstimationItemsPhoto(estimateId)
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

    suspend fun getJobEstiItemForEstimateId(estimateId: String?): LiveData<List<JobEstimateWorksDTO>> {
        return withContext(Dispatchers.IO) {
            Db.getEstimateWorkDao().getJobMeasureItemsForJobId(estimateId)
        }
    }

    suspend fun getWokrCodes(eId: Int): LiveData<List<WF_WorkStepDTO>> {
        return withContext(Dispatchers.IO) {
            Db.getWorkStepDao().getWorkflowSteps(eId)
        }
    }

    suspend fun getSectionByRouteSectionProject(
        sectionId: Int,
        linearId: String?,
        projectId: String?
    ): LiveData<String?> {
        return withContext(Dispatchers.IO) {
            Db.getProjectSectionDao()
                .getSectionByRouteSectionProject(sectionId.toString(), linearId!!, projectId)
        }

    }

    suspend fun getProjectSection(sectionId: String?): LiveData<ProjectSectionDTO> {
        return withContext(Dispatchers.IO) {
            Db.getProjectSectionDao().getSection(sectionId!!)
        }

    }

    suspend fun getItemForItemId(projectItemId: String?): LiveData<ProjectItemDTO> {
        return withContext(Dispatchers.IO) {
            Db.getProjectItemDao().getItemForItemId(projectItemId!!)
        }

    }

    fun updateNewJob(
        newjobId: String,
        startKM: Double,
        endKM: Double,
        sectionId: String,
        newJobItemEstimatesList: ArrayList<JobItemEstimateDTO>,
        jobItemSectionArrayList: ArrayList<JobSectionDTO>
    ) {
        Coroutines.io {
            if (Db.getJobDao().checkIfJobExist(newjobId)) {
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

    suspend fun getSingleJobFromJobId(jobId: String?): LiveData<JobDTO> {
        return withContext(Dispatchers.IO) {
            Db.getJobDao().getJobFromJobId(jobId!!)
        }
    }

    suspend fun jobExists(jobId: String?): Boolean {
        return withContext(Dispatchers.IO) {
            Db.getJobDao().checkIfJobExist(jobId!!)
        }
    }

    suspend fun getJobItemEstimateForEstimateId(estimateId: String): LiveData<JobItemEstimateDTO> {
        return withContext(Dispatchers.IO) {
            Db.getJobItemEstimateDao().getJobItemEstimateForEstimateId(estimateId)
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

    suspend fun getUOMForProjectItemId(projectItemId: String): String {
        return withContext(Dispatchers.IO) {
            Db.getProjectItemDao().getUOMForProjectItemId(projectItemId)
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

    fun delete(item: ItemDTOTemp) {
        Coroutines.io {
            Db.getItemDaoTemp().deleteItem(item)
        }
    }

    suspend fun saveNewItem(newjItem: ItemDTOTemp) {
        Coroutines.io {
            if (!Db.getItemDaoTemp().checkItemExistsItemId(newjItem.itemId)) {

                Db.getItemDaoTemp().insertItems(newjItem)
            }
        }
    }

    fun deleteItemList(jobId: String) {
        Coroutines.io {
            Db.getItemDaoTemp().deleteItemList(jobId)
        }
    }

    fun deleteJobFromList(jobId: String) {
        Coroutines.io {
            Db.getJobDao().deleteJobForJobId(jobId)
        }
    }

    suspend fun saveNewJob(newJob: JobDTO?) {
        Coroutines.io {
            if (newJob != null && !Db.getJobDao().checkIfJobExist(newJob.JobId)) {
                Db.getJobDao().insertOrUpdateJobs(newJob)
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
                if (matcher.find() && section != null) {
                    val itemCode = matcher.group(1).replace("\\s+".toRegex(), "")
                    if (!Db.getSectionItemDao().checkIfSectionitemsExist(itemCode))
                        Db.getSectionItemDao().insertSectionitem(
                            section,
                            itemCode,
                            sectionItemId
                        )
                }

            }

        }
    }

    private suspend fun saveContracts(contracts: List<ContractDTO>) {

        val actId = 3
        val workState = arrayOf("TA", "START", "MIDDLE", "END", "RTA")
        val workStateDescriptions = arrayOf(
            "Traffic Accommodation",
            "Work Start",
            "Work Middle",
            "Work Completed",
            "Removal of Traffic Accommodation"
        )
        for (step_code in workState.iterator()) {
            if (!Db.getWorkStepDao().checkWorkFlowStepExistsWorkCode(step_code))
                Db.getWorkStepDao().insertStepsCode(step_code, actId)

            for (desccri in workStateDescriptions.iterator()) {
                if (!Db.getWorkStepDao().checkWorkFlowStepExistsDesc(desccri))
                    Db.getWorkStepDao().updateStepsDesc(desccri, step_code)
            }
        }

        if (contracts.isNotEmpty()) {
            val validContracts = contracts.filter { contract ->
                contract.projects != null && !contract.contractId.isBlank()
            }
                .distinctBy { contract -> contract.contractId }
            for (contract in validContracts) {
                if (!Db.getContractDao().checkIfContractExists(contract.contractId))
                    Db.getContractDao().insertContract(contract)

                val validProjects =
                    contract.projects?.filter { project ->
                        !project.projectId.isBlank()
                    }?.distinctBy { project -> project.projectId }

                if (validProjects != null) {
                    for (project in validProjects) {
                            if (Db.getProjectDao().checkProjectExists(project.projectId)) {
                                Timber.i("Contract: ${contract.descr} (${contract.contractId}) ProjectId: ${project.descr} (${project.projectId}) -> Duplicated")
                                continue
                            } else {
                                try {
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
                                } catch (ex: Exception) {
                                    Timber.e(
                                        ex,
                                        "Contract: ${contract.descr} (${contract.contractId}) ProjectId: ${project.descr} (${project.projectId}) -> ${ex.message}"
                                    )
                                }
                            }
                            if (project.items != null) {
                                val distinctItems = project.items.distinctBy { item -> item.itemId }
                                for (item in distinctItems) {
                                    if (Db.getProjectItemDao()
                                            .checkItemExistsItemId(item.itemId)
                                    ) {
                                        continue
                                    } else {
                                        try {
                                            val pattern = Pattern.compile("(.*?)\\.")
                                            val matcher = pattern.matcher(item.itemCode)
                                            if (matcher.find()) {
                                                val itemCode = matcher.group(1) + "0"
                                                //  Lets Get the ID Back on Match
                                                val sectionItemId = Db.getSectionItemDao()
                                                    .getSectionItemId(
                                                        itemCode.replace(
                                                            "\\s+".toRegex(),
                                                            ""
                                                        )
                                                    )

                                                Db.getProjectItemDao().insertItem(
                                                    itemId = item.itemId,
                                                    itemCode = item.itemCode,
                                                    descr = item.descr,
                                                    itemSections = item.itemSections,
                                                    tenderRate = item.tenderRate,
                                                    uom = item.uom,
                                                    workflowId = item.workflowId,
                                                    sectionItemId = sectionItemId,
                                                    quantity = item.quantity,
                                                    estimateId = item.estimateId,
                                                    projectId = project.projectId
                                                )
                                            }
                                        } catch (ex: Exception) {
                                            Timber.e(ex, "ItemId: ${item.itemId} -> ${ex.message}")
                                        }
                                    }

                                }
                            }

                            if (project.projectSections != null) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    prefs.savelastSavedAt(LocalDateTime.now().toString())
                                }
                                for (section in project.projectSections) { //project.projectSections
                                    if (!Db.getProjectSectionDao()
                                            .checkSectionExists(section.sectionId)
                                    )
                                        try {
                                            Db.getProjectSectionDao().insertSection(
                                                section.sectionId,
                                                section.route,
                                                section.section,
                                                section.startKm,
                                                section.endKm,
                                                section.direction,
                                                project.projectId
                                            )
                                        } catch (ex: Exception) {
                                            Timber.e(
                                                ex,
                                                "ProjectSectionItemId ${section.sectionId} -> ${ex.message}"
                                            )
                                        }
                                }
                            }

                            if (project.voItems != null) {
                                for (voItem in project.voItems) { //project.voItems
                                    if (!Db.getVoItemDao().checkIfVoItemExist(voItem.projectVoId))
                                        try {
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
                                        } catch (ex: Exception) {
                                            Timber.e(
                                                ex,
                                                "VoItemProjectVoId: ${voItem.projectVoId} -> ${ex.message}"
                                            )
                                        }
                                }
                            }

                        }
                    }
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
        linearId: String,
        pointLocation: Double,
        sectionId: Int,
        projectId: String?,
        jobId: String?
    ) {
        if (linearId != null && !Db.getSectionPointDao()
                .checkSectionExists(sectionId, projectId, jobId)
        ) {
            Db.getSectionPointDao()
                .insertSection(direction, linearId, pointLocation, sectionId, projectId, jobId)
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

                    if (workFlow.workFlowRoute != null) {
                        for (workFlowRoute in workFlow.workFlowRoute!!) {
                            if (!Db.getWorkFlowRouteDao()
                                    .checkWorkFlowRouteExists(workFlowRoute.routeId)
                            )
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
                        if (ContractVoId != null) {
                            setContractVoId(DataConversion.toBigEndian(ContractVoId))
                        }
                        setTrackRouteId(DataConversion.toBigEndian(TrackRouteId))
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
                        if (!Db.getJobItemEstimateDao()
                                .checkIfJobItemEstimateExist(jobItemEstimate.estimateId)
                        ) {
                            jobItemEstimate.setEstimateId(DataConversion.toBigEndian(jobItemEstimate.estimateId))
                            jobItemEstimate.setJobId(DataConversion.toBigEndian(jobItemEstimate.jobId))
                            jobItemEstimate.setProjectItemId(
                                DataConversion.toBigEndian(
                                    jobItemEstimate.projectItemId
                                )
                            )
                            if (jobItemEstimate.trackRouteId != null)
                                jobItemEstimate.setTrackRouteId(
                                    DataConversion.toBigEndian(
                                        jobItemEstimate.trackRouteId
                                    )
                                ) else jobItemEstimate.trackRouteId = null

                            jobItemEstimate.setProjectVoId(
                                DataConversion.toBigEndian(
                                    jobItemEstimate.projectVoId
                                )
                            )
                            Db.getJobItemEstimateDao().insertJobItemEstimate(jobItemEstimate)
                            Db.getJobDao().setEstimateActId(jobItemEstimate.actId, job.JobId)
                            if (jobItemEstimate.jobItemEstimatePhotos != null) {
                                for (jobItemEstimatePhoto in jobItemEstimate.jobItemEstimatePhotos!!) {
                                    if (!Db.getJobItemEstimatePhotoDao()
                                            .checkIfJobItemEstimatePhotoExistsByPhotoId(
                                                jobItemEstimatePhoto.photoId
                                            )
                                    )
                                        jobItemEstimatePhoto.setPhotoPath(
                                            Environment.getExternalStorageDirectory()
                                                .toString() + File.separator
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
                                            if (!Db.getEstimateWorkPhotoDao()
                                                    .checkIfEstimateWorksPhotoExist(
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
                        if (jobItemEstimate.jobItemMeasure != null) {
                            for (jobItemMeasure in jobItemEstimate.jobItemMeasure) {
                                if (!Db.getJobItemMeasureDao().checkIfJobItemMeasureExists(
                                        jobItemMeasure.itemMeasureId!!
                                    )
                                ) {
                                    jobItemMeasure.setItemMeasureId(
                                        DataConversion.toBigEndian(
                                            jobItemMeasure.itemMeasureId
                                        )
                                    )
                                    jobItemMeasure.setJobId(
                                        DataConversion.toBigEndian(
                                            jobItemMeasure.jobId
                                        )
                                    )
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
                                    jobItemMeasure.setEstimateId(
                                        DataConversion.toBigEndian(
                                            jobItemMeasure.estimateId
                                        )
                                    )
                                    jobItemMeasure.setProjectVoId(
                                        DataConversion.toBigEndian(
                                            jobItemMeasure.projectVoId
                                        )
                                    )
                                    jobItemMeasure.setTrackRouteId(
                                        DataConversion.toBigEndian(
                                            jobItemMeasure.trackRouteId
                                        )
                                    )
                                    jobItemMeasure.setJobNo(job.JiNo)
                                    if (!Db.getJobItemMeasureDao().checkIfJobItemMeasureExists(
                                            jobItemMeasure.itemMeasureId!!
                                        )
                                    )
                                        Db.getJobItemMeasureDao().insertJobItemMeasure(
                                            jobItemMeasure
                                        )
                                    Db.getJobDao().setMeasureActId(jobItemMeasure.actId, job.JobId)
                                    Db.getJobItemEstimateDao().setMeasureActId(
                                        jobItemMeasure.actId,
                                        jobItemMeasure.estimateId!!
                                    )
//                            job.setMeasureActId(jobItemMeasure.actId)
                                    if (jobItemMeasure.jobItemMeasurePhotos != null) {
                                        for (jobItemMeasurePhoto in jobItemMeasure.jobItemMeasurePhotos) {
                                            if (!Db.getJobItemMeasurePhotoDao()
                                                    .checkIfJobItemMeasurePhotoExists(
                                                        jobItemMeasurePhoto.filename!!
                                                    )
                                            ) jobItemMeasurePhoto.setPhotoPath(
                                                Environment.getExternalStorageDirectory()
                                                    .toString() + File.separator
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
                                            Db.getJobItemMeasurePhotoDao()
                                                .insertJobItemMeasurePhoto(
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
                                            jobItemMeasurePhoto.setPhotoPath(
                                                Environment.getExternalStorageDirectory()
                                                    .toString() + File.separator
                                                        + PhotoUtil.FOLDER + File.separator + jobItemMeasurePhoto.filename
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
                        if (!Db.getJobItemMeasureDao()
                                .checkIfJobItemMeasureExists(jobItemMeasure.itemMeasureId!!)
                        ) {
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
                            if (!Db.getJobItemMeasureDao().checkIfJobItemMeasureExists(
                                    jobItemMeasure.itemMeasureId!!
                                )
                            )
                                Db.getJobItemMeasureDao().insertJobItemMeasure(jobItemMeasure)
                            Db.getJobDao().setMeasureActId(jobItemMeasure.actId, job.JobId)
                            Db.getJobItemEstimateDao()
                                .setMeasureActId(jobItemMeasure.actId, jobItemMeasure.estimateId!!)
//                            job.setMeasureActId(jobItemMeasure.actId)
                            if (jobItemMeasure.jobItemMeasurePhotos != null) {
                                for (jobItemMeasurePhoto in jobItemMeasure.jobItemMeasurePhotos) {
                                    if (!Db.getJobItemMeasurePhotoDao()
                                            .checkIfJobItemMeasurePhotoExists(
                                                jobItemMeasurePhoto.filename!!
                                            )
                                    ) jobItemMeasurePhoto.setPhotoPath(
                                        Environment.getExternalStorageDirectory()
                                            .toString() + File.separator
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
                                }
                            }
                        }
                    }
                }

            }
        }
    }

    suspend fun getPhotoForJobItemMeasure(filename: String) {

        val photoMeasure = apiRequest { api.getPhotoMeasure(filename) }
        measurePhoto.postValue(photoMeasure.photo, filename)
    }

    private suspend fun getPhotoForJobItemEstimate(filename: String) {
        val photoEstimate = apiRequest { api.getPhotoEstimate(filename) }
        estimatePhoto.postValue(photoEstimate.photo, filename)
    }

    private fun sendMSg(uploadResponse: String?) {
        val response: UploadImageResponse? = null
        if (uploadResponse.isNullOrEmpty())
            jobDataController?.setMsg(response!!.errorMessage)
    }

    private fun saveWorkflowJob(workflowj: WorkflowJobDTO?) {
        try {
            val job = setWorkflowJobBigEndianGuids(workflowj!!)
            insertOrUpdateWorkflowJobInSQLite(job)
        } catch (ex: NullPointerException) {
            Timber.e(ex, "Non-nullable WorkFlow Job is null.")
        }
    }

    private fun saveWorkflowJob2(workflowj: WorkflowJobDTO?) {
        if (workflowj != null) {
            val job = setWorkflowJobBigEndianGuids(workflowj)
            insertOrUpdateWorkflowJobInSQLite(job)
        }

    }

    private fun saveTaskList(toDoListGroups: ArrayList<ToDoGroupsDTO>?) {

        if (toDoListGroups != null) {
            saveUserTaskList(toDoListGroups)

        }
    }

    private fun saveUserTaskList(toDoListGroups: ArrayList<ToDoGroupsDTO>?) {
        Coroutines.io {
            if (toDoListGroups != null) {
                for (toDoListGroup in toDoListGroups) {
                    if (!Db.getToDoGroupsDao().checkIfGroupCollectionExist(toDoListGroup.groupId)) {
                        Db.getToDoGroupsDao().insertToDoGroups(toDoListGroup)
                    }

                    val entitiesArrayList = toDoListGroup.toDoListEntities

                    for (toDoListEntity in entitiesArrayList) {
                        val jobId = getJobIdFromPrimaryKeyValues(toDoListEntity.primaryKeyValues)
                        insertEntity(toDoListEntity, jobId!!)
                        val newJobId = DataConversion.toLittleEndian(jobId)
                        fetchJobList(newJobId!!)
                    }

                }

            }
        }
    }

    private fun insertEntity(entity: ToDoListEntityDTO, jobId: String) {
        Coroutines.io {

            if (!Db.getEntitiesDao()
                    .checkIfEntitiesExist(DataConversion.bigEndianToString(entity.trackRouteId!!))
            ) {
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
        val jobResponse = apiRequest { api.getJobsForApproval(jobId) }
        job.postValue(jobResponse.job)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun fetchContracts(userId: String) {
        val lastSavedAt = prefs.getLastSavedAt()

        if (lastSavedAt == null || isFetchNeeded(LocalDateTime.parse(lastSavedAt))) {

            val activitySectionsResponse =
                apiRequest { api.activitySectionsRefresh(userId) }
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

    }

    suspend fun getUserTaskList(): LiveData<List<ToDoListEntityDTO>> {


        return withContext(Dispatchers.IO) {
            val userId = Db.getUserDao().getUserID()
            fetchUserTaskList(userId)
            Db.getEntitiesDao().getAllEntities()
        }


    }

    suspend fun fetchUserTaskList(userId: String) {

        val toDoListGroupsResponse = apiRequest { api.getUserTaskList(userId) }
        toDoListGroups.postValue(toDoListGroupsResponse.toDoListGroups)
    }

    suspend fun refreshActivitySections(userId: String) {
        val activitySectionsResponse =
            apiRequest { api.activitySectionsRefresh(userId) }
        sectionItems.postValue(activitySectionsResponse.activitySections)
    }

    suspend fun refreshWorkflows(userId: String) {
        val workFlowResponse = apiRequest { api.workflowsRefresh(userId) }
        workFlow.postValue(workFlowResponse.workFlows)
    }

    suspend fun refreshLookups(userId: String) {
        val lookupResponse = apiRequest { api.lookupsRefresh(userId) }
        lookups.postValue(lookupResponse.mobileLookups)
    }

    suspend fun refreshContractInfo(userId: String) {
        val contractsResponse = apiRequest { api.refreshContractInfo(userId) }
        conTracts.postValue(contractsResponse.contracts)
    }

    private suspend fun fetchAllData(userId: String) {
        // TODO: Redo as async calls in parallel

        refreshActivitySections(userId)

        refreshWorkflows(userId)

        refreshLookups(userId)

        fetchUserTaskList(userId)

        refreshContractInfo(userId)

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

        Db.run {
            getEstimateWorkDao().deleteAll()
            getEstimateWorkPhotoDao().deleteAll()
            getLookupDao().deleteAll()
            getLookupOptionDao().deleteAll()
            getPrimaryKeyValueDao().deleteAll()
            getToDoGroupsDao().deleteAll()
            getEntitiesDao().deleteAll()
            getUserRoleDao().deleteAll()
            getUserDao().deleteAll()
            getJobDao().deleteAll()
            getJobSectionDao().deleteAll()
            getJobItemEstimateDao().deleteAll()
            getJobItemMeasureDao().deleteAll()
            getJobItemEstimatePhotoDao().deleteAll()
            getJobItemMeasurePhotoDao().deleteAll()
            getContractDao().deleteAll()
            getVoItemDao().deleteAll()
            getProjectDao().deleteAll()
            getProjectItemDao().deleteAll()
            getItemSectionDao().deleteAll()
            getProjectSectionDao().deleteAll()
            getWorkFlowDao().deleteAll()
            getWorkFlowRouteDao().deleteAll()
            getWorkflowsDao().deleteAll()
            getInfoClassDao().deleteAll()
            getActivityDao().deleteAll()
            getSectionItemDao().deleteAll()
            getItemDaoTemp().deleteAll()
            getSectionPointDao().deleteAll()
        }
        return null
    }

    suspend fun getJobApproveMeasureForActivityId(activityId: Int): LiveData<List<JobItemMeasureDTO>> {
        return withContext(Dispatchers.IO) {
            Db.getJobItemMeasureDao().getJobApproveMeasureForActivityId(activityId)
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

    private fun <T> MutableLiveData<T>.postValue(photo: String?, fileName: String) {
        saveEstimatePhoto(photo, fileName)
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

    private fun <T> MutableLiveData<T>.postValue(
        workflowJob: WorkflowJobDTO,
        jobItemMeasure: ArrayList<JobItemMeasureDTO>,
        activity: FragmentActivity?,
        itemMeasureJob: JobDTO
    ) {
        if (workflowJob != null) {
            val job = setWorkflowJobBigEndianGuids(workflowJob)
            insertOrUpdateWorkflowJobInSQLite(job)
            Coroutines.io {
                val myjob = getUpdatedJob(itemMeasureJob.JobId)
                uploadmeasueImages(jobItemMeasure, activity, myjob)
            }

        }
    }

    private fun uploadmeasueImages(
        jobItemMeasures: ArrayList<JobItemMeasureDTO>,
        activity: FragmentActivity?,
        itemMeasureJob: JobDTO
    ) {
        val imageCounter = 1
        var totalImages = 0
        if (jobItemMeasures != null) {
            for (jobItemMeasure in jobItemMeasures.iterator()) {
                if (jobItemMeasure.jobItemMeasurePhotos != null) {
                    for (photo in jobItemMeasure.jobItemMeasurePhotos) {
                        if (PhotoUtil.photoExist(photo.filename!!)) {
                            val data: ByteArray =
                                getData(photo.filename, PhotoQuality.HIGH, activity!!)
                            uploadmeasueImage(
                                photo.filename,
                                activity.getString(R.string.jpg),
                                data,
                                imageCounter,
                                totalImages,
                                itemMeasureJob,
                                activity
                            )
                            totalImages++
                        }
                    }


                }
            }
        }


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

        processImageUpload(
            filename,
            extension,
            photo,
            totalImages,
            imageCounter,
            itemMeasureJob,
            activity!!
        )

    }

    private fun <T> MutableLiveData<T>.postValue(
        response: String?,
        jobEstimateWorks: JobEstimateWorksDTO,
        activity: FragmentActivity,
        useR: UserDTO
    ) {
        if (response == null) {
            moveJobToNextWorkflowStep(jobEstimateWorks, useR)
        }
    }

    private fun moveJobToNextWorkflowStep(
        jobEstimateWorks: JobEstimateWorksDTO,
        useR: UserDTO
    ) {
        if (jobEstimateWorks.trackRouteId == null) {
            throw Exception("Error: trackRouteId is null")
        } else {
            jobEstimateWorks.setTrackRouteId(DataConversion.toLittleEndian(jobEstimateWorks.trackRouteId))
            val direction: Int = WorkflowDirection.NEXT.value
            val trackRouteId: String = jobEstimateWorks.trackRouteId
            val description = "work step done"

            Coroutines.io {
                val workflowMoveResponse = apiRequest {
                    api.getWorkflowMove(
                        useR.userId,
                        trackRouteId,
                        description,
                        direction
                    )
                }
                workflowJ.postValue(workflowMoveResponse.workflowJob)
                workflows.postValue(workflowMoveResponse.toDoListGroups)
            }


        }
    }

    private fun <T> MutableLiveData<T>.postValue(
        workflowj: WorkflowJobDTO,
        job: JobDTO,
        activity: FragmentActivity
    ) {
//        Coroutines.io {
        if (workflowj != null) {
            val createJob = setWorkflowJobBigEndianGuids(workflowj)
            insertOrUpdateWorkflowJobInSQLite(createJob)
            uploadcreateJobImages(job, activity)
        }
//        }
    }

    suspend fun getUpdatedJob(jobId: String): JobDTO {
        return withContext(Dispatchers.IO) {
            Db.getJobDao().getJobForJobId(jobId)
        }
    }

    private fun setWorkflowJobBigEndianGuids(job: WorkflowJobDTO): WorkflowJobDTO? {

        job.jobId = DataConversion.toBigEndian(job.jobId)
        job.trackRouteId = DataConversion.toBigEndian(job.trackRouteId)

        if (job.workflowItemEstimates != null) {
            for (jie in job.workflowItemEstimates) {

                jie.estimateId = DataConversion.toBigEndian(jie.estimateId)!!
                jie.trackRouteId = DataConversion.toBigEndian(jie.trackRouteId)!!
                //  Lets go through the WorkFlowEstimateWorks
                for (wfe in jie.workflowEstimateWorks) {
                    wfe.trackRouteId = DataConversion.toBigEndian(wfe.trackRouteId)!!
                    wfe.worksId = DataConversion.toBigEndian(wfe.worksId)!!

                    wfe.estimateId = DataConversion.toBigEndian(wfe.estimateId)!!

                }
            }
        }
        if (job.workflowItemMeasures != null) {
            for (jim in job.workflowItemMeasures) {

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

    private fun uploadcreateJobImages(packagejob: JobDTO, activity: FragmentActivity) {
        var imageCounter = 1
        var totalImages = 0


        when {
            packagejob.JobItemEstimates != null -> {
                if (packagejob.JobItemEstimates!!.isNotEmpty()) {
                    for (jobItemEstimate in packagejob.JobItemEstimates!!) {
                        if (jobItemEstimate.jobItemEstimatePhotos != null && jobItemEstimate.jobItemEstimatePhotos!!.size > 0) {
                            val photos: Array<JobItemEstimatesPhotoDTO> =
                                arrayOf<JobItemEstimatesPhotoDTO>(
                                    jobItemEstimate.jobItemEstimatePhotos!![0],
                                    jobItemEstimate.jobItemEstimatePhotos!![1]
                                )
                            for (jobItemEstimatePhoto in photos) {
                                if (PhotoUtil.photoExist(jobItemEstimatePhoto.filename)) {
                                    Timber.d("x -> UploadRrImage $imageCounter")
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
                                    Timber.d("x -> Error: photo filename is empty!")
                                }
                            }
                        } else {
                            Timber.d("x -> Error: photos are empty!")
                        }
                    }
                }
            }
            else -> {
                Timber.d("x -> Error: no job item estimates.")
            }
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
        packagejob: JobDTO,
        activity: FragmentActivity
    ) {

        Coroutines.io {
            val imagedata = JsonObject()
            imagedata.addProperty("Filename", filename)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                imagedata.addProperty("ImageByteArray", Base64.getEncoder().encodeToString(photo))
            }
            imagedata.addProperty("ImageFileExtension", extension)
            Log.e("JsonObject", "Json string $imagedata")

            val uploadImageResponse = apiRequest { api.uploadRrmImage(imagedata) }
            photoupload.postValue(uploadImageResponse.errorMessage)
            if (totalImages <= imageCounter)
                Coroutines.io {
                    moveJobToNextWorkflow(packagejob, activity)
                }
        }
    }

    private fun moveJobToNextWorkflow(
        job: JobDTO,
        activity: FragmentActivity
    ) {

        if (job.TrackRouteId == null) {
            throw Exception("Error: trackRouteId is null")
        } else {
            job.setTrackRouteId(DataConversion.toLittleEndian(job.TrackRouteId))
            val direction: Int = WorkflowDirection.NEXT.value
            val trackRouteId: String = job.TrackRouteId!!
            val description: String = activity.resources.getString(R.string.submit_for_approval)

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
                workflows.postValue(workflowMoveResponse.toDoListGroups)
            }

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

    fun deleteItemMeasurefromList(itemMeasureId: String) {
        Coroutines.io {
            Db.getJobItemMeasureDao().deleteItemMeasurefromList(itemMeasureId)
        }
    }

    fun deleteItemMeasurephotofromList(itemMeasureId: String) {
        Coroutines.io {
            Db.getJobItemMeasurePhotoDao().deleteItemMeasurephotofromList(itemMeasureId)
        }
    }

    fun deleteItemfromList(itemId: String) {
        Coroutines.io {
            Db.getItemDaoTemp().deleteItemfromList(itemId)
        }
    }

    suspend fun createEstimateWorksPhoto(
        estimateWorksPhotos: ArrayList<JobEstimateWorksPhotoDTO>,
        itemEstiWorks: JobEstimateWorksDTO
    ) {
        Coroutines.io {
            if (estimateWorksPhotos != null) {
                for (estimateWorksPhoto in estimateWorksPhotos) {
                    if (!Db.getEstimateWorkPhotoDao().checkIfEstimateWorksPhotoExist(
                            estimateWorksPhoto.filename
                        )
                    ) {
                        Db.getEstimateWorkPhotoDao().insertEstimateWorksPhoto(estimateWorksPhoto)
                    } // else {
//                Db.getEstimateWorkPhotoDao().updateExistingEstimateWorksPhoto(estimateWorksPhoto, estimatId)
                    // }

                }
                Db.getEstimateWorkDao().updateJobEstimateWorkForEstimateID(
                    itemEstiWorks.jobEstimateWorksPhotos!!,
                    itemEstiWorks.estimateId
                )
            }

        }

    }

    private fun SaveMeasurementResponse.getWorkflowJob(): WorkflowJobDTO {
        return workflowJob
    }

    private fun JobResponse.getWorkflowJob(): WorkflowJobDTO {
        return workflowJob
    }

    private fun JobItemMeasureDTO.setItemMeasurePhotoDTO(jobItemMeasurePhotoDTO: ArrayList<JobItemMeasurePhotoDTO>) {
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

    companion object {
        val TAG: String = OfflineDataRepository::class.java.simpleName
    }

}

