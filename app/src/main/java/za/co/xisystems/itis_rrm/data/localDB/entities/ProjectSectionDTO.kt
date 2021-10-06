/*
 * Updated by Shaun McDonald on 2021/01/25
 * Last modified on 2021/01/25 6:30 PM
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 */

package za.co.xisystems.itis_rrm.data.localDB.entities

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Created by Francis Mahlava on 2019/11/22.
 */

const val PROJECT_SECTION_TABLE = "PROJECT_SECTION_TABLE"

@Entity(
    tableName = PROJECT_SECTION_TABLE,
    foreignKeys = [
        ForeignKey(
            entity = ProjectDTO::class,
            parentColumns = arrayOf("projectId"),
            childColumns = arrayOf("projectId"),
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ProjectSectionDTO(
    @PrimaryKey
    val id: Int,

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
) : Serializable, Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(sectionId)
        parcel.writeString(route)
        parcel.writeString(section)
        parcel.writeDouble(startKm)
        parcel.writeDouble(endKm)
        parcel.writeString(direction)
        parcel.writeString(projectId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Creator<ProjectSectionDTO> {
        const val serialVersionUID: Long = 20L
        override fun createFromParcel(parcel: Parcel): ProjectSectionDTO {
            return ProjectSectionDTO(parcel)
        }

        override fun newArray(size: Int): Array<ProjectSectionDTO?> {
            return arrayOfNulls(size)
        }
    }
}
