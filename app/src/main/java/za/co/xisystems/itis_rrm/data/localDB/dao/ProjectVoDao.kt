package za.co.xisystems.itis_rrm.data.localDB.dao
//
//import androidx.lifecycle.LiveData
//import androidx.room.Dao
//import androidx.room.Insert
//import androidx.room.OnConflictStrategy
//import androidx.room.Query
//import za.co.xisystems.itis_rrm.data.localDB.entities.ContractVoDTO
//import za.co.xisystems.itis_rrm.data.localDB.entities.ItemSectionDTO
//import za.co.xisystems.itis_rrm.data.localDB.entities.ProjectItemDTO
//import za.co.xisystems.itis_rrm.data.localDB.entities.ProjectVoDTO
//import za.co.xisystems.itis_rrm.domain.ProjectSelector
//import za.co.xisystems.itis_rrm.domain.ProjectVoSelector
//
///**
// * Created by Francis Mahlava on 2019/11/21.
// */
//
//@Dao
//interface ProjectVoDao {
//
////    @Insert(onConflict = OnConflictStrategy.REPLACE)
////    fun insertProjectVo(projectVoDTO: ProjectVoDTO)
////
////    @Query("SELECT EXiSTS (SELECT * FROM PROJECTS_VO_TABLE WHERE projectVoId = :projectVoId)")
////    fun checkItemExistsProjectVo(projectVoId: String): Boolean
////
////    @Query("SELECT * FROM PROJECTS_VO_TABLE WHERE projectId = :projectId")
////    fun getProjectVoData(projectId: String): List<ProjectVoDTO>
////
////    @Query("SELECT projectId, projectVoId, voNumber, contractVoId FROM PROJECTS_VO_TABLE WHERE projectId = :projectId ORDER BY voNumber")
////    fun getContractVoSelectors(projectId: String): List<ProjectVoSelector>
////
////    @Query("SELECT * FROM PROJECTS_VO_TABLE WHERE sectionItemId LIKE :sectionItem AND contractVoId LIKE :contractVoId")
////    fun getAllItemsForSectionItemByContractVoId(
////        sectionItem: String,
////        contractVoId: String
////    ): LiveData<List<ProjectVoDTO>>
////
////    @Query("SELECT * FROM PROJECTS_VO_TABLE WHERE itemId LIKE :itemId")
////    fun getItemForID(itemId: String): ProjectVoDTO
//
//}
