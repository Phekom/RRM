/*
 * Updated by Shaun McDonald on 2021/02/08
 * Last modified on 2021/02/07 8:43 AM
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

const val PROJECT_ITEM_TABLE_TEMP = "PROJECT_ITEM_TABLE_TEMP"

@Entity(
    tableName = PROJECT_ITEM_TABLE_TEMP,
    foreignKeys = [
        ForeignKey(
            entity = ProjectDTO::class,
            parentColumns = arrayOf("projectId"),
            childColumns = arrayOf("projectId"),
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ItemDTOTemp(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    @SerializedName("ItemId")
    @ColumnInfo(name = "itemId", index = true)
    val itemId: String,
    @SerializedName("Descr")
    val descr: String?,
    @SerializedName("ItemCode")
    val itemCode: String?,

    @SerializedName("ItemSections")
    val itemSections: ArrayList<ItemSectionDTO> = ArrayList(),

    @SerializedName("TenderRate")
    val tenderRate: Double = 0.toDouble(),
    @SerializedName("Uom")
    val uom: String?,
    @SerializedName("WorkflowId")
    val workflowId: Int?,

    val sectionItemId: String?,

    var quantity: Double = 0.toDouble(),

    val estimateId: String?,

    @SerializedName("ProjectId")
    @ColumnInfo(name = "projectId", index = true)
    val projectId: String,

    val jobId: String

) : Serializable, Parcelable {
    constructor(parcel: Parcel) : this(
        id = parcel.readLong(),
        itemId = parcel.readString()!!,
        descr = parcel.readString(),
        itemCode = parcel.readString(),
        itemSections = arrayListOf<ItemSectionDTO>().apply {
            parcel.readList(this.toList(), ItemSectionDTO::class.java.classLoader)
        },
        tenderRate = parcel.readDouble(),
        uom = parcel.readString(),
        workflowId = parcel.readValue(Int::class.java.classLoader) as? Int,
        sectionItemId = parcel.readString(),
        quantity = parcel.readDouble(),
        estimateId = parcel.readString(),
        projectId = parcel.readString()!!,
        jobId = parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(itemId)
        parcel.writeString(descr)
        parcel.writeString(itemCode)
        parcel.writeDouble(tenderRate)
        parcel.writeString(uom)
        parcel.writeValue(workflowId)
        parcel.writeString(sectionItemId)
        parcel.writeDouble(quantity)
        parcel.writeString(estimateId)
        parcel.writeString(projectId)
        parcel.writeString(jobId)
        parcel.writeList(itemSections.toList())
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Creator<ItemDTOTemp> {
        const val serialVersionUID = 4L
        override fun createFromParcel(parcel: Parcel): ItemDTOTemp {
            return ItemDTOTemp(parcel)
        }

        override fun newArray(size: Int): Array<ItemDTOTemp?> {
            return arrayOfNulls(size)
        }
    }
}
