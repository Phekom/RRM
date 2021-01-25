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

const val WORKFLOWs_TABLE = "WORKFLOWs_TABLE"

@Entity(tableName = WORKFLOWs_TABLE)
data class WorkFlowsDTO(
    @SerializedName("Activities")
    @PrimaryKey
    val activities: ArrayList<ActivityDTO> = ArrayList(),
    @SerializedName("InfoClasses")
    val infoClasses: ArrayList<InfoClassDTO> = ArrayList(),
    @SerializedName("Workflows")
    val workflows: ArrayList<WorkFlowDTO> = ArrayList()
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
        parcel.writeList(activities.toList())
        parcel.writeList(infoClasses.toList())
        parcel.writeList(workflows.toList())
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Creator<WorkFlowsDTO> {
        override fun createFromParcel(parcel: Parcel): WorkFlowsDTO {
            return WorkFlowsDTO(parcel)
        }

        override fun newArray(size: Int): Array<WorkFlowsDTO?> {
            return arrayOfNulls(size)
        }
    }
}
