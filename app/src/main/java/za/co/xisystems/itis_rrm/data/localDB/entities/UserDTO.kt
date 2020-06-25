package za.co.xisystems.itis_rrm.data.localDB.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Created by Francis Mahlava on 2019/10/23.
 */

// const val CURRENT_LOGGEDIN_USER = 0
const val USER_TABLE = "USER_TABLE"

@Entity(tableName = USER_TABLE)
data class UserDTO(

    @SerializedName("RegistrationId")
    val registrationId: String, // E1676AD5BDEB6C4E8520F48160E01EAC
    @SerializedName("UserId")
    @PrimaryKey
    val userId: String, // 3920
    @SerializedName("UserName")
    val userName: String, // niebuhrk
    @SerializedName("UserRoles")
    val userRoles: ArrayList<UserRoleDTO>,
    @SerializedName("UserStatus")
    val userStatus: String, // Y
    var PIN: String?,
    var PHONE_NUMBER: String?,
    var IMEI: String?,
    var DEVICE: String?,
    var Password: String?,
    var WEB_SERVICE_URI: String?

) : Serializable {

//    @PrimaryKey(autoGenerate = false)
//    var uid: Int = CURRENT_LOGGEDIN_USER
}
