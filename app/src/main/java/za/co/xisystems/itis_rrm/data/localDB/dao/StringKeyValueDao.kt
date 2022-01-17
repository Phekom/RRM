package za.co.xisystems.itis_rrm.data.localDB.dao

import androidx.annotation.NonNull
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import za.co.xisystems.itis_rrm.data.localDB.entities.StringKeyValuePair

@Dao
interface StringKeyValueDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(keyValueData: StringKeyValuePair)

    @Query("SELECT * FROM StringKeyValuePair WHERE `key` = :key LIMIT 1")
    fun get(@NonNull key: String): StringKeyValuePair?

    @Query("DELETE FROM StringKeyValuePair WHERE `key` = :key")
    fun delete(@NonNull key: String)

    @Query("DELETE FROM StringKeyValuePair")
    fun clear()

    @Query("SELECT * FROM StringKeyValuePair")
    fun getAll(): List<StringKeyValuePair>?
}
