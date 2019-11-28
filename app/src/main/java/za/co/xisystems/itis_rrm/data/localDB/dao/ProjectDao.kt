package za.co.xisystems.itis_rrm.data.localDB.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import za.co.xisystems.itis_rrm.data.localDB.entities.ProjectDTO

/**
 * Created by Francis Mahlava on 2019/11/22.
 */

@Dao
interface ProjectDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject( project : List<ProjectDTO>)

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