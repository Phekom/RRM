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
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Created by Francis Mahlava on 2019/11/21.
 */

const val WORKFLOW_ROUTE_TABLE = "WORKFLOW_ROUTE_TABLE"

@Entity(
    tableName = WORKFLOW_ROUTE_TABLE, foreignKeys = [ForeignKey(
        entity = WorkFlowDTO::class,
        parentColumns = arrayOf("workflowId"),
        childColumns = arrayOf("workflowId"),
        onDelete = ForeignKey.CASCADE
    )]
)
data class WorkFlowRouteDTO(

    @PrimaryKey
    var id: Int,

    @SerializedName("RouteId")
    val routeId: Long,

    @SerializedName("ActId")
    val actId: Long,

    @SerializedName("NextRouteId")
    val nextRouteId: Long,

    @SerializedName("FailRouteId")
    val failRouteId: Long,

    @SerializedName("ErrorRouteId")
    val errorRouteId: Long,

    @SerializedName("CanStart")
    val canStart: Long,

    @SerializedName("WorkflowId")
    @ColumnInfo(name = "workflowId", index = true)
    var workflowId: Long?
) : Serializable, Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readLong(),
        parcel.readLong(),
        parcel.readLong(),
        parcel.readLong(),
        parcel.readLong(),
        parcel.readLong(),
        parcel.readValue(Long::class.java.classLoader) as? Long
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeLong(routeId)
        parcel.writeLong(actId)
        parcel.writeLong(nextRouteId)
        parcel.writeLong(failRouteId)
        parcel.writeLong(errorRouteId)
        parcel.writeLong(canStart)
        parcel.writeValue(workflowId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Creator<WorkFlowRouteDTO> {
        const val serialVersionUID: Long = 34L
        override fun createFromParcel(parcel: Parcel): WorkFlowRouteDTO {
            return WorkFlowRouteDTO(parcel)
        }

        override fun newArray(size: Int): Array<WorkFlowRouteDTO?> {
            return arrayOfNulls(size)
        }
    }
}
