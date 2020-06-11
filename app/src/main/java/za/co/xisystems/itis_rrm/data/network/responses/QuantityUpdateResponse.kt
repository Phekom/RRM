package za.co.xisystems.itis_rrm.data.network.responses


import com.google.gson.annotations.SerializedName

/**
 * Created by Francis Mahlava on 2019/10/23.
 */

data class QuantityUpdateResponse(
    @SerializedName("ErrorMessage")
    val errorMessage: String? // null
)