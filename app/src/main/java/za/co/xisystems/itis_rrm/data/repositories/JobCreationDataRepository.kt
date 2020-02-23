package za.co.xisystems.itis_rrm.data.repositories

//import sun.security.krb5.Confounder.bytes

import android.app.Activity
import android.os.Build
import android.os.Environment
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.data._commons.views.ToastUtils
import za.co.xisystems.itis_rrm.data.localDB.AppDatabase
import za.co.xisystems.itis_rrm.data.localDB.JobDataController
import za.co.xisystems.itis_rrm.data.localDB.entities.*
import za.co.xisystems.itis_rrm.data.network.BaseConnectionApi
import za.co.xisystems.itis_rrm.data.network.OfflineListener
import za.co.xisystems.itis_rrm.data.network.SafeApiRequest
import za.co.xisystems.itis_rrm.data.network.responses.UploadImageResponse
import za.co.xisystems.itis_rrm.data.preferences.PreferenceProvider
import za.co.xisystems.itis_rrm.utils.*
import za.co.xisystems.itis_rrm.utils.PhotoUtil.getPhotoPathFromExternalDirectory
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

const val MINIMUM_INTERVALY = 3
private val jobDataController : JobDataController? = null
class JobCreationDataRepository(private val api: BaseConnectionApi, private val Db: AppDatabase, private val prefs: PreferenceProvider) : SafeApiRequest() {
    companion object {
        val TAG: String = JobCreationDataRepository::class.java.simpleName
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
//    private val new_job = MutableLiveData<JobDTOTemp>()
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
//        projectItems.observeForever {
//            //            saveProjectItems(it)
//        }

        toDoListGroups.observeForever {
//            saveUserTaskList(it)
        }

//        workflows.observeForever {
//            saveTaskList(it)
//        }

        job.observeForever {
            saveJobs(it)

        }

//        new_job.observeForever {
//            processRrmJobResponse(it)
//
//        }
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

    private fun sendMSg(uploadResponse : String?) {
        val response: UploadImageResponse? = null
        if (uploadResponse.isNullOrEmpty())
            jobDataController?.setMsg(response!!.errorMessage)
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

    private suspend fun fetchJobList(jobId: String) {
        val jobResponse = apiRequest { api.getJobsForApproval(jobId) }
        job.postValue(jobResponse.job)

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


    private fun saveContracts(contracts: List<ContractDTO>) {
        Coroutines.io {
            //            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                prefs.savelastSavedAt(LocalDateTime.now().toString())
//            }
            val actId = 3
            val workState = arrayOf("TA", "START", "MIDDLE", "END", "RTA")
            val workStateDescriptions = arrayOf("Traffic Accomodation", "Work Start", "Work Middle", "Work Completed", "Removal of Traffic Accomodation")
            for(step_code in workState.iterator()){
                if(!Db.getWorkStepDao().checkWorkFlowStepExistsWorkCode(step_code))
                    Db.getWorkStepDao().insertStepsCode(step_code,actId)

                for (desccri in workStateDescriptions.iterator()){
                    if(!Db.getWorkStepDao().checkWorkFlowStepExistsDesc(desccri))
                        Db.getWorkStepDao().updateStepsDesc(desccri, step_code)
                }
            }

//
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
            val userId = Db.getUserDao().getuserID()
            fetchContracts(userId)
            Db.getSectionItemDao().getSectionItems()
        }
    }


    private suspend fun fetchContracts(userId: String) {
        val lastSavedAt = prefs.getLastSavedAt()
            if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    lastSavedAt == null || isFetchNeeded(LocalDateTime.parse(lastSavedAt))
                } else {
                    true
                }
            ) {

                val activitySectionsResponse = apiRequest { api.activitySectionsRefresh(userId) }
                sectionItems.postValue(activitySectionsResponse.activitySections)

            }


    }

    private fun isFetchNeeded(savedAt: LocalDateTime): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ChronoUnit.DAYS.between(savedAt, LocalDateTime.now()) > MINIMUM_INTERVALY
        } else {
            true
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
            ToastUtils().toastLong(activity, e.message)
        } catch (e: NoInternetException) {
            ToastUtils().toastLong(activity, e.message)
            Log.e("NetworkConnection", "No Internet Connection", e)
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
            val direction: Int = WorkflowDirection.NEXT.getValue()
            val trackRouteId: String = job.TrackRouteId!!
            val description: String =
                activity.getResources().getString(R.string.submit_for_approval)

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




    private  fun <T> MutableLiveData<T>.postValue(
        direction: String, linearId: String, pointLocation: Double, sectionId: Int, projectId: String?, jobId: String?, item: ItemDTOTemp?
    ) { saveRouteSectionPoint(direction,linearId, pointLocation,sectionId,projectId, jobId,  item) }

    private fun saveRouteSectionPoint(direction: String, linearId: String, pointLocation: Double, sectionId: Int, projectId: String?, jobId: String?, item: ItemDTOTemp?) {
        if (linearId != null) {
            //Db.getProjectSectionDao().getSectionByRouteSectionProject(linearId, sectionId, direction, projectId)
//           activity?.toast(direction + linearId +  "$pointLocation"  + sectionId.toString()+ projectId )
            if (!Db!!.getSectionPointDao().checkSectionExists(sectionId,projectId,jobId)){
                Db.getSectionPointDao().insertSection(direction,linearId,pointLocation,sectionId,projectId,jobId)
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

                    }

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
//                        DueDate.toString()
//                        StartDate.toString()
//                        IssueDate = DateToString(IssueDate)
//                        val gsonBuilder = GsonBuilder()
//                        gsonBuilder.registerTypeAdapter(Date::class.java, DateDeserializer())
//                        setDuedate(StringToDate(DueDate.toString()))
//                        setIssueDate(StringToDate(IssueDate.toString()))
//                        setStartDate(StringToDate(StartDate.toString()))
//                        setDateApproval(ApprovalDate)



//
//                        ZonedDateTime.parse(DueDate.toString())
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
                            if(jobItemEstimate.trackRouteId != null )
                                jobItemEstimate.setTrackRouteId(
                                    DataConversion.toBigEndian(
                                        jobItemEstimate.trackRouteId
                                    )
                                )else jobItemEstimate.trackRouteId = null

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
                                        val fileName =   DataConversion.toLittleEndian(jobItemEstimatePhoto.filename)
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

    private fun JobItemMeasureDTO.setJobNo(jiNo: String?) {
        this.jimNo = jiNo
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












































