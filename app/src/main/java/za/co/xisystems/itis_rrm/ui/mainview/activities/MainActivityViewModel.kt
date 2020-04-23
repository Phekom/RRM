package za.co.xisystems.itis_rrm.ui.mainview.activities

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimateDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemMeasureDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.UserRoleDTO
import za.co.xisystems.itis_rrm.data.repositories.OfflineDataRepository
import za.co.xisystems.itis_rrm.ui.mainview.approvemeasure.approveMeasure_Item.ApproveMeasureItem
import za.co.xisystems.itis_rrm.utils.lazyDeferred

/**
 * Created by Francis Mahlava on 03,October,2019
 */
class MainActivityViewModel (
    private val offlineDataRepository: OfflineDataRepository
) : ViewModel() {

    val offlinedata by lazyDeferred {
        offlineDataRepository.getSectionItems()
        offlineDataRepository.getContracts()
    }

    val user by lazyDeferred {
        offlineDataRepository.getUser()
    }


    val measureapproval_Item = MutableLiveData<ApproveMeasureItem>()
    fun Item5(measureapproval: ApproveMeasureItem) {
        measureapproval_Item.value = measureapproval
    }

    suspend fun getJobsForActivityId2(activityId1: Int, activityId2: Int): LiveData<List<JobDTO>> {
        return withContext(Dispatchers.IO) {
            offlineDataRepository.getJobsForActivityIds1(activityId1,activityId2)
        }
    }

    suspend fun getJobsForActivityId(activityId: Int): LiveData<List<JobDTO>> {
        return withContext(Dispatchers.IO) {
            offlineDataRepository.getJobsForActId(activityId)
        }
    }



    suspend fun getJobMeasureForActivityId(
        activityId: Int,
        activityId2: Int,
        activityId3: Int
    ): LiveData<List<JobItemEstimateDTO>> {
        return withContext(Dispatchers.IO) {
            offlineDataRepository.getJobMeasureForActivityId(activityId, activityId2)
        }
    }
    suspend fun getRoles(): LiveData<List<UserRoleDTO>> {
        return withContext(Dispatchers.IO) {
            offlineDataRepository.getRoles()
        }
    }


    suspend fun getJobApproveMeasureForActivityId(activityId: Int): LiveData<List<JobItemMeasureDTO>> {
        return withContext(Dispatchers.IO) {
            offlineDataRepository.getJobApproveMeasureForActivityId(activityId)
        }
    }



    suspend fun deleteAllData() : Void?{
        return withContext(Dispatchers.IO) {
            offlineDataRepository.deleteAllData()
        }
    }



}