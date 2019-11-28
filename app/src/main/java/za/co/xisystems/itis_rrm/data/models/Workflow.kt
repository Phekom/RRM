package za.co.xisystems.itis_rrm.data.models


import com.google.gson.annotations.SerializedName

data class Workflow(
    @SerializedName("DateCreated")
    val dateCreated: String,
    @SerializedName("ErrorRouteId")
    val errorRouteId: Any,
    @SerializedName("RevNo")
    val revNo: Int,
    @SerializedName("StartRouteId")
    val startRouteId: Int,
    @SerializedName("UserId")
    val userId: Int,
    @SerializedName("WfHeaderId")
    val wfHeaderId: Int,
    @SerializedName("WorkFlowRoute")
    val workFlowRoute: List<WorkFlowRoute>,
    @SerializedName("WorkflowId")
    val workflowId: Int
)