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
    val primary_key: String?,
    @SerializedName("Value")
    var valueString: String?,

    var trackRouteId: String?,

    @SerializedName("ActivityId")
    val activityId: Int, // 3

//    var valueBytes: ByteArray? = Base64Utils.decode(value)
    var valueBytes: ByteArray?,


    @SerializedName("ValueType")
    val valueType: String?
//    @Ignore
//    @SerializedName("PrimaryKeyValues")
//    val primaryKeyValues: ArrayList<PrimaryKeyValueDTO>
) {
    var value: ByteArray?
        get() = if (valueString == null) valueBytes else Base64Utils.decode(valueString)
        set(value) {
            this.valueBytes = value
            this.valueString = Base64Utils.encode(value)
        }
}

