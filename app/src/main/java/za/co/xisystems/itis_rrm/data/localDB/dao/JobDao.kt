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
    suspend fun insertJob( intities : List<JobDTO> )

    @Query("SELECT * FROM JOB_TABLE WHERE jobId = :jobId")
    fun checkIfJobExist(jobId: String): LiveData<List<JobDTO>>

    @Query("SELECT * FROM JOB_TABLE WHERE activityId = null OR activityId = 0 ")
    fun checkIfUnsubmittedJobsExist(): LiveData<List<JobDTO>>

//    updateWorkflowJob(WorkflowJob existingJob)

//    updateJob(Job existingJob)

//    setJobToSynced(Job job)

    @Query("SELECT * FROM JOB_TABLE ")
    fun getAllItemsForAllProjects() : LiveData<List<JobDTO>>

    @Query("SELECT * FROM JOB_TABLE WHERE is_synced = 0 ")
    fun getUnSyncedJobs() : LiveData<List<JobDTO>>

//    @Update
//    fun update(noteDTO: NoteDTO)

    @Query("SELECT * FROM JOB_TABLE WHERE activityId = :actId")
    fun getJobsForActivityId(actId: String): LiveData<List<JobDTO>>


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