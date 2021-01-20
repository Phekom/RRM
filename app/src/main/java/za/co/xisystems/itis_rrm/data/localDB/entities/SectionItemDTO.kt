/*
 * Updated by Shaun McDonald on 2021/22/20
 * Last modified on 2021/01/20 12:47 PM
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 */

package za.co.xisystems.itis_rrm.data.localDB.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Created by Francis Mahlava on 2019/11/22.
 */

const val SECTION_ITEM_TABLE = "SECTION_ITEM_TABLE"

@Entity(
    tableName = SECTION_ITEM_TABLE,
    indices = [Index(value = ["itemCode"], unique = true)]
)

data class SectionItemDTO(
    @PrimaryKey(autoGenerate = false)
    val id: Int,

    var sectionItemId: String,

    @SerializedName("itemCode")
    var itemCode: String,

    @SerializedName("ActivitySections")
    var description: String?
) : Serializable
