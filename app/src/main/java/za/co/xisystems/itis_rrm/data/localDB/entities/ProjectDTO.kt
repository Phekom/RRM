/**
 * Updated by Shaun McDonald on 2021/05/15
 * Last modified on 2021/05/14, 20:32
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 **/

/**
 * Updated by Shaun McDonald on 2021/05/14
 * Last modified on 2021/05/14, 19:43
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 **/

package za.co.xisystems.itis_rrm.data.localDB.entities

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable
import org.jetbrains.annotations.NotNull

/**
 * Created by Francis Mahlava on 2019/11/22.
 */

const val PROJECT_TABLE = "PROJECT_TABLE"

@Entity(
    tableName = PROJECT_TABLE,
    foreignKeys = [
        ForeignKey(
            entity = ContractDTO::class,
            parentColumns = arrayOf("contractId"),
            childColumns = arrayOf("contractId"),
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["projectId"], unique = true, name = "")]
)

data class ProjectDTO(
    @PrimaryKey
    @NotNull
    val id: Int,

    @SerializedName("ProjectId")
    @NotNull
    val projectId: String, // A73DEBE5ADDC4D16947857E9AE606278

    @SerializedName("Descr")
    val descr: String?, // MD11 General N2-1

    @SerializedName("EndDate")
    val endDate: String?, // 2020-11-30T00:00:00

    @SerializedName("Items")
    val items: ArrayList<ProjectItemDTO> = ArrayList(),

    @SerializedName("ProjectCode")
    val projectCode: String?, // MD11 General Vuka-Kew N2-1

    @SerializedName("ProjectMinus")
    val projectMinus: String?,

    @SerializedName("ProjectPlus")
    val projectPlus: String?,

    @SerializedName("Sections")
    val projectSections: ArrayList<ProjectSectionDTO> = ArrayList(),

    @SerializedName("VoItems")
    val voItems: ArrayList<VoItemDTO> = ArrayList(),

    @SerializedName("ContractId")
    @ColumnInfo(name = "contractId", index = true)
    val contractId: String

) : Serializable, Parcelable {
    constructor(parcel: Parcel) : this(
        id = parcel.readInt(),
        projectId = parcel.readString()!!,
        descr = parcel.readString(),
        endDate = parcel.readString(),
        items = arrayListOf<ProjectItemDTO>().apply {
            parcel.readList(this.toList(), ProjectItemDTO::class.java.classLoader)
        },
        projectCode = parcel.readString(),
        projectMinus = parcel.readString(),
        projectPlus = parcel.readString(),
        projectSections = arrayListOf<ProjectSectionDTO>().apply {
            parcel.readList(this.toList(), ProjectSectionDTO::class.java.classLoader)
        },
        voItems = arrayListOf<VoItemDTO>().apply {
            parcel.readList(this.toList(), VoItemDTO::class.java.classLoader)
        },
        contractId = parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(projectId)
        parcel.writeString(descr)
        parcel.writeString(endDate)
        parcel.writeString(projectCode)
        parcel.writeString(projectMinus)
        parcel.writeString(projectPlus)
        parcel.writeString(contractId)
        parcel.writeList(items.toList())
        parcel.writeList(projectSections.toList())
        parcel.writeList(voItems.toList())
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Creator<ProjectDTO> {
        const val serialVersionUID: Long = 18L

        override fun createFromParcel(parcel: Parcel): ProjectDTO {
            return ProjectDTO(parcel)
        }

        override fun newArray(size: Int): Array<ProjectDTO?> {
            return arrayOfNulls(size)
        }
    }
}
