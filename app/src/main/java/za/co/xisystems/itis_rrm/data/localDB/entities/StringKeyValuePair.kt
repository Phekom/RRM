package za.co.xisystems.itis_rrm.data.localDB.entities

import androidx.room.Entity

@Entity(primaryKeys = ["key"])
data class StringKeyValuePair(
    val key: String, val value: String
)