package za.co.xisystems.itis_rrm.data.localDB.entities


import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

const val WORKFLOWs_TABLE = "WORKFLOWs_TABLE"

@Entity(tableName = WORKFLOWs_TABLE)
data class WorkFlowsDTO(
    @SerializedName("Activities")
    @PrimaryKey
    val activities: ArrayList<ActivityDTO>,
    @SerializedName("InfoClasses")
    val infoClasses: ArrayList<InfoClassDTO>,
    @SerializedName("Workflows")
    val workflows: ArrayList<WorkFlowDTO>
)