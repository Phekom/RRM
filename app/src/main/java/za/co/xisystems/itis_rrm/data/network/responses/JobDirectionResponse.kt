package za.co.xisystems.itis_rrm.data.network.responses

import com.google.gson.annotations.SerializedName
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDirectionDTO

data class JobDirectionResponse(
    @SerializedName("ErrorMessage")
    val errorMessage: String,
    @SerializedName("JobDirection")
    val jobDirection: ArrayList<JobDirectionDTO>
)