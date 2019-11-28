package za.co.xisystems.itis_rrm.data.jj


import com.google.gson.annotations.SerializedName
import za.co.xisystems.itis_rrm.data.models.ToDoGroups

data class ToDoListGroupsResponse(
    @SerializedName("ErrorMessage")
    val errorMessage: Any, // null
    @SerializedName("ToDoListGroups")
    val toDoListGroups: List<ToDoGroups>
)