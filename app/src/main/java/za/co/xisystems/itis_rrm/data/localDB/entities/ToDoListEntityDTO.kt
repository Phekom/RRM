package za.co.xisystems.itis_rrm.data.localDB.entities


import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity
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
    @SerializedName("EntitiesDTO")
    val entityDTOS: List<ToDoListEntityDTO>,
    @SerializedName("EntityName")
    val entityName: String, // PRJ_JOB
    @SerializedName("Location")
    val location: String, // null
    @SerializedName("PrimaryKeyValues")
    val primaryKeyValues: List<PrimaryKeyValueDTO>,
    @SerializedName("RecordVersion")
    val recordVersion: Int, // 0
    @SerializedName("TrackRouteId")
    val trackRouteId: String // lyG8L/N/fn/gUxkDAQrgFA==
)