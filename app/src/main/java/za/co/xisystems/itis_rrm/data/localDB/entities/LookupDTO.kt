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

/**
 * Created by Francis Mahlava on 2019/11/21.
 */

const val LOOKUP_TABLE = "LOOKUP_TABLE"

@Entity(tableName = LOOKUP_TABLE)
class LookupDTO(
//    @SerializedName("ChildLookups")
//    val childLookups: ArrayList<ChildLookupDTO>? = null,
    @SerializedName("LookupName")
    @PrimaryKey
    val lookupName: String,
    @SerializedName("LookupOptions")
    val lookupOptions: ArrayList<LookupOptionDTO> = ArrayList()

) : Serializable, Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        arrayListOf<LookupOptionDTO>().apply {
            parcel.readList(this.toList(), LookupOptionDTO::class.java.classLoader)
        }
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(lookupName)
        parcel.writeList(lookupOptions.toList())
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Creator<LookupDTO> {
        const val serialVersionUID: Long = 15L

        override fun createFromParcel(parcel: Parcel): LookupDTO {
            return LookupDTO(parcel)
        }

        override fun newArray(size: Int): Array<LookupDTO?> {
            return arrayOfNulls(size)
        }
    }
}
