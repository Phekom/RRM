package za.co.xisystems.itis_rrm.ui.mainview.activities.jobmain.ui.add_items

import android.app.Application
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.*
import androidx.room.Transaction
import kotlinx.coroutines.*
import timber.log.Timber
import za.co.xisystems.itis_rrm.custom.errors.XIErrorHandler
import za.co.xisystems.itis_rrm.custom.events.XIEvent
import za.co.xisystems.itis_rrm.custom.results.XIResult
import za.co.xisystems.itis_rrm.data.localDB.entities.ItemDTOTemp
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimateDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimatesPhotoDTO
import za.co.xisystems.itis_rrm.data.repositories.JobCreationDataRepository
import za.co.xisystems.itis_rrm.data.repositories.UserRepository
import za.co.xisystems.itis_rrm.domain.ContractVoSelector
import za.co.xisystems.itis_rrm.forge.DefaultDispatcherProvider
import za.co.xisystems.itis_rrm.forge.DispatcherProvider
import za.co.xisystems.itis_rrm.utils.JobItemEstimateSize
import za.co.xisystems.itis_rrm.utils.JobUtils
import za.co.xisystems.itis_rrm.utils.PhotoUtil
import kotlin.coroutines.CoroutineContext

class AddItemsViewModel(
    private val jobCreationDataRepository: JobCreationDataRepository,
    private val userRepository: UserRepository,
    application: Application,
    private val photoUtil: PhotoUtil,
    private val dispatchers: DispatcherProvider = DefaultDispatcherProvider()
) : ViewModel() {

    private val superJob = SupervisorJob()
    var jobToEdit: MutableLiveData<JobDTO> = MutableLiveData()
    private var ioContext: CoroutineContext = Job(superJob) + dispatchers.io()
    private var mainContext: CoroutineContext = Job(superJob) + Dispatchers.Main
    val backupSubmissionJob: MutableLiveData<XIEvent<JobDTO>> = MutableLiveData()
    var jobForSubmission: MutableLiveData<XIEvent<JobDTO>> = MutableLiveData()
    var jobForValidation: MutableLiveData<XIEvent<JobDTO>> = MutableLiveData()
    var jobForReUpload: MutableLiveData<XIEvent<JobDTO>> = MutableLiveData()
    val totalJobCost: MutableLiveData<String> = MutableLiveData()
    var tempProjectItem: MutableLiveData<XIEvent<ItemDTOTemp>> = MutableLiveData()
    val jobId: MutableLiveData<String?> = MutableLiveData()
    val loggedUser = MutableLiveData<Int>()

    fun setLoggerUser(inLoggedUser: Int) {
        loggedUser.value = inLoggedUser
    }

    suspend fun getJobForId(jobId: String): JobDTO {
        return withContext(dispatchers.io()) {
            jobCreationDataRepository.getJobForId(jobId)
        }
    }


    suspend fun getContractNoForId(contractId: String?): String {
        return withContext(dispatchers.io()) {
            jobCreationDataRepository.getContractNoForId(contractId)
        }
    }



    suspend fun getContractVoData(contractVoId: String): LiveData<List<ContractVoSelector>> = liveData {
        withContext(ioContext) {
            val data = jobCreationDataRepository.getContractVoData(contractVoId)
            withContext(mainContext) {
                emit(data)
            }
        }
    }


    suspend fun getProjectCodeForId(projectId: String?): String {
        return withContext(dispatchers.io()) {
            jobCreationDataRepository.getProjectCodeForId(projectId)
        }
    }

    suspend fun getAllProjectItems(projectId: String, jobId: String): LiveData<List<ItemDTOTemp>> {
        return withContext(dispatchers.io()) {
            jobCreationDataRepository.getAllProjectItems(projectId, jobId)
        }
    }

    suspend fun backupProjectItem(item: ItemDTOTemp): Long = withContext(dispatchers.io()) {
        return@withContext jobCreationDataRepository.backupProjectItem(item)
    }

    suspend fun getJobEstimateIndexByItemAndJobId(
        itemId: String,
        jobId: String
    ): JobItemEstimateDTO? = withContext(ioContext) {
        return@withContext jobCreationDataRepository.getJobEstimateIndexByItemAndJobId(itemId, jobId)
    }

    suspend fun backupJob(job: JobDTO) = viewModelScope.launch(ioContext) {
        jobCreationDataRepository.backupJob(job)
        withContext(mainContext) {
            jobId.value = job.jobId
            setJobToEdit(job.jobId)
        }
    }

    suspend fun getJobEstimationItemsPhotoStart(estimateId: String): JobItemEstimatesPhotoDTO {
        return withContext(dispatchers.io()) {
            jobCreationDataRepository.getJobEstimationItemsPhotoStart(estimateId)
        }
    }

    suspend fun getJobEstimationItemsPhotoStartPath(estimateId: String): String {
        return withContext(dispatchers.io()) {
            jobCreationDataRepository.getJobEstimationItemsPhotoStartPath(estimateId)
        }
    }

    suspend fun getJobItemEstimatePhotosForEstimateId(estimateId: String): List<JobItemEstimatesPhotoDTO> {
        return withContext(dispatchers.io()) {
            jobCreationDataRepository.getJobItemEstimatePhotosForEstimateId(estimateId)
        }
    }

    suspend fun getJobEstimationItemsPhotoEnd(estimateId: String): JobItemEstimatesPhotoDTO {
        return withContext(dispatchers.io()) {
            jobCreationDataRepository.getJobEstimationItemsPhotoEnd(estimateId)
        }
    }

    suspend fun getJobEstimationItemsPhotoEndPath(estimateId: String): String {
        return withContext(dispatchers.io()) {
            jobCreationDataRepository.getJobEstimationItemsPhotoEndPath(estimateId)
        }
    }

    suspend fun estimateComplete(newJobItemEstimate: JobItemEstimateDTO?): Boolean {
        return newJobItemEstimate?.let { isEstimateComplete(it) } ?: false
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

    fun deleteJobFromList(jobId: String) {
        jobCreationDataRepository.deleteJobfromList(jobId)
    }

    fun resetValidationState() {
        jobForValidation = MutableLiveData()
        jobForSubmission = MutableLiveData()
    }

    fun setJobForSubmission(inJobId: String) = viewModelScope.launch(mainContext) {
        jobCreationDataRepository.getUpdatedJob(inJobId).also {
             jobForSubmission.value = XIEvent(it)
        }
    }


    fun setJobToEdit(jobId: String) = viewModelScope.launch(ioContext) {
        val fetchedJob = jobCreationDataRepository.getUpdatedJob(jobId)
        withContext(mainContext) {
            totalJobCost.value = JobUtils.formatTotalCost(fetchedJob)
            jobToEdit.value = fetchedJob
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

    fun setJobToValidate(geoCodedJobId: String) = viewModelScope.launch(mainContext) {
        jobCreationDataRepository.getUpdatedJob(geoCodedJobId).also {
            jobForValidation.value = XIEvent(it)
        }
    }

    fun eraseUsedAndExpiredPhotos(job: JobDTO) {
        job.jobItemEstimates.forEach { estimate ->
            estimate.jobItemEstimatePhotos.forEach { image ->
                eraseUsedAndOldPhotos(image.filename, image.photoPath)
            }
        }
    }

    @Transaction
    private fun eraseUsedAndOldPhotos(fileName: String, photoPath: String) =
        viewModelScope.launch(Dispatchers.IO) {
            if (photoUtil.photoExist(fileName)) {
                photoUtil.deleteImageFile(photoPath)
            }
            jobCreationDataRepository.eraseUsedAndExpiredPhoto(fileName)
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


//    fun deleteItemList(jobId: String) {
//        jobCreationDataRepository.deleteItemList(jobId)
//    }

    fun deleteItemFromList(itemId: String, estimateId: String?) = viewModelScope.launch(ioContext) {
        val recordsAffected = jobCreationDataRepository.deleteItemFromList(itemId, estimateId)
        Timber.d("deleteItemFromList: $recordsAffected deleted.")
    }


}