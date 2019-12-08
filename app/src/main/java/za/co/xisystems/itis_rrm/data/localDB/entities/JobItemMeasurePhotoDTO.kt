package za.co.xisystems.itis_rrm.data.localDB.entities


import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity
data class JobItemMeasurePhotoDTO(
    @SerializedName("Descr")
    val descr: String,
    @SerializedName("Filename")
    val filename: String,
    @SerializedName("ItemMeasureId")
    val itemMeasureId: String,
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
    @SerializedName("PrjJobItemMeasureDto")
    val jobItemMeasureDto: ArrayList<JobItemMeasureDTO>,
    @SerializedName("RecordSynchStateId")
    val recordSynchStateId: Int,
    @SerializedName("RecordVersion")
    val recordVersion: Int
)