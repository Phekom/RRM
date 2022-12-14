package za.co.xisystems.itis_rrm.data.localDB.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import za.co.xisystems.itis_rrm.data.localDB.entities.UnallocatedPhotoDTO

@Dao
interface UnallocatedPhotoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUnallocatedPhoto(photo: UnallocatedPhotoDTO)

    @Update
    fun updateUnallocatedPhoto(photo: UnallocatedPhotoDTO)

    @Query("SELECT EXISTS (SELECT * FROM UNALLOCATED_PHOTOS WHERE photoId = :photoId)")
    fun checkIfUnallocatedPhotoExistsByPhotoId(photoId: String): Boolean

    @Query("SELECT * FROM UNALLOCATED_PHOTOS WHERE photoId = :photoId")
    fun getUnallocatedPhoto(photoId: String): UnallocatedPhotoDTO

    @Query("DELETE FROM UNALLOCATED_PHOTOS")
    fun deleteAll()

    @Query("DELETE FROM UNALLOCATED_PHOTOS WHERE photoId = :photoId")
    fun deletePhotoById(photoId: String)

    @Transaction
    fun retakeUnallocatedPhoto(photoId: String, newPhoto: UnallocatedPhotoDTO) {
        deletePhotoById(photoId)
        insertUnallocatedPhoto(newPhoto)
    }

    @Query(
        "SELECT * FROM UNALLOCATED_PHOTOS WHERE (routeMarker LIKE :criteria OR descr LIKE :criteria) " +
            "AND datetime(photoDate) >= datetime('now','-1 day')"
    )
    fun searchUnallocatedPhotos(criteria: String): List<UnallocatedPhotoDTO>?

    @Query(
        "SELECT * FROM UNALLOCATED_PHOTOS WHERE DATETIME(photoDate) >= " +
            "DATETIME('now','-1 day') ORDER BY DATE(photoDate)"
    )
    fun getAllPhotos(): LiveData<List<UnallocatedPhotoDTO>>?

    @Delete
    fun deletePhoto(unallocatedPhoto: UnallocatedPhotoDTO)

    @Query(
        "DELETE FROM UNALLOCATED_PHOTOS WHERE DATE(photoDate) < " +
            "DATE('now','-1 day')"
    )
    fun deleteExpiredPhotos()
}
