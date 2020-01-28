package za.co.xisystems.itis_rrm.data.localDB.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Created by Francis Mahlava on 2019/11/26.
 */

const val JOB_SECTION_TABLE = "JOB_SECTION_TABLE"

@Entity(tableName = JOB_SECTION_TABLE)
class JobSectionDTO(
    @SerializedName("JobSectionId")
    @PrimaryKey
    var jobSectionId: String,
    @SerializedName("ProjectSectionId")
    var projectSectionId: String?,
    @SerializedName("JobId")
    var jobId: String?,
    @SerializedName("StartKm")
    val startKm: Double,
    @SerializedName("EndKm")
    val endKm: Double,
    @SerializedName("PrjJobDto")
    val job: JobDTO?,
    @SerializedName("RecordSynchStateId")
    val recordSynchStateId: Int,
    @SerializedName("RecordVersion")
    val recordVersion: Int

): Serializable