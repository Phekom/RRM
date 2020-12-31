package za.co.xisystems.itis_rrm.data.localDB.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable

const val INFO_CLASS_TABLE = "INFO_CLASS_TABLE"

@Entity(tableName = INFO_CLASS_TABLE)
data class InfoClassDTO(

    @PrimaryKey
    val sLinkId: String,

    val sInfoClassId: String?,

    @SerializedName("WfId")
    val wfId: Int?
) : Serializable
