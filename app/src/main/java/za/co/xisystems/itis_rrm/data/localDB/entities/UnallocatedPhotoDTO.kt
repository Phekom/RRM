package za.co.xisystems.itis_rrm.data.localDB.entities

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable

const val UNALLOCATED_PHOTO_TABLE_NAME = "UNALLOCATED_PHOTOS"

@Entity(
    tableName = UNALLOCATED_PHOTO_TABLE_NAME
)
data class UnallocatedPhotoDTO(
    @PrimaryKey(autoGenerate = true)
    var id: Long,
    @SerializedName("Descr")
    val descr: String,
    @SerializedName("Filename")
    val filename: String,
    @SerializedName("PhotoDate")
    val photoDate: String,
    @SerializedName("PhotoId")
    var photoId: String,
    @SerializedName("PhotoLatitude")
    val photoLatitude: Double,
    @SerializedName("PhotoLongitude")
    val photoLongitude: Double,
    @SerializedName("KmMarker")
    val kmMarker: Double?,
    @SerializedName("PhotoPath")
    val photoPath: String,
    @SerializedName("RecordSynchStateId")
    val recordSynchStateId: Int,
    @SerializedName("RecordVersion")
    val recordVersion: Int,
    @SerializedName("RouteMarker")
    var routeMarker: String?,
    @SerializedName("Allocated")
    var allocated: Boolean,
    var pxHeight: Int,
    var pxWidth: Int,

) : Serializable, Parcelable {
    constructor(parcel: Parcel) : this(
        id = parcel.readLong(),
        descr = parcel.readString()!!,
        filename = parcel.readString()!!,
        photoDate = parcel.readString()!!,
        photoId = parcel.readString()!!,
        photoLatitude = parcel.readDouble(),
        photoLongitude = parcel.readDouble(),
        kmMarker = parcel.readValue(Double::class.java.classLoader) as? Double,
        photoPath = parcel.readString()!!,
        recordSynchStateId = parcel.readInt(),
        recordVersion = parcel.readInt(),
        routeMarker = parcel.readString(),
        allocated = parcel.readByte() != 0.toByte(),
        pxHeight = parcel.readInt(),
        pxWidth = parcel.readInt()
    )

    val aspectRatio: Float get() = pxHeight.toFloat() * pxWidth.toFloat()

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(descr)
        parcel.writeString(filename)
        parcel.writeString(photoDate)
        parcel.writeString(photoId)
        parcel.writeDouble(photoLatitude)
        parcel.writeDouble(photoLongitude)
        parcel.writeValue(kmMarker)
        parcel.writeString(photoPath)
        parcel.writeInt(recordSynchStateId)
        parcel.writeInt(recordVersion)
        parcel.writeString(routeMarker)
        parcel.writeByte(if (allocated) 1 else 0)
        parcel.writeInt(pxHeight)
        parcel.writeInt(pxWidth)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<UnallocatedPhotoDTO> {
        override fun createFromParcel(parcel: Parcel): UnallocatedPhotoDTO {
            return UnallocatedPhotoDTO(parcel)
        }

        override fun newArray(size: Int): Array<UnallocatedPhotoDTO?> {
            return arrayOfNulls(size)
        }
    }
}
