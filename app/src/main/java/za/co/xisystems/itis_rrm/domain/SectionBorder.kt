package za.co.xisystems.itis_rrm.domain

import android.os.Parcel
import android.os.Parcelable

data class SectionBorder(
    val section: String,
    val kmMarker: Double
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readDouble()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(section)
        parcel.writeDouble(kmMarker)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SectionBorder> {
        override fun createFromParcel(parcel: Parcel): SectionBorder {
            return SectionBorder(parcel)
        }

        override fun newArray(size: Int): Array<SectionBorder?> {
            return arrayOfNulls(size)
        }
    }
}
