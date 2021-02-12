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

/**
 * Created by Francis Mahlava on 2019/11/21.
 */

const val WORKFLOW_TABLE = "WORKFLOW_TABLE"

@Entity(tableName = WORKFLOW_TABLE)
data class WorkFlowDTO(
    @SerializedName("DateCreated")
    var dateCreated: String,

    @SerializedName("ErrorRouteId")
    var errorRouteId: Long,

    @SerializedName("RevNo")
    var revNo: Long,

    @SerializedName("StartRouteId")
    var startRouteId: Long,

    @SerializedName("UserId")
    var userId: Long,

    @SerializedName("WfHeaderId")
    var wfHeaderId: Long,

    @SerializedName("WorkFlowRoute")
    var workFlowRoute: ArrayList<WorkFlowRouteDTO> = ArrayList(),

    @SerializedName("WorkflowId")
    @PrimaryKey
    var workflowId: Long

): Serializable, Parcelable {
    constructor(parcel: Parcel) : this(
        dateCreated = parcel.readString()!!,
        errorRouteId = parcel.readLong(),
        revNo = parcel.readLong(),
        startRouteId = parcel.readLong(),
        userId = parcel.readLong(),
        wfHeaderId = parcel.readLong(),
        workFlowRoute = arrayListOf<WorkFlowRouteDTO>().apply {
            parcel.readList(this.toList(), WorkFlowRouteDTO::class.java.classLoader)
        },
        workflowId = parcel.readLong()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(dateCreated)
        parcel.writeLong(errorRouteId)
        parcel.writeLong(revNo)
        parcel.writeLong(startRouteId)
        parcel.writeLong(userId)
        parcel.writeLong(wfHeaderId)
        parcel.writeLong(workflowId)
        parcel.writeList(workFlowRoute.toList())
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Creator<WorkFlowDTO> {
        override fun createFromParcel(parcel: Parcel): WorkFlowDTO {
            return WorkFlowDTO(parcel)
        }

        override fun newArray(size: Int): Array<WorkFlowDTO?> {
            return arrayOfNulls(size)
        }
    }
}
