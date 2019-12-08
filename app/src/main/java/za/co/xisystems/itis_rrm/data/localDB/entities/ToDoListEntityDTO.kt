package za.co.xisystems.itis_rrm.data.localDB.entities


import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.android.gms.common.util.Base64Utils
import com.google.gson.annotations.SerializedName

const val ToDo_ENTITY_TABLE = "ToDo_ENTITY_TABLE"
//const val TRACK_ROUTE_ID = "TRACK_ROUTE_ID"

@Entity(tableName = ToDo_ENTITY_TABLE)
data class ToDoListEntityDTO(
    @SerializedName("Actionable")
    val actionable: Boolean, // false
    @SerializedName("ActivityId")
    @PrimaryKey
    val activityId: Int, // 3
    @SerializedName("CurrentRouteId")
    val currentRouteId: Int, // 3
    @SerializedName("Data")
    val `data`: String, // hho
    @SerializedName("Description")
    val description: String, // 000000309 - smalltest
    @SerializedName("Entities")
    val entities: ArrayList<ToDoListEntityDTO>,
    @SerializedName("EntityName")
    val entityName: String, // PRJ_JOB
    @SerializedName("Location")
    val location: String, // null
    @SerializedName("PrimaryKeyValues")
    val primaryKeyValues: ArrayList<PrimaryKeyValueDTO>,
    @SerializedName("RecordVersion")
    val recordVersion: Int, // 0
    @SerializedName("TrackRouteId")
    val trackRouteId: String?,
    var jobId: String?,
    var trackRouteIdBytes: ByteArray? = Base64Utils.decode(trackRouteId)


) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ToDoListEntityDTO

        if (actionable != other.actionable) return false
        if (activityId != other.activityId) return false
        if (currentRouteId != other.currentRouteId) return false
        if (`data` != other.`data`) return false
        if (description != other.description) return false
        if (entities != other.entities) return false
        if (entityName != other.entityName) return false
        if (location != other.location) return false
        if (primaryKeyValues != other.primaryKeyValues) return false
        if (recordVersion != other.recordVersion) return false
        if (trackRouteId != other.trackRouteId) return false
        if (trackRouteIdBytes != null) {
            if (other.trackRouteIdBytes == null) return false
            if (!trackRouteIdBytes!!.contentEquals(other.trackRouteIdBytes!!)) return false
        } else if (other.trackRouteIdBytes != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = actionable.hashCode()
        result = 31 * result + activityId
        result = 31 * result + currentRouteId
        result = 31 * result + `data`.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + entities.hashCode()
        result = 31 * result + entityName.hashCode()
        result = 31 * result + location.hashCode()
        result = 31 * result + primaryKeyValues.hashCode()
        result = 31 * result + recordVersion
        result = 31 * result + (trackRouteId?.hashCode() ?: 0)
        result = 31 * result + (trackRouteIdBytes?.contentHashCode() ?: 0)
        return result
    }
}