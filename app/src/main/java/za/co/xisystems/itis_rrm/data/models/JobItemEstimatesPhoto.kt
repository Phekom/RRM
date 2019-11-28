package za.co.xisystems.itis_rrm.data.models


import com.google.gson.annotations.SerializedName

data class JobItemEstimatesPhoto(
    @SerializedName("Descr")
    val descr: String,
    @SerializedName("EstimateId")
    val estimateId: String,
    @SerializedName("Filename")
    val filename: String,
    @SerializedName("PhotoDate")
    val photoDate: String,
    @SerializedName("PhotoId")
    val photoId: String,
    @SerializedName("PhotoLatitude")
    val photoLatitude: Double,
    @SerializedName("PhotoLongitude")
    val photoLongitude: Double,
    @SerializedName("PhotoPath")
    val photoPath: String,
    @SerializedName("PrjJobItemEstimateDto")
    val prjJobItemEstimateDto: PrjJobItemEstimateDto,
    @SerializedName("RecordSynchStateId")
    val recordSynchStateId: Int,
    @SerializedName("RecordVersion")
    val recordVersion: Int
)