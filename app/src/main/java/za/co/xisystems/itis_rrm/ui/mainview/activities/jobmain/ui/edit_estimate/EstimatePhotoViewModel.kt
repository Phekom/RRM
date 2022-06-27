package za.co.xisystems.itis_rrm.ui.mainview.activities.jobmain.ui.edit_estimate

import android.app.Application
import android.net.Uri
import androidx.lifecycle.*
import androidx.room.Transaction
import kotlinx.coroutines.*
import timber.log.Timber
import za.co.xisystems.itis_rrm.custom.events.XIEvent
import za.co.xisystems.itis_rrm.data._commons.utils.SqlLitUtils
import za.co.xisystems.itis_rrm.data.localDB.entities.ItemDTOTemp
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimateDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimatesPhotoDTO
import za.co.xisystems.itis_rrm.data.repositories.JobCreationDataRepository
import za.co.xisystems.itis_rrm.data.repositories.UserRepository
import za.co.xisystems.itis_rrm.domain.ContractSelector
import za.co.xisystems.itis_rrm.domain.ProjectSelector
import za.co.xisystems.itis_rrm.forge.DefaultDispatcherProvider
import za.co.xisystems.itis_rrm.forge.DispatcherProvider
import za.co.xisystems.itis_rrm.services.LocationModel
import za.co.xisystems.itis_rrm.ui.mainview.activities.jobmain.new_job_utils.models.PhotoType
import za.co.xisystems.itis_rrm.utils.*

import java.util.*
import kotlin.collections.ArrayList
import kotlin.coroutines.CoroutineContext

class EstimatePhotoViewModel(
    private val jobCreationDataRepository: JobCreationDataRepository,
    private val userRepository: UserRepository,
    application: Application,
    private val photoUtil: PhotoUtil,
    private val dispatchers: DispatcherProvider = DefaultDispatcherProvider()
    ) : ViewModel() {

    private val superJob = SupervisorJob()
    private var ioContext: CoroutineContext = Job(superJob) + dispatchers.io()
    private var mainContext: CoroutineContext = Job(superJob) + Dispatchers.Main
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
    var jobToEdit: MutableLiveData<JobDTO> = MutableLiveData()
    val estimateQty = MutableLiveData<Double>()
    val estimateJbType = MutableLiveData<String>()
    val estimateLineRate = MutableLiveData<Double>()
    val sectionId: MutableLiveData<String> = MutableLiveData()

    val user by lazyDeferred {
        jobCreationDataRepository.getUser()
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

    fun setJobToEdit(jobId: String) = viewModelScope.launch(ioContext) {
        val fetchedJob = jobCreationDataRepository.getUpdatedJob(jobId)
        withContext(mainContext) {
            totalJobCost.value = JobUtils.formatTotalCost(fetchedJob)
            jobToEdit.value = fetchedJob
        }
    }

    suspend fun backupJob(job: JobDTO) = viewModelScope.launch(ioContext) {
        jobCreationDataRepository.backupJob(job)
        withContext(mainContext) {
            jobId.value = job.jobId
            setJobToEdit(job.jobId)
        }
    }

    fun setEstimateToEdit(estimateId: String) = viewModelScope.launch(ioContext) {
        val estimateItem = jobCreationDataRepository.getEstimateById(estimateId)
        withContext(mainContext) {
            currentEstimate.value = XIEvent(estimateItem)
        }
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


    suspend fun backupEstimatePhoto(photo: JobItemEstimatesPhotoDTO) = withContext(ioContext) {
        return@withContext jobCreationDataRepository.backupEstimatePhoto(photo)
    }

    suspend fun backupProjectItem(item: ItemDTOTemp): Long = withContext(dispatchers.io()) {
        return@withContext jobCreationDataRepository.backupProjectItem(item)
    }



    fun deleteItemFromList(itemId: String, estimateId: String?) = viewModelScope.launch(ioContext) {
        val recordsAffected = jobCreationDataRepository.deleteItemFromList(itemId, estimateId)
        Timber.d("deleteItemFromList: $recordsAffected deleted.")
    }

    fun setEstimateQuantity(inQty: Double) {
        estimateQty.value = inQty
    }

    fun setEstimateLineRate(tenderRate: Double) = viewModelScope.launch(mainContext) {
        estimateLineRate.value = tenderRate
    }

    fun setCurrentProjectItem(itemId: String?) = viewModelScope.launch(ioContext) {
        val projectItem = jobCreationDataRepository.getProjectItemById(itemId)
        withContext(mainContext) {
            setTempProjectItem(projectItem)
        }
    }

    fun setTempProjectItem(inSectionProjectItem: ItemDTOTemp) = viewModelScope.launch(mainContext) {
        tempProjectItem.value = XIEvent(inSectionProjectItem)
    }


    suspend fun checkIfPhotoExists(imageFileName: String) = withContext(ioContext) {
        return@withContext jobCreationDataRepository.checkIfPhotoExists(imageFileName)
    }

    suspend fun checkIfPhotoExistsByNameAndEstimateId(imageFileName: String, estimateId: String) = withContext(ioContext) {
        return@withContext jobCreationDataRepository.checkIfPhotoExistsByNameAndEstimateId(imageFileName,estimateId )
    }


    fun setSectionId(inSectionId: String) {
        sectionId.value = inSectionId
    }

    suspend fun getEstimatePhotoByName(imageFileName: String): JobItemEstimatesPhotoDTO? = withContext(ioContext) {
        return@withContext jobCreationDataRepository.getEstimatePhotoByName(imageFileName)
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
            contractVoId = newJob?.contractVoId,
            projectVoId = item?.projectVoId,
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

    suspend fun estimateComplete(newJobItemEstimate: JobItemEstimateDTO?): Boolean {
        return newJobItemEstimate?.let { isEstimateComplete(it) } ?: false
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


    fun saveNewJob(newJob: JobDTO) {
        jobCreationDataRepository.saveNewJob(newJob)
    }


    suspend fun createItemEstimatePhoto2(
        itemEst: JobItemEstimateDTO,
        filePath: Map<String, String>,
        currentLocation: LocationModel,
        itemIdPhotoType: Map<String, String>,
        pointLocation: Double
    ): JobItemEstimatesPhotoDTO = withContext(ioContext) {

        val isPhotoStart = itemIdPhotoType["type"] == PhotoType.START.name

//        val existingPhotoPair = itemEst.getJobItemEstimatePhoto(isPhotoStart)
//        existingPhotoPair.second?.let { existingPhoto ->
//            // Delete the existing photo from storage and db
//            eraseExistingPhoto(existingPhoto.photoId, existingPhoto.filename, existingPhoto.photoPath)
//        }

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

    @Transaction
    private fun eraseExistingPhoto(photoId: String, fileName: String, photoPath: String) =
        viewModelScope.launch(ioContext) {
            if (photoUtil.photoExist(fileName)) {
                photoUtil.deleteImageFile(photoPath)
            }
            jobCreationDataRepository.eraseExistingPhoto(photoId)
        }

    suspend fun getJobForId(jobId: String): JobDTO {
        return withContext(dispatchers.io()) {
            jobCreationDataRepository.getJobForId(jobId)
        }
    }

    suspend fun getItemTempForID(itemId : String): ItemDTOTemp {
        return withContext(dispatchers.io()) {
            jobCreationDataRepository.getItemTempForID(itemId)
        }
    }

    suspend fun getEstimateForId(estimateId : String): JobItemEstimateDTO {
        return withContext(dispatchers.io()) {
            jobCreationDataRepository.getEstimateById(estimateId)
        }
    }


}