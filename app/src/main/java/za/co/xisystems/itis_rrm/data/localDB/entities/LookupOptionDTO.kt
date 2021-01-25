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
 * Created by Francis Mahlava on 2019/11/21.
 */

const val LOOKUP_OPTION_TABLE = "LOOKUP_OPTION_TABLE"

@Entity(
    tableName = LOOKUP_OPTION_TABLE, foreignKeys = [ForeignKey(
        entity = LookupDTO::class,
        parentColumns = arrayOf("lookupName"),
        childColumns = arrayOf("lookupName"),
        onDelete = ForeignKey.CASCADE
    )]
)
class LookupOptionDTO(

    @PrimaryKey
    val id: Int = 0,

    @SerializedName("ValueMember")
    val valueMember: String?, // 3920

    @SerializedName("DisplayMember")
    val displayMember: String?, // Kallie Niebuhr

    @SerializedName("ContextMember")
    val contextMember: String?,

    @SerializedName("LookupName")
    @ColumnInfo(name = "lookupName", index = true)
    var lookupName: String

) : Serializable, Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(valueMember)
        parcel.writeString(displayMember)
        parcel.writeString(contextMember)
        parcel.writeString(lookupName)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Creator<LookupOptionDTO> {
        override fun createFromParcel(parcel: Parcel): LookupOptionDTO {
            return LookupOptionDTO(parcel)
        }

        override fun newArray(size: Int): Array<LookupOptionDTO?> {
            return arrayOfNulls(size)
        }
    }
}
