package za.co.xisystems.itis_rrm.data.localDB.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * Created by Francis Mahlava on 2020/02/12.
 */

const val WorkStep_TABLE = "WorkStep_TABLE"

@Entity(tableName = WorkStep_TABLE)
class WF_WorkStepDTO(
    @SerializedName("WorkStep_ID")
    @PrimaryKey
    val WorkStep_ID: Int,
    @SerializedName("Step_Code")
    val Step_Code: String?,
    @SerializedName("DESCR")
    val Descrip: String?,
    @SerializedName("ACT_TYPE_ID")
    val Act_Type_id: Int?
)
