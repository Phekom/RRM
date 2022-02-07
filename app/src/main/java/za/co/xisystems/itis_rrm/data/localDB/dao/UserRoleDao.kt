package za.co.xisystems.itis_rrm.data.localDB.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import za.co.xisystems.itis_rrm.data.localDB.entities.UserRoleDTO

/**
 * Created by Francis Mahlava on 2019/11/21.
 */

@Dao
interface UserRoleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveAllRoles(userRole: List<UserRoleDTO>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveRole(userRole: UserRoleDTO)

    @Query("DELETE FROM USER_ROLE_TABLE")
    fun deleteAllUserRoles()

    @Query("SELECT * FROM USER_ROLE_TABLE ")
    fun getRoles(): LiveData<List<UserRoleDTO>>

    @Query("SELECT * FROM USER_ROLE_TABLE ")
    fun getRolesList(): List<UserRoleDTO>

    @Query("SELECT * FROM USER_ROLE_TABLE WHERE roleIdentifier = :roleIdentifier")
    fun checkRole(roleIdentifier: String): LiveData<List<UserRoleDTO>>

    @Query("DELETE FROM USER_ROLE_TABLE")
    fun deleteAll()
}
