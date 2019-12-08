package za.co.xisystems.itis_rrm.data.localDB

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import za.co.xisystems.itis_rrm.data.localDB.dao.*
import za.co.xisystems.itis_rrm.data.localDB.entities.*
import za.co.xisystems.itis_rrm.utils.Converters

/**
 * Created by Francis Mahlava on 2019/10/23., exportSchema = false
 */

@Database(
    entities = [JobDTO::class, UserDTO::class , UserRoleDTO::class, ItemDTO::class,
        ContractDTO::class, VoItemDTO::class, ProjectDTO::class , ProjectSectionDTO::class ,PrimaryKeyValueDTO::class
        , LookupOptionDTO::class ,LookupDTO::class ,EntitiesDTO::class ,ItemSectionDTO::class ,WorkFlowDTO::class
        , WorkFlowRouteDTO::class ,JobSectionDTO::class ,InfoClassDTO::class ,ActivityDTO::class , ToDoGroupsDTO::class
        , JobItemEstimatesPhotoDTO::class ,JobItemMeasurePhotoDTO::class ,JobItemEstimateDTO::class ,JobItemMeasureDTO::class
        , ToDoListEntityDTO::class , ChildLookupDTO::class,JobEstimateWorksDTO::class , JobEstimateWorksPhotoDTO::class
        ,SectionItemDTO::class,WorkFlowsDTO::class


    ],
    version = 1
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun getJobDao(): JobDao
    abstract fun getUserDao(): UserDao
    abstract fun getUserRoleDao(): UserRoleDao
    abstract fun getContractDao(): ContractDao
    abstract fun getVoItemDao(): VoItemDao
    abstract fun getProjectDao(): ProjectDao
    abstract fun getPrimaryKeyValueDao(): PrimaryKeyValueDao
    abstract fun getLookupOptionDao(): LookupOptionDao
    abstract fun getLookupDao(): LookupDao
    abstract fun getEntitiesDao(): EntitiesDao
    abstract fun getItemDao(): ItemDao
    abstract fun getItemSectionDao(): ItemSectionDao
    abstract fun getProjectSectionDao(): ProjectSectionDao
    abstract fun getWorkFlowDao(): WorkFlowDao
    abstract fun getWorkFlowRouteDao(): WorkFlowRouteDao
    abstract fun getJobSectionDao(): JobSectionDao
    abstract fun getInfoClassDao(): InfoClassDao
    abstract fun getActivityDao(): ActivityDao
    abstract fun getToDoGroupsDao(): ToDoGroupsDao
    abstract fun getEstimateWorkDao(): EstimateWorkDao
    abstract fun getSectionItemDao(): SectionItemDao
    abstract fun getWorkflowsDao(): WorkflowsDao





//    abstract fun getItemSectionDao(): WorkflowsDao
//    abstract fun getProjectSectionDao(): ProjectSectionDao
//
//    abstract fun getWorkFlowRouteDao(): WorkFlowRouteDao





    companion object {

        @Volatile
        private var instance: AppDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
            instance ?: buildDatabase(context).also {
                instance = it
            }
        }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "myRRM_Database.db"

            ).build()


    }
}