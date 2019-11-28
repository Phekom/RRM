package za.co.xisystems.itis_rrm.data.localDB.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * Created by Francis Mahlava on 2019/11/22.
 */

const val PROJECT_SECTION_TABLE = "PROJECT_SECTION_TABLE"

@Entity(tableName = PROJECT_SECTION_TABLE)
data class SectionDTO (
    @SerializedName("SectionId")
    @PrimaryKey
    val sectionId: String,

    @SerializedName("Direction")
    val direction: String,
    @SerializedName("EndKm")
    val endKm: Double,
    @SerializedName("ProjectId")
    val projectId: String,
    @SerializedName("Route")
    val route: String,
    @SerializedName("Section")
    val section: String,

    @SerializedName("StartKm")
    val startKm: Double
)