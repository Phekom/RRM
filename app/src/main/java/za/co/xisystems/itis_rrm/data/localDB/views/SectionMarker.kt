/**
 * Created by Shaun McDonald on 2021/07/02
 * Last modified on 02/07/2021, 14:36
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 */

package za.co.xisystems.itis_rrm.data.localDB.views

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator

data class SectionMarker(
    val sectionId: String,
    val pointLocation: Double
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readDouble()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(sectionId)
        parcel.writeDouble(pointLocation)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Creator<SectionMarker> {
        override fun createFromParcel(parcel: Parcel): SectionMarker {
            return SectionMarker(parcel)
        }

        override fun newArray(size: Int): Array<SectionMarker?> {
            return arrayOfNulls(size)
        }
    }
}
