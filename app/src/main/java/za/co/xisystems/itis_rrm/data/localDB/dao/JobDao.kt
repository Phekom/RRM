package za.co.xisystems.itis_rrm.data.localDB.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
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
    fun insertOrUpdateJobs(job: JobDTO)

    @Query("SELECT * FROM JOB_TABLE WHERE jobId = :jobId")
    fun checkIfJobExist(jobId: String): Boolean

    @Query("SELECT * FROM JOB_TABLE WHERE ActId = null OR ActId = 0 ")
    fun checkIfUnsubmittedJobsExist(): Boolean

    @Query("UPDATE JOB_TABLE SET route =:route, section =:section WHERE jobId = :jobId")
    fun updateAllJobs(route: String?, section: String?, jobId: String?)

    @Query("UPDATE JOB_TABLE SET TrackRouteId =:trackRouteId, ActId =:actId, JiNo =:jiNo WHERE jobId = :jobId")
    fun updateJob(trackRouteId: String?, actId: Int, jiNo: String?, jobId: String?)

    @Query("UPDATE JOB_TABLE SET ESTIMATES_ACT_ID =:actId WHERE jobId = :jobId")
    fun setEstimateActId(actId: Int?, jobId: String?)

    @Query("UPDATE JOB_TABLE SET WORKS_ACT_ID =:actId WHERE jobId = :jobId")
    fun setEstimateWorksActId(actId: Int, jobId: String)

    @Query("UPDATE JOB_TABLE SET MEASURE_ACT_ID =:actId WHERE jobId = :jobId")
    fun setMeasureActId(actId: Int, jobId: String)

    @Query("SELECT descr FROM JOB_TABLE WHERE jobId = :jobId")
    fun getItemDescription(jobId: String): String

    @Query("SELECT jiNo FROM JOB_TABLE WHERE jobId = :jobId")
    suspend fun getItemJobNo(jobId: String): String

    @Query("SELECT StartKm FROM JOB_TABLE WHERE jobId = :jobId")
    fun getItemStartKm(jobId: String): Double

    @Query("SELECT EndKm FROM JOB_TABLE WHERE jobId = :jobId")
    fun getItemEndKm(jobId: String): Double

    @Query("SELECT TrackRouteId FROM JOB_TABLE WHERE jobId = :jobId")
    fun getItemTrackRouteId(jobId: String): String

    @Query("SELECT * FROM JOB_TABLE ")
    fun getAllJobsForAllProjects(): LiveData<List<JobDTO>>

    @Query("SELECT * FROM JOB_TABLE WHERE is_synced = 0 ")
    fun getUnSyncedJobs(): LiveData<List<JobDTO>>

    @Query("SELECT * FROM JOB_TABLE WHERE actId = :jobApproved  AND ESTIMATES_ACT_ID LIKE :estimateComplete AND  WORKS_ACT_ID LIKE :estWorksComplete AND MEASURE_ACT_ID LIKE :measureComplete ORDER BY jiNo ASC")
    fun getJobsMeasureForActivityIds(
        estimateComplete: Int,
        measureComplete: Int,
        estWorksComplete: Int,
        jobApproved: Int
    ): LiveData<List<JobDTO>>

    @Query("SELECT * FROM JOB_TABLE WHERE actId = :actId ORDER BY jiNo ASC")
    fun getJobsForActivityId(actId: Int): LiveData<List<JobDTO>>

    @Query(" SELECT j.*, e.* FROM JOB_TABLE AS j JOIN JOB_ITEM_ESTIMATE AS e ON e.JobId = j.JobId WHERE j.ActId Like :actId and e.ActId Like :actId2 ORDER BY jiNo ASC ")
    fun getJobsForActivityIds(actId: Int, actId2: Int): List<JobDTO>

    @Query(" SELECT j.*, e.* FROM JOB_TABLE AS j JOIN JOB_ITEM_ESTIMATE AS e ON e.JobId = j.JobId WHERE j.ActId Like :actId and e.ActId Like :actId2 ORDER BY jiNo ASC ")
    fun getJobsForActivityIds1(actId: Int, actId2: Int): LiveData<List<JobDTO>>

    //    LiveData<List<JobDTO>>
// OR e.ActId Like 8
    @Query("UPDATE JOB_TABLE SET SectionId =:sectionId ,StartKm =:startKM , EndKm =:endKM ,JobItemEstimates =:newJobItemEstimatesList, JobSections =:jobItemSectionArrayList  WHERE jobId = :newJobId ")
    fun updateJoSecId(
        newJobId: String,
        startKM: Double,
        endKM: Double,
        sectionId: String,
        newJobItemEstimatesList: ArrayList<JobItemEstimateDTO>,
        jobItemSectionArrayList: ArrayList<JobSectionDTO>
    )

    @Query("SELECT * FROM JOB_TABLE WHERE jobId = :jobId")
    fun getJobForJobId(jobId: String): JobDTO

    @Query("SELECT jobId FROM JOB_TABLE WHERE activityId = :actId")
    fun getJobIds(actId: Int): LiveData<List<String>>

    @Query("SELECT * FROM JOB_TABLE WHERE jobId = :jobId")
    fun getJobFromJobId(jobId: String): LiveData<JobDTO>

    @Query("DELETE FROM JOB_TABLE WHERE jobId = :jobId")
    fun deleteJobForJobId(jobId: String)

    @Query("DELETE FROM JOB_TABLE")
    fun deleteAll()
}
