package za.co.xisystems.itis_rrm.ui.mainview.unsubmitted

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.ProjectSectionDTO
import za.co.xisystems.itis_rrm.data.repositories.OfflineDataRepository

class UnSubmittedViewModel(
    private val offlineDataRepository: OfflineDataRepository
) : ViewModel() {


//    val offlinedata by lazyDeferred {
//        offlineDataRepository.getSectionItems()
//        offlineDataRepository.getContracts()
//    }

    val jobtoEdit_Item = MutableLiveData<JobDTO>()
    fun Item5(jobEdit_Item: JobDTO) {
        jobtoEdit_Item.value = jobEdit_Item
    }


    suspend fun getJobsForActivityId(activityId: Int): LiveData<List<JobDTO>> {
        return withContext(Dispatchers.IO) {
            offlineDataRepository.getJobsForActId(activityId)
        }
    }

    suspend fun getDescForProjectId(projectId: String): String {
        return withContext(Dispatchers.IO) {
            offlineDataRepository.getProjectDescription(projectId)
        }
    }

    suspend fun getProjectSectionIdForJobId(jobId: String): String {
        return withContext(Dispatchers.IO) {
            offlineDataRepository.getProjectSectionIdForJobId(jobId)
        }
    }
    suspend fun getRouteForProjectSectionId(sectionId: String): String {
        return withContext(Dispatchers.IO) {
            offlineDataRepository.getRouteForProjectSectionId(sectionId)
        }
    }
    suspend fun getSectionForProjectSectionId(sectionId: String): String {
        return withContext(Dispatchers.IO) {
            offlineDataRepository.getSectionForProjectSectionId(sectionId)
        }
    }

    fun deleJobfromList(jobId: String) {
        offlineDataRepository.deleteJobFromList(jobId)
    }

    fun deleteItemList(jobId: String) {
        offlineDataRepository.deleteItemList(jobId)
    }

   suspend fun getProjectSection(sectionId: String?): LiveData<ProjectSectionDTO> {
        return withContext(Dispatchers.IO) {
            offlineDataRepository.getProjectSection(sectionId)
        }
    }


}