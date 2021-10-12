package za.co.xisystems.itis_rrm.data.network.responses

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by Shaun McDonald on 2020/04/14.
 * Copyright (c) 2020 XI Systems. All rights reserved.
 */

/**
 * Error response class from broken API call
 */
data class ErrorResponse(val message: String) : Parcelable {
    constructor(parcel: Parcel) : this(parcel.readString()!!)

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(message)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ErrorResponse> {
        override fun createFromParcel(parcel: Parcel): ErrorResponse {
            return ErrorResponse(parcel)
        }

        override fun newArray(size: Int): Array<ErrorResponse?> {
            return arrayOfNulls(size)
        }
    }
}
