package za.co.xisystems.itis_rrm.data.localDB.entities


import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.android.gms.common.util.Base64Utils
import com.google.gson.annotations.SerializedName

const val ToDo_ENTITY_TABLE = "ToDo_ENTITY_TABLE"
//const val TRACK_ROUTE_ID = "TRACK_ROUTE_ID"

@Entity(tableName = ToDo_ENTITY_TABLE)
data class ToDoListEntityDTO(
    @PrimaryKey
    val id :Int,
    @SerializedName("TrackRouteId")
    val trackRouteId: String?,
    var trackRouteIdBytes: ByteArray? = Base64Utils.decode(trackRouteId!!),

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
    val entities: ArrayList<ToDoListEntityDTO>,
    @SerializedName("EntityName")
    val entityName: String?, // PRJ_JOB
    @SerializedName("Location")
    val location: String?, // null
    @SerializedName("PrimaryKeyValues")
    val primaryKeyValues: ArrayList<PrimaryKeyValueDTO>,
    @SerializedName("RecordVersion")
    val recordVersion: Int?, // 0




    var jobId: String?




)