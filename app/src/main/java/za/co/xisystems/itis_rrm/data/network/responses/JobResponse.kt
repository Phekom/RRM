package za.co.xisystems.itis_rrm.data.network.responses


import com.google.gson.annotations.SerializedName
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.WorkflowJobDTO

data class JobResponse (
    @SerializedName("ErrorMessage")
    val errorMessage: String,
    @SerializedName("Job")
    val job: JobDTO,
//    val job: ArrayList<JobDTO>
//    @SerializedName("ToDoListGroups")
    @SerializedName("WorkflowJob")
    var workflowJob: WorkflowJobDTO
)