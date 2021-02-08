/*
 * Updated by Shaun McDonald on 2021/02/08
 * Last modified on 2021/02/08 2:32 PM
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 */

package za.co.xisystems.itis_rrm.data.localDB.entities

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Created by Francis Mahlava on 2020/02/04.
 */
class WorkflowJobSectionDTO(

    @SerializedName("JobId")
    var jobId: String? = null,

    @SerializedName("JobSectionId")
    val jobSectionId: String? = null,

    @SerializedName("ProjectSectionId")
    val projectSectionId: String? = null,

    @SerializedName("RecordVersion")
    val recordVersion: Int = 0,

    @SerializedName("RecordSynchStateId")
    val recordSynchStateId: Int = 0,

    @SerializedName("StartKm")
    val startKm: Double = 0.0,

    @SerializedName("EndKm")
    val endKm: Double = 0.0

) : Serializable, Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readDouble(),
        parcel.readDouble()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(jobId)
        parcel.writeString(jobSectionId)
        parcel.writeString(projectSectionId)
        parcel.writeInt(recordVersion)
        parcel.writeInt(recordSynchStateId)
        parcel.writeDouble(startKm)
        parcel.writeDouble(endKm)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Creator<WorkflowJobSectionDTO> {
        override fun createFromParcel(parcel: Parcel): WorkflowJobSectionDTO {
            return WorkflowJobSectionDTO(parcel)
        }

        override fun newArray(size: Int): Array<WorkflowJobSectionDTO?> {
            return arrayOfNulls(size)
        }
    }
}
