package za.co.xisystems.itis_rrm.data.network.responses


import com.fasterxml.jackson.annotation.JsonProperty
import com.google.gson.annotations.SerializedName
import za.co.xisystems.itis_rrm.data.localDB.entities.LookupDTO

data class LookupResponse(
    @SerializedName("ErrorMessage")
    val errorMessage: String,
    @SerializedName("MobileLookups")
    val mobileLookups: ArrayList<LookupDTO>
)