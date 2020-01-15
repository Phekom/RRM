package za.co.xisystems.itis_rrm.data.localDB.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimateDTO

/**
 * Created by Francis Mahlava on 2019/11/21.
 */


@Dao
interface JobItemEstimateDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJobItemEstimate( jobItemEstimate : JobItemEstimateDTO)

    @Query("SELECT * FROM JOB_ITEM_ESTIMATE WHERE estimateId = :estimateId")
    fun checkIfJobItemEstimateExist(estimateId: String): Boolean

    @Query("SELECT * FROM JOB_ITEM_ESTIMATE WHERE jobId = :jobId")
    fun getJobEstimationItemsForJobId(jobId: String) : LiveData<List<JobItemEstimateDTO>>


    @Query("SELECT * FROM JOB_ITEM_ESTIMATE WHERE actId = :actId  ORDER BY jobId ASC " )
    fun getJobsForActivityId(actId: Int): LiveData<List<JobItemEstimateDTO>>

    @Query("SELECT estimateId FROM JOB_ITEM_ESTIMATE WHERE jobId = :jobId")
    fun getJobEstimateIdForJobId(jobId: String) : String

    @Query("UPDATE JOB_ITEM_ESTIMATE SET TrackRouteId =:trackRouteId, ActId =:actId WHERE estimateId = :estimateId")
    fun updateExistingJobItemEstimateWorkflow(trackRouteId: String?, actId: Int, estimateId: String?)

//    @Query("SELECT * FROM PROJECT_ITEM_TABLE WHERE sectionItemId = :sectionItem AND projectId = :projectId")
//    fun getAllItemsForSectionItem(sectionItem : String, projectId : String ): LiveData<List<ItemDTO>>


//    @Query("DELETE FROM PROJECT_ITEM_TABLE")
//    fun deleteAll()
}