package za.co.xisystems.itis_rrm.data.network.responses

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Created by Pieter Jacobs in 2016-02-10.
 * Updated by Pieter Jacobs during 2016/05, 2016/07.
 */
class UploadImageResponse : Serializable {
    @SerializedName("ErrorMessage")
    var errorMessage: String? = null

    val isSuccess: Boolean
        get() = null == errorMessage || errorMessage == ""
}