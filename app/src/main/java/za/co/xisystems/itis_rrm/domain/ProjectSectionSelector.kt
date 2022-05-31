/*
 * Updated by Francis Mahlava on 2022/05/16
 * Last modified on 2022/05/16 12:57 PM
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 */

/*
 * Copyright (c) 2022.  XI Systems  - All rights reserved
 */

package za.co.xisystems.itis_rrm.domain

import android.os.Parcel
import android.os.Parcelable

data class ProjectSectionSelector(
    val projectId: String,
    val sectionId: String,
    val route : String,
    val section: String,
    val direction: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(projectId)
        parcel.writeString(sectionId)
        parcel.writeString(route)
        parcel.writeString(section)
        parcel.writeString(direction)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ProjectSectionSelector> {
        override fun createFromParcel(parcel: Parcel): ProjectSectionSelector {
            return ProjectSectionSelector(parcel)
        }

        override fun newArray(size: Int): Array<ProjectSectionSelector?> {
            return arrayOfNulls(size)
        }
    }
}
