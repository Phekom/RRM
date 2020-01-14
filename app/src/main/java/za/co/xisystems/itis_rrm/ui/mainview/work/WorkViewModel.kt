package za.co.xisystems.itis_rrm.ui.mainview.work

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobEstimateWorksDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimateDTO
import za.co.xisystems.itis_rrm.data.repositories.OfflineDataRepository

class WorkViewModel(
private val offlineDataRepository: OfflineDataRepository
) : ViewModel() {


//    val offlinedata by lazyDeferred {
//        offlineDataRepository.getSectionItems()
//        offlineDataRepository.getContracts()
//    }


    val work_Item = MutableLiveData<String>()
    fun Item5(work: String) {
        work_Item.value = work
    }
    suspend fun getJobsForActivityId(activityId1: Int, activityId2: Int): LiveData<List<JobDTO>> {
        return withContext(Dispatchers.IO) {
            offlineDataRepository.getJobsForActivityIds1(activityId1,activityId2)
        }
    }

    suspend fun getJobsForActivityIds(activityId1: Int): LiveData<List<JobItemEstimateDTO>> {
        return withContext(Dispatchers.IO) {
            offlineDataRepository.getJobsForActivityIds(activityId1)
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
    suspend fun getDescForProjectItemId(projectItemId: String): String {
        return withContext(Dispatchers.IO) {
            offlineDataRepository.getProjectItemDescription(projectItemId)
        }
    }

    suspend fun getJobEstiItemForEstimateId(estimateId: String?): LiveData<List<JobEstimateWorksDTO>> {
        return withContext(Dispatchers.IO) {
            offlineDataRepository.getJobEstiItemForEstimateId(estimateId)
        }
    }
    suspend fun getItemDescription(jobId: String): String {
        return withContext(Dispatchers.IO) {
            offlineDataRepository.getItemDescription(jobId)
        }
    }
    suspend fun getItemJobNo(jobId: String): String {
        return withContext(Dispatchers.IO) {
            offlineDataRepository.getItemJobNo(jobId)
        }
    }

    suspend fun getItemStartKm(jobId: String): Double {
        return withContext(Dispatchers.IO) {
            offlineDataRepository.getItemStartKm(jobId)
        }
    }

    suspend fun getItemEndKm(jobId: String): Double {
        return withContext(Dispatchers.IO) {
            offlineDataRepository.getItemEndKm(jobId)
        }
    }

    suspend fun getItemTrackRouteId(jobId: String): String {
        return withContext(Dispatchers.IO) {
            offlineDataRepository.getItemTrackRouteId(jobId)
        }
    }







}