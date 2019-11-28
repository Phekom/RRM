package za.co.xisystems.itis_rrm.data.localDB.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import za.co.xisystems.itis_rrm.data.localDB.entities.LookupOptionDTO

/**
 * Created by Francis Mahlava on 2019/11/21.
 */

@Dao
interface LookupOptionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLookupOptions( lookupOptions : List<LookupOptionDTO>)

    @Query("SELECT * FROM LOOKUP_OPTION_TABLE WHERE valueMember = :valueMember AND lookupName =:lookupName")
    fun checkLookupOptionExists(valueMember: String, lookupName: String): Boolean

//    @Query("SELECT * FROM LOOKUP_OPTION_TABLE ")
//    fun getWorkflow() : LiveData<ArrayList<LookupOptionDTO>>
//
//
//    @Query("SELECT * FROM LOOKUP_OPTION_TABLE WHERE actId = :currentActId")
//    fun getNextRouteId(currentActId: Int): LiveData<Long>


    @Query("SELECT * FROM LOOKUP_OPTION_TABLE WHERE valueMember = :valueMember AND lookupName =:lookupName")
    fun getLookupOptionsForLookupNameAndValueMember(valueMember: Int, lookupName: String): LiveData<LookupOptionDTO>


    @Query("SELECT * FROM LOOKUP_OPTION_TABLE WHERE lookupName = :lookupName")
    fun getAllLookupOptionsForLookupName(lookupName: String): LiveData<List<LookupOptionDTO>>


    @Query("DELETE FROM LOOKUP_OPTION_TABLE")
    fun deleteAll()

//    @Query("SELECT * FROM LOOKUP_OPTION_TABLE LEFT JOIN WORKFLOW_TABLE ON  WORKFLOW_TABLE.workflowId = LOOKUP_OPTION_TABLE.workflowId = :itemWorkflowId")
}