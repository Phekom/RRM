/**
 * Updated by Shaun McDonald on 2021/05/15
 * Last modified on 2021/05/14, 20:32
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 **/

/**
 * Updated by Shaun McDonald on 2021/05/14
 * Last modified on 2021/05/14, 19:43
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 **/

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
    suspend fun insertJobItemEstimate(jobItemEstimate: JobItemEstimateDTO)

    @Query("SELECT * FROM JOB_ITEM_ESTIMATE WHERE estimateId = :estimateId")
    fun checkIfJobItemEstimateExist(estimateId: String): Boolean

    @Query("SELECT * FROM JOB_ITEM_ESTIMATE WHERE jobId = :jobId AND actId = :actID")
    fun getJobEstimationItemsForJobId(jobId: String, actID: Int): LiveData<List<JobItemEstimateDTO>>

    @Query("SELECT * FROM JOB_ITEM_ESTIMATE WHERE jobId = :jobId")
    fun getJobEstimationItemsForJobId2(jobId: String): LiveData<List<JobItemEstimateDTO>>

    @Query("SELECT * FROM JOB_ITEM_ESTIMATE WHERE actId = :actId and measureActId IN (:activityId2,:activityId3) ORDER BY measureActId DESC ")
    fun getJobMeasureForActivityId(actId: Int, activityId2: Int, activityId3: Int): LiveData<List<JobItemEstimateDTO>>

    @Query("SELECT * FROM JOB_ITEM_ESTIMATE WHERE actId = :actId AND measureActId =:activityId ORDER BY measureActId ASC ")
    fun getJobMeasureForActivityId2(actId: Int, activityId: Int): LiveData<List<JobItemEstimateDTO>>

    @Query("SELECT qty FROM JOB_ITEM_ESTIMATE WHERE estimateId = :estimateId")
    fun getQuantityForEstimationItemId(estimateId: String): LiveData<Double>

    @Query("SELECT lineRate FROM JOB_ITEM_ESTIMATE WHERE estimateId = :estimateId")
    fun getLineRateForEstimationItemId(estimateId: String): LiveData<Double>

    @Query("UPDATE JOB_ITEM_ESTIMATE SET qty =:newQuantity, lineRate =:newTotal  WHERE estimateId = :newEstimateId")
    fun upDateLineRate(newEstimateId: String, newQuantity: Double, newTotal: Double)

    @Query("SELECT * FROM JOB_ITEM_ESTIMATE WHERE actId LIKE :actId  ORDER BY jobId ASC ")
    fun getJobsForActivityId(actId: Int): LiveData<List<JobItemEstimateDTO>>

    @Query("SELECT estimateId FROM JOB_ITEM_ESTIMATE WHERE jobId = :jobId")
    fun getJobEstimateIdForJobId(jobId: String): String

    @Query("UPDATE JOB_ITEM_ESTIMATE SET TrackRouteId =:trackRouteId, ActId =:actId  WHERE estimateId = :estimateId")
    fun updateExistingJobItemEstimateWorkflow(
        trackRouteId: String?,
        actId: Int,
        estimateId: String?
    )

    @Query("UPDATE JOB_ITEM_ESTIMATE SET TrackRouteId =:trackRouteId, ActId =:actId AND measureActId =:actId WHERE estimateId = :estimateId")
    fun updateExistingJobItemEstimateWorkflow2(
        trackRouteId: String?,
        actId: Int,
        estimateId: String?
    )

    @Query("SELECT * FROM JOB_ITEM_ESTIMATE WHERE jobId = :jobID")
    fun getJobItemsToMeasureForJobId(jobID: String): LiveData<List<JobItemEstimateDTO>>

    @Query("DELETE FROM JOB_ITEM_ESTIMATE")
    fun deleteAll()

    @Query("SELECT * FROM JOB_ITEM_ESTIMATE WHERE estimateId = :estimateId")
    fun getJobItemEstimateForEstimateId(estimateId: String): JobItemEstimateDTO

    @Query("UPDATE JOB_ITEM_ESTIMATE SET measureActId =:actId WHERE estimateId = :estimateId")
    fun setMeasureActId(actId: Int, estimateId: String)

    @Query("SELECT COUNT(A.estimateId ) AS 'workDone' FROM JOB_ITEM_ESTIMATE AS A JOIN JOB_ESTIMATE_WORKS  AS B ON B.estimateId = A.estimateId AND B.actId = :estWorksComplete WHERE A.jobId LIKE :jobId AND A.actId = :estimateWorkPartComplete ")
    fun getJobItemsEstimatesDoneForJobId(
        jobId: String?,
        estimateWorkPartComplete: Int,
        estWorksComplete: Int
    ): Int

    @Query("UPDATE JOB_ITEM_ESTIMATE set actId = :actId WHERE estimateId = :estimateId")
    suspend fun updateActIdForJobItemEstimate(actId: Int, estimateId: String): Int

    @Query("DELETE FROM JOB_ITEM_ESTIMATE WHERE estimateId = :estimateId")
    suspend fun deleteJobItemEstimateByEstimateId(estimateId: String): Int
}
