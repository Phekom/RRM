package za.co.xisystems.itis_rrm.ui.mainview.approvemeasure

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemMeasureDTO
import za.co.xisystems.itis_rrm.data.repositories.OfflineDataRepository
import za.co.xisystems.itis_rrm.ui.mainview.approvemeasure.approveMeasure_Item.ApproveMeasure_Item
import za.co.xisystems.itis_rrm.utils.lazyDeferred

/**
 * Created by Francis Mahlava on 03,October,2019
 */
class ApproveMeasureViewModel (
    private val offlineDataRepository: OfflineDataRepository
) : ViewModel() {

//    val offlinedata by lazyDeferred {
//        offlineDataRepository.getSectionItems()
//        offlineDataRepository.getContracts()
//    }

    val user by lazyDeferred {
        offlineDataRepository.getUser()
    }


    val measureapproval_Item = MutableLiveData<ApproveMeasure_Item>()
    fun Item5(measureapproval: ApproveMeasure_Item) {
        measureapproval_Item.value = measureapproval
    }


    suspend fun getJobApproveMeasureForActivityId(activityId: Int): LiveData<List<JobItemMeasureDTO>> {
        return withContext(Dispatchers.IO) {
            offlineDataRepository.getJobApproveMeasureForActivityId(activityId)
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

    suspend fun getJobMeasureItemsForJobId(jobID: String?,actId: Int): LiveData<List<JobItemMeasureDTO>> {
        return withContext(Dispatchers.IO) {
            offlineDataRepository.getJobMeasureItemsForJobId(jobID, actId)
        }
    }

    suspend fun getUOMForProjectItemId(projectItemId: String): String {
        return withContext(Dispatchers.IO) {
            offlineDataRepository.getUOMForProjectItemId(projectItemId)
        }
    }

    suspend fun getJobMeasureItemsPhotoPath(itemMeasureId: String): String {
        return withContext(Dispatchers.IO) {
            offlineDataRepository.getJobMeasureItemsPhotoPath(itemMeasureId)
        }
    }

    suspend fun processWorkflowMove( userId: String, trackRounteId: String, description: String?, direction: Int ) {
        return withContext(Dispatchers.IO) {
            offlineDataRepository.processWorkflowMove( userId ,trackRounteId, description, direction)
        }
    }


}