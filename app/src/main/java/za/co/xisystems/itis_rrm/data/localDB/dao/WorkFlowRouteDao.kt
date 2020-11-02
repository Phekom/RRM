package za.co.xisystems.itis_rrm.data.localDB.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import za.co.xisystems.itis_rrm.data.localDB.entities.WorkFlowRouteDTO

/**
 * Created by Francis Mahlava on 2019/11/21.
 */

@Dao
interface WorkFlowRouteDao {

//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insertWorkFlowRoutes( workFlowRoute : WorkFlowRouteDTO)

//    @Query("INSERT INTO WORKFLOW_ROUTE_TABLE ( workFlowRoute.ict, workflowId ) VALUES (:workFlowRoute, :workflowId)")
//    fun insertWorkFlowRoute( workFlowRoute : WorkFlowRouteDTO , workflowId: Long?) : Long

    @Query("INSERT INTO WORKFLOW_ROUTE_TABLE ( routeId, actId, nextRouteId, failRouteId, errorRouteId, canStart, workflowId ) VALUES (:routeId, :actId, :nextRouteId, :failRouteId, :errorRouteId, :canStart, :workflowId)")
    fun insertWorkFlowRoute(
        routeId: Long,
        actId: Long,
        nextRouteId: Long,
        failRouteId: Long,
        errorRouteId: Long,
        canStart: Long,
        workflowId: Long?
    ): Long

    @Query("SELECT * FROM WORKFLOW_ROUTE_TABLE WHERE routeId = :routeId")
    fun checkWorkFlowRouteExists(routeId: Long): Boolean

    @Query("SELECT * FROM WORKFLOW_ROUTE_TABLE ")
    fun getWorkflow(): LiveData<List<WorkFlowRouteDTO>>

    @Query("SELECT * FROM WORKFLOW_ROUTE_TABLE WHERE actId = :currentActId")
    fun getNextRouteId(currentActId: Int): Long

    @Query("SELECT * FROM WORKFLOW_ROUTE_TABLE LEFT JOIN WORKFLOW_TABLE ON  WORKFLOW_TABLE.workflowId = WORKFLOW_ROUTE_TABLE.workflowId = :itemWorkflowId")
    fun getHighestNextWorkflowId(itemWorkflowId: Long): Long

    @Query("SELECT * FROM WORKFLOW_ROUTE_TABLE WHERE workFlowId = :workFlowId")
    fun getWorkFlowRouteForWorkflowId(workFlowId: Long): Long

    @Query("DELETE FROM WORKFLOW_ROUTE_TABLE")
    fun deleteAll()
}
