/*
 * Updated by Shaun McDonald on 2021/22/20
 * Last modified on 2021/01/20 12:55 PM
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 */

package za.co.xisystems.itis_rrm.data.localDB.entities

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
    val actId: Int,
    @SerializedName("JobId")
    @PrimaryKey
    var jobId: String = SqlLitUtils.generateUuid(),
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
    var JobItemEstimates: ArrayList<JobItemEstimateDTO>? = arrayListOf(),
    @SerializedName("MobileJobItemMeasures")
    var JobItemMeasures: ArrayList<JobItemMeasureDTO>? = arrayListOf(),
    @SerializedName("MobileJobSections")
    var JobSections: ArrayList<JobSectionDTO>? = arrayListOf(),
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

) : Serializable {

    private fun getJobEstimateIndexByItemId(itemId: String?): Int {

        JobItemEstimates?.forEachIndexed { index, estimate ->
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
            JobItemEstimates?.removeAt(x)
        } else null
    }

    fun getJobEstimateByItemId(itemId: String?): JobItemEstimateDTO? {
        val x = getJobEstimateIndexByItemId(itemId)
        return if (JobItemEstimates.isNullOrEmpty() || x < 0) {
            null
        } else {
            JobItemEstimates!![x]
        }
    }
}
