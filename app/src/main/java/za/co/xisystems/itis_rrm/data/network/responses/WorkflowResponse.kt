package za.co.xisystems.itis_rrm.data.network.responses


import com.fasterxml.jackson.annotation.JsonProperty
import com.google.gson.annotations.SerializedName
import za.co.xisystems.itis_rrm.data.localDB.entities.WorkFlowsDTO

data class WorkflowResponse(
    @SerializedName("ErrorMessage")
    val errorMessage: String, // null
    @SerializedName("Workflows")
    val workFlows: WorkFlowsDTO
)