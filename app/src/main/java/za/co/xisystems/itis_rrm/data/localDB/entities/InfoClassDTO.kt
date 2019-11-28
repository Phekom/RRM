package za.co.xisystems.itis_rrm.data.localDB.entities


import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName


@Entity
data class InfoClassDTO(
    @PrimaryKey
    val sInfoClassId: String,
    val sLinkId: String,
    @SerializedName("WfId")
    val wfId: Int
)