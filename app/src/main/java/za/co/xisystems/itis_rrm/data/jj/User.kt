package za.co.xisystems.itis_rrm.data.jj


import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("RegistrationId")
    val registrationId: String, // E1676AD5BDEB6C4E8520F48160E01EAC
    @SerializedName("UserId")
    val userId: String, // 3920
    @SerializedName("UserName")
    val userName: String, // niebuhrk
    @SerializedName("UserRoles")
    val userRoles: List<UserRole>,
    @SerializedName("UserStatus")
    val userStatus: String // Y
)