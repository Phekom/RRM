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
    val ACT_ID: Long,
    @SerializedName("ActName")
    val ACT_NAME: String,
    @SerializedName("ActTypeId")
    val ACT_TYPE_ID: Long,
    @SerializedName("ApprovalId")
    val APPROVAL_ID: Long,
    @SerializedName("Descr")
    val DESCRIPTION: String,

    val CONTENT_ID: Long
)