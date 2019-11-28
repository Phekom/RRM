package za.co.xisystems.itis_rrm.data.models


import com.google.gson.annotations.SerializedName

data class Workflows(
    @SerializedName("Activities")
    val activities: List<Activity>,
    @SerializedName("InfoClasses")
    val infoClasses: List<InfoClasse>,
    @SerializedName("WorkflowResponse")
    val workflows: List<Workflow>
)