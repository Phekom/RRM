package za.co.xisystems.itis_rrm.data.localDB.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import za.co.xisystems.itis_rrm.data.localDB.entities.ItemDTOTemp

/**
 * Created by Francis Mahlava on 2019/11/21.
 */

@Dao
interface ItemDaoTemp {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(item: ItemDTOTemp): Long

    @Query("SELECT * FROM PROJECT_ITEM_TABLE_TEMP WHERE itemId = :itemId")
    fun checkItemExistsItemId(itemId: String): Boolean

    @Query("SELECT * FROM PROJECT_ITEM_TABLE_TEMP WHERE projectId = :projectId AND jobId = :jobId")
    fun getAllProjecItems(projectId: String, jobId: String): LiveData<List<ItemDTOTemp>>

    @Query("SELECT * FROM PROJECT_ITEM_TABLE_TEMP WHERE itemId = :itemId")
    suspend fun getProjectItemById(itemId: String): ItemDTOTemp

    @Query("DELETE FROM PROJECT_ITEM_TABLE_TEMP")
    fun deleteAll()

    @Delete
    fun deleteItem(item: ItemDTOTemp)

    @Query("DELETE FROM PROJECT_ITEM_TABLE_TEMP WHERE jobId = :jobId")
    fun deleteItemList(jobId: String)

    @Query("DELETE FROM PROJECT_ITEM_TABLE_TEMP WHERE itemId = :itemId")
    fun deleteItemFromList(itemId: String): Int
}
