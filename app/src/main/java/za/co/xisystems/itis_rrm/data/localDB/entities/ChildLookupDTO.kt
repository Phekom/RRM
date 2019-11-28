package za.co.xisystems.itis_rrm.data.localDB.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Created by Francis Mahlava on 2019/11/21.
 */

@Entity
data class ChildLookupDTO (
    @PrimaryKey
    val look : String
)