///**
// * Updated by Shaun McDonald on 2021/05/15
// * Last modified on 2021/05/14, 20:59
// * Copyright (c) 2021.  XI Systems  - All rights reserved
// **/
//
///**
// * Updated by Shaun McDonald on 2021/05/14
// * Last modified on 2021/05/14, 19:43
// * Copyright (c) 2021.  XI Systems  - All rights reserved
// **/
//
package za.co.xisystems.itis_rrm.data.localDB.entities
//
//import android.os.Parcel
//import android.os.Parcelable
//import android.os.Parcelable.Creator
//import androidx.room.Entity
//import androidx.room.PrimaryKey
//import com.google.gson.annotations.SerializedName
//import java.io.Serializable
//import java.util.ArrayList
//import org.jetbrains.annotations.NotNull
//
///**
// * Created by Francis Mahlava on 2019/11/21.
// */
//
//const val JOB_TABLE_TEMP = "JOB_TABLE_TEMP"
//
//@Entity(tableName = JOB_TABLE_TEMP)
//data class JobDTOTemp(
//    @SerializedName("ActId")
//    var actId: Int,
//    @SerializedName("JobId")
//    @PrimaryKey
//    @NotNull
//    var jobId: String,
//
//    @SerializedName("ContractVoId")
//    var contractVoId: String?,
//    @SerializedName("ProjectId")
//    var projectId: String?,
//
//    var sectionId: String?,
//    @SerializedName("StartKm")
//    var startKm: Double,
//    @SerializedName("EndKm")
//    var endKm: Double,
//    @SerializedName("Descr")
//    var descr: String?,
//    @SerializedName("JiNo")
//    val jiNo: String?,
//    @SerializedName("UserId")
//    val userId: Int,
//    @SerializedName("TrackRouteId")
//    var trackRouteId: String?,
//    @SerializedName("Section")
//    val section: String?,
//
//    @SerializedName("Cpa")
//    val cpa: Int,
//    @SerializedName("DayWork")
//    val dayWork: Int,
//    @SerializedName("ContractorId")
//    val contractorId: Int,
//    @SerializedName("M9100")
//    val m9100: Int,
//    @SerializedName("IssueDate")
//    var issueDate: String? = null,
//    @SerializedName("StartDate")
//    var startDate: String? = null,
//    @SerializedName("DueDate")
//    var dueDate: String? = null,
//    @SerializedName("ApprovalDate")
//    val approvalDate: String? = null,
//    @SerializedName("MobileJobItemEstimates")
//    var jobItemEstimates: ArrayList<JobItemEstimateDTO> = arrayListOf(),
//    @SerializedName("MobileJobItemMeasures")
//    var jobItemMeasures: ArrayList<JobItemMeasureDTO> = arrayListOf(),
//    @SerializedName("MobileJobSections")
//    var jobSections: ArrayList<JobSectionDTO> = arrayListOf(),
//    @SerializedName("PerfitemGroupId")
//    var perfitemGroupId: String?,
//
//    @SerializedName("RecordVersion")
//    val recordVersion: Int,
//    @SerializedName("Remarks")
//    val remarks: String?,
//    @SerializedName("Route")
//    var route: String?,
//    @SerializedName("RrmJiNo")
//    val rrmJiNo: String?,
//
//    @SerializedName("EngineerId")
//    val engineerId: Int,
//    @SerializedName("EntireRoute")
//    val entireRoute: Int,
//    @SerializedName("IsExtraWork")
//    val isExtraWork: Int,
//
//    @SerializedName("JobCategoryId")
//    val jobCategoryId: Int,
//    @SerializedName("JobDirectionId")
//    val jobDirectionId: Int,
//
//    @SerializedName("JobPositionId")
//    val jobPositionId: Int,
//    @SerializedName("JobStatusId")
//    val jobStatusId: Int,
//
//    @SerializedName("ProjectVoId")
//
//    var projectVoId: String?,
//    @SerializedName("QtyUpdateAllowed")
//    val qtyUpdateAllowed: Int,
//    @SerializedName("RecordSynchStateId")
//    val recordSynchStateId: Int,
//
//    @SerializedName("VoId")
//    var voId: String?,
//    @SerializedName("WorkCompleteDate")
//    val workCompleteDate: String? = null,
//    @SerializedName("WorkStartDate")
//    val workStartDate: String? = null
//
//) : Serializable, Parcelable {
//    constructor(parcel: Parcel) : this(
//        actId = parcel.readInt(),
//        jobId = parcel.readString()!!,
//        contractVoId = parcel.readString(),
//        projectId = parcel.readString(),
//        sectionId = parcel.readString(),
//        startKm = parcel.readDouble(),
//        endKm = parcel.readDouble(),
//        descr = parcel.readString(),
//        jiNo = parcel.readString(),
//        userId = parcel.readInt(),
//        trackRouteId = parcel.readString(),
//        section = parcel.readString(),
//        cpa = parcel.readInt(),
//        dayWork = parcel.readInt(),
//        contractorId = parcel.readInt(),
//        m9100 = parcel.readInt(),
//        issueDate = parcel.readString(),
//        startDate = parcel.readString(),
//        dueDate = parcel.readString(),
//        approvalDate = parcel.readString(),
//        jobItemEstimates = arrayListOf<JobItemEstimateDTO>().apply {
//            parcel.readList(this.toList(), JobItemEstimateDTO::class.java.classLoader)
//        },
//        jobItemMeasures = arrayListOf<JobItemMeasureDTO>().apply {
//            parcel.readList(this.toList(), JobItemMeasureDTO::class.java.classLoader)
//        },
//        jobSections = arrayListOf<JobSectionDTO>().apply {
//            parcel.readList(this.toList(), JobSectionDTO::class.java.classLoader)
//        },
//        perfitemGroupId = parcel.readString(),
//        recordVersion = parcel.readInt(),
//        remarks = parcel.readString(),
//        route = parcel.readString(),
//        rrmJiNo = parcel.readString(),
//        engineerId = parcel.readInt(),
//        entireRoute = parcel.readInt(),
//        isExtraWork = parcel.readInt(),
//        jobCategoryId = parcel.readInt(),
//        jobDirectionId = parcel.readInt(),
//        jobPositionId = parcel.readInt(),
//        jobStatusId = parcel.readInt(),
//        projectVoId = parcel.readString(),
//        qtyUpdateAllowed = parcel.readInt(),
//        recordSynchStateId = parcel.readInt(),
//        voId = parcel.readString(),
//        workCompleteDate = parcel.readString(),
//        workStartDate = parcel.readString()
//    )
//
//    override fun writeToParcel(parcel: Parcel, flags: Int) {
//        parcel.writeInt(actId)
//        parcel.writeString(jobId)
//        parcel.writeString(contractVoId)
//        parcel.writeString(projectId)
//        parcel.writeString(sectionId)
//        parcel.writeDouble(startKm)
//        parcel.writeDouble(endKm)
//        parcel.writeString(descr)
//        parcel.writeString(jiNo)
//        parcel.writeInt(userId)
//        parcel.writeString(trackRouteId)
//        parcel.writeString(section)
//        parcel.writeInt(cpa)
//        parcel.writeInt(dayWork)
//        parcel.writeInt(contractorId)
//        parcel.writeInt(m9100)
//        parcel.writeString(issueDate)
//        parcel.writeString(startDate)
//        parcel.writeString(dueDate)
//        parcel.writeString(approvalDate)
//        parcel.writeList(jobItemEstimates.toList())
//        parcel.writeList(jobItemMeasures.toList())
//        parcel.writeList(jobSections.toList())
//        parcel.writeString(perfitemGroupId)
//        parcel.writeInt(recordVersion)
//        parcel.writeString(remarks)
//        parcel.writeString(route)
//        parcel.writeString(rrmJiNo)
//        parcel.writeInt(engineerId)
//        parcel.writeInt(entireRoute)
//        parcel.writeInt(isExtraWork)
//        parcel.writeInt(jobCategoryId)
//        parcel.writeInt(jobDirectionId)
//        parcel.writeInt(jobPositionId)
//        parcel.writeInt(jobStatusId)
//        parcel.writeString(projectVoId)
//        parcel.writeInt(qtyUpdateAllowed)
//        parcel.writeInt(recordSynchStateId)
//        parcel.writeString(voId)
//        parcel.writeString(workCompleteDate)
//        parcel.writeString(workStartDate)
//    }
//
//    override fun describeContents(): Int {
//        return 0
//    }
//
//    companion object CREATOR : Creator<JobDTOTemp> {
//        const val serialVersionUID = 7L
//        override fun createFromParcel(parcel: Parcel): JobDTOTemp {
//            return JobDTOTemp(parcel)
//        }
//
//        override fun newArray(size: Int): Array<JobDTOTemp?> {
//            return arrayOfNulls(size)
//        }
//    }
//}
