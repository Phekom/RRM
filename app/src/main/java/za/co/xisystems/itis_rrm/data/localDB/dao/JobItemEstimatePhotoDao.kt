package za.co.xisystems.itis_rrm.data.localDB.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimatesPhotoDTO

/**
 * Created by Francis Mahlava on 2019/11/21.
 */

@Dao
interface JobItemEstimatePhotoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJobItemEstimatePhoto(jobItemEstimatePhoto: JobItemEstimatesPhotoDTO)

    @Query("SELECT * FROM JOB_ITEM_ESTIMATE_PHOTO WHERE photoId = :photoId")
    fun checkIfJobItemEstimatePhotoExistsByPhotoId(photoId: String): Boolean

    @Query("SELECT * FROM JOB_ITEM_ESTIMATE_PHOTO WHERE estimateId = :estimateId")
    fun getJobEstimationItemsPhoto(estimateId: String): LiveData<List<JobItemEstimatesPhotoDTO>>

    @Query("SELECT photoPath FROM JOB_ITEM_ESTIMATE_PHOTO WHERE estimateId = :estimateId AND is_PhotoStart LIKE 0 ")
    fun getJobEstimationItemsPhotoEndPath(estimateId: String): String

    @Query("SELECT photoPath FROM JOB_ITEM_ESTIMATE_PHOTO WHERE estimateId = :estimateId AND is_PhotoStart LIKE 1 ")
    fun getJobEstimationItemsPhotoStartPath(estimateId: String): String

    @Query("SELECT * FROM JOB_ITEM_ESTIMATE_PHOTO WHERE estimateId = :estimateId")
    fun getJobItemEstimatePhotoForEstimateId(estimateId: String): LiveData<List<JobItemEstimatesPhotoDTO>>

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
    @Query("DELETE FROM JOB_ITEM_ESTIMATE_PHOTO")
    fun deleteAll()
}
