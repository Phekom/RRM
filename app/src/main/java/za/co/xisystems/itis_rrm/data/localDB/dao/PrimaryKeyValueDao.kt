package za.co.xisystems.itis_rrm.data.localDB.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import za.co.xisystems.itis_rrm.data.localDB.entities.PrimaryKeyValueDTO

/**
 * Created by Francis Mahlava on 2019/11/22.
 */

@Dao
interface PrimaryKeyValueDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrimaryKeyValues( primaryKeyValue : PrimaryKeyValueDTO )

    @Query("INSERT INTO PRIMARY_KEY_VALUE_TABLE (primary_key, valueString, trackRouteId, activityId) VALUES (:primary_key, :value, :trackRouteId, :activityId)")
    fun insertPrimaryKeyValue(primary_key: String?, value: String?, trackRouteId: String?,activityId: Int)

    @Query("SELECT * FROM PRIMARY_KEY_VALUE_TABLE ")
    fun getAllPrimaryKeyValue() : LiveData<List<PrimaryKeyValueDTO>>

    @Query("SELECT * FROM PRIMARY_KEY_VALUE_TABLE WHERE trackRouteId = :trackRouteId")
    fun checkPrimaryKeyValuesExistTrackRouteId(trackRouteId: String): Boolean

    @Query("SELECT * FROM PRIMARY_KEY_VALUE_TABLE WHERE trackRouteId = :trackRouteId")
    fun getPrimaryKeyValuesFromTrackRouteId(trackRouteId: String): LiveData<PrimaryKeyValueDTO>

    @Query("SELECT * FROM PRIMARY_KEY_VALUE_TABLE WHERE valueString = :jobId")
    fun getPrimaryKeyValueForJobId(jobId: String): LiveData<PrimaryKeyValueDTO>


    @Query("DELETE FROM PRIMARY_KEY_VALUE_TABLE")
    fun deleteAll()

}