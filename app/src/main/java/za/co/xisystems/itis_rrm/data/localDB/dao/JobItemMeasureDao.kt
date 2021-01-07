package za.co.xisystems.itis_rrm.data.localDB.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import java.util.ArrayList
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemMeasureDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemMeasurePhotoDTO

/**
 * Created by Francis Mahlava on 2019/11/21.
 */

@Dao
interface JobItemMeasureDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertJobItemMeasure(jobItemMeasure: JobItemMeasureDTO): Long

    @Query("UPDATE JOB_ITEM_MEASURE SET jobItemMeasurePhotos =:jobItemMeasurePhotoList WHERE itemMeasureId = :itemMeasureId")
    fun upDatePhotList(
        jobItemMeasurePhotoList: ArrayList<JobItemMeasurePhotoDTO>,
        itemMeasureId: String
    )

    @Query("SELECT * FROM JOB_ITEM_MEASURE WHERE itemMeasureId = :itemMeasureId AND deleted = 0")
    fun checkIfJobItemMeasureExists(itemMeasureId: String): Boolean

    @Query("SELECT * FROM JOB_ITEM_MEASURE WHERE actId = :actId AND deleted = 0 ORDER BY jimNo ASC")
    fun getJobApproveMeasureForActivityId(actId: Int): LiveData<List<JobItemMeasureDTO>>

    @Query("SELECT * FROM JOB_ITEM_MEASURE WHERE jobId = :jobId AND actId = :actId AND deleted = 0 ORDER BY jimNo ASC")
    fun
        getJobMeasureItemsForJobId(jobId: String?, actId: Int): LiveData<List<JobItemMeasureDTO>>

    @Query("SELECT * FROM JOB_ITEM_MEASURE WHERE jobId = :jobId AND actId = :actId AND deleted = 0 ORDER BY jimNo ASC")
    fun getJobMeasuresForJobId(jobId: String?, actId: Int): List<JobItemMeasureDTO>

    @Query("UPDATE JOB_ITEM_MEASURE SET trackRouteId =:trackRouteId, ActId =:actId , measureGroupId =:measureGroupId  WHERE itemMeasureId = :itemMeasureId")
    fun updateWorkflowJobItemMeasure(
        itemMeasureId: String?,
        trackRouteId: String?,
        actId: Int,
        measureGroupId: String?
    )

    @Query("SELECT * FROM JOB_ITEM_MEASURE WHERE estimateId = :estimateId AND jobId LIKE :jobId AND deleted = 0")
    fun checkIfJobItemMeasureExistsForJobIdAndEstimateId(
        jobId: String?,
        estimateId: String
    ): Boolean

    @Query("SELECT * FROM JOB_ITEM_MEASURE WHERE jobId = :jobId AND deleted = 0")
    fun getJobItemMeasuresForJobIdAndEstimateId(jobId: String?): LiveData<List<JobItemMeasureDTO>>

    @Query("SELECT * FROM JOB_ITEM_MEASURE WHERE estimateId = :estimateId AND jobId LIKE :jobId AND deleted = 0")
    fun getJobItemMeasuresForJobIdAndEstimateId2(
        jobId: String?,
        estimateId: String
    ): LiveData<List<JobItemMeasureDTO>>

    @Query("SELECT * FROM JOB_ITEM_MEASURE WHERE jobId = :jobId AND deleted = 0 ORDER BY jimNo ASC")
    fun getJobItemMeasureForJobId(jobId: String): LiveData<JobItemMeasureDTO>

    @Query("DELETE FROM JOB_ITEM_MEASURE WHERE itemMeasureId = :itemMeasureId AND deleted = 0")
    fun deleteItemMeasurefromList(itemMeasureId: String)

    @Query("SELECT qty FROM JOB_ITEM_MEASURE WHERE itemMeasureId = :itemMeasureId AND deleted = 0")
    fun getQuantityForMeasureItemId(itemMeasureId: String): LiveData<Double>

    @Query("SELECT lineRate FROM JOB_ITEM_MEASURE WHERE itemMeasureId = :itemMeasureId AND deleted = 0")
    fun getLineRateForMeasureItemId(itemMeasureId: String): LiveData<Double>

    @Query("UPDATE JOB_ITEM_MEASURE SET qty =:newQuantity WHERE itemMeasureId = :newitemMeasureId AND deleted = 0")
    fun upDateQty(newitemMeasureId: String, newQuantity: Double)

    @Query("UPDATE JOB_ITEM_MEASURE SET deleted = 1 WHERE itemMeasureId= :itemMeasureId AND deleted = 0")
    fun deleteMeasurement(itemMeasureId: String): Int

    @Query("UPDATE JOB_ITEM_MEASURE SET deleted = 0 WHERE itemMeasureId= :itemMeasureId AND deleted = 1")
    fun undeleteMeasurement(itemMeasureId: String): Int

    @Query("UPDATE JOB_ITEM_MEASURE SET deleted = 0 WHERE deleted = 1")
    fun undeleteAllMeasurements(): Int

    @Query("DELETE FROM JOB_ITEM_MEASURE")
    fun deleteAll()

    @Query("SELECT * FROM JOB_ITEM_MEASURE WHERE itemMeasureId = :itemMeasureId LIMIT 1")
    fun getJobItemMeasureByItemMeasureId(itemMeasureId: String): LiveData<JobItemMeasureDTO>
}
