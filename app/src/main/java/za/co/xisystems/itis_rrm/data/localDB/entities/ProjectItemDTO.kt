package za.co.xisystems.itis_rrm.data.localDB.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import java.io.Serializable

/**
 * Created by Francis Mahlava on 2019/11/21.
 */

const val PROJECT_ITEM_TABLE = "PROJECT_ITEM_TABLE"

@Entity(
    tableName = PROJECT_ITEM_TABLE, foreignKeys = [
        ForeignKey(
            entity = ProjectDTO::class,
            parentColumns = arrayOf("projectId"),
            childColumns = arrayOf("projectId"),
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ProjectItemDTO(
    @PrimaryKey
    val id: Int,
    @SerializedName("ItemId")
    @NotNull
    val itemId: String,
    @SerializedName("Descr")
    val descr: String?,
    @SerializedName("ItemCode")
    val itemCode: String?,
    @Nullable
    @SerializedName("ItemSections")
    val itemSections: ArrayList<ItemSectionDTO>?,
    @SerializedName("TenderRate")
    val tenderRate: Double = 0.0,
    @SerializedName("Uom")
    val uom: String?,
    @SerializedName("WorkflowId")
    val workflowId: Int?,

    val sectionItemId: String?,

    val quantity: Double = 0.toDouble(),

    val estimateId: String?,

    @SerializedName("ProjectId")
    @NotNull
    @ColumnInfo(name = "projectId", index = true)
    val projectId: String?

) : Serializable
