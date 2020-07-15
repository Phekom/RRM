package za.co.xisystems.itis_rrm.data.repositories

import android.app.Activity
import androidx.lifecycle.MutableLiveData
import java.util.ArrayList
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.ProjectSectionDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.ToDoGroupsDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.VoItemDTO
import za.co.xisystems.itis_rrm.data.network.SafeApiRequest

/**
 * Created by Francis Mahlava on 2019/11/19.
 */

// const val MINIMUM_INTERVAL = 5

class ContractsRepository : SafeApiRequest() {

    //
//    private val conTracts = MutableLiveData<List<ContractDTO>>()
// //    private val sectionItems = MutableLiveData<SectionItemDTO>()
//    private val sectionItems = MutableLiveData<ArrayList<String>>()
//
    private val toDoListGroups = MutableLiveData<ArrayList<ToDoGroupsDTO>>()
    private val job = MutableLiveData<JobDTO>()
    //    private val new_job = MutableLiveData<JobDTOTemp>()

    private val activity: Activity? = null

    // private var rListener: OfflineListener? = null
    // private val conTracts = MutableLiveData<List<ContractDTO>>()
    //    private val sectionItems = MutableLiveData<ArrayList<SectionItemDTO>>()
    // private val sectionItems = MutableLiveData<ArrayList<String>>()
    // private val projects = MutableLiveData<ArrayList<ProjectDTO>>()
    // private val projectItems = MutableLiveData<ArrayList<ProjectItemDTO>>()
    private val voItems = MutableLiveData<ArrayList<VoItemDTO>>()
    private val projectSections = MutableLiveData<ArrayList<ProjectSectionDTO>>()
    // private val estimatePhoto = MutableLiveData<String>()
    // private val measurePhoto = MutableLiveData<String>()
    // private val workFlow = MutableLiveData<WorkFlowsDTO>()
    // private val lookups = MutableLiveData<ArrayList<LookupDTO>>()

    private val workflows = MutableLiveData<ArrayList<ToDoGroupsDTO>>()

    init {
//        conTracts.observeForever {
//            saveContracts(it)
//        }
//        sectionItems.observeForever {
// //            saveSectionItems(it)
//            insertSectionsItems(it)
//        }

        toDoListGroups.observeForever {
//            saveUserTaskList(it)
        }

//        workflows.observeForever {
//            saveTaskList(it)
//        }

//    job.observeForever {
//        saveJobs(it)
//
//    }
//    workFlow.observeForever {
//            saveWorkFlowsInfo(it)
//        }

//        new_job.observeForever {
//            processRrmJobResponse(it)
//
//        }

//        lookups.observeForever {
//            saveLookups(it)
//        }
// //        projectItems.observeForever {
// //            //            saveProjectItems(it)
// //        }
//
//        conTracts.observeForever {
//            saveContracts(it)
//        }
//        sectionItems.observeForever {
//            saveSectionsItems(it)
//        }
    }

//    private fun insertEntity(entity: ToDoListEntityDTO, jobId: String) {
//        Coroutines.io {
//
//            if (!Db.getEntitiesDao().checkIfEntitiesExist(DataConversion.bigEndianToString(entity.trackRouteId!!))) {
//                Db.getEntitiesDao().insertEntitie(
//                    DataConversion.bigEndianToString(entity.trackRouteId!!)
//                    ,
//                    if (entity.actionable) 1 else 0,
//                    entity.activityId,
//                    entity.currentRouteId,
//                    entity.data,
//                    entity.description,
//                    entity.entities,
//                    entity.entityName,
//                    entity.location,
//                    entity.primaryKeyValues,
//                    entity.recordVersion!!,
//                    jobId
//                )
//
//                for (primaryKeyValue in entity.primaryKeyValues) {
//                    Db.getPrimaryKeyValueDao().insertPrimaryKeyValue(
//                        primaryKeyValue.primary_key,
//                        DataConversion.bigEndianToString(primaryKeyValue.p_value!!),
//                        DataConversion.bigEndianToString(entity.trackRouteId!!),
//                        entity.activityId
//                    )
//                }
//
//            }
//        }
//    }
//
//
//    private fun getJobIdFromPrimaryKeyValues(primaryKeyValues: ArrayList<PrimaryKeyValueDTO>): String? {
//        for (primaryKeyValue in primaryKeyValues) {
//            if (primaryKeyValue.primary_key!!.contains("JobId")) {
//                return DataConversion.bigEndianToString(primaryKeyValue.p_value!!)
//            }
//        }
//        return null
//    }
//
//
//    private fun saveContracts(contracts: List<ContractDTO>) {
//        Coroutines.io {
//            //            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
// //                prefs.savelastSavedAt(LocalDateTime.now().toString())
// //            }
//            val actId = 3
//            val workState = arrayOf("TA", "START", "MIDDLE", "END", "RTA")
//            val workStateDescriptions = arrayOf("Traffic Accomodation", "Work Start", "Work Middle", "Work Completed", "Removal of Traffic Accomodation")
//            for(step_code in workState.iterator()){
//                if(!Db.getWorkStepDao().checkWorkFlowStepExistsWorkCode(step_code))
//                    Db.getWorkStepDao().insertStepsCode(step_code,actId)
//
//                for (desccri in workStateDescriptions.iterator()){
//                    if(!Db.getWorkStepDao().checkWorkFlowStepExistsDesc(desccri))
//                        Db.getWorkStepDao().updateStepsDesc(desccri, step_code)
//                }
//            }
//
// //
//            if (contracts != null) {
//                for (contract in contracts) {
//                    if (!Db.getContractDao().checkIfContractExists(contract.contractId))
//                        Db.getContractDao().insertContract(contract)
//                    if (contract.projects != null) {
// //                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
// //                            prefs.savelastSavedAt(LocalDateTime.now().toString())
// //                        }
//                        for (project in contract.projects) {
//                            if (!Db.getProjectDao().checkProjectExists(project.projectId)) {
//                                Db.getProjectDao().insertProject(
//                                    project.projectId,
//                                    project.descr,
//                                    project.endDate,
//                                    project.items,
//                                    project.projectCode,
//                                    project.projectMinus,
//                                    project.projectPlus,
//                                    project.projectSections,
//                                    project.voItems,
//                                    contract.contractId
//                                )
//                            }
//                            if (project.items != null) {
// //                                val projectId = DataConversion.toLittleEndian(project.projectId)
//                                for (item in project.items) {
//                                    if (!Db.getProjectItemDao().checkItemExistsItemId(item.itemId)) {
// //                                        Db.getItemDao().insertItem(item)
//                                        //  Lets get the ID from Sections Items
//                                        val pattern = Pattern.compile("(.*?)\\.")
//                                        val matcher = pattern.matcher(item.itemCode)
//                                        if (matcher.find()) {
//                                            val itemCode = matcher.group(1) + "0"
//                                            //  Lets Get the ID Back on Match
//                                            val sectionItemId = Db.getSectionItemDao().getSectionItemId(itemCode.replace("\\s+".toRegex(), ""))
// //                                            val sectionItemId = Db.getSectionItemDao().getSectionItemId(item.itemCode!!)
//                                            Db.getProjectItemDao().insertItem(item.itemId, item.itemCode, item.descr, item.itemSections, item.tenderRate, item.uom,
//                                                item.workflowId, sectionItemId, item.quantity, item.estimateId, project.projectId)
//                                        }
//                                    }
//
//                                }
//                            }
//
//                            if (project.projectSections != null) {
//                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                                    prefs.savelastSavedAt(LocalDateTime.now().toString())
//                                }
//                                for (section in project.projectSections) { //project.projectSections
//                                    if (!Db.getProjectSectionDao().checkSectionExists(section.sectionId))
// //                                        Db.getProjectSectionDao().insertSections(section)
//                                        Db.getProjectSectionDao().insertSection(
//                                            section.sectionId,
//                                            section.route,
//                                            section.section,
//                                            section.startKm,
//                                            section.endKm,
//                                            section.direction,
//                                            project.projectId
//                                        )
//                                }
//                            }
//
//                            if (project.voItems != null) {
//                                for (voItem in project.voItems) { //project.voItems
//                                    if (!Db.getVoItemDao().checkIfVoItemExist(voItem.projectVoId))
// //                                        Db.getVoItemDao().insertVoItem(voItem)
//                                        Db.getVoItemDao().insertVoItem(
//                                            voItem.projectVoId,
//                                            voItem.itemCode,
//                                            voItem.voDescr,
//                                            voItem.descr,
//                                            voItem.uom,
//                                            voItem.rate,
//                                            voItem.projectItemId,
//                                            voItem.contractVoId,
//                                            voItem.contractVoItemId,
//                                            project.projectId
//                                        )
//                                }
//                            }
//
//
//                        }
//                    }
//                }
//            }
//
//        }
//    }
//
//
//
//
//    fun saveLookups(lookups: ArrayList<LookupDTO>?) {
//        Coroutines.io {
//            if (lookups != null) {
//                for (lookup in lookups) {
//                    if (!Db.getLookupDao().checkIfLookupExist(lookup.lookupName))
//                        Db.getLookupDao().insertLookup(lookup)
//
//                    if (lookup.lookupOptions != null) {
//                        for (lookupOption in lookup.lookupOptions) {
//                            if (!Db.getLookupOptionDao().checkLookupOptionExists(
//                                    lookupOption.valueMember,
//                                    lookup.lookupName
//                                )
//                            )
//                                Db.getLookupOptionDao().insertLookupOption(
//                                    lookupOption.valueMember, lookupOption.displayMember,
//                                    lookupOption.contextMember, lookup.lookupName
//                                )
//                        }
//                    }
//                }
//            }
//        }
//    }

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
// //        workflows.postValue(workflowMoveResponse.toDoListGroups)
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

//
//    suspend fun getUpdatedJob(jobId: String): JobDTO {
//        return withContext(Dispatchers.IO) {
//            Db.getJobDao().getJobForJobId(jobId)
//        }
//    }

//    private fun uploadmeasueImages(
//        jobItemMeasures: ArrayList<JobItemMeasureDTO>,
//        activity: FragmentActivity?,
//        itemMeasureJob: JobDTO
//    ) {
//        var imageCounter = 1
//        var totalImages = 0
//        if (jobItemMeasures != null) {
//            for (jobItemMeasure in jobItemMeasures.iterator()) {
//                if (jobItemMeasure.jobItemMeasurePhotos != null) {
// //                    var filename = jobItemMeasure.jobItemMeasurePhotos.
//                    for (photo in jobItemMeasure.jobItemMeasurePhotos) {
//                        if (PhotoUtil.photoExist(photo.filename!!)) {
//                            val data: ByteArray = getData(photo.filename, PhotoQuality.HIGH, activity!!)
//                            uploadmeasueImage(photo.filename, activity.getString(R.string.jpg),data,imageCounter, totalImages, itemMeasureJob,activity )
// //                                uploadmeasueImage(jobItemEstimatePhoto.filename, PhotoQuality.HIGH, imageCounter, totalImages, packagejob,activity)
//                            totalImages++
//                        }
//                    }
//
//
//                }
//            }
//        }
//
//    }

//    private fun getData(filename: String, photoQuality: PhotoQuality, activity: FragmentActivity
//    ): ByteArray {
//        val uri = getPhotoPathFromExternalDirectory(activity.applicationContext, filename)
//        val bitmap = PhotoUtil.getPhotoBitmapFromFile(activity.applicationContext, uri, photoQuality)
//        val photo = PhotoUtil.getCompressedPhotoWithExifInfo(activity.applicationContext, bitmap!!, filename)
//        return photo
//    }

//    private fun uploadmeasueImage(
//        filename: String,
//        extension: String,
//        photo: ByteArray,
//        imageCounter: Int,
//        totalImages: Int,
//        itemMeasureJob: JobDTO,
//        activity: FragmentActivity?
//    ) {
//
// //        val bitmap = PhotoUtil.getPhotoBitmapFromFile(activity!!.applicationContext, PhotoUtil.getPhotoPathFromExternalDirectory( activity!!.applicationContext,filename), PhotoQuality.HIGH)
// //        val photo = PhotoUtil.getCompressedPhotoWithExifInfo(activity!!.applicationContext,bitmap!!, filename) //
//
//        processImageUpload(filename, extension,photo, totalImages, imageCounter,itemMeasureJob,activity!!)
//
// //        Coroutines.main {
// //            measureViewModel.processImageUpload(filename,extension ,photo)
// //            activity?.hideKeyboard()
// //            popViewOnJobSubmit()
// //        }
//    }

//    private fun processImageUpload(
//        filename: String,
//        extension: String,
//        photo: ByteArray,
//        totalImages: Int,
//        imageCounter: Int,
//        itemMeasureJob: JobDTO,
//        activity: FragmentActivity
//    ) {
//
//        Coroutines.io {
//            val imagedata = JsonObject()
//            imagedata.addProperty("Filename", filename)
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                imagedata.addProperty("ImageByteArray", Base64.getEncoder().encodeToString(photo))
//            }
//            imagedata.addProperty("ImageFileExtension", extension)
//            Log.e("JsonObject", "Json string $imagedata")
//
//            val uploadImageResponse = apiRequest { api.uploadRrmImage(imagedata) }
//            photoupload.postValue(uploadImageResponse.errorMessage)
//            if (totalImages <= imageCounter)
//                Coroutines.io {
//                    val myjob  = getUpdatedJob(itemMeasureJob.JobId)
//                    moveJobToNextWorkflow(myjob, activity)
//                }
//        }
//    }

//    private fun moveJobToNextWorkflow(
//        job: JobDTO,
//        activity: FragmentActivity
//    ) {
//
//        if (job.TrackRouteId == null) {
//            Looper.prepare() // to be able to make toast
//            Toast.makeText(activity, "Error: trackRouteId is null", Toast.LENGTH_LONG).show()
//        } else {
//            val direction: Int = WorkflowDirection.NEXT.getValue()
//            val trackRouteId: String =  DataConversion.toLittleEndian(job.TrackRouteId)!!
//            val description: String = activity.getResources().getString(R.string.submit_for_approval)
//
//            Coroutines.io {
//                val workflowMoveResponse = apiRequest { api.getWorkflowMove(job.UserId.toString(), trackRouteId, description, direction) }
//                workflowJ.postValue(workflowMoveResponse.workflowJob)
// //                workflows.postValue(workflowMoveResponse.toDoListGroups)
// //                workflows.postValue(workflowMoveResponse.toDoListGroups)
//            }
//
//        }
//    }

//    private fun JobDTO.setTrackRouteId(toLittleEndian: String?) {
//        this.TrackRouteId =  toLittleEndian
//    }
//
//    suspend fun getJobItemsToMeasureForJobId(jobID: String?): LiveData<List<JobItemEstimateDTO>> {
//        return withContext(Dispatchers.IO) {
//            Db.getJobItemEstimateDao().getJobItemsToMeasureForJobId(jobID!!)
//        }
//    }

//    suspend fun getSingleJobFromJobId(jobId: String?): LiveData<JobDTO> {
//        return withContext(Dispatchers.IO) {
//            Db.getJobDao().getJobFromJobId(jobId!!)
//        }
//    }

//    suspend fun getJobItemMeasuresForJobIdAndEstimateId2(
//        jobId: String?,
//        estimateId: String,
//        jobItemMeasureArrayList: ArrayList<JobItemMeasureDTO>
//    ): LiveData<List<JobItemMeasureDTO>>  {
//        val jobItemMeasures = Db.getJobItemMeasureDao().getJobItemMeasuresForJobIdAndEstimateId(jobId, estimateId)
//        if (jobItemMeasures != null) {
//            for (jobItemMeasure in jobItemMeasureArrayList) {
//                if (jobItemMeasure != null) {
//                    if (Db.getJobItemMeasurePhotoDao().checkIfJobItemMeasurePhotoExistsForMeasureId(
//                            jobItemMeasure.itemMeasureId
//                        )
//                    )
//
//                        for (itemMeasurePhoto in jobItemMeasure.jobItemMeasurePhotos) {
//                            Db.getJobItemMeasurePhotoDao().getJobItemMeasurePhotosForItemMeasureID(jobItemMeasure.itemMeasureId)
//
//                            if (Db.getProjectItemDao().checkItemExistsItemId(jobItemMeasure.projectItemId!!)) {
//                                val selectedItem: LiveData<ProjectItemDTO> = Db.getProjectItemDao().getItemForItemId(jobItemMeasure.projectItemId!!)
//                                jobItemMeasure.selectedItemUom = selectedItem.value?.uom
//                            }
//                        }
//
//
//                }
//            }
//        }
//
//        return withContext(Dispatchers.IO) {
//            jobItemMeasures!!
//        }
//    }
//

//
//    suspend fun getItemForItemId(projectItemId: String?): LiveData<ProjectItemDTO> {
//        return withContext(Dispatchers.IO) {
//            Db.getProjectItemDao().getItemForItemId(projectItemId!!)
//        }
//
//    }

//    suspend fun saveJobItemMeasureItems(jobItemMeasures: ArrayList<JobItemMeasureDTO>) {
//        Coroutines.io {
//            for (jobItemMeasure in jobItemMeasures!!.iterator()){
//                if (!Db.getJobItemMeasureDao().checkIfJobItemMeasureExists(jobItemMeasure.itemMeasureId!!)){
//                    Db.getJobItemMeasureDao().insertJobItemMeasure(jobItemMeasure!!)
//                }
//
//            }
//
//        }
//    }

//
//
//    suspend fun getJobMeasureItemsPhotoPath2(itemMeasureId: String): String {
//        return withContext(Dispatchers.IO) {
//            Db.getJobItemMeasurePhotoDao().getJobMeasureItemsPhotoPath(itemMeasureId)
//        }
//    }

//    suspend fun deleteItemMeasurefromList(itemMeasureId: String) {
//        Coroutines.io {
//            Db.getJobItemMeasureDao().deleteItemMeasurefromList(itemMeasureId)
//        }
//    }
//
//    suspend fun deleteItemMeasurephotofromList(itemMeasureId: String) {
//        Coroutines.io {
//            Db.getJobItemMeasurePhotoDao().deleteItemMeasurephotofromList(itemMeasureId)
//        }
//    }

//
//    suspend fun setJobItemMeasureImages(
//        jobItemMeasurePhotoList: ArrayList<JobItemMeasurePhotoDTO>,
//        estimateId: String?,
//        selectedJobItemMeasure: JobItemMeasureDTO
//    ){
//        Coroutines.io {
//            for (jobItemMeasurePhoto in jobItemMeasurePhotoList.iterator()){
//                Db.getJobItemMeasureDao().insertJobItemMeasure(selectedJobItemMeasure)
//                if (!Db.getJobItemMeasurePhotoDao().checkIfJobItemMeasurePhotoExists(jobItemMeasurePhoto.filename!!)){
//
//                    Db.getJobItemMeasurePhotoDao().insertJobItemMeasurePhoto(jobItemMeasurePhoto!!)
//                    jobItemMeasurePhoto.setEstimateId(estimateId)
//                    Db.getJobItemMeasureDao().upDatePhotList(jobItemMeasurePhotoList,selectedJobItemMeasure.itemMeasureId!!)
//                }
//
//            }
//
//        }
//    }
//
//    suspend fun getJobItemMeasuresForJobIdAndEstimateId(
//        jobId: String?,
//        estimateId: String
//    ): LiveData<List<JobItemMeasureDTO>>  {
//        val jobItemMeasures = Db.getJobItemMeasureDao().getJobItemMeasuresForJobIdAndEstimateId(jobId,estimateId)
//        return withContext(Dispatchers.IO) {
//            jobItemMeasures!!
//        }
//    }
//

//
//    suspend fun getJobItemMeasurePhotosForItemEstimateID(estimateId: String): LiveData<List<JobItemMeasurePhotoDTO>>  {
//        return withContext(Dispatchers.IO) {
//            Db.getJobItemMeasurePhotoDao().getJobItemMeasurePhotosForItemEstimateID(estimateId)
//        }
//    }
//
//    private fun JobItemMeasurePhotoDTO.setEstimateId(estimateId: String?) {
//        this.estimateId = estimateId
//    }

//    suspend fun getItemJobNo(jobId: String): String {
//        return withContext(Dispatchers.IO) {
//            Db.getJobDao().getItemJobNo(jobId)
//        }
//    }

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

// }

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
// //        workflows.postValue(workflowMoveResponse.toDoListGroups)
//
//    }

//    suspend fun getOfflinedata(): LiveData<List<ContractDTO>> {
//        return withContext(Dispatchers.IO) {
//            val userId = Db.getUserDao().getuserID()
//            fetchContracts(userId)
//            Db.getContractDao().getAllContracts()
//        }
//    }
//    suspend fun getSectionItems(): LiveData<SectionItemDTO> {
//        return withContext(Dispatchers.IO) {
//            val userId = Db.getUserDao().getuserID()
//            fetchContracts(userId)
//            Db.getSectionItemDao().getAllSectionItems()
//        }
//    }
//    private fun saveContracts(offlinedata: List<ContractDTO>) {
//        Coroutines.io {
//            prefs.savelastSavedAt(LocalDateTime.now().toString())
//            Db.getContractDao().saveAllContracts(offlinedata)
//        }
//    }
//    private fun saveSectionItems(sectionItems: SectionItemDTO) {
//        Coroutines.io {
// //            Db.getSectionItemDao().insertEntities(sectionItems)
//        }
//    }
//
//    suspend fun getRouteSectionPoint(
//        latitude: Double,
//        longitude: Double,
//        useR: String,
//        projectId: String?,
//        jobId: String,
//        itemCode: ItemDTOTemp?
//    ) {
// //        val routeSectionPointResponse = apiRequest { api.getRouteSectionPoint(latitude,longitude,useR) }
// //        routeSectionPoint.postValue(routeSectionPointResponse.direction,routeSectionPointResponse.linearId,routeSectionPointResponse.pointLocation,routeSectionPointResponse.sectionId, projectId, jobId, itemCode)
//    }

//    suspend fun submitJob(userId: Int, job: JobDTO, activity: FragmentActivity)  : String{
//
//        val jobhead = JsonObject()
//        val jobdata = JsonObject()
//        val jobestimatedata = JsonObject()
//        val array = JsonArray()
//        array.add(jobestimatedata)
//        val jobestimateimagesdata1 = JsonObject()
//        val jobestimateimagesdata = JsonObject()
//        val array4 = JsonArray()
//        array4.add(jobestimateimagesdata1)
//        array4.add(jobestimateimagesdata)
// //        jobItemEstimatePhotoEnd
// //        jobItemEstimatePhotoStart
//        jobestimatedata.addProperty("selectedItemUOM", job.JobItemEstimates?.get(0)?.SelectedItemUOM )
//        jobestimatedata.addProperty("estimateComplete",job.JobItemEstimates?.get(0)?.estimateComplete )
//        jobestimatedata.addProperty("ActId", job.JobItemEstimates?.get(0)?.actId )
//        jobestimatedata.addProperty("EstimateId",  DataConversion.toLittleEndian( job.JobItemEstimates?.get(0)?.estimateId ))
//        jobestimatedata.addProperty("PrjJobDto", job.JobItemEstimates?.get(0)?.job.toString() )
//        jobestimatedata.addProperty("JobId",  DataConversion.toLittleEndian( job.JobItemEstimates?.get(0)?.jobId ))
//        jobestimatedata.add("MobileJobItemEstimatesPhotos", array4)
//        jobestimatedata.addProperty("LineRate", job.JobItemEstimates?.get(0)?.lineRate )
//        jobestimatedata.addProperty("MobileEstimateWorks",  job.JobItemEstimates?.get(0)?.jobEstimateWorks?.toString() )
//        jobestimatedata.addProperty("MobileJobItemMeasures",  job.JobItemEstimates?.get(0)?.jobItemMeasure.toString() )
//        jobestimatedata.addProperty("ProjectItemId",   DataConversion.toLittleEndian( job.JobItemEstimates?.get(0)?.projectItemId ))
//        jobestimatedata.addProperty("ProjectVoId",  job.JobItemEstimates?.get(0)?.projectVoId )
//        jobestimatedata.addProperty("Qty",  job.JobItemEstimates?.get(0)?.qty )
//        jobestimatedata.addProperty("RecordSynchStateId",  job.JobItemEstimates?.get(0)?.recordSynchStateId )
//        jobestimatedata.addProperty("RecordVersion",  job.JobItemEstimates?.get(0)?.recordVersion )
//        jobestimatedata.addProperty("TrackRouteId",  job.JobItemEstimates?.get(0)?.trackRouteId )
//
// //        for (i in  job.JobItemEstimates?.get(0)?.jobItemEstimatePhotos!!.indices){
// //
// //        }
//        jobestimateimagesdata1.addProperty("Descr", job.JobItemEstimates?.get(0)?.jobItemEstimatePhotos?.get(0)?.descr )
//        jobestimateimagesdata1.addProperty("Endkm",job.JobItemEstimates?.get(0)?.jobItemEstimatePhotos?.get(0)?.endKm )
//        jobestimateimagesdata1.addProperty("EstimateId", DataConversion.toLittleEndian( job.JobItemEstimates?.get(0)?.jobItemEstimatePhotos?.get(0)?.estimateId ))
//        jobestimateimagesdata1.addProperty("Filename", job.JobItemEstimates?.get(0)?.jobItemEstimatePhotos?.get(0)?.filename )
//        jobestimateimagesdata1.addProperty("IsPhotoStart", job.JobItemEstimates?.get(0)?.jobItemEstimatePhotos?.get(0)?.is_PhotoStart )
//        jobestimateimagesdata1.addProperty("PrjJobItemEstimateDto", job.JobItemEstimates?.get(0)?.jobItemEstimatePhotos?.get(0)?.jobItemEstimate.toString() )
//        jobestimateimagesdata1.addProperty("PhotoDate", job.JobItemEstimates?.get(0)?.jobItemEstimatePhotos?.get(0)?.photoDate )
//        jobestimateimagesdata1.addProperty("PhotoEnd",  job.JobItemEstimates?.get(0)?.jobItemEstimatePhotos?.get(0)?.photoEnd )
//        jobestimateimagesdata1.addProperty("PhotoId",  DataConversion.toLittleEndian( job.JobItemEstimates?.get(0)?.jobItemEstimatePhotos?.get(0)?.photoId ))
//        jobestimateimagesdata1.addProperty("PhotoLatitude",  job.JobItemEstimates?.get(0)?.jobItemEstimatePhotos?.get(0)?.photoLatitude )
//        jobestimateimagesdata1.addProperty("PhotoLatitudeEnd",  job.JobItemEstimates?.get(0)?.jobItemEstimatePhotos?.get(0)?.photoLatitudeEnd )
//        jobestimateimagesdata1.addProperty("PhotoLongitude",  job.JobItemEstimates?.get(0)?.jobItemEstimatePhotos?.get(0)?.photoLongitude )
//        jobestimateimagesdata1.addProperty("PhotoLongitudeEnd",  job.JobItemEstimates?.get(0)?.jobItemEstimatePhotos?.get(0)?.photoLongitudeEnd )
//        jobestimateimagesdata1.addProperty("PhotoPath",  job.JobItemEstimates?.get(0)?.jobItemEstimatePhotos?.get(0)?.photoPath )
//        jobestimateimagesdata1.addProperty("PhotoStart",  job.JobItemEstimates?.get(0)?.jobItemEstimatePhotos?.get(0)?.photoStart )
//        jobestimateimagesdata1.addProperty("RecordSynchStateId",  job.JobItemEstimates?.get(0)?.jobItemEstimatePhotos?.get(0)?.recordSynchStateId )
//        jobestimateimagesdata1.addProperty("RecordVersion",  job.JobItemEstimates?.get(0)?.jobItemEstimatePhotos?.get(0)?.recordVersion )
//        jobestimateimagesdata1.addProperty("Startkm",  job.JobItemEstimates?.get(0)?.jobItemEstimatePhotos?.get(0)?.startKm )
//
//        jobestimateimagesdata.addProperty("Descr", job.JobItemEstimates?.get(0)?.jobItemEstimatePhotos?.get(1)?.descr )
//        jobestimateimagesdata.addProperty("Endkm",job.JobItemEstimates?.get(0)?.jobItemEstimatePhotos?.get(1)?.endKm )
//        jobestimateimagesdata.addProperty("EstimateId", DataConversion.toLittleEndian( job.JobItemEstimates?.get(0)?.jobItemEstimatePhotos?.get(1)?.estimateId ))
//        jobestimateimagesdata.addProperty("Filename", job.JobItemEstimates?.get(0)?.jobItemEstimatePhotos?.get(1)?.filename )
//        jobestimateimagesdata.addProperty("IsPhotoStart", job.JobItemEstimates?.get(0)?.jobItemEstimatePhotos?.get(1)?.is_PhotoStart )
//        jobestimateimagesdata.addProperty("PrjJobItemEstimateDto", job.JobItemEstimates?.get(0)?.jobItemEstimatePhotos?.get(1)?.jobItemEstimate.toString() )
//        jobestimateimagesdata.addProperty("PhotoDate", job.JobItemEstimates?.get(0)?.jobItemEstimatePhotos?.get(1)?.photoDate )
//        jobestimateimagesdata.addProperty("PhotoEnd",  job.JobItemEstimates?.get(0)?.jobItemEstimatePhotos?.get(1)?.photoEnd )
//        jobestimateimagesdata.addProperty("PhotoId",  DataConversion.toLittleEndian( job.JobItemEstimates?.get(0)?.jobItemEstimatePhotos?.get(1)?.photoId ))
//        jobestimateimagesdata.addProperty("PhotoLatitude",  job.JobItemEstimates?.get(0)?.jobItemEstimatePhotos?.get(1)?.photoLatitude )
//        jobestimateimagesdata.addProperty("PhotoLatitudeEnd",  job.JobItemEstimates?.get(0)?.jobItemEstimatePhotos?.get(1)?.photoLatitudeEnd )
//        jobestimateimagesdata.addProperty("PhotoLongitude",  job.JobItemEstimates?.get(0)?.jobItemEstimatePhotos?.get(1)?.photoLongitude )
//        jobestimateimagesdata.addProperty("PhotoLongitudeEnd",  job.JobItemEstimates?.get(0)?.jobItemEstimatePhotos?.get(1)?.photoLongitudeEnd )
//        jobestimateimagesdata.addProperty("PhotoPath",  job.JobItemEstimates?.get(0)?.jobItemEstimatePhotos?.get(1)?.photoPath )
//        jobestimateimagesdata.addProperty("PhotoStart",  job.JobItemEstimates?.get(0)?.jobItemEstimatePhotos?.get(1)?.photoStart )
//        jobestimateimagesdata.addProperty("RecordSynchStateId",  job.JobItemEstimates?.get(0)?.jobItemEstimatePhotos?.get(1)?.recordSynchStateId )
//        jobestimateimagesdata.addProperty("RecordVersion",  job.JobItemEstimates?.get(0)?.jobItemEstimatePhotos?.get(1)?.recordVersion )
//        jobestimateimagesdata.addProperty("Startkm",  job.JobItemEstimates?.get(0)?.jobItemEstimatePhotos?.get(1)?.startKm )
//
//
//        val jobmeasuredata = JsonObject()
//        val array1 = JsonArray()
//         //array1.add(jobmeasuredata)//job.JobItemEstimates.toString()
//        val jobesectiondata = JsonObject()
//        val array2 = JsonArray()
//        array2.add(jobesectiondata)
//        jobesectiondata.addProperty("EndKm", job.JobSections?.get(0)?.endKm )
//        jobesectiondata.addProperty("PrjJobDto", job.JobSections?.get(0)?.job.toString() )
//        jobesectiondata.addProperty("JobId", DataConversion.toLittleEndian( job.JobSections?.get(0)?.jobId))
//        jobesectiondata.addProperty("JobSectionId", DataConversion.toLittleEndian( job.JobSections?.get(0)?.jobSectionId ))
//        jobesectiondata.addProperty("ProjectSectionId", DataConversion.toLittleEndian( job.JobSections?.get(0)?.projectSectionId ))
//        jobesectiondata.addProperty("RecordSynchStateId", job.JobSections?.get(0)?.recordSynchStateId )
//        jobesectiondata.addProperty("RecordVersion", job.JobSections?.get(0)?.recordVersion )
//        jobesectiondata.addProperty("StartKm", job.JobSections?.get(0)?.startKm )
//
// //        array.add(job.JobItemEstimates.toString())
//        jobdata.addProperty("ActId", job.ActId )
//        jobdata.addProperty("IssueDate", job.IssueDate.toString() )
//        jobdata.addProperty("DueDate", job.DueDate.toString() )
//        jobdata.addProperty("StartDate", job.StartDate.toString() )
//        jobdata.addProperty("ApprovalDate", job.ApprovalDate.toString() )
//        jobdata.addProperty("ContractVoId", DataConversion.toLittleEndian( job.ContractVoId ))
//        jobdata.addProperty("ContractorId", job.ContractorId )
//        jobdata.addProperty("Cpa", job.Cpa )
//        jobdata.addProperty("DayWork", job.DayWork )
//        jobdata.addProperty("Descr", job.Descr )
//        jobdata.addProperty("EndKm", job.EndKm )
//        jobdata.addProperty("EngineerId", job.EngineerId )
//        jobdata.addProperty("EntireRoute", job.EntireRoute )
//        jobdata.addProperty("IsExtraWork", job.IsExtraWork )
//        jobdata.addProperty("JiNo", job.JiNo )
//        jobdata.addProperty("JobCategoryId", job.JobCategoryId )
//        jobdata.addProperty("JobDirectionId", job.JobDirectionId )
//        jobdata.addProperty("JobId", DataConversion.toLittleEndian( job.JobId  ))
//        jobdata.add("MobileJobItemEstimates", array )
//        jobdata.add("MobileJobItemMeasures", array1 )
//        jobdata.addProperty("JobPositionId", job.JobPositionId )
//        jobdata.add("MobileJobSections", array2 )
//        jobdata.addProperty("JobStatusId", job.JobStatusId )
//        jobdata.addProperty("M9100", job.M9100 )
//        jobdata.addProperty("PerfitemGroupId", job.PerfitemGroupId )
//        jobdata.addProperty("ProjectId", DataConversion.toLittleEndian( job.ProjectId ))
//        jobdata.addProperty("ProjectVoId", job.ProjectVoId )
//        jobdata.addProperty("QtyUpdateAllowed", job.QtyUpdateAllowed )
//        jobdata.addProperty("RecordSynchStateId", job.RecordSynchStateId )
//        jobdata.addProperty("RecordVersion", job.RecordVersion )
//        jobdata.addProperty("Remarks", job.Remarks )
//        jobdata.addProperty("Route", job.Route )
//        jobdata.addProperty("RrmJiNo", job.RrmJiNo )
//        jobdata.addProperty("Section", job.Section)
//        jobdata.addProperty("SectionId", job.SectionId )
//        jobdata.addProperty("StartKm", job.StartKm )
//        jobdata.addProperty("TrackRouteId", job.TrackRouteId )
//        jobdata.addProperty("UserId", job.UserId )
//        jobdata.addProperty("VoId", job.VoId )
//        jobdata.addProperty("WorkCompleteDate", job.WorkCompleteDate )
//        jobdata.addProperty("WorkStartDate", job.WorkStartDate )
// //
//        jobhead.add("Job", jobdata)
//        jobhead.addProperty("UserId", userId)
// //        var jsonk = gson.toJson(json2)
//        Log.e("JsonObject", "Json string $jobhead")
//        val jobResponse = apiRequest { api.sendJobsForApproval(jobhead) }
//        workflowJ2.postValue(jobResponse.workflowJob,job ,activity)
//
//        val messages = jobResponse.errorMessage   //activity.getResources().getString(R.string.please_wait)
//        return withContext(Dispatchers.IO) {
//            messages
//        }
//    }

//    suspend fun submitWorks(
//        itemEstiWorks: JobEstimateWorksDTO,
//        estimateWorksPhotoArrayList: java.util.ArrayList<JobEstimateWorksPhotoDTO>,
//        activity: FragmentActivity,
//        jobitemEsti: JobItemEstimateDTO?,
//        jobitemEstiWorks: JobEstimateWorksDTO?,
//        useR: UserDTO
//    ) : String
//    {
//        val worksdata = JsonObject()
//        val wrkdata = JsonObject()
//        val jdata = JsonObject()
//        val array = JsonArray()
//        array.add(jdata)
//        wrkdata.addProperty("ActId", itemEstiWorks.actId )
//        wrkdata.addProperty("EstimateId", DataConversion.toLittleEndian( itemEstiWorks.estimateId ))
//        wrkdata.addProperty("PrjJobItemEstimateDto", jobitemEsti.toString())
//        wrkdata.add("MobileJobEstimateWorksPhotos", array)
//        wrkdata.addProperty("RecordSynchStateId", itemEstiWorks.recordSynchStateId )
//        wrkdata.addProperty("RecordVersion", itemEstiWorks.recordVersion )
//        wrkdata.addProperty("TrackRouteId", DataConversion.toLittleEndian( itemEstiWorks.trackRouteId))
//        wrkdata.addProperty("WorksId", DataConversion.toLittleEndian( itemEstiWorks.worksId ))
//
//        jdata.addProperty("Descr", estimateWorksPhotoArrayList.get(0)?.descr )
//        jdata.addProperty("Filename",  estimateWorksPhotoArrayList.get(0)?.filename )
//        jdata.addProperty("PrjEstimateWorksDto", jobitemEstiWorks.toString())
//        jdata.addProperty("PhotoActivityId",  estimateWorksPhotoArrayList.get(0)?.photoActivityId )
//        jdata.addProperty("PhotoDate", estimateWorksPhotoArrayList.get(0)?.photoDate )
//        jdata.addProperty("PhotoId",  DataConversion.toLittleEndian( estimateWorksPhotoArrayList.get(0)?.photoId ))
//        jdata.addProperty("PhotoLatitude",  estimateWorksPhotoArrayList.get(0)?.photoLatitude )
//        jdata.addProperty("PhotoLongitude",   estimateWorksPhotoArrayList.get(0)?.photoLongitude )
//        jdata.addProperty("PhotoPath",  estimateWorksPhotoArrayList.get(0)?.photoPath )
//        jdata.addProperty("RecordSynchStateId",  estimateWorksPhotoArrayList.get(0)?.recordSynchStateId )
//        jdata.addProperty("RecordVersion",  estimateWorksPhotoArrayList.get(0)?.recordVersion )
//        jdata.addProperty("WorksId",  DataConversion.toLittleEndian( estimateWorksPhotoArrayList.get(0)?.worksId ))
//
//        worksdata.add("JobEstimateWorksItem", wrkdata)
//        Log.e("JsonObject", "Json string $worksdata")
//        val uploadWorksItemResponse = apiRequest { api.uploadWorksItem(worksdata) }
//        works.postValue(uploadWorksItemResponse.errorMessage,itemEstiWorks ,activity,useR)
//
//        val messages = activity.getResources().getString(R.string.please_wait) //uploadWorksItemResponse.errorMessage
//        return withContext(Dispatchers.IO) {
//            messages
//        }
//
// //        DataConversion.toLittleEndian()
//    }

//   suspend fun saveMeasurementItems(
//       userId: String,
//       jobId: String,
//       jimNo: String?,
//       contractVoId: String?,
//       mSures: ArrayList<JobItemMeasureDTO>,
//       activity: FragmentActivity?,
//       itemMeasureJob: JobDTO
//   ) : String {
//       val measuredata = JsonObject()
//       val jmadata = JsonObject()
//       val jdata = JsonObject()
//       val array = JsonArray()
//       val array1 = JsonArray()
//       val array2 = JsonArray()
//       val array3 = JsonArray()
//       measuredata.addProperty("ContractId", contractVoId)
//       measuredata.addProperty("JiNo", jimNo)
//       measuredata.addProperty("JobId", jobId)
//       measuredata.add("MeasurementItems", array )
// //       array.add(jdata)
//       array.add(jdata)
//       for (i in  mSures.indices){
//           jdata.addProperty("ActId", mSures.get(i).actId )
//           jdata.addProperty("ApprovalDate", mSures.get(i).approvalDate)
//           jdata.addProperty("Cpa", mSures.get(i).cpa)
//           jdata.addProperty("EndKm", mSures.get(i).endKm)
//           jdata.addProperty("EstimateId", DataConversion.toLittleEndian(mSures.get(i).estimateId))
//           jdata.addProperty("ItemMeasureId", DataConversion.toLittleEndian(mSures.get(i).itemMeasureId))
//           jdata.addProperty("JimNo", mSures.get(i).jimNo)
//           jdata.addProperty("PrjJobDto", mSures.get(i).job.toString())
// //       jdata.add("PrjJobDto", array1 )
// //       array1.add(jmadata)
//           jdata.addProperty("JobDirectionId", mSures.get(i).jobDirectionId)
//           jdata.addProperty("JobId", DataConversion.toLittleEndian(mSures.get(i).jobId))
//           jdata.addProperty("PrjJobItemEstimateDto", mSures.get(i).jobItemEstimate.toString())
// //       jdata.add("PrjJobItemEstimateDto", array2 )
// //       array2.add(jmadata)
//           jdata.addProperty("LineAmount", mSures.get(i).lineAmount)
//           jdata.addProperty("LineRate", mSures.get(i).lineRate)
//           jdata.addProperty("MeasureDate", mSures.get(i).measureDate)
//           jdata.addProperty("MeasureGroupId", mSures.get(i).measureGroupId)
//           jdata.add("PrjItemMeasurePhotoDtos", array3)
//           array3.add(jmadata)
//           jmadata.addProperty("Descr",mSures.get(i).jobItemMeasurePhotos.get(i).descr)
//           jmadata.addProperty("Filename",mSures.get(i).jobItemMeasurePhotos.get(i).filename)
//           jmadata.addProperty("ItemMeasureId",DataConversion.toLittleEndian(mSures.get(i).jobItemMeasurePhotos.get(i).itemMeasureId))
//           jmadata.addProperty("PrjJobItemMeasureDto",mSures.get(i).jobItemMeasurePhotos.get(i).jobItemMeasure.toString())
//           jmadata.addProperty("PhotoDate",mSures.get(i).jobItemMeasurePhotos.get(i).photoDate)
//           jmadata.addProperty("PhotoId", DataConversion.toLittleEndian(mSures.get(i).jobItemMeasurePhotos.get(i).photoId))
//           jmadata.addProperty("PhotoLatitude",mSures.get(i).jobItemMeasurePhotos.get(i).photoLatitude)
//           jmadata.addProperty("PhotoLongitude",mSures.get(i).jobItemMeasurePhotos.get(i).photoLongitude)
//           jmadata.addProperty("PhotoPath",mSures.get(i).jobItemMeasurePhotos.get(i).photoPath)
//           jmadata.addProperty("RecordSynchStateId",mSures.get(i).jobItemMeasurePhotos.get(i).recordSynchStateId)
//           jmadata.addProperty("RecordVersion",mSures.get(i).jobItemMeasurePhotos.get(i).recordVersion)
//
//           jdata.addProperty("ProjectItemId", DataConversion.toLittleEndian(mSures.get(i).projectItemId))
//           jdata.addProperty("ProjectVoId", mSures.get(i).projectVoId)
//           jdata.addProperty("Qty", mSures.get(i).qty)
//           jdata.addProperty("RecordSynchStateId", mSures.get(i).recordSynchStateId)
//           jdata.addProperty("RecordVersion", mSures.get(i).recordVersion)
//           jdata.addProperty("StartKm", mSures.get(i).startKm)
//           jdata.addProperty("TrackRouteId", DataConversion.toLittleEndian(mSures.get(i).trackRouteId))
//
//       }
//
//
//       //TODO(finish building the MeasureItems and Location)
// //       measuredata.add("MeasurementItems", mSures)
//       measuredata.addProperty("UserId", userId)
//
//
//       Log.e("JsonObject", "Json string $measuredata")
//       val measurementItemResponse = apiRequest { api.saveMeasurementItems(measuredata) }
//        workflowJ.postValue(measurementItemResponse.workflowJob,mSures, activity, itemMeasureJob)
//
//       val messages = measurementItemResponse.errorMessage //activity?.getResources()?.getString(R.string.please_wait)
//       return withContext(Dispatchers.IO) {
//           messages
//       }
//    }

//    suspend fun imageUpload(filename: String, extension: String, photo: ByteArray) {
//        val imagedata = JsonObject()
//        imagedata.addProperty("Filename", filename)
//        imagedata.addProperty("ImageByteArray", Base64.getEncoder().encodeToString(photo))
//        imagedata.addProperty("ImageFileExtension", extension)
//
//        val uploadImageResponse = apiRequest { api.uploadRrmImage(imagedata) }
//        photoupload.postValue(uploadImageResponse.errorMessage)
//    }

//    fun insertSectionsItems(activitySections: ArrayList<String>) {
//        Coroutines.io {
//            //            Db.getSectionItemDao().insertEntities(sectionItems)
//            for (activitySection in activitySections) {
//
//                //  Lets get the String
//                val pattern = Pattern.compile("(.*?):")
//                val matcher = pattern.matcher(activitySection)
//                val scetionItemId = SqlLitUtils.generateUuid()
//
//
//                if (matcher.find()) {
//                    Db.getSectionItemDao().insertSectionitem(activitySection,
//                        matcher.group(1).replace("\\s+".toRegex(), ""), scetionItemId
//                    )
//
//                }
//
//            }
//        }
//
//
//    }
// //    suspend fun saveJobItemMeasureItems(jobItemMeasures: ArrayList<JobItemMeasureDTO>) {
// //        Coroutines.io {
// //            for (jobItemMeasure in jobItemMeasures!!.iterator()){
// //                if (!Db.getJobItemMeasureDao().checkIfJobItemMeasureExists(jobItemMeasure.itemMeasureId!!)){
// //                    Db.getJobItemMeasureDao().insertJobItemMeasure(jobItemMeasure!!)
// //                }
// //
// //            }
// //
// //        }
// //    }
//
// //    suspend fun setJobItemMeasureImages(
// //        jobItemMeasurePhotoList: ArrayList<JobItemMeasureDTO>,
// //        estimateId: String?,
// //        selectedJobItemMeasure: JobItemMeasureDTO
// //    ){
// //        Coroutines.io {
// //            for (jobItemMeasurePhoto in jobItemMeasurePhotoList.iterator()){
// //                Db.getJobItemMeasureDao().insertJobItemMeasure(selectedJobItemMeasure)
// //                if (!Db.getJobItemMeasurePhotoDao().checkIfJobItemMeasurePhotoExists(jobItemMeasurePhoto.filename!!)){
// //
// //                    Db.getJobItemMeasurePhotoDao().insertJobItemMeasurePhoto(jobItemMeasurePhoto!!)
// //                    jobItemMeasurePhoto.setEstimateId(estimateId)
// //                    Db.getJobItemMeasureDao().upDatePhotList(jobItemMeasurePhotoList,selectedJobItemMeasure.itemMeasureId!!)
// //                }
// //
// //            }
// //
// //        }
// //    }
//
    //    suspend fun getJobMeasureForActivityId(activityId: Int): LiveData<List<JobItemEstimateDTO>> {
//        return withContext(Dispatchers.IO) {
//            offlineDataRepository.getJobMeasureForActivityId(activityId)getJobMeasureForActivityId
//        }
//    }
//    suspend fun getProjectSectionIdForJobId(jobId: String): String {
//        return withContext(Dispatchers.IO) {
//            offlineDataRepository.getProjectSectionIdForJobId(jobId)
//        }
//    }

//    suspend fun getRouteForProjectSectionId(sectionId: String): String {
//        return withContext(Dispatchers.IO) {
//            offlineDataRepository.getRouteForProjectSectionId(sectionId)
//        }
//    }
//    suspend fun getSectionForProjectSectionId(sectionId: String): String {
//        return withContext(Dispatchers.IO) {
//            offlineDataRepository.getSectionForProjectSectionId(sectionId)
//        }
//    }

//    suspend fun getItemDesc(jobId: String): String {
//        return withContext(Dispatchers.IO) {
//            offlineDataRepository.getItemDescription(jobId)
//        }
//    }

//    pend fun getDescForProjectId(projectItemId: String): String {
//        return withContext(Dispatchers.IO) {
//            offlineDataRepository.getProjectItemDescription(projectItemId)
//        }
//    }

//    suspend fun getJobMeasureItemsForJobId(jobID: String?,actId: Int): LiveData<List<JobItemMeasureDTO>> {
//        return withContext(Dispatchers.IO) {
//            offlineDataRepository.getJobMeasureItemsForJobId(jobID, actId)
//        }
//    }

//    suspend fun getUOMForProjectItemId(projectItemId: String): String {
//        return withContext(Dispatchers.IO) {
//            offlineDataRepository.getUOMForProjectItemId(projectItemId)
//        }
//    }

//    private fun saveUserTaskList(toDoListGroups: ArrayList<ToDoGroupsDTO>?) {
//        Coroutines.io {
//            if (toDoListGroups != null) {
//                for (toDoListGroup in toDoListGroups) {
//                    if (!Db.getToDoGroupsDao().checkIfGroupCollectionExist(toDoListGroup.groupId)) {
//                        Db.getToDoGroupsDao().insertToDoGroups(toDoListGroup)
//
//                    }
//
//                    val entitiesArrayList = toDoListGroup.toDoListEntities
//
//                    for (toDoListEntity in entitiesArrayList) {
//                        val jobId = getJobIdFromPrimaryKeyValues(toDoListEntity.primaryKeyValues)
//                        insertEntity(toDoListEntity, jobId!!)
//                        val job_Id = DataConversion.toLittleEndian(jobId)
//                        fetchJobList(job_Id!!)
// //                        for (subEntity in toDoListEntity.entities) {
// //                            insertEntity(subEntity, jobId)
// ////                            val job_Id = DataConversion.toLittleEndian(jobId!!)
// ////                            fetchJobList(job_Id!!)
// //                        }
//
//                    }
//
//                }
//
//            }
//        }
//    }
//
//
//    private suspend fun fetchJobList(jobId: String) {
//        val jobResponse = apiRequest { api.getJobsForApproval(jobId) }
//        job.postValue(jobResponse.job)
//
//    }

//
//    fetchContracts(userId)
//    val userId = Db.getUserDao().getuserID()
//    private suspend fun fetchContracts(userId: String) {
//        val lastSavedAt = prefs.getLastSavedAt()
//        if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                lastSavedAt == null || isFetchNeeded(LocalDateTime.parse(lastSavedAt))
//            } else {
//                true
//            }
//        ) {
//
//            val activitySectionsResponse = apiRequest { api.activitySectionsRefresh(userId) }
//            sectionItems.postValue(activitySectionsResponse.activitySections)
//
//        }
//
//
//    }

//    private fun isFetchNeeded(savedAt: LocalDateTime): Boolean {
//        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            ChronoUnit.DAYS.between(savedAt, LocalDateTime.now()) > MINIMUM_INTERVALY
//        } else {
//            true
//        }
//    }
//
//    private suspend fun getPhotoForJobItemEstimate(filename: String) {
//        try {
//            val photoEstimate = apiRequest { api.getPhotoEstimate(filename) }
//            estimatePhoto.postValue(photoEstimate.photo, filename)
//            ToastUtils().toastLong(activity, "You do not have an active data connection ")
//        } catch (e: ApiException) {
//            ToastUtils().toastLong(activity, e.message)
//        } catch (e: NoInternetException) {
//            ToastUtils().toastLong(activity, e.message)
//            Log.e("NetworkConnection", "No Internet Connection", e)
//        }
//    }
//
//    suspend fun getPhotoForJobItemMeasure(filename: String) {
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
//
//                    for (entities in entitiesArrayList) {
//                        if (!Db.getPrimaryKeyValueDao().checkPrimaryKeyValuesExistTrackRouteId( Util.ByteArrayToStringUUID( entities.trackRouteId )!!)
//                        ) {
//                            val primaryKeyValue = Db.getPrimaryKeyValueDao().getPrimaryKeyValuesFromTrackRouteId(Util.ByteArrayToStringUUID(entities.trackRouteId)!!)
//
//                               if (primaryKeyValue != null) {
//                                   var job : JobDTO? = null
//                                   job?.setJobId(Util.ByteArrayToStringUUID(primaryKeyValue.p_value))
//                                   job?.setDescr(entities.description)
// //                                   val job_Id = DataConversion.toLittleEndian(jobId!!)
//                                   fetchJobList(Util.ByteArrayToStringUUID(primaryKeyValue.p_value)!!)
// //                                   if (!ApproveJobsFragment.jobArrayContains(
// //                                           job.getJobId(),
// //                                           jobList
// //                                       )
// //                                   ) jobList.add(job)
//                               }
//
//
//
//
//                        }
//                    }

//
//    private fun JobItemMeasureDTO.setJobNo(jiNo: String?) {
//        this.jimNo = jiNo
//    }

//    private fun saveWorkFlowsInfo(workFlows: WorkFlowsDTO) {
//        Coroutines.io {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                prefs.savelastSavedAt(LocalDateTime.now().toString())
//            }
//            if (workFlows != null)
//                Db.getWorkflowsDao().insertWorkFlows(workFlows)
//            if (workFlows.workflows != null) {
//                for (workFlow in workFlows.workflows) {
//                    if (!Db.getWorkFlowDao().checkWorkFlowExistsWorkflowID(workFlow.workflowId))
//                        Db.getWorkFlowDao().insertWorkFlow(workFlow)
// //                    Db.getWorkFlowDao().insertWorkFlow(workFlow.dateCreated,workFlow.errorRouteId, workFlow.revNo, workFlow.startRouteId, workFlow.userId,
// //                        workFlow.wfHeaderId, workFlow.workFlowRoute, workFlow.workflowId)
//
//                    if (workFlow.workFlowRoute != null) {
//                        for (workFlowRoute in workFlow.workFlowRoute!!) {  //ArrayList<WorkFlowRouteDTO>()
//                            if (!Db.getWorkFlowRouteDao().checkWorkFlowRouteExists(workFlowRoute.routeId))
// //                                Db.getWorkFlowRouteDao().insertWorkFlowRoutes(
// //                                    workFlowRoute )
//                                Db.getWorkFlowRouteDao().insertWorkFlowRoute(
//                                    workFlowRoute.routeId,
//                                    workFlowRoute.actId,
//                                    workFlowRoute.nextRouteId,
//                                    workFlowRoute.failRouteId,
//                                    workFlowRoute.errorRouteId,
//                                    workFlowRoute.canStart,
//                                    workFlow.workflowId
//                                )
//                        }
//                    }
//                }
//            }
//
//            if (workFlows.activities != null) {
//                for (activity in workFlows.activities) {
//                    Db.getActivityDao().insertActivitys(activity)
// //                    Db.getActivityDao().insertActivity( activity.actId,  activity.actTypeId, activity.approvalId, activity.sContentId,  activity.actName, activity.descr )
//                }
//            }
//
//            if (workFlows.infoClasses != null) {
//                for (infoClass in workFlows.infoClasses) {
//                    Db.getInfoClassDao().insertInfoClasses(infoClass)
// //                    Db.getInfoClassDao().insertInfoClass(infoClass.sLinkId, infoClass.sInfoClassId,  infoClass.wfId)
//                }
//            }
//        }
//    }
//
//

//    private fun saveJobs(job: JobDTO?) {
//        Coroutines.io {
//            if (job != null) {
//
//                if (!Db.getJobDao().checkIfJobExist(job.JobId)) {
//                    job.run {
//                        setJobId(DataConversion.toBigEndian(JobId))
//                        setProjectId(DataConversion.toBigEndian(ProjectId))
//                        if (ContractVoId != null){
//                            setContractVoId(DataConversion.toBigEndian(ContractVoId))
//                        }
//                        setTrackRouteId(DataConversion.toBigEndian(TrackRouteId))
// //                        DueDate.toString()
// //                        StartDate.toString()
// //                        IssueDate = DateToString(IssueDate)
// //                        val gsonBuilder = GsonBuilder()
// //                        gsonBuilder.registerTypeAdapter(Date::class.java, DateDeserializer())
// //                        setDuedate(StringToDate(DueDate.toString()))
// //                        setIssueDate(StringToDate(IssueDate.toString()))
// //                        setStartDate(StringToDate(StartDate.toString()))
// //                        setDateApproval(ApprovalDate)
//
//
//
// //
// //                        ZonedDateTime.parse(DueDate.toString())
// //                        setRoute(route)
//                    }
//                    DataConversion.toBigEndian(job.PerfitemGroupId)
//                    DataConversion.toBigEndian(job.ProjectVoId)
//                    Db.getJobDao().insertOrUpdateJobs(job)
//                }
//
//                if (job.JobSections != null && job.JobSections!!.size != 0) {
//                    for (jobSection in job.JobSections!!) {
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
//                if (job.JobItemEstimates != null && job.JobItemEstimates!!.size != 0) {
//                    for (jobItemEstimate in job.JobItemEstimates!!) {
//                        if (!Db.getJobItemEstimateDao().checkIfJobItemEstimateExist(jobItemEstimate.estimateId)
//                        ) {
//                            jobItemEstimate.setEstimateId(DataConversion.toBigEndian(jobItemEstimate.estimateId))
//                            jobItemEstimate.setJobId(DataConversion.toBigEndian(jobItemEstimate.jobId))
//                            jobItemEstimate.setProjectItemId(
//                                DataConversion.toBigEndian(
//                                    jobItemEstimate.projectItemId
//                                )
//                            )
//                            if(jobItemEstimate.trackRouteId != null )
//                                jobItemEstimate.setTrackRouteId(
//                                    DataConversion.toBigEndian(
//                                        jobItemEstimate.trackRouteId
//                                    )
//                                )else jobItemEstimate.trackRouteId = null
//
//                            jobItemEstimate.setProjectVoId(
//                                DataConversion.toBigEndian(
//                                    jobItemEstimate.projectVoId
//                                )
//                            )
//                            Db.getJobItemEstimateDao().insertJobItemEstimate(jobItemEstimate)
//                            Db.getJobDao().setEstimateActId(jobItemEstimate.actId, job.JobId)
// //                            job.setEstimateActId(jobItemEstimate.actId)
//                            if (jobItemEstimate.jobItemEstimatePhotos != null) {
//                                for (jobItemEstimatePhoto in jobItemEstimate.jobItemEstimatePhotos!!) {
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
//                                        val fileName =   DataConversion.toLittleEndian(jobItemEstimatePhoto.filename)
//                                        getPhotoForJobItemEstimate(jobItemEstimatePhoto.filename)
//                                    }
//                                }
//                            }
//                            if (jobItemEstimate.jobEstimateWorks != null) {
//                                for (jobEstimateWorks in jobItemEstimate.jobEstimateWorks!!) {
//                                    if (!Db.getEstimateWorkDao().checkIfJobEstimateWorksExist(
//                                            jobEstimateWorks.worksId
//                                        )
//                                    ) jobEstimateWorks.setWorksId(
//                                        DataConversion.toBigEndian(
//                                            jobEstimateWorks.worksId
//                                        )
//                                    )
//                                    jobEstimateWorks.setEstimateId(
//                                        DataConversion.toBigEndian(
//                                            jobEstimateWorks.estimateId
//                                        )
//                                    )
//                                    jobEstimateWorks.setTrackRouteId(
//                                        DataConversion.toBigEndian(
//                                            jobEstimateWorks.trackRouteId
//                                        )
//                                    )
//                                    Db.getEstimateWorkDao().insertJobEstimateWorks(
//                                        jobEstimateWorks
//                                    )
//                                    Db.getJobDao()
//                                        .setEstimateWorksActId(jobEstimateWorks.actId, job.JobId)
// //                                    job.setEstimateWorksActId(jobEstimateWorks.actId)
//                                    if (jobEstimateWorks.jobEstimateWorksPhotos != null) {
//                                        for (estimateWorksPhoto in jobEstimateWorks.jobEstimateWorksPhotos!!) {
//                                            if (!Db.getEstimateWorkPhotoDao().checkIfEstimateWorksPhotoExist(
//                                                    estimateWorksPhoto.filename
//                                                )
//                                            ) estimateWorksPhoto.setWorksId(
//                                                DataConversion.toBigEndian(
//                                                    estimateWorksPhoto.worksId
//                                                )
//                                            )
//                                            estimateWorksPhoto.setPhotoId(
//                                                DataConversion.toBigEndian(
//                                                    estimateWorksPhoto.photoId
//                                                )
//                                            )
//                                            Db.getEstimateWorkPhotoDao().insertEstimateWorksPhoto(
//                                                estimateWorksPhoto
//                                            )
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//
//                if (job.JobItemMeasures != null) {
//                    for (jobItemMeasure in job.JobItemMeasures!!) {
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
//                            jobItemMeasure.setJobNo(job.JiNo)
//                            Db.getJobItemMeasureDao().insertJobItemMeasure(jobItemMeasure)
//                            Db.getJobDao().setMeasureActId(jobItemMeasure.actId, job.JobId)
// //                            job.setMeasureActId(jobItemMeasure.actId)
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
// //                                    else {
// //                                        populateAppropriateViewForPhotos()
// //                                    }
// //                                    if (!PhotoUtil.photoExist(jobItemEstimatePhoto.filename)) {
// //                                        getPhotoForJobItemEstimate(jobItemEstimatePhoto.filename)
// //                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//
//            }
//        }
//    }
//
//    private fun saveSectionsItems(sections: ArrayList<String>?) {
//        Coroutines.io {
//
//            for (section in sections!!) {
//                //  Lets get the String
//                val pattern = Pattern.compile("(.*?):")
//                val matcher = pattern.matcher(section)
//
//                val sectionItemId = SqlLitUtils.generateUuid()
//                val activitySection = section
//                if (matcher.find()) {
//                    if (section != null) {
//                        val itemCode = matcher.group(1).replace("\\s+".toRegex(), "")
//                        if (!Db.getSectionItemDao().checkIfSectionitemsExist(itemCode))
//                            Db.getSectionItemDao().insertSectionitem(
//                                activitySection!!,
//                                itemCode!!,
//                                sectionItemId!!
//                            )
//
//                    }
//
//                }
//
//            }
//
//        }
//    }

//    suspend fun getJobMeasureItemsPhotoPath(itemMeasureId: String): String {
//        return withContext(Dispatchers.IO) {
//            offlineDataRepository.getJobMeasureItemsPhotoPath(itemMeasureId)
//        }
//    }

//
//    private suspend fun fetchContracts(userId: String) {
//        val lastSavedAt = prefs.getLastSavedAt()
//        if (lastSavedAt == null || isFetchNeeded(LocalDateTime.parse(lastSavedAt))) {
//            val activitySectionsResponse = apiRequest { api.ActivitySectionsRefresh(userId) }
//            sectionItems.postValue(activitySectionsResponse.activitySections)
//
//            val myResponse = apiRequest { api.refreshContractInfo(userId) }
//            conTracts.postValue(myResponse.offlinedata)
//        }
//    }
//
//    private fun isFetchNeeded(savedAt: LocalDateTime): Boolean {
//        return ChronoUnit.MINUTES.between(savedAt, LocalDateTime.now()) > MINIMUM_INTERVAL
//    }
//
}
