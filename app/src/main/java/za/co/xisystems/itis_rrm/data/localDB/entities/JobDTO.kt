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
    val WorkStartDate: String?,

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

        JobItemEstimates?.forEachIndexed {index,estimate ->
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
        return if (JobItemEstimates.isNullOrEmpty() || x < 0 ) {
            null
        } else {
            JobItemEstimates!![x]
        }
    }
}
