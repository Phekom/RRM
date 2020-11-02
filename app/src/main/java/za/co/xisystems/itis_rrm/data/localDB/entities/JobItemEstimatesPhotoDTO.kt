package za.co.xisystems.itis_rrm.data.localDB.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable

const val JOB_ITEM_ESTIMATE_PHOTO = "JOB_ITEM_ESTIMATE_PHOTO"

@Entity(tableName = JOB_ITEM_ESTIMATE_PHOTO)
data class JobItemEstimatesPhotoDTO(
    @SerializedName("Descr")
    var descr: String,
    @SerializedName("EstimateId")
    var estimateId: String,
    @SerializedName("Filename")
    var filename: String,
    @SerializedName("PhotoDate")
    var photoDate: String,
    @SerializedName("PhotoId")
    @PrimaryKey
    var photoId: String,
    @SerializedName("PhotoStart")
    val photoStart: String?,
    @SerializedName("PhotoEnd")
    val photoEnd: String?,
    @SerializedName("Startkm")
    val startKm: Double = 0.0,
    @SerializedName("Endkm")
    val endKm: Double = 0.0,
    @SerializedName("PhotoLatitude")
    var photoLatitude: Double? = 0.0,
    @SerializedName("PhotoLongitude")
    var photoLongitude: Double? = 0.0,

    @SerializedName("PhotoLatitudeEnd")
    var photoLatitudeEnd: Double = 0.0,
    @SerializedName("PhotoLongitudeEnd")
    var photoLongitudeEnd: Double = 0.0,

    @SerializedName("PhotoPath")
    var photoPath: String,
    @SerializedName("PrjJobItemEstimateDto")
    var jobItemEstimate: JobItemEstimateDTO?,
    @SerializedName("RecordSynchStateId")
    val recordSynchStateId: Int,
    @SerializedName("RecordVersion")
    val recordVersion: Int,
    @SerializedName("IsPhotoStart")
    var is_PhotoStart: Boolean,
    @SerializedName("Photo")
    val image: ByteArray?

//    val jobItemEstimate: ArrayList<JobItemEstimateDTO>,
) : Serializable {

    fun isPhotoStart(): Boolean {
        return is_PhotoStart
    }

    fun setPhotoLatitude(photoLatitude: Double) {
        this.photoLatitude = photoLatitude
    }

    fun setPhotoLongitude(photoLongitude: Double) {
        this.photoLongitude = photoLongitude
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as JobItemEstimatesPhotoDTO

        if (descr != other.descr) return false
        if (estimateId != other.estimateId) return false
        if (filename != other.filename) return false
        if (photoDate != other.photoDate) return false
        if (photoId != other.photoId) return false
        if (photoStart != other.photoStart) return false
        if (photoEnd != other.photoEnd) return false
        if (photoPath != other.photoPath) return false
        if (image != null) {
            if (other.image == null) return false
            if (!image.contentEquals(other.image)) return false
        } else if (other.image != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = photoId.hashCode()
        result = 31 * result + photoPath.hashCode()
        result = 31 * result + (image?.contentHashCode() ?: 0)
        return result
    }
}
