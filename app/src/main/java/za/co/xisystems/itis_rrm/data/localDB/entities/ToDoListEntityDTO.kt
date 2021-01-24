/*
 * Updated by Shaun McDonald on 2021/22/20
 * Last modified on 2021/01/20 12:46 PM
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 */

package za.co.xisystems.itis_rrm.data.localDB.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import org.springframework.util.Base64Utils
import java.io.Serializable

const val TODO_ENTITY_TABLE = "TODO_ENTITY_TABLE"

@Entity(tableName = TODO_ENTITY_TABLE)
data class ToDoListEntityDTO(
    @PrimaryKey
    val id: Int,
    @SerializedName("TrackRouteId")
    var trackRouteIdString: String?,
    var trackRouteIdBytes: ByteArray?,

    @SerializedName("Actionable")
    val actionable: Boolean, // false
    @SerializedName("ActivityId")
    val activityId: Int, // 3
    @SerializedName("CurrentRouteId")
    val currentRouteId: Int, // 3
    @SerializedName("Data")
    val data: String?, // hho
    @SerializedName("Description")
    val description: String?, // 000000309 - smalltest
    @SerializedName("Entities")
    val entities: ArrayList<ToDoListEntityDTO> = arrayListOf(),
    @SerializedName("EntityName")
    val entityName: String?, // PRJ_JOB
    @SerializedName("Location")
    val location: String?, // null
    @SerializedName("PrimaryKeyValues")
    val primaryKeyValues: ArrayList<PrimaryKeyValueDTO> = arrayListOf(),
    @SerializedName("RecordVersion")
    val recordVersion: Int?, // 0

    var jobId: String?

) : Serializable {
    var trackRouteId: ByteArray?
        get() = if (trackRouteIdString == null) trackRouteIdBytes else Base64Utils.decodeFromString(
            trackRouteIdString
        )
        set(trackRouteId) {
            this.trackRouteIdBytes = trackRouteId
            this.trackRouteIdString = Base64Utils.encode(trackRouteId).toString()
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ToDoListEntityDTO

        if (trackRouteIdBytes != null) {
            if (other.trackRouteIdBytes == null) return false
            if (!trackRouteIdBytes.contentEquals(other.trackRouteIdBytes)) return false
        } else if (other.trackRouteIdBytes != null) return false
        if (data != other.data) return false
        if (description != other.description) return false
        if (entities != other.entities) return false

        return true
    }

    override fun hashCode(): Int {
        var result = trackRouteIdBytes?.contentHashCode() ?: 0
        result = 31 * result + (data?.hashCode() ?: 0)
        result = 31 * result + (description?.hashCode() ?: 0)
        result = 31 * result + entities.hashCode()
        return result
    }
}
