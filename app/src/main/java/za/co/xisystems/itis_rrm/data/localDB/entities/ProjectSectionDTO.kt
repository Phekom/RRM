package za.co.xisystems.itis_rrm.data.localDB.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.fasterxml.jackson.annotation.JsonProperty
import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Created by Francis Mahlava on 2019/11/22.
 */

const val PROJECT_SECTION_TABLE = "PROJECT_SECTION_TABLE"

@Entity(tableName = PROJECT_SECTION_TABLE , foreignKeys = [
    ForeignKey(
        entity = ProjectDTO::class,
        parentColumns = arrayOf("projectId"),
        childColumns = arrayOf("projectId"),
        onDelete = ForeignKey.CASCADE
    )]
)
data class ProjectSectionDTO (
    @PrimaryKey
    val id : Int,

    @SerializedName("SectionId")
    val sectionId: String,

    @SerializedName("Route")
    val route: String,

    @SerializedName("Section")
    val section: String,

    @SerializedName("StartKm")
    val startKm: Double,
    @SerializedName("EndKm")
    val endKm: Double,


    @SerializedName("Direction")
    val direction: String?,

    @SerializedName("ProjectId")
    @ColumnInfo(name = "projectId", index = true)
    val projectId: String?
) : Serializable