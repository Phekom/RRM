package za.co.xisystems.itis_rrm.data.localDB.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import za.co.xisystems.itis_rrm.data.localDB.entities.ItemSectionDTO

/**
 * Created by Francis Mahlava on 2019/11/21.
 */


@Dao
interface ItemSectionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntities( intities : List<ItemSectionDTO> )
//
//    @Query("SELECT * FROM PROJECT_ITEM_TABLE WHERE itemId = :itemId")
//    fun checkItemExistsItemId(itemId: String): LiveData<List<ItemDTO>>
//
//    @Query("SELECT * FROM PROJECT_ITEM_TABLE ")
//    fun getAllItemsForAllProjects() : LiveData<List<ItemDTO>>
//
//
//    @Query("SELECT * FROM PROJECT_ITEM_TABLE WHERE itemId = :itemId")
//    fun getItemForItemId(itemId: String): LiveData<ItemDTO>
//
//
//    @Query("SELECT * FROM PROJECT_ITEM_TABLE WHERE projectId = :projectId")
//    fun getAllItemsForProjectId(projectId: String): LiveData<List<ItemDTO>>
//
//
//    @Query("SELECT * FROM PROJECT_ITEM_TABLE WHERE sectionItemId = :sectionItem AND projectId = :projectId")
//    fun getAllItemsForSectionItem(sectionItem : String, projectId : String ): LiveData<List<ItemDTO>>
//
//
    @Query("DELETE FROM ITEM_SECTION_TABLE")
    fun deleteAll()
}