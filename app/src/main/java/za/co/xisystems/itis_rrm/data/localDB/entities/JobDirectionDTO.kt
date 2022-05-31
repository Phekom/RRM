package za.co.xisystems.itis_rrm.data.localDB.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * Created by Francis Mahlava on 2019/11/26.
 */

const val JOB_DIRECTION_TABLE = "JOB_DIRECTION_TABLE"
@Entity(tableName = JOB_DIRECTION_TABLE)
data class JobDirectionDTO(
    @SerializedName("JobDirection")
    val jobDirection: String?,
    @PrimaryKey
    @SerializedName("JobDirectionId")
    val jobDirectionId: Int
)