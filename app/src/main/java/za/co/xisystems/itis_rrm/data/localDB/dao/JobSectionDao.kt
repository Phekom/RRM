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
    suspend fun insertJobSection(jobSection: JobSectionDTO)

    @Query("SELECT * FROM JOB_SECTION_TABLE WHERE jobSectionId = :jobSectionId")
    fun checkIfJobSectionExist(jobSectionId: String): Boolean

    @Query("SELECT * FROM JOB_SECTION_TABLE WHERE jobId = :jobId")
    fun checkIfJobSectionExistForJobId(jobId: String?): Boolean

//    @Query("SELECT * FROM JOB_SECTION_TABLE WHERE jobId = :jobId")
//    fun getJobSectionFromJobId(jobId: String): LiveData<List<JobSectionDTO>>

    @Query("SELECT projectSectionId FROM JOB_SECTION_TABLE WHERE jobId = :jobId")
    fun getProjectSectionId(jobId: String): String


//    @Query("SELECT * FROM PROJECT_ITEM_TABLE WHERE projectId = :projectId")
//    fun getAllItemsForProjectId(projectId: String): LiveData<List<ItemDTO>>
//
//
//    @Query("SELECT * FROM PROJECT_ITEM_TABLE WHERE sectionItemId = :sectionItem AND projectId = :projectId")
//    fun getAllItemsForSectionItem(sectionItem : String, projectId : String ): LiveData<List<ItemDTO>>
//
//
//    @Query("DELETE FROM PROJECT_ITEM_TABLE")
//    fun deleteAll()
}