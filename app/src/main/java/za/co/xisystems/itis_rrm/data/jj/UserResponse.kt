package za.co.xisystems.itis_rrm.data.jj


import com.google.gson.annotations.SerializedName

data class UserResponse(
    @SerializedName("ErrorMessage")
    val errorMessage: String, // null
    @SerializedName("RegistrationId")
    val registrationId: String, // E1676AD5BDEB6C4E8520F48160E01EAC
    @SerializedName("User")
    val user: User,
    @SerializedName("UserId")
    val userId: String // 3920
)