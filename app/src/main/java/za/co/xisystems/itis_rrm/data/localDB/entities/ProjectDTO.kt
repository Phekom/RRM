/*
 * Updated by Shaun McDonald on 2021/22/20
 * Last modified on 2021/01/20 12:55 PM
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 */

package za.co.xisystems.itis_rrm.data.localDB.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import org.jetbrains.annotations.NotNull
import za.co.xisystems.itis_rrm.utils.SqlLitUtils
import java.io.Serializable

/**
 * Created by Francis Mahlava on 2019/11/22.
 */

const val PROJECT_TABLE = "PROJECT_TABLE"

@Entity(
    tableName = PROJECT_TABLE, foreignKeys = [ForeignKey(
        entity = ContractDTO::class,
        parentColumns = arrayOf("contractId"),
        childColumns = arrayOf("contractId"),
        onDelete = ForeignKey.CASCADE
    )], indices = [Index(value = ["projectId"], unique = true)]
)

data class ProjectDTO(
    @PrimaryKey
    @NotNull
    val id: Int,

    @SerializedName("ProjectId")
    @NotNull
    val projectId: String = SqlLitUtils.generateUuid(),

    @SerializedName("Descr")
    val descr: String?,

    @SerializedName("EndDate")
    val endDate: String?,

    @SerializedName("Items")
    val items: ArrayList<ProjectItemDTO>? = arrayListOf(),

    @SerializedName("ProjectCode")
    val projectCode: String?,

    @SerializedName("ProjectMinus")
    val projectMinus: String?,

    @SerializedName("ProjectPlus")
    val projectPlus: String?,

    @SerializedName("Sections")
    val projectSections: ArrayList<ProjectSectionDTO>? = arrayListOf(),

    @SerializedName("VoItems")
    val voItems: ArrayList<VoItemDTO>? = arrayListOf(),

    @SerializedName("ContractId")
    @ColumnInfo(name = "contractId", index = true)
    val contractId: String

) : Serializable
