/*
 * Updated by Shaun McDonald on 2021/22/20
 * Last modified on 2021/01/20 12:55 PM
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

/**
 * Created by Francis Mahlava on 2019/11/21.
 */

const val JOB_TABLE = "JOB_TABLE"

@Entity(tableName = JOB_TABLE)
class JobDTO(
    @SerializedName("ActId")
    val actId: Int,
    @SerializedName("JobId")
    @PrimaryKey
    var jobId: String,
    @SerializedName("ContractVoId")
    var contractVoId: String?,
    @SerializedName("ProjectId")
    var projectId: String?,
    @SerializedName("SectionId")
    var sectionId: String?,
    @SerializedName("StartKm")
    var startKm: Double,
    @SerializedName("EndKm")
    var endKm: Double,
    @SerializedName("Descr")
    var descr: String?,
    @SerializedName("JiNo")
    val jiNo: String?,
    @SerializedName("UserId")
    val userId: Int,
    @SerializedName("TrackRouteId")
    var trackRouteId: String?,
    @SerializedName("Section")
    val section: String?,

    @SerializedName("Cpa")
    val Cpa: Int,
    @SerializedName("DayWork")
    val DayWork: Int,

    @SerializedName("ContractorId")
    val ContractorId: Int,
    @SerializedName("M9100")
    val M9100: Int,

    @SerializedName("IssueDate")
    var IssueDate: String? = null,
    @SerializedName("StartDate")
    var StartDate: String? = null,
    @SerializedName("DueDate")
    var DueDate: String? = null,
    @SerializedName("ApprovalDate")
    var ApprovalDate: String? = null,
    @SerializedName("MobileJobItemEstimates")
    var JobItemEstimates: ArrayList<JobItemEstimateDTO> = ArrayList(),
    @SerializedName("MobileJobItemMeasures")
    var JobItemMeasures: ArrayList<JobItemMeasureDTO> = ArrayList(),
    @SerializedName("MobileJobSections")
    var JobSections: ArrayList<JobSectionDTO> = ArrayList(),
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
    val WorkCompleteDate: String? = null,

    @SerializedName("WorkStartDate")
    val WorkStartDate: String? = null,

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
        actId = parcel.readInt(),
        jobId = parcel.readString()!!,
        contractVoId = parcel.readString(),
        projectId = parcel.readString(),
        sectionId = parcel.readString(),
        startKm = parcel.readDouble(),
        endKm = parcel.readDouble(),
        descr = parcel.readString(),
        jiNo = parcel.readString(),
        userId = parcel.readInt(),
        trackRouteId = parcel.readString(),
        section = parcel.readString(),
        Cpa = parcel.readInt(),
        DayWork = parcel.readInt(),
        ContractorId = parcel.readInt(),
        M9100 = parcel.readInt(),
        IssueDate = parcel.readString(),
        StartDate = parcel.readString(),
        DueDate = parcel.readString(),
        ApprovalDate = parcel.readString(),
        JobItemEstimates = arrayListOf<JobItemEstimateDTO>().apply {
            parcel.readList(this.toList(), JobItemEstimateDTO::class.java.classLoader)
        },
        JobItemMeasures = arrayListOf<JobItemMeasureDTO>().apply {
            parcel.readList(this.toList(), JobItemMeasureDTO::class.java.classLoader)
        },
        JobSections = arrayListOf<JobSectionDTO>().apply {
            parcel.readList(this.toList(), JobSectionDTO::class.java.classLoader)
        },
        PerfitemGroupId = parcel.readString(),
        RecordVersion = parcel.readInt(),
        Remarks = parcel.readString(),
        Route = parcel.readString(),
        RrmJiNo = parcel.readString(),
        EngineerId = parcel.readInt(),
        EntireRoute = parcel.readInt(),
        IsExtraWork = parcel.readInt(),
        JobCategoryId = parcel.readInt(),
        JobDirectionId = parcel.readInt(),
        JobPositionId = parcel.readInt(),
        JobStatusId = parcel.readInt(),
        ProjectVoId = parcel.readString(),
        QtyUpdateAllowed = parcel.readInt(),
        RecordSynchStateId = parcel.readInt(),
        VoId = parcel.readString(),
        WorkCompleteDate = parcel.readString(),
        WorkStartDate = parcel.readString(),
        ESTIMATES_ACT_ID = parcel.readValue(Int::class.java.classLoader) as? Int,
        MEASURE_ACT_ID = parcel.readValue(Int::class.java.classLoader) as? Int,
        WORKS_ACT_ID = parcel.readInt(),
        sortString = parcel.readString(),
        ActivityId = parcel.readInt(),
        Is_synced = parcel.readString(),
        deleted = parcel.readInt()
    ) {
    }

    private fun getJobEstimateIndexByItemId(itemId: String?): Int {

        JobItemEstimates.forEachIndexed { index, estimate ->
            if (estimate.projectItemId != null && estimate.projectItemId.equals(
                    itemId
                )
            ) {
                return index
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
        return if (JobItemEstimates.isNullOrEmpty() || x < 0) {
            null
        } else {
            JobItemEstimates[x]
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(actId)
        parcel.writeString(jobId)
        parcel.writeString(contractVoId)
        parcel.writeString(projectId)
        parcel.writeString(sectionId)
        parcel.writeDouble(startKm)
        parcel.writeDouble(endKm)
        parcel.writeString(descr)
        parcel.writeString(jiNo)
        parcel.writeInt(userId)
        parcel.writeString(trackRouteId)
        parcel.writeString(section)
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
        parcel.writeList(JobSections.toList())
        parcel.writeList(JobItemMeasures.toList())
        parcel.writeList(JobItemEstimates.toList())
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
