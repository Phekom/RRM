package za.co.xisystems.itis_rrm.data.models


import com.google.gson.annotations.SerializedName

data class Entity(
    @SerializedName("Actionable")
    val actionable: Boolean,
    @SerializedName("ActivityId")
    val activityId: Int,
    @SerializedName("CurrentRouteId")
    val currentRouteId: Int,
    @SerializedName("Data")
    val `data`: String,
    @SerializedName("Description")
    val description: String,
    @SerializedName("EntitiesDTO")
    val entities: List<Entity>,
    @SerializedName("EntityName")
    val entityName: String,
    @SerializedName("Location")
    val location: Any,
    @SerializedName("PrimaryKeyValues")
    val primaryKeyValues: List<PrimaryKeyValue>,
    @SerializedName("RecordVersion")
    val recordVersion: Int,
    @SerializedName("TrackRouteId")
    val trackRouteId: String
)