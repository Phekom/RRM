package za.co.xisystems.itis_rrm.data.localDB.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import za.co.xisystems.itis_rrm.data.localDB.entities.WF_WorkStepDTO

/**
 * Created by Francis Mahlava on 2019/12/04.
 */

@Dao
interface WorkStepDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkFlowStep(workSteps: WF_WorkStepDTO)

    @Query("SELECT * FROM WorkStep_TABLE WHERE Step_Code = :workCode")
    fun checkWorkFlowStepExistsWorkCode(workCode: String): Boolean

    @Query("SELECT * FROM WorkStep_TABLE WHERE Descrip = :workDesc")
    fun checkWorkFlowStepExistsDesc(workDesc: String): Boolean

    @Query("INSERT INTO WorkStep_TABLE (Step_Code, Act_Type_id ) VALUES (:Step_Code,:actId)")
    fun insertStepsCode(Step_Code: String, actId: Int)

    @Query("UPDATE WorkStep_TABLE SET Descrip =:Descrip WHERE Step_Code = :Step_Code")
    fun updateStepsDesc(Descrip: String?, Step_Code: String)

//    @Query("INSERT INTO WorkStep_TABLE (Descrip ) VALUES (:Descrip)")
//    fun insertStepsDesc(Descrip :String)

//    @Query("SELECT * FROM WorkStep_TABLE ")
//    fun getWorkflowSteps() : LiveData<List<WF_WorkStepDTO>>

    @Query("SELECT DISTINCT * FROM WorkStep_TABLE  WHERE Act_Type_id LIKE :eId")
    fun getWorkflowSteps(eId: Int): LiveData<List<WF_WorkStepDTO>>

    @Query("DELETE FROM WorkStep_TABLE")
    fun deleteAll()
}
