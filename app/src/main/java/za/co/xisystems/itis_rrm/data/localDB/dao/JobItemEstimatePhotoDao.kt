package za.co.xisystems.itis_rrm.data.localDB.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimatesPhotoDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.UnallocatedPhotoDTO

/**
 * Created by Francis Mahlava on 2019/11/21.
 */

@Dao
interface JobItemEstimatePhotoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertJobItemEstimatePhoto(jobItemEstimatePhoto: JobItemEstimatesPhotoDTO)

    @Update
    fun updateJobItemEstimatePhoto(jobItemEstimatePhoto: JobItemEstimatesPhotoDTO)

    @Query("SELECT EXISTS (SELECT * FROM JOB_ITEM_ESTIMATE_PHOTO WHERE photoId = :photoId)")
    fun checkIfJobItemEstimatePhotoExistsByPhotoId(photoId: String): Boolean

    @Query("SELECT photoPath FROM JOB_ITEM_ESTIMATE_PHOTO WHERE estimateId = :estimateId AND isPhotostart LIKE 0 ")
    fun getJobEstimationItemsPhotoEndPath(estimateId: String): String

    @Query("SELECT photoPath FROM JOB_ITEM_ESTIMATE_PHOTO WHERE estimateId = :estimateId AND isPhotostart LIKE 1 ")
    fun getJobEstimationItemsPhotoStartPath(estimateId: String): String

    @Query("SELECT * FROM JOB_ITEM_ESTIMATE_PHOTO WHERE photoId = :photoId")
    fun getJobItemEstimatePhoto(photoId: String): JobItemEstimatesPhotoDTO

    @Query("SELECT * FROM JOB_ITEM_ESTIMATE_PHOTO WHERE estimateId = :estimateId")
    fun getJobItemEstimatePhotoForEstimateId(estimateId: String): LiveData<List<JobItemEstimatesPhotoDTO>>

    @Query("DELETE FROM JOB_ITEM_ESTIMATE_PHOTO WHERE filename = :filename")
    fun deletePhotoByNAme(filename: String)

    @Query("DELETE FROM JOB_ITEM_ESTIMATE_PHOTO")
    fun deleteAll()

    @Query("DELETE FROM JOB_ITEM_ESTIMATE_PHOTO WHERE photoId = :photoId")
    fun deletePhotoById(photoId: String)

    @Query("DELETE FROM JOB_ITEM_ESTIMATE_PHOTO WHERE estimateId = :estimateId")
    fun deleteJobItemEstimatePhotosByEstimateId(estimateId: String): Int

    @Transaction
    fun replaceEstimatePhoto(photoId: String, estimatePhoto: JobItemEstimatesPhotoDTO) {
        deletePhotoById(photoId)
        insertJobItemEstimatePhoto(estimatePhoto)
    }

//    @Query("SELECT * FROM JOB_ITEM_ESTIMATE_PHOTO WHERE (routeMarker LIKE :criteria OR descr LIKE :criteria) AND datetime(photoDate) >= datetime('now','-1 day')")
//    fun searchUnallocatedPhotos(criteria: String): List<UnallocatedPhotoDTO>?

    @Query("SELECT * FROM JOB_ITEM_ESTIMATE_PHOTO WHERE estimateId = :estimateId")
    fun getEstimateStartPhotoForId(estimateId: String): JobItemEstimatesPhotoDTO


    @Query("SELECT * FROM JOB_ITEM_ESTIMATE_PHOTO WHERE filename = :imageFileName")
    fun getEstimatePhotoByName(imageFileName: String): JobItemEstimatesPhotoDTO

    @Query("SELECT EXISTS (SELECT * FROM JOB_ITEM_ESTIMATE_PHOTO WHERE filename = :filename)")
    fun checkIfJobItemEstimatePhotoExistsByName(filename: String): Boolean

}
