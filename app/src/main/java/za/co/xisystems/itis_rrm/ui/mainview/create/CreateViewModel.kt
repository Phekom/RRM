/**
 * Updated by Shaun McDonald on 2021/05/18
 * Last modified on 2021/05/18, 10:09
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 **/

package za.co.xisystems.itis_rrm.ui.mainview.create

import android.app.Application
import android.net.Uri
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.*
import kotlinx.coroutines.*
import timber.log.Timber
import za.co.xisystems.itis_rrm.data.localDB.entities.*
import za.co.xisystems.itis_rrm.data.repositories.JobCreationDataRepository
import za.co.xisystems.itis_rrm.data.repositories.UserRepository
import za.co.xisystems.itis_rrm.domain.ContractSelector
import za.co.xisystems.itis_rrm.domain.ProjectSelector
import za.co.xisystems.itis_rrm.services.LocationModel
import za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils.models.PhotoType
import za.co.xisystems.itis_rrm.ui.mainview.create.select_item.SectionProjectItem
import za.co.xisystems.itis_rrm.utils.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.coroutines.CoroutineContext

/**
 * Created by Francis Mahlava on 2019/10/18.
 */

class CreateViewModel(
    private val jobCreationDataRepository: JobCreationDataRepository,
    private val userRepository: UserRepository,
    application: Application
) : AndroidViewModel(application) {

    var jobDesc: String? = null

    private val superJob = SupervisorJob()
    var currentJob: MutableLiveData<JobDTO> = MutableLiveData()
    private var ioContext: CoroutineContext = Job(superJob) + Dispatchers.IO + uncaughtExceptionHandler
    private var mainContext: CoroutineContext = Job(superJob) + Dispatchers.Main + uncaughtExceptionHandler
    val estimateQty = MutableLiveData<Double>()
    val estimateLineRate = MutableLiveData<Double>()
    val sectionId: MutableLiveData<String> = MutableLiveData()
    val user by lazyDeferred {
        jobCreationDataRepository.getUser()
    }
    val loggedUser = MutableLiveData<Int>()
    val description: MutableLiveData<String> = MutableLiveData()
    val newJob: MutableLiveData<JobDTO?> = MutableLiveData()
    val contractNo = MutableLiveData<String>()
    val contractId = MutableLiveData<String>()
    val projectId = MutableLiveData<String>()
    val projectCode = MutableLiveData<String>()
    val sectionProjectItem: MutableLiveData<SectionProjectItem> = MutableLiveData()
    var jobItem = MutableLiveData<JobDTO?>()
    var projectItemTemp: MutableLiveData<ItemDTOTemp> = MutableLiveData()
    val jobId: MutableLiveData<String?> = MutableLiveData()
    var tempProjectItem: MutableLiveData<ItemDTOTemp> = MutableLiveData()
    val photoUtil = PhotoUtil.getInstance(getApplication())
    var currentEstimate: MutableLiveData<JobItemEstimateDTO> = MutableLiveData()
    val currentImageUri: MutableLiveData<Uri> = MutableLiveData()

    fun setCurrentJob(inJobItemToEdit: JobDTO) {
        currentJob.value = inJobItemToEdit
    }

    val currentUser by lazyDeferred {
        userRepository.getUser().distinctUntilChanged()
    }

    fun setEstimateQuantity(inQty: Double) {
        estimateQty.value = inQty
    }

    fun setSectionId(inSectionId: String) {
        sectionId.value = inSectionId
    }

    fun setLoggerUser(inLoggedUser: Int) {
        loggedUser.value = inLoggedUser
    }

    fun setDescription(desc: String) {
        description.value = desc
    }

    fun setContractorNo(inContractNo: String) {
        contractNo.value = inContractNo
    }

    fun setContractId(inContractId: String) {
        contractId.value = inContractId
    }

    fun setProjectId(inProjectId: String) {
        projectId.value = inProjectId
    }

    fun setProjectCode(inProjectCode: String) {
        projectCode.value = inProjectCode
    }

    fun setTempProjectItem(inSectionProjectItem: ItemDTOTemp) {
        tempProjectItem.value = inSectionProjectItem
    }

    suspend fun getJob(inJobId: String) {
        jobItem.value = jobCreationDataRepository.getUpdatedJob(jobId = inJobId)
    }

    fun saveNewJob(newJob: JobDTO) {
        jobCreationDataRepository.saveNewJob(newJob)
    }

    suspend fun getContracts(): LiveData<List<ContractDTO>> {
        return withContext(Dispatchers.IO) {
            jobCreationDataRepository.getSectionItems()
            jobCreationDataRepository.getContracts()
        }
    }

    suspend fun getSomeProjects(contractId: String): LiveData<List<ProjectDTO>> {
        return withContext(Dispatchers.IO) {
            jobCreationDataRepository.getContractProjects(contractId)
        }
    }

    suspend fun getAllItemsForSectionItemByProjectId(
        sectionItemId: String,
        projectId: String
    ): LiveData<List<ProjectItemDTO>> {
        return withContext(Dispatchers.IO) {
            jobCreationDataRepository.getAllItemsForSectionItemByProject(sectionItemId, projectId)
        }
    }

    suspend fun getSectionItemsForProject(projectId: String): LiveData<List<SectionItemDTO>> {
        return withContext(Dispatchers.IO) {
            jobCreationDataRepository.getAllSectionItemsForProject(projectId)
        }
    }

    suspend fun saveNewItem(tempItem: ItemDTOTemp) {
        return withContext(Dispatchers.IO) {
            jobCreationDataRepository.saveNewItem(tempItem)
        }
    }

    fun deleteJobFromList(jobId: String) {
        jobCreationDataRepository.deleteJobfromList(jobId)
    }

    suspend fun updateNewJob(
        newJobId: String,
        startKM: Double,
        endKM: Double,
        sectionId: String,
        newJobItemEstimatesList: ArrayList<JobItemEstimateDTO>,
        jobItemSectionArrayList: ArrayList<JobSectionDTO>
    ) {
        withContext(ioContext) {
            jobCreationDataRepository.updateNewJob(
                newJobId,
                startKM,
                endKM,
                sectionId,
                newJobItemEstimatesList,
                jobItemSectionArrayList
            )
        }
    }

    suspend fun getPointSectionData(projectId: String): SectionPointDTO {
        return withContext(ioContext) {
            jobCreationDataRepository.getPointSectionData(projectId)
        }
    }

    suspend fun getSectionByRouteSectionProject(
        sectionId: String,
        linearId: String?,
        projectId: String?,
        pointLocation: Double
    ): String? {
        return withContext(ioContext) {
            jobCreationDataRepository.getSectionByRouteSectionProject(
                sectionId,
                linearId,
                projectId,
                pointLocation
            )
        }
    }

    suspend fun getSection(sectionId: String): LiveData<ProjectSectionDTO> {
        return withContext(Dispatchers.IO) {
            jobCreationDataRepository.getLiveSection(sectionId)
        }
    }

    suspend fun getRouteSectionPoint(
        latitude: Double,
        longitude: Double,
        useR: String,
        projectId: String?,
        jobId: String
    ): String? {
        return withContext(Dispatchers.IO) {
            jobCreationDataRepository.getRouteSectionPoint(
                latitude,
                longitude,
                useR,
                projectId,
                jobId
            )
        }
    }

    suspend fun getAllProjectItems(projectId: String, jobId: String): LiveData<List<ItemDTOTemp>> {
        return withContext(Dispatchers.IO) {
            jobCreationDataRepository.getAllProjectItems(projectId, jobId)
        }
    }

    suspend fun areEstimatesValid(job: JobDTO?, items: ArrayList<Any?>?): Boolean = withContext(Dispatchers.IO) {
        var isValid = true
        when {
            !JobUtils.areQuantitiesValid(job) -> {
                isValid = false
            }
            job == null || items == null || job.jobItemEstimates.isNullOrEmpty()
                || items.size != job.jobItemEstimates.size -> {
                isValid = false
            }
            else -> {
                job.jobItemEstimates.forEach { estimate ->
                    if (!isEstimateComplete(estimate)) {
                        isValid = false
                    }
                }
            }
        }
        return@withContext isValid
    }

    suspend fun submitJob(
        userId: Int,
        job: JobDTO,
        activity: FragmentActivity
    ): String {
        return withContext(ioContext) {
            jobCreationDataRepository.submitJob(userId, job, activity)
        }
    }

    fun deleteItemList(jobId: String) {
        jobCreationDataRepository.deleteItemList(jobId)
    }

    fun deleteItemFromList(itemId: String, estimateId: String?) = viewModelScope.launch(ioContext) {
        val recordsAffected = jobCreationDataRepository.deleteItemFromList(itemId, estimateId)
        Timber.d("deleteItemFromList: $recordsAffected deleted.")
    }

    suspend fun getContractNoForId(contractVoId: String?): String {
        return withContext(Dispatchers.IO) {
            jobCreationDataRepository.getContractNoForId(contractVoId)
        }
    }

    suspend fun getProjectCodeForId(projectId: String?): String {
        return withContext(Dispatchers.IO) {
            jobCreationDataRepository.getProjectCodeForId(projectId)
        }
    }

    suspend fun backupJob(job: JobDTO) = viewModelScope.launch(ioContext) {
        jobCreationDataRepository.backupJob(job)
        withContext(mainContext) {
            setJobToEdit(job.jobId)
        }
    }

    /**
     * This method will be called when this ViewModel is no longer used and will be destroyed.
     *
     *
     * It is useful when ViewModel observes some data and you need to clear this subscription to
     * prevent a leak of this ViewModel.
     */
    override fun onCleared() {
        super.onCleared()
        superJob.cancelChildren()
    }

    fun setJobToEdit(jobId: String) = viewModelScope.launch(ioContext) {
        val fetchedJob = jobCreationDataRepository.getUpdatedJob(jobId)
        withContext(mainContext) {
            currentJob.value = fetchedJob
        }
    }

    suspend fun checkIfJobSectionExists(jobId: String?, projectSectionId: String?): Boolean {
        return withContext(Dispatchers.IO) {
            jobCreationDataRepository.checkIfJobSectionExistForJobAndProjectSection(jobId, projectSectionId)
        }
    }

    suspend fun getContractSelectors(): LiveData<List<ContractSelector>> = liveData {
        withContext(ioContext) {
            val data = jobCreationDataRepository.getContractSelectors()
            withContext(mainContext) {
                emit(data)
            }
        }
    }

    suspend fun getProjectSelectors(contractId: String): LiveData<List<ProjectSelector>> = liveData {
        withContext(ioContext) {
            val data = jobCreationDataRepository.getProjectSelectors(contractId)
            withContext(mainContext) {
                emit(data)
            }
        }
    }

    suspend fun isEstimateComplete(estimate: JobItemEstimateDTO): Boolean = withContext(Dispatchers.IO) {
        return@withContext if (estimate.size() < 2) {
            false
        } else {
            val photoStart = estimate.jobItemEstimatePhotos[0]
            val photoEnd = estimate.jobItemEstimatePhotos[1]
            photoUtil.photoExist(photoStart.filename) && photoUtil.photoExist(photoEnd.filename)
        }
    }

    suspend fun estimateComplete(newJobItemEstimate: JobItemEstimateDTO?): Boolean {
        return newJobItemEstimate?.let { isEstimateComplete(it) } ?: false
    }

    suspend fun getRealSectionStartKm(
        projectSectionDTO: ProjectSectionDTO,
        pointLocation: Double
    ) = jobCreationDataRepository.findRealSectionStartKm(projectSectionDTO, pointLocation).pointLocation

    suspend fun getRealSectionEndKm(
        projectSectionDTO: ProjectSectionDTO,
        pointLocation: Double
    ) = jobCreationDataRepository.findRealSectionEndKm(projectSectionDTO, pointLocation).pointLocation

    suspend fun setCurrentProjectItem(itemId: String?) = viewModelScope.launch(ioContext) {
        val projectItem = jobCreationDataRepository.getProjectItemById(itemId)
        withContext(mainContext) {
            projectItem?.let { setTempProjectItem(it) }
        }
    }

    suspend fun setEstimateToEdit(estimateId: String) = viewModelScope.launch(ioContext) {
        val estimateItem = jobCreationDataRepository.getEstimateById(estimateId)
        withContext(mainContext) {
            currentEstimate.value = estimateItem
        }
    }

    suspend fun createItemEstimatePhoto(
        itemEst: JobItemEstimateDTO,
        filePath: Map<String, String>,
        currentLocation: LocationModel?,
        itemIdPhotoType: Map<String, String>,
        pointLocation: Double
    ) = withContext(ioContext) {

        val isPhotoStart = itemIdPhotoType["type"] == PhotoType.START.name
        val photoId: String = SqlLitUtils.generateUuid()

        // newJobItemEstimatesPhotosList.add(newEstimatePhoto)
        return@withContext JobItemEstimatesPhotoDTO(
            descr = "",
            estimateId = itemEst.estimateId,
            filename = filePath["filename"] ?: error(""),
            photoDate = DateUtil.dateToString(Date())!!,
            photoId = photoId,
            photoStart = null,
            photoEnd = null,
            startKm = pointLocation,
            endKm = pointLocation,
            photoLatitude = currentLocation!!.latitude,
            photoLongitude = currentLocation.longitude,
            photoLatitudeEnd = currentLocation.latitude,
            photoLongitudeEnd = currentLocation.longitude,
            photoPath = filePath["path"] ?: error(""),
            recordSynchStateId = 0,
            recordVersion = 0,
            isPhotostart = isPhotoStart,
            sectionMarker = currentLocation.toString()
        )
    }

    suspend fun backupEstimate(estimate: JobItemEstimateDTO) = withContext(ioContext) {
        val data = jobCreationDataRepository.backupEstimate(estimate)
        withContext(mainContext) {
            currentEstimate.value = data
        }
    }

    fun createItemEstimate(
        itemId: String,
        jobId: String,
    ): Job = viewModelScope.launch(ioContext) {
        val estimateId = SqlLitUtils.generateUuid()
        val newJob = jobCreationDataRepository.getUpdatedJob(jobId)
        val item = jobCreationDataRepository.getProjectItemById(itemId)
        // newJobItemEstimatesList.add(newEstimate)
        val estimate = JobItemEstimateDTO(
            actId = 0,
            estimateId = estimateId,
            jobId = newJob.jobId,
            lineRate = item!!.tenderRate,
            jobEstimateWorks = arrayListOf(),
            jobItemEstimatePhotos = arrayListOf(),
            jobItemMeasure = arrayListOf(),
            // newJobItemPhotosList,
            // jobItemMeasure = null,
            // job = null,
            projectItemId = itemId,
            projectVoId = newJob.projectVoId,
            qty = 1.0,
            recordSynchStateId = 0,
            recordVersion = 0,
            trackRouteId = null,
            jobItemEstimatePhotoStart = null,
            jobItemEstimatePhotoEnd = null,
            estimateComplete = null,
            measureActId = 0,
            selectedItemUom = item.uom
        )
        backupEstimate(estimate)
        newJob.jobItemEstimates.add(estimate)
        backupJob(newJob)
        withContext(mainContext) {
            setJobToEdit(newJob.jobId)
            setEstimateToEdit(estimate.estimateId)
        }
    }

    fun updateEstimateCosting(estimateId: String, qty: Double, tenderRate: Double) = viewModelScope.launch(ioContext) {
        val updatedEstimate = currentEstimate.value?.copy(
            qty = qty, lineRate = tenderRate
        )
        updatedEstimate?.let {
            backupEstimate(it)
            withContext(mainContext) {
                setEstimateToEdit(it.estimateId)
            }
        }
    }

    fun generatePhotoUri() = viewModelScope.launch(ioContext) {
        val newPhotoUri = photoUtil.getUri()
        withContext(mainContext) {
            currentImageUri.postValue(newPhotoUri)
        }
    }

    suspend fun addPhotoToJobEstimate(
        estimateId: String,
        itemIdPhotoType: Map<String, String>,
        photoFilePath: Map<String, String>?,
        estimateLocation: LocationModel
    ): JobItemEstimatesPhotoDTO = withContext(Dispatchers.IO) {
        val jobEstimate = jobCreationDataRepository.getEstimateById(estimateId)
        val photo = createItemEstimatePhoto(
            itemEst = jobEstimate,
            filePath = photoFilePath!!,
            currentLocation = estimateLocation,
            itemIdPhotoType = itemIdPhotoType,
            pointLocation = -1.0
        )
        jobEstimate.setJobItemEstimatePhoto(photo)
        jobCreationDataRepository.backupEstimate(jobEstimate)
        return@withContext photo
    }

    fun updateEstimatePhotos(
        estimateId: String,
        estimatePhotos: ArrayList<JobItemEstimatesPhotoDTO?>
    ) = viewModelScope.launch(mainContext) {
        val estimate = jobCreationDataRepository.getEstimateById(estimateId)
        val newPhotos = estimatePhotos.mapNotNull { photo ->
            photo
        } as ArrayList<JobItemEstimatesPhotoDTO>

        estimate.jobItemEstimatePhotos.clear()

        estimate.jobItemEstimatePhotos.addAll(JobUtils.sort(newPhotos) ?: ArrayList())

        val updatedEstimate = jobCreationDataRepository.backupEstimate(estimate)

        val currentJob = jobCreationDataRepository.getUpdatedJob(estimate.jobId!!)
        val existingEstimateIndex =
            currentJob.jobItemEstimates.indexOfFirst { existing ->
                existing.estimateId == updatedEstimate.estimateId
            }
        if (existingEstimateIndex != -1) {
            currentJob.jobItemEstimates[existingEstimateIndex] = updatedEstimate
        } else {
            currentJob.jobItemEstimates.add(updatedEstimate)
        }
        jobCreationDataRepository.backupJob(currentJob)
        withContext(mainContext) {
            setJobToEdit(currentJob.jobId)
            setCurrentProjectItem(estimate.projectItemId)
            setEstimateToEdit(estimate.estimateId)
        }
    }

    fun unbindEstimateView() {
        currentJob = MutableLiveData()
        currentEstimate = MutableLiveData()
        projectItemTemp = MutableLiveData()
    }

    suspend fun backupProjectItem(item: ItemDTOTemp): Long = withContext(Dispatchers.IO) {
        return@withContext jobCreationDataRepository.backupProjectItem(item)
    }

    val jobForSubmission: MutableLiveData<JobDTO> = MutableLiveData()

    suspend fun setJobForSubmission(inJob: JobDTO) = withContext(mainContext) {
        jobForSubmission.value = inJob
    }
}
