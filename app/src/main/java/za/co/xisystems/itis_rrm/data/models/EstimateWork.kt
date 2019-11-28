package za.co.xisystems.itis_rrm.data.models


import com.google.gson.annotations.SerializedName

data class EstimateWork(
    @SerializedName("ActId")
    val actId: Int,
    @SerializedName("EstimateId")
    val estimateId: String,
//    @SerializedName("PrjEstWorksPhotoDtos")
//    val prjEstWorksPhotoDtos: List<EstimateWorksPhoto>,
    @SerializedName("PrjJobItemEstimateDto")
    val prjJobItemEstimateDto: PrjJobItemEstimateDto,
    @SerializedName("RecordSynchStateId")
    val recordSynchStateId: Int,
    @SerializedName("RecordVersion")
    val recordVersion: Int,
    @SerializedName("TrackRouteId")
    val trackRouteId: String,
    @SerializedName("WorksId")
    val worksId: String
)