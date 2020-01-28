package za.co.xisystems.itis_rrm.data.localDB.entities


import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable

const val JOB_ITEM_MEASURE_PHOTO_TEMP = "JOB_ITEM_MEASURE_PHOTO_TEMP"

@Entity(tableName = JOB_ITEM_MEASURE_PHOTO_TEMP)
data class JobItemMeasurePhotoDTOTemp(
    @PrimaryKey(autoGenerate = true)
    val ID: Int,
    @SerializedName("Descr")
    val descr: String?,
    @SerializedName("Filename")
    val filename: String?,
    @SerializedName("ItemMeasureId")
    var itemMeasureId: String?,
    @SerializedName("EstimateId")
    var estimateId: String?,
    @SerializedName("PhotoDate")
    val photoDate: String?,
    @SerializedName("PhotoId")
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
    val photoLatitude: Double,
    @SerializedName("PhotoLongitude")
    val photoLongitude: Double,
    @SerializedName("PhotoPath")
    var photoPath: String?,
    @SerializedName("PrjJobItemMeasureDto")
    val jobItemMeasureTemp: JobItemMeasureDTOTemp,
    @SerializedName("RecordSynchStateId")
    val recordSynchStateId: Int,
    @SerializedName("RecordVersion")
    val recordVersion: Int
): Serializable