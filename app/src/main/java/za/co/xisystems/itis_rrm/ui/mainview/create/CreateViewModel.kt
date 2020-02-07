package za.co.xisystems.itis_rrm.ui.mainview.create

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import za.co.xisystems.itis_rrm.data.localDB.entities.*
import za.co.xisystems.itis_rrm.data.repositories.OfflineDataRepository
import za.co.xisystems.itis_rrm.ui.mainview.create.select_item.SectionProj_Item
import za.co.xisystems.itis_rrm.utils.lazyDeferred
import java.util.ArrayList

/**
 * Created by Francis Mahlava on 2019/10/18.
 */

class CreateViewModel(
    private val offlineDataRepository: OfflineDataRepository
) : ViewModel() {


//    val offlinedata by lazyDeferred {

//        offlineDataRepository.getAllSectionItem()
//        offlineDataRepository.getVoItems()
//        offlineDataRepository.getProjects()
//        offlineDataRepository.getWorkFlows()=
//        offlineDataRepository.getContracts()

//    }



    val EstimateQty = MutableLiveData<Int>()
    fun Estimate(Qty: Int) {
        EstimateQty.value = Qty
    }

    val costLineRate = MutableLiveData<String>()
    fun lineRate(rate: String) {
        costLineRate.value = rate
    }

    val offlinedata by lazyDeferred {
        offlineDataRepository.getSectionItems()
    }


    val sectionId = MutableLiveData<String>()
    fun sectionId(section_Id: String) {
        sectionId.value = section_Id
    }

    val user by lazyDeferred {
    offlineDataRepository.getUser()
}
//        try {

    suspend fun getContracts(): LiveData<List<ContractDTO>> {
            return withContext(Dispatchers.IO) {
                offlineDataRepository.getSectionItems()
                offlineDataRepository.getContracts()
            }
    }


    suspend fun getItemForItemId(projectItemId: String?): LiveData<ProjectItemDTO>{
        return withContext(Dispatchers.IO) {
            offlineDataRepository.getItemForItemId(projectItemId)
        }
    }

    suspend fun getSomeProjects(contractId: String): LiveData<List<ProjectDTO>> {
        val contrId = contractId
        return withContext(Dispatchers.IO) {
            offlineDataRepository.getContractProjects(contrId)
        }
    }

    suspend fun getJobItemEstimatePhotoForEstimateId(estimateId: String): LiveData<List<JobItemEstimatesPhotoDTO>> {
        return withContext(Dispatchers.IO) {
            offlineDataRepository.getJobItemEstimatePhotoForEstimateId(estimateId)
        }
    }

//    suspend fun getSomeProjects(contractId: String): LiveData<List<ProjectDTO>> {
//        val contrId = contractId
//        return withContext(Dispatchers.IO) {
//            offlineDataRepository.getContractProjects(contrId)
//        }
//    }

    suspend fun getAllItemsForProjectId(projectId: String): LiveData<List<ProjectItemDTO>> {
        return withContext(Dispatchers.IO) {
            offlineDataRepository.getAllItemsForProjectId(projectId)
        }
    }
    suspend fun getAllItemsForSectionItem(sectionItemId: String,projectId: String): LiveData<List<ProjectItemDTO>> {
        return withContext(Dispatchers.IO) {
            offlineDataRepository.getAllItemsForSectionItem(sectionItemId,projectId)
        }
    }

    suspend fun getItemForItemCode(itemCode: String): LiveData<List<ProjectItemDTO>> {
        return withContext(Dispatchers.IO) {
            offlineDataRepository.getItemForItemCode(itemCode)
        }
    }


    suspend  fun getAllProjecItems(projectId: String): LiveData<List<ItemDTOTemp>> {
        return withContext(Dispatchers.IO) {
            offlineDataRepository.getAllProjecItems(projectId)
        }
    }

    suspend fun getAllSectionItem(): LiveData<List<SectionItemDTO>> {
        return withContext(Dispatchers.IO) {
            offlineDataRepository.getAllSectionItem()
        }
    }

    val loggedUser = MutableLiveData<Int>()
    fun userN(user_n: Int) {
        loggedUser.value = user_n
    }

    val descriptioN = MutableLiveData<String>()
    fun userN(desc: String) {
        descriptioN.value = desc
    }

    val newjob = MutableLiveData<JobDTOTemp>()
    fun userN(job : JobDTOTemp) {
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

    val job_Item = MutableLiveData<JobDTOTemp>()
    fun projecIte(job_Ite: JobDTOTemp) {
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
    suspend fun saveNewJob(newjob: JobDTOTemp) {
        offlineDataRepository.saveNewJob(newjob)
    }

    suspend fun deleJobfromList(jobId: String) {
        offlineDataRepository.deleJobfromList(jobId)
    }


    suspend fun updateNewJob(
        newjobId: String,
        startKM: Double,
        endKM: Double,
        sectionId: String,
        newJobItemEstimatesList: ArrayList<JobItemEstimateDTO>,
        jobItemSectionArrayList: ArrayList<JobSectionDTO>
    ) {
        offlineDataRepository.updateNewJob(newjobId,startKM,endKM,sectionId,newJobItemEstimatesList,jobItemSectionArrayList)
    }
    suspend fun saveNewItem(newjItem: ItemDTOTemp) {
        offlineDataRepository.saveNewItem(newjItem)
    }

    suspend fun delete(item: ItemDTOTemp) {
        offlineDataRepository.delete(item)
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
            offlineDataRepository.getRouteSectionPoint( latitude,longitude,useR, projectId, jobId, itemCode)
        }

    }

    suspend fun getJobforEstinmate(jobId: String): LiveData<JobDTOTemp> {
        return withContext(Dispatchers.IO) {
            offlineDataRepository.getJobforEstinmate(jobId)
        }
    }

   suspend fun getPointSectionData( projectId: String?): LiveData<SectionPointDTO> {//jobId: String,jobId,
        return withContext(Dispatchers.IO) {
            offlineDataRepository.getPointSectionData( projectId)
        }
    }

    suspend fun getSectionByRouteSectionProject(
        sectionId: Int,
        linearId: String?,
        projectId: String?
    ): LiveData<String> {
        return withContext(Dispatchers.IO) {
            offlineDataRepository.getSectionByRouteSectionProject(sectionId,linearId,projectId)
        }
    }

    suspend fun getSection(sectionId: String): LiveData<ProjectSectionDTO> {
        return withContext(Dispatchers.IO) {
            offlineDataRepository.getSection(sectionId)
        }
    }

    suspend fun submitJob(
        userId: Int,
        job: JobDTOTemp,
        activity: FragmentActivity
    ): String {
        return withContext(Dispatchers.IO) {
            offlineDataRepository.submitJob( userId, job, activity)
        }

    }

    suspend fun deleteItemfromList(itemId: String) {
        offlineDataRepository.deleteItemfromList(itemId)
    }

//        } catch (e: ApiException) {
//            authListener?.onFailure(e.message!!)
//        } catch (e: NoInternetException) {
//            authListener?.onFailure(e.message!!)
//        }
//
//    }


//    val projects by lazyDeferred {
////        offlineDataRepository.getVoItems()
////        offlineDataRepository.getProjects()
//        rListener?.onStarted()
//        offlineDataRepository.getContractProjects("F777EF7070884494A1EB6CFA51B60AA6")
//    }
//    val projectsItems by lazyDeferred {
//        //        offlineDataRepository.getProjects()
////        offlineDataRepository.getProjectItems()
//        rListener?.onStarted()
//        offlineDataRepository.getAllItemsForProjectId("ACBE1CDE0FEA482D9E15767A208E0320")
//
//    }


}