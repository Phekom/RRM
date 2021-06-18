/*
 * Updated by Shaun McDonald on 2021/02/08
 * Last modified on 2021/02/07 12:13 AM
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
import java.util.ArrayList

/**
 * Created by Francis Mahlava on 2020/02/04.
 */

@Entity
class WorkflowJobDTO(

    @PrimaryKey
    @SerializedName("JobId")
    var jobId: String? = null,

    @SerializedName("ActId")
    var actId: Int = 0,

    @SerializedName("TrackRouteId")
    var trackRouteId: String? = null,

    @SerializedName("JiNo")
    var jiNo: String? = null,

    @SerializedName("WorkflowItemEstimates")
    val workflowItemEstimates: ArrayList<WorkflowItemEstimateDTO> = ArrayList(),

    @SerializedName("WorkflowItemMeasures")
    val workflowItemMeasures: ArrayList<WorkflowItemMeasureDTO> = ArrayList(),

    @SerializedName("WorkflowJobSections")
    val workflowJobSections: ArrayList<JobSectionDTO> = ArrayList()

) : Serializable, Parcelable {
    constructor(parcel: Parcel) : this(
        jobId = parcel.readString(),
        actId = parcel.readInt(),
        trackRouteId = parcel.readString(),
        jiNo = parcel.readString(),
        workflowItemEstimates = arrayListOf<WorkflowItemEstimateDTO>().apply {
            parcel.readList(this.toList(), WorkflowItemEstimateDTO::class.java.classLoader)
        },
        workflowItemMeasures = arrayListOf<WorkflowItemMeasureDTO>().apply {
            parcel.readList(this.toList(), WorkflowItemMeasureDTO::class.java.classLoader)
        },
        workflowJobSections = arrayListOf<JobSectionDTO>().apply {
            parcel.readList(this.toList(), JobSectionDTO::class.java.classLoader)
        }
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(jobId)
        parcel.writeInt(actId)
        parcel.writeString(trackRouteId)
        parcel.writeString(jiNo)
        parcel.writeList(workflowItemEstimates.toList())
        parcel.writeList(workflowItemMeasures.toList())
        parcel.writeList(workflowJobSections.toList())
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Creator<WorkflowJobDTO> {
        const val serialVersionUID: Long = 32L
        override fun createFromParcel(parcel: Parcel): WorkflowJobDTO {
            return WorkflowJobDTO(parcel)
        }

        override fun newArray(size: Int): Array<WorkflowJobDTO?> {
            return arrayOfNulls(size)
        }
    }
}
