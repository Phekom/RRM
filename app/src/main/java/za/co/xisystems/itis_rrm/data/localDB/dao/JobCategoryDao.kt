package za.co.xisystems.itis_rrm.data.localDB.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import za.co.xisystems.itis_rrm.data.localDB.entities.JobCategoryDTO

/**
 * Created by Francis Mahlava on 2019/12/04.
 */

@Dao
interface JobCategoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertJobCategory(category : JobCategoryDTO)

//    @Query("SELECT * FROM JOB_CATEGORY_TABLE WHERE stepCode = :workCode")
//    fun checkWorkFlowStepExistsWorkCode(workCode: String): Boolean
//
//    @Query("SELECT * FROM JOB_CATEGORY_TABLE WHERE descrip = :workDesc")
//    fun checkWorkFlowStepExistsDesc(workDesc: String): Boolean
//
//    @Query("INSERT INTO JOB_CATEGORY_TABLE (stepCode, actTypeId ) VALUES ( :stepCode, :actId)")
//    fun insertStepsCode(stepCode: String, actId: Int)
//
//    @Query("UPDATE JOB_CATEGORY_TABLE SET descrip = :descrip WHERE stepCode = :stepCode")
//    fun updateStepsDesc(descrip: String?, stepCode: String)
//
    @Query("SELECT DISTINCT * FROM JOB_CATEGORY_TABLE")
    fun getJobCategories(): List<JobCategoryDTO>

    @Query("DELETE FROM WorkStep_TABLE")
    fun deleteAll()
}
