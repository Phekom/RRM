package za.co.xisystems.itis_rrm.ui.mainview.estmeasure

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import za.co.xisystems.itis_rrm.data.localDB.entities.*
import za.co.xisystems.itis_rrm.data.repositories.MeasureCreationDataRepository
import za.co.xisystems.itis_rrm.data.repositories.OfflineDataRepository
import za.co.xisystems.itis_rrm.ui.mainview.estmeasure.estimate_measure_item.EstimateMeasure_Item
import za.co.xisystems.itis_rrm.utils.lazyDeferred

class MeasureViewModel (
    private val measureCreationDataRepository: MeasureCreationDataRepository,
    private val offlineDataRepository: OfflineDataRepository
) : ViewModel() {

    val offlinedatas by lazyDeferred {
        offlineDataRepository.getUserTaskList()
    }

//    val measure_Item = MutableLiveData<String>()
//    fun Item5(measurea: String) {
//        measure_Item.value = measurea
//    }

    val user by lazyDeferred {
        measureCreationDataRepository.getUser()
    }



    val measurea1_Item1 = MutableLiveData<JobItemMeasureDTO>()
    fun Item1(measurea1: JobItemMeasureDTO) {
        measurea1_Item1.value = measurea1
    }

    val measurea1_Item2 = MutableLiveData<JobItemMeasureDTO>()
    fun Item2(measurea2: JobItemMeasureDTO) {
        measurea1_Item2.value = measurea2
    }


    val measurea1_Item3 = MutableLiveData<List<JobItemMeasurePhotoDTO>>()
    fun Item3(measurea3: List<JobItemMeasurePhotoDTO>) {
        measurea1_Item3.value = measurea3
    }


    val measure_Item = MutableLiveData<EstimateMeasure_Item>()
    fun Item5(measurea: EstimateMeasure_Item) {
        measure_Item.value = measurea
    }

    suspend fun getJobMeasureForActivityId(
        activityId: Int,
        activityId2: Int
    ): LiveData<List<JobItemEstimateDTO>> {
        return withContext(Dispatchers.IO) {
            measureCreationDataRepository.getJobMeasureForActivityId(activityId, activityId2)
        }
    }


//    suspend fun getJobMeasureForActivityId(activityId: Int): LiveData<List<JobItemEstimateDTO>> {
//        return withContext(Dispatchers.IO) {
//            measureDataRepository.getJobMeasureForActivityId(activityId)
//        }
//    }


    suspend fun getProjectSectionIdForJobId(jobId: String): String {
        return withContext(Dispatchers.IO) {
            measureCreationDataRepository.getProjectSectionIdForJobId(jobId)
        }
    }

    suspend fun getRouteForProjectSectionId(sectionId: String): String {
        return withContext(Dispatchers.IO) {
            measureCreationDataRepository.getRouteForProjectSectionId(sectionId)
        }
    }
    suspend fun getSectionForProjectSectionId(sectionId: String): String {
        return withContext(Dispatchers.IO) {
            measureCreationDataRepository.getSectionForProjectSectionId(sectionId)
        }
    }
    suspend fun getItemDescription(jobId: String): String {
        return withContext(Dispatchers.IO) {
            measureCreationDataRepository.getItemDescription(jobId)
        }
    }
    suspend fun getDescForProjectItemId(projectItemId: String): String {
        return withContext(Dispatchers.IO) {
            measureCreationDataRepository.getProjectItemDescription(projectItemId)
        }
    }
    suspend fun getItemJobNo(jobId: String): String {
        return withContext(Dispatchers.IO) {
            measureCreationDataRepository.getItemJobNo(jobId)
        }
    }


    suspend fun getJobMeasureItemsPhotoPath(itemMeasureId: String): String {
        return withContext(Dispatchers.IO) {
            measureCreationDataRepository.getJobMeasureItemsPhotoPath(itemMeasureId)
        }
    }

    suspend fun deleteItemMeasurefromList(itemMeasureId: String) {
        measureCreationDataRepository.deleteItemMeasurefromList(itemMeasureId)
    }
    suspend fun deleteItemMeasurephotofromList(itemMeasureId: String) {
        measureCreationDataRepository.deleteItemMeasurephotofromList(itemMeasureId)
    }
    suspend fun processWorkflowMove(
        userId: String,
        jobId: String,
        jimNo: String?,
        contractVoId: String?,
        mSures: ArrayList<JobItemMeasureDTO>,
        activity: FragmentActivity,
        itemMeasureJob: JobDTO
    ) : String {
        return withContext(Dispatchers.IO) {
            measureCreationDataRepository.saveMeasurementItems( userId, jobId,jimNo,contractVoId,mSures, activity, itemMeasureJob)
        }

    }

    suspend fun getJobItemMeasuresForJobIdAndEstimateId(
        jobId: String?,
        estimateId: String
    ): LiveData<List<JobItemMeasureDTO>> {
        return withContext(Dispatchers.IO) {
            measureCreationDataRepository.getJobItemMeasuresForJobIdAndEstimateId(jobId,estimateId)
        }
    }

    suspend fun getJobItemMeasuresForJobIdAndEstimateId2(
        jobId: String?,
        estimateId: String
     //   ,jobItemMeasureArrayList: ArrayList<JobItemMeasureDTO>
    ): LiveData<List<JobItemMeasureDTO>> {
        return withContext(Dispatchers.IO) {
            measureCreationDataRepository.getJobItemMeasuresForJobIdAndEstimateId2(jobId, estimateId)//,jobItemMeasureArrayList
        }
    }

    suspend fun getJobItemsToMeasureForJobId(jobID: String?): LiveData<List<JobItemEstimateDTO>>  {
        return withContext(Dispatchers.IO) {
            measureCreationDataRepository.getJobItemsToMeasureForJobId(jobID)
        }
    }

    suspend fun getItemForItemId(projectItemId: String?): LiveData<ProjectItemDTO> {
        return withContext(Dispatchers.IO) {
            measureCreationDataRepository.getItemForItemId(projectItemId)
        }
    }
    suspend fun getJobFromJobId(jobId: String?):LiveData<JobDTO> {
        return withContext(Dispatchers.IO) {
            measureCreationDataRepository.getSingleJobFromJobId(jobId)
        }
    }

    suspend fun setJobItemMeasureImages(
        jobItemMeasurePhotoList: ArrayList<JobItemMeasurePhotoDTO>,
        estimateId: String?,
        selectedJobItemMeasure: JobItemMeasureDTO
    ) {
        measureCreationDataRepository.setJobItemMeasureImages(jobItemMeasurePhotoList,estimateId, selectedJobItemMeasure)
    }

    suspend fun saveJobItemMeasureItems(jobItemMeasureDTO: ArrayList<JobItemMeasureDTO>) {
        measureCreationDataRepository.saveJobItemMeasureItems(jobItemMeasureDTO)
    }

    suspend fun getJobItemMeasurePhotosForItemEstimateID(estimateId: String): LiveData<List<JobItemMeasurePhotoDTO>> {
        return withContext(Dispatchers.IO) {
            measureCreationDataRepository.getJobItemMeasurePhotosForItemEstimateID(estimateId)
        }
    }





    //   fun getItemJobNo(jobId: String): String {
//        return withContext(Dispatchers.IO) {
//            measureDataRepository.getItemJobNo(jobId)
//        }
//    }

//    suspend  fun checkIfJobItemMeasureExistsForJobIdAndEstimateId(jobId: String?, estimateId: String): Boolean {
//        return withContext(Dispatchers.IO) {
//            measureDataRepository.checkIfJobItemMeasureExistsForJobIdAndEstimateId(jobId, estimateId)
//        }
//    }

//    suspend fun getJobItemMeasuresForJobIdAndEstimateId( jobId: String?, estimateId: String ): List<JobItemMeasureDTO> {
//        return withContext(Dispatchers.IO) {
//             measureDataRepository.getJobItemMeasuresForJobIdAndEstimateId(jobId, estimateId)
//        }
//    }

//    suspend fun getJobItemsToMeasureForJobId(activityId: Int): LiveData<List<JobItemEstimateDTO>> {
//        return withContext(Dispatchers.IO) {
//            measureDataRepository.getJobMeasureForActivityId(activityId)
//        }
//    }
//


//    suspend fun getJobItemMeasureForJobId(jobId: String?) :LiveData<JobItemMeasureDTO>{
//        return withContext(Dispatchers.IO) {
//            measureDataRepository.getJobItemMeasureForJobId(jobId)
//        }
//    }
//
//    suspend fun getJobItemMeasurePhotosForItemMeasureID(itemMeasureId: String): LiveData<List<JobItemMeasurePhotoDTO>> {
//        return withContext(Dispatchers.IO) {
//            measureDataRepository.getJobItemMeasurePhotosForItemMeasureID(itemMeasureId)
//        }
//    }
//
//    suspend fun getPhotoForJobItemMeasure(filename: String) {
//        return withContext(Dispatchers.IO) {
//            measureDataRepository.getPhotoForJobItemMeasure(filename)
//        }
//    }


//    suspend fun processImageUpload(
//        filename: String,
//        extension: String,
//        photo: ByteArray
//    ) {
//        return withContext(Dispatchers.IO) {
//            measureDataRepository.imageUpload( filename,extension,photo)
//        }
//
//    }





//    suspend fun createJobItemMeasureItem(selectedItemMeasure: ItemDTO?, quantity: Double, jobForJobItemEstimate: JobDTO, selectedJobItemEstimate: JobItemEstimateDTO, jobItemMeasurePhotoDTO: ArrayList<JobItemMeasurePhotoDTO>) {
//        measureDataRepository.createJobItemMeasureItem(selectedItemMeasure!!,quantity,jobForJobItemEstimate,selectedJobItemEstimate,jobItemMeasurePhotoDTO)
//    }

//    suspend  fun setJobItemMeasure( jobId: String, projectItemId: String?, quantity: Double,  lineRate: Double, startKm: Double,  endKm: Double, jobDirectionId: Int,
//        recordVersion : Int, recordSynchStateId : Int,  estimateId: String, projectVoId: String,  cpa: Int,   lineAmount: Double,  date: String,  uom: String?
//    ) : LiveData<JobItemMeasureDTO>{
//        return withContext(Dispatchers.IO) {
//            measureDataRepository.setJobItemMeasure(jobId, projectItemId, quantity,  lineRate, startKm,  endKm, jobDirectionId,
//            recordVersion , recordSynchStateId ,  estimateId, projectVoId,  cpa,   lineAmount,  date,  uom)
//        }
//    }

}