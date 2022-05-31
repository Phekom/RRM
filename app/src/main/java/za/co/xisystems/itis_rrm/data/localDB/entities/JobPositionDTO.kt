package za.co.xisystems.itis_rrm.data.localDB.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * Created by Francis Mahlava on 2019/11/26.
 */

const val JOB_POSITION_TABLE = "JOB_POSITION_TABLE"
@Entity(tableName = JOB_POSITION_TABLE)
data class JobPositionDTO(
    @SerializedName("JobPosition")
    val jobPosition: String,
    @PrimaryKey
    @SerializedName("JobPositionId")
    val jobPositionId: Int
)