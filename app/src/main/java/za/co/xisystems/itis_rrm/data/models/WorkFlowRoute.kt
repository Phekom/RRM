package za.co.xisystems.itis_rrm.data.models


import com.google.gson.annotations.SerializedName

data class WorkFlowRoute(
    @SerializedName("ActId")
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
    val routeId: Long
)