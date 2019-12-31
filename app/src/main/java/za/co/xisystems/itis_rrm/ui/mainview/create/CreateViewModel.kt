package za.co.xisystems.itis_rrm.ui.mainview.create

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import za.co.xisystems.itis_rrm.data.localDB.entities.ContractDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.ItemDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.ProjectDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.SectionItemDTO
import za.co.xisystems.itis_rrm.data.repositories.OfflineDataRepository

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

//        try {

    suspend fun getContracts(): LiveData<List<ContractDTO>> {
            return withContext(Dispatchers.IO) {
                offlineDataRepository.getSectionItems()
                offlineDataRepository.getContracts()
            }
    }




    suspend fun getSomeProjects(contractId: String): LiveData<List<ProjectDTO>> {
        val contrId = contractId
        return withContext(Dispatchers.IO) {
            offlineDataRepository.getContractProjects(contrId)
        }
    }

    suspend fun getAllItemsForProjectId(projectId: String): LiveData<List<ItemDTO>> {
        return withContext(Dispatchers.IO) {
            offlineDataRepository.getAllItemsForProjectId(projectId)
        }
    }
    suspend fun getAllItemsForSectionItem(sectionItemId: String,projectId: String): LiveData<List<ItemDTO>> {
        return withContext(Dispatchers.IO) {
            offlineDataRepository.getAllItemsForSectionItem(sectionItemId,projectId)
        }
    }

    suspend fun getItemForItemCode(itemCode: String): LiveData<List<ItemDTO>> {
        return withContext(Dispatchers.IO) {
            offlineDataRepository.getItemForItemCode(itemCode)
        }
    }

    suspend fun getAllSectionItem(): LiveData<List<SectionItemDTO>> {
        return withContext(Dispatchers.IO) {
            offlineDataRepository.getAllSectionItem()
        }
    }


    val contract_No = MutableLiveData<String>()
    fun contractNmbr(contract_Nmbr: String) {
        contract_No.value = contract_Nmbr
    }

    val project_Code = MutableLiveData<String>()
    fun projecCode(projec_Code: String) {
        project_Code.value = projec_Code
    }
    val project_Item = MutableLiveData<String>()
    fun projecItem(projec_Item: String) {
        project_Item.value = projec_Item
    }

    val proId = MutableLiveData<String>()
    suspend fun getProject(projectId: String) {
        return withContext(Dispatchers.IO) {
            proId.value = projectId
        }
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