package za.co.xisystems.itis_rrm.data.models


import com.google.gson.annotations.SerializedName

data class JobSection(
    @SerializedName("EndKm")
    val endKm: Double,
    @SerializedName("JobId")
    val jobId: String,
    @SerializedName("JobSectionId")
    val jobSectionId: String,
    @SerializedName("PrjJobDto")
    val prjJobDto: PrjJobDto,
    @SerializedName("ProjectSectionId")
    val projectSectionId: String,
    @SerializedName("RecordSynchStateId")
    val recordSynchStateId: Int,
    @SerializedName("RecordVersion")
    val recordVersion: Int,
    @SerializedName("StartKm")
    val startKm: Double
)