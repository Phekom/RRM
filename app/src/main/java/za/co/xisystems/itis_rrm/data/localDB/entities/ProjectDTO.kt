package za.co.xisystems.itis_rrm.data.localDB.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * Created by Francis Mahlava on 2019/11/22.
 */

const val PROJECT_TABLE = "PROJECT_TABLE"

@Entity(
    tableName = PROJECT_TABLE
    , foreignKeys = arrayOf(
        ForeignKey(
            entity = ContractDTO::class,
            parentColumns = arrayOf("contractId"),
            childColumns = arrayOf("contractId"),
            onDelete = ForeignKey.NO_ACTION
        )
    )
//    ,indices = arrayOf(Index(value = ["projectId"],unique = true))
)
data class ProjectDTO(

    @SerializedName("ProjectId")
    @PrimaryKey
    val projectId: String,

    @SerializedName("Descr")
    val descr: String?,

    @SerializedName("EndDate")
    val endDate: String?,

    @SerializedName("Items")
    val items: ArrayList<ItemDTO>?,

    @SerializedName("ProjectCode")
    val projectCode: String?,

    @SerializedName("ProjectMinus")
    val projectMinus: String?,

    @SerializedName("ProjectPlus")
    val projectPlus: String?,

    @SerializedName("Sections")
    val projectSections: ArrayList<ProjectSectionDTO>?,

    @SerializedName("VoItems")
    val voItems: ArrayList<VoItemDTO>?,

    @SerializedName("ContractId")
    @ColumnInfo(name = "contractId", index = true)
    val contractId: String?


)

