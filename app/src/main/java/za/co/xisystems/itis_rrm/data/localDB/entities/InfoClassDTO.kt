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
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable
import org.jetbrains.annotations.NotNull

const val INFO_CLASS_TABLE = "INFO_CLASS_TABLE"

@Entity(tableName = INFO_CLASS_TABLE)
data class InfoClassDTO(
    @PrimaryKey
    @NotNull
    val sLinkId: String,

    val sInfoClassId: String?,

    @SerializedName("WfId")
    val wfId: Int?
) : Serializable, Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(sLinkId)
        parcel.writeString(sInfoClassId)
        parcel.writeValue(wfId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Creator<InfoClassDTO> {
        const val serialVersionUID = 3L
        override fun createFromParcel(parcel: Parcel): InfoClassDTO {
            return InfoClassDTO(parcel)
        }

        override fun newArray(size: Int): Array<InfoClassDTO?> {
            return arrayOfNulls(size)
        }
    }
}
