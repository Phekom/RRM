package za.co.xisystems.itis_rrm.data.models


import com.google.gson.annotations.SerializedName
import za.co.xisystems.itis_rrm.data.localDB.entities.LookupOptionDTO

data class Lookup(
    @SerializedName("ChildLookups")
    val childLookups: Any,
    @SerializedName("LookupName")
    val lookupName: String,
    @SerializedName("LookupOptions")
    val lookupOptions: List<LookupOptionDTO>
)