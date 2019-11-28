package za.co.xisystems.itis_rrm.data.localDB.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * Created by Francis Mahlava on 2019/11/26.
 */


@Entity
class JobEstimateWorksDTO(
    @SerializedName("ActId")
    val actId: Int,
    @SerializedName("EstimateId")
    val estimateId: String,
    @SerializedName("PrjEstWorksPhotoDtos")
    val estWorksPhotos: List<JobEstimateWorksPhotoDTO>?,
    @SerializedName("PrjJobItemEstimateDto")
    val jobItemEstimate: List<JobEstimateWorksDTO>,
    @SerializedName("RecordSynchStateId")
    val recordSynchStateId: Int,
    @SerializedName("RecordVersion")
    val recordVersion: Int,
    @SerializedName("TrackRouteId")
    val trackRouteId: String,
    @SerializedName("WorksId")
    @PrimaryKey
    val worksId: String
)
