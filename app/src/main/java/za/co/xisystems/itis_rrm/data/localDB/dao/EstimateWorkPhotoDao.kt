package za.co.xisystems.itis_rrm.data.localDB.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import za.co.xisystems.itis_rrm.data.localDB.entities.JobEstimateWorksPhotoDTO

/**
 * Created by Francis Mahlava on 2019/11/26.
 */
@Dao
interface EstimateWorkPhotoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEstimateWorksPhoto(estimateWorksPhoto: JobEstimateWorksPhotoDTO)

    @Query("SELECT EXISTS (SELECT * FROM JOB_ESTIMATE_WORKS_PHOTO WHERE filename = :filename)")
    fun checkIfEstimateWorksPhotoExist(filename: String): Boolean

    @Query("SELECT * FROM JOB_ESTIMATE_WORKS_PHOTO WHERE worksId = :worksId")
    suspend fun getEstimateWorksPhotoForWorksId(worksId: String): List<JobEstimateWorksPhotoDTO>

    @Query("DELETE FROM JOB_ESTIMATE_WORKS_PHOTO")
    fun deleteAll()

    @Update
    fun updateExistingEstimateWorksPhoto(estimateWorksPhoto: JobEstimateWorksPhotoDTO)

    @Query("SELECT * FROM JOB_ESTIMATE_WORKS_PHOTO WHERE worksId = :worksId AND photoActivityId = :actId")
    fun getEstimateWorksPhotoForWorksIdAndActID(worksId: String, actId: Int): List<JobEstimateWorksPhotoDTO>
}
