package za.co.xisystems.itis_rrm.data.network.responses

import com.google.gson.annotations.SerializedName
import za.co.xisystems.itis_rrm.data.localDB.entities.ProjectItemDTO

data class ItemsResponse(
    @SerializedName("ErrorMessage")
    val errorMessage: String,
    @SerializedName("Items")
    val items: ArrayList<ProjectItemDTO>
)
