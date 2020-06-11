package za.co.xisystems.itis_rrm.data.network.responses


import com.google.gson.annotations.SerializedName
import za.co.xisystems.itis_rrm.data.localDB.entities.WorkflowJobDTO

/**
 * SaveMeasurementResponse - API receipt for saved measurements
 * @property errorMessage String?
 * @property workflowJob WorkflowJobDTO
 * @constructor
 */
data class SaveMeasurementResponse(
    @SerializedName("ErrorMessage")
    val errorMessage: String?, // all errorMessages are nullable
    @SerializedName("WorkflowJob")
    var workflowJob: WorkflowJobDTO
)