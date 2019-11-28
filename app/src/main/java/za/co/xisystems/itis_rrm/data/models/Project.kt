package za.co.xisystems.itis_rrm.data.models


import com.google.gson.annotations.SerializedName

data class Project(
    @SerializedName("Descr")
    val descr: String,
    @SerializedName("EndDate")
    val endDate: String,
    @SerializedName("Items")
    val items: List<Any>,
    @SerializedName("ProjectCode")
    val projectCode: String,
    @SerializedName("ProjectId")
    val projectId: String,
    @SerializedName("ProjectMinus")
    val projectMinus: String,
    @SerializedName("ProjectPlus")
    val projectPlus: String,
    @SerializedName("Sections")
    val sections: List<Section>,
    @SerializedName("VoItemsResponse")
    val voItems: List<Any>
)