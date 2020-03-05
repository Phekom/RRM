package za.co.xisystems.itis_rrm.data.localDB.entities


import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

const val TODO_GROUPS_TABLE = "TODO_GROUPS_TABLE"
@Entity(
    tableName = TODO_GROUPS_TABLE
//    , foreignKeys = [
//        ForeignKey(
//            entity = ProjectDTO::class,
//            parentColumns = arrayOf("projectId"),
//            childColumns = arrayOf("projectId"),
//            onDelete = ForeignKey.NO_ACTION
//        )
//    ]
)
data class ToDoGroupsDTO(

    @SerializedName("GroupId")
    @PrimaryKey
    val groupId: String, // MeasureApprove
    @SerializedName("GroupName")
    val groupName: String, // Measurements To Approve

    @SerializedName("GroupDescription")
    val groupDescription: String, // Measurements that require Approval for payment processing

    @SerializedName("Entities")
    val toDoListEntities: ArrayList<ToDoListEntityDTO>,

    @SerializedName("SortOrder")
    val sortOrder: Int // 5
)