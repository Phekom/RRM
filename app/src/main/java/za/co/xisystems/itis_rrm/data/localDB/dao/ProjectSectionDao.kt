package za.co.xisystems.itis_rrm.data.localDB.dao

import androidx.lifecycle.LiveData
import androidx.room.*
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

    @Query("SELECT * FROM PROJECT_SECTION_TABLE WHERE section = :section AND projectId LIKE :projectId")
    fun checkSectionNewExists(section: Int, projectId: String?): Boolean


    @Query("INSERT INTO PROJECT_SECTION_TABLE (sectionId, route ,section ,startKm ,  endKm ,direction ,projectId ) VALUES (:sectionId ,:route ,:section ,:startKm ,:endKm ,:direction ,:projectId)")
    fun insertSection(sectionId: String,route: String,section: String,startKm: Double?,  endKm: Double?,direction: String?,projectId: String)


    @Query("SELECT * FROM PROJECT_SECTION_TABLE ")
    fun getAllItemsForAllProjects() : LiveData<List<ProjectSectionDTO>>


    @Query("SELECT route FROM PROJECT_SECTION_TABLE WHERE sectionId = :sectionId")
    fun getRouteForProjectSectionId(sectionId: String): String

    @Query("SELECT section FROM PROJECT_SECTION_TABLE WHERE sectionId = :sectionId")
    fun getSectionForProjectSectionId(sectionId: String): String

    @Query("SELECT sectionId FROM PROJECT_SECTION_TABLE WHERE section = :sectionId  AND route = :linearId AND projectId = :projectId")
    fun getSectionByRouteSectionProject( sectionId: Int, linearId: String, projectId: String?) : LiveData<String>
//    fun getSectionByRouteSectionProject(linearId: String, sectionId: Int, direction: String, projectId: String?)

    @Query("SELECT * FROM PROJECT_SECTION_TABLE WHERE sectionId LIKE :sectionId")
    fun getSection(sectionId: String): LiveData<ProjectSectionDTO>






    //    @Query("SELECT * FROM PROJECT_ITEM_TABLE WHERE projectId = :projectId")
//    fun getAllItemsForProjectId(projectId: String): LiveData<List<ItemDTO>>
//
//
//    @Query("SELECT * FROM PROJECT_ITEM_TABLE WHERE sectionItemId = :sectionItem AND projectId = :projectId")
//    fun getAllItemsForSectionItem(sectionItem : String, projectId : String ): LiveData<List<ItemDTO>>
//
//
    @Query("DELETE FROM PROJECT_SECTION_TABLE")
    fun deleteAll()



}