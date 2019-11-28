package za.co.xisystems.itis_rrm.data.localDB.entities


import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName


@Entity
data class ToDoGroupsDTO(
    @SerializedName("Entities")
    val toDoListEntityDTOS: List<ToDoListEntityDTO>,
    @SerializedName("GroupDescription")
    val groupDescription: String, // Measurements that require Approval for payment processing
    @SerializedName("GroupId")
    @PrimaryKey
    val groupId: String, // MeasureApprove
    @SerializedName("GroupName")
    val groupName: String, // Measurements To Approve
    @SerializedName("SortOrder")
    val sortOrder: Int // 5
)