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
    val jobId: String = UUID.randomUUID().toString(),

    @SerializedName("ActId")
    val actId: Int,
    @SerializedName("ApprovalDate")
    val approvalDate:String = Date().toString(),
    @SerializedName("ContractVoId")
    var contractVoId: String,
    @SerializedName("ContractorId")
    val contractorId: Int,
    @SerializedName("Cpa")
    val cpa: Int,
    @SerializedName("DayWork")
    val dayWork: Int,
    @SerializedName("Descr")
    var descr: String,
    @SerializedName("DueDate")
    var dueDate:  String = Date().toString(),
    @SerializedName("EndKm")
    val endKm: Double,
    @SerializedName("EngineerId")
    val engineerId: Int,
    @SerializedName("EntireRoute")
    val entireRoute: Int,
    @SerializedName("IsExtraWork")
    val isExtraWork: Int,
    @SerializedName("IssueDate")
    var issueDate: String = Date().toString(),
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
    var jobItemEstimates: ArrayList<JobItemEstimateDTO>?,
    @SerializedName("MobileJobItemMeasures")
    var jobItemMeasures: ArrayList<JobItemMeasureDTO>?,
    @SerializedName("MobileJobSections")
    val jobSections: ArrayList<JobSectionDTO>,
    @SerializedName("PerfitemGroupId")
    val perfitemGroupId: String,
    @SerializedName("ProjectId")
    var projectId: String,
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
    var startDate: String = Date().toString(),
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

    private var sortString: String? = null,

    val activityId: Int,

    val is_synced: String?,

    var ROWID: String?


) : Serializable {

    fun addOrUpdateJobItemEstimate(newEstimate: JobItemEstimateDTO) {
        val x = getJobEstimateIndexByItemId(newEstimate.projectItemId)
        if (x < 0) Companion.getJobItemEstimates(this)!!.add(newEstimate) else Companion.getJobItemEstimates(
            this)!!.set(
            x,
            newEstimate
        )
    }


    fun jobEstimateExist(itemId: String?): Boolean {
        return getJobEstimateIndexByItemId(itemId) > -1
    }

    fun getJobEstimateIndexByItemId(itemId: String?): Int {
        if (itemId != null) for (i in Companion.getJobItemEstimates(this)!!.indices) {
            val currEstimate: JobItemEstimateDTO = Companion.getJobItemEstimates(this)!![i]
            if (currEstimate != null && currEstimate.projectItemId != null) if (currEstimate.projectItemId == itemId
            ) {
                return i
            }
        }
        return -1
    }

    fun removeJobEstimateByItemId(itemId: String?): JobItemEstimateDTO? {
        val x = getJobEstimateIndexByItemId(itemId)
        return if (x > -1) {
            Companion.getJobItemEstimates(this)!!.removeAt(x)
        } else null
    }

    fun getJobEstimateByItemId(itemId: String?): JobItemEstimateDTO? {
        val x = getJobEstimateIndexByItemId(itemId)
        return if (x < 0) null else Companion.getJobItemEstimates(this)!!.get(x)
    }


    fun getSortString(): String? {
        return sortString
    }

    fun setSortString(sortString: String) {
        this.sortString = sortString
    }


    fun clearJobItemEstimates() {
        Companion.getJobItemEstimates(this)!!.clear()
    }

    companion object {
        private fun getJobItemEstimates(jobDTO: JobDTO): ArrayList<JobItemEstimateDTO>? {
            return jobDTO.jobItemEstimates
                ?: ArrayList<JobItemEstimateDTO>().also {
                    jobDTO.jobItemEstimates = it
                }
        }
    }

}