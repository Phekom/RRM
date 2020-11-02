package za.co.xisystems.itis_rrm.data.localDB.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Created by Francis Mahlava on 2019/11/26.
 */

const val JOB_ESTIMATE_WORKS_PHOTO = "JOB_ESTIMATE_WORKS_PHOTO"

@Entity(tableName = JOB_ESTIMATE_WORKS_PHOTO)
data class JobEstimateWorksPhotoDTO(
    @PrimaryKey(autoGenerate = true)
    var Id: Int,
    @SerializedName("Descr")
    val descr: String,
    @SerializedName("Filename")
    val filename: String,
    @SerializedName("PhotoActivityId")
    val photoActivityId: Int,
    @SerializedName("PhotoDate")
    val photoDate: String,
    @SerializedName("PhotoId")
    var photoId: String,
    @SerializedName("PhotoLatitude")
    val photoLatitude: Double,
    @SerializedName("PhotoLongitude")
    val photoLongitude: Double,
    @SerializedName("PhotoPath")
    val photoPath: String,
    @SerializedName("PrjEstimateWorksDto")
    val estimateWorks: ArrayList<JobEstimateWorksDTO>,
    @SerializedName("RecordSynchStateId")
    val recordSynchStateId: Int,
    @SerializedName("RecordVersion")
    val recordVersion: Int,
    @SerializedName("WorksId")
    var worksId: String
) : Serializable
