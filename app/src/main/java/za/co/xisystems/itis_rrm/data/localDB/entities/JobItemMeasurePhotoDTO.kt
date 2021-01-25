/*
 * Updated by Shaun McDonald on 2021/01/25
 * Last modified on 2021/01/25 6:30 PM
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 */

package za.co.xisystems.itis_rrm.data.localDB.entities

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable

const val JOB_ITEM_MEASURE_PHOTO = "JOB_ITEM_MEASURE_PHOTO"

@Entity(tableName = JOB_ITEM_MEASURE_PHOTO)
data class JobItemMeasurePhotoDTO(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    @SerializedName("Descr")
    val descr: String?,
    @SerializedName("Filename")
    val filename: String?,
    @SerializedName("EstimateId")
    var estimateId: String?,
    @SerializedName("ItemMeasureId")
    var itemMeasureId: String?,
    @SerializedName("PhotoDate")
    val photoDate: String?,
    @SerializedName("PhotoId")
    var photoId: String,
    @SerializedName("PhotoLatitude")
    val photoLatitude: Double,
    @SerializedName("PhotoLongitude")
    val photoLongitude: Double,
    @SerializedName("PhotoPath")
    var photoPath: String?,
    @SerializedName("RecordSynchStateId")
    val recordSynchStateId: Int,
    @SerializedName("RecordVersion")
    val recordVersion: Int
) : Serializable, Parcelable {
    constructor(parcel: Parcel) : this(
        id = parcel.readInt(),
        descr = parcel.readString(),
        filename = parcel.readString(),
        estimateId = parcel.readString(),
        itemMeasureId = parcel.readString(),
        photoDate = parcel.readString(),
        photoId = parcel.readString()!!,
        photoLatitude = parcel.readDouble(),
        photoLongitude = parcel.readDouble(),
        photoPath = parcel.readString(),
        recordSynchStateId = parcel.readInt(),
        recordVersion = parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(descr)
        parcel.writeString(filename)
        parcel.writeString(estimateId)
        parcel.writeString(itemMeasureId)
        parcel.writeString(photoDate)
        parcel.writeString(photoId)
        parcel.writeDouble(photoLatitude)
        parcel.writeDouble(photoLongitude)
        parcel.writeString(photoPath)
        parcel.writeInt(recordSynchStateId)
        parcel.writeInt(recordVersion)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Creator<JobItemMeasurePhotoDTO> {
        override fun createFromParcel(parcel: Parcel): JobItemMeasurePhotoDTO {
            return JobItemMeasurePhotoDTO(parcel)
        }

        override fun newArray(size: Int): Array<JobItemMeasurePhotoDTO?> {
            return arrayOfNulls(size)
        }
    }
}
