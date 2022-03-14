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

const val JOB_ITEM_ESTIMATE_PHOTO = "JOB_ITEM_ESTIMATE_PHOTO"

@Entity(tableName = JOB_ITEM_ESTIMATE_PHOTO)
data class JobItemEstimatesPhotoDTO(
    @SerializedName("Descr")
    var descr: String,
    @SerializedName("EstimateId")
    var estimateId: String,
    @SerializedName("Filename")
    var filename: String,
    @SerializedName("PhotoDate")
    var photoDate: String,
    @SerializedName("PhotoId")
    @PrimaryKey
    var photoId: String,
    @SerializedName("PhotoStart")
    val photoStart: String?,
    @SerializedName("PhotoEnd")
    val photoEnd: String?,
    @SerializedName("Startkm")
    var startKm: Double = 0.0,
    @SerializedName("Endkm")
    val endKm: Double = 0.0,
    @SerializedName("PhotoLatitude")
    var photoLatitude: Double? = 0.0,
    @SerializedName("PhotoLongitude")
    var photoLongitude: Double? = 0.0,

    @SerializedName("PhotoLatitudeEnd")
    var photoLatitudeEnd: Double = 0.0,
    @SerializedName("PhotoLongitudeEnd")
    var photoLongitudeEnd: Double = 0.0,

    @SerializedName("PhotoPath")
    var photoPath: String,
    @SerializedName("RecordSynchStateId")
    val recordSynchStateId: Int,
    @SerializedName("RecordVersion")
    val recordVersion: Int,
    @SerializedName("IsPhotoStart")
    var isPhotostart: Boolean,

    var sectionMarker: String?,

    val geoCoded: Boolean = false

) : Serializable, Parcelable {

    constructor(parcel: Parcel) : this(
        descr = parcel.readString()!!,
        estimateId = parcel.readString()!!,
        filename = parcel.readString()!!,
        photoDate = parcel.readString()!!,
        photoId = parcel.readString()!!,
        photoStart = parcel.readString(),
        photoEnd = parcel.readString(),
        startKm = parcel.readDouble(),
        endKm = parcel.readDouble(),
        photoLatitude = parcel.readValue(Double::class.java.classLoader) as? Double,
        photoLongitude = parcel.readValue(Double::class.java.classLoader) as? Double,
        photoLatitudeEnd = parcel.readDouble(),
        photoLongitudeEnd = parcel.readDouble(),
        photoPath = parcel.readString()!!,
        recordSynchStateId = parcel.readInt(),
        recordVersion = parcel.readInt(),
        isPhotostart = parcel.readByte() != 0.toByte(),
        sectionMarker = parcel.readString(),
        geoCoded = parcel.readByte() != 0.toByte()
    )

    override fun equals(other: Any?): Boolean {
        return when {
            this === other -> true
            javaClass != other?.javaClass -> false
            else -> {
                other as JobItemEstimatesPhotoDTO
                isOtherEqual(other)
            }
        }
    }

    private fun isOtherEqual(other: JobItemEstimatesPhotoDTO) = when {
        descr != other.descr -> false
        estimateId != other.estimateId -> false
        filename != other.filename -> false
        photoDate != other.photoDate -> false
        photoId != other.photoId -> false
        else -> true
    }

    override fun hashCode(): Int {
        var result = photoId.hashCode()
        result = 31 * result + photoPath.hashCode()
        return result
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(descr)
        parcel.writeString(estimateId)
        parcel.writeString(filename)
        parcel.writeString(photoDate)
        parcel.writeString(photoId)
        parcel.writeString(photoStart)
        parcel.writeString(photoEnd)
        parcel.writeDouble(startKm)
        parcel.writeDouble(endKm)
        parcel.writeValue(photoLatitude)
        parcel.writeValue(photoLongitude)
        parcel.writeDouble(photoLatitudeEnd)
        parcel.writeDouble(photoLongitudeEnd)
        parcel.writeString(photoPath)
        parcel.writeInt(recordSynchStateId)
        parcel.writeInt(recordVersion)
        parcel.writeByte(if (isPhotostart) 1 else 0)
        parcel.writeString(sectionMarker)
        parcel.writeByte(if (geoCoded) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    fun isStartPhoto(): Boolean {
        return this.isPhotostart
    }

    companion object CREATOR : Creator<JobItemEstimatesPhotoDTO> {
        const val serialVersionUID: Long = 11L

        override fun createFromParcel(parcel: Parcel): JobItemEstimatesPhotoDTO {
            return JobItemEstimatesPhotoDTO(parcel)
        }

        override fun newArray(size: Int): Array<JobItemEstimatesPhotoDTO?> {
            return arrayOfNulls(size)
        }
    }
}
