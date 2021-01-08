package za.co.xisystems.itis_rrm.data.localDB.entities

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import org.springframework.util.Base64Utils
import java.io.Serializable

const val TODO_ENTITY_TABLE = "TODO_ENTITY_TABLE"

@Entity(tableName = TODO_ENTITY_TABLE)
data class ToDoListEntityDTO(
    @PrimaryKey
    val id: Int,
    @SerializedName("TrackRouteId")
    var trackRouteIdString: String?,
    var trackRouteIdBytes: ByteArray?,

    @SerializedName("Actionable")
    val actionable: Boolean, // false
    @SerializedName("ActivityId")
    val activityId: Int, // 3
    @SerializedName("CurrentRouteId")
    val currentRouteId: Int, // 3
    @SerializedName("Data")
    val data: String?, // hho
    @SerializedName("Description")
    val description: String?, // 000000309 - smalltest
    @SerializedName("Entities")
    val entities: ArrayList<ToDoListEntityDTO>,
    @SerializedName("EntityName")
    val entityName: String?, // PRJ_JOB
    @SerializedName("Location")
    val location: String?, // null
    @SerializedName("PrimaryKeyValues")
    val primaryKeyValues: ArrayList<PrimaryKeyValueDTO>,
    @SerializedName("RecordVersion")
    val recordVersion: Int?, // 0

    var jobId: String?

) : Serializable, Parcelable {
    var trackRouteId: ByteArray?
        get() = if (trackRouteIdString == null) trackRouteIdBytes else Base64Utils.decodeFromString(
            trackRouteIdString
        )
        set(trackRouteId) {
            this.trackRouteIdBytes = trackRouteId
            this.trackRouteIdString = Base64Utils.encode(trackRouteId).toString()
        }

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString(),
        parcel.createByteArray(),
        parcel.readByte() != 0.toByte(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        TODO("entities"),
        parcel.readString(),
        parcel.readString(),
        TODO("primaryKeyValues"),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString()
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ToDoListEntityDTO

        if (trackRouteIdBytes != null) {
            if (other.trackRouteIdBytes == null) return false
            if (!trackRouteIdBytes.contentEquals(other.trackRouteIdBytes)) return false
        } else if (other.trackRouteIdBytes != null) return false
        if (data != other.data) return false
        if (description != other.description) return false
        if (entities != other.entities) return false

        return true
    }

    override fun hashCode(): Int {
        var result = trackRouteIdBytes?.contentHashCode() ?: 0
        result = 31 * result + (data?.hashCode() ?: 0)
        result = 31 * result + (description?.hashCode() ?: 0)
        result = 31 * result + entities.hashCode()
        return result
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(trackRouteIdString)
        parcel.writeByteArray(trackRouteIdBytes)
        parcel.writeByte(if (actionable) 1 else 0)
        parcel.writeInt(activityId)
        parcel.writeInt(currentRouteId)
        parcel.writeString(data)
        parcel.writeString(description)
        parcel.writeString(entityName)
        parcel.writeString(location)
        parcel.writeValue(recordVersion)
        parcel.writeString(jobId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Creator<ToDoListEntityDTO> {
        override fun createFromParcel(parcel: Parcel): ToDoListEntityDTO {
            return ToDoListEntityDTO(parcel)
        }

        override fun newArray(size: Int): Array<ToDoListEntityDTO?> {
            return arrayOfNulls(size)
        }
    }
}
