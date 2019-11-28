package za.co.xisystems.itis_rrm.data.models


import com.google.gson.annotations.SerializedName

data class InfoClasse(
    val sInfoClassId: String,
    val sLinkId: String,
    @SerializedName("WfId")
    val wfId: Int
)