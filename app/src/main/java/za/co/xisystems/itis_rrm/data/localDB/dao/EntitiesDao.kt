package za.co.xisystems.itis_rrm.data.localDB.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import za.co.xisystems.itis_rrm.data.localDB.entities.EntitiesDTO

/**
 * Created by Francis Mahlava on 2019/11/21.
 */

@Dao
interface EntitiesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntities( intities : List<EntitiesDTO> )

    @Query("SELECT * FROM TABLE_ENTITY WHERE trackRouteId = :trackRouteId")
    fun checkIfEntitiesExist(trackRouteId: String): LiveData<List<EntitiesDTO>>

    @Query("SELECT * FROM TABLE_ENTITY ")
    fun getAllEntities() : LiveData<List<EntitiesDTO>>


    @Query("SELECT * FROM TABLE_ENTITY WHERE trackRouteId = :trackRouteId")
    fun getEntitiesForTrackRouteId(trackRouteId: String): LiveData<EntitiesDTO>


    @Query("SELECT * FROM TABLE_ENTITY WHERE activityId = :actId")
    fun getEntitiesListForActivityId(actId: String): LiveData<List<EntitiesDTO>>


    @Query("SELECT * FROM TABLE_ENTITY WHERE activityId = :actId AND jobId = :jobId")
    fun getEntitiesForJobId(jobId : String, actId : Int ): LiveData<List<EntitiesDTO>>


    @Query("DELETE FROM TABLE_ENTITY")
    fun deleteAllEntities()

}