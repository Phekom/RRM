package za.co.xisystems.itis_rrm.data.localDB.entities

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Created by Francis Mahlava on 2019/11/26.
 */

const val SECTION_POINT_TABLE = "SECTION_POINT_TABLE"

@Entity(tableName = SECTION_POINT_TABLE)
data class SectionPointDTO(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    @SerializedName("SectionId")
    val sectionId: Int, // 4
    @SerializedName("Direction")
    val direction: String?, // N
    @SerializedName("LinearId")
    val linearId: String?, // N001
    @SerializedName("PointLocation")
    val pointLocation: Double, // 81.678365

    @SerializedName("projectId")
    val projectId: String?,
    @SerializedName("jobId")
    val jobId: String?

) : Serializable, Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readDouble(),
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeInt(sectionId)
        parcel.writeString(direction)
        parcel.writeString(linearId)
        parcel.writeDouble(pointLocation)
        parcel.writeString(projectId)
        parcel.writeString(jobId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Creator<SectionPointDTO> {
        override fun createFromParcel(parcel: Parcel): SectionPointDTO {
            return SectionPointDTO(parcel)
        }

        override fun newArray(size: Int): Array<SectionPointDTO?> {
            return arrayOfNulls(size)
        }
    }
}
