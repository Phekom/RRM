package za.co.xisystems.itis_rrm.data.localDB.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * Created by Francis Mahlava on 2019/11/21.
 */

const val TABLE_JOB_VO_ITEM = "JOB_VO_ITEM"
const val PROJECT_VO_ID = 0

@Entity(tableName = TABLE_JOB_VO_ITEM)
data class VoItemDTO(

    @SerializedName("ContractVoId")
    val contractVoId: String,
    @SerializedName("ContractVoItemId")
    val contractVoItemId: String,
    @SerializedName("Descr")
    val descr: String,
    @SerializedName("ItemCode")
    val itemCode: String,
    @SerializedName("ProjectId")
    val projectId: String,
    @SerializedName("ProjectItemId")
    val projectItemId: String,
    @SerializedName("ProjectVoId")
    val projectVoId: String,
    @SerializedName("Rate")
    val rate: Double,
    @SerializedName("Uom")
    val uom: String,
    @SerializedName("VoDescr")
    val voDescr: String


){

    @PrimaryKey(autoGenerate = true)
    var voItemId: Int = PROJECT_VO_ID
}