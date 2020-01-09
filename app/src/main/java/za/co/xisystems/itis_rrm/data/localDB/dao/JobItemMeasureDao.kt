package za.co.xisystems.itis_rrm.data.localDB.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemMeasureDTO

/**
 * Created by Francis Mahlava on 2019/11/21.
 */


@Dao
interface JobItemMeasureDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJobItemMeasure( jobItemMeasure : JobItemMeasureDTO) : Long

    @Query("SELECT * FROM JOB_ITEM_MEASURE WHERE itemMeasureId = :itemMeasureId")
    fun checkIfJobItemMeasureExists(itemMeasureId: String): Boolean

    @Query("SELECT * FROM JOB_ITEM_MEASURE WHERE actId = :actId ORDER BY jimNo ASC")
    fun getJobApproveMeasureForActivityId(actId: Int): LiveData<List<JobItemMeasureDTO>>

    @Query("SELECT * FROM JOB_ITEM_MEASURE WHERE jobId = :jobId AND actId = :actId ORDER BY jimNo ASC")
    fun getJobMeasureItemsForJobId(jobId: String?,actId: Int): LiveData<List<JobItemMeasureDTO>>


//    @Query("SELECT * FROM JOB_ITEM_MEASURE WHERE jobId = :jobId ORDER BY jimNo ASC")
//    fun getJobItemMeasureForJobId(jobId: String): LiveData<List<JobItemMeasureDTO>>



//    @Query("SELECT * FROM PROJECT_ITEM_TABLE WHERE itemId = :itemId")
//    fun getItemForItemId(itemId: String): LiveData<ItemDTO>
//
//
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