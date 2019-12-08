package za.co.xisystems.itis_rrm.data.localDB.entities


import androidx.core.util.Pair
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import za.co.xisystems.itis_rrm.utils.JobUtils
import java.util.*


/**
 * Created by Francis Mahlava on 2019/11/21.
 */

//const val PROJECT_ITEM_TABLE = "PROJECT_ITEM_TABLE"
//
//
//@ToDoListEntityDTO(tableName = PROJECT_ITEM_TABLE)
@Entity
data class JobItemEstimateDTO(

    @SerializedName("ActId")
    val actId: Int,
    @SerializedName("EstimateId")
    @PrimaryKey
    val estimateId: String,
    @SerializedName("JobId")
    val jobId: String,
    @SerializedName("LineRate")
    val lineRate: Double,
    @SerializedName("MobileEstimateWorks")
    val mobileEstimateWorks: ArrayList<JobEstimateWorksDTO>,
    @SerializedName("MobileJobItemEstimatesPhotos")
    val jobItemEstimatesPhotos: ArrayList<JobItemEstimatesPhotoDTO>,
    @SerializedName("MobileJobItemMeasures")
    val mobileJobItemMeasures: ArrayList<JobItemMeasureDTO>,
    @SerializedName("PrjJobDto")
    val prjJobDto: ArrayList<JobDTO>,
    @SerializedName("ProjectItemId")
    val projectItemId: String,
    @SerializedName("ProjectVoId")
    val projectVoId: String,
    @SerializedName("Qty")
    val qty: Double,
    @SerializedName("RecordSynchStateId")
    val recordSynchStateId: Int,
    @SerializedName("RecordVersion")
    val recordVersion: Int,
    @SerializedName("TrackRouteId")
    val trackRouteId: String

){
    fun getJobItemEstimatePhoto(lookForStartPhoto: Boolean): Pair<Int, JobItemEstimatesPhotoDTO> {
        val photos = jobItemEstimatesPhotos
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

    fun getPhoto(x: Int): JobItemEstimatesPhotoDTO? {
        return if ( jobItemEstimatesPhotos != null && -1 < x && x < size()) {
            jobItemEstimatesPhotos.get(x)
        } else null
    }

    fun setJobItemEstimatePhotoStart(photoStart: JobItemEstimatesPhotoDTO) {
        photoStart.estimateId
        setJobItemEstimatePhoto(photoStart)
    }

    fun getJobItemEstimatePhotoEnd(): JobItemEstimatesPhotoDTO? {
        return getJobItemEstimatePhoto(false).second
    }

    fun setJobItemEstimatePhotoEnd(photoEnd: JobItemEstimatesPhotoDTO) {
        photoEnd.estimateId
        setJobItemEstimatePhoto(photoEnd)
    }

    private fun setJobItemEstimatePhoto(photo: JobItemEstimatesPhotoDTO) {
        if (jobItemEstimatesPhotos == null) {
            ArrayList<JobItemEstimatesPhotoDTO>()
            jobItemEstimatesPhotos.add(photo)
        } else {
            val photoToChange = getJobItemEstimatePhoto(photo.isPhotoStart())
            val index = photoToChange.first!!
            if (index == -1)
                jobItemEstimatesPhotos.add(photo)
            else
                jobItemEstimatesPhotos.set(index, photo)
        }
        JobUtils.sort(jobItemEstimatesPhotos)
    }

    fun isEstimateComplete(): Boolean {
        if (size() < 2)
            return false
        else {
            val photoStart = jobItemEstimatesPhotos.get(0)
            val photoEnd = jobItemEstimatesPhotos.get(1)
            return if (photoStart == null || photoStart!!.filename == null
                || photoEnd == null || photoEnd!!.filename == null
            )
                false
            else
                true
        }
    }

    fun size(): Int {
        return if (jobItemEstimatesPhotos == null) 0 else jobItemEstimatesPhotos.size
    }
}