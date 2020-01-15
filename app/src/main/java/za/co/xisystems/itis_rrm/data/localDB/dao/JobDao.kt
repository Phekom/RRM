package za.co.xisystems.itis_rrm.data.localDB.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO

/**
 * Created by Francis Mahlava on 2019/11/21.
 */

@Dao
interface JobDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateJobs(jobs: JobDTO)

    @Query("SELECT * FROM JOB_TABLE WHERE jobId = :jobId")
    fun checkIfJobExist(jobId: String): Boolean

    @Query("SELECT * FROM JOB_TABLE WHERE activityId = null OR activityId = 0 ")
    fun checkIfUnsubmittedJobsExist(): Boolean

    @Query("UPDATE JOB_TABLE SET route =:route, section =:section WHERE jobId = :jobId")
    fun updateAllJobs(route: String?, section: String?, jobId: String?)

    @Query("UPDATE JOB_TABLE SET TrackRouteId =:trackRouteId, ActId =:actId, JiNo =:jiNo WHERE jobId = :jobId")
    fun updateJob(jobId: String?, actId: Int, trackRouteId: String?, jiNo: String?)

    @Query("UPDATE JOB_TABLE SET ESTIMATES_ACT_ID =:actId WHERE jobId = :jobId")
    fun setEstimateActId(actId: Int?, jobId: String?)


    @Query("UPDATE JOB_TABLE SET WORKS_ACT_ID =:actId WHERE jobId = :jobId")
    fun setEstimateWorksActId(actId: Int, jobId: String)

    @Query("UPDATE JOB_TABLE SET MEASURE_ACT_ID =:actId WHERE jobId = :jobId")
    fun setMeasureActId(actId: Int, jobId: String)


    @Query("SELECT descr FROM JOB_TABLE WHERE jobId = :jobId")
    fun getItemDescription(jobId: String): String

    @Query("SELECT jiNo FROM JOB_TABLE WHERE jobId = :jobId")
    fun getItemJobNo(jobId: String): String

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

    @Query("SELECT * FROM JOB_TABLE WHERE actId = :actId ORDER BY jiNo ASC")
    fun getJobsForActivityId(actId: Int): LiveData<List<JobDTO>>

    @Query("SELECT * FROM JOB_TABLE WHERE actId = :actId AND ESTIMATES_ACT_ID = :actId2 ORDER BY jiNo ASC")
    fun getJobsForActivityIds1(actId: Int, actId2: Int): LiveData<List<JobDTO>>

    @Query("SELECT jobId FROM JOB_TABLE WHERE activityId = :actId")
    fun getJobIds(actId: Int): LiveData<List<String>>


    @Query("SELECT * FROM JOB_TABLE WHERE jobId = :jobId")
    fun getJobFromJobId(jobId: String): LiveData<JobDTO>

    @Query("DELETE FROM JOB_TABLE WHERE jobId = :jobId")
    fun deleteJobForJobId(jobId: String)


//    @Query("SELECT * FROM JOB_TABLE WHERE sectionItemId = :sectionItem AND projectId = :projectId")
//    fun getAllItemsForSectionItem(sectionItem : String, projectId : String ): LiveData<List<JobDTO>>


    @Query("DELETE FROM JOB_TABLE")
    fun deleteAll()




}




//    updateJob(Job existingJob)
//    INSERT INTO Customers (CustomerName, City, Country)
//    SELECT SupplierName, City, Country FROM Suppliers
//    WHERE Country='Germany';
//    setJobToSynced(Job job)