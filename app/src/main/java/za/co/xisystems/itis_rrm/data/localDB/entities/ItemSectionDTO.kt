/*
 * Updated by Shaun McDonald on 2021/22/20
 * Last modified on 2021/01/20 12:46 PM
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 */

package za.co.xisystems.itis_rrm.data.localDB.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Created by Francis Mahlava on 2019/11/26.
 */

const val ITEM_SECTION_TABLE = "ITEM_SECTION_TABLE"

@Entity(tableName = ITEM_SECTION_TABLE)
data class ItemSectionDTO(

    @SerializedName("SectionId")
    @PrimaryKey
    val sectionId: String,
    @SerializedName("Direction")
    val direction: String?,
    @SerializedName("EndKm")
    val endKm: Double,
    @SerializedName("ProjectId")
    val projectId: String?,
    @SerializedName("Route")
    val route: String?,
    @SerializedName("Section")
    val section: String?,

    @SerializedName("StartKm")
    val startKm: Double
) : Serializable
