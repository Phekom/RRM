package za.co.xisystems.itis_rrm.data.network.responses


import com.google.gson.annotations.SerializedName
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.ToDoGroupsDTO

data class JobResponse (
    @SerializedName("ErrorMessage")
    val errorMessage: String,
    @SerializedName("Job")
    val job: JobDTO,
    @SerializedName("ToDoListGroups")
    val toDoListGroups: List<ToDoGroupsDTO>
)