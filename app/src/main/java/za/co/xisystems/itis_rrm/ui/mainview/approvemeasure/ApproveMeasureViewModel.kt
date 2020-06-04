package za.co.xisystems.itis_rrm.ui.mainview.approvemeasure

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemMeasureDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.ToDoListEntityDTO
import za.co.xisystems.itis_rrm.data.repositories.MeasureApprovalDataRepository
import za.co.xisystems.itis_rrm.data.repositories.OfflineDataRepository
import za.co.xisystems.itis_rrm.ui.mainview.approvemeasure.approveMeasure_Item.ApproveMeasureItem
import za.co.xisystems.itis_rrm.utils.lazyDeferred

/**
 * Created by Francis Mahlava on 03,October,2019
 */
class ApproveMeasureViewModel (
    private val measureApprovalDataRepository: MeasureApprovalDataRepository,
    private val offlineDataRepository: OfflineDataRepository
) : ViewModel() {

    val offlineUserTaskList by lazyDeferred {
        offlineDataRepository.getUserTaskList()
    }

    val user by lazyDeferred {
        measureApprovalDataRepository.getUser()
    }


    val measureapproval_Item = MutableLiveData<ApproveMeasureItem>()
    fun Item5(measureapproval: ApproveMeasureItem) {
        measureapproval_Item.value = measureapproval
    }

    suspend fun getJobApproveMeasureForActivityId(activityId: Int): LiveData<List<JobItemMeasureDTO>> {
        return withContext(Dispatchers.IO) {
            measureApprovalDataRepository.getJobApproveMeasureForActivityId(activityId)
        }
    }
//    suspend fun getJobApproveMeasureForActivityId(activityId: Int): LiveData<List<JobItemMeasureDTO>> {
//        return withContext(Dispatchers.IO) {
//            offlineDataRepository.getJobApproveMeasureForActivityId(activityId)
//        }
//    }

    suspend fun getJobsMeasureForActivityId(
        estimateComplete: Int,
        measureComplete: Int,
        estWorksComplete: Int,
        jobApproved: Int
    ): LiveData<List<JobDTO>> {
        return withContext(Dispatchers.IO) {
            measureApprovalDataRepository.getJobsMeasureForActivityId(estimateComplete,measureComplete,estWorksComplete,jobApproved)
        }
    }

    suspend fun getEntitiesListForActivityId(activityId: Int): LiveData<List<ToDoListEntityDTO>> {
        return withContext(Dispatchers.IO) {
            measureApprovalDataRepository.getEntitiesListForActivityId(activityId)
        }
    }

    suspend fun getProjectSectionIdForJobId(jobId: String): String {
        return withContext(Dispatchers.IO) {
            measureApprovalDataRepository.getProjectSectionIdForJobId(jobId)
        }
    }

    suspend fun getRouteForProjectSectionId(sectionId: String): String {
        return withContext(Dispatchers.IO) {
            measureApprovalDataRepository.getRouteForProjectSectionId(sectionId)
        }
    }
    suspend fun getSectionForProjectSectionId(sectionId: String): String {
        return withContext(Dispatchers.IO) {
            measureApprovalDataRepository.getSectionForProjectSectionId(sectionId)
        }
    }

    suspend fun getItemDesc(jobId: String): String {
        return withContext(Dispatchers.IO) {
            measureApprovalDataRepository.getItemDescription(jobId)
        }
    }
    suspend fun getDescForProjectId(projectItemId: String): String {
        return withContext(Dispatchers.IO) {
            measureApprovalDataRepository.getProjectItemDescription(projectItemId)
        }
    }

    suspend fun getJobMeasureItemsForJobId(jobID: String?,actId: Int): LiveData<List<JobItemMeasureDTO>> {
        return withContext(Dispatchers.IO) {
            measureApprovalDataRepository.getJobMeasureItemsForJobId(jobID, actId)
        }
    }

    suspend fun getUOMForProjectItemId(projectItemId: String): String {
        return withContext(Dispatchers.IO) {
            measureApprovalDataRepository.getUOMForProjectItemId(projectItemId)
        }
    }

    suspend fun getJobMeasureItemsPhotoPath(itemMeasureId: String): String {
        return withContext(Dispatchers.IO) {
            measureApprovalDataRepository.getJobMeasureItemsPhotoPath(itemMeasureId)
        }
    }

    suspend fun processWorkflowMove( userId: String, trackRounteId: String, description: String?, direction: Int ) : String {
        return withContext(Dispatchers.IO) {
            measureApprovalDataRepository.processWorkflowMove( userId ,trackRounteId, description, direction)
        }
    }

//    suspend fun getJobFromJobId(jobId: String): JobDTO {
//        return withContext(Dispatchers.IO) {
//            offlineDataRepository.getUpdatedJob(jobId)
//        }
//    }

    suspend fun upDateMeasure(
        new_quantity: String,
        itemMeasureId: String?
    ): String {
        return withContext(Dispatchers.IO) {
            measureApprovalDataRepository.upDateMeasure(new_quantity, itemMeasureId!!)
        }
    }


    suspend fun getQuantityForMeasureItemId(itemMeasureId: String): LiveData<Double> {
        return withContext(Dispatchers.IO) {
            measureApprovalDataRepository.getQuantityForMeasureItemId(itemMeasureId)
        }
    }



    suspend fun getLineRateForMeasureItemId(itemMeasureId: String):  LiveData<Double> {
        return withContext(Dispatchers.IO) {
            measureApprovalDataRepository.getLineRateForMeasureItemId(itemMeasureId)
        }
    }


}