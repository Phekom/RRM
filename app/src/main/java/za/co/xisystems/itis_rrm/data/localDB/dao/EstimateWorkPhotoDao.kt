package za.co.xisystems.itis_rrm.data.localDB.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import za.co.xisystems.itis_rrm.data.localDB.entities.JobEstimateWorksPhotoDTO

/**
 * Created by Francis Mahlava on 2019/11/26.
 */
@Dao
interface EstimateWorkPhotoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEstimateWorksPhoto( estimateworksphoto : JobEstimateWorksPhotoDTO)

    @Query("SELECT * FROM JOB_ESTIMATE_WORKS_PHOTO WHERE filename = :filename")
    fun checkIfEstimateWorksPhotoExist(filename: String): Boolean


}