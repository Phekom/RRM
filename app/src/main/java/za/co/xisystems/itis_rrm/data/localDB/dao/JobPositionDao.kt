package za.co.xisystems.itis_rrm.data.localDB.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import za.co.xisystems.itis_rrm.data.localDB.entities.JobCategoryDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobPositionDTO

/**
 * Created by Francis Mahlava on 2019/12/04.
 */

@Dao
interface JobPositionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertJobPosition(jposition : JobPositionDTO)

    @Query("SELECT * FROM JOB_POSITION_TABLE WHERE jobPosition = :position")
    fun checkJobPositionExists(position : String): Boolean

    @Query("SELECT DISTINCT * FROM JOB_POSITION_TABLE")
    fun getJobPositions(): List<JobPositionDTO>


}
