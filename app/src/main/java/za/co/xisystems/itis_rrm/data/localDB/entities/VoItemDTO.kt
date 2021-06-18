/*
 * Updated by Shaun McDonald on 2021/01/25
 * Last modified on 2021/01/25 6:30 PM
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 */

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

const val TABLE_JOB_VO_ITEM = "TABLE_JOB_VO_ITEM"

@Entity(
    tableName = TABLE_JOB_VO_ITEM, foreignKeys = [ForeignKey(
        entity = ProjectDTO::class,
        parentColumns = arrayOf("projectId"),
        childColumns = arrayOf("projectId"),
        onDelete = ForeignKey.CASCADE
    )]
)
data class VoItemDTO(
    @PrimaryKey
    val id: Int,
    @SerializedName("ProjectVoId")
    val projectVoId: String,
    @SerializedName("ItemCode")
    val itemCode: String?,
    @SerializedName("VoDescr")
    val voDescr: String?,
    @SerializedName("Descr")
    val descr: String?,
    @SerializedName("Uom")
    val uom: String?,
    @SerializedName("Rate")
    val rate: Double?,
    @SerializedName("ProjectItemId")
    val projectItemId: String?,
    @SerializedName("ContractVoId")
    val contractVoId: String?,
    @SerializedName("ContractVoItemId")
    val contractVoItemId: String?,
    @SerializedName("ProjectId")
    @ColumnInfo(name = "projectId", index = true)
    val projectId: String

) : Serializable, Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Double::class.java.classLoader) as? Double,
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(projectVoId)
        parcel.writeString(itemCode)
        parcel.writeString(voDescr)
        parcel.writeString(descr)
        parcel.writeString(uom)
        parcel.writeValue(rate)
        parcel.writeString(projectItemId)
        parcel.writeString(contractVoId)
        parcel.writeString(contractVoItemId)
        parcel.writeString(projectId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Creator<VoItemDTO> {
        const val serialVersionUID: Long = 27L
        override fun createFromParcel(parcel: Parcel): VoItemDTO {
            return VoItemDTO(parcel)
        }

        override fun newArray(size: Int): Array<VoItemDTO?> {
            return arrayOfNulls(size)
        }
    }
}
