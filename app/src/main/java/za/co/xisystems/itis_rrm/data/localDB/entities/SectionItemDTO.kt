/*
 * Updated by Shaun McDonald on 2021/01/25
 * Last modified on 2021/01/25 6:30 PM
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 */

/*******************************************************************************
 * Updated by Shaun McDonald on 2021/29/25
 * Last modified on 2021/01/25 3:23 PM
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 ******************************************************************************/

package za.co.xisystems.itis_rrm.data.localDB.entities

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Created by Francis Mahlava on 2019/11/22.
 */

const val SECTION_ITEM_TABLE = "SECTION_ITEM_TABLE"

@Entity(
    tableName = SECTION_ITEM_TABLE,
    indices = [Index(value = ["itemCode"], unique = true)]
)

data class SectionItemDTO(
    @PrimaryKey(autoGenerate = false)
    val id: Int,

    var sectionItemId: String,

    @SerializedName("itemCode")
    var itemCode: String,

    @SerializedName("ActivitySections")
    var description: String?
) : Serializable, Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(sectionItemId)
        parcel.writeString(itemCode)
        parcel.writeString(description)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Creator<SectionItemDTO> {
        const val serialVersionUID: Long = 21L

        override fun createFromParcel(parcel: Parcel): SectionItemDTO {
            return SectionItemDTO(parcel)
        }

        override fun newArray(size: Int): Array<SectionItemDTO?> {
            return arrayOfNulls(size)
        }
    }
}
