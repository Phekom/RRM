package za.co.xisystems.itis_rrm.data.localDB.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import za.co.xisystems.itis_rrm.data.localDB.entities.JobEstimateWorksDTO

/**
 * Created by Francis Mahlava on 2019/11/26.
 */
@Dao
interface EstimateWorkDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJobEstimateWorks( estimates : JobEstimateWorksDTO )

    @Query("SELECT * FROM JOB_ESTIMATE_WORKS WHERE worksId = :worksId")
    fun checkIfJobEstimateWorksExist(worksId: String): Boolean


}