package za.co.xisystems.itis_rrm.data.network.responses

//import com.google.gson.annotations.JsonProperty
import com.google.gson.annotations.SerializedName
import za.co.xisystems.itis_rrm.data.localDB.entities.UserDTO

/**
 * Created by Francis Mahlava on 2019/10/23.
 */

data class AuthResponse(

    @SerializedName("isSuccessful")
    val isSuccessful: Boolean,

    @SerializedName("ErrorMessage")
    val errorMessage: String?,
    @SerializedName("RegistrationId")
    val registrationId: String?, // E1676AD5BDEB6C4E8520F48160E01EAC
    @SerializedName("User")
    val user: UserDTO?,
    @SerializedName("UserId")
    val userId: String? // 3920


)