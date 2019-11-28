package za.co.xisystems.itis_rrm.data.models


import com.google.gson.annotations.SerializedName

data class Section(
    @SerializedName("Direction")
    val direction: String,
    @SerializedName("EndKm")
    val endKm: Double,
    @SerializedName("ProjectId")
    val projectId: String,
    @SerializedName("Route")
    val route: String,
    @SerializedName("Section")
    val section: String,
    @SerializedName("SectionId")
    val sectionId: String,
    @SerializedName("StartKm")
    val startKm: Double
)