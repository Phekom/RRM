/**
 * Updated by Shaun McDonald on 2021/05/19
 * Last modified on 2021/05/18, 16:31
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 **/

@file:Suppress("SpellCheckingInspection")

package za.co.xisystems.itis_rrm.data.localDB

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import dev.matrix.roomigrant.GenerateRoomMigrations
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import za.co.xisystems.itis_rrm.BuildConfig
import za.co.xisystems.itis_rrm.data.localDB.dao.*
import za.co.xisystems.itis_rrm.data.localDB.entities.*
import za.co.xisystems.itis_rrm.data.localDB.views.ContractSelectorView
import za.co.xisystems.itis_rrm.forge.XIArmoury
import za.co.xisystems.itis_rrm.utils.Converters
import za.co.xisystems.itis_rrm.utils.DatetimeConverters

/**
 * Created by Francis Mahlava on 2019/10/23., exportSchema = false
 */

@Database(
    entities = [
        JobDTO::class, UserDTO::class, UserRoleDTO::class,
        ProjectItemDTO::class, ItemDTOTemp::class, JobDTOTemp::class,
        ContractDTO::class, VoItemDTO::class, ProjectDTO::class, JobTypeEntityDTO::class,
        ProjectSectionDTO::class, PrimaryKeyValueDTO::class, LookupOptionDTO::class,
        LookupDTO::class, ItemSectionDTO::class, WorkFlowDTO::class,
        SectionPointDTO::class, WorkFlowRouteDTO::class, JobSectionDTO::class,
        InfoClassDTO::class, ActivityDTO::class, ToDoGroupsDTO::class,
        JobItemEstimatesPhotoDTO::class, JobItemMeasurePhotoDTO::class, JobItemEstimateDTO::class,
        JobItemMeasureDTO::class, ToDoListEntityDTO::class, ChildLookupDTO::class,
        JobEstimateWorksDTO::class, JobEstimateWorksPhotoDTO::class, SectionItemDTO::class,
        WorkFlowsDTO::class, WfWorkStepDTO::class, UnallocatedPhotoDTO::class
    ],
    views = [ContractSelectorView::class],
    exportSchema = true,
    version = 27
)

@TypeConverters(Converters::class, DatetimeConverters::class)
@GenerateRoomMigrations

abstract class AppDatabase : RoomDatabase() {

    companion object {
        private const val MAX_DB_VERSIONS = 999_999_999

        @Volatile
        private var instance: AppDatabase? = null
        private val LOCK = Any()
        private var secretphrase: String? = null
        operator fun invoke(context: Context, armoury: XIArmoury) = instance ?: synchronized(LOCK) {
            secretphrase = armoury.readPassphrase()
            instance ?: buildDatabase(context).also {
                instance = it
            }
        }

        private fun buildDatabase(context: Context) =
            when (BuildConfig.DEBUG) {
                true -> {
                    // Unencrypted DB for Dev, Tracing and Testing
                    Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "myRRM_Development.db"
                    ).addMigrations(*AppDatabase_Migrations.build())
                        .fallbackToDestructiveMigrationFrom(MAX_DB_VERSIONS).build()
                }
                else -> {
                    // Encrypted DB with one-time generated passphrase
                    val passphrase: ByteArray =
                        SQLiteDatabase.getBytes(
                            secretphrase!!.toCharArray()
                        )
                    val factory = SupportFactory(passphrase, null, false)
                    Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "myRRM_Release.db"
                    ).openHelperFactory(factory)
                        .addMigrations(*AppDatabase_Migrations.build())
                        .fallbackToDestructiveMigrationFrom(MAX_DB_VERSIONS).build()
                }
            }

        fun closeDown() {
            if (instance?.isOpen == true) {
                instance?.close()
            }
            instance = null
        }
    }

    abstract fun getJobDao(): JobDao
    abstract fun getJobSectionDao(): JobSectionDao
    abstract fun getJobItemEstimateDao(): JobItemEstimateDao
    abstract fun getJobItemMeasureDao(): JobItemMeasureDao
    abstract fun getJobItemEstimatePhotoDao(): JobItemEstimatePhotoDao
    abstract fun getJobItemMeasurePhotoDao(): JobItemMeasurePhotoDao
    abstract fun getUserDao(): UserDao
    abstract fun getUserRoleDao(): UserRoleDao
    abstract fun getContractDao(): ContractDao
    abstract fun getVoItemDao(): VoItemDao
    abstract fun getProjectDao(): ProjectDao
    abstract fun getPrimaryKeyValueDao(): PrimaryKeyValueDao
    abstract fun getLookupOptionDao(): LookupOptionDao
    abstract fun getLookupDao(): LookupDao
    abstract fun getEntitiesDao(): EntitiesDao
    abstract fun getProjectItemDao(): ProjectItemDao
    abstract fun getProjectSectionDao(): ProjectSectionDao
    abstract fun getWorkFlowDao(): WorkFlowDao
    abstract fun getWorkFlowRouteDao(): WorkFlowRouteDao
    abstract fun getWorkflowsDao(): WorkflowsDao
    abstract fun getInfoClassDao(): InfoClassDao
    abstract fun getActivityDao(): ActivityDao
    abstract fun getToDoGroupsDao(): ToDoGroupsDao
    abstract fun getEstimateWorkDao(): EstimateWorkDao
    abstract fun getEstimateWorkPhotoDao(): EstimateWorkPhotoDao
    abstract fun getSectionItemDao(): SectionItemDao
    abstract fun getItemDaoTemp(): ItemDaoTemp
    abstract fun getJobTypeDao(): JobTypeDao
    abstract fun getSectionPointDao(): SectionPointDao
    abstract fun getWorkStepDao(): WorkStepDao
    abstract fun getUnallocatedPhotoDao(): UnallocatedPhotoDao
}
