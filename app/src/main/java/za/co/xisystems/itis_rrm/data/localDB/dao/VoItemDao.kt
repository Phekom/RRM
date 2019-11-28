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
    suspend fun insertVoItem( voItems : List<VoItemDTO> )

    @Query("SELECT * FROM JOB_VO_ITEM ")
    fun getAllVoltem() : LiveData<List<VoItemDTO>>

    @Query("SELECT * FROM JOB_VO_ITEM WHERE contractVoId = :contractVoId")
    fun checkIfVoItemExist(contractVoId: String): LiveData<List<VoItemDTO>>

    @Query("SELECT * FROM JOB_VO_ITEM WHERE projectId = :projectId")
    fun getVoItemForProjectItemId(projectId: String): LiveData<List<VoItemDTO>>

    @Query("DELETE FROM JOB_VO_ITEM")
    fun deleteAllVoItem()


}