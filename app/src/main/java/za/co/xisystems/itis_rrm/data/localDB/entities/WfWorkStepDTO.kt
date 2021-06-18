/**
 * Updated by Shaun McDonald on 2021/05/15
 * Last modified on 2021/05/14, 20:32
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 **/

/**
 * Updated by Shaun McDonald on 2021/05/14
 * Last modified on 2021/05/14, 19:43
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 **/

package za.co.xisystems.itis_rrm.data.localDB.entities

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Created by Francis Mahlava on 2020/02/12.
 */

const val WorkStep_TABLE = "WorkStep_TABLE"

@Entity(tableName = WorkStep_TABLE)
class WfWorkStepDTO(
    @SerializedName("WorkStep_ID")
    @PrimaryKey
    val workStepId: Int,
    @SerializedName("Step_Code")
    val stepCode: String?,
    @SerializedName("DESCR")
    val descrip: String?,
    @SerializedName("ACT_TYPE_ID")
    val actTypeId: Int?
) : Serializable, Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(workStepId)
        parcel.writeString(stepCode)
        parcel.writeString(descrip)
        parcel.writeValue(actTypeId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Creator<WfWorkStepDTO> {
        const val serialVersionUID: Long = 27L
        override fun createFromParcel(parcel: Parcel): WfWorkStepDTO {
            return WfWorkStepDTO(parcel)
        }

        override fun newArray(size: Int): Array<WfWorkStepDTO?> {
            return arrayOfNulls(size)
        }
    }
}
