package za.co.xisystems.itis_rrm.data.network.request

import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO

/**
 * Created by Pieter Jacobs on 2016/01/20.
 * Updated by Pieter Jacobs during 2016/06.
 */
class RrmJobRequest : Serializable {
    @JsonProperty("Job")
    var job: JobDTO? = null

    @JsonProperty("UserId")
    var userId = 0
}
