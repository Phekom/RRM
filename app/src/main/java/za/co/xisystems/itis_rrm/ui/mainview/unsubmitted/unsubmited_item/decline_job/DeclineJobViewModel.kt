package za.co.xisystems.itis_rrm.ui.mainview.unsubmitted.unsubmited_item.decline_job

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDeclineDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.ProjectSectionDTO
import za.co.xisystems.itis_rrm.data.repositories.OfflineDataRepository
import za.co.xisystems.itis_rrm.utils.PhotoUtil

class DeclineJobViewModel(
    private val offlineDataRepository: OfflineDataRepository,
    val photoUtil: PhotoUtil,
) : ViewModel() {

    val jobtoEdit_Item = MutableLiveData<JobDTO>()
    suspend fun getJobToEdit(jobId: String) {
        val editJob = offlineDataRepository.getUpdatedJob(jobId)
        jobtoEdit_Item.postValue(editJob)
    }

    suspend fun getJobforId(jobId: String?): JobDTO {
        return withContext(Dispatchers.IO) {
            offlineDataRepository.getUpdatedJob(jobId!!)
        }
    }

    suspend fun submitForDecline(declinedata: JobDeclineDTO?) : String {
        return withContext(Dispatchers.IO) {
            offlineDataRepository.submitForDecline(declinedata)
        }
    }


    suspend fun getDescForProjectId(projectId: String): String {
        return withContext(Dispatchers.IO) {
            offlineDataRepository.getProjectDescription(projectId)
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
