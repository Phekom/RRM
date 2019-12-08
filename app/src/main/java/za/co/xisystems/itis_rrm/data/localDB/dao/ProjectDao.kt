package za.co.xisystems.itis_rrm.data.localDB.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import za.co.xisystems.itis_rrm.data.localDB.entities.ItemDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.ProjectDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.ProjectSectionDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.VoItemDTO

/**
 * Created by Francis Mahlava on 2019/11/22.
 */

@Dao
interface ProjectDao {

//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insertProjects(project : List<ProjectDTO>)

//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insertProject(project : ProjectDTO)


    @Query("INSERT INTO PROJECT_TABLE (projectId,descr, endDate, items, projectCode, projectMinus, projectPlus,projectSections, voItems, contractId) VALUES (:projectId,:descr, :endDate, :items, :projectCode, :projectMinus, :projectPlus, :projectSections, :voItems, :contractId)")
    fun insertProject(projectId: String, descr: String?, endDate: String?,
                      items: ArrayList<ItemDTO>?, projectCode: String?, projectMinus: String?, projectPlus: String?,
                      projectSections: ArrayList<ProjectSectionDTO>?, voItems: ArrayList<VoItemDTO>?, contractId : String?) : Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateProject(project : ProjectDTO)

    @Query("SELECT projectId FROM PROJECT_TABLE WHERE projectId = projectId")
    fun getProjectId() : String


    @Query("SELECT * FROM PROJECT_TABLE ")
    fun getAllProjects() : LiveData<List<ProjectDTO>>

    @Query("SELECT * FROM PROJECT_TABLE WHERE projectId = :projectId")
    fun checkProjectExists(projectId: String): Boolean

//    @Query("SELECT * FROM PROJECT_TABLE WHERE contractId = :contractId")
//    fun getAllProjectsByContract(contractId: String): LiveData<List<ProjectDTO>>
//
//    @Query("SELECT * FROM PROJECT_TABLE WHERE projectId = :projectId")
//    fun getProjectDescription(projectId: String): String
//
//
//    @Query("SELECT * FROM PROJECT_TABLE WHERE projectId = :projectId")
//    fun getProjectById(projectId: String): LiveData<ProjectDTO>
//
//    @Query("SELECT * FROM PROJECT_TABLE WHERE projectId = :projectId")
//    fun getContractId(projectId: String): String
//
//
//    @Query("DELETE FROM PROJECT_TABLE")
//    fun deleteAll()


}