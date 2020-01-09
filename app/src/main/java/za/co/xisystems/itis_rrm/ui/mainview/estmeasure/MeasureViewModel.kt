package za.co.xisystems.itis_rrm.ui.mainview.estmeasure

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimateDTO
import za.co.xisystems.itis_rrm.data.repositories.OfflineDataRepository

class MeasureViewModel (
    private val offlineDataRepository: OfflineDataRepository
) : ViewModel() {

//    val offlinedata by lazyDeferred {
//        offlineDataRepository.getSectionItems()
//        offlineDataRepository.getContracts()
//    }

    val measure_Item = MutableLiveData<String>()
    fun Item5(measurea: String) {
        measure_Item.value = measurea
    }

    suspend fun getJobMeasureForActivityId(activityId: Int): LiveData<List<JobItemEstimateDTO>> {
        return withContext(Dispatchers.IO) {
            offlineDataRepository.getJobMeasureForActivityId(activityId)
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


}