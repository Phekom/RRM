package za.co.xisystems.itis_rrm.data.models


import androidx.room.Ignore
import com.google.gson.annotations.SerializedName

data class LookupOption(
    @SerializedName("ContextMember")
    val contextMember: String,
    @SerializedName("DisplayMember")
    val displayMember: String,
    @SerializedName("ValueMember")
    val valueMember: String,
    @Ignore
    var LookupName: String
)