/*
 * Updated by Shaun McDonald on 2021/01/25
 * Last modified on 2021/01/25 6:30 PM
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 */

package za.co.xisystems.itis_rrm.data.localDB.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.ArrayList

const val JOB_ITEM_MEASURE = "JOB_ITEM_MEASURE"

@Entity(tableName = JOB_ITEM_MEASURE)
data class JobItemMeasureDTO(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @SerializedName("ActId")
    var actId: Int,
    @SerializedName("ApprovalDate")
    var approvalDate: String? = null,
    @SerializedName("Cpa")
    var cpa: Int = 0,
    @SerializedName("EndKm")
    var endKm: Double,
    @SerializedName("EstimateId")
    var estimateId: String?,
    @SerializedName("ItemMeasureId")
    var itemMeasureId: String,
    @SerializedName("JimNo")
    var jimNo: String?,
    @SerializedName("JobDirectionId")
    var jobDirectionId: Int,
    @SerializedName("JobId")
    var jobId: String?,
    @SerializedName("LineAmount")
    var lineAmount: Double,
    @SerializedName("LineRate")
    var lineRate: Double,
    @SerializedName("MeasureDate")
    var measureDate: String? = null,
    @SerializedName("MeasureGroupId")
    var measureGroupId: String?,
    @SerializedName("PrjItemMeasurePhotoDtos")
    var jobItemMeasurePhotos: ArrayList<JobItemMeasurePhotoDTO> = ArrayList(),
    @SerializedName("ProjectItemId")
    var projectItemId: String?,
    @SerializedName("ProjectVoId")
    var projectVoId: String?,
    @SerializedName("Qty")
    var qty: Double,
    @SerializedName("RecordSynchStateId")
    var recordSynchStateId: Int,
    @SerializedName("RecordVersion")
    var recordVersion: Int,
    @SerializedName("StartKm")
    var startKm: Double,
    @SerializedName("TrackRouteId")
    var trackRouteId: String?,
    @SerializedName("Deleted")
    var deleted: Int = 0,
    var entityDescription: String?,
    var selectedItemUom: String?
) : Serializable
