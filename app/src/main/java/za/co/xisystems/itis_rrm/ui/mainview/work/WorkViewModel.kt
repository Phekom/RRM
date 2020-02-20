package za.co.xisystems.itis_rrm.ui.mainview.work

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import za.co.xisystems.itis_rrm.data.localDB.entities.*
import za.co.xisystems.itis_rrm.data.repositories.WorkDataRepository
import za.co.xisystems.itis_rrm.utils.lazyDeferred

class WorkViewModel(
private val workDataRepository: WorkDataRepository
) : ViewModel() {


//    val getWokrCodes by lazyDeferred {
//        offlineDataRepository.getWokrCodes()
////        offlineDataRepository.getContracts()
//    }

val user by lazyDeferred {
    workDataRepository.getUser()
}

    val work_Item = MutableLiveData<JobItemEstimateDTO>()
    fun Item5(work: JobItemEstimateDTO) {
        work_Item.value = work
    }

    val work_ItemJob = MutableLiveData<JobDTO>()
    fun Item5(workjob: JobDTO) {
        work_ItemJob.value = workjob
    }

    suspend fun getJobsForActivityId(activityId1: Int, activityId2: Int): LiveData<List<JobDTO>> {
        return withContext(Dispatchers.IO) {
            workDataRepository.getJobsForActivityIds1(activityId1,activityId2)
        }
    }

    suspend fun getJobEstimationItemsForJobId(jobID: String?, estimateIncomplete: Int): LiveData<List<JobItemEstimateDTO>> {
        return withContext(Dispatchers.IO) {
            workDataRepository.getJobEstimationItemsForJobId(jobID)
        }
    }

    suspend fun getDescForProjectItemId(projectItemId: String): String {
        return withContext(Dispatchers.IO) {
            workDataRepository.getProjectItemDescription(projectItemId)
        }
    }

    suspend fun getItemDescription(jobId: String): String {
        return withContext(Dispatchers.IO) {
            workDataRepository.getItemDescription(jobId)
        }
    }
    suspend fun getItemJobNo(jobId: String): String {
        return withContext(Dispatchers.IO) {
            workDataRepository.getItemJobNo(jobId)
        }
    }

    suspend fun getItemStartKm(jobId: String): Double {
        return withContext(Dispatchers.IO) {
            workDataRepository.getItemStartKm(jobId)
        }
    }

    suspend fun getItemEndKm(jobId: String): Double {
        return withContext(Dispatchers.IO) {
            workDataRepository.getItemEndKm(jobId)
        }
    }

    suspend fun getItemTrackRouteId(jobId: String): String {
        return withContext(Dispatchers.IO) {
            workDataRepository.getItemTrackRouteId(jobId)
        }
    }

    suspend fun getProjectSectionIdForJobId(jobId: String): String {
        return withContext(Dispatchers.IO) {
            workDataRepository.getProjectSectionIdForJobId(jobId)
        }
    }

    suspend fun getRouteForProjectSectionId(sectionId: String): String {
        return withContext(Dispatchers.IO) {
            workDataRepository.getRouteForProjectSectionId(sectionId)
        }
    }
    suspend fun getSectionForProjectSectionId(sectionId: String): String {
        return withContext(Dispatchers.IO) {
            workDataRepository.getSectionForProjectSectionId(sectionId)
        }
    }




    suspend fun getJobEstiItemForEstimateId(estimateId: String?): LiveData<List<JobEstimateWorksDTO>> {
        return withContext(Dispatchers.IO) {
            workDataRepository.getJobEstiItemForEstimateId(estimateId)
        }
    }
    suspend fun getWokrCodes(eId: Int): LiveData<List<WF_WorkStepDTO>> {
        return withContext(Dispatchers.IO) {
            workDataRepository.getWokrCodes(eId)
        }
    }



    suspend fun createSaveWorksPhotos(
        estimateWorksPhoto: ArrayList<JobEstimateWorksPhotoDTO>,
        estimat: JobItemEstimateDTO,
        itemEstiWorks: JobEstimateWorksDTO
    ) {
        return withContext(Dispatchers.IO) {
            workDataRepository.createEstimateWorksPhoto(estimateWorksPhoto,estimat,itemEstiWorks)
        }
    }


    suspend fun submitWorks(
        itemEstiWorks: JobEstimateWorksDTO,
        activity: FragmentActivity,
        itemEstiJob: JobDTO

    ) : String{
        return withContext(Dispatchers.IO) {
            workDataRepository.submitWorks( itemEstiWorks, activity, itemEstiJob)
        }

    }

    suspend fun getJobItemEstimateForEstimateId(estimateId: String): LiveData<JobItemEstimateDTO> {
        return withContext(Dispatchers.IO) {
            workDataRepository.getJobItemEstimateForEstimateId(estimateId)
        }

    }

    suspend fun processWorkflowMove( userId: String, trackRounteId: String, description: String?, direction: Int ) : String {
        return withContext(Dispatchers.IO) {
            workDataRepository.processWorkflowMove( userId ,trackRounteId, description, direction)
        }
    }



    suspend fun getJobItemsEstimatesDoneForJobId(
        jobId: String?,
        estimateWorkPartComplete: Int,
        estWorksComplete: Int
    ): Int {
        return withContext(Dispatchers.IO) {
            workDataRepository.getJobItemsEstimatesDoneForJobId( jobId, estimateWorkPartComplete, estWorksComplete)
        }
    }

    suspend fun getWorkItemsForActID(actId: Int): LiveData<List<JobEstimateWorksDTO>> {
        return withContext(Dispatchers.IO) {
            workDataRepository.getWorkItemsForActID( actId)
        }
    }


//    suspend fun getJobsForActivityId(activityId: Int): LiveData<List<JobItemEstimateDTO>> {
//        return withContext(Dispatchers.IO) {
//            offlineDataRepository.getJobsEstimateForActivityId(activityId)
//        }
//    }





//    suspend fun getJobItemsEstimatesDoneForJobId(
//        jobId: String?,
//        estWorksComplete: Int
//    ): LiveData<List<JobItemEstimateDTO>> {
//        return withContext(Dispatchers.IO) {
//            offlineDataRepository.getJobItemsEstimatesDoneForJobId( jobId, estWorksComplete)
//        }
//    }




}