package za.co.xisystems.itis_rrm.data.localDB.dao

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
    suspend fun insertWorkFlows( workFlows : List<WorkFlowDTO> )

//    @Query("SELECT * FROM WORKFLOW_TABLE WHERE workflowId = :workflowId")
//    fun checkWorkFlowExistsWorkflowID(workflowId: Long): LiveData<WorkFlowDTO>
//
//    @Query("SELECT * FROM WORKFLOW_TABLE ")
//    fun getWorkflow() : LiveData<List<WorkFlowDTO>>


//    @Query("SELECT * FROM WORKFLOW_TABLE WHERE trackRouteId = :trackRouteId")
//    fun getEntitiesForTrackRouteId(trackRouteId: String): LiveData<WorkFlowDTO>
//
//
//    @Query("SELECT * FROM WORKFLOW_TABLE WHERE activityId = :actId")
//    fun getEntitiesListForActivityId(actId: String): LiveData<List<WorkFlowDTO>>
//
//
//    @Query("SELECT * FROM WORKFLOW_TABLE WHERE activityId = :actId AND jobId = :jobId")
//    fun getEntitiesForJobId(jobId : String, actId : Int ): LiveData<ArrayList<WorkFlowDTO>>


    @Query("DELETE FROM WORKFLOW_TABLE")
    fun deleteAll()

}