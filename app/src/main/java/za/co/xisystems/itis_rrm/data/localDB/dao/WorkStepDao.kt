package za.co.xisystems.itis_rrm.data.localDB.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import za.co.xisystems.itis_rrm.data.localDB.entities.WfWorkStepDTO

/**
 * Created by Francis Mahlava on 2019/12/04.
 */

@Dao
interface WorkStepDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkFlowStep(workSteps: WfWorkStepDTO)

    @Query("SELECT * FROM WorkStep_TABLE WHERE stepCode = :workCode")
    fun checkWorkFlowStepExistsWorkCode(workCode: String): Boolean

    @Query("SELECT * FROM WorkStep_TABLE WHERE descrip = :workDesc")
    fun checkWorkFlowStepExistsDesc(workDesc: String): Boolean

    @Query("INSERT INTO WorkStep_TABLE (stepCode, actTypeId ) VALUES ( :stepCode, :actId)")
    fun insertStepsCode(stepCode: String, actId: Int)

    @Query("UPDATE WorkStep_TABLE SET descrip = :descrip WHERE stepCode = :stepCode")
    fun updateStepsDesc(descrip: String?, stepCode: String)

    @Query("SELECT DISTINCT * FROM WorkStep_TABLE  WHERE actTypeId LIKE :eId")
    fun getWorkflowSteps(eId: Int): LiveData<List<WfWorkStepDTO>>

    @Query("DELETE FROM WorkStep_TABLE")
    fun deleteAll()
}
