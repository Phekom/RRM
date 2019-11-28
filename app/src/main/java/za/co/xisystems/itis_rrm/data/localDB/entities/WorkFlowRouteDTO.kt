package za.co.xisystems.itis_rrm.data.localDB.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * Created by Francis Mahlava on 2019/11/21.
 */

const val WORKFLOW_ROUTE_TABLE = "WORKFLOW_ROUTE_TABLE"

@Entity(tableName = WORKFLOW_ROUTE_TABLE)
data class WorkFlowRouteDTO(
    @SerializedName("ActId")
    @PrimaryKey
    val actId: Long,
    @SerializedName("CanStart")
    val canStart: Long,
    @SerializedName("ErrorRouteId")
    val errorRouteId: Long,
    @SerializedName("FailRouteId")
    val failRouteId: Long,
    @SerializedName("NextRouteId")
    val nextRouteId: Long,
    @SerializedName("RouteId")
    val routeId: Long,
    var workflowId: Long
)

