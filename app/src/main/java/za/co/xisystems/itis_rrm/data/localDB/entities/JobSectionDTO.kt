package za.co.xisystems.itis_rrm.data.localDB.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * Created by Francis Mahlava on 2019/11/26.
 */

const val JOB_SECTION_TABLE = "JOB_SECTION_TABLE"

@Entity(tableName = JOB_SECTION_TABLE)
class JobSectionDTO(
    @SerializedName("EndKm")
    val endKm: Double,
    @SerializedName("JobId")
    val jobId: String,
    @SerializedName("JobSectionId")
    @PrimaryKey
    val jobSectionId: String,
    @SerializedName("PrjJobDto")
    val prjJobDto: ArrayList<JobDTO>?,
    @SerializedName("ProjectSectionId")
    val projectSectionId: String,
    @SerializedName("RecordSynchStateId")
    val recordSynchStateId: Int,
    @SerializedName("RecordVersion")
    val recordVersion: Int,
    @SerializedName("StartKm")
    val startKm: Double
)