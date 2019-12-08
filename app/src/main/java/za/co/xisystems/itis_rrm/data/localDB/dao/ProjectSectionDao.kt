package za.co.xisystems.itis_rrm.data.localDB.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import za.co.xisystems.itis_rrm.data.localDB.entities.ProjectSectionDTO

/**
 * Created by Francis Mahlava on 2019/11/21.
 */


@Dao
interface ProjectSectionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSections(section : ProjectSectionDTO)

    @Query("SELECT * FROM PROJECT_SECTION_TABLE WHERE sectionId = :sectionId")
    fun checkSectionExists(sectionId: String): Boolean

    @Query("INSERT INTO PROJECT_SECTION_TABLE (sectionId, route ,section ,startKm ,  endKm ,direction ,projectId ) VALUES (:sectionId ,:route ,:section ,:startKm ,:endKm ,:direction ,:projectId)")
    fun insertSection(sectionId: String,route: String,section: String,startKm: Double?,  endKm: Double?,direction: String?,projectId: String)


    @Query("SELECT * FROM PROJECT_SECTION_TABLE ")
    fun getAllItemsForAllProjects() : LiveData<List<ProjectSectionDTO>>

//
//    @Query("SELECT * FROM PROJECT_ITEM_TABLE WHERE itemId = :itemId")
//    fun getItemForItemId(itemId: String): LiveData<ItemDTO>


//    @Query("SELECT * FROM PROJECT_ITEM_TABLE WHERE projectId = :projectId")
//    fun getAllItemsForProjectId(projectId: String): LiveData<List<ItemDTO>>
//
//
//    @Query("SELECT * FROM PROJECT_ITEM_TABLE WHERE sectionItemId = :sectionItem AND projectId = :projectId")
//    fun getAllItemsForSectionItem(sectionItem : String, projectId : String ): LiveData<List<ItemDTO>>
//
//
//    @Query("DELETE FROM PROJECT_ITEM_TABLE")
//    fun deleteAll()
}