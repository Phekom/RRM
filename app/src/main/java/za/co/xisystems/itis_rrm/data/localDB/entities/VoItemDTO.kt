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
import org.jetbrains.annotations.Nullable
import java.io.Serializable

/**
 * Created by Francis Mahlava on 2019/11/21.
 */

const val TABLE_JOB_VO_ITEM = "TABLE_JOB_VO_ITEM"

@Entity(
    tableName = TABLE_JOB_VO_ITEM,
    foreignKeys = [
        ForeignKey(
            entity = ProjectDTO::class,
            parentColumns = arrayOf("projectId"),
            childColumns = arrayOf("projectId"),
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class VoItemDTO(
    @SerializedName("ProjectVoId")
    @PrimaryKey
    val projectVoId: String,
    @SerializedName("ProjectItemId")
    val projectVoItemId: String?,
    @SerializedName("ContractVoId")
    val contractVoId: String?,
    @SerializedName("ContractVoItemId")
    val contractVoItemId: String?,
    @SerializedName("ProjectId")
    @ColumnInfo(name = "projectId", index = true)
    val projectId: String, // 2A74D6A6D69842D1A2ADFD54BF7308CD
    @SerializedName("ItemCode")
    val itemCode: String?,
    @SerializedName("VoDescr")
    val voDescr: String?,
    @SerializedName("Descr")
    val descr: String?,
    @SerializedName("Uom")
    val uom: String?,
    @SerializedName("Rate")
    val tenderValue: Double?,
    @SerializedName("Uomdescr")
    var uomdescr: String?,
    @SerializedName("WorkflowId")
    var workflowId: Int = 0,
    var sectionItemId: String?,
    val quantity: Double = 0.toDouble(),
    val estimateId: String?,
    @Nullable
    @SerializedName("ItemSections")
    val itemSections: ArrayList<ItemSectionDTO> = ArrayList()

    ) : Serializable, Parcelable {
    constructor(parcel: Parcel) : this(
       // parcel.readInt(),
        parcel.readString()!!,
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()!!,
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readDouble(),
        parcel.readString(),
        workflowId = parcel.readInt(),
        sectionItemId = parcel.readString(),
        quantity = parcel.readDouble(),
        estimateId = parcel.readString(),
        itemSections = arrayListOf<ItemSectionDTO>().apply {
            parcel.readList(this.toList(), ItemSectionDTO::class.java.classLoader)
        },
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
       // parcel.writeInt(id)
        parcel.writeString(projectVoId)
        parcel.writeString(projectVoItemId)
        parcel.writeString(contractVoId)
        parcel.writeString(contractVoItemId)
        parcel.writeString(projectId)
        parcel.writeString(itemCode)
        parcel.writeString(voDescr)
        parcel.writeString(descr)
        parcel.writeString(uom)
        parcel.writeValue(tenderValue)
        parcel.writeString(uomdescr)
        parcel.writeValue(workflowId)
        parcel.writeString(sectionItemId)
        parcel.writeDouble(quantity)
        parcel.writeString(estimateId)
        parcel.writeList(itemSections.toList())
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
