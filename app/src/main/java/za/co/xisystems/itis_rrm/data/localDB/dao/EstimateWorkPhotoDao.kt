package za.co.xisystems.itis_rrm.data.localDB.dao

import androidx.lifecycle.LiveData
import androidx.room.*
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

    @Query("SELECT * FROM JOB_ESTIMATE_WORKS_PHOTO WHERE worksId = :worksId")
    fun getEstimateWorksPhotoForWorksId(worksId: String): LiveData<List<JobEstimateWorksPhotoDTO>>?



    @Query("DELETE FROM JOB_ESTIMATE_WORKS_PHOTO")
    fun deleteAll()

    @Update
    fun updateExistingEstimateWorksPhoto(estimateWorksPhoto: JobEstimateWorksPhotoDTO)



}