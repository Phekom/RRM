package za.co.xisystems.itis_rrm.data.localDB.entities


import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName


/**
 * Created by Francis Mahlava on 2019/11/21.
 */

//const val PROJECT_ITEM_TABLE = "PROJECT_ITEM_TABLE"
//
//
//@ToDoListEntityDTO(tableName = PROJECT_ITEM_TABLE)
@Entity
data class JobItemEstimateDTO(

    @SerializedName("ActId")
    val actId: Int,
    @SerializedName("EstimateId")
    @PrimaryKey
    val estimateId: String,
    @SerializedName("JobId")
    val jobId: String,
    @SerializedName("LineRate")
    val lineRate: Double,
    @SerializedName("MobileEstimateWorks")
    val mobileEstimateWorks: List<JobEstimateWorksDTO>,
    @SerializedName("MobileJobItemEstimatesPhotos")
    val jobItemEstimatesPhotos: List<JobItemEstimatesPhotoDTO>,
    @SerializedName("MobileJobItemMeasures")
    val mobileJobItemMeasures: List<JobItemMeasureDTO>,
    @SerializedName("PrjJobDto")
    val prjJobDto: List<JobDTO>,
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
    @SerializedName("TrackRouteId")
    val trackRouteId: String

)