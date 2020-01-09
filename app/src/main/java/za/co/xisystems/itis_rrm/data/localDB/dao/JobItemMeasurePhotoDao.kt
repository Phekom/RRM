package za.co.xisystems.itis_rrm.data.localDB.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemMeasurePhotoDTO

/**
 * Created by Francis Mahlava on 2019/11/21.
 */


@Dao
interface JobItemMeasurePhotoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJobItemMeasurePhoto( jobItemMeasurePhoto : JobItemMeasurePhotoDTO)

    @Query("SELECT * FROM JOB_ITEM_MEASURE_PHOTO WHERE filename = :filename")
    fun checkIfJobItemMeasurePhotoExists(filename: String): Boolean

    @Query("SELECT photoPath FROM JOB_ITEM_MEASURE_PHOTO WHERE itemMeasureId = :itemMeasureId ")
    fun getJobMeasureItemsPhotoPath(itemMeasureId: String) :  String




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