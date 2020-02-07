package za.co.xisystems.itis_rrm.data.localDB.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.fasterxml.jackson.annotation.JsonProperty
import com.google.gson.annotations.SerializedName

/**
 * Created by Francis Mahlava on 2019/11/21.
 */


const val LOOKUP_OPTION_TABLE = "LOOKUP_OPTION_TABLE"
@Entity(
    tableName = LOOKUP_OPTION_TABLE, foreignKeys = arrayOf(
        ForeignKey(
            entity = LookupDTO::class,
            parentColumns = arrayOf("lookupName"),
            childColumns = arrayOf("lookupName"),
            onDelete = ForeignKey.CASCADE
        )
    )
)
class LookupOptionDTO(

    @PrimaryKey
    val id: Int = 0,

    @SerializedName("ValueMember")
    val valueMember: String?, // 3920

    @SerializedName("DisplayMember")
    val displayMember: String?, // Kallie Niebuhr

    @SerializedName("ContextMember")
    val contextMember: String?,

    @SerializedName("LookupName")
    @ColumnInfo(name = "lookupName", index = true)
    var lookupName: String

)