package za.co.xisystems.itis_rrm.data.repositories

// import sun.security.krb5.Confounder.bytes

// import android.app.Activity
import android.os.Environment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import za.co.xisystems.itis_rrm.custom.results.XIProgress
import za.co.xisystems.itis_rrm.custom.results.XIResult
import za.co.xisystems.itis_rrm.custom.results.XIStatus
import za.co.xisystems.itis_rrm.custom.results.XISuccess
import za.co.xisystems.itis_rrm.data.localDB.AppDatabase
import za.co.xisystems.itis_rrm.data.localDB.JobDataController
import za.co.xisystems.itis_rrm.data.localDB.entities.ContractDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.ItemDTOTemp
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobEstimateWorksDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobEstimateWorksPhotoDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimateDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimatesPhotoDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemMeasureDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemMeasurePhotoDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobSectionDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.LookupDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.PrimaryKeyValueDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.ProjectDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.ProjectItemDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.ProjectSectionDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.SectionItemDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.ToDoGroupsDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.ToDoListEntityDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.UserDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.UserRoleDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.VoItemDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.WorkFlowDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.WorkFlowsDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.WorkflowItemEstimateDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.WorkflowItemMeasureDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.WorkflowJobDTO
import za.co.xisystems.itis_rrm.data.network.BaseConnectionApi
import za.co.xisystems.itis_rrm.data.network.SafeApiRequest
import za.co.xisystems.itis_rrm.data.network.responses.UploadImageResponse
import za.co.xisystems.itis_rrm.data.preferences.PreferenceProvider
import za.co.xisystems.itis_rrm.utils.Coroutines
import za.co.xisystems.itis_rrm.utils.DataConversion
import za.co.xisystems.itis_rrm.utils.PhotoUtil
import za.co.xisystems.itis_rrm.utils.SqlLitUtils
import java.io.File
import java.util.regex.Pattern

private val jobDataController: JobDataController? = null

/**
 * OfflineDataRepository - fetching from mobile Services
 */
class OfflineDataRepository(
    private val api: BaseConnectionApi,
    private val appDb: AppDatabase,
    private val prefs: PreferenceProvider
) : SafeApiRequest() {
    var entitiesFetched = false
    private val activity: FragmentActivity? = null
    private val conTracts = MutableLiveData<List<ContractDTO>>()
    private val sectionItems = MutableLiveData<ArrayList<String>>()
    private val job = MutableLiveData<JobDTO>()
    private val estimatePhoto = MutableLiveData<String>()
    private val measurePhoto = MutableLiveData<String>()
    private val workFlow = MutableLiveData<WorkFlowsDTO>()
    private val lookups = MutableLiveData<ArrayList<LookupDTO>>()
    private val toDoListGroups = MutableLiveData<ArrayList<ToDoGroupsDTO>>()
    private val workflows = MutableLiveData<ArrayList<ToDoGroupsDTO>>()
    private val workflowJ = MutableLiveData<WorkflowJobDTO>()
    private val workflowJ2 = MutableLiveData<WorkflowJobDTO>()
    private val photoUpload = MutableLiveData<String>()

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

        photoUpload.observeForever {
            sendMSg(it)
        }
    }

    val bigSyncDone: MutableLiveData<Boolean> = MutableLiveData()
    val toDoListStatus: MutableLiveData<XIResult<Boolean>> = MutableLiveData()

    suspend fun bigSyncCheck() {
        withContext(Dispatchers.IO) {
            bigSyncDone.postValue(appDb.getContractDao().countContracts() >= 1)
        }
    }

    val databaseStatus: MutableLiveData<XIResult<Boolean>> = MutableLiveData()

    suspend fun getContracts(): LiveData<List<ContractDTO>> {
        return withContext(Dispatchers.IO) {
            appDb.getContractDao().getAllContracts()
        }
    }

    suspend fun getRoles(): LiveData<List<UserRoleDTO>> {
        return withContext(Dispatchers.IO) {
            appDb.getUserRoleDao().getRoles()
        }
    }

    suspend fun getWorkFlows(): LiveData<List<WorkFlowDTO>> {

        return withContext(Dispatchers.IO) {
            val userId = appDb.getUserDao().getUserID()
            fetchAllData(userId)
            appDb.getWorkFlowDao().getWorkflows()
        }
    }

    suspend fun getSectionItems(): LiveData<List<SectionItemDTO>> {
        return withContext(Dispatchers.IO) {
            appDb.getSectionItemDao().getSectionItems()
        }
    }

    suspend fun getUser(): LiveData<UserDTO> {
        return withContext(Dispatchers.IO) {
            appDb.getUserDao().getUser()
        }
    }

    suspend fun getJobsForActivityId(activityId: Int): LiveData<List<JobDTO>> {
        return withContext(Dispatchers.IO) {
            appDb.getJobDao().getJobsForActivityId(activityId)
        }
    }

    suspend fun getJobsForActId(activityId: Int): LiveData<List<JobDTO>> {
        return withContext(Dispatchers.IO) {
            appDb.getJobDao().getJobsForActivityId(activityId)
        }
    }

    suspend fun getJobsForActivityIds1(activityId1: Int, activityId2: Int): LiveData<List<JobDTO>> {
        return withContext(Dispatchers.IO) {
            appDb.getJobDao().getJobsForActivityIds1(activityId1, activityId2)
        }
    }

    suspend fun getJobMeasureForActivityId(
        activityId: Int,
        activityId2: Int
    ): LiveData<List<JobItemEstimateDTO>> {
        return withContext(Dispatchers.IO) {
            appDb.getJobItemEstimateDao().getJobMeasureForActivityId(activityId, activityId2)
        }
    }

    suspend fun getAllItemsForProjectId(projectId: String): LiveData<List<ProjectItemDTO>> {
        return withContext(Dispatchers.IO) {
            appDb.getProjectItemDao().getAllItemsForProjectId(projectId)
        }
    }

    suspend fun getAllItemsForSectionItem(
        sectionItemId: String,
        projectId: String
    ): LiveData<List<ProjectItemDTO>> {
        return withContext(Dispatchers.IO) {
            appDb.getProjectItemDao().getAllItemsForSectionItemByProject(sectionItemId, projectId)
        }
    }

    suspend fun getJobMeasureItemsPhotoPath(itemMeasureId: String): List<String> {
        return withContext(Dispatchers.IO) {
            appDb.getJobItemMeasurePhotoDao().getJobMeasureItemPhotoPaths(itemMeasureId)
        }
    }

    suspend fun getProjectSection(sectionId: String?): LiveData<ProjectSectionDTO> {
        return withContext(Dispatchers.IO) {
            appDb.getProjectSectionDao().getSection(sectionId!!)
        }
    }

    suspend fun getItemForItemId(projectItemId: String?): LiveData<ProjectItemDTO> {
        return withContext(Dispatchers.IO) {
            appDb.getProjectItemDao().getItemForItemId(projectItemId!!)
        }
    }

    suspend fun getProjectSectionIdForJobId(jobId: String?): String {
        return withContext(Dispatchers.IO) {
            appDb.getJobSectionDao().getProjectSectionId(jobId!!)
        }
    }

    suspend fun getProjectDescription(projectId: String): String {
        return withContext(Dispatchers.IO) {
            appDb.getProjectDao().getProjectDescription(projectId)
        }
    }

    suspend fun getProjectItemDescription(projectItemId: String): String {
        return withContext(Dispatchers.IO) {
            appDb.getProjectItemDao().getProjectItemDescription(projectItemId)
        }
    }

    suspend fun getUOMForProjectItemId(projectItemId: String): String {
        return withContext(Dispatchers.IO) {
            appDb.getProjectItemDao().getUOMForProjectItemId(projectItemId)
        }
    }

    suspend fun getJobMeasureItemsForJobId(
        jobID: String?,
        actId: Int
    ): LiveData<List<JobItemMeasureDTO>> {
        return withContext(Dispatchers.IO) {
            appDb.getJobItemMeasureDao().getJobMeasureItemsForJobId(jobID!!, actId)
        }
    }

    suspend fun getItemDescription(jobId: String): String {
        return withContext(Dispatchers.IO) {
            appDb.getJobDao().getItemDescription(jobId)
        }
    }

    suspend fun getItemJobNo(jobId: String): String {
        return withContext(Dispatchers.IO) {
            appDb.getJobDao().getItemJobNo(jobId)
        }
    }

    suspend fun getRouteForProjectSectionId(sectionId: String?): String {
        return withContext(Dispatchers.IO) {
            appDb.getProjectSectionDao().getRouteForProjectSectionId(sectionId!!)
        }
    }

    suspend fun getSectionForProjectSectionId(sectionId: String?): String {
        return withContext(Dispatchers.IO) {
            appDb.getProjectSectionDao().getSectionForProjectSectionId(sectionId!!)
        }
    }

    fun delete(item: ItemDTOTemp) {
        Coroutines.io {
            appDb.getItemDaoTemp().deleteItem(item)
        }
    }

    fun deleteItemList(jobId: String) {
        Coroutines.io {
            appDb.getItemDaoTemp().deleteItemList(jobId)
        }
    }

    fun deleteJobFromList(jobId: String) {
        Coroutines.io {
            appDb.getJobDao().deleteJobForJobId(jobId)
        }
    }

    private fun saveSectionsItems(sections: ArrayList<String>?) {
        Coroutines.io {

            sections?.forEach { section ->
                //  Let's get the String
                val pattern = Pattern.compile("(.*?):")
                val matcher = pattern.matcher(section)

                val sectionItemId = SqlLitUtils.generateUuid()
                if (matcher.find() && section.isNotEmpty()) {
                    val itemCode = matcher.group(1)?.replace("\\s+".toRegex(), "")
                    itemCode?.let {
                        try {
                            if (!appDb.getSectionItemDao().checkIfSectionitemsExist(it))
                                appDb.getSectionItemDao().insertSectionitem(
                                    section,
                                    it,
                                    sectionItemId
                                )
                        } catch (e: Exception) {
                            Timber.e(e, "Exception creating section item $itemCode")
                        }
                    }
                }
            }
        }
    }

    private suspend fun saveContracts(contracts: List<ContractDTO>) {

        createWorkflowSteps()

        if (contracts.isNotEmpty()) {
            val validContracts = contracts.filter { contract ->
                contract.projects != null && !contract.contractId.isBlank()
            }
                .distinctBy { contract -> contract.contractId }
            contractMax += validContracts.count()
            validContracts.forEach { contract ->
                if (!appDb.getContractDao().checkIfContractExists(contract.contractId)) {
                    appDb.getContractDao().insertContract(contract)
                    contractCount++

                    val validProjects =
                        contract.projects?.filter { project ->
                            !project.projectId.isBlank()
                        }?.distinctBy { project -> project.projectId }

                    validProjects?.let {
                        updateProjects(validProjects, contract)
                    }
                }
            }
        }
    }

    private fun createWorkflowSteps() {
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
            if (!appDb.getWorkStepDao().checkWorkFlowStepExistsWorkCode(step_code))
                appDb.getWorkStepDao().insertStepsCode(step_code, actId)

            for (description in workStateDescriptions.iterator()) {
                if (!appDb.getWorkStepDao().checkWorkFlowStepExistsDesc(description))
                    appDb.getWorkStepDao().updateStepsDesc(description, step_code)
            }
        }
    }

    private fun updateProjects(
        validProjects: List<ProjectDTO>?,
        contract: ContractDTO
    ) {
        Coroutines.api {
            projectMax += validProjects?.count() ?: 0

            validProjects?.forEach { project ->
                if (appDb.getProjectDao().checkProjectExists(project.projectId)) {
                    Timber.i("Contract: ${contract.shortDescr} (${contract.contractId}) ProjectId: ${project.descr} (${project.projectId}) -> Duplicated")
                } else {
                    try {

                        appDb.getProjectDao().insertProject(
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

                        project.items?.let { items ->
                            updateProjectItems(items, project)
                        }

                        project.projectSections?.let { projectSections ->
                            updateProjectSections(projectSections, project)
                        }

                        project.voItems?.let { voItems ->
                            updateVOItems(voItems, project)
                        }

                        projectCount++

                        if (contractCount >= contractMax && projectCount >= projectMax) {
                            databaseStatus.postValue(XIStatus("All projects retrieved."))
                            databaseStatus.postValue(XISuccess(true))
                            databaseStatus.postValue(XIProgress(false))
                        }
                    } catch (ex: Exception) {
                        Timber.e(
                            ex,
                            "Contract: ${contract.shortDescr} (${contract.contractId}) ProjectId: ${project.descr} (${project.projectId}) -> ${ex.message}"
                        )
                    }
                }
            }
        }
    }

    private fun updateProjectItems(
        distinctItems: List<ProjectItemDTO>,
        project: ProjectDTO
    ) {
        distinctItems.forEach { item ->
            if (!appDb.getProjectItemDao()
                    .checkItemExistsItemId(item.itemId)
            ) {
                try {
                    val pattern = Pattern.compile("(.*?)\\.")
                    val matcher = pattern.matcher(item.itemCode!!)
                    if (matcher.find()) {
                        val itemCode = "${matcher.group(1)}0"
                        //  Let's Get the ID Back on Match
                        val sectionItemId = appDb.getSectionItemDao()
                            .getSectionItemId(
                                itemCode.replace(
                                    "\\s+".toRegex(),
                                    ""
                                )
                            )

                        appDb.getProjectItemDao().insertItem(
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

    private fun updateProjectSections(
        projectSections: ArrayList<ProjectSectionDTO>,
        project: ProjectDTO
    ) {
        projectSections.forEach { section ->
            if (!appDb.getProjectSectionDao()
                    .checkSectionExists(section.sectionId)
            )
                try {
                    appDb.getProjectSectionDao().insertSection(
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

    private fun updateVOItems(
        voItems: ArrayList<VoItemDTO>?,
        project: ProjectDTO
    ) {
        voItems?.forEach { voItem ->
            if (!appDb.getVoItemDao().checkIfVoItemExist(voItem.projectVoId))
                try {
                    appDb.getVoItemDao().insertVoItem(
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

    private fun saveWorkFlowsInfo(workFlows: WorkFlowsDTO) {
        Coroutines.io {

            appDb.getWorkflowsDao().insertWorkFlows(workFlows)

            workFlows.workflows.forEach { workFlow ->
                if (!appDb.getWorkFlowDao().checkWorkFlowExistsWorkflowID(workFlow.workflowId))
                    appDb.getWorkFlowDao().insertWorkFlow(workFlow)

                workFlow.workFlowRoute?.let {
                    saveWorkflowRoutes(workFlow)
                }
            }

            workFlows.activities.forEach { activity ->
                appDb.getActivityDao().insertActivitys(activity)
            }

            workFlows.infoClasses.forEach { infoClass ->
                appDb.getInfoClassDao().insertInfoClasses(infoClass)
            }
        }
    }

    private fun saveWorkflowRoutes(workFlow: WorkFlowDTO) {
        for (workFlowRoute in workFlow.workFlowRoute!!) {
            if (!appDb.getWorkFlowRouteDao()
                    .checkWorkFlowRouteExists(workFlowRoute.routeId)
            )
                appDb.getWorkFlowRouteDao().insertWorkFlowRoute(
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

    private fun saveJobs(job: JobDTO?) {
        Coroutines.io {
            job?.let {

                if (!appDb.getJobDao().checkIfJobExist(job.JobId)) {
                    job.run {
                        setJobId(DataConversion.toBigEndian(JobId))
                        setProjectId(DataConversion.toBigEndian(ProjectId))
                        if (ContractVoId != null) {
                            setContractVoId(DataConversion.toBigEndian(ContractVoId))
                        }
                        setTrackRouteId(DataConversion.toBigEndian(TrackRouteId))
                    }
                    job.PerfitemGroupId = DataConversion.toBigEndian(job.PerfitemGroupId)
                    job.ProjectVoId = DataConversion.toBigEndian(job.ProjectVoId)
                    appDb.getJobDao().insertOrUpdateJobs(job)
                }

                job.JobSections?.let {
                    saveJobSections(job)
                }

                job.JobItemEstimates?.let {
                    saveJobItemEstimates(job)
                }

                job.JobItemMeasures?.let {
                    saveJobItemMeasuresForJob(job)
                }
            }
        }
    }

    private suspend fun saveJobItemMeasuresForJob(
        job: JobDTO
    ) {
        for (jobItemMeasure in job.JobItemMeasures!!) {
            if (!appDb.getJobItemMeasureDao()
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
                jobItemMeasure.setQty(jobItemMeasure.qty)
                jobItemMeasure.setDeleted(0)
                if (!appDb.getJobItemMeasureDao()
                        .checkIfJobItemMeasureExists(jobItemMeasure.itemMeasureId!!)
                )
                    appDb.getJobItemMeasureDao().insertJobItemMeasure(jobItemMeasure)

                appDb.getJobDao().setMeasureActId(jobItemMeasure.actId, job.JobId)
                appDb.getJobItemEstimateDao()
                    .setMeasureActId(jobItemMeasure.actId, jobItemMeasure.estimateId!!)

                if (jobItemMeasure.jobItemMeasurePhotos.isNotEmpty()) {
                    saveJobItemMeasurePhotos(jobItemMeasure)
                }

                appDb.getJobItemMeasureDao().undeleteMeasurement(jobItemMeasure.itemMeasureId!!)
            }
        }
    }

    private suspend fun saveJobSections(job: JobDTO) {
        for (jobSection in job.JobSections!!) {
            if (!appDb.getJobSectionDao().checkIfJobSectionExist(jobSection.jobSectionId))
                jobSection.setJobSectionId(DataConversion.toBigEndian(jobSection.jobSectionId))
            jobSection.setProjectSectionId(DataConversion.toBigEndian(jobSection.projectSectionId))
            jobSection.setJobId(DataConversion.toBigEndian(jobSection.jobId))
            appDb.getJobSectionDao().insertJobSection(
                jobSection
            )
        }
    }

    private suspend fun saveJobItemEstimates(
        job: JobDTO
    ) {
        job.JobItemEstimates?.forEach { jobItemEstimate ->
            if (!appDb.getJobItemEstimateDao()
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

                appDb.getJobItemEstimateDao().insertJobItemEstimate(jobItemEstimate)
                appDb.getJobDao().setEstimateActId(jobItemEstimate.actId, job.JobId)
                jobItemEstimate.jobItemEstimatePhotos?.let {
                    saveJobItemEstimatePhotos(jobItemEstimate)
                }

                jobItemEstimate.jobEstimateWorks?.let {
                    saveJobItemEstimateWorks(jobItemEstimate, job)
                }

                jobItemEstimate.jobItemMeasure?.let {
                    saveJobItemMeasuresForEstimate(jobItemEstimate.jobItemMeasure, job)
                }
            }
        }
    }

    private suspend fun saveJobItemEstimatePhotos(
        jobItemEstimate: JobItemEstimateDTO
    ) {
        for (jobItemEstimatePhoto in jobItemEstimate.jobItemEstimatePhotos!!) {
            if (!appDb.getJobItemEstimatePhotoDao()
                    .checkIfJobItemEstimatePhotoExistsByPhotoId(
                        jobItemEstimatePhoto.photoId
                    )
            )
                jobItemEstimatePhoto.setPhotoPath(
                    Environment.getExternalStorageDirectory()
                        .toString() + File.separator +
                        PhotoUtil.FOLDER + File.separator + jobItemEstimatePhoto.filename
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
            appDb.getJobItemEstimatePhotoDao().insertJobItemEstimatePhoto(
                jobItemEstimatePhoto
            )
            if (!PhotoUtil.photoExist(jobItemEstimatePhoto.filename)) {
                getPhotoForJobItemEstimate(jobItemEstimatePhoto.filename)
            }
        }
    }

    private suspend fun saveJobItemEstimateWorks(
        jobItemEstimate: JobItemEstimateDTO,
        job: JobDTO
    ) {
        for (jobEstimateWorks in jobItemEstimate.jobEstimateWorks!!) {
            if (!appDb.getEstimateWorkDao().checkIfJobEstimateWorksExist(
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
            appDb.getEstimateWorkDao().insertJobEstimateWorks(
                jobEstimateWorks
            )
            appDb.getJobDao()
                .setEstimateWorksActId(jobEstimateWorks.actId, job.JobId)
//                                    job.setEstimateWorksActId(jobEstimateWorks.actId)
            if (jobEstimateWorks.jobEstimateWorksPhotos != null) {
                saveJobItemEstimateWorksPhotos(jobEstimateWorks)
            }
        }
    }

    private suspend fun saveJobItemEstimateWorksPhotos(jobEstimateWorks: JobEstimateWorksDTO) {
        for (estimateWorksPhoto in jobEstimateWorks.jobEstimateWorksPhotos!!) {
            if (!appDb.getEstimateWorkPhotoDao()
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
            appDb.getEstimateWorkPhotoDao().insertEstimateWorksPhoto(
                estimateWorksPhoto
            )
        }
    }

    private suspend fun saveJobItemMeasuresForEstimate(
        jobItemMeasures: java.util.ArrayList<JobItemMeasureDTO>,
        job: JobDTO
    ) {
        for (jobItemMeasure in jobItemMeasures) {
            if (!appDb.getJobItemMeasureDao().checkIfJobItemMeasureExists(
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
                if (!appDb.getJobItemMeasureDao().checkIfJobItemMeasureExists(
                        jobItemMeasure.itemMeasureId!!
                    )
                )
                    appDb.getJobItemMeasureDao().insertJobItemMeasure(
                        jobItemMeasure
                    )
                appDb.getJobDao().setMeasureActId(jobItemMeasure.actId, job.JobId)
                appDb.getJobItemEstimateDao().setMeasureActId(
                    jobItemMeasure.actId,
                    jobItemMeasure.estimateId!!
                )

                if (jobItemMeasure.jobItemMeasurePhotos.isNotEmpty()) {
                    saveJobItemMeasurePhotos(jobItemMeasure)
                }
            }
        }
    }

    private suspend fun saveJobItemMeasurePhotos(
        jobItemMeasure: JobItemMeasureDTO
    ) {
        for (jobItemMeasurePhoto in jobItemMeasure.jobItemMeasurePhotos) {
            if (!appDb.getJobItemMeasurePhotoDao()
                    .checkIfJobItemMeasurePhotoExists(
                        jobItemMeasurePhoto.filename!!
                    )
            ) jobItemMeasurePhoto.setPhotoPath(
                Environment.getExternalStorageDirectory()
                    .toString() + File.separator +
                    PhotoUtil.FOLDER + File.separator + jobItemMeasurePhoto.filename
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

            if (!PhotoUtil.photoExist(jobItemMeasurePhoto.filename))
                getPhotoForJobItemMeasure(jobItemMeasurePhoto.filename)

            jobItemMeasurePhoto.setPhotoPath(
                Environment.getExternalStorageDirectory()
                    .toString() + File.separator +
                    PhotoUtil.FOLDER + File.separator + jobItemMeasurePhoto.filename
            )

            appDb.getJobItemMeasurePhotoDao()
                .insertJobItemMeasurePhoto(
                    jobItemMeasurePhoto
                )
        }
    }

    private suspend fun getPhotoForJobItemMeasure(filename: String) {

        val photoMeasure = apiRequest { api.getPhotoMeasure(filename) }
        postValue(photoMeasure.photo, filename)
    }

    private suspend fun getPhotoForJobItemEstimate(filename: String) {
        val photoEstimate = apiRequest { api.getPhotoEstimate(filename) }
        postValue(photoEstimate.photo, filename)
    }

    private fun sendMSg(uploadResponse: String?) {
        val response: UploadImageResponse? = null
        if (uploadResponse.isNullOrEmpty())
            jobDataController?.setMsg(response!!.errorMessage)
    }

    private fun saveWorkflowJob(workflowJob: WorkflowJobDTO?) {
        try {
            val job = setWorkflowJobBigEndianGuids(workflowJob!!)
            insertOrUpdateWorkflowJobInSQLite(job)
        } catch (ex: NullPointerException) {
            Timber.e(ex, "Non-nullable WorkFlow Job is null.")
        }
    }

    private fun saveWorkflowJob2(workflowJob: WorkflowJobDTO?) {
        if (workflowJob != null) {
            val job = setWorkflowJobBigEndianGuids(workflowJob)
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
                    if (!appDb.getToDoGroupsDao().checkIfGroupCollectionExist(toDoListGroup.groupId)) {
                        appDb.getToDoGroupsDao().insertToDoGroups(toDoListGroup)
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

            if (!appDb.getEntitiesDao()
                    .checkIfEntitiesExist(DataConversion.bigEndianToString(entity.trackRouteId!!))
            ) {
                appDb.getEntitiesDao().insertEntitie(
                    DataConversion.bigEndianToString(entity.trackRouteId!!),
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
                    appDb.getPrimaryKeyValueDao().insertPrimaryKeyValue(
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

    var contractCount: Int = 0
    var contractMax: Int = 0
    var projectCount: Int = 0
    var projectMax: Int = 0

    suspend fun fetchContracts(userId: String): Boolean {
        contractCount = 0
        contractMax = 0
        projectCount = 0
        projectMax = 0
        return withContext(Dispatchers.Default) {
            postStatus("Fetching Activity Sections")
            val activitySectionsResponse =
                apiRequest { api.activitySectionsRefresh(userId) }
            sectionItems.postValue(activitySectionsResponse.activitySections)

            postStatus("Updating Workflows")
            val workFlowResponse = apiRequest { api.workflowsRefresh(userId) }
            workFlow.postValue(workFlowResponse.workFlows)

            postStatus("Updating Lookups")
            val lookupResponse = apiRequest { api.lookupsRefresh(userId) }
            lookups.postValue(lookupResponse.mobileLookups)

            postStatus("Updating Task List")
            val toDoListGroupsResponse = apiRequest { api.getUserTaskList(userId) }
            toDoListGroups.postValue(toDoListGroupsResponse.toDoListGroups)

            postStatus("Updating Contracts")
            val contractsResponse = apiRequest { api.getAllContractsByUserId(userId) }
            // conTracts.postValue(contractsResponse.contracts)
            saveContracts(contractsResponse.contracts)
            true
        }
    }

    suspend fun getUserTaskList(): LiveData<List<ToDoListEntityDTO>> {

        return withContext(Dispatchers.IO) {
            val userId = appDb.getUserDao().getUserID()
            fetchUserTaskList(userId)
            appDb.getEntitiesDao().getAllEntities()
        }
    }

    suspend fun getAllEntities(): Int {
        return withContext(Dispatchers.IO) {
            appDb.getEntitiesDao().getAllEntities()
            7
        }
    }

    suspend fun fetchUserTaskList(userId: String): Int {
        val toDoListGroupsResponse = apiRequest { api.getUserTaskList(userId) }
        toDoListGroups.postValue(toDoListGroupsResponse.toDoListGroups)

        return 5
    }

    suspend fun refreshActivitySections(userId: String): Int {
        val activitySectionsResponse =
            apiRequest { api.activitySectionsRefresh(userId) }
        sectionItems.postValue(activitySectionsResponse.activitySections)
        return 1
    }

    suspend fun refreshWorkflows(userId: String): Int {
        val workFlowResponse = apiRequest { api.workflowsRefresh(userId) }
        workFlow.postValue(workFlowResponse.workFlows)
        return 2
    }

    suspend fun refreshLookups(userId: String): Int {
        val lookupResponse = apiRequest { api.lookupsRefresh(userId) }
        lookups.postValue(lookupResponse.mobileLookups)
        return 3
    }

    suspend fun getAllContractsByUserId(userId: String): Int {
        val contractsResponse = apiRequest { api.getAllContractsByUserId(userId) }
        conTracts.postValue(contractsResponse.contracts)
        return 4
    }

    suspend fun fetchAllData(userId: String): Boolean {
        // Redo as async calls in parallel
        return withContext(Dispatchers.IO) {

            if (!entitiesFetched) {
                postStatus("Fetching Entities")
                getAllEntities()
                entitiesFetched = true
            }
            postStatus("Refreshing Contracts")
            getAllContractsByUserId(userId)

            postStatus("Refreshing Activity Sessions")
            refreshActivitySections(userId)

            postStatus("Refreshing Workflows")
            refreshWorkflows(userId)

            postStatus("Refreshing Lookups")
            refreshLookups(userId)

            postStatus("Fetching Task List")
            fetchUserTaskList(userId)

            true
        }
    }

    private fun postStatus(message: String) {
        val status = XIStatus(message)
        databaseStatus.postValue(status)
    }

    private fun saveLookups(lookups: ArrayList<LookupDTO>?) {
        Coroutines.io {
            lookups?.forEach { lookup ->
                lookup.let {
                    if (!appDb.getLookupDao().checkIfLookupExist(it.lookupName))
                        appDb.getLookupDao().insertLookup(it)

                    if (!lookup.lookupOptions.isNullOrEmpty()) {
                        lookup.lookupOptions.forEach { lookupOption ->
                            if (!appDb.getLookupOptionDao().checkLookupOptionExists(
                                    lookupOption.valueMember,
                                    lookup.lookupName
                                )
                            )

                                appDb.getLookupOptionDao().insertLookupOption(
                                    lookupOption.valueMember, lookupOption.displayMember,
                                    lookupOption.contextMember, lookup.lookupName
                                )
                        }
                    }
                }
            }
        }
    }

    fun deleteAllData(): Void? {

        appDb.clearAllTables()
        entitiesFetched = false
        return null
    }

    suspend fun getJobApproveMeasureForActivityId(activityId: Int): LiveData<List<JobItemMeasureDTO>> {
        return withContext(Dispatchers.IO) {
            appDb.getJobItemMeasureDao().getJobApproveMeasureForActivityId(activityId)
        }
    }

    private fun insertOrUpdateWorkflowJobInSQLite(job: WorkflowJobDTO?) {
        job?.let {
            Coroutines.io {
                updateWorkflowJobValuesAndInsertWhenNeeded(it)
            }
        }
    }

    private suspend fun updateWorkflowJobValuesAndInsertWhenNeeded(job: WorkflowJobDTO) {
        withContext(Dispatchers.Default) {
            appDb.getJobDao().updateJob(job.trackRouteId, job.actId, job.jiNo, job.jobId)

            if (!job.workflowItemEstimates.isNullOrEmpty()) {
                for (jobItemEstimate in job.workflowItemEstimates) {
                    appDb.getJobItemEstimateDao().updateExistingJobItemEstimateWorkflow(
                        jobItemEstimate.trackRouteId,
                        jobItemEstimate.actId,
                        jobItemEstimate.estimateId
                    )

                    if (jobItemEstimate.workflowEstimateWorks.isNotEmpty()) {
                        updateWorkflowEstimateWorks(jobItemEstimate)
                    }
                }

                if (!job.workflowItemMeasures.isNullOrEmpty()) {
                    updateWorkflowItemMeasures(job.workflowItemMeasures)
                }
            }

            //  Place the Job Section, UPDATE OR CREATE
            if (!job.workflowJobSections.isNullOrEmpty()) {
                saveJobSectionsForWorkflow(job.workflowJobSections)
            }
        }
    }

    private fun updateWorkflowItemMeasures(
        workflowItemMeasures: java.util.ArrayList<WorkflowItemMeasureDTO>
    ) {
        for (jobItemMeasure in workflowItemMeasures) {
            appDb.getJobItemMeasureDao().updateWorkflowJobItemMeasure(
                jobItemMeasure.itemMeasureId,
                jobItemMeasure.trackRouteId,
                jobItemMeasure.actId,
                jobItemMeasure.measureGroupId
            )
        }
    }

    private fun updateWorkflowEstimateWorks(jobItemEstimate: WorkflowItemEstimateDTO) {
        for (jobEstimateWorks in jobItemEstimate.workflowEstimateWorks) {
            if (!appDb.getEstimateWorkDao().checkIfJobEstimateWorksExist(jobEstimateWorks.worksId)) {
                // This part should be unreachable
            } else {
                appDb.getEstimateWorkDao().updateJobEstimateWorksWorkflow(
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

    private suspend fun saveJobSectionsForWorkflow(
        workflowJobSections: java.util.ArrayList<JobSectionDTO>
    ) {
        for (jobSection in workflowJobSections) {
            if (!appDb.getJobSectionDao().checkIfJobSectionExist(jobSection.jobSectionId))
                appDb.getJobSectionDao().insertJobSection(jobSection) else
                appDb.getJobSectionDao().updateExistingJobSectionWorkflow(
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

    private fun JobItemMeasureDTO.setJobNo(jiNo: String?) {
        this.jimNo = jiNo
    }

    private fun postValue(photo: String?, fileName: String) {
        saveEstimatePhoto(photo, fileName)
    }

    private fun saveEstimatePhoto(estimatePhoto: String?, fileName: String) {
        Coroutines.io {
            if (estimatePhoto != null && fileName.isNotBlank()) {
                PhotoUtil.createPhotoFolder(estimatePhoto, fileName)
            } else {
                PhotoUtil.createPhotoFolder()
            }
        }
    }

    suspend fun getUpdatedJob(jobId: String): JobDTO {
        return withContext(Dispatchers.IO) {
            appDb.getJobDao().getJobForJobId(jobId)
        }
    }

    private fun setWorkflowJobBigEndianGuids(job: WorkflowJobDTO): WorkflowJobDTO? {

        job.jobId = DataConversion.toBigEndian(job.jobId)
        job.trackRouteId = DataConversion.toBigEndian(job.trackRouteId)

        job.workflowItemEstimates?.forEach { jie ->

            jie.estimateId = DataConversion.toBigEndian(jie.estimateId)!!
            jie.trackRouteId = DataConversion.toBigEndian(jie.trackRouteId)!!
            //  Let's go through the WorkFlowEstimateWorks
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

    private fun JobItemMeasurePhotoDTO.setPhotoPath(photoPath: String) {
        this.photoPath = photoPath
    }

    private fun JobItemEstimatesPhotoDTO.setIsPhotoStart(photoStart: Boolean) {
        this.is_PhotoStart = photoStart
    }

    private fun JobItemEstimatesPhotoDTO.setPhotoPath(photoPath: String) {
        this.photoPath = photoPath
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

    private fun JobItemMeasureDTO.setQty(qty: Double) {
        this.qty = qty
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

    suspend fun getServiceHealth(userId: String): Boolean {

        val healthCheck = apiRequest { api.healthCheck(userId) }
        return healthCheck.errorMessage.isNullOrBlank() || healthCheck.isAlive == 1
    }

    suspend fun getProjects(): LiveData<List<ProjectDTO>> {
        return withContext(Dispatchers.IO) {
            appDb.getProjectDao().getAllProjects()
        }
    }

    companion object {
        val TAG: String = OfflineDataRepository::class.java.simpleName
    }
}

private fun JobItemMeasureDTO.setDeleted(i: Int) {
    this.deleted = i
}
