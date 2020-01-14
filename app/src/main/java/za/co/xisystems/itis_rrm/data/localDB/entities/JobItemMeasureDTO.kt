package za.co.xisystems.itis_rrm.data.localDB.entities


import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.util.*

const val JOB_ITEM_MEASURE = "JOB_ITEM_MEASURE"

@Entity(tableName = JOB_ITEM_MEASURE)
data class JobItemMeasureDTO(
    @SerializedName("ActId")
    @PrimaryKey
    val actId: Int,
    @SerializedName("ApprovalDate")
    val approvalDate: String? = Date().toString(),
    @SerializedName("Cpa")
    val cpa: Int = 0,
    @SerializedName("EndKm")
    val endKm: Double,
    @SerializedName("EstimateId")
    var estimateId: String?,
    @SerializedName("ItemMeasureId")
    var itemMeasureId: String?,
    @SerializedName("JimNo")
    var jimNo: String?,
    @SerializedName("JobDirectionId")
    val jobDirectionId: Int,
    @SerializedName("JobId")
    var jobId: String?,
    @SerializedName("LineAmount")
    val lineAmount: Double,
    @SerializedName("LineRate")
    val lineRate: Double,
    @SerializedName("MeasureDate")
    val measureDate: String? = Date().toString(),
    @SerializedName("MeasureGroupId")
    var measureGroupId: String?,
    @SerializedName("PrjItemMeasurePhotoDtos")
    val jobItemMeasurePhotos: ArrayList<JobItemMeasurePhotoDTO>,
    @SerializedName("PrjJobDto")
    val job: JobDTO,
    @SerializedName("PrjJobItemEstimateDto")
//    val prjJobItemEstimateDto: ArrayList<JobItemEstimateDTO>,
    val jobItemEstimate: JobItemEstimateDTO,
    @SerializedName("ProjectItemId")
    var projectItemId: String?,
    @SerializedName("ProjectVoId")
    var projectVoId: String?,
    @SerializedName("Qty")
    val qty: Double,
    @SerializedName("RecordSynchStateId")
    val recordSynchStateId: Int,
    @SerializedName("RecordVersion")
    val recordVersion: Int,
    @SerializedName("StartKm")
    val startKm: Double,
    @SerializedName("TrackRouteId")
    var trackRouteId: String?,


    var entityDescription: String?,

    var selectedItemUom: String?
)