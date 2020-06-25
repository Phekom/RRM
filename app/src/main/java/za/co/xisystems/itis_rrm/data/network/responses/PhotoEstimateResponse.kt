package za.co.xisystems.itis_rrm.data.network.responses

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class PhotoEstimateResponse(

    @SerializedName("Photo")
    var photo: String?,
    @SerializedName("ErrorMessage")
    var errorMessage: String? = null

) : Serializable
