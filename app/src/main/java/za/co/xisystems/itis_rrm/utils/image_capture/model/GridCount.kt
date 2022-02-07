package za.co.xisystems.itis_rrm.utils.image_capture.model

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by Francis Mahlava on 2021/11/23.
 */
data class GridCount(val portrait: Int, val landscape: Int) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(portrait)
        parcel.writeInt(landscape)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<GridCount> {
        override fun createFromParcel(parcel: Parcel): GridCount {
            return GridCount(parcel)
        }

        override fun newArray(size: Int): Array<GridCount?> {
            return arrayOfNulls(size)
        }
    }
}