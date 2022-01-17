/**
 * Updated by Shaun McDonald on 2021/05/18
 * Last modified on 2021/05/18, 10:09
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 **/

package za.co.xisystems.itis_rrm.ui.mainview.create

import android.app.Application
import android.net.Uri
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import androidx.room.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import za.co.xisystems.itis_rrm.custom.errors.XIErrorHandler
import za.co.xisystems.itis_rrm.custom.events.XIEvent
import za.co.xisystems.itis_rrm.custom.results.XIResult
import za.co.xisystems.itis_rrm.data.localDB.entities.ItemDTOTemp
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimateDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimatesPhotoDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobTypeEntityDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.ProjectItemDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.ProjectSectionDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.SectionItemDTO
import za.co.xisystems.itis_rrm.data.repositories.CapturedPictureRepository
import za.co.xisystems.itis_rrm.data.repositories.JobCreationDataRepository
import za.co.xisystems.itis_rrm.data.repositories.UserRepository
import za.co.xisystems.itis_rrm.domain.ContractSelector
import za.co.xisystems.itis_rrm.domain.ProjectSelector
import za.co.xisystems.itis_rrm.utils.DispatcherProvider
import za.co.xisystems.itis_rrm.services.LocationModel
import za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils.models.PhotoType
import za.co.xisystems.itis_rrm.utils.DateUtil
import za.co.xisystems.itis_rrm.utils.DefaultDispatcherProvider
import za.co.xisystems.itis_rrm.utils.JobItemEstimateSize
import za.co.xisystems.itis_rrm.utils.JobUtils
import za.co.xisystems.itis_rrm.utils.PhotoUtil
import za.co.xisystems.itis_rrm.utils.SqlLitUtils
import za.co.xisystems.itis_rrm.utils.lazyDeferred
import java.util.Date
import kotlin.coroutines.CoroutineContext

/**
 * Created by Francis Mahlava on 2019/10/18.
 */

class CreateViewModel(
    private val capturedPictureRepository: CapturedPictureRepository,
    private val jobCreationDataRepository: JobCreationDataRepository,
    private val userRepository: UserRepository,
    application: Application,
    private val photoUtil: PhotoUtil,
    private val dispatchers: DispatcherProvider = DefaultDispatcherProvider()
) : AndroidViewModel(application) {

    var jobDesc: String? = null
    private val superJob = SupervisorJob()
    var jobToEdit: MutableLiveData<JobDTO> = MutableLiveData()
    private var ioContext: CoroutineContext = Job(superJob) + dispatchers.io()
    private var mainContext: CoroutineContext = Job(superJob) + Dispatchers.Main
    val estimateQty = MutableLiveData<Double>()
    val estimateJbType = MutableLiveData<String>()
    val estimateLineRate = MutableLiveData<Double>()
    val sectionId: MutableLiveData<String> = MutableLiveData()
    val user by lazyDeferred {
        jobCreationDataRepository.getUser()
    }
    val loggedUser = MutableLiveData<Int>()
    val description: MutableLiveData<String> = MutableLiveData()
    val contractNo = MutableLiveData<String>()
    val contractId = MutableLiveData<String>()
    val projectId = MutableLiveData<String>()
    val projectCode = MutableLiveData<String>()
    var itemJob: MutableLiveData<XIEvent<JobDTO>> = MutableLiveData()
    val jobId: MutableLiveData<String?> = MutableLiveData()
    var tempProjectItem: MutableLiveData<XIEvent<ItemDTOTemp>> = MutableLiveData()
    var currentEstimate: MutableLiveData<XIEvent<JobItemEstimateDTO>> = MutableLiveData()
    val currentImageUri: MutableLiveData<XIEvent<Uri>> = MutableLiveData()
    val totalJobCost: MutableLiveData<String> = MutableLiveData()
    val backupSubmissionJob: MutableLiveData<XIEvent<JobDTO>> = MutableLiveData()

    val currentUser by lazyDeferred {
        userRepository.getUser().distinctUntilChanged()
    }

    var jobForSubmission: MutableLiveData<XIEvent<JobDTO>> = MutableLiveData()
    var jobForValidation: MutableLiveData<XIEvent<JobDTO>> = MutableLiveData()
    var jobForReUpload: MutableLiveData<XIEvent<JobDTO>> = MutableLiveData()

    fun setEstimateQuantity(inQty: Double) {
        estimateQty.value = inQty
    }

    fun setEstimateJbType(jbType: String?) {
        estimateJbType.value = jbType!!
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

    fun setTempProjectItem(inSectionProjectItem: ItemDTOTemp) = viewModelScope.launch(mainContext) {
        tempProjectItem.value = XIEvent(inSectionProjectItem)
    }

    suspend fun getJob(inJobId: String) {
        itemJob.value = XIEvent(jobCreationDataRepository.getUpdatedJob(jobId = inJobId))
    }

    fun saveNewJob(newJob: JobDTO) {
        jobCreationDataRepository.saveNewJob(newJob)
    }

    suspend fun getAllItemsForSectionItemByProjectId(
        sectionItemId: String,
        projectId: String
    ): LiveData<List<ProjectItemDTO>> {
        return withContext(dispatchers.io()) {
            jobCreationDataRepository.getAllItemsForSectionItemByProject(sectionItemId, projectId)
        }
    }

    suspend fun getSectionItemsForProject(projectId: String): LiveData<List<SectionItemDTO>> {
        return withContext(dispatchers.io()) {
            jobCreationDataRepository.getAllSectionItemsForProject(projectId)
        }
    }

    suspend fun saveNewItem(tempItem: ItemDTOTemp) {
        return withContext(dispatchers.io()) {
            jobCreationDataRepository.saveNewItem(tempItem)
        }
    }

    fun deleteJobFromList(jobId: String) {
        jobCreationDataRepository.deleteJobfromList(jobId)
    }

    suspend fun getSection(sectionId: String): LiveData<ProjectSectionDTO> {
        return withContext(dispatchers.io()) {
            jobCreationDataRepository.getLiveSection(sectionId)
        }
    }

    suspend fun getAllProjectItems(projectId: String, jobId: String): LiveData<List<ItemDTOTemp>> {
        return withContext(dispatchers.io()) {
            jobCreationDataRepository.getAllProjectItems(projectId, jobId)
        }
    }

    suspend fun areEstimatesValid(job: JobDTO?, items: ArrayList<Any?>?): Boolean = withContext(dispatchers.io()) {
        var isValid = true
        when {
            !JobUtils.areQuantitiesValid(job) -> {
                isValid = false
            }
            job == null || items == null || job.jobItemEstimates.isNullOrEmpty() ||
                items.size != job.jobItemEstimates.size -> {
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
    ): XIResult<Boolean> = withContext(ioContext) {
        try {
            // Initial upload to server
            val workflowJob = jobCreationDataRepository.submitJob(userId, job)
            // Workflow updates, upload images
            val updatedJob = jobCreationDataRepository.postWorkflowJob(workflowJob, job, activity)
            // Final workflow move and local persistence
            val nextWorkflowJob = jobCreationDataRepository.moveJobToNextWorkflow(updatedJob, activity)
            // Persist workflow results
            jobCreationDataRepository.saveWorkflowJob(nextWorkflowJob)
            // Delete tempProjectItems
            jobCreationDataRepository.deleteItemList(updatedJob.jobId)
            return@withContext XIResult.Success(true)
        } catch (ex: Exception) {
            val message = "Failed to submit job - ${ex.message ?: XIErrorHandler.UNKNOWN_ERROR}"
            Timber.e(ex, message)
            return@withContext XIResult.Error(exception = ex, message = message)
        }
    }

    suspend fun reUploadJob(
        job: JobDTO,
        activity: FragmentActivity
    ): XIResult<Boolean> = withContext(ioContext) {
        try {
            val nextWorkflowJob = jobCreationDataRepository.moveJobToNextWorkflow(job, activity)
            // Persist workflow results
            jobCreationDataRepository.saveWorkflowJob(nextWorkflowJob)
            // Delete tempProjectItems
            jobCreationDataRepository.deleteItemList(job.jobId)
            return@withContext XIResult.Success(true)
        } catch (ex: Exception) {
            val message = "Failed to submit job - ${ex.message ?: XIErrorHandler.UNKNOWN_ERROR}"
            Timber.e(ex, message)
            return@withContext XIResult.Error(exception = ex, message = message)
        }
    }

    fun deleteItemList(jobId: String) {
        jobCreationDataRepository.deleteItemList(jobId)
    }

    fun deleteItemFromList(itemId: Long, estimateId: String?) = viewModelScope.launch(ioContext) {
        val recordsAffected = jobCreationDataRepository.deleteItemFromList(itemId, estimateId)
        Timber.d("deleteItemFromList: $recordsAffected deleted.")
    }

    suspend fun getContractNoForId(contractVoId: String?): String {
        return withContext(dispatchers.io()) {
            jobCreationDataRepository.getContractNoForId(contractVoId)
        }
    }

    suspend fun getProjectCodeForId(projectId: String?): String {
        return withContext(dispatchers.io()) {
            jobCreationDataRepository.getProjectCodeForId(projectId)
        }
    }

    suspend fun backupJob(job: JobDTO) = viewModelScope.launch(ioContext) {
        jobCreationDataRepository.backupJob(job)
        withContext(mainContext) {
            jobId.value = job.jobId
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
        Timber.d("^*^ Creation ViewModel Cleared!")
        super.onCleared()
        superJob.cancelChildren()
    }

    fun setJobToEdit(jobId: String) = viewModelScope.launch(ioContext) {
        val fetchedJob = jobCreationDataRepository.getUpdatedJob(jobId)
        withContext(mainContext) {
            totalJobCost.value = JobUtils.formatTotalCost(fetchedJob)
            jobToEdit.value = fetchedJob
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

    fun getProjectSelectors(contractId: String): LiveData<List<ProjectSelector>> = liveData {
        withContext(ioContext) {
            val data = jobCreationDataRepository.getProjectSelectors(contractId)
            withContext(mainContext) {
                emit(data)
            }
        }
    }

    private suspend fun isEstimateComplete(estimate: JobItemEstimateDTO): Boolean = withContext(dispatchers.io()) {
        return@withContext if (
            estimate.jobItemEstimateSize.equals(JobItemEstimateSize.POINT.getValue())
        ) {
            if (estimate.size() < 1) {
                false
            } else {
                val photoStart = estimate.jobItemEstimatePhotos[0]
                photoUtil.photoExist(photoStart.filename)
            }
        } else {
            if (estimate.size() < 2) {
                false
            } else {
                val photoStart = estimate.jobItemEstimatePhotos[0]
                val photoEnd = estimate.jobItemEstimatePhotos[1]
                photoUtil.photoExist(photoStart.filename) && photoUtil.photoExist(photoEnd.filename)
            }
        }
    }

    suspend fun estimateComplete(newJobItemEstimate: JobItemEstimateDTO?): Boolean {
        return newJobItemEstimate?.let { isEstimateComplete(it) } ?: false
    }

    fun setCurrentProjectItem(itemId: String?) = viewModelScope.launch(ioContext) {
        val projectItem = jobCreationDataRepository.getProjectItemById(itemId)
        withContext(mainContext) {
            setTempProjectItem(projectItem)
        }
    }

    fun setEstimateToEdit(estimateId: String) = viewModelScope.launch(ioContext) {
        val estimateItem = jobCreationDataRepository.getEstimateById(estimateId)
        withContext(mainContext) {
            currentEstimate.value = XIEvent(estimateItem)
        }
    }

    fun setEstimateJobType(estimateId: String) = viewModelScope.launch(ioContext) {
        val estimateItem = jobCreationDataRepository.getEstimateById(estimateId)
        withContext(mainContext) {
            currentEstimate.value = XIEvent(estimateItem)
        }
    }

    /**
     * Creates a new estimate photograph,
     * erasing the existing start or end photo as applicable
     *
     * @param itemEst JobItemEstimateDTO
     * @param filePath Map<String, String>
     * @param currentLocation LocationModel
     * @param itemIdPhotoType Map<String, String>
     * @param pointLocation Double
     * @return JobItemEstimatesPhotoDTO
     */

    suspend fun createItemEstimatePhoto(
        itemEst: JobItemEstimateDTO,
        filePath: Map<String, String>,
        currentLocation: LocationModel,
        itemIdPhotoType: Map<String, String>,
        pointLocation: Double
    ): JobItemEstimatesPhotoDTO = withContext(ioContext) {

        val isPhotoStart = itemIdPhotoType["type"] == PhotoType.START.name

        val existingPhotoPair = itemEst.getJobItemEstimatePhoto(isPhotoStart)
        existingPhotoPair.second?.let { existingPhoto ->
            // Delete the existing photo from storage and db
            eraseExistingPhoto(existingPhoto.photoId, existingPhoto.filename, existingPhoto.photoPath)
        }

        val photoId = SqlLitUtils.generateUuid()

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
            photoLatitude = currentLocation.latitude,
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

    /**
     * Back up an estimate in progress
     * @param estimate JobItemEstimateDTO
     */
    suspend fun backupEstimate(estimate: JobItemEstimateDTO) = withContext(ioContext) {
        val data = jobCreationDataRepository.backupEstimate(estimate)
        withContext(mainContext) {
            currentEstimate.value = XIEvent(data)
        }
    }

    suspend fun getJobType(): LiveData<List<JobTypeEntityDTO>> {
        return withContext(dispatchers.io()) {
            jobCreationDataRepository.getStructureTypes()
        }
    }

    suspend fun createItemEstimate(
        itemId: String?,
        newJob: JobDTO?,
        item: ItemDTOTemp?,
        estimateSize: String?

    ): JobItemEstimateDTO = withContext(ioContext) {
        val estimateId = SqlLitUtils.generateUuid()

        // newJobItemEstimatesList.add(newEstimate)
        return@withContext JobItemEstimateDTO(
            actId = 0,
            estimateId = estimateId,
            jobId = newJob?.jobId,
            lineRate = item!!.tenderRate,
            jobItemEstimateSize = estimateSize,
            jobEstimateWorks = arrayListOf(),
            jobItemEstimatePhotos = arrayListOf(),
            jobItemMeasure = arrayListOf(),
            projectItemId = itemId,
            projectVoId = newJob?.projectVoId,
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
    }

    fun generatePhotoUri() = viewModelScope.launch(ioContext) {
        val newPhotoUri = photoUtil.getUri()
        withContext(mainContext) {
            newPhotoUri?.let {
                currentImageUri.value = XIEvent(it)
            }
        }
    }

    suspend fun addPhotoToJobEstimate(
        estimateId: String,
        itemIdPhotoType: Map<String, String>,
        photoFilePath: Map<String, String>?,
        estimateLocation: LocationModel
    ): JobItemEstimatesPhotoDTO = withContext(dispatchers.io()) {
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

    suspend fun updateEstimatePhotos(
        estimateId: String,
        estimatePhotos: java.util.ArrayList<JobItemEstimatesPhotoDTO>
    ) = withContext(ioContext) {
        val estimate = jobCreationDataRepository.getEstimateById(estimateId)
        val newPhotos = estimatePhotos.filter { photo ->
            photoUtil.photoExist(photo.filename)
        } as ArrayList<JobItemEstimatesPhotoDTO>

        estimate.jobItemEstimatePhotos.clear()

        estimate.jobItemEstimatePhotos.addAll(JobUtils.sort(newPhotos) ?: ArrayList())

        return@withContext jobCreationDataRepository.backupEstimate(estimate)
    }

    fun unbindEstimateView() {
        itemJob = MutableLiveData()
        currentEstimate = MutableLiveData()
        tempProjectItem = MutableLiveData()
    }

    suspend fun backupProjectItem(item: ItemDTOTemp): Long = withContext(dispatchers.io()) {
        return@withContext jobCreationDataRepository.backupProjectItem(item)
    }

    fun setJobForSubmission(inJobId: String) = viewModelScope.launch(ioContext) {
        jobCreationDataRepository.getUpdatedJob(inJobId).also {
            withContext(mainContext) {
                jobForSubmission.value = XIEvent(it)
            }
        }
    }

    fun setJobToValidate(geoCodedJobId: String) = viewModelScope.launch(ioContext) {
        jobCreationDataRepository.getUpdatedJob(geoCodedJobId).also {
            withContext(mainContext) {
                jobForValidation.value = XIEvent(it)
            }
        }
    }

    suspend fun backupEstimatePhoto(photo: JobItemEstimatesPhotoDTO) = withContext(ioContext) {
        return@withContext jobCreationDataRepository.backupEstimatePhoto(photo)
    }

    fun setItemJob(jobId: String) = viewModelScope.launch(ioContext) {
        val job = jobCreationDataRepository.getUpdatedJob(jobId)
        withContext(mainContext) {
            itemJob.value = XIEvent(job)
        }
    }

    suspend fun getJobEstimateIndexByItemAndJobId(
        itemId: String,
        jobId: String
    ): JobItemEstimateDTO? = withContext(ioContext) {
        return@withContext jobCreationDataRepository.getJobEstimateIndexByItemAndJobId(itemId, jobId)
    }

    fun setEstimateLineRate(tenderRate: Double) = viewModelScope.launch(mainContext) {
        estimateLineRate.value = tenderRate
    }

    @Transaction
    private fun eraseExistingPhoto(photoId: String, fileName: String, photoPath: String) =
        viewModelScope.launch(ioContext) {
            if (photoUtil.photoExist(fileName)) {
                photoUtil.deleteImageFile(photoPath)
            }
            jobCreationDataRepository.eraseExistingPhoto(photoId)
        }

    fun setJobForReUpload(jobId: String) = viewModelScope.launch(ioContext) {
        val job = jobCreationDataRepository.getUpdatedJob(jobId)
        withContext(mainContext) {
            jobForReUpload.value = XIEvent(job)
        }
    }

    fun resetUploadState() {
        jobForReUpload = MutableLiveData()
    }

    fun resetValidationState() {
        jobForValidation = MutableLiveData()
        jobForSubmission = MutableLiveData()
    }

}
