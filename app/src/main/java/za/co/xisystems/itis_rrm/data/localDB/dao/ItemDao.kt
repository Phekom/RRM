package za.co.xisystems.itis_rrm.data.localDB.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import za.co.xisystems.itis_rrm.data.localDB.entities.ItemDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.ItemSectionDTO

/**
 * Created by Francis Mahlava on 2019/11/21.
 */


@Dao
interface ItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems( item : ItemDTO)

    @Query("SELECT * FROM PROJECT_ITEM_TABLE WHERE itemId = :itemId")
    fun checkItemExistsItemId(itemId: String): Boolean

//    @Query("SELECT * FROM PROJECT_ITEM_TABLE WHERE itemId = :itemId")
//    fun getItemForItemId(itemId: String): LiveData<ItemDTO>

    @Query("SELECT sectionItemId FROM PROJECT_ITEM_TABLE WHERE itemId = :itemId")
    fun getSectionItemId(itemId: String): String

    @Query("INSERT INTO PROJECT_ITEM_TABLE (itemId ,itemCode,descr, itemSections, tenderRate, uom, workflowId,sectionItemId, quantity, estimateId, projectId) VALUES (:itemId, :itemCode,:descr, :itemSections, :tenderRate, :uom, :workflowId, :sectionItemId, :quantity, :estimateId, :projectId)")
    fun insertItem(itemId :String, itemCode :String?, descr :String?, itemSections : ArrayList<ItemSectionDTO>, tenderRate :Double, uom :String?, workflowId :Int?, sectionItemId :String?, quantity :Double, estimateId :String?, projectId :String)



    @Query("SELECT * FROM PROJECT_ITEM_TABLE ")
    fun getAllItemsForAllProjects() : LiveData<List<ItemDTO>>


    @Query("SELECT * FROM PROJECT_ITEM_TABLE WHERE itemId = :itemId")
    fun getItemForItemId(itemId: String): LiveData<ItemDTO>


    @Query("SELECT * FROM PROJECT_ITEM_TABLE WHERE projectId = :projectId")
    fun getAllItemsForProjectId(projectId: String): LiveData<List<ItemDTO>>


    @Query("SELECT * FROM PROJECT_ITEM_TABLE WHERE sectionItemId = :sectionItem AND projectId = :projectId")
    fun getAllItemsForSectionItem(sectionItem : String, projectId : String ): LiveData<List<ItemDTO>>


    @Query("DELETE FROM PROJECT_ITEM_TABLE")
    fun deleteAll()
}