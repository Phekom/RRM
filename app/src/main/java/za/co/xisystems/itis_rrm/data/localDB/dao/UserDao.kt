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
    fun insert(user: UserDTO): Long

    @Query("SELECT EXISTS (SELECT * FROM USER_TABLE WHERE userId = :userId)")
    fun checkUserExists(userId: String): Boolean
    @Query("UPDATE USER_TABLE SET pinHash = :pinHash,  phoneNumber = :phoneNumber, imei=:imei, device=:device ")
    fun updateUser(pinHash: String?, phoneNumber: String?, imei: String?, device: String?)

    @Query("SELECT * FROM USER_TABLE LIMIT 1")
    fun getUser(): LiveData<UserDTO>

    @Query("SELECT UserId FROM USER_TABLE LIMIT 1")
    fun getUserID(): String

    @Query("SELECT UserName FROM USER_TABLE LIMIT 1")
    fun getUserName(): String

    @Delete
    fun removeUser(userDTO: UserDTO)

    @Query("DELETE FROM USER_TABLE")
    fun deleteUser()

    @Query("SELECT pinHash FROM USER_TABLE LIMIT 1")
    fun getPinHash(): String?

    fun getHash(): String? {
        return getPinHash()
    }

    @Query("UPDATE USER_TABLE SET pinHash = :binHash WHERE userId = :userId")
    fun putHash(userId: String, binHash: ByteArray)

    @Query("DELETE FROM USER_TABLE")
    fun deleteAll()

    @Query("UPDATE USER_TABLE SET pinHash = :newHash WHERE pinHash = :oldHash")
    fun updateUserHash(newHash: String, oldHash: String)

    @Query("UPDATE USER_TABLE SET authd = :yayNay WHERE userId = :userId")
    fun toggleAuthd(userId: String, yayNay: Boolean)

    @Query("UPDATE USER_TABLE SET authd = 1")
    fun pinAuthenticated()

    @Query("UPDATE USER_TABLE SET authd = 0")
    fun pinExpired()
}
