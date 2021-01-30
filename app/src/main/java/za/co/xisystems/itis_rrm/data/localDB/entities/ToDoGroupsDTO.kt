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

const val TODO_GROUPS_TABLE = "TODO_GROUPS_TABLE"

@Entity(
    tableName = TODO_GROUPS_TABLE
//    , foreignKeys = [
//        ForeignKey(
//            entity = ProjectDTO::class,
//            parentColumns = arrayOf("projectId"),
//            childColumns = arrayOf("projectId"),
//            onDelete = ForeignKey.NO_ACTION
//        )
//    ]
)
data class ToDoGroupsDTO(

    @SerializedName("GroupId")
    @PrimaryKey
    val groupId: String, // MeasureApprove
    @SerializedName("GroupName")
    val groupName: String, // Measurements To Approve

    @SerializedName("GroupDescription")
    val groupDescription: String, // Measurements that require Approval for payment processing

    @SerializedName("Entities")
    val toDoListEntities: ArrayList<ToDoListEntityDTO> = ArrayList(),

    @SerializedName("SortOrder")
    val sortOrder: Int // 5
) : Parcelable {
    constructor(parcel: Parcel) : this(
        groupId = parcel.readString()!!,
        groupName = parcel.readString()!!,
        groupDescription = parcel.readString()!!,
        toDoListEntities = arrayListOf<ToDoListEntityDTO>().apply {
            parcel.readList(this.toList(), ToDoListEntityDTO::class.java.classLoader)
        },
        sortOrder = parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(groupId)
        parcel.writeString(groupName)
        parcel.writeList(toDoListEntities.toList())
        parcel.writeString(groupDescription)
        parcel.writeInt(sortOrder)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Creator<ToDoGroupsDTO> {
        override fun createFromParcel(parcel: Parcel): ToDoGroupsDTO {
            return ToDoGroupsDTO(parcel)
        }

        override fun newArray(size: Int): Array<ToDoGroupsDTO?> {
            return arrayOfNulls(size)
        }
    }
}
