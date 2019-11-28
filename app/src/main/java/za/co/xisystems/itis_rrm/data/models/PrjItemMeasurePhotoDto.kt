package za.co.xisystems.itis_rrm.data.models


import com.google.gson.annotations.SerializedName

data class PrjItemMeasurePhotoDto(
    @SerializedName("Descr")
    val descr: Any,
    @SerializedName("Filename")
    val filename: String,
    @SerializedName("ItemMeasureId")
    val itemMeasureId: String,
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
    @SerializedName("PrjJobItemMeasureDto")
    val prjJobItemMeasureDto: PrjJobItemMeasureDto,
    @SerializedName("RecordSynchStateId")
    val recordSynchStateId: Int,
    @SerializedName("RecordVersion")
    val recordVersion: Int
)