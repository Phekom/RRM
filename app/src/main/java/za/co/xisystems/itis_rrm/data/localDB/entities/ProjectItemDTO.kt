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
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

/**
 * Created by Francis Mahlava on 2019/11/21.
 */

const val PROJECT_ITEM_TABLE = "PROJECT_ITEM_TABLE"

@Entity(
    tableName = PROJECT_ITEM_TABLE,
    foreignKeys = [
        ForeignKey(
            entity = ProjectDTO::class,
            parentColumns = arrayOf("projectId"),
            childColumns = arrayOf("projectId"),
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ProjectItemDTO(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    @SerializedName("ItemId")
    @NotNull
    val itemId: String,
    @SerializedName("Descr")
    val descr: String?,
    @SerializedName("ParentDescr")
    val parentDescr: String?, //"Repair of existing fence of greater than 100 m lengths:"
    @SerializedName("ItemCode")
    val itemCode: String?,
    @Nullable
    @SerializedName("ItemSections")
    val itemSections: ArrayList<ItemSectionDTO> = ArrayList(),
    @SerializedName("TenderRate")
    val tenderRate: Double = 0.0,
    @SerializedName("Uom")
    val uom: String?,
    @SerializedName("WorkflowId")
    val workflowId: Int?,
    val sectionItemId: String?,
    val quantity: Double = 0.toDouble(),
    val estimateId: String?,
    @SerializedName("ProjectId")
    @NotNull
    @ColumnInfo(name = "projectId", index = true)
    val projectId: String?,
    @SerializedName("ContractVoId")
    var contractVoId: String?,
    @SerializedName("ProjectVoId")
    var projectVoId: String?

) : Serializable, Parcelable {
    constructor(parcel: Parcel) : this(
        id = parcel.readInt(),
        itemId = parcel.readString()!!,
        descr = parcel.readString(),
        parentDescr = parcel.readString(),
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
        projectId = parcel.readString(),
        contractVoId = parcel.readString(),
        projectVoId = parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(itemId)
        parcel.writeString(descr)
        parcel.writeString(parentDescr)
        parcel.writeString(itemCode)
        parcel.writeDouble(tenderRate)
        parcel.writeString(uom)
        parcel.writeValue(workflowId)
        parcel.writeString(sectionItemId)
        parcel.writeDouble(quantity)
        parcel.writeString(estimateId)
        parcel.writeString(projectId)
        parcel.writeString(contractVoId)
        parcel.writeString(projectVoId)
        parcel.writeList(itemSections.toList())
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Creator<ProjectItemDTO> {
        const val serialVersionUID: Long = 19L

        override fun createFromParcel(parcel: Parcel): ProjectItemDTO {
            return ProjectItemDTO(parcel)
        }

        override fun newArray(size: Int): Array<ProjectItemDTO?> {
            return arrayOfNulls(size)
        }
    }
}
