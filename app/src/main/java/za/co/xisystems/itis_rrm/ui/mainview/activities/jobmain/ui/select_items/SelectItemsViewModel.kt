package za.co.xisystems.itis_rrm.ui.mainview.activities.jobmain.ui.select_items

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.withContext
import za.co.xisystems.itis_rrm.data.localDB.entities.*
import za.co.xisystems.itis_rrm.data.repositories.JobCreationDataRepository
import za.co.xisystems.itis_rrm.data.repositories.UserRepository
import za.co.xisystems.itis_rrm.forge.DefaultDispatcherProvider
import za.co.xisystems.itis_rrm.forge.DispatcherProvider
import za.co.xisystems.itis_rrm.utils.PhotoUtil

class SelectItemsViewModel(
    private val jobCreationDataRepository: JobCreationDataRepository,
    private val userRepository: UserRepository,
    application: Application,
    private val photoUtil: PhotoUtil,
    private val dispatchers: DispatcherProvider = DefaultDispatcherProvider()
) : ViewModel() {

    suspend fun saveNewItem(tempItem: ItemDTOTemp) {
        return withContext(dispatchers.io()) {
            jobCreationDataRepository.saveNewItem(tempItem)
        }
    }


    suspend fun getSectionItemsForProject(projectId: String): LiveData<List<SectionItemDTO>> {
        return withContext(dispatchers.io()) {
            jobCreationDataRepository.getAllSectionItemsForProject(projectId)
        }
    }

    suspend fun getSectionItemsForProject2(contractVoId: String): LiveData<List<SectionItemDTO>> {
        return withContext(dispatchers.io()) {
            jobCreationDataRepository.getAllSectionItemsForProject2(contractVoId)
        }
    }


    suspend fun getAllItemsForSectionItemByProjectId(
        sectionItemId: String,
        projectId: String
    ): LiveData<List<ProjectItemDTO>> {
        return withContext(dispatchers.io()) {
            jobCreationDataRepository.getAllItemsForSectionItemByProject(sectionItemId, projectId)
        }
    }

    suspend fun getAllItemsForSectionItemByContractVoId(
        sectionItemId: String,
        contractVoId: String
    ): LiveData<List<VoItemDTO>> {
        return withContext(dispatchers.io()) {
            jobCreationDataRepository.getAllItemsForSectionItemByContractVoId(sectionItemId, contractVoId)
        }
    }


}


