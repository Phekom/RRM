/*
 * Updated by Shaun McDonald on 2021/01/25
 * Last modified on 2021/01/25 6:30 PM
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 */

package za.co.xisystems.itis_rrm.data.localDB.entities

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class WorkflowEstimateWorkDTO(
    @SerializedName("ActId")
    var actId: Int, // 1
    @SerializedName("EstimateId")
    var estimateId: String, // sample string 3
    @SerializedName("RecordSynchStateId")
    var recordSynchStateId: Int, // 5
    @SerializedName("RecordVersion")
    var recordVersion: Int, // 4
    @SerializedName("TrackRouteId")
    var trackRouteId: String, // sample string 2
    @SerializedName("WorksId")
    var worksId: String // sample string 1
) : Serializable
