package za.co.xisystems.itis_rrm.ui.mainview.approvejobs

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimateDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimatesPhotoDTO
import za.co.xisystems.itis_rrm.data.repositories.OfflineDataRepository

/**
 * Created by Francis Mahlava on 03,October,2019
 */
class ApproveJobsViewModel (
    private val offlineDataRepository: OfflineDataRepository
) : ViewModel() {

//    val offlinedata by lazyDeferred {
//        offlineDataRepository.getJobs()
//    }

    suspend fun getJobsForActivityId(activityId: Int): LiveData<List<JobDTO>> {
        return withContext(Dispatchers.IO) {
            offlineDataRepository.getJobsForActivityId(activityId)
        }
    }
    suspend fun getDescForProjectId(projectId: String): String {
        return withContext(Dispatchers.IO) {
            offlineDataRepository.getProjectDescription(projectId)
        }
    }
    suspend fun getDescForProjectItemId(projectItemId: String): String {
        return withContext(Dispatchers.IO) {
            offlineDataRepository.getProjectItemDescription(projectItemId)
        }
    }
    suspend fun getUOMForProjectItemId(projectItemId: String): String {
        return withContext(Dispatchers.IO) {
            offlineDataRepository.getUOMForProjectItemId(projectItemId)
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

    suspend fun getJobEstimationItemsForJobId(jobID: String?): LiveData<List<JobItemEstimateDTO>> {
        return withContext(Dispatchers.IO) {
            offlineDataRepository.getJobEstimationItemsForJobId(jobID)
        }
    }

    suspend fun getJobEstimationItemsPhoto(estimateId: String):  LiveData<List<JobItemEstimatesPhotoDTO>> {
        return withContext(Dispatchers.IO) {
            offlineDataRepository.getJobEstimationItemsPhoto(estimateId)
        }
    }
    suspend fun getJobEstimationItemsPhotoStartPath(estimateId: String): String {
        return withContext(Dispatchers.IO) {
            offlineDataRepository.getJobEstimationItemsPhotoStartPath(estimateId)
        }
    }
    suspend fun getJobEstimationItemsPhotoEndPath(estimateId: String): String {
        return withContext(Dispatchers.IO) {
            offlineDataRepository.getJobEstimationItemsPhotoEndPath(estimateId)
        }
    }


    val jobapproval_Item1 = MutableLiveData<String>()
    fun Item1(jobapproval: String) {
        jobapproval_Item1.value = jobapproval
    }
    val jobapproval_Item2 = MutableLiveData<String>()
    fun Item2(jobapproval2: String) {
        jobapproval_Item2.value = jobapproval2
    }
    val jobapproval_Item3 = MutableLiveData<String>()
    fun Item3(jobapproval3: String) {
        jobapproval_Item3.value = jobapproval3
    }
    val jobapproval_Item4 = MutableLiveData<String>()
    fun Item4(jobapproval4: String) {
        jobapproval_Item4.value = jobapproval4
    }
    val jobapproval_Item5 = MutableLiveData<String>()
    fun Item5(jobapproval5: String) {
        jobapproval_Item5.value = jobapproval5
    }



}