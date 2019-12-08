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
    suspend fun insertEntities(intities : SectionItemDTO )

    @Query("INSERT INTO SECTION_ITEM_TABLE (sectionItemId, itemCode, description)VALUES(:sectionItemId, :itemCode,:description)")
    fun insertSectionitem(description : String  ,itemCode: String, sectionItemId: String)

//    @Query("SELECT sectionItemId FROM SECTION_ITEM_TABLE WHERE itemCode + :itemId ")

    @Query("SELECT sectionItemId FROM SECTION_ITEM_TABLE WHERE itemCode = :itemCode")
    fun getSectionItemId(itemCode: String): String


    @Query("SELECT * FROM SECTION_ITEM_TABLE WHERE sectionItemId = :sectionItemId")
    fun getDescriptionFromSectionItemId(sectionItemId: String) : LiveData<SectionItemDTO>

    @Query("SELECT * FROM SECTION_ITEM_TABLE")
    fun getAllSectionItems() : LiveData<SectionItemDTO>



//    sectionItemId
}