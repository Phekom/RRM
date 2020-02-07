package za.co.xisystems.itis_rrm.data.localDB.entities


import androidx.core.util.Pair
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fasterxml.jackson.annotation.JsonProperty
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
    val is_photoStart: Boolean
        get() = is_PhotoStart == isPhotoStart()

    fun isPhotoStart(): Boolean {
        return isPhotoStart()
    }


    fun setPhotoLatitude(photoLatitude: Double) {
        this.photoLatitude = photoLatitude
    }

    fun setPhotoLongitude(photoLongitude: Double) {
        this.photoLongitude = photoLongitude
    }


//    fun getJobItemEstimatePhotoStart(): JobItemEstimatesPhotoDTO {
//        return getJobItemEstimatePhoto(true).second!!
//    }
//    fun getJobItemEstimatePhotoEnd(): JobItemEstimatesPhotoDTO {
//        return getJobItemEstimatePhoto(false).second!!
//    }




}