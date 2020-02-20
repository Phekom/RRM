package za.co.xisystems.itis_rrm.data.localDB.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import za.co.xisystems.itis_rrm.data.localDB.entities.ItemDTOTemp
import za.co.xisystems.itis_rrm.data.localDB.entities.ProjectItemDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.ItemSectionDTO

/**
 * Created by Francis Mahlava on 2019/11/21.
 */


@Dao
interface ItemDao_Temp {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems( item : ItemDTOTemp) : Long

    @Query("SELECT * FROM PROJECT_ITEM_TABLE_TEMP WHERE itemId = :itemId")
    fun checkItemExistsItemId(itemId: String): Boolean

    @Query("SELECT * FROM PROJECT_ITEM_TABLE_TEMP WHERE projectId = :projectId ")
    fun getAllProjecItems(projectId: String):  LiveData<List<ItemDTOTemp>>

//    @Query("SELECT * FROM PROJECT_ITEM_TABLE_TEMP WHERE itemId = :itemId")
//    fun getItemForItemId(itemId: String): LiveData<ItemDTO>




//    @Query("SELECT sectionItemId FROM PROJECT_ITEM_TABLE_TEMP WHERE itemId = :itemId")
//    fun getSectionItemId(itemId: String): String
//
//    @Query("INSERT INTO PROJECT_ITEM_TABLE_TEMP (itemId ,itemCode,descr, itemSections, tenderRate, uom, workflowId,sectionItemId, quantity, estimateId, projectId) VALUES (:itemId, :itemCode,:descr, :itemSections, :tenderRate, :uom, :workflowId, :sectionItemId, :quantity, :estimateId, :projectId)")
//    fun insertItem(itemId :String, itemCode :String?, descr :String?, itemSections : ArrayList<ItemSectionDTO>, tenderRate :Double, uom :String?, workflowId :Int?, sectionItemId :String?, quantity :Double, estimateId :String?, projectId :String)
//
//    @Query("SELECT descr FROM PROJECT_ITEM_TABLE_TEMP WHERE itemId = :itemId")
//    fun getProjectItemDescription(itemId: String): String
//
//    @Query("SELECT uom FROM PROJECT_ITEM_TABLE_TEMP WHERE itemId = :itemId")
//    fun getUOMForProjectItemId(itemId: String): String
//


//    @Query("SELECT * FROM PROJECT_ITEM_TABLE_TEMP WHERE itemId LIKE :itemId")
//    fun getItemForItemId(itemId: String): LiveData<ProjectItemDTO>
//
//    @Query("SELECT * FROM PROJECT_ITEM_TABLE_TEMP WHERE sectionItemId LIKE :sectionItemId")
//    fun getItemForItemCode(sectionItemId: String): LiveData<List<ProjectItemDTO>>
//
//    @Query("SELECT * FROM PROJECT_ITEM_TABLE_TEMP WHERE projectId LIKE :projectId ORDER BY itemCode ASC "  )
//    fun getAllItemsForProjectId(projectId: String): LiveData<List<ProjectItemDTO>>

//
//    @Query("SELECT * FROM PROJECT_ITEM_TABLE_TEMP WHERE sectionItemId LIKE :sectionItem AND projectId LIKE :projectId")
//    fun getAllItemsForSectionItem(sectionItem : String, projectId : String ): LiveData<List<ProjectItemDTO>>
//

    @Query("DELETE FROM PROJECT_ITEM_TABLE_TEMP")
    fun deleteAll()

    @Delete
   fun deleteItem(item: ItemDTOTemp)


    @Query("DELETE FROM PROJECT_ITEM_TABLE_TEMP WHERE jobId = :jobId")
    fun deleteItemList(jobId: String)

    @Query("DELETE FROM PROJECT_ITEM_TABLE_TEMP WHERE itemId = :itemId")
    fun deleteItemfromList(itemId: String)






}