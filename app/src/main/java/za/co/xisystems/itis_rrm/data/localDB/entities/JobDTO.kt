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
 * Created by Francis Mahlava on 2019/11/21.
 */

const val JOB_TABLE = "JOB_TABLE"

@Entity(tableName = JOB_TABLE)
class JobDTO(
    @SerializedName("ActId")
    val ActId: Int,
    @SerializedName("JobId")
    @PrimaryKey
    var JobId: String = SqlLitUtils.generateUuid(),
    @SerializedName("ContractVoId")
    var ContractVoId: String?,
    @SerializedName("ProjectId")
    var ProjectId: String?,
    @SerializedName("SectionId")
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
    var IssueDate: String?,
    @SerializedName("StartDate")
    var StartDate: String?,
    @SerializedName("DueDate")
    var DueDate: String?,
    @SerializedName("ApprovalDate")
    var ApprovalDate: String?,
    @SerializedName("MobileJobItemEstimates")
    var JobItemEstimates: ArrayList<JobItemEstimateDTO>,
    @SerializedName("MobileJobItemMeasures")
    var JobItemMeasures: ArrayList<JobItemMeasureDTO>,
    @SerializedName("MobileJobSections")
    var JobSections: ArrayList<JobSectionDTO>,
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
    val WorkStartDate: String?,

    var ESTIMATES_ACT_ID: Int?,

    var MEASURE_ACT_ID: Int?,

    var WORKS_ACT_ID: Int,

    val sortString: String?,

    val ActivityId: Int,

    val Is_synced: String?,

    @SerializedName("Deleted")
    var deleted: Int = 0

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
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
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
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readInt(),
        parcel.readString(),
        parcel.readInt(),
        parcel.readString(),
        parcel.readInt()
    )

    private fun getJobEstimateIndexByItemId(itemId: String?): Int {
        if (itemId != null) for (i in JobItemEstimates.indices) {
            val currEstimate: JobItemEstimateDTO = JobItemEstimates[i]
            if (currEstimate.projectItemId != null && currEstimate.projectItemId.equals(
                    itemId
                )
            ) {
                return i
            }
        }
        return -1
    }

    fun removeJobEstimateByItemId(itemId: String?): JobItemEstimateDTO? {
        val x = getJobEstimateIndexByItemId(itemId)
        return if (x > -1) {
            JobItemEstimates.removeAt(x)
        } else null
    }

    fun getJobEstimateByItemId(itemId: String?): JobItemEstimateDTO? {
        val x = getJobEstimateIndexByItemId(itemId)
        return if (x < 0) null else JobItemEstimates[x]
    }

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
        parcel.writeString(IssueDate)
        parcel.writeString(StartDate)
        parcel.writeString(DueDate)
        parcel.writeString(ApprovalDate)
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
        parcel.writeValue(ESTIMATES_ACT_ID)
        parcel.writeValue(MEASURE_ACT_ID)
        parcel.writeInt(WORKS_ACT_ID)
        parcel.writeString(sortString)
        parcel.writeInt(ActivityId)
        parcel.writeString(Is_synced)
        parcel.writeInt(deleted)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Creator<JobDTO> {
        override fun createFromParcel(parcel: Parcel): JobDTO {
            return JobDTO(parcel)
        }

        override fun newArray(size: Int): Array<JobDTO?> {
            return arrayOfNulls(size)
        }
    }
}
