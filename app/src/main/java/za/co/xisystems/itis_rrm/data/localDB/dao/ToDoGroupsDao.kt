package za.co.xisystems.itis_rrm.data.localDB.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import za.co.xisystems.itis_rrm.data.localDB.entities.ToDoGroupsDTO

/**
 * Created by Francis Mahlava on 2019/11/26.
 */

@Dao
interface ToDoGroupsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertToDoGroups(toDoGroup: ToDoGroupsDTO)

    @Query("SELECT * FROM TODO_GROUPS_TABLE WHERE groupId = :groupId")
    fun checkIfGroupCollectionExist(groupId: String): Boolean

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

    //    @Delete
//    suspend fun removeUser(userDTO: UserDTO)
//
//    @Query("DELETE FROM USER_TABLE WHERE uid = $CURRENT_LOGGEDIN_USER")
//    fun deleteUser()
//
    @Query("DELETE FROM TODO_GROUPS_TABLE")
    fun deleteAll()
}
