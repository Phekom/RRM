package za.co.xisystems.itis_rrm.data.network.request

import com.fasterxml.jackson.annotation.JsonProperty
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTOTemp
import java.io.Serializable

/**
 * Created by Pieter Jacobs on 2016/01/20.
 * Updated by Pieter Jacobs during 2016/06.
 */
class RrmJobRequest : Serializable {
    @JsonProperty("Job")
    var job: JobDTOTemp? = null
    @JsonProperty("UserId")
    var userId = 0
}
