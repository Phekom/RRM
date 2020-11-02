package za.co.xisystems.itis_rrm.data.localDB.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import za.co.xisystems.itis_rrm.data.localDB.entities.WorkFlowDTO

/**
 * Created by Francis Mahlava on 2019/11/21.
 */

@Dao
interface WorkFlowDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkFlow(workFlow: WorkFlowDTO)

    @Query("SELECT * FROM WORKFLOW_TABLE WHERE workflowId = :workflowId")
    fun checkWorkFlowExistsWorkflowID(workflowId: Long): Boolean

//    @Query("INSERT INTO WORKFLOW_TABLE (dateCreated,errorRouteId, revNo, startRouteId, userId, wfHeaderId, workFlowRoute, workflowId) VALUES (:dateCreated,:errorRouteId, :revNo, :startRouteId, :userId, :wfHeaderId, :workFlowRoute, :workflowId)")
//    fun insertWorkFlow(dateCreated: String,errorRouteId: Long, revNo: Long, startRouteId: Long, userId: Long, wfHeaderId: Long, workFlowRoute: ArrayList<WorkFlowRouteDTO>?, workflowId: Long)

    @Query("SELECT * FROM WORKFLOW_TABLE ")
    fun getWorkflows(): LiveData<List<WorkFlowDTO>>

    @Query("DELETE FROM WORKFLOW_TABLE")
    fun deleteAll()
}
