package za.co.xisystems.itis_rrm.data.network.responses

import com.google.gson.annotations.SerializedName
import za.co.xisystems.itis_rrm.data.localDB.entities.JobPositionDTO

data class JobPositionResponse(
    @SerializedName("ErrorMessage")
    val errorMessage: String,
    @SerializedName("JobPosition")
    val jobPosition: ArrayList<JobPositionDTO>
)