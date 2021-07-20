/*
 * Updated by Shaun McDonald on 2021/01/25
 * Last modified on 2021/01/25 6:30 PM
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 */

package za.co.xisystems.itis_rrm.data.localDB.entities

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import androidx.core.util.Pair
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.ArrayList
import za.co.xisystems.itis_rrm.utils.JobUtils

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
    var lineRate: Double,
    @SerializedName("MobileEstimateWorks")
    var jobEstimateWorks: ArrayList<JobEstimateWorksDTO> = ArrayList(),
    @SerializedName("MobileJobItemEstimatesPhotos")
    var jobItemEstimatePhotos: ArrayList<JobItemEstimatesPhotoDTO> = ArrayList(),
    @SerializedName("MobileJobItemMeasures")
    var jobItemMeasure: ArrayList<JobItemMeasureDTO> = ArrayList(),
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

    @SerializedName("MEASURE_ACT_ID")
    var measureActId: Int = 0,

    @SerializedName("SelectedItemUOM")
    val selectedItemUom: String?,
    var startKmMarker: String? = null,
    var endKmMarker: String? = null
) : Parcelable, Serializable {

    constructor(parcel: Parcel) : this(
        actId = parcel.readInt(),
        estimateId = parcel.readString()!!,
        jobId = parcel.readString(),
        lineRate = parcel.readDouble(),
        jobEstimateWorks = arrayListOf<JobEstimateWorksDTO>().apply {
            parcel.readList(this.toList(), JobEstimateWorksDTO::class.java.classLoader)
        },
        jobItemEstimatePhotos = arrayListOf<JobItemEstimatesPhotoDTO>().apply {
            parcel.readList(this.toList(), JobItemEstimatesPhotoDTO::class.java.classLoader)
        },
        jobItemMeasure = arrayListOf<JobItemMeasureDTO>().apply {
            parcel.readList(this.toList(), JobItemMeasureDTO::class.java.classLoader)
        },
        projectItemId = parcel.readString(),
        projectVoId = parcel.readString(),
        qty = parcel.readDouble(),
        recordSynchStateId = parcel.readInt(),
        recordVersion = parcel.readInt(),
        trackRouteId = parcel.readString(),
        jobItemEstimatePhotoStart = parcel.readValue(JobItemEstimatesPhotoDTO::class.java.classLoader)
            as? JobItemEstimatesPhotoDTO,
        jobItemEstimatePhotoEnd = parcel.readValue(JobItemEstimatesPhotoDTO::class.java.classLoader)
            as? JobItemEstimatesPhotoDTO,
        estimateComplete = parcel.readString(),
        measureActId = parcel.readInt(),
        selectedItemUom = parcel.readString(),
        startKmMarker = parcel.readString(),
        endKmMarker = parcel.readString()
    )

    private fun getJobItemEstimatePhoto(lookForStartPhoto: Boolean): Pair<Int, JobItemEstimatesPhotoDTO>? {
        return jobItemEstimatePhotos.filter { photo ->
            photo.isPhotostart == lookForStartPhoto
        }
            .mapIndexed { index, photo ->
                Pair(index, photo)
            }.first()
    }

    fun getPhoto(x: Int): JobItemEstimatesPhotoDTO? {
        return if (jobItemEstimatePhotos.isNotEmpty() && -1 < x && x < size()) {
            jobItemEstimatePhotos[x]
        } else null
    }

    fun setJobItemEstimatePhoto(photo: JobItemEstimatesPhotoDTO) {
        if (jobItemEstimatePhotos.isEmpty()) {
            jobItemEstimatePhotos.add(photo)
        } else {
            val photoToChange = getJobItemEstimatePhoto(photo.isPhotoStart())
            val index = photoToChange.first!!
            if (index == -1) {
                jobItemEstimatePhotos.add(photo)
            } else {
                jobItemEstimatePhotos[index] = photo
            }
        }
        JobUtils.sort(jobItemEstimatePhotos)
    }

    fun size(): Int {
        return jobItemEstimatePhotos.size
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(actId)
        parcel.writeString(estimateId)
        parcel.writeString(jobId)
        parcel.writeDouble(lineRate)
        parcel.writeString(projectItemId)
        parcel.writeString(projectVoId)
        parcel.writeDouble(qty)
        parcel.writeInt(recordSynchStateId)
        parcel.writeInt(recordVersion)
        parcel.writeString(trackRouteId)
        parcel.writeString(estimateComplete)
        parcel.writeInt(measureActId)
        parcel.writeString(selectedItemUom)
        parcel.writeList(jobItemMeasure.toList())
        parcel.writeList(jobEstimateWorks.toList())
        parcel.writeList(jobItemEstimatePhotos.toList())
        parcel.writeValue(jobItemEstimatePhotoStart)
        parcel.writeValue(jobItemEstimatePhotoEnd)
        parcel.writeString(startKmMarker)
        parcel.writeString(endKmMarker)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Creator<JobItemEstimateDTO> {
        const val serialVersionUID: Long = 10L
        override fun createFromParcel(parcel: Parcel): JobItemEstimateDTO {
            return JobItemEstimateDTO(parcel)
        }

        override fun newArray(size: Int): Array<JobItemEstimateDTO?> {
            return arrayOfNulls(size)
        }
    }
}
