package za.co.xisystems.itis_rrm.data.localDB.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * Created by Francis Mahlava on 2019/11/26.
 */

const val JOB_CATEGORY_TABLE = "JOB_CATEGORY_TABLE"
@Entity(tableName = JOB_CATEGORY_TABLE)
data class JobCategoryDTO(
    @SerializedName("JobCategory")
    val jobCategory: String,
    @PrimaryKey
    @SerializedName("JobCategoryId")
    val jobCategoryId: Int
)