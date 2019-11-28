package za.co.xisystems.itis_rrm.data.jj


import com.google.gson.annotations.SerializedName

data class UserRole(
    @SerializedName("RoleDescription")
    val roleDescription: String, // RRM Job Mobile - Engineer
    @SerializedName("RoleIdentifier")
    val roleIdentifier: String // D9E16C2A31FA4CC28961E20B652B292C
)