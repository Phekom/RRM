package za.co.xisystems.itis_rrm.data.localDB.entities


import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.util.*

@Entity
data class JobItemMeasureDTO(
    @SerializedName("ActId")
    @PrimaryKey
    val actId: Int,
    @SerializedName("ApprovalDate")
    val approvalDate: String = Date().toString(),
    @SerializedName("Cpa")
    val cpa: Int = 0,
    @SerializedName("EndKm")
    val endKm: Double,
    @SerializedName("EstimateId")
    val estimateId: String,
    @SerializedName("ItemMeasureId")
    val itemMeasureId: String,
    @SerializedName("JimNo")
    val jimNo: String,
    @SerializedName("JobDirectionId")
    val jobDirectionId: Int,
    @SerializedName("JobId")
    val jobId: String,
    @SerializedName("LineAmount")
    val lineAmount: Double,
    @SerializedName("LineRate")
    val lineRate: Double,
    @SerializedName("MeasureDate")
    val measureDate: String = Date().toString(),
    @SerializedName("MeasureGroupId")
    val measureGroupId: String,
    @SerializedName("PrjItemMeasurePhotoDtos")
    val prjItemMeasurePhotoDtos: ArrayList<JobItemMeasurePhotoDTO>,
    @SerializedName("PrjJobDto")
    val prjJobDto: ArrayList<JobDTO>,
    @SerializedName("PrjJobItemEstimateDto")
    val prjJobItemEstimateDto: ArrayList<JobItemEstimateDTO>,
    @SerializedName("ProjectItemId")
    val projectItemId: String,
    @SerializedName("ProjectVoId")
    val projectVoId: String,
    @SerializedName("Qty")
    val qty: Double,
    @SerializedName("RecordSynchStateId")
    val recordSynchStateId: Int,
    @SerializedName("RecordVersion")
    val recordVersion: Int,
    @SerializedName("StartKm")
    val startKm: Double,
    @SerializedName("TrackRouteId")
    val trackRouteId: String,


    var entityDescription: String?,

    var selectedItemUom: String?
)