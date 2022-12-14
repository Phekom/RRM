/**
 * Updated by Shaun McDonald on 2021/02/08
 * Last modified on 2021/02/07 12:57 PM
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 */

package za.co.xisystems.itis_rrm.data.localDB.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import za.co.xisystems.itis_rrm.data.localDB.entities.ProjectDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.ProjectItemDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.ProjectSectionDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.VoItemDTO
import za.co.xisystems.itis_rrm.domain.ProjectSelector

/**
 * Created by Francis Mahlava on 2019/11/22.
 */

@Dao
interface ProjectDao {

    //    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @Query("INSERT INTO PROJECT_TABLE (projectId,descr, endDate, items, projectCode, projectMinus, projectPlus,projectSections, voItems, contractId) VALUES (:projectId,:descr, :endDate, :items, :projectCode, :projectMinus, :projectPlus, :projectSections, :voItems, :contractId)")
    fun insertProject(
        projectId: String,
        descr: String?,
        endDate: String?,
        items: ArrayList<ProjectItemDTO>?,
        projectCode: String?,
        projectMinus: String?,
        projectPlus: String?,
        projectSections: ArrayList<ProjectSectionDTO>?,
        voItems: ArrayList<VoItemDTO>?,
        contractId: String?
    ): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateProject(project: ProjectDTO)

    @Query("SELECT contractId FROM PROJECT_TABLE WHERE projectId = :projectId")
    fun getContractIdForProjectId(projectId: String): String

//    @Query("SELECT projectId FROM PROJECT_TABLE WHERE projectId = projectId")
//    fun getProjectId(): String

    @Query("SELECT * FROM PROJECT_TABLE ")
    fun getAllProjects(): LiveData<List<ProjectDTO>>

    @Query("SELECT projectId FROM PROJECT_TABLE WHERE projectId = :projectId")
    fun checkProjectExists(projectId: String): Boolean

    @Query("SELECT * FROM PROJECT_TABLE WHERE contractId = :contractId ORDER BY projectCode")
    fun getAllProjectsByContract(contractId: String): LiveData<List<ProjectDTO>>

    @Query("SELECT descr FROM PROJECT_TABLE WHERE projectId = :projectId")
    fun getProjectDescription(projectId: String): String

    @Query("SELECT * FROM PROJECT_TABLE WHERE projectId = :projectId")
    fun getProjectById(projectId: String): ProjectDTO

    @Query("DELETE FROM PROJECT_TABLE")
    fun deleteAll()

    @Query("SELECT projectCode FROM PROJECT_TABLE WHERE projectId LIKE :projectId")
    fun getProjectCodeForId(projectId: String?): String

    @Query("SELECT projectId, contractId, projectCode, descr FROM PROJECT_TABLE WHERE contractId = :contractId ORDER BY projectCode")
    fun getProjectSelectorsForContractId(contractId: String): List<ProjectSelector>
}
