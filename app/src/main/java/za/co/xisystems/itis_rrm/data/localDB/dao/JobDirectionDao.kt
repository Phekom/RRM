package za.co.xisystems.itis_rrm.data.localDB.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import za.co.xisystems.itis_rrm.data.localDB.entities.JobCategoryDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDirectionDTO

/**
 * Created by Francis Mahlava on 2019/12/04.
 */

@Dao
interface JobDirectionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertJobDirection(jobdirection : JobDirectionDTO)

//    @Query("SELECT * FROM JOB_DIRECTION_TABLE WHERE stepCode = :workCode")
//    fun checkWorkFlowStepExistsWorkCode(workCode: String): Boolean

    @Query("SELECT DISTINCT * FROM JOB_DIRECTION_TABLE")
    fun getJobDirections(): List<JobDirectionDTO>

}
