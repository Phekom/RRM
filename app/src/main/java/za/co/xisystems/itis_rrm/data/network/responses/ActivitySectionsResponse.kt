package za.co.xisystems.itis_rrm.data.network.responses


import com.fasterxml.jackson.annotation.JsonProperty
import com.google.gson.annotations.SerializedName

/**
 * Created by Francis Mahlava on 2019/10/23.
 */

data class ActivitySectionsResponse(
    @SerializedName("ActivitySections")
    val activitySections: ArrayList<String>,
//    @SerializedName("ActivitySections")
//    val activitySections: ArrayList<SectionItemDTO>,
    @SerializedName("ErrorMessage")
    val errorMessage: String // null
)