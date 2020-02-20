package za.co.xisystems.itis_rrm.data.localDB.entities


import androidx.room.Entity
import com.google.gson.annotations.SerializedName

@Entity
data class WorkflowItemMeasureDTO(
    @SerializedName("ActId")
    var actId: Int, // 1
    @SerializedName("ItemMeasureId")
    var itemMeasureId: String, // sample string 1
    @SerializedName("MeasureGroupId")
    var measureGroupId: String, // sample string 3
    @SerializedName("TrackRouteId")
    var trackRouteId: String // sample string 2
)

