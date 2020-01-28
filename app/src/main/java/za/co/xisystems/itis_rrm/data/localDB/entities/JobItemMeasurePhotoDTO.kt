package za.co.xisystems.itis_rrm.data.localDB.entities


import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable

const val JOB_ITEM_MEASURE_PHOTO = "JOB_ITEM_MEASURE_PHOTO"

@Entity(tableName = JOB_ITEM_MEASURE_PHOTO)
data class JobItemMeasurePhotoDTO(
    @SerializedName("Descr")
    val descr: String?,
    @SerializedName("Filename")
    val filename: String?,
    @SerializedName("ItemMeasureId")
    var itemMeasureId: String?,
    @SerializedName("PhotoDate")
    val photoDate: String?,
    @SerializedName("PhotoId")
    @PrimaryKey
    var photoId: String,
    @SerializedName("PhotoLatitude")
    val photoLatitude: Double,
    @SerializedName("PhotoLongitude")
    val photoLongitude: Double,
    @SerializedName("PhotoPath")
    var photoPath: String?,
    @SerializedName("PrjJobItemMeasureDto")
    val jobItemMeasure: JobItemMeasureDTO,
    @SerializedName("RecordSynchStateId")
    val recordSynchStateId: Int,
    @SerializedName("RecordVersion")
    val recordVersion: Int
): Serializable