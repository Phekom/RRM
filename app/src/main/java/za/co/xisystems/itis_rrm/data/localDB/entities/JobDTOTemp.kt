package za.co.xisystems.itis_rrm.data.localDB.entities

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

) : Serializable {

    fun addOrUpdateJobItemEstimate(newEstimate: JobItemEstimateDTO) {
        val x = getJobEstimateIndexByItemId(newEstimate.projectItemId)
        if (x < 0) JobItemEstimates?.add(newEstimate) else JobItemEstimates?.set(
            x,
            newEstimate
        )
    }

    fun jobEstimateExist(itemId: String?): Boolean {
        return getJobEstimateIndexByItemId(itemId) > -1
    }

    private fun getJobEstimateIndexByItemId(itemId: String?): Int {
        if (itemId != null) for (i in JobItemEstimates!!.indices) {
            val currEstimate: JobItemEstimateDTO = JobItemEstimates?.get(i)!!
            if (currEstimate.projectItemId != null &&
                currEstimate.projectItemId.equals(itemId)
            ) {
                return i
            }
        }
        return -1
    }

    fun getJobEstimateByItemId(itemId: String?): JobItemEstimateDTO? {
        val x = getJobEstimateIndexByItemId(itemId)
        return if (x < 0) null else JobItemEstimates?.get(x)
    }

}
