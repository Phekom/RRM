package za.co.xisystems.itis_rrm.data.localDB.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import za.co.xisystems.itis_rrm.data.localDB.entities.PrimaryKeyValueDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.ToDoListEntityDTO

/**
 * Created by Francis Mahlava on 2019/11/21.
 */

@Dao
interface EntitiesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntities(intities: ToDoListEntityDTO)

    @Query("INSERT INTO TODO_ENTITY_TABLE (trackRouteIdString, actionable, activityId, currentRouteId, data, description, entities, entityName, location, primaryKeyValues, recordVersion, jobId) VALUES (:trackRouteId, :actionable, :activityId, :currentRouteId, :data , :description, :entities, :entityName, :location, :primaryKeyValues, :recordVersion, :jobId)")
    fun insertEntitie(
        trackRouteId: String?,
        actionable: Int,
        activityId: Int,
        currentRouteId: Int,
        data: String?,
        description: String?,
        entities: ArrayList<ToDoListEntityDTO>,
        entityName: String?,
        location: String?,
        primaryKeyValues: ArrayList<PrimaryKeyValueDTO>,
        recordVersion: Int,
        jobId: String?
    )

    @Query("SELECT * FROM TODO_ENTITY_TABLE WHERE trackRouteIdString = :trackRouteId")
    fun checkIfEntitiesExist(trackRouteId: String?): Boolean

    @Query("SELECT * FROM TODO_ENTITY_TABLE ")
    fun getAllEntities(): LiveData<List<ToDoListEntityDTO>>

//    @Query("SELECT * FROM JOB_ITEM_MEASURE WHERE actId = :actId ORDER BY jimNo ASC")
//    fun getJobApproveMeasureForActivityId(actId: Int): LiveData<List<ToDoListEntityDTO>>

    @Query("SELECT * FROM TODO_ENTITY_TABLE WHERE trackRouteIdString = :trackRouteId")
    fun getEntitiesForTrackRouteId(trackRouteId: String): LiveData<ToDoListEntityDTO>

    @Query("SELECT * FROM TODO_ENTITY_TABLE WHERE activityId = :actId")
    fun getEntitiesListForActivityId(actId: Int): LiveData<List<ToDoListEntityDTO>>

    @Query("SELECT * FROM TODO_ENTITY_TABLE WHERE activityId = :actId AND jobId = :jobId")
    fun getEntitiesForJobIdAndActId(jobId: String, actId: Int): LiveData<List<ToDoListEntityDTO>>

//    @Query("SELECT * FROM TODO_ENTITY_TABLE WHERE jobId = :jobId")
//    fun getEntitiesForJobId(jobId : String): LiveData<List<ToDoListEntityDTO>>

    @Query("SELECT jobId FROM TODO_ENTITY_TABLE WHERE jobId = jobId")
    fun getJobIdForEntities(): String?

    @Query("DELETE FROM TODO_ENTITY_TABLE")
    fun deleteAll()
}
