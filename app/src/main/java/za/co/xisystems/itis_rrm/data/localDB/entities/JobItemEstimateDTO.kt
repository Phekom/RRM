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
import za.co.xisystems.itis_rrm.utils.JobItemEstimateSize
import za.co.xisystems.itis_rrm.utils.JobUtils
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
    var estimateId: String,
    @SerializedName("JobId")
    var jobId: String?,
    @SerializedName("LineRate")
    var lineRate: Double,
    @SerializedName("JobEstimateSize") // JobEstimateSize
    var jobItemEstimateSize: String?,
    @SerializedName("MobileEstimateWorks")
    var jobEstimateWorks: ArrayList<JobEstimateWorksDTO> = ArrayList(),
    @SerializedName("MobileJobItemEstimatesPhotos")
    var jobItemEstimatePhotos: ArrayList<JobItemEstimatesPhotoDTO> = ArrayList(),
    @SerializedName("MobileJobItemMeasures")
    var jobItemMeasure: ArrayList<JobItemMeasureDTO> = ArrayList(),
    @SerializedName("ProjectItemId")
    var projectItemId: String?,
    @SerializedName("ContractVoId")
    var contractVoId: String?,
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
    var geoCoded: Boolean = false
) : Parcelable, Serializable {

    constructor(parcel: Parcel) : this(
        actId = parcel.readInt(),
        estimateId = parcel.readString()!!,
        jobId = parcel.readString(),
        lineRate = parcel.readDouble(),
        jobItemEstimateSize = parcel.readString(),
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
        contractVoId = parcel.readString(),
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
        geoCoded = parcel.readByte() != 0.toByte()
    )

    fun getJobItemEstimatePhoto(lookForStartPhoto: Boolean): Pair<Int, JobItemEstimatesPhotoDTO> {

        val photoToReplace =
            jobItemEstimatePhotos.firstOrNull { photo ->
                photo.isStartPhoto() == lookForStartPhoto
            } ?: return Pair(-1, null)
        val photoIndex = jobItemEstimatePhotos.indexOf(photoToReplace)
        return Pair(photoIndex, photoToReplace)
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
            val photoToChange = getJobItemEstimatePhoto(photo.isStartPhoto())
            val index = photoToChange.first!!
            if (index == -1) {
                jobItemEstimatePhotos.add(photo)
            } else {
                jobItemEstimatePhotos[index] = photo
            }
        }
        jobItemEstimatePhotos = JobUtils.sort(jobItemEstimatePhotos)!!
    }

    fun size(): Int {
        return jobItemEstimatePhotos.size
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(actId)
        parcel.writeString(estimateId)
        parcel.writeString(jobId)
        parcel.writeDouble(lineRate)
        parcel.writeString(jobItemEstimateSize)
        parcel.writeString(projectItemId)
        parcel.writeString(projectVoId)
        parcel.writeString(contractVoId)
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
        parcel.writeByte(if (geoCoded) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    fun arePhotosGeoCoded(): Boolean {
        var result = true
        if (jobItemEstimateSize == JobItemEstimateSize.POINT.getValue() && jobItemEstimatePhotos.size < 1) return false
        if (jobItemEstimateSize == JobItemEstimateSize.LINE.getValue() && jobItemEstimatePhotos.size < 2) return false
        for (photo in jobItemEstimatePhotos) {
            if (!photo.geoCoded) {
                result = false
                break
            }
        }
        return result
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
