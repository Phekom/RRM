package za.co.xisystems.itis_rrm.data.localDB.entities


import androidx.core.util.Pair
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import za.co.xisystems.itis_rrm.utils.JobUtils
import java.io.Serializable
import java.util.*


/**
 * Created by Francis Mahlava on 2019/11/21.
 */

const val JOB_ITEM_ESTIMATE = "JOB_ITEM_ESTIMATE"

@Entity(tableName = JOB_ITEM_ESTIMATE)
data class JobItemEstimateDTO(

    @SerializedName("ActId")
    val actId: Int,
    @SerializedName("EstimateId")
    @PrimaryKey
    var estimateId: String,
    @SerializedName("JobId")
    var jobId: String?,
    @SerializedName("LineRate")
    val lineRate: Double,
    @SerializedName("MobileEstimateWorks")
    var jobEstimateWorks: ArrayList<JobEstimateWorksDTO>,
    @SerializedName("MobileJobItemEstimatesPhotos")
    var jobItemEstimatePhotos: ArrayList<JobItemEstimatesPhotoDTO>,
    @SerializedName("MobileJobItemMeasures")
    val jobItemMeasure: ArrayList<JobItemMeasureDTO>,
    @SerializedName("PrjJobDto")
    val job: JobDTO?,
    @SerializedName("ProjectItemId")
    var projectItemId: String?,
    @SerializedName("ProjectVoId")
    var projectVoId: String?,
    @SerializedName("Qty")
    val qty: Double,
    @SerializedName("RecordSynchStateId")
    val recordSynchStateId: Int,
    @SerializedName("RecordVersion")
    val recordVersion: Int,
    @SerializedName("TrackRouteId")
    var trackRouteId: String,

//   val jobItemEstimatePhotoStart : JobItemEstimatesPhotoDTO? = null,
//   val jobItemEstimatePhotoEnd : JobItemEstimatesPhotoDTO? = null

    val  estimateComplete : Int = 0,
//    var entityDescription: String?,
    val SelectedItemUOM: String?

): Serializable {

    fun getJobItemEstimatePhoto(lookForStartPhoto: Boolean): Pair<Int, JobItemEstimatesPhotoDTO> {
        val photos = jobItemEstimatePhotos
        var i = 0
        while (photos != null && i < photos!!.size) {
            val isPhotoStart = photos!!.get(i).isPhotoStart()
            if (lookForStartPhoto) {
                if (isPhotoStart) {
                    println("look: $lookForStartPhoto is:$isPhotoStart")
                    val pair = Pair<Int, JobItemEstimatesPhotoDTO>(i, photos!!.get(i))
                    println("pari[" + pair.first + "]" + pair)
                    return pair
                }
            } else {
                if (!isPhotoStart) {
                    println("look: $lookForStartPhoto is:$isPhotoStart")
                    return Pair<Int, JobItemEstimatesPhotoDTO>(i, photos!!.get(i))
                }
            }
            i++
        }
        return Pair<Int, JobItemEstimatesPhotoDTO>(-1, null)
    }

    fun getJobItemEstimatePhotoStart(): JobItemEstimatesPhotoDTO? {
        return getJobItemEstimatePhoto(true).second
    }
    fun getJobItemEstimatePhotoEnd(): JobItemEstimatesPhotoDTO? {
        return getJobItemEstimatePhoto(false).second
    }

    fun getPhoto(x: Int): JobItemEstimatesPhotoDTO? {
        return if ( jobItemEstimatePhotos != null && -1 < x && x < size()) {
            jobItemEstimatePhotos.get(x)
        } else null
    }

    fun setJobItemEstimatePhotoStart(photoStart: JobItemEstimatesPhotoDTO) {
        photoStart.estimateId
        setJobItemEstimatePhoto(photoStart)
    }


    fun setJobItemEstimatePhotoEnd(photoEnd: JobItemEstimatesPhotoDTO) {
        photoEnd.estimateId
        setJobItemEstimatePhoto(photoEnd)
    }

    private fun setJobItemEstimatePhoto(photo: JobItemEstimatesPhotoDTO) {
        if (jobItemEstimatePhotos == null) {
            ArrayList<JobItemEstimatesPhotoDTO>()
            jobItemEstimatePhotos.add(photo)
        } else {
            val photoToChange = getJobItemEstimatePhoto(photo.isPhotoStart())
            val index = photoToChange.first!!
            if (index == -1)
                jobItemEstimatePhotos.add(photo)
            else
                jobItemEstimatePhotos.set(index, photo)
        }
        JobUtils.sort(jobItemEstimatePhotos)
    }

    fun isEstimateComplete(): Boolean {
        if (size() < 2)
            return false
        else {
            val photoStart = jobItemEstimatePhotos.get(0)
            val photoEnd = jobItemEstimatePhotos.get(1)
            return !(photoStart!!.filename == null || photoEnd == null || photoEnd!!.filename == null)
        }
    }

    fun size(): Int {
        return if (jobItemEstimatePhotos == null) 0 else jobItemEstimatePhotos.size
    }
}