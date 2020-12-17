package za.co.xisystems.itis_rrm.data.localDB.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import za.co.xisystems.itis_rrm.data.localDB.entities.ItemSectionDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.ProjectItemDTO

/**
 * Created by Francis Mahlava on 2019/11/21.
 */

@Dao
interface ProjectItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(item: ProjectItemDTO)

    @Query("SELECT EXiSTS (SELECT * FROM PROJECT_ITEM_TABLE WHERE itemId = :itemId)")
    fun checkItemExistsItemId(itemId: String): Boolean

    @Query("SELECT sectionItemId FROM PROJECT_ITEM_TABLE WHERE itemId = :itemId")
    fun getSectionItemId(itemId: String): String

    @Query("INSERT INTO PROJECT_ITEM_TABLE (itemId ,itemCode,descr, itemSections, tenderRate, uom, workflowId,sectionItemId, quantity, estimateId, projectId) VALUES (:itemId, :itemCode,:descr, :itemSections, :tenderRate, :uom, :workflowId, :sectionItemId, :quantity, :estimateId, :projectId)")
    fun insertItem(
        itemId: String,
        itemCode: String?,
        descr: String?,
        itemSections: ArrayList<ItemSectionDTO>,
        tenderRate: Double,
        uom: String?,
        workflowId: Int?,
        sectionItemId: String?,
        quantity: Double,
        estimateId: String?,
        projectId: String
    ): Long

    @Query("SELECT descr FROM PROJECT_ITEM_TABLE WHERE itemId = :itemId")
    fun getProjectItemDescription(itemId: String): String

    @Query("SELECT uom FROM PROJECT_ITEM_TABLE WHERE itemId = :itemId")
    fun getUOMForProjectItemId(itemId: String): String

    @Query("SELECT tenderRate FROM PROJECT_ITEM_TABLE WHERE itemId = :itemId")
    fun getTenderRateForProjectItemId(itemId: String): Double

    @Query("SELECT * FROM PROJECT_ITEM_TABLE ")
    fun getAllItemsForAllProjects(): LiveData<List<ProjectItemDTO>>

    @Query("SELECT * FROM PROJECT_ITEM_TABLE WHERE itemId LIKE :itemId")
    fun getItemForItemId(itemId: String): LiveData<ProjectItemDTO>

    @Query("SELECT * FROM PROJECT_ITEM_TABLE WHERE sectionItemId LIKE :sectionItemId")
    fun getItemForItemCode(sectionItemId: String): LiveData<List<ProjectItemDTO>>

    @Query("SELECT * FROM PROJECT_ITEM_TABLE WHERE projectId LIKE :projectId ORDER BY itemCode ASC ")
    fun getAllItemsForProjectId(projectId: String): LiveData<List<ProjectItemDTO>>

    @Query("SELECT DISTINCT sectionItemId FROM PROJECT_ITEM_TABLE WHERE projectId = :projectId")
    fun getSectionItemIdsForProjectId(projectId: String): List<String>

    @Query("SELECT * FROM PROJECT_ITEM_TABLE WHERE sectionItemId LIKE :sectionItem AND projectId LIKE :projectId")
    fun getAllItemsForSectionItemByProject(
        sectionItem: String,
        projectId: String
    ): LiveData<List<ProjectItemDTO>>

    @Query("DELETE FROM PROJECT_ITEM_TABLE")
    fun deleteAll()
}
