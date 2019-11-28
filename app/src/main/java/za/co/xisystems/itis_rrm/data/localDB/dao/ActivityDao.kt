package za.co.xisystems.itis_rrm.data.localDB.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import za.co.xisystems.itis_rrm.data.localDB.entities.ActivityDTO

/**
 * Created by Francis Mahlava on 2019/11/21.
 */

@Dao
interface ActivityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntities( activities : List<ActivityDTO> )

//    @Query("SELECT * FROM ActivityDTO WHERE trackRouteId = :trackRouteId")
//    fun checkIfEntitiesExist(trackRouteId: String): LiveData<List<ActivityDTO>>
//
//    @Query("SELECT * FROM TABLE_ENTITY ")
//    fun getAllEntities() : LiveData<List<ActivityDTO>>
//
//
//    @Query("SELECT * FROM TABLE_ENTITY WHERE trackRouteId = :trackRouteId")
//    fun getEntitiesForTrackRouteId(trackRouteId: String): LiveData<ActivityDTO>
//
//
//    @Query("SELECT * FROM TABLE_ENTITY WHERE activityId = :actId")
//    fun getEntitiesListForActivityId(actId: String): LiveData<List<ActivityDTO>>
//
//
//    @Query("SELECT * FROM TABLE_ENTITY WHERE activityId = :actId AND jobId = :jobId")
//    fun getEntitiesForJobId(jobId : String, actId : Int ): LiveData<ArrayList<ActivityDTO>>
//
//
//    @Query("DELETE FROM TABLE_ENTITY")
//    fun deleteAllEntities()

}