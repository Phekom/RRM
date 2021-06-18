/*
 * Updated by Shaun McDonald on 2021/02/08
 * Last modified on 2021/02/07 1:31 AM
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 */

package za.co.xisystems.itis_rrm.data.localDB.entities

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import androidx.room.Entity
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Entity
data class WorkflowItemEstimateDTO(
    @SerializedName("ActId")
    var actId: Int, // 1
    @SerializedName("EstimateId")
    var estimateId: String, // sample string 1
    @SerializedName("TrackRouteId")
    var trackRouteId: String, // sample string 2
    @SerializedName("WorkflowEstimateWorks")
    var workflowEstimateWorks: ArrayList<WorkflowEstimateWorkDTO> = ArrayList()
) : Serializable, Parcelable {
    constructor(parcel: Parcel) : this(
        actId = parcel.readInt(),
        estimateId = parcel.readString()!!,
        trackRouteId = parcel.readString()!!,
        workflowEstimateWorks = arrayListOf<WorkflowEstimateWorkDTO>().apply {
            parcel.readList(this.toList(), WorkflowEstimateWorkDTO::class.java.classLoader)
        }
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(actId)
        parcel.writeString(estimateId)
        parcel.writeString(trackRouteId)
        parcel.writeList(workflowEstimateWorks.toList())
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Creator<WorkflowItemEstimateDTO> {
        const val serialVersionUID: Long = 30L
        override fun createFromParcel(parcel: Parcel): WorkflowItemEstimateDTO {
            return WorkflowItemEstimateDTO(parcel)
        }

        override fun newArray(size: Int): Array<WorkflowItemEstimateDTO?> {
            return arrayOfNulls(size)
        }
    }
}
