package za.co.xisystems.itis_rrm.data.network.responses

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class PhotoPotholeResponse(
    @SerializedName("ByteArray")
    val photo : String? = null,
    @SerializedName("ErrorMessage")
    val errorMessage: String?,
    @SerializedName("FileName")
    val fileName: String?,
    @SerializedName("FullPath")
    val fullPath: String?,
    @SerializedName("Latitude")
    val latitude: String?,
    @SerializedName("Longitude")
    val longitude: String?,
    @SerializedName("PhotoDate")
    val photoDate: String?
) : Serializable
