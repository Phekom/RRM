package za.co.xisystems.itis_rrm.data.localDB.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import za.co.xisystems.itis_rrm.data.localDB.entities.JobEstimateWorksDTO

/**
 * Created by Francis Mahlava on 2019/11/26.
 */
@Dao
interface EstimateWorkDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJobEstimateWorks( estimates : JobEstimateWorksDTO )

    @Query("SELECT * FROM JOB_ESTIMATE_WORKS WHERE worksId = :worksId")
    fun checkIfJobEstimateWorksExist(worksId: String): Boolean

    @Query("SELECT * FROM JOB_ESTIMATE_WORKS WHERE estimateId = :estimateId")
    fun getJobMeasureItemsForJobId(estimateId: String?): LiveData<List<JobEstimateWorksDTO>>

    @Query("UPDATE JOB_ESTIMATE_WORKS SET estimateId =:estimateId, recordVersion=:recordVersion,recordSynchStateId =:recordSynchStateId, trackRouteId =:trackRouteId, ActId =:actId WHERE worksId = :worksId")
    fun updateJobEstimateWorksWorkflow( worksId: String?, estimateId: String?, recordVersion: Int, recordSynchStateId: Int,  actId: Int, trackRouteId: String? )

    @Query("SELECT * FROM JOB_ESTIMATE_WORKS WHERE estimateId = :estimateId")
    fun checkIfJobEstimateWorksExistForEstimateId(estimateId: String): Boolean

    @Query("SELECT * FROM JOB_ESTIMATE_WORKS WHERE estimateId = :estimateId")
    fun getJobEstimateWorksForEstimateId(estimateId: String?): JobEstimateWorksDTO

    @Query("DELETE FROM JOB_ESTIMATE_WORKS")
    fun deleteAll()






}