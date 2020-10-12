package za.co.xisystems.itis_rrm.data.localDB.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import za.co.xisystems.itis_rrm.data.localDB.entities.WorkFlowsDTO

/**
 * Created by Francis Mahlava on 2019/12/04.
 */

@Dao
interface WorkflowsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkFlows(workFlows: WorkFlowsDTO)

    @Query("SELECT * FROM WORKFLOWs_TABLE ")
    fun getWorkflows(): LiveData<List<WorkFlowsDTO>>

    @Query("DELETE FROM WORKFLOWs_TABLE")
    fun deleteAll()
}
