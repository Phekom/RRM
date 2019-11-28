package za.co.xisystems.itis_rrm.data.localDB.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import za.co.xisystems.itis_rrm.data.localDB.entities.HealthDTO

/**
 * Created by Francis Mahlava on 2019/10/23.
 */


@Dao
interface HealthDao{

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(health: HealthDTO) : Long

    @Query("SELECT * FROM HealthDTO")
    fun getLife() : LiveData<HealthDTO>


}