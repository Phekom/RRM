/*
 * Updated by Shaun McDonald on 2021/01/25
 * Last modified on 2021/01/25 6:30 PM
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 */

package za.co.xisystems.itis_rrm.data.localDB.entities

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Created by Francis Mahlava on 2019/11/26.
 */

const val JOB_ESTIMATE_WORKS = "JOB_ESTIMATE_WORKS"

@Entity(tableName = JOB_ESTIMATE_WORKS)
class JobEstimateWorksDTO(
    @SerializedName("ActId")
    var actId: Int,
    @SerializedName("EstimateId")
    var estimateId: String?,
    @SerializedName("MobileJobEstimateWorksPhotos")
    var jobEstimateWorksPhotos: ArrayList<JobEstimateWorksPhotoDTO> = ArrayList(),
    @SerializedName("RecordSynchStateId")
    var recordSynchStateId: Int,
    @SerializedName("RecordVersion")
    var recordVersion: Int,
    @SerializedName("TrackRouteId")
    var trackRouteId: String,
    @SerializedName("WorksId")
    @PrimaryKey
    var worksId: String
) : Serializable, Parcelable {
    constructor(parcel: Parcel) : this(
        actId = parcel.readInt(),
        estimateId = parcel.readString(),
        jobEstimateWorksPhotos = arrayListOf<JobEstimateWorksPhotoDTO>().apply {
            parcel.readList(this.toList(), JobEstimateWorksPhotoDTO::class.java.classLoader)
        },
        recordSynchStateId = parcel.readInt(),
        recordVersion = parcel.readInt(),
        trackRouteId = parcel.readString()!!,
        worksId = parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(actId)
        parcel.writeString(estimateId)
        parcel.writeList(jobEstimateWorksPhotos.toList())
        parcel.writeInt(recordSynchStateId)
        parcel.writeInt(recordVersion)
        parcel.writeString(trackRouteId)
        parcel.writeString(worksId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Creator<JobEstimateWorksDTO> {
        const val serialVersionUID = 8L
        override fun createFromParcel(parcel: Parcel): JobEstimateWorksDTO {
            return JobEstimateWorksDTO(parcel)
        }

        override fun newArray(size: Int): Array<JobEstimateWorksDTO?> {
            return arrayOfNulls(size)
        }
    }
}
