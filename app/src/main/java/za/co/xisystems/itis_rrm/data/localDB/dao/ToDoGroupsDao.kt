package za.co.xisystems.itis_rrm.data.localDB.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import za.co.xisystems.itis_rrm.data.localDB.entities.CURRENT_LOGGEDIN_USER
import za.co.xisystems.itis_rrm.data.localDB.entities.ToDoGroupsDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.UserDTO

/**
 * Created by Francis Mahlava on 2019/11/26.
 */

@Dao
interface ToDoGroupsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertToDoGroups(toDoGroups : List<ToDoGroupsDTO> )


//    @Query("SELECT * FROM USER_TABLE WHERE uid = $CURRENT_LOGGEDIN_USER")
//    fun getuser() : LiveData<UserDTO>
//
//    @Query("SELECT UserId FROM USER_TABLE WHERE uid = $CURRENT_LOGGEDIN_USER")
//    fun getuserID() : String
//
//    @Query("SELECT UserName FROM USER_TABLE WHERE uid = $CURRENT_LOGGEDIN_USER")
//    fun getUserName() : String

//    @Query("SELECT userRoles FROM USER_TABLE WHERE uid = $CURRENT_LOGGEDIN_USER")
//    fun getUserRole() : LiveData<List<UserDTO>>

    @Delete
    suspend fun removeUser(userDTO: UserDTO)

    @Query("DELETE FROM USER_TABLE WHERE uid = $CURRENT_LOGGEDIN_USER")
    fun deleteUser()

    @Query("SELECT * FROM USER_TABLE WHERE uid = $CURRENT_LOGGEDIN_USER LIMIT 1" )
    fun getPin() : LiveData<UserDTO>
}