package za.co.xisystems.itis_rrm.data.localDB.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * Created by Francis Mahlava on 2019/11/21.
 */

const val WORKFLOW_ROUTE_TABLE = "WORKFLOW_ROUTE_TABLE"
@Entity(
    tableName = WORKFLOW_ROUTE_TABLE, foreignKeys = arrayOf(
        ForeignKey(
            entity = WorkFlowDTO::class,
            parentColumns = arrayOf("workflowId"),
            childColumns = arrayOf("workflowId"),
            onDelete = ForeignKey.CASCADE
        )
    )
)
data class WorkFlowRouteDTO(

    @PrimaryKey
    var id: Int,

    @SerializedName("RouteId")
    val routeId: Long,

    @SerializedName("ActId")
    val actId: Long,

    @SerializedName("NextRouteId")
    val nextRouteId: Long,

    @SerializedName("FailRouteId")
    val failRouteId: Long,

    @SerializedName("ErrorRouteId")
    val errorRouteId: Long,

    @SerializedName("CanStart")
    val canStart: Long,

    @SerializedName("WorkflowId")
    @ColumnInfo(name = "workflowId", index = true)
    var workflowId: Long?
)
