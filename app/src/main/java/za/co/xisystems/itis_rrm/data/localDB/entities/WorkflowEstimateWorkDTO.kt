/*
 * Updated by Shaun McDonald on 2021/01/25
 * Last modified on 2021/01/25 6:30 PM
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 */

package za.co.xisystems.itis_rrm.data.localDB.entities

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class WorkflowEstimateWorkDTO(
    @SerializedName("ActId")
    var actId: Int, // 1
    @SerializedName("EstimateId")
    var estimateId: String, // sample string 3
    @SerializedName("RecordSynchStateId")
    var recordSynchStateId: Int, // 5
    @SerializedName("RecordVersion")
    var recordVersion: Int, // 4
    @SerializedName("TrackRouteId")
    var trackRouteId: String, // sample string 2
    @SerializedName("WorksId")
    var worksId: String // sample string 1
) : Serializable, Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(actId)
        parcel.writeString(estimateId)
        parcel.writeInt(recordSynchStateId)
        parcel.writeInt(recordVersion)
        parcel.writeString(trackRouteId)
        parcel.writeString(worksId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Creator<WorkflowEstimateWorkDTO> {
        override fun createFromParcel(parcel: Parcel): WorkflowEstimateWorkDTO {
            return WorkflowEstimateWorkDTO(parcel)
        }

        override fun newArray(size: Int): Array<WorkflowEstimateWorkDTO?> {
            return arrayOfNulls(size)
        }
    }
}
