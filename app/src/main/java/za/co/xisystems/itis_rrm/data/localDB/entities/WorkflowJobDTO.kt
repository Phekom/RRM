package za.co.xisystems.itis_rrm.data.localDB.entities

import com.google.gson.annotations.SerializedName
import za.co.xisystems.itis_rrm.data.localDB.models.WorkflowItemEstimate
import za.co.xisystems.itis_rrm.data.localDB.models.WorkflowItemMeasure
import za.co.xisystems.itis_rrm.data.localDB.models.WorkflowJobSection
import java.io.Serializable
import java.util.*

/**
 * Created by Francis Mahlava on 2020/02/04.
 */
class WorkflowJobDTO (

    @SerializedName("JobId")
    var jobId: String? = null,

    @SerializedName("ActId")
    var actId : Int = 0,

    @SerializedName("TrackRouteId")
    var trackRouteId: String? = null,

    @SerializedName("JiNo")
    var jiNo: String? = null,

    @SerializedName("WorkflowItemEstimates")
     val workflowItemEstimates: ArrayList<WorkflowItemEstimate>? = null,

    @SerializedName("WorkflowItemMeasures")
     val workflowItemMeasures: ArrayList<WorkflowItemMeasure>? = null,

    @SerializedName("WorkflowJobSections")
     val workflowJobSections: ArrayList<JobSectionDTO>? = null

): Serializable {}