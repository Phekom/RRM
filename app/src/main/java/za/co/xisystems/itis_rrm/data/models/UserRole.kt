package za.co.xisystems.itis_rrm.data.models


import com.google.gson.annotations.SerializedName

data class UserRole(
    @SerializedName("RoleDescription")
    val roleDescription: String,
    @SerializedName("RoleIdentifier")
    val roleIdentifier: String
)