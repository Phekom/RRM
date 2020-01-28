package za.co.xisystems.itis_rrm.data.localDB.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.*

/**
 * Created by Francis Mahlava on 2019/11/21.
 */

const val JOB_TABLE_TEMP = "JOB_TABLE_TEMP"
//val JOB_ID = UUID.randomUUID().toString()

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

    val SectionId : String?,
    @SerializedName("StartKm")
    val StartKm: Double,
    @SerializedName("EndKm")
    val EndKm: Double,
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
    var IssueDate: String? = Date().toString(),
    @SerializedName("StartDate")
    var StartDate: String? = Date().toString(),
    @SerializedName("DueDate")
    var DueDate:  String? = Date().toString(),
    @SerializedName("ApprovalDate")
    val ApprovalDate:String? = Date().toString(),

    @SerializedName("MobileJobItemEstimates")
    var JobItemEstimates: ArrayList<JobItemEstimateDTO>?,
    @SerializedName("MobileJobItemMeasures")
    var JobItemMeasures: ArrayList<JobItemMeasureDTO>?,
    @SerializedName("MobileJobSections")
    var JobSections: ArrayList<JobSectionDTO>?,
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
    val WorkStartDate: String?



) : Serializable{

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



    fun getJobEstimateIndexByItemId(itemId: String?): Int {
        if (itemId != null) for (i in JobItemEstimates!!.indices) {
            val currEstimate: JobItemEstimateDTO = JobItemEstimates?.get(i)!!
            if (currEstimate?.projectItemId != null) if (currEstimate.projectItemId.equals(
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
            JobItemEstimates?.removeAt(x)
        } else null
    }

    fun getJobEstimateByItemId(itemId: String?): JobItemEstimateDTO? {
        val x = getJobEstimateIndexByItemId(itemId)
        return if (x < 0) null else JobItemEstimates?.get(x)
    }

//    fun getJobItemMeasures(): ArrayList<JobItemMeasureDTO?>? {
//        return jobItemMeasures
//    }

//    fun setJobItemMeasures(jobItemMeasures: ArrayList<JobItemMeasureDTO?>) {
//        jobItemMeasures = jobItemMeasures
//    }

//    fun getSortString(): String? {
//        return sortString
//    }
//
//    fun setSortString(sortString: String?) {
//        this.sortString = sortString
//    }


    fun clearJobItemEstimates() {
        JobItemEstimates?.clear()
    }



}