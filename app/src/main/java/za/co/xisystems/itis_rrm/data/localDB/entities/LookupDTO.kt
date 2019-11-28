package za.co.xisystems.itis_rrm.data.localDB.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Created by Francis Mahlava on 2019/11/21.
 */


const val LOOKUP_TABLE = "LOOKUP_TABLE"

@Entity(tableName = LOOKUP_TABLE)
class LookupDTO (
    val childLookups: List<ChildLookupDTO>?,
    @PrimaryKey
    val lookupName: String,
    val lookupOptions: List<LookupOptionDTO>?
)
