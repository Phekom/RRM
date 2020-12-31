package za.co.xisystems.itis_rrm.data.localDB

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// Soft delete of jobs for Decline Job functionality
val migration_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE JOB_TABLE ADD COLUMN deleted INT NOT NULL DEFAULT 0")
    }
}