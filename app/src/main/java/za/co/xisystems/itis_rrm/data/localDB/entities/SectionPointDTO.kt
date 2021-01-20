/*
 * Updated by Shaun McDonald on 2021/22/20
 * Last modified on 2021/01/20 12:55 PM
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

const val SECTION_POINT_TABLE = "SECTION_POINT_TABLE"

@Entity(tableName = SECTION_POINT_TABLE)
data class SectionPointDTO(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    @SerializedName("SectionId")
    val sectionId: Int, // 4
    @SerializedName("Direction")
    val direction: String?, // N
    @SerializedName("LinearId")
    val linearId: String?, // N001
    @SerializedName("PointLocation")
    val pointLocation: Double, // 81.678365

    @SerializedName("projectId")
    val projectId: String?,
    @SerializedName("jobId")
    val jobId: String?

) : Serializable
