package za.co.xisystems.itis_rrm.data.localDB.entities


import androidx.room.Entity
import com.google.gson.annotations.SerializedName

@Entity
data class WorkflowItemEstimateDTO(
    @SerializedName("ActId")
    var actId: Int, // 1
    @SerializedName("EstimateId")
    var estimateId: String, // sample string 1
    @SerializedName("TrackRouteId")
    var trackRouteId: String, // sample string 2
    @SerializedName("WorkflowEstimateWorks")
    var workflowEstimateWorks: List<WorkflowEstimateWorkDTO>
)

