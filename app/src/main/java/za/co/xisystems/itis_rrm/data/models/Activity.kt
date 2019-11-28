package za.co.xisystems.itis_rrm.data.models


import com.google.gson.annotations.SerializedName

data class Activity(
    @SerializedName("ActId")
    val actId: Long,
    @SerializedName("ActName")
    val actName: String,
    @SerializedName("ActTypeId")
    val actTypeId: Long,
    @SerializedName("ApprovalId")
    val approvalId: Long,
    @SerializedName("Descr")
    val descr: String,
    val sContentId: Long
)