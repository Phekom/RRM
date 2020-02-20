package za.co.xisystems.itis_rrm.data.network.responses


import com.fasterxml.jackson.annotation.JsonProperty
import com.google.gson.annotations.SerializedName
import za.co.xisystems.itis_rrm.data.localDB.entities.WorkflowJobDTO

data class SaveMeasurementResponse(
    @SerializedName("ErrorMessage")
    val errorMessage: String, // sample string 1
    @SerializedName("WorkflowJob")
    var workflowJob: WorkflowJobDTO
) {

//    fun getWorkflowJob(): WorkflowJob? {
//        return workflowJob
//    }

//    fun setWorkflowJob(workflowJob: WorkflowJob?) {
//        this.workflowJob = workflowJob!!
//    }
}