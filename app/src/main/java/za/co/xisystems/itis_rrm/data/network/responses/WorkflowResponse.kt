package za.co.xisystems.itis_rrm.data.network.responses


import com.google.gson.annotations.SerializedName
import za.co.xisystems.itis_rrm.data.localDB.entities.ToDoGroupsDTO
import za.co.xisystems.itis_rrm.data.models.Job
import za.co.xisystems.itis_rrm.data.models.Workflows

data class WorkflowResponse(
    @SerializedName("ErrorMessage")
    val errorMessage: String,
    @SerializedName("WorkflowResponse")
    val workflows: Workflows,

    @SerializedName("ToDoListGroups")
    val toDoListGroups: List<ToDoGroupsDTO>,
    @SerializedName("JobDTO")
    val job: Job
)