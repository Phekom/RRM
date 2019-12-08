package za.co.xisystems.itis_rrm.data.localDB.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import za.co.xisystems.itis_rrm.data.localDB.entities.UserDTO

/**
 * Created by Francis Mahlava on 2019/10/23.
 */


@Dao
interface UserDao{

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(userDTO: UserDTO) : Long

    @Query("SELECT * FROM USER_TABLE WHERE userId = userId")
    fun getuser() : LiveData<UserDTO>

    @Query("SELECT UserId FROM USER_TABLE WHERE userId = userId")
    fun getuserID() : String

    @Query("SELECT UserName FROM USER_TABLE WHERE userId = userId")
    fun getUserName() : String

//    @Query("SELECT userRoles FROM USER_TABLE WHERE uid = $CURRENT_LOGGEDIN_USER")
//    fun getUserRole() : LiveData<List<UserDTO>>

    @Delete
    suspend fun removeUser(userDTO: UserDTO)

    @Query("DELETE FROM USER_TABLE WHERE userId = userId")
    fun deleteUser()

    @Query("SELECT * FROM USER_TABLE WHERE userId = userId LIMIT 1" )
    fun getPin() : LiveData<UserDTO>

}