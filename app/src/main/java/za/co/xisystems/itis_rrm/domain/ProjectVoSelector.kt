/*
 * Created By Francis Mahlava
 * Copyright (c) 2022.  XI Systems  - All rights reserved
 */

package za.co.xisystems.itis_rrm.domain

import android.os.Parcel
import android.os.Parcelable

data class ProjectVoSelector(
    val projectId: String,
    val projectVoId: String,
    val voNumber: String?,
    val contractVoId: String?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(projectId)
        parcel.writeString(projectVoId)
        parcel.writeString(voNumber)
        parcel.writeString(contractVoId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ProjectVoSelector> {
        override fun createFromParcel(parcel: Parcel): ProjectVoSelector {
            return ProjectVoSelector(parcel)
        }

        override fun newArray(size: Int): Array<ProjectVoSelector?> {
            return arrayOfNulls(size)
        }
    }
}
