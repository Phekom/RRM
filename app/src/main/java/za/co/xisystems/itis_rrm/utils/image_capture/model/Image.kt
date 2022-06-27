package za.co.xisystems.itis_rrm.utils.image_capture.model

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable

/**
 * Created by Francis Mahlava on 2021/11/23.
 */

data class Image(
    var uri: Uri,
    var name: String,
    var bucketId: Long = 0,
    var bucketName: String = "",
    var latitude: Double = 0.0,
    var longitude: Double = 0.0

) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readParcelable(Uri::class.java.classLoader)!!,
        parcel.readString()!!,
        parcel.readLong(),
        parcel.readString()!!,
        parcel.readDouble(),
        parcel.readDouble()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(uri, flags)
        parcel.writeString(name)
        parcel.writeLong(bucketId)
        parcel.writeString(bucketName)
        parcel.writeDouble(latitude)
        parcel.writeDouble(longitude)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Image> {
        override fun createFromParcel(parcel: Parcel): Image {
            return Image(parcel)
        }

        override fun newArray(size: Int): Array<Image?> {
            return arrayOfNulls(size)
        }
    }
}