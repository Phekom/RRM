package za.co.xisystems.itis_rrm.data.network.responses

import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

/**
 * Created by Pieter Jacobs in 2016-02-10.
 * Updated by Pieter Jacobs during 2016/05, 2016/07.
 */
class UploadImageResponse : Serializable {
    @JsonProperty("ErrorMessage")
    var errorMessage: String? = null

    val isSuccess: Boolean
        get() = null == errorMessage || errorMessage == ""
}