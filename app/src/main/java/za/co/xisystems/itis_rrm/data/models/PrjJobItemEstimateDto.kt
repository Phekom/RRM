package za.co.xisystems.itis_rrm.data.models


import com.google.gson.annotations.SerializedName

data class PrjJobItemEstimateDto(
    @SerializedName("ActId")
    val actId: Int,
    @SerializedName("EstimateId")
    val estimateId: String,
    @SerializedName("JobId")
    val jobId: String,
    @SerializedName("LineRate")
    val lineRate: Double,
    @SerializedName("MobileEstimateWorks")
    val mobileEstimateWorks: Any,
    @SerializedName("MobileJobItemEstimatesPhotos")
    val mobileJobItemEstimatesPhotos: Any,
    @SerializedName("MobileJobItemMeasures")
    val mobileJobItemMeasures: Any,
    @SerializedName("PrjJobDto")
    val prjJobDto: Any,
    @SerializedName("ProjectItemId")
    val projectItemId: String,
    @SerializedName("ProjectVoId")
    val projectVoId: Any,
    @SerializedName("Qty")
    val qty: Double,
    @SerializedName("RecordSynchStateId")
    val recordSynchStateId: Int,
    @SerializedName("RecordVersion")
    val recordVersion: Int,
    @SerializedName("TrackRouteId")
    val trackRouteId: String
)