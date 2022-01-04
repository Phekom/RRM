package za.co.xisystems.itis_rrm.data.localDB.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import za.co.xisystems.itis_rrm.data.localDB.entities.LookupDTO

/**
 * Created by Francis Mahlava on 2019/11/21.
 */

@Dao
interface LookupDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertLookup(lookups: LookupDTO): Long

    @Query("SELECT * FROM LOOKUP_TABLE WHERE lookupName = :lookupName")
    fun checkIfLookupExist(lookupName: String): Boolean

//    @Query("INSERT INTO LOOKUP_TABLE (lookupName, lookupOptions) VALUES (:lookupName, :lookupOptions)")
//    fun insertLookup(lookupName: String,lookupOptions: ArrayList<LookupOptionDTO>)

//    @Query("SELECT * FROM LOOKUP_TABLE ")
//    fun getWorkflow() : LiveData<List<LookupDTO>>
//
//
//    @Query("SELECT * FROM LOOKUP_TABLE WHERE actId = :currentActId")
//    fun getNextRouteId(currentActId: Int): LiveData<Long>
//
//
//    @Query("SELECT * FROM LOOKUP_TABLE LEFT JOIN WORKFLOW_TABLE ON  WORKFLOW_TABLE.workflowId = LOOKUP_TABLE.workflowId = :itemWorkflowId")
//    fun getHighestNextWorkflowId(itemWorkflowId: Long): LiveData<Long>
//
//
//    @Query("SELECT * FROM LOOKUP_TABLE WHERE workFlowId = :workFlowId")
//    fun getWorkFlowRouteForWorkflowId(workFlowId : Long ): LiveData<List<Long>>

    @Query("DELETE FROM LOOKUP_TABLE")
    fun deleteAll()
}
