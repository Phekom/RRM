package za.co.xisystems.itis_rrm.data.network.responses


import com.fasterxml.jackson.annotation.JsonProperty
import com.google.gson.annotations.SerializedName
import za.co.xisystems.itis_rrm.data.localDB.entities.UserRoleDTO

data class UserRoleResponse(
    @SerializedName("ErrorMessage")
    val errorMessage: String,
    @SerializedName("UserRoles")
    val userRoles: List<UserRoleDTO>
)