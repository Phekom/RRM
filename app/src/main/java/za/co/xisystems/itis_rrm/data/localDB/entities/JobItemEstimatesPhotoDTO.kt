package za.co.xisystems.itis_rrm.data.localDB.entities


import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

const val JOB_ITEM_ESTIMATE_PHOTO = "JOB_ITEM_ESTIMATE_PHOTO"

@Entity(tableName = JOB_ITEM_ESTIMATE_PHOTO)
data class JobItemEstimatesPhotoDTO(
    @SerializedName("Descr")
    val descr: String,
    @SerializedName("EstimateId")
    var estimateId: String,
    @SerializedName("Filename")
    val filename: String,
    @SerializedName("PhotoDate")
    val photoDate: String,
    @SerializedName("PhotoId")
    @PrimaryKey
    var photoId: String,

    @SerializedName("PhotoLatitude")
    val photoLatitude: Double,
    @SerializedName("PhotoLongitude")
    val photoLongitude: Double,
    @SerializedName("PhotoPath")
    var photoPath: String,
    @SerializedName("PrjJobItemEstimateDto")
    val jobItemEstimate: JobItemEstimateDTO,
    @SerializedName("RecordSynchStateId")
    val recordSynchStateId: Int,
    @SerializedName("RecordVersion")
    val recordVersion: Int,
    @SerializedName("IsPhotoStart")
    var is_PhotoStart: Boolean,
    @SerializedName("Photo")
    val image: ByteArray?

//    val jobItemEstimate: ArrayList<JobItemEstimateDTO>,
) {
    //    @SerializedName("IsPhotoStart")
    val is_photoStart: Boolean
        get() = is_PhotoStart == isPhotoStart()

    fun isPhotoStart(): Boolean {
        return isPhotoStart()
    }

}