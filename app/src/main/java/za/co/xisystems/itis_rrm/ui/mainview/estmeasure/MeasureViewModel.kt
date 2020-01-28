package za.co.xisystems.itis_rrm.ui.mainview.estmeasure

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import za.co.xisystems.itis_rrm.data.localDB.entities.*
import za.co.xisystems.itis_rrm.data.repositories.OfflineDataRepository
import za.co.xisystems.itis_rrm.ui.mainview.estmeasure.estimate_measure_item.EstimateMeasure_Item
import za.co.xisystems.itis_rrm.utils.lazyDeferred

class MeasureViewModel (
    private val offlineDataRepository: OfflineDataRepository
) : ViewModel() {

//    val offlinedata by lazyDeferred {
//        offlineDataRepository.getSectionItems()
//        offlineDataRepository.getContracts()
//    }

//    val measure_Item = MutableLiveData<String>()
//    fun Item5(measurea: String) {
//        measure_Item.value = measurea
//    }

    val user by lazyDeferred {
        offlineDataRepository.getUser()
    }



    val measurea1_Item1 = MutableLiveData<JobItemMeasureDTOTemp>()
    fun Item1(measurea1: JobItemMeasureDTOTemp) {
        measurea1_Item1.value = measurea1
    }

    val measurea1_Item2 = MutableLiveData<JobItemMeasureDTOTemp>()
    fun Item2(measurea2: JobItemMeasureDTOTemp) {
        measurea1_Item2.value = measurea2
    }


    val measurea1_Item3 = MutableLiveData<List<JobItemMeasurePhotoDTOTemp>>()
    fun Item3(measurea3: List<JobItemMeasurePhotoDTOTemp>) {
        measurea1_Item3.value = measurea3
    }


    val measure_Item = MutableLiveData<EstimateMeasure_Item>()
    fun Item5(measurea: EstimateMeasure_Item) {
        measure_Item.value = measurea
    }

    suspend fun getJobMeasureForActivityId(activityId: Int): LiveData<List<JobItemEstimateDTO>> {
        return withContext(Dispatchers.IO) {
            offlineDataRepository.getJobMeasureForActivityId(activityId)
        }
    }
    suspend fun getProjectSectionIdForJobId(jobId: String): String {
        return withContext(Dispatchers.IO) {
            offlineDataRepository.getProjectSectionIdForJobId(jobId)
        }
    }

    suspend fun getRouteForProjectSectionId(sectionId: String): String {
        return withContext(Dispatchers.IO) {
            offlineDataRepository.getRouteForProjectSectionId(sectionId)
        }
    }
    suspend fun getSectionForProjectSectionId(sectionId: String): String {
        return withContext(Dispatchers.IO) {
            offlineDataRepository.getSectionForProjectSectionId(sectionId)
        }
    }
    suspend fun getItemDescription(jobId: String): String {
        return withContext(Dispatchers.IO) {
            offlineDataRepository.getItemDescription(jobId)
        }
    }
    suspend fun getDescForProjectItemId(projectItemId: String): String {
        return withContext(Dispatchers.IO) {
            offlineDataRepository.getProjectItemDescription(projectItemId)
        }
    }
    suspend fun getItemJobNo(jobId: String): String {
        return withContext(Dispatchers.IO) {
            offlineDataRepository.getItemJobNo(jobId)
        }
    }

//   fun getItemJobNo(jobId: String): String {
//        return withContext(Dispatchers.IO) {
//            offlineDataRepository.getItemJobNo(jobId)
//        }
//    }


    suspend  fun checkIfJobItemMeasureExistsForJobIdAndEstimateId(jobId: String?, estimateId: String): Boolean {
        return withContext(Dispatchers.IO) {
            offlineDataRepository.checkIfJobItemMeasureExistsForJobIdAndEstimateId(jobId, estimateId)
        }
    }

//    suspend fun getJobItemMeasuresForJobIdAndEstimateId( jobId: String?, estimateId: String ): List<JobItemMeasureDTO> {
//        return withContext(Dispatchers.IO) {
//             offlineDataRepository.getJobItemMeasuresForJobIdAndEstimateId(jobId, estimateId)
//        }
//    }

    suspend fun getJobItemMeasuresForJobIdAndEstimateId(
        jobId: String?,
        estimateId: String
    ): LiveData<List<JobItemMeasureDTOTemp>> {
        return withContext(Dispatchers.IO) {
            offlineDataRepository.getJobItemMeasuresForJobIdAndEstimateId(jobId,estimateId)
        }
    }

    suspend fun getJobItemMeasuresForJobIdAndEstimateId2(
        jobId: String?,
        estimateId: String,
        jobItemMeasureArrayList: ArrayList<JobItemMeasureDTOTemp>
    ): LiveData<List<JobItemMeasureDTOTemp>> {
        return withContext(Dispatchers.IO) {
            offlineDataRepository.getJobItemMeasuresForJobIdAndEstimateId2(jobId, estimateId,jobItemMeasureArrayList)
        }
    }
//    suspend fun getJobItemsToMeasureForJobId(activityId: Int): LiveData<List<JobItemEstimateDTO>> {
//        return withContext(Dispatchers.IO) {
//            offlineDataRepository.getJobMeasureForActivityId(activityId)
//        }
//    }
//
   suspend fun getJobItemsToMeasureForJobId(jobID: String?): LiveData<List<JobItemEstimateDTO>>  {
        return withContext(Dispatchers.IO) {
            offlineDataRepository.getJobItemsToMeasureForJobId(jobID)
        }
    }

   suspend fun getItemForItemId(projectItemId: String?): LiveData<ProjectItemDTO> {
                return withContext(Dispatchers.IO) {
            offlineDataRepository.getItemForItemId(projectItemId)
        }
    }
        suspend fun getJobFromJobId(jobId: String?):LiveData<JobDTO> {
        return withContext(Dispatchers.IO) {
            offlineDataRepository.getSingleJobFromJobId(jobId)
        }
    }

    suspend fun setJobItemMeasureImages(
        jobItemMeasurePhotoList: ArrayList<JobItemMeasurePhotoDTOTemp>,
        estimateId: String?
    ) {
        offlineDataRepository.setJobItemMeasureImages(jobItemMeasurePhotoList,estimateId)
    }

    suspend fun saveJobItemMeasureItems(jobItemMeasureDTO: ArrayList<JobItemMeasureDTOTemp>) {
        offlineDataRepository.saveJobItemMeasureItems(jobItemMeasureDTO)
    }

    suspend fun getJobItemMeasureForJobId(jobId: String?) :LiveData<JobItemMeasureDTOTemp>{
        return withContext(Dispatchers.IO) {
            offlineDataRepository.getJobItemMeasureForJobId(jobId)
        }
    }

    suspend fun getJobItemMeasurePhotosForItemMeasureID(itemMeasureId: String): LiveData<List<JobItemMeasurePhotoDTOTemp>> {
        return withContext(Dispatchers.IO) {
            offlineDataRepository.getJobItemMeasurePhotosForItemMeasureID(itemMeasureId)
        }
    }
    suspend fun getJobItemMeasurePhotosForItemEstimateID(estimateId: String): LiveData<List<JobItemMeasurePhotoDTOTemp>> {
        return withContext(Dispatchers.IO) {
            offlineDataRepository.getJobItemMeasurePhotosForItemEstimateID(estimateId)
        }
    }
    suspend fun getPhotoForJobItemMeasure(filename: String) {
        return withContext(Dispatchers.IO) {
            offlineDataRepository.getPhotoForJobItemMeasure(filename)
        }
    }
    suspend fun getJobMeasureItemsPhotoPath(itemMeasureId: String): String {
        return withContext(Dispatchers.IO) {
            offlineDataRepository.getJobMeasureItemsPhotoPath2(itemMeasureId)
        }
    }

    suspend fun processWorkflowMove(
        userId: String,
        jobId: String,
        jimNo: String?,
        contractVoId: String?,
        mSures: ArrayList<JobItemMeasureDTOTemp>
    ) {
        return withContext(Dispatchers.IO) {
            offlineDataRepository.saveMeasurementItems( userId, jobId,jimNo,contractVoId,mSures)
        }

    }

    suspend fun processImageUpload(
        filename: String,
        extension: String,
        photo: ByteArray
    ) {
        return withContext(Dispatchers.IO) {
            offlineDataRepository.imageUpload( filename,extension,photo)
        }

    }


//    suspend fun createJobItemMeasureItem(selectedItemMeasure: ItemDTO?, quantity: Double, jobForJobItemEstimate: JobDTO, selectedJobItemEstimate: JobItemEstimateDTO, jobItemMeasurePhotoDTO: ArrayList<JobItemMeasurePhotoDTO>) {
//        offlineDataRepository.createJobItemMeasureItem(selectedItemMeasure!!,quantity,jobForJobItemEstimate,selectedJobItemEstimate,jobItemMeasurePhotoDTO)
//    }

//    suspend  fun setJobItemMeasure( jobId: String, projectItemId: String?, quantity: Double,  lineRate: Double, startKm: Double,  endKm: Double, jobDirectionId: Int,
//        recordVersion : Int, recordSynchStateId : Int,  estimateId: String, projectVoId: String,  cpa: Int,   lineAmount: Double,  date: String,  uom: String?
//    ) : LiveData<JobItemMeasureDTO>{
//        return withContext(Dispatchers.IO) {
//            offlineDataRepository.setJobItemMeasure(jobId, projectItemId, quantity,  lineRate, startKm,  endKm, jobDirectionId,
//            recordVersion , recordSynchStateId ,  estimateId, projectVoId,  cpa,   lineAmount,  date,  uom)
//        }
//    }

}