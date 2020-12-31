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

const val JOB_SECTION_TABLE = "JOB_SECTION_TABLE"

@Entity(tableName = JOB_SECTION_TABLE)
class JobSectionDTO(
    @SerializedName("JobSectionId")
    @PrimaryKey
    var jobSectionId: String = SqlLitUtils.generateUuid(),
    @SerializedName("ProjectSectionId")
    var projectSectionId: String?,
    @SerializedName("JobId")
    var jobId: String?,
    @SerializedName("StartKm")
    val startKm: Double,
    @SerializedName("EndKm")
    val endKm: Double,
//    @SerializedName("PrjJobDto")
//    val job: JobDTO? = null,
    @SerializedName("RecordSynchStateId")
    val recordSynchStateId: Int,
    @SerializedName("RecordVersion")
    val recordVersion: Int
) : Serializable, Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString(),
        parcel.readString(),
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readInt(),
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(jobSectionId)
        parcel.writeString(projectSectionId)
        parcel.writeString(jobId)
        parcel.writeDouble(startKm)
        parcel.writeDouble(endKm)
        parcel.writeInt(recordSynchStateId)
        parcel.writeInt(recordVersion)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Creator<JobSectionDTO> {
        override fun createFromParcel(parcel: Parcel): JobSectionDTO {
            return JobSectionDTO(parcel)
        }

        override fun newArray(size: Int): Array<JobSectionDTO?> {
            return arrayOfNulls(size)
        }
    }
}
