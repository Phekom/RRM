/*
 * Updated by Shaun McDonald on 2021/22/20
 * Last modified on 2021/01/20 1:22 PM
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 */

package za.co.xisystems.itis_rrm.data.localDB.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import za.co.xisystems.itis_rrm.utils.SqlLitUtils
import java.io.Serializable

const val JOB_ITEM_ESTIMATE_PHOTO = "JOB_ITEM_ESTIMATE_PHOTO"

@Entity(tableName = JOB_ITEM_ESTIMATE_PHOTO)
data class JobItemEstimatesPhotoDTO(
    @SerializedName("Descr")
    var descr: String,
    @SerializedName("EstimateId")
    var estimateId: String,
    @SerializedName("Filename")
    var filename: String,
    @SerializedName("PhotoDate")
    var photoDate: String,
    @SerializedName("PhotoId")
    @PrimaryKey
    var photoId: String = SqlLitUtils.generateUuid(),
    @SerializedName("PhotoStart")
    val photoStart: String?,
    @SerializedName("PhotoEnd")
    val photoEnd: String?,
    @SerializedName("Startkm")
    val startKm: Double = 0.0,
    @SerializedName("Endkm")
    val endKm: Double = 0.0,
    @SerializedName("PhotoLatitude")
    var photoLatitude: Double? = 0.0,
    @SerializedName("PhotoLongitude")
    var photoLongitude: Double? = 0.0,

    @SerializedName("PhotoLatitudeEnd")
    var photoLatitudeEnd: Double = 0.0,
    @SerializedName("PhotoLongitudeEnd")
    var photoLongitudeEnd: Double = 0.0,

    @SerializedName("PhotoPath")
    var photoPath: String,
    @SerializedName("RecordSynchStateId")
    val recordSynchStateId: Int,
    @SerializedName("RecordVersion")
    val recordVersion: Int,
    @SerializedName("IsPhotoStart")
    var isPhotostart: Boolean
) : Serializable {

    fun isPhotoStart(): Boolean {
        return isPhotostart
    }

    override fun equals(other: Any?): Boolean {
        return when {
            this === other -> true
            javaClass != other?.javaClass -> false
            else -> {
                other as JobItemEstimatesPhotoDTO
                when {
                    descr != other.descr -> false
                    estimateId != other.estimateId -> false
                    filename != other.filename -> false
                    photoDate != other.photoDate -> false
                    photoId != other.photoId -> false
                    else -> true
                }
            }
        }
    }

    override fun hashCode(): Int {
        var result = photoId.hashCode()
        result = 31 * result + photoPath.hashCode()
        return result
    }
}
