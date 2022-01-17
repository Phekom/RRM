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
import org.jetbrains.annotations.NotNull

/**
 * Created by Francis Mahlava on 2019/11/26.
 */

const val ACTIVITY_TABLE = "ACTIVITY_TABLE"

@Entity(tableName = ACTIVITY_TABLE)
data class ActivityDTO(
    @SerializedName("ActId")
    @PrimaryKey
    val actId: Long,
    @SerializedName("ActTypeId")
    val actTypeId: Long?,
    @SerializedName("ApprovalId")
    val approvalId: Long = 0,
    @SerializedName("sContentId")
    val sContentId: Long = 0,
    @SerializedName("ActName")
    val actName: String?,
    @SerializedName("Descr")
    val descr: String?

) : Serializable, Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readValue(Long::class.java.classLoader) as? Long,
        parcel.readLong(),
        parcel.readLong(),
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(actId)
        parcel.writeValue(actTypeId)
        parcel.writeLong(approvalId)
        parcel.writeLong(sContentId)
        parcel.writeString(actName)
        parcel.writeString(descr)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Creator<ActivityDTO> {
        const val serialVersionUID = 1L
        override fun createFromParcel(parcel: Parcel): ActivityDTO {
            return ActivityDTO(parcel)
        }

        override fun newArray(size: Int): Array<ActivityDTO?> {
            return arrayOfNulls(size)
        }
    }
}
