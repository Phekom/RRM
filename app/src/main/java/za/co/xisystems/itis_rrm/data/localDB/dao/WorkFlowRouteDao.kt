package za.co.xisystems.itis_rrm.data.localDB.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import za.co.xisystems.itis_rrm.data.localDB.entities.WorkFlowRouteDTO

/**
 * Created by Francis Mahlava on 2019/11/21.
 */

@Dao
interface WorkFlowRouteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkFlows( workFlowRoute : List<WorkFlowRouteDTO>)

    @Query("SELECT * FROM WORKFLOW_ROUTE_TABLE WHERE routeId = :routeId")
    fun checkWorkFlowExistsWorkflowID(routeId: Long): Boolean

    @Query("SELECT * FROM WORKFLOW_ROUTE_TABLE ")
    fun getWorkflow() : LiveData<List<WorkFlowRouteDTO>>


    @Query("SELECT * FROM WORKFLOW_ROUTE_TABLE WHERE actId = :currentActId")
    fun getNextRouteId(currentActId: Int): Long


    @Query("SELECT * FROM WORKFLOW_ROUTE_TABLE LEFT JOIN WORKFLOW_TABLE ON  WORKFLOW_TABLE.workflowId = WORKFLOW_ROUTE_TABLE.workflowId = :itemWorkflowId")
    fun getHighestNextWorkflowId(itemWorkflowId: Long): Long


    @Query("SELECT * FROM WORKFLOW_ROUTE_TABLE WHERE workFlowId = :workFlowId")
    fun getWorkFlowRouteForWorkflowId(workFlowId : Long ): Long


    @Query("DELETE FROM WORKFLOW_ROUTE_TABLE")
    fun deleteAll()

}