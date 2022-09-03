package za.co.xisystems.itis_rrm.data.localDB.entities


import com.google.gson.annotations.SerializedName

data class JobDeclineDTO(
    @SerializedName("CancelComments")
    var cancelComments: String?,
    @SerializedName("CancelReasonID")
    val cancelReasonID: Int?,
    @SerializedName("FileName")
    val fileName: String?,
    @SerializedName("JobId")
    val jobId: String?,
    @SerializedName("PhotoDate")
    val photoDate: String?,
    @SerializedName("PhotoLatitude")
    val photoLatitude: Double?,
    @SerializedName("PhotoLongitude")
    val photoLongitude: Double?,
    @SerializedName("PhotoPath")
    val photoPath: String?
)