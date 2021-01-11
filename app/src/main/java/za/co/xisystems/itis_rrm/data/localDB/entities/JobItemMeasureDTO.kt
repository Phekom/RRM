package za.co.xisystems.itis_rrm.data.localDB.entities

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import za.co.xisystems.itis_rrm.utils.SqlLitUtils
import java.io.Serializable
import java.util.ArrayList
import java.util.Date

const val JOB_ITEM_MEASURE = "JOB_ITEM_MEASURE"

@Entity(tableName = JOB_ITEM_MEASURE)
data class JobItemMeasureDTO(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @SerializedName("ActId")
    var actId: Int,
    @SerializedName("ApprovalDate")
    var approvalDate: String? = Date().toString(),
    @SerializedName("Cpa")
    var cpa: Int = 0,
    @SerializedName("EndKm")
    var endKm: Double,
    @SerializedName("EstimateId")
    var estimateId: String?,
    @SerializedName("ItemMeasureId")
    var itemMeasureId: String = SqlLitUtils.generateUuid(),
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
    var measureDate: String? = Date().toString(),
    @SerializedName("MeasureGroupId")
    var measureGroupId: String?,
    @SerializedName("PrjItemMeasurePhotoDtos")
    var jobItemMeasurePhotos: ArrayList<JobItemMeasurePhotoDTO>,
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

    var selectedItemUom: String?,
    val job: JobDTO?,
    val jobItemEstimate: JobItemEstimateDTO?

) : Serializable, Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString(),
        parcel.readInt(),
        parcel.readDouble(),
        parcel.readString(),
        parcel.readString()!!,
        parcel.readString(),
        parcel.readInt(),
        parcel.readString(),
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readString(),
        parcel.readString(),
        TODO("jobItemMeasurePhotos"),
        parcel.readString(),
        parcel.readString(),
        parcel.readDouble(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readDouble(),
        parcel.readString(),
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readParcelable(JobDTO::class.java.classLoader),
        parcel.readParcelable(JobItemEstimateDTO::class.java.classLoader)
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
        parcel.writeParcelable(job, flags)
        parcel.writeParcelable(jobItemEstimate, flags)
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
