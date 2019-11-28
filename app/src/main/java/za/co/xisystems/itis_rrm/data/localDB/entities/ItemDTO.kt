package za.co.xisystems.itis_rrm.data.localDB.entities


import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName


/**
 * Created by Francis Mahlava on 2019/11/21.
 */

const val PROJECT_ITEM_TABLE = "PROJECT_ITEM_TABLE"


@Entity(tableName = PROJECT_ITEM_TABLE)
data class ItemDTO(

    @SerializedName("ItemId")
    @PrimaryKey
    val itemId: String,
    @SerializedName("Descr")
    val descr: String,
    @SerializedName("ItemCode")
    val itemCode: String,

    @SerializedName("ItemSections")
    val itemSections: List<ItemSectionDTO>,
    @SerializedName("ProjectId")
    val projectId: String,
    @SerializedName("TenderRate")
    val tenderRate: Double,
    @SerializedName("Uom")
    val uom: String,
    @SerializedName("WorkflowId")
    val workflowId: Int,

    val sectionItemId: String,

    val quantity: Double = 0.toDouble(),

   val estimateId: String

)