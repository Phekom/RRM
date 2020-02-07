package za.co.xisystems.itis_rrm.ui.mainview.unsubmitted

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTOTemp
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimateDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.ProjectSectionDTO
import za.co.xisystems.itis_rrm.data.repositories.OfflineDataRepository
import za.co.xisystems.itis_rrm.utils.lazyDeferred

class UnSubmittedViewModel(
    private val offlineDataRepository: OfflineDataRepository
) : ViewModel() {


//    val offlinedata by lazyDeferred {
//        offlineDataRepository.getSectionItems()
//        offlineDataRepository.getContracts()
//    }

    suspend fun getJobsForActivityId(activityId: Int): LiveData<List<JobDTOTemp>> {
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

    suspend fun deleJobfromList(jobId: String) {
        offlineDataRepository.deleJobfromList(jobId)
    }

   suspend fun getProjectSection(sectionId: String?): LiveData<ProjectSectionDTO> {
        return withContext(Dispatchers.IO) {
            offlineDataRepository.getProjectSection(sectionId)
        }
    }


}