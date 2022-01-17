package za.co.xisystems.itis_rrm.domain

import android.os.Parcel
import android.os.Parcelable

data class MeasurementTrack(
    var userId: String,
    var trackRouteId: String,
    var description: String,
    var direction: Int
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(userId)
        parcel.writeString(trackRouteId)
        parcel.writeString(description)
        parcel.writeInt(direction)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MeasurementTrack> {
        override fun createFromParcel(parcel: Parcel): MeasurementTrack {
            return MeasurementTrack(parcel)
        }

        override fun newArray(size: Int): Array<MeasurementTrack?> {
            return arrayOfNulls(size)
        }
    }
}
