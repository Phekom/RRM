package za.co.xisystems.itis_rrm.ui.models

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import za.co.xisystems.itis_rrm.data.localDB.entities.*
import za.co.xisystems.itis_rrm.data.repositories.JobCreationDataRepository
import za.co.xisystems.itis_rrm.ui.mainview.create.select_item.SectionProj_Item
import za.co.xisystems.itis_rrm.utils.JobUtils
import za.co.xisystems.itis_rrm.utils.lazyDeferred
import java.util.*

/**
 * Created by Francis Mahlava on 2019/10/18.
 */

class CreateViewModel(
    private val jobCreationDataRepository: JobCreationDataRepository
) : ViewModel() {

    // TODO: Create Call to create a new job item


    val jobToEditItem = MutableLiveData<JobDTO>()
    fun setJobToEditItem(inJobItemToEdit: JobDTO) {
        jobToEditItem.value = inJobItemToEdit
    }

    val estimateQty = MutableLiveData<Double>()
    fun setEstimateQuantity(inQty: Double) {
        estimateQty.value = inQty
    }

    val estimateLineRate = MutableLiveData<Double>()
    fun setEstimateLineRate(inRate: Double) {
        estimateLineRate.value = inRate
    }

    val offlineSectionItems by lazyDeferred {
        jobCreationDataRepository.getSectionItems()
    }


    val sectionId = MutableLiveData<String>()
    fun setSectionId(inSectionId: String) {
        sectionId.value = inSectionId
    }


    val user by lazyDeferred {
        jobCreationDataRepository.getUser()
    }

    val loggedUser = MutableLiveData<Int>()
    fun setLoggerUser(inLoggedUser: Int) {
        loggedUser.value = inLoggedUser
    }

    val description = MutableLiveData<String>()
    fun setDescription(desc: String) {
        description.value = desc
    }

    val newJob = MutableLiveData<JobDTO>()
    fun createNewJob(job: JobDTO) {
        newJob.value = job
    }


    val contractNo = MutableLiveData<String>()
    fun setContractorNo(inContractNo: String) {
        contractNo.value = inContractNo
    }

    val contractId = MutableLiveData<String>()
    fun setContractId(inContractId: String) {
        contractId.value = inContractId
    }

    val projectId = MutableLiveData<String>()
    fun setProjectId(inProjectId: String) {
        projectId.value = inProjectId
    }

    val projectCode = MutableLiveData<String>()
    fun setProjectCode(inProjectCode: String) {
        projectCode.value = inProjectCode
    }

    private val projectItem = MutableLiveData<ProjectItemDTO>()
    fun setProjectItem(inProjectItem: ProjectItemDTO) {
        projectItem.value = inProjectItem
    }

    val sectionProjectItem = MutableLiveData<SectionProj_Item>()
    fun setSectionProjectItem(inSectionProjectItem: SectionProj_Item) {
        sectionProjectItem.value = inSectionProjectItem
    }

    val jobItem = MutableLiveData<JobDTO>()
    suspend fun getJob(inJobId: String) {
        jobItem.value = jobCreationDataRepository.getUpdatedJob(jobId = inJobId)
    }

    val projectItemTemp = MutableLiveData<ItemDTOTemp>()
    fun setProjectItemTemp(inProjectItemTemp: ItemDTOTemp) {
        projectItemTemp.value = inProjectItemTemp
    }

    private val projectRate = MutableLiveData<Double>()
    fun setProjectRate(inProjectRate: Double) {
        projectRate.value = inProjectRate
    }

    suspend fun saveNewJob(newjob: JobDTO) {
        jobCreationDataRepository.saveNewJob(newjob)
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

    suspend fun getAllSectionItem(): LiveData<List<SectionItemDTO>> {
        return withContext(Dispatchers.IO) {
            jobCreationDataRepository.getAllSectionItems()
        }
    }

    suspend fun getAllItemsForSectionItem(
        sectionItemId: String,
        projectId: String
    ): LiveData<List<ProjectItemDTO>> {
        return withContext(Dispatchers.IO) {
            jobCreationDataRepository.getAllItemsForSectionItem(sectionItemId, projectId)
        }
    }

    suspend fun getSectionItemsForProject(projectId: String): LiveData<List<SectionItemDTO>> {
        return withContext(Dispatchers.IO) {
            jobCreationDataRepository.getAllSectionItemsForProject(projectId)
        }
    }

    // TODO: Should return some sort of status
    suspend fun saveNewItem(newjItem: ItemDTOTemp) {
        return withContext(Dispatchers.IO) {
            jobCreationDataRepository.saveNewItem(newjItem)
        }
    }

    fun deleteItemTemp(item: ItemDTOTemp) {
        jobCreationDataRepository.delete(item)
    }


    fun deleteJobFromList(jobId: String) {
        jobCreationDataRepository.deleteJobfromList(jobId)
    }

    suspend fun getJobSectionForJobId(jobId: String): JobSectionDTO? {
        return withContext(Dispatchers.IO) {
            jobCreationDataRepository.getJobSection(jobId)
        }
    }


    fun updateNewJob(
        newjobId: String,
        startKM: Double,
        endKM: Double,
        sectionId: String,
        newJobItemEstimatesList: ArrayList<JobItemEstimateDTO>,
        jobItemSectionArrayList: ArrayList<JobSectionDTO>
    ) {

        jobCreationDataRepository.updateNewJob(
            newjobId,
            startKM,
            endKM,
            sectionId,
            newJobItemEstimatesList,
            jobItemSectionArrayList
        )
    }

    suspend fun getPointSectionData(projectId: String?): LiveData<SectionPointDTO> {//jobId: String,jobId,


        return withContext(Dispatchers.IO) {
            jobCreationDataRepository.getPointSectionData(projectId)
        }
    }

    suspend fun getSectionByRouteSectionProject(
        sectionId: Int,
        linearId: String?,
        projectId: String?
    ): LiveData<String?> {
        return withContext(Dispatchers.IO) {
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
        jobId: String,
        itemCode: ItemDTOTemp?
    ): LiveData<String?> {

        return withContext(Dispatchers.IO) {
            jobCreationDataRepository.getRouteSectionPoint(
                latitude,
                longitude,
                useR,
                projectId,
                jobId,
                itemCode
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
        if (!JobUtils.areQuantitiesValid(job)) {
            isValid = false
        } else if (job == null || items == null || job.JobItemEstimates == null || items.size != job.JobItemEstimates!!.size) {
            isValid = false
        } else {
            for (estimate in job.JobItemEstimates!!) {
                if (!estimate.isEstimateComplete()) {
                    isValid = false
                    break
                }
            }
        }
        if (!isValid) {
            return withContext(Dispatchers.IO) {
                false
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
        return withContext(Dispatchers.IO) {
            jobCreationDataRepository.submitJob(userId, job, activity)

        }

    }

    fun deleteItemList(jobId: String) {
        jobCreationDataRepository.deleteItemList(jobId)
    }

    fun deleteItemfromList(itemId: String) {
        jobCreationDataRepository.deleteItemFromList(itemId)
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

}


