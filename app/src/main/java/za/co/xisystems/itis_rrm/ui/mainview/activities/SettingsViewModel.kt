package za.co.xisystems.itis_rrm.ui.mainview.activities

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemMeasureDTO
import za.co.xisystems.itis_rrm.data.repositories.OfflineDataRepository
import za.co.xisystems.itis_rrm.utils.lazyDeferred

/**
 * Created by Francis Mahlava on 03,October,2019
 */
class SettingsViewModel(
    private val offlineDataRepository: OfflineDataRepository
) : ViewModel() {

    val user by lazyDeferred {
        offlineDataRepository.getUser()
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

    suspend fun getItemDesc(jobId: String): String {
        return withContext(Dispatchers.IO) {
            offlineDataRepository.getItemDescription(jobId)
        }
    }

    suspend fun getDescForProjectId(projectItemId: String): String {
        return withContext(Dispatchers.IO) {
            offlineDataRepository.getProjectItemDescription(projectItemId)
        }
    }

    suspend fun getJobMeasureItemsForJobId(
        jobID: String?,
        actId: Int
    ): LiveData<List<JobItemMeasureDTO>> {
        return withContext(Dispatchers.IO) {
            offlineDataRepository.getJobMeasureItemsForJobId(jobID, actId)
        }
    }

    suspend fun getUOMForProjectItemId(projectItemId: String): String {
        return withContext(Dispatchers.IO) {
            offlineDataRepository.getUOMForProjectItemId(projectItemId)
        }
    }

    suspend fun deleteAllData(): Void? {
        return withContext(Dispatchers.IO) {
            offlineDataRepository.deleteAllData()
        }
    }
}
