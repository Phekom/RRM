package za.co.xisystems.itis_rrm.data.localDB.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import za.co.xisystems.itis_rrm.data.localDB.entities.InfoClassDTO

/**
 * Created by Francis Mahlava on 2019/11/26.
 */

@Dao
interface InfoClassDao {
//
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInfoClasses( intities : InfoClassDTO )

    @Query("INSERT INTO INFO_CLASS_TABLE (sLinkId, sInfoClassId,  wfId) VALUES ( :sLinkId, :sInfoClassId,  :wfId)")
    fun insertInfoClass( sLinkId: String, sInfoClassId: String?,  wfId: Int?)

    @Query("DELETE FROM INFO_CLASS_TABLE")
    fun deleteAll()
}