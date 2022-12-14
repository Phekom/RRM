package za.co.xisystems.itis_rrm.data.localDB.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import za.co.xisystems.itis_rrm.data.localDB.entities.ProjectSectionDTO
import za.co.xisystems.itis_rrm.data.localDB.views.SectionMarker
import za.co.xisystems.itis_rrm.domain.ProjectSectionSelector
import za.co.xisystems.itis_rrm.domain.ProjectSelector
import za.co.xisystems.itis_rrm.domain.SectionBorder

/**
 * Created by Francis Mahlava on 2019/11/21.
 */

@Dao
interface ProjectSectionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSections(section: ProjectSectionDTO)

    @Query("SELECT * FROM PROJECT_SECTION_TABLE WHERE sectionId = :sectionId")
    fun checkSectionExists(sectionId: String): Boolean

    @Query("SELECT * FROM PROJECT_SECTION_TABLE WHERE section = :section AND projectId LIKE :projectId")
    fun checkSectionNewExists(section: Int, projectId: String?): Boolean

    @Query(
        "INSERT INTO PROJECT_SECTION_TABLE " +
            "(sectionId, route ,section ,startKm ,  endKm ,direction ,projectId )" +
            " VALUES (:sectionId ,:route ,:section ,:startKm ,:endKm ,:direction ,:projectId)"
    )
    fun insertSection(
        sectionId: String,
        route: String,
        section: String,
        startKm: Double?,
        endKm: Double?,
        direction: String?,
        projectId: String
    ): Long

    @Query(
        "SELECT sectionId, " +
            ":kmMarker - (endKm + 0.0001) as pointLocation FROM PROJECT_SECTION_TABLE " +
            "WHERE route = :route AND pointLocation > 0 ORDER BY pointLocation LIMIT 1"
    )
    fun findRealSectionStartKm(
        route: String,
        kmMarker: Double
    ): SectionMarker?

    @Query(
        "SELECT sectionId, endKm as pointLocation " +
            "FROM PROJECT_SECTION_TABLE WHERE route = :route AND endKm - :kmMarker > 0 " +
            "ORDER BY (endkm - :kmMarker) LIMIT 1"
    )
    fun findRealSectionEndKm(
        route: String,
        kmMarker: Double
    ): SectionMarker

    @Query("UPDATE PROJECT_SECTION_TABLE SET direction =:direction WHERE projectId = :projectId")
    fun updateSectionDirection(direction: String?, projectId: String?): Int

    @Query("SELECT * FROM PROJECT_SECTION_TABLE ")
    fun getAllItemsForAllProjects(): LiveData<List<ProjectSectionDTO>>

    @Query("SELECT route FROM PROJECT_SECTION_TABLE WHERE sectionId = :sectionId")
    fun getRouteForProjectSectionId(sectionId: String?): String

    @Query("SELECT section FROM PROJECT_SECTION_TABLE WHERE sectionId = :sectionId")
    fun getSectionForProjectSectionId(sectionId: String?): String

    @Query(
        "SELECT sectionId FROM PROJECT_SECTION_TABLE " +
            "WHERE section = :section  AND route = :linearId AND projectId = :projectId " +
            "AND :pointLocation BETWEEN startKm AND endKm " +
            "ORDER BY endKm LIMIT 1"
    )
    fun getSectionByRouteSectionProject(
        section: String,
        linearId: String?,
        projectId: String?,
        pointLocation: Double
    ): String?

    @Query("SELECT * FROM PROJECT_SECTION_TABLE WHERE sectionId LIKE :sectionId")
    fun getLiveSection(sectionId: String): LiveData<ProjectSectionDTO>

    @Query("SELECT * FROM PROJECT_SECTION_TABLE WHERE sectionId LIKE :sectionId")
    fun getSection(sectionId: String): ProjectSectionDTO

    @Query("DELETE FROM PROJECT_SECTION_TABLE")
    fun deleteAll()

    @Query(
        "SELECT section, endKm as kmMarker FROM PROJECT_SECTION_TABLE " +
            "WHERE route = :linearId AND direction = :direction AND " +
            ":pointLocation > endKm ORDER BY (:pointLocation - endKm) LIMIT 1"
    )
    fun findClosestEndKm(linearId: String, pointLocation: Double, direction: String): SectionBorder?

    @Query(
        "SELECT section, startKm as kmMarker FROM PROJECT_SECTION_TABLE " +
            "WHERE route = :linearId AND direction = :direction AND " +
            ":pointLocation < startKm ORDER BY (startKm - :pointLocation) LIMIT 1"
    )
    fun findClosestStartKm(linearId: String, pointLocation: Double, direction: String): SectionBorder?

    @Query("SELECT * FROM PROJECT_SECTION_TABLE WHERE sectionId = :projectSectionId")
    fun getProjectSection(projectSectionId: String): ProjectSectionDTO

    @Query("SELECT projectId, sectionId, route, section,direction FROM PROJECT_SECTION_TABLE WHERE projectId = :projectId ORDER BY section")
    fun getProjectSectionsSelectors(projectId: String): List<ProjectSectionSelector>
}
