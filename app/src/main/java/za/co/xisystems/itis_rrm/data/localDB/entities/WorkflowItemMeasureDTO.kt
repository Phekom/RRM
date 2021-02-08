/*
 * Updated by Shaun McDonald on 2021/02/08
 * Last modified on 2021/02/08 2:32 PM
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 */

package za.co.xisystems.itis_rrm.data.localDB.entities

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import androidx.room.Entity
import com.google.gson.annotations.SerializedName
import za.co.xisystems.itis_rrm.domain.MeasurementTrack
import za.co.xisystems.itis_rrm.utils.DataConversion
import java.io.Serializable

@Entity
data class WorkflowItemMeasureDTO(
    @SerializedName("ActId")
    var actId: Int, // 1
    @SerializedName("ItemMeasureId")
    var itemMeasureId: String, // sample string 1
    @SerializedName("MeasureGroupId")
    var measureGroupId: String, // sample string 3
    @SerializedName("TrackRouteId")
    var trackRouteId: String // sample string 2
): Serializable, Parcelable {
    constructor(parcel: Parcel) : this(
        actId = parcel.readInt(),
        itemMeasureId = parcel.readString()!!,
        measureGroupId = parcel.readString()!!,
        trackRouteId = parcel.readString()!!
    )

    fun toMeasurementTrack(userId: String, description: String, direction: Int): MeasurementTrack {
        return DataConversion.toLittleEndian(trackRouteId)?.let {
            MeasurementTrack(
                userId = userId,
                description = description,
                direction = direction,
                trackRouteId = it
            )
        }!!
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(actId)
        parcel.writeString(itemMeasureId)
        parcel.writeString(measureGroupId)
        parcel.writeString(trackRouteId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Creator<WorkflowItemMeasureDTO> {
        override fun createFromParcel(parcel: Parcel): WorkflowItemMeasureDTO {
            return WorkflowItemMeasureDTO(parcel)
        }

        override fun newArray(size: Int): Array<WorkflowItemMeasureDTO?> {
            return arrayOfNulls(size)
        }
    }
}
