package za.co.xisystems.itis_rrm.data.models


import com.google.gson.annotations.SerializedName
import za.co.xisystems.itis_rrm.data.localDB.entities.ActivityDTO

data class Workflows(
    @SerializedName("Activities")
    val activities: List<ActivityDTO>,
    @SerializedName("InfoClasses")
    val infoClasses: List<InfoClasse>,
    @SerializedName("WorkflowResponse")
    val workflows: List<Workflow>
)