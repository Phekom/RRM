package za.co.xisystems.itis_rrm.data.localDB.entities


import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
@Entity
data class JobEstimateWorksPhotoDTO(
    @SerializedName("Descr")
    val descr: String,
    @SerializedName("Filename")
    val filename: String,
    @SerializedName("PhotoActivityId")
    val photoActivityId: Int,
    @SerializedName("PhotoDate")
    val photoDate: String,
    @SerializedName("PhotoId")
    val photoId: String,
    @SerializedName("PhotoLatitude")
    val photoLatitude: Double,
    @SerializedName("PhotoLongitude")
    val photoLongitude: Double,
    @SerializedName("PhotoPath")
    val photoPath: String,
    @SerializedName("PrjEstimateWorksDto")
    val estimateWorks: List<JobEstimateWorksDTO>,
    @SerializedName("RecordSynchStateId")
    val recordSynchStateId: Int,
    @SerializedName("RecordVersion")
    val recordVersion: Int,
    @SerializedName("WorksId")
    @PrimaryKey
    val worksId: String
)