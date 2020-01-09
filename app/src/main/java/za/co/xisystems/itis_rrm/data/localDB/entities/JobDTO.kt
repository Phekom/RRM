package za.co.xisystems.itis_rrm.data.localDB.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable
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
    var JobId: String = UUID.randomUUID().toString(),
    @SerializedName("ProjectId")
    var ProjectId: String?,
    @SerializedName("JiNo")
    val JiNo: String?,
    @SerializedName("UserId")
    val UserId: Int,
    @SerializedName("ActId")
    val ActId: Int,
    @SerializedName("Descr")
    var Descr: String?,
    @SerializedName("IssueDate")
    var IssueDate: String? = Date().toString(),
    @SerializedName("StartDate")
    var StartDate: String? = Date().toString(),
    @SerializedName("DueDate")
    var DueDate:  String? = Date().toString(),
    @SerializedName("TrackRouteId")
    var TrackRouteId: String?,
    @SerializedName("StartKm")
    val StartKm: Double,
    @SerializedName("EndKm")
    val EndKm: Double,
    @SerializedName("ContractVoId")
    var ContractVoId: String?,

    @SerializedName("ApprovalDate")
    val ApprovalDate:String? = Date().toString(),
    @SerializedName("ContractorId")
    val ContractorId: Int,
    @SerializedName("M9100")
    val M9100: Int,
    var ESTIMATES_ACT_ID: Int?,

    var MEASURE_ACT_ID: Int?,

    var WORKS_ACT_ID: Int?,
    @SerializedName("MobileJobItemEstimates")
    var JobItemEstimates: ArrayList<JobItemEstimateDTO>?,
    @SerializedName("MobileJobItemMeasures")
    var JobItemMeasures: ArrayList<JobItemMeasureDTO>?,
    @SerializedName("MobileJobSections")
    val JobSections: ArrayList<JobSectionDTO>?,
    @SerializedName("PerfitemGroupId")
    val PerfitemGroupId: String?,
    @SerializedName("RecordVersion")
    val RecordVersion: Int,
    @SerializedName("Remarks")
    val Remarks: String?,
    @SerializedName("Route")
    var Route: String?,
    @SerializedName("RrmJiNo")
    val RrmJiNo: String?,
    @SerializedName("Section")
    val Section: String?,

    @SerializedName("Cpa")
    val Cpa: Int,
    @SerializedName("DayWork")
    val DayWork: Int,

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
    val ProjectVoId: String?,
    @SerializedName("QtyUpdateAllowed")
    val QtyUpdateAllowed: Int,
    @SerializedName("RecordSynchStateId")
    val RecordSynchStateId: Int,

    @SerializedName("VoId")
    val VoId: String?,
    @SerializedName("WorkCompleteDate")
    val WorkCompleteDate: String?,
    @SerializedName("WorkStartDate")
    val WorkStartDate: String?,

    val sortString: String?,

    val ActivityId: Int,

    val Is_synced: String?

//    var ESTIMATES_ACT_ID: Int?



) : Serializable{




}