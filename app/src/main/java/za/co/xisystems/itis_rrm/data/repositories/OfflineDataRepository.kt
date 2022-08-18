/**
 * Updated by Shaun McDonald on 2021/05/19
 * Last modified on 2022/05/18, 16:50
 * by Francis Mahlava
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 **/

package za.co.xisystems.itis_rrm.data.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.distinctUntilChanged
import androidx.room.Transaction
import kotlinx.coroutines.withContext
import timber.log.Timber
import za.co.xisystems.itis_rrm.custom.errors.XIErrorHandler
import za.co.xisystems.itis_rrm.custom.events.XIEvent
import za.co.xisystems.itis_rrm.custom.results.XIResult
import za.co.xisystems.itis_rrm.data._commons.utils.SqlLitUtils
import za.co.xisystems.itis_rrm.data.localDB.AppDatabase
import za.co.xisystems.itis_rrm.data.localDB.JobDataController
import za.co.xisystems.itis_rrm.data.localDB.entities.*
import za.co.xisystems.itis_rrm.data.network.BaseConnectionApi
import za.co.xisystems.itis_rrm.data.network.SafeApiRequest
import za.co.xisystems.itis_rrm.data.network.responses.HealthCheckResponse
import za.co.xisystems.itis_rrm.data.network.responses.UploadImageResponse
import za.co.xisystems.itis_rrm.data.network.responses.VersionCheckResponse
import za.co.xisystems.itis_rrm.forge.DefaultDispatcherProvider
import za.co.xisystems.itis_rrm.forge.DispatcherProvider
import za.co.xisystems.itis_rrm.ui.mainview.activities.main.MainActivity.Companion.PROJECT_ENGINEER_ROLE_IDENTIFIER
import za.co.xisystems.itis_rrm.utils.*
import java.io.File
import java.util.regex.Pattern

private val jobDataController: JobDataController? = null

/**
 * OfflineDataRepository - fetching from mobile Services
 */
class OfflineDataRepository(
    private val api: BaseConnectionApi,
    private val appDb: AppDatabase,
    private val photoUtil: PhotoUtil,
    private val dispatchers: DispatcherProvider = DefaultDispatcherProvider()
) : SafeApiRequest() {

    private var entitiesFetched = false
    private val conTracts = MutableLiveData<ArrayList<ContractDTO>>()
    private val sectionItems = MutableLiveData<ArrayList<String>>()
    private val job = MutableLiveData<JobDTO>()
    private val workFlow = MutableLiveData<WorkFlowsDTO>()
    private val category = MutableLiveData<ArrayList<JobCategoryDTO>>()
    private val positions = MutableLiveData<ArrayList<JobPositionDTO>>()
    private val directions = MutableLiveData<ArrayList<JobDirectionDTO>>()
    private val lookups = MutableLiveData<ArrayList<LookupDTO>>()
    private val toDoListGroups = MutableLiveData<ArrayList<ToDoGroupsDTO>>()
    private val workflows = MutableLiveData<ArrayList<ToDoGroupsDTO>>()
    private val workflowJ = MutableLiveData<WorkflowJobDTO>()
    private val workflowJ2 = MutableLiveData<WorkflowJobDTO>()
    private val photoUpload = MutableLiveData<String>()
    val bigSyncDone: MutableLiveData<Boolean> = MutableLiveData()
    var databaseStatus: MutableLiveData<XIEvent<XIResult<Boolean>>> = MutableLiveData()
    private var tasksMax: Int = 0
    private var tasksCount: Int = 0
    private var contractCount: Int = 0
    private var contractMax: Int = 0
    private var projectCount: Int = 0
    private var projectMax: Int = 0
    private var contractVoCount: Int = 0
    private var contractVosMax: Int = 0
    private var projectVoCount: Int = 0
    private var projectVosMax: Int = 0
    private var newContracts: Boolean = false
    private var newProjects: Boolean = false
    private var newContractVos: Boolean = false
    private var newProjectVos: Boolean = false

    init {
        conTracts.observeForever {
            Coroutines.default {
                saveContracts(it)
            }
        }

        sectionItems.observeForever {
            Coroutines.default {
                saveSectionsItems(it)
            }
        }

        workFlow.observeForever {
            Coroutines.default {
                saveWorkFlowsInfo(it)
            }
        }

        lookups.observeForever {
            Coroutines.default {
                saveLookups(it)
            }
        }

        toDoListGroups.observeForever {
            Coroutines.default {
                saveUserTaskList(it)
            }
        }

        workflows.observeForever {
            saveTaskList(it)
        }

        job.observeForever {
            Coroutines.io {
                saveJob(it)
            }
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

        workFlow.observeForever {
            Coroutines.default {
                saveWorkFlowsInfo(it)
            }
        }

        category.observeForever {
            Coroutines.default {
                saveCategoryItems(it)
            }
        }

        positions.observeForever {
            Coroutines.default {
                savePositionItems(it)
            }
        }

        directions.observeForever {
            Coroutines.default {
                saveDirectionItems(it)
            }
        }

    }

    companion object {
        val TAG: String = OfflineDataRepository::class.java.simpleName
    }

    suspend fun bigSyncCheck() = withContext(dispatchers.io()) {
        return@withContext appDb.getContractDao().countContracts() >= 1
    }

    suspend fun getContracts(): LiveData<List<ContractDTO>> {
        return withContext(dispatchers.io()) {
            appDb.getContractDao().getAllContracts()
        }
    }

    suspend fun getRoles(): LiveData<List<UserRoleDTO>> {
        return withContext(dispatchers.io()) {
            appDb.getUserRoleDao().getRoles()
        }
    }

    suspend fun getRolesList(): List<UserRoleDTO> {
        return withContext(dispatchers.io()) {
            appDb.getUserRoleDao().getRolesList()
        }
    }

    suspend fun getSectionItems(): LiveData<List<SectionItemDTO>> {
        return withContext(dispatchers.io()) {
            appDb.getSectionItemDao().getSectionItems().distinctUntilChanged()
        }
    }

    suspend fun getUser(): LiveData<UserDTO> {
        return withContext(dispatchers.io()) {
            appDb.getUserDao().getUser()
        }
    }


    suspend fun getJobsFromActId(activityId: Int): List<JobDTO> {
        return withContext(dispatchers.io()) {
            appDb.getJobDao().getJobsFromActId(activityId)
        }
    }

    suspend fun getJobsForActId(vararg activityIds: Int): LiveData<List<JobDTO>> {
        return withContext(dispatchers.io()) {
            appDb.getJobDao().getJobsForActivityId(*activityIds).distinctUntilChanged()
        }
    }

    suspend fun getJobsForActivityIds1(activityId1: Int, activityId2: Int): LiveData<List<JobDTO>> {
        return withContext(dispatchers.io()) {
            appDb.getJobDao().getJobsForActivityIds1(activityId1, activityId2).distinctUntilChanged()
        }
    }

    suspend fun getJobMeasureForActivityId(
        activityId: Int,
        activityId2: Int,
        activityId3: Int
    ): LiveData<List<JobItemEstimateDTO>> = withContext(dispatchers.io()) {
        return@withContext appDb.getJobItemEstimateDao()
            .getJobMeasureForActivityId(activityId, activityId2, activityId3).distinctUntilChanged()
    }

    suspend fun getJobMeasureItemsPhotoPath(itemMeasureId: String): List<String> {
        return withContext(dispatchers.io()) {
            appDb.getJobItemMeasurePhotoDao().getJobMeasureItemPhotoPaths(itemMeasureId)
        }
    }

    suspend fun getProjectSection(sectionId: String?): LiveData<ProjectSectionDTO> {
        return withContext(dispatchers.io()) {
            appDb.getProjectSectionDao().getLiveSection(sectionId!!)
        }
    }

    suspend fun getProjectSectionIdForJobId(jobId: String?): String {
        return withContext(dispatchers.io()) {
            appDb.getJobSectionDao().getProjectSectionId(jobId!!)
        }
    }

    suspend fun getAppVersionCheck(versionNmb: String): VersionCheckResponse {
        return withContext(dispatchers.io()) {
            val appVersionCheck = apiRequest { api.versionCheck(versionNmb) }
            appVersionCheck
        }
    }

    suspend fun getProjectSectionForId(sectionId: String?): ProjectSectionDTO {
        return withContext(dispatchers.io()) {
            appDb.getProjectSectionDao().getSection(sectionId!!)
        }
    }

    suspend fun getProjectDescription(projectId: String): String {
        return withContext(dispatchers.io()) {
            appDb.getProjectDao().getProjectDescription(projectId)
        }
    }


    suspend fun getProjectItemDescription(projectItemId: String): String {
        return withContext(dispatchers.io()) {
            appDb.getProjectItemDao().getProjectItemDescription(projectItemId)
        }
    }

    suspend fun getUOMForProjectItemId(projectItemId: String): String = withContext(dispatchers.io()) {
        return@withContext appDb.getProjectItemDao().getUOMForProjectItemId(projectItemId) ?: ""
    }

    suspend fun getJobMeasureItemsForJobId(
        jobID: String?,
        actId: Int
    ): LiveData<List<JobItemMeasureDTO>> {
        return withContext(dispatchers.io()) {
            appDb.getJobItemMeasureDao().getJobItemMeasuresByJobIdAndActId(jobID!!, actId)
        }
    }

    suspend fun getItemDescription(jobId: String): String {
        return withContext(dispatchers.io()) {
            appDb.getJobDao().getItemDescription(jobId)
        }
    }

    suspend fun getItemJobNo(jobId: String): String {
        return withContext(dispatchers.io()) {
            appDb.getJobDao().getItemJobNo(jobId)
        }
    }

    suspend fun getRouteForProjectSectionId(sectionId: String?): String {
        return withContext(dispatchers.io()) {
            appDb.getProjectSectionDao().getRouteForProjectSectionId(sectionId!!)
        }
    }

    suspend fun getSectionForProjectSectionId(sectionId: String?): String {
        return withContext(dispatchers.io()) {
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

    private fun saveCategoryItems(categories: ArrayList<JobCategoryDTO>) {
        try {
            categories.forEach { category ->
                appDb.getJobCategoryDao().insertJobCategory(category)
                // postEvent(XIResult.ProgressUpdate("positions", sectionCount.toFloat() / sectionSize.toFloat()))
            }
            postEvent(XIResult.ProgressUpdate("categories", -1.0f))
        } catch (throwable: Throwable) {
            Timber.e(throwable, "Exception caught saving categories items: ${throwable.message}")
        }
    }

    private fun saveDirectionItems(directions: ArrayList<JobDirectionDTO>) {
        try {
            directions.forEach { direction ->
                appDb.getJobDirectionDao().insertJobDirection(direction)
                // postEvent(XIResult.ProgressUpdate("positions", sectionCount.toFloat() / sectionSize.toFloat()))
            }
            postEvent(XIResult.ProgressUpdate("directions", -1.0f))
        } catch (throwable: Throwable) {
            Timber.e(throwable, "Exception caught saving directions items: ${throwable.message}")
        }
    }

    private fun savePositionItems(positions: ArrayList<JobPositionDTO>) {
        try {
            positions?.forEach { position ->
                appDb.getJobPositionDao().insertJobPosition(position)
                // postEvent(XIResult.ProgressUpdate("positions", sectionCount.toFloat() / sectionSize.toFloat()))
            }
            postEvent(XIResult.ProgressUpdate("positions", -1.0f))
        } catch (throwable: Throwable) {
            Timber.e(throwable, "Exception caught saving positions items: ${throwable.message}")
        }
    }


    private fun saveSectionsItems(sections: ArrayList<String>?) {
        var sectionSize: Int = sections?.size ?: 0
        var sectionCount = 0

        try {
            sections?.forEach { section ->
                //  Let's get the String
                val pattern = Pattern.compile("(.*?):")
                val matcher = pattern.matcher(section)

                val sectionItemId = SqlLitUtils.generateUuid()
                if (matcher.find() && section.isNotBlank()) {
                    val itemCode = matcher.group(1)?.replace("\\s+".toRegex(), "")
                    itemCode?.let {
//                  "ItemCode": "M020.01(a)"
                        if (!appDb.getSectionItemDao().checkIfSectionItemsExist(it)) {
                            appDb.getSectionItemDao().insertSectionItem(
                                description = section,
                                itemCode = it,
                                sectionItemId = sectionItemId
                            )
                            sectionCount++
                        } else {
                            sectionSize--
                        }
                    }
                }
                postEvent(XIResult.ProgressUpdate("sections", sectionCount.toFloat() / sectionSize.toFloat()))
            }
            postEvent(XIResult.ProgressUpdate("sections", -1.0f))
        } catch (throwable: Throwable) {
            Timber.e(throwable, "Exception caught saving section items: ${throwable.message}")
        }
    }

    @Transaction
    private suspend fun saveContracts(contracts: List<ContractDTO>) {
        newContracts = false
        newProjects = false
        contractCount = 0
        contractMax = 0
        projectCount = 0
        projectMax = 0

        createWorkflowSteps()
        if (contracts.isEmpty()) {
            Timber.e("Error saving contracts: NO Contracts Available")
        } else {
            try {
                val validContracts = contracts.filter { contract ->
                    contract.projects.isNotEmpty() && contract.contractId.isNotBlank()
                }
                    .distinctBy { contract -> contract.contractId }
                contractMax += validContracts.count()
                validContracts.forEach { contract ->
                    if (!appDb.getContractDao().checkIfContractExists(contract.contractId)) {
                        appDb.getContractDao().insertContract(contract)
                        contractCount++
                        newContracts = true

                        val validProjects =
                            contract.projects.filter { project ->
                                project.projectId.isNotBlank()
                            }.distinctBy { project -> project.projectId }

                        if (!validProjects.isNullOrEmpty()) {
                            saveProjects(validProjects, contract)
                        }

                        val validContractVos =
                            contract.contractVos.filter { contractVos ->
                                contractVos.contractVoId.isNotBlank()
                            }.distinctBy { contractVos -> contractVos.contractVoId }

                        if (!validContractVos.isNullOrEmpty()) {
                            saveContractVos(validContractVos, contract)
                        }

                    } else {
                        contractMax--
                    }

                    Timber.d("cr**: $contractCount / $contractMax contracts")
                    Timber.d("cr**: $projectCount / $projectMax projects")
                }
            } catch (ex: Exception) {
                Timber.e(ex, "Error saving contracts: ${ex.message ?: XIErrorHandler.UNKNOWN_ERROR}")
            }
        }

    }


    private fun createWorkflowSteps() {
        val actId = ActivityIdConstants.JOB_APPROVED
        val workState = arrayOf("TA", "START", "MIDDLE", "END", "RTA")
        val workStateDescriptions = arrayOf(
            "Traffic Accommodation",
            "Work Start",
            "Work Middle",
            "Work Completed",
            "Removal of Traffic Accommodation"
        )
        val jobTypes = arrayOf(
            "Point",
            "Line"
        )
        jobTypes.forEach { structType ->
            if (!appDb.getJobTypeDao().checkifExists(structType)) {
                appDb.getJobTypeDao().insertType(structType)
            }
        }
        for (step_code in workState.iterator()) {
            if (!appDb.getWorkStepDao().checkWorkFlowStepExistsWorkCode(step_code)) {
                appDb.getWorkStepDao().insertStepsCode(step_code, actId)
            }

            for (description in workStateDescriptions.iterator()) {
                if (!appDb.getWorkStepDao().checkWorkFlowStepExistsDesc(description)) {
                    appDb.getWorkStepDao().updateStepsDesc(description, step_code)
                }
            }
        }
    }

    @Transaction
    private suspend fun saveContractVos(validContractVos: List<ContractVoDTO>, contract: ContractDTO) {
        withContext(dispatchers.io()) {
            contractVosMax += validContractVos.size
            validContractVos.forEach { contractVo ->
                try {
                    if (appDb.getContractVoDao().checkIfContractVoExists(contractVo.contractVoId)) {
                        Timber.i(
                            "Contract: ${contract.shortDescr} (${contract.contractId}) "
                                .plus("ContractVoId: ${contractVo.voNumber} (${contractVo.contractVoId}) -> Duplicated")
                        )
                        contractVosMax--
                    } else {
                        appDb.getContractVoDao().insertContractVo(contractVo)
                        contractVoCount++
                        newContractVos = true
                    }

                    // Timber.d("pr**: $contractCount / $contractMax contracts")
                    Timber.d("pr**: $contractVoCount / $contractVosMax contractVos")

                    postEvent(
                        XIResult.ProgressUpdate(
                            "contractVos",
                            (contractVoCount.toFloat() * contractCount.toFloat()) /
                                    (contractVosMax.toFloat() * contractMax.toFloat())
                        )
                    )
                } catch (ex: Exception) {
                    Timber.e(
                        ex,
                        ("Contract: ${contract.shortDescr} (${contract.contractId}) ContractVoId: ${contractVo.voNumber}")
                            .plus(" ${contractVo.contractVoId}) -> ${ex.message}")

                    )
                }
            }
            if (contractCount == contractMax && contractVoCount == contractVosMax) {
                postEvent(XIResult.ProgressUpdate("ContractVos", -1.0f))
            }
        }
    }


    @Transaction
    private suspend fun saveProjects(
        validProjects: List<ProjectDTO>,
        contract: ContractDTO
    ) {
        withContext(dispatchers.io()) {
            projectMax += validProjects.size
            validProjects.forEach { project ->
                try {
                    if (appDb.getProjectDao().checkProjectExists(project.projectId)) {
                        Timber.i(
                            "Contract: ${contract.shortDescr} (${contract.contractId}) "
                                .plus("ProjectId: ${project.descr} (${project.projectId}) -> Duplicated")
                        )
                        projectMax--
                    } else {

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
                        projectCount++
                        newProjects = true
                    }

                    updateProjectItems(project.items, project)

                    updateProjectSections(project.projectSections, project)

                    updateVOItems(project.voItems, project)
                    //updateProjectVoItems(contractVo.projectVos!!, contractVo)

                    Timber.d("pr**: $contractCount / $contractMax contracts")
                    Timber.d("pr**: $projectCount / $projectMax projects")

                    postEvent(
                        XIResult.ProgressUpdate(
                            "projects",
                            (projectCount.toFloat() * contractCount.toFloat()) /
                                    (projectMax.toFloat() * contractMax.toFloat())
                        )
                    )
                } catch (ex: Exception) {
                    Timber.e(
                        ex,
                        ("Contract: ${contract.shortDescr} (${contract.contractId}) ProjectId: ${project.descr}")
                            .plus(" ${project.projectId}) -> ${ex.message}")

                    )
                }
            }
            if (contractCount == contractMax && projectCount == projectMax) {
                postEvent(XIResult.ProgressUpdate("projects", -1.0f))
            }
        }
    }


//    @Transaction
//    private fun updateProjectVoItems(projectVos: ArrayList<ProjectVoDTO>, contractVo: ContractVoDTO) {
//        projectVos.forEach { projectVo ->
//            if (!appDb.getProjectVoDao()
//                    .checkItemExistsProjectVo(projectVo.projectVoId)
//            ) {
//                try {
//                    if (projectVo.itemCode.isNullOrEmpty()){
//                        projectVo.setVoApprovalNumber(contractVo.nRAApprovalNumber)
//                        projectVo.setVoNumber(contractVo.voNumber)
//                        appDb.getProjectVoDao().insertProjectVoItem(projectVo)
//
//                    }else{
//                        val pattern = Pattern.compile("(.*?)\\.")
//                        val matcher = pattern.matcher(projectVo.itemCode!!)
//                        if (matcher.find()) {
//                            val itemCode = "${matcher.group(1)}0"
//                            //  Let's Get the ID Back on Match
//                            val sectionItemId = appDb.getSectionItemDao()
//                                .getSectionItemId(
//                                    itemCode.replace(
//                                        "\\s+".toRegex(),
//                                        ""
//                                    )
//                                )
//                            projectVo.setVoApprovalNumber(contractVo.nRAApprovalNumber)
//                            projectVo.setVoNumber(contractVo.voNumber)
//                            projectVo.setSectionItemId(sectionItemId)
//                            projectVo.itemId = projectVo.itemCode
//                            projectVo.workflowId = 4
//                            appDb.getProjectVoDao().insertProjectVoItem(projectVo)
//
//                        }
//
//                    }
//
//
//                } catch (ex: Exception) {
//                    Timber.e(
//                        ex,
//                        "ProjectVo ${projectVo.projectVoId} -> ${ex.message}"
//                    )
//                }
//            }
//        }
//    }


    @Transaction
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
                            parentDescr = item.parentDescr,
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

    @Transaction
    private fun updateProjectSections(
        projectSections: ArrayList<ProjectSectionDTO>,
        project: ProjectDTO
    ) {
        projectSections.forEach { section ->
            if (!appDb.getProjectSectionDao()
                    .checkSectionExists(section.sectionId)
            ) {
                try {
                    appDb.getProjectSectionDao().insertSection(
                        section.sectionId,
                        section.route!!,
                        section.section!!,
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
    }

    private fun updateVOItems(
        voItems: ArrayList<VoItemDTO>?,
        project: ProjectDTO
    ) {
        voItems?.forEach { voItem ->
            if (!appDb.getVoItemDao().checkIfExistsProjectVoItem(voItem.projectVoId)) {
                try {
                    if (voItem.itemCode.isNullOrEmpty()) {
//                        voItem.setVoApprovalNumber(project.nRAApprovalNumber)
//                        voItem.setVoNumber(contractVo.voNumber)
                        appDb.getVoItemDao().insertProjectVoItem(voItem)
                    } else {
                        val pattern = Pattern.compile("(.*?)\\.")
                        val matcher = pattern.matcher(voItem.itemCode)
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
//                            projectVo.setVoApprovalNumber(contractVo.nRAApprovalNumber)
//                            projectVo.setVoNumber(contractVo.voNumber)
                            voItem.setSectionItemId(sectionItemId)
                            voItem.workflowId = 4
                            appDb.getVoItemDao().insertProjectVoItem(voItem)

                        }

                    }


                } catch (ex: Exception) {
                    Timber.e(
                        ex,
                        "VoItemProjectVoId: ${voItem.projectVoId} -> ${ex.message}"
                    )
                }
            }
        }
    }

    private fun saveWorkFlowsInfo(workFlows: WorkFlowsDTO) {
        Coroutines.io {

            appDb.getWorkflowsDao().insertWorkFlows(workFlows)

            workFlows.workflows?.forEach { workFlow ->
                if (!appDb.getWorkFlowDao().checkWorkFlowExistsWorkflowID(workFlow.workflowId)) {
                    appDb.getWorkFlowDao().insertWorkFlow(workFlow)
                }

                saveWorkflowRoutes(workFlow)
            }

            workFlows.activities.forEach { activity ->
                appDb.getActivityDao().insertActivitys(activity)
            }

            workFlows.infoClasses?.forEach { infoClass ->
                appDb.getInfoClassDao().insertInfoClasses(infoClass)
            }
        }
    }

    private fun saveWorkflowRoutes(workFlow: WorkFlowDTO) {
        for (workFlowRoute in workFlow.workFlowRoute) {
            if (!appDb.getWorkFlowRouteDao()
                    .checkWorkFlowRouteExists(workFlowRoute.routeId)
            ) {
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
    }

    @Transaction
    private suspend fun saveJob(jobDTO: JobDTO?) {
//        if(appDb.getJobDao().checkIfJobExist())


        jobDTO?.let { job ->
            val jobID = DataConversion.toBigEndian(job.jobId).toString()
            if (!appDb.getJobDao().checkIfJobExist(jobID)) {
                val newcontractID = appDb.getProjectDao().getContractIdForProjectId(DataConversion.toBigEndian(job.projectId!!).toString())

                job.run {
                    setJobId(DataConversion.toBigEndian(jobId))
                    setProjectId(DataConversion.toBigEndian(projectId))
                    if (newcontractID != null) {
                        setContractId(newcontractID)//DataConversion.toBigEndian(newcontractID))
                    }
                    if (contractVoId != null) {
                        setContractVoId(DataConversion.toBigEndian(contractVoId))
                    }
                    if (trackRouteId != null) {
                        setTrackRouteId(DataConversion.toBigEndian(trackRouteId))
                    }
                }
                job.perfitemGroupId = DataConversion.toBigEndian(job.perfitemGroupId)
                job.projectVoId = DataConversion.toBigEndian(job.projectVoId)
                appDb.getJobDao().insertOrUpdateJob(job)
            }

            saveJobSections(job)

            saveJobItemEstimates(job)

            if (!job.jobItemMeasures.isNullOrEmpty()) {
                saveJobItemMeasuresForJob(job)
            }
        }
    }

    private suspend fun saveJobItemMeasuresForJob(
        job: JobDTO,
    ) {
        job.jobItemMeasures.forEach { jobItemMeasure ->
            if (!appDb.getJobItemMeasureDao()
                    .checkIfJobItemMeasureExists(jobItemMeasure.itemMeasureId)
            ) {
                DataConversion.toBigEndian(
                    jobItemMeasure.itemMeasureId
                )?.let {
                    jobItemMeasure.setItemMeasureId(
                        it
                    )
                }
                jobItemMeasure.setJobId(
                    DataConversion.toBigEndian(jobItemMeasure.jobId)!!
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
                jobItemMeasure.setEstimateId(DataConversion.toBigEndian(jobItemMeasure.estimateId))
                jobItemMeasure.setProjectVoId(DataConversion.toBigEndian(jobItemMeasure.projectVoId))
                jobItemMeasure.setTrackRouteId(DataConversion.toBigEndian(jobItemMeasure.trackRouteId))
                jobItemMeasure.setJobNo(job.jiNo)
                jobItemMeasure.setQty(jobItemMeasure.qty)
                jobItemMeasure.setDeleted(0)
                if (!appDb.getJobItemMeasureDao()
                        .checkIfJobItemMeasureExists(jobItemMeasure.itemMeasureId)
                ) {
                    appDb.getJobItemMeasureDao().insertJobItemMeasure(jobItemMeasure)
                }

                appDb.getJobDao().setMeasureActId(jobItemMeasure.actId, job.jobId)
                appDb.getJobItemEstimateDao()
                    .setMeasureActId(jobItemMeasure.actId, jobItemMeasure.estimateId!!)

                if (jobItemMeasure.jobItemMeasurePhotos.isNotEmpty()) {
                    saveJobItemMeasurePhotos(jobItemMeasure)
                }

                appDb.getJobItemMeasureDao().undeleteMeasurement(jobItemMeasure.itemMeasureId)
            }
        }
    }

    private fun saveJobSections(job: JobDTO) {
        job.jobSections.forEach { jobSection ->

            if (!appDb.getJobSectionDao().checkIfJobSectionExist(jobSection.jobSectionId)) {
                jobSection.setJobSectionId(DataConversion.toBigEndian(jobSection.jobSectionId))
            }
            jobSection.setProjectSectionId(DataConversion.toBigEndian(jobSection.projectSectionId))
            jobSection.setJobId(DataConversion.toBigEndian(jobSection.jobId))
            appDb.getJobSectionDao().insertJobSection(
                jobSection
            )
        }
    }

    private suspend fun saveJobItemEstimates(
        job: JobDTO,
    ) {
        job.jobItemEstimates.forEach { jobItemEstimate ->
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
                if (jobItemEstimate.trackRouteId != null) {
                    jobItemEstimate.setTrackRouteId(
                        DataConversion.toBigEndian(
                            jobItemEstimate.trackRouteId
                        )
                    )
                } else {
                    jobItemEstimate.trackRouteId = null
                    jobItemEstimate.setContractVoId(
                        DataConversion.toBigEndian(
                            jobItemEstimate.contractVoId
                        )
                    )
                    jobItemEstimate.setProjectVoId(
                        DataConversion.toBigEndian(
                            jobItemEstimate.projectVoId
                        )
                    )
                }

                appDb.getJobItemEstimateDao().insertJobItemEstimate(jobItemEstimate)
                appDb.getJobDao().setEstimateActId(jobItemEstimate.actId, job.jobId)
                val roleList = getRolesList()

                saveJobItemEstimatePhotos(jobItemEstimate, roleList)

                saveJobItemEstimateWorks(jobItemEstimate, job)

                saveJobItemMeasuresForEstimate(jobItemEstimate.jobItemMeasure, job, roleList)
            }
        }
    }

    private suspend fun saveJobItemEstimatePhotos(
        jobItemEstimate: JobItemEstimateDTO,
        roleList: List<UserRoleDTO>,
    ) {
        jobItemEstimate.jobItemEstimatePhotos.forEach { jobItemEstimatePhoto ->
            if (!appDb.getJobItemEstimatePhotoDao()
                    .checkIfJobItemEstimatePhotoExistsByPhotoId(
                        jobItemEstimatePhoto.photoId
                    )
            ) {
                jobItemEstimatePhoto.setPhotoPath(
                    photoUtil.pictureFolder.toString()
                        .plus(File.separator).plus(jobItemEstimatePhoto.filename)
                )
            }

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

            for (role in roleList) {
                val roleID = role.roleDescription
                // Only enable what is needed for each role description.
                // Users with multiple roles get 'best permissions'
                when {
//                            roleID.equals(PROJECT_USER_ROLE_IDENTIFIER, ignoreCase = true) -> {
//
//
//                            }
//
//                            roleID.equals(PROJECT_SUB_CONTRACTOR_ROLE_IDENTIFIER, ignoreCase = true) -> {
//
//
//                            }
//
//                            roleID.equals(PROJECT_CONTRACTOR_ROLE_IDENTIFIER, ignoreCase = true) -> {
//
//
//                            }
//
//                            roleID.equals(PROJECT_SITE_ENGINEER_ROLE_IDENTIFIER, ignoreCase = true) -> {
//
//
//                            }

                    roleID.equals(PROJECT_ENGINEER_ROLE_IDENTIFIER, ignoreCase = true) -> {
                        if (!photoUtil.photoExist(jobItemEstimatePhoto.filename)) {
                            getPhotoForJobItemEstimate(jobItemEstimatePhoto.filename)
                        }
                    }

                }
            }
        }
    }

    private fun saveJobItemEstimateWorks(
        jobItemEstimate: JobItemEstimateDTO,
        job: JobDTO
    ) {
        for (jobEstimateWorks in jobItemEstimate.jobEstimateWorks) {
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
                .setEstimateWorksActId(jobEstimateWorks.actId, job.jobId)

            if (jobEstimateWorks.jobEstimateWorksPhotos.isNullOrEmpty()) {
                jobEstimateWorks.jobEstimateWorksPhotos = arrayListOf()
            }

            saveJobItemEstimateWorksPhotos(jobEstimateWorks)
        }
    }

    private fun saveJobItemEstimateWorksPhotos(jobEstimateWorks: JobEstimateWorksDTO) {
        for (estimateWorksPhoto in jobEstimateWorks.jobEstimateWorksPhotos) {
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
        job: JobDTO,
        roleList: List<UserRoleDTO>
    ) {
        for (jobItemMeasure in jobItemMeasures) {
            if (!appDb.getJobItemMeasureDao().checkIfJobItemMeasureExists(
                    jobItemMeasure.itemMeasureId
                )
            ) {
                DataConversion.toBigEndian(
                    jobItemMeasure.itemMeasureId
                )?.let {
                    jobItemMeasure.setItemMeasureId(
                        it
                    )
                }
                DataConversion.toBigEndian(
                    jobItemMeasure.jobId
                )?.let {
                    jobItemMeasure.setJobId(
                        it
                    )
                }
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
                jobItemMeasure.setJobNo(job.jiNo)
                if (!appDb.getJobItemMeasureDao().checkIfJobItemMeasureExists(
                        jobItemMeasure.itemMeasureId
                    )
                ) {
                    appDb.getJobItemMeasureDao().insertJobItemMeasure(
                        jobItemMeasure
                    )
                }
                appDb.getJobDao().setMeasureActId(jobItemMeasure.actId, job.jobId)
                appDb.getJobItemEstimateDao().setMeasureActId(
                    jobItemMeasure.actId,
                    jobItemMeasure.estimateId!!
                )

                if (jobItemMeasure.jobItemMeasurePhotos.isNotEmpty()) {
                    for (role in roleList) {
                        val roleID = role.roleDescription
                        // Only enable what is needed for each role description.
                        // Users with multiple roles get 'best permissions'
                        when {
                            roleID.equals(PROJECT_ENGINEER_ROLE_IDENTIFIER, ignoreCase = true) -> {
                                saveJobItemMeasurePhotos(jobItemMeasure)
                            }

                        }
                    }

                }
            }
        }
    }

    private suspend fun saveJobItemMeasurePhotos(
        jobItemMeasure: JobItemMeasureDTO
    ) {
        jobItemMeasure.jobItemMeasurePhotos.forEach { jobItemMeasurePhoto ->
            if (!appDb.getJobItemMeasurePhotoDao()
                    .checkIfJobItemMeasurePhotoExists(
                        jobItemMeasurePhoto.filename!!
                    )
            ) {
                jobItemMeasurePhoto.setPhotoPath(
                    photoUtil.pictureFolder.toString().plus(File.separator).plus(jobItemMeasurePhoto.filename)
                )
            }
            jobItemMeasurePhoto.setPhotoId(
                DataConversion.toBigEndian(
                    jobItemMeasurePhoto.photoId
                )
            )
            jobItemMeasurePhoto.setItemMeasureId(
                jobItemMeasure.itemMeasureId
            )

            jobItemMeasurePhoto.setEstimateId(
                jobItemMeasure.estimateId
            )

            if (!photoUtil.photoExist(jobItemMeasurePhoto.filename)) {
                getPhotoForJobItemMeasure(jobItemMeasurePhoto.filename)
            }

            jobItemMeasurePhoto.setPhotoPath(
                photoUtil.pictureFolder.toString().plus(File.separator)
                    .plus(jobItemMeasurePhoto.filename)
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
        savePhoto(photoEstimate.photo, filename)

    }

    private fun sendMSg(uploadResponse: String?) {
        val response: UploadImageResponse? = null
        if (uploadResponse.isNullOrEmpty()) {
            jobDataController?.setMsg(response!!.errorMessage)
        }
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

        toDoListGroups?.let {
            Coroutines.io {
                saveUserTaskList(it)
            }
        }
    }

    private suspend fun saveUserTaskList(toDoListGroups: ArrayList<ToDoGroupsDTO>?) {
        withContext(dispatchers.io()) {
            try {
                tasksMax = toDoListGroups?.size ?: 0
                tasksCount = 0
                toDoListGroups?.forEach { toDoListGroup ->
                    if (!appDb.getToDoGroupsDao().checkIfGroupCollectionExist(toDoListGroup.groupId)) {
                        appDb.getToDoGroupsDao().insertToDoGroups(toDoListGroup)
                    }

                    val entitiesArrayList = toDoListGroup.toDoListEntities

                    entitiesArrayList.forEach { toDoListEntity ->
                        val jobId = getJobIdFromPrimaryKeyValues(toDoListEntity.primaryKeyValues)
                        jobId?.let { id ->
                            insertEntity(toDoListEntity, id)
                            val newJobId = DataConversion.toLittleEndian(id)
                            newJobId?.let { newId ->
                                fetchJobList(newId)
                            }
                        }
                    }
                    tasksCount++
                    postEvent(XIResult.ProgressUpdate("tasks", tasksCount.toFloat() / tasksMax.toFloat()))
                }
            } catch (throwable: Throwable) {
                val message = "Failed to save task list locally: ${throwable.message ?: XIErrorHandler.UNKNOWN_ERROR}"
                Timber.e(throwable, message)
                val dbError = XIResult.Error(throwable, message, "Contract Synch")
                postEvent(dbError)
            } finally {
                postEvent(XIResult.ProgressUpdate("tasks", -1.0f))
            }
        }
    }

    private fun insertEntity(entity: ToDoListEntityDTO, jobId: String) {
        Coroutines.io {
            if (entity.trackRouteId != null) {
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
                            DataConversion.bigEndianToString(primaryKeyValue.pValue!!),
                            DataConversion.bigEndianToString(entity.trackRouteId!!),
                            entity.activityId
                        )
                    }
                }
            }

        }
    }

    private fun getJobIdFromPrimaryKeyValues(primaryKeyValues: List<PrimaryKeyValueDTO>): String? {
        for (primaryKeyValue in primaryKeyValues) {
            if (primaryKeyValue.primary_key!!.contains("JobId")) {
                return DataConversion.bigEndianToString(primaryKeyValue.pValue!!)
            }
        }
        return null
    }

    private suspend fun fetchJobList(jobId: String) {
        val jobResponse = apiRequest { api.getJobsForApproval(jobId) }
        job.postValue(jobResponse.job)
    }

    suspend fun loadActivitySections(userId: String) {
        postStatus("Fetching Activity Sections")
        val activitySectionsResponse = apiRequest { api.activitySectionsRefresh(userId) }
        val jobCategoryResponse = apiRequest { api.getJobCategories(userId) }
        val jobDirectionResponse = apiRequest { api.getJobDirections(userId) }
        val jobPositionResponse = apiRequest { api.getJobPositions(userId) }
        directions.postValue(jobDirectionResponse.jobDirection)
        positions.postValue(jobPositionResponse.jobPosition)
        category.postValue(jobCategoryResponse.jobCategory)
        saveSectionsItems(activitySectionsResponse.activitySections)
    }

    suspend fun loadWorkflows(userId: String) {
        postStatus("Updating Workflows")
        val workFlowResponse = apiRequest { api.workflowsRefresh(userId) }
        workFlow.postValue(workFlowResponse.workFlows)

    }

    suspend fun loadLookups(userId: String) {
        postStatus("Updating Lookups")
        val lookupResponse = apiRequest { api.lookupsRefresh(userId) }
        lookups.postValue(lookupResponse.mobileLookups)
    }

    suspend fun loadTaskList(userId: String) {
        postStatus("Updating Task List")
        val toDoListGroupsResponse = apiRequest { api.getUserTaskList(userId) }
        saveUserTaskList(toDoListGroupsResponse.toDoListGroups)
    }

    suspend fun loadContracts(userId: String) {
        postStatus("Updating Contracts")
        val contractsResponse = apiRequest { api.getAllContractsByUserId(userId) }
//         conTracts.postValue(contractsResponse.contracts)
        saveContracts(contractsResponse.contracts)
    }

    suspend fun getUserTaskList(): LiveData<List<ToDoListEntityDTO>> {
        val userId = appDb.getUserDao().getUserID()
        fetchUserTaskList(userId)
        return appDb.getEntitiesDao().getAllEntities()
    }

    private suspend fun fetchUserTaskList(userId: String) {
        val toDoListGroupsResponse = apiRequest { api.getUserTaskList(userId) }
        toDoListGroups.postValue(toDoListGroupsResponse.toDoListGroups)
    }

    private fun postEvent(result: XIResult<Boolean>) {
        databaseStatus.postValue(XIEvent(result))
    }

    private fun postStatus(message: String) {
        val status = XIResult.Status(message)
        postEvent(status)
    }

    private fun saveLookups(lookups: ArrayList<LookupDTO>?) {
        Coroutines.io {
            lookups?.forEach { lookup ->
                lookup.let {
                    if (!appDb.getLookupDao().checkIfLookupExist(it.lookupName)) {
                        appDb.getLookupDao().insertLookup(it)
                    }

                    if (!lookup.lookupOptions.isNullOrEmpty()) {
                        lookup.lookupOptions.forEach { lookupOption ->
                            if (!appDb.getLookupOptionDao().checkLookupOptionExists(
                                    lookupOption.valueMember,
                                    lookup.lookupName
                                )
                            ) {

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
    }

    @Transaction
    fun deleteAllData(): Void? {

        appDb.clearAllTables()
        entitiesFetched = false
        return null
    }

    suspend fun checkUnsubmittedMeasureList(activityId: Int): List<JobItemMeasureDTO> {
        return withContext(dispatchers.io()) {
            appDb.getJobItemMeasureDao().checkUnsubmittedMeasureList(activityId)
        }
    }

    suspend fun getJobApproveMeasureForActivityId(activityId: Int): LiveData<List<JobItemMeasureDTO>> {
        return withContext(dispatchers.io()) {
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
        withContext(dispatchers.io()) {
            appDb.getJobDao().updateJob(job.trackRouteId, job.actId, job.jiNo, job.jobId)

            if (!job.workflowItemEstimates.isNullOrEmpty()) {
                for (jobItemEstimate in job.workflowItemEstimates) {
                    appDb.getJobItemEstimateDao().updateExistingJobItemEstimateWorkflow(
                        jobItemEstimate.trackRouteId,
                        jobItemEstimate.actId,
                        jobItemEstimate.estimateId
                    )

                    updateWorkflowEstimateWorks(jobItemEstimate)
                }

                updateWorkflowItemMeasures(job.workflowItemMeasures)
            }
            saveJobSectionsForWorkflow(job.workflowJobSections)
        }
    }

    private fun updateWorkflowItemMeasures(
        workflowItemMeasures: java.util.ArrayList<WorkflowItemMeasureDTO>
    ) {
        workflowItemMeasures.forEach { jobItemMeasure ->
            appDb.getJobItemMeasureDao().updateWorkflowJobItemMeasure(
                jobItemMeasure.itemMeasureId,
                jobItemMeasure.trackRouteId,
                jobItemMeasure.actId,
                jobItemMeasure.measureGroupId
            )
        }
    }

    private fun updateWorkflowEstimateWorks(jobItemEstimate: WorkflowItemEstimateDTO) {
        jobItemEstimate.workflowEstimateWorks.forEach { jobEstimateWorks ->
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

    private fun saveJobSectionsForWorkflow(
        workflowJobSections: java.util.ArrayList<JobSectionDTO>
    ) {
        workflowJobSections.forEach { jobSection ->
            if (!appDb.getJobSectionDao()
                    .checkIfJobSectionExist(jobSection.jobSectionId)
            ) {
                appDb.getJobSectionDao().insertJobSection(jobSection)
            } else {
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
    }

    private fun JobItemMeasureDTO.setJobNo(jiNo: String?) {
        this.jimNo = jiNo
    }

    private fun postValue(photo: String?, fileName: String) {
        savePhoto(photo, fileName)
    }


    private fun savePhoto(encodedPhoto: String?, fileName: String) {
        Coroutines.io {
            if (encodedPhoto != null && fileName.isNotBlank()) {
                photoUtil.persistImageToLocal(encodedPhoto, fileName)
            }
        }
    }

    suspend fun getUpdatedJob(jobId: String): JobDTO {
        return withContext(dispatchers.io()) {
            appDb.getJobDao().getJobForJobId(jobId)
        }
    }

    private fun setWorkflowJobBigEndianGuids(job: WorkflowJobDTO): WorkflowJobDTO {

        job.jobId = DataConversion.toBigEndian(job.jobId)
        job.trackRouteId = DataConversion.toBigEndian(job.trackRouteId)

        job.workflowItemEstimates.forEach { jie ->

            jie.estimateId = DataConversion.toBigEndian(jie.estimateId)!!
            jie.trackRouteId = DataConversion.toBigEndian(jie.trackRouteId)!!
            //  Let's go through the WorkFlowEstimateWorks
            jie.workflowEstimateWorks.forEach { wfe ->
                wfe.trackRouteId = DataConversion.toBigEndian(wfe.trackRouteId)!!
                wfe.worksId = DataConversion.toBigEndian(wfe.worksId)!!

                wfe.estimateId = DataConversion.toBigEndian(wfe.estimateId)!!
            }
        }

        job.workflowItemMeasures.forEach { jim ->
            jim.itemMeasureId = DataConversion.toBigEndian(jim.itemMeasureId)!!
            jim.measureGroupId = DataConversion.toBigEndian(jim.measureGroupId)!!
            jim.trackRouteId = DataConversion.toBigEndian(jim.trackRouteId)!!
        }

        job.workflowJobSections.forEach { js ->
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
        this.isPhotostart = photoStart
    }

    private fun JobItemEstimatesPhotoDTO.setPhotoPath(photoPath: String) {
        this.photoPath = photoPath
    }

    private fun JobDTO.setJobId(toBigEndian: String?) {
        this.jobId = toBigEndian!!
    }

    private fun JobDTO.setProjectId(toBigEndian: String?) {
        this.projectId = toBigEndian!!
    }

    private fun JobDTO.setContractVoId(toBigEndian: String?) {
        this.contractVoId = toBigEndian!!
    }

    private fun JobDTO.setContractId(toBigEndian: String?) {
        this.contractId = toBigEndian!!
    }

    private fun JobDTO.setTrackRouteId(toBigEndian: String?) {
        this.trackRouteId = toBigEndian!!
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

    private fun JobItemMeasureDTO.setItemMeasureId(toBigEndian: String) {
        this.itemMeasureId = toBigEndian
    }

    private fun JobItemMeasureDTO.setJobId(toBigEndian: String) {
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

    private fun JobItemMeasurePhotoDTO.setEstimateId(toBigEndian: String?) {
        this.estimateId = toBigEndian!!
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

    private fun JobItemEstimateDTO.setContractVoId(toBigEndian: String?) {
        this.contractVoId = toBigEndian
    }

//    private fun ProjectVoDTO.setVoApprovalNumber(approvalNumber: String?) {
//        this.approvalNumber = approvalNumber
//    }
//
//    private fun ProjectVoDTO.setVoNumber(voNumber: String?) {
//        this.voNumber = voNumber
//    }

    private fun VoItemDTO.setSectionItemId(sectionItemId: String?) {
        this.sectionItemId = sectionItemId
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

    suspend fun getServiceHealth(): Boolean {
        return try {
            val userId = appDb.getUserDao().getUserID()
            val healthCheck = apiRequest { api.healthCheck(userId) }
            healthCheck.errorMessage.isNullOrBlank() || healthCheck.isAlive == 1
        } catch (t: Throwable) {
            val errorMessage = "Failed to check service health: ${t.message ?: XIErrorHandler.UNKNOWN_ERROR}"
            val healthError = XIResult.Error(t, errorMessage)
            postEvent(healthError)
            false
        }
    }

    suspend fun getHealthCheck(): HealthCheckResponse {
        val userId = appDb.getUserDao().getUserID()
        val healthCheck = apiRequest { api.healthCheck(userId) }
        return withContext(dispatchers.io()) {
            healthCheck
        }
    }

    private fun JobItemMeasureDTO.setDeleted(i: Int) {
        this.deleted = i
    }

}
