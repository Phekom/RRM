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
import timber.log.Timber
import za.co.xisystems.itis_rrm.BuildConfig
import za.co.xisystems.itis_rrm.data.localDB.dao.ActivityDao
import za.co.xisystems.itis_rrm.data.localDB.dao.ContractDao
import za.co.xisystems.itis_rrm.data.localDB.dao.EntitiesDao
import za.co.xisystems.itis_rrm.data.localDB.dao.EstimateWorkDao
import za.co.xisystems.itis_rrm.data.localDB.dao.EstimateWorkPhotoDao
import za.co.xisystems.itis_rrm.data.localDB.dao.InfoClassDao
import za.co.xisystems.itis_rrm.data.localDB.dao.ItemDaoTemp
import za.co.xisystems.itis_rrm.data.localDB.dao.ItemSectionDao
import za.co.xisystems.itis_rrm.data.localDB.dao.JobDao
import za.co.xisystems.itis_rrm.data.localDB.dao.JobDaoTemp
import za.co.xisystems.itis_rrm.data.localDB.dao.JobItemEstimateDao
import za.co.xisystems.itis_rrm.data.localDB.dao.JobItemEstimatePhotoDao
import za.co.xisystems.itis_rrm.data.localDB.dao.JobItemMeasureDao
import za.co.xisystems.itis_rrm.data.localDB.dao.JobItemMeasurePhotoDao
import za.co.xisystems.itis_rrm.data.localDB.dao.JobSectionDao
import za.co.xisystems.itis_rrm.data.localDB.dao.LookupDao
import za.co.xisystems.itis_rrm.data.localDB.dao.LookupOptionDao
import za.co.xisystems.itis_rrm.data.localDB.dao.PrimaryKeyValueDao
import za.co.xisystems.itis_rrm.data.localDB.dao.ProjectDao
import za.co.xisystems.itis_rrm.data.localDB.dao.ProjectItemDao
import za.co.xisystems.itis_rrm.data.localDB.dao.ProjectSectionDao
import za.co.xisystems.itis_rrm.data.localDB.dao.SectionItemDao
import za.co.xisystems.itis_rrm.data.localDB.dao.SectionPointDao
import za.co.xisystems.itis_rrm.data.localDB.dao.ToDoGroupsDao
import za.co.xisystems.itis_rrm.data.localDB.dao.UserDao
import za.co.xisystems.itis_rrm.data.localDB.dao.UserRoleDao
import za.co.xisystems.itis_rrm.data.localDB.dao.VoItemDao
import za.co.xisystems.itis_rrm.data.localDB.dao.WorkFlowDao
import za.co.xisystems.itis_rrm.data.localDB.dao.WorkFlowRouteDao
import za.co.xisystems.itis_rrm.data.localDB.dao.WorkStepDao
import za.co.xisystems.itis_rrm.data.localDB.dao.WorkflowsDao
import za.co.xisystems.itis_rrm.data.localDB.entities.ActivityDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.ChildLookupDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.ContractDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.InfoClassDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.ItemDTOTemp
import za.co.xisystems.itis_rrm.data.localDB.entities.ItemSectionDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTOTemp
import za.co.xisystems.itis_rrm.data.localDB.entities.JobEstimateWorksDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobEstimateWorksPhotoDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimateDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimatesPhotoDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemMeasureDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemMeasurePhotoDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobSectionDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.LookupDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.LookupOptionDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.PrimaryKeyValueDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.ProjectDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.ProjectItemDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.ProjectSectionDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.SectionItemDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.SectionPointDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.ToDoGroupsDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.ToDoListEntityDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.UserDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.UserRoleDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.VoItemDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.WfWorkStepDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.WorkFlowDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.WorkFlowRouteDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.WorkFlowsDTO
import za.co.xisystems.itis_rrm.data.localDB.views.ContractSelectorView
import za.co.xisystems.itis_rrm.forge.XIArmoury
import za.co.xisystems.itis_rrm.utils.Converters
import za.co.xisystems.itis_rrm.utils.DatetimeConverters

/**
 * Created by Francis Mahlava on 2019/10/23., exportSchema = false
 */

@Database(
    entities = [JobDTO::class, UserDTO::class, UserRoleDTO::class,
        ProjectItemDTO::class, ItemDTOTemp::class, JobDTOTemp::class,
        ContractDTO::class, VoItemDTO::class, ProjectDTO::class,
        ProjectSectionDTO::class, PrimaryKeyValueDTO::class, LookupOptionDTO::class,
        LookupDTO::class, ItemSectionDTO::class, WorkFlowDTO::class,
        SectionPointDTO::class, WorkFlowRouteDTO::class, JobSectionDTO::class,
        InfoClassDTO::class, ActivityDTO::class, ToDoGroupsDTO::class,
        JobItemEstimatesPhotoDTO::class, JobItemMeasurePhotoDTO::class, JobItemEstimateDTO::class,
        JobItemMeasureDTO::class, ToDoListEntityDTO::class, ChildLookupDTO::class,
        JobEstimateWorksDTO::class, JobEstimateWorksPhotoDTO::class, SectionItemDTO::class,
        WorkFlowsDTO::class, WfWorkStepDTO::class
    ],
    views = [ContractSelectorView::class],
    exportSchema = true,
    version = 19
)

@TypeConverters(Converters::class, DatetimeConverters::class)
@GenerateRoomMigrations

abstract class AppDatabase : RoomDatabase() {

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
    abstract fun getItemSectionDao(): ItemSectionDao
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
    abstract fun getJobDaoTemp(): JobDaoTemp
    abstract fun getItemDaoTemp(): ItemDaoTemp
    abstract fun getSectionPointDao(): SectionPointDao
    abstract fun getWorkStepDao(): WorkStepDao

    companion object {
        private const val MAX_DB_VERSIONS = 999_999_999
        @Volatile private var instance: AppDatabase? = null
        private val LOCK = Any()
        private var secretphrase: String? = null
        operator fun invoke(context: Context, armoury: XIArmoury) = instance ?: synchronized(LOCK) {
            secretphrase = armoury.readSecretPassphrase()
            instance ?: buildDatabase(context.applicationContext).also {
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
                    Timber.d("^*^ DB Pass: $passphrase")
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
    }
}
