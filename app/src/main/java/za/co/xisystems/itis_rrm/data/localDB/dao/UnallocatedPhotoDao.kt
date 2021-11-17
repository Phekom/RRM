package za.co.xisystems.itis_rrm.data.localDB.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import za.co.xisystems.itis_rrm.data.localDB.entities.UnallocatedPhotoDTO

@Dao
interface UnallocatedPhotoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnallocatedPhoto(photo: UnallocatedPhotoDTO)

    @Update
    suspend fun updateUnallocatedPhoto(photo: UnallocatedPhotoDTO)

    @Query("SELECT EXISTS (SELECT * FROM UNALLOCATED_PHOTOS WHERE photoId = :photoId)")
    fun checkIfUnallocatedPhotoExistsByPhotoId(photoId: String): Boolean

    @Query("SELECT * FROM UNALLOCATED_PHOTOS WHERE photoId = :photoId")
    fun getUnallocatedPhoto(photoId: String): UnallocatedPhotoDTO

    @Query("DELETE FROM UNALLOCATED_PHOTOS")
    fun deleteAll()

    @Query("DELETE FROM UNALLOCATED_PHOTOS WHERE photoId = :photoId")
    suspend fun deletePhotoById(photoId: String)

    @Transaction
    suspend fun retakeUnallocatedPhoto(photoId: String, newPhoto: UnallocatedPhotoDTO) {
        deletePhotoById(photoId)
        insertUnallocatedPhoto(newPhoto)
    }

    @Query("SELECT * FROM UNALLOCATED_PHOTOS WHERE DATETIME(photoDate) >= DATETIME('now','-1 day')")
    suspend fun filterUnallocatedPhotosByTime(): List<UnallocatedPhotoDTO>

    @Query("SELECT * FROM UNALLOCATED_PHOTOS WHERE routeMarker LIKE :routeMarker " +
        "AND datetime(photoDate) >= datetime('now','-1 day')")
    suspend fun filterUnallocatedPhotosByRouteMarker(routeMarker: String): List<UnallocatedPhotoDTO>
}
