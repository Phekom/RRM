package za.co.xisystems.itis_rrm.data.localDB.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * Created by Francis Mahlava on 2019/11/22.
 */

const val SECTION_ITEM_TABLE = "SECTION_ITEM_TABLE"

@Entity(tableName = SECTION_ITEM_TABLE
    , indices = [Index(value = ["itemCode"], unique = true)]
)


data class SectionItemDTO (
//    @SerializedName("SectionId")
//    @@PrimaryKey(autoGenerate = true)
//    val sectionId: String,
//    @ColumnInfo(name = "workflowId", index = true)
    @PrimaryKey
    var sectionItemId: String,

    @ColumnInfo(name = "itemCode")
    var itemCode: String?,
    @SerializedName("ActivitySections")
    var description: String
){
//     fun compareTo(other: SectionItemDTO): Int {
//        return (description ?: "").compareTo(other.description ?: "")
//    }
//    override fun hashCode(): Int {
//        return description?.hashCode() ?: 0
//    }
//
//    override fun equals(other: Any?): Boolean {
//        if (other is SectionItem) {
//            return other.description.equals(description)
//        } else
//            return super.equals(other)
//    }
//
//    override fun toString(): String {
//        return description
//    }
}


