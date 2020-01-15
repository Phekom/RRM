package za.co.xisystems.itis_rrm.data.localDB.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.util.*

/**
 * Created by Francis Mahlava on 2019/11/26.
 */


const val JOB_ESTIMATE_WORKS = "JOB_ESTIMATE_WORKS"

@Entity(tableName = JOB_ESTIMATE_WORKS)
class JobEstimateWorksDTO(
    @SerializedName("ActId")
    val actId: Int,
    @SerializedName("EstimateId")
    var estimateId: String?,
    @SerializedName("PrjEstWorksPhotoDtos")
    val jobEstimateWorksPhotos: ArrayList<JobEstimateWorksPhotoDTO>?,
    @SerializedName("PrjJobItemEstimateDto")
    val jobItemEstimate: JobItemEstimateDTO,
    @SerializedName("RecordSynchStateId")
    val recordSynchStateId: Int,
    @SerializedName("RecordVersion")
    val recordVersion: Int,
    @SerializedName("TrackRouteId")
    var trackRouteId: String,
    @SerializedName("WorksId")
    @PrimaryKey
    var worksId: String
)
