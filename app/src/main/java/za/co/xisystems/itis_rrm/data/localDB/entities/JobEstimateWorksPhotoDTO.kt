package za.co.xisystems.itis_rrm.data.localDB.entities

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import za.co.xisystems.itis_rrm.utils.SqlLitUtils
import java.io.Serializable

/**
 * Created by Francis Mahlava on 2019/11/26.
 */

const val JOB_ESTIMATE_WORKS_PHOTO = "JOB_ESTIMATE_WORKS_PHOTO"

@Entity(tableName = JOB_ESTIMATE_WORKS_PHOTO)
data class JobEstimateWorksPhotoDTO(
    @PrimaryKey(autoGenerate = true)
    var id: Int,
    @SerializedName("Descr")
    val descr: String,
    @SerializedName("Filename")
    val filename: String,
    @SerializedName("PhotoActivityId")
    val photoActivityId: Int,
    @SerializedName("PhotoDate")
    val photoDate: String,
    @SerializedName("PhotoId")
    var photoId: String = SqlLitUtils.generateUuid(),
    @SerializedName("PhotoLatitude")
    val photoLatitude: Double,
    @SerializedName("PhotoLongitude")
    val photoLongitude: Double,
    @SerializedName("PhotoPath")
    val photoPath: String,
    @SerializedName("RecordSynchStateId")
    val recordSynchStateId: Int,
    @SerializedName("RecordVersion")
    val recordVersion: Int,
    @SerializedName("WorksId")
    var worksId: String
) : Serializable, Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(descr)
        parcel.writeString(filename)
        parcel.writeInt(photoActivityId)
        parcel.writeString(photoDate)
        parcel.writeString(photoId)
        parcel.writeDouble(photoLatitude)
        parcel.writeDouble(photoLongitude)
        parcel.writeString(photoPath)
        parcel.writeInt(recordSynchStateId)
        parcel.writeInt(recordVersion)
        parcel.writeString(worksId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Creator<JobEstimateWorksPhotoDTO> {
        override fun createFromParcel(parcel: Parcel): JobEstimateWorksPhotoDTO {
            return JobEstimateWorksPhotoDTO(parcel)
        }

        override fun newArray(size: Int): Array<JobEstimateWorksPhotoDTO?> {
            return arrayOfNulls(size)
        }
    }
}
