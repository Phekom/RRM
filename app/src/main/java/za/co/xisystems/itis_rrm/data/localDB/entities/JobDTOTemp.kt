package za.co.xisystems.itis_rrm.data.localDB.entities

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.ArrayList
import java.util.Date

/**
 * Created by Francis Mahlava on 2019/11/21.
 */

const val JOB_TABLE_TEMP = "JOB_TABLE_TEMP"

@Entity(tableName = JOB_TABLE_TEMP)
class JobDTOTemp(
    @SerializedName("ActId")
    val ActId: Int,
    @SerializedName("JobId")
    @PrimaryKey
    var JobId: String,
    @SerializedName("ContractVoId")
    var ContractVoId: String?,
    @SerializedName("ProjectId")
    var ProjectId: String?,

    var SectionId: String?,
    @SerializedName("StartKm")
    var StartKm: Double,
    @SerializedName("EndKm")
    var EndKm: Double,
    @SerializedName("Descr")
    var Descr: String?,
    @SerializedName("JiNo")
    val JiNo: String?,
    @SerializedName("UserId")
    val UserId: Int,
    @SerializedName("TrackRouteId")
    var TrackRouteId: String?,
    @SerializedName("Section")
    val Section: String?,

    @SerializedName("Cpa")
    val Cpa: Int,
    @SerializedName("DayWork")
    val DayWork: Int,

    @SerializedName("ContractorId")
    val ContractorId: Int,
    @SerializedName("M9100")
    val M9100: Int,

    @SerializedName("IssueDate")
    var IssueDate: Date? = Date(),
    @SerializedName("StartDate")
    var StartDate: Date? = Date(),
    @SerializedName("DueDate")
    var DueDate: Date? = Date(),
    @SerializedName("ApprovalDate")
    val ApprovalDate: Date? = Date(),

    @SerializedName("MobileJobItemEstimates")
    var JobItemEstimates: ArrayList<JobItemEstimateDTO>?,
    @SerializedName("MobileJobItemMeasures")
    var JobItemMeasures: ArrayList<JobItemMeasureDTO>?,
    @SerializedName("MobileJobSections")
    var JobSections: ArrayList<JobSectionDTO>?,
    @SerializedName("PerfitemGroupId")
    var PerfitemGroupId: String?,
    @SerializedName("RecordVersion")
    val RecordVersion: Int,
    @SerializedName("Remarks")
    val Remarks: String?,
    @SerializedName("Route")
    var Route: String?,
    @SerializedName("RrmJiNo")
    val RrmJiNo: String?,

    @SerializedName("EngineerId")
    val EngineerId: Int,
    @SerializedName("EntireRoute")
    val EntireRoute: Int,
    @SerializedName("IsExtraWork")
    val IsExtraWork: Int,

    @SerializedName("JobCategoryId")
    val JobCategoryId: Int,
    @SerializedName("JobDirectionId")
    val JobDirectionId: Int,

    @SerializedName("JobPositionId")
    val JobPositionId: Int,
    @SerializedName("JobStatusId")
    val JobStatusId: Int,

    @SerializedName("ProjectVoId")
    var ProjectVoId: String?,
    @SerializedName("QtyUpdateAllowed")
    val QtyUpdateAllowed: Int,
    @SerializedName("RecordSynchStateId")
    val RecordSynchStateId: Int,

    @SerializedName("VoId")
    var VoId: String?,
    @SerializedName("WorkCompleteDate")
    val WorkCompleteDate: String?,
    @SerializedName("WorkStartDate")
    val WorkStartDate: String?

) : Serializable, Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readString(),
        parcel.readString(),
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        TODO("IssueDate"),
        TODO("StartDate"),
        TODO("DueDate"),
        TODO("ApprovalDate"),
        TODO("JobItemEstimates"),
        TODO("JobItemMeasures"),
        TODO("JobSections"),
        parcel.readString(),
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(ActId)
        parcel.writeString(JobId)
        parcel.writeString(ContractVoId)
        parcel.writeString(ProjectId)
        parcel.writeString(SectionId)
        parcel.writeDouble(StartKm)
        parcel.writeDouble(EndKm)
        parcel.writeString(Descr)
        parcel.writeString(JiNo)
        parcel.writeInt(UserId)
        parcel.writeString(TrackRouteId)
        parcel.writeString(Section)
        parcel.writeInt(Cpa)
        parcel.writeInt(DayWork)
        parcel.writeInt(ContractorId)
        parcel.writeInt(M9100)
        parcel.writeString(PerfitemGroupId)
        parcel.writeInt(RecordVersion)
        parcel.writeString(Remarks)
        parcel.writeString(Route)
        parcel.writeString(RrmJiNo)
        parcel.writeInt(EngineerId)
        parcel.writeInt(EntireRoute)
        parcel.writeInt(IsExtraWork)
        parcel.writeInt(JobCategoryId)
        parcel.writeInt(JobDirectionId)
        parcel.writeInt(JobPositionId)
        parcel.writeInt(JobStatusId)
        parcel.writeString(ProjectVoId)
        parcel.writeInt(QtyUpdateAllowed)
        parcel.writeInt(RecordSynchStateId)
        parcel.writeString(VoId)
        parcel.writeString(WorkCompleteDate)
        parcel.writeString(WorkStartDate)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Creator<JobDTOTemp> {
        override fun createFromParcel(parcel: Parcel): JobDTOTemp {
            return JobDTOTemp(parcel)
        }

        override fun newArray(size: Int): Array<JobDTOTemp?> {
            return arrayOfNulls(size)
        }
    }
}
