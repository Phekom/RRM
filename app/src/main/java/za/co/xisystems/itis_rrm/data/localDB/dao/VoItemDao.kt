package za.co.xisystems.itis_rrm.data.localDB.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import za.co.xisystems.itis_rrm.data.localDB.entities.VoItemDTO

/**
 * Created by Francis Mahlava on 2019/11/21.
 */
@Dao
interface VoItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVoItems( voItem : VoItemDTO )


    @Query("INSERT INTO TABLE_JOB_VO_ITEM (projectVoId,itemCode,voDescr,descr,uom,rate,projectItemId,contractVoId, contractVoItemId, projectId) VALUES (:projectVoId,:itemCode, :voDescr,:descr, :uom, :rate,:projectItemId,:contractVoId,:contractVoItemId,:projectId )")
    fun insertVoItem(projectVoId: String,itemCode: String?,voDescr: String?,descr: String?,uom: String?,rate: Double?,projectItemId: String?,contractVoId: String?, contractVoItemId: String?, projectId: String)

    @Query("SELECT * FROM TABLE_JOB_VO_ITEM ")
    fun getAllVoltem() : LiveData<List<VoItemDTO>>

//    @Query("SELECT * FROM TABLE_JOB_VO_ITEM WHERE contractVoId = :contractVoId")
//    fun checkIfVoItemExist(contractVoId: String): LiveData<List<VoItemDTO>>

    @Query("SELECT * FROM TABLE_JOB_VO_ITEM WHERE projectVoId = :projectVoId")
    fun checkIfVoItemExist(projectVoId: String): Boolean

    @Query("SELECT * FROM TABLE_JOB_VO_ITEM WHERE projectId = :projectId")
    fun getVoItemsForProjectId(projectId: String): LiveData<List<VoItemDTO>>


    @Query("SELECT * FROM TABLE_JOB_VO_ITEM WHERE projectItemId = :projectItemId")
    fun getVoItemForProjectItemId(projectItemId: String): LiveData<VoItemDTO>

    @Query("DELETE FROM TABLE_JOB_VO_ITEM")
    fun deleteAll()


}



