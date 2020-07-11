package za.co.xisystems.itis_rrm.data.localDB.entities

import androidx.room.Entity
import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.ArrayList

/**
 * Created by Francis Mahlava on 2020/02/04.
 */

@Entity
class WorkflowJobDTO(

    @SerializedName("JobId")
    var jobId: String? = null,

    @SerializedName("ActId")
    var actId: Int = 0,

    @SerializedName("TrackRouteId")
    var trackRouteId: String? = null,

    @SerializedName("JiNo")
    var jiNo: String? = null,

    @SerializedName("WorkflowItemEstimates")
    val workflowItemEstimates: ArrayList<WorkflowItemEstimateDTO>? = null,

    @SerializedName("WorkflowItemMeasures")
    val workflowItemMeasures: ArrayList<WorkflowItemMeasureDTO>? = null,

    @SerializedName("WorkflowJobSections")
    val workflowJobSections: ArrayList<JobSectionDTO>? = null

) : Serializable
