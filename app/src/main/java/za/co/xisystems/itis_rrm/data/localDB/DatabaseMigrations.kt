package za.co.xisystems.itis_rrm.data.localDB

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import timber.log.Timber

object DatabaseMigrations {
    fun build(): Migration {
        val migrations = object : Migration(10, 11) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE JOB_TABLE ADD COLUMN deleted INT NOT NULL DEFAULT 0")
            }
        }
        Timber.d("build called")
        return migrations
    }
}
