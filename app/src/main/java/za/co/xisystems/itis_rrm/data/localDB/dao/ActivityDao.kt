package za.co.xisystems.itis_rrm.data.localDB.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import za.co.xisystems.itis_rrm.data.localDB.entities.ActivityDTO

/**
 * Created by Francis Mahlava on 2019/11/21.
 */

@Dao
interface ActivityDao {
//
@Insert(onConflict = OnConflictStrategy.REPLACE)
suspend fun insertActivitys(activities: ActivityDTO)

    @Query("INSERT INTO ACTIVITY_TABLE ( actId,  actTypeId, approvalId, sContentId,  actName, descr) VALUES ( :ACT_ID, :ACT_NAME, :ACT_TYPE_ID, :APPROVAL_ID,  :DESCRIPTION,:CONTENT_ID)")
    fun insertActivity(
        ACT_ID: Long,
        ACT_TYPE_ID: Long?,
        APPROVAL_ID: Long?,
        CONTENT_ID: Long?,
        ACT_NAME: String?,
        DESCRIPTION: String?
    )

    @Query("SELECT * FROM ACTIVITY_TABLE ")
    fun getAllActivities(): LiveData<List<ActivityDTO>>

    @Query("DELETE FROM ACTIVITY_TABLE")
    fun deleteAll()
}
