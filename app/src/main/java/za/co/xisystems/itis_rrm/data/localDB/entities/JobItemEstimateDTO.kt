/*
 * Updated by Shaun McDonald on 2021/22/20
 * Last modified on 2021/01/20 12:46 PM
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 */

package za.co.xisystems.itis_rrm.data.localDB.entities

import androidx.core.util.Pair
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import za.co.xisystems.itis_rrm.utils.JobUtils
import za.co.xisystems.itis_rrm.utils.SqlLitUtils
import java.io.Serializable
import java.util.ArrayList

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
    var estimateId: String = SqlLitUtils.generateUuid(),
    @SerializedName("JobId")
    var jobId: String?,
    @SerializedName("LineRate")
    var lineRate: Double,
    @SerializedName("MobileEstimateWorks")
    var jobEstimateWorks: ArrayList<JobEstimateWorksDTO>? = arrayListOf(),
    @SerializedName("MobileJobItemEstimatesPhotos")
    var jobItemEstimatePhotos: ArrayList<JobItemEstimatesPhotoDTO>? = arrayListOf(),
    @SerializedName("MobileJobItemMeasures")
   val jobItemMeasure: ArrayList<JobItemMeasureDTO>? = arrayListOf(),
//    @SerializedName("PrjJobDto")
//    val job: JobDTO? = null,
    @SerializedName("ProjectItemId")
    var projectItemId: String?,
    @SerializedName("ProjectVoId")
    var projectVoId: String?,
    @SerializedName("Qty")
    var qty: Double,
    @SerializedName("RecordSynchStateId")
    val recordSynchStateId: Int,
    @SerializedName("RecordVersion")
    val recordVersion: Int,
    @SerializedName("TrackRouteId")
    var trackRouteId: String? = null,

    val jobItemEstimatePhotoStart: JobItemEstimatesPhotoDTO?,
    val jobItemEstimatePhotoEnd: JobItemEstimatesPhotoDTO?,

    var estimateComplete: String?,
//    var entityDescription: String?,

    var MEASURE_ACT_ID: Int = 0,

    val SelectedItemUOM: String?

) : Serializable {

    private fun getJobItemEstimatePhoto(lookForStartPhoto: Boolean): Pair<Int, JobItemEstimatesPhotoDTO> {
        val photos = jobItemEstimatePhotos
        var i = 0
        while (photos != null && i < photos.size) {
            val isPhotoStart = photos[i].isPhotoStart()
            if (lookForStartPhoto) {
                if (isPhotoStart) {
                    println("look: $lookForStartPhoto is:$isPhotoStart")
                    val pair = Pair(i, photos[i])
                    println("pair[" + pair.first + "]" + pair)
                    return pair
                }
            } else {
                if (!isPhotoStart) {
                    println("look: $lookForStartPhoto is:$isPhotoStart")
                    return Pair<Int, JobItemEstimatesPhotoDTO>(i, photos[i])
                }
            }
            i++
        }
        return Pair<Int, JobItemEstimatesPhotoDTO>(-1, null)
    }

    fun getPhoto(x: Int): JobItemEstimatesPhotoDTO? {
        return if (jobItemEstimatePhotos != null && -1 < x && x < size()) {
            jobItemEstimatePhotos!![x]
        } else null
    }

    fun setJobItemEstimatePhoto(photo: JobItemEstimatesPhotoDTO) {
        if (jobItemEstimatePhotos == null) {
            jobItemEstimatePhotos = ArrayList<JobItemEstimatesPhotoDTO>()
            jobItemEstimatePhotos?.add(photo)
        } else {
            val photoToChange = getJobItemEstimatePhoto(photo.isPhotoStart())
            val index = photoToChange.first!!
            if (index == -1) {
                jobItemEstimatePhotos!!.add(photo)
            } else {
                jobItemEstimatePhotos!![index] = photo
            }
        }
        JobUtils.sort(jobItemEstimatePhotos)
    }

    fun isEstimateComplete(): Boolean {
        return if (size() < 2) {
            false
        } else {
            val photoStart = jobItemEstimatePhotos?.get(0)
            val photoEnd = jobItemEstimatePhotos?.get(1)
            !(photoStart?.filename == null || photoEnd == null)
        }
    }

    fun size(): Int {
        return if (jobItemEstimatePhotos == null) 0 else jobItemEstimatePhotos!!.size
    }
}
