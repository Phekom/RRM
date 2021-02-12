/*
 * Updated by Shaun McDonald on 2021/02/08
 * Last modified on 2021/02/07 12:13 AM
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 */

package za.co.xisystems.itis_rrm.data.localDB.entities

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
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
) : Serializable, Parcelable {
    constructor(parcel: Parcel) : this(
        id = parcel.readInt(),
        actId = parcel.readInt(),
        approvalDate = parcel.readString(),
        cpa = parcel.readInt(),
        endKm = parcel.readDouble(),
        estimateId = parcel.readString(),
        itemMeasureId = parcel.readString()!!,
        jimNo = parcel.readString(),
        jobDirectionId = parcel.readInt(),
        jobId = parcel.readString(),
        lineAmount = parcel.readDouble(),
        lineRate = parcel.readDouble(),
        measureDate = parcel.readString(),
        measureGroupId = parcel.readString(),
        jobItemMeasurePhotos = arrayListOf<JobItemMeasurePhotoDTO>().apply {
            parcel.readList(this.toList(), JobItemMeasurePhotoDTO::class.java.classLoader)
        },
        projectItemId = parcel.readString(),
        projectVoId = parcel.readString(),
        qty = parcel.readDouble(),
        recordSynchStateId = parcel.readInt(),
        recordVersion = parcel.readInt(),
        startKm = parcel.readDouble(),
        trackRouteId = parcel.readString(),
        deleted = parcel.readInt(),
        entityDescription = parcel.readString(),
        selectedItemUom = parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeInt(actId)
        parcel.writeString(approvalDate)
        parcel.writeInt(cpa)
        parcel.writeDouble(endKm)
        parcel.writeString(estimateId)
        parcel.writeString(itemMeasureId)
        parcel.writeString(jimNo)
        parcel.writeInt(jobDirectionId)
        parcel.writeString(jobId)
        parcel.writeDouble(lineAmount)
        parcel.writeDouble(lineRate)
        parcel.writeString(measureDate)
        parcel.writeString(measureGroupId)
        parcel.writeString(projectItemId)
        parcel.writeString(projectVoId)
        parcel.writeDouble(qty)
        parcel.writeInt(recordSynchStateId)
        parcel.writeInt(recordVersion)
        parcel.writeDouble(startKm)
        parcel.writeString(trackRouteId)
        parcel.writeInt(deleted)
        parcel.writeString(entityDescription)
        parcel.writeString(selectedItemUom)
        parcel.writeList(jobItemMeasurePhotos.toList())
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Creator<JobItemMeasureDTO> {
        override fun createFromParcel(parcel: Parcel): JobItemMeasureDTO {
            return JobItemMeasureDTO(parcel)
        }

        override fun newArray(size: Int): Array<JobItemMeasureDTO?> {
            return arrayOfNulls(size)
        }
    }
}
