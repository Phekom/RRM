package za.co.xisystems.itis_rrm.data.localDB.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.android.gms.common.util.Base64Utils
import com.google.gson.annotations.SerializedName

/**
 * Created by Francis Mahlava on 2019/11/22.
 */

const val PRIMARY_KEY_VALUE_TABLE = "PRIMARY_KEY_VALUE_TABLE"

@Entity(tableName = PRIMARY_KEY_VALUE_TABLE)
data class PrimaryKeyValueDTO(
    @PrimaryKey
    val id: Int,
    @SerializedName("Key")
    val key: String?,
    @SerializedName("Value")
    val value: String?,
    @SerializedName("ValueType")
    val valueType: String?,
//    @Ignore
    var trackRouteId: String?,

    var valueBytes: ByteArray? = Base64Utils.decode(value)

//    @SerializedName("PrimaryKeyValues")
//    val primaryKeyValues: ArrayList<PrimaryKeyValueDTO>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PrimaryKeyValueDTO

        if (id != other.id) return false
        if (key != other.key) return false
        if (value != other.value) return false
        if (valueType != other.valueType) return false
        if (trackRouteId != other.trackRouteId) return false
        if (valueBytes != null) {
            if (other.valueBytes == null) return false
            if (!valueBytes!!.contentEquals(other.valueBytes!!)) return false
        } else if (other.valueBytes != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + (key?.hashCode() ?: 0)
        result = 31 * result + (value?.hashCode() ?: 0)
        result = 31 * result + (valueType?.hashCode() ?: 0)
        result = 31 * result + (trackRouteId?.hashCode() ?: 0)
        result = 31 * result + (valueBytes?.contentHashCode() ?: 0)
        return result
    }
}