package za.co.xisystems.itis_rrm.data.localDB.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * Created by Francis Mahlava on 2019/11/21.
 */

const val USER_ROLE_TABLE = "USER_ROLE_TABLE"

@Entity(tableName = USER_ROLE_TABLE)
data class UserRoleDTO(

    @SerializedName("RoleIdentifier")
    @PrimaryKey
    val roleIdentifier: String, // D9E16C2A31FA4CC28961E20B652B292C
    @SerializedName("RoleDescription")
    val roleDescription: String // RRM Job Mobile - Engineer



)

