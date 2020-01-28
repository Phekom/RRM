package za.co.xisystems.itis_rrm.data.network.responses


import com.google.gson.annotations.SerializedName
import za.co.xisystems.itis_rrm.data.localDB.models.WorkflowJob

data class SaveMeasurementResponse(
    @SerializedName("ErrorMessage")
    val errorMessage: String, // sample string 1
    @SerializedName("WorkflowJob")
    var workflowJob: WorkflowJob
) {

//    fun getWorkflowJob(): WorkflowJob? {
//        return workflowJob
//    }

//    fun setWorkflowJob(workflowJob: WorkflowJob?) {
//        this.workflowJob = workflowJob!!
//    }
}