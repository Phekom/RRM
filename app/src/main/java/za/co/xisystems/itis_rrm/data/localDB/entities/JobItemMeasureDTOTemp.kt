package za.co.xisystems.itis_rrm.data.localDB.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.*

const val JOB_ITEM_MEASURE_TEMP = "JOB_ITEM_MEASURE_TEMP"

@Entity(tableName = JOB_ITEM_MEASURE_TEMP)
data class JobItemMeasureDTOTemp(
    @PrimaryKey(autoGenerate = true)
    val ID: Int,
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
    var itemMeasureId: String?,
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
    var jobItemMeasurePhotos: ArrayList<JobItemMeasurePhotoDTOTemp>,
    @SerializedName("PrjJobDto")
    var job: JobDTO?,
    @SerializedName("PrjJobItemEstimateDto")
//    val prjJobItemEstimateDto: ArrayList<JobItemEstimateDTO>,
    var jobItemEstimate: JobItemEstimateDTO,
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


    var entityDescription: String?,

    var selectedItemUom: String?

) : Serializable