package za.co.xisystems.itis_rrm.data.localDB.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * Created by Francis Mahlava on 2019/11/21.
 */

const val LOOKUP_TABLE = "LOOKUP_TABLE"

@Entity(tableName = LOOKUP_TABLE)
class LookupDTO(
//    @SerializedName("ChildLookups")
//    val childLookups: ArrayList<ChildLookupDTO>? = null,
    @SerializedName("LookupName")
    @PrimaryKey
    val lookupName: String,
    @SerializedName("LookupOptions")
    val lookupOptions: ArrayList<LookupOptionDTO>? = arrayListOf()

)
