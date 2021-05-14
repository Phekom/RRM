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
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import java.util.ArrayList
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimateDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobSectionDTO

/**
 * Created by Francis Mahlava on 2019/11/21.
 */

@Dao

interface JobDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdateJob(job: JobDTO)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJobs(jobs: List<JobDTO>)

    @Delete
    fun deleteJob(job: JobDTO): Int

    @Query("SELECT * FROM JOB_TABLE WHERE jobId = :jobId AND deleted = 0")
    fun checkIfJobExist(jobId: String): Boolean

    @Query("SELECT * FROM JOB_TABLE WHERE actId = null OR actId = 0 and deleted = 0")
    fun checkIfUnsubmittedJobsExist(): Boolean

    @Query("UPDATE JOB_TABLE SET route =:route, section =:section WHERE jobId = :jobId AND deleted = 0")
    fun updateAllJobs(route: String?, section: String?, jobId: String?)

    @Query(
        "UPDATE JOB_TABLE SET trackRouteId =:trackRouteId, actId =:actId, jiNo =:jiNo" +
            " WHERE jobId = :jobId AND deleted = 0"
    )
    fun updateJob(trackRouteId: String?, actId: Int, jiNo: String?, jobId: String?)

    @Query("UPDATE JOB_TABLE SET estimatesActId =:actId WHERE jobId = :jobId AND deleted = 0")
    fun setEstimateActId(actId: Int?, jobId: String?)

    @Query("UPDATE JOB_TABLE SET worksActId =:actId WHERE jobId = :jobId AND deleted = 0")
    fun setEstimateWorksActId(actId: Int, jobId: String)

    @Query("UPDATE JOB_TABLE SET measureActId =:actId WHERE jobId = :jobId AND deleted = 0")
    fun setMeasureActId(actId: Int, jobId: String)

    @Query("SELECT descr FROM JOB_TABLE WHERE jobId = :jobId AND deleted = 0")
    fun getItemDescription(jobId: String): String

    @Query("SELECT jiNo FROM JOB_TABLE WHERE jobId = :jobId AND deleted = 0")
    suspend fun getItemJobNo(jobId: String): String

    @Query("SELECT startKm FROM JOB_TABLE WHERE jobId = :jobId AND deleted = 0")
    fun getItemStartKm(jobId: String): Double

    @Query("SELECT endKm FROM JOB_TABLE WHERE jobId = :jobId AND deleted = 0")
    fun getItemEndKm(jobId: String): Double

    @Query("SELECT trackRouteId FROM JOB_TABLE WHERE jobId = :jobId AND deleted = 0")
    fun getItemTrackRouteId(jobId: String): String

    @Query("SELECT * FROM JOB_TABLE where deleted = 0")
    fun getAllJobsForAllProjects(): LiveData<List<JobDTO>>

    @Query("SELECT * FROM JOB_TABLE WHERE isSynced = 0 AND deleted = 0")
    fun getUnSyncedJobs(): LiveData<List<JobDTO>>

    @Query(
        "SELECT * FROM JOB_TABLE WHERE " +
        "actId = :jobApproved  AND estimatesActId LIKE :estimateComplete " +
        "AND  worksActId LIKE :estWorksComplete AND " +
            "measureActId LIKE :measureComplete AND deleted = 0 ORDER BY jiNo ASC"
    )
    fun getJobsMeasureForActivityIds(
        estimateComplete: Int,
        measureComplete: Int,
        estWorksComplete: Int,
        jobApproved: Int
    ): LiveData<List<JobDTO>>

    @Query("SELECT * FROM JOB_TABLE WHERE actId = :actId AND deleted = 0 ORDER BY jiNo ASC")
    fun getJobsForActivityId(actId: Int): LiveData<List<JobDTO>>

    @Query(
        " SELECT j.*, e.* FROM JOB_TABLE AS j JOIN " +
        "JOB_ITEM_ESTIMATE AS e ON e.JobId = j.jobId " +
        "WHERE j.actId Like :actId and e.ActId Like :actId2 " +
            "AND j.deleted = 0 ORDER BY jiNo ASC "
    )
    fun getJobsForActivityIds(actId: Int, actId2: Int): List<JobDTO>

    @Query(
        " SELECT j.*, e.* FROM JOB_TABLE AS j JOIN JOB_ITEM_ESTIMATE AS e " +
        "ON e.JobId = j.jobId WHERE j.actId Like :actId " +
            "AND e.ActId Like :actId2 AND j.deleted = 0 ORDER BY jiNo ASC "
    )
    fun getJobsForActivityIds1(actId: Int, actId2: Int): LiveData<List<JobDTO>>

    //    LiveData<List<JobDTO>>
// OR e.ActId Like 8
    @Query(
        "UPDATE JOB_TABLE SET sectionId =:sectionId ,startKm =:startKM , endKm =:endKM ," +
        "jobItemEstimates =:newJobItemEstimatesList, jobSections =:jobItemSectionArrayList" +
            "  WHERE jobId = :newJobId AND deleted = 0"
    )
    fun updateJoSecId(
        newJobId: String,
        startKM: Double,
        endKM: Double,
        sectionId: String,
        newJobItemEstimatesList: ArrayList<JobItemEstimateDTO>,
        jobItemSectionArrayList: ArrayList<JobSectionDTO>
    )

    @Query("SELECT * FROM JOB_TABLE WHERE jobId = :jobId AND deleted = 0")
    fun getJobForJobId(jobId: String): JobDTO

    @Query("SELECT jobId FROM JOB_TABLE WHERE activityId = :actId AND deleted = 0")
    fun getJobIds(actId: Int): LiveData<List<String>>

    @Query("SELECT * FROM JOB_TABLE WHERE jobId = :jobId AND deleted = 0")
    fun getJobFromJobId(jobId: String): LiveData<JobDTO>

    @Query("DELETE FROM JOB_TABLE WHERE jobId = :jobId AND deleted = 0")
    fun deleteJobForJobId(jobId: String)

    @Query("DELETE FROM JOB_TABLE")
    fun deleteAll()

    @Query("UPDATE JOB_TABLE SET deleted = 1 WHERE jobId = :jobId AND deleted = 0")
    fun softDeleteJobForJobId(jobId: String)

    @Query("UPDATE JOB_TABLE SET deleted = 0 WHERE jobId = :jobId AND deleted = 1")
    fun unDeleteJobForJobId(jobId: String)
}
