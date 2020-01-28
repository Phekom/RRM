package za.co.xisystems.itis_rrm.data.localDB.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemMeasureDTOTemp
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemMeasurePhotoDTOTemp
import java.util.ArrayList

/**
 * Created by Francis Mahlava on 2019/11/21.
 */


@Dao
interface JobItemMeasureDao_Temp {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertJobItemMeasure( jobItemMeasure : JobItemMeasureDTOTemp) : Long

//    @Query("INSERT INTO JOB_ITEM_MEASURE_TEMP (itemMeasureId, jobId, projectItemId, qty, lineRate, startKm, endKm, jobDirectionId, recordVersion, recordSynchStateId, estimateId, projectVoId, cpa, lineAmount, measureDate, selectedItemUom ) VALUES ( :itemMeasureId, :jobId, :projectItemId, :qty, :lineRate, :startKm, :endKm, :jobDirectionId, :recordVersion, :recordSynchStateId, :estimateId, :projectVoId, :cpa, :lineAmount, :measureDate, :selectedItemUom )")
//    fun insertJobItemMeasure2(itemMeasureId: String, jobId: String, projectItemId: String, qty: Double, lineRate: Double, startKm: Double, endKm: Double, jobDirectionId: Int,
//         recordVersion: Int, recordSynchStateId: Int, estimateId: String, projectVoId: String, cpa: Int, lineAmount: Double, measureDate: String, selectedItemUom: String): Long

    @Query("UPDATE JOB_ITEM_MEASURE_TEMP SET jobItemMeasurePhotos =:jobItemMeasurePhotoList WHERE itemMeasureId = :itemMeasureId")
    fun upDatePhotList(jobItemMeasurePhotoList: JobItemMeasureDTOTemp, itemMeasureId: String)

//    fun insertJobItemMeasure2(projectId: String, descr: String?, endDate: String?,
//                      items: ArrayList<ItemDTO>?, projectCode: String?, projectMinus: String?, projectPlus: String?,
//                      projectSections: ArrayList<ProjectSectionDTO>?, voItems: ArrayList<VoItemDTO>?, contractId : String?) : Long

    @Query("SELECT * FROM JOB_ITEM_MEASURE_TEMP WHERE itemMeasureId = :itemMeasureId")
    fun checkIfJobItemMeasureExists(itemMeasureId: String): Boolean

    @Query("SELECT * FROM JOB_ITEM_MEASURE_TEMP WHERE actId = :actId ORDER BY jimNo ASC")
    fun getJobApproveMeasureForActivityId(actId: Int): LiveData<List<JobItemMeasureDTOTemp>>

    @Query("SELECT * FROM JOB_ITEM_MEASURE_TEMP WHERE jobId = :jobId AND actId = :actId ORDER BY jimNo ASC")
    fun getJobMeasureItemsForJobId(jobId: String?,actId: Int): LiveData<List<JobItemMeasureDTOTemp>>

    @Query("UPDATE JOB_ITEM_MEASURE_TEMP SET trackRouteId =:trackRouteId, ActId =:actId , measureGroupId =:measureGroupId  WHERE itemMeasureId = :itemMeasureId")
    fun updateWorkflowJobItemMeasure(itemMeasureId: String?, trackRouteId: String?, actId: Int, measureGroupId: String?)

    @Query("SELECT * FROM JOB_ITEM_MEASURE_TEMP WHERE estimateId = :estimateId AND jobId LIKE :jobId")
    fun checkIfJobItemMeasureExistsForJobIdAndEstimateId(jobId: String?, estimateId: String): Boolean

    @Query("SELECT * FROM JOB_ITEM_MEASURE_TEMP WHERE estimateId = :estimateId AND jobId LIKE :jobId")
    fun getJobItemMeasuresForJobIdAndEstimateId( jobId: String?, estimateId: String ): LiveData<List<JobItemMeasureDTOTemp>>

    @Query("SELECT * FROM JOB_ITEM_MEASURE_TEMP WHERE  jobId LIKE :jobId")
    fun getJobItemMeasuresForJobIdAndEstimateId( jobId: String?): LiveData<List<JobItemMeasureDTOTemp>>


    @Query("SELECT * FROM JOB_ITEM_MEASURE_TEMP WHERE jobId = :jobId ORDER BY jimNo ASC")
    fun getJobItemMeasureForJobId(jobId: String): LiveData<JobItemMeasureDTOTemp>



//    LiveData<List<JobItemMeasurePhotoDTO>>


//    @Query("SELECT * FROM PROJECT_ITEM_TABLE WHERE itemId = :itemId")
//    fun getItemForItemId(itemId: String): LiveData<ItemDTO>
//
//
//    @Query("SELECT * FROM PROJECT_ITEM_TABLE WHERE projectId = :projectId")
//    fun getAllItemsForProjectId(projectId: String): LiveData<List<ItemDTO>>
//
//
//    @Query("SELECT * FROM PROJECT_ITEM_TABLE WHERE sectionItemId = :sectionItem AND projectId = :projectId")
//    fun getAllItemsForSectionItem(sectionItem : String, projectId : String ): LiveData<List<ItemDTO>>
//
//
    @Query("DELETE FROM JOB_ITEM_MEASURE_TEMP")
    fun deleteAll()




}