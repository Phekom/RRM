package za.co.xisystems.itis_rrm.data.localDB.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.util.*

/**
 * Created by Francis Mahlava on 2019/11/21.
 */

const val JOB_TABLE = "JOB_TABLE"
//val JOB_ID = UUID.randomUUID().toString()

@Entity(tableName = JOB_TABLE)
class JobDTO(
    @SerializedName("JobId")
    @PrimaryKey(autoGenerate = false)
    val jobId: String = UUID.randomUUID().toString(),

    @SerializedName("ActId")
    val actId: Int,
    @SerializedName("ApprovalDate")
    val approvalDate:String = Date().toString(),
    @SerializedName("ContractVoId")
    val contractVoId: String,
    @SerializedName("ContractorId")
    val contractorId: Int,
    @SerializedName("Cpa")
    val cpa: Int,
    @SerializedName("DayWork")
    val dayWork: Int,
    @SerializedName("Descr")
    val descr: String,
    @SerializedName("DueDate")
    val dueDate:  String = Date().toString(),
    @SerializedName("EndKm")
    val endKm: Double,
    @SerializedName("EngineerId")
    val engineerId: Int,
    @SerializedName("EntireRoute")
    val entireRoute: Int,
    @SerializedName("IsExtraWork")
    val isExtraWork: Int,
    @SerializedName("IssueDate")
    val issueDate: String = Date().toString(),
    @SerializedName("JiNo")
    val jiNo: String,
    @SerializedName("JobCategoryId")
    val jobCategoryId: Int,
    @SerializedName("JobDirectionId")
    val jobDirectionId: Int,

    @SerializedName("JobPositionId")
    val jobPositionId: Int,
    @SerializedName("JobStatusId")
    val jobStatusId: Int,
    @SerializedName("M9100")
    val m9100: Int,
    @SerializedName("MobileJobItemEstimates")
    val jobItemEstimates: ArrayList<JobItemEstimateDTO>,
    @SerializedName("MobileJobItemMeasures")
    val jobItemMeasures: ArrayList<JobItemMeasureDTO>,
    @SerializedName("MobileJobSections")
    val jobSections: ArrayList<JobSectionDTO>,
    @SerializedName("PerfitemGroupId")
    val perfitemGroupId: String,
    @SerializedName("ProjectId")
    val projectId: String,
    @SerializedName("ProjectVoId")
    val projectVoId: String,
    @SerializedName("QtyUpdateAllowed")
    val qtyUpdateAllowed: Int,
    @SerializedName("RecordSynchStateId")
    val recordSynchStateId: Int,
    @SerializedName("RecordVersion")
    val recordVersion: Int,
    @SerializedName("Remarks")
    val remarks: String,
    @SerializedName("Route")
    val route: String,
    @SerializedName("RrmJiNo")
    val rrmJiNo: String,
    @SerializedName("Section")
    val section: String,
    @SerializedName("StartDate")
    val startDate: String = Date().toString(),
    @SerializedName("StartKm")
    val startKm: Double,
    @SerializedName("TrackRouteId")
    val trackRouteId: String,
    @SerializedName("UserId")
    val userId: Int,
    @SerializedName("VoId")
    val voId: String,
    @SerializedName("WorkCompleteDate")
    val workCompleteDate: String,
    @SerializedName("WorkStartDate")
    val workStartDate: String,


    val activityId: Int,

    val is_synced: String?,

    var ROWID: String?


) {
//    constructor(
//        StartDate = Date()
//                IssueDate = Date ())


//    val jobId : String? =

}