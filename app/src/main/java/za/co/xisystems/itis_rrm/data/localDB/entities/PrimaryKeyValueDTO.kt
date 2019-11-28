package za.co.xisystems.itis_rrm.data.localDB.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
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

    var valueBytes: ByteArray?

//    @SerializedName("PrimaryKeyValues")
//    val primaryKeyValues: List<PrimaryKeyValueDTO>
)