/*
 * Updated by Shaun McDonald on 2021/02/04
 * Last modified on 2021/02/04 11:19 AM
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 */

package za.co.xisystems.itis_rrm.data.localDB.entities

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Created by Francis Mahlava on 2019/11/21.
 */

const val JOB_TABLE = "JOB_TABLE"

@Entity(
    indices = [Index(value = ["projectId", "jobId"], unique = true, name = "idxjobIdByProject")],
    tableName = JOB_TABLE
)

class JobDTO(
    @SerializedName("ActId")
    val actId: Int,
    @PrimaryKey
    @SerializedName("JobId")
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
    val cpa: Int,
    @SerializedName("DayWork")
    val dayWork: Int,

    @SerializedName("ContractorId")
    val contractorId: Int,
    @SerializedName("M9100")
    val m9100: Int,

    @SerializedName("IssueDate")
    var issueDate: String? = null,
    @SerializedName("StartDate")
    var startDate: String? = null,
    @SerializedName("DueDate")
    var dueDate: String? = null,
    @SerializedName("ApprovalDate")
    var approvalDate: String? = null,
    @SerializedName("MobileJobItemEstimates")
    var jobItemEstimates: ArrayList<JobItemEstimateDTO> = ArrayList(),
    @SerializedName("MobileJobItemMeasures")
    var jobItemMeasures: ArrayList<JobItemMeasureDTO> = ArrayList(),
    @SerializedName("MobileJobSections")
    var jobSections: ArrayList<JobSectionDTO> = ArrayList(),
    @SerializedName("PerfitemGroupId")
    var perfitemGroupId: String?,

    @SerializedName("RecordVersion")
    val recordVersion: Int,

    @SerializedName("Remarks")
    val remarks: String?,

    @SerializedName("Route")
    var route: String?,

    @SerializedName("RrmJiNo")
    val rrmJiNo: String?,

    @SerializedName("EngineerId")
    val engineerId: Int,

    @SerializedName("EntireRoute")
    val entireRoute: Int,

    @SerializedName("IsExtraWork")
    val isExtraWork: Int,

    @SerializedName("JobCategoryId")
    val jobCategoryId: Int,

    @SerializedName("JobDirectionId")
    val jobDirectionId: Int,

    @SerializedName("JobPositionId")
    val jobPositionId: Int,

    @SerializedName("JobStatusId")
    val jobStatusId: Int,

    @SerializedName("ProjectVoId")
    var projectVoId: String?,

    @SerializedName("QtyUpdateAllowed")
    val qtyUpdateAllowed: Int,

    @SerializedName("RecordSynchStateId")
    val recordSynchStateId: Int,

    @SerializedName("VoId")
    var voId: String?,

    @SerializedName("WorkCompleteDate")
    val workCompleteDate: String? = null,

    @SerializedName("WorkStartDate")
    val workStartDate: String? = null,

    @SerializedName("ESTIMATES_ACT_ID")
    var estimatesActId: Int?,

    @SerializedName("MEASURE_ACT_ID")
    var measureActId: Int?,

    @SerializedName("WORKS_ACT_ID")
    var worksActId: Int,

    val sortString: String?,

    @SerializedName("ActivityId")
    val activityId: Int,

    @SerializedName("Is_synced")
    val isSynced: String?,

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
        cpa = parcel.readInt(),
        dayWork = parcel.readInt(),
        contractorId = parcel.readInt(),
        m9100 = parcel.readInt(),
        issueDate = parcel.readString(),
        startDate = parcel.readString(),
        dueDate = parcel.readString(),
        approvalDate = parcel.readString(),
        jobItemEstimates = arrayListOf<JobItemEstimateDTO>().apply {
            parcel.readList(this.toList(), JobItemEstimateDTO::class.java.classLoader)
        },
        jobItemMeasures = arrayListOf<JobItemMeasureDTO>().apply {
            parcel.readList(this.toList(), JobItemMeasureDTO::class.java.classLoader)
        },
        jobSections = arrayListOf<JobSectionDTO>().apply {
            parcel.readList(this.toList(), JobSectionDTO::class.java.classLoader)
        },
        perfitemGroupId = parcel.readString(),
        recordVersion = parcel.readInt(),
        remarks = parcel.readString(),
        route = parcel.readString(),
        rrmJiNo = parcel.readString(),
        engineerId = parcel.readInt(),
        entireRoute = parcel.readInt(),
        isExtraWork = parcel.readInt(),
        jobCategoryId = parcel.readInt(),
        jobDirectionId = parcel.readInt(),
        jobPositionId = parcel.readInt(),
        jobStatusId = parcel.readInt(),
        projectVoId = parcel.readString(),
        qtyUpdateAllowed = parcel.readInt(),
        recordSynchStateId = parcel.readInt(),
        voId = parcel.readString(),
        workCompleteDate = parcel.readString(),
        workStartDate = parcel.readString(),
        estimatesActId = parcel.readValue(Int::class.java.classLoader) as? Int,
        measureActId = parcel.readValue(Int::class.java.classLoader) as? Int,
        worksActId = parcel.readInt(),
        sortString = parcel.readString(),
        activityId = parcel.readInt(),
        isSynced = parcel.readString(),
        deleted = parcel.readInt()
    )

    private fun getJobEstimateIndexByItemId(itemId: String?): Int {

        jobItemEstimates.forEachIndexed { index, estimate ->
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
            jobItemEstimates.removeAt(x)
        } else null
    }

    fun getJobEstimateByItemId(itemId: String?): JobItemEstimateDTO? {
        val x = getJobEstimateIndexByItemId(itemId)
        return if (jobItemEstimates.isNullOrEmpty() || x < 0) {
            null
        } else {
            jobItemEstimates[x]
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
        parcel.writeInt(cpa)
        parcel.writeInt(dayWork)
        parcel.writeInt(contractorId)
        parcel.writeInt(m9100)
        parcel.writeString(issueDate)
        parcel.writeString(startDate)
        parcel.writeString(dueDate)
        parcel.writeString(approvalDate)
        parcel.writeString(perfitemGroupId)
        parcel.writeInt(recordVersion)
        parcel.writeString(remarks)
        parcel.writeString(route)
        parcel.writeString(rrmJiNo)
        parcel.writeInt(engineerId)
        parcel.writeInt(entireRoute)
        parcel.writeInt(isExtraWork)
        parcel.writeInt(jobCategoryId)
        parcel.writeInt(jobDirectionId)
        parcel.writeInt(jobPositionId)
        parcel.writeInt(jobStatusId)
        parcel.writeString(projectVoId)
        parcel.writeInt(qtyUpdateAllowed)
        parcel.writeInt(recordSynchStateId)
        parcel.writeString(voId)
        parcel.writeString(workCompleteDate)
        parcel.writeString(workStartDate)
        parcel.writeValue(estimatesActId)
        parcel.writeValue(measureActId)
        parcel.writeInt(worksActId)
        parcel.writeString(sortString)
        parcel.writeInt(activityId)
        parcel.writeString(isSynced)
        parcel.writeInt(deleted)
        parcel.writeList(jobSections.toList())
        parcel.writeList(jobItemMeasures.toList())
        parcel.writeList(jobItemEstimates.toList())
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
