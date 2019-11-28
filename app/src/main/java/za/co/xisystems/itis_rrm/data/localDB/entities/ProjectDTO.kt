package za.co.xisystems.itis_rrm.data.localDB.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * Created by Francis Mahlava on 2019/11/22.
 */

const val PROJECT_TABLE = "PROJECT_TABLE"

@Entity(tableName = PROJECT_TABLE)
data class ProjectDTO(

    @SerializedName("ProjectId")
    @PrimaryKey
    val projectId: String,

    @SerializedName("Descr")
    val descr: String?,

    @SerializedName("EndDate")
    val endDate: String?,

    @SerializedName("Items")
    val items: List<ItemDTO>?,

    @SerializedName("ProjectCode")
    val projectCode: String?,

    @SerializedName("ProjectMinus")
    val projectMinus: String?,

    @SerializedName("ProjectPlus")
    val projectPlus: String?,

    @SerializedName("Sections")
    val sections: List<SectionDTO>?,

    @SerializedName("VoItems")
    val voItems: List<VoItemDTO>?,

    var contractId: String?

)

