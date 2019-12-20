package za.co.xisystems.itis_rrm.data.localDB.entities

import android.os.Parcel
import android.os.Parcelable
import androidx.room.*
import com.google.gson.annotations.SerializedName
import java.io.Serializable

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
    ,indices = arrayOf(Index(value = ["projectId"],unique = true))
)
data class ProjectDTO(
    @PrimaryKey
    val id: Int,

    @SerializedName("ProjectId")
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


): Serializable, Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        TODO("items"),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        TODO("projectSections"),
        TODO("voItems"),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(projectId)
        parcel.writeString(descr)
        parcel.writeString(endDate)
        parcel.writeString(projectCode)
        parcel.writeString(projectMinus)
        parcel.writeString(projectPlus)
        parcel.writeString(contractId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ProjectDTO> {
        override fun createFromParcel(parcel: Parcel): ProjectDTO {
            return ProjectDTO(parcel)
        }

        override fun newArray(size: Int): Array<ProjectDTO?> {
            return arrayOfNulls(size)
        }
    }
}

