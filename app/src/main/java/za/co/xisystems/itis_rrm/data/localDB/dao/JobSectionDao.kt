package za.co.xisystems.itis_rrm.data.localDB.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import za.co.xisystems.itis_rrm.data.localDB.entities.JobSectionDTO

/**
 * Created by Francis Mahlava on 2019/11/21.
 */

@Dao
interface JobSectionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertJobSection(jobSection: JobSectionDTO)

    @Query("SELECT * FROM JOB_SECTION_TABLE WHERE jobSectionId = :jobSectionId")
    fun checkIfJobSectionExist(jobSectionId: String): Boolean

    @Query("SELECT EXISTS(SELECT * FROM JOB_SECTION_TABLE WHERE jobId = :jobId AND projectSectionId = :projectSectionId)")
    fun checkIfJobSectionExistForJob(jobId: String?, projectSectionId: String?): Boolean

    @Query("SELECT * FROM JOB_SECTION_TABLE WHERE jobId = :jobId")
    fun getJobSectionFromJobId(jobId: String): JobSectionDTO?

    @Query("SELECT projectSectionId FROM JOB_SECTION_TABLE WHERE jobId = :jobId")
    fun getProjectSectionId(jobId: String): String

    @Query("UPDATE JOB_SECTION_TABLE SET jobSectionId =:jobSectionId,projectSectionId =:projectSectionId,jobId =:jobId,startKm =:startKm,endKm =:endKm, recordVersion =:recordVersion , recordSynchStateId =:recordSynchStateId  WHERE jobSectionId = :jobSectionId")
    fun updateExistingJobSectionWorkflow(
        jobSectionId: String?,
        projectSectionId: String?,
        jobId: String?,
        startKm: Double,
        endKm: Double,
        recordVersion: Int,
        recordSynchStateId: Int
    )

    @Query("DELETE FROM JOB_SECTION_TABLE")
    fun deleteAll()
}
