package za.co.xisystems.itis_rrm.data.repositories

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import za.co.xisystems.itis_rrm.data._commons.views.ToastUtils
import za.co.xisystems.itis_rrm.data.localDB.AppDatabase
import za.co.xisystems.itis_rrm.data.localDB.entities.*
import za.co.xisystems.itis_rrm.data.network.BaseConnectionApi
import za.co.xisystems.itis_rrm.data.network.OfflineListener
import za.co.xisystems.itis_rrm.data.network.SafeApiRequest
import za.co.xisystems.itis_rrm.data.preferences.PreferenceProvider
import za.co.xisystems.itis_rrm.utils.*
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.regex.Pattern

/**
 * Created by Francis Mahlava on 2019/11/28.
 */

const val MINIMUM_INTERVAL = 1

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
    private val sectionItems = MutableLiveData<ArrayList<String>>()
    private val projects = MutableLiveData<ArrayList<ProjectDTO>>()
    private val projectItems = MutableLiveData<ArrayList<ItemDTO>>()
    private val voItems = MutableLiveData<ArrayList<VoItemDTO>>()
    private val projectSections = MutableLiveData<ArrayList<ProjectSectionDTO>>()
    //    private val workFlows = MutableLiveData<ArrayList<WorkFlowsDTO>>()
    private val workFlow = MutableLiveData<WorkFlowsDTO>()
    private val lookups = MutableLiveData<ArrayList<LookupDTO>>()
    private val toDoListGroups = MutableLiveData<ArrayList<ToDoGroupsDTO>>()

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

//        voItems.observeForever {
//                        saveVoItems(it)
//        }


//
//        sectionItems.observeForever {
//            //            saveSectionItems(it)
//            saveSectionsItems(it)//            saveProjectsItems(it)
//        }
    }


    suspend fun getContracts(): LiveData<List<ContractDTO>> {
        return withContext(Dispatchers.IO) {
            val userId = Db.getUserDao().getuserID()
            fetchContracts(userId)
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

    suspend fun getProjectItems(): LiveData<List<ItemDTO>> {
        return withContext(Dispatchers.IO) {
            //            val projectId = DataConversion.toLittleEndian( Db.getProjectDao().getProjectId())
            Db.getItemDao().getAllItemsForAllProjects()
        }
    }

    suspend fun getItemForItemCode(sectionItemId: String): LiveData<List<ItemDTO>> {
        return withContext(Dispatchers.IO) {
            //            val projectId = DataConversion.toLittleEndian( Db.getProjectDao().getProjectId())
            Db.getItemDao().getItemForItemCode(sectionItemId)
        }
    }

    suspend fun getAllItemsForProjectId(projectId: String): LiveData<List<ItemDTO>> {
        return withContext(Dispatchers.IO) {
            //            val projectId = DataConversion.toLittleEndian( Db.getProjectDao().getProjectId())
            Db.getItemDao().getAllItemsForProjectId(projectId)
        }
    }
    suspend fun getAllItemsForSectionItem(sectionItemId: String,projectId: String): LiveData<List<ItemDTO>> {
        return withContext(Dispatchers.IO) {
            //            val projectId = DataConversion.toLittleEndian( Db.getProjectDao().getProjectId())
            Db.getItemDao().getAllItemsForSectionItem(sectionItemId,projectId)
        }
    }


    suspend fun getWorkFlows(): LiveData<List<WorkFlowDTO>> {
        return withContext(Dispatchers.IO) {
            val userId = Db.getUserDao().getuserID()
//            fetchContracts(userId)
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

    suspend fun getAllSectionItem(): LiveData<List<SectionItemDTO>> {
        return withContext(Dispatchers.IO) {
            Db.getSectionItemDao().getAllSectionItems()
        }
    }

    private fun saveSectionsItems(activitySections: ArrayList<String>) {
        Coroutines.io {
            prefs.savelastSavedAt(LocalDateTime.now().toString())
            for (activitySection in activitySections) {

                //  Lets get the String
                val pattern = Pattern.compile("(.*?):")
                val matcher = pattern.matcher(activitySection)
                val sectionItemId = SqlLitUtils.generateUuid()

                if (matcher.find()) {
//                    Db.getSectionItemDao().insertSectionitems
                    Db.getSectionItemDao().insertSectionitem(
                        activitySection,
                        matcher.group(1).replace("\\s+".toRegex(), ""), sectionItemId
                    )
                }
            }
        }
    }

    private fun saveContracts(contracts: List<ContractDTO>) {
        Coroutines.io {
            prefs.savelastSavedAt(LocalDateTime.now().toString())
//            Db.getContractDao().saveAllContracts(contracts)
            if (contracts != null) {
                for (contract in contracts) {
                    if (!Db.getContractDao().checkIfContractExists(contract.contractId))
                        Db.getContractDao().insertContract(contract)
                    if (contract.projects != null) {
                        prefs.savelastSavedAt(LocalDateTime.now().toString())
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
                                    if (!Db.getItemDao().checkItemExistsItemId(item.itemId)) {
//                                        Db.getItemDao().insertItem(item)
                                        //  Lets get the ID from Sections Items
                                        val pattern = Pattern.compile("(.*?)\\.")
                                        val matcher = pattern.matcher(item.itemCode)
                                        if (matcher.find()) {
                                            val itemCode = matcher.group(1) + "0"
                                            //  Lets Get the ID Back on Match
                                            val sectionItemId =
                                                Db.getSectionItemDao().getSectionItemId(
                                                    itemCode.replace(
                                                        "\\s+".toRegex(), ""
                                                    )
                                                )
                                            Db.getItemDao().insertItem(
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
                                prefs.savelastSavedAt(LocalDateTime.now().toString())
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

    private fun saveWorkFlowsInfo(workFlows: WorkFlowsDTO) {
        Coroutines.io {
            prefs.savelastSavedAt(LocalDateTime.now().toString())
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

    private suspend fun fetchProjectItems(projectId: String) {
//        val lastSavedAt = prefs.getLastSavedAt()
//        if (lastSavedAt == null || isFetchNeeded(LocalDateTime.parse(lastSavedAt))) {
//        val itemsResponse = apiRequest { api.getProjectItems(projectId) }
//        projectItems.postValue(itemsResponse.items)
//        }
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
//                        )
                    }

                    val entitiesArrayList = toDoListGroup.toDoListEntities

                    for (toDoListEntity in entitiesArrayList) {
                        val jobId = getJobIdFromPrimaryKeyValues(toDoListEntity.primaryKeyValues)

                        insertEntity(toDoListEntity, jobId!!)

                        for (subEntity in toDoListEntity.entities) {
                            insertEntity(subEntity, jobId)
                        }
                    }
                }
            }
        }
    }

    private fun insertEntity(entity: ToDoListEntityDTO, jobId: String) {
        Coroutines.io {
            if (!Db.getEntitiesDao().checkIfEntitiesExist(DataConversion.bigEndianToString(entity.trackRouteId))) {
                Db.getEntitiesDao().insertEntitie(
                    DataConversion.bigEndianToString(entity.trackRouteId)
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
                        DataConversion.bigEndianToString(primaryKeyValue.value),
                        DataConversion.bigEndianToString(entity.trackRouteId),
                        entity.activityId
                    )
                }
            }
        }
    }

    private fun getJobIdFromPrimaryKeyValues(primaryKeyValues: ArrayList<PrimaryKeyValueDTO>): String? {
        for (primaryKeyValue in primaryKeyValues) {
            if (primaryKeyValue.primary_key!!.contains("JobId")) {
                return DataConversion.bigEndianToString(primaryKeyValue.value)
            }
        }
        return null
    }


    private suspend fun fetchUserTaskList(userId: String) {

        val lastSavedAt = prefs.getLastSavedAt()
        if (lastSavedAt == null || isFetchNeeded(LocalDateTime.parse(lastSavedAt))) {
            try {
                val workFlowResponse = apiRequest { api.workflowsRefresh(userId) }
                workFlow.postValue(workFlowResponse.workFlows)

            } catch (e: NoInternetException) {
                ToastUtils().toastLong(appContext,e.message)
            }
        }
    }


    private suspend fun fetchContracts(userId: String) {
        val lastSavedAt = prefs.getLastSavedAt()
        try {
        if (lastSavedAt == null || isFetchNeeded(LocalDateTime.parse(lastSavedAt))) {

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
            ToastUtils().toastLong(appContext,"You do not have an active data connection ")
            } catch (e: ApiException) {
            ToastUtils().toastLong(appContext,e.message)
            } catch (e: NoInternetException) {
            ToastUtils().toastLong(appContext,e.message)
                Log.e("NetworkConnection", "No Internet Connection", e)
            }

    }

    private fun isFetchNeeded(savedAt: LocalDateTime): Boolean {
        return ChronoUnit.MINUTES.between(savedAt, LocalDateTime.now()) > MINIMUM_INTERVAL
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

}

private operator fun <T> LiveData<T>.not(): Boolean {
    return true
}



