/*
 * Updated by Shaun McDonald on 2021/02/08
 * Last modified on 2021/02/07 9:05 AM
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

const val WORKFLOWs_TABLE = "WORKFLOWs_TABLE"

@Entity(tableName = WORKFLOWs_TABLE)
data class WorkFlowsDTO(
    @PrimaryKey
    @SerializedName("Activities")
    val activities: ArrayList<ActivityDTO>,
    @SerializedName("InfoClasses")
    val infoClasses: ArrayList<InfoClassDTO>?,
    @SerializedName("Workflows")
    val workflows: ArrayList<WorkFlowDTO>?
) : Serializable, Parcelable {
    constructor(parcel: Parcel) : this(
        activities = arrayListOf<ActivityDTO>().apply {
            parcel.readList(this.toList(), ActivityDTO::class.java.classLoader)
        },
        infoClasses = arrayListOf<InfoClassDTO>().apply {
            parcel.readList(this.toList(), InfoClassDTO::class.java.classLoader)
        },
        workflows = arrayListOf<WorkFlowDTO>().apply {
            parcel.readList(this.toList(), WorkFlowDTO::class.java.classLoader)
        }
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeList(activities?.toList())
        parcel.writeList(infoClasses?.toList())
        parcel.writeList(workflows?.toList())
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Creator<WorkFlowsDTO> {
        const val serialVersionUID: Long = 35L
        override fun createFromParcel(parcel: Parcel): WorkFlowsDTO {
            return WorkFlowsDTO(parcel)
        }

        override fun newArray(size: Int): Array<WorkFlowsDTO?> {
            return arrayOfNulls(size)
        }
    }
}
