/*
 * Updated by Shaun McDonald on 2021/01/25
 * Last modified on 2021/01/25 6:30 PM
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 */

package za.co.xisystems.itis_rrm.data.localDB.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable

const val WORKFLOWs_TABLE = "WORKFLOWs_TABLE"

@Entity(tableName = WORKFLOWs_TABLE)
data class WorkFlowsDTO(
    @SerializedName("Activities")
    @PrimaryKey
    val activities: ArrayList<ActivityDTO> = ArrayList(),
    @SerializedName("InfoClasses")
    val infoClasses: ArrayList<InfoClassDTO> = ArrayList(),
    @SerializedName("Workflows")
    val workflows: ArrayList<WorkFlowDTO> = ArrayList()
) : Serializable
