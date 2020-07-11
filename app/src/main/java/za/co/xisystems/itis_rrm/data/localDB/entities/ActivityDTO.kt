package za.co.xisystems.itis_rrm.data.localDB.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * Created by Francis Mahlava on 2019/11/26.
 */

const val ACTIVITY_TABLE = "ACTIVITY_TABLE"

@Entity(tableName = ACTIVITY_TABLE)
data class ActivityDTO(
    @SerializedName("ActId")
    @PrimaryKey
    val actId: Long,
    @SerializedName("ActTypeId")
    val actTypeId: Long?,
    @SerializedName("ApprovalId")
    val approvalId: Long = 0,
    @SerializedName("sContentId")
    val sContentId: Long = 0,
    @SerializedName("ActName")
    val actName: String?,
    @SerializedName("Descr")
    val descr: String?

)
