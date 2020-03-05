package za.co.xisystems.itis_rrm.data.network.responses


import com.google.gson.annotations.SerializedName
import za.co.xisystems.itis_rrm.data.localDB.entities.ToDoGroupsDTO

data class ToDoListGroupsResponse(
    @SerializedName("ErrorMessage")
    val errorMessage: String,
    @SerializedName("ToDoListGroups")
    val toDoListGroups: ArrayList<ToDoGroupsDTO>
//    @SerializedName("WorkflowJob")
//    val workflowJob: String
)