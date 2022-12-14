package za.co.xisystems.itis_rrm.ui.mainview.unsubmitted

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.ProjectSectionDTO
import za.co.xisystems.itis_rrm.data.network.responses.PhotoPotholeResponse
import za.co.xisystems.itis_rrm.data.repositories.OfflineDataRepository

class UnSubmittedViewModel(
    private val offlineDataRepository: OfflineDataRepository
) : ViewModel() {

    val jobtoEdit_Item = MutableLiveData<JobDTO>()
    suspend fun getJobToEdit(jobId: String) {
        val editJob = offlineDataRepository.getUpdatedJob(jobId)
        jobtoEdit_Item.postValue(editJob)
    }

    suspend fun getJobsForActivityIds(vararg activityIds: Int): LiveData<List<JobDTO>> {
        return withContext(Dispatchers.IO) {
            offlineDataRepository.getJobsForActId(*activityIds)
        }
    }

    suspend fun getDescForProjectId(projectId: String): String {
        return withContext(Dispatchers.IO) {
            offlineDataRepository.getProjectDescription(projectId)
        }
    }


    suspend fun getPotholePhoto(jobId: String?) : PhotoPotholeResponse {
        return withContext(Dispatchers.IO) {
            offlineDataRepository.getPotholePhoto(jobId!!)
        }
    }

    suspend fun getProjectSectionForId(sectionId: String?): ProjectSectionDTO {
        return withContext(Dispatchers.IO) {
            offlineDataRepository.getProjectSectionForId(sectionId)
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

    fun deleteJobFromList(jobId: String) {
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
