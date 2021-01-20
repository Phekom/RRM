/*
 * Updated by Shaun McDonald on 2021/22/20
 * Last modified on 2021/01/20 12:55 PM
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 */

package za.co.xisystems.itis_rrm.data.localDB.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import za.co.xisystems.itis_rrm.utils.SqlLitUtils
import java.io.Serializable

/**
 * Created by Francis Mahlava on 2019/11/26.
 */

const val JOB_ESTIMATE_WORKS = "JOB_ESTIMATE_WORKS"

@Entity(tableName = JOB_ESTIMATE_WORKS)
class JobEstimateWorksDTO(
    @SerializedName("ActId")
    var actId: Int,
    @SerializedName("EstimateId")
    var estimateId: String?,
    @SerializedName("MobileJobEstimateWorksPhotos")
    var jobEstimateWorksPhotos: ArrayList<JobEstimateWorksPhotoDTO>?,
    @SerializedName("RecordSynchStateId")
    var recordSynchStateId: Int,
    @SerializedName("RecordVersion")
    var recordVersion: Int,
    @SerializedName("TrackRouteId")
    var trackRouteId: String,
    @SerializedName("WorksId")
    @PrimaryKey
    var worksId: String = SqlLitUtils.generateUuid()
) : Serializable
