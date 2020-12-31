package za.co.xisystems.itis_rrm.data.localDB

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val migration_1_2: Migration
    get() = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE JOB_ITEM_MEASURE ADD COLUMN deleted INT NOT NULL DEFAULT 0")
        }
    }
