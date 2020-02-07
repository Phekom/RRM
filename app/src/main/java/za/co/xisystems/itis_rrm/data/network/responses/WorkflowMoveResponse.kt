package za.co.xisystems.itis_rrm.data.network.responses


import com.fasterxml.jackson.annotation.JsonProperty
import com.google.gson.annotations.SerializedName
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.ToDoGroupsDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.WorkflowJobDTO
import za.co.xisystems.itis_rrm.data.localDB.models.WorkflowJob


data class WorkflowMoveResponse(
    @SerializedName("ErrorMessage")
    val errorMessage: String,
    @SerializedName("ToDoListGroups")
    val toDoListGroups: ArrayList<ToDoGroupsDTO>,
    @SerializedName("JobDTO")
    val job: JobDTO,
    @SerializedName("WorkflowJob")
    private var workflowJob: WorkflowJobDTO?



){
    fun getWorkflowJob(): WorkflowJobDTO? {
        return workflowJob
    }
    fun setWorkflowJob(workflowJob: WorkflowJobDTO?) {
        this.workflowJob = workflowJob
    }
}