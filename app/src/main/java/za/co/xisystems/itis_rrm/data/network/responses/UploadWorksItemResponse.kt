package za.co.xisystems.itis_rrm.data.network.responses

import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

/**
 * Created by georgezampetakis on 17/03/2017.
 */
class UploadWorksItemResponse : Serializable {
    @JsonProperty("ErrorMessage")
    var errorMessage: String? = null

    val isSuccess: Boolean
        get() = null == errorMessage || errorMessage == ""
}