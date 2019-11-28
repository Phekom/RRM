package za.co.xisystems.itis_rrm.data.localDB.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * Created by Francis Mahlava on 2019/11/21.
 */

const val WORKFLOW_TABLE = "WORKFLOW_TABLE"

@Entity(tableName = WORKFLOW_TABLE)
data class WorkFlowDTO(

    @SerializedName("WorkflowId")
    @PrimaryKey
    var workflowId: Long,
    @SerializedName("RevNo")
    var revNo: Long,
    @SerializedName("StartRouteId")
    var startRouteId: Long,
    @SerializedName("ErrorRouteId")
    var errorRouteId: Long,
    @SerializedName("UserId")
    var userId: Long,
    @SerializedName("WfHeaderId")
    var wfHeaderId: Long,
    @SerializedName("DateCreated")
    var dateCreated: String,
    @SerializedName("WorkFlowRoute")
    var workFlowRoute: List<WorkFlowRouteDTO>?


)