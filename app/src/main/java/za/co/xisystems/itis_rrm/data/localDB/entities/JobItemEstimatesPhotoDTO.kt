package za.co.xisystems.itis_rrm.data.localDB.entities


import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable

const val JOB_ITEM_ESTIMATE_PHOTO = "JOB_ITEM_ESTIMATE_PHOTO"

@Entity(tableName = JOB_ITEM_ESTIMATE_PHOTO)
data class JobItemEstimatesPhotoDTO(
    @SerializedName("Descr")
    val descr: String,
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
    val startKm : Double = 0.0,
    @SerializedName("Endkm")
    val endKm : Double = 0.0,
    @SerializedName("PhotoLatitude")
    var photoLatitude : Double? = 0.0,
    @SerializedName("PhotoLongitude")
    var photoLongitude:  Double? = 0.0,

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
    //    @SerializedName("IsPhotoStart")
//    val is_photoStart: Boolean
//        get() = is_PhotoStart == isPhotoStart()

    fun isPhotoStart(): Boolean {
        return isPhotoStart()
    }


//    fun setPhotoLatitude(photoLatitude: Double) {
//        this.photoLatitude = photoLatitude
//    }
//
//    fun setPhotoLongitude(photoLongitude: Double) {
//        this.photoLongitude = photoLongitude
//    }

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
        if (startKm != other.startKm) return false
        if (endKm != other.endKm) return false
        if (photoLatitude != other.photoLatitude) return false
        if (photoLongitude != other.photoLongitude) return false
        if (photoLatitudeEnd != other.photoLatitudeEnd) return false
        if (photoLongitudeEnd != other.photoLongitudeEnd) return false
        if (photoPath != other.photoPath) return false
        if (jobItemEstimate != other.jobItemEstimate) return false
        if (recordSynchStateId != other.recordSynchStateId) return false
        if (recordVersion != other.recordVersion) return false
        if (is_PhotoStart != other.is_PhotoStart) return false
        if (image != null) {
            if (other.image == null) return false
            if (!image.contentEquals(other.image)) return false
        } else if (other.image != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = descr.hashCode()
        result = 31 * result + estimateId.hashCode()
        result = 31 * result + filename.hashCode()
        result = 31 * result + photoDate.hashCode()
        result = 31 * result + photoId.hashCode()
        result = 31 * result + (photoStart?.hashCode() ?: 0)
        result = 31 * result + (photoEnd?.hashCode() ?: 0)
        result = 31 * result + startKm.hashCode()
        result = 31 * result + endKm.hashCode()
        result = 31 * result + (photoLatitude?.hashCode() ?: 0)
        result = 31 * result + (photoLongitude?.hashCode() ?: 0)
        result = 31 * result + photoLatitudeEnd.hashCode()
        result = 31 * result + photoLongitudeEnd.hashCode()
        result = 31 * result + photoPath.hashCode()
        result = 31 * result + (jobItemEstimate?.hashCode() ?: 0)
        result = 31 * result + recordSynchStateId
        result = 31 * result + recordVersion
        result = 31 * result + is_PhotoStart.hashCode()
        result = 31 * result + (image?.contentHashCode() ?: 0)
        return result
    }


//    fun getJobItemEstimatePhotoStart(): JobItemEstimatesPhotoDTO {
//        return getJobItemEstimatePhoto(true).second!!
//    }
//    fun getJobItemEstimatePhotoEnd(): JobItemEstimatesPhotoDTO {
//        return getJobItemEstimatePhoto(false).second!!
//    }




}