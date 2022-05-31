package za.co.xisystems.itis_rrm.data.network.responses

import com.google.gson.annotations.SerializedName
import za.co.xisystems.itis_rrm.data.localDB.entities.JobCategoryDTO

data class JobCategoryResponse(
    @SerializedName("ErrorMessage")
    val errorMessage: String,
    @SerializedName("JobCategory")
    val jobCategory: ArrayList<JobCategoryDTO>
)