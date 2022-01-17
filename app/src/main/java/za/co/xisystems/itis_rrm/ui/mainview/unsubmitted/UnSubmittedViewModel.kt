package za.co.xisystems.itis_rrm.ui.mainview.unsubmitted

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.ProjectSectionDTO
import za.co.xisystems.itis_rrm.data.repositories.OfflineDataRepository
import za.co.xisystems.itis_rrm.utils.DefaultDispatcherProvider
import za.co.xisystems.itis_rrm.utils.DispatcherProvider

class UnSubmittedViewModel(
    private val offlineDataRepository: OfflineDataRepository,
    private val dispatchers: DispatcherProvider = DefaultDispatcherProvider()
) : ViewModel() {

    suspend fun getJobsForActivityIds(vararg activityIds: Int): LiveData<List<JobDTO>> {
        return withContext(dispatchers.io()) {
            offlineDataRepository.getJobsForActId(*activityIds)
        }
    }

    suspend fun getDescForProjectId(projectId: String): String {
        return withContext(dispatchers.io()) {
            offlineDataRepository.getProjectDescription(projectId)
        }
    }

    suspend fun getProjectSectionIdForJobId(jobId: String): String {
        return withContext(dispatchers.io()) {
            offlineDataRepository.getProjectSectionIdForJobId(jobId)
        }
    }

    suspend fun getRouteForProjectSectionId(sectionId: String): String {
        return withContext(dispatchers.io()) {
            offlineDataRepository.getRouteForProjectSectionId(sectionId)
        }
    }

    suspend fun getSectionForProjectSectionId(sectionId: String): String {
        return withContext(dispatchers.io()) {
            offlineDataRepository.getSectionForProjectSectionId(sectionId)
        }
    }

    fun deleteJobFromList(jobId: String) {
        offlineDataRepository.deleteJobFromList(jobId)
    }

    fun deleteItemList(jobId: String) {
        offlineDataRepository.deleteItemList(jobId)
    }

    suspend fun getProjectSection(sectionId: String?): LiveData<ProjectSectionDTO> {
        return withContext(dispatchers.io()) {
            offlineDataRepository.getProjectSection(sectionId)
        }
    }
}
