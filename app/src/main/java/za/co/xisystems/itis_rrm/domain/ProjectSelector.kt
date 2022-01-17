/*
 * Updated by Shaun McDonald on 2021/02/08
 * Last modified on 2021/02/07 12:57 PM
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 */

/*
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 */

package za.co.xisystems.itis_rrm.domain

import android.os.Parcel
import android.os.Parcelable

data class ProjectSelector(
    val projectId: String,
    val projectCode: String,
    val descr: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(projectId)
        parcel.writeString(projectCode)
        parcel.writeString(descr)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ProjectSelector> {
        override fun createFromParcel(parcel: Parcel): ProjectSelector {
            return ProjectSelector(parcel)
        }

        override fun newArray(size: Int): Array<ProjectSelector?> {
            return arrayOfNulls(size)
        }
    }
}
