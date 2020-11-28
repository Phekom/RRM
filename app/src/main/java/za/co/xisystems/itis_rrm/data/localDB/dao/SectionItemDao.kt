package za.co.xisystems.itis_rrm.data.localDB.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import za.co.xisystems.itis_rrm.data.localDB.entities.SectionItemDTO

/**
 * Created by Francis Mahlava on 2019/11/27.
 */
@Dao
interface SectionItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSectionItem(activitySections: SectionItemDTO)

    @Query("SELECT * FROM SECTION_ITEM_TABLE WHERE itemCode = :itemCode")
    suspend fun checkIfSectionItemsExist(itemCode: String?): Boolean

    @Query("INSERT INTO SECTION_ITEM_TABLE (sectionItemId, itemCode, description)VALUES(:sectionItemId, :itemCode,:description)")
    suspend fun insertSectionItem(description: String, itemCode: String, sectionItemId: String)

//    @Query("SELECT sectionItemId FROM SECTION_ITEM_TABLE WHERE itemCode + :itemId ")

    @Query("SELECT sectionItemId FROM SECTION_ITEM_TABLE WHERE itemCode LIKE :itemCode")
    suspend fun getSectionItemId(itemCode: String): String

    @Query("SELECT * FROM SECTION_ITEM_TABLE WHERE sectionItemId LIKE :sectionItemId")
    suspend fun getDescriptionFromSectionItemId(sectionItemId: String): List<SectionItemDTO>

    @Query("SELECT * FROM SECTION_ITEM_TABLE")
    suspend fun getSectionItems(): LiveData<List<SectionItemDTO>>

    @Query("SELECT * FROM SECTION_ITEM_TABLE ORDER BY  itemCode  ASC")
    suspend fun getAllSectionItems(): LiveData<List<SectionItemDTO>>

    @Query("DELETE FROM SECTION_ITEM_TABLE")
    suspend fun deleteAll()

    @Query("SELECT * FROM SECTION_ITEM_TABLE  WHERE sectionItemId IN (SELECT DISTINCT sectionItemId FROM PROJECT_ITEM_TABLE WHERE projectId = :projectId) ORDER BY itemCode ASC")
    suspend fun getFilteredSectionItems(projectId: String): LiveData<List<SectionItemDTO>>
}
