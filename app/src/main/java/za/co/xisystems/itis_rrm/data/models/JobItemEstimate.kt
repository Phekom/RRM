package za.co.xisystems.itis_rrm.data.models


import com.google.gson.annotations.SerializedName

data class JobItemEstimate(
    @SerializedName("ActId")
    val actId: Int,
    @SerializedName("EstimateId")
    val estimateId: String,
    @SerializedName("JobId")
    val jobId: String,
    @SerializedName("LineRate")
    val lineRate: Double,
    @SerializedName("MobileEstimateWorks")
    val mobileEstimateWorks: List<EstimateWork>,
    @SerializedName("MobileJobItemEstimatesPhotos")
    val mobileJobItemEstimatesPhotos: List<JobItemEstimatesPhoto>,
    @SerializedName("MobileJobItemMeasures")
    val mobileJobItemMeasures: List<Any>,
    @SerializedName("PrjJobDto")
    val prjJobDto: PrjJobDto,
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