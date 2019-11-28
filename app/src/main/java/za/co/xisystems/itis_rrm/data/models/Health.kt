package za.co.xisystems.itis_rrm.data.models


import com.google.gson.annotations.SerializedName

data class Health(
    @SerializedName("IsAlive")
    val isAlive: Int
)