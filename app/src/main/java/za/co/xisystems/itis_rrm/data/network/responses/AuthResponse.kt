package za.co.xisystems.itis_rrm.data.network.responses

import com.fasterxml.jackson.annotation.JsonProperty
import com.google.gson.annotations.SerializedName
//import com.google.gson.annotations.JsonProperty
import za.co.xisystems.itis_rrm.data.localDB.entities.UserDTO

/**
 * Created by Francis Mahlava on 2019/10/23.
 */

data class AuthResponse(

    @SerializedName("isSuccessful")
    val isSuccessful: Boolean?,

    @SerializedName("ErrorMessage")
    val errorMessage: String, // null
    @SerializedName("RegistrationId")
    val registrationId: String, // E1676AD5BDEB6C4E8520F48160E01EAC
    @SerializedName("User")
    val user: UserDTO,
    @SerializedName("UserId")
    val userId: String // 3920


)