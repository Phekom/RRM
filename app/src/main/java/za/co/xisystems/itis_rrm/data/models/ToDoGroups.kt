package za.co.xisystems.itis_rrm.data.models


import com.google.gson.annotations.SerializedName

data class ToDoGroups(
    @SerializedName("EntitiesDTO")
    val entities: List<Entity>,
    @SerializedName("GroupDescription")
    val groupDescription: String,
    @SerializedName("GroupId")
    val groupId: String,
    @SerializedName("GroupName")
    val groupName: String,
    @SerializedName("SortOrder")
    val sortOrder: Int
)