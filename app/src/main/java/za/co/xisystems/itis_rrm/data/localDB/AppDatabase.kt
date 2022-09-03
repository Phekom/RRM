/**
 * Updated by Shaun McDonald on 2021/05/19
 * Last modified on 2021/05/18, 16:31
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 **/

@file:Suppress("SpellCheckingInspection")

package za.co.xisystems.itis_rrm.data.localDB

import android.content.Context
import androidx.room.*
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
        ProjectItemDTO::class, ItemDTOTemp::class, //JobDTOTemp::class,
        ContractDTO::class, VoItemDTO::class, ProjectDTO::class, JobTypeEntityDTO::class,
        ProjectSectionDTO::class, PrimaryKeyValueDTO::class, LookupOptionDTO::class,
        LookupDTO::class, ItemSectionDTO::class, WorkFlowDTO::class,
        SectionPointDTO::class, WorkFlowRouteDTO::class, JobSectionDTO::class,
        InfoClassDTO::class, ActivityDTO::class, ToDoGroupsDTO::class,
        JobItemEstimatesPhotoDTO::class, JobItemMeasurePhotoDTO::class, JobItemEstimateDTO::class,
        JobItemMeasureDTO::class, ToDoListEntityDTO::class, ChildLookupDTO::class,
        JobEstimateWorksDTO::class, JobEstimateWorksPhotoDTO::class, SectionItemDTO::class,
        WorkFlowsDTO::class, WfWorkStepDTO::class, UnallocatedPhotoDTO::class, ContractVoDTO::class,
        JobCategoryDTO::class, JobPositionDTO::class, JobDirectionDTO::class,
    ],
    views = [ContractSelectorView::class],
    exportSchema = true,
    autoMigrations = [
        AutoMigration (from = 36, to = 37)
    ],
    version = 37
)

@TypeConverters(Converters::class, DatetimeConverters::class)
@GenerateRoomMigrations

abstract class AppDatabase : RoomDatabase() {

    companion object {
        private const val MAX_DB_VERSIONS = 999_999_999
        private const val DB_NAME = "myRRM_Release.db"

        @Volatile
        internal var instance: AppDatabase? = null
        private val LOCK = Any()
        private var secretphrase: String? = null

        @JvmStatic
        operator fun invoke(context: Context, armoury: XIArmoury) = instance ?: synchronized(LOCK) {
//            val  state=  SQLCipherUtils.getDatabaseState(context,
//                DB_NAME)
            secretphrase = armoury.readPassphrase()
            instance ?: buildDatabase(context).also {
                instance = it
            }
        }

        @JvmStatic
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
                        DB_NAME
                    ).openHelperFactory(factory)
                        .addMigrations(*AppDatabase_Migrations.build())
                        .fallbackToDestructiveMigrationFrom(MAX_DB_VERSIONS).build()
                }
            }

        @JvmStatic
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
    abstract fun getContractVoDao(): ContractVoDao
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
    abstract fun getJobDirectionDao(): JobDirectionDao
    abstract fun getJobCategoryDao(): JobCategoryDao
    abstract fun getJobPositionDao(): JobPositionDao


}
