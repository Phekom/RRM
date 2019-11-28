package za.co.xisystems.itis_rrm.data.localDB.entities


import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity
data class JobItemEstimatesPhotoDTO(
    @SerializedName("Descr")
    val descr: String,
    @SerializedName("EstimateId")
    val estimateId: String,
    @SerializedName("Filename")
    val filename: String,
    @SerializedName("PhotoDate")
    val photoDate: String,
    @SerializedName("PhotoId")
    @PrimaryKey
    val photoId: String,

    @SerializedName("PhotoLatitude")
    val photoLatitude: Double,
    @SerializedName("PhotoLongitude")
    val photoLongitude: Double,
    @SerializedName("PhotoPath")
    val photoPath: String,
    @SerializedName("PrjJobItemEstimateDto")
    val jobItemEstimate: List<JobItemEstimateDTO>,
    @SerializedName("RecordSynchStateId")
    val recordSynchStateId: Int,
    @SerializedName("RecordVersion")
    val recordVersion: Int
)