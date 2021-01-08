package za.co.xisystems.itis_rrm.data.localDB.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import za.co.xisystems.itis_rrm.data.localDB.entities.UserDTO

/**
 * Created by Francis Mahlava on 2019/10/23.
 */

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: UserDTO): Long

    @Query("SELECT * FROM USER_TABLE WHERE userId = :userId")
    fun checkUserExists(userId: String): Boolean

    @Query("UPDATE USER_TABLE SET pin =:binHash,  phoneNumber = :phoneNumber, imei=:imei, device=:device ")
    fun updateUser(binHash: ByteArray?, phoneNumber: String?, imei: String?, device: String?)

    @Query("SELECT * FROM USER_TABLE WHERE userId = userId")
    fun getUser(): LiveData<UserDTO>

    @Query("SELECT UserId FROM USER_TABLE WHERE userId = userId")
    fun getUserID(): String

    @Query("SELECT UserName FROM USER_TABLE WHERE userId = userId")
    fun getUserName(): String

    @Delete
    suspend fun removeUser(userDTO: UserDTO)

    @Query("DELETE FROM USER_TABLE WHERE userId = userId")
    fun deleteUser()

    @Query("SELECT PIN FROM USER_TABLE WHERE userId = userId LIMIT 1")
    fun getPin(): ByteArray?

    @Query("SELECT pin FROM USER_TABLE LIMIT 1")
    fun getHash(): ByteArray?

    @Query("UPDATE USER_TABLE SET pin = :binHash WHERE userId = :userId")
    fun putHash(userId: String, binHash: ByteArray)

    @Query("DELETE FROM USER_TABLE")
    fun deleteAll()

    @Query("UPDATE USER_TABLE SET pin = :newHash WHERE pin = :oldHash")
    fun updateUserHash(newHash: ByteArray, oldHash: ByteArray)
}
