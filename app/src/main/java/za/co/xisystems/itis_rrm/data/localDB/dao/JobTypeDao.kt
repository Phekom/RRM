package za.co.xisystems.itis_rrm.data.localDB.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import za.co.xisystems.itis_rrm.data.localDB.entities.JobTypeEntityDTO

/**
 * Created by Francis Mahlava on 2019/10/23.
 */

@Dao
interface JobTypeDao {

    @Query("INSERT INTO JobType_TABLE (description) VALUES (:name)")
    fun insertType(name: String)

    @Query("SELECT * FROM JobType_TABLE WHERE description = :description")
    fun checkifExists(description: String): Boolean

    @Query("SELECT * FROM JobType_TABLE ")
    fun getAll(): LiveData<List<JobTypeEntityDTO>>

    @Query("SELECT COUNT(A.description ) AS 'description' FROM JobType_TABLE AS A ")
    fun getIncidentDataCount(): Int

    @Query("DELETE FROM JobType_TABLE")
    fun deleteAll()
}
