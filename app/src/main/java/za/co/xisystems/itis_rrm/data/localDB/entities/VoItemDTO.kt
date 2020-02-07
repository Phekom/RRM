package za.co.xisystems.itis_rrm.data.localDB.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.fasterxml.jackson.annotation.JsonProperty
import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Created by Francis Mahlava on 2019/11/21.
 */

const val TABLE_JOB_VO_ITEM = "TABLE_JOB_VO_ITEM"
const val PROJECT_VO_ID = 0

@Entity(tableName = TABLE_JOB_VO_ITEM, foreignKeys = arrayOf(
    ForeignKey(
        entity = ProjectDTO::class,
        parentColumns = arrayOf("projectId"),
        childColumns = arrayOf("projectId"),
        onDelete = ForeignKey.CASCADE)
     )
)
data class VoItemDTO(
    @PrimaryKey
    val id :Int,
    @SerializedName("ProjectVoId")
    val projectVoId: String,
    @SerializedName("ItemCode")
    val itemCode: String?,
    @SerializedName("VoDescr")
    val voDescr: String?,
    @SerializedName("Descr")
    val descr: String?,
    @SerializedName("Uom")
    val uom: String?,
    @SerializedName("Rate")
    val rate: Double?,
    @SerializedName("ProjectItemId")
    val projectItemId: String?,
    @SerializedName("ContractVoId")
    val contractVoId: String?,
    @SerializedName("ContractVoItemId")
    val contractVoItemId: String?,
    @SerializedName("ProjectId")
    @ColumnInfo(name = "projectId", index = true)
    val projectId: String

): Serializable