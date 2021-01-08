package za.co.xisystems.itis_rrm.data.localDB.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Created by Francis Mahlava on 2019/10/23.
 */

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
    val userStatus: String,
    @SerializedName("PHONE_NUMBER")
    var phoneNumber: String?,
    @SerializedName("IMEI")
    var imei: String?,
    @SerializedName("DEVICE")
    var device: String?,
    var pin: ByteArray?

) : Serializable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UserDTO

        if (registrationId != other.registrationId) return false
        if (userId != other.userId) return false
        if (userName != other.userName) return false
        if (userRoles != other.userRoles) return false
        if (phoneNumber != other.phoneNumber) return false
        if (imei != other.imei) return false
        if (device != other.device) return false

        return true
    }

    override fun hashCode(): Int {
        var result = registrationId.hashCode()
        result = 31 * result + userId.hashCode()
        result = 31 * result + userName.hashCode()
        result = 31 * result + userRoles.hashCode()
        result = 31 * result + (phoneNumber?.hashCode() ?: 0)
        result = 31 * result + (imei?.hashCode() ?: 0)
        result = 31 * result + (device?.hashCode() ?: 0)
        return result
    }
}
