/*
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 */

package za.co.xisystems.itis_rrm.domain

import android.os.Parcel
import android.os.Parcelable

data class ContractSelector(
    val contractId: String,
    val contractNo: String?,
    val shortDescr: String?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(contractId)
        parcel.writeString(contractNo)
        parcel.writeString(shortDescr)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ContractSelector> {
        override fun createFromParcel(parcel: Parcel): ContractSelector {
            return ContractSelector(parcel)
        }

        override fun newArray(size: Int): Array<ContractSelector?> {
            return arrayOfNulls(size)
        }
    }
}
