/*
 * Updated by Shaun McDonald on 2021/22/20
 * Last modified on 2021/01/20 12:46 PM
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 */

package za.co.xisystems.itis_rrm.data.localDB.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import org.jetbrains.annotations.NotNull
import za.co.xisystems.itis_rrm.utils.SqlLitUtils
import java.io.Serializable
import java.util.ArrayList

/**
 * Created by Francis Mahlava on 2019/11/21.
 */

const val JOB_TABLE_TEMP = "JOB_TABLE_TEMP"

@Entity(tableName = JOB_TABLE_TEMP)
data class JobDTOTemp(
    @SerializedName("ActId")
    var ActId: Int,
    @SerializedName("JobId")
    @PrimaryKey
    @NotNull
    var JobId: String = SqlLitUtils.generateUuid(),

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
    var IssueDate: String? = null,
    @SerializedName("StartDate")
    var StartDate: String? = null,
    @SerializedName("DueDate")
    var DueDate: String? = null,
    @SerializedName("ApprovalDate")
    val ApprovalDate: String? = null,
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
    val WorkStartDate: String? = null

) : Serializable
