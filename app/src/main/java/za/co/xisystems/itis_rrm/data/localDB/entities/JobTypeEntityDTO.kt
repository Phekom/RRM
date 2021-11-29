package za.co.xisystems.itis_rrm.data.localDB.entities

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable

const val JobType_TABLE = "JobType_TABLE"

@Entity(tableName = JobType_TABLE)
class JobTypeEntityDTO(
    @PrimaryKey
    @SerializedName("IdValue")
    var typeId: Int,
    @SerializedName("DisplayValue")
    val description: String // Public
) : Serializable, Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(typeId)
        parcel.writeString(description)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<JobTypeEntityDTO> {
        override fun createFromParcel(parcel: Parcel): JobTypeEntityDTO {
            return JobTypeEntityDTO(parcel)
        }

        override fun newArray(size: Int): Array<JobTypeEntityDTO?> {
            return arrayOfNulls(size)
        }
    }
}
