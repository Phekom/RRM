/*
 * Updated by Shaun McDonald on 2021/01/25
 * Last modified on 2021/01/25 6:30 PM
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 */

package za.co.xisystems.itis_rrm.data.localDB.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * Created by Francis Mahlava on 2020/02/12.
 */

const val WorkStep_TABLE = "WorkStep_TABLE"

@Entity(tableName = WorkStep_TABLE)
class WfWorkStepDTO(
    @SerializedName("WorkStep_ID")
    @PrimaryKey
    val workStepId: Int,
    @SerializedName("Step_Code")
    val stepCode: String?,
    @SerializedName("DESCR")
    val descrip: String?,
    @SerializedName("ACT_TYPE_ID")
    val actTypeId: Int?
)
