package za.co.xisystems.itis_rrm.data.localDB.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import za.co.xisystems.itis_rrm.data.localDB.entities.SectionPointDTO

/**
 * Created by Francis Mahlava on 2019/11/21.
 */

@Dao
interface SectionPointDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSection(section: SectionPointDTO)

    @Query("SELECT * FROM SECTION_POINT_TABLE WHERE sectionId = :sectionId AND projectId LIKE :projectId AND jobId LIKE :jobId")
    fun checkSectionExists(sectionId: Int, projectId: String?, jobId: String?): Boolean

    @Query("INSERT INTO SECTION_POINT_TABLE (direction, linearId ,pointLocation,sectionId ,projectId ,  jobId ) VALUES (:direction ,:linearId ,:pointLocation  ,:sectionId ,:projectId ,:jobId)")
    fun insertSection(
        direction: String,
        linearId: String,
        pointLocation: Double,
        sectionId: Int,
        projectId: String?,
        jobId: String?
    )

    @Query("SELECT * FROM SECTION_POINT_TABLE WHERE  projectId LIKE :projectId")
    fun getPointSectionData(projectId: String?): LiveData<SectionPointDTO> // jobId: String jobId LIKE :jobId AND

    @Query("SELECT * FROM SECTION_POINT_TABLE WHERE sectionId = :sectionId AND projectId LIKE :projectId AND jobId LIKE :jobId")
    fun getExistingSection(
        sectionId: Int,
        projectId: String?,
        jobId: String?
    ): LiveData<SectionPointDTO>
//
//    @Query("SELECT route FROM SECTION_POINT_TABLE WHERE sectionId = :sectionId")
//    fun getRouteForProjectSectionId(sectionId: String): String
//
//    @Query("SELECT section FROM SECTION_POINT_TABLE WHERE sectionId = :sectionId")
//    fun getSectionForProjectSectionId(sectionId: String): String
//
//    @Query("SELECT sectionId FROM PROJECT_SECTION_TABLE WHERE section = :sectionId  AND route = :linearId AND projectId = :projectId")
//    fun getSectionByRouteSectionProject(linearId: String, sectionId: Int, projectId: String?) : String
// //    fun getSectionByRouteSectionProject(linearId: String, sectionId: Int, direction: String, projectId: String?)
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
    @Query("DELETE FROM SECTION_POINT_TABLE")
    fun deleteAll()
}
