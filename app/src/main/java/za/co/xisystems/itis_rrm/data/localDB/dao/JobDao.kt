/**
 * Updated by Shaun McDonald on 2021/05/15
 * Last modified on 2021/05/14, 20:32
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 **/

/**
 * Updated by Shaun McDonald on 2021/05/14
 * Last modified on 2021/05/14, 19:43
 * Last modified by Francis Mahlava on 2022/08/29, 23:22
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 **/

package za.co.xisystems.itis_rrm.data.localDB.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.RoomWarnings
import androidx.room.Update
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimateDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobSectionDTO
import za.co.xisystems.itis_rrm.utils.ActivityIdConstants
import java.util.ArrayList

/**
 * Created by Francis Mahlava on 2019/11/21.
 */

@Dao
@SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
interface JobDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdateJob(job: JobDTO)

    @Update
    fun updateJob(job: JobDTO)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertJobs(jobs: List<JobDTO>)

    @Delete
    fun deleteJob(job: JobDTO)

    @Query("SELECT EXISTS (SELECT * FROM JOB_TABLE WHERE jobId = :jobId AND deleted = 0)")
    fun checkIfJobExist(jobId: String): Boolean

    @Query("SELECT * FROM JOB_TABLE WHERE actId = null OR actId = 0 and deleted = 0")
    fun checkIfUnsubmittedJobsExist(): Boolean

    @Query("UPDATE JOB_TABLE SET actId =:actId WHERE jobId = :jobId AND deleted = 0")
    fun updateJobID(actId: Int?, jobId: String?)

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
    fun getItemJobNo(jobId: String): String

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
        "SELECT * FROM JOB_TABLE WHERE actId = :jobApproved " +
            "AND estimatesActId = :estimateComplete " +
            "AND  worksActId = :estWorksComplete AND " +
            "measureActId = :measureComplete AND deleted = 0 ORDER BY jiNo ASC"
    )
    fun getJobsMeasureForActivityIds(
        estimateComplete: Int,
        measureComplete: Int,
        estWorksComplete: Int,
        jobApproved: Int
    ): LiveData<List<JobDTO>>

    @RewriteQueriesToDropUnusedColumns
    @Query(
        "SELECT * FROM JOB_TABLE WHERE " +
            "actId IN (:actIds) AND deleted = 0 " +
            "ORDER BY jiNo ASC"
    )
    fun getJobsForActivityId(vararg actIds: Int): LiveData<List<JobDTO>>

    @RewriteQueriesToDropUnusedColumns
    @Query(
        " SELECT j.*, e.* FROM JOB_TABLE AS j JOIN JOB_ITEM_ESTIMATE AS e ON e.JobId = j.jobId " +
            "WHERE j.actId = :jobActId and e.ActId = :estimateActId OR e.ActId = :estimateActId2 ORDER BY DATE(j.approvalDate) DESC, jiNO ASC"

    )
    fun getJobsByJobAndEstimateActivityIds(jobActId: Int, estimateActId: Int,estimateActId2: Int): LiveData<List<JobDTO>>?

    @Query(
        " SELECT j.*, e.* FROM JOB_TABLE AS j JOIN JOB_ITEM_ESTIMATE AS e " +
            "ON e.JobId = j.jobId WHERE j.actId = :jobApproved " +
            "AND (e.ActId = :estimateIncomplete OR e.ActId = :estimateWorkPartComplete) AND j.deleted = 0 " +
            "ORDER BY jiNo ASC "
    )
    @RewriteQueriesToDropUnusedColumns
    fun getJobWorkForActivityId(jobApproved: Int, estimateIncomplete: Int, estimateWorkPartComplete: Int): LiveData<List<JobDTO>>

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

//    @RewriteQueriesToDropUnusedColumns
//    @Query(
//        " SELECT j.*, e.* FROM JOB_TABLE AS j JOIN JOB_ITEM_ESTIMATE AS e " +
//            "ON e.JobId = j.jobId WHERE j.actId = :jobActId " +
//            "AND e.ActId = :estimateActId AND j.deleted = 0 AND (j.jiNo LIKE :criteria " +
//            "OR j.descr LIKE :criteria)" +
//            "ORDER BY DATETIME(j.workStartDate) DESC, jiNO ASC"
//    )

//    fun findWork(
//        criteria: String,
//        jobActId: Int = ActivityIdConstants.JOB_APPROVED,
//        estimateActId: Int = ActivityIdConstants.ESTIMATE_INCOMPLETE
//    ): List<JobDTO>

    @Query("SELECT * FROM JOB_TABLE WHERE jobId = :jobId AND deleted = 0")
    fun getJobForJobId(jobId: String): JobDTO

//    @Query("SELECT jobId FROM JOB_TABLE WHERE activityId = :actId AND deleted = 0")
//    fun getJobIds(actId: Int): LiveData<List<String>>

    @Query("SELECT * FROM JOB_TABLE WHERE actId = :actId AND deleted = 0")
    fun getJobsFromActId(actId: Int): List<JobDTO>

    @Query("SELECT * FROM JOB_TABLE WHERE jobId = :jobId AND deleted = 0")
    fun getJobFromJobId(jobId: String): LiveData<JobDTO>

    @Query("DELETE FROM JOB_TABLE WHERE jobId = :jobId AND deleted = 0")
    fun deleteJobForJobId(jobId: String)

    @Query("DELETE FROM JOB_TABLE")
    fun deleteAll()

//    @Query("UPDATE JOB_TABLE SET deleted = 1 WHERE jobId = :jobId AND deleted = 0")
//    fun softDeleteJobForJobId(jobId: String)

    @Query("UPDATE JOB_TABLE SET deleted = 0 WHERE jobId = :jobId AND deleted = 1")
    fun softDeleteJobForJobId(jobId: String)

    fun getAllWork(): LiveData<List<JobDTO>>? =
        getJobsByJobAndEstimateActivityIds(
            jobActId = ActivityIdConstants.JOB_APPROVED,
            estimateActId = ActivityIdConstants.ESTIMATE_INCOMPLETE,
            estimateActId2 = ActivityIdConstants.ESTIMATE_WORK_PART_COMPLETE
        )

   // fun searchJobs(criteria: String) = findWork(criteria)
}
