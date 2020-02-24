package za.co.xisystems.itis_rrm.ui.mainview.approvejobs

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimateDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimatesPhotoDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.ToDoListEntityDTO
import za.co.xisystems.itis_rrm.data.repositories.JobApprovalDataRepository
import za.co.xisystems.itis_rrm.data.repositories.OfflineDataRepository
import za.co.xisystems.itis_rrm.ui.mainview.approvejobs.approve_job_item.ApproveJob_Item
import za.co.xisystems.itis_rrm.utils.lazyDeferred

/**
 * Created by Francis Mahlava on 03,October,2019
 */
class ApproveJobsViewModel (
    private val jobApprovalDataRepository: JobApprovalDataRepository,
    private val offlineDataRepository: OfflineDataRepository
) : ViewModel() {

    val user by lazyDeferred {
        jobApprovalDataRepository.getUser()
    }

    val offlinedatas by lazyDeferred {
        offlineDataRepository.getUserTaskList()
    }

    suspend fun getUOMForProjectItemId(projectItemId: String): String {
        return withContext(Dispatchers.IO) {
            jobApprovalDataRepository.getUOMForProjectItemId(projectItemId)
        }
    }

    suspend fun getProjectSectionIdForJobId(jobId: String): String {
        return withContext(Dispatchers.IO) {
            jobApprovalDataRepository.getProjectSectionIdForJobId(jobId)
        }
    }
    suspend fun getRouteForProjectSectionId(sectionId: String): String {
        return withContext(Dispatchers.IO) {
            jobApprovalDataRepository.getRouteForProjectSectionId(sectionId)
        }
    }
    suspend fun getSectionForProjectSectionId(sectionId: String): String {
        return withContext(Dispatchers.IO) {
            jobApprovalDataRepository.getSectionForProjectSectionId(sectionId)
        }
    }

    val jobapproval_Item6 = MutableLiveData<ApproveJob_Item>()
    fun Itemss(jobapproval6: ApproveJob_Item) {
        jobapproval_Item6.value = jobapproval6
    }

    suspend fun processWorkflowMove( userId: String, trackRounteId: String, description: String?, direction: Int ) : String {
        return withContext(Dispatchers.IO) {
            jobApprovalDataRepository.processWorkflowMove( userId ,trackRounteId, description, direction)
        }
    }

    suspend fun getJobsForActivityId(activityId: Int): LiveData<List<JobDTO>> {
        return withContext(Dispatchers.IO) {
            jobApprovalDataRepository.getJobsForActivityId(
                activityId
//                , measureComplete,
//                estWorksComplete,
//                jobApproved
            )
        }
    }

    suspend fun getDescForProjectId(projectId: String): String {
        return withContext(Dispatchers.IO) {
            jobApprovalDataRepository.getProjectDescription(projectId)
        }
    }

    suspend fun getJobEstimationItemsForJobId(jobID: String?): LiveData<List<JobItemEstimateDTO>> {
        return withContext(Dispatchers.IO) {
            jobApprovalDataRepository.getJobEstimationItemsForJobId(jobID)
        }
    }


    suspend fun getJobEstimationItemsPhotoStartPath(estimateId: String): String {
        return withContext(Dispatchers.IO) {
            jobApprovalDataRepository.getJobEstimationItemsPhotoStartPath(estimateId)
        }
    }
    suspend fun getJobEstimationItemsPhotoEndPath(estimateId: String): String {
        return withContext(Dispatchers.IO) {
            jobApprovalDataRepository.getJobEstimationItemsPhotoEndPath(estimateId)
        }
    }

    suspend fun getDescForProjectItemId(projectItemId: String): String {
        return withContext(Dispatchers.IO) {
            jobApprovalDataRepository.getProjectItemDescription(projectItemId)
        }
    }

















//
//    suspend fun getEntitiesListForActivityId(activityId: Int): LiveData<List<ToDoListEntityDTO>> {
//        return withContext(Dispatchers.IO) {
//            jobApprovalDataRepository.getEntitiesListForActivityId(activityId)
//        }
//    }
//
//
//
//    suspend fun getJobEstimationItemsPhoto(estimateId: String):  LiveData<List<JobItemEstimatesPhotoDTO>> {
//        return withContext(Dispatchers.IO) {
//            jobApprovalDataRepository.getJobEstimationItemsPhoto(estimateId)
//        }
//    }
//

//    suspend fun getMessages() : String {
//        return withContext(Dispatchers.IO) {
//            offlineDataRepository.getMessages()
//        }
//    }

//    val jobapproval_Item1 = MutableLiveData<String>()
//    fun Item1(jobapproval: String) {
//        jobapproval_Item1.value = jobapproval
//    }
//    val jobapproval_Item2 = MutableLiveData<String>()
//    fun Item2(jobapproval2: String) {
//        jobapproval_Item2.value = jobapproval2
//    }
//    val jobapproval_Item3 = MutableLiveData<String>()
//    fun Item3(jobapproval3: String) {
//        jobapproval_Item3.value = jobapproval3
//    }
//    val jobapproval_Item4 = MutableLiveData<String>()
//    fun Item4(jobapproval4: String) {
//        jobapproval_Item4.value = jobapproval4
//    }
//    val jobapproval_Item5 = MutableLiveData<String>()
//    fun Item5(jobapproval5: String) {
//        jobapproval_Item5.value = jobapproval5
//    }


}