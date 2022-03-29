/*
 * Created By Francis Mahlava
 * Copyright (c) 2022.  XI Systems  - All rights reserved
 */

package za.co.xisystems.itis_rrm.domain

import android.os.Parcel
import android.os.Parcelable

data class ContractVoSelector(
    val contractVoId: String?,
    val nRAApprovalNumber: String,
    val voNumber: String?,
//    val projectId: String,
//    val projectVoId: String,
//    val voNumber: String?,
//    val contractVoId: String?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(contractVoId)
        parcel.writeString(nRAApprovalNumber)
        parcel.writeString(voNumber)

    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ContractVoSelector> {
        override fun createFromParcel(parcel: Parcel): ContractVoSelector {
            return ContractVoSelector(parcel)
        }

        override fun newArray(size: Int): Array<ContractVoSelector?> {
            return arrayOfNulls(size)
        }
    }
}
