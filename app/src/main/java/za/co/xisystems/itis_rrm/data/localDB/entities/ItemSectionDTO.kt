/*
 * Updated by Shaun McDonald on 2021/01/25
 * Last modified on 2021/01/25 6:30 PM
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 */

package za.co.xisystems.itis_rrm.data.localDB.entities

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable
import org.jetbrains.annotations.NotNull

/**
 * Created by Francis Mahlava on 2019/11/26.
 */

const val ITEM_SECTION_TABLE = "ITEM_SECTION_TABLE"

@Entity(tableName = ITEM_SECTION_TABLE)
data class ItemSectionDTO(

    @SerializedName("SectionId")
    @PrimaryKey
    @NotNull
    val sectionId: String,
    @SerializedName("Direction")
    val direction: String?,
    @SerializedName("EndKm")
    val endKm: Double,
    @SerializedName("ProjectId")
    val projectId: String?,
    @SerializedName("Route")
    val route: String?,
    @SerializedName("Section")
    val section: String?,

    @SerializedName("StartKm")
    val startKm: Double
) : Serializable, Parcelable {
    constructor(parcel: Parcel) : this(
        sectionId = parcel.readString()!!,
        direction = parcel.readString(),
        endKm = parcel.readDouble(),
        projectId = parcel.readString(),
        route = parcel.readString(),
        section = parcel.readString(),
        startKm = parcel.readDouble()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(sectionId)
        parcel.writeString(direction)
        parcel.writeDouble(endKm)
        parcel.writeString(projectId)
        parcel.writeString(route)
        parcel.writeString(section)
        parcel.writeDouble(startKm)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Creator<ItemSectionDTO> {
        const val serialVersionUID = 5L
        override fun createFromParcel(parcel: Parcel): ItemSectionDTO {
            return ItemSectionDTO(parcel)
        }

        override fun newArray(size: Int): Array<ItemSectionDTO?> {
            return arrayOfNulls(size)
        }
    }
}
