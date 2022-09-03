package za.co.xisystems.itis_rrm.data.network.responses


import com.google.gson.annotations.SerializedName

data class JobCancelResponse(
    @SerializedName("CancelComments")
    val cancelComments: String,
    @SerializedName("CancelReasonID")
    val cancelReasonID: Int,
    @SerializedName("ErrorMessage")
    val errorMessage: String,
    @SerializedName("FileName")
    val fileName: String,
    @SerializedName("JobId")
    val jobId: String,
    @SerializedName("PhotoDate")
    val photoDate: String,
    @SerializedName("PhotoLatitude")
    val photoLatitude: Double,
    @SerializedName("PhotoLongitude")
    val photoLongitude: Double,
    @SerializedName("PhotoPath")
    val photoPath: String,
    @SerializedName("ActivityID")
    var activityID: Int?
)