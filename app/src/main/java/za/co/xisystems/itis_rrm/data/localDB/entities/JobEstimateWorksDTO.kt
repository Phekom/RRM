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

const val JOB_ESTIMATE_WORKS = "JOB_ESTIMATE_WORKS"

@Entity(tableName = JOB_ESTIMATE_WORKS)
class JobEstimateWorksDTO(
    @SerializedName("ActId")
    var actId: Int,
    @SerializedName("EstimateId")
    var estimateId: String?,
    @SerializedName("MobileJobEstimateWorksPhotos")
    var jobEstimateWorksPhotos: ArrayList<JobEstimateWorksPhotoDTO>?,
    @SerializedName("RecordSynchStateId")
    var recordSynchStateId: Int,
    @SerializedName("RecordVersion")
    var recordVersion: Int,
    @SerializedName("TrackRouteId")
    var trackRouteId: String,
    @SerializedName("WorksId")
    @PrimaryKey
    var worksId: String = SqlLitUtils.generateUuid()
) : Serializable, Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString(),
        TODO("jobEstimateWorksPhotos"),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(actId)
        parcel.writeString(estimateId)
        parcel.writeInt(recordSynchStateId)
        parcel.writeInt(recordVersion)
        parcel.writeString(trackRouteId)
        parcel.writeString(worksId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Creator<JobEstimateWorksDTO> {
        override fun createFromParcel(parcel: Parcel): JobEstimateWorksDTO {
            return JobEstimateWorksDTO(parcel)
        }

        override fun newArray(size: Int): Array<JobEstimateWorksDTO?> {
            return arrayOfNulls(size)
        }
    }
}
