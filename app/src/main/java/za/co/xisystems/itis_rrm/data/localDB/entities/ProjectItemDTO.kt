package za.co.xisystems.itis_rrm.data.localDB.entities

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
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

) : Serializable, Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readString(),
        parcel.readString(),
        TODO("itemSections"),
        parcel.readDouble(),
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString(),
        parcel.readDouble(),
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(itemId)
        parcel.writeString(descr)
        parcel.writeString(itemCode)
        parcel.writeDouble(tenderRate)
        parcel.writeString(uom)
        parcel.writeValue(workflowId)
        parcel.writeString(sectionItemId)
        parcel.writeDouble(quantity)
        parcel.writeString(estimateId)
        parcel.writeString(projectId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Creator<ProjectItemDTO> {
        override fun createFromParcel(parcel: Parcel): ProjectItemDTO {
            return ProjectItemDTO(parcel)
        }

        override fun newArray(size: Int): Array<ProjectItemDTO?> {
            return arrayOfNulls(size)
        }
    }
}
