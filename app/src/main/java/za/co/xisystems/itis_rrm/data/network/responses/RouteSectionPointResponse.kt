package za.co.xisystems.itis_rrm.data.network.responses

import com.google.gson.annotations.SerializedName

data class RouteSectionPointResponse(
    @SerializedName("Direction")
    val direction: String,
    @SerializedName("ErrorMessage")
    val errorMessage: String,
    @SerializedName("LinearId")
    val linearId: String,
    @SerializedName("PointLocation")
    val pointLocation: Double,
    @SerializedName("SectionId")
    val sectionId: Int,
    @SerializedName("BufferLocation")
    val bufferLocation: String

)
