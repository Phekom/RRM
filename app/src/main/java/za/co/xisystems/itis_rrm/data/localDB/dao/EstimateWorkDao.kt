package za.co.xisystems.itis_rrm.data.localDB.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import za.co.xisystems.itis_rrm.data.localDB.entities.JobEstimateWorksDTO

/**
 * Created by Francis Mahlava on 2019/11/26.
 */
@Dao
interface EstimateWorkDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntities( estimates : List<JobEstimateWorksDTO> )



}