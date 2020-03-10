package za.co.xisystems.itis_rrm.data.localDB.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemMeasureDTO
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

    @Query("SELECT * FROM JOB_ITEM_MEASURE_PHOTO WHERE itemMeasureId = :itemMeasureId")
    fun checkIfJobItemMeasurePhotoExistsForMeasureId(itemMeasureId: String?): Boolean

    @Query("SELECT * FROM JOB_ITEM_MEASURE_PHOTO WHERE itemMeasureId = :itemMeasureId")
    fun getJobItemMeasurePhotosForItemMeasureID(itemMeasureId: String?) : LiveData<List<JobItemMeasurePhotoDTO>>

    @Query("SELECT * FROM JOB_ITEM_MEASURE_PHOTO WHERE estimateId = :estimateId")
    fun getJobItemMeasurePhotosForItemEstimateID(estimateId: String?) : LiveData<List<JobItemMeasurePhotoDTO>>

    @Query("DELETE FROM JOB_ITEM_MEASURE_PHOTO")
    fun deleteAll()

    @Query("DELETE FROM JOB_ITEM_MEASURE_PHOTO WHERE ItemMeasureId = :ItemMeasureId")
    fun deleteItemMeasurephotofromList(ItemMeasureId: String)














//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insertJobItemMeasurePhoto( jobItemMeasurePhoto : JobItemMeasurePhotoDTO)
//
//    @Query("SELECT * FROM JOB_ITEM_MEASURE_PHOTO WHERE filename = :filename")
//    fun checkIfJobItemMeasurePhotoExists(filename: String): Boolean
//
//    @Query("SELECT photoPath FROM JOB_ITEM_MEASURE_PHOTO WHERE itemMeasureId = :itemMeasureId ")
//    fun getJobMeasureItemsPhotoPath(itemMeasureId: String) :  String
//
//    @Query("SELECT * FROM JOB_ITEM_MEASURE_PHOTO WHERE itemMeasureId = :itemMeasureId")
//    fun checkIfJobItemMeasurePhotoExistsForMeasureId(itemMeasureId: String?): Boolean
//
//    @Query("SELECT * FROM JOB_ITEM_MEASURE_PHOTO WHERE itemMeasureId = :itemMeasureId")
//    fun getJobItemMeasurePhotosForItemMeasureID(itemMeasureId: String?) : LiveData<List<JobItemMeasurePhotoDTO>>
//
//
//
//    @Query("DELETE FROM JOB_ITEM_MEASURE_PHOTO WHERE ItemMeasureId = :ItemMeasureId")
//    fun deleteItemMeasurephotofromList(ItemMeasureId: String)
//
//
//
//    @Query("SELECT * FROM JOB_ITEM_MEASURE_PHOTO WHERE estimateId = :estimateId")
//    fun getJobItemMeasurePhotosForItemEstimateID(estimateId: String?) : LiveData<List<JobItemMeasurePhotoDTO>>
    
//    @Query("SELECT * FROM JOB_ITEM_MEASURE_PHOTO WHERE itemMeasureId = :itemMeasureId")
//    fun getJobItemMeasurePhotosForItemMeasureID2(itemMeasureId: String?) :  ArrayList<JobItemMeasurePhotoDTO>

//    @Query("SELECT * FROM PROJECT_ITEM_TABLE WHERE projectId = :projectId")
//    fun getAllItemsForProjectId(projectId: String): LiveData<List<ItemDTO>>
//
//
//    @Query("SELECT * FROM PROJECT_ITEM_TABLE WHERE sectionItemId = :sectionItem AND projectId = :projectId")
//    fun getAllItemsForSectionItem(sectionItem : String, projectId : String ): LiveData<List<ItemDTO>>

//@Query("DELETE FROM JOB_ITEM_MEASURE_PHOTO")
//fun deleteAll()


}