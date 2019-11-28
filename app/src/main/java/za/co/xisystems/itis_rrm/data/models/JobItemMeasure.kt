package za.co.xisystems.itis_rrm.data.models


import com.google.gson.annotations.SerializedName

data class JobItemMeasure(
    @SerializedName("ActId")
    val actId: Int,
    @SerializedName("ApprovalDate")
    val approvalDate: Any,
    @SerializedName("Cpa")
    val cpa: Int,
    @SerializedName("EndKm")
    val endKm: Double,
    @SerializedName("EstimateId")
    val estimateId: String,
    @SerializedName("ItemMeasureId")
    val itemMeasureId: String,
    @SerializedName("JimNo")
    val jimNo: Any,
    @SerializedName("JobDirectionId")
    val jobDirectionId: Int,
    @SerializedName("JobId")
    val jobId: String,
    @SerializedName("LineAmount")
    val lineAmount: Double,
    @SerializedName("LineRate")
    val lineRate: Double,
    @SerializedName("MeasureDate")
    val measureDate: Any,
    @SerializedName("MeasureGroupId")
    val measureGroupId: String,
    @SerializedName("PrjItemMeasurePhotoDtos")
    val prjItemMeasurePhotoDtos: List<PrjItemMeasurePhotoDto>,
    @SerializedName("PrjJobDto")
    val prjJobDto: PrjJobDto,
    @SerializedName("PrjJobItemEstimateDto")
    val prjJobItemEstimateDto: PrjJobItemEstimateDto,
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
    @SerializedName("StartKm")
    val startKm: Double,
    @SerializedName("TrackRouteId")
    val trackRouteId: String
)