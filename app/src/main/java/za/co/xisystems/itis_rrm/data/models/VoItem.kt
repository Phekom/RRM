package za.co.xisystems.itis_rrm.data.models


import com.google.gson.annotations.SerializedName

data class VoItem(
    @SerializedName("ContractVoId")
    val contractVoId: String,
    @SerializedName("ContractVoItemId")
    val contractVoItemId: String,
    @SerializedName("Descr")
    val descr: String,
    @SerializedName("ItemCode")
    val itemCode: String,
    @SerializedName("ProjectId")
    val projectId: String,
    @SerializedName("ProjectItemId")
    val projectItemId: String,
    @SerializedName("ProjectVoId")
    val projectVoId: String,
    @SerializedName("Rate")
    val rate: Double,
    @SerializedName("Uom")
    val uom: String,
    @SerializedName("VoDescr")
    val voDescr: String
)