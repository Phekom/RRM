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
    fun insertSection(section: SectionPointDTO)

    @Query(
        "SELECT EXISTS(SELECT * FROM SECTION_POINT_TABLE " +
            "WHERE sectionId = :sectionId AND projectId LIKE :projectId " +
            "AND jobId LIKE :jobId AND ABS(pointLocation - :pointLocation) <= 0.5)"
    )
    fun checkSectionExists(sectionId: Int, projectId: String?, jobId: String?, pointLocation: Double): Boolean

    @Query("INSERT INTO SECTION_POINT_TABLE (direction, linearId ,pointLocation,sectionId ,projectId ,  jobId ) VALUES (:direction ,:linearId ,:pointLocation  ,:sectionId ,:projectId ,:jobId)")
    fun insertSection(
        direction: String,
        linearId: String,
        pointLocation: Double,
        sectionId: Int,
        projectId: String?,
        jobId: String?
    ): Long

    @Query("SELECT * FROM SECTION_POINT_TABLE WHERE  projectId = :projectId")
    fun getPointSectionData(projectId: String): SectionPointDTO

    @Query("SELECT * FROM SECTION_POINT_TABLE WHERE sectionId = :sectionId AND projectId LIKE :projectId AND jobId LIKE :jobId")
    fun getExistingSection(
        sectionId: Int,
        projectId: String?,
        jobId: String?
    ): LiveData<SectionPointDTO>

    @Query("DELETE FROM SECTION_POINT_TABLE")
    fun deleteAll()
}
