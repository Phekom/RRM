package za.co.xisystems.itis_rrm.data.localDB.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * Created by Francis Mahlava on 2019/11/22.
 */

const val SECTION_ITEM_TABLE = "SECTION_ITEM_TABLE"

@Entity(tableName = SECTION_ITEM_TABLE)
data class SectionItemDTO (
//    @SerializedName("SectionId")
//    @PrimaryKey
//    val sectionId: String,
//    @ColumnInfo(name = "workflowId", index = true)
    @PrimaryKey
    var sectionItemId: String,
    var itemCode: String?,
    @SerializedName("ActivitySections")
    var description: String
)


