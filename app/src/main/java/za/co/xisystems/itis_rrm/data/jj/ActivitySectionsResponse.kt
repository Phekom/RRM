package za.co.xisystems.itis_rrm.data.jj


import com.google.gson.annotations.SerializedName

data class ActivitySectionsResponse(
    @SerializedName("ActivitySections")
    val activitySections: List<String>,
    @SerializedName("ErrorMessage")
    val errorMessage: String // null
)