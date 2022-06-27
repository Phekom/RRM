package za.co.xisystems.itis_rrm.utils.image_capture.camera

import kotlinx.coroutines.withContext
import za.co.xisystems.itis_rrm.base.BaseViewModel
import za.co.xisystems.itis_rrm.data._commons.utils.SqlLitUtils
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimatesPhotoDTO
import za.co.xisystems.itis_rrm.data.repositories.CapturedPictureRepository
import za.co.xisystems.itis_rrm.forge.DefaultDispatcherProvider
import za.co.xisystems.itis_rrm.forge.DispatcherProvider
import za.co.xisystems.itis_rrm.services.LocationModel
import za.co.xisystems.itis_rrm.utils.DateUtil
import za.co.xisystems.itis_rrm.utils.PhotoUtil

import java.util.*

class CaptureImagesViewModel(
    private val dataRepository: CapturedPictureRepository,
    val photoUtil: PhotoUtil,
    val dispatchers: DispatcherProvider = DefaultDispatcherProvider()
) : BaseViewModel() {
//    photoUtil = photoUtil

    suspend fun createUnallocatedPhoto(
        filenamePath: Map<String, String>,
        currentLocation: LocationModel,
        pointLocation: Double,
        itemidPhototype: Map<String, String>
    ): JobItemEstimatesPhotoDTO = withContext(ioContext) {
        val isPhotoStart = itemidPhototype["type"] == "UnAllocated"
        val photoId = SqlLitUtils.generateUuid()

        return@withContext JobItemEstimatesPhotoDTO(
            descr = itemidPhototype["type"]!!,
            estimateId = "",
            filename = filenamePath["filename"] ?: error(""),
            photoDate = DateUtil.dateToString(Date())!!,
            photoId = photoId,
            photoStart = null,
            photoEnd = null,
            startKm = pointLocation,
            endKm = pointLocation,
            photoLatitude = currentLocation.latitude,
            photoLongitude = currentLocation.longitude,
            photoLatitudeEnd = currentLocation.latitude,
            photoLongitudeEnd = currentLocation.longitude,
            photoPath = filenamePath["path"] ?: error(""),
            recordSynchStateId = 0,
            recordVersion = 0,
            isPhotostart = false,
            sectionMarker = currentLocation.toString()
        )
    }


//    suspend fun getDisasterLogForID(selectedID: String): NewDamageEntryDTO {
//        return withContext(dispatchers.io()) {
//            dataRepository.getDisasterLogForID(selectedID)
//        }
//    }
//
////    suspend fun getInspectionForID(inspectionId: String?): DamageInspectionDTO {
////        return withContext(Dispatchers.IO) {
////            dataRepository.getInspectionForID(inspectionId)
////        }
////    }
//
//    suspend fun savePhotos(photos: ArrayList<MobileAppPhotoDTO>, fromSource : String ) : String {
//        return withContext(dispatchers.io()) {
//            dataRepository.savePhotos(photos, fromSource)
//        }
//    }
//
//    suspend fun getDefectInspectionForID(
//        selectedInspectionId: String?
//    ): DefectsDTO {
//        return withContext(dispatchers.io()) {
//            dataRepository.getDefectInspectionForID(selectedInspectionId!!)
//        }
//    }
//
//    suspend fun getSaveDefect(itemDefects: DefectsDTO) {
//        return withContext(dispatchers.io()) {
//            dataRepository.getSaveDefect(itemDefects)
//        }
//    }


}