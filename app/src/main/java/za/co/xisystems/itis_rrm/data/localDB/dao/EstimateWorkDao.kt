package za.co.xisystems.itis_rrm.data.localDB.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import za.co.xisystems.itis_rrm.data.localDB.entities.JobEstimateWorksDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobEstimateWorksPhotoDTO

/**
 * Created by Francis Mahlava on 2019/11/26.
 */
@Dao
interface EstimateWorkDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJobEstimateWorks(estimates: JobEstimateWorksDTO)

    @Query("SELECT * FROM JOB_ESTIMATE_WORKS WHERE worksId = :worksId")
    fun checkIfJobEstimateWorksExist(worksId: String): Boolean

    @Query("SELECT * FROM JOB_ESTIMATE_WORKS WHERE estimateId = :estimateId")
    fun getLiveJobEstimateWorksForEstimateId(estimateId: String?): LiveData<JobEstimateWorksDTO>

    @Query(
        "UPDATE JOB_ESTIMATE_WORKS " +
            "SET jobEstimateWorksPhotos =:jobEstimateWorksPhotos " +
            "WHERE estimateId = :estimateId"
    )
    fun updateJobEstimateWorkForEstimateID(
        jobEstimateWorksPhotos: ArrayList<JobEstimateWorksPhotoDTO>,
        estimateId: String?
    )

    @Query(
        "UPDATE JOB_ESTIMATE_WORKS "
            .plus("SET estimateId =:estimateId, recordVersion=:recordVersion,")
            .plus("recordSynchStateId =:recordSynchStateId, ")
            .plus("trackRouteId =:trackRouteId, ")
            .plus("ActId =:actId WHERE worksId = :worksId")
    )
    fun updateJobEstimateWorksWorkflow(
        worksId: String?,
        estimateId: String?,
        recordVersion: Int,
        recordSynchStateId: Int,
        actId: Int,
        trackRouteId: String?
    )

    @Query("SELECT * FROM JOB_ESTIMATE_WORKS WHERE estimateId = :estimateId")
    fun checkIfJobEstimateWorksExistForEstimateId(estimateId: String): Boolean

    @Query("SELECT * FROM JOB_ESTIMATE_WORKS WHERE estimateId = :estimateId")
    fun getJobEstimateWorksForEstimateId(estimateId: String?): JobEstimateWorksDTO

    @Query("DELETE FROM JOB_ESTIMATE_WORKS")
    fun deleteAll()

    @Query("SELECT * FROM JOB_ESTIMATE_WORKS WHERE actId < :actId")
    fun getWorkItemsForActID(actId: Int): LiveData<List<JobEstimateWorksDTO>>

    @Query("SELECT * FROM JOB_ESTIMATE_WORKS WHERE actId = :actId AND estimateId = :estimateId")
    suspend fun getWorkItemsForEstimateIDAndActID(estimateId: String?, actId: Int): JobEstimateWorksDTO
}
