package za.co.xisystems.itis_rrm.data.network.responses

import com.google.gson.annotations.SerializedName
import za.co.xisystems.itis_rrm.data.localDB.entities.VoItemDTO

data class VoItemsResponse(
    @SerializedName("ErrorMessage")
    val errorMessage: String,
    @SerializedName("VoItems")
    val voItems: ArrayList<VoItemDTO>
)
