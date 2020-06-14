package za.co.xisystems.itis_rrm.data.localDB.entities

import com.google.gson.annotations.SerializedName

data class WorkflowEstimateWorkDTO(
    @SerializedName("ActId")
    var actId: Int, // 1
    @SerializedName("EstimateId")
    var estimateId: String, // sample string 3
    @SerializedName("RecordSynchStateId")
    var recordSynchStateId: Int, // 5
    @SerializedName("RecordVersion")
    var recordVersion: Int, // 4
    @SerializedName("TrackRouteId")
    var trackRouteId: String, // sample string 2
    @SerializedName("WorksId")
    var worksId: String // sample string 1
)
