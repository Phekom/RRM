//package za.co.xisystems.itis_rrm.data.localDB.entities
//
//
//import androidx.room.Entity
//import androidx.room.PrimaryKey
//import com.google.gson.annotations.SerializedName
//
//const val ENTITY_TABLE = "ENTITY_TABLE"
//const val TRACK_ROUTE_ID = "TRACK_ROUTE_ID"
//
//@Entity(tableName = ENTITY_TABLE)
//data class EntitiesDTO(
//    @SerializedName("Actionable")
//    val actionable: Boolean, // true
//    @SerializedName("ActivityId")
//    @PrimaryKey
//    val activityId: Int, // 11
//    @SerializedName("CurrentRouteId")
//    val currentRouteId: Int, // 11
//    @SerializedName("Data")
//    val `data`: String, // null
//    @SerializedName("Description")
//    val description: String, // Measurement - Type 2 (Small) - Quantity : 1
//    @SerializedName("Entities")
//    val entities: ArrayList<EntitiesDTO>,
//    @SerializedName("EntityName")
//    val entityName: String, // PRJ_JOB_ITEM_MEASURE
//    @SerializedName("Location")
//    val location: String, // null
//    @SerializedName("PrimaryKeyValues")
//    val primaryKeyValues: ArrayList<PrimaryKeyValueDTO>,
//    @SerializedName("RecordVersion")
//    val recordVersion: Int, // 0
//    @SerializedName("TrackRouteId")
//    val trackRouteId: String, // lyOwMusPMrHgUxkDAQqGEg==
//
//    var jobId: String?
//
//
//)