package za.co.xisystems.itis_rrm.data.network.responses


import com.fasterxml.jackson.annotation.JsonProperty
import com.google.gson.annotations.SerializedName

data class HealthCheckResponse(
    @SerializedName("ErrorMessage")
    val errorMessage: String,
    @SerializedName("IsAlive")
    val isAlive: Int
)