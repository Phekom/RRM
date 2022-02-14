package za.co.xisystems.itis_rrm.data.network.responses

import com.google.gson.annotations.SerializedName

data class VersionCheckResponse(
    @SerializedName("ErrorMessage")
    val errorMessage: String?,
    @SerializedName("CorrectVersion") // "1.6.0.32"
    val correctVersion: String,
    @SerializedName("LatestVersion") // "1.6.0.32"
    val latestVersion: String,
    @SerializedName("ReleaseDate") //"2022/02/07 12:00:00 AM"
    val releaseDate: String
)
