/*
 * Updated by Shaun McDonald on 2021/01/25
 * Last modified on 2021/01/25 6:30 PM
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 */

package za.co.xisystems.itis_rrm.data.localDB.entities

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Created by Francis Mahlava on 2020/02/04.
 */
class WorkflowJobSectionDTO(

    @SerializedName("JobId")
    var jobId: String? = null,

    @SerializedName("JobSectionId")
    val jobSectionId: String? = null,

    @SerializedName("ProjectSectionId")
    val projectSectionId: String? = null,

    @SerializedName("RecordVersion")
    val recordVersion: Int = 0,

    @SerializedName("RecordSynchStateId")
    val recordSynchStateId: Int = 0,

    @SerializedName("StartKm")
    val startKm: Double = 0.0,

    @SerializedName("EndKm")
    val endKm: Double = 0.0

) : Serializable
