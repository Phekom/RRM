package za.co.xisystems.itis_rrm.data.localDB.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * Created by Francis Mahlava on 2019/11/21.
 */


const val LOOKUP_OPTION_TABLE = "LOOKUP_OPTION_TABLE"

@Entity(tableName = LOOKUP_OPTION_TABLE)
class LookupOptionDTO (
    @PrimaryKey
    val id : Int,
    @SerializedName("ContextMember")
    val contextMember: String?,
    @SerializedName("DisplayMember")
    val displayMember: String?,
    @SerializedName("ValueMember")
    val valueMember: String?,
//    val lookupOptions: List<LookupOptionDTO>,
//    @Ignore
    var LookupName: String?

)