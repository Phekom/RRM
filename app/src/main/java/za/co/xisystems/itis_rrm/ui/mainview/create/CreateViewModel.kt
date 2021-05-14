/**
 * Updated by Shaun McDonald on 2021/05/15
 * Last modified on 2021/05/14, 20:32
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 **/

/**
 * Updated by Shaun McDonald on 2021/05/14
 * Last modified on 2021/05/14, 19:43
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 **/

/**
 * Updated by Shaun McDonald on 2021/05/14
 * Last modified on 2021/05/14, 16:39
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 **/

/**
 * Updated by Shaun McDonald on 2021/05/14
 * Last modified on 2021/05/14, 15:47
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 **/

package za.co.xisystems.itis_rrm.ui.mainview.create

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import java.util.ArrayList
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import za.co.xisystems.itis_rrm.data.localDB.entities.ContractDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.ItemDTOTemp
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimateDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobSectionDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.ProjectDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.ProjectItemDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.ProjectSectionDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.SectionItemDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.SectionPointDTO
import za.co.xisystems.itis_rrm.data.repositories.JobCreationDataRepository
import za.co.xisystems.itis_rrm.domain.ContractSelector
import za.co.xisystems.itis_rrm.domain.ProjectSelector
import za.co.xisystems.itis_rrm.ui.mainview.create.select_item.SectionProj_Item
import za.co.xisystems.itis_rrm.utils.JobUtils
import za.co.xisystems.itis_rrm.utils.lazyDeferred
import za.co.xisystems.itis_rrm.utils.uncaughtExceptionHandler

/**
 * Created by Francis Mahlava on 2019/10/18.
 */

class CreateViewModel(
    private val jobCreationDataRepository: JobCreationDataRepository
) : ViewModel() {

    var jobDesc: String? = null

    private val superJob = SupervisorJob()
    val currentJob: MutableLiveData<JobDTO?> = MutableLiveData()
    private var ioContext: CoroutineContext
    private var mainContext: CoroutineContext
    private val estimateQty = MutableLiveData<Double>()
    val estimateLineRate = MutableLiveData<Double>()
    val sectionId = MutableLiveData<String>()
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
    val sectionProjectItem = MutableLiveData<SectionProj_Item>()
    val jobItem = MutableLiveData<JobDTO?>()
    val projectItemTemp = MutableLiveData<ItemDTOTemp>()
    val jobId: MutableLiveData<String?> = MutableLiveData()

    init {
        ioContext = Job(superJob) + Dispatchers.IO + uncaughtExceptionHandler
        mainContext = Job(superJob) + Dispatchers.Main + uncaughtExceptionHandler
    }

    fun setCurrentJob(inJobItemToEdit: JobDTO?) {
        currentJob.value = inJobItemToEdit
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

    fun setSectionProjectItem(inSectionProjectItem: SectionProj_Item) {
        sectionProjectItem.value = inSectionProjectItem
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
        projectId: String?
    ): String? {
        return withContext(ioContext) {
            jobCreationDataRepository.getSectionByRouteSectionProject(
                sectionId,
                linearId,
                projectId
            )
        }
    }

    suspend fun getSection(sectionId: String): LiveData<ProjectSectionDTO> {
        return withContext(Dispatchers.IO) {
            jobCreationDataRepository.getSection(sectionId)
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

    suspend fun areEstimatesValid(job: JobDTO?, items: ArrayList<Any?>?): Boolean {
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
                    if (!estimate.isEstimateComplete()) {
                        isValid = false
                    }
                }
            }
        }
        return withContext(Dispatchers.IO) {
            isValid
        }
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
}
