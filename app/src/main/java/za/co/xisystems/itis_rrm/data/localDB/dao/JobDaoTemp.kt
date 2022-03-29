/**
 * Updated by Shaun McDonald on 2021/05/15
 * Last modified on 2021/05/14, 20:59
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 **/

/**
 * Updated by Shaun McDonald on 2021/05/14
 * Last modified on 2021/05/14, 19:43
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 **/

package za.co.xisystems.itis_rrm.data.localDB.dao

//import androidx.lifecycle.LiveData
//import androidx.room.Dao
//import androidx.room.Insert
//import androidx.room.OnConflictStrategy
//import androidx.room.Query
//import java.util.ArrayList
//import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTOTemp
//import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimateDTO
//import za.co.xisystems.itis_rrm.data.localDB.entities.JobSectionDTO
//
///**
// * Created by Francis Mahlava on 2019/11/21.
// */
//
//@Dao
//interface JobDaoTemp {
//
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    fun insertOrUpdateJobs(jobs: JobDTOTemp)
//
//    @Query("SELECT * FROM JOB_TABLE_TEMP WHERE jobId = :jobId")
//    fun checkIfJobExist(jobId: String): Boolean
//
//    @Query("UPDATE JOB_TABLE_TEMP SET route =:route, section =:section WHERE jobId = :jobId")
//    fun updateAllJobs(route: String?, section: String?, jobId: String?)
//
//    @Query("UPDATE JOB_TABLE_TEMP SET trackRouteId =:trackRouteId, actId =:actId, jiNo =:jiNo WHERE jobId = :jobId")
//    fun updateJob(jobId: String?, actId: Int, trackRouteId: String?, jiNo: String?)
//
//    @Query("UPDATE JOB_TABLE_TEMP SET sectionId =:sectionId ,startKm =:startKM , endKm =:endKM ,JobItemEstimates =:newJobItemEstimatesList, jobSections =:jobItemSectionArrayList  WHERE jobId = :newjobId ")
//    fun updateJoSecId(
//        newjobId: String,
//        startKM: Double,
//        endKM: Double,
//        sectionId: String,
//        newJobItemEstimatesList: ArrayList<JobItemEstimateDTO>,
//        jobItemSectionArrayList: ArrayList<JobSectionDTO>
//    )
//
//    @Query("SELECT descr FROM JOB_TABLE_TEMP WHERE jobId = :jobId")
//    fun getItemDescription(jobId: String): String
//
//    @Query("SELECT jiNo FROM JOB_TABLE_TEMP WHERE jobId = :jobId")
//    fun getItemJobNo(jobId: String): String
//
//    @Query("SELECT startKm FROM JOB_TABLE_TEMP WHERE jobId = :jobId")
//    fun getItemStartKm(jobId: String): Double
//
//    @Query("SELECT endKm FROM JOB_TABLE_TEMP WHERE jobId = :jobId")
//    fun getItemEndKm(jobId: String): Double
//
//    @Query("SELECT trackRouteId FROM JOB_TABLE_TEMP WHERE jobId = :jobId")
//    fun getItemTrackRouteId(jobId: String): String
//
//    @Query("SELECT * FROM JOB_TABLE_TEMP ")
//    fun getAllJobsForAllProjects(): LiveData<List<JobDTOTemp>>
//
//    @Query("SELECT * FROM JOB_TABLE_TEMP WHERE actId = :actId ORDER BY jiNo ASC")
//    fun getJobsForActivityId(actId: Int): LiveData<List<JobDTOTemp>>
//
//    @Query("SELECT * FROM JOB_TABLE_TEMP WHERE jobId = :jobId")
//    fun getJobForJobId(jobId: String): JobDTOTemp
//
//    @Query("SELECT * FROM JOB_TABLE_TEMP WHERE jobId = :jobId")
//    fun getJobFromJobId(jobId: String): LiveData<JobDTOTemp>
//
//    @Query("DELETE FROM JOB_TABLE_TEMP WHERE jobId = :jobId")
//    fun deleteJobForJobId(jobId: String)
//
//    @Query("DELETE FROM JOB_TABLE_TEMP")
//    fun deleteAll()
//}
