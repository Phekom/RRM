package za.co.xisystems.itis_rrm.data.network.responses


import com.google.gson.annotations.SerializedName
import za.co.xisystems.itis_rrm.data.localDB.entities.LookupDTO

data class LookupResponse(
    @SerializedName("ErrorMessage")
    val errorMessage: String,
    @SerializedName("MobileLookups")
    val mobileLookups: List<LookupDTO>
)