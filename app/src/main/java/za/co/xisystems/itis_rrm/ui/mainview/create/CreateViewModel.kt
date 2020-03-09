package za.co.xisystems.itis_rrm.ui.mainview.create

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


    val jobtoEdit_Item = MutableLiveData<JobDTO>()
    fun Item5(jobEdit_Item: JobDTO) {
        jobtoEdit_Item.value = jobEdit_Item
    }

    val EstimateQty = MutableLiveData<Int>()
    fun Estimate(Qty: Int) {
        EstimateQty.value = Qty
    }

    val costLineRate = MutableLiveData<String>()
    fun lineRate(rate: String) {
        costLineRate.value = rate
    }

    val offlinedata by lazyDeferred {
        jobCreationDataRepository.getSectionItems()
    }


    val sectionId = MutableLiveData<String>()
    fun sectionId(section_Id: String) {
        sectionId.value = section_Id
    }


    val user by lazyDeferred {
        jobCreationDataRepository.getUser()
    }

    val loggedUser = MutableLiveData<Int>()
    fun userN(user_n: Int) {
        loggedUser.value = user_n
    }

    val descriptioN = MutableLiveData<String>()
    fun userN(desc: String) {
        descriptioN.value = desc
    }

    val newjob = MutableLiveData<JobDTO>()
    fun userN(job: JobDTO) {
        newjob.value = job
    }


    val contract_No = MutableLiveData<String>()
    fun contractNmbr(contract_Nmbr: String) {
        contract_No.value = contract_Nmbr
    }

    val contract_ID = MutableLiveData<String>()
    fun contractIdd(contract_Id: String) {
        contract_ID.value = contract_Id
    }

    val project_ID = MutableLiveData<String>()
    fun contractI(project_Id: String) {
        project_ID.value = project_Id
    }

    val project_Code = MutableLiveData<String>()
    fun projecCode(projec_Code: String) {
        project_Code.value = projec_Code
    }

    val projectSec_Item = MutableLiveData<ProjectItemDTO>()
    fun projecI(projec_Item: ProjectItemDTO) {
        projectSec_Item.value = projec_Item
    }

    val Sec_Item = MutableLiveData<SectionProj_Item>()
    fun projecItem(projec_Item: SectionProj_Item) {
        Sec_Item.value = projec_Item
    }

    val job_Item = MutableLiveData<JobDTO>()
    fun projecIte(job_Ite: JobDTO) {
        job_Item.value = job_Ite
    }

    val project_Item = MutableLiveData<ItemDTOTemp>()
    fun projecItem(projec_Item: ItemDTOTemp) {
        project_Item.value = projec_Item
    }

    val project_Rate = MutableLiveData<Double>()
    fun projecRate(projec_Rate: Double) {
        project_Rate.value = projec_Rate
    }

//    val project_Rate = MutableLiveData<Double>()
//    fun projecRate(projec_Rate: Double) {
//        project_Rate.value = projec_Rate
//    }


    val proId = MutableLiveData<String>()
    suspend fun getProject(projectId: String) {
        return withContext(Dispatchers.IO) {
            proId.value = projectId
        }
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
        val contrId = contractId
        return withContext(Dispatchers.IO) {
            jobCreationDataRepository.getContractProjects(contrId)
        }
    }

    suspend fun getAllSectionItem(): LiveData<List<SectionItemDTO>> {
        return withContext(Dispatchers.IO) {
            jobCreationDataRepository.getAllSectionItem()
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


    suspend fun saveNewItem(newjItem: ItemDTOTemp) {
        jobCreationDataRepository.saveNewItem(newjItem)
    }

    suspend fun delete(item: ItemDTOTemp) {
        jobCreationDataRepository.delete(item)
    }


    suspend fun deleJobfromList(jobId: String) {
        jobCreationDataRepository.deleJobfromList(jobId)
    }

    suspend fun updateNewJob(
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
    ): LiveData<String> {
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
    ) {
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

    suspend fun getAllProjecItems(projectId: String, jobId: String): LiveData<List<ItemDTOTemp>> {
        return withContext(Dispatchers.IO) {
            jobCreationDataRepository.getAllProjecItems(projectId, jobId)
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
//                !isValid
                false

            }
        }
       return withContext(Dispatchers.IO) {
           isValid
           true
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

    suspend fun deleteItemList(jobId: String) {
        jobCreationDataRepository.deleteItemList(jobId)
    }

    suspend fun deleteItemfromList(itemId: String) {
        jobCreationDataRepository.deleteItemfromList(itemId)
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


